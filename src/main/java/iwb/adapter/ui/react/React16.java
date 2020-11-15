package iwb.adapter.ui.react;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import iwb.adapter.ui.ViewAdapter;
import iwb.adapter.ui.surveyjs.SurveyJS;
import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.enums.FieldDefinitions;
import iwb.exception.IWBException;
import iwb.model.db.Log5Feed;
import iwb.model.db.W5BIGraphDashboard;
import iwb.model.db.W5Card;
import iwb.model.db.W5Component;
import iwb.model.db.W5Conversion;
import iwb.model.db.W5ConvertedObject;
import iwb.model.db.W5Detay;
import iwb.model.db.W5Form;
import iwb.model.db.W5FormCell;
import iwb.model.db.W5FormCellProperty;
import iwb.model.db.W5FormModule;
import iwb.model.db.W5FormSmsMail;
import iwb.model.db.W5FormSmsMailAlarm;
import iwb.model.db.W5Grid;
import iwb.model.db.W5GridColumn;
import iwb.model.db.W5List;
import iwb.model.db.W5ListColumn;
import iwb.model.db.W5LookUp;
import iwb.model.db.W5LookUpDetay;
import iwb.model.db.W5ObjectMenuItem;
import iwb.model.db.W5ObjectToolbarItem;
import iwb.model.db.W5Page;
import iwb.model.db.W5PageObject;
import iwb.model.db.W5Query;
import iwb.model.db.W5QueryField;
import iwb.model.db.W5QueryParam;
import iwb.model.db.W5Table;
import iwb.model.db.W5TableField;
import iwb.model.db.W5Workflow;
import iwb.model.db.W5WorkflowStep;
import iwb.model.helper.W5CommentHelper;
import iwb.model.helper.W5FormCellHelper;
import iwb.model.helper.W5TableChildHelper;
import iwb.model.helper.W5TableRecordHelper;
import iwb.model.result.W5CardResult;
import iwb.model.result.W5FormResult;
import iwb.model.result.W5GlobalFuncResult;
import iwb.model.result.W5GridResult;
import iwb.model.result.W5ListViewResult;
import iwb.model.result.W5PageResult;
import iwb.model.result.W5QueryResult;
import iwb.model.result.W5TableRecordInfoResult;
import iwb.util.EncryptionUtil;
import iwb.util.GenericUtil;
import iwb.util.UserUtil;

public class React16 implements ViewAdapter {
	final public static String[] labelMap = new String[]{"info","warning","danger"};
	final public static String[] filterMap = new String[]{"","serverFilter","dateRangeFilter","numberFilter","numberFilter","numberFilter"};
	final public static String[] dateFormatMulti = new String[] {"DD/MM/YYYY","MM/DD/YYYY","YYYY/MM/DD"};
	final public static String[] fileAcceptMap = new String[]{"image/*",".xls,.xlsx,.csv",
			".doc,.docx,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document",".pdf",".txt"};
	
	public StringBuilder serializeValidatonErrors(Map<String, String> errorMap,
			String locale) {
		StringBuilder buf = new StringBuilder();
		buf.append("[");
		boolean b = false;
		for (String q : errorMap.keySet()) {
			if (b)
				buf.append("\n,");
			else
				b = true;
			buf.append("{\"id\":\"").append(q).append("\",\"msg\":\"")
					.append(GenericUtil.stringToJS2(errorMap.get(q)))
					.append("\",\"dsc\":\"")
					.append(LocaleMsgCache.get2(0, locale, q)).append("\"}");// TODO.  aslinda customizationId olmasi lazim
		}
		buf.append("]");
		return buf;
	}

	public StringBuilder serializeFormCellStore(W5FormCellHelper cellResult,
			int customizationId, String locale) {
		return null;
	}

	public StringBuilder serializePostForm(W5FormResult formResult) {
		String xlocale = (String) formResult.getScd().get("locale");
		StringBuilder buf = new StringBuilder();

		buf.append("{\n\"formId\": ").append(formResult.getFormId())
				.append(",\n\"success\": ")
				.append(formResult.getErrorMap().isEmpty());
		if (!formResult.getErrorMap().isEmpty())
			buf.append(",\n\"errorType\":\"validation\",\n\"errors\":")
					.append(serializeValidatonErrors(formResult.getErrorMap(),
							xlocale));

		if (!formResult.getOutputMessages().isEmpty()) {
			buf.append(",\n\"msgs\":[");
			boolean b = false;
			for (String s : formResult.getOutputMessages()) {
				if (b)
					buf.append("\n,");
				else
					b = true;
				buf.append("\"").append(GenericUtil.stringToJS2(s)).append("\"");
			}
			buf.append("]");
		}
		if (!formResult.getOutputFields().isEmpty()) {
			buf.append(",\n\"outs\":").append(
					GenericUtil.fromMapToJsonString2Recursive(formResult
							.getOutputFields()));
		}
		/*
		 * if(PromisCache.getAppSettingIntValue(formResult.getScd(),
		 * "bpm_flag")!=0 &&
		 * !PromisUtil.isEmpty(formResult.getNextBpmActions())){
		 * buf.append(",\n\"nextBpmActions\":["); boolean b = false;
		 * for(BpmAction ba:formResult.getNextBpmActions()){
		 * if(b)buf.append("\n,");else b=true;
		 * buf.append("{\"boxLabel\":\"").append
		 * (ba.getDsc()).append("\",\"value\":"
		 * ).append(ba.getActionId()).append(
		 * ",\"js_code\":\"").append(PromisUtil
		 * .stringToJS(ba.getWizardStepJsCode())).append("\"}"); }
		 * buf.append("]"); }
		 */
		if (!GenericUtil.isEmpty(formResult.getPreviewMapList())) {
			buf.append(",\n\"smsMailPreviews\":[");
			boolean b = false;
			for (Map<String, String> m : formResult.getPreviewMapList()) {
				if (b)
					buf.append("\n,");
				else
					b = true;
				buf.append("{\"tbId\":").append(m.get("_tableId"))
						.append(",\"tbPk\":").append(m.get("_tablePk"))
						.append(",\"fsmId\":").append(m.get("_fsmId"))
						.append(",\"fsmTip\":").append(m.get("_fsmTip"))
						.append("}");
			}
			buf.append("]");
		}
		if (!GenericUtil.isEmpty(formResult.getFormAlarmList())) {
			buf.append(",\n\"alarmPreviews\":[");
			boolean b = false;
			for (W5FormSmsMailAlarm fsma : formResult.getFormAlarmList()) {
				if (b)
					buf.append("\n,");
				else
					b = true;
				buf.append("{\"tbId\":")
						.append(fsma.getTableId())
						.append(",\"tbPk\":")
						.append(fsma.getTablePk())
						.append(",\"dsc\":\"")
						.append(GenericUtil.stringToJS2(fsma.getDsc()))
						.append("\",\"fsmId\":")
						.append(fsma.getFormSmsMailId())
						.append(",\"alarmDttm\":\"")
						.append(GenericUtil.uFormatDateTime(fsma.getAlarmDttm()))
						.append("\"}");
			}
			buf.append("]");
		}
		if (!GenericUtil.isEmpty(formResult.getPreviewConversionMapList())) {
			buf.append(",\n\"conversionPreviews\":[");
			boolean b = false;
			for (Map<String, String> m : formResult
					.getPreviewConversionMapList()) {
				if (b)
					buf.append("\n,");
				else
					b = true;
				buf.append(GenericUtil.fromMapToJsonString2(m));
			}
			buf.append("]");
		}
	
		buf.append("\n}");
		return buf;
	}

	public StringBuilder serializeShowForm(W5FormResult fr) {
		StringBuilder s = new StringBuilder();
		s.append("var _page_tab_id='").append(fr.getUniqueId())
				.append("';\n");

			
		if (GenericUtil.uInt(fr.getRequestParams().get("a")) != 5 && fr.getForm().getRenderType() != 0) { // tabpanel ve icinde gridler varsa
			for (W5FormModule m : fr.getForm().get_moduleList()){
					switch (m.getModuleType()) {
					case 4:// form
						if (fr.getModuleFormMap() == null)
							break;
						W5FormResult nfr = fr.getModuleFormMap().get(
								m.getObjectId());
						if (nfr == null)
							return null;
						s.append("class ").append(nfr.getForm().getDsc())
								.append(" extends React.Component").append(serializeGetForm(nfr))
								.append(";\n");
						break;
					case 5:// grid
						if (fr.getModuleGridMap() == null)
							return null;
						if (m.getModuleViewType() == 0
								|| fr.getAction() == m
										.getModuleViewType()) {
							W5GridResult gridResult = fr
									.getModuleGridMap().get(m.getObjectId());
							gridResult.setAction(fr.getAction());
							W5Table mainTable = gridResult.getGrid() != null
									&& gridResult.getGrid()
											.get_defaultCrudForm() != null ? FrameworkCache
									.getTable(gridResult.getScd(), gridResult
											.getGrid().get_defaultCrudForm()
											.getObjectId()) : null;
							if (mainTable != null
									&& (!GenericUtil
											.accessControl(
													fr.getScd(),
													mainTable
															.getAccessViewTip(),
													mainTable
															.getAccessViewRoles(),
													mainTable
															.getAccessViewUsers())))
								gridResult = null;// hicbirsey
							else {
								gridResult.setViewReadOnlyMode(fr
										.isViewMode());
								s.append("\n")
										.append(fr.getForm().get_renderTemplate()!=null && fr.getForm().get_renderTemplate().getLocaleMsgFlag() != 0 ? GenericUtil
												.filterExt(serializeGrid(gridResult).toString(),
														fr.getScd(),
														fr.getRequestParams(),
														null)
												: serializeGrid(gridResult))
										.append("\n");
							}
						}
					}
				}
		}

		W5Form f = fr.getForm();
		
		if(f.getRenderType()==4) {//wizard and insert
			return SurveyJS.serializeForm4SurveyJS(fr, 5)
					.append("return _(CardBody,{},_('i',{style:{float:'right',fontSize: '1.5rem', color: '#999', marginTop: 11, cursor:'pointer'},onClick:()=>iwb.closeTab(), className:'icon-close'}), _(Survey.Survey,{model:survey}))");//react			
		}
		
		Map<String, Object> scd = fr.getScd();
		String xlocale = (String) scd.get("locale");
		int customizationId = (Integer) scd.get("customizationId");
		int userId = (Integer) scd.get("userId");
		
		s.append("var cfgForm={formId: ")
			.append(fr.getFormId()).append(", a:").append(fr.getAction())
			.append(", name:'").append(LocaleMsgCache.get2(customizationId, xlocale, fr.getForm().getLocaleMsgKey()))
			.append("',\n id:'").append(fr.getUniqueId()).append("', defaultWidth:").append(f.getDefaultWidth()).append(", defaultHeight:").append(f.getDefaultHeight());
	
	
	
//		boolean liveSyncRecord = false;
		// form(table) fields
		if (f.getObjectType() == 2
				&& FrameworkCache.getTable(scd, f.getObjectId()) != null) {
			s.append(",\n renderTip:").append(fr.getForm().getRenderType());
			W5Table t = FrameworkCache.getTable(scd, f.getObjectId());
			// insert AND continue control
			s.append(", crudTableId:").append(f.getObjectId());
			if (fr.getAction() == 2) { // insert
				long tmpId = -GenericUtil.getNextTmpId();
				s.append(", contFlag:").append(f.getContEntryFlag() != 0).append(",\n tmpId:").append(tmpId);
				fr.getRequestParams().put("_tmpId", "" + tmpId);
			} else if (fr.getAction() == 1) { // edit
				s.append(",\n pk:").append(GenericUtil.fromMapToJsonString(fr.getPkFields()));
				if(!fr.isViewMode() && (t.getAccessDeleteTip()==0 || !GenericUtil.isEmpty(t.getAccessDeleteUserFields()) || GenericUtil.accessControl(scd, t.getAccessDeleteTip(), t.getAccessDeleteRoles(), t.getAccessDeleteUsers())))s.append(", deletable:!0");
				
			}
	
		
			boolean mobile = GenericUtil.uInt(fr.getScd().get("mobile")) != 0;

			if (FrameworkCache.getAppSettingIntValue(scd, "make_comment_flag") != 0
					&& t.getMakeCommentFlag() != 0){
				s.append(",\n commentFlag:true, commentCount:");
				if(fr.getCommentExtraInfo()!=null){
					String[] ozc = fr.getCommentExtraInfo().split(";");//commentCount;commentUserId;lastCommentDttm;viewUserIds-msg
					int ndx = ozc[3].indexOf('-');
					s.append(ozc[0]).append(", commentExtra:{\"last_dttm\":\"").append(ozc[2])
						.append("\",\"user_id\":").append(ozc[1])
						.append(",\"user_dsc\":\"").append(UserUtil.getUserDsc( GenericUtil.uInt(ozc[1])))
						.append("\",\"is_new\":").append(!GenericUtil.hasPartInside(ozc[3].substring(0,ndx), userId+""))
						.append(",\"msg\":\"").append(GenericUtil.stringToHtml(ozc[3].substring(ndx+1)))
						.append("\"}");
				} else s.append(fr.getCommentCount());
			}
			if (FrameworkCache.getAppSettingIntValue(scd, "file_attachment_flag") != 0 && t.getFileAttachmentFlag() != 0
					&& FrameworkCache.roleAccessControl(scd, 101))
				s.append(",\n fileAttachFlag:true, fileAttachCount:").append(fr.getFileAttachmentCount());
	
			if (fr.isViewMode())s.append(",\n viewMode:true");
	
			if (!fr.isViewMode() && f.get_formSmsMailList() != null
					&& !f.get_formSmsMailList().isEmpty()) { // automatic sms isleri varsa
				int cnt = 0;
				for (W5FormSmsMail fsm : f.get_formSmsMailList())
					if (fsm.getSmsMailSentType() != 3
							&& ((fsm.getSmsMailType() == 0 && FrameworkSetting.sms) || (fsm
									.getSmsMailType() != 0 && FrameworkSetting.mail))
							&& fsm.getAlarmFlag() == 0
							&& GenericUtil.hasPartInside2(fsm.getActionTypes(),
									fr.getAction())
							&& GenericUtil.hasPartInside2(fsm.getWebMobileTips(), mobile ? "2" : "1")) {
						cnt++;
					}
				if (cnt > 0) {
					s.append(",\n\"smsMailTemplateCnt\":").append(cnt++).append(",\n\"smsMailTemplates\":[");
					boolean b = false;
					for (W5FormSmsMail fsm : f.get_formSmsMailList())
						if (fsm.getSmsMailSentType() != 3
								&& ((fsm.getSmsMailType() == 0
										&& FrameworkSetting.sms) || (fsm
										.getSmsMailType() != 0
										&& FrameworkSetting.mail))
								&& fsm.getAlarmFlag() == 0
								&& GenericUtil.hasPartInside2(
										fsm.getActionTypes(),
										fr.getAction())
								&& GenericUtil
										.hasPartInside2(fsm.getWebMobileTips(),
												mobile ? "2" : "1")) {
							if (b)
								s.append("\n,");
							else
								b = true;
							s.append("{\"xid\":")
									.append(fsm.getFormSmsMailId())
									.append(",\"text\":\"")
//									.append(fsm.getSmsMailTip() == 0 ? "[SMS] ": "[" + (LocaleMsgCache.get2( customizationId,xlocale, "email_upper")) + "] ")
									.append(LocaleMsgCache.get2(
											customizationId, xlocale,
											fsm.getDsc()))
									.append("\",\"checked\":")
									.append(fsm.getSmsMailSentType() == 1
											|| fsm.getSmsMailSentType() == 0)
									.append(",\"smsMailTip\":")
									.append(fsm.getSmsMailType())
									.append(",\"previewFlag\":")
									.append(fsm.getPreviewFlag() != 0);
							if (fsm.getSmsMailSentType() == 0)
								s.append(",\"disabled\":true");
							s.append("}");
						}
					s.append("]");
				}
	
				if (FrameworkSetting.alarm) {
					cnt = 0;
					for (W5FormSmsMail fsm : f.get_formSmsMailList())
						if (fsm.getSmsMailSentType() != 3
								&& ((fsm.getSmsMailType() == 0
										&& FrameworkSetting.sms) || (fsm
										.getSmsMailType() != 0
										&& FrameworkSetting.mail))
								&& fsm.getAlarmFlag() != 0
								&& GenericUtil.hasPartInside2(
										fsm.getActionTypes(),
										fr.getAction())
								&& GenericUtil
										.hasPartInside2(fsm.getWebMobileTips(),
												mobile ? "2" : "1")) {
							cnt++;
						}
					if (cnt > 0) {
						Map<Integer, W5FormSmsMailAlarm> alarmMap = new HashMap();
						if (!GenericUtil.isEmpty(fr.getFormAlarmList()))
							for (W5FormSmsMailAlarm a : fr
									.getFormAlarmList()) {
								alarmMap.put(a.getFormSmsMailId(), a);
							}
						s.append(",\n\"alarmTemplateCnt\":").append(cnt++)
								.append(",\n\"alarmTemplates\":[");
						boolean b = false;
						for (W5FormSmsMail fsm : f.get_formSmsMailList())
							if (fsm.getSmsMailSentType() != 3
									&& ((fsm.getSmsMailType() == 0
											&& FrameworkSetting.sms) || (fsm
											.getSmsMailType() != 0
											&& FrameworkSetting.mail))
									&& fsm.getAlarmFlag() != 0
									&& GenericUtil.hasPartInside2(
											fsm.getActionTypes(),
											fr.getAction())
									&& GenericUtil.hasPartInside2(fsm.getWebMobileTips(), mobile ? "2"
											: "1")) {
								W5FormSmsMailAlarm a = alarmMap.get(fsm
										.getFormSmsMailId());
								if (b)
									s.append("\n,");
								else
									b = true;
								s.append("{\"xid\":")
										.append(fsm.getFormSmsMailId())
										.append(",\"text\":\"")
//										.append(fsm.getSmsMailTip() == 0 ? "[SMS] " : "["+ (LocaleMsgCache.get2(customizationId, xlocale, "email_upper")) + "] ")
										.append(GenericUtil.stringToJS(fsm
												.getDsc()))
										.append("\",\"checked\":")
										.append(a != null
												|| fsm.getSmsMailSentType() == 1
												|| fsm.getSmsMailSentType() == 0)
										.append(",\"smsMailTip\":")
										.append(fsm.getSmsMailType());
								s.append(",\"previewFlag\":").append(
										fsm.getPreviewFlag() != 0);
								if ((a != null && a.getStatus() != 1)
										|| fsm.getSmsMailSentType() == 0)
									s.append(",\"disabled\":true");
								// s.append(",\"menu\":[");
								// s.append("new Ext.ux.form.DateTime({\"width\":200");
								if (a != null && a.getStatus() != 1)
									s.append(",\"disabled2\":true");
								if (a != null)
									s.append(",\"value\":\"")
											.append(GenericUtil
													.uFormatDateTime(a
															.getAlarmDttm()))
											.append("\"");
								W5TableField rtf = t.get_tableFieldMap().get(
										fsm.getAlarmDttmFieldId());
								if (rtf != null) {
									s.append(", \"relatedFieldName\":\"")
											.append(rtf.getDsc())
											.append("\",\"timeTip\":")
											.append(fsm.getAlarmTmTip())
											.append(",\"timeDif\":\"")
											.append(fsm.getAlarmTm())
											.append("\"");
								}
								
								s.append("}");
							}
						
						s.append("]");
					}
				}
			}
	
			if (f.get_conversionList() != null
					&& !f.get_conversionList().isEmpty()) {
				int cnt = 0;
				for (W5Conversion fsm : f.get_conversionList())
					if (fsm.getConversionType() != 3
							&& GenericUtil.hasPartInside2(fsm.getActionTypes(),
									fr.getAction())) { // bu action ile
																// ilgili var mi
																// kayit
						cnt++;
					}
				if (!fr.isViewMode()
						&& (cnt > 0 || !GenericUtil.isEmpty(fr
								.getMapConvertedObject()))) {
					s.append(",\nconversionCnt:")
							.append(f.get_conversionList().size())
							.append(",\nconversionForms:[");
					boolean b = false;
					for (W5Conversion fsm : f.get_conversionList())
						if ((fsm.getConversionType() != 3/* invisible-checked */
								&& GenericUtil.hasPartInside2(
										fsm.getActionTypes(),
										fr.getAction()) || (fr
								.getMapConvertedObject() != null && fr
								.getMapConvertedObject().containsKey(
										fsm.getConversionId())))) {
							W5Table dt = FrameworkCache.getTable(scd,
									fsm.getDstTableId());
							if ((dt.getAccessViewTip() == 0
									|| !GenericUtil.isEmpty(dt
											.getAccessUpdateUserFields()) || GenericUtil
										.accessControl(scd,
												dt.getAccessViewTip(),
												dt.getAccessViewRoles(),
												dt.getAccessViewUsers()))
									&& GenericUtil.accessControl(scd,
											dt.getAccessInsertTip(),
											dt.getAccessInsertRoles(),
											dt.getAccessInsertUsers())) {
								if (b)
									s.append("\n,");
								else
									b = true;
								boolean isConvertedBefore = fr
										.getAction() == 1
										&& fr.getMapConvertedObject() != null
										&& fr.getMapConvertedObject()
												.containsKey(
														fsm.getConversionId());
								boolean check = false;
								List<W5ConvertedObject> convertedObjects = null;
								if (isConvertedBefore
										&& fsm.getConversionType() != 3
										&& GenericUtil.hasPartInside2(
												fsm.getActionTypes(),
												fr.getAction())) {
									convertedObjects = fr
											.getMapConvertedObject().get(
													fsm.getConversionId());
									if (fsm.getMaxNumofConversion() == 0
											|| convertedObjects.size() < fsm
													.getMaxNumofConversion()) {
										check = true;
									}
								} else
									check = true;
								if (check) {
									s.append("{xid:")
											.append(fsm.getConversionId())
											.append(",text:\"")
											.append(LocaleMsgCache.get2(scd,
													fsm.getDsc()))
//											.append(formResult.getAction() == 2 ? (fsm.getPreviewFlag() != 0 ? " (<i>" + (LocaleMsgCache.get2( customizationId, xlocale, "with_preview")) + "</i>)" : "") : "")
											.append("\",checked:")
											.append(fsm.getConversionType() == 1
													|| fsm.getConversionType() == 0);
									if (fsm.getConversionType() == 0)
										s.append(",disabled:true");
									s.append(",previewFlag:").append(fsm.getPreviewFlag() != 0 );
									s.append("}");
								}
								if (isConvertedBefore
										&& convertedObjects != null)
									for (W5ConvertedObject co : convertedObjects)
										if (co.get_relatedRecord().size() > 0) {
											if (check)
												s.append("\n,");
											else
												check = true;
											// if(fsm.getSynchOnUpdateFlag()!=0)co.get_relatedRecord().get(0).setRecordDsc(co.get_relatedRecord().get(0).getRecordDsc()+" (<i color=red>auto_update</i>)");
											s.append("{lbl:\"")
													.append(LocaleMsgCache
															.get2(scd,
																	fsm.getDsc())
															.substring(0, 5))
													.append("\",").append(FieldDefinitions.queryFieldName_HierarchicalData).append(":")
													.append(serializeTableHelperList(
															customizationId,
															xlocale,
															co.get_relatedRecord()));
											if (fsm.getSynchOnUpdateFlag() != 0)
												s.append(",sync:true");
											s.append("}");
										}
							}
						}
					s.append("]");
				}
	
				cnt = 0;
				for (W5Conversion fsm : f.get_conversionList())
					if (GenericUtil.hasPartInside2(fsm.getActionTypes(), 0)) { // manuel
																				// icin
																				// var
																				// mi
						cnt++;
					}
				if (cnt > 0) {
					s.append(",\nmanualConversionForms:[")
							.append(serializeManualConversions(scd,
									f.get_conversionList())).append("]");
				}
			}
			
			if (fr.getApprovalRecord() != null && FrameworkCache.getWorkflow(fr.getScd(), fr.getApprovalRecord().getApprovalId())!=null) { // Burası Artık Onay Mekanizması başlamış
				W5Workflow a = FrameworkCache.getWorkflow(fr.getScd(), fr.getApprovalRecord().getApprovalId());
				if (fr.getApprovalRecord().getApprovalStepId() == 901) {// kendisi start for approval yapacak
					if ((a.getManualAppUserIds() == null
							&& a.getManualAppRoleIds() == null
							&& GenericUtil
									.accessControl(scd, fr
											.getApprovalRecord()
											.getApprovalActionTip() /*
																	 * ??? Bu ne
																	 */,
											fr.getApprovalRecord()
													.getApprovalRoles(), fr
													.getApprovalRecord()
													.getApprovalUsers()) || (GenericUtil
							.hasPartInside2(a.getManualAppRoleIds(),
									scd.get("roleId")) || GenericUtil
							.hasPartInside2(a.getManualAppUserIds(),
									scd.get("userId")))) // Burası daha güzel
															// yazılabilir
					)// TODO:Buraya tableUserIdField yetki kontrolü eklenecek
						// (a.getManualAppTableFieldIds())
						s.append(",\n approval:{approvalRecordId:")
								.append(fr.getApprovalRecord()
										.getApprovalRecordId())
								.append(",wait4start:true,status:901,dsc:\"")
								.append(LocaleMsgCache.get2(scd, a.getDsc())).append("\",btnStartApprovalLabel:\"")
								    								.append(LocaleMsgCache.get2(scd, a.getApprovalRequestMsg())).append("\"}");
				} else if (fr.getApprovalRecord().getApprovalStepId() > 997 && fr.getApprovalRecord().getApprovalStepId() < 1000) {// rejected(999) or approved(998)
					s.append(",\n approval:{approvalRecordId:")
								.append(fr.getApprovalRecord()
										.getApprovalRecordId())
								.append(",status:").append(fr.getApprovalRecord().getApprovalStepId()).append(",dsc:\"")
								.append(LocaleMsgCache.get2(scd, a.getDsc())).append("\"}, deletable:false");
				} else if (GenericUtil.accessControl(scd, (short) 1, fr
						.getApprovalRecord().getApprovalRoles(), fr
						.getApprovalRecord().getApprovalUsers())) {
					s.append(",\n approval:{approvalRecordId:").append(fr.getApprovalRecord()
							.getApprovalRecordId()).append(",versionNo:")
					.append(fr.getApprovalRecord().getVersionNo())
					.append(",returnFlag:")
					.append(fr.getApprovalRecord().getReturnFlag() != 0).append(",dsc:\"")
					.append(LocaleMsgCache.get2(scd, a.getDsc())).append("\"");
					W5WorkflowStep wfs = a.get_approvalStepMap().get(fr.getApprovalRecord().getApprovalStepId());
					if(wfs.getOnApproveFormId()!=null)s.append(",approveFormId:").append(wfs.getOnApproveFormId());
					if(wfs.getOnRejectFormId()!=null)s.append(",rejectFormId:").append(wfs.getOnRejectFormId());
					if(wfs.getOnReturnFormId()!=null)s.append(",returnFormId:").append(wfs.getOnReturnFormId());
					s.append(",btnApproveLabel:\"").append(LocaleMsgCache.get2(scd, wfs.getBtnApproveLabel())).append("\"");
					if((fr.getApprovalRecord().getReturnFlag() != 0))
						s.append(",btnReturnLabel:\"").append(LocaleMsgCache.get2(scd, wfs.getBtnReturnLabel())).append("\"");
					if(GenericUtil.safeEquals(wfs.getOnRejectStepSql(),"!"))
						s.append(",reject:false");
					s.append(",stepDsc:'")
							.append(fr.getApprovalStep() != null ? GenericUtil
									.stringToJS(fr.getApprovalStep()
											.getDsc()) : "-")
							.append("'}");
					if(wfs.getAccessDeleteTip()!=0 && GenericUtil.isEmpty(wfs.getAccessDeleteUserFields()) && !GenericUtil.accessControl(scd, wfs.getAccessDeleteTip(), wfs.getAccessDeleteRoles(), wfs.getAccessDeleteUsers()))s.append(", deletable:false");

				}
			}
		}
		boolean b = false;
		if (!fr.getOutputMessages().isEmpty()) {
			s.append(",\n\"msgs\":[");
			for (String sx : fr.getOutputMessages()) {
				if (b)s.append("\n,");
				else b = true;
				s.append("'").append(GenericUtil.stringToJS(sx)).append("'");
			}
			s.append("]");
		}
	
		
		if ((fr.getForm().getObjectType()!=2 || fr.getAction()==1) && f.get_toolbarItemList().size() > 0) { // extra buttonlari var mi yok
													// mu?
			StringBuilder buttons = serializeToolbarItems(scd,
					f.get_toolbarItemList(), (fr.getFormId() > 0 ? true
							: false), null);
			if (buttons.length() > 1) {
				s.append(",\n extraButtons:[").append(buttons).append("]");
			}
		}
		for (String sx : fr.getOutputFields().keySet()) {
			s.append(",\n ").append(sx).append(":")
					.append(fr.getOutputFields().get(sx));// TODO:aslinda' liolması lazim
		}
		s.append("};\nclass bodyForm extends XForm").append(serializeGetForm(fr));

		if (fr.getForm().get_renderTemplate() != null && fr.getForm().getRenderTemplateId()!=26) {
				s.append("\n").append(
					fr.getForm().get_renderTemplate()
							.getLocaleMsgFlag() != 0 ? GenericUtil
							.filterExt(fr.getForm()
									.get_renderTemplate().getCode(),
									fr.getScd(),
									fr.getRequestParams(), null)
							: fr.getForm().get_renderTemplate()
									.getCode());
		} else if(true || fr.getForm().getObjectType()==2)
			s.append("\nreturn _(XTabForm, {body:bodyForm, cfg:cfgForm, parentCt:parentCt, callAttributes:callAttributes});");
		

		return s;
	}

