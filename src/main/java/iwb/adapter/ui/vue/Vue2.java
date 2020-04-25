package iwb.adapter.ui.vue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import iwb.adapter.ui.ViewAdapter;
import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.domain.db.Log5Feed;
import iwb.domain.db.W5BIGraphDashboard;
import iwb.domain.db.W5Card;
import iwb.domain.db.W5Conversion;
import iwb.domain.db.W5ConvertedObject;
import iwb.domain.db.W5Detay;
import iwb.domain.db.W5Form;
import iwb.domain.db.W5FormCell;
import iwb.domain.db.W5FormModule;
import iwb.domain.db.W5FormSmsMail;
import iwb.domain.db.W5FormSmsMailAlarm;
import iwb.domain.db.W5Grid;
import iwb.domain.db.W5GridColumn;
import iwb.domain.db.W5List;
import iwb.domain.db.W5ListColumn;
import iwb.domain.db.W5LookUp;
import iwb.domain.db.W5LookUpDetay;
import iwb.domain.db.W5ObjectMenuItem;
import iwb.domain.db.W5ObjectToolbarItem;
import iwb.domain.db.W5Page;
import iwb.domain.db.W5PageObject;
import iwb.domain.db.W5Query;
import iwb.domain.db.W5QueryField;
import iwb.domain.db.W5QueryParam;
import iwb.domain.db.W5Table;
import iwb.domain.db.W5TableField;
import iwb.domain.db.W5Workflow;
import iwb.domain.helper.W5CommentHelper;
import iwb.domain.helper.W5FormCellHelper;
import iwb.domain.helper.W5TableChildHelper;
import iwb.domain.helper.W5TableRecordHelper;
import iwb.domain.result.W5CardResult;
import iwb.domain.result.W5FormResult;
import iwb.domain.result.W5GlobalFuncResult;
import iwb.domain.result.W5GridResult;
import iwb.domain.result.W5ListViewResult;
import iwb.domain.result.W5PageResult;
import iwb.domain.result.W5QueryResult;
import iwb.domain.result.W5TableRecordInfoResult;
import iwb.enums.FieldDefinitions;
import iwb.exception.IWBException;
import iwb.util.GenericUtil;
import iwb.util.UserUtil;

public class Vue2 implements ViewAdapter {
	final public static String[] labelMap = new String[]{"info","warning","danger"};
	final public static String[] filterMap = new String[]{"","serverFilter","dateRangeFilter","numberFilter","numberFilter","numberFilter"};
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

