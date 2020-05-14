package iwb.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.metadata.rdbms.PostgreSQLWriter;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.domain.db.Log5Feed;
import iwb.domain.db.Log5WorkflowRecord;
import iwb.domain.db.W5Conversion;
import iwb.domain.db.W5ConvertedObject;
import iwb.domain.db.W5FormModule;
import iwb.domain.db.W5Table;
import iwb.domain.db.W5TableChild;
import iwb.domain.db.W5TableEvent;
import iwb.domain.db.W5Workflow;
import iwb.domain.db.W5WorkflowRecord;
import iwb.domain.db.W5WorkflowStep;
import iwb.domain.helper.W5QueuedActionHelper;
import iwb.domain.helper.W5SynchAfterPostHelper;
import iwb.domain.helper.W5TableRecordHelper;
import iwb.domain.result.W5FormResult;
import iwb.domain.result.W5GlobalFuncResult;
import iwb.exception.IWBException;
import iwb.util.DBUtil;
import iwb.util.GenericUtil;
import iwb.util.NashornUtil;

@Component
public class CRUDEngine {
	@Lazy
	@Autowired
	private PostgreSQL dao;

	@Lazy
	@Autowired
	private MetadataLoader metadataLoader;

	@Lazy
	@Autowired
	private PostgreSQLWriter metadataWriter;
	
	@Lazy
	@Autowired
	private ConversionEngine conversionEngine;

	@Lazy
	@Autowired
	private QueryEngine queryEngine;

	@Lazy
	@Autowired
	private WorkflowEngine workflowEngine;

	@Lazy
	@Autowired
	private GlobalScriptEngine scriptEngine;
	

	@Lazy
	@Autowired
	private NotificationEngine notificationEngine;
	
	
	@Lazy
	@Autowired 
	private AccessControlEngine acEngine;

