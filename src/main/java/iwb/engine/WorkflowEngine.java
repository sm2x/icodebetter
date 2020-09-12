package iwb.engine;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.exception.IWBException;
import iwb.model.db.Log5WorkflowRecord;
import iwb.model.db.W5Comment;
import iwb.model.db.W5Email;
import iwb.model.db.W5Form;
import iwb.model.db.W5FormSmsMail;
import iwb.model.db.W5Table;
import iwb.model.db.W5Workflow;
import iwb.model.db.W5WorkflowRecord;
import iwb.model.db.W5WorkflowStep;
import iwb.model.result.W5FormResult;
import iwb.util.GenericUtil;
import iwb.util.NashornUtil;
import iwb.util.UserUtil;

@Component
public class WorkflowEngine {
	
	@Lazy
	@Autowired
	private MetadataLoader metadataLoader;
	
	@Lazy
	@Autowired
	private PostgreSQL dao;

	@Lazy
	@Autowired
	private NotificationEngine notificationEngine;

	@Lazy
	@Autowired
	private GlobalScriptEngine scriptEngine;

	// TODO: onayda, iade'de, reject'te notification gitsin
	public Map<String, Object> approveRecord(Map<String, Object> scd, int approvalRecordId, int approvalAction,
			Map<String, String> parameterMap) {

		Map<String, Object> result = new HashMap<String, Object>();
		if (!FrameworkSetting.workflow)
			return result;

		int customizationId = (Integer) scd.get("customizationId");
		int userId = (Integer) scd.get("userId");
		int versionNo = GenericUtil.uInt(parameterMap.get("_avno"));
		W5WorkflowRecord ar = (W5WorkflowRecord) metadataLoader.getMetadataObject(
				"W5WorkflowRecord","approvalRecordId", approvalRecordId,
				scd.get("projectId"), "Workflow Record not Found");
		String mesaj = "";
		String xlocale = (String) scd.get("locale");

		if (ar == null || ar.getFinishedFlag() != 0) {
			result.put("status", false);
			return result;
		}

		W5Workflow a = FrameworkCache.getWorkflow(scd, ar.getApprovalId());
		if (a.getActiveFlag() == 0) {
			throw new IWBException("validation", "Workflow", approvalRecordId, null,
					LocaleMsgCache.get2(0, xlocale, "approval_not_active"), null);
		}
		if (approvalAction != 901) {
			if (false && versionNo != ar.getVersionNo()) {
				throw new IWBException("security", "WorkflowRecord", approvalRecordId, null,
						LocaleMsgCache.get2(0, xlocale, "approval_onay_kaydi_degismis"), null);
			}
			if (!GenericUtil.accessControl(scd, (short) 1, ar.getApprovalRoles(), ar.getApprovalUsers())) {
				throw new IWBException("security", "WorkflowRecord", approvalRecordId, null,
						LocaleMsgCache.get2(0, xlocale, "approval_onay_kaydina_hakkiniz_yok"), null);
			}
		}
		boolean isFinished = false;
		String notificationIds = "";

		W5WorkflowStep currentStep = a.get_approvalStepMap().get(ar.getApprovalStepId()).getNewInstance();

		W5WorkflowStep nextStep = null;
		Map<String, Object> advancedNextStepSqlResult = null;
		switch (approvalAction) {
		case 901: // start approval
			mesaj = " '" + scd.get("completeName") + "' "
					+ LocaleMsgCache.get2(0, xlocale, "approval_presented_for_your_approval");
			if (a.getApprovalRequestTip() != 2)
				throw new IWBException("security", "WorkflowRecord", approvalRecordId, null,
						LocaleMsgCache.get2(0, xlocale, "approval_onay_talebi_yapilamaz"), null);
			if (!GenericUtil.accessControl(scd, ar.getAccessViewTip(), ar.getApprovalRoles(), ar.getApprovalUsers()))
				throw new IWBException("security", "WorkflowRecord", approvalRecordId, null,
						LocaleMsgCache.get2(0, xlocale, "approval_onay_talebi_hakkiniz_yok"), null);
			if (ar.getApprovalStepId() != 901)
				throw new IWBException("security", "WorkflowRecord", approvalRecordId, null,
						LocaleMsgCache.get2(0, xlocale, "approval_onay_talebi_onceden_yapilmis"), null);
			Map<String, Object> advancedStepSqlResult = null;
			if (a.getAdvancedBeginSql() != null && a.getAdvancedBeginSql().length() > 10) { // calisacak
				parameterMap.put("_tb_pk", "" + ar.getTablePk());
				Object oz = scriptEngine.executeScript(scd, parameterMap, a.getAdvancedBeginSql(), null,
						"wf_" + a.getWorkflowId() + "_abs");
				if (oz != null) {
					if (oz instanceof Boolean) {
						if (!((Boolean) oz))
							throw new IWBException("validation", "WorkflowRecord", approvalRecordId, null,
									LocaleMsgCache.get2(0, xlocale, "approval_request_denied"), null);
					} else
						advancedStepSqlResult = NashornUtil.fromScriptObject2Map(oz);
				}

			}
			nextStep = null;
			switch (a.getApprovalFlowTip()) { // simple
			case 0: // basit onay
				nextStep = a.get_approvalStepList().get(0).getNewInstance();
				break;
			case 1: // complex onay
				if (advancedStepSqlResult != null
						&& GenericUtil.uInt(advancedStepSqlResult.get("approval_step_id")) != 0) {
					nextStep = a.get_approvalStepMap()
							.get(GenericUtil.uInt(advancedStepSqlResult.get("approval_step_id"))).getNewInstance();
				} else {
					nextStep = a.get_approvalStepList().get(0).getNewInstance();
				}
				if(nextStep!=null && advancedStepSqlResult!=null &&
						(!GenericUtil.isEmpty(advancedStepSqlResult.get("approval_users")) || !GenericUtil.isEmpty(advancedStepSqlResult.get("approval_roles")))){
					Object o = advancedStepSqlResult.get("approval_users");
					nextStep.setApprovalUsers(o!=null ? o.toString():"");
					o = advancedStepSqlResult.get("approval_roles");
					nextStep.setApprovalRoles(o!=null ? o.toString():"");
				}

				break;
			case 2: // hierarchical onay //deprecated
				break;
			case 3: // dynamic onay //deprecated
				break;
			}

			break;
		case 1: // onay
			if(!GenericUtil.isEmpty(currentStep.getOnApproveNotificationIds()))
				notificationIds = currentStep.getOnApproveNotificationIds();
			mesaj = " '" + scd.get("completeName") + "' "
					+ LocaleMsgCache.get2(0, xlocale, "approval_presented_for_your_approval");
			switch (a.getApprovalFlowTip()) {
			case 0: // basit onay
				if (a.getActionTip() == 3) { // TODO: delete ise o zaman o kaydi ve bunu sil
					Map<String, String> mz = new HashMap();
					mz.put("ptable_id", "" + ar.getTableId());
					mz.put("ptable_pk", "" + ar.getTablePk());
					scriptEngine.executeGlobalFunc(scd, 690, mz, (short) 2); // bu kaydin child kayitlari var mi?
																				// iwb.w5_table_field'daki
					// default_control_tip ve default_lookup_table_id'ye bakiliyor
					W5FormResult fr = new W5FormResult(-1);
					fr.setForm(new W5Form());
					// fr.getForm().set_sourceTable(PromisCache.getTable(scd, ar.getTableId()));
					fr.setRequestParams(parameterMap);
					fr.setScd(scd);
					parameterMap.put("t" + /* fr.getForm().get_sourceTable() */ FrameworkCache
							.getTable(scd, ar.getTableId()).get_tableFieldList().get(0).getDsc(), "" + ar.getTablePk());
					fr.setPkFields(new HashMap());
					fr.setOutputFields(new HashMap());
					dao.deleteTableRecord(fr, ""); // TODO burasi
				}
				isFinished = true; // basit onay ise hemen bitir
				ar.setApprovalActionTip((short) 5); // bitti(onaylandi)
				ar.setApprovalStepId(998);
				break;
			case 3: // dynamic onay
			case 2: // hierarchical
			case 1: // complex onay
				int nextStepId = 0;
				if (!GenericUtil.isEmpty(currentStep.getOnApproveStepSql())) {
					parameterMap.put("_tb_pk", "" + ar.getTablePk());
					Object oz = scriptEngine.executeScript(scd, parameterMap, currentStep.getOnApproveStepSql(),
							null, "wfs_" + currentStep.getApprovalStepSeqId() + "_ass");
					if (oz != null) {
						if (oz instanceof Boolean) {
							if (!((Boolean) oz))
								throw new IWBException("framework", "WorkflowRecord", approvalRecordId, null,
										LocaleMsgCache.get2(0, xlocale, "approval_denied"), null);
						} else if (oz instanceof Integer) {
							nextStepId = (Integer) oz;
						} else {
							advancedNextStepSqlResult = NashornUtil.fromScriptObject2Map(oz);
							if(advancedNextStepSqlResult!=null && advancedNextStepSqlResult.get("nextStepId")!=null)
								nextStepId = GenericUtil.uInt(advancedNextStepSqlResult.get("nextStepId"));
						}
					}
				}
				if (currentStep.getFinalStepFlag() == 0 && (advancedNextStepSqlResult==null || GenericUtil.uInt(advancedNextStepSqlResult.get("finished_flag"))==0) ) {
					if(nextStepId==0)nextStepId = currentStep.getOnApproveStepId();
					if (nextStepId == 0 && currentStep.getApprovalStepId() == 901) {
						throw new IWBException("validation", "WorkflowRecord", approvalRecordId, null,
								LocaleMsgCache.get2(0, xlocale, "approval_wrong_action"), null);
					}

					nextStep = a.get_approvalStepMap().get(nextStepId);
					nextStep = nextStep.getNewInstance();
				} else {
					nextStep = null;
				}
				if (nextStep == null) {
					isFinished = true;
					ar.setApprovalActionTip((short) 5); // bitti(onaylandi)
					ar.setApprovalStepId(998);
				} else if (advancedNextStepSqlResult != null) {
					if (GenericUtil.uInt(advancedNextStepSqlResult.get("finished_flag")) != 0) { // bitti mi?
						isFinished = true; // approval bitti
						ar.setApprovalActionTip((short) 5);
						ar.setApprovalStepId(998);
					}
				}
				break;
			}

			break;
		case 2: // iade: TODO . baska?
			if(!GenericUtil.isEmpty(currentStep.getOnReturnNotificationIds()))
				notificationIds = currentStep.getOnReturnNotificationIds();
			if (currentStep.getApprovalStepId() == 901) {
				throw new IWBException("validation", "WorkflowRecord", approvalRecordId, null,
						LocaleMsgCache.get2(0, xlocale, "approval_wrong_action"), null);
			}
			mesaj = " '" + scd.get("completeName") + "' "
					+ LocaleMsgCache.get2(0, xlocale, "approval_were_returned_by");
			if (ar.getReturnFlag() == 0) { // yapilamaz
				throw new IWBException("validation", "WorkflowRecord", approvalRecordId, null,
						LocaleMsgCache.get2(0, xlocale, "approval_not_return"), null);
			}
			if (currentStep.getOnReturnStepId() == 0) { // yapilamaz
				throw new IWBException("validation", "WorkflowRecord", approvalRecordId, null,
						LocaleMsgCache.get2(0, xlocale, "approval_not_return_not_setting"), null);
			}
			switch (a.getApprovalFlowTip()) {
			case 0: // basit onay ise bir kisi geriye git
				if (a.getApprovalRequestTip() == 2 && ar.getReturnFlag() != 0) {
					nextStep = new W5WorkflowStep();
					nextStep.setReturnFlag(ar.getReturnFlag());
					nextStep.setApprovalUsers("" + ar.getInsertUserId());
					nextStep.setApprovalStepId(901);
				} else
					throw new IWBException("validation", "WorkflowRecord", approvalRecordId, null,
							LocaleMsgCache.get2(0, xlocale, "approval_not_return_simple_approval"), null);
				break;
			case 1: // complex onay
				int returnStepId = currentStep.getOnReturnStepId();
				if (currentStep.getOnReturnStepSql() != null) {
					parameterMap.put("_tb_pk", "" + ar.getTablePk());
					Object oz = scriptEngine.executeScript(scd, parameterMap, currentStep.getOnReturnStepSql(), null,
							"wfs_" + returnStepId + "_rss");
					if (oz != null) {
						if (oz instanceof Boolean) {
							if (!((Boolean) oz))
								throw new IWBException("validation", "WorkflowRecord", approvalRecordId, null,
										LocaleMsgCache.get2(0, xlocale, "return_denied"), null);
						} else if (oz instanceof Integer) {
							returnStepId = (Integer) oz;
						} else
							advancedNextStepSqlResult = NashornUtil.fromScriptObject2Map(oz);
					}
				}
				nextStep = a.get_approvalStepMap().get(returnStepId).getNewInstance();
				if (nextStep == null) {
					throw new IWBException("validation", "WorkflowRecord", approvalRecordId, null,
							(String) advancedNextStepSqlResult.get("error_msg"), null);
				}
				break;
			case 2: // hierarchical onay
			}

			break;
		case 3: // red
			if(!GenericUtil.isEmpty(currentStep.getOnRejectNotificationIds()))
				notificationIds = currentStep.getOnRejectNotificationIds();
			if (currentStep.getApprovalStepId() == 901) {
				throw new IWBException("validation", "WorkflowRecord", approvalRecordId, null,
						LocaleMsgCache.get2(0, xlocale, "approval_wrong_action"), null);
			}
			if (currentStep.getOnRejectStepSql() != null) {
				parameterMap.put("_tb_pk", "" + ar.getTablePk());
				Object oz = scriptEngine.executeScript(scd, parameterMap, currentStep.getOnRejectStepSql(),
						null, "wfs_" + currentStep.getApprovalStepId() + "_ass2");
				if (oz != null) {
					if (oz instanceof Boolean) {
						if (!((Boolean) oz))
							throw new IWBException("framework", "WorkflowRecord", approvalRecordId, null,
									LocaleMsgCache.get2(0, xlocale, "reject_denied"), null);
					} else { 
						advancedNextStepSqlResult = NashornUtil.fromScriptObject2Map(oz);
					}
				}
				/*
				 * Object[] oz = DBUtil.filterExt4SQL( currentStep.getOnApproveStepSql(), scd,
				 * parameterMap, null); advancedNextStepSqlResult = dao.runSQLQuery2Map(oz[0].
				 * toString(), (List) oz[1], null); if
				 * (advancedNextStepSqlResult.get("next_step_id") != null) nextStepId =
				 * GenericUtil.uInt(advancedNextStepSqlResult.get("next_step_id"));
				 */
			}
			mesaj = " '" + scd.get("completeName") + "' " + LocaleMsgCache.get2(0, xlocale, "approval_rejected_by");
			if (a.getOnRejectTip() == 2) { // red olunca kaydi sil
				// dao.copyTableRecord(a.getTableId(), ar.getTablePk() ,"promis",
				// "promis_approval");
				// //TODO:buna benzer birsey
				W5Table t = FrameworkCache.getTable(scd, a.getTableId());
				String tableDsc = t.getDsc();
				String tablePkDsc = t.get_tableParamList().get(0).getExpressionDsc();
				if(t.get_tableParamList().size()==1) {
					int recordCound = dao.executeUpdateSQLQuery(
							"delete from " + tableDsc + " x where x." + tablePkDsc + "=?",
							ar.getTablePk());
					if (recordCound != 1) {
						throw new IWBException("validation", "Workflow Delete record", approvalRecordId, null,
								"Wrong number of delete record = " + recordCound, null);
				}
				}
			} else { // "rejected" olarak isaretle. approve_record kaydi duracak. approve_step_id:999
				// olacak. finished olacak
				ar.setApprovalStepId(999);
				ar.setApprovalRoles(currentStep.getApprovalRoles());
				ar.setApprovalUsers(currentStep.getApprovalUsers());
			}

			isFinished = true;
			break;
		case 0: // red
			if (currentStep.getApprovalStepId() == 901) {
				throw new IWBException("validation", "WorkflowRecord", approvalRecordId, null,
						LocaleMsgCache.get2(0, xlocale, "approval_wrong_action"), null);
			}
		}
		Log5WorkflowRecord logRecord = new Log5WorkflowRecord();
		logRecord.setProjectUuid(ar.getProjectUuid());
		logRecord.setUserId(userId);
		logRecord.setApprovalRecordId(approvalRecordId);
		if (currentStep != null)
			logRecord.setApprovalStepId(currentStep.getApprovalStepId());
		logRecord.setApprovalId(ar.getApprovalId());
		logRecord.setDsc(parameterMap.get("_adsc"));

		ar.setVersionUserId(userId);

		if ((a.getApprovalFlowTip() != 2 && a.getApprovalFlowTip() != 3) || nextStep != null)
			switch (a.getActionTip()) { // bu hangi tip bir islem?
			case 2: // insert
				switch (approvalAction) {
				case 1: // onay
					if (isFinished) { // son adim mi?
						logRecord.setApprovalActionTip((short) 5); // finished(approved)

					} else { // kompleks adimlar
						logRecord.setApprovalActionTip((short) approvalAction);
						ar.setReturnFlag(nextStep.getReturnFlag());
						ar.setApprovalActionTip((short) approvalAction);
						if (advancedNextStepSqlResult != null
								&& (!GenericUtil.isEmpty(advancedNextStepSqlResult.get("approval_roles"))
										|| !GenericUtil.isEmpty(advancedNextStepSqlResult.get("approval_users")))) {
							ar.setApprovalRoles((String) advancedNextStepSqlResult.get("approval_roles"));
							ar.setApprovalUsers((String) advancedNextStepSqlResult.get("approval_users"));
						} else {
							ar.setApprovalRoles(nextStep.getApprovalRoles());
							ar.setApprovalUsers(nextStep.getApprovalUsers());
						}
						ar.setApprovalStepId(nextStep.getApprovalStepId());
						ar.setAccessViewTip(nextStep.getAccessViewTip());
						ar.setAccessViewRoles(nextStep.getAccessViewRoles());
						ar.setAccessViewUsers(nextStep.getAccessViewUsers());
					}

					break;
				case 901: // onay baslat
				case 2: // iade
					if (nextStep.getApprovalStepId() == 901) { // Dönülecek Adım aslında bir adım değil, onay başlangıcı
																// olacak
						logRecord.setApprovalActionTip((short) approvalAction);
						ar.setApprovalActionTip((short) 0);
						ar.setApprovalRoles(a.getManualAppRoleIds());
						ar.setApprovalUsers(a.getManualAppUserIds());
						if (a.getManualAppRoleIds() == null && a.getManualAppUserIds() == null) {
							if (a.getAdvancedBeginSql() != null && a.getAdvancedBeginSql().length() > 10) { // calisacak
								Map<String, Object> advancedStepSqlResult = null;

								Object oz = scriptEngine.executeScript(scd, parameterMap, a.getAdvancedBeginSql(), null,
										"wf_" + a.getWorkflowId() + "_abs");
								if (oz != null) {
									if (oz instanceof Boolean) {
										if (!((Boolean) oz))
											ar.setApprovalUsers(String.valueOf(ar.getInsertUserId()));
									} else
										advancedStepSqlResult = NashornUtil.fromScriptObject2Map(oz);
								}
								// advancedStepSqlResult = dao.runSQLQuery2Map(a.getAdvancedBeginSql(), scd,
								// parameterMap, null);
								// donen bir cevap var, aktive_flag deger olarak var ve onun degeri 0 ise o
								// zaman girmeyecek
								/*
								 * if (advancedStepSqlResult != null && advancedStepSqlResult.get("active_flag")
								 * != null && GenericUtil.uInt(advancedStepSqlResult.get("active_flag")) == 0) {
								 * // girmeyecek ar.setApprovalUsers(String.valueOf(ar.getInsertUserId())); }
								 * else
								 */if (advancedStepSqlResult != null) {
									if (advancedStepSqlResult.get("approval_users") != null)
										ar.setApprovalUsers((String) advancedStepSqlResult.get("approval_users"));
									if (advancedStepSqlResult.get("approval_roles") != null)
										ar.setApprovalRoles((String) advancedStepSqlResult.get("approval_roles"));
								}
							} else {
								ar.setApprovalUsers(String.valueOf(ar.getInsertUserId()));
							}
						}
						ar.setApprovalStepId(nextStep.getApprovalStepId()); // nextStep.getApprovalStepId() = 901
																			// geliyor sorun
						// yok
						
					} else {
						logRecord.setApprovalActionTip((short) approvalAction);
						ar.setReturnFlag(nextStep.getReturnFlag());
						ar.setApprovalStepId(nextStep.getApprovalStepId());
						if (approvalAction == 901) {
							if (!GenericUtil.isEmpty(nextStep.getApprovalRoles())
									|| !GenericUtil.isEmpty(nextStep.getApprovalUsers())) {
								ar.setApprovalRoles(nextStep.getApprovalRoles());
								ar.setApprovalUsers(nextStep.getApprovalUsers());
							}

							if (!GenericUtil.isEmpty(nextStep.getAccessViewRoles())
									|| !GenericUtil.isEmpty(nextStep.getAccessViewUsers())) {
								ar.setAccessViewTip(nextStep.getAccessViewTip());
								ar.setAccessViewRoles(nextStep.getAccessViewRoles());
								ar.setAccessViewUsers(nextStep.getAccessViewUsers());
							}
						} else if (approvalAction == 2) {
							ar.setApprovalUsers(null);
							ar.setApprovalRoles(null);
							if (!GenericUtil.isEmpty(nextStep.getApprovalRoles())
									|| !GenericUtil.isEmpty(nextStep.getApprovalUsers())) {
								ar.setApprovalRoles(nextStep.getApprovalRoles());
								ar.setApprovalUsers(nextStep.getApprovalUsers());
							}
							
						}
					}
					break;
				case 3: // red
					logRecord.setApprovalActionTip((short) approvalAction);
					break;
				}
				break;

			case 1: // edit
				break;

			case 3: // delete
				break;
			}
		else
			logRecord.setApprovalActionTip((short) approvalAction);


		if (isFinished) { // Finished ise
			ar.setFinishedFlag((short) 1);
			ar.setApprovalRoles(null);
			ar.setApprovalUsers(null);
			ar.setAccessViewTip((short) 0);
		}

		/* Record Save Ediliyor */
		dao.saveObject(logRecord);
		ar.setVersionNo(ar.getVersionNo() + 1);
		dao.updateObject(ar);

		if (!isFinished && (approvalAction == 1 || approvalAction == 2 || approvalAction == 901)) {
			updateEscalationSettings(ar, nextStep);
		}

		// Comment Yazma
		if (!GenericUtil.isEmpty((String) parameterMap.get("_adsc"))) {
			W5Comment comment = new W5Comment((String) scd.get("projectId"));
			comment.setTableId(ar.getTableId());
			comment.setTablePk(ar.getTablePk());
			comment.setDsc(parameterMap.get("_adsc") + "");
			comment.setCommentUserId(userId);
			comment.setCommentDttm(new java.sql.Timestamp(new Date().getTime()));
//	      comment.setCustomizationId(Integer.parseInt(scd.get("customizationId") + ""));
			dao.saveObject(comment);
		}

		result.put("status", true);

		String webPageId = parameterMap.get(".w");
		if (ar != null && !GenericUtil.isEmpty(webPageId)) {
			Map m = new HashMap();
			m.put(".w", webPageId);
			m.put(".pk", ar.getTableId() + "-" + ar.getTablePk());
			m.put(".a", "11");
			m.put(".e", "4");
			UserUtil.liveSyncAction(scd, m); // (customizationId, table_id+"-"+table_pk, userId, webPageId, false);
		}
		if(!GenericUtil.isEmpty(notificationIds)) {
			Map newParamMap = new HashMap();
			newParamMap.putAll(parameterMap);
			newParamMap.put("wf_name", a.getDsc());
			newParamMap.put("wf_stepName", currentStep.getDsc());
			if(nextStep!=null) {
				newParamMap.put("wf_nextStepName", nextStep.getDsc());
			}
			if (!GenericUtil.isEmpty(parameterMap.get("_adsc"))) {
				newParamMap.put("wf_actionComment", parameterMap.get("_adsc"));
			}

			String[] nids = notificationIds.split(",");
			for(int qi=0;qi<nids.length;qi++) {
				int notId = GenericUtil.uInt(nids[qi]);
				if(notId>0) {
					W5FormSmsMail fsm = (W5FormSmsMail)metadataLoader.getMetadataObject("W5FormSmsMail","formSmsMailId", notId, (String) scd.get("projectId"), null);
					if(fsm!=null) {
						if (!GenericUtil.isEmpty(fsm.getConditionSqlCode())) {
							boolean conditionCheck = dao.conditionRecordExistsCheck(scd, newParamMap, ar.getTableId(), ar.getTablePk(), fsm.getConditionSqlCode());
							if (!conditionCheck)
								continue;
						}
						W5Email email = dao.interprateMailTemplate(fsm, scd, newParamMap, ar.getTableId(), ar.getTablePk());
						if(email!=null) {
							email.set_oms(metadataLoader.findObjectMailSetting(scd, email.getMailSettingId()));
							notificationEngine.sendMail(scd, email, parameterMap);
						}
					}
				}
			}
		}
		
		return result;
	}