	public StringBuilder serializeGetFormSimple(W5FormResult formResult) {
		StringBuilder s = new StringBuilder();
		String xlocale = (String) formResult.getScd().get("locale");
		int customizationId = (Integer) formResult.getScd().get(
				"customizationId");
		boolean mobile = GenericUtil.uInt(formResult.getScd().get("mobile")) != 0;

		W5Form f = formResult.getForm();
		s.append("{\n\"success\":true, \"formId\":")
				.append(formResult.getFormId()).append(", \"a\":")
				.append(formResult.getAction());
		W5Table t = null;
		if (f.getObjectType() == 2) {
			t = FrameworkCache.getTable(formResult.getScd(), f.getObjectId());
			if (FrameworkCache.getAppSettingIntValue(formResult.getScd(),
					"file_attachment_flag") != 0
					&& t.getFileAttachmentFlag() != 0)
				s.append(",\n \"fileAttachFlag\":true, \"fileAttachCount\":")
						.append(formResult.getFileAttachmentCount());
		}

		if (formResult.getAction() == 2) {
			long tmpId = -GenericUtil.getNextTmpId();
			s.append(",\n \"tmpId\":").append(tmpId);
		}

		if (f.get_formSmsMailList() != null
				&& !f.get_formSmsMailList().isEmpty()) { // automatic sms isleri
															// varsa
			int cnt = 0;
			for (W5FormSmsMail fsm : f.get_formSmsMailList())
				if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms) || (fsm
						.getSmsMailType() != 0 && FrameworkSetting.mail))
						&& fsm.getAlarmFlag() == 0
						&& GenericUtil.hasPartInside2(fsm.getActionTypes(),
								formResult.getAction())
						&& GenericUtil.hasPartInside2(fsm.getWebMobileTips(),
								mobile ? "2" : "1")) {
					cnt++;
				}
			if (cnt > 0) {
				s.append(",\n\"smsMailTemplateCnt\":").append(cnt)
						.append(",\n\"smsMailTemplates\":[");
				boolean b = false;
				for (W5FormSmsMail fsm : f.get_formSmsMailList())
					if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms) || (fsm
							.getSmsMailType() != 0 && FrameworkSetting.mail))
							&& fsm.getAlarmFlag() == 0
							&& GenericUtil.hasPartInside2(fsm.getActionTypes(),
									formResult.getAction())
							&& GenericUtil.hasPartInside2(
									fsm.getWebMobileTips(), mobile ? "2" : "1")) {
						if (b)
							s.append("\n,");
						else
							b = true;
						s.append("{\"xid\":")
								.append(fsm.getFormSmsMailId())
								.append(",\"text\":\"")
//								.append(fsm.getSmsMailTip() == 0 ? "[SMS] ": "[" + (LocaleMsgCache.get2(customizationId, xlocale, "email_upper")) + "] ")
								.append(LocaleMsgCache.get2(customizationId,
										xlocale, fsm.getDsc()))
								.append(fsm.getPreviewFlag() != 0 ? " ("
										+ (LocaleMsgCache.get2(
												customizationId, xlocale,
												"with_preview")) + ")" : "")
								.append("\",\"checked\":")
								.append(fsm.getSmsMailSentType() == 1
										|| fsm.getSmsMailSentType() == 0)
								.append(",\"smsMailTip\":")
								.append(fsm.getSmsMailType())
								.append(",\"previewFlag\":")
								.append(fsm.getPreviewFlag() != 0);
						if (fsm.getSmsMailSentType() == 0)
							s.append(",\"disabled\":true");
						s.append("}");
					}
				s.append("]");
			}

			cnt = 0;
			for (W5FormSmsMail fsm : f.get_formSmsMailList())
				if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms) || (fsm
						.getSmsMailType() != 0 && FrameworkSetting.mail))
						&& fsm.getAlarmFlag() != 0
						&& GenericUtil.hasPartInside2(fsm.getActionTypes(),
								formResult.getAction())
						&& GenericUtil.hasPartInside2(fsm.getWebMobileTips(),
								mobile ? "2" : "1")) {
					cnt++;
				}
			if (cnt > 0) {
				Map<Integer, W5FormSmsMailAlarm> alarmMap = new HashMap();
				if (!GenericUtil.isEmpty(formResult.getFormAlarmList()))
					for (W5FormSmsMailAlarm a : formResult.getFormAlarmList()) {
						alarmMap.put(a.getFormSmsMailId(), a);
					}
				s.append(",\n\"alarmTemplateCnt\":").append(cnt++)
						.append(",\n\"alarmTemplates\":[");
				boolean b = false;
				for (W5FormSmsMail fsm : f.get_formSmsMailList())
					if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms) || (fsm
							.getSmsMailType() != 0 && FrameworkSetting.mail))
							&& fsm.getAlarmFlag() != 0
							&& GenericUtil.hasPartInside2(fsm.getActionTypes(),
									formResult.getAction())
							&& GenericUtil.hasPartInside2(
									fsm.getWebMobileTips(), mobile ? "2" : "1")) {
						W5FormSmsMailAlarm a = alarmMap.get(fsm
								.getFormSmsMailId());
						if (b)
							s.append("\n,");
						else
							b = true;
						s.append("{\"xid\":")
								.append(fsm.getFormSmsMailId())
								.append(",\"text\":\"")
//								.append(fsm.getSmsMailTip() == 0 ? "[SMS] " : "[" + (LocaleMsgCache.get2( customizationId, xlocale, "email_upper")) + "] ")
								.append(GenericUtil.stringToJS(fsm.getDsc()))
								.append(fsm.getPreviewFlag() != 0 ? " ("
										+ (LocaleMsgCache.get2(
												customizationId, xlocale,
												"with_preview")) + ")" : "")
								.append("\",\"checked\":")
								.append(a != null
										|| fsm.getSmsMailSentType() == 1
										|| fsm.getSmsMailSentType() == 0)
								.append(",\"smsMailTip\":")
								.append(fsm.getSmsMailType());
						s.append(",\"previewFlag\":").append(
								fsm.getPreviewFlag() != 0);
						if ((a != null && a.getStatus() != 1)
								|| fsm.getSmsMailSentType() == 0)
							s.append(",\"disabled\":true");
						// s.append(",\"menu\":[");
						// s.append("new Ext.ux.form.DateTime({\"width\":200");
						if (a != null && a.getStatus() != 1)
							s.append(",\"disabled2\":true");
						if (a != null)
							s.append(",\"value\":\"")
									.append(GenericUtil.uFormatDateTime(a
											.getAlarmDttm())).append("\"");
						W5TableField rtf = t.get_tableFieldMap().get(
								fsm.getAlarmDttmFieldId());
						if (rtf != null) {
							s.append(", \"relatedFieldName\":\"")
									.append(rtf.getDsc())
									.append("\",\"timeTip\":")
									.append(fsm.getAlarmTmTip())
									.append(",\"timeDif\":\"")
									.append(fsm.getAlarmTm()).append("\"");
						}
						// s.append("})");

						/*
						 * s.append(
						 * "{\"xtype\": \"datefield\", \"width\": 115, \"format\": \"d/m/Y H:i\""
						 * );
						 * if(a!=null)s.append(",\"value\":\"").append(PromisUtil
						 * .uFormatDate(a.getAlarmDttm())).append("\"");
						 * W5TableField rtf =
						 * t.get_tableFieldMap().get(fsm.getAlarmDttmFieldId());
						 * if(rtf!=null){
						 * s.append(", \"relatedFieldName\":\"").append
						 * (rtf.getDsc
						 * ()).append("\",\"timeTip\":").append(fsm.getAlarmTmTip
						 * (
						 * )).append(",\"timeDif\":\"").append(fsm.getAlarmTm())
						 * .append("\""); } s.append("}");
						 */
						// s.append("]");
						s.append("}");
					}
				s.append("]");
			}
		}
		if (!formResult.getOutputMessages().isEmpty()) {
			s.append(",\n\"msgs\":[");
			boolean b = false;
			for (String sx : formResult.getOutputMessages()) {
				if (b)
					s.append("\n,");
				else
					b = true;
				s.append("\"").append(GenericUtil.stringToJS2(sx)).append("\"");
			}
			s.append("]");
		}
		if (formResult.isViewMode())
			s.append(",\n \"readOnly\":true");
		s.append(",\n\"cells\":[");
		boolean b = false, bb;
		for (W5FormCellHelper fc : formResult.getFormCellResults())
			if (fc.getFormCell().getActiveFlag() != 0
					&& fc.getFormCell().getControlType() != 102) {
				if (fc.getFormCell().getControlType() != 102) {// label'dan
																// farkli ise.
																// label direk
																// render
																// edilirken
																// koyuluyor
					if (b)
						s.append("\n,");
					else
						b = true;
					s.append("{\"id\":\"")
							.append(fc.getFormCell().getDsc())
							.append("\",\"label\":\"")
							.append(LocaleMsgCache
									.get2(customizationId, xlocale, fc
											.getFormCell().getLocaleMsgKey()))
							.append("\",\"not_null\":")
							.append(fc.getFormCell().getNotNullFlag() != 0)
							.append(",\"value\":\"");
					if (!GenericUtil.isEmpty(fc.getHiddenValue())) {
						s.append(GenericUtil.stringToJS2(fc.getHiddenValue()))
								.append("\"").append(", \"readOnly\":true");
					} else if (!GenericUtil.isEmpty(fc.getValue())) {
						s.append(GenericUtil.stringToJS2(fc.getValue())).append(
								"\"");
					} else
						s.append("\"");
					switch (fc.getFormCell().getControlType()) {
					case 10:// advanced select
						if (!GenericUtil.isEmpty(fc.getValue())
								&& fc.getLookupQueryResult() != null
								&& !GenericUtil.isEmpty(fc
										.getLookupQueryResult().getData())
								&& !GenericUtil.isEmpty(fc
										.getLookupQueryResult().getData()
										.get(0)[0]))
							s.append(", \"text\":\"")
									.append(GenericUtil.stringToJS2(fc
											.getLookupQueryResult().getData()
											.get(0)[0].toString()))
									.append("\"");
						break;
					case 6:// static
						s.append(", \"data\":[");
						bb = false;
						for (W5Detay p : (List<W5Detay>) fc.getLookupListValues()) {
							if (bb)
								s.append(",");
							else
								bb = true;
							s.append("[\"")
									.append(GenericUtil.stringToJS2(fc
											.getLocaleMsgFlag() != 0 ? LocaleMsgCache
											.get2(customizationId, xlocale,
													p.getDsc()) : p.getDsc()))
									.append("\",\"").append(p.getVal())
									.append("\"");
							s.append("]");
						}
						s.append("]");
						break;
					case 7: // query
						if (!GenericUtil.isEmpty(fc.getLookupQueryResult()
								.getData())) {
							s.append(", \"data\":[");
							bb = false;
							for (Object[] p : fc.getLookupQueryResult()
									.getData()) {
								if (bb)
									s.append(",");
								else
									bb = true;
								boolean bbb = false;
								s.append("[");
								for (W5QueryField qf : fc
										.getLookupQueryResult().getQuery()
										.get_queryFields()) {
									Object z = p[qf.getTabOrder() - 1];
									if (bbb)
										s.append(",");
									else
										bbb = true;
									if (z == null)
										z = "";
									s.append("\"")
											.append(qf.getPostProcessType() == 2 ? LocaleMsgCache
													.get2(customizationId,
															xlocale,
															z.toString())
													: GenericUtil.stringToJS2(z
															.toString()))
											.append("\"");
								}
								s.append("]");
							}
							s.append("]");
						}

					}
					// if(fc.getFormCell().getControlTip()==24)s.append("_").append(fc.getFormCell().getDsc()).append(".treePanel.getRootNode().expand();\n");
					s.append("}");
				}
			}
		s.append("]}");
		return s;
	}

	private StringBuilder serializeGetForm(W5FormResult formResult) {
		Map<String, Object> scd = formResult.getScd();
		StringBuilder s = new StringBuilder();
		String xlocale = (String) scd.get("locale");
		int customizationId = (Integer) scd.get("customizationId");
		int userId = (Integer) scd.get("userId");
		boolean mobile = GenericUtil.uInt(scd.get("mobile")) != 0;

		if (formResult.getUniqueId() == null)formResult.setUniqueId(GenericUtil.getNextId("fi2"));
		W5Form f = formResult.getForm();
		// s.append("var ").append(formResult.getForm().getDsc()).append("=");
		String[] postFormStr = new String[] { "", "search_form",
				"ajaxPostForm",
				f.getObjectType() == 3 ? "rpt/" + f.getDsc() : "ajaxExecDbFunc",
				"ajaxExecDbFunc",null,null,"search_form", "search_form", null,null,"ajaxCallWs?serviceName="+(f.getObjectType() == 11 ? FrameworkCache.getServiceNameByMethodId(scd,  f.getObjectId()):"")+"&"};
		s.append("{\nconstructor(props, context){\nsuper(props, context);\nprops.parentCt.form=this;this.url='").append(postFormStr[f.getObjectType()])
			.append("';this.params=").append(GenericUtil.fromMapToJsonString(formResult.getRequestParams()))
			.append(";\nif(props.setCmp)props.setCmp(this);this.egrids={};this.state=(!props.values && iwb.forms['").append(formResult.getUniqueId()).append("']) ||{errors:{},values:props.values||{");
		
		boolean b = false;
		for (W5FormCellHelper fc : formResult.getFormCellResults())if (fc.getFormCell().getActiveFlag() != 0 && fc.getFormCell().getControlType()>0 && fc.getFormCell().getControlType()<100) {
			if (b)s.append(","); else b = true;
			s.append(fc.getFormCell().getDsc()).append(":'");
			String value = fc.getHiddenValue(); if(value == null) value =  fc.getValue();
			if(!GenericUtil.isEmpty(value))switch(fc.getFormCell().getControlType()){
			case	2://date && 
				s.append(GenericUtil.uDateStr(value));
				break;
			case	18://timestamp
				if (!"0".equals(value) && value.length() <= 10)
					value = GenericUtil.uDateStr(value) + " 00:00:00";
				s.append("0".equals(value) ? GenericUtil.uFormatDateTime(new Date()) : value);			
				break;
			case	5://checkbox
				s.setLength(s.length()-1);
				s.append(GenericUtil.uInt(value)!=0);
				continue;
			default:
				s.append(GenericUtil.stringToJS(value));
				
			}
			s.append("'");
		}
		
		//if(!GenericUtil.isEmpty(formResult.getForm().get_conversionList()))s.append(",_cnvStr:!0");
		//if(!GenericUtil.isEmpty(formResult.getForm().get_formSmsMailList()))s.append(",_smsStr:!0");
		
		s.append("},\noptions:{");
		b = false;
		for (W5FormCellHelper fc : formResult.getFormCellResults())if (!GenericUtil.isEmpty(fc.getValue()) && fc.getFormCell().getActiveFlag() != 0 && fc.getFormCell().getControlType()==10
				&& fc.getLookupQueryResult() != null && !GenericUtil.isEmpty(fc.getLookupQueryResult().getData())) {
			if (b)s.append(","); else b = true;
			Object[] oo = fc.getLookupQueryResult().getData().get(0);
			s.append(fc.getFormCell().getDsc()).append(":[{id:'").append(oo[1]).append("',dsc:'").append(GenericUtil.stringToJS(oo[0].toString())).append("'}]");
		}

		s.append("},activeTab:false}");
		//\nif(this.componentWillPost)this.componentWillPost=this.componentWillPost.bind(this);
		Map<String, List<W5FormCell>> pcr = new HashMap();
		for (W5FormCellHelper fc : formResult.getFormCellResults())if (fc.getFormCell().getActiveFlag() != 0 && (fc.getFormCell().getControlType()==9 ||fc.getFormCell().getControlType()==16) && fc.getFormCell().getParentFormCellId()!=0 && !GenericUtil.isEmpty(fc.getFormCell().getLookupIncludedParams())) {//combo remote
			for (W5FormCellHelper rfc : formResult.getFormCellResults()) {
				if (rfc.getFormCell().getFormCellId() == fc.getFormCell().getParentFormCellId()) {
					W5FormCell pfc = rfc.getFormCell();
					if (pfc.getControlType() == 6 || pfc.getControlType() == 7 || pfc.getControlType() == 9 || pfc.getControlType() == 10 || pfc.getControlType() == 51) {
						List<W5FormCell> lfc = pcr.get(pfc.getDsc());
						if(lfc==null){
							lfc= new ArrayList();
							pcr.put(pfc.getDsc(), lfc);
						}
						lfc.add(fc.getFormCell());
					}
					break;
				}
			}
			
			
		}
		s.append("\nthis._id='").append(formResult.getUniqueId()).append("';this.triggerz4ComboRemotes={");
		b = false;
		for(String k:pcr.keySet()){
			if(b)s.append(",");else b=true;
			s.append(k).append(":[");
			List<W5FormCell> lfc = pcr.get(k);
			for(W5FormCell fc:lfc){
				s.append("{n:'").append(fc.getDsc()).append("', f:(ax,bx,cx)=>{\n").append(fc.getLookupIncludedParams()).append("\n}},");
			}
			s.setLength(s.length()-1);
			s.append("]");
		} 
		s.append("}");
		
	/*	s.append("\nthis.onChange=this.onChange.bind(this);this.onComboChange=this.onComboChange.bind(this);this.onLovComboChange=this.onLovComboChange.bind(this);this.onNumberChange=this.onNumberChange.bind(this);this.submit=this.submit.bind(this);this.toggleTab=this.toggleTab.bind(this);this.getValues=this.getValues.bind(this);}");
		s.append("\ntoggleTab(tab){if(this.state.activeTab!==tab)this.setState({activeTab:tab});}");
		s.append("\ngetValues(){return Object.assign({}, this.state.values);}");//this.cfg={...}
		s.append("\nsubmit(cfg){var p = Object.assign({}, this.state.values);if(this.componentWillPost){var r = this.componentWillPost(p, cfg||{});if(r===false)return false;p = Object.assign(p, r);}iwb.request({url:this.url+'?'+iwb.JSON2URI(this.params)+'_renderer=react16&.r='+Math.random(), params:p, self:this, errorCallback:(json)=>{var errors={};if(json.errorType)switch(json.errorType){case	'validation':toastr.error('Validation Errors');if(json.errors && json.errors.length)json.errors.map(function(o){errors[o.id]=o.msg;});if(json.error)iwb.showModal({title:'ERROR',footer:false, color:'danger', size:'sm', body:json.error});break;default:alert(json.errorType);} else alert(json);this.setState({errors:errors});return false;}, successCallback:(json, xcfg)=>{if(cfg.callback)cfg.callback(json,xcfg);}});}");//this.cfg={...}
		s.append("\nonChange(e){var values=this.state.values;if(e.target){var o=e.target;values[o.name]=o.type=='checkbox'?o.checked:o.value;this.setState({values:values});}}");//this.cfg={...}
		s.append("\nonComboChange(dsc){var self=this;return function(o){var values=self.state.values;var v=o && o.id;values[dsc]=v;var q=self.triggerz4ComboRemotes;if(q[dsc])q[dsc].map(function(zzz){var nv=zzz.f(v,null,values);if(nv)iwb.request({url:'ajaxQueryData?'+iwb.JSON2URI(nv)+'.r='+Math.random(), successCallback:function(r){var options=self.state.options;options[zzz.n]=r.data;self.setState({options:options});}});else{var options=self.state.options;options[zzz.n]=[];self.setState({options:options});}});self.setState({values:values});}}");//this.cfg={...}
		s.append("\nonLovComboChange(dsc){var self=this;return function(o){var values=self.state.values;var v=[];if(o)o.map(function(q){v.push(q.id)});values[dsc]=v.join(',');self.setState({values:values});}}");//this.cfg={...}
		s.append("\nonDateChange(dsc, dttm){var self=this;return function(o){var values=self.state.values;var v=o && o._d;values[dsc]=dttm ? fmtDateTime(v):fmtShortDate(v);self.setState({values:values});}}");//this.cfg={...}
		s.append("\nonNumberChange(dsc){var self=this;return function(o){var values=self.state.values;var v=o && o.value;values[dsc]=v;self.setState({values:values});}}");//this.cfg={...}
		s.append("\ncomponentDidMount(){var self=this, q=this.triggerz4ComboRemotes,values=this.state.values;for(var dsc in q)if(values[dsc])q[dsc].map(function(zzz){var nv=zzz.f(values[dsc],null,values);if(nv)iwb.request({url:'ajaxQueryData?'+iwb.JSON2URI(nv)+'.r='+Math.random(), successCallback:function(r){var options=self.state.options;options[zzz.n]=r.data;self.setState({options:options});}});else{}});}");
		s.append("\ncomponentWillUnmount(){iwb.forms['").append(formResult.getUniqueId()).append("']=Object.assign({},this.state);");
*/
//		if (liveSyncRecord)formResult.getRequestParams().put(".t", formResult.getUniqueId());
		s.append("}");
		s.append("\n render(){\nvar mf=this, values=this.state.values, options=this.state.options, errors=this.state.errors, viewMode=this.props.viewMode;\n");

		
		for (W5FormCellHelper fc : formResult.getFormCellResults())
			if (fc.getFormCell().getActiveFlag() != 0) {
				if (fc.getFormCell().getControlType() != 102) {// label'dan farkli ise. label direk render edilirken koyuluyor
					s.append("var _").append(fc.getFormCell().getDsc()).append("=").append(serializeFormCell(customizationId, xlocale,fc, formResult)).append(";\n");
				} else {
					fc.setValue(LocaleMsgCache.get2(customizationId, xlocale,
							fc.getFormCell().getLocaleMsgKey()));
				}
			}

		s.append("\nvar __action__=").append(formResult.getAction()).append("\n");

		// 24 nolu form form edit form olduğu için onu çevirmesin.
		String postCode = (formResult.getForm().get_renderTemplate() != null
				&& formResult.getForm().get_renderTemplate().getLocaleMsgFlag() == 1 && formResult
				.getFormId() != 24) ? GenericUtil.filterExt(
				formResult.getForm().getJsCode(), scd,
				formResult.getRequestParams(), null).toString() : formResult
				.getForm().getJsCode();

		b = true;
		if (postCode != null && postCode.length() > 10) {
			if (postCode.charAt(0) == '!') {
				postCode = postCode.substring(1);
			} else
				b = false;
		} else
			postCode = "";
		if (!GenericUtil.isEmpty(postCode) && postCode.indexOf("Ext.")==-1) {
			s.append("try{").append(postCode).append("\n}catch(e){");
			s.append(FrameworkSetting.debug ? "if(confirm('ERROR form.JS!!! Throw? : ' + e.message))throw e;"
					: "alert('System/Customization ERROR : ' + e.message)");
			s.append("}\n");
		}

		if(formResult.getForm().getObjectType()==1 | formResult.getForm().getObjectType() == 8 ){ //search ise
			s.append(renderSearchFormModuleList(customizationId, xlocale,
					formResult.getUniqueId(),
					formResult.getFormCellResults(),
					"mf=_(Form, {id:'"+formResult.getUniqueId()+"'},")).append(");\n");
		} else switch (formResult.getForm().getRenderType()) {
		case 1:// fieldset
		case	4://wizard
			s.append(renderFormFieldset(formResult));
			break;
		case 2:// tabpanel
			s.append(renderFormTabpanel(formResult));
			break;
		case 3:// tabpanel+border
			s.append(renderFormTabpanel(formResult));
//				s.append(renderFormTabpanelBorder(formResult));
			break;
		case 0:// temiz
			s.append(renderFormModuleListTop(customizationId, xlocale,
					formResult.getUniqueId(),
					formResult.getFormCellResults(),
					"mf=", formResult.getRequestParams().get("_modal")!=null ? -1:formResult.getForm().getDefaultWidth())).append(";\n");
		}


		s.append("\nreturn mf}}");

		return s;
	}

	private StringBuilder renderFormTabpanel(W5FormResult formResult) {
		String xlocale = (String) formResult.getScd().get("locale");
		int customizationId = (Integer) formResult.getScd().get(
				"customizationId");
		Map<Integer, List<W5FormCellHelper>> map = new HashMap<Integer, List<W5FormCellHelper>>();
		map.put(0, new ArrayList<W5FormCellHelper>());
		if (formResult.getForm().get_moduleList() != null)
			for (W5FormModule m : formResult.getForm().get_moduleList()) {
				map.put(m.getFormModuleId(), new ArrayList<W5FormCellHelper>());
			}
		for (W5FormCellHelper m : formResult.getFormCellResults())
			if (m.getFormCell().getActiveFlag() != 0) {
				List<W5FormCellHelper> l = map.get(m.getFormCell().getFormModuleId());
				if (l == null)
					l = map.get(0);
				l.add(m);
			}
		StringBuilder buf = new StringBuilder();
		buf.append("mf=_('span',null");

		int defaultWidth = -1;
		if(formResult.getRequestParams().get("_modal")==null)defaultWidth = formResult.getForm().getDefaultWidth();
		List<String> extendedForms = new ArrayList();
		if (map.get(0).size() > 0) {
			buf.append(renderFormModuleListTop(customizationId, xlocale,
					formResult.getUniqueId(), map.get(0), ",", defaultWidth));
		}
		StringBuilder contentBuf = new StringBuilder();
		int firstTab = 0;
		if (formResult.getForm().get_moduleList() != null){
			for (W5FormModule m : formResult.getForm().get_moduleList())
				if (m.getFormModuleId() != 0) {
					if ((m.getModuleViewType() == 0 || formResult.getAction() == m.getModuleViewType()) 
							) {
						switch (m.getModuleType()) {
						case	4:break;//form 
						case	5://grid
							W5GridResult gridResult = formResult.getModuleGridMap().get(m.getObjectId());
							W5Table mainTable = gridResult.getGrid() != null
									&& gridResult.getGrid().get_defaultCrudForm() != null ? FrameworkCache
									.getTable(formResult.getScd(), gridResult.getGrid()
											.get_defaultCrudForm().getObjectId())
									: null;
							if (mainTable != null
									&& ( !GenericUtil
											.accessControl(formResult.getScd(), mainTable.getAccessViewTip(), mainTable.getAccessViewRoles(), mainTable.getAccessViewUsers())))
								gridResult = null;// hicbirsey
							else {
								if(firstTab==0){
									firstTab = m.getFormModuleId();
									buf.append(",_(Row,null,_(Col,{ className: 'mb-3 mt-4' }, _(Nav,{tabs: true}");
									contentBuf.append(", _(TabContent,{ className:'form-tabcontent', activeTab: this.state.activeTab||'").append(firstTab).append("' }");
								}
								buf.append(",_(NavItem, null,_(NavLinkS,{className: classNames({active: ").append(firstTab==m.getFormModuleId()?"!this.state.activeTab || ":"").append("this.state.activeTab === '").append(m.getFormModuleId()).append("'}), onClick:()=> this.toggleTab('").append(m.getFormModuleId()).append("')},'").append(LocaleMsgCache.get2(formResult.getScd(), m.getLocaleMsgKey())).append("'))");
								contentBuf.append(",_(TabPane, {tabId: '").append(m.getFormModuleId()).append("' },_(XEditGrid,").append(gridResult.getGrid().getDsc()).append("))");
								
							}
							break;
						

						default:
							if (!map.get(m.getFormModuleId()).isEmpty()) {
								if(firstTab==0){
									firstTab = m.getFormModuleId();
									buf.append(",_(Row,null,_(Col,{ className: 'mb-3 mt-4' }, _(Nav,{tabs: true}");
									contentBuf.append(", _(TabContent,{ className:'form-tabcontent', activeTab: this.state.activeTab||'").append(firstTab).append("' }");
								}
								buf.append(",_(NavItem, null,_(NavLinkS,{className: classNames({active: ").append(firstTab==m.getFormModuleId()?"!this.state.activeTab || ":"").append("this.state.activeTab === '").append(m.getFormModuleId()).append("'}), onClick:()=> this.toggleTab('").append(m.getFormModuleId()).append("')},'").append(LocaleMsgCache.get2(formResult.getScd(), m.getLocaleMsgKey())).append("'))");
								contentBuf.append(renderFormModuleListTop(customizationId, xlocale, formResult.getUniqueId(),
										map.get(m.getFormModuleId()), ",_(TabPane, {tabId: '"+m.getFormModuleId()+"' }, ", defaultWidth)).append(")");
							}
						}
					}
				}
		}
		if(firstTab>0){
			buf.append(")").append(contentBuf).append(")))");
		}
		buf.append(");");

		return buf;
	}


	private StringBuilder recursiveTemplateObject(List l, int parentObjectId, int level) {
		if(level>5 && l==null || l.size()<2)return null;
		StringBuilder buf = new StringBuilder();
		for(Object o:l)if(o instanceof W5GridResult){
			W5GridResult gr = (W5GridResult)o;
			if(gr.getTplObj().getPageObjectId()!=parentObjectId && gr.getTplObj().getParentObjectId()==parentObjectId){
				if(buf.length()==0){
					if(level>1)buf.append("region:'west',");
					buf.append("detailGrids:[");
				}
				buf.append("{grid:").append(gr.getGrid().getDsc());

				if(gr.getGrid().get_crudTable()!=null){
					W5Table t = gr.getGrid().get_crudTable();
					buf.append(",pk:{").append(t.get_tableParamList().get(0).getDsc()).append(":'").append(t.get_tableParamList().get(0).getExpressionDsc()).append("'}");
				}
				if(!GenericUtil.isEmpty(gr.getTplObj().get_srcQueryFieldName()) && !GenericUtil.isEmpty(gr.getTplObj().get_dstQueryParamName())){
					buf.append(",params:{").append(gr.getTplObj().get_dstQueryParamName()).append(":'").append(gr.getTplObj().get_srcQueryFieldName()).append("'");
					if(!GenericUtil.isEmpty(gr.getTplObj().getDstStaticQueryParamVal()) && !GenericUtil.isEmpty(gr.getTplObj().get_dstStaticQueryParamName())){
						buf.append(",").append(gr.getTplObj().get_dstStaticQueryParamName()).append(":'!").append(gr.getTplObj().getDstStaticQueryParamVal()).append("'");
					}
					buf.append("}");
				}
				if(!GenericUtil.isEmpty(gr.getTplObj().getPostJsCode())) {
					String s = gr.getTplObj().getPostJsCode();
					if(s.charAt(0)!=',')buf.append(",");
					buf.append(gr.getTplObj().getPostJsCode());
				}
				StringBuilder rbuf = recursiveTemplateObject(l, gr.getTplObj().getPageObjectId(), level+1);
				if(rbuf!=null && rbuf.length()>0)
					buf.append(",").append(rbuf);
				buf.append("},");
			}
		} else if(o instanceof W5CardResult){
			W5CardResult gr = (W5CardResult)o;
			if(gr.getTplObj().getPageObjectId()!=parentObjectId && gr.getTplObj().getParentObjectId()==parentObjectId){
				if(buf.length()==0){
					buf.append("detailGrids:[");
				}
				buf.append("{card:").append(gr.getCard().getDsc());
				if(gr.getCard().get_crudTable()!=null){
					W5Table t = gr.getCard().get_crudTable();
					buf.append(",pk:{").append(t.get_tableParamList().get(0).getDsc()).append(":'").append(t.get_tableParamList().get(0).getExpressionDsc()).append("'}");
				}
				if(!GenericUtil.isEmpty(gr.getTplObj().get_srcQueryFieldName()) && !GenericUtil.isEmpty(gr.getTplObj().get_dstQueryParamName())){
					buf.append(",params:{").append(gr.getTplObj().get_dstQueryParamName()).append(":'").append(gr.getTplObj().get_srcQueryFieldName()).append("'");
					if(!GenericUtil.isEmpty(gr.getTplObj().getDstStaticQueryParamVal()) && !GenericUtil.isEmpty(gr.getTplObj().get_dstStaticQueryParamName())){
						buf.append(",").append(gr.getTplObj().get_dstStaticQueryParamName()).append(":'!").append(gr.getTplObj().getDstStaticQueryParamVal()).append("'");
					}
					buf.append("}");
				}
				StringBuilder rbuf = recursiveTemplateObject(l, gr.getTplObj().getPageObjectId(), level+1);
				if(rbuf!=null && rbuf.length()>0)buf.append(",").append(rbuf);
				buf.append("},");
			}
		}
		if(buf.length()>0){
			buf.setLength(buf.length()-1);
			buf.append("]");
		}
		return buf;
	}

	private StringBuilder renderTemplateObject(W5PageResult pr) {
		StringBuilder buf = new StringBuilder();
		if(pr.getPageObjectList().get(0) instanceof W5CardResult){
			W5CardResult gr = (W5CardResult)pr.getPageObjectList().get(0);
			buf.append("return iwb.ui.buildPanel({t:_page_tab_id, card:").append(gr.getCard().getDsc());
			if(gr.getCard().get_crudTable()!=null){
				W5Table t = gr.getCard().get_crudTable();
				buf.append(",pk:{").append(t.get_tableParamList().get(0).getDsc()).append(":'").append(t.get_tableParamList().get(0).getExpressionDsc()).append("'}");
			}
			buf.append("});");
			return buf;
		}
		if(!(pr.getPageObjectList().get(0) instanceof W5GridResult))return buf;
		W5GridResult gr = (W5GridResult)pr.getPageObjectList().get(0);
		buf.append("return iwb.ui.buildPanel({t:_page_tab_id, grid:").append(gr.getGrid().getDsc());
		if(gr.getGrid().get_crudTable()!=null){
			W5Table t = gr.getGrid().get_crudTable();
			buf.append(",pk:{").append(t.get_tableParamList().get(0).getDsc()).append(":'").append(t.get_tableParamList().get(0).getExpressionDsc()).append("'}");
		}
		if(pr.getPageObjectList().size()>1){
			StringBuilder rbuf = recursiveTemplateObject(pr.getPageObjectList(), ((W5GridResult)pr.getPageObjectList().get(0)).getTplObj().getPageObjectId(), 1);
			if(rbuf!=null && rbuf.length()>0)
				buf.append(",").append(rbuf);
			
			/*
			buf.append(",detailGrids:[");
			for(int qi=1;qi<templateResult.getTemplateObjectList().size();qi++){
				if(qi>1)buf.append(",\n");
				
				W5GridResult gr=((W5GridResult)templateResult.getTemplateObjectList().get(qi));
				buf.append("{grid:").append(gr.getGrid().getDsc()).append(", params:{").append(gr.getTplObj().get_dstQueryParamName()).append(":'").append(gr.getTplObj().get_srcQueryFieldName()).append("'");
				if(gr.getTplObj().get_dstStaticQueryParamName()!=null)
					buf.append(", ").append(gr.getTplObj().get_dstStaticQueryParamName()).append(":'!").append(gr.getTplObj().getDstStaticQueryParamVal()).append("'");
				buf.append("}}");
				
			}
			buf.append("]");
			*/
			
		}
		buf.append("});");
		return buf;
	}
	private StringBuilder renderFormTabpanelBorder(W5FormResult formResult) {
		String xlocale = (String) formResult.getScd().get("locale");
		int customizationId = (Integer) formResult.getScd().get(
				"customizationId");
		Map<Integer, List<W5FormCellHelper>> map = new HashMap<Integer, List<W5FormCellHelper>>();
		map.put(0, new ArrayList<W5FormCellHelper>());
		if (formResult.getForm().get_moduleList() != null)
			for (W5FormModule m : formResult.getForm().get_moduleList()) {
				map.put(m.getFormModuleId(), new ArrayList<W5FormCellHelper>());
			}
		else {
			formResult.getForm().set_moduleList(new ArrayList());

		}
		for (W5FormCellHelper m : formResult.getFormCellResults())
			if (m.getFormCell().getActiveFlag() != 0) {
				List<W5FormCellHelper> l = map.get(m.getFormCell()
						.getFormModuleId());
				if (l == null)
					l = map.get(0);
				l.add(m);
			}
		List<String> extendedForms = new ArrayList();
		String formBodyStyle = FrameworkCache.getAppSettingStringValue(
				formResult.getScd(), "form_body_style");
		StringBuilder buf = new StringBuilder();
		buf.append("mf=iwb.apply(mf,{xtype:'form', layout:'border',border:false, items:[");
		if (map.get(0).size() > 0) {
			buf.append(
					renderFormModuleList(
							customizationId,
							xlocale,
							formResult.getUniqueId(),
							map.get(0),
							"{xtype:'panel',region:'north',border:false,bodyStyle:'overflowY:auto',split:true,height:"
									+ formResult.getForm().getDefaultHeight()
									+ ",items:[{xtype:'fieldset'"
									+ (GenericUtil.isEmpty(formBodyStyle) ? ""
											: ",bodyStyle:'" + formBodyStyle
													+ "'"), formResult.getForm().getDefaultWidth(), formResult.getForm().getLabelWidth())).append("]}");

			// (formBodyColor!=null ?
			// ",bodyStyle:'background-color:#"+formBodyColor+";background-image:url(../images/custom/bubble.png);background-repeat:no-repeat'"
			// : "")));
		}

		boolean b = false;
		buf.append(",{xtype:'tabpanel',region:'center',activeTab: 0, deferredRender:false,defaults:{bodyStyle:'padding:0px'}, items:[");// defaults:{autoHeight:true,
																																		// bodyStyle:'padding:10px'},
		for (W5FormModule m : formResult.getForm().get_moduleList())
			if (m.getFormModuleId() != 0) {
				if ((m.getModuleViewType() == 0 || formResult.getAction() == m
						.getModuleViewType())) {
					switch (m.getModuleType()) {
					case 4:// form
						if (GenericUtil.uInt(formResult.getRequestParams().get(
								"a")) == 5)
							break;
						W5FormResult subFormResult = formResult
								.getModuleFormMap() == null ? null : formResult
								.getModuleFormMap().get(m.getObjectId());
						W5Table mainTablex = subFormResult != null
								&& subFormResult.getForm() != null ? FrameworkCache
								.getTable(formResult.getScd(), subFormResult
										.getForm().getObjectId()) : null;
						if (mainTablex == null)
							continue;
						if (mainTablex != null
								&& ( !GenericUtil
										.accessControl(
												formResult.getScd(),
												mainTablex.getAccessViewTip(),
												mainTablex.getAccessViewRoles(),
												mainTablex.getAccessViewUsers())))
							subFormResult = null;// hicbirsey
						else {
							if (b)
								buf.append(",");
							else
								b = true;
							buf.append("iwb.apply(")
									.append(subFormResult.getForm().getDsc())
									.append(",{xtype:null,layout:'form',title:'")
									.append(LocaleMsgCache.get2(
											customizationId, xlocale,
											m.getLocaleMsgKey()))
									.append("',height:")
									.append(subFormResult.getForm()
											.getDefaultHeight())
									.append(",autoScroll:true})");
							extendedForms.add(subFormResult.getForm().getDsc());
						}
						break;
					case 5:// grid(edit)
						if (formResult.getModuleGridMap() == null)
							break;
						if (GenericUtil.uInt(formResult.getRequestParams().get(
								"a")) == 5)
							break;
						W5GridResult gridResult = formResult.getModuleGridMap()
								.get(m.getObjectId());
						W5Table mainTable = gridResult.getGrid() != null
								&& gridResult.getGrid().get_defaultCrudForm() != null ? FrameworkCache
								.getTable(formResult.getScd(), gridResult.getGrid()
										.get_defaultCrudForm().getObjectId())
								: null;
						if (mainTable != null
								&& (!GenericUtil
										.accessControl(formResult.getScd(),
												mainTable.getAccessViewTip(),
												mainTable.getAccessViewRoles(),
												mainTable.getAccessViewUsers())))
							gridResult = null;// hicbirsey
						else {
							if (b)
								buf.append(",");
							else
								b = true;
							buf.append(gridResult.getGrid().getDsc())
									.append("._gp=new ")
									.append(formResult.isViewMode() ? (gridResult
											.getGrid().getTreeMasterFieldId() == 0 ? "Ext.grid.GridPanel"
											: "Ext.ux.maximgb.tg.GridPanel")
											: (gridResult.getGrid()
													.getTreeMasterFieldId() == 0 ? "Ext.grid.EditorGridPanel"
													: "Ext.ux.maximgb.tg.EditorGridPanel"))
									.append("(iwb.apply(")
									.append(gridResult.getGrid().getDsc())
									.append(",{title:'")
									.append(LocaleMsgCache.get2(
											customizationId, xlocale,
											m.getLocaleMsgKey()))
									.append("',height:")
									.append(gridResult.getGrid()
											.getDefaultHeight())
									.append(",autoScroll:true,clicksToEdit: 1*_app.edit_grid_clicks_to_edit}))");
						}
						break;
					default:
						if (!map.get(m.getFormModuleId()).isEmpty()) {
							if (b)
								buf.append(",");
							else
								b = true;
							String extra = "{layout:'form',title:'"
									+ LocaleMsgCache.get2(customizationId,
											xlocale, m.getLocaleMsgKey()) + "'";
							// if(formBodyColor!=null)extra+=",bodyStyle:'background-color: #"+formBodyColor+"'";
							if (formBodyStyle != null)
								extra += ",bodyStyle:'" + formBodyStyle + "'";

							W5FormCellHelper extraInfo = getModulExtraInfo(
									(String) formResult.getScd().get("locale"),
									m.getLocaleMsgKey());
							if (extraInfo != null)
								map.get(m.getFormModuleId()).add(0, extraInfo);
							buf.append(renderFormModuleList(customizationId,
									xlocale, formResult.getUniqueId(),
									map.get(m.getFormModuleId()), extra, formResult.getForm().getDefaultWidth(), formResult.getForm().getLabelWidth()));
						}

					}
				}
			}
		buf.append("]");
		// if (tabHeight>0) buf.append(",height:").append(tabHeight); TODO:
		// defaults:{autoHeight:true, kısmını kaldırdığımızda gridin boyutunu
		// alıyor ve scroll çıkıyor ancak veri çok ise sıkıntı olabilir.
		buf.append("}]}");
		buf.append(");");

		if (!extendedForms.isEmpty()) {
			buf.append("\nmf._extendedForms=[");
			b = false;
			for (String s : extendedForms) {
				if (b)
					buf.append(",");
				else
					b = true;
				buf.append(s);
			}
			buf.append("];");
		}
		return buf;
		/* new Ext.grid.GridPanel(iwb.apply(detailGrid,grdExtra)) */
	}

	private StringBuilder renderFormFieldset(W5FormResult formResult) {
		String xlocale = (String) formResult.getScd().get("locale");
		int customizationId = (Integer) formResult.getScd().get(
				"customizationId");
		Map<Integer, List<W5FormCellHelper>> map = new HashMap<Integer, List<W5FormCellHelper>>();
		map.put(0, new ArrayList<W5FormCellHelper>());
		if (formResult.getForm().get_moduleList() != null)
			for (W5FormModule m : formResult.getForm().get_moduleList()) {
				map.put(m.getFormModuleId(), new ArrayList<W5FormCellHelper>());
			}
		for (W5FormCellHelper m : formResult.getFormCellResults())
			if (m.getFormCell().getActiveFlag() != 0) {
				List<W5FormCellHelper> l = map.get(m.getFormCell()
						.getFormModuleId());
				if (l == null)
					l = map.get(0);
				l.add(m);
			}
		StringBuilder buf = new StringBuilder();
		buf.append("mf=_('span',null");

		int defaultWidth = -1;
		if(formResult.getRequestParams().get("_modal")==null)defaultWidth = formResult.getForm().getDefaultWidth();
		List<String> extendedForms = new ArrayList();
		if (map.get(0).size() > 0) {
			buf.append(renderFormModuleListTop(customizationId, xlocale,
					formResult.getUniqueId(), map.get(0), ",", defaultWidth));
		}
		if (formResult.getForm().get_moduleList() != null)
			for (W5FormModule m : formResult.getForm().get_moduleList())
				if (m.getFormModuleId() != 0) {
					if ((m.getModuleViewType() == 0 || formResult.getAction() == m
							.getModuleViewType()) ) {
						switch (m.getModuleType()) {
						case	4: break;
						case 5://grid
						W5GridResult gridResult = formResult.getModuleGridMap().get(m.getObjectId());
						W5Table mainTable = gridResult.getGrid() != null
								&& gridResult.getGrid().get_defaultCrudForm() != null ? FrameworkCache
								.getTable(formResult.getScd(), gridResult.getGrid()
										.get_defaultCrudForm().getObjectId())
								: null;
						if (mainTable != null
								&& (!GenericUtil
										.accessControl(formResult.getScd(), mainTable.getAccessViewTip(), mainTable.getAccessViewRoles(), mainTable.getAccessViewUsers())))
							gridResult = null;// hicbirsey
						else {
							buf.append(",_('div',{className:'hr-text', style:{marginTop:'20px'}},_('h6',null,'").append(LocaleMsgCache.get2(customizationId, xlocale, m.getLocaleMsgKey())).append("')),_(XEditGrid,").append(gridResult.getGrid().getDsc()).append(")");
							
						}
						break;

						default:
							if (!map.get(m.getFormModuleId()).isEmpty()) {
								buf.append(renderFormModuleListTop(
										customizationId, xlocale,
										formResult.getUniqueId(),
										map.get(m.getFormModuleId()), ",_('div',{className:'hr-text', style:{marginTop:'20px'}},_('h6',null,'"+LocaleMsgCache.get2(customizationId, xlocale, m.getLocaleMsgKey())+"')),", defaultWidth));
							}
						}
					}
				}
		buf.append(");");

		return buf;
	}
	
	
	private StringBuilder renderFormCellWithLabelTop(W5FormCellHelper fc){
		StringBuilder buf = new StringBuilder();
		String dsc = fc.getFormCell().getDsc();
		if(fc.getFormCell().getControlType() == 5){//checkbox
			buf.append(",\n_").append(dsc).append(" && _(FormGroup, {style:{marginBottom:'0.3rem', display: _").append(dsc).append(".hidden?'none':''}}, _(Label, {style:{marginRight:'1rem'}, className:'inputLabel', htmlFor:\"")
			.append(dsc).append("\"},_").append(dsc).append(".label), _(Label,{ className: 'switch switch-3d switch-'+(viewMode?'secondary':'primary') }, _(_").append(dsc)
			.append(".$||Input,viewMode?Object.assign({disabled:true},_").append(dsc).append("):_").append(dsc).append("),_('span', { className: 'switch-label' }),_('span', { className: 'switch-handle' })))");
		} else {
			if (fc.getFormCell().getControlType() == 102) {// displayField4info
				buf.append("\n,_('div', {style:{padding:'0.45rem .85rem', borderRadius:30}, className:'alert alert-").append(labelMap[fc.getFormCell().getLookupQueryId()]).append("'}, _('i',{className:'icon-info'}),' ','").append(GenericUtil.stringToJS(fc.getValue())).append("')");
			} else if (fc.getFormCell().getControlType() == 100) {// button
				buf.append("\n, _").append(dsc).append(" && !_").append(dsc).append(".hidden && _(FormGroup, null, _(Button,_").append(dsc).append("))");
			} else {
				buf.append("\n, _").append(dsc).append(" && _(FormGroup, _").append(dsc).append(".hidden?{style:{display:'none'}}:(errors.").append(dsc).append(" && {className:'validation-error'}), _(Label, {className:'inputLabel', htmlFor:\"").append(dsc).append("\"},_").append(dsc).append(".label");
				buf.append(", \" \", _").append(dsc).append(".hint && _(\"span\",{className:\"xlabel-hint\", title:_").append(dsc).append(".hint},_(\"i\",{className:\"icon-question\"}))");
				if(FrameworkSetting.reactLabelRequired && /*fc.getFormCell().getNotNullFlag()!=0 && */fc.getFormCell().getNrdType()==0)buf.append(", \" \", !_").append(dsc).append(".readOnly && !viewMode && _").append(dsc).append(".required && _(\"span\",{className:\"xlabel-required\"},getLocMsg(\"required\"))");
				buf.append("), viewMode ? iwb.getFieldRawValue(_").append(dsc).append(",this.state.options.").append(dsc).append(") :_(_").append(dsc).append(".$||Input,_").append(dsc).append("),errors.").append(dsc).append(" && _('small',null,errors.").append(dsc).append("))");
			}
		}
		return buf;
	}
	private StringBuilder renderFormModuleListTop(int customizationId,
			String xlocale, String formUniqueId,
			List<W5FormCellHelper> formCells, String xtype, int defaultWidth/*-1:modal*/) {
		StringBuilder buf = new StringBuilder();
		// if(xtype!=null)buf.append("{frame:true,xtype:'").append(xtype).append("'");
		if(xtype!=null)buf.append(xtype);
		int lc = 0;
		int[] maxWidths = new int[10], minWidths = new int[10];
		for (W5FormCellHelper fc : formCells)
			if (fc.getFormCell().getActiveFlag() != 0){
				lc = Math.max(lc, fc.getFormCell().getTabOrder() / 1000);
				if(lc<3){
					maxWidths[lc] = Math.max(maxWidths[lc], fc.getFormCell().getControlWidth());
					minWidths[lc] = Math.min(minWidths[lc], fc.getFormCell().getControlWidth());
				}
			}
		if(lc>2)lc=2;

		int totalControlWidth = 0;
		for(int qi=0;qi<=lc;qi++){
			if(minWidths[lc]<0) maxWidths[qi] =Math.max(-(350*minWidths[lc])/100,maxWidths[qi]);
			maxWidths[qi]+=25;
			totalControlWidth+=maxWidths[qi]; //padding yuzunden 25 pixel de yeniyo
		}
		if(defaultWidth>0){
			if(totalControlWidth>defaultWidth) defaultWidth = totalControlWidth;
			defaultWidth = 6 * defaultWidth / 5; //extjs -> bootstrap rate
		} //else totalControlWidth = 6 * totalControlWidth / 5;
		boolean modal = defaultWidth==-1;
		
		if (lc == 0) {// hersey duz
			int xl = modal?12:Math.min(12, (12*defaultWidth)/1140);// extraLarge >=1200px
			int lg = modal?12:Math.min(12, (12*defaultWidth)/960);// Large >=992px
			int md = modal?12:Math.min(12, (12*defaultWidth)/720);// Medium >=768px
			int sm = modal?12:Math.min(12, (12*defaultWidth)/540);// Small >=576px
			buf.append("_(Row, null, _(Col,{xs:'12',xl:'").append(xl).append("',lg:'").append(lg).append("',md:'").append(md).append("',sm:'").append(sm).append("'}");
			for (int i = 0; i < formCells.size(); i++) {
				W5FormCellHelper fc = formCells.get(i);
				if (fc.getFormCell().getActiveFlag() == 0 || fc.getFormCell().getControlType()==0)
					continue;
//				String dsc = fc.getFormCell().getDsc();
				
				if (i < formCells.size() - 1 && formCells.get(i + 1).getFormCell().getControlType() != 0 && formCells.get(i + 1).getFormCell().getActiveFlag() != 0
						&& formCells.get(i + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder()) { // yanyana koymak icin. 
					buf.append(", _(Row,null");
					
					int columnWidthTotal = fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300;
					for(int ji=i;ji < formCells.size() - 1 && formCells.get(ji + 1).getFormCell().getControlType() != 0 && formCells.get(ji + 1).getFormCell().getActiveFlag() != 0 && formCells.get(ji + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder();ji++){
						columnWidthTotal += formCells.get(ji+1).getFormCell().getControlWidth()>0 ? formCells.get(ji+1).getFormCell().getControlWidth():300;
					}
					
					int totalColXs = 0;
					int xs = 12*(fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300)/columnWidthTotal;
					if(xs==0)xs=1;
					totalColXs+=xs;
					buf.append(",_(Col,{xs:12, md:").append(xs).append("}").append(renderFormCellWithLabelTop(fc)).append(")");
					while (i < formCells.size() - 1 && formCells.get(i + 1).getFormCell().getControlType() != 0 && formCells.get(i + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder()) {
						i++;
						fc = formCells.get(i);
						xs = 12*(fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300)/columnWidthTotal;
						if(xs==0)xs=1;
						totalColXs+=xs;
						buf.append(",_(Col,{xs:12, md:").append(xs).append("}").append(renderFormCellWithLabelTop(fc)).append(")");
					}
					buf.append(")");
				} else {
					buf.append(renderFormCellWithLabelTop(fc));
				}
			}
			buf.append("))");
		} else {
			for(int qi=0;qi<=lc;qi++){
				maxWidths[qi]=6*maxWidths[qi]/5;
			}
			int xl = Math.min(12, (12*maxWidths[0])/(modal?totalControlWidth:1140));// extraLarge >=1200px
			int lg = modal ? xl :Math.min(12, (12*maxWidths[0])/960);// Large >=992px
			int md = modal ? xl :Math.min(12, (12*maxWidths[0])/720);// Medium >=768px
			int sm = modal ? xl: Math.min(12, (12*maxWidths[0])/540);// Small >=576px

			buf.append("_(Row, null, _(Col,{xs:'12',xl:'").append(xl).append("',lg:'").append(lg).append("',md:'").append(md).append("',sm:'").append(sm).append("'}");
			StringBuilder columnBuf = new StringBuilder();
			int order=0;
			for (int i = 0; i < formCells.size(); i++) {
				W5FormCellHelper fc = formCells.get(i);
				if (fc.getFormCell().getActiveFlag() == 0 || fc.getFormCell().getControlType()==0)
					continue;
				if (fc.getFormCell().getTabOrder() / 1000 != order) {
					order = fc.getFormCell().getTabOrder() / 1000;
					int nxl = Math.min(12, (12*maxWidths[order])/(modal?totalControlWidth:1140));// extraLarge >=1200px
					int nlg = modal ? xl :Math.min(12, (12*maxWidths[order])/960);// Large >=992px
					int nmd = modal ? xl :Math.min(12, (12*maxWidths[order])/720);// Medium >=768px
					int nsm = modal ? xl :Math.min(12, (12*maxWidths[order])/320);// Small >=576px
					if(modal){
						xl+=nxl;
						if(xl==13){
							nxl--;
							nlg = nmd = nsm = nxl;
						}
					}
					
					if (columnBuf.length() > 0) {
						buf.append(columnBuf.append("), _(Col,{xs:12,xl:'").append(nxl).append("',lg:'").append(nlg).append("',md:'").append(nmd).append("',sm:'").append(nsm).append("'}"));
						columnBuf.setLength(0);
					}
				}
				if (i < formCells.size() - 1 && formCells.get(i + 1).getFormCell().getControlType() != 0 && formCells.get(i + 1).getFormCell().getActiveFlag() != 0
						&& formCells.get(i + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder()) { // yanyana koymak icin. 
					columnBuf.append(", _(Row,null");
					
					int columnWidthTotal = fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300;
					for(int ji=i;ji < formCells.size() - 1 && formCells.get(ji + 1).getFormCell().getControlType() != 0 && formCells.get(ji + 1).getFormCell().getActiveFlag() != 0 && formCells.get(ji + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder();ji++){
						columnWidthTotal += formCells.get(ji+1).getFormCell().getControlWidth()>0 ? formCells.get(ji+1).getFormCell().getControlWidth():300;
					}
					
					int totalColXs = 0;
					int xs = 12*(fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300)/columnWidthTotal;
					if(xs==0)xs=1;
					totalColXs+=xs;
					columnBuf.append(",_(Col,{xs:12,md:").append(xs).append("}").append(renderFormCellWithLabelTop(fc)).append(")"); //").append(fc.getFormCell().getControlWidth()>200 ? 12:xs).append("
					while (i < formCells.size() - 1 && formCells.get(i + 1).getFormCell().getControlType() != 0 && formCells.get(i + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder()) {
						i++;
						fc = formCells.get(i);
						xs = 12*(fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300)/columnWidthTotal;
						if(xs==0)xs=1;
						totalColXs+=xs;
						columnBuf.append(",_(Col,{xs:12,md:").append(xs).append("}").append(renderFormCellWithLabelTop(fc)).append(")");
					}
					columnBuf.append(")");
				} else {
					columnBuf.append(renderFormCellWithLabelTop(fc));

				}
			}
			buf.append(columnBuf.append("))"));
		}
//		buf.append("}");
		return buf;
	}
	
	private StringBuilder renderFormModuleList(int customizationId,
			String xlocale, String formUniqueId,
			List<W5FormCellHelper> formCells, String xtype, int defaultWidth, int labelWidth) {
		StringBuilder buf = new StringBuilder();
		// if(xtype!=null)buf.append("{frame:true,xtype:'").append(xtype).append("'");
		if(xtype!=null)buf.append(xtype);
		int lc = 0;
		int[] maxWidths = new int[10], minWidths = new int[10];
		for (W5FormCellHelper fc : formCells)
			if (fc.getFormCell().getActiveFlag() != 0){
				lc = Math.max(lc, fc.getFormCell().getTabOrder() / 1000);
				if(lc<3){
					maxWidths[lc] = Math.max(maxWidths[lc], fc.getFormCell().getControlWidth());
					minWidths[lc] = Math.min(minWidths[lc], fc.getFormCell().getControlWidth());
				}
			}
		if(lc>2)lc=2;

		int totalControlWidth = 0;
		for(int qi=0;qi<=lc;qi++){
			if(minWidths[lc]<0) maxWidths[qi] =Math.max(-(350*minWidths[lc])/100,maxWidths[qi]);
			maxWidths[qi]+=25;
			totalControlWidth+=maxWidths[qi]+labelWidth; //padding yuzunden 25 pixel de yeniyo
		}
		if(totalControlWidth>defaultWidth) defaultWidth = totalControlWidth;
		defaultWidth = 6 * defaultWidth / 5; //extjs -> bootstrap rate
		labelWidth = 6 * labelWidth / 5;
		if (lc == 0) {// hersey duz
			int xl = Math.min(12, (12*defaultWidth+600)/1140);// extraLarge >=1200px
			int lg = Math.min(12, (12*defaultWidth+480)/960);// Large >=992px
			int md = Math.min(12, (12*defaultWidth+360)/720);// Medium >=768px
			int sm = Math.min(12, (12*defaultWidth+270)/540);// Small >=576px
			
			buf.append("_(Row, {style:{maxWidth:'").append(defaultWidth).append("px'}}, _(Col,{xs:'12',xl:'").append(xl).append("',lg:'").append(lg).append("',md:'").append(md).append("',sm:'").append(sm).append("'}");// ,\nautoHeight:false

			int lxl = Math.max(1, Math.min(12, (12*labelWidth+300)/Math.min(defaultWidth,1140)));// extraLarge >=1200px
			int llg = Math.max(1, Math.min(12, (12*labelWidth+240)/Math.min(defaultWidth,960)));// Large >=992px
			int lmd = Math.min(12, (12*labelWidth+180)/Math.min(defaultWidth,720));// Medium >=768px
			int lsm = Math.min(12, (12*labelWidth+130)/Math.min(defaultWidth,540));// Small >=576px
			StringBuilder labelBuf = new StringBuilder(), inputBuf = new StringBuilder();
			labelBuf.append("xs:'12',xl:'").append(lxl).append("',lg:'").append(llg).append("',md:'").append(lmd).append("',sm:'").append(lsm).append("'");
			inputBuf.append("xs:'12',xl:'").append(12-lxl).append("',lg:'").append(12-llg).append("',md:'").append(12-lmd).append("',sm:'").append(12-lsm).append("'");

			for (int i = 0; i < formCells.size(); i++) {
				W5FormCellHelper fc = formCells.get(i);
				if (fc.getFormCell().getActiveFlag() == 0 || fc.getFormCell().getControlType()==0)
					continue;
				if(fc.getFormCell().getControlType() == 5){
					buf.append(", _(FormGroup, {row:true}, _(Label, {").append(labelBuf).append(",htmlFor:\"")
					.append(fc.getFormCell().getDsc()).append("\"},_").append(fc.getFormCell().getDsc()).append(".label), _(Label,{className: 'switch switch-3d switch-primary' }, _").append(fc.getFormCell().getDsc())
					.append(",_('span', { className: 'switch-label' }),_('span', { className: 'switch-handle' })))");
				} else {
					buf.append(", _(FormGroup, {row:true}, _(Label, {").append(labelBuf).append(",");//
					if (fc.getFormCell().getControlType() == 102) {// displayField4info
						buf.append("md:null}, \"").append(fc.getValue()).append("\"))");
					} else {
						buf.append("htmlFor:\"").append(fc.getFormCell().getDsc()).append("\"},_").append(fc.getFormCell().getDsc()).append(".label), _(Col,{").append(inputBuf).append("},_")
						.append(fc.getFormCell().getDsc()).append("))");
					}
				}
			}
			buf.append("))");
		} else {
			for(int qi=0;qi<=lc;qi++){
				maxWidths[qi]=6*maxWidths[qi]/5;
			}
			int xl = Math.min(12, (12*(maxWidths[0]+labelWidth))/1140);// extraLarge >=1200px
			int lg = Math.min(12, (12*(maxWidths[0]+labelWidth))/960);// Large >=992px
			int md = Math.min(12, (12*(maxWidths[0]+labelWidth))/720);// Medium >=768px
			int sm = Math.min(12, (12*(maxWidths[0]+labelWidth))/540);// Small >=576px
			
			buf.append("_(Row, {style:{maxWidth:'").append(defaultWidth).append("px'}}, _(Col,{xs:'12',xl:'").append(xl).append("',lg:'").append(lg).append("',md:'").append(md).append("',sm:'").append(sm).append("'}");// ,\nautoHeight:false

			int lxl = Math.max(1, Math.min(12, (12*labelWidth+300)/Math.min(maxWidths[0]+labelWidth,1140)));// extraLarge >=1200px
			int llg = Math.max(1, Math.min(12, (12*labelWidth+240)/Math.min(maxWidths[0]+labelWidth,960)));// Large >=992px
			int lmd = Math.min(12, (12*labelWidth+180)/Math.min(maxWidths[0]+labelWidth,720));// Medium >=768px
			int lsm = Math.min(12, (12*labelWidth+130)/Math.min(maxWidths[0]+labelWidth,540));// Small >=576px
			StringBuilder labelBuf = new StringBuilder(), inputBuf = new StringBuilder();
			labelBuf.append("xs:'12',xl:'").append(lxl).append("',lg:'").append(llg).append("',md:'").append(lmd).append("',sm:'").append(lsm).append("'");
			inputBuf.append("xs:'12',xl:'").append(12-lxl).append("',lg:'").append(12-llg).append("',md:'").append(12-lmd).append("',sm:'").append(12-lsm).append("'");

			StringBuilder columnBuf = new StringBuilder();
			int order=-1;
			for (int i = 0; i < formCells.size(); i++) {
				W5FormCellHelper fc = formCells.get(i);
				if (fc.getFormCell().getActiveFlag() == 0 || fc.getFormCell().getControlType()==0)
					continue;
				if (fc.getFormCell().getTabOrder() / 1000 != order) {
					order = fc.getFormCell().getTabOrder() / 1000;
					xl = Math.min(12, (12*(maxWidths[order]+labelWidth))/1140);// extraLarge >=1200px
					lg = Math.min(12, (12*(maxWidths[order]+labelWidth))/960);// Large >=992px
					md = Math.min(12, (12*(maxWidths[order]+labelWidth))/720);// Medium >=768px
					sm = Math.min(12, (12*(maxWidths[order]+labelWidth))/320);// Small >=576px


					if (columnBuf.length() > 0) {
						buf.append(columnBuf.append("), _(Col,{xs:'12',xl:'").append(xl).append("',lg:'").append(lg).append("',md:'").append(md).append("',sm:'").append(sm).append("'}"));
						columnBuf.setLength(0);
					}
				}
				if(fc.getFormCell().getControlType() == 5){
					columnBuf.append(", _(FormGroup, {row:true}, _(Label, {").append(labelBuf).append(",htmlFor:\"")
					.append(fc.getFormCell().getDsc()).append("\"},_").append(fc.getFormCell().getDsc()).append(".label), _(Label,{ className: 'switch switch-3d switch-primary' }, _").append(fc.getFormCell().getDsc())
					.append(",_('span', { className: 'switch-label' }),_('span', { className: 'switch-handle' })))");
				} else {
					columnBuf.append(", _(FormGroup, {row:true}, _(Label, {").append(fc.getFormCell().getControlType() == 102 ? "xxmd:null":labelBuf).append(",");//
					if (fc.getFormCell().getControlType() == 102) {// displayField4info
						columnBuf.append("}, \"").append(fc.getValue()).append("\"))");
					} else {
						columnBuf.append("htmlFor:\"").append(fc.getFormCell().getDsc()).append("\"},_").append(fc.getFormCell().getDsc()).append(".label), _(Col,{").append(inputBuf).append("},_")
						.append(fc.getFormCell().getDsc()).append("))");
					}
				}
			}
			buf.append(columnBuf.append("))"));
		}
//		buf.append("}");
		return buf;
	}
	
	private StringBuilder renderSearchFormModuleList(int customizationId,
			String xlocale, String formUniqueId,
			List<W5FormCellHelper> formCells, String xtype) {
		StringBuilder buf = new StringBuilder();
		// if(xtype!=null)buf.append("{frame:true,xtype:'").append(xtype).append("'");
		if(xtype!=null)buf.append(xtype);
		buf.append("_('div',null");// ,normalde Col olmasi lazim
		for (int i = 0; i < formCells.size(); i++) {
			W5FormCellHelper fc = formCells.get(i);
			if (fc.getFormCell().getActiveFlag() == 0 || fc.getFormCell().getControlType()==0)
				continue;
			String dsc= fc.getFormCell().getDsc();
			if(fc.getFormCell().getControlType() == 5){
				buf.append(",\n_").append(dsc).append(" && _(FormGroup, {style:{marginBottom:'0.3rem', display: _").append(dsc).append(".hidden?'none':''}}, _(Label,{ className: 'switch switch-3d switch-primary' }, _(_").append(dsc)
				.append(".$||Input,_").append(dsc).append("),_('span', { className: 'switch-label' }),_('span', { className: 'switch-handle' })), _(Label, {style:{marginLeft:'1rem'},htmlFor:\"")
				.append(dsc).append("\"},_").append(dsc).append(".label))");
			} else {
				if (fc.getFormCell().getControlType() == 102) {// displayField4info
					buf.append("\n,_('div', {style:{padding:'0.45rem .85rem', borderRadius:30}, className:'alert alert-").append(labelMap[fc.getFormCell().getLookupQueryId()]).append("'}, _('i',{className:'icon-info'}),' ','").append(GenericUtil.stringToJS(fc.getValue())).append("')");
				} else if (fc.getFormCell().getControlType() == 100) {// button
					buf.append("\n, _").append(dsc).append(" && !_").append(dsc).append(".hidden && _(FormGroup, null, _(Button,_").append(dsc).append("))");
				} else {
					buf.append("\n, _").append(dsc).append(" && _(FormGroup, _").append(dsc).append(".hidden?{style:{display:'none'}}:null, _(Label, {className:'inputLabel', htmlFor:\"").append(dsc).append("\"},_").append(dsc).append(".label), _(_").append(dsc).append(".$||Input,_").append(dsc).append("))");
				}
			}
		}
		buf.append(")");
		return buf;
	}
	
	private W5FormCellHelper getModulExtraInfo(String locale, String key) {
		W5FormCellHelper fce = null;
		key += "_info";
		String moduleExtraInfo = LocaleMsgCache.get2(0, locale, key); // TODO.
																		// aslinda
																		// cusId
																		// olacak
		if (moduleExtraInfo != null && !moduleExtraInfo.equals(key)) {
			W5FormCell fc = new W5FormCell();
			fc.setControlType((short) 102);// displayField4info
			fc.setTabOrder((short) -1);
			fce = new W5FormCellHelper(fc);
			fce.setValue(moduleExtraInfo);
		}
		return fce;
	}

	@SuppressWarnings("unchecked")
	private StringBuilder serializeFormCellProperty(W5FormCellHelper cellResult, W5FormResult formResult) {
		StringBuilder buf = new StringBuilder();
		if(!GenericUtil.isEmpty(cellResult.getFormCell().get_formCellPropertyList())) for(W5FormCellProperty fcp:cellResult.getFormCell().get_formCellPropertyList()){
			if(fcp.getLkpPropertyType()==3) {
				buf.append(",hint:'").append(GenericUtil.stringToJS(fcp.getVal())).append("'");				
			} else  for(W5FormCellHelper fcr:formResult.getFormCellResults())if(fcr.getFormCell().getFormCellId()==fcp.getRelatedFormCellId()) {
				if(fcr.getFormCell().getActiveFlag()!=0 && fcp.getLkpPropertyType()!=3) {
					buf.append(",").append(new String[] {"required:","hidden:!","readOnly:"}[fcp.getLkpPropertyType()]);
						buf.append("iwb.formElementProperty(").append(fcp.getLkpOperatorType()).append(", values.");
						buf.append(fcr.getFormCell().getDsc());
						if(fcp.getLkpOperatorType()>=0 && fcr.getFormCell().getControlType()!=5)buf.append(",'").append(GenericUtil.stringToJS(fcp.getVal())).append("'");
						buf.append(")");
					
					break;
				}
			}			
		}
		if(cellResult.getFormCell().getControlType()==2 && cellResult.getFormCell().getParentFormCellId()!=0 && cellResult.getFormCell().getParentFormCellId()!=cellResult.getFormCell().getFormCellId()) {
			for(W5FormCellHelper fcr:formResult.getFormCellResults())if(fcr.getFormCell().getFormCellId()==cellResult.getFormCell().getParentFormCellId()) {
				buf.append(",isValidDate:(current)=> {return values.").append(fcr.getFormCell().getDsc()).append(" ? current.isSameOrAfter(moment(values.").append(fcr.getFormCell().getDsc()).append(",iwb.dateFormat)):true}");
			}
		}
		
		return buf;
	}

	
	@SuppressWarnings("unchecked")
	private StringBuilder serializeFormCell(int customizationId,
			String xlocale, W5FormCellHelper cellResult, W5FormResult formResult) {
		W5FormCell fc = cellResult.getFormCell();
		String value = cellResult.getValue(); // bu ilerde hashmap ten gelebilir
		// int customizationId =
		// PromisUtil.uInt(formResult.getScd().get("customizationId"));
		StringBuilder buf = new StringBuilder();
		if (fc.getControlType() == 0)return buf.append("'").append(GenericUtil.stringToJS(value)).append("'");
		buf.append("{");
		
		if (fc.getControlType() == 102)
			return buf.append("$:'div', className:'alert alert-").append(labelMap[fc.getLookupQueryId()]).append("',children:[_('i',{className:'icon-info'}),' ','").append(GenericUtil.stringToJS(value)).append("']}");
		else if ((fc.getControlType() == 101 || cellResult.getHiddenValue() != null)/* && (fc.getControlTip()!=9 && fc.getControlTip()!=16) */) {
			buf.append("type:'text', readOnly:true, hiddenValue:'").append(GenericUtil.stringToJS(cellResult.getHiddenValue())).append("',label:'").append(LocaleMsgCache.get2(customizationId, xlocale, fc.getLocaleMsgKey())).append("',disabled:true, value:'").append(GenericUtil.stringToJS(value)).append("'");
			if(fc.get_sourceObjectDetail()==null && !GenericUtil.isEmpty(fc.getExtraDefinition())){
				if(!fc.getExtraDefinition().startsWith(",")) {
					buf.append(",");
				}
				buf.append(fc.getExtraDefinition());
				
			}
			buf.append("}");
			return buf;
			
		}

		switch(fc.getControlType()){
		case	1:buf.append("type:");
			if(GenericUtil.safeEquals(fc.getVtype(), "email"))buf.append("'email'");
			else if(GenericUtil.safeEquals(fc.getVtype(), "url"))buf.append("'url'");
			else buf.append("'text'");
			break;//string
		case	42:buf.append("type:'password'");break;
		case	2:buf.append("$:Datetime, dateFormat:'").append(FrameworkCache.getAppSettingStringValue(customizationId, "date_format", dateFormatMulti[formResult!=null && formResult.getScd()!=null ? GenericUtil.uInt(formResult.getScd().get("date_format")):0])).append("',timeFormat:false, closeOnSelect:true");break; //TODO:date
		case	18:buf.append("$:Datetime, dateFormat:'").append(FrameworkCache.getAppSettingStringValue(customizationId, "date_format", dateFormatMulti[formResult!=null && formResult.getScd()!=null ? GenericUtil.uInt(formResult.getScd().get("date_format")):0])).append("',timeFormat:'HH:mm'");break; //TODO:datetime
		case	22:buf.append("$:Datetime, dateFormat:false, className:'rdt-time', timeFormat:'hh:mm a'");break; //TODO:time
		case	3://double
		case	4://integer
			buf.append("$:NumberFormat,style:{textAlign:'right'},className:'form-control");
			if(fc.getNotNullFlag()!=0)buf.append("',required:true");else buf.append("'");
			if(fc.getControlType()==3)
				buf.append(",thousandSeparator:',',decimalSeparator:'.',decimalScale:").append(fc.getLookupQueryId()==0?2:fc.getLookupQueryId());
			else
				buf.append(",decimalScale:0");
			break;//int
		case	5:buf.append("type:'checkbox', className:'switch-input'");break;
		case	100:buf.append("$:Button,color:'primary',onClick:(ax){").append(fc.getDefaultValue()).append("},children:[");
				if(fc.getLocaleMsgKey().startsWith("icon-"))buf.append("_('i',{className:'").append(fc.getLocaleMsgKey()).append("'})]");
				else buf.append("'").append(LocaleMsgCache.get2(customizationId, xlocale, fc.getLocaleMsgKey())).append("']");
				if(fc.getControlWidth()>0)buf.append(",width:").append(fc.getControlWidth());
				if(!GenericUtil.isEmpty(fc.getExtraDefinition())){
					buf.append(fc.getExtraDefinition());//button
				}
				return buf.append(serializeFormCellProperty(cellResult, formResult)).append("}");

		case	6://combo static
		case	8:// lovcombo-static
		case	58:// superbox lovcombo-static

		case	7://combo query
		case	15://lovcombo query
		case	59://superbox lovcombo query
			buf.append("$:").append(formResult!=null && fc.getParentFormCellId()==1?"CheckboxGroup":"Select")
				.append(", placeholder: getLocMsg('select_placeholder'), valueKey:'id',labelKey:'dsc',options:[");//static combo
			if ((fc.getControlType()==6 || fc.getControlType()==8 ||fc.getControlType()==58) && cellResult.getLookupListValues() != null) {
				boolean b1=false;
				
				for (W5Detay p : (List<W5Detay>) cellResult
						.getLookupListValues()) {
					if (b1)
						buf.append(",");
					else
						b1 = true;
					buf.append("{id:'").append(p.getVal()).append("',dsc:'")
							.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache
									.get2(customizationId, xlocale, p.getDsc())
									: p.getDsc()).append("'");
					buf.append("}");
				}
			} else if ((fc.getControlType()==7 || fc.getControlType()==15 ||fc.getControlType()==59)){
				if(cellResult.getLookupQueryResult()!=null && cellResult.getLookupQueryResult().getData() != null) {
					boolean b1 = false;
					for (Object[] p : cellResult.getLookupQueryResult().getData()) {
						if (b1)
							buf.append(",");
						else
							b1 = true;
						boolean bb = false;
						buf.append("{");
						for (W5QueryField f : cellResult.getLookupQueryResult().getQuery().get_queryFields()) {
							Object z = p[f.getTabOrder() - 1];
							if (bb)
								buf.append(",");
							else
								bb = true;
							if (z == null)z = "";
							buf.append(f.getDsc()).append(":'")
									.append(f.getPostProcessType() == 2 ? LocaleMsgCache
											.get2(customizationId, xlocale,
													z.toString()) : GenericUtil
											.stringToJS(z.toString()))
									.append("'");
						}
						buf.append("}");
					}
				}
				if(fc.getControlType()==15 && cellResult.getLookupQueryResult().getQueryId()==606 && formResult!=null && formResult.getForm()!=null && formResult.getForm().getObjectType()==1) {//workflow extra records
					buf.append(",{dsc:'approved', id:998},{dsc:'rejected', id:999}");
				}
			}
			buf.append("], clearable:").append(fc.getNotNullFlag()==0);
			if(fc.getControlType()==8 ||fc.getControlType()==58 || fc.getControlType()==15 ||fc.getControlType()==59)buf.append(",multi:true,closeOnSelect:false");
		break; 
		
		case	9://combo query remote
		case	16://lovcombo query remote
			buf.append("$:Select, placeholder: getLocMsg('select_placeholder'), options:options.").append(fc.getDsc()).append("||[],valueKey:'id', labelKey:'dsc',clearable:").append(fc.getNotNullFlag()==0);
			if(fc.getControlType()==16)buf.append(",multi:true");
			break;
		case	10://advanced select: TODO ilk geldiginde oo loadOptions'ta atanacak

			int maxRows = FrameworkCache.getAppSettingIntValue(0,
					"advanced_select_max_rows");
			if (maxRows == 0)
				maxRows = 100;
			buf.append("$:Select.Async, cacheOptions:!0,/*isLoading:!0, */defaultOptions:options.").append(fc.getDsc()).append("||[],valueKey:'id', labelKey:'dsc', placeholder:'").append(LocaleMsgCache.get2(0, xlocale, "autocomplete_placeholder"))
				.append("', loadOptions:(input, callback)=>{var xself=this;if(!input)callback();else iwb.request({url:'ajaxQueryData?_renderer=react16&_qid=").append(fc.getLookupQueryId()).append("&limit=").append(maxRows);
			if(!GenericUtil.isEmpty(fc.getLookupIncludedParams()))buf.append("&").append(fc.getLookupIncludedParams());
			buf.append("', params:{xdsc:input}, successCallback:function(result, cfg){var options=xself.state.options||{};options.").append(fc.getDsc()).append("=result.data;xself.setState({options});callback(null, {options: result.data,complete: false});}});},clearable:").append(fc.getNotNullFlag()==0);
		break; // advanced select
		case	23://treecombo(local)
		case	26://lovtreecombo(local) TODO
			buf.append("type:'text'");
		break; // 		

		case	12://html editor
			buf.append("$:XHTMLEditor, ");
		case	25://textarea(ozel tanimlama)
		case	41://codemirror
		case	11:
			buf.append("type:'textarea'");
			break; // textarea
//		{ view:"label", label:'Fill the form below to access <br>the main datacore.'
		case	72://signature
			buf.append("$:Signature, width:").append(fc.getControlWidth());
			break;
		case	71://file attachment
			buf.append("$:FileInput, parentCt: this, onFileChange: this.onFileChange(), cfg:cfgForm");
			Map evm = cellResult.getExtraValuesMap();
			if(!GenericUtil.isEmpty(evm)) {
				buf.append(", fileId:").append(evm.get("id")).append(", fileSize:").append(evm.get("fsize")).append(", fileName:\"").append(GenericUtil.stringToJS2((String)evm.get("dsc"))).append("\"");
			}
			if(fc.getLookupQueryId()>0 && fc.getLookupQueryId()<=fileAcceptMap.length) {
				buf.append(", accept:\"").append(fileAcceptMap[fc.getLookupQueryId()-1]).append("\"");
			} else if(fc.getLookupQueryId()==-1 && !GenericUtil.isEmpty(fc.getLookupIncludedValues()))
					buf.append(", accept:\"").append(GenericUtil.stringToJS2(fc.getLookupIncludedValues())).append("\"");
			break;
		
		default:			
			buf.append("type:'text'");
			break;
		
		
		}
		buf.append(",name:'").append(fc.getDsc()).append("'");//,id:'").append(fc.getDsc()).append("'");
		
		if(fc.getControlType()!=3 && fc.getControlType()!=4 && fc.getControlType()!=5 && fc.getControlType()!=22 && fc.getNotNullFlag()!=0)buf.append(",required:true");
		buf.append(", label:'").append(LocaleMsgCache.get2(customizationId, xlocale, fc.getLocaleMsgKey())).append("'");

		if(formResult!=null){ //FORM
			switch(fc.getControlType()){
			case	8:	case	58: case	15:case	59://fc.getControlTip()==8 ||fc.getControlTip()==58 || fc.getControlTip()==15 ||fc.getControlTip()==59
				buf.append(",value:values.").append(fc.getDsc()).append(" ? values.").append(fc.getDsc()).append(".split(','):[]");
				break;
			case	5:
					buf.append(",checked:values.").append(fc.getDsc()).append("||false");
					break; 
			case	10:
				buf.append(", value: values.").append(fc.getDsc()).append(" ? iwb.findAsyncValue(values.").append(fc.getDsc()).append(",options.").append(fc.getDsc()).append("):''");
				break;
			default:buf.append(",value:values.").append(fc.getDsc()).append("||''");
			}
		//	if(true)buf.append(",on:{onChange:function(newv, oldv){this.validate();}}");
			
			switch(fc.getControlType()){
			case	6:	case	7:
			case	9:	
				buf.append(",onChange:this.onComboChange('").append(fc.getDsc()).append("')");
				break;
			case 10:
				buf.append(",onChange:this.onComboChange('").append(fc.getDsc()).append("')");
				break;
			case	8:	case	58: case	15:case 16:case	59://fc.getControlTip()==8 ||fc.getControlTip()==58 || fc.getControlTip()==15 ||fc.getControlTip()==59
				buf.append(",onChange:this.").append(fc.getParentFormCellId()==1?"onCheckboxGroupChange":"onLovComboChange").append("('").append(fc.getDsc()).append("')");
				break;
			case	3:	case	4:
				buf.append(",onValueChange:this.onNumberChange('").append(fc.getDsc()).append("')");
				break;
			case	2:	case	18://case	22:
				buf.append(",onChange:this.onDateChange('").append(fc.getDsc()).append("',").append(fc.getControlType()==18).append(")");
				break;
			case	12:	//html editor:
				buf.append(",onHtmlChange:this.onHtmlChange('").append(fc.getDsc()).append("')");
				break;
			case 22:
				buf.append(",onChange:this.onTimeChange('").append(fc.getDsc()).append("')");
				break;
			default:
				buf.append(",onChange:this.onChange");
			}
		} else { //grid/toolbar/list/gantt
			buf.append(",_control:").append(fc.getControlType());
			switch(fc.getControlType()){
				case	5:
					buf.append(",defaultChecked:").append(GenericUtil.uInt(value)>0);
					break; 
				default:buf.append(",defaultValue:'").append(GenericUtil.stringToJS(value)).append("'");
			}
		}
		buf.append(serializeFormCellProperty(cellResult, formResult));
		if(!GenericUtil.isEmpty(fc.getExtraDefinition()))buf.append(fc.getExtraDefinition());

		buf.append("}");

		return buf;
	}

	private StringBuilder serializeToolbarItems(Map scd,
			List<W5ObjectToolbarItem> items, boolean mediumButtonSize, Map lookupMap) {
		if (items == null || items.size() == 0)
			return null;
		String xlocale = (String) scd.get("locale");
		int customizationId = (Integer) scd.get("customizationId");
		StringBuilder buttons = new StringBuilder();
		boolean b = false;
		int itemCount = 0;
		for (W5ObjectToolbarItem toolbarItem : items)if (GenericUtil.accessControl(scd, toolbarItem.getAccessViewTip(),
				toolbarItem.getAccessViewRoles(),
				toolbarItem.getAccessViewUsers())) {
			if (b)
				buttons.append(",");
			else
				b = true;
			if (toolbarItem.getControlType() == 0
					|| toolbarItem.getControlType() == 100) { // yok(0): button
															// +
															// tooltip;button(100)
															// icon+text
			//	if (toolbarItem.getDsc().equals("-"))buttons.append("{ $template:'Spacer' }"); else 
			//	if (toolbarItem.getDsc().equals("->"))buttons.append("{}"); else 
				if (toolbarItem.getObjectType() == 15) {// form toolbar
					buttons.append("{type:'icon' , value:'")
							.append(LocaleMsgCache.get2(scd, toolbarItem.getLocaleMsgKey()))
							.append("',");
					if (mediumButtonSize)
						buttons.append("iconAlign: 'top', scale:'medium', style:{margin: '0px 5px 0px 5px'},");
					buttons.append("iconCls:'")
							.append(toolbarItem.getImgIcon())
							.append("', click:(a,b,c)=>{\n")
							.append(LocaleMsgCache.filter2(
									customizationId, xlocale,
									toolbarItem.getCode())).append("\n}}");
					itemCount++;
				} else {
					buttons.append("{type:'button'");
					if(!GenericUtil.isEmpty(toolbarItem.getImgIcon()))buttons.append(", icon:'icon-").append(toolbarItem.getImgIcon()).append("'");
					buttons.append(", text:'").append(LocaleMsgCache.get2(scd, toolbarItem.getLocaleMsgKey()))
							.append("', click:(a,b,c)=>{\n")
							.append(LocaleMsgCache.filter2(
									customizationId, xlocale,
									toolbarItem.getCode())).append("\n}}");
					itemCount++;
				}
			} else { // controlTip
				W5FormCell cell = new W5FormCell();
				cell.setControlType(toolbarItem.getControlType());
				cell.setLookupQueryId(toolbarItem.getLookupQueryId());
				cell.setLocaleMsgKey(toolbarItem.getLocaleMsgKey());
				cell.setDsc(toolbarItem.getDsc());
				cell.setNotNullFlag((short) 1);
				cell.setExtraDefinition(",tooltip:'"
						+ LocaleMsgCache.get2(customizationId, xlocale,
								toolbarItem.getLocaleMsgKey())
						+ "',ref:'../" + toolbarItem.getDsc() + "'");
				if (toolbarItem.getCode() != null
						&& toolbarItem.getCode().length() > 2)
					cell.setExtraDefinition(cell.getExtraDefinition() + ","
							+ toolbarItem.getCode());
				W5FormCellHelper cellResult = new W5FormCellHelper(cell);
				if (toolbarItem.getControlType() == 6
						|| toolbarItem.getControlType() == 8
						|| toolbarItem.getControlType() == 14) {
					W5LookUp lu = FrameworkCache.getLookUp(scd,
							toolbarItem.getLookupQueryId());

					List<W5LookUpDetay> dl = new ArrayList<W5LookUpDetay>(
							lu.get_detayList().size());
					for (W5LookUpDetay dx : lu.get_detayList()) {
						W5LookUpDetay e = new W5LookUpDetay();
						e.setVal(dx.getVal());
						e.setDsc(LocaleMsgCache.get2(customizationId,
								xlocale, dx.getDsc()));
						dl.add(e);
					}
					cellResult.setLookupListValues(dl);
				} else if(lookupMap!=null && (toolbarItem.getControlType() == 7
						|| toolbarItem.getControlType() == 15)) {
					cellResult.setLookupQueryResult((W5QueryResult)lookupMap.get("_tlb_"+toolbarItem.getToolbarItemId()));
					
				}
				buttons.append(serializeFormCell(customizationId, xlocale,
						cellResult, null));
				itemCount++;
			}
		}
		return itemCount > 0 ? buttons : null;
	}

	private StringBuilder serializeMenuItems(Map scd,
			List<W5ObjectMenuItem> items) {
		if (items == null || items.size() == 0)
			return null;
		String xlocale = (String) scd.get("locale");
		int customizationId = (Integer) scd.get("customizationId");
		StringBuilder buttons = new StringBuilder();
		boolean b = false;
		int itemCount = 0;
		for (W5ObjectMenuItem menuItem : items)
			if (GenericUtil.accessControl(scd, menuItem.getAccessViewTip(),
					menuItem.getAccessViewRoles(),
					menuItem.getAccessViewUsers())) {
				if (b)
					buttons.append(",");
				else
					b = true;
				if (!menuItem.getDsc().equals("-")){

					buttons.append("{text:'")
							.append(LocaleMsgCache.get2(customizationId,
									xlocale, menuItem.getLocaleMsgKey()))
							.append("'");
					if (!GenericUtil.isEmpty(menuItem.getImgIcon()))
						buttons.append(",icon:'icon-").append(menuItem.getImgIcon())
								.append("'");
					buttons.append(",onClick:function(a,b,c){\n")
							.append(menuItem.getCode()).append("\n}}");
					itemCount++;
				}

			}
		return itemCount > 0 ? buttons : null;
	}
	
	public StringBuilder serializeGridRecordCreate(W5GridResult gridResult) {
		StringBuilder html = new StringBuilder();
		
		return html;
	}

	public StringBuilder serializeCard(W5CardResult dataViewResult) {
		Map<String, Object> scd = dataViewResult.getScd();
		String xlocale = (String) dataViewResult.getScd().get("locale");
		int customizationId = (Integer) dataViewResult.getScd().get(
				"customizationId");
		W5Card d = dataViewResult.getCard();
		StringBuilder buf = new StringBuilder();
		buf.append("\nvar ")
				.append(d.getDsc())
				.append("={cardId:")
				.append(d.getCardId())
				.append(",name:'")
				.append(LocaleMsgCache.get2(scd,
						d.getLocaleMsgKey()))
				.append("',setCmp:(o)=> {")
				.append(d.getDsc()).append(".cmp = o;}")
				.append(",_url:'ajaxQueryData?.w='+_webPageId+'&_qid=")
				.append(d.getQueryId()).append("&_dvid=")
				.append(d.getCardId());

		if (d.getDefaultPageRecordNumber() != 0)
			buf.append("&firstLimit=").append(d.getDefaultPageRecordNumber());
		buf.append("'");
		if (d.getDefaultWidth() != 0)
			buf.append(", defaultWidth:").append(d.getDefaultWidth());
		if (d.getDefaultHeight() != 0)
			buf.append(", defaultHeight:").append(d.getDefaultHeight());
		//azat card
		if (d.get_defaultCrudForm() != null) {
			W5Table t = FrameworkCache.getTable(scd, d.get_defaultCrudForm().getObjectId());
			if(true || FrameworkCache.getAppSettingIntValue(customizationId, "new_record_label_flag")!=0)
				buf.append(",newRecordLabel:'").append(LocaleMsgCache.get2(scd,"new_record_prefix"))
				.append(LocaleMsgCache.get2(scd,d.get_defaultCrudForm().getLocaleMsgKey()).toUpperCase()).append("'");
			
			boolean insertFlag = GenericUtil.accessControl(scd,t.getAccessInsertTip(), t.getAccessInsertRoles(),t.getAccessInsertUsers());
			buf.append(",\n crudFormId:")
				.append(d.getDefaultCrudFormId())
				.append(", crudTableId:")
				.append(t.getTableId())
				.append(", crudFlags:{insert:")
				.append(insertFlag)
				.append(",edit:")
				.append(t.getAccessUpdateUserFields() != null
						|| GenericUtil.accessControl(scd,
								t.getAccessUpdateTip(),
								t.getAccessUpdateRoles(),
								t.getAccessUpdateUsers()))
				.append(",remove:")
				.append(t.getAccessDeleteUserFields() != null
						|| GenericUtil.accessControl(scd,
								t.getAccessDeleteTip(),
								t.getAccessDeleteRoles(),
								t.getAccessDeleteUsers()))
				.append("}");
		}
		buf.append(",\n gridReport:").append(FrameworkCache.roleAccessControl(scd, 105));
	    if (!GenericUtil.isEmpty(d.get_orderQueryFieldNames())) {
	        buf.append(",\n orderNames:[");
	        for (String f : d.get_orderQueryFieldNames()) {
	          buf.append("{id:'")
	              .append(f)
	              .append("',dsc:'")
	              .append(LocaleMsgCache.get2(scd, f))
	              .append("'},");
	        }
	        buf.setLength(buf.length() - 1);
	        buf.append("]");
	      }
		if (d.get_crudTable()!= null) {
		}
		//azat card
		if (dataViewResult.getSearchFormResult() != null) {
			buf.append(",\n searchForm: class extends XForm").append(
					serializeGetForm(dataViewResult.getSearchFormResult()));
		}
		if (!GenericUtil.isEmpty(d.get_toolbarItemList())) { // extra buttonlari
															// var mi yok mu?
			StringBuilder buttons = serializeToolbarItems(
					dataViewResult.getScd(), d.get_toolbarItemList(), false, null);
			if (buttons != null && buttons.length() > 1) {
				buf.append(",\n extraButtons:[").append(buttons).append("]");
			}
		}
		
		if (!GenericUtil.isEmpty(d.get_menuItemList() )) { // menu (rightClick) buttonlari
			StringBuilder buttons = serializeMenuItems(dataViewResult.getScd(), d.get_menuItemList());
			if (buttons != null && buttons.length() > 1) {
				buf.append(",\n menuButtons:[").append(buttons).append("]");
			}
		}

		if (d.getDefaultPageRecordNumber() != 0)
			buf.append(",\n pageSize:").append(d.getDefaultPageRecordNumber());
		// buf.append(",\n tpl:'<tpl for=\".\">").append(PromisUtil.stringToJS(d.getTemplateCode())).append("</tpl>',\nautoScroll:true,overClass:'x-view-over',itemSelector:'table.grid_detay'};\n");
		buf.append(",\n render(props){\n")
				.append(d.getTemplateCode())
				.append("\n}");
		
		return buf.append("}");
	}

	public StringBuilder serializeListView(W5ListViewResult listViewResult) {
		StringBuilder buf = new StringBuilder();
		buf.append("\n//TODO serializeListView");

		return buf;
	}

	public StringBuilder serializeGrid(W5GridResult gridResult) {
		return serializeGrid(gridResult, null);
	}
	private StringBuilder serializeGrid(W5GridResult gridResult, String dsc) {
		Map<String, Object> scd = gridResult.getScd();
		String xlocale = (String) scd.get("locale");
		int customizationId = (Integer) scd.get("customizationId");
		W5Grid g = gridResult.getGrid();
		W5Query q = g.get_query();
		StringBuilder buf = new StringBuilder();
		boolean expander = false;
		if(dsc==null)dsc=g.getDsc();
		
		buf.append("var ").append(dsc).append(" = {gridId:")
				.append(g.getGridId()).append(",queryId:").append(g.getQueryId()).append(",setCmp:(o)=> {")
				.append(dsc).append(".cmp = o;}");
		if (g.getSelectionModeType()!=0){
			if(g.getSelectionModeType()==2 || g.getSelectionModeType()==3)
				buf.append(", multiselect:true");
		}
		buf.append(",keyField:'").append(g.get_pkQueryField().getDsc()).append("'");
		if(g.getTreeMasterFieldId() != 0) {
			W5QueryField treeMasterField = g.get_queryFieldMap().get(g.getTreeMasterFieldId());
			if(treeMasterField != null) {
				buf.append(",tree:!0, treeParentKey:'parent_id', tableTreeColumn:'").append(treeMasterField.getDsc()).append("'");
			}
		} else if(g.getGroupingFieldId()!=0) {
			W5QueryField groupingField = g.get_queryFieldMap().get(g.getGroupingFieldId());
			if(groupingField != null) {
				buf.append(",groupColumn:'").append(groupingField.getDsc()).append("'");
			} else buf.append(",_disableIntegratedGrouping:!0");
		} else buf.append(",_disableIntegratedGrouping:!0");
		
		if (gridResult.getExtraOutMap() != null
				&& !gridResult.getExtraOutMap().isEmpty()) {
			buf.append(",\n extraOutMap:")
					.append(GenericUtil.fromMapToJsonString(gridResult
							.getExtraOutMap()));
		}

			

		if (g.getDefaultWidth() != 0)
			buf.append(", defaultWidth:").append(g.getDefaultWidth());

		if (g.getSelectionModeType() == 2 || g.getSelectionModeType() == 3) // multi Select
			buf.append(",\n selectRow:{mode: 'checkbox',clickToSelect: true}");
		if (g.getDefaultHeight() > 0)
			buf.append(", defaultHeight:").append(g.getDefaultHeight());

		buf.append(",\n gridReport:").append(FrameworkCache.roleAccessControl(scd, 105));
		
		buf.append(",\n displayInfo:").append(g.getDefaultPageRecordNumber()>0);
		if(q.get_aggQueryFields()!=null) {
			buf.append(",\n displayAgg:[");
			for(W5QueryField f:q.get_aggQueryFields())
				buf.append("{id:'").append(f.getDsc()).append("', f:(x)=> _('span',{},'")
				.append(LocaleMsgCache.get2(scd, f.getDsc())).append(" ', _('span',{style:{borderRadius:100, background:'rgba(228, 242, 251, 0.8)', padding:'2px 5px'}},fmtDecimal(x,2,2)))},");
			buf.setLength(buf.length()-1);
			buf.append("]");
		}

		if (!GenericUtil.isEmpty(g.get_crudFormSmsMailList())) {
			buf.append(",\n formSmsMailList:[");
			boolean b = false;
			for (W5FormSmsMail fsm : g.get_crudFormSmsMailList())
				if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms) || (fsm
						.getSmsMailType() != 0 && FrameworkSetting.mail))
						&& fsm.getAlarmFlag() == 0
						&& GenericUtil.hasPartInside2(fsm.getWebMobileTips(),
								GenericUtil.uInt(scd.get("mobile")) != 0 ? "2"
										: "1")) {
					if (b)
						buf.append("\n,");
					else
						b = true;
					buf.append("{xid:")
							.append(fsm.getFormSmsMailId())
							.append(",text:\"")
//							.append(fsm.getSmsMailTip() == 0 ? "[SMS] " : "[" + (LocaleMsgCache.get2(customizationId, xlocale,"email_upper")) + "] ")
							.append(GenericUtil.stringToJS(LocaleMsgCache.get2(customizationId, xlocale, fsm.getDsc())))
							.append("\",smsMailTip:")
							.append(fsm.getSmsMailType()).append("}");
				}
			buf.append("]");
		}
		if (!GenericUtil.isEmpty(g.get_crudFormConversionList())) {
			buf.append(",\n formConversionList:[")
					.append(serializeManualConversions(scd,
							g.get_crudFormConversionList())).append("]");
		}
		
		String uniqueId = GenericUtil.getNextId("ng");
		if(false)buf.append(",striped:true,hover:true,bordered:false,");
		buf.append(",name:'").append(LocaleMsgCache.get2(scd,
						g.getLocaleMsgKey())).append("',\n id:'").append(uniqueId).append("'");

		
			buf.append(",\n _url:'ajaxQueryData?_renderer=react16&.t='+_page_tab_id+'&.w='+_webPageId+'&_qid=")
					.append(g.getQueryId()).append("&_gid=")
					.append(g.getGridId());

			if (g.getDefaultPageRecordNumber() != 0)
				buf.append("&firstLimit=").append(g
								.getDefaultPageRecordNumber())
						.append("',remote:{sort: true}"); //pagination: true, filter: true, sort: true, cellEdit: true
			else
				buf.append("'");

			
			

		if (g.getDefaultPageRecordNumber() != 0)
			buf.append(",\n pageSize:").append(g
							.getDefaultPageRecordNumber());

		if (gridResult.getSearchFormResult() != null) {
			gridResult.getSearchFormResult().setUniqueId("s-"+uniqueId);
			buf.append(",\n searchForm:class extends XForm").append(serializeGetForm(gridResult.getSearchFormResult()));

		}
		
		if(g.getDefaultPageRecordNumber()>0 && g.get_query()!=null && g.get_query().get_queryParams()!=null){
			for(W5QueryParam qp:g.get_query().get_queryParams())if(qp.getDsc().equals("xsearch"))
				buf.append(",\n globalSearch:true");
		}
		

		if (g.get_defaultCrudForm() != null) { // insert update delete buttons
			if(g.get_defaultCrudForm().getObjectType()==2) { //table
				W5Table t = FrameworkCache.getTable(scd, g.get_defaultCrudForm()
						.getObjectId());// g.get_defaultCrudForm().get_sourceTable();
				boolean insertFlag = GenericUtil.accessControl(scd,
						t.getAccessInsertTip(), t.getAccessInsertRoles(),
						t.getAccessInsertUsers());

				if(true || FrameworkCache.getAppSettingIntValue(customizationId, "new_record_label_flag")!=0)
					buf.append(",newRecordLabel:'").append(LocaleMsgCache.get2(scd,"new_record_prefix"))
					.append(LocaleMsgCache.get2(scd,g.get_defaultCrudForm().getLocaleMsgKey()).toUpperCase()).append("'");
				
				buf.append(",\n crudFormId:")
						.append(g.getDefaultCrudFormId())
						.append(", crudTableId:")
						.append(t.getTableId())
						.append(", crudFlags:{insert:")
						.append(insertFlag)
						.append(",edit:")
						.append(t.getAccessUpdateUserFields() != null
								|| GenericUtil.accessControl(scd,
										t.getAccessUpdateTip(),
										t.getAccessUpdateRoles(),
										t.getAccessUpdateUsers()))
						.append(",remove:")
						.append(t.getAccessDeleteUserFields() != null
								|| GenericUtil.accessControl(scd,
										t.getAccessDeleteTip(),
										t.getAccessDeleteRoles(),
										t.getAccessDeleteUsers()));
				if (g.getInsertEditModeFlag() != 0 && insertFlag)
					buf.append(",insertEditMode:true");
				if (insertFlag) {
					if (t.getCopyTip() == 1)
						buf.append(",xcopy:true");
					else if (t.getCopyTip() == 2)
						buf.append(",ximport:true");
				}
				// if(PromisCache.getAppSettingIntValue(scd, "revision_flag")!=0
				// && t.getRevisionFlag()!=0)buf.append(",xrevision:true");
				buf.append("}");
				if ((t.getDoUpdateLogFlag() != 0 || t.getDoDeleteLogFlag() != 0)
						&& FrameworkCache.roleAccessControl(scd,
								108))
					buf.append(",\n logFlags:{edit:")
							.append(t.getDoUpdateLogFlag() != 0)
							.append(",remove:")
							.append(t.getDoDeleteLogFlag() != 0).append("}");

				if (g.getInsertEditModeFlag() != 0 && insertFlag)
					buf.append(serializeGridRecordCreate(gridResult));
				// if(g.get_defaultCrudForm().get_sourceTable().getFileAttachmentFlag()!=0)
				int tableId = t.getTableId();
				if (tableId != 0 && scd != null) {

					if (FrameworkCache.getAppSettingIntValue(customizationId,
							"file_attachment_flag") != 0
							&& t.getFileAttachmentFlag() != 0
							&& FrameworkCache.roleAccessControl(scd,
									101)
							&& FrameworkCache.roleAccessControl(scd,
									 102))
						buf.append(",\n fileAttachFlag:true");
					if (FrameworkCache.getAppSettingIntValue(customizationId,
							"make_comment_flag") != 0
							&& t.getMakeCommentFlag() != 0
							&& FrameworkCache.roleAccessControl(scd,
									 103))
						buf.append(",\n makeCommentFlag:true");
					
				
				}
			} else {
				buf.append(",\n crudFormId:")
				.append(g.getDefaultCrudFormId())
				.append(", crudFlags:{insert:!0}");
			}
		}

		if (!GenericUtil.isEmpty(g.get_toolbarItemList())) { // extra
															// buttonlari
															// var mi yok
															// mu?
			StringBuilder buttons = serializeToolbarItems(scd,
					g.get_toolbarItemList(), false, gridResult.getExtraOutMap());
			if (buttons != null && buttons.length() > 1) {
				buf.append(",\n extraButtons:[")
						.append(LocaleMsgCache.filter2(customizationId,
								xlocale, buttons.toString())).append("]");
			}
		}

		if (!GenericUtil.isEmpty(g.get_menuItemList())) { // extra buttonlari
															// var mi yok
															// mu?
			StringBuilder buttons = serializeMenuItems(scd,
					g.get_menuItemList());
			if (buttons != null && buttons.length() > 1) {
				buf.append(",\n menuButtons:[").append(buttons).append("]");
			}
		}
		

	
		buf.append("\n}");

		buf.append(serializeGridColumns(gridResult, dsc));


		if (!GenericUtil.isEmpty(g.getJsCode())) {
			buf.append("\ntry{")
					.append(GenericUtil.filterExt(g.getJsCode(), scd,
							gridResult.getRequestParams(), null))
					.append("\n}catch(e){")
					.append(FrameworkSetting.debug ? "if(confirm('ERROR grid.JS!!! Throw? : ' + e.message))throw e;"
							: "alert('System/Customization ERROR : ' + e.message)");
			buf.append("}\n");
		}
		return buf;
	}

	private String toDefaultLookupQueryReader() {
		return "root:'data',totalProperty:'browseInfo.totalCount',id:'id',fields:[{name:'id'},{name:'dsc'},{name:'code'}]";
	}

	private StringBuilder serializeQueryReader(
			List<W5QueryField> queryFieldList, Map scd) {
		StringBuilder html = new StringBuilder();
		html.append(",fields:[");
		boolean b = false;
		for (W5QueryField f : queryFieldList) {
			if (b)
				html.append(",\n");
			else
				b = true;
			html.append("{id:'");
			switch (f.getPostProcessType()) {
			case 9:
				html.append("_").append(f.getDsc());
				break;
			case 6:
				html.append(f.getDsc().substring(1));
				break;
			default:
				html.append(f.getDsc());
			}
			html.append("', name:'").append(GenericUtil.stringToJS(LocaleMsgCache.get2(scd, f.getDsc()))).append("'");
			html.append("}");
		}

		html.append("]");

		return html;
	}

	/*
	 * private StringBuilder serializeQueryReader4Tree(List<W5QueryField>
	 * queryFieldList, String id, List<W5TableField> extendedTableFields,
	 * List<W5QueryField> postProcessQueryFieldList, int processTip){
	 * StringBuilder html = new StringBuilder(); html.append(
	 * "{root:'data',successProperty: 'success',totalProperty:'browseInfo.totalCount',id:'"
	 * ).append(id).append("'},new Ext.data.Record.create(["); boolean b =
	 * false; for(W5QueryField f:queryFieldList){ if(b)html.append(",\n"); else
	 * b=true; html.append("{name:'"); switch(f.getPostProcessTip()){ case
	 * 9:html.append("_").append(f.getDsc());break; case
	 * 6:html.append(f.getDsc().substring(1));break;
	 * default:html.append(f.getDsc()); } html.append("'");
	 * if(f.getFieldTip()>2)
	 * html.append(",type:'").append(PromisSetting.sortMap[f
	 * .getFieldTip()]).append("'");
	 * if(f.getFieldTip()==2)html.append(",type:'date',dateFormat:'d/m/Y h:i:s'"
	 * );
	 * 
	 * if(f.getPostProcessTip()>=10)html.append("},{name:'").append(f.getDsc()).
	 * append("_qw_'"); html.append("}"); }
	 * if(extendedTableFields!=null)for(W5TableField f:extendedTableFields){
	 * html.append(",\n{name:'"); html.append(f.getDsc()).append("'");
	 * if(f.getFieldTip
	 * ()>2)html.append(",type:'").append(PromisSetting.sortMap[f
	 * .getFieldTip()]).append("'");
	 * if(f.getFieldTip()==2)html.append(",type:'date',dateFormat:'d/m/Y h:i:s'"
	 * );
	 * if(f.getExtendedLookUpId()>0)html.append("},{name:'").append(f.getDsc()
	 * ).append("_qw_'"); html.append("}"); }
	 * if(postProcessQueryFieldList!=null)for(W5QueryField
	 * f:postProcessQueryFieldList){
	 * html.append(",\n{name:'").append(f.getDsc()).append("',type:'int'}");
	 * if(f
	 * .getPostProcessTip()>0)html.append(",\n{name:'").append(f.getDsc()).append
	 * ("_qw_'}"); } switch(processTip){ case 1://log html.append(
	 * ",\n{name:'xrow_id'},{name:'log5_dttm',type:'date',dateFormat:'d/m/Y h:i:s'},\n{name:'log5_user_id',type:'int'},{name:'log5_user_id_qw_'}"
	 * ); break; case 2://parentRecord html.append(",\n{name:'_record'}");
	 * break; }
	 * 
	 * html.append("])");
	 * 
	 * return html; }
	 */
	final public static String[] postQueryMap = new String[] {
			"disabledCheckBoxHtml", "accessControlHtml",
			"fileAttachmentHtml", "commentHtml", "keywordHtml",
			"approvalHtml", "mailBoxRenderer", "pictureHtml", "revisionHtml", "vcsHtml" };

	private StringBuilder serializeListColumns(W5ListViewResult listResult) {
		String xlocale = (String) listResult.getScd().get("locale");
		int customizationId = (Integer) listResult.getScd().get(
				"customizationId");
		W5List l = listResult.getListView();
		List<W5ListColumn> newColumns = new ArrayList(l.get_listColumnList()
				.size());
		for (W5ListColumn c : l.get_listColumnList())
			if (c.get_queryField() != null) {
				W5QueryField f = c.get_queryField();
				W5TableField tf = f.getMainTableFieldId() > 0 ? listResult
						.getListView().get_mainTable().get_tableFieldMap()
						.get(f.getMainTableFieldId()) : null;
				if (tf != null) {
					
					if (!GenericUtil.accessControl(listResult.getScd(),
							tf.getAccessViewTip(), tf.getAccessViewRoles(),
							tf.getAccessViewUsers()))
						continue;// access control
				}
				newColumns.add(c);
			}

		StringBuilder buf = new StringBuilder();
		buf.append("\n").append(listResult.getListView().getDsc())
				.append(".columns=[");
		boolean b = false;
		for (W5ListColumn c : newColumns) {
			String qds = c.get_queryField().getDsc();
			if (b)
				buf.append(",\n");
			else
				b = true;
			buf.append("{header: '").append(
					LocaleMsgCache.get2(customizationId, xlocale,
							c.getLocaleMsgKey()));
			buf.append("', width: ")
					.append(new BigDecimal(c.getWidth()).divide(new BigDecimal(l.get_totalWidth()), 2,BigDecimal.ROUND_UP))
					.append(", dataIndex: '")
					.append(qds)
					.append("'")
					.append(", sortable: ")
					.append(c.getSortableFlag() != 0
							&& c.get_queryField().getPostProcessType() != 101); // post
																				// sql
																				// select
																				// tip==101
			// .append(", id: '").append(qds).append("'"); //post sql select
			// tip==101
			if (c.getAlignTip() != 1)
				buf.append(", align: '")
						.append(FrameworkSetting.alignMap[c.getAlignTip()])
						.append("'");// left'ten farkli ise
			if (!GenericUtil.isEmpty(c.getTemplate()))
				buf.append(", tpl:'")
						.append(GenericUtil.stringToJS(c.getTemplate()))
						.append("'");
			/*
			 * if(c.getRenderer()!=null){
			 * buf.append(", renderer:").append(c.getRenderer());//browser
			 * renderer ise //
			 * if(c.getRenderer().equals("disabledCheckBoxHtml"))
			 * boolRendererFlag=true; } else
			 * if(c.get_queryField().getPostProcessTip()>=10 &&
			 * c.get_queryField().getPostProcessTip()!=101){
			 * buf.append(", renderer:gridQwRenderer('"
			 * ).append(qds).append("')");//browser renderer ise } else {
			 * if(qds.length()>3 && qds.indexOf("_dt")==qds.length()-3)
			 * buf.append(", renderer:fmtShortDate");//browser renderer ise else
			 * if(qds.length()>5 && qds.indexOf("_dttm")==qds.length()-5)
			 * buf.append(", renderer:fmtDateTime");//browser renderer ise else
			 * if(qds.length()>5 && qds.indexOf("_flag")==qds.length()-5){
			 * buf.append(", renderer:disabledCheckBoxHtml");//browser renderer
			 * ise // boolRendererFlag = true; } else
			 * if(listResult.getListView()
			 * .get_queryFieldMapDsc().get(qds+"_qw_")!=null){
			 * buf.append(", renderer:gridQwRenderer('"
			 * ).append(qds).append("')");//browser renderer ise //
			 * qwRendererFlag = true; }
			 * 
			 * }
			 */
			if (c.getVisibleFlag() == 0)
				buf.append(", hidden: true");
			if (c.getExtraDefinition() != null
					&& c.getExtraDefinition().length() > 2)
				buf.append(c.getExtraDefinition());
			buf.append("}");
		}
		buf.append("]");
		return buf;
	}

	private StringBuilder serializeGridColumns(W5GridResult gridResult, String dsc) {
/*
columns:[
					{ id:"package",	header:{ css:"myaction", text:"Name" } , 			width:200 ,	sort:"string"},
					{ id:"section",	header:"Section",		width:120 ,	sort:"string"},
					{ id:"size",	header:"Size" , 		width:80  ,sort:"int"},
					{ id:"architecture",	header:"PC", 	width:60  ,	sort:"string"}
				],

 */
		Map<String, Object> scd = gridResult.getScd();
		W5Grid grid = gridResult.getGrid();
		if(dsc==null)dsc= grid.getDsc();
		String xlocale = (String) scd.get("locale");
		int customizationId = (Integer) scd.get("customizationId");
		List<W5GridColumn> oldColumns = grid.get_gridColumnList();
		W5Table viewTable = grid.get_viewTable();
		W5Table crudTable = grid.get_crudTable();
		if (crudTable == null)
			crudTable = viewTable;
		

		List<W5GridColumn> newColumns = new ArrayList();
		StringBuilder bufGrdColumnGroups = new StringBuilder();
		for (W5GridColumn c : oldColumns)
			if (c.get_queryField() != null) {
				W5QueryField f = c.get_queryField();
				W5TableField tf = viewTable!=null && f.getMainTableFieldId() > 0 ? viewTable
						.get_tableFieldMap().get(f.getMainTableFieldId())
						: null;
				if (tf != null) {
					
					if (tf.getAccessViewUserFields()==null && !GenericUtil.accessControl(gridResult.getScd(),
							tf.getAccessViewTip(), tf.getAccessViewRoles(),
							tf.getAccessViewUsers()))
						continue;// access control
				}
				newColumns.add(c);
			}			

		if (grid.get_postProcessQueryFields() != null && (gridResult.getRequestParams()==null || GenericUtil.uInt(gridResult.getRequestParams(), "_no_post_process_fields")==0)) {
			boolean gridPostProcessColumnFirst = FrameworkCache.getAppSettingIntValue(scd,"grid_post_process_column_first")!=0;
			boolean gridPostProcessCommentFirst = FrameworkCache.getAppSettingIntValue(scd,"grid_post_process_comment_first")!=0;
			int x = 0;
			for (W5QueryField f : grid.get_postProcessQueryFields()) {
				if(!f.getDsc().equals("ar_version_no")){
					if (viewTable != null)
						switch (f.getFieldType()) {
						case 2:// file attachment
						case 7:// picture attachment
							if (!FrameworkCache.roleAccessControl(
									gridResult.getScd(),
									 101))
								continue;
							break;
						case 6:// mail
							if (!FrameworkCache.roleAccessControl(
									gridResult.getScd(),
									 106))
								continue;
							break;
						}
					W5GridColumn c = new W5GridColumn();
					c.set_queryField(f);
					c.setWidth((short)40);//f.getTabOrder()
					c.setAlignType((short) 0);
					c.setLocaleMsgKey("");//:("<span class=\"webix_icon fa-"+ FrameworkSetting.postQueryGridImgMap4Webix[f.getFieldTip()]+ "\"></span>")
					c.setVisibleFlag((short) 1);
					String renderer = postQueryMap[f.getFieldType()];
					c.setRenderer(renderer);
					if(f.getDsc().equals(FieldDefinitions.queryFieldName_Comment) && FrameworkCache.getAppSettingIntValue(scd, "make_comment_summary_flag")!=0){
						c.setWidth((short) (f.getTabOrder() + 10));
						c.setSortableFlag((short)1);
					}
					if (f.getDsc().equals(FieldDefinitions.queryFieldName_Workflow)) {// approval_record_flag
						c.setWidth((short) (f.getTabOrder() + 100));
						c.setAlignType((short) 1);
						c.setLocaleMsgKey("approval_status");
						newColumns.add(x, c);
						x++;
						continue;
					} else if (renderer.indexOf("Renderer") > 0) {// renderer
																	// var
						c.setRenderer(renderer + "(" + dsc + ")");
					}
					if (gridPostProcessColumnFirst && f.getDsc().equals(FieldDefinitions.queryFieldName_FileAttachment)) {
						newColumns.add(c);
						x++;
					} else if (gridPostProcessCommentFirst && f.getDsc().equals(FieldDefinitions.queryFieldName_Comment)) {
						newColumns.add(x, c);
						x++;
					} else if (f.getDsc().equals(FieldDefinitions.queryFieldName_Revision)) {
						newColumns.add(x, c);
						x++;
					} else
						newColumns.add(c);
	
				}
			}
		}

//		if (newColumns.size() > 0)newColumns.get(0).setWidth((short) (newColumns.get(0).getWidth() + 10));

		StringBuilder buf = new StringBuilder();
		boolean b = false;
		boolean insertOrEditPrivilege = !gridResult.isViewReadOnlyMode()
				&& (crudTable == null || (crudTable != null && (!GenericUtil
						.isEmpty(crudTable.getAccessUpdateUserFields())
						|| GenericUtil.accessControl(gridResult.getScd(),
								crudTable.getAccessInsertTip(),
								crudTable.getAccessInsertRoles(),
								crudTable.getAccessInsertUsers()) || GenericUtil
							.accessControl(gridResult.getScd(),
									crudTable.getAccessUpdateTip(),
									crudTable.getAccessUpdateRoles(),
									crudTable.getAccessUpdateUsers()))));
		Set<Integer> editableColumnSet = new HashSet<Integer>();
	
		for (W5GridColumn c : newColumns)
			if (c.get_formCell() != null) { // editorler
				if (insertOrEditPrivilege
						|| (c.getFormCellId() < 0/* freeField? */&& crudTable != null)) {
					W5TableField tf = c.get_queryField().getMainTableFieldId() > 0
							&& crudTable != null ? crudTable
							.get_tableFieldMap().get(
									c.get_queryField().getMainTableFieldId())
							: null;

					if (tf != null)
						if (gridResult.getAction() == 1) {
							if (tf.getAccessUpdateTip() != 0
									&& GenericUtil.isEmpty(tf
											.getAccessUpdateUserFields())
									&& !GenericUtil.accessControl(
											gridResult.getScd(),
											tf.getAccessUpdateTip(),
											tf.getAccessUpdateRoles(),
											tf.getAccessUpdateUsers()))
								continue;
						} else if (gridResult.getAction() == 2) {
							if (!GenericUtil.accessControl(gridResult.getScd(),
									tf.getAccessInsertTip(),
									tf.getAccessInsertRoles(),
									tf.getAccessInsertUsers()))
								continue;
						}
					W5FormCell f = c.get_formCell();
					W5FormCellHelper fcr = gridResult.getFormCellResultMap()
							.get(f.getFormCellId());
					if (fcr == null)
						continue;
					buf.append("\n").append(dsc).append("._").append(f.getDsc()).append("=").append(serializeFormCell(customizationId, xlocale,
									fcr, null));
					/*	if (f.getControlTip() == 9 || f.getControlTip() == 10) {
						buf.append("\n").append(dsc).append("._")
								.append(f.getDsc())
								.append(".on('select',function(a,b,c){\n")
								.append(dsc)
								.append(".sm.getSelected().data.")
								.append(c.get_queryField().getDsc())
								.append("_qw_=b.data.dsc})");
					}
					 */

					b = true;
					editableColumnSet.add(c.getQueryFieldId());
				}
			}
		if (b)
			buf.append("\n").append(dsc).append(".editable=true");

		buf.append("\n").append(dsc).append(".columns=[");
		b = false;

		StringBuffer bufFilters = new StringBuffer(); // grid filtreleri ilgili
														// kolonları tutacak

		for (W5GridColumn c : newColumns) if(c.getVisibleFlag() != 0){
			String qds = c.get_queryField().getDsc();

			if (b)
				buf.append(",\n");
			else
				b = true;
			boolean editableFlag = editableColumnSet.contains(c.getQueryFieldId());
			
			buf.append("{title:");
			if (!editableFlag) {
				buf.append("'").append(LocaleMsgCache.get2(scd, c.getLocaleMsgKey())).append("'");
			} else {
				buf.append("_('span',{style:{color:'darkorange'}},'").append(LocaleMsgCache.get2(scd,c.getLocaleMsgKey())).append("')");
			}
			boolean qwRendererFlag = false;
			boolean boolRendererFlag = false;
			if(c.getFilterFlag()!=0)buf.append(", filter:!0");
			buf.append(", name: '").append(qds).append("'");
			if(c.getSortableFlag() != 0 && c.get_queryField().getPostProcessType() != 101){
					buf.append(", sort:true");
					if(c.get_queryField().getFieldType()>1){//TODO. custom after string sorting
//						? new String[]{"","string","date","int","int","",""}[c.get_queryField().getFieldTip()] : "server").append("'");
					}
			}
			
			if (c.getAlignType() != 1)buf.append(", align:'").append(FrameworkSetting.alignMap[c.getAlignType()]).append("'");// left'ten farkli ise
//			if(grid.getAutoExpandFieldId()!=0 && grid.getAutoExpandFieldId()==c.getQueryFieldId())buf.append(", fillspace:!0").append(", minWidth: ").append((4*c.getWidth())/3);//.append(c.getWidth());
//			else buf.append(", width: '").append((4*c.getWidth())/3).append("%'");//.append(c.getWidth());
			buf.append(", width: ").append((6*c.getWidth())/5);//.append(c.getWidth());

			W5TableField tf = c.get_queryField().getMainTableFieldId() > 0
					&& crudTable != null ? crudTable.get_tableFieldMap().get(
					c.get_queryField().getMainTableFieldId()) : null;
			if ((c.getFormCellId() < 0/* freeField? */&& crudTable != null)
					|| (insertOrEditPrivilege && c.get_formCell() != null && (tf == null || GenericUtil
							.accessControl(gridResult.getScd(), (gridResult.getAction() == 1 ? tf.getAccessUpdateTip()
									: tf.getAccessInsertTip()), tf
									.getAccessUpdateRoles(), tf
									.getAccessUpdateUsers()))))
				buf.append(", editor:").append(dsc).append("._").append(c.get_formCell().getDsc());
			
			if (!GenericUtil.isEmpty(c.getRenderer())) {
				buf.append(", formatter:").append(c.getRenderer());// browser renderer ise
				if (c.getRenderer().equals("disabledCheckBoxHtml"))
					boolRendererFlag = true;
			} else if (c.get_queryField().getPostProcessType() >= 10
					&& c.get_queryField().getPostProcessType() <90) {
				if (c.get_formCell() == null || !editableFlag) {
					if (FrameworkSetting.chat && (c.get_queryField().getPostProcessType() == 20 || c.get_queryField().getPostProcessType() == 53)) // user lookup ise
						buf.append(", formatter:gridUserRenderer");// browser renderer ise
					else if (c.get_queryField().getPostProcessType() == 12) // table lookup ise
						buf.append(", formatter:gridQwRendererWithLink(").append(c.get_queryField().getLookupQueryId()).append(")");// browser renderer ise
					else {
						boolean bx = true;
						if(c.get_queryField().getPostProcessType() < 11){
							W5LookUp lu = FrameworkCache.getLookUp(scd, c.get_queryField().getLookupQueryId());
							if(lu!=null && lu.getCssClassFlag()!=0){
								bx = false;
								buf.append(", formatter:function(row){var badgeMap={'':false");
								for(W5LookUpDetay lud:lu.get_detayList())if(!GenericUtil.isEmpty(lud.getParentVal()))buf.append(",'").append(lud.getVal()).append("':'").append(lud.getParentVal()).append("'");
								buf.append("};var badge=badgeMap[row.").append(qds).append("||''];return badge?_('span',{className:'badge badge-pill badge-'+badge},row.").append(qds).append("_qw_):row.").append(qds).append("_qw_;}");// browser renderer ise
							}
						}
						if(bx)buf.append(", formatter:function(row){return row.").append(qds).append("_qw_;}");// browser renderer ise
					}
					qwRendererFlag = true;
				} else
					switch (c.get_formCell().getControlType()) {
					case 6:
					case 7:
						buf.append(", formatter:editGridComboRenderer('").append(qds).append("',")
								.append(grid.getDsc()).append("._").append(c.get_formCell().getDsc()).append(")");
						break;
					case 15:
						buf.append(", formatter:editGridLovComboRenderer('").append(qds).append("',")
								.append(grid.getDsc()).append("._").append(c.get_formCell().getDsc()).append(")");
						break;
					default:
						buf.append(", formatter:function(row){return row.").append(qds).append("_qw_;}");// browser renderer ise
						qwRendererFlag = true;
					}
			} else if (qds.length() > 3 && qds.endsWith("_dt"))
				buf.append(", formatter:strShortDate");// browser formatter ise
			else if (qds.length() > 5 && qds.endsWith("_dttm")){
				buf.append(", formatter:strDateTime").append(FrameworkCache.getAppSettingIntValue(0, "fmt_date_time_ago_flag")!=0 ?"Ago":"");// browser formatter ise
			} else if ((qds.length() > 5
					&& qds.endsWith("_flag")) || (qds.length() > 3
							&& qds.startsWith("is_"))) {
				buf.append(", formatter:disabledCheckBoxHtml");// browser formatter ise
				boolRendererFlag = true;
			} else if (grid.get_queryFieldMapDsc().get(qds + "_qw_") != null) {
				buf.append(", formatter:function(row){return row.").append(qds).append("_qw_;}");// browser renderer ise
				qwRendererFlag = true;
			}
			
			if (c.getExtraDefinition() != null && c.getExtraDefinition().length() > 2)
				buf.append(c.getExtraDefinition());
			buf.append("}");
		
		}
		buf.append("]");

		return buf;
	}

	public StringBuilder serializeTreeQueryRemoteData(W5QueryResult queryResult) {
		String children = queryResult.getRequestParams().get("_children") != null ? queryResult
				.getRequestParams().get("_children") : "children";
		int customizationId = (Integer) queryResult.getScd().get(
				"customizationId");
		String xlocale = (String) queryResult.getScd().get("locale");
		StringBuilder buf = new StringBuilder();
		if (queryResult.getErrorMap().isEmpty()) {
			buf.append("[");
			int leafField = -1;
			if (queryResult.getNewQueryFields() != null) {
				for (W5QueryField field : queryResult.getNewQueryFields())
					if (leafField == -1 && field.getDsc().equals("leaf")) {
						leafField = field.getTabOrder() - 1;
						break;
					}
				if (leafField == -1)
					throw new IWBException("sql", "Query(TreeRemote)",
							queryResult.getQueryId(), GenericUtil.replaceSql(
									queryResult.getExecutedSql(),
									queryResult.getSqlParams()),
							"TreeQueryField does'nt exist: [level]", null);

				List<Object[]> datas = queryResult.getData();
				if (datas != null && datas.size() > 0) {
					boolean bx = false;
					for (Object[] o : datas) {
						if (bx)
							buf.append(",");
						else
							bx = true;
						buf.append("\n{"); // satir
						boolean b = false;
						for (W5QueryField f : queryResult.getNewQueryFields()) {

							if (b)
								buf.append(",");
							else
								b = true;
							Object obj = o[f.getTabOrder() - 1];
							if (f.getPostProcessType() == 9)
								buf.append("_");
							if (f.getFieldType() == 5) {
								buf.append(f.getDsc()).append(":")
										.append(GenericUtil.uInt(obj) != 0);
								continue;
							}
							buf.append(f.getPostProcessType() == 6 ? f.getDsc().substring(1):f.getDsc()).append(":'");
							if (obj != null) {
								switch (f.getPostProcessType()) { // queryField
																	// PostProcessTip
								case 8:
									buf.append(GenericUtil.stringToHtml(obj));
									break;
								case 20: // user LookUp
									buf.append(obj)
											.append("',")
											.append(f.getDsc())
											.append("_qw_:'")
											.append(UserUtil.getUserName(GenericUtil.uInt(obj)));
									break;
								case 21: // users LookUp
									String[] ids = ((String) obj).split(",");
									if (ids.length > 0) {
										String res = "";
										for (String s : ids) {
											res += ","
													+ UserUtil.getUserName(GenericUtil.uInt(s));
										}
										buf.append(obj).append("',")
												.append(f.getDsc())
												.append("_qw_:'")
												.append(res.substring(1));
									}
									break;
								case 53: // User LookUp Real Name
									buf.append(obj)
											.append("',")
											.append(f.getDsc())
											.append("_qw_:'")
											.append(UserUtil.getUserDsc(GenericUtil.uInt(obj)));
									break;
								case 54: // Users LookUp Real Name
									String[] ids11 = ((String) obj).split(",");
									if (ids11.length > 0) {
										String res = "";
										for (String s : ids11) {
											res += ","
													+ UserUtil.getUserDsc(GenericUtil.uInt(s));
										}
										buf.append(obj).append("',")
												.append(f.getDsc())
												.append("_qw_:'")
												.append(res.substring(1));
									}
									break;
								case 22:
								case 23: // roles: TODO
									buf.append(obj);
									break;
								case 1:// duz
									buf.append(obj);
									break;
								case 2: // locale filtresinden gececek
									buf.append(LocaleMsgCache.get2(
											customizationId, xlocale,
											obj.toString()));
									break;
								case 10:
								case 11: // demek ki static lookup'li deger
											// tutulacak
									buf.append(GenericUtil.stringToJS(obj
											.toString()));
									if (f.getLookupQueryId() == 0)
										break;
									W5LookUp lookUp = FrameworkCache.getLookUp(
											queryResult.getScd(),
											f.getLookupQueryId());
									if (lookUp == null)
										break;
									buf.append("',").append(f.getDsc())
											.append("_qw_:'");
									String[] objs = f.getPostProcessType() == 11 ? ((String) obj)
											.split(",") : new String[] { obj
											.toString() };
									boolean bz = false;
									for (String q : objs) {
										if (bz)
											buf.append(", ");
										else
											bz = true;
										W5LookUpDetay d = lookUp.get_detayMap()
												.get(q);
										if (d != null) {
											String s = d.getDsc();
											if (s != null) {
												s = LocaleMsgCache.get2(
															customizationId,
															xlocale, s);
												buf.append(GenericUtil
														.stringToJS(s));
											}
										} else {
											buf.append("???: ").append(q);
										}
									}
									break;
								case 12:
								case 13:// TODO

									break;
								case 49:// approval _qw_
									buf.append(obj);
									int id = Math.abs(GenericUtil.uInt(obj));
									if (id == 999)
										buf.append("',").append(f.getDsc())
												.append("_qw_:'Reddedildi");
									else
										buf.append("',")
												.append(f.getDsc())
												.append("_qw_:'")
												.append(FrameworkCache.getWorkflow(queryResult.getScd(),f.getLookupQueryId())
														.get_approvalStepMap()
														.get(id).getDsc());
									break;
								
								default:
									buf.append(GenericUtil.stringToJS(obj
											.toString()));
								}
							}
							buf.append("'");
						}
						// if(!leafFlag)buf.append(",").append(children).append(":[]");
						buf.append("}");

					}
				}
			}
			buf.append("]");
		}
		return buf;
	}
	
	private StringBuilder recursiveSerialize(List<StringBuilder> td, Map<String, List> m, String children){
		if(td==null || td.isEmpty())return null;
		boolean b = false;
		StringBuilder s = new StringBuilder();
		for(StringBuilder sb:td){
			int posOf = sb.indexOf(":");
			String id= sb.substring(0,posOf);
			
			if (b)
				s.append(",");
			else
				b = true;
			s.append("{").append(sb.substring(posOf+1));
			List<StringBuilder> childs = m.get(id);
			if(childs!=null && !childs.isEmpty())
				s.append(",\"").append(children).append("\":[").append(recursiveSerialize(m.get(id), m, children)).append("]");
			s.append("}");
		}
		return s;
	}

	public StringBuilder serializeTreeQueryData(W5QueryResult qr) {
		/*
{
              view:"tree", gravity:0.15,
              select:true, on:{'onItemClick':function(ax,bx,cx,dx){console.log(this.getItem(ax));console.log(bx);console.log(cx);}},
    data: [
        {id:"root22", value:"Cars", data:[
            { id:"1", open:true, value:"Toyota", data:[
              { id:"21312", value:"Avalon", href:"showPage?_tid=123" },
                { id:"1212", value:"Corolla" },
                { id:"1.3", value:"Camry" }
            ]},
            { id:"2", value:"Skoda", open:true, data:[
                { id:"2.1", value:"Octavia" },
                { id:"2.2", value:"Superb" }
            ]}
        ]},{id:"root12322", value:"Cars2", data:[
            { id:"1dsf", open:true, value:"Toyota", data:[
                { id:"21sdf312", value:"Avalon" },
                { id:"1sdf212", value:"Corolla" },
                { id:"1sdf.3", value:"Camry" }
            ]},
            { id:"2sdf", value:"Skoda", open:true, data:[
                { id:"2.sf1", value:"Octavia" },
                { id:"2.sdf2", value:"Superb" }
            ]}
        ]}
    ]
}

		 */
		String children = qr.getRequestParams().get("_children") != null ? qr
				.getRequestParams().get("_children") : "data";
		int customizationId = (Integer) qr.getScd().get(
				"customizationId");
		String xlocale = (String) qr.getScd().get("locale");
		StringBuilder buf = new StringBuilder();
		boolean json = GenericUtil.uInt(qr.getRequestParams(), "_json")!=0;
		boolean dismissNull = qr.getRequestParams()!=null && GenericUtil.uInt(qr.getRequestParams(), "_dismissNull")!=0;
		if(json)buf.append("{\"success\":true,\"data\":");
		if (qr.getErrorMap().isEmpty()) {
			buf.append("[");
//			int levelField = -1;
			int idField = -1;
			int parentField = -1;
			if (qr.getNewQueryFields() != null) {
				for (W5QueryField field : qr.getNewQueryFields()){
//					if (levelField == -1 && field.getDsc().equals("xlevel")) { levelField = field.getTabOrder() - 1; continue; }
					if (idField == -1 && field.getDsc().equals("id")) {
						idField = field.getTabOrder() - 1;
						if(parentField!=-1)break;
						else continue;
					}
					if (parentField == -1 && field.getDsc().equals("parent_id")) {
						parentField = field.getTabOrder() - 1;
						if(idField!=-1)break;
						else continue;
					}
				}
				if (idField == -1 || parentField==-1){
					idField = -1;parentField = -1;
					for (W5QueryField field : qr.getNewQueryFields()){
//						if (levelField == -1 && field.getDsc().equals("xlevel")) { levelField = field.getTabOrder() - 1; continue; }
						if (idField == -1 && field.getDsc().equals("x_id")) {
							idField = field.getTabOrder() - 1;
							if(parentField!=-1)break;
							else continue;
						}
						if (parentField == -1 && field.getDsc().equals("x_parent")) {
							parentField = field.getTabOrder() - 1;
							if(idField!=-1)break;
							else continue;
						}
					}
					if (idField == -1 || parentField==-1)throw new IWBException("sql", "Query(Tree)",
							qr.getQueryId(), GenericUtil.replaceSql(
									qr.getExecutedSql(),
									qr.getSqlParams()),
							"TreeQueryField does\"nt exist: [id || parent_id]", null);
				}

				List<StringBuilder> treeData = new ArrayList();
				Map<String, List> mapOfParent = new HashMap<String, List>();
				
				List<Object[]> datas = qr.getData();
				if (datas != null && datas.size() > 0) {
					for (Object[] o : datas) {
						String id = o[idField].toString();
						mapOfParent.put(id, new ArrayList());
					}
					for (Object[] o : datas) {
						String id = o[idField].toString();
						String parent = o[parentField].toString();
						List childTree = mapOfParent.get(parent);
						if(childTree==null)childTree=treeData;
						
						boolean b = false;
						StringBuilder buf2= new StringBuilder();
						buf2.append(id).append(":");//ilk bastaki
						for (W5QueryField f : qr.getNewQueryFields()) {
							Object obj = o[f.getTabOrder() - 1];
							if(dismissNull && obj==null)continue;
							if (b)
								buf2.append(",");
							else
								b = true;
							buf2.append("\"");
							if (f.getPostProcessType() == 9)
								buf2.append("_");
							if (f.getFieldType() == 5) {
								buf2.append(f.getDsc()).append("\":")
										.append(GenericUtil.uInt(obj) != 0);
								continue;
							}
							if(f.getDsc().equals("xtext") || f.getDsc().equals("text"))buf2.append("value\":");//hack for webix
							else buf2.append(f.getPostProcessType() == 6 ? f.getDsc().substring(1):f.getDsc()).append("\":");
							if (f.getFieldType() != 8)
								buf2.append("\"");
							else {
								buf2.append("{");
							} // JSON ise başka
							if (obj != null) {
								switch (f.getPostProcessType()) { // queryField
																	// PostProcessTip
								case 8:
									buf2.append(GenericUtil.stringToHtml(obj));
									break;
								case 20: // user LookUp
									buf2.append(obj)
											.append("\",\"")
											.append(f.getDsc())
											.append("_qw_\":\"")
											.append(UserUtil.getUserName(
													GenericUtil.uInt(obj)));
									break;
								case 21: // users LookUp
									String[] ids = ((String) obj).split(",");
									if (ids.length > 0) {
										String res = "";
										for (String s : ids) {
											res += ","
													+ UserUtil.getUserName(GenericUtil.uInt(s));
										}
										buf2.append(obj).append("\",\"")
												.append(f.getDsc())
												.append("_qw_\":\"")
												.append(res.substring(1));
									}
									break;
								case 53: // User LookUp Real Name
									buf2.append(obj)
											.append("\",\"")
											.append(f.getDsc())
											.append("_qw_\":\"")
											.append(UserUtil.getUserDsc(GenericUtil.uInt(obj)));
									break;
								case 54: // Users LookUp Real Name
									String[] ids11 = ((String) obj).split(",");
									if (ids11.length > 0) {
										String res = "";
										for (String s : ids11) {
											res += ","
													+ UserUtil.getUserDsc(GenericUtil.uInt(s));
										}
										buf2.append(obj).append("\",\"")
												.append(f.getDsc())
												.append("_qw_\":\"")
												.append(res.substring(1));
									}
									break;
								case 22:
								case 23: // roles: TODO
									buf2.append(obj);
									break;
								case 1:// duz
									buf2.append(obj);
									break;
								case 2: // locale filtresinden gececek
									buf2.append(LocaleMsgCache.get2(
											qr.getScd(),
											obj.toString()));
									break;
								case 10:
								case 11: // demek ki static lookup\"li deger
											// tutulacak
									buf2.append(GenericUtil.stringToJS2(obj
											.toString()));
									if (f.getLookupQueryId() == 0)
										break;
									W5LookUp lookUp = FrameworkCache.getLookUp(
											qr.getScd(),
											f.getLookupQueryId());
									if (lookUp == null)
										break;
									buf2.append("\",\"").append(f.getDsc())
											.append("_qw_\":\"");
									String[] objs = f.getPostProcessType() == 11 ? ((String) obj)
											.split(",") : new String[] { obj
											.toString() };
									boolean bz = false;
									for (String q : objs) {
										if (bz)
											buf2.append(", ");
										else
											bz = true;
										W5LookUpDetay d = lookUp.get_detayMap()
												.get(q);
										if (d != null) {
											String s = d.getDsc();
											if (s != null) {
												s = LocaleMsgCache.get2(
														qr.getScd(), s);
												buf2.append(GenericUtil
														.stringToJS2(s));
											}
										} else {
											buf2.append("???: ").append(q);
										}
									}
									break;
								case 12:
								case 13:// TODO

									break;
								case 49:// approval _qw_
									buf2.append(obj);
									int id2 = Math.abs(GenericUtil.uInt(obj));
									if (id2 == 999)
										buf2.append("\",\"").append(f.getDsc())
												.append("_qw_\":\"Reddedildi");
									else
										buf2.append("\",\"")
												.append(f.getDsc())
												.append("_qw_\":\"")
												.append(FrameworkCache.getWorkflow(qr.getScd(),f.getLookupQueryId())
														.get_approvalStepMap()
														.get(id2).getDsc());
									break;
								
								default:
									buf2.append(GenericUtil.stringToJS2(obj
											.toString()));
								}
							}
							if (f.getFieldType() != 8)
								buf2.append("\"");
							else {
								buf2.append("}");
							} // JSON ise başka
						}
						childTree.add(buf2);
					}
				}
				
				buf.append(recursiveSerialize(treeData, mapOfParent, children));
			}
			buf.append("]");
			
			if(json)buf.append(",\n\"pos\":0, \"total_count\":").append(qr.getFetchRowCount()).append("}");

			return buf;
		} else {
			return buf
					.append("{\"success\":false,\"errorType\":\"validation\",\n\"errors\":")
					.append(serializeValidatonErrors(qr.getErrorMap(),
							xlocale)).append("}");

		}
	}

	private StringBuilder serializeTableHelperList(int customizationId,
			String xlocale, List<W5TableRecordHelper> ltrh) {
		StringBuilder buf = new StringBuilder();
		boolean bq = false;
		buf.append("[");
		if (ltrh != null)
			for (W5TableRecordHelper trh : ltrh) {
				W5Table dt = FrameworkCache.getTable(customizationId,
						trh.getTableId());
				if (dt == null)
					break;
				if (bq)
					buf.append(",");
				else
					bq = true;
				buf.append("{\"tid\":")
						.append(trh.getTableId())
						.append(",\"tpk\":")
						.append(trh.getTablePk())
						.append(",\"tcc\":")
						.append(trh.getCommentCount())
						.append(",\"tdsc\":\"")
						.append(LocaleMsgCache.get2(customizationId, xlocale,
								dt.getDsc())).append("\"")
						.append(",\"dsc\":\"")
						.append(GenericUtil.stringToJS2(trh.getRecordDsc()))
						.append("\"}");
			}
		buf.append("]");
		return buf;
	}

	public StringBuilder serializeQueryData(W5QueryResult qr) {
		if (qr.getQuery().getQueryType() == 10 || (qr.getRequestParams()!=null && GenericUtil.uInt(qr.getRequestParams(), "_tqd")!=0) )
			return serializeTreeQueryData(qr);
		if (qr.getQuery().getQueryType() == 14)
			return serializeTreeQueryRemoteData(qr);
		int customizationId = (Integer) qr.getScd().get("customizationId");
		String xlocale = (String) qr.getScd().get("locale");
		String userIdStr = qr.getScd().containsKey("userId") ? qr.getScd().get("userId").toString() : null;
		List datas = qr.getData();
		StringBuilder buf = new StringBuilder();
		boolean convertDateToStr = qr.getRequestParams()!=null && GenericUtil.uInt(qr.getRequestParams(), "_cdds")!=0; 
		buf.append("{\"success\":").append(qr.getErrorMap().isEmpty())
				.append(",\"queryId\":").append(qr.getQueryId())
				.append(",\"execDttm\":\"")
				.append(GenericUtil.uFormatDateTime(new Date())).append("\"");
		W5Table t = null;
		if (qr.getErrorMap().isEmpty()) {
			boolean dismissNull = qr.getRequestParams()!=null && qr.getRequestParams().containsKey("_dismissNull");
			buf.append(",\n\"data\":["); // ana
			if (datas != null && datas.size() > 0) {
				boolean isMap = (datas.get(0) instanceof Map);
				boolean bx = false;
				for (Object o : datas) {
					if (bx)
						buf.append(",\n");
					else
						bx = true;
					buf.append("{"); // satir
					boolean b = false;
					for (W5QueryField f : qr.getNewQueryFields()) {
						Object obj = isMap ? ((Map)o).get(f.getDsc()) : ((Object[])o)[f.getTabOrder() - 1];
						if(obj==null && dismissNull)continue;
						if (b)
							buf.append(",");
						else
							b = true;
						if (f.getPostProcessType() == 9)
							buf.append("\"_");
						else
							buf.append("\"");
						buf.append(f.getPostProcessType() == 6 ? f.getDsc()
								.substring(1) : f.getDsc());
						if (f.getFieldType() == 5) {// boolean
							buf.append("\":").append(GenericUtil.uInt(obj) != 0);
							continue;
						}
						if (f.getFieldType() == 6) {// auto
							buf.append("\":");
							if (obj == null || obj.toString().equals("0"))
								buf.append("null");
							else if (GenericUtil.uInt(obj) != 0)
								buf.append(obj);
							else
								buf.append("\"").append(obj).append("\"");
							continue;
						} else if (convertDateToStr && f.getFieldType() == 2 && obj!=null && (obj instanceof java.sql.Timestamp || obj instanceof java.util.Date)) {// date 
							buf.append("\":\"").append(obj instanceof java.sql.Timestamp ? GenericUtil.uFormatDateTime((java.sql.Timestamp)obj) : GenericUtil.uFormatDateTime((java.util.Date)obj)).append("\"");
							continue;
						} else if(f.getFieldType() == 8) {
							buf.append("\":");
							if (obj == null)buf.append("null");
							else if(obj instanceof Map)buf.append(GenericUtil.fromMapToJsonString2Recursive((Map)obj));
							else if(obj instanceof List)buf.append(GenericUtil.fromListToJsonString2Recursive((List)obj));
							else buf.append(obj);
							continue;
						}
						buf.append("\":\"");
						if (obj != null)
							switch (f.getPostProcessType()) { // queryField
																// PostProcessTip
							case 5://decryption
								
								buf.append(GenericUtil.stringToJS2(EncryptionUtil.decrypt(obj.toString(), f.getLookupQueryId())));
								break;
							case 14://dcfryption + data maskng
								obj = EncryptionUtil.decrypt(obj.toString(), f.getLookupQueryId());
								if(obj==null)obj="";
							case	4://data masking
								int maskType = f.getLookupQueryId();
								if(f.getMainTableFieldId()>0 && qr.getQuery().getSourceObjectId()>0 && qr.getQuery().getQuerySourceType()==15) {
									if(t == null) t = FrameworkCache.getTable(qr.getScd(), qr.getQuery().getSourceObjectId());
									W5TableField tf = t.get_tableFieldMap().get(f.getMainTableFieldId());
									if(tf!=null && tf.getAccessMaskTip()>0 && GenericUtil.isEmpty(tf.getAccessMaskUserFields()) 
											&& GenericUtil.accessControl(qr.getScd(), tf.getAccessMaskTip(), tf.getAccessMaskRoles(), tf.getAccessMaskUsers())) {
										buf.append(GenericUtil.stringToJS2(obj
												.toString()));
										break;
									}
									if(tf!=null && f.getPostProcessType()==14)maskType = tf.getAccessMaskTip();
								}
								String strMask = FrameworkCache.getAppSettingStringValue(0, "data_mask", "**********");
								String sobj = obj.toString();
								if(sobj.length()==0) sobj = "x";
								
								switch(maskType) {
								case	1://full
									buf.append(strMask);break;
								case	2://beginning
									buf.append(sobj.charAt(0)).append(strMask.substring(1));break;
								case	3://beg + end
									buf.append(sobj.charAt(0)).append(strMask.substring(2)).append(sobj.charAt(sobj.length()-1));break;
								}
								break;
							case 3:
								buf.append(GenericUtil.onlyHTMLToJS(obj.toString()));
								break;

							case 8:
								buf.append(GenericUtil.stringToHtml2(obj));
								break;
							case 20: // user LookUp
								buf.append(obj)
										.append("\",\"")
										.append(f.getDsc())
										.append("_qw_\":\"")
										.append(UserUtil.getUserName(
												GenericUtil.uInt(obj)));
								break;
							case 21: // users LookUp
								String[] ids = ((String) obj).split(",");
								if (ids.length > 0) {
									String res = "";
									for (String s : ids) {
										res += ","
												+ UserUtil.getUserName(GenericUtil.uInt(s));
									}
									buf.append(obj).append("\",\"")
											.append(f.getDsc())
											.append("_qw_\":\"")
											.append(res.substring(1));
								}
								break;
							case 53: // User LookUp Real Name
								buf.append(obj)
										.append("\",\"")
										.append(f.getDsc())
										.append("_qw_\":\"")
										.append(UserUtil.getUserDsc(GenericUtil.uInt(obj)));
								break;
							case 54: // Users LookUp Real Name
								String[] ids11 = ((String) obj).split(",");
								if (ids11.length > 0) {
									String res = "";
									for (String s : ids11) {
										res += ","
												+ UserUtil.getUserDsc(GenericUtil.uInt(s));
									}
									buf.append(obj).append("\",\"")
											.append(f.getDsc())
											.append("_qw_\":\"")
											.append(res.substring(1));
								}
								break;
							case 22:
							case 23: // roles: TODO
								buf.append(obj);
								break;
							case 1:// duz
								buf.append(obj);
								break;
							case 2: // locale filtresinden gececek
								buf.append(LocaleMsgCache.get2(
										customizationId, xlocale,
										obj.toString()));
								break;
							case 10:
							case 11: // demek ki static lookup'li deger
										// tutulacak
								buf.append(GenericUtil.stringToJS2(obj
										.toString()));
								if (f.getLookupQueryId() == 0)
									break;
								W5LookUp lookUp = FrameworkCache.getLookUp(
										qr.getScd(), f.getLookupQueryId());
								if (lookUp == null)
									break;
								buf.append("\",\"").append(f.getDsc())
										.append("_qw_\":\"");
								String[] objs = f.getPostProcessType() == 11 ? ((String) obj)
										.split(",") : new String[] { obj
										.toString() };
								boolean bz = false;
								for (String q : objs) {
									if (bz)
										buf.append(", ");
									else
										bz = true;
									W5LookUpDetay d = lookUp.get_detayMap()
											.get(q);
									if (d != null) {
										String s = d.getDsc();
										if (s != null) {
											s = LocaleMsgCache.get2(
														customizationId,
														xlocale, s);
											buf.append(GenericUtil
													.stringToJS2(s));
										}
									} else {
										buf.append("???: ").append(q);
									}
								}
								break;
							case 13:
							case 12:// table Lookup
								buf.append(GenericUtil.stringToJS2(obj
										.toString()));
								break;
							case	48://comment extra info
								String[] ozc = ((String) obj).split(";");//commentCount;commentUserId;lastCommentDttm;viewUserIds-msg
								int ndx = ozc[3].indexOf('-');
								buf.append(ozc[0]).append("\",\"").append(FieldDefinitions.queryFieldName_CommentExtra)
									.append("\":{\"last_dttm\":\"").append(ozc[2])
									.append("\",\"user_id\":").append(ozc[1])
									.append(",\"user_dsc\":\"").append(UserUtil.getUserDsc(GenericUtil.uInt(ozc[1])))
									.append("\",\"is_new\":").append(!GenericUtil.hasPartInside(ozc[3].substring(0,ndx), userIdStr))
									.append(",\"msg\":\"").append(GenericUtil.stringToHtml(ozc[3].substring(ndx+1)))
									.append("\"}");
								continue;
//								break;
							case 49:// approval _qw_
								String[] ozs = ((String) obj).split(";");
								int appId = GenericUtil.uInt(ozs[1]);// approvalId:
																	// kendisi
																	// yetkili
																	// ise + ,
																	// aksi
																	// halde -
								int appStepId = GenericUtil.uInt(ozs[2]);// approvalStepId
								if (appStepId != 998
										&& !GenericUtil.accessControl(
												qr.getScd(),
												(short) 1,
												ozs.length > 3 ? ozs[3] : null,
												ozs.length > 4 ? ozs[4] : null))
									buf.append("-");
								buf.append(ozs[2]);
								W5Workflow appr = FrameworkCache.getWorkflow(qr.getScd(),appId);
								String appStepDsc = "";
								if (appr != null
										&& appr.get_approvalStepMap().get(
												Math.abs(appStepId)) != null)
									appStepDsc = appr.get_approvalStepMap()
											.get(Math.abs(appStepId)).getDsc();

								buf.append("\",\"pkpkpk_arf_id\":")
										.append(ozs[0])
										.append(",\"")
										.append(f.getDsc())
										.append("_qw_\":\"")
										.append(LocaleMsgCache.get2(
												customizationId, xlocale,
												appStepDsc));
								if (ozs.length > 3 && ozs[3] != null
										&& ozs[3].length() > 0) {// roleIds
									buf.append("\",\"app_role_ids_qw_\":\"");
									String[] roleIds = ozs[3].split(",");
									Map<Integer, String> roles = FrameworkCache.wRoles.get(customizationId);
									if(roles!=null)for (String rid : roleIds) {
										buf.append(
												roles.get(
														GenericUtil.uInt(rid)) != null ? roles.get(GenericUtil
																.uInt(rid))
														: "null").append(", ");
									} else buf.append("null, ");
									buf.setLength(buf.length() - 2);
								}
								if (ozs.length > 4 && ozs[4] != null
										&& ozs[4].length() > 0) {// userIds
									buf.append("\",\"app_user_ids_qw_\":\"");
									String[] userIds = ozs[4].split(",");
									for (String uid : userIds) {
										buf.append(
												UserUtil.getUserDsc(
														
														GenericUtil.uInt(uid)))
												.append(", ");
									}
									buf.setLength(buf.length() - 2);
								}
								break;
							/*
							 * case 49://approval _qw_ buf.append(obj); int
							 * appStepId = PromisUtil.uInt(obj);
							 * buf.append("\",\""
							 * ).append(f.getDsc()).append("_qw_\":\""
							 * ).append(PromisCache
							 * .wApprovals.get(f.getLookupQueryId
							 * ()).get_approvalStepMap
							 * ().get(Math.abs(appStepId)).getDsc()); break;
							 */
						
							default:
								buf.append(GenericUtil.stringToJS2(obj
										.toString()));
							}
						buf.append("\"");

					}
					if (qr.getQuery().getShowParentRecordFlag() != 0
							&& !isMap && ((Object[])o)[((Object[])o).length - 1] != null) {
						buf.append(",\"").append(FieldDefinitions.queryFieldName_HierarchicalData).append("\":")
								.append(serializeTableHelperList(
										customizationId,
										xlocale,
										(List<W5TableRecordHelper>) ((Object[])o)[((Object[])o).length - 1]));
					}
					buf.append("}"); // satir
				}
			}
			buf.append("],\n\"pos\":")
					.append(qr.getStartRowNumber())
					.append(",\"total_count\":")
					.append(qr.getResultRowCount());
			if (FrameworkSetting.debug && qr.getExecutedSql() != null) {
				buf.append(",\n\"sql\":\"")
						.append(GenericUtil.stringToJS2(GenericUtil.replaceSql(
								qr.getExecutedSql(),
								qr.getSqlParams()))).append("\"");
			}
			if (!GenericUtil.isEmpty(qr.getExtraOutMap()))
				buf.append(",\n \"extraOutMap\":").append(
						GenericUtil.fromMapToJsonString(qr
								.getExtraOutMap()));
		} else
			buf.append(",\n\"errorType\":\"validation\",\n\"errors\":")
					.append(serializeValidatonErrors(qr.getErrorMap(),
							xlocale));

		return buf.append("}");
	}
	

	private StringBuilder serializeLookUp(W5PageResult pr) {
		StringBuilder buf2 = new StringBuilder();
		boolean b = false;
		if(!GenericUtil.isEmpty(pr.getPage().get_pageObjectList()))for(W5PageObject po :pr.getPage().get_pageObjectList()) if(po.getObjectType()==6){
			b = true;
			break;			
		}
		if(b) {
			Set<Integer> ls = new HashSet();
			
			if(!GenericUtil.isEmpty(pr.getPage().get_pageObjectList()))for(W5PageObject po :pr.getPage().get_pageObjectList()) if(po.getObjectType()==6){
				ls.add(po.getObjectId());
			}
			if(!ls.isEmpty()) {
				buf2.append("var _lookUps={");
				boolean b2 = false;
				for (Integer lookUpId : ls) {
					W5LookUp lu = FrameworkCache.getLookUp(
							pr.getScd(), lookUpId);
					if(lu==null)continue;
					if (b2)
						buf2.append(",\n");
					else
						b2 = true;
					buf2.append(lu.getDsc()).append(":[");
					Map<String, String> tempMap = new HashMap<String, String>();
					boolean b3=false;
					for (W5LookUpDetay lud : lu.get_detayList()) if(lud.getActiveFlag()!=0){
						if (b3)
							buf2.append(", ");
						else
							b3 = true;
						buf2.append("{\"").append(lud.getVal()).append("\":\"").append(LocaleMsgCache
										.get2(pr.getScd(),lud.getDsc())).append("\"");
						if(!GenericUtil.isEmpty(lud.getParentVal()))
							buf2.append(",\"extra\":\"").append(lud.getParentVal()).append("\"");
						buf2.append("}");
						
					}
					buf2.append("]");
				}
				buf2.append("};\n");
			}
			
		}
		return buf2;
	}

	public StringBuilder serializeTemplate(W5PageResult pr) {
		boolean replacePostJsCode = false;
		W5Page page = pr.getPage();

		StringBuilder buf = new StringBuilder();
		StringBuilder postBuf = new StringBuilder();
		String code = null;
		int customizationId = (Integer) pr.getScd().get(
				"customizationId");
		String xlocale = (String) pr.getScd().get("locale");
		if (page.getPageType() != 0) { // not html 
			// notification Control
			// masterRecord Control
			
			if(pr.getRequestParams()!=null) {

				// request
				buf.append("var _request=")
						.append(GenericUtil.fromMapToJsonString(pr
								.getRequestParams())).append("\n");
				if (pr.getRequestParams().get("_tabId") != null)
					buf.append("var _page_tab_id='")
							.append(pr.getRequestParams().get("_tabId"))
							.append("';\n");
				else {
					buf.append("var _page_tab_id='")
							.append(GenericUtil.getNextId("tpi")).append("';\n");
				}
				
				buf.append(serializeLookUp(pr));
				
				if(!GenericUtil.isEmpty(pr.getPage().getCssCode()) && pr.getPage().getCssCode().trim().length()>3){
					buf.append("iwb.addCssString(\"")
					.append(GenericUtil.stringToJS2(pr.getPage().getCssCode().trim())).append("\",").append(pr.getPageId()).append(");\n");
				}
			}
			
			if(pr.getPageObjectList()!=null) { // has detail list
				for (Object i : pr.getPageObjectList()) if(i instanceof W5Component){
					W5Component c = (W5Component)i;
					buf.append("\nvar ").append(c.getDsc()).append("= React.lazy(()=>iwb.import('comp/").append(c.getComponentId()).append(".js?.x='));\n");
					if(!GenericUtil.isEmpty(c.getCssCode()))buf.append("\n iwb.addCss('comp/").append(c.getComponentId()).append(".css?.x=',").append(c.getComponentId()).append(");\n");
					
				}

				int customObjectCount = 1, tabOrder = 1;
				for (Object i : pr.getPageObjectList()) {
					if (i instanceof W5GridResult) { // objectType=1
						W5GridResult gr = (W5GridResult) i;
						buf.append(serializeGrid(gr));
						buf.append("\n").append(gr.getGrid().getDsc())
								.append(".tabOrder=").append(tabOrder++); // template
																			// grid
																			// sirasi
																			// icin.
						if (gr.getGridId() < 0) {
							buf.append("\nvar _grid")
									.append(customObjectCount++).append("=")
									.append(gr.getGrid().getDsc()).append("\n");
						}
						// if(replacePostJsCode)
					} else if (i instanceof W5CardResult) {// objectType=2
						W5CardResult dr = (W5CardResult) i;
						buf.append(serializeCard(dr));
						if (dr.getCardId() < 0) {
							buf.append("\nvar _card")
									.append(customObjectCount++).append("=")
									.append(dr.getCard().getDsc())
									.append("\n");
						}
					} else if (i instanceof W5ListViewResult) {// objectType=7
						W5ListViewResult lr = (W5ListViewResult) i;
						buf.append(serializeListView(lr));
						if (lr.getListId() < 0) {
							buf.append("\nvar _listView")
									.append(customObjectCount++).append("=")
									.append(lr.getListView().getDsc())
									.append("\n");
						}
					} else if (i instanceof W5FormResult) {// objectType=3
						W5FormResult fr = (W5FormResult) i;
						if (Math.abs(fr.getObjectType()) == 3) { // form
							buf.append("\nclass ").append(fr.getForm().getDsc())
									.append(" extends XForm").append(serializeGetForm(fr));
						}
						if (fr.getFormId() < 0) {
							buf.append("\nvar _form")
									.append(customObjectCount++).append("=")
									.append(fr.getForm().getDsc()).append("\n");
						}
					} else if (i instanceof W5GlobalFuncResult) {
						buf.append("\nvar ")
						.append(((W5GlobalFuncResult) i).getGlobalFunc()
								.getDsc()).append("=")
						.append(serializeGlobalFunc((W5GlobalFuncResult) i))
						.append("\n");
					}else if (i instanceof W5PageResult) {
						buf.append("\nvar ")
						.append(((W5PageResult) i).getPage()
								.getDsc()).append("=function(props2){\n")
						.append(serializeTemplate((W5PageResult) i))
						.append("\n}\n");
					} else if (i instanceof W5QueryResult) { // query, badge&&gauge
						W5Query q = ((W5QueryResult)i).getQuery();
						W5PageObject orjPageObject = null;
						for (W5PageObject o : page.get_pageObjectList())if(o.getObjectId()==q.getQueryId()) {
							orjPageObject = o;
							break;
						}
						if(q.getQueryType()==22 || q.getQueryType()==21) { //gauge && badge
							if(pr.getPage().getPageType()==2) {//page
								//find the origin pageObject
								if(orjPageObject!=null && orjPageObject.getParentObjectId()!=0) for (W5PageObject o : page.get_pageObjectList())if(orjPageObject.getParentObjectId()==o.getPageObjectId()){
									if(o.getObjectType()==1) {//grid
										for (Object ix : pr.getPageObjectList()) if(ix!=null && ix instanceof W5GridResult && ((W5GridResult)ix).getGridId()==o.getObjectId()){
											String grName = ((W5GridResult)ix).getGrid().getDsc();
											buf.append("\n if(!").append(grName).append(".summary)")
												.append(grName).append(".summary=[];\n ").append(grName).append(".summary.push(").append(q.getQueryId()).append(");\n");
											break;										
										}
										
									}
									break;
									
								}
							} else buf.append("\nvar ")
							.append(((W5QueryResult) i).getQuery().getDsc())
							.append("=").append(serializeQueryData((W5QueryResult) i));
						} else if(q.getQueryType()==15) { //Graph Query 
							buf.append("\nvar ")
							.append(q.getDsc())
							.append("={name:'").append(GenericUtil.stringToJS(LocaleMsgCache.get2(pr.getScd(), q.getDsc()))).append("', graphTip:").append(orjPageObject.getParentObjectId()).append(",queryId:").append(q.getQueryId()).append(serializeQueryReader(q.get_queryFields(), pr.getScd()))
								.append("}\n");
						} else //queryResult
							buf.append("\nvar ")
							.append(((W5QueryResult) i).getQuery().getDsc())
							.append("=").append(serializeQueryData((W5QueryResult) i))
								.append("\n");
					}  else if (i instanceof W5BIGraphDashboard) {
						W5BIGraphDashboard gd = (W5BIGraphDashboard) i;
						buf.append("\nvar graph")
								.append(gd.getGraphDashboardId())
								.append("=")
								.append(serializeGraphDashboard(gd, pr.getScd()))
								.append(";\n");
						
						W5PageObject orjPageObject = null;
						for (W5PageObject o : page.get_pageObjectList())if(o.getObjectId()==gd.getGraphDashboardId() && o.getObjectType()==9) {
							orjPageObject = o;
							break;
						}
						if(orjPageObject!=null && orjPageObject.getParentObjectId()!=0) for (W5PageObject o : page.get_pageObjectList())if(orjPageObject.getParentObjectId()==o.getPageObjectId()){
							if(o.getObjectType()==1) {//grid
								for (Object ix : pr.getPageObjectList()) if(ix!=null && ix instanceof W5GridResult && ((W5GridResult)ix).getGridId()==o.getObjectId()){
									String grName = ((W5GridResult)ix).getGrid().getDsc();
									buf.append("\n if(!").append(grName).append(".summary)")
										.append(grName).append(".summary=[];\n ").append(grName).append(".summary.push(graph").append(gd.getGraphDashboardId()).append(");\n");
									break;										
								}								
							}
							break;							
						}
						
					} else if (i instanceof String) {
						buf.append("\nvar ").append(i).append("={}");
					}
					buf.append("\n");
				}
			}
			
			if (replacePostJsCode) {

			} else
				code = page.getCode();
		} else {//html
			StringBuilder buf2 = new StringBuilder();
			buf2.append("var _webPageId='").append(GenericUtil.getNextId("wpi"))
					.append("';\nvar _page_tab_id='")
					.append(GenericUtil.getNextId("tpi")).append("';\n");
			buf2.append("var _request=")
					.append(GenericUtil.fromMapToJsonString(pr
							.getRequestParams())).append(";\n");
			buf2.append("var _scd=")
					.append(GenericUtil.fromMapToJsonString(pr
							.getScd())).append(";\n");
			buf2.append("iwb.dateFormat = '").append(dateFormatMulti[GenericUtil.uInt(pr.getScd().get("date_format"))]).append("';\n");
			Map<String, String> publishedAppSetting = new HashMap<String, String>();
			for (String key : FrameworkCache.publishAppSettings) {
				publishedAppSetting.put(
						key,
						FrameworkCache.getAppSettingStringValue(
								pr.getScd(), key));
			}
			buf2.append("var _app=")
					.append(GenericUtil.fromMapToJsonString(publishedAppSetting))
					.append(";\n");

			buf2.append(serializeLookUp(pr));

			int customObjectCount=1;
			for (Object i : pr.getPageObjectList()) {
				if (i instanceof W5GridResult) {
					W5GridResult gr = (W5GridResult) i;
					buf2.append(serializeGrid(gr));
					buf2.append("\nvar _grid")
					.append(customObjectCount++).append("=")
					.append(gr.getGrid().getDsc()).append(";\n");
				} else if (i instanceof W5CardResult) {// objectType=2
					W5CardResult dr = (W5CardResult) i;
					buf2.append(serializeCard(dr));
				} else if (i instanceof W5ListViewResult) {// objectType=7
					W5ListViewResult lr = (W5ListViewResult) i;
					buf2.append(serializeListView(lr));
				} else if (i instanceof W5FormResult) {
					W5FormResult fr = (W5FormResult) i;
					if (Math.abs(fr.getObjectType()) == 3) { // form
						buf2.append("\nclass ").append(fr.getForm().getDsc())
								.append(" extends XForm").append(serializeGetForm(fr));
					}
				} else if (i instanceof W5GlobalFuncResult) {
					buf2.append("\nvar ")
							.append(((W5GlobalFuncResult) i).getGlobalFunc()
									.getDsc()).append("=")
							.append(serializeGlobalFunc((W5GlobalFuncResult) i))
							.append(";\n");
				} else if (i instanceof W5QueryResult) {
					buf2.append("\nvar ")
							.append(((W5QueryResult) i).getQuery().getDsc())
							.append("=")
							.append(serializeQueryData((W5QueryResult) i))
							.append(";\n");
				} else if (i instanceof W5BIGraphDashboard) {
					W5BIGraphDashboard gd = (W5BIGraphDashboard) i;
					buf2.append("\nvar graph")
							.append(gd.getGraphDashboardId())
							.append("=")
							.append(serializeGraphDashboard(gd, pr.getScd()))
							.append(";\n");
				} else if (i instanceof String) {
					buf2.append("\nvar ").append(i).append("={};");
				} else if(i instanceof W5PageResult) {
					W5PageResult pr2 = (W5PageResult)i;
					buf2.append("\nvar myDashboard=function(xprops){\n")
					.append(serializeTemplate(pr2))
					.append("\n};\n");
				}
				buf2.append("\n");
			}
			StringBuilder buf3 = new StringBuilder();
			buf3.append("var _localeMsg=")
					.append(GenericUtil.fromMapToJsonString(LocaleMsgCache
							.getPublishLocale2(customizationId, pr
									.getScd().get("locale").toString())))
					.append("\n");
			// buf3.append("function getLocMsg(key){if(key==null)return '';var val=_localeMsg[key];return val || key;}\n");
//			buf3.append("var _CompanyLogoFileId=1;\n");
			code = page.getCode().replace("${promis}", buf2.toString())
					.replace("${localemsg}", buf3.toString());
			if (page.getCode().contains("${promis-css}")) {
				StringBuilder buf4 = new StringBuilder();

				if(!GenericUtil.isEmpty(page.getCssCode()) && page.getCssCode().trim().length()>3){
					buf4.append(page.getCssCode()).append("\n");
				}
				/*W5LookUp c = FrameworkCache.getLookUp(pr.getScd(), 665);
				if(c!=null)for (W5LookUpDetay d : c.get_detayList()) {
					buf4.append(".bgColor")
							.append(d.getVal().replace("#", ""))
							.append("{background-color:")
							.append(d.getVal()).append(";}\n");
				}*/
				FrameworkCache.addPageResource(pr.getScd(), "css-"+page.getPageId(), buf4.toString());
				code = code.replace("${promis-css}", " <link rel=\"stylesheet\" type=\"text/css\" href=\"dyn-res/css-"+page.getPageId()+".css?.x="+page.getVersionNo()+"\" />");

			}
			
			if(page.getCode().contains("${components}")) {
				StringBuilder bufc = new StringBuilder();

				for (Object i : pr.getPageObjectList()) if(i instanceof W5Component){
					W5Component c = (W5Component)i;
					bufc.append("<script src=\"comp/").append(c.getComponentId()).append(".js?.x=\"></script>;\n");
					if(!GenericUtil.isEmpty(c.getCssCode()))bufc.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"comp/").append(c.getComponentId()).append(".css?.x=\"/>;\n");
					
				}
				code = code.replace("${components}", bufc.toString());
			}
			
		}
		/*
		 * if(templateResult.getTemplateId()==2){ // ana sayfa Map<String,
		 * String> m = new HashMap<String, String>(); int customizationId =
		 * PromisUtil.uInt(templateResult.getScd().get("customizationId"));
		 * for(String key:PromisCache.publishAppSettings) m.put(key,
		 * PromisCache.getAppSettingStringValue(customizationId, key));
		 * buf.append
		 * ("var appSetting =").append(PromisUtil.fromMapToJsonString(m));
		 * 
		 * }
		 */
		if(!GenericUtil.isEmpty(code))
			buf.append("\n").append(code.startsWith("!") ? code.substring(1) : code);

		if(!GenericUtil.isEmpty(pr.getPageObjectList()))switch(pr.getPage().getPageType()){
		case	2:case	4://page, pop up
			buf.append("\n").append(renderTemplateObject(pr));
			break;
		case	10://dashboard
			buf.append("\n").append(renderDashboardObject(pr));
			break;
			
		}
		
		return page.getLocaleMsgFlag() != 0 ? GenericUtil.filterExt(
				buf.toString(), pr.getScd(),
				pr.getRequestParams(), null) : buf;
	}
	private StringBuilder serializeGraphDashboard(W5BIGraphDashboard gd, Map<String, Object> scd){
		StringBuilder buf = new StringBuilder();
		buf.append("{graphId:").append(gd.getGraphDashboardId())
		 .append(",name:'").append(LocaleMsgCache.get2(scd, gd.getLocaleMsgKey())).append("', gridId:").append(gd.getGridId()).append(",tableId:").append(gd.getTableId())
		 .append(",is3d:").append(gd.getIs3dFlag()!=0).append(",dtTip:").append(gd.getDtTip())
		.append(",graphTip:").append(gd.getGraphTip()).append(",groupBy:'").append(gd.getGraphGroupByField()).append("',funcTip:").append(gd.getGraphFuncTip()).append(",funcFields:'").append(gd.getGraphFuncFields())
		.append("', queryParams:").append(gd.getQueryBaseParams());
		if(gd.getStackedQueryField()!=0)buf.append(",stackedFieldId:").append(gd.getStackedQueryField());
		if(gd.getDefaultHeight()!=0)buf.append(",height:").append(gd.getDefaultHeight());
		if(gd.getLegendFlag()!=0)buf.append(",legend:true");
		if(gd.getTableId()==0 || FrameworkCache.getTable(scd, gd.getTableId())==null)buf.append(",query:true");
		buf.append("}");
		return buf;
	}
	
	private Object renderDashboardObject(W5PageResult pr) {
		StringBuilder buf = new StringBuilder();
		if(GenericUtil.isEmpty(pr.getPageObjectList()))return buf;
		buf.append("return _(XDashboard,{t:_page_tab_id, rows:[");
		int rowId=-1;
		for(Object o:pr.getPageObjectList())if(o!=null){
			W5PageObject po = null;
			StringBuilder rbuf = new StringBuilder();
			if(o instanceof W5GridResult){
				W5GridResult gr = (W5GridResult)o;
				po = gr.getTplObj();
				rbuf.append("{grid:").append(gr.getGrid().getDsc());
				
			} else if(o instanceof W5BIGraphDashboard){
				W5BIGraphDashboard gr = (W5BIGraphDashboard)o;
				rbuf.append("{graph:graph").append(gr.getGraphDashboardId());
				for(W5PageObject po2:pr.getPage().get_pageObjectList())if(po2.getObjectId()==gr.getGraphDashboardId()){
					po = po2;
					break;
				}
			} else if(o instanceof W5QueryResult){
				W5QueryResult qr = (W5QueryResult)o;
				for(W5PageObject po2:pr.getPage().get_pageObjectList())if(po2.getObjectId()==qr.getQueryId()){
					po = po2;
					break;
				}
				switch(po.getObjectType()) {
				case 15://graphic from raw query
					rbuf.append("{gquery:").append(qr.getQuery().getDsc());
					break;
				case 10://badge
					rbuf.append("{query:").append(qr.getQuery().getDsc());
					break;
				case 22://gauge
					rbuf.append("{gauge:").append(qr.getQuery().getQueryId());
					break;
				}
			}else if(o instanceof W5PageResult){
				W5PageResult pr2 = (W5PageResult)o;
				rbuf.append("{page:").append(pr2.getPage().getDsc());
				for(W5PageObject po2:pr.getPage().get_pageObjectList())if(po2.getObjectId()==pr2.getPageId()){
					po = po2;
					break;
				}
			} else if(o instanceof W5CardResult){
				W5CardResult cr = (W5CardResult)o;
				rbuf.append("{card:").append(cr.getCard().getDsc());
				for(W5PageObject po2:pr.getPage().get_pageObjectList())if(po2.getObjectId()==cr.getCardId()){
					po = po2;
					break;
				}
//				po = cr.getTplObj();TODO
			}
			if(po!=null){
				int currentRowID = po.getTabOrder()/1000;
				if(currentRowID!=rowId){
					if(rowId>-1){
						buf.append("],");
					}
					buf.append("[");
				}
				if(!GenericUtil.isEmpty(po.getPostJsCode())){
					rbuf.append(",props:{").append(po.getPostJsCode()).append("}");
				}
				rbuf.append("}");
				if(rowId == currentRowID)buf.append(",");
				buf.append(rbuf);
				rowId= currentRowID;
			}
		}
		if(rowId!=-1)buf.append("]");
		buf.append("]});");
		return buf;
	}

	public StringBuilder serializeTableRecordInfo(
			W5TableRecordInfoResult tableRecordInfoResult) {
		Map<String, Object> scd = tableRecordInfoResult.getScd();
		String xlocale = (String) scd.get("locale");
		int customizationId = (Integer) scd.get(
				"customizationId");
		StringBuilder buf = new StringBuilder();
		W5TableRecordHelper trh0 = tableRecordInfoResult.getParentList().get(0);
		buf.append("{\"success\":true,\"tableId\":")
				.append(tableRecordInfoResult.getTableId())
				.append(",\"tablePk\":")
				.append(tableRecordInfoResult.getTablePk())
				.append(",\"tdsc\":\"")
				.append(LocaleMsgCache.get2(customizationId, xlocale,
						FrameworkCache
								.getTable(scd, trh0.getTableId())
								.getDsc())).append("\",\"dsc\":\"")
				.append(GenericUtil.stringToJS2(trh0.getRecordDsc()))
				.append("\"");
		if (tableRecordInfoResult.getInsertUserId() > 0)
			buf.append(",\nprofile_picture_id:").append(
					UserUtil.getUserProfilePicture(
							tableRecordInfoResult.getInsertUserId()));
		if (!GenericUtil.isEmpty(tableRecordInfoResult.getVersionDttm())) {
			buf.append(",\n\"version_no\":")
					.append(tableRecordInfoResult.getVersionNo())
					.append(",\"insert_user_id\":")
					.append(tableRecordInfoResult.getInsertUserId())
					.append(",\"insert_user_id_qw_\":\"")
					.append(UserUtil.getUserDsc(tableRecordInfoResult.getInsertUserId()))
					.append("\",\"insert_dttm\":\"")
					.append(tableRecordInfoResult.getInsertDttm())
					.append("\",\"version_user_id\":")
					.append(tableRecordInfoResult.getVersionUserId())
					.append(",\"version_user_id_qw_\":\"")
					.append(UserUtil.getUserDsc(tableRecordInfoResult.getVersionUserId()))
					.append("\",\"version_dttm\":\"")
					.append(tableRecordInfoResult.getVersionDttm())
					.append("\"");
		}
		if (tableRecordInfoResult.getFileAttachmentCount() != -1)
			buf.append(",\nfileAttachFlag:true, fileAttachCount:").append(
					tableRecordInfoResult.getFileAttachmentCount());
		if (tableRecordInfoResult.getCommentCount() != -1)
			buf.append(",\ncommentFlag:true, commentCount:").append(
					tableRecordInfoResult.getCommentCount());
		
		if (tableRecordInfoResult.getAccessControlCount() != -1)
			buf.append(",\naccessControlFlag:true, accessControlCount:")
					.append(tableRecordInfoResult.getAccessControlCount());
		if (tableRecordInfoResult.getFormMailSmsCount() > 0)
			buf.append(",\nformSmsMailCount:").append(
					tableRecordInfoResult.getFormMailSmsCount());
		if (tableRecordInfoResult.getConversionCount() > 0)
			buf.append(",\nconversionCount:").append(
					tableRecordInfoResult.getConversionCount());

		buf.append(",\n\"parents\":[");// TODO: burda aradan 1 gunluk bir zaman
										// varsa hic dikkate alma denilebilir
		boolean b = false;
		for (W5TableRecordHelper trh : tableRecordInfoResult.getParentList()) {
			W5Table dt = FrameworkCache
					.getTable(scd, trh.getTableId());
			if (dt == null)
				break;
			if (b)
				buf.append(",\n");
			else
				b = true;
			buf.append("{\"tid\":")
					.append(trh.getTableId())
					.append(",\"tpk\":")
					.append(trh.getTablePk())
					.append(",\"tdsc\":\"")
					.append(LocaleMsgCache.get2(customizationId, xlocale,
							dt.getDsc())).append("\",\"dsc\":\"")
					.append(GenericUtil.stringToJS2(trh.getRecordDsc()))
					.append("\"");
			if (dt.getMakeCommentFlag() != 0 && trh.getCommentCount() > 0)
				buf.append(",\"tcc\":").append(trh.getCommentCount());
			buf.append("}");

		}
		b = false;
		buf.append("]");
		if (!GenericUtil.isEmpty(tableRecordInfoResult.getChildList())) {
			buf.append(",\n\"childs\":[");
			for (W5TableChildHelper tch : tableRecordInfoResult.getChildList())
				if (tch.getChildCount() > 0) {
					W5Table dt = FrameworkCache.getTable(scd, tch
							.getTableChild().getRelatedTableId());
					if (dt == null)
						break;
					if (b)
						buf.append(",\n");
					else
						b = true;
					buf.append("{\"tid\":")
							.append(dt.getTableId())
							.append(",\"tdsc\":\"")
							.append(LocaleMsgCache.get2(customizationId,
									xlocale, dt.getDsc())).append("\",\"tc\":")
							.append(tch.getChildCount());
					if (dt.getMakeCommentFlag() != 0)
						buf.append(",\"tcc\":").append(
								tch.getTotalCommentCount());
					if (dt.getFileAttachmentFlag() != 0)
						buf.append(",\"tfc\":").append(
								tch.getTotalFileAttachmentCount());
					if (tch.getTableChild().getChildViewTip() > 0) {
						buf.append(",\"vtip\":")
								.append(tch.getTableChild().getChildViewTip())
								.append(",\"void\":")
								.append(tch.getTableChild()
										.getChildViewObjectId());
					}
					buf.append(",\"rel_id\":")
							.append(tch.getTableChild().getTableChildId())
							.append(",\"mtbid\":")
							.append(tableRecordInfoResult.getTableId())
							.append(",\"mtbpk\":")
							.append(tableRecordInfoResult.getTablePk())
							.append("}");
				}
			buf.append("]");
		}
		buf.append("}");
		return buf;
	}

	public StringBuilder serializeGlobalFunc(W5GlobalFuncResult dbFuncResult) {
		String xlocale = (String) dbFuncResult.getScd().get("locale");
		StringBuilder buf = new StringBuilder();
		buf.append("{\"success\":").append(dbFuncResult.isSuccess())
				.append(",\"db_func_id\":").append(dbFuncResult.getGlobalFuncId());
		if (dbFuncResult.getErrorMap() != null
				&& dbFuncResult.getErrorMap().size() > 0)
			buf.append(",\n\"errorType\":\"validation\",\n\"errors\":").append(
					serializeValidatonErrors(dbFuncResult.getErrorMap(),
							xlocale));
		else if (dbFuncResult.getResultMap() != null)
			buf.append(",\n\"result\":")
					.append(GenericUtil.fromMapToJsonString2(dbFuncResult
							.getResultMap()));
		else if (dbFuncResult.getRequestParams().get("perror_msg") != null)
			buf.append(",\n\"errorMsg\":\"")
					.append(GenericUtil.stringToJS(dbFuncResult
							.getRequestParams().get("perror_msg")))
					.append("\"");
		buf.append("}");
		return buf;
	}

	public StringBuilder serializeFeeds(Map<String, Object> scd,
			int platestFeedIndex, int pfeedTip, int proleId, int puserId,
			int pmoduleId) {
		StringBuilder buf = new StringBuilder(512);
		String xlocale = (String) scd.get("locale");
		int customizationId = (Integer) scd.get("customizationId");
		long currentTime = System.currentTimeMillis();
		// sorunlar
		// 1. ayni tipte bir islem varsa az once (edit, comment, file attach,
		// detaya crud, ...)
		// 2. security
		// 3. detay'da
		List<Log5Feed> lall = FrameworkCache.wFeeds.get(customizationId);
		if (lall == null)
			return buf
					.append("{\"success\":true,\"data\":[],\"browseInfo\":{\"startRow\":0,\"fetchCount\":0,\"totalCount\":0}}");
		int maxDerinlik = FrameworkCache.getAppSettingIntValue(scd,
				"feed_control_depth");
		int maxFeedCount = FrameworkCache.getAppSettingIntValue(scd,
				"feed_record_per_page");
		int qj = lall.size(), feedCount = 0;
		int userTip = ((Integer) scd.get("userTip"));
		buf.append("{\"success\":true,\"latest_feed_index\":").append(qj);
		if (lall == null || qj - 1 <= platestFeedIndex)
			return buf.append("}");
		Map<Integer, Log5Feed> relatedFeedMap = new HashMap<Integer, Log5Feed>();
		if (platestFeedIndex < 0)
			platestFeedIndex = -1;
		if (qj - platestFeedIndex > maxDerinlik)
			platestFeedIndex = qj - maxDerinlik;
		buf.append(",\"data\":[");
		for (int qi = qj - 1; qi > platestFeedIndex && feedCount < maxFeedCount; qi--) {
			Log5Feed feed = lall.get(qi);
			if (feed == null)
				continue;
			if (userTip != feed.getInsertUserTip())
				continue;
			if (pfeedTip != -1 && pfeedTip != feed.getFeedTip())
				continue;
			if (proleId != -1 && proleId != feed.getInsertRoleId())
				continue;
			if (relatedFeedMap.containsKey(feed.getFeedId()))
				continue;
			W5Table t = FrameworkCache.getTable(scd, feed.getTableId());
		

			if (t != null)
				switch (t.getAccessViewTip()) {
				case 0:
				
					break;
				default:
					if (!GenericUtil.accessControl(scd, t.getAccessViewTip(),
							t.getAccessViewRoles(), t.getAccessViewUsers()))
						continue;
				}
			if (feed.get_viewAccessControl() != null
					&& !GenericUtil.accessControl(scd, (short) 1, feed
							.get_viewAccessControl().getAccessRoles(), feed
							.get_viewAccessControl().getAccessUsers())) {
				continue;
			}
			if (t != null && feed.get_tableRecordList() != null) {
				boolean bcont = false;
				for (W5TableRecordHelper trh : feed.get_tableRecordList())
					if (bcont)
						break;
					else if (t.getTableId() != trh.getTableId()) {
						W5Table tx = FrameworkCache
								.getTable(scd, trh.getTableId());
						if (tx != null)
							switch (tx.getAccessViewTip()) {
							case 0:
								if (!FrameworkCache.roleAccessControl(scd,
										0)) {
									bcont = true;
									continue;
								}
								break;
							default:
								if (!GenericUtil.accessControl(scd,
										tx.getAccessViewTip(),
										tx.getAccessViewRoles(),
										tx.getAccessViewUsers())) {
									bcont = true;
									continue;
								}
							}
						if (trh.getViewAccessControl() != null
								&& !GenericUtil.accessControl(scd, (short) 1,
										trh.getViewAccessControl()
												.getAccessRoles(), trh
												.getViewAccessControl()
												.getAccessUsers())) {
							bcont = true;
							break;
						}
					}
				if (bcont)
					continue;
			}
			if (puserId != -1 && feed.getInsertUserId() != puserId) {// spesifik
																		// bir
																		// user
																		// icin
				if (feed.get_relatedFeedMap() == null)
					continue;
				boolean bx = true;
				for (Integer k : feed.get_relatedFeedMap().keySet())
					if (feed.get_relatedFeedMap().get(k).getInsertUserId() == puserId) {
						bx = false;
						break;
					}
				if (bx)
					continue;
			}
			if (feedCount > 0)
				buf.append(",\n");
			feedCount++;
			if (feed.get_relatedFeedMap() != null)
				relatedFeedMap.putAll(feed.get_relatedFeedMap());

			// lnew.add(feed);
			buf.append("{\"feed_id\":")
					.append(feed.getFeedId())
					.append(",\"tid\":")
					.append(feed.getTableId())
					.append(",\"tpk\":")
					.append(feed.getTablePk())
					.append(",\"tcc\":")
					.append(feed.get_commentCount())
					.append(",\"insert_time\":")
					.append(currentTime - feed.get_insertTime())
					.append(",\"user_id\":")
					.append(feed.getInsertUserId())
					.append(",\"user_id_qw_\":\"")
					.append(UserUtil.getUserDsc(feed.getInsertUserId()))
					.append("\",\"profile_picture_id\":")
					.append(UserUtil.getUserProfilePicture(
							feed.getInsertUserId()))
					.append(",\"show_feed_tip\":")
					.append(feed.get_showFeedTip())
					// 1:detail, else main
					.append(",\"feed_tip\":")
					.append(feed.getFeedTip())
					.append(",\"feed_tip_qw_\":\"")
					.append(LocaleMsgCache.get2(
							customizationId,
							xlocale,
							FrameworkCache
									.getLookUp(scd, 563)
									.get_detayMap()
									.get(new Integer(feed.getFeedTip())
											.toString()).getDsc()))
					.append("\"");
			if (feed.get_relatedFeedMap() != null) {
				Set<Integer> relatedUsers = new HashSet<Integer>();
				relatedUsers.add(feed.getInsertUserId());
				for (Integer k : feed.get_relatedFeedMap().keySet())
					relatedUsers.add(feed.get_relatedFeedMap().get(k)
							.getInsertUserId());
				if (relatedUsers.size() > 1) {
					relatedUsers.remove(feed.getInsertUserId());
					buf.append(",\"related_users\":[");// TODO: burda aradan 1
														// gunluk bir zaman
														// varsa hic dikkate
														// alma denilebilir
					boolean b = false;
					for (Integer k : relatedUsers) {
						if (b)
							buf.append(",");
						else
							b = true;
						buf.append("\"")
								.append(UserUtil.getUserDsc( k))
								.append("\"");
					}
					buf.append("]");
				}
			}
			if (t != null && feed.get_tableRecordList() != null) {
				buf.append(",\"record\":[");// TODO: burda aradan 1 gunluk bir
											// zaman varsa hic dikkate alma
											// denilebilir
				boolean b = false;
				for (W5TableRecordHelper trh : feed.get_tableRecordList()) {
					W5Table dt = FrameworkCache.getTable(scd, trh.getTableId());
					if (dt == null)
						break;
					if (b)
						buf.append(",");
					else
						b = true;
					buf.append("{\"tid\":")
							.append(trh.getTableId())
							.append(",\"tpk\":")
							.append(trh.getTablePk())
							.append(",\"tcc\":")
							.append(trh.getCommentCount())
							.append(",\"tdsc\":\"")
							.append(LocaleMsgCache.get2(customizationId,
									xlocale, dt.getDsc()))
							.append("\",\"dsc\":\"")
							.append(GenericUtil.stringToJS2(trh.getRecordDsc()))
							.append("\"}");

				}
				buf.append("]");
			}
			if (feed.get_tableCommentList() != null) {
				buf.append(",\"comments\":[");// TODO: burda aradan 1 gunluk bir
												// zaman varsa hic dikkate alma
												// denilebilir
				boolean b = false;
				for (W5CommentHelper ch : feed.get_tableCommentList()) {
					if (b)
						buf.append(",");
					else
						b = true;
					buf.append("{\"insert_time\":")
							.append(currentTime - ch.getInsertTime())
							.append(",\"user_id\":")
							.append(ch.getInsertUserId())
							.append(",\"user_id_qw_\":\"")
							.append(UserUtil.getUserDsc(ch.getInsertUserId()))
							.append("\",\"dsc\":\"")
							.append(GenericUtil.stringToJS(ch.getDsc()))
							.append("\"}");

				}
				buf.append("]");
			}
			buf.append("}");
		}
		buf.append("]");
		/*
		 * if(!relatedFeedMap.isEmpty()){//TODO simdilik full yuklenecegi icin
		 * sorun yok buf.append("\n,\"related_feed_ids\":["); boolean b= false;
		 * for(Integer k:relatedFeedMap.keySet()){//TODO: burda aradan 1 gunluk
		 * bir zaman varsa hic dikkate alma denilebilir if(b)buf.append(",");
		 * else b=true; buf.append(k);
		 * 
		 * } buf.append("]"); }
		 */
		return buf.append(",\n\"browseInfo\":{\"startRow\":0,\"fetchCount\":")
				.append(feedCount).append(",\"totalCount\":").append(feedCount)
				.append("}}");
	}

	private StringBuilder serializeManualConversions(Map scd,
			List<W5Conversion> l) {
		StringBuilder s = new StringBuilder();
		int customizationId = (Integer) scd.get("customizationId");
		boolean b = false;
		for (W5Conversion fsm : l)
			if (GenericUtil.hasPartInside2(fsm.getActionTypes(), 0)) { // manuel
																		// icin
																		// var
																		// mi
				W5Table dt = FrameworkCache.getTable(scd,
						fsm.getDstTableId());
				if ((dt.getAccessViewTip() == 0
						|| !GenericUtil.isEmpty(dt.getAccessUpdateUserFields()) || GenericUtil
							.accessControl(scd, dt.getAccessViewTip(),
									dt.getAccessViewRoles(),
									dt.getAccessViewUsers()))
						&& GenericUtil.accessControl(scd,
								dt.getAccessInsertTip(),
								dt.getAccessInsertRoles(),
								dt.getAccessInsertUsers())) {
					if (b)
						s.append("\n,");
					else
						b = true;
					s.append("{xid:")
							.append(fsm.getConversionId())
							.append(",_fid:")
							.append(fsm.getDstFormId())
							.append(",preview:")
							.append(fsm.getPreviewFlag() != 0)
							.append(",text:\"")
							.append(GenericUtil.stringToJS(LocaleMsgCache.get2(
									customizationId, scd.get("locale")
											.toString(), fsm.getDsc())))
							.append("\"}");
				}
			}
		return s;
	}
	public	StringBuilder serializeShowForm2(W5FormResult formResult) {return null;}

}