	@SuppressWarnings({ "unused", "unchecked" })
	public List<W5QueuedActionHelper> postForm4Table(W5FormResult formResult, String paramSuffix,
			Set<String> checkedParentRecords) {
		try {
			dao.checkTenant(formResult.getScd());
			List<W5QueuedActionHelper> result = new ArrayList<W5QueuedActionHelper>();
			int formId = formResult.getFormId();
			int action = formResult.getAction();
			int realAction = action;
			Map<String, Object> scd = formResult.getScd();
			Map<String, String> requestParams = formResult.getRequestParams();

			metadataWriter.beforePostForm(formResult, dao, paramSuffix);
			boolean dev = scd.get("roleId") != null && (Integer) scd.get("roleId") == 0
					&& GenericUtil.uInt(requestParams, "_dev") != 0;
			String projectId = dev ? FrameworkSetting.devUuid : (String) scd.get("projectId");
			W5Table t = FrameworkCache.getTable(projectId, formResult.getForm().getObjectId()); // formResult.getForm().get_sourceTable();

			String schema = null;
			W5Workflow workflow = null;
			W5WorkflowRecord workflowRecord = null;
			W5WorkflowStep workflowStep = null;
			boolean accessControlSelfFlag = true; // kendisi VEYA kendisi+master
			if (accessControlSelfFlag) {
				int outCnt = formResult.getOutputMessages().size();
				acEngine.accessControl4FormTable(formResult, paramSuffix);
				if (formResult.isViewMode()) {
					throw new IWBException("security", "Form", formId, null,
							formResult.getOutputMessages().size() > outCnt ? formResult.getOutputMessages().get(outCnt)
									: LocaleMsgCache.get2(0, (String) scd.get("locale"),
											"fw_security_table_control_update"),
							null);
				}
				/*
				if (FrameworkSetting.workflow) {
					appRecord = formResult.getApprovalRecord();
					if (appRecord != null) {
						approval = FrameworkCache.getWorkflow(scd, appRecord.getApprovalId()); // dao.loadObject(W5Workflow.class,
						approvalStep = formResult.getApprovalStep();
						// formResult.getApprovalRecord().getApprovalId());
						boolean canCancel = GenericUtil.hasPartInside2(approval.getAfterFinUpdateUserIds(),
								scd.get("userId")) && appRecord.getApprovalActionTip() == 5
								&& appRecord.getApprovalStepId() == 998 ? true : false;
//						approvalStep = approval.get_approvalStepMap().get(appRecord.getApprovalStepId()).getNewInstance();
						if (approvalStep != null && approvalStep.getApprovalStepId() != 901
								&& approvalStep.getUpdatableFields() == null && !canCancel) {
							throw new IWBException("security", "Form", formId, null,
									LocaleMsgCache.get2(0, (String) scd.get("locale"),
											"fw_onay_sureci_icerisinde_bu_kaydin_alanlarini_guncelleyemezsiniz"),
									null);
						} 
					}
				} */
			}
			boolean mobile = GenericUtil.uInt(formResult.getScd().get("mobile")) != 0;
			int sourceStepId = -1;
			String ptablePk = null; // accessControl islemi icin
			String pcopyTablePk = null; // accessControl islemi icin
			Log5Feed feed = null;
			int feedTableId = 0, feedTablePk = 0;
			// once load edilmis eski objeye gerek var mi? wdiget'lar icin
			// gerekir, condition olan yerler
			// icin de gerekebilir
			Map<String, Object> oldObj = null;

			if (action == 9 /* edit (if not insert) */) {
				action = dao.checkIfRecordsExists(scd, requestParams, t) ? 1 : 2;
			}

			List<W5TableEvent> tla = FrameworkCache.getTableEvents(projectId, t.getTableId());
			/* tableTrigger Before Action start */
			if (tla != null)
				extFormTableEvent(formResult, new String[] { "_", "bu", "bi", "bd", "_", "bi" }[action], scd,
						requestParams, t, requestParams.get(t.get_tableParamList().get(0).getDsc() + paramSuffix),
						paramSuffix);
			/* end of tableTrigger */

			switch (action) {
			case 1: // update
				ptablePk = requestParams.get(t.get_tableParamList().get(0).getDsc() + paramSuffix);
				if (GenericUtil.isEmpty(paramSuffix)) {
					formResult.setPkFields(new HashMap());
					formResult.getPkFields().put(t.get_tableParamList().get(0).getDsc(), ptablePk);
				}
				if (FrameworkSetting.workflow && accessControlSelfFlag) {

					if (workflowRecord == null && t.get_approvalMap() != null) { // su
																			// anda
																			// bir
																			// onay
																			// icinde
																			// degil
																			// ve
																			// onay
																			// mekanizmasi
																			// var
																			// mi
																			// bunda?
						workflow = t.get_approvalMap().get((short) 1); // action=1
																		// for
																		// update

						if (workflow != null && workflow.getActiveFlag() != 0) { // update workflow
							Map<String, Object> advancedStepSqlResult = null;
							if (workflow.getAdvancedBeginSql() != null
									&& workflow.getAdvancedBeginSql().length() > 10) { // calisacak
								advancedStepSqlResult = dao.runSQLQuery2Map(workflow.getAdvancedBeginSql(), scd,
										requestParams, null);
								// donen bir cevap var, aktive_flag deger olarak
								// var ve onun degeri 0 ise o zaman
								// girmeyecek
								if (advancedStepSqlResult != null && advancedStepSqlResult.get("active_flag") != null
										&& GenericUtil.uInt(advancedStepSqlResult.get("active_flag")) == 0) { // girmeyecek
									workflow = null; // approval olmayacak
								}
							}
							if (workflow != null) { // has workflow?
								workflowStep = null;
								if (workflow.getApprovalFlowTip() == 0) { // simple
									workflowStep = workflow.get_approvalStepList().get(0).getNewInstance();
								} else { // complex
									if (advancedStepSqlResult != null
											&& advancedStepSqlResult.get("approval_step_id") != null
											&& GenericUtil.uInt(advancedStepSqlResult.get("approval_step_id")) != 0)
										workflowStep = workflow.get_approvalStepMap()
												.get(GenericUtil.uInt(advancedStepSqlResult.get("approval_step_id")));
									else
										workflowStep = workflow.get_approvalStepList().get(0).getNewInstance();
								}
								if (workflowStep != null) { // step hazir
									workflowRecord = new W5WorkflowRecord(scd, workflowStep.getApprovalStepId(),
											workflow.getWorkflowId(), formResult.getForm().getObjectId(), (short) 0,
											workflowStep.getReturnFlag());
									boolean bau = advancedStepSqlResult != null
											&& advancedStepSqlResult.get("approval_users") != null;
									workflowRecord
											.setApprovalUsers(bau ? (String) advancedStepSqlResult.get("approval_users")
													: workflowStep.getApprovalUsers());
									workflowRecord
											.setApprovalRoles(bau ? (String) advancedStepSqlResult.get("approval_roles")
													: workflowStep.getApprovalRoles());
									boolean bavt = advancedStepSqlResult != null
											&& advancedStepSqlResult.get("access_view_tip") != null;
									workflowRecord
											.setAccessViewTip(bavt
													? (short) GenericUtil
															.uInt(advancedStepSqlResult.get("access_view_tip"))
													: workflowStep.getAccessViewTip());
									workflowRecord.setAccessViewRoles(
											bavt ? (String) advancedStepSqlResult.get("access_view_roles")
													: workflowStep.getAccessViewRoles());
									workflowRecord.setAccessViewUsers(
											bavt ? (String) advancedStepSqlResult.get("access_view_users")
													: workflowStep.getAccessViewUsers());
									if (workflowRecord.getAccessViewTip() != 0 && !GenericUtil
											.hasPartInside2(workflowRecord.getAccessViewUsers(), scd.get("userId"))) // goruntuleme
																												// kisiti
																												// var
																												// ve
																												// kendisi
																												// goremiyorsa,
										// kendisini de ekle
										workflowRecord.setAccessViewUsers(workflowRecord.getAccessViewUsers() != null
												? workflowRecord.getAccessViewUsers() + "," + scd.get("userId")
												: scd.get("userId").toString());
								} else {
									throw new IWBException("framework", "Workflow", workflow.getWorkflowId(), null,
											LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_wrong_approval_definition"),
											null);
								}
							}
						}

						if (workflow == null) {
							workflow = t.get_approvalMap().get((short) 2); // action=2 insert
																			
							if (workflow != null && workflow.getApprovalRequestTip() == 2
									&& workflow.getManualDemandStartAppFlag() == 0)
								workflow = null;
						}

						if (workflowRecord == null && t.get_approvalMap() != null
								&& formResult.getRequestParams().get("_aa") != null
								&& GenericUtil.uInt(formResult.getRequestParams().get("_aa")) == -1) { // Insertle
																										// ilgili
																										// bir
																										// onay
																										// başlatma
																										// isteği
																										// var
																										// ve
																										// böylece
																										// artık
							// 901'e giriyor
							Map<String, Object> advancedStepSqlResult = null;
							if (workflow.getAdvancedBeginSql() != null
									&& workflow.getAdvancedBeginSql().length() > 10) {
								Object[] oz = DBUtil.filterExt4SQL(workflow.getAdvancedBeginSql(), scd, requestParams,
										null);
								advancedStepSqlResult = dao.runSQLQuery2Map(oz[0].toString(), (List) oz[1], null);
								if (advancedStepSqlResult != null) {
									if (advancedStepSqlResult.get("active_flag") != null
											&& GenericUtil.uInt(advancedStepSqlResult.get("active_flag")) == 0)
										workflow = null;
									else {
										workflowStep = new W5WorkflowStep();
										workflowStep.setApprovalUsers("" + (Integer) scd.get("userId"));
										workflowStep.setApprovalStepId(901);
									}
									if (advancedStepSqlResult.get("error_msg") != null)
										throw new IWBException("security", "Workflow", workflow.getWorkflowId(), null,
												(String) advancedStepSqlResult.get("error_msg"), null);
								}
							} else {
								workflowStep = new W5WorkflowStep();
								workflowStep.setApprovalRoles(workflow.getManualAppRoleIds());
								workflowStep.setApprovalUsers(workflow.getManualAppUserIds());
								if (workflow.getManualAppTableFieldIds() != null) {
								} else if (workflowStep.getApprovalUsers() == null)
									workflowStep.setApprovalUsers("" + (Integer) scd.get("userId"));

								workflowStep.setApprovalStepId(901);
							}

							if (workflowStep != null) { // step hazir
								workflowRecord = new W5WorkflowRecord(scd, workflowStep.getApprovalStepId(),
										workflow.getWorkflowId(), formResult.getForm().getObjectId(), (short) 0,
										workflowStep.getReturnFlag());
								workflowRecord.setApprovalUsers(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("approval_users") != null
												? (String) advancedStepSqlResult.get("approval_users")
												: workflowStep.getApprovalUsers());
								workflowRecord.setApprovalRoles(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("approval_roles") != null
												? (String) advancedStepSqlResult.get("approval_roles")
												: workflowStep.getApprovalRoles());
								workflowRecord.setAccessViewTip(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("access_view_tip") != null
												? (short) GenericUtil.uInt(advancedStepSqlResult.get("access_view_tip"))
												: workflowStep.getAccessViewTip());
								workflowRecord.setAccessViewRoles(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("access_view_roles") != null
												? (String) advancedStepSqlResult.get("access_view_roles")
												: workflowStep.getAccessViewRoles());
								workflowRecord.setAccessViewUsers(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("access_view_users") != null
												? (String) advancedStepSqlResult.get("access_view_users")
												: workflowStep.getAccessViewUsers());
								if (workflowRecord.getAccessViewTip() != 0 && !GenericUtil
										.hasPartInside2(workflowRecord.getAccessViewUsers(), scd.get("userId"))) // goruntuleme
																											// kisiti
																											// var
																											// ve
																											// kendisi
																											// goremiyorsa,
									// kendisini de ekle
									workflowRecord.setAccessViewUsers(workflowRecord.getAccessViewUsers() != null
											? workflowRecord.getAccessViewUsers() + "," + scd.get("userId")
											: scd.get("userId").toString());
							} else {
								throw new IWBException("framework", "Workflow", formId, null,
										LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_error_workflow_definition"),
										null);
							}
						}
					} else {
						if (workflowRecord != null) {
							String noUpdateVersionNo = FrameworkCache.getAppSettingStringValue(scd,
									"approval_no_update_version_no");
							if (GenericUtil.isEmpty(noUpdateVersionNo)
									|| !GenericUtil.hasPartInside(noUpdateVersionNo, "" + t.getTableId())) {
								workflowRecord.setVersionNo(workflowRecord.getVersionNo() + 1);
								dao.updateObject(workflowRecord);
							}
							if (FrameworkSetting.liveSyncRecord)
								formResult.addSyncRecord(new W5SynchAfterPostHelper((String) scd.get("projectId"),
										392 /* w5_approval_record */, "" + workflowRecord.getApprovalRecordId(),
										(Integer) scd.get("userId"), requestParams.get(".w"), (short) 1));
							workflowRecord = null; // bu kaydedilmeyecek
						}
					}
				}

				dao.updateFormTable(formResult, paramSuffix);

				//
				// if(formResult.getErrorMap().isEmpty())FrameworkCache.removeTableCacheValue(t.getCustomizationId(),
				// t.getTableId(),GenericUtil.uInt(ptablePk));//caching icin

				if (FrameworkSetting.workflow && accessControlSelfFlag && formResult.getErrorMap().isEmpty()
						&& workflowRecord != null) { // aproval baslanmis
					int tablePk = GenericUtil.uInt(formResult.getOutputFields()
							.get(/* formResult.getForm().get_sourceTable() */ FrameworkCache
									.getTable(scd, formResult.getForm().getObjectId()).get_tableFieldList().get(0)
									.getDsc()));
					if (tablePk == 0) {
						tablePk = GenericUtil.uInt(ptablePk);
					}
					workflowRecord.setTablePk(tablePk);
					String summaryText = dao.getSummaryText4Record(scd, t.getTableId(), tablePk);
					workflowRecord.setDsc(GenericUtil.uStrMax(summaryText, 512));
					dao.saveObject(workflowRecord);
					if (FrameworkSetting.liveSyncRecord)
						formResult.addSyncRecord(new W5SynchAfterPostHelper((String) scd.get("projectId"),
								392 /* w5_approval_record */, "" + workflowRecord.getApprovalRecordId(),
								(Integer) scd.get("userId"), requestParams.get(".w"), (short) 2));
					Log5WorkflowRecord logRecord = new Log5WorkflowRecord();
					logRecord.setProjectUuid(workflowRecord.getProjectUuid());

					logRecord.setApprovalActionTip((short) 0); // start,
																// approve,
																// return,
																// reject,
																// time_limit_cont
																// ,final_approve
					logRecord.setUserId((Integer) scd.get("userId"));
					logRecord.setApprovalRecordId(workflowRecord.getApprovalRecordId());
					logRecord.setApprovalStepId(sourceStepId);
					logRecord.setApprovalId(workflowRecord.getApprovalId());
					dao.saveObject(logRecord);
					formResult.getOutputMessages()
							.add(t.get_approvalMap().get((short) 2).getDsc() + " "
									+ LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_workflow_started") + " ("
									+ summaryText + ")");

					// Mail ve SMS işlemleri _aa=-1 gelirse //

					// Notification TODO
					
				}
				break;
			case 5: // copy
			case 2: // insert
				if (FrameworkSetting.workflow && accessControlSelfFlag && t.get_approvalMap() != null) { // onay
																											// mekanizmasi
																											// var
																											// mi
																											// bunda?
					workflow = t.get_approvalMap().get((short) 2); // action=2
																	// for
																	// insert
					if (workflow != null && workflow.getActiveFlag() != 0 && workflow.getApprovalRequestTip() >= 1) { // insert
																														// approval
																														// mekanizmasi
																														// var
																														// ve
																														// automatic
						Map<String, Object> advancedStepSqlResult = null;
						switch (workflow.getApprovalRequestTip()) { // eger
																	// approval
																	// olacaksa
						case 1: // automatic approval
							if (workflow.getAdvancedBeginSql() != null
									&& workflow.getAdvancedBeginSql().trim().length() > 2) { // calisacak
								
								Object oz = scriptEngine.executeScript(scd, requestParams, workflow.getAdvancedBeginSql(), null, "wf_"+workflow.getWorkflowId()+"_abs");
								if(oz!=null) {
									if(oz instanceof Boolean) {
										if(!((Boolean)oz))workflow=null;
									} else
										advancedStepSqlResult = NashornUtil.fromScriptObject2Map(oz); 
								}
							}
							workflowStep = null;
							if(workflow!=null)switch (workflow.getApprovalFlowTip()) { // simple
							case 0: // basit onay
								workflowStep = workflow.get_approvalStepList().get(0).getNewInstance();
								break;
							case 1: // complex onay
								if (advancedStepSqlResult != null
										&& advancedStepSqlResult.get("approval_step_id") != null
										&& GenericUtil.uInt(advancedStepSqlResult.get("approval_step_id")) != 0)
									workflowStep = workflow.get_approvalStepMap()
											.get(GenericUtil.uInt(advancedStepSqlResult.get("approval_step_id")))
											.getNewInstance();
								else
									workflowStep = workflow.get_approvalStepList().get(0).getNewInstance();
								break;

							}

							break;
						case 2: // manual after action
							if (workflow.getManualDemandStartAppFlag() == 0
									|| (workflow.getManualDemandStartAppFlag() == 1
											&& GenericUtil.uInt(formResult.getRequestParams().get("_aa")) == -1)) { // Eğer
																													// onay
																													// mekanizması
																													// elle
																													// başlatılmayacaksa
																													// burada
																													// 901'e
						
								if(workflowStep==null) {
									workflowStep = new W5WorkflowStep();
									// if(approval.getDynamicStepFlag()!=0))
									workflowStep.setApprovalRoles(workflow.getManualAppRoleIds());
									workflowStep.setApprovalUsers(workflow.getManualAppUserIds());
									if (workflow.getManualAppTableFieldIds() != null) { // TODO:
																						
									} else if (workflowStep.getApprovalUsers() == null) // TODO:
																						// yanlis
										workflowStep.setApprovalUsers("" + (Integer) scd.get("userId"));

									workflowStep.setApprovalStepId(901); // wait
																			// for
																			// starting
																			// approval
								}
							}
							break;
						}
						if (workflow != null && (workflow.getManualDemandStartAppFlag() == 0
								|| (workflow.getManualDemandStartAppFlag() == 1
										&& GenericUtil.uInt(formResult.getRequestParams().get("_aa")) == -1))) { // Onay
																													// Mek
																													// Başlat
							if (workflowStep != null) { // step hazir
								// if(approval.getApprovalStrategyTip()==0)schema
								// =
								// FrameworkCache.getAppSettingStringValue(scd,
								// "approval_schema");
								workflowRecord = new W5WorkflowRecord((String) scd.get("projectId"));
								workflowRecord.setApprovalId(workflow.getWorkflowId());
								workflowRecord.setApprovalStepId(workflowStep.getApprovalStepId());
								workflowRecord.setApprovalActionTip((short) 0); // start,approve,return,reject,time_limit_exceed
								workflowRecord.setTableId(formResult.getForm().getObjectId());
								workflowRecord.setReturnFlag(workflowStep.getReturnFlag());
								workflowRecord.setApprovalUsers(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("approval_users") != null
												? (String) advancedStepSqlResult.get("approval_users")
												: workflowStep.getApprovalUsers());
								workflowRecord.setApprovalRoles(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("approval_roles") != null
												? (String) advancedStepSqlResult.get("approval_roles")
												: workflowStep.getApprovalRoles());
								workflowRecord.setAccessViewTip(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("access_view_tip") != null
												? (short) GenericUtil.uInt(advancedStepSqlResult.get("access_view_tip"))
												: workflowStep.getAccessViewTip());
								workflowRecord.setAccessViewRoles(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("access_view_roles") != null
												? (String) advancedStepSqlResult.get("access_view_roles")
												: workflowStep.getAccessViewRoles());
								workflowRecord.setAccessViewUsers(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("access_view_users") != null
												? (String) advancedStepSqlResult.get("access_view_users")
												: workflowStep.getAccessViewUsers());
								if (workflowRecord.getAccessViewTip() != 0 && !GenericUtil
										.hasPartInside2(workflowRecord.getAccessViewUsers(), scd.get("userId"))) // goruntuleme
																											// kisiti
																											// var
																											// ve
																											// kendisi
																											// goremiyorsa,
									// kendisini de ekle
									workflowRecord.setAccessViewUsers(workflowRecord.getAccessViewUsers() != null
											? workflowRecord.getAccessViewUsers() + "," + scd.get("userId")
											: scd.get("userId").toString());
								workflowRecord.setInsertUserId((Integer) scd.get("userId"));
								workflowRecord.setVersionUserId((Integer) scd.get("userId"));
								// appRecord.setCustomizationId((Integer)scd.get("customizationId"));
								workflowRecord.setHierarchicalLevel(0);
							} else {
								throw new IWBException("framework", "Workflow", formId, null,
										LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_error_workflow_definition"),
										null);
							}
						}
					}
				}
				if (action == 2) // 2:insert
					dao.insertFormTable(formResult, paramSuffix);
				else { // 5:copy
					dao.copyFormTable(formResult, schema, paramSuffix, paramSuffix != null && paramSuffix.length() > 0);
					pcopyTablePk = requestParams.get(t.get_tableParamList().get(0).getDsc());
					action = 2;
				}

				if (formResult.getOutputFields() != null && !formResult.getOutputFields().isEmpty()) {
					Object o = formResult.getOutputFields().get(t.get_tableParamList().get(0).getDsc().substring(1));
					// user fieldlardan gelen alanlar.
					/*
					 * if(approval.getManualAppTableFieldIds() != null){
					 * dao.getUsersFromUserFields(PromisCache.getTable(scd,
					 * approval.getTableId()),
					 * approval.getManualAppTableFieldIds(), scd, o.toString());
					 * }
					 */
					if (o != null) {
						ptablePk = o.toString();
						requestParams.put(t.get_tableParamList().get(0).getDsc(), ptablePk);
						if (!GenericUtil.isEmpty(paramSuffix))
							requestParams.put(t.get_tableParamList().get(0).getDsc() + paramSuffix, ptablePk);
					}
				}

				if (FrameworkSetting.workflow && accessControlSelfFlag && formResult.getErrorMap().isEmpty()
						&& workflowRecord != null) { // aproval baslanmis
					int tablePk = GenericUtil.uInt(formResult.getOutputFields()
							.get(/* formResult.getForm().get_sourceTable() */ FrameworkCache
									.getTable(scd, formResult.getForm().getObjectId()).get_tableFieldList().get(0)
									.getDsc()));
					workflowRecord.setTablePk(tablePk);
					String summaryText = dao.getSummaryText4Record(scd, t.getTableId(), tablePk);
					workflowRecord.setDsc(summaryText);
					dao.saveObject(workflowRecord);
					if (FrameworkSetting.liveSyncRecord)
						formResult.addSyncRecord(new W5SynchAfterPostHelper((String) scd.get("projectId"),
								392 /* w5_approval_record */, "" + workflowRecord.getApprovalRecordId(),
								(Integer) scd.get("userId"), requestParams.get(".w"), (short) 1));
					Log5WorkflowRecord logRecord = new Log5WorkflowRecord();
					logRecord.setProjectUuid(workflowRecord.getProjectUuid());

					logRecord.setApprovalActionTip((short) 0); // start,
																// approve,
																// return,
																// reject,
																// time_limit_cont
																// ,final_approve,
																// deleted
					logRecord.setUserId((Integer) scd.get("userId"));
					logRecord.setApprovalRecordId(workflowRecord.getApprovalRecordId());
					logRecord.setApprovalStepId(sourceStepId);
					logRecord.setApprovalId(workflowRecord.getApprovalId());
					dao.saveObject(logRecord);

					workflow = t.get_approvalMap().get((short) 2); // action=1
																	// for
																	// update
					String appRecordUserList = null;
					String appRecordRoleList = null;
					String mesajBody = "";

					
					/*
					 * Ekstra Bildirim Bilgileri, SMS, EMail ve Notification
					 * yükleniyor SMS Mail Tip -> 0 SMS , 1 E-Mail, 2
					 * Notification
					 */

					Map<Integer, Map> extraInformData = new HashMap<Integer, Map>();

					/* Ekstra bildirim sonu */

					// Notification TODO

					

					if (workflowRecord.getApprovalStepId() != 901)
						formResult.getOutputMessages()
								.add(t.get_approvalMap().get((short) 2).getDsc() + ", "
										+ LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_workflow_started")
										+ " (" + summaryText + ")");
					else
						formResult.getOutputMessages()
								.add(t.get_approvalMap().get((short) 2).getDsc() + ", "
										+ LocaleMsgCache.get2(0, (String) scd.get("locale"),
												"manuel_olarak_onay_surecini_baslatabilirsiniz")
										+ " (" + summaryText + ")");
				}
				break;
			case 3: // delete
				ptablePk = requestParams.get(t.get_tableParamList().get(0).getDsc() + paramSuffix);
				if (FrameworkSetting.vcs && t.getVcsFlag() != 0) {
					requestParams.put("_iwb_vcs_dsc",
							dao.getTableRecordSummary(scd, t.getTableId(), GenericUtil.uInt(ptablePk), 32));
				}
				if (FrameworkSetting.workflow && accessControlSelfFlag) {
					if (workflowRecord != null) { // eger bir approval sureci
												// icindeyse
						Log5WorkflowRecord logRecord = new Log5WorkflowRecord();
						logRecord.setProjectUuid(workflowRecord.getProjectUuid());

						logRecord.setApprovalActionTip((short) 6); // start,
																	// approve,
																	// return,
																	// reject,
																	// time_limit_cont
																	// ,final_approve,
																	// deleted
						logRecord.setUserId((Integer) scd.get("userId"));
						logRecord.setApprovalRecordId(workflowRecord.getApprovalRecordId());
						logRecord.setApprovalStepId(workflowRecord.getApprovalStepId());
						logRecord.setApprovalId(workflowRecord.getApprovalId());
						dao.saveObject(logRecord);
						dao.removeObject(workflowRecord); // TODO:aslinda bir de loga
														// atmali bunu
						workflowRecord = null;
					} else if (t.get_approvalMap() != null) { // onay
																// mekanizmasi
																// var mi bunda?
						workflow = t.get_approvalMap().get((short) 3); // action=2
																		// for
																		// delete
						if (workflow != null && workflow.getActiveFlag() != 0
								&& workflow.getApprovalRequestTip() >= 1) { // insert
																			// approval
																			// mekanizmasi
																			// var
																			// ve
																			// automatic
							Map<String, Object> advancedStepSqlResult = null;
							switch (workflow.getApprovalRequestTip()) { // eger
																		// approval
																		// olacaksa
							case 1: // automatic approval
								if (workflow.getAdvancedBeginSql() != null
										&& workflow.getAdvancedBeginSql().length() > 10) { // calisacak
									Object[] oz = DBUtil.filterExt4SQL(workflow.getAdvancedBeginSql(), scd,
											requestParams, null);
									advancedStepSqlResult = dao.runSQLQuery2Map(oz[0].toString(), (List) oz[1], null);
									// donen bir cevap var, aktive_flag deger
									// olarak var ve onun degeri 0 ise o
									// zaman girmeyecek
									if (advancedStepSqlResult != null) {
										if (advancedStepSqlResult.get("active_flag") != null
												&& GenericUtil.uInt(advancedStepSqlResult.get("active_flag")) == 0) // girmeyecek
											workflow = null; // approval
																// olmayacak
										if (advancedStepSqlResult.get("error_msg") != null) // girmeyecek
											throw new IWBException("security", "Workflow", workflow.getWorkflowId(),
													null, (String) advancedStepSqlResult.get("error_msg"), null);
									}
								}
								workflowStep = null;
								switch (workflow.getApprovalFlowTip()) { // simple
								case 0: // basit onay
									workflowStep = workflow.get_approvalStepList().get(0).getNewInstance();
									break;
								case 1: // complex onay
									if (advancedStepSqlResult != null
											&& advancedStepSqlResult.get("approval_step_id") != null
											&& GenericUtil.uInt(advancedStepSqlResult.get("approval_step_id")) != 0)
										workflowStep = workflow.get_approvalStepMap()
												.get(GenericUtil.uInt(advancedStepSqlResult.get("approval_step_id")));
									else
										workflowStep = workflow.get_approvalStepList().get(0).getNewInstance();
									break;
								}

								break;
							}

							if (workflowStep != null) { // step hazir
								workflowRecord = new W5WorkflowRecord();
								workflowRecord.setApprovalId(workflow.getWorkflowId());
								workflowRecord.setApprovalStepId(workflowStep.getApprovalStepId());
								workflowRecord.setApprovalActionTip((short) 0); // start,approve,return,reject,time_limit_exceed
								workflowRecord.setTableId(formResult.getForm().getObjectId());
								workflowRecord.setReturnFlag(workflowStep.getReturnFlag());
								workflowRecord.setApprovalUsers(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("approval_users") != null
												? (String) advancedStepSqlResult.get("approval_users")
												: workflowStep.getApprovalUsers());
								workflowRecord.setApprovalRoles(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("approval_roles") != null
												? (String) advancedStepSqlResult.get("approval_roles")
												: workflowStep.getApprovalRoles());
								workflowRecord.setAccessViewTip(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("access_view_tip") != null
												? (short) GenericUtil.uInt(advancedStepSqlResult.get("access_view_tip"))
												: workflowStep.getAccessViewTip());
								workflowRecord.setAccessViewRoles(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("access_view_roles") != null
												? (String) advancedStepSqlResult.get("access_view_roles")
												: workflowStep.getAccessViewRoles());
								workflowRecord.setAccessViewUsers(advancedStepSqlResult != null
										&& advancedStepSqlResult.get("access_view_users") != null
												? (String) advancedStepSqlResult.get("access_view_users")
												: workflowStep.getAccessViewUsers());
								if (workflowRecord.getAccessViewTip() != 0 && !GenericUtil
										.hasPartInside2(workflowRecord.getAccessViewUsers(), scd.get("userId"))) // goruntuleme
																											// kisiti
																											// var
																											// ve
																											// kendisi
																											// goremiyorsa,
									// kendisini de ekle
									workflowRecord.setAccessViewUsers(workflowRecord.getAccessViewUsers() != null
											? workflowRecord.getAccessViewUsers() + "," + scd.get("userId")
											: scd.get("userId").toString());
								workflowRecord.setInsertUserId((Integer) scd.get("userId"));
								workflowRecord.setVersionUserId((Integer) scd.get("userId"));
								// appRecord.setCustomizationId((Integer)scd.get("customizationId"));
							} else {
								throw new IWBException("framework", "Workflow", formId, null,
										LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_error_workflow_definition"),
										null);
							}
						}
					}
				}

				if (workflowRecord == null && FrameworkSetting.feed && t.getShowFeedTip() != 0
						&& (t.getTableId() != 671 && t.getTableId() != 329 && t.getTableId() != 44)) { // TODO:
																										// delete
																										// icin
																										// onceden
																										// bakmak
																										// lazim
																										// yoksa
																										// gidecek
																										// kayit
					feed = new Log5Feed(scd);
					feed.set_showFeedTip(t.getShowFeedTip());
					switch (feed.get_showFeedTip()) {
					case 2: // master
						feed.setFeedTip((short) 3); // remove:master icin
						feed.setTableId(feedTableId = t.getTableId());
						feed.setTablePk(feedTablePk = GenericUtil.uInt(ptablePk));
						break;
					case 1: // detail
						feed.setFeedTip((short) 4); // edit:detaya gore
						feed.setDetailTableId(feedTableId = t.getTableId());
						feed.setDetailTablePk(feedTablePk = GenericUtil.uInt(ptablePk));
						break;
					}
					if (feed != null) {
						feed.set_tableRecordList(dao.findRecordParentRecords(scd, feedTableId, feedTablePk, 0, true));
						if (feed.get_tableRecordList() != null && feed.get_tableRecordList().size() > 1
								&& feed.get_showFeedTip() == 1) {
							W5TableRecordHelper trh = feed.get_tableRecordList().get(1);
							feed.setTableId(trh.getTableId());
							feed.setTablePk(trh.getTablePk());
							feed.set_commentCount(trh.getCommentCount());
						} else if (feed.get_tableRecordList() != null && feed.get_tableRecordList().size() > 0)
							feed.set_commentCount(feed.get_tableRecordList().get(0).getCommentCount());
					}
				}
				Map<String, String> mz = new HashMap();
				mz.put("ptable_id", "" + t.getTableId());
				mz.put("ptable_pk", ptablePk);
				mz.put(".w", requestParams.get(".w"));
				W5GlobalFuncResult dfr = scriptEngine.executeGlobalFunc(scd, 690, mz, (short)7);//control for any child records
				if (ptablePk != null && workflowRecord == null) {
					boolean b = dao.deleteTableRecord(formResult, paramSuffix);
					if (!b)
						formResult.getOutputMessages().add(LocaleMsgCache.get2(scd, "record_not_found"));
				}
				if (formResult.getErrorMap().isEmpty()) {
					// FrameworkCache.removeTableCacheValue(t.getCustomizationId(),
					// t.getTableId(),GenericUtil.uInt(requestParams.get(t.get_tableParamList().get(0).getDsc()+paramSuffix)));//caching
					// icin

					if (FrameworkSetting.workflow && workflowRecord != null) { // aproval
																			// baslanmis
						int tablePk = GenericUtil.uInt(ptablePk);
						workflowRecord.setTablePk(tablePk);
						String summaryText = dao.getSummaryText4Record(scd, t.getTableId(), tablePk);
						workflowRecord.setDsc(summaryText);
						dao.saveObject(workflowRecord);
						if (FrameworkSetting.liveSyncRecord)
							formResult.addSyncRecord(new W5SynchAfterPostHelper((String) scd.get("projectId"),
									392 /* w5_approval_record */, "" + workflowRecord.getApprovalRecordId(),
									(Integer) scd.get("userId"), requestParams.get(".w"), (short) 1));

						Log5WorkflowRecord logRecord = new Log5WorkflowRecord();
						logRecord.setProjectUuid(workflowRecord.getProjectUuid());

						logRecord.setApprovalActionTip((short) 0); // start,
																	// approve,
																	// return,
																	// reject,
																	// time_limit_cont
																	// ,final_approve,
																	// deleted
						logRecord.setUserId((Integer) scd.get("userId"));
						logRecord.setApprovalRecordId(workflowRecord.getApprovalRecordId());
						logRecord.setApprovalStepId(sourceStepId);
						logRecord.setApprovalId(workflowRecord.getApprovalId());
						dao.saveObject(logRecord);

						workflow = t.get_approvalMap().get((short) 3); // action=3
																		// for
																		// delete
						String appRecordUserList = null;
						String appRecordRoleList = null;
						String mesajBody = "";

						//TODO Notification

						if (workflowRecord.getApprovalStepId() != 901)
							formResult.getOutputMessages()
									.add(t.get_approvalMap().get((short) 3).getDsc() + ", "
											+ LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_workflow_started")
											+ " (" + summaryText + ")");
						else
							formResult.getOutputMessages()
									.add(t.get_approvalMap().get((short) 2).getDsc() + ", "
											+ LocaleMsgCache.get2(scd,
													"islemlerinizi_tamamlayip_manuel_olarak_onay_surecini_baslatabilirsiniz")
									+ " (" + summaryText + ")");
					}
				}

				break;
			default: // sorun var
				throw new IWBException("validation", "Action", action, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_wrong_form_action"), null);
			}

			if (formResult.getErrorMap().isEmpty()) { // sorun yok

				/*
				 * if((action==1 || action==2) && t.getTableId()==15){ //TODO:
				 * simdilik daha yavas calistigi tespit edildi, o yuzden
				 * vazgecildi
				 * dao.createTableAuditDefinition(scd,PromisUtil.uInt(ptablePk))
				 * ; }
				 */

				boolean bc = false; // boolean copy
				if (realAction == 5 && formResult.getForm().getObjectType() == 2
						&& /* formResult.getForm().get_sourceTable() */ FrameworkCache
								.getTable(scd, formResult.getForm().getObjectId()).get_tableChildList() != null) { // copy
																													// ise
																													// o
																													// zaman
																													// detay
																													// var
																													// ise
																													// onlari
																													// da
																													// kopyala
					String copyTblIds = requestParams.get("_copy_tbl_ids");
					for (W5TableChild tc : /*
											 * formResult.getForm().
											 * get_sourceTable()
											 */
					FrameworkCache.getTable(scd, formResult.getForm().getObjectId()).get_tableChildList())
						if (tc.getCopyStrategyTip() != 0 && (tc.getCopyStrategyTip() == 1
								|| GenericUtil.hasPartInside2(copyTblIds, "" + tc.getRelatedTableId()))) {
							dao.copyFormTableDetail(formResult, tc, null, schema, paramSuffix);
							bc = true;
						}
				}

				// detail records kaydet
				boolean bd = false;
				if (paramSuffix.length() == 0 && (action == 1 || action == 2)) { // detail
																					// kaydet
					for (int qi = 1; GenericUtil.uInt(requestParams.get("_fid" + qi)) != 0; qi++)
						try {
							if (qi == 1) {
								requestParams.putAll((Map) formResult.getOutputFields());
								checkedParentRecords.add(formResult.getForm().getObjectId() + "^" + ptablePk);
							}
							int detailFormId = GenericUtil.uInt(requestParams.get("_fid" + qi));
							int dirtyCount = GenericUtil.uInt(requestParams.get("_cnt" + qi));
							if (dirtyCount == 0)
								continue;
							Map<String, String> fieldConMap = new HashMap();
							for (String s1 : requestParams.keySet())
								if (s1.startsWith("_fid" + qi + "_con_")) {
									fieldConMap.put(s1.substring(10), requestParams.get(s1));
								}
							if (detailFormId > 0) {
								if (!fieldConMap.isEmpty()) {
									for (String key : fieldConMap.keySet())
										if (formResult.getOutputFields().containsKey(fieldConMap.get(key)))
											for (int qi2 = 1; qi2 <= dirtyCount; qi2++)
												requestParams.put(key + qi + "." + qi2, formResult.getOutputFields()
														.get(fieldConMap.get(key)).toString());
								}
								if (formResult.getOutputFields() != null) {
									for (String key : formResult.getOutputFields().keySet())
										for (int qi2 = 1; qi2 <= dirtyCount; qi2++)
											requestParams.put(key + qi + "." + qi2,
													formResult.getOutputFields().get(key).toString());
								}
								W5FormResult detailForm = postEditGrid4Table(scd, detailFormId, dirtyCount,
										requestParams, qi + ".", checkedParentRecords);
								if (!GenericUtil.isEmpty(detailForm.getQueueActionList()))
									result.addAll(detailForm.getQueueActionList());
								if (!GenericUtil.isEmpty(detailForm.getOutputFields()))
									formResult.getOutputFields().put("_fid" + qi, detailForm.getOutputFields());
								if (FrameworkSetting.liveSyncRecord)
									formResult.addSyncRecordAll(detailForm.getListSyncAfterPostHelper());
								bd = true;
							} else {
								scriptEngine.postEditGridGlobalFunc(scd, -formId, dirtyCount, requestParams, qi + ".");
								bd = true;
							}
						} catch (Exception e) {
							throw new IWBException("framework", "postForm(detail)",
									GenericUtil.uInt(requestParams.get("_fid" + qi)), null,
									"[40," + GenericUtil.uInt(requestParams.get("_fid" + qi)) + "]", e);
						}
				}
				
				if (tla != null && action!=3)
					extFormTableEvent(formResult, new String[] { "_", "xu", "xi", "_", "_", "xi" }[action], scd,
							requestParams, t, requestParams.get(t.get_tableParamList().get(0).getDsc() + paramSuffix),
							paramSuffix);
				// approval
				if (FrameworkSetting.workflow && GenericUtil.uInt(requestParams.get("_arid" + paramSuffix)) != 0) { // kaydet
																													// ve
																													// approve
																													// et???
					workflowEngine.approveRecord(scd, GenericUtil.uInt(requestParams.get("_arid" + paramSuffix)),
							GenericUtil.uInt(requestParams.get("_aa" + paramSuffix)), requestParams);
				}

				/* tableTrigger After Action start */
				if (tla != null)
					extFormTableEvent(formResult, new String[] { "_", "au", "ai", "ad", "_", "ai" }[action], scd,
							requestParams, t, ptablePk, paramSuffix);
						/* end of tableTrigger */

				/* form conversion */
				conversionEngine.extFormConversion(formResult, paramSuffix, action, scd, requestParams, t, ptablePk,
						false);
						/* end of form conversion */

				/* alarm start */
				notificationEngine.extFormAlarm(formResult, action, scd, requestParams, t, mobile, ptablePk);
				/* end of alarm */

				/* sms/mail customized templates */
				notificationEngine.extFormSmsMail(formResult, result, action, scd, requestParams, t, mobile, ptablePk);
				/* end of sms/mail customized templates */

				/* vcs control */
				if(t.getVcsFlag()!=0)metadataWriter.extFormVcsControl(formResult, action, scd, requestParams, t, ptablePk);
				/* end of vcs */

				if (action == 2) { // conversion time
					if (GenericUtil.isEmpty(paramSuffix) && requestParams.containsKey("_cnvId")
							&& requestParams.containsKey("_cnvTblPk")) { // conversion
																			// var
																			// burda
						int conversionId = GenericUtil.uInt(requestParams.get("_cnvId"));
						int conversionTablePk = GenericUtil.uInt(requestParams.get("_cnvTblPk"));
						W5Conversion cnv = (W5Conversion)metadataLoader.getMetadataObject("W5Conversion","conversionId", conversionId,
								(String) scd.get("projectId"), null);
						if (cnv!=null && cnv.getDstFormId() == formId) { // validation for Destination Conversion form
							W5ConvertedObject co = new W5ConvertedObject(scd, conversionId, conversionTablePk,
									GenericUtil.uInt(ptablePk));
							dao.saveObject(co);
							if (!GenericUtil.isEmpty(cnv.getRhinoCode())) {
								scriptEngine.executeScript(scd, requestParams, cnv.getRhinoCode(), null, "707r"+cnv.getConversionId());
							}
						}
					}
				}
			}
			metadataWriter.afterPostForm(formResult, paramSuffix);

			if (FrameworkSetting.liveSyncRecord && formResult.getErrorMap().isEmpty() && formResult.getForm() != null
					&& formResult.getForm().getObjectType() == 2) {
				int userId = (Integer) formResult.getScd().get("userId");
				// int customizationId =
				// (Integer)formResult.getScd().get("customizationId");
				t = FrameworkCache.getTable(formResult.getScd(), formResult.getForm().getObjectId());
				String webPageId = formResult.getRequestParams().get(".w");
				if (t.getLiveSyncFlag() != 0 && webPageId != null) {
					String key = "";
					if (formResult.getAction() == 1 || formResult.getAction() == 3) {
						for (String k : formResult.getPkFields().keySet())
							if (!k.startsWith("customization") && !k.startsWith("project"))
								key += "*" + formResult.getPkFields().get(k);
						if(!GenericUtil.isEmpty(key)) {
							key = formResult.getForm().getObjectId() + "-" + key.substring(1);
							formResult.setLiveSyncKey(key);
						}
					}

					 formResult.addSyncRecord(new
					 W5SynchAfterPostHelper((String)formResult.getScd().get("projectId"),
					 t.getTableId(), key, userId, webPageId,
					 (short)formResult.getAction())); //TODO

				}
			}

			return result;
		} catch (Exception e) {
			throw new IWBException("framework", "postForm", formResult.getFormId(), null,
					"[40," + formResult.getFormId() + "] " + formResult.getForm().getDsc(), e);
		}
	}


	public W5FormResult postEditGrid4Table(Map<String, Object> scd, int formId, int dirtyCount,
			Map<String, String> requestParams, String prefix, Set<String> checkedParentRecords) {
		List<W5QueuedActionHelper> queuedGlobalFuncList = new ArrayList<W5QueuedActionHelper>();

		W5FormResult formResult = metadataLoader.getFormResult(scd, formId, 2, requestParams);
		W5Table t = FrameworkCache.getTable(scd, formResult.getForm().getObjectId()); // formResult.getForm().get_sourceTable();
		if (t.getAccessViewTip() == 0 && !FrameworkCache.roleAccessControl(scd, 0)) {
			throw new IWBException("security", "Module", 0, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_modul_control"), null);
		}
		if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
				t.getAccessViewRoles(), t.getAccessViewUsers())) {
			throw new IWBException("security", "Form", formId, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_table_control_view"), null);
		}
		Map<String, Object> tmpOutputFields = new HashMap<String, Object>();
		for (int id = 1; id <= dirtyCount; id++) {
			formResult.setAction(GenericUtil.uInt(requestParams.get("a" + prefix + id)));
			queuedGlobalFuncList.addAll(postForm4Table(formResult, prefix + id, checkedParentRecords));

			if (!formResult.getErrorMap().isEmpty()) {
				throw new IWBException("validation", "Form", formId, null,
						"Detail Validation("
								+ LocaleMsgCache.get2(scd,formResult.getForm().getLocaleMsgKey())
								+ "): " + GenericUtil.fromMapToHtmlString2(formResult.getScd(), formResult.getErrorMap()),
						null);
			} else if (!GenericUtil.isEmpty(formResult.getOutputFields()))
				for (String key : formResult.getOutputFields().keySet()) {
					tmpOutputFields.put(key + prefix + id, formResult.getOutputFields().get(key));
					// Burada değişiklik var 29.06.2016
					tmpOutputFields.put(key + prefix + id, formResult.getOutputFields().get(key));
					// Detayın detayı kaydediliyor
					for (int i = 1; requestParams.containsKey("_fid" + prefix + id + "_" + i + "." + 1); i++) {
						String subPrefix = prefix + id + "_" + i + ".";
						int fid = GenericUtil.uInt(requestParams.get("_fid" + subPrefix + 1));
						// Master Primary Key
						requestParams.put(key + subPrefix + 1, formResult.getOutputFields().get(key).toString());
						// Master of Master Primary Key //
						if (requestParams.containsKey("root_pk")) {
							String pk = "";
							Object rootPk = requestParams.get(requestParams.get("root_pk"));
							if (rootPk instanceof BigDecimal) {
								pk = String.valueOf((BigDecimal) rootPk);
							} else {
								pk = rootPk.toString();
							}
							requestParams.put(requestParams.get("root_pk") + subPrefix + 1, pk);
						}
						W5FormResult fr = postEditGrid4Table(scd, fid, 1, requestParams, subPrefix,
								new HashSet<String>());
						if (!fr.getErrorMap().isEmpty()) {
							throw new IWBException("validation", "Form", fid, null,
									"Detay Grid Validation("
											+ LocaleMsgCache.get2(scd, fr.getForm().getLocaleMsgKey())
											+ "): " + GenericUtil.fromMapToJsonString(fr.getErrorMap()),
									null);
						}
					}
				}
		}
		if (!GenericUtil.isEmpty(tmpOutputFields))
			formResult.setOutputFields(tmpOutputFields);

		formResult.setQueuedActionList(queuedGlobalFuncList);
		if (formResult.getOutputMessages() != null && formResult.getOutputMessages().isEmpty())
			formResult.getOutputMessages().add("Successfully accomplished " + dirtyCount + " actions.");
		return formResult;
	}

	public void extFormTableEvent(W5FormResult formResult, String action, Map<String, Object> scd,
			Map<String, String> requestParams, W5Table t, String ptablePk, String prefix) {
		List<W5TableEvent> tla = FrameworkCache.getTableEvents(scd, t.getTableId());
		if (tla == null)
			return;
		Map<String, String> newRequestParam = GenericUtil.isEmpty(prefix) ? requestParams : null;
		for (W5TableEvent ta : tla)
			if (GenericUtil.hasPartInside2(ta.getLkpTriggerActions(), action)) {
				if (ta.getLkpCodeType() == 1) { // javascript
					if (newRequestParam == null) {
						newRequestParam = new HashMap();
						if (!GenericUtil.isEmpty(requestParams))
							for (String key : requestParams.keySet())
								if (key != null && key.endsWith(prefix)) {
									newRequestParam.put(key.substring(0, key.length() - prefix.length()),
											requestParams.get(key));
								}
					}
					scriptEngine.executeTableEvent(ta, formResult, action, scd, newRequestParam, t, ptablePk);
					
				} else if (ta.getLkpCodeType() == 4)
					try { // sql
						Map<String, Object> obj = new HashMap();
						obj.put("triggerAction", action);
						Map<String, Object> m = dao.runSQLQuery2Map(ta.getTriggerCode(), scd, requestParams, obj);
						if (m != null) {
							String msg = LocaleMsgCache.get2(scd, ta.getDsc());
							if (m.get("result") != null)
								msg = m.get("result").toString();
							short resultAction = ta.getLkpResultAction();
							if (m.containsKey("resultAction"))
								resultAction = (short) GenericUtil.uInt(m.get("resultAction"));
							switch (resultAction) {
							case 1: // readonly
								formResult.setViewMode(true);
							case 0: // continue
								formResult.getOutputMessages().add(msg);
								break;
							case 2: // confirm & continue
								if (!requestParams.containsKey("_confirmId_" + ta.getTableTriggerId()))
									throw new IWBException("confirm", "ConfirmId", ta.getTableTriggerId(), null, msg,
											null);
								break;
							case 3: // stop with message
								throw new IWBException("security", "TableTrigger", ta.getTableTriggerId(), null, msg,
										null);
							}
						}
					} catch (Exception e) {
						throw new IWBException("sql", "Event", ta.getTableTriggerId(), ta.getTriggerCode(),
								"[1209," + ta.getTableTriggerId() + "] " + ta.getDsc(), e);
					}
			}
	}


	public W5FormResult postForm4Table(Map<String, Object> scd, int formId, int action,
			Map<String, String> requestParams, String prefix) {
		if (formId == 0 && GenericUtil.uInt(requestParams.get("_tb_id")) != 0) {
			W5Table t = FrameworkCache.getTable(scd, GenericUtil.uInt(requestParams.get("_tb_id")));
			formId = t.getDefaultUpdateFormId();
			requestParams.put(t.get_tableParamList().get(0).getDsc(), requestParams.get("_tb_pk"));
			action = 1;
			requestParams.put("a", "1");
		}
		W5FormResult mainFormResult = metadataLoader.getFormResult(scd, formId, action, requestParams);
		boolean dev = scd.get("roleId") != null && (Integer) scd.get("roleId") == 0
				&& GenericUtil.uInt(requestParams, "_dev") != 0;
		W5Table t = FrameworkCache.getTable(scd, mainFormResult.getForm().getObjectId()); // mainFormResult.getForm().get_sourceTable();
		if (t.getAccessViewTip() == 0
				&& (!FrameworkCache.roleAccessControl(scd, 0) || !FrameworkCache.roleAccessControl(scd, action))) {
			throw new IWBException("security", "Module", 0, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_modul_control"), null);
		}
		if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
				t.getAccessViewRoles(), t.getAccessViewUsers())) {
			throw new IWBException("security", "Form", formId, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_table_control_view"), null);
		}
		Set<String> checkedParentRecords = new HashSet<String>();
		mainFormResult.setQueuedActionList(postForm4Table(mainFormResult, prefix, checkedParentRecords));
		if (!mainFormResult.getErrorMap().isEmpty())
			return mainFormResult;
		requestParams.remove("_fid1"); // TODO: daha iyisi yapılana kadar en
										// iyisi bu. Form extended işleminde
										// a1.1 gibi
		// değerler bir kez daha gönderiliyordu.
		if (mainFormResult.getForm().get_moduleList() != null) {
			for (W5FormModule m : mainFormResult.getForm().get_moduleList())
				if (m.getModuleType() == 4) { // form
					if (m.getModuleViewType() == 0 || (m.getModuleViewType() == 1 && action == 1)
							|| (m.getModuleViewType() == 2 && action == 2)) {
						int newAction = GenericUtil.uInt(requestParams.get("a" + m.getTabOrder()));
						if (newAction == 0)
							newAction = action;
						W5FormResult subFormResult = metadataLoader.getFormResult(scd, m.getObjectId(), newAction, requestParams);
						t = FrameworkCache.getTable(scd, mainFormResult.getForm().getObjectId()); // mainFormResult.getForm().get_sourceTable();
						if ((t.getAccessViewTip() == 0 && !FrameworkCache.roleAccessControl(scd, 0))
								|| (!GenericUtil.accessControl(scd, t.getAccessViewTip(), t.getAccessViewRoles(),
										t.getAccessViewUsers()))) {
							// nothing
						} else {
							postForm4Table(subFormResult, prefix, checkedParentRecords);
							if (!subFormResult.getErrorMap().isEmpty()) {
								throw new IWBException("validation", "Form", m.getObjectId(), null,
										LocaleMsgCache.get2(0, (String) scd.get("locale"),
												"fw_validation_error_detail_form") + ": "
										+ GenericUtil.fromMapToJsonString(subFormResult.getErrorMap()), null);
							}
							if (!GenericUtil.isEmpty(subFormResult.getOutputFields())
									&& !GenericUtil.isEmpty(mainFormResult.getOutputFields()))
								mainFormResult.getOutputFields().putAll(subFormResult.getOutputFields());
						}
					}
				}
		}
		if ((action == 1 || action == 3) && FrameworkSetting.liveSyncRecord) { // TODO
																				
			// bir kaydın guncellendigi/sildigi ve user'in kapattigi
			String webPageId = requestParams.get(".w");
			String tabId = requestParams.get(".t ");
		}
		return mainFormResult;
	}

	
	public W5FormResult postFormAsJson(Map<String, Object> scd, int mainFormId, int action, JSONObject mainFormData,
			int detailFormId, JSONArray detailFormData) {
		Map<String, String> requestParams = new HashMap<String, String>();
		Iterator<String> ik = mainFormData.keys();
		while (ik.hasNext())
			try {
				String k = ik.next();
				if (!GenericUtil.isEmpty(mainFormData.get(k)))
					requestParams.put(k, mainFormData.get(k).toString());
			} catch (Exception e) {
				throw new IWBException("framework", "Json2FormPost(FormId)", mainFormId, null, e.getMessage(), null);
			}
		requestParams.put("_fid1", "" + detailFormId);
		requestParams.put("_cnt1", "" + detailFormData.length());
		for (int i = 0; i < detailFormData.length(); i++)
			try {
				JSONObject d = (JSONObject) detailFormData.get(i);
				ik = d.keys();
				requestParams.put("a1." + (i + 1), "2");
				while (ik.hasNext())
					try {
						String k = ik.next();
						if (!GenericUtil.isEmpty(d.get(k)))
							requestParams.put(k + "1." + (i + 1), d.get(k).toString());
					} catch (Exception e) {
						throw new IWBException("framework", "Json2FormPost(FormId)", mainFormId, null, e.getMessage(),
								e);
					}
			} catch (Exception e) {
				throw new IWBException("framework", "Json2FormPostDetail(FormId)", detailFormId, null, e.getMessage(),
						e);
			}
		return postForm4Table(scd, mainFormId, action, requestParams, "");
	}
}