	public void updateEscalationSettings(W5WorkflowRecord rec, W5WorkflowStep step) {
		if (step.getTimeLimitFlag() != 0 && step.getTimeLimitDuration() > 0) {
			dao.executeUpdateSQLQuery(
					"update iwb.w5_approval_record t set valid_until_dttm=current_timestamp + interval '"
							+ step.getTimeLimitDuration() + " "
							+ new String[] { "minute", "hour", "day", "week", "month" }[step.getTimeLimitDurationTip()
									- 1]
							+ "' where t.approval_record_id=? AND t.project_uuid=?"
//		    			,nextStep.getTimeLimitDuration() + " " + new String[] {"minute","hour","day","week","month"}[nextStep.getTimeLimitDurationTip()-1]
					, rec.getApprovalRecordId(), rec.getProjectUuid());

		}
	}


	public void updateWorkflowEscalatedRecords(W5WorkflowStep step, W5WorkflowStep nextStep) {
		List<W5WorkflowRecord> records = listWorkflowEscalatedRecords(step);
		for (W5WorkflowRecord rec : records) {
			rec.setApprovalStepId(nextStep.getApprovalStepId());
			rec.setApprovalUsers(nextStep.getApprovalUsers());
			rec.setApprovalRoles(nextStep.getApprovalRoles());
			rec.setApprovalActionTip((short) 4); // escalated
			dao.updateObject(rec);
			if (nextStep.getFinalStepFlag() == 0) {
				updateEscalationSettings(rec, nextStep);
			}

			Log5WorkflowRecord logRecord = new Log5WorkflowRecord();
			logRecord.setProjectUuid(step.getProjectUuid());
			logRecord.setUserId(1);// system
			logRecord.setApprovalRecordId(rec.getApprovalRecordId());
			logRecord.setApprovalStepId(step.getApprovalStepId());
			logRecord.setApprovalId(step.getWorkflowId());
			logRecord.setDsc("escalation");
			logRecord.setApprovalActionTip((short) 4);
			dao.saveObject(logRecord);

		}

//			dao.executeUpdateSQLQuery("update iwb.w5_approval_record set ", params)

	}

