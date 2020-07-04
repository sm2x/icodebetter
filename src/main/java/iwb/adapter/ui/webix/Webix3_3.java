package iwb.adapter.ui.webix;

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
import iwb.domain.db.W5FormHint;
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
import iwb.domain.db.W5Table;
import iwb.domain.db.W5TableChild;
import iwb.domain.db.W5TableField;
import iwb.domain.db.W5Workflow;
import iwb.domain.db.W5WorkflowStep;
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

public class Webix3_3 implements ViewAdapter {
	final public static String[] labelMap = new String[]{"info","warning","warning"};
	final public static String[] filterMap = new String[]{"","textFilter","dateRangeFilter","numberFilter","numberFilter","numberFilter"};
	/*serverFilter*/
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
					.append(LocaleMsgCache.get2(0, locale, q)).append("\"}");// TODO.
																				// aslinda
																				// customizationId
																				// olmasi
																				// lazim
		}
		buf.append("]");
		return buf;
	}

	public StringBuilder serializeFormCellStore(W5FormCellHelper cellResult,
			int customizationId, String locale) {
		StringBuilder buf = new StringBuilder();
		boolean b = false;
		buf.append("{\"success\":true, \"data\" : [");
		if (cellResult.getLookupListValues() != null) { // cell LookUp'tan
														// geliyor
			for (W5Detay p : (List<W5Detay>) cellResult.getLookupListValues()) {
				if (b)
					buf.append(",");
				else
					b = true;
				buf.append("[\"")
						.append(p.getVal())
						.append("\",\"")
						.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache
								.get2(customizationId, locale, p.getDsc())
								.replaceAll(",", "-") : p.getDsc().replaceAll(
								",", "-")).append("\"");
				buf.append("]");
			}
		} else if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan
																// geliyor
			if (cellResult.getLookupQueryResult().getData() != null)
				for (Object[] p : cellResult.getLookupQueryResult().getData()) {
					if (b)
						buf.append(",");
					else
						b = true;
					boolean bb = false;
					buf.append("[");
					for (W5QueryField f : cellResult.getLookupQueryResult()
							.getQuery().get_queryFields()) {
						Object z = p[f.getTabOrder() - 1];
						if (bb)
							buf.append(",");
						else
							bb = true;
						if (z == null)
							z = "";
						buf.append("\"")
								.append(f.getPostProcessType() == 2 ? LocaleMsgCache
										.get2(customizationId, locale,
												z.toString()).replaceAll(",",
												"-") : GenericUtil.stringToJS2(
										z.toString()).replaceAll(",", "-"))
								.append("\"");
					}
					buf.append("]");
				}
		}
		buf.append("]}");
		return buf;
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
				&& formResult.getForm().getObjectType() == 2
				&& formResult.getAction() == 1;
		for(W5FormCellHelper fc:formResult.getFormCellResults())if((fc.getFormCell().getControlType()==7 || fc.getFormCell().getControlType()==10 || fc.getFormCell().getControlType()==23 ) && fc.getFormCell().getDialogGridId()>0 && fc.getExtraValuesMap()!=null){
			W5GridResult gridResult = (W5GridResult)fc.getExtraValuesMap().get("dialogGrid");
			if(gridResult!=null){
				gridResult.setViewReadOnlyMode(true);
				gridResult.getRequestParams().put("_no_post_process_fields", "1");
				s.append(serializeGrid(gridResult, fc.getFormCell().getDsc()+"_"+gridResult.getGrid().getDsc())).append("\n");
			}
		}
			
		if (GenericUtil.uInt(formResult.getRequestParams().get("a")) != 5 && formResult.getForm().getRenderType() != 0) { // tabpanel ve icinde gridler varsa
			for (W5FormModule m : formResult.getForm().get_moduleList()){
					switch (m.getModuleType()) {
					case 4:// form
						if (formResult.getModuleFormMap() == null)
							break;
						W5FormResult nfr = formResult.getModuleFormMap().get(
								m.getObjectId());
						if (nfr == null)
							return null;
						s.append("var ").append(nfr.getForm().getDsc())
								.append("=").append(serializeGetForm(nfr))
								.append(".render();\n");
						break;
					case 5:// grid
						if (formResult.getModuleGridMap() == null)
							return null;
						if (m.getModuleViewType() == 0
								|| formResult.getAction() == m
										.getModuleViewType()) {
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

		s.append("var getForm=").append(serializeGetForm(formResult));

		/*if(formResult.getForm().getRenderTemplateId()==26 && formResult.getForm().get_renderTemplate() != null && formResult.getScd()!=null && formResult.getScd().containsKey("_renderer") && formResult.getScd().get("_renderer").toString().startsWith("webix"))
			s.append("\nvar extDef = getForm.render();return addTab4Form(extDef, getForm, callAttributes);"); 
		else if (formResult.getRequestParams() != null
				&& formResult.getRequestParams().containsKey("_log5_log_id")
				&& FrameworkCache.wTemplates.containsKey(formResult.getScd().get(
						"customizationId"))
				&& FrameworkCache.wTemplates.get(
						formResult.getScd().get("customizationId"))
						.containsKey(668)) {
			W5Template tpl = FrameworkCache.wTemplates.get(
					formResult.getScd().get("customizationId")).get(668);
			s.append("\n").append(
					tpl.getLocaleMsgFlag() != 0 ? GenericUtil.filterExt(
							tpl.getCode(), formResult.getScd(),
							formResult.getRequestParams(), null) : tpl
							.getCode());
		} else */if (formResult.getForm().get_renderTemplate() != null && formResult.getForm().getRenderTemplateId()!=26) {
				s.append("\n").append(formResult.getForm().get_renderTemplate().getLocaleMsgFlag() != 0 ? GenericUtil
							.filterExt(formResult.getForm()
									.get_renderTemplate().getCode(),
									formResult.getScd(),
									formResult.getRequestParams(), null)
							: formResult.getForm().get_renderTemplate()
									.getCode());
		} else if(formResult.getForm().getObjectType()==2){
			s.append("\nreturn iwb.ui.buildCRUDForm(getForm, callAttributes);\n");
		}

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
			t = FrameworkCache.getTable(customizationId, f.getObjectId());
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
				if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms && FrameworkCache
						.getAppSettingIntValue(customizationId, "sms_flag") != 0) || (fsm
						.getSmsMailType() != 0 && FrameworkSetting.mail && FrameworkCache
						.getAppSettingIntValue(customizationId, "mail_flag") != 0))
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
					if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms && FrameworkCache
							.getAppSettingIntValue(customizationId, "sms_flag") != 0) || (fsm
							.getSmsMailType() != 0 && FrameworkSetting.mail && FrameworkCache
							.getAppSettingIntValue(customizationId, "mail_flag") != 0))
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
								.append(fsm.getSmsMailType() == 0 ? "[SMS] "
										: "["
												+ (LocaleMsgCache.get2(
														customizationId,
														xlocale, "email_upper"))
												+ "] ")
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
				if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms && FrameworkCache
						.getAppSettingIntValue(customizationId, "sms_flag") != 0) || (fsm
						.getSmsMailType() != 0 && FrameworkSetting.mail && FrameworkCache
						.getAppSettingIntValue(customizationId, "mail_flag") != 0))
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
					if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms && FrameworkCache
							.getAppSettingIntValue(customizationId, "sms_flag") != 0) || (fsm
							.getSmsMailType() != 0 && FrameworkSetting.mail && FrameworkCache
							.getAppSettingIntValue(customizationId, "mail_flag") != 0))
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
								.append(fsm.getSmsMailType() == 0 ? "[SMS] "
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

		W5Form f = formResult.getForm();
		// s.append("var ").append(formResult.getForm().getDsc()).append("=");
		String[] postFormStr = new String[] { "", "search_form",
				"ajaxPostForm",
				f.getObjectType() == 3 ? "rpt/" + f.getDsc() : "ajaxExecDbFunc",
				"ajaxExecDbFunc", "", "", "", "" };
		s.append("{ formId: ").append(formResult.getFormId())
				.append(", a:").append(formResult.getAction()).append(", name:'")
				.append(LocaleMsgCache.get2(customizationId, xlocale, formResult.getForm().getLocaleMsgKey()))
				.append("',id:'").append(formResult.getUniqueId())
				.append("', defaultWidth:").append(f.getDefaultWidth()).append(", defaultHeight:").append(f.getDefaultHeight());

		if (f.get_formHintList() != null) {
			boolean b = false;
			for (W5FormHint sx : f.get_formHintList())
				if (sx.getLocale().equals(xlocale)
						&& (sx.getActionTips().contains(
								"" + formResult.getAction())
								|| formResult.getForm().getObjectType() == 3 || formResult
								.getForm().getObjectType() == 4)) {
					if (b)
						s.append("\n,");
					else {
						s.append(",\n hmsgs:[");
						b = true;
					}
					s.append("{text:'")
							.append(GenericUtil.stringToJS(sx.getDsc()))
							.append("',tip:").append(sx.getHintTip());
					s.append(",color:\"").append(sx.getHintColor())
							.append("\"").append("}");
				}
			if (b)
				s.append("]");
		}

		boolean liveSyncRecord = false;
		// form(table) fields
		if (f.getObjectType() == 2
				&& FrameworkCache.getTable(customizationId, f.getObjectId()) != null) {
			s.append(",\n renderTip:").append(
					formResult.getForm().getRenderType());
			W5Table t = FrameworkCache.getTable(customizationId, f.getObjectId());
			liveSyncRecord = FrameworkSetting.liveSyncRecord
					&& t.getLiveSyncFlag() != 0 && !formResult.isViewMode();
			// insert AND continue control
			s.append(",\n crudTableId:").append(f.getObjectId());
			if (formResult.getAction() == 2) { // insert
				long tmpId = -GenericUtil.getNextTmpId();
				s.append(",\n contFlag:").append(f.getContEntryFlag() != 0).append(",\n tmpId:").append(tmpId);
				formResult.getRequestParams().put("_tmpId", "" + tmpId);
			} else if (formResult.getAction() == 1) { // edit
				s.append(", pk:").append(GenericUtil.fromMapToJsonString(formResult.getPkFields()));
				if(t.getAccessDeleteTip()==0 || !GenericUtil.isEmpty(t.getAccessDeleteUserFields()) || GenericUtil.accessControl(scd, t.getAccessDeleteTip(), t.getAccessDeleteRoles(), t.getAccessDeleteUsers()))s.append(", deletable:!0");
				if (liveSyncRecord) {
					s.append(",\n liveSync:true");
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

			if (t.getCopyTip() == 1) {
				if (formResult.getAction() == 1)
					s.append(",\n copyFlag:true");
				else if (formResult.getRequestParams().get("a") != null
						&& formResult.getRequestParams().get("a").equals("5")) {// kopyalama
																				// yapilacak
																				// sorulacaklari
																				// diz
					s.append(",\n copyTableIds:[");
					boolean b = false;
					if (t.get_tableChildList() != null)
						for (W5TableChild tc : t.get_tableChildList())
							if (tc.getCopyStrategyTip() != 0) {
								if (b)
									s.append("\n,");
								else
									b = true;
								s.append("{id:'rtb_id_")
										.append(tc.getRelatedTableId())
										.append("'");
								if (tc.getCopyStrategyTip() == 1)
									s.append(",disabled:true,checked:true");
								s.append(",boxLabel:'")
										.append(LocaleMsgCache.get2(
												customizationId,
												xlocale,
												FrameworkCache.getTable(scd,
														tc.getRelatedTableId())
														.getDsc()))
										.append("'}");
							}
					s.append("]");
				}
			}

			/*
			 * if(PromisCache.getAppSettingIntValue(scd, "bpm_flag")!=0 &&
			 * formResult.getAction()==2 &&
			 * t.get_listStartProcess()!=null){//BPM start process list
			 * s.append(",\n bpmProcesses:["); boolean b=false; for(BpmProcess
			 * bp:t.get_listStartProcess())if(PromisUtil.accessControl(scd,
			 * bp.getAccessViewTip(), bp.getAccessViewRoles(),
			 * bp.getAccessViewUsers())){ if(b)s.append("\n,");else b=true;
			 * s.append
			 * ("{id:").append(bp.getProcessId()).append(",dsc:'").append
			 * (PromisUtil.stringToJS(bp.getDsc())).append("'}"); }
			 * s.append("]"); }
			 */

			if (FrameworkCache.getAppSettingIntValue(scd, "log_flag") != 0
					&& (t.getDoUpdateLogFlag() != 0 || t.getDoInsertLogFlag() != 0)
					&& FrameworkCache.roleAccessControl(scd,  108))
				s.append(",\n logFlags:{edit:")
						.append(t.getDoUpdateLogFlag() != 0).append(",insert:")
						.append(t.getDoInsertLogFlag() != 0).append("}");
			if (FrameworkCache.roleAccessControl(scd, 109))
				s.append(",\n smsMailTemplateCrudFlag:true");
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
			if (FrameworkCache.getAppSettingIntValue(scd, "file_attachment_flag") != 0
					&& t.getFileAttachmentFlag() != 0
					&& FrameworkCache.roleAccessControl(scd,  101))
				s.append(",\n fileAttachFlag:true, fileAttachCount:").append(
						formResult.getFileAttachmentCount());
		
			
			if (formResult.isViewMode())
				s.append(",\n viewMode:true");

			if (!formResult.isViewMode() && f.get_formSmsMailList() != null
					&& !f.get_formSmsMailList().isEmpty()) { // automatic sms
																// isleri varsa
				int cnt = 0;
				for (W5FormSmsMail fsm : f.get_formSmsMailList())
					if (fsm.getSmsMailSentType() != 3
							&& ((fsm.getSmsMailType() == 0 && FrameworkSetting.sms && FrameworkCache
									.getAppSettingIntValue(customizationId,
											"sms_flag") != 0) || (fsm
									.getSmsMailType() != 0 && FrameworkSetting.mail && FrameworkCache
									.getAppSettingIntValue(customizationId,
											"mail_flag") != 0))
							&& fsm.getAlarmFlag() == 0
							&& GenericUtil.hasPartInside2(fsm.getActionTypes(),
									formResult.getAction())
							&& GenericUtil.hasPartInside2(
									fsm.getWebMobileTips(), mobile ? "2" : "1")) {
						cnt++;
					}
				if (cnt > 0) {
					s.append(",\n\"smsMailTemplateCnt\":").append(cnt++)
							.append(",\n\"smsMailTemplates\":[");
					boolean b = false;
					for (W5FormSmsMail fsm : f.get_formSmsMailList())
						if (fsm.getSmsMailSentType() != 3
								&& ((fsm.getSmsMailType() == 0
										&& FrameworkSetting.sms && FrameworkCache
										.getAppSettingIntValue(customizationId,
												"sms_flag") != 0) || (fsm
										.getSmsMailType() != 0
										&& FrameworkSetting.mail && FrameworkCache
										.getAppSettingIntValue(customizationId,
												"mail_flag") != 0))
								&& fsm.getAlarmFlag() == 0
								&& GenericUtil.hasPartInside2(
										fsm.getActionTypes(),
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
									.append(fsm.getSmsMailType() == 0 ? "[<b>SMS</b>] "
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
										&& FrameworkSetting.sms && FrameworkCache
										.getAppSettingIntValue(customizationId,
												"sms_flag") != 0) || (fsm
										.getSmsMailType() != 0
										&& FrameworkSetting.mail && FrameworkCache
										.getAppSettingIntValue(customizationId,
												"mail_flag") != 0))
								&& fsm.getAlarmFlag() != 0
								&& GenericUtil.hasPartInside2(
										fsm.getActionTypes(),
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
							if (fsm.getSmsMailSentType() != 3
									&& ((fsm.getSmsMailType() == 0
											&& FrameworkSetting.sms && FrameworkCache
											.getAppSettingIntValue(
													customizationId, "sms_flag") != 0) || (fsm
											.getSmsMailType() != 0
											&& FrameworkSetting.mail && FrameworkCache
											.getAppSettingIntValue(
													customizationId,
													"mail_flag") != 0))
									&& fsm.getAlarmFlag() != 0
									&& GenericUtil.hasPartInside2(
											fsm.getActionTypes(),
											formResult.getAction())
									&& GenericUtil.hasPartInside2(fsm
											.getWebMobileTips(), mobile ? "2"
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
										.append(fsm.getSmsMailType() == 0 ? "[<b>SMS</b>] "
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
						if ((fsm.getConversionType() != 3/* invisible-checked */
								&& GenericUtil.hasPartInside2(
										fsm.getActionTypes(),
										formResult.getAction()) || (formResult
								.getMapConvertedObject() != null && formResult
								.getMapConvertedObject().containsKey(
										fsm.getConversionId())))) {
							W5Table dt = FrameworkCache.getTable(customizationId,
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
										&& fsm.getConversionType() != 3
										&& GenericUtil.hasPartInside2(
												fsm.getActionTypes(),
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
											.append(fsm.getConversionType() == 1
													|| fsm.getConversionType() == 0);
									if (fsm.getConversionType() == 0)
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
		}
		if (!formResult.getOutputMessages().isEmpty()) {
			s.append(",\n\"msgs\":[");
			boolean b = false;
			for (String sx : formResult.getOutputMessages()) {
				if (b)
					s.append("\n,");
				else
					b = true;
				s.append("'").append(GenericUtil.stringToJS(sx)).append("'");
			}
			s.append("]");
		}

		if (formResult.getApprovalRecord() != null) { // Burası Artık Onay Mekanizması başlamış
			W5Workflow a = FrameworkCache.getWorkflow(formResult.getScd(), formResult.getApprovalRecord().getApprovalId());
			if (formResult.getApprovalRecord().getApprovalStepId() == 901) {// kendisi start for approval yapacak
				if ((a.getManualAppUserIds() == null
						&& a.getManualAppRoleIds() == null
						&& GenericUtil
								.accessControl(scd, formResult
										.getApprovalRecord()
										.getApprovalActionTip() /*
																 * ??? Bu ne
																 */,
										formResult.getApprovalRecord()
												.getApprovalRoles(), formResult
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
							.append(formResult.getApprovalRecord()
									.getApprovalRecordId())
							.append(",wait4start:true,dynamic:")
							.append(a.getApprovalFlowTip() == 3).append("}");
			} else if (GenericUtil.accessControl(scd, (short) 1, formResult
					.getApprovalRecord().getApprovalRoles(), formResult
					.getApprovalRecord().getApprovalUsers())) {
				// TODO:buraya e-sign ile ilgili kontrol eklenecek. dinamik onay
				// varsa approval değilse aprrovalstep kontrol edilecek
				s.append(",\n approval:{approvalRecordId:")
						.append(formResult.getApprovalRecord()
								.getApprovalRecordId());
				W5WorkflowStep step = a.get_approvalStepMap().get(formResult.getApprovalRecord().getApprovalStepId());
				/*if(step!=null && step.getOnApproveFormActionTip()>0 && step.getOnApproveFormId()!=null && step.getOnApproveFormId()>0){
					s.append(",onAppFormActionTip:").append(step.getOnApproveFormActionTip()).append(",onAppFormId:").append(step.getOnApproveFormId());
					int approvalRelatedTablePk = GenericUtil.uInt(formResult.getOutputFields().get("_approval_app_pk")); 
					if(approvalRelatedTablePk>0)s.append(",apk:").append(approvalRelatedTablePk);
				}				
				if(formResult.getApprovalRecord().getReturnFlag() != 0){
					s.append(",returnFlag:true");
					if(step!=null && step.getOnReturnFormActionTip()>0 && step.getOnReturnFormId()!=null && step.getOnReturnFormId()>0){
						s.append(",onRetFormActionTip:").append(step.getOnReturnFormActionTip()).append(",onRetFormId:").append(step.getOnReturnFormId());
						int approvalRelatedTablePk = GenericUtil.uInt(formResult.getOutputFields().get("_approval_ret_pk")); 
						if(approvalRelatedTablePk>0)s.append(",rpk:").append(approvalRelatedTablePk);
					}
				}
				if(step!=null && step.getOnRejectFormId()!=null && step.getOnRejectFormId()>0){
					s.append(",rejectFlag:true,onRejFormId:").append(step.getOnRejectFormId());
				}*/
				s.append(",approvalStepId:")
				.append(step.getApprovalStepId())
				.append(",approvalId:")
				.append(step.getWorkflowId()).append(",versionNo:")
						.append(formResult.getApprovalRecord().getVersionNo())
						.append(",stepDsc:'")
						.append(formResult.getApprovalStep() != null ? GenericUtil
								.stringToJS(formResult.getApprovalStep()
										.getDsc()) : "-")
						.append("'}");
			}
		}
		if (f.get_toolbarItemList().size() > 0) { // extra buttonlari var mi yok
													// mu?
			StringBuilder buttons = serializeToolbarItems(scd,
					f.get_toolbarItemList(), (formResult.getFormId() > 0 ? true
							: false));
			/*
			 * boolean b = false; for(W5ObjectToolbarItem
			 * toolbarItem:f.get_toolbarItemList())
			 * if(PromisUtil.accessControl(scd, toolbarItem.getAccessViewTip(),
			 * toolbarItem.getAccessViewRoles(),
			 * toolbarItem.getAccessViewUsers())){ if(b)buttons.append(",");
			 * else b = true; if(toolbarItem.getDsc().equals("-"))
			 * buttons.append("'-'"); else{
			 * buttons.append("{text:'").append(PromisLocaleMsg
			 * .get2(customizationId,
			 * xlocale,toolbarItem.getLocaleMsgKey())).append("',");
			 * if(formResult.getFormId()>0)buttons.append(
			 * "iconAlign: 'top', scale:'medium', style:{margin: '0px 5px 0px 5px'},"
			 * );
			 * buttons.append("iconCls:'").append(toolbarItem.getImgIcon()).append
			 * (
			 * "', handler:function(a,b,c){\n").append(toolbarItem.getCode()).append
			 * ("\n}}"); } }
			 */
			if (buttons.length() > 1) {
				s.append(",\n extraButtons:[").append(buttons).append("]");
			}
		}
		for (String sx : formResult.getOutputFields().keySet()) {
			s.append(",\n ").append(sx).append(":")
					.append(formResult.getOutputFields().get(sx));// TODO:aslinda
																	// ' li
																	// olması
																	// lazim
		}

		if (liveSyncRecord)
			formResult.getRequestParams().put(".t", formResult.getUniqueId());
		s.append(",\n render:function(){\nvar mf={_formId:").append(
				formResult.getFormId());
		if (liveSyncRecord)
			s.append(",id:'").append(formResult.getUniqueId()).append("'");
		s.append(",baseParams:")
				.append(GenericUtil.fromMapToJsonString(formResult
						.getRequestParams()));
		if(formResult.getForm().getLabelAlignType()>0)s
				.append(",\nlabelAlign:'")
				.append(FrameworkSetting.alignMap[formResult.getForm().getLabelAlignType()]).append("',\nlabelWidth:")
				.append(4*formResult.getForm().getLabelWidth()/3);
		else 
			s.append(", elementsConfig: {labelPosition: \"top\",labelWidth:").append(4*formResult.getForm().getLabelWidth()/3).append("}");
		s.append(",_url:'")
				.append(postFormStr[formResult.getForm().getObjectType()])
				.append("'}\n");
		/*
		 * if(PromisSetting.liveSyncRecord && formResult!=null &&
		 * formResult.getForm()!=null && formResult.getForm().getObjectTip()==2
		 * && formResult.getAction()==1){
		 * s.append("tab={_l:{pk:,t:'").append(formResult
		 * .getUniqueId()).append("'}}"); }
		 */
		
		
		for(W5FormCell fc:formResult.getForm().get_formCells())if(fc.getControlType()==99 && fc.get_sourceObjectDetail()!=null){//grid is
/*			W5Grid g = (W5Grid)fc.get_sourceObjectDetail();
			W5GridResult gr = new W5GridResult(g.getGridId());
			gr.setRequestParams(formResult.getRequestParams());gr.setScd(formResult.getScd());gr.setFormCellResultMap(new HashMap());
			
			for(W5GridColumn column:g.get_gridColumnList())if(column.get_formCell()!=null){
				gr.getFormCellResultMap().put(column.get_formCell().getFormCellId(), new W5FormCellHelper(column.get_formCell()));
			}
			
			gr.setGrid(g);*/
			W5GridResult gr = formResult.getModuleGridMap().get(fc.getLookupQueryId());
			s.append(serializeGrid(gr)).append("\n");
		}

		for (W5FormCellHelper fc : formResult.getFormCellResults())
			if (fc.getFormCell().getActiveFlag() != 0) {
				if (fc.getFormCell().getControlType() != 102) {// label'dan
																// farkli ise.
																// label direk
																// render
																// edilirken
																// koyuluyor
					s.append("var _").append(fc.getFormCell().getDsc()).append("=")
							.append("mf._").append(fc.getFormCell().getDsc()).append("=")
							.append(serializeFormCell(customizationId, xlocale,fc, formResult)).append(";\n");
					// if(fc.getFormCell().getControlTip()==24)s.append("_").append(fc.getFormCell().getDsc()).append(".treePanel.getRootNode().expand();\n");
				} else {
					fc.setValue(LocaleMsgCache.get2(customizationId, xlocale,
							fc.getFormCell().getLocaleMsgKey()));
				}
			}

		s.append("\nvar __anaBaslik__='")
				.append(GenericUtil.stringToJS(formResult.getForm()
						.getLocaleMsgKey())).append("'\nvar __action__=")
				.append(formResult.getAction()).append("\n");

		// 24 nolu form form edit form olduğu için onu çevirmesin.
		String postCode = (formResult.getForm().get_renderTemplate() != null
				&& formResult.getForm().get_renderTemplate().getLocaleMsgFlag() == 1 && formResult
				.getFormId() != 24) ? GenericUtil.filterExt(
				formResult.getForm().getJsCode(), scd,
				formResult.getRequestParams(), null).toString() : formResult
				.getForm().getJsCode();

		boolean b = true;
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

		if(formResult.getForm().getObjectType()==1){
			s.append(renderFormModuleList(customizationId, xlocale,
					formResult.getUniqueId(),
					formResult.getFormCellResults(),
					"mf=iwb.apply(mf,{view:'form', elements:["
					 +(formResult.getForm().getLabelAlignType()>0 ? "{type:'section',template:'<b style=\"color:#f00\">Arama Kriterleri</b>'},":"")+"{"));
//				if(formResult.getForm().getLabelAlignTip()==0)s.append("},{},{view:'toolbar',paddingX: 5,paddingY: 5,height: 40, elements: [{view:'button', tooltip:'Reload', type: 'icon', icon: 'reload', value:'Reload'}]");
			s.append("}]});\n");// "+				(formBodyColor!=null ? ",bodyStyle:'background-color:
		} else switch (formResult.getForm().getRenderType()) {
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
			s.append(renderFormModuleList(customizationId, xlocale,
							formResult.getUniqueId(),
							formResult.getFormCellResults(),
							"mf=iwb.apply(mf,{view:'form', elements:[{"))
					.append("}]});\n");
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
		StringBuilder buf = new StringBuilder();
		buf.append("mf=iwb.apply(mf,{view:'form',elementsConfig: {labelAlign:'")
				.append(FrameworkSetting.alignMap[formResult.getForm().getLabelAlignType()]).append("',labelWidth: ").append(4*formResult.getForm().getLabelWidth()/3).append("}, elements:[{id:\"resp-").append(formResult.getUniqueId()).append("\",rows:[");
		if (map.get(0).size() > 0) {
			buf.append(renderFormModuleList(customizationId, xlocale,
					formResult.getUniqueId(), map.get(0), "{"))
				.append("},");
		}

		boolean b = false;
//		int tabHeight = 0;
		
																														
		StringBuilder buf2 = new StringBuilder();
		for (W5FormModule m : formResult.getForm().get_moduleList())
			if (m.getFormModuleId() != 0) {
				if ((m.getModuleViewType() == 0 || formResult.getAction() == m
						.getModuleViewType())) {
					switch (m.getModuleType()) {
					case	4: break;
/*					case 4:// form
						if (GenericUtil.uInt(formResult.getRequestParams().get(
								"a")) == 5)
							break;
						W5FormResult subFormResult = formResult
								.getModuleFormMap() == null ? null : formResult
								.getModuleFormMap().get(m.getObjectId());
						W5Table mainTablex = subFormResult != null
								&& subFormResult.getForm() != null ? FrameworkCache
								.getTable(customizationId, subFormResult
										.getForm().getObjectId()) : null;
						if (mainTablex == null)
							continue;
						if (FrameworkSetting.moduleAccessControl != 0
								&& mainTablex != null
								&& (!FrameworkCache.roleAccessControl(
										formResult.getScd(),
										mainTablex.getModuleId(), 0) | !GenericUtil
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
									.append(",{id:_page_tab_id+'_fm_'+"+m.getFormModuleId()+",xtype:null,layout:'form',title:'")
									.append(LocaleMsgCache.get2(
											customizationId, xlocale,
											m.getLocaleMsgKey()))
									.append("',height:")
									.append(subFormResult.getForm()
											.getDefaultHeight())
									.append(",autoScroll:true})");
							extendedForms.add(subFormResult.getForm().getDsc());
						}
						tabHeight = subFormResult.getForm().getDefaultHeight();
						break;*/
					case 5:// grid(edit)
						if (formResult.getModuleGridMap() == null)
							break;
						if (GenericUtil.uInt(formResult.getRequestParams().get(
								"a")) == 5)
							break;
						W5GridResult gridResult = formResult.getModuleGridMap()
								.get(m.getObjectId());
						int tabHeight = gridResult.getGrid().getDefaultHeight();
						W5Table mainTable = gridResult.getGrid() != null
								&& gridResult.getGrid().get_defaultCrudForm() != null ? FrameworkCache
								.getTable(customizationId, gridResult.getGrid()
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
								buf2.append(",");
							else
								b = true;
							buf2.append("{id:_page_tab_id+'_fm_'+"+m.getFormModuleId()+",header:'").append(LocaleMsgCache.get2(customizationId, xlocale, gridResult.getGrid().getLocaleMsgKey()))
									.append("',height:").append(gridResult.getGrid().getDefaultHeight())
									.append(",body:").append(gridResult.getGrid().getDsc()).append("}");
						}
						break; 
					default:
						if (!map.get(m.getFormModuleId()).isEmpty()) {
							if (b)
								buf2.append(",");
							else
								b = true;
							String extra = "{id:_page_tab_id+'_fm_'+"+m.getFormModuleId()+",header:'"
									+ LocaleMsgCache.get2(customizationId, xlocale, m.getLocaleMsgKey()) + "', body:{";
							// if(formBodyColor!=null)extra+=",bodyStyle:'background-color: #"+formBodyColor+"'";
							buf2.append(renderFormModuleList(customizationId,
									xlocale, formResult.getUniqueId(),
									map.get(m.getFormModuleId()), extra))
							.append("}}");
						}

					}
				}
			}
		if(buf2.length()>0)buf.append("{view:'tabview',cells:[").append(buf2).append("]}");// defaults:{autoHeight:true,

		buf.append("]}]});");


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
//		return addTab4GridWSearchForm({t:_page_tab_id,grid:grd_online_users1, pk:{tuser_id:'user_id'}});
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
													+ "'"))).append("]}");

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
								.getTable(customizationId, subFormResult
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
								.getTable(customizationId, gridResult.getGrid()
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
									map.get(m.getFormModuleId()), extra));
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
		buf.append("mf=iwb.apply(mf,{view:'form',elementsConfig: {labelAlign:'")
				.append(FrameworkSetting.alignMap[formResult.getForm().getLabelAlignType()]).append("',labelWidth: ").append(4*formResult.getForm().getLabelWidth()/3).append("},\nelements:[{id:\"resp-").append(formResult.getUniqueId()).append("\",rows:[");

		boolean b = false;
		if (formResult.getUniqueId() == null)
			formResult.setUniqueId(GenericUtil.getNextId("fi2"));
		List<String> extendedForms = new ArrayList();
		if (map.get(0).size() > 0) {
			buf.append(renderFormModuleList(customizationId, xlocale,
					formResult.getUniqueId(), map.get(0), "{"))
				.append("}");
			b = true;
		}
		if (formResult.getForm().get_moduleList() != null)
			for (W5FormModule m : formResult.getForm().get_moduleList())
				if (m.getFormModuleId() != 0) {
					if ((m.getModuleViewType() == 0 || formResult.getAction() == m
							.getModuleViewType())) {
						switch (m.getModuleType()) {
						case	4: case 5:break;
						
/*						case 4:// form
							if (GenericUtil.uInt(formResult.getRequestParams()
									.get("a")) == 5)
								break;
							W5FormResult subFormResult = formResult
									.getModuleFormMap().get(m.getObjectId());
							W5Table mainTablex = subFormResult != null
									&& subFormResult.getForm() != null ? FrameworkCache
									.getTable(customizationId, subFormResult
											.getForm().getObjectId()) : null;
							if (FrameworkSetting.moduleAccessControl != 0
									&& mainTablex != null
									&& (!FrameworkCache.roleAccessControl(
											formResult.getScd(),
											mainTablex.getModuleId(), 0) | !GenericUtil
											.accessControl(
													formResult.getScd(),
													mainTablex
															.getAccessViewTip(),
													mainTablex
															.getAccessViewRoles(),
													mainTablex
															.getAccessViewUsers())))
								subFormResult = null;// hicbirsey
							else {
								if (b)
									buf.append(",");
								else
									b = true;
								buf.append("Ext.apply(")
										.append(subFormResult.getForm()
												.getDsc())
										.append(",{xtype:null,layout:'form',title:'")
										.append(LocaleMsgCache.get2(
												customizationId, xlocale,
												m.getLocaleMsgKey()))
										.append("',height:")
										.append(subFormResult.getForm()
												.getDefaultHeight())
										.append(",autoScroll:true})");

								extendedForms.add(subFormResult.getForm()
										.getDsc());
							}
							break; */
/*						case 5:// grid(edit)
							if (formResult.getModuleGridMap() == null)
								break;
							if (GenericUtil.uInt(formResult.getRequestParams()
									.get("a")) == 5)
								break;
							W5GridResult gridResult = formResult
									.getModuleGridMap().get(m.getObjectId());
							W5Table mainTable = gridResult.getGrid() != null
									&& gridResult.getGrid()
											.get_defaultCrudForm() != null ? FrameworkCache
									.getTable(customizationId, gridResult
											.getGrid().get_defaultCrudForm()
											.getObjectId()) : null;
							if (FrameworkSetting.moduleAccessControl != 0
									&& mainTable != null
									&& (!FrameworkCache.roleAccessControl(
											formResult.getScd(),
											mainTable.getModuleId(), 0) | !GenericUtil
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
								if (b)
									buf.append(",");
								else
									b = true;
								buf.append(gridResult.getGrid().getDsc())
										.append("._gp=new ")
										.append(formResult.isViewMode() ? (gridResult
												.getGrid()
												.getTreeMasterFieldId() == 0 ? "Ext.grid.GridPanel"
												: "Ext.ux.maximgb.tg.GridPanel")
												: (gridResult.getGrid()
														.getTreeMasterFieldId() == 0 ? "Ext.grid.EditorGridPanel"
														: "Ext.ux.maximgb.tg.EditorGridPanel"))
										.append("(Ext.apply(")
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
							break; */
						default:
							if (!map.get(m.getFormModuleId()).isEmpty()) {
								if (b)
									buf.append(",");
								else
									b = true;
								buf.append(renderFormModuleList(
										customizationId, xlocale,
										formResult.getUniqueId(),
										map.get(m.getFormModuleId()), "{type:'section',template:'<b style=\"color:#f00\">" + LocaleMsgCache.get2(customizationId, xlocale, m.getLocaleMsgKey()) + "</b>'},{"))
								.append("}");
							}
						}
					}
				}
		buf.append("]}]});");
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
	}
	
	private StringBuilder renderFormModuleList(int customizationId,
			String xlocale, String formUniqueId,
			List<W5FormCellHelper> formCells, String xtype) {
		StringBuilder buf = new StringBuilder();
		// if(xtype!=null)buf.append("{frame:true,xtype:'").append(xtype).append("'");
		if(xtype!=null)buf.append(xtype);
		int lc = 0;
		for (W5FormCellHelper fc : formCells)
			if (fc.getFormCell().getActiveFlag() != 0)
				lc = Math.max(lc, fc.getFormCell().getTabOrder() / 1000);
		if (lc == 0) {// hersey duz
			buf.append("rows:[");// ,\nautoHeight:false
			boolean b = false;
			for (int i = 0; i < formCells.size(); i++) {
				W5FormCellHelper fc = formCells.get(i);
				if (fc.getFormCell().getActiveFlag() == 0)
					continue;
				if (fc.getFormCell().getControlType() == 102) {// displayField4info
					if (b)
						buf.append(",");
					else
						b = true;
					buf.append(serializeFormCell(customizationId, xlocale, fc,
							null));
				} else if (fc.getFormCell().getControlType() != 0) {
					if (i < formCells.size() - 1
							&& formCells.get(i + 1).getFormCell()
									.getControlType() != 0
							&& formCells.get(i + 1).getFormCell()
									.getActiveFlag() != 0
							&& formCells.get(i + 1).getFormCell().getTabOrder() == fc
									.getFormCell().getTabOrder()) { // yanyana
																	// koymak
																	// icin
						if (b)
							buf.append(",");
						else
							b = true;
						buf.append(
								"{cols: [_").append(fc.getFormCell().getDsc());
						while (i < formCells.size() - 1
								&& formCells.get(i + 1).getFormCell()
										.getControlType() != 0
								&& formCells.get(i + 1).getFormCell()
										.getTabOrder() == fc.getFormCell()
										.getTabOrder()) {
							i++;
							W5FormCellHelper fc2 = formCells.get(i);
							// if(!fc2.getFormCell().getLocaleMsgKey().equals("."))buf.append(",{width:100, xtype: 'displayfield', value: _").append(fc2.getFormCell().getDsc()).append(".fieldLabel}");
							buf.append(",_").append(fc2.getFormCell().getDsc());
						}
						buf.append("]}");

					} else {
						if (b)
							buf.append(",");
						else
							b = true;
						buf.append("_").append(fc.getFormCell().getDsc());
					}
				}
			}
			buf.append("]");
		} else { // multi column
			buf.append("cols:[");
			StringBuilder columnBuf = new StringBuilder();
			boolean b = false;
			int order = -1;
			for (int i = 0; i < formCells.size(); i++) {
				W5FormCellHelper fc = formCells.get(i);
				if (fc.getFormCell().getActiveFlag() == 0)
					continue;
				if (fc.getFormCell().getControlType() != 0) {
					if (fc.getFormCell().getTabOrder() / 1000 != order) {
						order = fc.getFormCell().getTabOrder() / 1000;
						if (columnBuf.length() > 0) {
							buf.append(columnBuf.append("]},"));
							columnBuf.setLength(0);
						}
						columnBuf
								.append("{rows:[");
						b = false;
					}
					if (i < formCells.size() - 1
							&& formCells.get(i + 1).getFormCell()
									.getControlType() != 0
							&& formCells.get(i + 1).getFormCell()
									.getActiveFlag() != 0
							&& formCells.get(i + 1).getFormCell().getTabOrder() == fc
									.getFormCell().getTabOrder()) { // yanyana
																	// koymak
																	// icin
						if (b)
							columnBuf.append(",");
						else
							b = true;
						columnBuf
								.append("{cols: [");
						
						/*if (fc.getFormCell().getExtraDefinition() != null
								&& fc.getFormCell().getExtraDefinition().length() > 1) {
							columnBuf.append(fc.getFormCell().getExtraDefinition());
						}*/
						
						if (fc.getFormCell().getControlType() == 102)// displayField4info
							columnBuf.append(serializeFormCell(0, "tr", fc,
									null));
						else
							columnBuf.append("_").append(
									fc.getFormCell().getDsc());
						while (i < formCells.size() - 1
								&& formCells.get(i + 1).getFormCell()
										.getControlType() != 0
								&& formCells.get(i + 1).getFormCell()
										.getActiveFlag() != 0
								&& formCells.get(i + 1).getFormCell()
										.getTabOrder() == fc.getFormCell()
										.getTabOrder()) {
							i++;
							W5FormCellHelper fc2 = formCells.get(i);
							// if(!fc2.getFormCell().getLocaleMsgKey().equals("."))buf.append(",{width:100, xtype: 'displayfield', value: _").append(fc2.getFormCell().getDsc()).append(".fieldLabel}");
							if (fc2.getFormCell().getControlType() == 102)// displayField4info
								columnBuf.append(",").append(
										serializeFormCell(customizationId,
												xlocale, fc2, null));
							else
								columnBuf.append(",_").append(
										fc2.getFormCell().getDsc());
						}
						columnBuf.append("]}");
					} else {
						if (b)
							columnBuf.append(",");
						else
							b = true;
						if (fc.getFormCell().getControlType() == 102)// displayField4info
							columnBuf.append(serializeFormCell(customizationId,
									xlocale, fc, null));
						else
							columnBuf.append("_").append(
									fc.getFormCell().getDsc());
					}
				}
			}
			buf.append(columnBuf.append("]}]"));
		}
//		buf.append("}");
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
	private StringBuilder serializeFormCell(int customizationId,
			String xlocale, W5FormCellHelper cellResult, W5FormResult formResult) {
		W5FormCell fc = cellResult.getFormCell();
		String value = cellResult.getValue(); // bu ilerde hashmap ten gelebilir
		// int customizationId =
		// PromisUtil.uInt(formResult.getScd().get("customizationId"));
		StringBuilder buf = new StringBuilder();
		
		buf.append("{view:'");
		
		if (fc.getControlType() == 102)
			return buf.append("label', label:'<span class=\"webix_icon icon fa-").append(labelMap[fc.getLookupQueryId()]).append("\"></span>").append(value).append("'}");
		else if ((fc.getControlType() == 101 || cellResult.getHiddenValue() != null)/* && (fc.getControlTip()!=9 && fc.getControlTip()!=16) */) {
			return buf.append("text', hiddenValue:'").append(GenericUtil.stringToJS(cellResult.getHiddenValue())).append("',label:'").append(LocaleMsgCache.get2(customizationId, xlocale, fc.getLocaleMsgKey())).append("',disabled:true, value:'").append(GenericUtil.stringToJS(value)).append("'}");
			
		}

		switch(fc.getControlType()){
		case	1:buf.append("text'");break;
		case	2:buf.append("datepicker', /*stringResult:true,*/format:webix.Date.dateToStr('%d/%m/%Y')");break;
		case	18:buf.append("datepicker', /*stringResult:true, */format:webix.Date.dateToStr('%d/%m/%Y %H:%i:%s'),timepicker:true");break;//timestamp
		case	3:buf.append("text',validate:webix.rules.isNumber");break;//int
		case	4:buf.append("text',validate:webix.rules.isNumber");break;//double
		case	5:buf.append("checkbox'");break;
		case	100:return buf.append("button',inputWidth:").append(fc.getControlWidth()).append(",label:'").append(LocaleMsgCache.get2(customizationId, xlocale, fc.getLocaleMsgKey()))
				.append("',click:function(ax){").append(fc.getExtraDefinition()).append("}}");//button

		case	6:buf.append("combo',options:[");//static combo
		if (cellResult.getLookupListValues() != null) {
			boolean b1=false;
			
			for (W5Detay p : (List<W5Detay>) cellResult
					.getLookupListValues()) {
				if (b1)
					buf.append(",");
				else
					b1 = true;
				buf.append("{id:'")
						.append(p.getVal())
						.append("',value:'")
						.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache
								.get2(customizationId, xlocale, p.getDsc())
								: p.getDsc()).append("'");
				buf.append("}");
			}
		}
		buf.append("]");
		break;
		case	8:// lovcombo-static
		case	58:// superbox lovcombo-static
			buf.append("multicombo',options:[");//static combo
			if (cellResult.getLookupListValues() != null) {
				boolean b1=false;
				
				for (W5Detay p : (List<W5Detay>) cellResult
						.getLookupListValues()) {
					if (b1)
						buf.append(",");
					else
						b1 = true;
					buf.append("{id:'")
							.append(p.getVal())
							.append("',value:'")
							.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache
									.get2(customizationId, xlocale, p.getDsc())
									: p.getDsc()).append("'");
					buf.append("}");
				}
			}
			buf.append("]");			
			break;
		case	7:buf.append("combo',options:[");//combo query
		if (cellResult.getLookupQueryResult().getData() != null){
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
					if (z == null)
						z = "";
					buf.append(f.getDsc().equals("dsc")?"value":f.getDsc()).append(":'")
							.append(f.getPostProcessType() == 2 ? LocaleMsgCache
									.get2(customizationId, xlocale,
											z.toString()) : GenericUtil
									.stringToJS(z.toString()))
							.append("'");
				}
				buf.append("}");
			}
		}
		buf.append("]");
		break; 
		case	23://treecombo(local)
		case	26://lovtreecombo(local)
			if(fc.getControlType()==26)buf.append("multi");
			buf.append("combo',suggest:{");//combo query
			if(fc.getControlType()==23 && fc.getDialogGridId()!=0 && cellResult.getExtraValuesMap()!=null && cellResult.getExtraValuesMap().containsKey("dialogGrid")){
				W5GridResult gr = (W5GridResult)cellResult.getExtraValuesMap().get("dialogGrid");
				String dsc = fc.getDsc()+"_"+gr.getGrid().getDsc();
				buf.append("view:'combo-dt-suggest',fitMaster:false, height:").append(gr.getGrid().getDefaultHeight()+(gr.getGrid().getDefaultPageRecordNumber()>0?42:0)).append(",body:{view:'layout',rows:[iwb.apply(").append(dsc).append(",{url:").append(dsc)
				.append("._url,select:!0, pager:'pager'+getForm.id,width:").append(gr.getGrid().getDefaultWidth()).append("})");
				if(gr.getGrid().getDefaultPageRecordNumber()>0)buf.append(",{ view:'pager', height:32, id:'pager'+getForm.id,  size:").append(gr.getGrid().getDefaultPageRecordNumber()).append(",  group:5 }");
				buf.append("]}}");
			
			} else {
				buf.append("body:{view:'tree', data:");
				if (cellResult.getLookupQueryResult().getData() != null){
					buf.append(serializeTreeQueryData(cellResult.getLookupQueryResult()));
				} else buf.append("[]");
				buf.append("}}");
			}
		break; // 
		case	15://lovcombo query
		case	59://superbox lovcombo query
			buf.append("multicombo',options:[");//combo query
			if (cellResult.getLookupQueryResult().getData() != null){
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
						if (z == null)
							z = "";
						buf.append(f.getDsc().equals("dsc")?"value":f.getDsc()).append(":'")
								.append(f.getPostProcessType() == 2 ? LocaleMsgCache
										.get2(customizationId, xlocale,
												z.toString()) : GenericUtil
										.stringToJS(z.toString()))
								.append("'");
					}
					buf.append("}");
				}
			}
			buf.append("]");
			break;
		case	9://combo query remote
			buf.append("combo',ds:{reload:function(){webix.message('").append(fc.getDsc()).append(".ds.reload() override it!');}},options:[]");
			if (formResult != null && fc.getLookupIncludedParams() != null
					&& fc.getLookupIncludedParams().length() > 0
					&& fc.getParentFormCellId() > 0) {
				for (W5FormCellHelper rfc : formResult.getFormCellResults()) {
					if (rfc.getFormCell().getFormCellId() == fc
							.getParentFormCellId()) {
						W5FormCell pfc = rfc.getFormCell();
						if (pfc.getControlType() == 6
								|| pfc.getControlType() == 7
								|| pfc.getControlType() == 9
								|| pfc.getControlType() == 10
								|| pfc.getControlType() == 51) {
							buf.append(
									",on:{onAfterRender:function(){combo2combo(_")
									.append(pfc.getDsc()).append(",_")
									.append(fc.getDsc())
									.append(",function(ax,bx){\n")
									.append(fc.getLookupIncludedParams())
									.append("\n},__action__)}");

							buf.append("}");
						}
						break;
					}
				}
			}
			break;
		case	25://textarea(ozel tanimlama)
		case	41://codemirror
		case	11:buf.append("textarea'");
		break; // textarea
//		{ view:"label", label:'Fill the form below to access <br>the main datacore.'
		
		case	10:
			buf.append("combo', placeholder:'").append(LocaleMsgCache.get2(0, xlocale, "autocomplete_placeholder")).append("', suggest:");
			if(fc.getDialogGridId()!=0 && cellResult.getExtraValuesMap()!=null && cellResult.getExtraValuesMap().containsKey("dialogGrid")){
				W5GridResult gr = (W5GridResult)cellResult.getExtraValuesMap().get("dialogGrid");
				String dsc = fc.getDsc()+"_"+gr.getGrid().getDsc();
				buf.append("{view:'combo-dt-suggest',fitMaster:false, height:").append(gr.getGrid().getDefaultHeight()+(gr.getGrid().getDefaultPageRecordNumber()>0?42:0)).append(",body:{view:'layout',rows:[iwb.apply(").append(dsc).append(",{url:").append(dsc)
				.append("._url,select:!0, pager:'pager'+getForm.id,width:").append(gr.getGrid().getDefaultWidth()).append("})");
				if(gr.getGrid().getDefaultPageRecordNumber()>0)buf.append(",{ view:'pager', height:32, id:'pager'+getForm.id,  size:").append(gr.getGrid().getDefaultPageRecordNumber()).append(",  group:5 }");
				buf.append("]}}");
			
			} else {
				buf.append("'ajaxQueryData?_renderer=webix3_3&_qid=").append(fc.getLookupQueryId());
				if(!GenericUtil.isEmpty(fc.getLookupIncludedParams()))buf.append("&").append(fc.getLookupIncludedParams());
				buf.append("'");
			}
		break; // advanced select
		case	71://file attachment
			buf.setLength(0);
			buf.append("{type:'clean',cols:[{view:'text',id:getForm.id+'-'+").append(fc.getFormCellId()).append(",width:").append(formResult.getForm().getLabelWidth()+fc.getControlWidth()).append(",disabled:!0,label:'").append(LocaleMsgCache //
					.get2(customizationId, xlocale,fc.getLocaleMsgKey())).append("',xtemplate:function(data){var names = [];if (data.each)data.each(function(obj){  names.push(obj.name);});var r=names.join(', ');return r;},");//, inputWidth:").append(fc.getControlWidth());
			if(!GenericUtil.isEmpty(value))buf.append(",value:'").append(GenericUtil.stringToJS(value)).append("'");
			buf.append("},{view:'uploader',css: 'button_primary button_raised', type: 'iconButton' ,icon: 'upload',width:120, link:getForm.id+'-'+").append(fc.getFormCellId()).append(",label:'Yükle',multiple:false,upload:'upload2.form?profilePictureFlag=0&table_id=").append(formResult.getForm().getObjectId()).append("&table_pk='+getFormPk(getForm)");
			if(!GenericUtil.isEmpty(fc.getExtraDefinition()))buf.append(fc.getExtraDefinition());
			buf.append("},{}]}");//{view: "uploader", label: "Dosya Yükle", upload:'upload2.form?profilePictureFlag=0&table_id='+t+'&table_pk='+k}
			return buf;
		
		default:buf.append("text'");break;
		
		
		}
		buf.append(",name:'").append(fc.getDsc()).append("'");
		if(fc.getNotNullFlag()!=0)buf.append(",required:true");
		buf.append(", label:'").append(LocaleMsgCache.get2(customizationId, xlocale, fc.getLocaleMsgKey())).append("'");
//		if(fc.getControlWidth()>0)buf.append(",inputWidth:").append(fc.getControlWidth()+(formResult!=null && formResult.getForm()!=null ? formResult.getForm().getLabelWidth() : 150));
		if(fc.getControlWidth()>0 && fc.getControlType()!=5){
			buf.append(",inputWidth:").append(4*fc.getControlWidth()/3).append(",minWidth:").append(4*(fc.getControlWidth()+(formResult!=null && formResult.getForm()!=null ? formResult.getForm().getLabelWidth() : 100))/3);
		}
	
		if(!GenericUtil.isEmpty(value))switch(fc.getControlType()){
			case	2://date && 
				buf.append(",value:'").append(GenericUtil.uDateStr(value)).append("'");
				break;
			case	18://timestamp
				if (!"0".equals(value) && value.length() <= 10)
					value = GenericUtil.uDateStr(value) + " 00:00:00";
				buf.append(",value:'")
						.append("0".equals(value) ? GenericUtil
								.uFormatDateTime(new Date()) : value)
						.append("'");			
				break;
			default:buf.append(",value:'").append(GenericUtil.stringToJS(value)).append("'");
		}
	//	if(true)buf.append(",on:{onChange:function(newv, oldv){this.validate();}}");
		if(fc.getControlType()!=100 && !GenericUtil.isEmpty(fc.getExtraDefinition()))buf.append(fc.getExtraDefinition());
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
				if (toolbarItem.getControlType() == 0
						|| toolbarItem.getControlType() == 100) { // yok(0): button
																// +
																// tooltip;button(100)
																// icon+text
					if (toolbarItem.getDsc().equals("-"))buttons.append("{ $template:'Spacer' }"); else 
					if (toolbarItem.getDsc().equals("->"))
						buttons.append("{}");
					else if (toolbarItem.getObjectType() == 15) {// form toolbar
																// ise
						buttons.append("{view:'button', value:'")
								.append(LocaleMsgCache.get2(customizationId,
										xlocale, toolbarItem.getLocaleMsgKey()))
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
								.append(LocaleMsgCache.get2(customizationId,
										xlocale, toolbarItem.getLocaleMsgKey()))
								.append("', click:function(a,b,c){\n")
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
				if (gc.get_queryField().getFieldType() > 2)
					html.append(",type:'")
							.append(FrameworkSetting.sortMap[gc.get_queryField()
									.getFieldType()]).append("'");
				html.append("}");
			}
		html.append("]),\n initRecord:{");
		b = false;
		for (W5GridColumn gc : (List<W5GridColumn>) gridResult.getGrid()
				.get_gridColumnList())
			if (gc.get_formCell() != null) {
				Object obz = null;
				switch (gc.get_formCell().getInitialSourceType()) {
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
				.append(d.getCardId())
				.append(",name:'")
				.append(LocaleMsgCache.get2(customizationId, xlocale,
						d.getLocaleMsgKey()))
				.append("'")
				.append(",_url:'ajaxQueryData?.w='+_webPageId+'&_qid=")
				.append(d.getQueryId()).append("&_dvid=")
				.append(d.getCardId());

		if (d.getDefaultPageRecordNumber() != 0)
			buf.append("&firstLimit=").append(d.getDefaultPageRecordNumber());
		buf.append("'");

		if (d.getDefaultWidth() != 0)
			buf.append(",\n defaultWidth:").append(d.getDefaultWidth());
		if (d.getDefaultHeight() != 0)
			buf.append(",\n defaultHeight:").append(d.getDefaultHeight());
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
				.append(GenericUtil.stringToJS2(d.getTemplateCode()))
				.append("\"");
		if (!GenericUtil.isEmpty(d.getJsCode())) {
			buf.append("\ntry{")
					.append(GenericUtil.filterExt(d.getJsCode(),
							dataViewResult.getScd(),
							dataViewResult.getRequestParams(), null))
					.append("\n}catch(e){")
					.append(FrameworkSetting.debug ? "if(confirm('ERROR cardView.JS!!! Throw? : ' + e.message))throw e;"
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
		
		buf.append("var ").append(dsc).append(" = {view:'").append(g.getTreeMasterFieldId()>0 ? "treetable":"datatable").append("',gridId:")
				.append(g.getGridId()).append(",queryId:").append(g.getQueryId());
		if (!gridResult.isViewLogMode() && g.getSelectionModeType()!=0){
			buf.append(", select:'row'");
			if(g.getSelectionModeType()==2 || g.getSelectionModeType()==3)
				buf.append(", multiselect:true");
		}
		buf.append(",idField:'").append(g.get_pkQueryField().getDsc()).append("'");
		
		if (gridResult.getExtraOutMap() != null
				&& !gridResult.getExtraOutMap().isEmpty()) {
			buf.append(",\n extraOutMap:")
					.append(GenericUtil.fromMapToJsonString(gridResult
							.getExtraOutMap()));
		}
			
		if (FrameworkSetting.liveSyncRecord && g.get_viewTable() != null
				&& g.get_viewTable().getLiveSyncFlag() != 0)
			buf.append(",\n liveSync:true");
		if (g.getDefaultWidth() != 0)
			buf.append(",\n defaultWidth:").append(g.getDefaultWidth());
		if (gridResult.isViewLogMode())
			buf.append(",\n defaultHeight:").append(
					FrameworkCache.getAppSettingIntValue(scd,
							"log_default_grid_height"));
		else {
			if (g.getSelectionModeType() == 2 || g.getSelectionModeType() == 3) // multi Select
				buf.append(",\n multiSelect:true");
/*			else if (g.getSelectionModeTip() == 5 && g.get_detailView() != null) // promis.js'de
																					// halledilmek
																					// uzere
				buf.append(",\n detailDlg:true"); */
			if (g.getDefaultHeight() > 0)
				buf.append(",\n defaultHeight:").append(g.getDefaultHeight());

			buf.append(",\n gridReport:").append(FrameworkCache.roleAccessControl(scd,  105));

		}
		buf.append(",\n loadMask:!0, displayInfo:").append(g.getDefaultPageRecordNumber()>0);
		
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
				if (((fsm.getSmsMailType() == 0 && FrameworkSetting.sms && FrameworkCache
						.getAppSettingIntValue(customizationId, "sms_flag") != 0) || (fsm
						.getSmsMailType() != 0 && FrameworkSetting.mail && FrameworkCache
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
							.append(fsm.getSmsMailType() == 0 ? "[<b>SMS</b>] "
									: "[<b>"
											+ (LocaleMsgCache.get2(
													customizationId, xlocale,
													"email_upper")) + "</b>] ")
							.append(GenericUtil.stringToJS(LocaleMsgCache.get2(
									customizationId, xlocale, fsm.getDsc())))
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
		

		buf.append(",on:{onLoadError:promisLoadException2} , name:'")
				.append(LocaleMsgCache.get2(customizationId, xlocale,
						g.getLocaleMsgKey())).append("',\n id:'")
				.append(GenericUtil.getNextId("ng")).append("',\n listeners:{}");

		
			buf.append(",\n ds:{reload:function(){webix.message('").append(dsc).append(".ds.reload() override it!');}},_url:'ajaxQueryData?_renderer=webix3_3&.t='+_page_tab_id+'&.w='+_webPageId+'&_qid=")
					.append(g.getQueryId()).append("&_gid=")
					.append(g.getGridId());
			if(g.getTreeMasterFieldId()>0)buf.append("&_tqd=1");//&_json=1

			if (gridResult.isViewLogMode() || g.getDefaultPageRecordNumber() != 0)
				buf.append("&firstLimit=").append(gridResult.isViewLogMode() ? FrameworkCache
								.getAppSettingIntValue(scd,
										"log_default_record_per_page") : g
								.getDefaultPageRecordNumber())
						.append("',remoteSort:true");
			else
				buf.append("'");

			
			

		if (gridResult.isViewLogMode() || g.getDefaultPageRecordNumber() != 0)
			buf.append(",\n pageSize:").append(
					gridResult.isViewLogMode() ? FrameworkCache
							.getAppSettingIntValue(scd,
									"log_default_record_per_page") : g
							.getDefaultPageRecordNumber());

		if (gridResult.getSearchFormResult() != null) {
			buf.append(",\n searchForm:").append(serializeGetForm(gridResult.getSearchFormResult()));
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
							"file_attachment_flag") != 0
							&& t.getFileAttachmentFlag() != 0
							&& FrameworkCache.roleAccessControl(scd, 101)
							&& FrameworkCache.roleAccessControl(scd, 102))
						buf.append(",\n fileAttachFlag:true");
					if (FrameworkCache.getAppSettingIntValue(customizationId,
							"make_comment_flag") != 0
							&& t.getMakeCommentFlag() != 0
							&& FrameworkCache.roleAccessControl(scd,
									103))
						buf.append(",\n makeCommentFlag:true");
				

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
			html.append("'");
			if (f.getFieldType() > 2)
				html.append(",type:'")
						.append(FrameworkSetting.sortMap[f.getFieldType()])
						.append("'");
			if (f.getFieldType() == 2)
				html.append(",type:'date',dateFormat:'d/m/Y h:i:s'");

			if (f.getPostProcessType() >= 10)
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
				if (f.getFieldType() > 2)
					html.append(",type:'")
							.append(FrameworkSetting.sortMap[f.getFieldType()])
							.append("'");
				if (f.getFieldType() == 2)
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
				if (f.getPostProcessType() > 0)
					html.append(",{name:'").append(f.getDsc()).append("_qw_'}");
				if (f.getPostProcessType() == 49)
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
					.append(c.getSortableFlag() != 0 && c.get_queryField().getPostProcessType() <90); // post
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
		if (!gridResult.isViewLogMode() && grid.get_postProcessQueryFields() != null && (gridResult.getRequestParams()==null || GenericUtil.uInt(gridResult.getRequestParams(), "_no_post_process_fields")==0)) {
			boolean gridPostProcessColumnFirst = FrameworkCache.getAppSettingIntValue(customizationId,"grid_post_process_column_first")!=0;
			boolean gridPostProcessCommentFirst = FrameworkCache.getAppSettingIntValue(customizationId,"grid_post_process_comment_first")!=0;
			int x = 0;
			for (W5QueryField f : grid.get_postProcessQueryFields()) {
				if(!f.getDsc().equals("ar_version_no")){
					if (viewTable != null)
						switch (f.getFieldType()) {
						case 2:// file attachment
						case 7:// picture attachment
							if (!FrameworkCache.roleAccessControl(
									gridResult.getScd(),101))
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
					c.setAlignType((short) 0);
					c.setLocaleMsgKey("");//:("<span class=\"webix_icon fa-"+ FrameworkSetting.postQueryGridImgMap4Webix[f.getFieldTip()]+ "\"></span>")
					c.setVisibleFlag((short) 1);
					String renderer = postQueryMap[f.getFieldType()];
					c.setRenderer(renderer);
					if(f.getDsc().equals(FieldDefinitions.queryFieldName_Comment) && FrameworkCache.getAppSettingIntValue(customizationId, "make_comment_summary_flag")!=0){
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
						newColumns.add(x, c);
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
			c_dttm.setAlignType((short) 1);
			c_dttm.setLocaleMsgKey("log_dttm");
			c_dttm.setVisibleFlag((short) 1);
			c_dttm.setRenderer("fmtDateTime");
			newColumns.add(0, c_dttm);

			W5QueryField qf_user = new W5QueryField();
			qf_user.setDsc("log5_user_id");
			W5GridColumn c_user = new W5GridColumn();
			c_user.set_queryField(qf_user);
			c_user.setWidth((short) 80);
			c_user.setAlignType((short) 1);
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
					buf.append("\n")
							.append(dsc)
							.append("._")
							.append(f.getDsc())
							.append("=")
							.append(serializeFormCell(customizationId, xlocale,
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

		for (W5GridColumn c : newColumns) {
			String qds = c.get_queryField().getDsc();

			if (b)
				buf.append(",\n");
			else
				b = true;
			boolean editableFlag = editableColumnSet.contains(c
					.getQueryFieldId());
			
			buf.append("{header:");
			if(c.getFilterFlag()==0 || c.get_queryField()==null || c.get_queryField().getFieldType()==0 || c.get_queryField().getFieldType()==5){
				buf.append("'");
				if (!editableFlag) {
					buf.append(
							LocaleMsgCache.get2(customizationId, xlocale,
									c.getLocaleMsgKey()));
				} else {
					buf.append("<b style=\"color:darkorange\">")
							.append(LocaleMsgCache.get2(customizationId, xlocale,
									c.getLocaleMsgKey())).append("</b>");
				}
				buf.append("'");
			} else { //filter
				buf.append("['");
				if (!editableFlag) {
					buf.append(LocaleMsgCache.get2(customizationId, xlocale,
									c.getLocaleMsgKey()));
				} else {
					buf.append("<b style=\"color:darkorange\">")
							.append(LocaleMsgCache.get2(customizationId, xlocale,
									c.getLocaleMsgKey())).append("</b>");
				}
				buf.append("'");
				switch(c.get_queryField().getPostProcessType()){
				case	10://static lookup
				case	11://lov-static lookup
					if(c.get_queryField().getLookupQueryId()!=0){
						W5LookUp lu = FrameworkCache.getLookUp(gridResult.getScd(),c.get_queryField().getLookupQueryId());
						if(lu==null)break;
						buf.append(",{content:'richSelectFilter', options:[");
						for(W5LookUpDetay ld:lu.get_detayList())
							buf.append("{id:'").append(GenericUtil.stringToJS(ld.getVal())).append("',value:'").append(GenericUtil.stringToJS(LocaleMsgCache.get2(gridResult.getScd(), ld.getDsc()))).append("'},");
						buf.append("]}");
					}
					break;
				case	12://lookup table
				case	13://lov-lookup table
					if(c.get_queryField().getLookupQueryId()!=0){
						W5Table ld = FrameworkCache.getTable(gridResult.getScd(),c.get_queryField().getLookupQueryId());
						if(ld==null || ld.getDefaultLookupQueryId()==0)break;
						buf.append(",{content:'richSelectFilter', options:'ajaxQueryData?_qid=").append(ld.getDefaultLookupQueryId()).append("'}");						
					}
					break;
				case	16://lookup query
				case	17://lov-lookup query
					if(c.get_queryField().getLookupQueryId()!=0){
						buf.append(",{content:'richSelectFilter', options:'ajaxQueryData?_qid=").append(c.get_queryField().getLookupQueryId()).append("'}");						
					}
					break;
				default:
					buf.append(",{content:'").append(filterMap[c.get_queryField().getFieldType()]).append("'}");
					
				}
				buf.append("]");
				
			}
			boolean qwRendererFlag = false;
			boolean boolRendererFlag = false;
			buf.append(", id: '")
					.append(qds)
					.append("'");
			if(c.getSortableFlag() != 0 && c.get_queryField().getPostProcessType() < 90)
					buf.append(", sort: '").append(grid.getDefaultPageRecordNumber()==0 ? new String[]{"","string","date","int","int","",""}[c.get_queryField().getFieldType()] : "server").append("'");
			
			if (c.getAlignType() != 1)
				buf.append(", css:{'text-align':'")
						.append(FrameworkSetting.alignMap[c.getAlignType()])
						.append("'}");// left'ten farkli ise
			if(grid.getAutoExpandFieldId()!=0 && grid.getAutoExpandFieldId()==c.getQueryFieldId())buf.append(", fillspace:!0").append(", minWidth: ").append((4*c.getWidth())/3);//.append(c.getWidth());
			else buf.append(", width: ").append((4*c.getWidth())/3);//.append(c.getWidth());

			
			W5TableField tf = c.get_queryField().getMainTableFieldId() > 0
					&& crudTable != null ? crudTable.get_tableFieldMap().get(
					c.get_queryField().getMainTableFieldId()) : null;
			if ((c.getFormCellId() < 0/* freeField? */&& crudTable != null)
					|| (insertOrEditPrivilege && c.get_formCell() != null && (tf == null || GenericUtil
							.accessControl(gridResult.getScd(), (gridResult
									.getAction() == 1 ? tf.getAccessUpdateTip()
									: tf.getAccessInsertTip()), tf
									.getAccessUpdateRoles(), tf
									.getAccessUpdateUsers()))))
				buf.append(", editor:").append(dsc).append("._")
				.append(c.get_formCell().getDsc()).append(".view").append(", options:").append(dsc).append("._")
				.append(c.get_formCell().getDsc()).append(".options");
			if (!GenericUtil.isEmpty(c.getRenderer())) {
				buf.append(", template:function(p){return ").append(c.getRenderer()).append("(p['").append(qds).append("'],this,{data:p},").append(dsc).append(");}");// browser renderer ise
				if (c.getRenderer().equals("disabledCheckBoxHtml"))
					boolRendererFlag = true;
			} else if(grid.getTreeMasterFieldId()>0 && c.getQueryFieldId()==grid.getTreeMasterFieldId()){
				W5QueryField qf = grid.get_queryFieldMap().get(grid.getTreeMasterFieldId());
				if(qf!=null)buf.append(", template:\"{common.treetable()} #").append(qf.getDsc()).append("#\"");				
			} else if (c.get_queryField().getPostProcessType() >= 10
					&& c.get_queryField().getPostProcessType() <90) {
				if (c.get_formCell() == null || !editableFlag) {
					if (FrameworkSetting.chat && (c.get_queryField().getPostProcessType() == 20 || c.get_queryField().getPostProcessType() == 53)) // user lookup ise
						buf.append(", template:gridUserRenderer('").append(qds).append("')");// browser renderer ise
					else if (c.get_queryField().getPostProcessType() == 12) // table lookup ise
						buf.append(", template:gridQwRendererWithLink('").append(qds).append("',").append(c.get_queryField().getLookupQueryId()).append(")");// browser renderer ise
					else 
						buf.append(", template:function(ax){return ax.").append(qds).append("_qw_;}");// browser renderer ise
					qwRendererFlag = true;
				}
			} else if ((qds.length() > 5
					&& qds.endsWith("_flag")) || (qds.length() > 3
							&& qds.startsWith("is_"))) {
				buf.append(", template:disabledCheckBoxHtml('").append(qds).append("')");// browser renderer ise
				boolRendererFlag = true;
			} else if (grid.get_queryFieldMapDsc().get(qds + "_qw_") != null) {
				buf.append(", template:function(ax){return ax.").append(qds)
				.append("_qw_;}");// browser renderer ise
				qwRendererFlag = true;
			}
			
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
												.append(FrameworkCache.getWorkflow(queryResult.getScd(), f.getLookupQueryId())
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
									buf2.append(obj).append("\",\"").append(f.getDsc())
											.append("_qw_\":\"").append(UserUtil.getUserName( GenericUtil.uInt(obj)));
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
												.append(FrameworkCache.getWorkflow(queryResult.getScd(), f.getLookupQueryId())
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
		if (queryResult.getQuery().getQueryType() == 10 || (queryResult.getRequestParams()!=null && GenericUtil.uInt(queryResult.getRequestParams(), "_tqd")!=0) )
			return serializeTreeQueryData(queryResult);
		if (queryResult.getQuery().getQueryType() == 14)
			return serializeTreeQueryRemoteData(queryResult);
		int customizationId = (Integer) queryResult.getScd().get("customizationId");
		String xlocale = (String) queryResult.getScd().get("locale");
		String userIdStr = queryResult.getScd().containsKey("userId") ? queryResult.getScd().get("userId").toString() : null;
		List datas = queryResult.getData();
		StringBuilder buf = new StringBuilder();
		boolean convertDateToStr = queryResult.getRequestParams()!=null && GenericUtil.uInt(queryResult.getRequestParams(), "_cdds")!=0; 
		buf.append("{\"success\":").append(queryResult.getErrorMap().isEmpty())
				.append(",\"queryId\":").append(queryResult.getQueryId())
				.append(",\"execDttm\":\"")
				.append(GenericUtil.uFormatDateTime(new Date())).append("\"");
		if (queryResult.getErrorMap().isEmpty()) {
			boolean dismissNull = queryResult.getRequestParams()!=null && queryResult.getRequestParams().containsKey("_dismissNull");
			buf.append(",\n\"data\":["); // ana
			if (datas != null && datas.size() > 0) {
				boolean bx = false;
				boolean isMap = (datas.get(0) instanceof Map);
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
						if (f.getPostProcessType() == 9)
							buf.append("\"_");
						else
							buf.append("\"");
						if(queryResult.getQuery().getQueryType()==3 && f.getDsc().equals("dsc"))
							buf.append("value");
						else 
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
						} else if(f.getFieldType() == 8) {
							buf.append("\":");
							if (obj == null)buf.append("null");
							else if(obj instanceof Map)buf.append(GenericUtil.fromMapToJsonString2Recursive((Map)obj));
							else if(obj instanceof List)buf.append(GenericUtil.fromListToJsonString2Recursive((List)obj));
							else buf.append(obj);
							continue;
						} else if (convertDateToStr && f.getFieldType() == 2 && obj!=null && (obj instanceof java.sql.Timestamp || obj instanceof java.util.Date)) {// date 
							buf.append("\":\"").append(obj instanceof java.sql.Timestamp ? GenericUtil.uFormatDateTime((java.sql.Timestamp)obj) : GenericUtil.uFormatDateTime((java.util.Date)obj)).append("\"");
							continue;
						}
						buf.append("\":\"");
						if (obj != null)
							switch (f.getPostProcessType()) { // queryField
																// PostProcessTip
							case	15://convert to []
								buf.setLength(buf.length()-1);
								buf.append("[").append(obj).append("]");
								continue;
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
								W5Workflow appr = FrameworkCache.getWorkflow(queryResult.getScd(), appId);
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
		if (page.getPageType() != 0) { // html degilse
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

			if (page.getPageType() != 8) { // wizard degilse
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
						if (dr.getCardId() < 0) {
							buf.append("\nvar _dataView")
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
						if (Math.abs(fr.getObjectType()) == 3) { // form
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
					buf.append("{\"objTip\":").append(o.getObjectType())
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
				FrameworkCache.addPageResource(pr.getScd(), "css-"+page.getPageId(), buf4.toString());
				code = code.replace("${promis-css}", " <link rel=\"stylesheet\" type=\"text/css\" href=\"/app/dyn-res/css-"+page.getPageId()+".css?.x="+page.getVersionNo()+"\" />");

			}
			
			
		}
	
		if(!GenericUtil.isEmpty(code))
			buf.append("\n").append(code.startsWith("!") ? code.substring(1) : code);

//		short ttip= templateResult.getPage().getTemplateTip();
//		if((ttip==2 || ttip==4) && !GenericUtil.isEmpty(templateResult.getPageObjectList()))buf.append("\n").append(renderTemplateObject(templateResult));
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

	public StringBuilder serializeTableRecordInfo(
			W5TableRecordInfoResult tableRecordInfoResult) {
		String xlocale = (String) tableRecordInfoResult.getScd().get("locale");
		int customizationId = (Integer) tableRecordInfoResult.getScd().get(
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
								.getTable(customizationId, trh0.getTableId())
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
					.getTable(customizationId, trh.getTableId());
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
					W5Table dt = FrameworkCache.getTable(customizationId, tch
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
					if (!FrameworkCache.roleAccessControl(scd,  0))
						continue;
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
				W5Table dt = FrameworkCache.getTable(customizationId,
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