	public StringBuilder serializeShowForm(W5FormResult formResult) {
		StringBuilder s = new StringBuilder();
		s.append("var _page_tab_id='").append(formResult.getUniqueId())
				.append("';\n");
		boolean liveSyncRecord = FrameworkSetting.liveSyncRecord
				&& formResult != null && formResult.getForm() != null
				&& formResult.getForm().getObjectTip() == 2
				&& formResult.getAction() == 1;

			
		if (GenericUtil.uInt(formResult.getRequestParams().get("a")) != 5 && formResult.getForm().getRenderTip() != 0) { // tabpanel ve icinde gridler varsa
			for (W5FormModule m : formResult.getForm().get_moduleList()) {
					switch (m.getModuleTip()) {
					case 4:// form
						if (formResult.getModuleFormMap() == null)
							break;
						W5FormResult nfr = formResult.getModuleFormMap().get(
								m.getObjectId());
						if (nfr == null)
							return null;
						s.append("var ").append(nfr.getForm().getDsc())
								.append(" =").append(serializeGetForm(nfr))
								.append(";\n");
						break;
					case 5:// grid
						if (formResult.getModuleGridMap() == null)
							return null;
						if (m.getModuleViewTip() == 0
								|| formResult.getAction() == m
										.getModuleViewTip()) {
							W5GridResult gridResult = formResult
									.getModuleGridMap().get(m.getObjectId());
							gridResult.setAction(formResult.getAction());
							W5Table mainTable = gridResult.getGrid() != null
									&& gridResult.getGrid()
											.get_defaultCrudForm() != null ? FrameworkCache
									.getTable(gridResult.getScd(), gridResult
											.getGrid().get_defaultCrudForm()
											.getObjectId()) : null;
							if (mainTable != null
									&& (!GenericUtil
											.accessControl(
													formResult.getScd(),
													mainTable
															.getAccessViewTip(),
													mainTable
															.getAccessViewRoles(),
													mainTable
															.getAccessViewUsers())))
								gridResult = null;// hicbirsey
							else {
								gridResult.setViewReadOnlyMode(formResult
										.isViewMode());
								s.append("\n")
										.append(formResult.getForm().get_renderTemplate()!=null && formResult.getForm().get_renderTemplate().getLocaleMsgFlag() != 0 ? GenericUtil
												.filterExt(
														serializeGrid(
																gridResult)
																.toString(),
														formResult.getScd(),
														formResult
																.getRequestParams(),
														null)
												: serializeGrid(gridResult))
										.append("\n");
								if (liveSyncRecord) {// TODO

								}
							}
						}
					}
				}
		}

		
		
		
		
		
		W5Form f = formResult.getForm();
		
		Map<String, Object> scd = formResult.getScd();
		String xlocale = (String) scd.get("locale");
		int customizationId = (Integer) scd.get("customizationId");
		int userId = (Integer) scd.get("userId");
		
		s.append("var cfgForm={formId: ")
			.append(formResult.getFormId()).append(", a:").append(formResult.getAction())
			.append(", name:'").append(LocaleMsgCache.get2(scd, formResult.getForm().getLocaleMsgKey()))
			.append("',\n id:'").append(formResult.getUniqueId()).append("', defaultWidth:").append(f.getDefaultWidth()).append(", defaultHeight:").append(f.getDefaultHeight());
	
	
	
//		boolean liveSyncRecord = false;
		// form(table) fields
		if (f.getObjectTip() == 2
				&& FrameworkCache.getTable(scd, f.getObjectId()) != null) {
			s.append(",\n renderTip:").append(
					formResult.getForm().getRenderTip());
			W5Table t = FrameworkCache.getTable(scd, f.getObjectId());
			liveSyncRecord = FrameworkSetting.liveSyncRecord
					&& t.getLiveSyncFlag() != 0 && !formResult.isViewMode();
			// insert AND continue control
			s.append(", crudTableId:").append(f.getObjectId());
			if (formResult.getAction() == 2) { // insert
				long tmpId = -GenericUtil.getNextTmpId();
				s.append(", contFlag:").append(f.getContEntryFlag() != 0)
						.append(",\n tmpId:").append(tmpId);
				formResult.getRequestParams().put("_tmpId", "" + tmpId);
			} else if (formResult.getAction() == 1) { // edit
				s.append(",\n pk:").append(GenericUtil.fromMapToJsonString(formResult.getPkFields()));
				if(t.getAccessDeleteTip()==0 || !GenericUtil.isEmpty(t.getAccessDeleteUserFields()) || GenericUtil.accessControl(scd, t.getAccessDeleteTip(), t.getAccessDeleteRoles(), t.getAccessDeleteUsers()))s.append(", deletable:!0");
				if (liveSyncRecord) {
					s.append(", liveSync:true");
					String webPageId = formResult.getRequestParams().get(".w");
					if (webPageId != null) {
						String key = "";
						for (String k : formResult.getPkFields().keySet())
							if (!k.startsWith("customization"))
								key += "*" + formResult.getPkFields().get(k);
						if (key.length() > 0) {
							key = t.getTableId() + "-" + key.substring(1);
							formResult.setLiveSyncKey(key);
							List<Object> l = UserUtil
									.syncGetListOfRecordEditUsers(
											(String)scd.get("projectId"), key,
											webPageId);
							if (!GenericUtil.isEmpty(l)) {// buna duyurulacak
								s.append(",\n liveSyncBy:")
										.append(GenericUtil
												.fromListToJsonString2Recursive((List) l));
							}
						}
					}
				}
	
			}
	
		
			boolean mobile = GenericUtil.uInt(formResult.getScd().get("mobile")) != 0;

			if (FrameworkCache.getAppSettingIntValue(scd, "make_comment_flag") != 0
					&& t.getMakeCommentFlag() != 0){
				s.append(",\n commentFlag:true, commentCount:");
				if(formResult.getCommentExtraInfo()!=null){
					String[] ozc = formResult.getCommentExtraInfo().split(";");//commentCount;commentUserId;lastCommentDttm;viewUserIds-msg
					int ndx = ozc[3].indexOf('-');
					s.append(ozc[0]).append(", commentExtra:{\"last_dttm\":\"").append(ozc[2])
						.append("\",\"user_id\":").append(ozc[1])
						.append(",\"user_dsc\":\"").append(UserUtil.getUserDsc( GenericUtil.uInt(ozc[1])))
						.append("\",\"is_new\":").append(!GenericUtil.hasPartInside(ozc[3].substring(0,ndx), userId+""))
						.append(",\"msg\":\"").append(GenericUtil.stringToHtml(ozc[3].substring(ndx+1)))
						.append("\"}");
				} else s.append(formResult.getCommentCount());
			}
			if (FrameworkCache.getAppSettingIntValue(scd, "file_attachment_flag") != 0 && t.getFileAttachmentFlag() != 0
					&& FrameworkCache.roleAccessControl(scd,  101))
				s.append(",\n fileAttachFlag:true, fileAttachCount:").append(formResult.getFileAttachmentCount());
			if (FrameworkCache.getAppSettingIntValue(scd, "row_based_security_flag") != 0
					&& ((Integer) scd.get("userTip") != 3 && t.getAccessTips() != null))
				s.append(",\n accessControlFlag:true, accessControlCount:").append(formResult.getAccessControlCount());
	
			if (formResult.isViewMode())s.append(",\n viewMode:true");
	
			if (!formResult.isViewMode() && f.get_formSmsMailList() != null
					&& !f.get_formSmsMailList().isEmpty()) { // automatic sms isleri varsa
				int cnt = 0;
				for (W5FormSmsMail fsm : f.get_formSmsMailList())
					if (fsm.getSmsMailSentTip() != 3
							&& ((fsm.getSmsMailTip() == 0 && FrameworkSetting.sms) || (fsm
									.getSmsMailTip() != 0 && FrameworkSetting.mail ))
							&& fsm.getAlarmFlag() == 0
							&& GenericUtil.hasPartInside2(fsm.getActionTips(),
									formResult.getAction())
							&& GenericUtil.hasPartInside2(fsm.getWebMobileTips(), mobile ? "2" : "1")) {
						cnt++;
					}
				if (cnt > 0) {
					s.append(",\n\"smsMailTemplateCnt\":").append(cnt++).append(",\n\"smsMailTemplates\":[");
					boolean b = false;
					for (W5FormSmsMail fsm : f.get_formSmsMailList())
						if (fsm.getSmsMailSentTip() != 3
								&& ((fsm.getSmsMailTip() == 0 && FrameworkSetting.sms) || (fsm
										.getSmsMailTip() != 0 && FrameworkSetting.mail))
								&& fsm.getAlarmFlag() == 0
								&& GenericUtil.hasPartInside2(
										fsm.getActionTips(),
										formResult.getAction())
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
									.append(fsm.getSmsMailTip() == 0 ? "[<b>SMS</b>] "
											: "[<b>"
													+ (LocaleMsgCache.get2(
															customizationId,
															xlocale,
															"email_upper"))
													+ "</b>] ")
									.append(LocaleMsgCache.get2(
											customizationId, xlocale,
											fsm.getDsc()))
									.append(fsm.getPreviewFlag() != 0 ? " (<i>"
											+ (LocaleMsgCache.get2(
													customizationId, xlocale,
													"with_preview")) + "</i>)"
											: "")
									.append("\",\"checked\":")
									.append(fsm.getSmsMailSentTip() == 1
											|| fsm.getSmsMailSentTip() == 0)
									.append(",\"smsMailTip\":")
									.append(fsm.getSmsMailTip())
									.append(",\"previewFlag\":")
									.append(fsm.getPreviewFlag() != 0);
							if (fsm.getSmsMailSentTip() == 0)
								s.append(",\"disabled\":true");
							s.append("}");
						}
					s.append("]");
				}
	
				if (FrameworkSetting.alarm) {
					cnt = 0;
					for (W5FormSmsMail fsm : f.get_formSmsMailList())
						if (fsm.getSmsMailSentTip() != 3
								&& ((fsm.getSmsMailTip() == 0
										&& FrameworkSetting.sms) || (fsm
										.getSmsMailTip() != 0
										&& FrameworkSetting.mail))
								&& fsm.getAlarmFlag() != 0
								&& GenericUtil.hasPartInside2(
										fsm.getActionTips(),
										formResult.getAction())
								&& GenericUtil
										.hasPartInside2(fsm.getWebMobileTips(),
												mobile ? "2" : "1")) {
							cnt++;
						}
					if (cnt > 0) {
						Map<Integer, W5FormSmsMailAlarm> alarmMap = new HashMap();
						if (!GenericUtil.isEmpty(formResult.getFormAlarmList()))
							for (W5FormSmsMailAlarm a : formResult
									.getFormAlarmList()) {
								alarmMap.put(a.getFormSmsMailId(), a);
							}
						s.append(",\n\"alarmTemplateCnt\":").append(cnt++)
								.append(",\n\"alarmTemplates\":[");
						boolean b = false;
						for (W5FormSmsMail fsm : f.get_formSmsMailList())
							if (fsm.getSmsMailSentTip() != 3
									&& ((fsm.getSmsMailTip() == 0
											&& FrameworkSetting.sms) || (fsm
											.getSmsMailTip() != 0
											&& FrameworkSetting.mail))
									&& fsm.getAlarmFlag() != 0
									&& GenericUtil.hasPartInside2(
											fsm.getActionTips(),
											formResult.getAction())
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
										.append(fsm.getSmsMailTip() == 0 ? "[<b>SMS</b>] "
												: "[<b>"
														+ (LocaleMsgCache
																.get2(customizationId,
																		xlocale,
																		"email_upper"))
														+ "</b>] ")
										.append(GenericUtil.stringToJS(fsm
												.getDsc()))
										.append(fsm.getPreviewFlag() != 0 ? " (<i>"
												+ (LocaleMsgCache
														.get2(customizationId,
																xlocale,
																"with_preview"))
												+ "</i>)"
												: "")
										.append("\",\"checked\":")
										.append(a != null
												|| fsm.getSmsMailSentTip() == 1
												|| fsm.getSmsMailSentTip() == 0)
										.append(",\"smsMailTip\":")
										.append(fsm.getSmsMailTip());
								s.append(",\"previewFlag\":").append(
										fsm.getPreviewFlag() != 0);
								if ((a != null && a.getStatus() != 1)
										|| fsm.getSmsMailSentTip() == 0)
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
					if (fsm.getConversionTip() != 3
							&& GenericUtil.hasPartInside2(fsm.getActionTips(),
									formResult.getAction())) { // bu action ile
																// ilgili var mi
																// kayit
						cnt++;
					}
				if (!formResult.isViewMode()
						&& (cnt > 0 || !GenericUtil.isEmpty(formResult
								.getMapConvertedObject()))) {
					s.append(",\nconversionCnt:")
							.append(f.get_conversionList().size())
							.append(",\nconversionForms:[");
					boolean b = false;
					for (W5Conversion fsm : f.get_conversionList())
						if ((fsm.getConversionTip() != 3/* invisible-checked */
								&& GenericUtil.hasPartInside2(
										fsm.getActionTips(),
										formResult.getAction()) || (formResult
								.getMapConvertedObject() != null && formResult
								.getMapConvertedObject().containsKey(
										fsm.getConversionId())))) {
							W5Table dt = FrameworkCache.getTable(formResult.getScd(),
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
								boolean isConvertedBefore = formResult
										.getAction() == 1
										&& formResult.getMapConvertedObject() != null
										&& formResult.getMapConvertedObject()
												.containsKey(
														fsm.getConversionId());
								boolean check = false;
								List<W5ConvertedObject> convertedObjects = null;
								if (isConvertedBefore
										&& fsm.getConversionTip() != 3
										&& GenericUtil.hasPartInside2(
												fsm.getActionTips(),
												formResult.getAction())) {
									convertedObjects = formResult
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
											.append(formResult.getAction() == 2 ? (fsm
													.getPreviewFlag() != 0 ? " (<i>"
													+ (LocaleMsgCache.get2(
															customizationId,
															xlocale,
															"with_preview"))
													+ "</i>)"
													: "")
													: "")
											.append("\",checked:")
											.append(fsm.getConversionTip() == 1
													|| fsm.getConversionTip() == 0);
									if (fsm.getConversionTip() == 0)
										s.append(",disabled:true");
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
					if (GenericUtil.hasPartInside2(fsm.getActionTips(), 0)) { // manuel
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
		}
		boolean b = false;
		if (!formResult.getOutputMessages().isEmpty()) {
			s.append(",\n\"msgs\":[");
			for (String sx : formResult.getOutputMessages()) {
				if (b)s.append("\n,");
				else b = true;
				s.append("'").append(GenericUtil.stringToJS(sx)).append("'");
			}
			s.append("]");
		}
	
		
		if (f.get_toolbarItemList().size() > 0) { // extra buttonlari var mi yok
													// mu?
			StringBuilder buttons = serializeToolbarItems(scd,
					f.get_toolbarItemList(), (formResult.getFormId() > 0 ? true
							: false));
			if (buttons.length() > 1) {
				s.append(",\n extraButtons:[").append(buttons).append("]");
			}
		}
		for (String sx : formResult.getOutputFields().keySet()) {
			s.append(",\n ").append(sx).append(":")
					.append(formResult.getOutputFields().get(sx));// TODO:aslinda' liolması lazim
		}
		s.append("};\nvar bodyForm =").append(serializeGetForm(formResult));

		if (formResult.getForm().get_renderTemplate() != null && formResult.getForm().getRenderTemplateId()!=26) {
				s.append("\n").append(
					formResult.getForm().get_renderTemplate()
							.getLocaleMsgFlag() != 0 ? GenericUtil
							.filterExt(formResult.getForm()
									.get_renderTemplate().getCode(),
									formResult.getScd(),
									formResult.getRequestParams(), null)
							: formResult.getForm().get_renderTemplate()
									.getCode());
		} else if(formResult.getForm().getObjectTip()==2)
			s.append("\nreturn {body:Vue.extend(bodyForm), cfg:cfgForm, parentCt:parentCt};");
		


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
		if (f.getObjectTip() == 2) {
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
				if (((fsm.getSmsMailTip() == 0 && FrameworkSetting.sms) || (fsm
						.getSmsMailTip() != 0 && FrameworkSetting.mail))
						&& fsm.getAlarmFlag() == 0
						&& GenericUtil.hasPartInside2(fsm.getActionTips(),
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
					if (((fsm.getSmsMailTip() == 0 && FrameworkSetting.sms) || (fsm
							.getSmsMailTip() != 0 && FrameworkSetting.mail))
							&& fsm.getAlarmFlag() == 0
							&& GenericUtil.hasPartInside2(fsm.getActionTips(),
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
								.append(fsm.getSmsMailTip() == 0 ? "[SMS] "
										: "["
												+ (LocaleMsgCache.get2(
														customizationId,
														xlocale, "email_upper"))
												+ "] ")
								.append(LocaleMsgCache.get2(formResult.getScd(), fsm.getDsc()))
								.append(fsm.getPreviewFlag() != 0 ? " ("
										+ (LocaleMsgCache.get2(
												formResult.getScd(),
												"with_preview")) + ")" : "")
								.append("\",\"checked\":")
								.append(fsm.getSmsMailSentTip() == 1
										|| fsm.getSmsMailSentTip() == 0)
								.append(",\"smsMailTip\":")
								.append(fsm.getSmsMailTip())
								.append(",\"previewFlag\":")
								.append(fsm.getPreviewFlag() != 0);
						if (fsm.getSmsMailSentTip() == 0)
							s.append(",\"disabled\":true");
						s.append("}");
					}
				s.append("]");
			}

			cnt = 0;
			for (W5FormSmsMail fsm : f.get_formSmsMailList())
				if (((fsm.getSmsMailTip() == 0 && FrameworkSetting.sms) || (fsm
						.getSmsMailTip() != 0 && FrameworkSetting.mail))
						&& fsm.getAlarmFlag() != 0
						&& GenericUtil.hasPartInside2(fsm.getActionTips(),
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
					if (((fsm.getSmsMailTip() == 0 && FrameworkSetting.sms) || (fsm
							.getSmsMailTip() != 0 && FrameworkSetting.mail))
							&& fsm.getAlarmFlag() != 0
							&& GenericUtil.hasPartInside2(fsm.getActionTips(),
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
								.append(fsm.getSmsMailTip() == 0 ? "[SMS] "
										: "["
												+ (LocaleMsgCache.get2(
														customizationId,
														xlocale, "email_upper"))
												+ "] ")
								.append(GenericUtil.stringToJS(fsm.getDsc()))
								.append(fsm.getPreviewFlag() != 0 ? " ("
										+ (LocaleMsgCache.get2(
												customizationId, xlocale,
												"with_preview")) + ")" : "")
								.append("\",\"checked\":")
								.append(a != null
										|| fsm.getSmsMailSentTip() == 1
										|| fsm.getSmsMailSentTip() == 0)
								.append(",\"smsMailTip\":")
								.append(fsm.getSmsMailTip());
						s.append(",\"previewFlag\":").append(
								fsm.getPreviewFlag() != 0);
						if ((a != null && a.getStatus() != 1)
								|| fsm.getSmsMailSentTip() == 0)
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
					&& fc.getFormCell().getControlTip() != 102) {
				if (fc.getFormCell().getControlTip() != 102) {// label'dan
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
									.get2(formResult.getScd(), fc
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
					switch (fc.getFormCell().getControlTip()) {
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
											.get2(formResult.getScd(),
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
											.append(qf.getPostProcessTip() == 2 ? LocaleMsgCache
													.get2(formResult.getScd(),
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
				f.getObjectTip() == 3 ? "rpt/" + f.getDsc() : "ajaxExecDbFunc",
				"ajaxExecDbFunc",null,null,"search_form", "search_form", null,null,"ajaxCallWs?serviceName="+FrameworkCache.getServiceNameByMethodId(scd,  f.getObjectId())};
		s.append("{\ndata(){\nreturn {manualValidation:false,params:").append(GenericUtil.fromMapToJsonString(formResult.getRequestParams())).append(",xerrors:{},values:{");
		
		boolean b = false;
		for (W5FormCellHelper fc : formResult.getFormCellResults())if (fc.getFormCell().getActiveFlag() != 0 && fc.getFormCell().getControlTip()>0 && fc.getFormCell().getControlTip()<100) {
			if (b)s.append(","); else b = true;
			s.append(fc.getFormCell().getDsc()).append(":'");
			String value = fc.getHiddenValue(); if(value == null) value =  fc.getValue();
			if(!GenericUtil.isEmpty(value))switch(fc.getFormCell().getControlTip()){
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
		s.append("},options:{}}},props:['viewMode']");
		//\nif(this.manualValidation)this.manualValidation=this.manualValidation.bind(this);
		Map<String, List<W5FormCell>> pcr = new HashMap();
		for (W5FormCellHelper fc : formResult.getFormCellResults())if (fc.getFormCell().getActiveFlag() != 0 && fc.getFormCell().getControlTip()==9 && fc.getFormCell().getParentFormCellId()!=0 && !GenericUtil.isEmpty(fc.getFormCell().getLookupIncludedParams())) {//combo remote
			for (W5FormCellHelper rfc : formResult.getFormCellResults()) {
				if (rfc.getFormCell().getFormCellId() == fc.getFormCell().getParentFormCellId()) {
					W5FormCell pfc = rfc.getFormCell();
					if (pfc.getControlTip() == 6 || pfc.getControlTip() == 7 || pfc.getControlTip() == 9 || pfc.getControlTip() == 10 || pfc.getControlTip() == 51) {
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
		s.append("\n, mounted(){if(typeof parentCt!='undefined')parentCt.form=this;}");
		s.append("\n, methods:{submit:function(cfg){var p = Object.assign({}, this.values);if(this.manualValidation){var r = this.manualValidation(p, cfg||{});if(r===false)return false;p = Object.assign(p, r);}iwb.request({url:'ajaxPostForm?'+iwb.JSON2URI(this.params)+'_renderer=vue2&.r='+Math.random(), params:p, self:this, errorCallback:(json,ecfg)=>{var errors={};if(json.errorType)switch(json.errorType){case	'validation':iwb.notifyVue('error','Validation Errors');if(json.errors && json.errors.length)json.errors.map(function(o){errors[o.id]=o.msg;});if(json.error)iwb.notifyVue('error',json.error);break;default:iwb.notifyVue('error',json.errorType);} else alert(json);this.xerrors=errors;return false;}, successCallback:(json, xcfg)=>{if(cfg.callback)cfg.callback(json,xcfg);}});}}");
	
//		if (liveSyncRecord)formResult.getRequestParams().put(".t", formResult.getUniqueId());
		s.append("\n,render(h){\nvar mf=this, values=this.values, options=this.options, errors=this.xerrors, viewMode=this.viewMode||false;\n");

		
		for (W5FormCellHelper fc : formResult.getFormCellResults())
			if (fc.getFormCell().getActiveFlag() != 0) {
				if (fc.getFormCell().getControlTip() != 102) {// label'dan farkli ise. label direk render edilirken koyuluyor
					s.append("var _").append(fc.getFormCell().getDsc()).append("=").append(serializeFormCell(customizationId, xlocale,fc, formResult)).append(";\n");
				} else {
					fc.setValue(LocaleMsgCache.get2(formResult.getScd(),
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

		if(formResult.getForm().getObjectTip()==1){ //search ise
			s.append(renderSearchFormModuleList(customizationId, xlocale,
					formResult.getUniqueId(),
					formResult.getFormCellResults(),
					"mf=h(Form, mf,")).append(");\n");
		} else if(false)switch (formResult.getForm().getRenderTip()) {
		case 1:// fieldset
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
		s.append(renderFormModuleListTop(customizationId, xlocale,
				formResult.getUniqueId(),
				formResult.getFormCellResults(),
				"mf=", formResult.getRequestParams().get("_modal")!=null ? -1:formResult.getForm().getDefaultWidth())).append(";\n");



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
		buf.append("mf=[");

		int defaultWidth = -1;
		if(formResult.getRequestParams().get("_modal")==null)defaultWidth = formResult.getForm().getDefaultWidth();
		List<String> extendedForms = new ArrayList();
		if (map.get(0).size() > 0) {
			buf.append(renderFormModuleListTop(customizationId, xlocale,
					formResult.getUniqueId(), map.get(0), "", defaultWidth));
		}
		StringBuilder contentBuf = new StringBuilder();
		int firstTab = 0;
		if (formResult.getForm().get_moduleList() != null){
			for (W5FormModule m : formResult.getForm().get_moduleList())
				if (m.getFormModuleId() != 0) {
					if ((m.getModuleViewTip() == 0 || formResult.getAction() == m.getModuleViewTip()) 
							) {
						switch (m.getModuleTip()) {
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
									buf.append(",h('el-row',null,h(Col,{ class: 'mb-3 mt-4' }, h(Nav,{tabs: true}");
									contentBuf.append(", h(TabContent,{ class:'form-tabcontent', activeTab: this.state.activeTab||'").append(firstTab).append("' }");
								}
								buf.append(",h(NavItem, null,h(NavLinkS,{class: classNames({active: ").append(firstTab==m.getFormModuleId()?"!this.state.activeTab || ":"").append("this.state.activeTab === '").append(m.getFormModuleId()).append("'}), onClick:()=> this.toggleTab('").append(m.getFormModuleId()).append("')},'").append(LocaleMsgCache.get2(formResult.getScd(), m.getLocaleMsgKey())).append("'))");
								contentBuf.append(",h(TabPane, {tabId: '").append(m.getFormModuleId()).append("' },h(XEditGrid,").append(gridResult.getGrid().getDsc()).append("))");
								
							}
							break;
						

						default:
							if (!map.get(m.getFormModuleId()).isEmpty()) {
								if(firstTab==0){
									firstTab = m.getFormModuleId();
									buf.append(",h('el-row',null,h(Col,{ class: 'mb-3 mt-4' }, h(Nav,{tabs: true}");
									contentBuf.append(", h(TabContent,{ class:'form-tabcontent', activeTab: this.state.activeTab||'").append(firstTab).append("' }");
								}
								buf.append(",h(NavItem, null,h(NavLinkS,{class: classNames({active: ").append(firstTab==m.getFormModuleId()?"!this.state.activeTab || ":"").append("this.state.activeTab === '").append(m.getFormModuleId()).append("'}), onClick:()=> this.toggleTab('").append(m.getFormModuleId()).append("')},'").append(LocaleMsgCache.get2(formResult.getScd(), m.getLocaleMsgKey())).append("'))");
								contentBuf.append(renderFormModuleListTop(customizationId, xlocale, formResult.getUniqueId(),
										map.get(m.getFormModuleId()), ",h(TabPane, {tabId: '"+m.getFormModuleId()+"' }, ", defaultWidth)).append(")");
							}
						}
					}
				}
		}
		if(firstTab>0){
			buf.append(")").append(contentBuf).append(")))");
		}
		buf.append("];");

		return buf;
	}


	private StringBuilder recursiveTemplateObject(List l, int parentObjectId, int level) {
		if(level>5 && l==null || l.size()<2)return null;
		StringBuilder buf = new StringBuilder();
		for(Object o:l)if(o instanceof W5GridResult){
			W5GridResult gr = (W5GridResult)o;
			if(gr.getTplObj().getTemplateObjectId()!=parentObjectId && gr.getTplObj().getParentObjectId()==parentObjectId){
				if(buf.length()==0){
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
				StringBuilder rbuf = recursiveTemplateObject(l, gr.getTplObj().getTemplateObjectId(), level+1);
				if(rbuf!=null && rbuf.length()>0)
					buf.append(",").append(rbuf);
				buf.append("},");
			}
		} else if(o instanceof W5CardResult){
			W5CardResult gr = (W5CardResult)o;
			if(gr.getTplObj().getTemplateObjectId()!=parentObjectId && gr.getTplObj().getParentObjectId()==parentObjectId){
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
				StringBuilder rbuf = recursiveTemplateObject(l, gr.getTplObj().getTemplateObjectId(), level+1);
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

	private StringBuilder renderTemplateObject(W5PageResult templateResult) {
//		return addTab4GridWSearchForm({t:_page_tab_id,grid:grd_online_users1, pk:{tuser_id:'user_id'}});
		StringBuilder buf = new StringBuilder();
		if(templateResult.getPageObjectList().get(0) instanceof W5CardResult){
			W5CardResult gr = (W5CardResult)templateResult.getPageObjectList().get(0);
			buf.append("return iwb.ui.buildPanel({t:_page_tab_id, card:").append(gr.getCard().getDsc());
			if(gr.getCard().get_crudTable()!=null){
				W5Table t = gr.getCard().get_crudTable();
				buf.append(",pk:{").append(t.get_tableParamList().get(0).getDsc()).append(":'").append(t.get_tableParamList().get(0).getExpressionDsc()).append("'}");
			}
			buf.append("});");
			return buf;
		}
		if(!(templateResult.getPageObjectList().get(0) instanceof W5GridResult))return buf;
		W5GridResult gr = (W5GridResult)templateResult.getPageObjectList().get(0);
		buf.append("return iwb.ui.buildPanel({t:_page_tab_id, grid:").append(gr.getGrid().getDsc());
		if(gr.getGrid().get_crudTable()!=null){
			W5Table t = gr.getGrid().get_crudTable();
			buf.append(",pk:{").append(t.get_tableParamList().get(0).getDsc()).append(":'").append(t.get_tableParamList().get(0).getExpressionDsc()).append("'}");
		}
		if(templateResult.getPageObjectList().size()>1){
			StringBuilder rbuf = recursiveTemplateObject(templateResult.getPageObjectList(), ((W5GridResult)templateResult.getPageObjectList().get(0)).getTplObj().getTemplateObjectId(), 1);
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
				if ((m.getModuleViewTip() == 0 || formResult.getAction() == m
						.getModuleViewTip())) {
					switch (m.getModuleTip()) {
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
								&& (!GenericUtil
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
									.append(LocaleMsgCache.get2(formResult.getScd(),
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
									+ LocaleMsgCache.get2(formResult.getScd(), m.getLocaleMsgKey()) + "'";
							// if(formBodyColor!=null)extra+=",bodyStyle:'background-color: #"+formBodyColor+"'";
							if (formBodyStyle != null)
								extra += ",bodyStyle:'" + formBodyStyle + "'";

							W5FormCellHelper extraInfo = getModulExtraInfo(
									(String) formResult.getScd().get("locale"),
									m.getLocaleMsgKey());
							if (extraInfo != null)
								map.get(m.getFormModuleId()).add(0, extraInfo);
							buf.append(renderFormModuleList(customizationId, xlocale, formResult.getUniqueId(),
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
		buf.append("mf=[");

		int defaultWidth = -1;
		if(formResult.getRequestParams().get("_modal")==null)defaultWidth = formResult.getForm().getDefaultWidth();
		List<String> extendedForms = new ArrayList();
		if (map.get(0).size() > 0) {
			buf.append(renderFormModuleListTop(customizationId, xlocale,
					formResult.getUniqueId(), map.get(0), "", defaultWidth));
		}
		if (formResult.getForm().get_moduleList() != null)
			for (W5FormModule m : formResult.getForm().get_moduleList())
				if (m.getFormModuleId() != 0) {
					if ((m.getModuleViewTip() == 0 || formResult.getAction() == m
							.getModuleViewTip())) {
						switch (m.getModuleTip()) {
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
							buf.append(",h('div',{class:'hr-text', style:{marginTop:'20px'}},h('h6',null,'").append(LocaleMsgCache.get2(formResult.getScd(), m.getLocaleMsgKey())).append("')),h(XEditGrid,").append(gridResult.getGrid().getDsc()).append(")");
							
						}
						break;

						default:
							if (!map.get(m.getFormModuleId()).isEmpty()) {
								buf.append(renderFormModuleListTop(
										customizationId, xlocale,
										formResult.getUniqueId(),
										map.get(m.getFormModuleId()), ",h('div',{class:'hr-text', style:{marginTop:'20px'}},h('h6',null,'"+LocaleMsgCache.get2(formResult.getScd(), m.getLocaleMsgKey())+"')),", defaultWidth));
							}
						}
					}
				}
		buf.append("];");

		return buf;
	}
	
	
	private StringBuilder renderFormCellWithLabelTop(W5FormCellHelper fc){
		StringBuilder buf = new StringBuilder();
		String dsc = fc.getFormCell().getDsc();
		short c = fc.getFormCell().getControlTip(); 
		if(c == 5){
			buf.append(",\n_").append(dsc).append(" && h('div', {class:'form-group',style:{display: _").append(dsc).append(".hidden?'none':''}}, [h(_").append(dsc)
			.append(".$||'el-switch',{props:viewMode?Object.assign({disabled:true},_").append(dsc).append("):_").append(dsc).append(",on:{input:(v)=>{this.values.").append(dsc).append("=v;}}}),h('span',{style:'padding-left:10px'},_").append(dsc).append(".label)])");
		} else {
			if (c == 102) {// displayField4info
//				buf.append("\n,h('div', {style:{padding:'0.45rem .85rem'}, class:'alert alert-").append(labelMap[fc.getFormCell().getLookupQueryId()]).append("'}, h('i',{class:'icon-info'}),' ','").append(GenericUtil.stringToJS(fc.getValue())).append("')");
				buf.append("\n,h('p', {/*style:{padding:'0.45rem .85rem'}, */class:'text-").append(labelMap[fc.getFormCell().getLookupQueryId()]).append("'}, [/*h('i',{dataNotify:'icon',class:'now-ui-icons ui-1_bell-53'}),*/'").append(GenericUtil.stringToJS(fc.getValue())).append("'])");
				
			} else if (c == 100) {// button
				buf.append("\n, _").append(dsc).append(" && !_").append(dsc).append(".hidden && h('div', {class:'form-group'}, [h('el-button',{props:_").append(dsc).append(", on:_").append(dsc).append(".on},_").append(dsc).append(".name)])");
			} else {
				buf.append("\n, _").append(dsc).append(" && h('div', _").append(dsc).append(".hidden?{style:{display:'none'}}:{class:'form-group'+(_").append(dsc).append(".required?' lrequired':'')+(errors.").append(dsc).append(" ? ' validation-error':'')}, [h('label', {htmlFor:\"").append(dsc).append("\"},_").append(dsc).append(".label), viewMode ? iwb.getFieldRawValue(h,_").append(dsc).append(",this.options.").append(dsc).append(") :h(_")
					.append(dsc).append(".$||'el-input',{class:_").append(dsc).append(".class||'',props:_").append(dsc).append(",on:{").append(c==2||c==18||c==22?"input":"change").append(":(v)=>{this.values.").append(dsc).append("=v;}}}");
				if(c==6 || c==8 || c==58 || c==7 || c==15 || c==59){
					buf.append(",_").append(dsc).append(".options && _").append(dsc).append(".options.map(function(o){return h('el-option',{props:{key:o.id,value:o.id,label:o.dsc}})})");
				} else if(c==9 || c==10){
					buf.append(",options.").append(dsc).append(" && options.").append(dsc).append(".map(function(o){return h('el-option',{props:{key:o.id,value:o.id,label:o.dsc}})})");
				}
				buf.append("),errors.").append(dsc).append(" && h('small',errors.").append(dsc).append(")])");
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
			buf.append("h('div', {class:'row'}, [h('div',{class:'col-12 col-xl-").append(xl).append(" col-lg-").append(lg).append(" col-md-").append(md).append(" col-sm-").append(sm).append("'},[false");
			for (int i = 0; i < formCells.size(); i++) {
				W5FormCellHelper fc = formCells.get(i);
				if (fc.getFormCell().getActiveFlag() == 0 || fc.getFormCell().getControlTip()==0)
					continue;
//				String dsc = fc.getFormCell().getDsc();
				
				if (i < formCells.size() - 1 && formCells.get(i + 1).getFormCell().getControlTip() != 0 && formCells.get(i + 1).getFormCell().getActiveFlag() != 0
						&& formCells.get(i + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder()) { // yanyana koymak icin. 
					buf.append(", h('div',{class:'row'}");
					
					int columnWidthTotal = fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300;
					for(int ji=i;ji < formCells.size() - 1 && formCells.get(ji + 1).getFormCell().getControlTip() != 0 && formCells.get(ji + 1).getFormCell().getActiveFlag() != 0 && formCells.get(ji + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder();ji++){
						columnWidthTotal += formCells.get(ji+1).getFormCell().getControlWidth()>0 ? formCells.get(ji+1).getFormCell().getControlWidth():300;
					}
					
					int totalColXs = 0;
					int xs = 12*(fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300)/columnWidthTotal;
					if(xs==0)xs=1;
					totalColXs+=xs;
					buf.append(",[h('div',{class:'col-12 col-md-").append(xs).append("'},[false").append(renderFormCellWithLabelTop(fc)).append("])");
					while (i < formCells.size() - 1 && formCells.get(i + 1).getFormCell().getControlTip() != 0 && formCells.get(i + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder()) {
						i++;
						fc = formCells.get(i);
						xs = 12*(fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300)/columnWidthTotal;
						if(xs==0)xs=1;
						totalColXs+=xs;
						buf.append(",h('div',{class:'col-12 col-md-").append(xs).append("'},[false").append(renderFormCellWithLabelTop(fc)).append("])");
					}
					buf.append("])");
				} else {
					buf.append(renderFormCellWithLabelTop(fc));
				}
			}
			buf.append("])])");
		} else {
			for(int qi=0;qi<=lc;qi++){
				maxWidths[qi]=6*maxWidths[qi]/5;
			}
			int xl = Math.min(12, (12*maxWidths[0])/(modal?totalControlWidth:1140));// extraLarge >=1200px
			int lg = modal ? xl :Math.min(12, (12*maxWidths[0])/960);// Large >=992px
			int md = modal ? xl :Math.min(12, (12*maxWidths[0])/720);// Medium >=768px
			int sm = modal ? xl: Math.min(12, (12*maxWidths[0])/540);// Small >=576px

			buf.append("h('div', {class:'row'}, [h('div',{class:'col-12 col-xl-").append(xl).append(" col-lg-").append(lg).append(" col-md-").append(md).append(" col-sm-").append(sm).append("'},[false");
			StringBuilder columnBuf = new StringBuilder();
			int order=0;
			for (int i = 0; i < formCells.size(); i++) {
				W5FormCellHelper fc = formCells.get(i);
				if (fc.getFormCell().getActiveFlag() == 0 || fc.getFormCell().getControlTip()==0)
					continue;
				if (fc.getFormCell().getTabOrder() / 1000 != order) {
					order = fc.getFormCell().getTabOrder() / 1000;
					int nxl = Math.min(12, (12*maxWidths[order])/(modal?totalControlWidth:1140));// extraLarge >=1200px
					int nlg = modal ? xl :Math.min(12, (12*maxWidths[order])/960);// Large >=992px
					int nmd = modal ? xl :Math.min(12, (12*maxWidths[order])/720);// Medium >=768px
					int nsm = modal ? xl :Math.min(12, (12*maxWidths[order])/320);// Small >=576px
					if(modal){
						xl+=nxl;
						if(xl==13 || xl==14){
							nxl--;if(xl==14)nxl--;
							nlg = nmd = nsm = nxl;
						}
					}
					
					if (columnBuf.length() > 0) {
						buf.append(columnBuf.append("]), h('div',{class:'col-12 col-xl-").append(nxl).append(" col-lg-").append(nlg).append(" col-md-").append(nmd).append(" col-sm-").append(nsm).append("'},[false"));
						columnBuf.setLength(0);
					}
				}
				if (i < formCells.size() - 1 && formCells.get(i + 1).getFormCell().getControlTip() != 0 && formCells.get(i + 1).getFormCell().getActiveFlag() != 0
						&& formCells.get(i + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder()) { // yanyana koymak icin. 
					columnBuf.append(", h('div', {class:'row'},[false");
					
					int columnWidthTotal = fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300;
					for(int ji=i;ji < formCells.size() - 1 && formCells.get(ji + 1).getFormCell().getControlTip() != 0 && formCells.get(ji + 1).getFormCell().getActiveFlag() != 0 && formCells.get(ji + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder();ji++){
						columnWidthTotal += formCells.get(ji+1).getFormCell().getControlWidth()>0 ? formCells.get(ji+1).getFormCell().getControlWidth():300;
					}
					
					int totalColXs = 0;
					int xs = 12*(fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300)/columnWidthTotal;
					if(xs==0)xs=1;
					totalColXs+=xs;
					columnBuf.append(",h('div',{class:'col-12 col-md-").append(xs).append("'},[false").append(renderFormCellWithLabelTop(fc)).append("])"); //").append(fc.getFormCell().getControlWidth()>200 ? 12:xs).append("
					while (i < formCells.size() - 1 && formCells.get(i + 1).getFormCell().getControlTip() != 0 && formCells.get(i + 1).getFormCell().getTabOrder() == fc.getFormCell().getTabOrder()) {
						i++;
						fc = formCells.get(i);
						xs = 12*(fc.getFormCell().getControlWidth()>0 ? fc.getFormCell().getControlWidth():300)/columnWidthTotal;
						if(xs==0)xs=1;
						totalColXs+=xs;
						columnBuf.append(",h('div',{class:'col-12 col-md-").append(xs).append("'},[false").append(renderFormCellWithLabelTop(fc)).append("])");
					}
					columnBuf.append("])");
				} else {
					columnBuf.append(renderFormCellWithLabelTop(fc));

				}
			}
			buf.append(columnBuf.append("])])"));
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
			
			buf.append("h('el-row', {style:{maxWidth:'").append(defaultWidth).append("px'}}, h(Col,{xs:'12',xl:'").append(xl).append("',lg:'").append(lg).append("',md:'").append(md).append("',sm:'").append(sm).append("'}");// ,\nautoHeight:false

			int lxl = Math.max(1, Math.min(12, (12*labelWidth+300)/Math.min(defaultWidth,1140)));// extraLarge >=1200px
			int llg = Math.max(1, Math.min(12, (12*labelWidth+240)/Math.min(defaultWidth,960)));// Large >=992px
			int lmd = Math.min(12, (12*labelWidth+180)/Math.min(defaultWidth,720));// Medium >=768px
			int lsm = Math.min(12, (12*labelWidth+130)/Math.min(defaultWidth,540));// Small >=576px
			StringBuilder labelBuf = new StringBuilder(), inputBuf = new StringBuilder();
			labelBuf.append("xs:'12',xl:'").append(lxl).append("',lg:'").append(llg).append("',md:'").append(lmd).append("',sm:'").append(lsm).append("'");
			inputBuf.append("xs:'12',xl:'").append(12-lxl).append("',lg:'").append(12-llg).append("',md:'").append(12-lmd).append("',sm:'").append(12-lsm).append("'");

			for (int i = 0; i < formCells.size(); i++) {
				W5FormCellHelper fc = formCells.get(i);
				if (fc.getFormCell().getActiveFlag() == 0 || fc.getFormCell().getControlTip()==0)
					continue;
				if(fc.getFormCell().getControlTip() == 5){
					buf.append(", h(FormGroup, {row:true}, h(Label, {").append(labelBuf).append(",htmlFor:\"")
					.append(fc.getFormCell().getDsc()).append("\"},_").append(fc.getFormCell().getDsc()).append(".label), h(Label,{class: 'switch switch-3d switch-primary' }, _").append(fc.getFormCell().getDsc())
					.append(",h('span', { class: 'switch-label' }),h('span', { class: 'switch-handle' })))");
				} else {
					buf.append(", h(FormGroup, {row:true}, h(Label, {").append(labelBuf).append(",");//
					if (fc.getFormCell().getControlTip() == 102) {// displayField4info
						buf.append("md:null}, \"").append(fc.getValue()).append("\"))");
					} else {
						buf.append("htmlFor:\"").append(fc.getFormCell().getDsc()).append("\"},_").append(fc.getFormCell().getDsc()).append(".label), h(Col,{").append(inputBuf).append("},_")
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
			
			buf.append("h('div', {class:'row',style:{maxWidth:'").append(defaultWidth).append("px'}}, h('div',{class:'col',xs:'12',xl:'").append(xl).append("',lg:'").append(lg).append("',md:'").append(md).append("',sm:'").append(sm).append("'}");// ,\nautoHeight:false

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
				if (fc.getFormCell().getActiveFlag() == 0 || fc.getFormCell().getControlTip()==0)
					continue;
				if (fc.getFormCell().getTabOrder() / 1000 != order) {
					order = fc.getFormCell().getTabOrder() / 1000;
					xl = Math.min(12, (12*(maxWidths[order]+labelWidth))/1140);// extraLarge >=1200px
					lg = Math.min(12, (12*(maxWidths[order]+labelWidth))/960);// Large >=992px
					md = Math.min(12, (12*(maxWidths[order]+labelWidth))/720);// Medium >=768px
					sm = Math.min(12, (12*(maxWidths[order]+labelWidth))/320);// Small >=576px


					if (columnBuf.length() > 0) {
						buf.append(columnBuf.append("), h(Col,{xs:'12',xl:'").append(xl).append("',lg:'").append(lg).append("',md:'").append(md).append("',sm:'").append(sm).append("'}"));
						columnBuf.setLength(0);
					}
				}
				if(fc.getFormCell().getControlTip() == 5){
					columnBuf.append(", h(FormGroup, {row:true}, h(Label, {").append(labelBuf).append(",htmlFor:\"")
					.append(fc.getFormCell().getDsc()).append("\"},_").append(fc.getFormCell().getDsc()).append(".label), h(Label,{ class: 'switch switch-3d switch-primary' }, _").append(fc.getFormCell().getDsc())
					.append(",h('span', { class: 'switch-label' }),h('span', { class: 'switch-handle' })))");
				} else {
					columnBuf.append(", h(FormGroup, {row:true}, h(Label, {").append(fc.getFormCell().getControlTip() == 102 ? "xxmd:null":labelBuf).append(",");//
					if (fc.getFormCell().getControlTip() == 102) {// displayField4info
						columnBuf.append("}, \"").append(fc.getValue()).append("\"))");
					} else {
						columnBuf.append("htmlFor:\"").append(fc.getFormCell().getDsc()).append("\"},_").append(fc.getFormCell().getDsc()).append(".label), h(Col,{").append(inputBuf).append("},_")
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
		buf.append("h('div',null");// ,normalde Col olmasi lazim
		for (int i = 0; i < formCells.size(); i++) {
			W5FormCellHelper fc = formCells.get(i);
			if (fc.getFormCell().getActiveFlag() == 0 || fc.getFormCell().getControlTip()==0)
				continue;
			String dsc= fc.getFormCell().getDsc();
			if(fc.getFormCell().getControlTip() == 5){
				buf.append(",\n_").append(dsc).append(" && h(FormGroup, {style:{marginBottom:'0.3rem', display: _").append(dsc).append(".hidden?'none':''}}, h(Label,{ class: 'switch switch-3d switch-primary' }, h(_").append(dsc)
				.append(".$||Input,_").append(dsc).append("),h('span', { class: 'switch-label' }),h('span', { class: 'switch-handle' })), h(Label, {style:{marginLeft:'1rem'},htmlFor:\"")
				.append(dsc).append("\"},_").append(dsc).append(".label))");
			} else {
				if (fc.getFormCell().getControlTip() == 102) {// displayField4info
					buf.append("\n,h('div', {style:{padding:'0.45rem .85rem'}, class:'alert alert-with-icon alert-").append(labelMap[fc.getFormCell().getLookupQueryId()]).append("'}, [h('i',{class:'now-ui-icons ui-1_bell-53'}),'").append(GenericUtil.stringToJS(fc.getValue())).append("'])");
				} else if (fc.getFormCell().getControlTip() == 100) {// button
					buf.append("\n, _").append(dsc).append(" && !_").append(dsc).append(".hidden && h(FormGroup, null, h(Button,_").append(dsc).append("))");
				} else {
					buf.append("\n, _").append(dsc).append(" && h(FormGroup, _").append(dsc).append(".hidden?{style:{display:'none'}}:null, h(Label, {htmlFor:\"").append(dsc).append("\"},_").append(dsc).append(".label), h(_").append(dsc).append(".$||Input,_").append(dsc).append("))");
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
			fc.setControlTip((short) 102);// displayField4info
			fc.setTabOrder((short) -1);
			fce = new W5FormCellHelper(fc);
			fce.setValue(moduleExtraInfo);
		}
		return fce;
	}

	@SuppressWarnings("unchecked")
	private StringBuilder serializeFormCell(int customizationId,
			String xlocale, W5FormCellHelper cellResult, W5FormResult formResult) {
		W5FormCell fc = cellResult.getFormCell();
		String value = cellResult.getValue(); // bu ilerde hashmap ten gelebilir
		// int customizationId =
		// PromisUtil.uInt(formResult.getScd().get("customizationId"));
		StringBuilder buf = new StringBuilder();
		if (fc.getControlTip() == 0)return buf.append("'").append(GenericUtil.stringToJS(value)).append("'");
		buf.append("{");
		
		if (fc.getControlTip() == 102)
			return buf.append("$:'div', class:'alert alert-").append(labelMap[fc.getLookupQueryId()]).append("',children:[h('i',{class:'icon-info'}),' ','").append(GenericUtil.stringToJS(value)).append("']}");
		else if ((fc.getControlTip() == 101 || cellResult.getHiddenValue() != null)/* && (fc.getControlTip()!=9 && fc.getControlTip()!=16) */) {
			return buf.append("type:'text', readOnly:true, hiddenValue:'").append(GenericUtil.stringToJS(cellResult.getHiddenValue())).append("',label:'").append(LocaleMsgCache.get2(formResult.getScd(), fc.getLocaleMsgKey())).append("',disabled:true, value:'").append(GenericUtil.stringToJS(value)).append("'}");
			
		}
		
		switch(fc.getControlTip()){//fc.getControlTip()
		case	1:buf.append("type:");
			if(GenericUtil.safeEquals(fc.getVtype(), "email"))buf.append("'email'");
			else if(GenericUtil.safeEquals(fc.getVtype(), "url"))buf.append("'url'");
			else buf.append("'text'");
			break;//string
		case	2:buf.append("$:'el-date-picker', class:'iwb-control',type:'date', rangeSeparator:'/',valueFormat:'dd/MM/yyyy',format:'dd/MM/yyyy'");break; //TODO:date
		case	18:buf.append("$:'el-date-picker', class:'iwb-control',type:'datetime', rangeSeparator:'/',valueFormat:'dd/MM/yyyy HH:mm',format:'dd/MM/yyyy HH:mm'");break; //TODO:datetime
		case	22:buf.append("$:'el-time-select', class:'iwb-control',format:'HH:mm'");break; //TODO:time
		case	3://double
			buf.append("type:'number'");
			if(fc.getNotNullFlag()!=0)buf.append(",class:'xrequired',required:true");
			if(fc.get_sourceObjectDetail()!=null && (fc.get_sourceObjectDetail() instanceof W5TableField)){
				W5TableField tf = (W5TableField)fc.get_sourceObjectDetail();
				if(tf.getMinValue()!=null)buf.append(",min:").append(tf.getMinValue());
				if(tf.getMaxValue()!=null)buf.append(",max:").append(tf.getMaxValue());
			}
			break;
		case	4://integer
			buf.append("$:'el-input-number',class:'iwb-control");
			if(fc.getNotNullFlag()!=0)buf.append(" xrequired',required:true");else buf.append("'");
			if(fc.get_sourceObjectDetail()!=null && (fc.get_sourceObjectDetail() instanceof W5TableField)){
				W5TableField tf = (W5TableField)fc.get_sourceObjectDetail();
				if(tf.getMinValue()!=null)buf.append(",min:").append(tf.getMinValue());
				if(tf.getMaxValue()!=null)buf.append(",max:").append(tf.getMaxValue());
			}
			break;
		case	5:buf.append("$:'el-switch'");break;
		case	100:buf.append("color:'primary',on:{click:function(ax){").append(fc.getDefaultValue()).append("}}");
				if(fc.getLocaleMsgKey().startsWith("icon-"))buf.append(",icon:'el-").append(fc.getLocaleMsgKey()).append("'");
				else if(fc.getLocaleMsgKey().startsWith("el-icon-"))buf.append(",icon:'").append(fc.getLocaleMsgKey()).append("'");
				else buf.append(", name:'").append(LocaleMsgCache.get2(formResult.getScd(), fc.getLocaleMsgKey())).append("'");
				if(!GenericUtil.isEmpty(fc.getExtraDefinition())){
					buf.append(fc.getExtraDefinition());//button
				}
				return buf.append("}");

		case	6://combo static
		case	8:// lovcombo-static
		case	58:// superbox lovcombo-static

		case	7://combo query
		case	15://lovcombo query
		case	59://superbox lovcombo query
			buf.append("$:'el-select',filterable:!0,options:[");//static combo
			if ((fc.getControlTip()==6 || fc.getControlTip()==8 ||fc.getControlTip()==58) && cellResult.getLookupListValues() != null) {
				boolean b1=false;
				
				for (W5Detay p : (List<W5Detay>) cellResult
						.getLookupListValues()) {
					if (b1)
						buf.append(",");
					else
						b1 = true;
					buf.append("{id:'").append(p.getVal()).append("',dsc:'")
							.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache
									.get2(formResult.getScd(), p.getDsc())
									: p.getDsc()).append("'");
					buf.append("}");
				}
			} else if ((fc.getControlTip()==7 || fc.getControlTip()==15 ||fc.getControlTip()==59) && cellResult.getLookupQueryResult().getData() != null){
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
								.append(f.getPostProcessTip() == 2 ? LocaleMsgCache
										.get2(formResult.getScd(),
												z.toString()) : GenericUtil
										.stringToJS(z.toString()))
								.append("'");
					}
					buf.append("}");
				}
			}
			buf.append("], clearable:").append(fc.getNotNullFlag()==0);
			if(fc.getControlTip()==8 ||fc.getControlTip()==58 || fc.getControlTip()==15 ||fc.getControlTip()==59)buf.append(",multiple:true");
		break; 
		
		case	9://combo query remote
			buf.append("$:'el-select',filterable:!0,options:options.").append(fc.getDsc()).append("||[]");
			break;
		case	10://advanced select: TODO ilk geldiginde oo loadOptions'ta atanacak
			if (value != null && cellResult.getLookupQueryResult() != null && cellResult.getLookupQueryResult().getData().size() > 0) {
				Object[] oo = cellResult.getLookupQueryResult().getData().get(0);
			}
			
			buf.append("$:'el-select', remote:true, filterable:!0, options:options.").append(fc.getDsc()).append("||[], placeholder:'").append(LocaleMsgCache.get2(0, xlocale, "autocomplete_placeholder"))
				.append("', remoteMethod:(query)=>{if(!query)callback();else iwb.request({url:'ajaxQueryData?_renderer=vue2&_qid=").append(fc.getLookupQueryId());
			if(!GenericUtil.isEmpty(fc.getLookupIncludedParams()))buf.append("&").append(fc.getLookupIncludedParams());
			buf.append("', params:{xdsc:query}, xxx:this, successCallback:function(result, cfg){cfg.xxx.options.").append(fc.getDsc()).append("=result.data;}});},clearable:").append(fc.getNotNullFlag()==0);
		break; // advanced select
		case	23://treecombo(local)
		case	26://lovtreecombo(local) TODO
			buf.append("type:'text'");
		break; // 		

		case	25://textarea(ozel tanimlama)
		case	41://codemirror
		case	11:
			buf.append("type:'textarea', autosize:{minRows:2}");
			break; // textarea
//		{ view:"label", label:'Fill the form below to access <br>the main datacore.'
		
		case	71://file attachment
			buf.append("type:'text'");
			return buf;
		
		default:			
			buf.append("type:'text'");
			break;
		
		
		}
		buf.append(",name:'").append(fc.getDsc()).append("'");//,id:'").append(fc.getDsc()).append("'");
		
		if(fc.getControlTip()!=3 && fc.getControlTip()!=4 && fc.getControlTip()!=5){
			if(fc.getNotNullFlag()!=0)buf.append(",required:true, class:'xrequired'");
			else buf.append(",clearable:true");
			if(fc.getMinLength()!=null && fc.getMinLength()>0)buf.append(",minlength:").append(fc.getMinLength());
			if(fc.getMaxLength()!=null && fc.getMaxLength()>0)buf.append(",maxlength:").append(fc.getMaxLength());
		}
		buf.append(", label:'").append(LocaleMsgCache.get2(formResult.getScd(), fc.getLocaleMsgKey())).append("'");

		if(formResult!=null){ //FORM
			switch(fc.getControlTip()){
			case	5:
				buf.append(",value:values.").append(fc.getDsc());
			break; 
			case	3:case	4:
				buf.append(",value:values.").append(fc.getDsc()).append(" ? 1*values.").append(fc.getDsc()).append(":''");
			break; 
			default:buf.append(",value:values.").append(fc.getDsc()).append("||''");
			}
		//	if(true)buf.append(",on:{onChange:function(newv, oldv){this.validate();}}");
		//	buf.append(",on:{change:(v)=>{console.log('change,',v);values.").append(fc.getDsc()).append("=v;this.values=values;}}");
			
		} else { //grid/toolbar/list/gantt
			buf.append(",_control:").append(fc.getControlTip());
			switch(fc.getControlTip()){
				case	5:
					buf.append(",value:").append(GenericUtil.uInt(value)>0);
					break; 
				default:buf.append(",value:'").append(GenericUtil.stringToJS(value)).append("'");
			}
		}
		if(!GenericUtil.isEmpty(fc.getExtraDefinition()))buf.append(fc.getExtraDefinition());

		buf.append("}");

		return buf;
	}

	private StringBuilder serializeToolbarItems(Map scd,
			List<W5ObjectToolbarItem> items, boolean mediumButtonSize) {
		if (items == null || items.size() == 0)
			return null;
		String xlocale = (String) scd.get("locale");
		int customizationId = (Integer) scd.get("customizationId");
		StringBuilder buttons = new StringBuilder();
		boolean b = false;
		int itemCount = 0;
		for (W5ObjectToolbarItem toolbarItem : items)
			if (GenericUtil.accessControl(scd, toolbarItem.getAccessViewTip(),
					toolbarItem.getAccessViewRoles(),
					toolbarItem.getAccessViewUsers())) {
				if (b)
					buttons.append(",");
				else
					b = true;
				if (toolbarItem.getItemTip() == 0
						|| toolbarItem.getItemTip() == 100) { // yok(0): button + tooltip;button(100) icon+text
					if (toolbarItem.getDsc().equals("-"))buttons.append("{ $template:'Spacer' }"); else 
					if (toolbarItem.getDsc().equals("->"))
						buttons.append("{}");
					else if (toolbarItem.getObjectTip() == 15) {// form toolbar
																// ise
						buttons.append("{view:'button', value:'")
								.append(LocaleMsgCache.get2(scd, toolbarItem.getLocaleMsgKey()))
								.append("',");
						if (mediumButtonSize)
							buttons.append("iconAlign: 'top', scale:'medium', style:{margin: '0px 5px 0px 5px'},");
						buttons.append("iconCls:'")
								.append(toolbarItem.getImgIcon())
								.append("', click:function(a,b,c){\n")
								.append(LocaleMsgCache.filter2(
										customizationId, xlocale,
										toolbarItem.getCode())).append("\n}}");
						itemCount++;
					} else {
						buttons.append("{view:'button', width:35, type:'icon', icon:'cube', tooltip:'")
								.append(LocaleMsgCache.get2(scd, toolbarItem.getLocaleMsgKey()))
								.append("', click:function(a,b,c){\n")
								.append(LocaleMsgCache.filter2(
										customizationId, xlocale,
										toolbarItem.getCode())).append("\n}}");
						itemCount++;
					}
				} else { // controlTip
					W5FormCell cell = new W5FormCell();
					cell.setControlTip(toolbarItem.getItemTip());
					cell.setLookupQueryId(toolbarItem.getLookupQueryId());
					cell.setLocaleMsgKey(toolbarItem.getLocaleMsgKey());
					cell.setDsc(toolbarItem.getDsc());
					cell.setNotNullFlag((short) 1);
					cell.setExtraDefinition(",tooltip:'"
							+ LocaleMsgCache.get2(scd,
									toolbarItem.getLocaleMsgKey())
							+ "',ref:'../" + toolbarItem.getDsc() + "'");
					if (toolbarItem.getCode() != null
							&& toolbarItem.getCode().length() > 2)
						cell.setExtraDefinition(cell.getExtraDefinition() + ","
								+ toolbarItem.getCode());
					W5FormCellHelper cellResult = new W5FormCellHelper(cell);
					if (toolbarItem.getItemTip() == 6
							|| toolbarItem.getItemTip() == 8
							|| toolbarItem.getItemTip() == 14) {
						W5LookUp lu = FrameworkCache.getLookUp(scd,
								toolbarItem.getLookupQueryId());
						List<W5LookUpDetay> dl = new ArrayList<W5LookUpDetay>(
								lu.get_detayList().size());
						for (W5LookUpDetay dx : lu.get_detayList()) {
							W5LookUpDetay e = new W5LookUpDetay();
							e.setVal(dx.getVal());
							e.setDsc(LocaleMsgCache.get2(scd, dx.getDsc()));
							dl.add(e);
						}
						cellResult.setLookupListValues(dl);
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
				if (menuItem.getDsc().equals("-"))
					buttons.append("'-'");
				else {

					/*
					 * Burası Bu şekilde değiştirilecek
					 * buttons.append("{text:'")
					 * .append(PromisLocaleMsg.get2(customizationId, xlocale,
					 * menuItem
					 * .getLocaleMsgKey())).append("', ref:'../").append(
					 * menuItem.getDsc()).append("'");
					 * if(!PromisUtil.isEmpty(menuItem
					 * .getImgIcon()))buttons.append
					 * (",iconCls:'").append(menuItem.getImgIcon()).append("'");
					 * buttons.append(",handler:function(a,b,c){\n")
					 * .append(menuItem.getCode()).append("\n}}"); itemCount++;
					 */

					buttons.append("{text:'")
							.append(LocaleMsgCache.get2(customizationId,
									xlocale, menuItem.getLocaleMsgKey()))
							.append("', ref:'../").append(menuItem.getDsc())
							.append("'");
					if (!GenericUtil.isEmpty(menuItem.getImgIcon()))
						buttons.append(",cls:'").append(menuItem.getImgIcon())
								.append("'");
					buttons.append(",handler:function(a,b,c){\n")
							.append(menuItem.getCode()).append("\n}}");
					itemCount++;
				}

			}
		return itemCount > 0 ? buttons : null;
	}
	
	public StringBuilder serializeGridRecordCreate(W5GridResult gridResult) {
		StringBuilder html = new StringBuilder();
		if(true)return html; 
		html.append(",\n record:Ext.data.Record.create([");
		boolean b = false;
		for (W5GridColumn gc : (List<W5GridColumn>) gridResult.getGrid()
				.get_gridColumnList())
			if (gc.get_formCell() != null) {
				if (b)
					html.append(",\n");
				else
					b = true;
				html.append("{name: '").append(gc.get_queryField().getDsc())
						.append("'");
				if (gc.get_queryField().getFieldTip() > 2)
					html.append(",type:'")
							.append(FrameworkSetting.sortMap[gc.get_queryField()
									.getFieldTip()]).append("'");
				html.append("}");
			}
		html.append("]),\n initRecord:{");
		b = false;
		for (W5GridColumn gc : (List<W5GridColumn>) gridResult.getGrid()
				.get_gridColumnList())
			if (gc.get_formCell() != null) {
				Object obz = null;
				switch (gc.get_formCell().getInitialSourceTip()) {
				case 0:// yok-sabit
					obz = gc.get_formCell().getInitialValue();
					break;
				case 1:// request
					obz = gridResult.getRequestParams().get(
							gc.get_formCell().getInitialValue());
					break;
				case 2:
					Object o = gridResult.getScd().get(
							gc.get_formCell().getInitialValue());
					obz = o == null ? null : o.toString();
					break;
				case 3:// app_setting
					obz = FrameworkCache.getAppSettingStringValue(gridResult
							.getScd(), gc.get_formCell().getInitialValue());
					break;
				case 4:// SQL TODO
						// runSQLQuery2Map(PromisUtil.filterExt(cell.getInitialValue(),
						// scd, requestParams).toString(), null, null);
					break;
				case 5:// CustomJS(Rhino) TODO
					break;
				}
				if (obz != null) {
					if (b)
						html.append(",\n");
					else
						b = true;
					html.append(gc.get_queryField().getDsc()).append(":'")
							.append(obz).append("'");
				}
			}
		html.append("}");
		return html;
	}

	public StringBuilder serializeCard(W5CardResult dataViewResult) {
		String xlocale = (String) dataViewResult.getScd().get("locale");
		int customizationId = (Integer) dataViewResult.getScd().get(
				"customizationId");
		W5Card d = dataViewResult.getCard();
		StringBuilder buf = new StringBuilder();
		buf.append("var ")
				.append(d.getDsc())
				.append("={cardId:")
				.append(d.getDataViewId())
				.append(",name:'")
				.append(LocaleMsgCache.get2(customizationId, xlocale,
						d.getLocaleMsgKey()))
				.append("'")
				.append(",_url:'ajaxQueryData?.w='+_webPageId+'&_qid=")
				.append(d.getQueryId()).append("&_dvid=")
				.append(d.getDataViewId());

		if (d.getDefaultPageRecordNumber() != 0)
			buf.append("&firstLimit=").append(d.getDefaultPageRecordNumber());
		buf.append("'");
		if (d.getDefaultWidth() != 0)
			buf.append(", defaultWidth:").append(d.getDefaultWidth());
		if (d.getDefaultHeight() != 0)
			buf.append(", defaultHeight:").append(d.getDefaultHeight());
		if (dataViewResult.getSearchFormResult() != null) {
			buf.append(",\n searchForm:").append(
					serializeGetForm(dataViewResult.getSearchFormResult()));
		}
		if (!GenericUtil.isEmpty(d.get_toolbarItemList())) { // extra buttonlari
															// var mi yok mu?
			StringBuilder buttons = serializeToolbarItems(
					dataViewResult.getScd(), d.get_toolbarItemList(), false);
			if (buttons != null && buttons.length() > 1) {
				buf.append(",\n extraButtons:[").append(buttons).append("]");
			}
		}

		if (d.getDefaultPageRecordNumber() != 0)
			buf.append(",\n pageSize:").append(d.getDefaultPageRecordNumber());
		// buf.append(",\n tpl:'<tpl for=\".\">").append(PromisUtil.stringToJS(d.getTemplateCode())).append("</tpl>',\nautoScroll:true,overClass:'x-view-over',itemSelector:'table.grid_detay'};\n");
		buf.append(",\n tpl:\"")
				.append(GenericUtil.filterExt(GenericUtil.stringToJS2(d.getTemplateCode()),
						dataViewResult.getScd(),
						dataViewResult.getRequestParams(), null))
				.append("\"};");
		if (!GenericUtil.isEmpty(d.getJsCode())) {
			buf.append("\ntry{")
					.append(GenericUtil.filterExt(d.getJsCode(),
							dataViewResult.getScd(),
							dataViewResult.getRequestParams(), null))
					.append("\n}catch(e){")
					.append(FrameworkSetting.debug ? "if(confirm('ERROR card.JS!!! Throw? : ' + e.message))throw e;"
							: "alert('System/Customization ERROR : ' + e.message)");
			buf.append("}\n");
		}
		return buf;
	}

	public StringBuilder serializeListView(W5ListViewResult listViewResult) {
		String xlocale = (String) listViewResult.getScd().get("locale");
		int customizationId = (Integer) listViewResult.getScd().get(
				"customizationId");
		W5List d = listViewResult.getListView();
		StringBuilder buf = new StringBuilder();
		buf.append("var ")
				.append(d.getDsc())
				.append("={listId:")
				.append(d.getListId())
				.append(",name:'")
				.append(LocaleMsgCache.get2(customizationId, xlocale,
						d.getLocaleMsgKey()))
				.append("'")
				.append(",store: new Ext.data.JsonStore({url:'ajaxQueryData?.t='+_page_tab_id+'&.w='+_webPageId+'&_qid=")
				.append(d.getQueryId()).append("&_lvid=").append(d.getListId());

		if (d.getDefaultPageRecordNumber() != 0)
			buf.append("&firstLimit=").append(d.getDefaultPageRecordNumber())
					.append("',remoteSort:true,");
		else
			buf.append("',");
		buf.append(
				serializeQueryReader(d.get_query().get_queryFields(), d
						.get_pkQueryField().getDsc(), null, null, 0, d
						.get_mainTable(), listViewResult.getScd())).append(
				",listeners:{loadexception:promisLoadException}})");
		if (d.getDefaultWidth() != 0)
			buf.append(",\n defaultWidth:").append(d.getDefaultWidth());
		if (d.getDefaultHeight() != 0)
			buf.append(",\n defaultHeight:").append(d.getDefaultHeight());
		switch (d.getSelectionTip()) {
		// case 0:buf.append(",\n singleSelect:false");break;
		case 1:
			buf.append(",\n singleSelect:true");
			break;
		case 2:
			buf.append(",\n multiSelect:true");
			break;
		}

		if (d.getDefaultPageRecordNumber() != 0)
			buf.append(",\n pageSize:").append(d.getDefaultPageRecordNumber());
		if (!GenericUtil.isEmpty(d.get_toolbarItemList())) { // extra buttonlari
															// var mi yok mu?
			StringBuilder buttons = serializeToolbarItems(
					listViewResult.getScd(), d.get_toolbarItemList(), false);
			if (buttons != null && buttons.length() > 1) {
				buf.append(",\n extraButtons:[").append(buttons).append("]");
			}
		}
		// buf.append(",\n tpl:'<tpl for=\".\">").append(PromisUtil.stringToJS(d.getTemplateCode())).append("</tpl>',\nautoScroll:true,overClass:'x-view-over',itemSelector:'table.grid_detay'};\n");
		// buf.append(",\n tpl:'").append(PromisUtil.stringToJS(d.getTemplateCode())).append("',\nautoScroll:true,overClass:'x-view-over',itemSelector:'table.grid_detay'};\n");
		buf.append("}\n");
		if (!GenericUtil.isEmpty(d.getJsCode())) {
			buf.append("\ntry{")
					.append(GenericUtil.filterExt(d.getJsCode(),
							listViewResult.getScd(),
							listViewResult.getRequestParams(), null))
					.append("\n}catch(e){")
					.append(FrameworkSetting.debug ? "if(confirm('ERROR listView.JS!!! Throw? : ' + e.message))throw e;"
							: "alert('System/Customization ERROR : ' + e.message)");
			buf.append("}\n");
		}
		buf.append(serializeListColumns(listViewResult));

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
				.append(g.getGridId()).append(",queryId:").append(g.getQueryId());
		if (!gridResult.isViewLogMode() && g.getSelectionModeTip()!=0){
			if(g.getSelectionModeTip()==2 || g.getSelectionModeTip()==3)
				buf.append(", multiselect:true");
		}
		buf.append(",keyField:'").append(g.get_pkQueryField().getDsc()).append("'");
		
		if (gridResult.getExtraOutMap() != null
				&& !gridResult.getExtraOutMap().isEmpty()) {
			buf.append(",\n extraOutMap:")
					.append(GenericUtil.fromMapToJsonString(gridResult
							.getExtraOutMap()));
		}

			
		if (FrameworkSetting.liveSyncRecord && g.get_viewTable() != null && g.get_viewTable().getLiveSyncFlag() != 0)
			buf.append(",\n liveSync:true");
		if (g.getDefaultWidth() != 0)
			buf.append(",\n defaultWidth:").append(g.getDefaultWidth());
		if (gridResult.isViewLogMode())
			buf.append(",\n defaultHeight:").append(
					FrameworkCache.getAppSettingIntValue(scd,
							"log_default_grid_height"));
		else {
			if (g.getSelectionModeTip() == 2 || g.getSelectionModeTip() == 3) // multi Select
				buf.append(",\n ,selectRow:{mode: 'checkbox',clickToSelect: true}");
/*			else if (g.getSelectionModeTip() == 5 && g.get_detailView() != null) // promis.js'de
																					// halledilmek
																					// uzere
				buf.append(",\n detailDlg:true");*/
			if (g.getDefaultHeight() > 0)
				buf.append(",\n defaultHeight:").append(g.getDefaultHeight());

			buf.append(",\n gridReport:").append(FrameworkCache.roleAccessControl(scd, 105));
		}
		buf.append(",\n loading:!0, displayInfo:").append(g.getDefaultPageRecordNumber()>0);
		
		if(FrameworkCache.getAppSettingIntValue(customizationId, "toplu_onay") == 1 && g.get_workflow() != null){
			buf.append(",\n approveBulk:true");
			if(g.get_workflow().getApprovalRequestTip() == 2){ // Onay manuel mi başlatılacak ?
				buf.append(",\n btnApproveRequest:true");
			}
		}
		if (!GenericUtil.isEmpty(g.get_crudFormSmsMailList())) {
			buf.append(",\n formSmsMailList:[");
			boolean b = false;
			for (W5FormSmsMail fsm : g.get_crudFormSmsMailList())
				if (((fsm.getSmsMailTip() == 0 && FrameworkSetting.sms && FrameworkCache
						.getAppSettingIntValue(customizationId, "sms_flag") != 0) || (fsm
						.getSmsMailTip() != 0 && FrameworkSetting.mail && FrameworkCache
						.getAppSettingIntValue(customizationId, "mail_flag") != 0))
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
							.append(fsm.getSmsMailTip() == 0 ? "[<b>SMS</b>] "
									: "[<b>"
											+ (LocaleMsgCache.get2(
													customizationId, xlocale,
													"email_upper")) + "</b>] ")
							.append(GenericUtil.stringToJS(LocaleMsgCache.get2(
									customizationId, xlocale, fsm.getDsc())))
							.append("\",smsMailTip:")
							.append(fsm.getSmsMailTip()).append("}");
				}
			buf.append("]");
		}
		if (!GenericUtil.isEmpty(g.get_crudFormConversionList())) {
			buf.append(",\n formConversionList:[")
					.append(serializeManualConversions(scd,
							g.get_crudFormConversionList())).append("]");
		}
		

		buf.append(",striped:true,hover:true,bordered:false, name:'").append(LocaleMsgCache.get2(customizationId, xlocale,
						g.getLocaleMsgKey())).append("',\n id:'")
				.append(GenericUtil.getNextId("ng")).append("',\n listeners:{}");

		
			buf.append(",\n _url:'ajaxQueryData?_renderer=vue2&.t='+_page_tab_id+'&.w='+_webPageId+'&_qid=")
					.append(g.getQueryId()).append("&_gid=")
					.append(g.getGridId());

			if (gridResult.isViewLogMode() || g.getDefaultPageRecordNumber() != 0)
				buf.append("&firstLimit=").append(gridResult.isViewLogMode() ? FrameworkCache
								.getAppSettingIntValue(scd,"log_default_record_per_page") : g
								.getDefaultPageRecordNumber())
						.append("',remote:{sort: true}"); //pagination: true, filter: true, sort: true, cellEdit: true
			else
				buf.append("'");

			
			

		if (gridResult.isViewLogMode() || g.getDefaultPageRecordNumber() != 0)
			buf.append(",\n pageSize:").append(
					gridResult.isViewLogMode() ? FrameworkCache
							.getAppSettingIntValue(scd,
									"log_default_record_per_page") : g
							.getDefaultPageRecordNumber());

		if (false && gridResult.getSearchFormResult() != null) {
			buf.append(",\n searchForm:").append(serializeGetForm(gridResult.getSearchFormResult()));
		}
		
		if(g.getDefaultPageRecordNumber()>0 && g.get_query()!=null && g.get_query().get_queryParams()!=null){
			for(W5QueryParam qp:g.get_query().get_queryParams())if(qp.getDsc().equals("xsearch"))
				buf.append(",\n globalSearch:true");
		}
		
		if (!gridResult.isViewLogMode()) {

			if (g.get_defaultCrudForm() != null) { // insert ve delete
													// buttonlari var mi yok mu?
				W5Table t = FrameworkCache.getTable(scd, g.get_defaultCrudForm()
						.getObjectId());// g.get_defaultCrudForm().get_sourceTable();
				boolean insertFlag = GenericUtil.accessControl(scd,
						t.getAccessInsertTip(), t.getAccessInsertRoles(),
						t.getAccessInsertUsers());
				buf.append(",\n crudFormId:")
						.append(g.getDefaultCrudFormId())
						.append(",\n crudTableId:")
						.append(t.getTableId())
						.append(",\n crudFlags:{insert:")
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
						&& FrameworkCache.roleAccessControl(scd, 108))
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
							"row_based_security_flag") != 0
							&& (Integer) scd.get("userTip") != 3
							&& t.getAccessTips() != null
							&& t.getAccessTips().length() > 0)
						buf.append(",\n accessControlFlag:true");
					if (FrameworkCache.getAppSettingIntValue(customizationId,
							"file_attachment_flag") != 0
							&& t.getFileAttachmentFlag() != 0
							&& FrameworkCache.roleAccessControl(scd, 101)
							&& FrameworkCache.roleAccessControl(scd, 102))
						buf.append(",\n fileAttachFlag:true");
					if (FrameworkCache.getAppSettingIntValue(customizationId,
							"make_comment_flag") != 0
							&& t.getMakeCommentFlag() != 0
							&& FrameworkCache.roleAccessControl(scd, 103))
						buf.append(",\n makeCommentFlag:true");
				
				
					if (FrameworkCache.roleAccessControl(scd,  11))
						buf.append(",\n bulkUpdateFlag:true");
					if (FrameworkCache.roleAccessControl(scd, 104))
						buf.append(",\n bulkEmailFlag:true");
				}
			}

			if (!GenericUtil.isEmpty(g.get_toolbarItemList())) { // extra
																// buttonlari
																// var mi yok
																// mu?
				StringBuilder buttons = serializeToolbarItems(scd,
						g.get_toolbarItemList(), false);
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
			List<W5QueryField> queryFieldList, String id,
			List<W5TableField> extendedTableFields,
			List<W5QueryField> postProcessQueryFieldList, int processTip,
			W5Table t, Map scd) {
		StringBuilder html = new StringBuilder();
		html.append("root:'data',totalProperty:'browseInfo.totalCount',id:'")
				.append(id).append("',fields:[");
		boolean b = false;
		for (W5QueryField f : queryFieldList) {
			if (f.getMainTableFieldId() != 0 && t != null && scd != null) {
				W5TableField tf = t.get_tableFieldMap().get(
						f.getMainTableFieldId());
				if (tf != null
						&& (
						(tf.getAccessViewUserFields()==null && !GenericUtil.accessControl(scd, tf.getAccessViewTip(), tf.getAccessViewRoles(), tf.getAccessViewUsers()))))
					continue;
			}
			if (b)
				html.append(",\n");
			else
				b = true;
			html.append("{name:'");
			switch (f.getPostProcessTip()) {
			case 9:
				html.append("_").append(f.getDsc());
				break;
			case 6:
				html.append(f.getDsc().substring(1));
				break;
			default:
				html.append(f.getDsc());
			}
			html.append("'");
			if (f.getFieldTip() > 2)
				html.append(",type:'")
						.append(FrameworkSetting.sortMap[f.getFieldTip()])
						.append("'");
			if (f.getFieldTip() == 2)
				html.append(",type:'date',dateFormat:'d/m/Y h:i:s'");

			if (f.getPostProcessTip() >= 10)
				html.append("},{name:'").append(f.getDsc()).append("_qw_'");
			html.append("}");
		}
		if (!GenericUtil.isEmpty(extendedTableFields))
			for (W5TableField f : extendedTableFields) {
				if (scd != null
						&& !GenericUtil.accessControl(scd, f.getAccessViewTip(),
								f.getAccessViewRoles(), f.getAccessViewUsers()))
					continue;
				html.append(",\n{name:'");
				html.append(f.getDsc()).append("'");
				if (f.getFieldTip() > 2)
					html.append(",type:'")
							.append(FrameworkSetting.sortMap[f.getFieldTip()])
							.append("'");
				if (f.getFieldTip() == 2)
					html.append(",type:'date',dateFormat:'d/m/Y h:i:s'");
				if (f.getDefaultLookupTableId() > 0)
					html.append("},{name:'").append(f.getDsc()).append("_qw_'");
				html.append("}");
			}
		if (!GenericUtil.isEmpty(postProcessQueryFieldList))
			for (W5QueryField f : postProcessQueryFieldList) {
				html.append(",\n{name:'").append(f.getDsc()).append("',type:'int'}");
				
				if(f.getDsc().equals(FieldDefinitions.queryFieldName_Comment) && FrameworkCache.getAppSettingIntValue(scd, "make_comment_summary_flag")!=0)
					html.append(",{name:'").append(FieldDefinitions.queryFieldName_CommentExtra).append("'}");
				if (f.getPostProcessTip() > 0)
					html.append(",{name:'").append(f.getDsc()).append("_qw_'}");
				if (f.getPostProcessTip() == 49)
					html.append(",{name:'pkpkpk_arf_id',type:'int'},{name:'app_role_ids_qw_'},{name:'app_user_ids_qw_'}");
			}
		switch (processTip) {
		case 1:// log
			html.append(",\n{name:'").append(FieldDefinitions.tableFieldName_LogId).append("'},{name:'")
			.append(FieldDefinitions.tableFieldName_LogDateTime).append("',type:'date',dateFormat:'d/m/Y h:i:s'},\n{name:'").append(FieldDefinitions.tableFieldName_LogUserId).append("',type:'int'},{name:'").append(FieldDefinitions.tableFieldName_LogUserId).append("_qw_'}");
			break;
		case 2:// parentRecord
			html.append(",\n{name:'").append(FieldDefinitions.queryFieldName_HierarchicalData).append("'}");
			break;
		}
		/*
		 * if(id.equals("xrow_id")){ }
		 */
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
			"fileAttachmentHtml", "commentRenderer", "keywordHtml",
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
							&& c.get_queryField().getPostProcessTip() <90); // post
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
		W5Grid grid = gridResult.getGrid();
		if(dsc==null)dsc= grid.getDsc();
		String xlocale = (String) gridResult.getScd().get("locale");
		int customizationId = (Integer) gridResult.getScd().get(
				"customizationId");
		List<W5GridColumn> oldColumns = grid.get_gridColumnList();
		W5Table viewTable = grid.get_viewTable();
		W5Table crudTable = grid.get_crudTable();
		if (crudTable == null)
			crudTable = viewTable;
		

		List<W5GridColumn> newColumns = new ArrayList();
		StringBuilder bufGrdColumnGroups = new StringBuilder();
		if (grid.getColumnRenderTip() == 1) { // column grouping olacak
		} else { // duz rendering
			for (W5GridColumn c : oldColumns)
				if (c.get_queryField() != null) {
					W5QueryField f = c.get_queryField();
					W5TableField tf = f.getMainTableFieldId() > 0 ? viewTable
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
		}
		if (!gridResult.isViewLogMode() && grid.get_postProcessQueryFields() != null && (gridResult.getRequestParams()==null || GenericUtil.uInt(gridResult.getRequestParams(), "_no_post_process_fields")==0)) {
			boolean gridPostProcessColumnFirst = FrameworkCache.getAppSettingIntValue(customizationId,"grid_post_process_column_first")!=0;
			boolean gridPostProcessCommentFirst = FrameworkCache.getAppSettingIntValue(customizationId,"grid_post_process_comment_first")!=0;
			int x = 0;
			for (W5QueryField f : grid.get_postProcessQueryFields()) {
				if(!f.getDsc().equals("ar_version_no")){
					if (viewTable != null)
						switch (f.getFieldTip()) {
						case 2:// file attachment
						case 7:// picture attachment
							if (!FrameworkCache.roleAccessControl(
									gridResult.getScd(), 101))
								continue;
							break;
						case 6:// mail
							if (!FrameworkCache.roleAccessControl(
									gridResult.getScd(),106))
								continue;
							break;
						}
					W5GridColumn c = new W5GridColumn();
					c.set_queryField(f);
					c.setWidth((short)40);//f.getTabOrder()
					c.setAlignTip((short) 0);
					c.setLocaleMsgKey("");//:("<span class=\"webix_icon fa-"+ FrameworkSetting.postQueryGridImgMap4Webix[f.getFieldTip()]+ "\"></span>")
					c.setVisibleFlag((short) 1);
					String renderer = postQueryMap[f.getFieldTip()];
					c.setRenderer(renderer);
					if(f.getDsc().equals(FieldDefinitions.queryFieldName_Comment) && FrameworkCache.getAppSettingIntValue(customizationId, "make_comment_summary_flag")!=0){
						c.setWidth((short) (f.getTabOrder() + 10));
						c.setSortableFlag((short)1);
					}
					if (f.getDsc().equals(FieldDefinitions.queryFieldName_Approval)) {// approval_record_flag
						c.setWidth((short) (f.getTabOrder() + 100));
						c.setAlignTip((short) 1);
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
		if (gridResult.isViewLogMode()) {// log ile ilgili
			gridResult.setViewReadOnlyMode(true);
			W5QueryField qf_dttm = new W5QueryField();
			qf_dttm.setDsc("log5_dttm");
			W5GridColumn c_dttm = new W5GridColumn();
			c_dttm.set_queryField(qf_dttm);
			c_dttm.setWidth((short) 120);
			c_dttm.setAlignTip((short) 1);
			c_dttm.setLocaleMsgKey("log_dttm");
			c_dttm.setVisibleFlag((short) 1);
			c_dttm.setRenderer("fmtDateTime");
			newColumns.add(0, c_dttm);

			W5QueryField qf_user = new W5QueryField();
			qf_user.setDsc("log5_user_id");
			W5GridColumn c_user = new W5GridColumn();
			c_user.set_queryField(qf_user);
			c_user.setWidth((short) 80);
			c_user.setAlignTip((short) 1);
			c_user.setLocaleMsgKey("log_user");
			c_user.setVisibleFlag((short) 1);
			c_user.setRenderer("gridQwRenderer('log5_user_id')");
			newColumns.add(1, c_user);
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
	
		if(false)for (W5GridColumn c : newColumns)
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


		for (W5GridColumn c : newColumns) if(c.getVisibleFlag() != 0){
			String qds = c.get_queryField().getDsc();

			if (b)
				buf.append(",\n");
			else
				b = true;
			boolean editableFlag = editableColumnSet.contains(c.getQueryFieldId());
			
			buf.append("{label:");
			if (true && !editableFlag) {
				buf.append("'").append(LocaleMsgCache.get2(customizationId, xlocale, c.getLocaleMsgKey())).append("'");
			} else {
				buf.append("h('span',{style:{color:'darkorange'}},'").append(LocaleMsgCache.get2(customizationId, xlocale,c.getLocaleMsgKey())).append("')");
			}
			boolean qwRendererFlag = false;
			boolean boolRendererFlag = false;
			buf.append(", prop: '").append(qds).append("'");
			if(c.getSortableFlag() != 0 && c.get_queryField().getPostProcessTip() <90){
					buf.append(", sortable:!0");
					if(c.get_queryField().getFieldTip()>1){//TODO. custom after string sorting
//						? new String[]{"","string","date","int","int","",""}[c.get_queryField().getFieldTip()] : "server").append("'");
					}
			}
			
			if (c.getAlignTip() != 1)buf.append(", align:'").append(FrameworkSetting.alignMap[c.getAlignTip()]).append("'");// left'ten farkli ise
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
			} else if (c.get_queryField().getPostProcessTip() >= 10
					&& c.get_queryField().getPostProcessTip() <90) {
				if (c.get_formCell() == null || !editableFlag) {
					if (FrameworkSetting.chat && (c.get_queryField().getPostProcessTip() == 20 || c.get_queryField().getPostProcessTip() == 53)) // user lookup ise
						buf.append(", formatter:gridUserRenderer");// browser renderer ise
					else if (c.get_queryField().getPostProcessTip() == 12) // table lookup ise
						buf.append(", formatter:gridQwRendererWithLink(").append(c.get_queryField().getLookupQueryId()).append(")");// browser renderer ise
					else {
					/*	boolean bx = true;
						if(c.get_queryField().getPostProcessTip() < 11){
							W5LookUp lu = FrameworkCache.getLookUp(customizationId, c.get_queryField().getLookupQueryId());
							if(lu!=null && lu.getCssClassFlag()!=0){
								bx = false;
								buf.append(", formatter:function(row){var badgeMap={'':false");
								for(W5LookUpDetay lud:lu.get_detayList())if(!GenericUtil.isEmpty(lud.getParentVal()))buf.append(",'").append(lud.getVal()).append("':'").append(lud.getParentVal()).append("'");
								buf.append("};var badge=badgeMap[row.").append(qds).append("||''];console.log('badge',badge,this,this._c);return !badge?{render(q){return 'ahmet';return q('span',{class:'badge badge-'+badge},row.").append(qds).append("_qw_)}}:row.").append(qds).append("_qw_;}");// browser renderer ise
							}
						}
						if(bx)*/
						buf.append(", formatter:function(row){return row.").append(qds).append("_qw_;}");// browser renderer ise
					}
//						buf.append(", formatter:function(row){return row.").append(qds).append("_qw_;}");// browser renderer ise
					qwRendererFlag = true;
				} else
					switch (c.get_formCell().getControlTip()) {
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
			} else if (qds.length() > 3 && qds.indexOf("_dt") == qds.length() - 3)
				buf.append(", renderer:strShortDate");// browser renderer ise
			else if (qds.length() > 5 && qds.indexOf("_dttm") == qds.length() - 5){
				buf.append(", renderer:strDateTime").append(FrameworkCache.getAppSettingIntValue(0, "fmt_date_time_ago_flag")!=0 ?"Ago":"");// browser renderer ise
			} else if ((qds.length() > 5
					&& qds.endsWith("_flag")) || (qds.length() > 3
							&& qds.startsWith("is_"))) {
				buf.append(", formatter:disabledCheckBoxHtml");// browser renderer ise
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
							if (f.getPostProcessTip() == 9)
								buf.append("_");
							if (f.getFieldTip() == 5) {
								buf.append(f.getDsc()).append(":")
										.append(GenericUtil.uInt(obj) != 0);
								continue;
							}
							buf.append(f.getPostProcessTip() == 6 ? f.getDsc().substring(1):f.getDsc()).append(":'");
							if (obj != null) {
								switch (f.getPostProcessTip()) { // queryField
																	// PostProcessTip
								case 8:
									buf.append(GenericUtil.stringToHtml(obj));
									break;
								case 20: // user LookUp
									buf.append(obj)
											.append("',")
											.append(f.getDsc())
											.append("_qw_:'")
											.append(UserUtil.getUserName(
													GenericUtil.uInt(obj)));
									break;
								case 21: // users LookUp
									String[] ids = ((String) obj).split(",");
									if (ids.length > 0) {
										String res = "";
										for (String s : ids) {
											res += ","
													+ UserUtil.getUserName(
															GenericUtil.uInt(s));
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
											.append(UserUtil.getUserDsc(
													GenericUtil.uInt(obj)));
									break;
								case 54: // Users LookUp Real Name
									String[] ids11 = ((String) obj).split(",");
									if (ids11.length > 0) {
										String res = "";
										for (String s : ids11) {
											res += ","
													+ UserUtil.getUserDsc(
															GenericUtil.uInt(s));
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
									String[] objs = f.getPostProcessTip() == 11 ? ((String) obj)
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

	public StringBuilder serializeTreeQueryData(W5QueryResult queryResult) {
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
		String children = queryResult.getRequestParams().get("_children") != null ? queryResult
				.getRequestParams().get("_children") : "data";
		int customizationId = (Integer) queryResult.getScd().get(
				"customizationId");
		String xlocale = (String) queryResult.getScd().get("locale");
		StringBuilder buf = new StringBuilder();
		boolean json = GenericUtil.uInt(queryResult.getRequestParams(), "_json")!=0;
		boolean dismissNull = queryResult.getRequestParams()!=null && GenericUtil.uInt(queryResult.getRequestParams(), "_dismissNull")!=0;
		if(json)buf.append("{\"success\":true,\"data\":");
		if (queryResult.getErrorMap().isEmpty()) {
			buf.append("[");
//			int levelField = -1;
			int idField = -1;
			int parentField = -1;
			if (queryResult.getNewQueryFields() != null) {
				for (W5QueryField field : queryResult.getNewQueryFields()){
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
					for (W5QueryField field : queryResult.getNewQueryFields()){
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
							queryResult.getQueryId(), GenericUtil.replaceSql(
									queryResult.getExecutedSql(),
									queryResult.getSqlParams()),
							"TreeQueryField does\"nt exist: [id || parent_id]", null);
				}

				List<StringBuilder> treeData = new ArrayList();
				Map<String, List> mapOfParent = new HashMap<String, List>();
				
				List<Object[]> datas = queryResult.getData();
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
						for (W5QueryField f : queryResult.getNewQueryFields()) {
							Object obj = o[f.getTabOrder() - 1];
							if(dismissNull && obj==null)continue;
							if (b)
								buf2.append(",");
							else
								b = true;
							buf2.append("\"");
							if (f.getPostProcessTip() == 9)
								buf2.append("_");
							if (f.getFieldTip() == 5) {
								buf2.append(f.getDsc()).append("\":")
										.append(GenericUtil.uInt(obj) != 0);
								continue;
							}
							if(f.getDsc().equals("xtext") || f.getDsc().equals("text"))buf2.append("value\":");//hack for webix
							else buf2.append(f.getPostProcessTip() == 6 ? f.getDsc().substring(1):f.getDsc()).append("\":");
							if (f.getFieldTip() != 8)
								buf2.append("\"");
							else {
								buf2.append("{");
							} // JSON ise başka
							if (obj != null) {
								switch (f.getPostProcessTip()) { // queryField
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
													+ UserUtil.getUserName(
															GenericUtil.uInt(s));
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
											.append(UserUtil.getUserDsc(
													GenericUtil.uInt(obj)));
									break;
								case 54: // Users LookUp Real Name
									String[] ids11 = ((String) obj).split(",");
									if (ids11.length > 0) {
										String res = "";
										for (String s : ids11) {
											res += ","
													+ UserUtil.getUserDsc(
															GenericUtil.uInt(s));
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
											customizationId, xlocale,
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
											queryResult.getScd(),
											f.getLookupQueryId());
									if (lookUp == null)
										break;
									buf2.append("\",\"").append(f.getDsc())
											.append("_qw_\":\"");
									String[] objs = f.getPostProcessTip() == 11 ? ((String) obj)
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
															customizationId,
															xlocale, s);
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
												.append(FrameworkCache.getWorkflow(queryResult.getScd(),f.getLookupQueryId())
														.get_approvalStepMap()
														.get(id2).getDsc());
									break;
								
								default:
									buf2.append(GenericUtil.stringToJS2(obj
											.toString()));
								}
							}
							if (f.getFieldTip() != 8)
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
			
			if(json)buf.append(",\n\"pos\":0, \"total_count\":").append(queryResult.getFetchRowCount()).append("}");

			return buf;
		} else {
			return buf
					.append("{\"success\":false,\"errorType\":\"validation\",\n\"errors\":")
					.append(serializeValidatonErrors(queryResult.getErrorMap(),
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

	public StringBuilder serializeQueryData(W5QueryResult queryResult) {
		if (queryResult.getQuery().getQueryTip() == 10 || (queryResult.getRequestParams()!=null && GenericUtil.uInt(queryResult.getRequestParams(), "_tqd")!=0) )
			return serializeTreeQueryData(queryResult);
		if (queryResult.getQuery().getQueryTip() == 14)
			return serializeTreeQueryRemoteData(queryResult);
		int customizationId = (Integer) queryResult.getScd().get("customizationId");
		String xlocale = (String) queryResult.getScd().get("locale");
		String userIdStr = queryResult.getScd().containsKey("userId") ? queryResult.getScd().get("userId").toString() : null;
		List datas = queryResult.getData();
		StringBuilder buf = new StringBuilder();
		boolean convertDateToStr = queryResult.getRequestParams()!=null && GenericUtil.uInt(queryResult.getRequestParams(), "_cdds")!=0; 
		buf.append("{\"success\":").append(GenericUtil.isEmpty(queryResult.getErrorMap()))
				.append(",\"queryId\":").append(queryResult.getQueryId())
				.append(",\"execDttm\":\"")
				.append(GenericUtil.uFormatDateTime(new Date())).append("\"");
		if (GenericUtil.isEmpty(queryResult.getErrorMap())) {
			boolean dismissNull = queryResult.getRequestParams()!=null && queryResult.getRequestParams().containsKey("_dismissNull");
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
					for (W5QueryField f : queryResult.getNewQueryFields()) {
						Object obj = isMap ? ((Map)o).get(f.getDsc()) : ((Object[])o)[f.getTabOrder() - 1];
						if(obj==null && dismissNull)continue;
						if (b)
							buf.append(",");
						else
							b = true;
						if (f.getPostProcessTip() == 9)
							buf.append("\"_");
						else
							buf.append("\"");
						buf.append(f.getPostProcessTip() == 6 ? f.getDsc()
								.substring(1) : f.getDsc());
						if (f.getFieldTip() == 5) {// boolean
							buf.append("\":").append(GenericUtil.uInt(obj) != 0);
							continue;
						}
						if (f.getFieldTip() == 6) {// auto
							buf.append("\":");
							if (obj == null || obj.toString().equals("0"))
								buf.append("null");
							else if (GenericUtil.uInt(obj) != 0)
								buf.append(obj);
							else
								buf.append("\"").append(obj).append("\"");
							continue;
						} else if (convertDateToStr && f.getFieldTip() == 2 && obj!=null && (obj instanceof java.sql.Timestamp || obj instanceof java.util.Date)) {// date 
							buf.append("\":\"").append(obj instanceof java.sql.Timestamp ? GenericUtil.uFormatDateTime((java.sql.Timestamp)obj) : GenericUtil.uFormatDateTime((java.util.Date)obj)).append("\"");
							continue;
						} else if(f.getFieldTip() == 8) {
							buf.append("\":");
							if (obj == null)buf.append("null");
							else if(obj instanceof Map)buf.append(GenericUtil.fromMapToJsonString2Recursive((Map)obj));
							else if(obj instanceof List)buf.append(GenericUtil.fromListToJsonString2Recursive((List)obj));
							else buf.append(obj);
							continue;
						}
						buf.append("\":\"");
						if (obj != null)
							switch (f.getPostProcessTip()) { // queryField
																// PostProcessTip
							case 3:
								buf.append(GenericUtil.onlyHTMLToJS(obj
										.toString()));
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
												+ UserUtil.getUserName(
														GenericUtil.uInt(s));
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
										.append(UserUtil.getUserDsc(
												GenericUtil.uInt(obj)));
								break;
							case 54: // Users LookUp Real Name
								String[] ids11 = ((String) obj).split(",");
								if (ids11.length > 0) {
									String res = "";
									for (String s : ids11) {
										res += ","
												+ UserUtil.getUserDsc(
														GenericUtil.uInt(s));
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
										queryResult.getScd(), f.getLookupQueryId());
								if (lookUp == null)
									break;
								buf.append("\",\"").append(f.getDsc())
										.append("_qw_\":\"");
								String[] objs = f.getPostProcessTip() == 11 ? ((String) obj)
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
									.append(",\"user_dsc\":\"").append(UserUtil.getUserDsc( GenericUtil.uInt(ozc[1])))
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
												queryResult.getScd(),
												(short) 1,
												ozs.length > 3 ? ozs[3] : null,
												ozs.length > 4 ? ozs[4] : null))
									buf.append("-");
								buf.append(ozs[2]);
								W5Workflow appr = FrameworkCache.getWorkflow(queryResult.getScd(),appId);
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
									for (String rid : roleIds) {
										buf.append(
												FrameworkCache.wRoles.get(
														customizationId).get(
														GenericUtil.uInt(rid)) != null ? FrameworkCache.wRoles
														.get(customizationId)
														.get(GenericUtil
																.uInt(rid))
														: "null").append(", ");
									}
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
					if (queryResult.getQuery().getShowParentRecordFlag() != 0
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
					.append(queryResult.getStartRowNumber())
					.append(",\"total_count\":")
					.append(queryResult.getResultRowCount());
			if (FrameworkSetting.debug && queryResult.getExecutedSql() != null) {
				buf.append(",\n\"sql\":\"")
						.append(GenericUtil.stringToJS2(GenericUtil.replaceSql(
								queryResult.getExecutedSql(),
								queryResult.getSqlParams()))).append("\"");
			}
			if (!GenericUtil.isEmpty(queryResult.getExtraOutMap()))
				buf.append(",\n \"extraOutMap\":").append(
						GenericUtil.fromMapToJsonString(queryResult
								.getExtraOutMap()));
		} else
			buf.append(",\n\"errorType\":\"validation\",\n\"errors\":")
					.append(serializeValidatonErrors(queryResult.getErrorMap(),
							xlocale));

		return buf.append("}");
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
		if (page.getTemplateTip() != 0) { // html degilse
			// notification Control
			// masterRecord Control
			if (pr.getMasterRecordList() != null
					&& !pr.getMasterRecordList().isEmpty())
				buf.append("\n_mrl=")
						.append(serializeTableHelperList(customizationId,
								xlocale, pr.getMasterRecordList()))
						.append(";\n");
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

			if (page.getTemplateTip() != 8) { // wizard degilse
				int customObjectCount = 1, tabOrder = 1;
				for (Object i : pr.getPageObjectList()) {
					if (i instanceof W5GridResult) { // objectTip=1
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
					} else if (i instanceof W5CardResult) {// objectTip=2
						W5CardResult dr = (W5CardResult) i;
						buf.append(serializeCard(dr));
						if (dr.getDataViewId() < 0) {
							buf.append("\nvar _card")
									.append(customObjectCount++).append("=")
									.append(dr.getCard().getDsc())
									.append("\n");
						}
					} else if (i instanceof W5ListViewResult) {// objectTip=7
						W5ListViewResult lr = (W5ListViewResult) i;
						buf.append(serializeListView(lr));
						if (lr.getListId() < 0) {
							buf.append("\nvar _listView")
									.append(customObjectCount++).append("=")
									.append(lr.getListView().getDsc())
									.append("\n");
						}
					} else if (i instanceof W5FormResult) {// objectTip=3
						W5FormResult fr = (W5FormResult) i;
						if (Math.abs(fr.getObjectTip()) == 3) { // form
							buf.append("\nvar ").append(fr.getForm().getDsc())
									.append("=").append(serializeGetForm(fr));
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
					} else if (i instanceof W5QueryResult) {
						buf.append("\nvar ")
								.append(((W5QueryResult) i).getQuery().getDsc())
								.append("=")
								.append(serializeQueryData((W5QueryResult) i))
								.append("\n");
					}  else if (i instanceof W5BIGraphDashboard) {
						W5BIGraphDashboard gd = (W5BIGraphDashboard) i;
						buf.append("\nvar graph")
								.append(gd.getGraphDashboardId())
								.append("=")
								.append(serializeGraphDashboard(gd, pr.getScd()))
								.append(";\n");
					} else if (i instanceof String) {
						buf.append("\nvar ").append(i).append("={}");
					}
					buf.append("\n");
				}
			} else { // wizard
				buf.append("\nvar templateObjects=[");
				boolean b = false;
				for (W5PageObject o : page.get_pageObjectList()) {
					if (b)
						buf.append(",\n");
					else
						b = true;
					buf.append("{\"objTip\":").append(o.getObjectTip())
							.append(",\"objId\":").append(o.getObjectId());
					if (!GenericUtil.isEmpty(o.getPostJsCode()))
						buf.append(",").append(o.getPostJsCode()); // ornek
																	// ,"url":"showFormByQuery","extraParam":"&_qid=1&asdsa"
					buf.append("}");
				}
				buf.append("\n]");
			}
			if (replacePostJsCode) {

			} else
				code = page.getCode();
		} else {
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

/*			if (!FrameworkCache.publishLookUps.isEmpty()) {
				buf2.append("var _lookups={");
				boolean b2 = false;
				for (Integer lookUpId : FrameworkCache.publishLookUps) {
					W5LookUp lu = FrameworkCache.getLookUp(
							templateResult.getScd(), lookUpId);
					if(lu==null)continue;
					if (b2)
						buf2.append(",\n");
					else
						b2 = true;
					buf2.append(lu.getDsc()).append(":");
					Map<String, String> tempMap = new HashMap<String, String>();
					for (W5LookUpDetay lud : lu.get_detayList())
						tempMap.put(
								lud.getVal(),
								LocaleMsgCache
										.get2(customizationId, xlocale,
												lud.getDsc()));
					buf2.append(GenericUtil.fromMapToJsonString(tempMap));
				}
				buf2.append("};\n");
			}*/
			int customObjectCount=1;
			for (Object i : pr.getPageObjectList()) {
				if (i instanceof W5GridResult) {
					W5GridResult gr = (W5GridResult) i;
					buf2.append(serializeGrid(gr));
					buf2.append("\nvar _grid")
					.append(customObjectCount++).append("=")
					.append(gr.getGrid().getDsc()).append(";\n");
				} else if (i instanceof W5CardResult) {// objectTip=2
					W5CardResult dr = (W5CardResult) i;
					buf2.append(serializeCard(dr));
				} else if (i instanceof W5ListViewResult) {// objectTip=7
					W5ListViewResult lr = (W5ListViewResult) i;
					buf2.append(serializeListView(lr));
				} else if (i instanceof W5FormResult) {
					W5FormResult fr = (W5FormResult) i;
					buf2.append("\nvar ").append(fr.getForm().getDsc())
								.append("=").append(serializeGetForm(fr));
					buf2.append("\nvar _form")
					.append(customObjectCount++).append("=")
					.append(fr.getForm().getDsc()).append(";\n");
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
				} else if (i instanceof String) {
					buf2.append("\nvar ").append(i).append("={};");
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
//			buf3.append("var _CompanyLogoFileId=1;\n")
			code = page.getCode().replace("${promis}", buf2.toString())
					.replace("${localemsg}", buf3.toString());
			
			if (page.getCode().contains("${promis-css}")) {
				StringBuilder buf4 = new StringBuilder();


				if(!GenericUtil.isEmpty(page.getCssCode()) && page.getCssCode().trim().length()>3){
					buf4.append(page.getCssCode()).append("\n");
				}
				W5LookUp c = FrameworkCache.getLookUp(pr.getScd(), 665);
				for (W5LookUpDetay d : c.get_detayList()) {
					buf4.append(".bgColor")
							.append(d.getVal().replace("#", ""))
							.append("{background-color:")
							.append(d.getVal()).append(";}\n");
				}
				FrameworkCache.addPageCss(pr.getScd(), page.getTemplateId(), buf4.toString());
				code = code.replace("${promis-css}", " <link rel=\"stylesheet\" type=\"text/css\" href=\"/app/dyn-res/"+page.getTemplateId()+".css?.x="+page.getVersionNo()+"\" />");

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

//		short ttip= templateResult.getPage().getTemplateTip();
//		if((ttip==2 || ttip==4) && !GenericUtil.isEmpty(templateResult.getPageObjectList()))buf.append("\n").append(renderTemplateObject(templateResult));
		if(!GenericUtil.isEmpty(pr.getPageObjectList()))switch(pr.getPage().getTemplateTip()){
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
				.append(LocaleMsgCache.get2(scd,
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
					.append(UserUtil.getUserDsc(
							tableRecordInfoResult.getInsertUserId()))
					.append("\",\"insert_dttm\":\"")
					.append(tableRecordInfoResult.getInsertDttm())
					.append("\",\"version_user_id\":")
					.append(tableRecordInfoResult.getVersionUserId())
					.append(",\"version_user_id_qw_\":\"")
					.append(UserUtil.getUserDsc(
							tableRecordInfoResult.getVersionUserId()))
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
								if (!FrameworkCache.roleAccessControl(scd, 0)) {
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
					.append(UserUtil.getUserDsc(
							feed.getInsertUserId()))
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
							.append(UserUtil.getUserDsc(
									ch.getInsertUserId()))
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

	public StringBuilder serializeException(Map<String, Object> scd,
			IWBException ex) {
		String locale = (scd == null) ? FrameworkCache.getAppSettingStringValue(0,
				"locale") : (String) scd.get("locale");
		int customizationId = (scd == null) ? FrameworkCache
				.getAppSettingIntValue(0, "default_customization_id")
				: GenericUtil.uInt(scd.get("customizationId"));
		StringBuilder b = new StringBuilder();
		b.append("{\"success\":false,\n\"errorType\":\"")
				.append(ex.getErrorType()).append("\"");
		String msg = ex.getMessage();
		String cause = ex.getCause() == null ? null : ex.getCause()
				.getMessage();
		if (msg != null) {
			if (FrameworkCache.getAppSettingIntValue(customizationId, "debug") != 1) {
				if (msg.contains("ORA-")) {
					String temp = msg.substring(0, 9);
					if (!temp.equals("ORA-00001")) {
						msg = LocaleMsgCache.get2(0, locale,
								FrameworkCache.wExceptions.get(temp));
					} else {
						temp = msg.substring(msg.indexOf('(') + 1,
								msg.indexOf(')'));
						msg = LocaleMsgCache.get2(0, locale,
								FrameworkCache.wExceptions.get(temp));
					}
				} else if (cause != null && cause.contains("ORA-")) {
					String temp = cause.substring(0, 9);
					if (temp.equals("ORA-02292")) // FOREIGN KEY
					{
						temp = cause.substring(cause.indexOf('(') + 1,
								cause.indexOf(')'));
						msg = LocaleMsgCache.get2(0, locale,
								FrameworkCache.wExceptions.get(temp));
					}
				}
			}

			else {
				int index = msg.indexOf("ORA-");
				if (index != -1) {
					int lastIndex = msg.indexOf(":", index + 4);
					if (lastIndex == index + 9) {
						String dbErrorCode = msg.substring(index, lastIndex);
						String errorMsg = LocaleMsgCache.get2(0, locale,
								dbErrorCode);
						if (errorMsg != null)
							msg = errorMsg + " (" + msg + ")";
					}
				}
			}
			if (msg == null) {
				msg = ex.getMessage();
				int index = msg.indexOf("ORA-");

				if (index != -1) {
					msg = LocaleMsgCache.filter2(0, locale, msg).toString();
					int lastIndex = msg.indexOf("ORA-", index + 4);
					msg = msg.substring(index + 10, lastIndex);
				}
			}

			b.append(",\n\"error\":'").append(GenericUtil.stringToJS(msg))
					.append("'");
		}

		if (ex.getObjectType() != null) {
			b.append(",\n\"objectType\":'")
					.append(GenericUtil.stringToJS(ex.getObjectType()))
					.append("'");
			if (ex.getObjectId() != 0) {
				b.append(",\n\"objectId\":").append(ex.getObjectId());
			}
		}

		if (FrameworkSetting.debug && ex.getSql() != null) {
			b.append(",\n\"sql\":'").append(GenericUtil.stringToJS(ex.getSql()))
					.append("'");
		}

		return b.append("}");
	}


	private StringBuilder serializeManualConversions(Map scd,
			List<W5Conversion> l) {
		StringBuilder s = new StringBuilder();
		int customizationId = (Integer) scd.get("customizationId");
		boolean b = false;
		for (W5Conversion fsm : l)
			if (GenericUtil.hasPartInside2(fsm.getActionTips(), 0)) { // manuel
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

	
	private Object renderDashboardObject(W5PageResult pr) {
		StringBuilder buf = new StringBuilder();
		if(GenericUtil.isEmpty(pr.getPageObjectList()))return buf;
		buf.append("return iwb.ui.buildDashboard({t:_page_tab_id, rows:[");
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
				rbuf.append("{query:").append(qr.getQuery().getDsc());
				for(W5PageObject po2:pr.getPage().get_pageObjectList())if(po2.getObjectId()==qr.getQueryId()){
					po = po2;
					break;
				}
			} else if(o instanceof W5CardResult){
				W5CardResult cr = (W5CardResult)o;
				rbuf.append("{card:").append(cr.getCard().getDsc());
				for(W5PageObject po2:pr.getPage().get_pageObjectList())if(po2.getObjectId()==cr.getDataViewId()){
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
		buf.append("}");
		return buf;
	}
}