	public List<W5WorkflowRecord> listWorkflowEscalatedRecords(W5WorkflowStep step) {
		return dao.find(
				"from W5WorkflowRecord t where t.workflowId=?0 AND t.approvalStepId=?1 AND t.projectUuid=?2 AND t.validUntilDttm>current_timestamp",
				step.getWorkflowId(), step.getApprovalStepId(), step.getProjectUuid());
	}

	public void updateWorkflowEscalatedRecord(W5WorkflowStep step, W5WorkflowRecord rec) {
		Map scd = new HashMap();
		scd.put("userId", 1);scd.put("roleId", 999);scd.put("projectId", step.getProjectUuid());
		Map parameterMap = new HashMap();
		parameterMap.put("id", rec.getApprovalRecordId());
		parameterMap.put("pk", rec.getTablePk());
		
		int nextStepId = step.getOnTimeLimitExceedStepId();
		
		Object oz = scriptEngine.executeScript(scd, parameterMap, step.getOnEscalationCode(), null,
				"wec_" + step.getWorkflowId() + "_abs");
		if(oz!=null && oz instanceof Integer)
			nextStepId = (Integer) oz;
		
		W5Workflow w = FrameworkCache.getWorkflow(step.getProjectUuid(), step.getWorkflowId());
		W5WorkflowStep nextStep = w.get_approvalStepMap().get(nextStepId);
		rec.setApprovalStepId(nextStep.getApprovalStepId());
		rec.setApprovalUsers(nextStep.getApprovalUsers());
		rec.setApprovalRoles(nextStep.getApprovalRoles());
		rec.setApprovalActionTip((short) 4); // escalated
		dao.updateObject(rec);
		if (nextStep.getFinalStepFlag() == 0) {
			updateEscalationSettings(rec, nextStep);
		}

		Log5WorkflowRecord logRecord = new Log5WorkflowRecord();
		logRecord.setProjectUuid(step.getProjectUuid());
		logRecord.setUserId(1);// system
		logRecord.setApprovalRecordId(rec.getApprovalRecordId());
		logRecord.setApprovalStepId(step.getApprovalStepId());
		logRecord.setApprovalId(step.getWorkflowId());
		logRecord.setDsc("escalation");
		logRecord.setApprovalActionTip((short) 4);
		dao.saveObject(logRecord);

	}
}
