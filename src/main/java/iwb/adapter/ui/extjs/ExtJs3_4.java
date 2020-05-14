package iwb.adapter.ui.extjs;

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
import iwb.domain.db.Log5Feed;
import iwb.domain.db.W5BIGraphDashboard;
import iwb.domain.db.W5Card;
import iwb.domain.db.W5Conversion;
import iwb.domain.db.W5ConvertedObject;
import iwb.domain.db.W5CustomGridColumnCondition;
import iwb.domain.db.W5CustomGridColumnRenderer;
import iwb.domain.db.W5Detay;
import iwb.domain.db.W5Form;
import iwb.domain.db.W5FormCell;
import iwb.domain.db.W5FormCellProperty;
import iwb.domain.db.W5FormHint;
import iwb.domain.db.W5FormModule;
import iwb.domain.db.W5FormSmsMail;
import iwb.domain.db.W5FormSmsMailAlarm;
import iwb.domain.db.W5GlobalFuncParam;
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
import iwb.domain.db.W5WsMethod;
import iwb.domain.db.W5WsMethodParam;
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
import iwb.util.EncryptionUtil;
import iwb.util.GenericUtil;
import iwb.util.HtmlFilter;
import iwb.util.UserUtil;

public class ExtJs3_4 implements ViewAdapter {
	final public static String[] dateFormatMulti = new String[] {"d/m/Y","m/d/Y","Y/m/d"};

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

	public StringBuilder serializePostForm(W5FormResult fr) {
		String xlocale = (String) fr.getScd().get("locale");
		StringBuilder buf = new StringBuilder();

		buf.append("{\n\"formId\": ").append(fr.getFormId())
				.append(",\n\"success\": ")
				.append(fr.getErrorMap().isEmpty());
		if (!fr.getErrorMap().isEmpty())
			buf.append(",\n\"errorType\":\"validation\",\n\"errors\":")
					.append(serializeValidatonErrors(fr.getErrorMap(),
							xlocale));

		if (!fr.getOutputMessages().isEmpty()) {
			buf.append(",\n\"msgs\":[");
			boolean b = false;
			for (String s : fr.getOutputMessages()) {
				if (b)
					buf.append("\n,");
				else
					b = true;
				buf.append("\"").append(GenericUtil.stringToJS2(s)).append("\"");
			}
			buf.append("]");
		}
		if (!fr.getOutputFields().isEmpty()) {
			buf.append(",\n\"outs\":").append(
					GenericUtil.fromMapToJsonString2Recursive(fr
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
		if (!GenericUtil.isEmpty(fr.getPreviewMapList())) {
			buf.append(",\n\"smsMailPreviews\":[");
			boolean b = false;
			for (Map<String, String> m : fr.getPreviewMapList()) {
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
		if (!GenericUtil.isEmpty(fr.getFormAlarmList())) {
			buf.append(",\n\"alarmPreviews\":[");
			boolean b = false;
			for (W5FormSmsMailAlarm fsma : fr.getFormAlarmList()) {
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
		if (!GenericUtil.isEmpty(fr.getPreviewConversionMapList())) {
			buf.append(",\n\"conversionPreviews\":[");
			boolean b = false;
			for (Map<String, String> m : fr
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
		
		if(GenericUtil.uInt(formResult.getRequestParams().get("_preview")) == 0 && formResult.getForm().getRenderType()==4) {//wizard and insert
			return SurveyJS.serializeForm4SurveyJS(formResult, 1).append("return new Ext.Panel({closable:!0, title:'").append(LocaleMsgCache.get2(formResult.getScd(), formResult.getForm().getLocaleMsgKey())).append("',html:'<div id=\"surveyElement-' + _page_tab_id + '\" style=\"width:100%;height:100%;overflow:auto\"></div>',listeners:{afterrender:()=>$('#surveyElement-' + _page_tab_id).Survey({ model: survey })}})");//extjs			
		}
		

		boolean liveSyncRecord = FrameworkSetting.liveSyncRecord
				&& formResult != null && formResult.getForm() != null
				&& formResult.getForm().getObjectType() == 2
				&& formResult.getAction() == 1;
		if (GenericUtil.uInt(formResult.getRequestParams().get("a")) != 5
				&& formResult.getForm().getRenderType() != 0) { // tabpanel ve
																// icinde
																// gridler varsa
			for (W5FormModule m : formResult.getForm().get_moduleList()) {
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
									&& (!FrameworkCache.roleAccessControl(
											formResult.getScd(), 0) | !GenericUtil
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

	/*	if (formResult.getRequestParams() != null
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
		} else */
		if(formResult.getScd()==null || (Integer)formResult.getScd().get("roleId")!=0 || GenericUtil.uInt(formResult.getRequestParams().get("_preview"))==0){
			if (formResult.getForm().get_renderTemplate() != null) {
				s.append("\n").append(
						formResult.getForm().get_renderTemplate()
								.getLocaleMsgFlag() != 0 ? GenericUtil
								.filterExt(formResult.getForm()
										.get_renderTemplate().getCode(),
										formResult.getScd(),
										formResult.getRequestParams(), null)
								: formResult.getForm().get_renderTemplate()
										.getCode());
			} else if(formResult.getForm().getObjectType()==2){
				s.append("\nreturn iwb.ui.buildCRUDForm(getForm, callAttributes, _page_tab_id);\n");
			}
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
						for (W5Detay p : (List<W5Detay>) fc
								.getLookupListValues()) {
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
	
	private String getPropertyMethodName(String dsc, short lkpPropertyTip, boolean b, String label) {
		StringBuilder s =new StringBuilder();
		switch(lkpPropertyTip) {
		case	0://required
			s.append("iwb.changeFieldLabel(_").append(dsc).append(",\"").append(label);
			if(b)s.append("<i class='label-required'></i>");
			s.append("\")");
			return s.toString();
		case	1://visible
			s.append("_").append(dsc);
			if(b)s.append(".show()");
			else s.append(".hide()");
			return s.toString();
		case	2://readonly
			s.append("_").append(dsc).append(".setReadOnly(").append(b).append(")");
			return s.toString();
		}
		return "";		
	}
	
	@SuppressWarnings("unchecked")
	private StringBuilder serializeFormCellDependencies(W5FormCellHelper cellResult, W5FormResult formResult) {
		StringBuilder buf = new StringBuilder();
		for(W5FormCellHelper fcr:formResult.getFormCellResults())if(fcr.getFormCell().getActiveFlag()!=0) {
			if(!GenericUtil.isEmpty(fcr.getFormCell().get_formCellPropertyList()))for(W5FormCellProperty fcp:fcr.getFormCell().get_formCellPropertyList()) if(cellResult.getFormCell().getFormCellId()==fcp.getRelatedFormCellId()) {
				String dsc = cellResult.getFormCell().getDsc();
				if(buf.length()==0) {
					buf.append("\nmf._").append(dsc).append(".on('");
					switch(cellResult.getFormCell().getControlType()) {
					case	5://checkbox
						buf.append("check");break;
					case	2://date
					case	11://timestamp
						buf.append("select");break;
					case	6://static lookup
					case	8://multi slu
					case	7://lu query
					case	15://multi lu query
						if(cellResult.getFormCell().getParentFormCellId()!=1) {
							buf.append("select");
							break;
						}
					default:
						buf.append("change");
					}
					buf.append("', ()=>onChange_").append(dsc).append("());\nfunction onChange_").append(dsc).append("(){var ax = getFieldValue(_").append(dsc).append(");\n");
				}
				String localMsg = fcp.getLkpPropertyType()!=0 ? "":LocaleMsgCache.get2(formResult.getScd(), fcr.getFormCell().getLocaleMsgKey());
				if(cellResult.getFormCell().getControlType()==5) {//checkbox
					if(fcp.getLkpPropertyType()!=0)
						buf.append("if(").append(fcp.getLkpOperatorType()==0?"!":"").append("ax)").append(getPropertyMethodName(fcr.getFormCell().getDsc(), fcp.getLkpPropertyType(), true, "")).
							append("; else ").append(getPropertyMethodName(fcr.getFormCell().getDsc(), fcp.getLkpPropertyType(), false, ""));
				} else if(fcp.getLkpOperatorType()<0){//isEmpty
					buf.append("if(").append(fcp.getLkpOperatorType()==-2?"!":"").append("(ax=='' || typeof ax=='undefined'))").append(getPropertyMethodName(fcr.getFormCell().getDsc(), fcp.getLkpPropertyType(), true, localMsg)).
					append("; else ").append(getPropertyMethodName(fcr.getFormCell().getDsc(), fcp.getLkpPropertyType(), false, localMsg));
				} else buf.append("if(iwb.formElementProperty(").append(fcp.getLkpOperatorType()).append(", ax, '")
					.append(GenericUtil.stringToJS(fcp.getVal())).append("'))").append(getPropertyMethodName(fcr.getFormCell().getDsc(), fcp.getLkpPropertyType(), true, localMsg)).
				append("; else ").append(getPropertyMethodName(fcr.getFormCell().getDsc(), fcp.getLkpPropertyType(), false, localMsg));
				
				buf.append("\n");
			}
			if(fcr.getFormCell().getControlType()==2 && fcr.getFormCell().getParentFormCellId()==cellResult.getFormCell().getFormCellId()) { //date and dependant
				String dsc = cellResult.getFormCell().getDsc();
				if(buf.length()==0) {
					buf.append("\nmf._").append(dsc).append(".on('select', ()=>onChange_").append(dsc).append("());\nfunction onChange_").append(dsc).append("(){var ax = getFieldValue(_").append(dsc).append(");\n");
				}
				
				buf.append("mf._").append(fcr.getFormCell().getDsc()).append(".setMinValue(ax?ax:'');\n");
				
			}
			
		}

		if(buf.length()>0)buf.append("\n};\nonChange_").append(cellResult.getFormCell().getDsc()).append("();\n");
		return buf;
	}

	private StringBuilder serializeGetForm(W5FormResult fr) {
		Map<String, Object> scd = fr.getScd();
		if(fr.getUniqueId() == null)fr.setUniqueId(GenericUtil.getNextId("fi"));

		StringBuilder s = new StringBuilder();
		String xlocale = (String) scd.get("locale");
		boolean dev = GenericUtil.uInt(fr.getRequestParams(),"_dev")!=0;
		int customizationId = dev ? 0:(Integer) fr.getScd().get("customizationId");
		int userId = (Integer) scd.get("userId");
		boolean mobile = GenericUtil.uInt(scd.get("mobile")) != 0;

		W5Form f = fr.getForm();
		// s.append("var ").append(formResult.getForm().getDsc()).append("=");
		String[] postFormStr = new String[] { "", "search_form",
				"ajaxPostForm",
				/*f.getObjectTip() == 3 ? "rpt/" + f.getDsc() : */"ajaxExecDbFunc",
				"ajaxExecDbFunc",null,null,"search_form", "search_form", null,null,"ajaxCallWs?serviceName="+FrameworkCache.getServiceNameByMethodId(scd,  f.getObjectId())};
		s.append("{\n formId: ")
				.append(fr.getFormId())
				.append(", a:").append(fr.getAction()).append(", name:'")
				.append(LocaleMsgCache.get2(customizationId, xlocale, fr.getForm().getLocaleMsgKey()))
				.append("',id:'").append(fr.getUniqueId())
				.append("',\n defaultWidth:").append(f.getDefaultWidth()).append(", defaultHeight:").append(f.getDefaultHeight());

		if (f.get_formHintList() != null) {
			boolean b = false;
			for (W5FormHint sx : f.get_formHintList())
				if (sx.getLocale().equals(xlocale)
						&& (sx.getActionTips().contains(
								"" + fr.getAction())
								|| fr.getForm().getObjectType() == 3 || fr
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
				&& FrameworkCache.getTable(scd, f.getObjectId()) != null) {
			s.append(",\n renderTip:").append(fr.getForm().getRenderType());
			W5Table t = FrameworkCache.getTable(scd, f.getObjectId());
			liveSyncRecord = FrameworkSetting.liveSyncRecord
					&& t.getLiveSyncFlag() != 0 && !fr.isViewMode();
			// insert AND continue control
			s.append(", crudTableId:").append(f.getObjectId());
			if (fr.getAction() == 2) { // insert
				long tmpId = -GenericUtil.getNextTmpId();
				s.append(", contFlag:").append(f.getContEntryFlag() != 0)
						.append(", tmpId:").append(tmpId);
				fr.getRequestParams().put("_tmpId", "" + tmpId);
			} else if (fr.getAction() == 1) { // edit
				s.append(",\n pk:").append(GenericUtil.fromMapToJsonString(fr.getPkFields()));
				if(t.getAccessDeleteTip()==0 || !GenericUtil.isEmpty(t.getAccessDeleteUserFields()) || GenericUtil.accessControl(scd, t.getAccessDeleteTip(), t.getAccessDeleteRoles(), t.getAccessDeleteUsers()))s.append(", deletable:!0");
				if (liveSyncRecord) {
					s.append(", liveSync:true");
					String webPageId = fr.getRequestParams().get(".w");
					if (webPageId != null) {
						String key = "";
						for (String k : fr.getPkFields().keySet())
							if (!k.startsWith("customization") && !k.startsWith("project"))
								key += "*" + fr.getPkFields().get(k);
						if (key.length() > 0) {
							key = t.getTableId() + "-" + key.substring(1);
							fr.setLiveSyncKey(key);
							List<Object> l = UserUtil.syncGetListOfRecordEditUsers((String)scd.get("projectId"), key, webPageId);
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
				if (fr.getAction() == 1)
					s.append(", copyFlag:true");
				else if (fr.getRequestParams().get("a") != null
						&& fr.getRequestParams().get("a").equals("5")) {// kopyalama
																				// yapilacak
																				// sorulacaklari
																				// diz
					s.append(", copyTableIds:[");
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

			if (FrameworkCache.getAppSettingIntValue(scd, "log_flag") != 0
					&& (t.getDoUpdateLogFlag() != 0 || t.getDoInsertLogFlag() != 0)
					&& FrameworkCache.roleAccessControl(scd, 108))
				s.append(",\n logFlags:{edit:")
						.append(t.getDoUpdateLogFlag() != 0).append(",insert:")
						.append(t.getDoInsertLogFlag() != 0).append("}");
			if (FrameworkCache.roleAccessControl(scd,109))
				s.append(",\n smsMailTemplateCrudFlag:true");
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
		
			if (FrameworkCache.getAppSettingIntValue(scd, "file_attachment_flag") != 0
					&& t.getFileAttachmentFlag() != 0
					&& FrameworkCache.roleAccessControl(scd,101))
				s.append(",\n fileAttachFlag:true, fileAttachCount:").append(
						fr.getFileAttachmentCount());

			if (fr.isViewMode())
				s.append(",\n viewMode:true");

			if (!fr.isViewMode() && f.get_formSmsMailList() != null
					&& !f.get_formSmsMailList().isEmpty()) { // automatic sms
																// isleri varsa
				int cnt = 0;
				for (W5FormSmsMail fsm : f.get_formSmsMailList())
					if (fsm.getSmsMailSentType() != 3
							&& ((fsm.getSmsMailType() == 0 && FrameworkSetting.sms) || (fsm
									.getSmsMailType() != 0 && FrameworkSetting.mail))
							&& fsm.getAlarmFlag() == 0
							&& GenericUtil.hasPartInside2(fsm.getActionTypes(),
									fr.getAction())
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
											&& FrameworkSetting.sms) || (fsm.getSmsMailType() != 0
											&& FrameworkSetting.mail))
									&& fsm.getAlarmFlag() != 0
									&& GenericUtil.hasPartInside2(
											fsm.getActionTypes(),
											fr.getAction())
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
							W5Table dt = fsm.getSrcDstTip()==0 ? FrameworkCache.getTable(scd,fsm.getDstTableId()) : null;
							if (dt==null || ((dt.getAccessViewTip() == 0
									|| !GenericUtil.isEmpty(dt
											.getAccessUpdateUserFields()) || GenericUtil
										.accessControl(scd,
												dt.getAccessViewTip(),
												dt.getAccessViewRoles(),
												dt.getAccessViewUsers()))
									&& GenericUtil.accessControl(scd,
											dt.getAccessInsertTip(),
											dt.getAccessInsertRoles(),
											dt.getAccessInsertUsers()))) {
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
											.append(fr.getAction() == 2 ? (fsm
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
										if (!GenericUtil.isEmpty(co.get_relatedRecord())) {
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
															fr.getScd(),
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
									f.get_conversionList(), dev)).append("]");
				}
			}
		}
		if (!fr.getOutputMessages().isEmpty()) {
			s.append(",\n\"msgs\":[");
			boolean b = false;
			for (String sx : fr.getOutputMessages()) {
				if (b)
					s.append("\n,");
				else
					b = true;
				s.append("'").append(GenericUtil.stringToJS(sx)).append("'");
			}
			s.append("]");
		}

		if (fr.getApprovalRecord() != null) { // Burası Artık Onay
														// Mekanizması başlamış
			W5Workflow a = FrameworkCache.getWorkflow(fr.getScd(),fr.getApprovalRecord().getApprovalId());
			if (fr.getApprovalRecord().getApprovalStepId() == 901) {// kendisi
																			// start
																			// for
																			// approval
																			// yapacak
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
					s.append(",\n approval:{status:901,dsc:\"").append(LocaleMsgCache.get2(scd, a.getDsc()))
					.append("\",btnStartApprovalLabel:\"").append(LocaleMsgCache.get2(scd, a.getApprovalRequestMsg()))
					.append("\", approvalRecordId:")
							.append(fr.getApprovalRecord()
									.getApprovalRecordId())
							.append(",wait4start:true}"); //").append(",dynamic:").append(a.getApprovalFlowTip() == 3).append("
			} else if (GenericUtil.accessControl(scd, (short) 1, fr
					.getApprovalRecord().getApprovalRoles(), fr
					.getApprovalRecord().getApprovalUsers())) {
				// TODO:buraya e-sign ile ilgili kontrol eklenecek. dinamik onay
				// varsa approval değilse aprrovalstep kontrol edilecek
				s.append(",\n approval:{approvalRecordId:")
						.append(fr.getApprovalRecord()
								.getApprovalRecordId())
						.append(",dsc:\"").append(LocaleMsgCache.get2(scd, a.getDsc()))
						.append("\", versionNo:")
						.append(fr.getApprovalRecord().getVersionNo())
						.append(",returnFlag:")
						.append(fr.getApprovalRecord().getReturnFlag() != 0);
				W5WorkflowStep wfs = a.get_approvalStepMap().get(fr.getApprovalRecord().getApprovalStepId());
				if(wfs.getOnApproveFormId()!=null)s.append(",approveFormId:").append(wfs.getOnApproveFormId());
				if(wfs.getOnRejectFormId()!=null)s.append(",rejectFormId:").append(wfs.getOnRejectFormId());
				if(wfs.getOnReturnFormId()!=null)s.append(",returnFormId:").append(wfs.getOnReturnFormId());
				s.append(",stepDsc:\"")
						.append(fr.getApprovalStep() != null ? fr.getApprovalStep().getDsc() : "-")
						.append("\"}");
			} else {
				s.append(",\n approval:{stepDsc:\"").append(fr.getApprovalStep() != null ? fr.getApprovalStep().getDsc() : "-")
				.append("\", dsc:\"").append(LocaleMsgCache.get2(scd, a.getDsc())).append("\"}");
			}
		} else { // Onay mekanizması başlamamış ama acaba başlatma isteği manual
					// yapılabilir mi ? Formun bağlı olduğu tablonun onay
					// mekanizması manualStart + Elle Başlatma İsteği aktif mi
			W5Table t = FrameworkCache.getTable(scd, f.getObjectId());
			if (t != null && t.get_approvalMap() != null
					&& t.get_approvalMap().get((short) 2) != null) {
				W5Workflow a = t.get_approvalMap().get((short) 2);
				if (a.getManualDemandStartAppFlag() != 0
						&& a.getApprovalRequestTip() == 2)
					s.append(",\n manualStartDemand:true");
			}
		}
		if (!GenericUtil.isEmpty(f.get_toolbarItemList())) { // extra buttonlari var mi yok
													// mu?
			StringBuilder buttons = serializeToolbarItems(scd,
					f.get_toolbarItemList(), (fr.getFormId() > 0 ? true
							: false));
			if (buttons.length() > 1) {
				s.append(",\n extraButtons:[").append(buttons).append("]");
			}
		}
		for (String sx : fr.getOutputFields().keySet()) {
			s.append(",\n ").append(sx).append(":")
					.append(fr.getOutputFields().get(sx));// TODO:aslinda
																	// ' li
																	// olması
																	// lazim
		}

		if (liveSyncRecord)
			fr.getRequestParams().put(".t", fr.getUniqueId());
		s.append(",\n render:function(){\nvar mf={_formId:").append(
				fr.getFormId());
//		if (liveSyncRecord)
		s.append(",id:'").append(fr.getUniqueId()).append("'");
		s.append(",baseParams:")
				.append(GenericUtil.fromMapToJsonString(fr
						.getRequestParams()))
				.append(",\nlabelAlign:'")
				.append(FrameworkSetting.alignMap[fr.getForm()
						.getLabelAlignType()]).append("', labelWidth:")
				.append(fr.getForm().getLabelWidth());
		if(fr.getForm().getObjectType()<5 || fr.getForm().getObjectType()==11)s.append(",url:'")
				.append(postFormStr[fr.getForm().getObjectType()])
				.append("'");
		s.append("}\n");
		/*
		 * if(PromisSetting.liveSyncRecord && formResult!=null &&
		 * formResult.getForm()!=null && formResult.getForm().getObjectTip()==2
		 * && formResult.getAction()==1){
		 * s.append("tab={_l:{pk:,t:'").append(formResult
		 * .getUniqueId()).append("'}}"); }
		 */
		
		
		for(W5FormCell fc:fr.getForm().get_formCells())if(fc.getControlType()==99 && fc.get_sourceObjectDetail()!=null){//grid is
			W5GridResult gr = fr.getModuleGridMap().get(fc.getLookupQueryId());
			s.append(serializeGrid(gr)).append("\n");
		}

		StringBuilder s2 =new StringBuilder();
		for (W5FormCellHelper fc : fr.getFormCellResults())
			if (fc.getFormCell().getActiveFlag() != 0) {
				if (fc.getFormCell().getControlType() != 102) {// only if it is not label
					s.append("var _")
							.append(fc.getFormCell().getDsc())
							.append("=")
							.append("mf._")
							.append(fc.getFormCell().getDsc())
							.append("=")
							.append(serializeFormCell(customizationId, xlocale,
									fc, fr)).append("\n");
					s2.append(serializeFormCellDependencies(fc, fr));
					// if(fc.getFormCell().getControlTip()==24)s.append("_").append(fc.getFormCell().getDsc()).append(".treePanel.getRootNode().expand();\n");
				} else {
					fc.setValue(LocaleMsgCache.get2(customizationId, xlocale,
							fc.getFormCell().getLocaleMsgKey()));
				}
			}

		s.append("\nvar __action__=").append(fr.getAction()).append(";\n");
		s.append(s2);
		
		if(scd==null || scd.get("roleId")==null || ((Integer)scd.get("roleId")!=0 || GenericUtil.uInt(fr.getRequestParams(),"_preview")==0)){
			// 24 nolu form form edit form olduğu için onu çevirmesin.
			String postCode = (fr.getForm().get_renderTemplate() != null && fr.getForm().get_renderTemplate().getLocaleMsgFlag() == 1 && fr
					.getFormId() != 24) ? GenericUtil.filterExt(
					fr.getForm().getJsCode(), scd,
					fr.getRequestParams(), null).toString() : fr
					.getForm().getJsCode();
	
			boolean b = true;
			if (postCode != null && postCode.length() > 10) {
				if (postCode.charAt(0) == '!') {
					postCode = postCode.substring(1);
				} else
					b = false;
			} else
				postCode = "";
			if (!GenericUtil.isEmpty(postCode)) {
				s.append("try{");
				if(FrameworkSetting.debug)s.append("\n/*iwb:start:form:").append(fr.getFormId()).append(":Code*/\n");
				s.append(postCode);
				if(FrameworkSetting.debug)s.append("\n/*iwb:end:form:").append(fr.getFormId()).append(":Code*/\n");
				s.append("\n}catch(e){");
				s.append(FrameworkSetting.debug ? "if(confirm('ERROR form.JS!!! Throw? : ' + e.message))throw e;"
						: "alert('System ERROR : ' + e.message)");
				
				s.append("}\n");
			}

		}
		
		
		List<W5Grid> mdGrids = new ArrayList();
		if (f.getObjectType()==2 && f.get_moduleList() != null)
			for (W5FormModule m : f.get_moduleList())
				if (m.getFormModuleId() != 0 && m.getModuleType()==5 && (m.getModuleViewType() == 0 || fr.getAction() == m
							.getModuleViewType())) {
					W5Table t = FrameworkCache.getTable(scd, f.getObjectId());
					if(t!=null && !GenericUtil.isEmpty(t.get_tableChildList()) && 
							(GenericUtil.isEmpty(fr.getForm().getJsCode()) || (!fr.getForm().getJsCode().contains("prepareDetailGridCRUDButtons") && !fr.getForm().getJsCode().contains("mf.componentWillPost")))) {
						W5GridResult gr = fr.getModuleGridMap().get(m.getObjectId());
						if(gr!=null && gr.getGrid().get_query()!=null && gr.getGrid().get_query().getSourceObjectId()!=0) {
							W5Table dt = FrameworkCache.getTable(scd, gr.getGrid().get_query().getSourceObjectId());
							if(dt!=null)for(W5TableChild tc:t.get_tableChildList()) if(tc.getRelatedTableId() == dt.getTableId()){
								s.append("prepareDetailGridCRUDButtons(").append(gr.getGrid().getDsc())
								.append(", {").append(dt.get_tableParamList().get(0).getDsc()).append(": '").append(dt.get_tableParamList().get(0).getExpressionDsc())
								.append("'});\nif(__action__==1){\n").append(gr.getGrid().getDsc())
								.append(".ds.baseParams = { x").append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc()).append(": mf.baseParams.t")
								.append(t.get_tableFieldList().get(0).getDsc());
								if(tc.getRelatedStaticTableFieldId()>0 && tc.getRelatedStaticTableFieldVal()>0)
									s.append(",").append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append(":'!")
										.append(tc.getRelatedStaticTableFieldVal()).append("'");
								s.append(" };\n").append(gr.getGrid().getDsc())
								.append(".ds.reload();\n").append(gr.getGrid().getDsc())
								.append("._postInsertParams={").append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc())
								.append(":mf.baseParams.t").append(t.get_tableFieldList().get(0).getDsc()).append("}} else\n").append(gr.getGrid().getDsc())
								.append("._postMap={");
								for(W5GridColumn gc:gr.getGrid().get_gridColumnList())if(gc.get_formCell()!=null)
									s.append(gc.get_formCell().getDsc()).append(":'").append(gc.get_formCell().getDsc()).append("',");
									
								s.append("}\n");
								mdGrids.add(gr.getGrid());
							}
						}
					}
					
				}
		if(!mdGrids.isEmpty()) {
			s.append("\nmf.componentWillPost=(vals)=>{\nvar prefix = 1, params = {}, fncx = __action__==2?prepareParams4gridINSERT:prepareParams4grid;");
			for(W5Grid gxx: mdGrids) {
				s.append("\nparams=Ext.apply(params, fncx(").append(gxx.getDsc())
					.append(",''+prefix));if(params && params['_fid'+prefix])prefix++;\n");
			}
			s.append("\nreturn params && params._fid1? params : true;}\n");
		}


		
		
		switch (fr.getForm().getRenderType()) {
		case 1:// fieldset
		case	4://wizard
			s.append(renderFormFieldset(fr));
			break;
		case 2:// tabpanel
			s.append(renderFormTabpanel(fr));
			break;
		case 3:// tabpanel+border
			s.append(renderFormTabpanelBorder(fr));
			break;
		case 0:// temiz
			s.append(
					renderFormModuleList(customizationId, xlocale,
							fr.getUniqueId(),
							fr.getFormCellResults(),
							"mf=Ext.apply(mf,{xtype:'form', border:false")).append(");\n");
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
		String formBodyStyle = FrameworkCache.getAppSettingStringValue(
				formResult.getScd(), "form_body_style");
		StringBuilder buf = new StringBuilder();
		buf.append("mf=Ext.apply(mf,{xtype:'form', border:false, items:[");
		if (map.get(0).size() > 0) {
			buf.append(renderFormModuleList(customizationId, xlocale,
					formResult.getUniqueId(), map.get(0), "{xtype:'fieldset'"
							+ (GenericUtil.isEmpty(formBodyStyle) ? ""
									: ",bodyStyle:'" + formBodyStyle + "'")));

			// (formBodyColor!=null ?
			// ",bodyStyle:'background-color:#"+formBodyColor+";background-image:url(../images/custom/bubble.png);background-repeat:no-repeat'"
			// : "")));
		}

		boolean b = false;
		int tabHeight = 0;
		buf.append(",{xtype:'tabpanel',id:_page_tab_id + '_").append(formResult.getFormId()).append("',cls:'iwb-detail-tab',activeTab: 0, border:false,deferredRender:false,defaults:{bodyStyle:'padding:0px'}, items:[");// defaults:{autoHeight:true, bodyStyle:'padding:10px'},
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
								&& (!FrameworkCache.roleAccessControl(
										formResult.getScd(),0) | !GenericUtil
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
							buf.append("Ext.apply(")
									.append(subFormResult.getForm().getDsc())
									.append(",{id:_page_tab_id+'_fm_").append(m.getFormModuleId()).append("',bodyStyle:'min-height:550px;padding-top:10px;',autoScroll:true,xtype:null,layout:'form',title:'")
									.append(LocaleMsgCache.get2(
											customizationId, xlocale,
											m.getLocaleMsgKey()))
									.append("',height:")
									.append(subFormResult.getForm()
											.getDefaultHeight())
									.append("})");
							extendedForms.add(subFormResult.getForm().getDsc());
						}
						tabHeight = subFormResult.getForm().getDefaultHeight();
						break;
					case 5:// grid(edit)
						if (formResult.getModuleGridMap() == null)
							break;
						if (GenericUtil.uInt(formResult.getRequestParams().get(
								"a")) == 5)
							break;
						W5GridResult gridResult = formResult.getModuleGridMap()
								.get(m.getObjectId());
						tabHeight = gridResult.getGrid().getDefaultHeight();
						W5Table mainTable = gridResult.getGrid() != null
								&& gridResult.getGrid().get_defaultCrudForm() != null ? FrameworkCache
								.getTable(formResult.getScd(), gridResult.getGrid()
										.get_defaultCrudForm().getObjectId())
								: null;
						if (mainTable != null
								&& (!FrameworkCache.roleAccessControl(
										formResult.getScd(),0) | !GenericUtil
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
									.append(formResult.isViewMode() ? (gridResult.getGrid().getTreeMasterFieldId() == 0 ? "Ext.grid.GridPanel": "Ext.ux.maximgb.tg.GridPanel")
											: (gridResult.getGrid().getTreeMasterFieldId() == 0 ? "Ext.grid.EditorGridPanel" : "Ext.ux.maximgb.tg.EditorGridPanel"))
									.append("(Ext.apply(").append(gridResult.getGrid().getDsc())
									.append(",{id:_page_tab_id+'_fm_'+"+m.getFormModuleId()+",border:false,bodyStyle:'min-height:").append(gridResult.getGrid().getDefaultHeight())
									.append("',title:'").append(LocaleMsgCache.get2(customizationId, xlocale, m.getLocaleMsgKey()))
									.append("',height:").append(gridResult.getGrid().getDefaultHeight())
									.append(",autoScroll:true,clicksToEdit: 1*_app.edit_grid_clicks_to_edit}))");
						}
						break;
					default:
						if (!map.get(m.getFormModuleId()).isEmpty()) {
							if (b)
								buf.append(",");
							else
								b = true;
							W5FormCellHelper extraInfo = getModulExtraInfo(
									(String) formResult.getScd().get("locale"),
									m.getLocaleMsgKey());
							if (extraInfo != null)
								map.get(m.getFormModuleId()).add(0, extraInfo);
							List<W5FormCellHelper> lfch = map.get(m.getFormModuleId());
							String extra = "{id:_page_tab_id+'_fm_"+m.getFormModuleId()+"',title:'"
									+ LocaleMsgCache.get2(customizationId,
											xlocale, m.getLocaleMsgKey()) + "'";
							if(lfch.size()==1 && lfch.get(0).getFormCell().getControlType()==41){
								extra+=",layout:'fit'";
							} else extra+=",layout:'form',bodyStyle:'min-height:550px;padding-top:10px;',autoScroll:true";

							buf.append(renderFormModuleList(customizationId,
									xlocale, formResult.getUniqueId(),
									lfch, extra));
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
		/* new Ext.grid.GridPanel(Ext.apply(detailGrid,grdExtra)) */
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
		buf.append("mf=Ext.apply(mf,{xtype:'form', layout:'border',border:false, items:[");
		if (map.get(0).size() > 0) {
			buf.append(
					renderFormModuleList(
							customizationId,
							xlocale,
							formResult.getUniqueId(),
							map.get(0),
							"{xtype:'panel',region:'north',border:false,bodyStyle:'overflowY:auto',ssplit:true,autoHeight:!0,sheight:"
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
		buf.append(",{xtype:'tabpanel',id:_page_tab_id + '_").append(formResult.getFormId()).append("',cls:'iwb-detail-tab',region:'center',activeTab: 0, deferredRender:false,defaults:{bodyStyle:'padding:0px'}, items:[");// defaults:{autoHeight:true,
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
								&& (!FrameworkCache.roleAccessControl(
										formResult.getScd(),0) | !GenericUtil
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
							buf.append("Ext.apply(")
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
								&& (!FrameworkCache.roleAccessControl(
										formResult.getScd(),0) | !GenericUtil
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
									.append("(Ext.apply(")
									.append(gridResult.getGrid().getDsc())
									.append(",{border:false,bodyStyle:'',title:'")
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
						/*	String extra = "{layout:'form',title:'"
									+ LocaleMsgCache.get2(customizationId,
											xlocale, m.getLocaleMsgKey()) + "'";
							// if(formBodyColor!=null)extra+=",bodyStyle:'background-color: #"+formBodyColor+"'";
							if (formBodyStyle != null)
								extra += ",bodyStyle:'" + formBodyStyle + "'";
*/
							W5FormCellHelper extraInfo = getModulExtraInfo(
									(String) formResult.getScd().get("locale"),
									m.getLocaleMsgKey());
							if (extraInfo != null)
								map.get(m.getFormModuleId()).add(0, extraInfo);
							List<W5FormCellHelper> lfch = map.get(m.getFormModuleId());
							String extra = "{id:_page_tab_id+'_fm_"+m.getFormModuleId()+"',title:'"
									+ LocaleMsgCache.get2(customizationId,
											xlocale, m.getLocaleMsgKey()) + "'";
							if(lfch.size()==1 && lfch.get(0).getFormCell().getControlType()==41){
								extra+=",layout:'fit'";
							} else extra+=",layout:'form',bodyStyle:'min-height:550px;padding-top:10px;',autoScroll:true";
							
							buf.append(renderFormModuleList(customizationId,
									xlocale, formResult.getUniqueId(),
									lfch, extra));
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
		/* new Ext.grid.GridPanel(Ext.apply(detailGrid,grdExtra)) */
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
		String formBodyStyle = FrameworkCache.getAppSettingStringValue(
				formResult.getScd(), "form_body_style");
		
		StringBuilder buf = new StringBuilder();
		buf.append("mf=Ext.apply(mf,{xtype:'form',border:false,\nitems:[");
		boolean b = false;

		boolean snFlag = FrameworkCache.getAppSettingIntValue(customizationId, "form_navigation_flag")!=0 && map.size()>5 && formResult.getFormCellResults().size()>10;
		StringBuilder sn = new StringBuilder();
		if(snFlag) {
			sn.append("<nav class='secondary-navigation'><h3>")
				.append(LocaleMsgCache.get2(formResult.getScd(), "table_of_contents"))
				.append("</h3><ol>");
			if (map.get(0).size() > 0)sn.append("<li><a href='#")
			.append(formResult.getUniqueId())
			.append("_fm_0'>")
			.append(LocaleMsgCache.get2(formResult.getScd(), "top_of_form"))
			.append("</a></li>");
			buf.append("{xtype:'container',html:!mf._hideSecondaryNavigation ? mf._secondaryNavigation:''},");
			b = true;
		}

		if (formResult.getUniqueId() == null)
			formResult.setUniqueId(GenericUtil.getNextId("fi2"));
		List<String> extendedForms = new ArrayList();
		if (map.get(0).size() > 0) {
			buf.append(renderFormModuleList(customizationId, xlocale,
					formResult.getUniqueId(), map.get(0), "{id:_page_tab_id+'_fm_0',xtype:'fieldset'"
							+ (GenericUtil.isEmpty(formBodyStyle) ? ""
									: ",bodyStyle:'" + formBodyStyle + "'")));
			// (formBodyColor!=null ?
			// ",bodyStyle:'background-color: #"+formBodyColor+";background-image:url(../images/custom/bubble.png);background-repeat:no-repeat'"
			// : "")));
			b = true;
		}
		//secondaryNavigation

		
		if (formResult.getForm().get_moduleList() != null)
			for (W5FormModule m : formResult.getForm().get_moduleList())
				if (m.getFormModuleId() != 0) {
					if ((m.getModuleViewType() == 0 || formResult.getAction() == m
							.getModuleViewType())) {
						if(snFlag) {
							sn.append("<li><a href='#")
							.append(formResult.getUniqueId())
							.append("_fm_")
							.append(m.getFormModuleId())
							.append("'>")
							.append(LocaleMsgCache.get2(formResult.getScd(), m.getLocaleMsgKey()))
							.append("</a></li>");
						}						
						switch (m.getModuleType()) {
						case 4:// form
							if (GenericUtil.uInt(formResult.getRequestParams()
									.get("a")) == 5)
								break;
							W5FormResult subFormResult = formResult
									.getModuleFormMap().get(m.getObjectId());
							W5Table mainTablex = subFormResult != null
									&& subFormResult.getForm() != null ? FrameworkCache
									.getTable(formResult.getScd(), subFormResult
											.getForm().getObjectId()) : null;
							if (mainTablex != null
									&& (!FrameworkCache.roleAccessControl(
											formResult.getScd(), 0) | !GenericUtil
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
							break;
						case 5:// grid(edit)
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
									.getTable(formResult.getScd(), gridResult
											.getGrid().get_defaultCrudForm()
											.getObjectId()) : null;
							if (mainTable != null
									&& (!FrameworkCache.roleAccessControl(
											formResult.getScd(),0) | !GenericUtil
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
										.append(",{border:false,bodyStyle:'',title:'")
										.append(LocaleMsgCache.get2(
												customizationId, xlocale,
												m.getLocaleMsgKey()))
										.append("',height:")
										.append(gridResult.getGrid()
												.getDefaultHeight())
										.append(",cls:'x-fieldset-grid', autoScroll:true,clicksToEdit: 1*_app.edit_grid_clicks_to_edit}))");
							}
							break;
						default:
							if (!map.get(m.getFormModuleId()).isEmpty()) {
								if (b)
									buf.append(",");
								else
									b = true;
								String extra = "{id:_page_tab_id+'_fm_"+m.getFormModuleId()+"',xtype:'fieldset',labelWidth:mf._lb_"
										+ m.getFormModuleId()
										+ "||"
										+ formResult.getForm().getLabelWidth()
										+ ",";
								if (m.getModuleType() > 0)
									extra += "collapsible: true,";
								extra += "title:'"
										+ LocaleMsgCache.get2(customizationId,
												xlocale, m.getLocaleMsgKey())
										+ "'";
								// if(formBodyColor!=null)extra+=",bodyStyle:'background-color: #"+formBodyColor+"'";//default:
								// E4EAED
								if (formBodyStyle != null)
									extra += ",bodyStyle:'" + formBodyStyle
											+ "'";

								W5FormCellHelper extraInfo = getModulExtraInfo(
										(String) formResult.getScd().get(
												"locale"), m.getLocaleMsgKey());
								if (extraInfo != null)
									map.get(m.getFormModuleId()).add(0,
											extraInfo);
								buf.append(renderFormModuleList(
										customizationId, xlocale,
										formResult.getUniqueId(),
										map.get(m.getFormModuleId()), extra));
							}
						}
					}
				}
		buf.append("]}");

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
		
		if(snFlag) {
			sn.append("</ol></nav>");
			StringBuilder nbuf = new StringBuilder();
			nbuf.append("\nmf._secondaryNavigation=\"").append(sn).append("\";\n").append(buf);
			
			buf = nbuf;
			
		}
		return buf;
	}

	private StringBuilder renderFormModuleList(int customizationId,
			String xlocale, String formUniqueId,
			List<W5FormCellHelper> formCells, String xtype) {
		StringBuilder buf = new StringBuilder();
		// if(xtype!=null)buf.append("{frame:true,xtype:'").append(xtype).append("'");
		buf.append(xtype);
		int lc = 0;
		int[] maxWidths = new int[100];
		for (W5FormCellHelper fc : formCells)if (fc.getFormCell().getActiveFlag() != 0) {
			int columnOrder = fc.getFormCell().getTabOrder() / 1000;
			int w = fc.getFormCell().getControlWidth();
			if(w<0) w=300;
			maxWidths[columnOrder] = Math.max(w, maxWidths[columnOrder]);
			lc = Math.max(lc, columnOrder);
		}
		if (lc == 0) {// hersey duz
			buf.append(",\nitems:[");// ,\nautoHeight:false
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
								"{xtype:'compositefield',id:'cf_"
										+ fc.getFormCell().getDsc() + "_"
										+ formUniqueId
										+ "',labelSeparator:'',fieldLabel: _")
								.append(fc.getFormCell().getDsc())
								.append(".fieldLabel,  msgTarget: 'side', anchor: '-20', defaults: { flex: 1 }");
						
						/*if (fc.getFormCell().getExtraDefinition() != null
								&& fc.getFormCell().getExtraDefinition().length() > 1) {
							buf.append(fc.getFormCell().getExtraDefinition());
						}*/
						
						buf.append(", items: [_").append(fc.getFormCell().getDsc());
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
			buf.append(",\nlayout:'column',\nitems:[");
			StringBuilder columnBuf = new StringBuilder();
			boolean b = false;
			int columnOrder = -1;
			for (int i = 0; i < formCells.size(); i++) {
				W5FormCellHelper fc = formCells.get(i);
				if (fc.getFormCell().getActiveFlag() == 0)
					continue;
				if (fc.getFormCell().getControlType() != 0) {
					if (fc.getFormCell().getTabOrder() / 1000 != columnOrder) {
						columnOrder = fc.getFormCell().getTabOrder() / 1000;
						if (columnBuf.length() > 0) {
							buf.append(columnBuf.append("]},"));
							columnBuf.setLength(0);
						}
						int columnWidth = Math.max(maxWidths[columnOrder], 200) + 150;
						columnBuf.append("{layout:'form',border:false,minW:").append(columnWidth).append(",style:'min-width:").append(columnWidth).append("px;max-width:").append(200+columnWidth).append("px;',columnWidth:")
								.append(1.0 / (lc + 1)).append(",items:[");
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
								.append("{xtype:'compositefield',id:'cf_"
										+ fc.getFormCell().getDsc() + "_"
										+ formUniqueId + "',fieldLabel: _")
								.append(fc.getFormCell().getDsc())
								.append(".fieldLabel,  msgTarget: 'side', anchor: '-20', defaults: { flex: 1 }");
						
						columnBuf.append(", items: [");
						
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
		buf.append("}");
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
		
		if(fc.getControlType()==99){//grid
			W5Grid g = (W5Grid)fc.get_sourceObjectDetail();
			buf.append("new Ext.grid.EditorGridPanel(Ext.apply(").append(g.getDsc())
				.append(",{title:'").append(LocaleMsgCache.get2(customizationId, xlocale, fc.getLocaleMsgKey()))
				.append("',width:").append(fc.getControlWidth())
				.append(",height:").append(g.getDefaultHeight());
				if (fc.getExtraDefinition() != null
						&& fc.getExtraDefinition().length() > 1) {
					buf.append(fc.getExtraDefinition());
				}
			buf.append(",autoScroll:true,clicksToEdit: 1*_app.edit_grid_clicks_to_edit}))");
			return buf;
		}
		
		buf.append("new Ext.form.");
		String fieldLabel = LocaleMsgCache.get2(customizationId, xlocale,
				fc.getLocaleMsgKey());
		if (fc.getControlType() == 102) {// displayInfo(label)
			buf.append("Label({hideLabel:true,text:'")
					.append(cellResult.getValue()).append("'").append(",cls:'")
					.append(FrameworkSetting.labelMap[fc.getLookupQueryId()])
					.append("'");
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1) {
				buf.append(fc.getExtraDefinition());
			}
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			return buf.append("})");
		}
		if ((fc.getControlType() == 101 || cellResult.getHiddenValue() != null)/* && (fc.getControlTip()!=9 && fc.getControlTip()!=16) */) {
			buf.append(
					"DisplayField({labelSeparator:'', _controlTip:101, fieldLabel:'")
					.append(fieldLabel).append("'")
					.append(", id:_page_tab_id+'")
					.append(cellResult.getFormCell().getDsc()).append("'");
			if (fc.getControlType() == 5) {
				return buf.append(",value:'<img src=\"../images/custom/")
						.append(GenericUtil.uInt(value) != 0 ? "" : "un")
						.append("checked_big.gif\" border=0>',hiddenValue:")
						.append(GenericUtil.uInt(value) != 0 ? "true" : "false")
						.append("})");
			}
			if (fc.getControlWidth() > 0)
				buf.append(",width:").append(fc.getControlWidth());
			else if (fc.getControlWidth() < 0)
				buf.append(",anchor:'%").append(-fc.getControlWidth())
						.append("'");
			buf.append(",value:'");
			switch (fc.getControlType()) {// htmlEditor
			case 12:
				String controlValue = value != null ? value.toLowerCase() : "";
				if (value != null) {
					if ((controlValue.indexOf("<b") > -1
							|| controlValue.indexOf("<p") > -1
							|| controlValue.indexOf("<div") > -1 || controlValue
							.indexOf("<span") > -1))
						buf.append(GenericUtil.stringToJS(value));
					else
						buf.append(GenericUtil.stringToHtml(value));
				}
				break;
			case 56:
				controlValue = value != null ? value.toLowerCase() : "";
				if (value != null) {
					if ((controlValue.indexOf("<b") > -1
							|| controlValue.indexOf("<p") > -1
							|| controlValue.indexOf("<div") > -1 || controlValue
							.indexOf("<span") > -1)) {
						value = new HtmlFilter().filter(value);
						value = GenericUtil.stringToJS(value);
						buf.append(value);
					} else {
						value = new HtmlFilter().filter(value);
						value = GenericUtil.stringToHtml(value);
						buf.append(value);
					}

				}
				break;

			case 101:
				buf.append(value == null ? fieldLabel : "<b>" + value + "</b>");
				break;
			case	7:case	10:
				if(FrameworkSetting.lookupEditFormFlag && cellResult.getLookupQueryResult()!=null){
					W5Table t = cellResult.getLookupQueryResult().getMainTable();
					if(t!=null && t.getTableId()!=336/*w5_user*/ && ((t.getDefaultUpdateFormId()!=0 || cellResult.getFormCell().getLookupEditFormId()!=0) && t.getAccessUpdateUserFields()==null || GenericUtil.accessControl(formResult.getScd(), t.getAccessUpdateTip(), t.getAccessUpdateRoles(),t.getAccessUpdateUsers()))){
						int editFormId = cellResult.getFormCell().getLookupEditFormId()!=0 ? cellResult.getFormCell().getLookupEditFormId():t.getDefaultUpdateFormId();
						buf.append("<a href=# style=\"font-weight: bold;text-decoration: none;border-bottom: 1px dotted;\" onclick=\"mainPanel.loadTab({attributes:{modalWindow:true,href:\\'showForm?a=1&_fid="
					+editFormId+"&_tb_id="+t.getTableId()+"&_tb_pk="+cellResult.getHiddenValue()+"\\'}});\">").append(GenericUtil.stringToHtml(value))
						.append("</a>");
						break;
					}
				}
			default:
				buf.append("<b>").append(GenericUtil.stringToHtml(value))
						.append("</b>");
			}
			buf.append("',hiddenValue:'")
					.append(fc.getControlType() == 101 ? GenericUtil
							.stringToJS(value)
							: (cellResult.getHiddenValue() != null
									&& cellResult.getHiddenValue().equals("_") ? ""
									: cellResult.getHiddenValue())).append("'");
			if ((fc.getControlType() == 101 || fc.getControlType() == 12)
					&& fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1) {
				buf.append(fc.getExtraDefinition());
			}
			if (fc.getControlType() == 9 && formResult != null
					&& fc.getLookupIncludedParams() != null
					&& fc.getLookupIncludedParams().length() > 0
					&& fc.getParentFormCellId() > 0) {
				for (W5FormCellHelper rfc : formResult.getFormCellResults()) {
					if (rfc.getFormCell().getFormCellId() == fc
							.getParentFormCellId()) {
						W5FormCell pfc = rfc.getFormCell();
						if (pfc.getControlType() == 6
								|| pfc.getControlType() == 7
								|| pfc.getControlType() == 9
								|| pfc.getControlType() == 10)
							buf.append(
									",listeners:{render:function(){combo2combo(_")
									.append(pfc.getDsc()).append(",_")
									.append(fc.getDsc())
									.append(",function(ax,bx){\n")
									.append(fc.getLookupIncludedParams())
									.append("\n},__action__)}}");
						break;
					}
				}
			}
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			return buf.append("})");
		}
		// f.get_formCellHelp().get(xlocale).get(fc.getFormCellId()) TODO:eralp

		W5Form form = formResult == null ? null : formResult.getForm();

		boolean notNull = fc.getNotNullFlag() != 0;
		// if(notNull)fieldLabel+=" <span style=\"color:red;\">*</span>";
		if (fc.getLocaleMsgKey() != null) {
			if (notNull && fc.getControlType() != 5 && fc.getControlType() != 13)
				fieldLabel += "<i class=\"label-required\"></i>',ctCls:'required";
		} else {

		}
		int controlType = fc.getControlType();
		String cellDsc = fc.getDsc();
		boolean liveSyncRecord = FrameworkSetting.liveSyncRecord
				&& formResult != null && formResult.getForm() != null
				&& formResult.getForm().getObjectType() == 2
				&& formResult.getAction() == 1 && fc.getControlType()!=41;
		String liveSyncStr = null;
		if (liveSyncRecord) {
			W5Table t = FrameworkCache.getTable(formResult.getScd(), formResult
					.getForm().getObjectId());
			if (t != null && t.getLiveSyncFlag() != 0) {
				String s = ".t=" + formResult.getUniqueId() + "&.pk="
						+ formResult.getLiveSyncKey() + "&.f=" + cellDsc;
				liveSyncStr = "focus:function(aq){if(!aq || !aq.el)return;aq._oldValue=aq.el.dom.value;promisRequest({url:'ajaxLiveSync?.a=4&"
						+ s
						+ "', successResponse:function(){}});},blur:function(aq){if(!aq || !aq.el)return;promisRequest({url:'ajaxLiveSync?.a='+(safeIsEqual(aq._oldValue,aq.el.dom.value)?'6':'5')+'&"
						+ s
						+ "',params:{'.nv':aq.getValue()}, successResponse:function(){}});}";
			} else
				liveSyncRecord = false;

		}
		String uniqeId = fc != null && formResult != null
				&& formResult.getUniqueId() != null ? "id:'"
				+ formResult.getUniqueId() + "-" + fc.getFormCellId() + "',"
				: "";
		boolean fadd = false;
		switch (controlType) {
		case 100:// button
			buf.setLength(0);
			buf.append("new Ext.Button({_controlTip:100,cls:'x-btn-fc',width:").append(
					fc.getControlWidth());
			if(fc.getxOrder()==1)buf.append(",style:'margin-left:").append(formResult != null && formResult.getForm() != null?formResult.getForm().getLabelWidth()+5:175).append("px;margin-bottom:10px'");
			String text = "";
			fieldLabel = fc.getLocaleMsgKey();
			String icon = cellResult.getFormCell().getLookupIncludedParams();
			Map evm = cellResult.getExtraValuesMap();
			
			if (evm != null) {
				if (evm.get("xlabel") != null)
					fieldLabel = evm.get("xlabel").toString();
				if (evm.get("icon") != null)
					icon = evm.get("icon").toString();
				if (evm.get("value") != null)
					buf.append(",_value:'").append(evm.get("value")).append("'");
				if (evm.get("hidden") != null)
					buf.append(",hidden:").append(GenericUtil.uInt(evm.get("hidden"))!=0);
			}

			if(!GenericUtil.isEmpty(icon)) {
				if(!icon.startsWith("icon-"))icon="icon-"+icon;
				text = "<i class=\""+icon+"\"></i>";
				
			}
			if (fieldLabel.equals(".")) {
				if(GenericUtil.isEmpty(text))text="&nbsp;";
				buf.append(",text:'").append(text).append("'");
			} else if(fieldLabel.startsWith(".")) {
				if(GenericUtil.isEmpty(text))text="&nbsp;";
				buf.append(",text:'").append(text).append("', tooltip:'")
				.append(LocaleMsgCache.get2(customizationId, xlocale, fieldLabel.substring(1)))
				.append("'");;
			} else {
				if(!GenericUtil.isEmpty(text))text+=" &nbsp; "+fieldLabel;
				buf.append(",text:'").append(text).append("'");
				
			}

			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");

			buf.append(",handler:function(a,b,c){\nvar result=")
					.append(cellResult.getExtraValuesMap() != null ? GenericUtil
							.fromMapToJsonString(cellResult.getExtraValuesMap())
							: "false").append("\n");
			buf.append(fc.getExtraDefinition()).append("\n}})");
			return buf;
		case 0:// YOK, sadece degerini koy
			return new StringBuilder("'").append(GenericUtil.stringToJS(value))
					.append("'");
		case 10:// autocomplete select
			if (fc.getDialogGridId() != 0)
				buf.append("Dialog");
			else if (!notNull)
				buf.append("Clearable");
			if (formResult != null
					&& fc.getDialogGridId() == 0
					&& formResult.getForm() != null
					&& formResult.getScd() != null
					&& formResult.getForm().getObjectType() == 2
					&& FrameworkCache.getAppSettingIntValue(formResult.getScd(), "combobox_add_flag") != 0
					&& (GenericUtil.isEmpty(cellResult.getFormCell().getExtraDefinition()) || cellResult.getFormCell().getExtraDefinition().indexOf("noInsertForm")<0)
					&& ((cellResult.getLookupQueryResult() != null
							&& cellResult.getLookupQueryResult().getMainTable() != null
							&& cellResult.getLookupQueryResult().getMainTable().getTableId() != 336 /*w5_user*/
							&& fc.getLookupEditFormId() != 0
							&& FrameworkCache.roleAccessControl(
									formResult.getScd(), 2)
							&& GenericUtil.accessControl(formResult.getScd(),
									cellResult.getLookupQueryResult()
											.getMainTable().getAccessViewTip(),
									cellResult.getLookupQueryResult()
											.getMainTable()
											.getAccessViewRoles(), cellResult
											.getLookupQueryResult()
											.getMainTable()
											.getAccessViewUsers()) && GenericUtil
								.accessControl(formResult.getScd(), cellResult
										.getLookupQueryResult().getMainTable()
										.getAccessInsertTip(), cellResult
										.getLookupQueryResult().getMainTable()
										.getAccessInsertRoles(), cellResult
										.getLookupQueryResult().getMainTable()
										.getAccessInsertUsers())))) {
				buf.append("Add");
				fadd = true;
			}
			int maxRows = FrameworkCache.getAppSettingIntValue(0,
					"advanced_select_max_rows");
			if (maxRows == 0)
				maxRows = 100;
			if(FrameworkSetting.lookupEditFormFlag && formResult!=null && formResult.getForm().getObjectType()==2 && (controlType == 10 )
					&& cellResult.getLookupQueryResult() != null
					&& cellResult.getLookupQueryResult().getMainTable() != null && cellResult.getLookupQueryResult().getMainTable().getTableId() != 336 /*w5_user*/
					&& (cellResult.getFormCell().getLookupEditFormId()!=0 || cellResult.getLookupQueryResult().getMainTable().getDefaultUpdateFormId()!=0)
/*					&& PromisCache.roleAccessControl(formResult.getScd(), cellResult.getLookupQueryResult().getMainTable().getModuleId(), 1)
					&& PromisUtil.accessControl(formResult.getScd(), 
							cellResult.getLookupQueryResult().getMainTable().getAccessViewTip(), 
							cellResult.getLookupQueryResult().getMainTable().getAccessViewRoles(),
							cellResult.getLookupQueryResult().getMainTable().getAccessViewUsers()) 
					&& PromisUtil.accessControl(formResult.getScd(),
							cellResult.getLookupQueryResult().getMainTable().getAccessUpdateTip(),
							cellResult.getLookupQueryResult().getMainTable().getAccessUpdateRoles(),
							cellResult.getLookupQueryResult().getMainTable().getAccessUpdateUsers())*/){
                // e.HOME, e.END, e.PAGE_UP, e.PAGE_DOWN,
                // e.TAB, e.ESC, arrow keys: e.LEFT, e.RIGHT, e.UP, e.DOWN
				W5Table t = cellResult.getLookupQueryResult().getMainTable();
				if(t.getAccessUpdateUserFields()==null || GenericUtil.accessControl(formResult.getScd(), t.getAccessUpdateTip(), t.getAccessUpdateRoles(),t.getAccessUpdateUsers())){
					int editFormId = cellResult.getFormCell().getLookupEditFormId()!=0 ? cellResult.getFormCell().getLookupEditFormId():t.getDefaultUpdateFormId();
					String s ="specialkey: function(field, e){if (e.getKey() == e.ENTER) {if(field.getValue())mainPanel.loadTab({attributes:{modalWindow:true,href:'showForm?a=1&_fid="
					+editFormId+"&_tb_id="+t.getTableId()+"&_tb_pk='+field.getValue()}});else alert(getLocMsg('js_once_birseyler_secmelisiniz'))}}";
					liveSyncStr = (liveSyncStr!=null) ? s+","+liveSyncStr:s;
					if(fieldLabel.endsWith("ctCls:'required")){
						fieldLabel+="_edit_form";
					} else {
						fieldLabel += "',ctCls:'edit_form";
					}
				}
			}
			buf.append("ComboBox({")
					.append(uniqeId)
					.append("_controlTip:10,labelSeparator:'',fieldLabel: '")
					.append(fieldLabel)
					.append("',hiddenName: '")
					.append(cellDsc)
					.append("',\nstore: new Ext.data.JsonStore({url:'ajaxQueryData?_fdid=")
					.append(fc.getFormCellId())
					.append("&.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId+'&_qid=")
					.append(cellResult.getLookupQueryResult()!=null ? cellResult.getLookupQueryResult().getQueryId() : cellResult.getFormCell().getLookupQueryId())
					.append("&limit=").append(maxRows);
			if (FrameworkSetting.validateLookups && formResult != null)
				buf.append("&_fuid=").append(formResult.getUniqueId())
						.append("&_fcid=").append(fc.getFormCellId());
			if (fc.getLookupIncludedParams() != null
					&& fc.getLookupIncludedParams().length() > 2)
				buf.append("&").append(fc.getLookupIncludedParams());
			buf.append("',")
					.append(serializeQueryReader(cellResult.getLookupQueryResult().getQuery().get_queryFields(), "id", null, 0, null, null))
					.append(",listeners:{loadexception:promisLoadException}}),\nvalueField:'id',displayField:'dsc',typeAhead:false,mode:'remote',triggerAction:'query',queryParam:'xdsc',selectOnFocus:true");
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			if (fc.getExtraDefinition() != null && fc.getExtraDefinition().length() > 1) // ornegin,minChars:10 olabilir
				buf.append(fc.getExtraDefinition());
			else {
				int minChars = FrameworkCache.getAppSettingIntValue(0, "advanced_select_min_chars");
				if (minChars > 0)
					buf.append(",\nminChars:").append(minChars);// PromisSetting.appSettings.get("advanced_select_min_chars")
			}
			

			// if(value!=null && cellResult.getLookupQueryResult()!=null &&
			// cellResult.getLookupQueryResult().getData().size()>0){
			// buf.append(",valueNotFoundText:'").append(PromisUtil.stringToJS(cellResult.getLookupQueryResult().getData().get(0)[0].toString())).append("'");
			if (value != null && cellResult.getLookupQueryResult() != null
					&& !GenericUtil.isEmpty(cellResult.getLookupQueryResult().getData())) {
				Object[] oo = cellResult.getLookupQueryResult().getData()
						.get(0);
				buf.append(
						",listeners:{render:function(a){a.store.loadData({browseInfo:{},data:[{id:'")
						.append(GenericUtil.stringToJS(value)).append("',dsc:'")
						.append(GenericUtil.stringToJS(oo[0].toString()))
						.append("'");
				if (oo.length > 2)
					for (int io = 2; io < oo.length; io++) {
						buf.append(",")
								.append(cellResult.getLookupQueryResult().getQuery().get_queryFields().get(io).getDsc())
								.append(":'")
								.append(oo[io] != null && oo[io] instanceof String ? GenericUtil.stringToJS((String) oo[io]) : oo[io])
								.append("'");
					}
				buf.append("}]})}");
				if (liveSyncStr != null) {
					buf.append(",").append(liveSyncStr);
					liveSyncStr = null;
				}
				buf.append("}");
			} else if (FrameworkSetting.advancedSelectShowEmptyText) {
				buf.append(",emptyText:'")
						.append(LocaleMsgCache.get2(0, xlocale,
								"autocomplete_placeholder")).append("'");// Birşeyler
																				// Yaz...
			}
			if (fc.getControlWidth() > 0)
				buf.append(",width:").append(fc.getControlWidth());
			if (value != null && value.length() > 0)
				buf.append(",value:'").append(GenericUtil.stringToJS(value))
						.append("'");
			if (fc.getNrdType() != 0)
				buf.append(",disabled:true");
			if (notNull)
				buf.append(",allowBlank:false");
			if (liveSyncStr != null)
				buf.append(",listeners:{").append(liveSyncStr).append("}");
			if (fadd
					&& notNull
					&& (fc.getExtraDefinition() == null || (fc
							.getExtraDefinition() != null && fc
							.getExtraDefinition().indexOf("onTrigger2Click") < 0))) {
				buf.append(",onTrigger2Click:function(a,b,c){");
				buf.append(
						"mainPanel.loadTab({attributes:{_formCell:mf._").append(fc.getDsc()).append(",modalWindow:true,href:'showForm?a=2&_fid=")
						.append(fc.getLookupEditFormId())
						.append("'}});");
				buf.append("}");
			} else if (fadd
					&& !notNull
					&& (fc.getExtraDefinition() == null || (fc
							.getExtraDefinition() != null && fc
							.getExtraDefinition().indexOf("onTrigger3Click") < 0))) {
				buf.append(",onTrigger3Click:function(a,b,c){");
				buf.append(
						"mainPanel.loadTab({attributes:{_formCell:mf._").append(fc.getDsc()).append(",modalWindow:true,href:'showForm?a=2&_fid=")
						.append(fc.getLookupEditFormId())
						.append("'}});");
				buf.append("}");
			}
			if (notNull
					&& fc.getDialogGridId() != 0
					&& (GenericUtil.isEmpty(fc.getExtraDefinition()) || fc
							.getExtraDefinition().indexOf("onTrigger2Click") < 0)) {
				buf.append(
						",onTrigger2Click:function(a,b,c){mainPanel.loadTab({attributes:{modalWindow:true,href:'showPage?_tid=178&_gid1=")
						.append(fc.getDialogGridId())
						.append("',buttons:[{text:'").append(LocaleMsgCache.get2(customizationId, xlocale, "select")).append("',handler:function(a,b){if(!a._grid || !a._grid.getSelectionModel().getSelected()){alert('Önce birşeyler seçiniz');return;}\n_")
						.append(cellDsc)
						.append(".setValue(a._grid.getSelectionModel().getSelected().id);_")
						.append(cellDsc)
						.append(".fireEvent('select');mainPanel.closeModalWindow();_")
						.append(cellDsc).append(".store.reload({params:{xid:_")
						.append(cellDsc)
						.append(".getValue()},callback:function(x,y,z){_")
						.append(cellDsc).append(".fireEvent('select',_")
						.append(cellDsc).append(",{data:{id:_").append(cellDsc)
						.append(".getValue()}},0);_").append(cellDsc)
						.append(".setValue(_").append(cellDsc)
						.append(".getValue());}});}}");
				if (!notNull)
					buf.append(
							",{text:'").append(LocaleMsgCache.get2(customizationId, xlocale, "clear_selection")).append("',handler:function(a,b){_")
							.append(cellDsc)
							.append(".setValue('');mainPanel.closeModalWindow();}}");
				buf.append("],value:_").append(cellDsc)
						.append(".getValue()}})}");
			} else if (!notNull
					&& fc.getDialogGridId() != 0
					&& (GenericUtil.isEmpty(fc.getExtraDefinition()) || fc
							.getExtraDefinition().indexOf("onTrigger3Click") < 0)) {
				buf.append(
						",onTrigger3Click:function(a,b,c){mainPanel.loadTab({attributes:{modalWindow:true,href:'showPage?_tid=178&_gid1=")
						.append(fc.getDialogGridId())
						.append("',buttons:[{text:'").append(LocaleMsgCache.get2(customizationId, xlocale, "select")).append("',handler:function(a,b){if(!a._grid || !a._grid.getSelectionModel().getSelected()){alert('Önce birşeyler seçiniz');return;}\n_")
						.append(cellDsc)
						.append(".setValue(a._grid.getSelectionModel().getSelected().id);_")
						.append(cellDsc)
						.append(".fireEvent('select');mainPanel.closeModalWindow();_")
						.append(cellDsc).append(".store.reload({params:{xid:_")
						.append(cellDsc)
						.append(".getValue()},callback:function(x,y,z){_")
						.append(cellDsc).append(".fireEvent('select',_")
						.append(cellDsc).append(",{data:{id:_").append(cellDsc)
						.append(".getValue()}},0);_").append(cellDsc)
						.append(".setValue(_").append(cellDsc)
						.append(".getValue());}});}}");
				if (!notNull)
					buf.append(
							",{text:'").append(LocaleMsgCache.get2(customizationId, xlocale, "clear_selection")).append("',handler:function(a,b){_")
							.append(cellDsc)
							.append(".setValue('');mainPanel.closeModalWindow();}}");
				buf.append("],value:_").append(cellDsc)
						.append(".getValue()}})}");
			}
			if (liveSyncStr != null)
				buf.append(",listeners:{").append(liveSyncStr).append("}");
			return buf.append("})");
		case 5:
			buf.append(
					"Checkbox({labelSeparator:'',_controlTip:5,value:1,checked:")
					.append(GenericUtil.uInt(value) != 0);
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			if (GenericUtil.uInt(fc.getLookupIncludedParams()) == 0)
				buf.append(",width:").append(fc.getControlWidth());
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1) // ornegin
																// ,tooltip:'ali'
																// gibi
				buf.append(fc.getExtraDefinition());
			return buf
					.append(GenericUtil.uInt(fc.getLookupIncludedParams()) != 0 || fc.getParentFormCellId()==2 ? ",fieldLabel:'',boxLabel:'"
							: ",fieldLabel:'").append(fieldLabel)
					.append("',name: '").append(cellDsc).append("'})");
		case	25://edit-textarea (ozel kodlama)
			if (fc.get_sourceObjectDetail() != null
			&& fc.get_sourceObjectDetail() instanceof W5TableField) {
				buf.append("ComboBoxMulti({")
						.append(uniqeId)
						.append("_controlTip:25,grow: true, preventScrollbars: false, labelSeparator:'',fieldLabel: '")
						.append(fieldLabel)
						.append("',\nstore: new Ext.data.JsonStore({url:'ajaxQueryData?_fdid=")
						.append(fc.getFormCellId())
						.append("&.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId+'&_qid=1075&xtable_field_id=")
						.append(fc.getObjectDetailId())
						.append("',root:'data',totalProperty:'browseInfo.totalCount',id:'dsc',fields:[{name:'dsc'}],listeners:{loadexception:promisLoadException}})")
						.append(",displayField:'dsc',forceSelection:false,typeAhead: false, loadingText: '").append(LocaleMsgCache.get2(customizationId, xlocale, "searching")).append("...',hideTrigger:true,queryParam:'xdsc',name:'")
						.append(cellDsc).append("'");
				if(formResult!=null && formResult.getUniqueId()!=null)
					buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
				if (value != null && value.length() > 0)
					buf.append(",value:'").append(GenericUtil.stringToJS(value))
							.append("'");
				if (notNull)
					buf.append(",allowBlank:false");
				if (fc.getExtraDefinition() != null
						&& fc.getExtraDefinition().length() > 1) // ornegin
																	// ,minChars:10
																	// olabilir
					buf.append(fc.getExtraDefinition());
				else {
					int minChars = FrameworkCache.getAppSettingIntValue(0,
							"advanced_select_min_chars");
					if (minChars > 0)
						buf.append(",\nminChars:").append(minChars);// PromisSetting.appSettings.get("advanced_select_min_chars")
				}
				if (fc.getControlWidth() > 0)
					buf.append(",width:").append(fc.getControlWidth());
				else if (fc.getControlWidth() < 0)
					buf.append(",anchor:'%").append(-fc.getControlWidth()).append("'");
				if (liveSyncStr != null)
					buf.append(",listeners:{").append(liveSyncStr).append("}");
				return buf.append("})");
				/*
				 * width: 570, pageSize:10, hideTrigger:true, tpl: resultTpl,
				 * itemSelector: 'div.search-item', onSelect: function(record){
				 * // override default onSelect to do redirect window.location =
				 * String
				 * .format('http://extjs.com/forum/showthread.php?t={0}&p={1}',
				 * record.data.topicId, record.id); } });
				 */
			} else
				buf.append("TextArea");
			break;
		case 19:// edit-string(ozel kodlama)
			if (fc.get_sourceObjectDetail() != null
					&& fc.get_sourceObjectDetail() instanceof W5TableField) {
				buf.append("ComboBox({")
						.append(uniqeId)
						.append("_controlTip:19,labelSeparator:'',fieldLabel: '")
						.append(fieldLabel)
						.append("',\nstore: new Ext.data.JsonStore({url:'ajaxQueryData?_fdid=")
						.append(fc.getFormCellId())
						.append("&.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId+'&_qid=1075&xtable_field_id=")
						.append(fc.getObjectDetailId())
						.append("',root:'data',totalProperty:'browseInfo.totalCount',id:'dsc',fields:[{name:'dsc'}],listeners:{loadexception:promisLoadException}})")
						.append(",displayField:'dsc',forceSelection:false,typeAhead: false, loadingText: '").append(LocaleMsgCache.get2(customizationId, xlocale, "searching")).append("...',hideTrigger:true,queryParam:'xdsc',name:'")
						.append(cellDsc).append("'");
				if (value != null && value.length() > 0)
					buf.append(",value:'").append(GenericUtil.stringToJS(value))
							.append("'");
				if (notNull)
					buf.append(",allowBlank:false");
				if (fc.getExtraDefinition() != null
						&& fc.getExtraDefinition().length() > 1) // ornegin
																	// ,minChars:10
																	// olabilir
					buf.append(fc.getExtraDefinition());
				else {
					int minChars = FrameworkCache.getAppSettingIntValue(0,
							"advanced_select_min_chars");
					if (minChars > 0)
						buf.append(",\nminChars:").append(minChars);// PromisSetting.appSettings.get("advanced_select_min_chars")
				}
				if (fc.getControlWidth() > 0)
					buf.append(",width:").append(fc.getControlWidth());
				else if (fc.getControlWidth() < 0)
					buf.append(",anchor:'%").append(-fc.getControlWidth()).append("'");
				if (liveSyncStr != null)
					buf.append(",listeners:{").append(liveSyncStr).append("}");
				return buf.append("})");
				/*
				 * width: 570, pageSize:10, hideTrigger:true, tpl: resultTpl,
				 * itemSelector: 'div.search-item', onSelect: function(record){
				 * // override default onSelect to do redirect window.location =
				 * String
				 * .format('http://extjs.com/forum/showthread.php?t={0}&p={1}',
				 * record.data.topicId, record.id); } });
				 */
			}
			// Prosedürlerin parametreleri için de yapılmalı, store ve
			// queryParam eksik, oluşan alanın extra koduna yazılmalı
			// store: new
			// Ext.data.JsonStore({url:'ajaxQueryData?_fdid=").append(fc.getFormCellId()).append("&.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId+'&_qid=1075',root:'data',totalProperty:'browseInfo.totalCount',id:'dsc',fields:[{name:'dsc'}],listeners:{loadexception:promisLoadException}}),queryParam:'xdsc'
			// gibi
			if (fc.get_sourceObjectDetail() != null
					&& fc.get_sourceObjectDetail() instanceof W5GlobalFuncParam) {
				buf.append("ComboBox({")
						.append(uniqeId)
						.append("_controlTip:19,labelSeparator:'',fieldLabel: '")
						.append(fieldLabel)
						.append("',\ndisplayField:'dsc',forceSelection:false,typeAhead: false, loadingText: '").append(LocaleMsgCache.get2(customizationId, xlocale, "searching")).append("...',hideTrigger:true,name:'")
						.append(cellDsc).append("'");
				if (value != null && value.length() > 0)
					buf.append(",value:'").append(GenericUtil.stringToJS(value))
							.append("'");
				if (notNull)
					buf.append(",allowBlank:false");
				if (fc.getExtraDefinition() != null
						&& fc.getExtraDefinition().length() > 1) // ornegin
																	// ,minChars:10
																	// olabilir
					buf.append(fc.getExtraDefinition());
				else {
					int minChars = FrameworkCache.getAppSettingIntValue(0,
							"advanced_select_min_chars");
					if (minChars > 0)
						buf.append(",\nminChars:").append(minChars);// PromisSetting.appSettings.get("advanced_select_min_chars")
				}
				if (fc.getControlWidth() > 0)
					buf.append(",width:").append(fc.getControlWidth());
				else if (fc.getControlWidth() < 0)
					buf.append(",anchor:'%").append(-fc.getControlWidth()).append("'");				
				if (liveSyncStr != null)
					buf.append(",listeners:{").append(liveSyncStr).append("}");
				return buf.append("})");
			}
		case 42:// passWord
		case 21:// localeMsgKey
		case 1:// edit-string
			if (controlType == 21
					|| (cellDsc != null && cellDsc.equals("locale_msg_key")))
				buf.append("LocaleMsgKey");
			else
				buf.append("TextField");
			break;
		case 31:// codelama sistemi
			buf.append("FormCellCodeDetail");
			break;
		case 13:// hidden field
			buf.append("Hidden");
			break;
		case 17:// edit-string
			buf.append("TriggerField");
			break;// nerde kullaniliyor?
		case 2:// edit-date
			buf.append("DateField");
			break;
		case 18:// timestamp
			buf.setLength(0);
			buf.append("new Ext.ux.form.DateTime");
			break;
		case 22:// TimeField
			buf.append("TimeField");
			break;
		case 3:// edit-double
			buf.setLength(0);
			buf.append("new Ext.ux.NumericField");
			break;			
		case 4:// edit-integer
			buf.append("NumberField");
			break;
		case 33:// edit-integer
			buf.setLength(0);
			buf.append("new Ext.ux.NumericField");
			break;
		case 41:// edit js - codemirror
			buf.setLength(0);
			buf.append("new Ext.ux.form.Monaco");
			break;
		case 11: // textarea
			buf.append("TextArea");
			break;
		case 12: // HtmlEditor
/*			buf.append("HtmlEditor");
			break; */
		case 56: // HtmlEditor_ck
			buf.setLength(0);
			buf.append("new Ext.ux.form.CKEditor5");
			break;
		case 16:// lovCombo-remote
			buf.setLength(0);
			buf.append("new Ext.ux.form.LovCombo({")
					.append(uniqeId)
					.append("_controlTip:16,labelSeparator:'',fieldLabel:'")
					.append(fieldLabel)
					.append("',hiddenName: '")
					.append(cellDsc)
					.append("',\nstore: new Ext.data.JsonStore({url:'ajaxQueryData?_fdid=")
					.append(fc.getFormCellId())
					.append("&.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId");
			if (FrameworkSetting.validateLookups && formResult != null)
				buf.append("+'&_fuid=").append(formResult.getUniqueId())
						.append("&_fcid=").append(fc.getFormCellId())
						.append("'");
			buf.append(",")
					.append(cellResult.getLookupQueryResult() == null ? toDefaultLookupQueryReader()
							: serializeQueryReader(cellResult
									.getLookupQueryResult().getQuery()
									.get_queryFields(), "id", null, 0,
									null, null))
					.append(",listeners:{loadexception:promisLoadException}}),\nvalueField:'id',displayField:'dsc',mode: 'local',triggerAction: 'all'");
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			if (fc.getControlWidth() > 0)
				buf.append(",width:").append(fc.getControlWidth());
			if (value != null && value.length() > 0) {
				// buf.append(",value:'").append(PromisUtil.stringToJS(value)).append("'");//gerek
				// var mi bu satira?
				buf.append(",_oldValue:'").append(GenericUtil.stringToJS(value))
						.append("'");
			}
			if (fc.getNrdType() != 0)
				buf.append(",disabled:true");
			if (notNull)
				buf.append(",allowBlank:false");
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1) // ornegin
																// ,tooltip:'ali'
																// gibi
				buf.append(fc.getExtraDefinition());
			if (cellResult.getHiddenValue() != null)
				buf.append(",\"readOnly\":true");
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
								|| pfc.getControlType() == 51
								|| pfc.getControlType() == 15) {
							buf.append(
									",listeners:{render:function(){combo2combo(_")
									.append(pfc.getDsc()).append(",_")
									.append(fc.getDsc())
									.append(",function(ax,bx){\n")
									.append(fc.getLookupIncludedParams())
									.append("\n},__action__)}");
							if (liveSyncStr != null) {
								buf.append(",").append(liveSyncStr);
								liveSyncStr = null;
							}
							buf.append("}");
						}
						break;
					}
				}
			}
			if (liveSyncStr != null)
				buf.append(",listeners:{").append(liveSyncStr).append("}");
			return buf.append("})");
		case 60:// superboxselect-remote
			buf.setLength(0);
			buf.append("new Ext.ux.form.SuperBoxSelect({")
					.append(uniqeId)
					.append("_controlTip:60,labelSeparator:'',fieldLabel:'")
					.append(fieldLabel)
					.append("',hiddenName: '")
					.append(cellDsc)
					.append("',\nstore: new Ext.data.JsonStore({url:'ajaxQueryData?_fdid=")
					.append(fc.getFormCellId())
					.append("&.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId+'&_=_',")
					.append(cellResult.getLookupQueryResult() == null ? toDefaultLookupQueryReader()
							: serializeQueryReader(cellResult
									.getLookupQueryResult().getQuery()
									.get_queryFields(), "id", null, 0,
									null, null))
					.append(",listeners:{loadexception:promisLoadException}}),\nvalueField:'id',displayField:'dsc',mode:'local',queryParam:'xdsc'");
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			if (fc.getControlWidth() > 0)
				buf.append(",width:").append(fc.getControlWidth());
			if (value != null && value.length() > 0) {
				// buf.append(",value:'").append(PromisUtil.stringToJS(value)).append("'");//gerek
				// var mi bu satira?
				buf.append(",_oldValue:'").append(GenericUtil.stringToJS(value))
						.append("'");
			}
			if (fc.getNrdType() != 0)
				buf.append(",disabled:true");
			if (notNull)
				buf.append(",allowBlank:false");
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1) // ornegin
																// ,tooltip:'ali'
																// gibi
				buf.append(fc.getExtraDefinition());
			if (cellResult.getHiddenValue() != null)
				buf.append(",readOnly:true");
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
									",listeners:{render:function(){combo2combo(_")
									.append(pfc.getDsc()).append(",_")
									.append(fc.getDsc())
									.append(",function(ax,bx){\n")
									.append(fc.getLookupIncludedParams())
									.append("\n},__action__)}");
							if (liveSyncStr != null) {
								buf.append(",").append(liveSyncStr);
								liveSyncStr = null;
							}
							buf.append("}");
						}
						break;
					}
				}
			}
			return buf.append("})");
		case 14: //TODO. bu ne ????
			if (fc.getNrdType() == 0) {
				buf.append("Add");
			}
		case 9:// combo-query(remote)
				// if(fc.getNrdTip()==0 && !notNull &&
				// !addButton)buf.append("Clearable");
			if (!notNull)
				buf.append("Clearable");
			if (fc.getDialogGridId() != 0)
				buf.append("Add");
			buf.append("ComboBox({")
					.append(uniqeId)
					.append("_controlTip:")
					.append(controlType)
					.append(",labelSeparator:'',fieldLabel:'")
					.append(fieldLabel)
					.append("',hiddenName: '")
					.append(cellDsc)
					.append("',\nstore: new Ext.data.JsonStore({url:'");

			buf.append("ajaxQueryData?_fdid=")
					.append(fc.getFormCellId())
					.append("&.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId+'&_=_");
			if (FrameworkSetting.validateLookups && formResult != null)
				buf.append("&_fuid=").append(formResult.getUniqueId())
						.append("&_fcid=").append(fc.getFormCellId());
			buf.append("',").append(cellResult.getLookupQueryResult() == null ? toDefaultLookupQueryReader()
					: serializeQueryReader(cellResult
							.getLookupQueryResult().getQuery()
							.get_queryFields(), "id", null, 0,
							null, null))
			.append(",listeners:{loadexception:promisLoadException}}),\nvalueField:'id',displayField:'dsc',typeAhead: false,mode: 'local',triggerAction: 'all',selectOnFocus:true");
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			if (fc.getControlWidth() > 0)
				buf.append(",width:").append(fc.getControlWidth());
			if (value != null && value.length() > 0)
				buf.append(",value:'").append(GenericUtil.stringToJS(value))
						.append("'");
			if (fc.getNrdType() != 0)
				buf.append(",disabled:true");
			if (notNull)
				buf.append(",allowBlank:false");
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1) { // ornegin
																// ,tooltip:'ali'
																// gibi
				buf.append(fc.getExtraDefinition());
			}
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
									",listeners:{render:function(){combo2combo(_")
									.append(pfc.getDsc()).append(",_")
									.append(fc.getDsc())
									.append(",function(ax,bx){\n")
									.append(fc.getLookupIncludedParams())
									.append("\n},__action__)}");
							if (liveSyncStr != null) {
								buf.append(",").append(liveSyncStr);
								liveSyncStr = null;
							}
							buf.append("}");
						}
						break;
					}
				}
			}
			/*
			 * if(cellResult.getHiddenValue()!=null)buf.append(
			 * ",readOnly:true,disabled:true"); else
			 */
			if (fc.getDialogGridId() != 0
					&& (fc.getExtraDefinition() == null || (fc
							.getExtraDefinition() != null && fc
							.getExtraDefinition().indexOf("onTrigger2Click") < 0))) {
				W5FormCell pfc = null;
				if (formResult != null)
					for (W5FormCellHelper rfc : formResult.getFormCellResults())
						if (rfc.getFormCell().getFormCellId() == fc
								.getParentFormCellId()) {
							pfc = rfc.getFormCell();
							break;
						}
				buf.append(",onTrigger2Click:function(a,b,c){\n");
				if (pfc != null)
					buf.append("if(!(_")
							.append(pfc.getDsc())
							.append(".hiddenValue || _")
							.append(pfc.getDsc())
							.append(".getValue())){Ext.Msg.show({title: '"
									+ LocaleMsgCache.get2(customizationId,
											xlocale, "error").replaceAll(",",
											"-")
									+ "', msg: '"
									+ LocaleMsgCache
											.get2(customizationId, xlocale,
													"error_parent_record_must_be_selected")
											.replaceAll(",", "-")
									+ "', icon: Ext.MessageBox.ERROR});return false;};\n");
				buf.append("mainPanel.loadTab({attributes:{id:'tb_gwt")
						.append(fc.getDialogGridId())
						.append("',href:'showForm?a=2&_fid=")
						.append(fc.getLookupEditFormId());
				if (pfc != null)
					buf.append("&i").append(pfc.getDsc()).append("='+(_")
							.append(pfc.getDsc()).append(".hiddenValue || _")
							.append(pfc.getDsc()).append(".getValue())");
				else
					buf.append("'");
				buf.append(",_grid:{ds:this.store}}})}");
			}

			return buf.append("})");
		case 15:
		case 8:
		case 52: // lovcombo-query(15:query+8:static), userdefined lov(52)
			if(formResult != null && fc.getParentFormCellId()==1) {
				buf.append("CheckboxGroup({").append(uniqeId).append("_controlTip:")
					.append(controlType)
					.append(",_checkbox:!0, labelSeparator:'',columns: 2, fieldLabel: '")
					.append(fieldLabel).append("',items: [");
				
				boolean b = false;
				if (cellResult.getLookupListValues() != null) { // cell LookUp'tan
					for (W5Detay p : (List<W5Detay>) cellResult
							.getLookupListValues()) {
						if (b)
							buf.append(",");
						else
							b = true;
						buf.append("{inputValue:'")
								.append(p.getVal())
								.append("',name:'")
								.append(fc.getDsc())
								.append("',boxLabel:'")
								.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache
										.get2(customizationId, xlocale, p.getDsc())
										.replaceAll(",", "-") : p.getDsc()
										.replaceAll(",", "-")).append("'");
						if(!GenericUtil.isEmpty(cellResult.getValue()) 
								&& GenericUtil.hasPartInside2(cellResult.getValue(), p.getVal()))
							buf.append(",checked:true");
						buf.append("}");
					}
				} else if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan
					b = false;
					if (cellResult.getLookupQueryResult().getData() != null)
						for (Object[] p : cellResult.getLookupQueryResult().getData()) {
							if (b)
								buf.append(",");
							else
								b = true;
							buf.append("{name:'").append(fc.getDsc()).append("'");
							for (W5QueryField f : cellResult.getLookupQueryResult().getQuery().get_queryFields()) {
								
								Object z = p[f.getTabOrder() - 1];
								if(z==null)continue;								
								buf.append(",");
								if(f.getDsc().equals("dsc"))
									buf.append("boxLabel:");
								else if(f.getDsc().equals("id")) {
									if(!GenericUtil.isEmpty(cellResult.getValue()) 
											&& GenericUtil.hasPartInside2(cellResult.getValue(), z.toString()))
										buf.append("checked:true,");
									buf.append("inputValue:");
								}

								buf.append("'")
										.append(f.getPostProcessType() == 2 ? LocaleMsgCache
												.get2(customizationId, xlocale,
														z.toString()).replaceAll(
														",", "-") : GenericUtil
												.stringToJS(z.toString())
												.replaceAll(",", "-")).append("'");
							}
							buf.append("}");
						}
				}
				buf.append("]");
				if(!GenericUtil.isEmpty(cellResult.getValue()))
					buf.append(",value:'").append(cellResult.getValue()).append("'");
				if(fc.getControlWidth()>0)buf.append(",width:").append(fc.getControlWidth());
				if(!GenericUtil.isEmpty(fc.getExtraDefinition()))buf.append(fc.getExtraDefinition());
				if(formResult!=null && formResult.getUniqueId()!=null)
					buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
				buf.append("})");
				return buf;
				
				
			} else {
				buf.setLength(0);
				buf.append("new Ext.ux.form.");
				if (formResult != null
						&& fc.getDialogGridId() == 0
						&& formResult.getForm() != null
						&& formResult.getScd() != null
						&& formResult.getForm().getObjectType() == 2
						&& FrameworkCache.getAppSettingIntValue(formResult.getScd(),
								"combobox_add_flag") != 0
						&& ((controlType == 15
								&& cellResult.getLookupQueryResult() != null
								&& cellResult.getLookupQueryResult().getMainTable() != null
								&& cellResult.getLookupQueryResult().getMainTable().getTableId() != 336 /*w5_user*/
								&& fc.getLookupEditFormId()!=0
								&& (GenericUtil.isEmpty(cellResult.getFormCell().getExtraDefinition()) || cellResult.getFormCell().getExtraDefinition().indexOf("noInsertForm")<0)
								&& FrameworkCache.roleAccessControl(
										formResult.getScd(),  2)
								&& GenericUtil.accessControl(formResult.getScd(),
										cellResult.getLookupQueryResult()
												.getMainTable().getAccessViewTip(),
										cellResult.getLookupQueryResult()
												.getMainTable()
												.getAccessViewRoles(), cellResult
												.getLookupQueryResult()
												.getMainTable()
												.getAccessViewUsers()) && GenericUtil
									.accessControl(formResult.getScd(), cellResult
											.getLookupQueryResult().getMainTable()
											.getAccessInsertTip(), cellResult
											.getLookupQueryResult().getMainTable()
											.getAccessInsertRoles(), cellResult
											.getLookupQueryResult().getMainTable()
											.getAccessInsertUsers()))
								|| (controlType == 8
										&& cellResult.getLookupListValues() != null
										&& ((Integer) formResult.getScd().get(
												"roleId") == 0) && FrameworkCache
											.roleAccessControl(
													formResult.getScd(),
													 107)) || (controlType == 52 && FrameworkCache
								.roleAccessControl(
										formResult.getScd(),
										 107)))) {
					buf.append("Add");
					fadd = true;
				}
				if (fc.getDialogGridId() != 0)
					buf.append("Dialog");
				buf.append("LovCombo({").append(uniqeId).append("_controlTip:")
						.append(controlType)
						.append(",labelSeparator:'',fieldLabel: '")
						.append(fieldLabel).append("',hiddenName: '")
						.append(cellDsc).append("'");
				if (fc.getExtraDefinition() != null
						&& fc.getExtraDefinition().length() > 1) // ornegin
																	// ,tooltip:'ali'
																	// gibi
					buf.append(fc.getExtraDefinition());
				boolean b = false;
				if (cellResult.getLookupListValues() != null) { // cell LookUp'tan
																// geliyor
					buf.append(",\nstore:new Ext.data.SimpleStore({id:0,fields: ['id', 'dsc'");
					buf.append("],data : [");
					for (W5Detay p : (List<W5Detay>) cellResult
							.getLookupListValues()) {
						if (b)
							buf.append(",");
						else
							b = true;
						buf.append("['")
								.append(p.getVal())
								.append("','")
								.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache
										.get2(customizationId, xlocale, p.getDsc())
										.replaceAll(",", "-") : p.getDsc()
										.replaceAll(",", "-")).append("'");
						buf.append("]");
					}
				} else if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan
																		// geliyor
					buf.append(",\nstore:new Ext.data.SimpleStore({id:1,\nfields:[");
					b = false;
					for (W5QueryField f : cellResult.getLookupQueryResult()
							.getQuery().get_queryFields()) {
						if (b)
							buf.append(",");
						else
							b = true;
						buf.append("{name:'").append(f.getDsc()).append("'");
						if (f.getFieldType() > 2)
							buf.append(",type:'")
									.append(FrameworkSetting.sortMap[f.getFieldType()])
									.append("'");
						if (f.getFieldType() == 2)
							buf.append(",type:'date',dateFormat:'").append(dateFormatMulti[formResult.getScd()!=null ? GenericUtil.uInt(formResult.getScd().get("date_format")):0]).append(" h:i:s'");
						// if(f.getPostProcessTip()>=10)buf.append("},{name:'").append(f.getDsc()).append("_qw_'");
						buf.append("}");
					}
					buf.append("],\ndata:[");
					b = false;
					if (cellResult.getLookupQueryResult().getData() != null)
						for (Object[] p : cellResult.getLookupQueryResult()
								.getData()) {
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
								buf.append("'")
										.append(f.getPostProcessType() == 2 ? LocaleMsgCache
												.get2(customizationId, xlocale,
														z.toString()).replaceAll(
														",", "-") : GenericUtil
												.stringToJS(z.toString())
												.replaceAll(",", "-")).append("'");
							}
							buf.append("]");
						}
				}
				buf.append("]})")
						.append(",\nvalueField:'id',displayField:'dsc',hideOnSelect:false,mode: 'local',triggerAction: 'all'");
				if (fc.getControlWidth() > 0)
					buf.append(",width:").append(fc.getControlWidth());
				if (value != null && value.length() > 0)
					buf.append(",value:'").append(GenericUtil.stringToJS(value))
							.append("'");
				if (fc.getNrdType() != 0)
					buf.append(",disabled:true");
				if (fc.getVtype() != null && fc.getVtype().length() > 0)
					buf.append(",vtype:'").append(fc.getVtype()).append("'");
				
				if (liveSyncStr != null)
					buf.append(",listeners:{").append(liveSyncStr).append("}");
				if (fadd
						&& (fc.getExtraDefinition() == null || (fc
								.getExtraDefinition() != null && fc
								.getExtraDefinition().indexOf("onTrigger2Click") < 0))) {
					buf.append(",onTrigger2Click:function(a,b,c){");
					switch (controlType) {
					case 8:
						buf.append(
								"mainPanel.loadTab({attributes:{_formCell:mf._").append(fc.getDsc()).append(",modalWindow:true,href:'showForm?a=2&look_up_id=")
								.append(fc.getLookupQueryId())
								.append("&_fid=2190'}});");
						break;
					case 15:
						buf.append(
								"mainPanel.loadTab({attributes:{_formCell:mf._").append(fc.getDsc()).append(", modalWindow:true,href:'showForm?a=2&_fid=")
								.append(fc.getLookupEditFormId())
								.append("'}});");
						break;
					case 52:
						buf.append(
								"mainPanel.loadTab({attributes:{_formCell:mf._").append(fc.getDsc()).append(",modalWindow:true,href:'showForm?a=2&look_up_id=")
								.append(fc.getLookupQueryId())
								.append("&_fid=964'}});");
						break;
	
					}
					buf.append("}");
				} else if (!fadd && fc.getDialogGridId() != 0) {
					buf.append(
							",onTrigger2Click:function(a,b,c){mainPanel.loadTab({attributes:{modalWindow:true,href:'showPage?_tid=178&_gid1=")
							.append(fc.getDialogGridId())
							.append("',buttons:[{text:'Seç',handler:function(a,b){if(!a._grid || a._grid.getSelectionModel().getSelections().length == 0){alert('Önce birşeyler seçiniz');return;}\n var sels=a._grid.getSelectionModel().getSelections();var str='';for(var i=0;i<sels.length;i++){str+=sels[i].id+',';};str=str.substring(0,str.length-1);\n_")
							.append(cellDsc)
							.append(".setValue(str);_")
							.append(cellDsc)
							.append(".fireEvent('select');mainPanel.closeModalWindow();_")
							.append(cellDsc).append(".store.reload({params:{xid:_")
							.append(cellDsc)
							.append(".getValue()},callback:function(x,y,z){_")
							.append(cellDsc).append(".fireEvent('select',_")
							.append(cellDsc).append(",{data:{id:_").append(cellDsc)
							.append(".getValue()}},0);_").append(cellDsc)
							.append(".setValue(_").append(cellDsc)
							.append(".getValue());}});}}");
					if (!notNull)
						buf.append(
								",{text:'Seçimi Temizle',handler:function(a,b){_")
								.append(cellDsc)
								.append(".setValue('');mainPanel.closeModalWindow();}}");
					buf.append("],value:_").append(cellDsc)
							.append(".getValue()}})}");
				}
				if(formResult!=null && formResult.getUniqueId()!=null)
					buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
				return buf.append("})");
			}
		case 58:
		case 59: // SuperBoxSelect-query(59:query+58:static)
			buf.setLength(0);
			buf.append("new Ext.ux.form.SuperBoxSelect({").append(uniqeId)
					.append("_controlTip:").append(controlType)
					.append(",labelSeparator:'',fieldLabel: '")
					.append(fieldLabel).append("',hiddenName: '")
					.append(cellDsc).append("'");
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1) // ornegin
																// ,tooltip:'ali'
																// gibi
				buf.append(fc.getExtraDefinition());
			boolean b1 = false;
			if (cellResult.getLookupListValues() != null) { // cell LookUp'tan
															// geliyor
				buf.append(",\nstore:new Ext.data.SimpleStore({id:0,fields: ['id', 'dsc'");
				buf.append("],data : [");
				for (W5Detay p : (List<W5Detay>) cellResult
						.getLookupListValues()) {
					if (b1)
						buf.append(",");
					else
						b1 = true;
					buf.append("['")
							.append(p.getVal())
							.append("','")
							.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache
									.get2(customizationId, xlocale, p.getDsc())
									.replaceAll(",", "-") : p.getDsc()
									.replaceAll(",", "-")).append("'");
					buf.append("]");
				}
			} else if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan
																	// geliyor
				buf.append(",\nstore:new Ext.data.SimpleStore({id:1,\nfields:[");
				b1 = false;
				for (W5QueryField f : cellResult.getLookupQueryResult()
						.getQuery().get_queryFields()) {
					if (b1)
						buf.append(",");
					else
						b1 = true;
					buf.append("{name:'").append(f.getDsc()).append("'");
					if (f.getFieldType() > 2)
						buf.append(",type:'")
								.append(FrameworkSetting.sortMap[f.getFieldType()])
								.append("'");
					if (f.getFieldType() == 2)
						buf.append(",type:'date',dateFormat:'").append(dateFormatMulti[formResult.getScd()!=null ? GenericUtil.uInt(formResult.getScd().get("date_format")):0]).append(" h:i:s'");
					// if(f.getPostProcessTip()>=10)buf.append("},{name:'").append(f.getDsc()).append("_qw_'");
					buf.append("}");
				}
				buf.append("],\ndata:[");
				b1 = false;
				if (cellResult.getLookupQueryResult().getData() != null)
					for (Object[] p : cellResult.getLookupQueryResult()
							.getData()) {
						if (b1)
							buf.append(",");
						else
							b1 = true;
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
							buf.append("'")
									.append(f.getPostProcessType() == 2 ? LocaleMsgCache
											.get2(customizationId, xlocale,
													z.toString()).replaceAll(
													",", "-") : GenericUtil
											.stringToJS(z.toString())
											.replaceAll(",", "-")).append("'");
						}
						buf.append("]");
					}
			}
			buf.append("]\n})")
					.append(",\nvalueField:'id',displayField:'dsc',hideOnSelect:false,mode: 'local',triggerAction: 'all'");
			if (fc.getControlWidth() > 0)
				buf.append(",width:").append(fc.getControlWidth());
			if (value != null && value.length() > 0)
				buf.append(",value:'").append(GenericUtil.stringToJS(value))
						.append("'");
			if (fc.getNrdType() != 0)
				buf.append(",disabled:true");
			if (fc.getVtype() != null && fc.getVtype().length() > 0)
				buf.append(",vtype:'").append(fc.getVtype()).append("'");
			
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			return buf.append("})");
		case 62: // superboxselect-free-text
			buf.setLength(0);

			buf.append(" new Ext.ux.form.SuperBoxSelect({")
					.append(uniqeId)
					.append("_controlTip:")
					.append(controlType)
					.append(",labelSeparator:'',fieldLabel: '")
					.append(fieldLabel)
					.append("',hiddenName: '")
					.append(cellDsc)
					.append("'");

			buf.append(",allowAddNewData:true, \nstore: new Ext.data.SimpleStore({id:0,fields: ['dsc'],data : [");
			if(!GenericUtil.isEmpty(value)) {
				String[] values = value.split(",");
				for(int qi=0;qi<values.length;qi++)
					buf.append("['").append(GenericUtil.stringToJS(values[qi])).append("'],");
				buf.setLength(buf.length()-1);
				
			}
			buf.append("]}),valueField:'dsc',displayField:'dsc',hideOnSelect:false,triggerAction: 'all',typeAhead:false,mode:'local'");
		
			if (fc.getControlWidth() > 0)
				buf.append(",width:").append(fc.getControlWidth());
			if (value != null && value.length() > 0)
				buf.append(",value:'").append(GenericUtil.stringToJS(value))
						.append("'");
			if (fc.getNrdType() != 0)
				buf.append(",disabled:true");
			if (notNull)
				buf.append(",allowBlank:false");
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1) // ornegin
																// ,tooltip:'ali'
																// gibi
				buf.append(fc.getExtraDefinition());
			//if (liveSyncStr != null)buf.append(",listeners:{").append(liveSyncStr).append("}");
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			return buf.append("})");
		case 61: // superboxselect-combo-query-advanced
			buf.setLength(0);
			int maxRows1 = FrameworkCache.getAppSettingIntValue(0,
					"advanced_select_max_rows");
			if (maxRows1 == 0)
				maxRows1 = 100;
			buf.append(" new Ext.ux.form.SuperBoxSelect({")
					.append(uniqeId)
					.append("_controlTip:")
					.append(controlType)
					.append(",labelSeparator:'',fieldLabel: '")
					.append(fieldLabel)
					.append("',hiddenName: '")
					.append(cellDsc)
					.append("',\nstore: new Ext.data.JsonStore({url:'ajaxQueryData?_fdid=")
					.append(fc.getFormCellId())
					.append("&.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId+'&_qid=")
					.append(cellResult.getLookupQueryResult().getQueryId())
					.append("&limit=").append(maxRows1);
			if (FrameworkSetting.validateLookups && formResult != null)
				buf.append("&_fuid=").append(formResult.getUniqueId())
						.append("&_fcid=").append(fc.getFormCellId());
			if (fc.getLookupIncludedParams() != null
					&& fc.getLookupIncludedParams().length() > 2)
				buf.append("&").append(fc.getLookupIncludedParams());
			buf.append("',")
					.append(serializeQueryReader(cellResult
							.getLookupQueryResult().getQuery()
							.get_queryFields(), "id", null, 0, null, null))
					.append(",listeners:{loadexception:promisLoadException}}),\nvalueField:'id',displayField:'dsc',typeAhead:false,mode:'remote',triggerAction:'all',queryParam:'xdsc',selectOnFocus:true");
			int minChars = FrameworkCache.getAppSettingIntValue(0,
					"advanced_select_min_chars");
			if (minChars > 0)
				buf.append(",\nminChars:").append(minChars);// PromisSetting.appSettings.get("advanced_select_min_chars")
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1)
				buf.append(fc.getExtraDefinition());

			// if(value!=null && cellResult.getLookupQueryResult()!=null &&
			// cellResult.getLookupQueryResult().getData().size()>0){
			// buf.append(",valueNotFoundText:'").append(PromisUtil.stringToJS(cellResult.getLookupQueryResult().getData().get(0)[0].toString())).append("'");
			if (value != null && cellResult.getLookupQueryResult() != null
					&& !GenericUtil.isEmpty(cellResult.getLookupQueryResult().getData())) {
				Object[] oo = cellResult.getLookupQueryResult().getData()
						.get(0);
				buf.append(
						",listeners:{render:function(a){a.store.loadData({browseInfo:{},data:[{id:'")
						.append(GenericUtil.stringToJS(value)).append("',dsc:'")
						.append(GenericUtil.stringToJS(oo[0].toString()))
						.append("'");
				if (oo.length > 2)
					for (int io = 2; io < oo.length; io++) {
						buf.append(",")
								.append(cellResult.getLookupQueryResult()
										.getQuery().get_queryFields().get(io)
										.getDsc()).append(":'").append(oo[io])
								.append("'");
					}
				buf.append("}]})}");
				if (liveSyncStr != null) {
					buf.append(",").append(liveSyncStr);
					liveSyncStr = null;
				}
				buf.append("}");
			} else if (FrameworkSetting.advancedSelectShowEmptyText) {
				buf.append(",emptyText:'")
						.append(LocaleMsgCache.get2(0, xlocale,
								"autocomplete_placeholder")).append("'");// Birşeyler
																				// Yaz...
			}
			if (fc.getControlWidth() > 0)
				buf.append(",width:").append(fc.getControlWidth());
			if (value != null && value.length() > 0)
				buf.append(",value:'").append(GenericUtil.stringToJS(value))
						.append("'");
			if (fc.getNrdType() != 0)
				buf.append(",disabled:true");
			if (notNull)
				buf.append(",allowBlank:false");
			if (liveSyncStr != null)
				buf.append(",listeners:{").append(liveSyncStr).append("}");
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			return buf.append("})");
		case	26:// lov-treecombo (query lookup) local WRONG TODO
		case	23:// treecombo (query lookup) local
			buf.setLength(0);
			buf.append("new Ext.ux.TreeCombo({").append(uniqeId)
					.append("_controlTip:").append(controlType)
					.append(",labelSeparator:'',fieldLabel:'")
					.append(fieldLabel).append("',hiddenName: '")
					.append(cellDsc).append("'");
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1) // ornegin ,tooltip:'ali' gibi
				buf.append(fc.getExtraDefinition());
			if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan geliyor
				int qtip = cellResult.getLookupQueryResult().getQuery().getQueryType(); 
				if (qtip == 12 || qtip == 13) {// lookup tree query
					if (cellResult.getLookupQueryResult().getData() != null) {
						buf.append(",\nchildren:").append(
								serializeTreeQueryData(cellResult
										.getLookupQueryResult()));
						// if(value!=null &&
						// value.length()>0)buf.append(",listeners:{render:function(){this.setValue('").append(value).append("')}}");
					}
				} else {// TODO: hata olamaz, tree combo'da sadece queryType==12
						// icin olur
				}
			}
			// buf.append(",\nvalueField:'id',displayField:'dsc',typeAhead: false,mode: 'local',triggerAction: 'all',selectOnFocus:true,forceSelection:true");
			if (fc.getControlWidth() > 0)
				buf.append(",width:").append(fc.getControlWidth());
			if (value != null && value.length() > 0)
				buf.append(",value:'").append(GenericUtil.stringToJS(value))
						.append("'");
			if (fc.getNrdType() != 0)
				buf.append(",disabled:true");
			if (notNull)
				buf.append(",allowBlank:false");
			if (fc.getExtraDefinition() != null && fc.getExtraDefinition().length() > 1) // ornegin ,tooltip:'ali'  gibi
				buf.append(fc.getExtraDefinition());
			if (liveSyncStr != null)
				buf.append(",listeners:{").append(liveSyncStr).append("}");
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			return buf.append("})");
		case	71://file upload
			buf.setLength(0);
			buf.append("new Ext.ux.form.FileUploadField({fieldLabel:'").append(fieldLabel).append("', name: '").append(cellDsc).append("', buttonText: 'Browse'");
			if (fc.getControlWidth() > 0)buf.append(",width:").append(fc.getControlWidth());
			if (fc.getNrdType() != 0)buf.append(",disabled:true");
			if (notNull)buf.append(",allowBlank:false");
			if (fc.getExtraDefinition() != null && fc.getExtraDefinition().length() > 1)buf.append(fc.getExtraDefinition());
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			return buf.append("})");
		case 24:// treecombo (query lookup) remote
			buf.setLength(0);
			buf.append("new Ext.ux.TreeCombo({").append(uniqeId)
					.append("url: 'ajaxQueryData?_fdid=")
					.append(fc.getFormCellId())
					.append("&.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId+'&_qid=")
					.append(cellResult.getLookupQueryResult().getQueryId())
					.append("',_controlTip:").append(controlType)
					.append(",labelSeparator:'',fieldLabel:'")
					.append(fieldLabel).append("',hiddenName: '")
					.append(cellDsc).append("'");
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1) // ornegin
																// ,tooltip:'ali'
																// gibi
				buf.append(fc.getExtraDefinition());
			if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan
																// geliyor
				if (cellResult.getLookupQueryResult().getQuery().getQueryType() == 14) {// lookup
																						// tree
																						// query
					if (cellResult.getLookupQueryResult().getData() != null) {
						buf.append(",\nchildren:").append(
								serializeTreeQueryRemoteData(cellResult
										.getLookupQueryResult()));
						// if(value!=null &&
						// value.length()>0)buf.append(",listeners:{render:function(){this.setValue('").append(value).append("')}}");
					}
				} else {// TODO: hata olamaz, tree combo'da sadece queryType==12
						// icin olur
				}
			}
			// buf.append(",\nvalueField:'id',displayField:'dsc',typeAhead: false,mode: 'local',triggerAction: 'all',selectOnFocus:true,forceSelection:true");
			if (fc.getControlWidth() > 0)
				buf.append(",width:").append(fc.getControlWidth());
			if (value != null && value.length() > 0)
				buf.append(",value:'").append(GenericUtil.stringToJS(value))
						.append("'");
			if (fc.getNrdType() != 0)
				buf.append(",disabled:true");
			if (notNull)
				buf.append(",allowBlank:false");
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1) // ornegin
																// ,tooltip:'ali'
																// gibi
				buf.append(fc.getExtraDefinition());
			if (liveSyncStr != null)
				buf.append(",listeners:{").append(liveSyncStr).append("}");
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			return buf.append("})");
		case 55: // TreePanel
			buf = new StringBuilder();
			buf.append("new Ext.ux.tree.CheckTreePanel({ ");
			buf.append("name:'").append(fc.getDsc()).append("',fieldLabel: '")
					.append(fieldLabel).append("'");
			if (fc.getControlWidth() > 0)
				buf.append(",width:").append(fc.getControlWidth());
			if (value != null && value.length() > 0)
				buf.append(",value:'").append(GenericUtil.stringToJS(value))
						.append("'");
			buf.append(",useArrows:true,enableDD:true,autoScroll:true,isFormField:true,border:true,rootVisible:false,listeners: {'afterrender': function(comp){ if(comp.value) comp.setValue(comp.value)}}");
			if (fc.getExtraDefinition() != null
					&& fc.getExtraDefinition().length() > 1)
				buf.append(fc.getExtraDefinition()); // ornegin ,tooltip:'ali'
														// gibi
			buf.append(",root:{nodeType:'async',expanded:true,text:'root',id:'root',uiProvider:false");
			if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan
																// geliyor
				if (cellResult.getLookupQueryResult().getQuery().getQueryType() == 12) {// lookup
																						// tree
																						// query
					if (cellResult.getLookupQueryResult().getData() != null) {
						buf.append(",\nchildren:").append(
								serializeTreeQueryData(cellResult
										.getLookupQueryResult()));
						// if(value!=null &&
						// value.length()>0)buf.append(",listeners:{render:function(){this.setValue('").append(value).append("')}}");
					}
				} else {// TODO: hata olamaz, tree combo'da sadece queryType==12
						// icin olur
				}

			}
			if (liveSyncStr != null)
				buf.append(",listeners:{").append(liveSyncStr).append("}");
			if(formResult!=null && formResult.getUniqueId()!=null)
				buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
			return buf.append("}})");
		case 6:// combobox (static lookup) local
		case 7:// combobox (query lookup) local
		case 51:// user defined combo
			// if(fc.get_sourceObject() != null && fc.get_sourceObject()
			// instanceof WTableField &&
			// (((WQueryParam)fc.get_sourceObject()).getOperatorTip()==8 ||
			// ((WQueryParam)fc.get_sourceObject()).getOperatorTip()==9))return
			// "''";
			if(formResult != null && fc.getParentFormCellId()==1) {
				buf.append("RadioGroup({").append(uniqeId).append("_controlTip:")
					.append(controlType)
					.append(",_checkbox:!0, labelSeparator:'', fieldLabel: '")
					.append(fieldLabel).append("',items: [");
				
				boolean b = false;
				int dataCount = 0;
				if (cellResult.getLookupListValues() != null) { // cell LookUp'tan
					dataCount = ((List<W5Detay>) cellResult.getLookupListValues()).size();
					for (W5Detay p : (List<W5Detay>) cellResult
							.getLookupListValues()) {
						if (b)
							buf.append(",");
						else
							b = true;
						buf.append("{inputValue:'")
								.append(p.getVal())
								.append("',name:'")
								.append(fc.getDsc())
								.append("',boxLabel:'")
								.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache
										.get2(customizationId, xlocale, p.getDsc())
										.replaceAll(",", "-") : p.getDsc()
										.replaceAll(",", "-")).append("'");
						if(!GenericUtil.isEmpty(cellResult.getValue()) 
								&& GenericUtil.hasPartInside2(cellResult.getValue(), p.getVal()))
							buf.append(",checked:true");
						buf.append("}");
					}
				} else if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan
					b = false;
					if (cellResult.getLookupQueryResult().getData() != null) {
						dataCount = cellResult.getLookupQueryResult().getData().size();
						for (Object[] p : cellResult.getLookupQueryResult().getData()) {
							if (b)
								buf.append(",");
							else
								b = true;
							buf.append("{name:'").append(fc.getDsc()).append("'");
							for (W5QueryField f : cellResult.getLookupQueryResult().getQuery().get_queryFields()) {
								
								Object z = p[f.getTabOrder() - 1];
								if(z==null)continue;
								
								buf.append(",");
								if(f.getDsc().equals("dsc"))
									buf.append("boxLabel:");
								else if(f.getDsc().equals("id")) {
									if(!GenericUtil.isEmpty(cellResult.getValue()) 
											&& GenericUtil.hasPartInside2(cellResult.getValue(), z.toString()))
										buf.append("checked:true,");
									buf.append("inputValue:");
								}

								buf.append("'")
										.append(f.getPostProcessType() == 2 ? LocaleMsgCache
												.get2(customizationId, xlocale,
														z.toString()).replaceAll(
														",", "-") : GenericUtil
												.stringToJS(z.toString())
												.replaceAll(",", "-")).append("'");
							}
							buf.append("}");
						}
					}
				}
				buf.append("]");
				if(dataCount>2)buf.append(",columns:2");
				if(fc.getControlWidth()>0)buf.append(",width:").append(fc.getControlWidth());
				if(!GenericUtil.isEmpty(cellResult.getValue()))buf.append(",value:'").append(GenericUtil.stringToJS(cellResult.getValue()))
					.append("'");;
						
				if(!GenericUtil.isEmpty(fc.getExtraDefinition()))buf.append(fc.getExtraDefinition());
				if(formResult!=null && formResult.getUniqueId()!=null)
					buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
				buf.append("})");
				return buf;
				
				
			} else {

				if (controlType == 7 && fc.getDialogGridId() != 0)
					buf.append("Dialog");
				else {
					if (!notNull)
						buf.append("Clearable");
					if (formResult != null
							&& formResult.getForm() != null
							&& formResult.getScd() != null
							&& formResult.getForm().getObjectType() == 2
							&& FrameworkCache.getAppSettingIntValue(
									formResult.getScd(), "combobox_add_flag") == 1
							&& ((controlType == 7
									&& cellResult.getLookupQueryResult() != null
									&& cellResult.getLookupQueryResult().getMainTable() != null
									&& cellResult.getLookupQueryResult().getMainTable().getTableId() != 336 /*w5_user*/
									&& fc.getLookupEditFormId()!=0
									&& FrameworkCache.roleAccessControl(formResult.getScd(),2)
									&& (GenericUtil.isEmpty(cellResult.getFormCell().getExtraDefinition()) || cellResult.getFormCell().getExtraDefinition().indexOf("noInsertForm")<0)
									&& GenericUtil.accessControl(formResult.getScd(), 
											cellResult.getLookupQueryResult().getMainTable().getAccessViewTip(), 
											cellResult.getLookupQueryResult().getMainTable().getAccessViewRoles(),
											cellResult.getLookupQueryResult().getMainTable().getAccessViewUsers()) 
									&& GenericUtil.accessControl(formResult.getScd(),
												cellResult.getLookupQueryResult().getMainTable().getAccessInsertTip(),
												cellResult.getLookupQueryResult().getMainTable().getAccessInsertRoles(),
												cellResult.getLookupQueryResult().getMainTable().getAccessInsertUsers()))
									|| (controlType == 6
											&& cellResult.getLookupListValues() != null
											&& ((Integer) formResult.getScd().get(
													"roleId") == 0) 
													&& FrameworkCache.roleAccessControl(formResult.getScd(), 107)) 
														|| (controlType == 51 && FrameworkCache.roleAccessControl(formResult.getScd(), 107)))) {
						buf.append("Add");
						fadd = true;
					}
				}
	
				if(FrameworkSetting.lookupEditFormFlag && formResult!=null && formResult.getForm().getObjectType()==2 && (controlType == 7 ||  controlType == 10 )
						&& cellResult.getLookupQueryResult() != null
						&& cellResult.getLookupQueryResult().getMainTable() != null && cellResult.getLookupQueryResult().getMainTable().getTableId() != 336 /*w5_user*/
						&& (cellResult.getFormCell().getLookupEditFormId()!=0 || cellResult.getLookupQueryResult().getMainTable().getDefaultUpdateFormId()!=0)
	/*					&& PromisCache.roleAccessControl(formResult.getScd(), cellResult.getLookupQueryResult().getMainTable().getModuleId(), 1)
						&& PromisUtil.accessControl(formResult.getScd(), 
								cellResult.getLookupQueryResult().getMainTable().getAccessViewTip(), 
								cellResult.getLookupQueryResult().getMainTable().getAccessViewRoles(),
								cellResult.getLookupQueryResult().getMainTable().getAccessViewUsers()) 
						&& PromisUtil.accessControl(formResult.getScd(),
								cellResult.getLookupQueryResult().getMainTable().getAccessUpdateTip(),
								cellResult.getLookupQueryResult().getMainTable().getAccessUpdateRoles(),
								cellResult.getLookupQueryResult().getMainTable().getAccessUpdateUsers())*/){
	                // e.HOME, e.END, e.PAGE_UP, e.PAGE_DOWN,
	                // e.TAB, e.ESC, arrow keys: e.LEFT, e.RIGHT, e.UP, e.DOWN
					W5Table t = cellResult.getLookupQueryResult().getMainTable();
					if(t.getAccessUpdateUserFields()==null || GenericUtil.accessControl(formResult.getScd(), t.getAccessUpdateTip(), t.getAccessUpdateRoles(),t.getAccessUpdateUsers())){
						int editFormId = cellResult.getFormCell().getLookupEditFormId()!=0 ? cellResult.getFormCell().getLookupEditFormId():t.getDefaultUpdateFormId();
						String s ="specialkey: function(field, e){if (e.getKey() == e.ENTER) {if(field.getValue())mainPanel.loadTab({attributes:{modalWindow:true,href:'showForm?a=1&_fid="
						+editFormId+"&_tb_id="+t.getTableId()+"&_tb_pk='+field.getValue()}});else alert(getLocMsg('js_once_birseyler_secmelisiniz'))}}";
						liveSyncStr = (liveSyncStr!=null) ? s+","+liveSyncStr:s;
						if(fieldLabel.endsWith("ctCls:'required")){
							fieldLabel+="_edit_form";
						} else {
							fieldLabel += "',ctCls:'edit_form";
						}
					}
				}
				
				buf.append("ComboBox({").append(uniqeId).append("_controlTip:")
						.append(controlType)
						.append(",labelSeparator:'',fieldLabel: '")
						.append(fieldLabel).append("',hiddenName: '")
						.append(cellDsc).append("'");
				if (fc.getExtraDefinition() != null
						&& fc.getExtraDefinition().length() > 1) 
					buf.append(fc.getExtraDefinition());
				else if (FrameworkSetting.simpleSelectShowEmptyText) {
					buf.append(",emptyText:'")
					.append(LocaleMsgCache.get2(0, xlocale,
							"simple_select_something")).append("'");// type something
				}
				if (cellResult.getLookupListValues() != null) { // cell LookUp'tan
																// geliyor
					buf.append(",\nstore: new Ext.data.SimpleStore({id:0,fields:['id','dsc'");
	
					buf.append("],data:[");
					b1 = false;
					for (W5Detay p : (List<W5Detay>) cellResult
							.getLookupListValues()) {
						if (b1)
							buf.append(",");
						else
							b1 = true;
						buf.append("['")
								.append(p.getVal())
								.append("','")
								.append(cellResult.getLocaleMsgFlag() != 0 ? LocaleMsgCache
										.get2(customizationId, xlocale, p.getDsc())
										: p.getDsc()).append("'");
						
						buf.append("]");
					}
					buf.append("]})");
				} else if (cellResult.getLookupQueryResult() != null) { // QueryResult'tan
																		// geliyor
					buf.append(",\nstore: new Ext.data.SimpleStore({id:1,fields:[");
					b1 = false;
					for (W5QueryField f : cellResult.getLookupQueryResult()
							.getQuery().get_queryFields()) {
						if (b1)
							buf.append(",");
						else
							b1 = true;
						buf.append("{name:'").append(f.getDsc()).append("'");
						if (f.getFieldType() > 2)
							buf.append(",type:'")
									.append(FrameworkSetting.sortMap[f.getFieldType()])
									.append("'");
						if (f.getFieldType() == 2)
							buf.append(",type:'date',dateFormat:'").append(dateFormatMulti[formResult.getScd()!=null ? GenericUtil.uInt(formResult.getScd().get("date_format")):0]).append(" h:i:s'");
						// if(f.getPostProcessTip()>=10)buf.append("},{name:'").append(f.getDsc()).append("_qw_'");
						buf.append("}");
					}
					buf.append("],data:[");
					b1 = false;
					if (cellResult.getLookupQueryResult().getData() != null)
						for (Object[] p : cellResult.getLookupQueryResult()
								.getData()) {
							if (b1)
								buf.append(",");
							else
								b1 = true;
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
								buf.append("'")
										.append(f.getPostProcessType() == 2 ? LocaleMsgCache
												.get2(customizationId, xlocale,
														z.toString()) : GenericUtil
												.stringToJS(z.toString()))
										.append("'");
							}
							buf.append("]");
						}
					buf.append("]})");
				}
	
				buf.append(",\nvalueField:'id',displayField:'dsc',typeAhead: false,mode: 'local',triggerAction: 'all',selectOnFocus:true,forceSelection:true");
				if (fc.getControlWidth() > 0)
					buf.append(",width:").append(fc.getControlWidth());
				if (value != null && value.length() > 0)
					buf.append(",value:'").append(GenericUtil.stringToJS(value))
							.append("'");
				if (fc.getNrdType() != 0)
					buf.append(",disabled:true");
				if (fc.getVtype() != null && fc.getVtype().length() > 0)
					buf.append(",vtype:'").append(fc.getVtype()).append("'");
				
				if (notNull)
					buf.append(",allowBlank:false");
				if (controlType == 7 && fc.getDialogGridId() != 0) {
					buf.append(
							",onTrigger2Click:function(a,b,c){mainPanel.loadTab({attributes:{modalWindow:true,href:'showPage?_tid=178&_gid1=")
							.append(fc.getDialogGridId())
							.append("',buttons:[{text:'Seç',handler:function(a,b){if(!a._grid || !a._grid.getSelectionModel().getSelected()){alert('Önce birşeyler seçiniz');return;}\n_")
							.append(cellDsc)
							.append(".setValue(a._grid.getSelectionModel().getSelected().id);_")
							.append(cellDsc)
							.append(".fireEvent('select');mainPanel.closeModalWindow();}}],value:_")
							.append(cellDsc).append(".getValue()}})}");
				} else if (fadd
						&& (fc.getExtraDefinition() == null || (fc
								.getExtraDefinition() != null && fc
								.getExtraDefinition().indexOf(
										"onTrigger" + (notNull ? "2" : "3")
												+ "Click") < 1))) {
					buf.append(",onTrigger").append(notNull ? 2 : 3)
							.append("Click:function(a,b,c){");
					switch (controlType) {
					case 6:
						buf.append(
								"mainPanel.loadTab({attributes:{modalWindow:true,href:'showForm?a=2&look_up_id=")
								.append(fc.getLookupQueryId())
								.append("&_fid=2190'}});");
						break;
					case 7:
						buf.append(
								"mainPanel.loadTab({attributes:{_formCell:mf._").append(fc.getDsc()).append(",modalWindow:true,href:'showForm?a=2&_fid=")
								.append(fc.getLookupEditFormId())
								.append("'}});");
						break;
					case 51:
						buf.append(
								"mainPanel.loadTab({attributes:{modalWindow:true,href:'showForm?a=2&look_up_id=")
								.append(fc.getLookupQueryId())
								.append("&_fid=964'}});");
						break;
					}
					buf.append("}");
				}
				if (liveSyncStr != null)//liveSyncRecord && 
					buf.append(",listeners:{").append(liveSyncStr).append("}");
				if(formResult!=null && formResult.getUniqueId()!=null)
					buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
				return buf.append("})");
			}
		}

		buf.append("({selectOnFocus:true,fieldLabel: '").append(fieldLabel)
				.append("',name: '").append(cellDsc).append("'");


		if (controlType == 41 && fc.getLookupQueryId()>0 && fc.getLookupQueryId()<6) {//codemirror
			if(FrameworkSetting.monaco)
				buf.append(",value:'',language:'").append(new String[]{"javascript","html","xml","sql","css"}[fc.getLookupQueryId()-1]).append("'");
			else 
				buf.append(",value:'',listeners:{},mode:'").append(new String[]{"javascript","htmlmixed","xml","sql"}[fc.getLookupQueryId()-1]).append("'");
		}

		if (fc.get_sourceObjectDetail() != null)
			buf.append(",allowBlank:").append(!notNull);
		
		switch(controlType) {
		case 2://date
			buf.append(",format: '").append(dateFormatMulti[formResult!=null && formResult.getScd()!=null ? GenericUtil.uInt(formResult.getScd().get("date_format")):0]).append("'");
			break;
		case 18://timestamp
			String frmt = dateFormatMulti[formResult!=null && formResult.getScd()!=null ? GenericUtil.uInt(formResult.getScd().get("date_format")):0]; 
			buf.append(",dateFormat: '").append(frmt).append("', hiddenFormat:'").append(frmt).append(" H:i:s'");
			break;
	
		case 4:
			buf.append(",style: 'text-align: right',decimalPrecision:0");
			break;
		/*
		 * if(controlType==3 && fc.getLookupQueryId()>0 &&
		 * fc.getLookupQueryId()<10 ){
		 * buf.append(",decimalPrecision:").append(fc.getLookupQueryId()); }
		 */
		case 3://double
			if (fc.getLookupQueryId() > 0 && fc.getLookupQueryId() < 13) {
				buf.append(",style: 'text-align: right',decimalPrecision:")
						.append(fc.getLookupQueryId());
			} else {
				buf.append(",style: 'text-align: right',decimalPrecision:8"); // grid form cell * EditDouble ise decimal precision default 2 değil 8 olsun.
			}
		break;
		
		case 33://money
			if (fc.getLookupQueryId() > 0 && fc.getLookupQueryId() < 13) {
				buf.append(",alwaysDisplayDecimals: true,decimalPrecision:")
						.append(fc.getLookupQueryId());
			} else {
				buf.append(",alwaysDisplayDecimals: true,decimalPrecision:2"); // grid form cell * EditDouble ise decimal precision default 2 değil 8 olsun.
			}
		break;		
		
		case 56://deprecated
			buf.append(",CKConfig: {customConfig : '/scripts/ext3.3/ckeditor/config.js'}");
		break;
		
		case 1://text
			buf.append(",cls:'x-icb-text'");
			break;
		case 42://password
			buf.append(",inputType:'password'");
			break;
		}

		if (fc.getMaxLength()!=null && fc.getMaxLength() > 0)
			buf.append(",maxLength:").append(fc.getMaxLength());
		if (fc.getControlWidth() > 0)
			buf.append(",width:").append(fc.getControlWidth());
		else if (fc.getControlWidth() < 0)
			buf.append(",anchor:'%").append(-fc.getControlWidth()).append("'");
		/*
		 * if(controlType==18)
		 * buf.append(",timeConfig:{altFormats:'H:i:s',allowBlank:"
		 * ).append(!notNull
		 * ).append("},dateConfig:{altFormats:'d/m/Y',allowBlank:"
		 * ).append(!notNull).append("}");
		 * buf.append(",timeFormat:'H:i:s',dateFormat:'d/m/Y'");
		 */
		if (value != null && value.length() > 0)
			switch (controlType) {
			case 18:// date time field
				if (!"0".equals(value) && value.length() <= 10)
					value = GenericUtil.uDateStr(value) + " 00:00:00";
				buf.append(",value:'")
						.append("0".equals(value) ? GenericUtil
								.uFormatDateTime(new Date()) : value)
						.append("'");
				break;
			case 2:// date field
				buf.append(",value:'").append(GenericUtil.uDateStr(value))
						.append("'");
				break;
			case	42://password
				buf.append(",value:'").append(FrameworkSetting.defaultPasswordMask)
				.append("'");
				break;
			default:
				buf.append(",value:'").append(GenericUtil.stringToJS(value))
						.append("'");
			}
		if (fc.getVtype() != null && fc.getVtype().length() > 0)
			buf.append(",vtype:'").append(fc.getVtype()).append("'");
		
		if (controlType == 11)
			buf.append(",grow:true,preventScrollbars:true");
		if (controlType == 17)
			buf.append(",triggerClass:'x-form-search-trigger'");
		if (controlType == 31)
			buf.append(",formCellId:").append(fc.getFormCellId());
		if (controlType == 22)
			buf.append(",format:'H:i:s'");

		if (fc.getNrdType() != 0)
			buf.append(",disabled:true");
		if (fc.getExtraDefinition() != null
				&& fc.getExtraDefinition().length() > 1) // ornegin
															// ,tooltip:'ali'
															// gibi
			buf.append(fc.getExtraDefinition());
		if (liveSyncRecord) {
			int ix = buf.indexOf("listeners:{render");
			if (ix < 0)
				buf.append(",listeners:{").append(liveSyncStr).append("}");
			else {
				liveSyncStr += ",";
				ix += "listeners:{".length();
				buf.append(liveSyncStr, ix, ix);
			}
		}
		if(formResult!=null && formResult.getUniqueId()!=null)
			buf.append(",id:'").append(formResult.getUniqueId()).append("-").append(fc.getFormCellId()).append("'");
		return buf.append(",labelSeparator:'',_controlTip:").append(controlType)
				.append("})");
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
					if (toolbarItem.getDsc().equals("-"))
						buttons.append("'-'");
					else if (toolbarItem.getDsc().equals("->"))
						buttons.append("'->'");
					else if (toolbarItem.getObjectType() == 15) {// form toolbar
																// ise
						buttons.append("{text:'")
								.append(LocaleMsgCache.get2(customizationId,
										xlocale, toolbarItem.getLocaleMsgKey()))
								.append("',");
						if (mediumButtonSize)
							buttons.append("iconAlign: 'top', scale:'medium', style:{margin: '0px 5px 0px 5px'},");
						buttons.append("iconCls:'")
								.append(toolbarItem.getImgIcon())
								.append("', handler:function(a,b,c){\n")
								.append(LocaleMsgCache.filter2(
										customizationId, xlocale,
										toolbarItem.getCode())).append("\n}}");
						itemCount++;
					} else {
						String icon = "<i class=\"icon-"+(GenericUtil.isEmpty(toolbarItem.getImgIcon())? "energy":toolbarItem.getImgIcon())+"\"></i>";
						buttons.append("{text:'")
								.append(toolbarItem.getControlType() == 0 ? icon : (GenericUtil.isEmpty(toolbarItem.getImgIcon()) ? "":(icon + " &nbsp;")) + LocaleMsgCache.get2(scd, toolbarItem.getLocaleMsgKey()))
								.append("', tooltip:'").append(LocaleMsgCache.get2(scd, toolbarItem.getLocaleMsgKey()))
								.append("', ref:'../")
								.append(toolbarItem.getDsc())
/*								.append("',")
								.append(toolbarItem.getItemTip() == 0 ? "cls"
										: "iconCls")
								.append(":'")
								.append(toolbarItem.getImgIcon())*/
								.append("', activeOnSelection:")
								.append(toolbarItem.getActiveOnSelectionFlag() != 0)
								.append(", handler:function(a,b,c){\n")
								.append(LocaleMsgCache.filter2(
										customizationId, xlocale,
										toolbarItem.getCode())).append("\n}}");
						itemCount++;
					}
				} else { // controlType
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
						if(lu!=null){
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

	public StringBuilder serializeCard(W5CardResult cr) {
		String xlocale = (String) cr.getScd().get("locale");
		int customizationId = (Integer) cr.getScd().get(
				"customizationId");
		W5Card c = cr.getCard();
		StringBuilder buf = new StringBuilder();
		buf.append("var ")
				.append(c.getDsc())
				.append("={dataViewId:")
				.append(c.getCardId())
				.append(",name:'")
				.append(LocaleMsgCache.get2(customizationId, xlocale,
						c.getLocaleMsgKey()))
				.append("'");
		if(!GenericUtil.isEmpty(c.get_orderQueryFieldNames())){
			buf.append(",\n orderNames:[");
			for(String f:c.get_orderQueryFieldNames()){
				buf.append("{id:'").append(f).append("',dsc:'").append(LocaleMsgCache.get2(cr.getScd(), f)).append("'},");
			}
			buf.setLength(buf.length()-1);
			buf.append("]");
		}		
		buf.append(",store: new Ext.data.JsonStore({url:'ajaxQueryData?.w='+_webPageId+'&_qid=")
				.append(c.getQueryId()).append("&_dvid=")
				.append(c.getCardId());

		if (c.getDefaultPageRecordNumber() != 0)
			buf.append("&firstLimit=").append(c.getDefaultPageRecordNumber())
					.append("',remoteSort:true,");
		else
			buf.append("',");
		buf.append(
				serializeQueryReader(c.get_query().get_queryFields(), c.get_pkQueryField().getDsc(), c.get_postProcessQueryFields(), (c
						.get_query().getShowParentRecordFlag() != 0 ? 2 : 0), c
						.get_crudTable(), cr.getScd())).append(
				",listeners:{loadexception:promisLoadException");
		// if(d.getDefaultPageRecordNumber()!=0)buf.append(",afterload:function(aa,bb){alert('geldim');alert(aa.getCount())}");
		buf.append("}})");
		if (c.getDefaultWidth() != 0)
			buf.append(",\n defaultWidth:").append(c.getDefaultWidth());
		if (c.getDefaultHeight() != 0)
			buf.append(", defaultHeight:").append(c.getDefaultHeight());
		if (cr.getSearchFormResult() != null) {
			buf.append(",\n searchForm:").append(
					serializeGetForm(cr.getSearchFormResult()));
		}
		if (!GenericUtil.isEmpty(c.get_toolbarItemList())) { // extra buttonlari
															// var mi yok mu?
			StringBuilder buttons = serializeToolbarItems(
					cr.getScd(), c.get_toolbarItemList(), false);
			if (buttons != null && buttons.length() > 1) {
				buf.append(",\n extraButtons:[").append(buttons).append("]");
			}
		}
		if (!GenericUtil.isEmpty(c.get_menuItemList())) { // menu items
			StringBuilder buttons = serializeMenuItems(cr.getScd(), c.get_menuItemList());
			if (buttons != null && buttons.length() > 1) {
			buf.append(",\n menuButtons:[").append(buttons).append("]");
			}
		}
		if (!GenericUtil.isEmpty(c.get_crudFormConversionList())) {
			buf.append(",\n formConversionList:[")
					.append(serializeManualConversions(cr.getScd(), c.get_crudFormConversionList(), false)).append("]");
		}
		if(c.get_defaultCrudForm()!=null) {
			if(FrameworkSetting.vcs && c.get_crudTable().getVcsFlag()!=0)buf.append(",\n vcs:!0");
			if(FrameworkCache.getAppSettingIntValue(customizationId, "new_record_label_flag")!=0)
				buf.append(",newRecordLabel:'").append(LocaleMsgCache.get2(cr.getScd(),"new_record_prefix"))
				.append(LocaleMsgCache.get2(cr.getScd(),c.get_defaultCrudForm().getLocaleMsgKey()).toUpperCase()).append("'");

			buf.append(",\n crudFormId:")
			.append(c.getDefaultCrudFormId())
			.append(serializeCrudFlags(cr.getScd(), c.get_crudTable(), false));
		}
		
		if (c.getDefaultPageRecordNumber() != 0)
			buf.append(",\n pageSize:").append(c.getDefaultPageRecordNumber());
		// buf.append(",\n tpl:'<tpl for=\".\">").append(PromisUtil.stringToJS(d.getTemplateCode())).append("</tpl>',\nautoScroll:true,overClass:'x-view-over',itemSelector:'table.grid_detay'};\n");
		buf.append(",\n tpl:\"")
				.append(GenericUtil.stringToJS2(c.getTemplateCode()))
				.append("\",\nautoScroll:true,overClass:\"x-view-over\",itemSelector:\"div.card\"};\n");
		if (!GenericUtil.isEmpty(c.getJsCode())) {
			buf.append("\ntry{")
					.append(GenericUtil.filterExt(c.getJsCode(),
							cr.getScd(),
							cr.getRequestParams(), null))
					.append("\n}catch(e){")
					.append(FrameworkSetting.debug ? "if(confirm('ERROR dataView.JS!!! Throw? : ' + e.message))throw e;"
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
				.append(",store: new Ext.data.JsonStore({baseParams:{},url:'ajaxQueryData?.t='+_page_tab_id+'&.w='+_webPageId+'&_qid=")
				.append(d.getQueryId()).append("&_lvid=").append(d.getListId());

		if (d.getDefaultPageRecordNumber() != 0)
			buf.append("&firstLimit=").append(d.getDefaultPageRecordNumber())
					.append("',remoteSort:true,");
		else
			buf.append("',");
		buf.append(
				serializeQueryReader(d.get_query().get_queryFields(), d
						.get_pkQueryField().getDsc(), null, 0, d
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

	private StringBuilder serializeCrudFlags(Map<String, Object> scd, W5Table t, boolean insertEditMode) {
		StringBuilder buf = new StringBuilder();
//		W5Table t = FrameworkCache.getTable(projectId, g.get_defaultCrudForm().getObjectId());// g.get_defaultCrudForm().get_sourceTable();
		boolean insertFlag = GenericUtil.accessControl(scd,
				t.getAccessInsertTip(), t.getAccessInsertRoles(),
				t.getAccessInsertUsers());
		
		buf.append(",\n crudTableId:")
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
		if (insertEditMode && insertFlag)
			buf.append(",insertEditMode:true");
		if (insertFlag) {
			if (t.getCopyTip() == 1)
				buf.append(",xcopy:true");
			else if (t.getCopyTip() == 2)
				buf.append(",ximport:true");
		}
		// if(PromisCache.getAppSettingIntValue(scd, "revision_flag")!=0
		// && t.getRevisionFlag()!=0)buf.append(",xrevision:true");
		return buf.append("}");		
	}
	
	public StringBuilder serializeGrid(W5GridResult gridResult) {
		Map<String, Object> scd = gridResult.getScd();
		String xlocale = (String) scd.get("locale");
		boolean dev = GenericUtil.uInt(gridResult.getRequestParams(),"_dev")!=0;
		int customizationId = dev ? 0:(Integer) scd.get("customizationId");
		String projectId = FrameworkCache.getProjectId(scd, "5."+gridResult.getGridId());
		W5Grid g = gridResult.getGrid();
		W5Query q = g.get_query();
		StringBuilder buf = new StringBuilder();
		boolean expander = false;

		buf.append("var ").append(g.getDsc()).append("_sm=new Ext.grid.");
		if (!gridResult.isViewLogMode()) {
			switch (g.getSelectionModeType()) {// 0,1:single,2:multi,3:checkbox
												// multi, 4:single + row
												// expander, 5: single + detail
												// dlg
			case	0:buf.setLength(buf.length()-"new Ext.grid.".length());
			buf.append("null;\n");
			break;
			case 3:
				buf.append("CheckboxSelectionModel()\n");
				break;
			default:
				buf.append("RowSelectionModel({singleSelect:")
						.append(g.getSelectionModeType() != 2).append("})\n");
				break;
			}
/*			if (g.getSelectionModeTip() == 4 && g.get_detailView() != null) {// rowexpander
				buf.append("var ")
						.append(g.getDsc())
						.append("_dv=new Ext.ux.grid.RowExpander({tpl:new Ext.Template('")
						.append(g.get_detailView().getLocaleMsgFlag() != 0 ? GenericUtil
								.stringToJS(LocaleMsgCache.filter2(
										customizationId, xlocale, g
												.get_detailView().getCode()))
								: GenericUtil.stringToJS(g.get_detailView()
										.getCode())).append("')})\n");
				expander = true;
			}*/
		} else
			buf.append("RowSelectionModel({singleSelect:true})\n");

		buf.append("var ").append(g.getDsc()).append(" = {\n gridId:").append(g.getGridId()).append(",queryId:").append(g.getQueryId());
		if (gridResult.getExtraOutMap() != null
				&& !gridResult.getExtraOutMap().isEmpty()) {
			buf.append(",\n extraOutMap:")
					.append(GenericUtil.fromMapToJsonString2Recursive(gridResult
							.getExtraOutMap()));
		}

			
		if (FrameworkSetting.liveSyncRecord && g.get_viewTable() != null
				&& g.get_viewTable().getLiveSyncFlag() != 0)
			buf.append(",\n liveSync:true");

		if (g.getDefaultWidth() != 0)
			buf.append(",\n defaultWidth:").append(g.getDefaultWidth());
		if (gridResult.isViewLogMode())
			buf.append(", defaultHeight:").append(
					FrameworkCache.getAppSettingIntValue(scd,
							"log_default_grid_height"));
		else {
			if (g.getSelectionModeType() == 2 || g.getSelectionModeType() == 3) // multi Select
				buf.append(",\n multiSelect:true");
/*			else if (g.getSelectionModeTip() == 5 && g.get_detailView() != null) // promis.js'de
																					// halledilmek
																					// uzere
				buf.append(",\n detailDlg:true");*/
			if (g.getDefaultHeight() > 0)
				buf.append(", defaultHeight:").append(g.getDefaultHeight());

			buf.append(",\n gridReport:").append(FrameworkCache.roleAccessControl(scd,  105));

			buf.append(", saveUserInfo:false");
		}
		buf.append(", loadMask:!0, displayInfo:").append(g.getDefaultPageRecordNumber()>0);
		
		if(q.get_aggQueryFields()!=null) {
			buf.append(",\n displayAgg:[");
			for(W5QueryField f:q.get_aggQueryFields())
				buf.append("{id:'").append(f.getDsc()).append("', f:(x)=> '<span style=\"border-bottom: 1px dotted #E91E63;margin-left:40px;\">")
				.append(LocaleMsgCache.get2(scd, f.getDsc())).append(" :  <b>'+fmtDecimal(x,2,2)+'</b></span>'},");
			buf.setLength(buf.length()-1);
			buf.append("]");
		}
		
		
		if(FrameworkCache.getAppSettingIntValue(customizationId, "toplu_onay") == 1 && g.get_workflow() != null){
			buf.append(",\n approveBulk:true");
			if(g.get_workflow().getApprovalRequestTip() == 2){ // Onay manuel mi başlatılacak ?
				buf.append(", btnApproveRequest:true");
			}
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
					.append(serializeManualConversions(scd, g.get_crudFormConversionList(), dev)).append("]");
		}
		if (gridResult.getRequestParams().get("_tanim_yok_") == null)
			buf.append(",\n sm:").append(g.getDsc()).append("_sm");

		buf.append(",\n viewConfig:{")
				.append(/* g.get_autoExpandField()!=null ? "forceFit:true" : */"")
				.append("}, plugins:[], name:'")
				.append(LocaleMsgCache.get2(customizationId, xlocale,
						g.getLocaleMsgKey())).append("',\n id:'")
				.append(GenericUtil.getNextId("ng")).append("', listeners:{}");

		String ajaxUrl = "ajaxQueryData";
		if (!gridResult.isViewLogMode() && g.getTreeMasterFieldId() != 0) {// tree query + Grouping Field varsa, o zaman
																			// bunu AdjacencyTreeGrid(MaximGB) olarak Goster
			buf.append(
					",\n ds:new Ext.ux.maximgb.tg.AdjacencyListStore({autoLoad:false,url:'"+ajaxUrl+"?.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId+'&_qid=")//_json=1&_tqd=1&
					.append(g.getQueryId());
			if (g.getDefaultPageRecordNumber() != 0)
				buf.append("&firstLimit=")
						.append(g.getDefaultPageRecordNumber())
						.append("',remoteSort:true");
			else
				buf.append("'");
			buf.append(",\n reader: new Ext.data.JsonReader({")
					.append(serializeQueryReader(g.get_query()
							.get_queryFields(), "_id", g
							.get_postProcessQueryFields(), (g.get_query()
							.getShowParentRecordFlag() != 0 ? 2 : 0),
							FrameworkCache.getTable(projectId, g.get_query()
									.getSourceObjectId()), scd))
					.append("}),listeners:{loadexception:promisLoadException}}),\n master_column_id:'")
					.append(g.get_queryFieldMap().get(g.getTreeMasterFieldId())
							.getDsc()).append("'");
			if (g.get_groupingField() != null)
				buf.append(",\n autoExpandColumn:'")
						.append(g.get_groupingField().getDsc()).append("'");
		} else if (!gridResult.isViewLogMode() && g.get_groupingField() != null) {// grouping
																					// fieldli
			buf.append(",\n view:new Ext.grid.GroupingView({groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? \"Items\" : \"Item\"]})'})");
			buf.append(",\n ds:new Ext.data.GroupingStore({groupField:'")
					.append(g.get_groupingField().getDsc())
					.append("',\n proxy:new Ext.data.HttpProxy({url:'"+ajaxUrl+"?.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId+'&_qid=")
					.append(g.getQueryId());
			if (g.getDefaultPageRecordNumber() != 0)
				buf.append("&firstLimit=").append(
						g.getDefaultPageRecordNumber());
			buf.append("',listeners:{loadexception:promisLoadException}})")
					.append(",\n reader: new Ext.data.JsonReader({")
					.append(serializeQueryReader(g.get_query()
							.get_queryFields(),
							gridResult.isViewLogMode() ? "log5_log_id" : g
									.get_pkQueryField().getDsc(),  g
									.get_postProcessQueryFields(), gridResult
									.isViewLogMode() ? 1 : (g.get_query()
									.getShowParentRecordFlag() != 0 ? 2 : 0),
							FrameworkCache.getTable(projectId, g.get_query().getSourceObjectId()), scd)).append("})})");
		} else {
			buf.append(
					",\n ds:new Ext.data.JsonStore({url:'"+ajaxUrl+"?.t='+_page_tab_id+'&.p='+_scd.projectId+'&.w='+_webPageId+'&_qid=")
					.append(g.getQueryId()).append("&_gid=")
					.append(g.getGridId());
			if (gridResult.isViewLogMode()
					|| g.getDefaultPageRecordNumber() != 0)
				buf.append("&firstLimit=")
						.append(gridResult.isViewLogMode() ? FrameworkCache
								.getAppSettingIntValue(scd,
										"log_default_record_per_page") : g
								.getDefaultPageRecordNumber())
						.append("',remoteSort:true,");
			else
				buf.append("',");
			buf.append(
					serializeQueryReader(g.get_query().get_queryFields(),
							gridResult.isViewLogMode() ? "log5_log_id" : g
									.get_pkQueryField().getDsc(), g
									.get_postProcessQueryFields(), gridResult
									.isViewLogMode() ? 1 : (g.get_query()
									.getShowParentRecordFlag() != 0 ? 2 : 0),
							FrameworkCache.getTable(projectId, g.get_query()
									.getSourceObjectId()), scd)).append(
					",listeners:{loadexception:promisLoadException}})");
		}

		if (gridResult.isViewLogMode() || g.getDefaultPageRecordNumber() != 0)
			buf.append(",\n pageSize:").append(
					gridResult.isViewLogMode() ? FrameworkCache
							.getAppSettingIntValue(scd,
									"log_default_record_per_page") : g
							.getDefaultPageRecordNumber());

		if (gridResult.getSearchFormResult() != null) {
			buf.append(",\n searchForm:").append(
					serializeGetForm(gridResult.getSearchFormResult()));
		}
		if (!gridResult.isViewLogMode()) {
			if (g.get_defaultCrudForm() != null) { // insert ve delete
													// buttonlari var mi yok mu?
				W5Table t = FrameworkCache.getTable(projectId, g.get_defaultCrudForm().getObjectId());// g.get_defaultCrudForm().get_sourceTable();
				boolean insertFlag = GenericUtil.accessControl(scd,
						t.getAccessInsertTip(), t.getAccessInsertRoles(),
						t.getAccessInsertUsers());
				if(FrameworkSetting.vcs && t.getVcsFlag()!=0)buf.append(",\n vcs:!0");
				
				if(FrameworkCache.getAppSettingIntValue(customizationId, "new_record_label_flag")!=0)
					buf.append(",newRecordLabel:'").append(LocaleMsgCache.get2(scd,"new_record_prefix"))
					.append(LocaleMsgCache.get2(scd,g.get_defaultCrudForm().getLocaleMsgKey()).toUpperCase()).append("'");

				
				buf.append(",\n crudFormId:")
						.append(g.getDefaultCrudFormId())
						.append(serializeCrudFlags(scd, t, g.getInsertEditModeFlag() != 0));

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
			if (g.get_autoExpandField() != null) {
				boolean b = true;
				if (g.get_defaultCrudForm() != null
						&& g.get_autoExpandField().getMainTableFieldId() != 0) { // insert
																					// ve
																					// delete
																					// buttonlari
																					// var
																					// mi
																					// yok
																					// mu?
					W5Table t = FrameworkCache.getTable(scd, g
							.get_defaultCrudForm().getObjectId());// g.get_defaultCrudForm().get_sourceTable();
					if (t != null) {
						W5TableField dt = t.get_tableFieldMap().get(
								g.get_autoExpandField().getMainTableFieldId());
						if (dt != null
								&& !GenericUtil.accessControl(scd,
										dt.getAccessViewTip(),
										dt.getAccessViewRoles(),
										dt.getAccessViewUsers()))
							b = false;
					}
				}
				if (b)
					buf.append(",\n autoExpandColumn:'")
							.append(g.get_autoExpandField().getDsc())
							.append("', autoExpandMin:100");
			}
		}

		buf.append("\n}");

		buf.append(serializeGridColumns(gridResult));

		if (expander)
			buf.append("\n").append(g.getDsc()).append(".plugins.push(")
					.append(g.getDsc()).append("_dv)");

		switch (g.getRowColorFxType()) {
		case 1:
			if (!GenericUtil.isEmpty(g.get_listCustomGridColumnRenderer())
					&& g.get_fxRowField() != null) {// lookup eslesme
				buf.append("\ntry{\n")
						// .append(g.getDsc()).append(".viewConfig.forceFit=true;\n")
						.append(g.getDsc())
						.append(".viewConfig.showPreview=true;\n")
						.append(g.getDsc())
						.append(".viewConfig.enableRowBody=true;\n")
						.append(g.getDsc())
						.append(".viewConfig.getRowClass=function(rec){\nswitch(rec.data.")
						.append(g.get_fxRowField().getDsc()).append("){");
				for (W5CustomGridColumnRenderer ix : g
						.get_listCustomGridColumnRenderer()) {
					buf.append("\ncase ");
					if (g.get_fxRowField().getFieldType() == 1)
						buf.append("'");
					buf.append(ix.getLookupDetayVal());
					if (g.get_fxRowField().getFieldType() == 1)
						buf.append("'");
					buf.append(":return '").append("bgColor")
							.append(ix.getCssVal().replace("#", ""))
							.append("';");
				}
				buf.append("\ndefault:return null;}}");
				buf.append("\n}catch(e){")
						.append(FrameworkSetting.debug ? "if(confirm('ERROR grid.fx(1)!!! Throw? : ' + e.message))throw e;"
								: "alert('System/Customization ERROR : ' + e.message)");
				buf.append("}\n");
			}
			break;
		case 2:
			if (!GenericUtil.isEmpty(g.get_listCustomGridColumnCondition())
					&& g.get_fxRowField() != null) {// kosul
				buf.append("\ntry{\n")
						// .append(g.getDsc()).append(".viewConfig.forceFit=true;\n")
						.append(g.getDsc())
						.append(".viewConfig.showPreview=true;\n")
						.append(g.getDsc())
						.append(".viewConfig.enableRowBody=true;\n")
						.append(g.getDsc())
						.append(".viewConfig.getRowClass=function(rec){");
				for (W5CustomGridColumnCondition ix : g
						.get_listCustomGridColumnCondition()) {
					buf.append("\nif(rec.data.")
							.append(g.get_fxRowField().getDsc())
							.append((FrameworkSetting.operatorMap[ix
									.getOperatorType()]).equals("=") ? "=="
									: FrameworkSetting.operatorMap[ix
											.getOperatorType()]);
					if (g.get_fxRowField().getFieldType() == 1)
						buf.append("'");
					buf.append(ix.getConditionVal());
					if (g.get_fxRowField().getFieldType() == 1)
						buf.append("'");
					buf.append(")return '").append("bgColor")
							.append(ix.getCssVal().replace("#", ""))
							.append("';");
				}
				buf.append("\nreturn null;}}catch(e){")
						.append(FrameworkSetting.debug ? "if(confirm('ERROR grid.fx(2)!!! Throw? : ' + e.message))throw e;"
								: "alert('System/Customization ERROR : ' + e.message)");
				buf.append("}\n");
			}
			break;
		case 3:
			if (!GenericUtil.isEmpty(g.get_listCustomGridColumnCondition())) {// kosul
				buf.append("\ntry{\n")
						// .append(g.getDsc()).append(".viewConfig.forceFit=true;\n")
						.append(g.getDsc())
						.append(".viewConfig.showPreview=true;\n")
						.append(g.getDsc())
						.append(".viewConfig.enableRowBody=true;\n")
						.append(g.getDsc())
						.append(".viewConfig.getRowClass=function(rec){");
				for (W5CustomGridColumnCondition ix : g
						.get_listCustomGridColumnCondition()) {
					buf.append("\nif(").append(ix.getConditionVal())
							.append(")return '").append("bgColor")
							.append(ix.getCssVal().replace("#", ""))
							.append("';");
				}
				buf.append("\nreturn null;}}catch(e){")
						.append(FrameworkSetting.debug ? "if(confirm('ERROR grid.fx(3)!!! Throw? : ' + e.message))throw e;"
								: "alert('System ERROR : ' + e.message)");
				buf.append("}\n");
			}

		}
		if(scd==null || (Integer)scd.get("roleId")!=0 || GenericUtil.uInt(gridResult.getRequestParams().get("_preview"))==0){
			if (!GenericUtil.isEmpty(g.getJsCode())) {
				buf.append("\ntry{");
				if(FrameworkSetting.debug)buf.append("\n/*iwb:start:grid:").append(gridResult.getGridId()).append(":Code*/\n");
				buf.append(GenericUtil.filterExt(g.getJsCode(), scd,gridResult.getRequestParams(), null));
				if(FrameworkSetting.debug)buf.append("\n/*iwb:end:grid:").append(gridResult.getGridId()).append(":Code*/\n");
				buf.append("\n}catch(e){")
						.append(FrameworkSetting.debug ? "if(confirm('ERROR grid.JS!!! Throw? : ' + e.message))throw e;"
								: "alert('System ERROR : ' + e.message)");
				buf.append("}\n");
			}
		}
		return buf;
	}

	private String toDefaultLookupQueryReader() {
		return "root:'data',totalProperty:'browseInfo.totalCount',id:'id',fields:[{name:'id'},{name:'dsc'},{name:'code'}]";
	}
	private StringBuilder serializeWsMethodReader(W5WsMethod wsMethod,String id,String rootData, Map scd){
		List<W5WsMethodParam> wsMethodParamList = wsMethod.get_params();
		StringBuilder html = new StringBuilder();
		html.append("root:'").append(GenericUtil.isEmpty(rootData) ? ("data."+wsMethod.getDsc()+"Response.data"):rootData).append("',id:'")
				.append(id).append("',fields:[");
		boolean b = false;
		for (W5WsMethodParam f : wsMethodParamList) if(f.getOutFlag()==1 && !f.getDsc().equals("data")){
			if (b)
				html.append(",\n");
			else
				b = true;
			html.append("{name:'").append(f.getDsc()).append("'");
			if (f.getParamType() > 2)
				html.append(",type:'")
						.append(FrameworkSetting.sortMap[f.getParamType()])
						.append("'");
			if (f.getParamType() == 2)
				html.append(",type:'date',dateFormat:'").append(dateFormatMulti[scd!=null ? GenericUtil.uInt(scd.get("date_format")):0]).append(" h:i:s'");

			html.append("}");
			
		}
		html.append("]");

		return html;
	}

	private StringBuilder serializeQueryReader(
			List<W5QueryField> queryFieldList, String id,
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
			if (f.getFieldType() > 2 && f.getFieldType() < 7)
				html.append(",type:'")
						.append(FrameworkSetting.sortMap[f.getFieldType()])
						.append("'");
			if (f.getFieldType() == 2)
				html.append(",type:'date',dateFormat:'").append(dateFormatMulti[scd!=null ? GenericUtil.uInt(scd.get("date_format")):0]).append(" h:i:s'");

			if (f.getPostProcessType() >= 10 && f.getPostProcessType() <90)
				html.append("},{name:'").append(f.getDsc()).append("_qw_'");
			html.append("}");
		}
	
		if (!GenericUtil.isEmpty(postProcessQueryFieldList))
			for (W5QueryField f : postProcessQueryFieldList) {
				html.append(",\n{name:'").append(f.getDsc()).append("'");
				if(!f.getDsc().equals(FieldDefinitions.queryFieldName_Vcs))html.append(",type:'int'}");
				else html.append("}");
				
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
			.append(FieldDefinitions.tableFieldName_LogDateTime).append("',type:'date',dateFormat:'").append(dateFormatMulti[scd!=null ? GenericUtil.uInt(scd.get("date_format")):0]).append(" h:i:s'},\n{name:'").append(FieldDefinitions.tableFieldName_LogUserId).append("',type:'int'},{name:'").append(FieldDefinitions.tableFieldName_LogUserId).append("_qw_'}");
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
			"fileAttachmentRenderer", "commentRenderer", "keywordHtml",
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
					.append((float)c.getWidth() / (float)l.get_totalWidth())
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

	private StringBuilder serializeGridColumns(W5GridResult gridResult) {
		Map<String, Object> scd = gridResult.getScd();
		String xlocale = (String) scd.get("locale");
		W5Grid grid = gridResult.getGrid();
//		boolean dev = GenericUtil.uInt(gridResult.getRequestParams(),"_dev")!=0;
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

					if (!GenericUtil.accessControl4SessionField(scd, tf.getRelatedSessionField()) || (tf.getAccessViewUserFields()==null && !GenericUtil.accessControl(gridResult.getScd(),
							tf.getAccessViewTip(), tf.getAccessViewRoles(),
							tf.getAccessViewUsers())))
						continue;// access control
				}
				newColumns.add(c);
			}			
		if (!gridResult.isViewLogMode() && grid.get_postProcessQueryFields() != null && (gridResult.getRequestParams()==null || GenericUtil.uInt(gridResult.getRequestParams(), "_no_post_process_fields")==0)) {
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
					c.setWidth(f.getTabOrder());
					c.setAlignType((short) 0);
					c.setLocaleMsgKey(f.getFieldType()==2?"<div class=\"ifile_attach\" style=\"background-position-x:center;\" border=\"0\"> &nbsp;</div>":"&nbsp;"); //<div class=\""+ FrameworkSetting.postQueryGridImgMap[f.getFieldTip()]+ "\"></div>
					c.setVisibleFlag((short) 1);
					String renderer = postQueryMap[f.getFieldType()];
					c.setRenderer(renderer);
					if(f.getDsc().equals(FieldDefinitions.queryFieldName_Comment) && FrameworkCache.getAppSettingIntValue(scd, "make_comment_summary_flag")!=0){
						c.setWidth((short) (f.getTabOrder() + 10));
						c.setSortableFlag((short)1);
					}
					if (FrameworkSetting.vcs && f.getDsc().equals(FieldDefinitions.queryFieldName_Vcs)) {// vcs
						c.setAlignType((short) 1);
//						c.setLocaleMsgKey("vcs_flag");
						newColumns.add(x, c);
						x++;
						continue;
					} else if (f.getDsc().equals(FieldDefinitions.queryFieldName_Workflow)) {// approval_record_flag
						c.setWidth((short) (f.getTabOrder() + 100));
						c.setAlignType((short) 1);
						c.setLocaleMsgKey("approval_status");
						newColumns.add(x, c);
						x++;
						continue;
					} else if (renderer.indexOf("Renderer") > 0) {// renderer
																	// var
						c.setRenderer(renderer + "(" + grid.getDsc() + ")");
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
			c_dttm.setRenderer("fmtDateTimeAgo");
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
		if (newColumns.size() > 0 && newColumns.get(0).getQueryFieldId()==0)newColumns.get(0).setWidth((short) (newColumns.get(0).getWidth() + 10));

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
							.append(grid.getDsc())
							.append("._")
							.append(f.getDsc())
							.append("=")
							.append(serializeFormCell(customizationId, xlocale,
									fcr, null));
					if (f.getControlType() == 9 || f.getControlType() == 10) {
						buf.append("\n").append(grid.getDsc()).append("._")
								.append(f.getDsc())
								.append(".on('select',function(a,b,c){\n")
								.append(grid.getDsc())
								.append(".sm.getSelected().data.")
								.append(c.get_queryField().getDsc())
								.append("_qw_=b.data.dsc})");
					}/*
					 * if(f.getControlTip()==16){
					 * buf.append("\n").append(grid.getDsc
					 * ()).append("._").append(f.getDsc()).append(
					 * ".on('beforecomplete',function(a,b,c){\nalert(a);")
					 * .append
					 * (grid.getDsc()).append(".sm.getSelected().data.").append
					 * (c
					 * .get_queryField().getDsc()).append("_qw_=a.getRawValue()})"
					 * ); }
					 */

					b = true;
					editableColumnSet.add(c.getQueryFieldId());
				}
			}
		if (b)
			buf.append("\n").append(grid.getDsc()).append(".editGrid=true");

		buf.append("\n").append(grid.getDsc()).append(".columns=[");
		b = false;
		if (grid.getSelectionModeType() == 3) {
			buf.append(grid.getDsc()).append("_sm"); // selectionmodel
			b = true;
		}
/*		if (grid.getSelectionModeTip() == 4 && grid.get_detailView() != null) {
			buf.append(grid.getDsc()).append("_dv"); // rowexpander
			b = true;
		}
*/
		StringBuilder bufFilters = new StringBuilder(); // grid filtreleri ilgili kolonları tutacak
		if(false && FrameworkCache.getAppSettingIntValue(scd, "grid_graph_marker")!=0){
			if (b)buf.append(",\n");
			buf.append("{header: '',dataIndex:'grid_graph_marker', width:20, hidden:true, renderer:gridGraphMarkerRenderer(").append(grid.getDsc()).append(")}");
			b = true;
		} 
		
		for (W5GridColumn c : newColumns) {
			String qds = c.get_queryField().getDsc();

			if (b)
				buf.append(",\n");
			else
				b = true;
			boolean editableFlag = editableColumnSet.contains(c
					.getQueryFieldId());
			if (!editableFlag) {
				buf.append("{header: '").append(
						LocaleMsgCache.get2(scd,
								c.getLocaleMsgKey()));
			} else {
				buf.append("{header: '")
						.append("<span class=\"editable_column\">")
						.append(LocaleMsgCache.get2(scd,
								c.getLocaleMsgKey())).append("</span>");
			}
			if (!qds.contains("pkpkpk"))
				buf.append("',tooltip: '")
						.append(LocaleMsgCache.get2(scd,
								c.getLocaleMsgKey()));

			boolean qwRendererFlag = false;
			boolean boolRendererFlag = false;
			buf.append("', dataIndex: '")
					.append(qds)
					.append("'")
					.append(", id: '")
					.append(qds)
					.append("'")
					.append(", sortable: ")
					.append(c.getSortableFlag() != 0
							&& c.get_queryField().getPostProcessType() != 101); // post
																				// sql
																				// select
																				// tip==101
			if (c.getAlignType() != 1)
				buf.append(", align: '")
						.append(FrameworkSetting.alignMap[c.getAlignType()])
						.append("'");// left'ten farkli ise
			buf.append(", width: ").append(c.getWidth());
			if (c.getRenderer() != null) {
				buf.append(", renderer:").append(c.getRenderer());// browser
																	// renderer
																	// ise
				if (c.getRenderer().equals("disabledCheckBoxHtml"))
					boolRendererFlag = true;
			} else if (c.get_queryField().getPostProcessType() >= 10
					&& c.get_queryField().getPostProcessType() <90) {
				if (c.get_formCell() == null || !editableFlag) {
					if (FrameworkSetting.chat
							&& (c.get_queryField().getPostProcessType() == 20 || c
									.get_queryField().getPostProcessType() == 53)) // user
																					// lookup
																					// ise
						buf.append(", renderer:gridUserRenderer('").append(qds)
								.append("')");// browser renderer ise
					else if (c.get_queryField().getPostProcessType() == 12) // table
																			// lookup
																			// ise
						buf.append(", renderer:gridQwRendererWithLink('")
								.append(qds).append("',")
								.append(c.get_queryField().getLookupQueryId())
								.append(")");// browser renderer ise
					else
						buf.append(", renderer:gridQwRenderer('").append(qds)
								.append("')");// browser renderer ise
				} else
					switch (c.get_formCell().getControlType()) {
					case 6:
					case 7:
						buf.append(", renderer:editGridComboRenderer(")
								.append(grid.getDsc()).append("._")
								.append(c.get_formCell().getDsc()).append(")");
						break;
					case 23:
						buf.append(", renderer:editGridTreeComboRenderer(")
								.append(grid.getDsc()).append("._")
								.append(c.get_formCell().getDsc()).append(",'")
								.append(qds).append("')");
						break;
					case 8:
					case 15:
						buf.append(", renderer:editGridLovComboRenderer(")
								.append(grid.getDsc()).append("._")
								.append(c.get_formCell().getDsc()).append(")");
						break;
					default:
						buf.append(", renderer:gridQwRenderer('").append(qds)
								.append("')");// browser renderer ise
					}
			} else {
				if (qds.length() > 3 && qds.indexOf("_dt") == qds.length() - 3)
					buf.append(", renderer:fmtShortDate");// browser renderer
															// ise
				else if (qds.length() > 5 && qds.indexOf("_dttm") == qds.length() - 5){
					buf.append(", renderer:fmtDateTimeAgo");// browser renderer ise
					
				} else if ((qds.length() > 5
						&& qds.endsWith("_flag")) || (qds.length() > 3
								&& qds.startsWith("is_"))) {
					buf.append(", renderer:disabledCheckBoxHtml");// browser
																	// renderer
																	// ise
					boolRendererFlag = true;
				} else if (grid.get_queryFieldMapDsc().get(qds + "_qw_") != null) {
					buf.append(", renderer:gridQwRenderer('").append(qds)
							.append("')");// browser renderer ise
					qwRendererFlag = true;
				}

			}
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
				buf.append(", editor:").append(grid.getDsc()).append("._")
						.append(c.get_formCell().getDsc());
			if (c.getVisibleFlag() == 0)
				buf.append(", hidden: true");
			if (c.getExtraDefinition() != null
					&& c.getExtraDefinition().length() > 2)
				buf.append(c.getExtraDefinition());
			buf.append("}");
			/*
			 * Grid Kolon Filtrelemesi İçin
			 */

			if (c.getFilterFlag() != 0) {
				if (c.get_queryField().getPostProcessType() == 10
						&& c.get_queryField().getLookupQueryId() != 0) {
					bufFilters.append("{type:'list',dataIndex:'").append(qds)
							.append("', options: [");
					W5LookUp lu = FrameworkCache.getLookUp(scd, c
							.get_queryField().getLookupQueryId());
					boolean b2 = false;
					for (W5LookUpDetay ld : lu.get_detayList()) {
						if (b2)
							bufFilters.append(",");
						else
							b2 = true;
						bufFilters.append("[");
						if (c.get_queryField().getFieldType() == 1)
							bufFilters.append("'");
						bufFilters.append(ld.getVal());
						if (c.get_queryField().getFieldType() == 1)
							bufFilters.append("'");
						bufFilters
								.append(",'")
								.append(LocaleMsgCache
										.get2(scd,
												ld.getDsc())).append("']");
					}
					bufFilters.append("]},");

				} else if ((c.get_queryField().getPostProcessType() == 0 || c
						.get_queryField().getPostProcessType() == 7)
						&& !qwRendererFlag)
					bufFilters
							.append("{type: '")
							.append(boolRendererFlag ? "boolean"
									: FrameworkSetting.filterMap[c
											.get_queryField().getFieldType()])
							.append("', dataIndex: '").append(qds)
							.append("'},");
			}
		}
		buf.append("]");

		if (bufGrdColumnGroups.length() > 5) {
			buf.append("\n")
					.append(grid.getDsc())
					.append(".plugins.push(new Ext.ux.grid.ColumnHeaderGroup({rows:[")
					.append(bufGrdColumnGroups).append("]}))");
			// buf.append("\n").append(grid.getDsc()).append(".viewConfig.forceFit=true");
		}

		if (bufFilters.length() > 0) {
			bufFilters.setLength(bufFilters.length() - 1);
			buf.append("\n").append(grid.getDsc()).append(".hasFilter=true");
			buf.append("\n")
					.append(grid.getDsc())
					.append(".plugins.push(new Ext.ux.grid.GridFilters({local:true, filters:[")
					.append(bufFilters).append("]}))");
		}
		return buf;
	}

	public StringBuilder serializeTreeQueryRemoteData(W5QueryResult qr) {
		String children = qr.getRequestParams().get("_children") != null ? qr
				.getRequestParams().get("_children") : "children";
		int customizationId = (Integer) qr.getScd().get(
				"customizationId");
		String xlocale = (String) qr.getScd().get("locale");
		StringBuilder buf = new StringBuilder();
		if (qr.getErrorMap().isEmpty()) {
			buf.append("[");
			int leafField = -1;
			if (qr.getNewQueryFields() != null) {
				for (W5QueryField field : qr.getNewQueryFields())
					if (leafField == -1 && field.getDsc().equals("leaf")) {
						leafField = field.getTabOrder() - 1;
						break;
					}
				if (leafField == -1)
					throw new IWBException("sql", "Query(TreeRemote)",
							qr.getQueryId(), GenericUtil.replaceSql(
									qr.getExecutedSql(),
									qr.getSqlParams()),
							"TreeQueryField does'nt exist: [level]", null);

				List<Object[]> datas = qr.getData();
				if (datas != null && datas.size() > 0) {
					boolean bx = false;
					for (Object[] o : datas) {
						if (bx)
							buf.append(",");
						else
							bx = true;
						buf.append("\n{"); // satir
						boolean b = false;
						for (W5QueryField f : qr.getNewQueryFields()) {

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
											qr.getScd(),
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
												.append(FrameworkCache.getWorkflow(qr.getScd(), f.getLookupQueryId())
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
				s.append(",").append(children).append(":[").append(recursiveSerialize(m.get(id), m, children)).append("]");
			s.append("}");
		}
		return s;
	}

	public StringBuilder serializeTreeQueryData(W5QueryResult qr) {
		String children = qr.getRequestParams().get("_children") != null ? qr
				.getRequestParams().get("_children") : "children";
		int customizationId = (Integer) qr.getScd().get(
				"customizationId");
		String xlocale = (String) qr.getScd().get("locale");
		StringBuilder buf = new StringBuilder();
		boolean json = GenericUtil.uInt(qr.getRequestParams(), "_json")!=0;
		if(json)buf.append("{success:true,data:");
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
						continue;
					}
					if (parentField == -1 && field.getDsc().equals("parent_id")) {
						parentField = field.getTabOrder() - 1;
						continue;
					}
				}
				if (idField == -1 || parentField==-1)
					throw new IWBException("sql", "Query(Tree)",
							qr.getQueryId(), GenericUtil.replaceSql(
									qr.getExecutedSql(),
									qr.getSqlParams()),
							"TreeQueryField does'nt exist: [id || parent_id]", null);

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

							if (b)
								buf2.append(",");
							else
								b = true;
							Object obj = o[f.getTabOrder() - 1];
							if (f.getPostProcessType() == 9)
								buf2.append("_");
							if (f.getFieldType() == 5) {
								buf2.append(f.getDsc()).append(":")
										.append(GenericUtil.uInt(obj) != 0);
								continue;
							}
							
							buf2.append(f.getPostProcessType() == 6 ? f.getDsc().substring(1):f.getDsc()).append(":");
							if (f.getFieldType() != 8)
								buf2.append("'");
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
										buf2.append(obj).append("',")
												.append(f.getDsc())
												.append("_qw_:'")
												.append(res.substring(1));
									}
									break;
								case 53: // User LookUp Real Name
									buf2.append(obj)
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
										buf2.append(obj).append("',")
												.append(f.getDsc())
												.append("_qw_:'")
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
								case 11: // demek ki static lookup'li deger
											// tutulacak
									buf2.append(GenericUtil.stringToJS(obj
											.toString()));
									if (f.getLookupQueryId() == 0)
										break;
									W5LookUp lookUp = FrameworkCache.getLookUp(
											qr.getScd(),
											f.getLookupQueryId());
									if (lookUp == null)
										break;
									buf2.append("',").append(f.getDsc())
											.append("_qw_:'");
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
														.stringToJS(s));
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
										buf2.append("',").append(f.getDsc())
												.append("_qw_:'Reddedildi");
									else
										buf2.append("',")
												.append(f.getDsc())
												.append("_qw_:'")
												.append(FrameworkCache.getWorkflow(qr.getScd(), f.getLookupQueryId())
														.get_approvalStepMap()
														.get(id2).getDsc());
									break;
								
								default:
									buf2.append(GenericUtil.stringToJS(obj
											.toString()));
								}
							}
							if (f.getFieldType() != 8)
								buf2.append("'");
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
			if(json)buf.append(",\n\"browseInfo\":{\"startRow\":").append(qr.getStartRowNumber()).append(",\"fetchCount\":")
			.append(qr.getFetchRowCount()).append("}}");
			return buf;
		} else {
			return buf
					.append("{success:false,errorType:'validation',\nerrors:")
					.append(serializeValidatonErrors(qr.getErrorMap(),
							xlocale)).append("}");

		}
	}

	private StringBuilder serializeTableHelperList(Map scd, List<W5TableRecordHelper> ltrh) {
		StringBuilder buf = new StringBuilder();
		boolean bq = false;
		buf.append("[");
		if (ltrh != null)
			for (W5TableRecordHelper trh : ltrh) {
				W5Table dt = FrameworkCache.getTable(scd,
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
						.append(LocaleMsgCache.get2(scd,
								dt.getDsc())).append("\"")
						.append(",\"dsc\":\"")
						.append(GenericUtil.stringToJS2(trh.getRecordDsc()))
						.append("\"}");
			}
		buf.append("]");
		return buf;
	}

	public StringBuilder serializeQueryData(W5QueryResult qr) {
		if (qr.getQuery().getQueryType() == 10)/* || (queryResult.getRequestParams()!=null && GenericUtil.uInt(queryResult.getRequestParams(), "_tqd")!=0)*/
			return serializeTreeQueryData(qr);
		if (qr.getQuery().getQueryType() == 14)
			return serializeTreeQueryRemoteData(qr);
		int customizationId = (Integer) qr.getScd().get("customizationId");
		String xlocale = (String) qr.getScd().get("locale");
		String userIdStr = qr.getScd().containsKey("userId") ? qr.getScd().get("userId").toString() : null;
		List datas = qr.getData();
		StringBuilder buf = new StringBuilder();
		buf.append("{\"success\":").append(qr.getErrorMap().isEmpty())
				.append(",\"queryId\":").append(qr.getQueryId())
				.append(",\"execDttm\":\"")
				.append(GenericUtil.uFormatDateTime(new Date())).append("\"");
		boolean dismissNull = qr.getRequestParams()!=null && GenericUtil.uInt(qr.getRequestParams(), "_dismissNull")!=0;
		W5Table t = null;
		if (qr.getErrorMap().isEmpty()) {
			buf.append(",\n\"data\":["); // ana
			if (!GenericUtil.isEmpty(datas)) {
				boolean bx = false;
				boolean isMap = (datas.get(0) instanceof Map);
				
				
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
							switch (f.getPostProcessType()) { // queryField PostProcessTip
							case	15://convert to []
								buf.setLength(buf.length()-1);
								buf.append("[").append(obj).append("]");
								continue;
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
							case 5://decryption
								buf.append(GenericUtil.stringToJS2(EncryptionUtil.decrypt(obj.toString(), f.getLookupQueryId())));
								break;
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
										.append(UserUtil.getUserName(GenericUtil.uInt(obj)));
								break;
							case 21: // users LookUp
								String[] ids = ((String) obj).split(",");
								if (ids.length > 0) {
									String res = "";
									for (String s : ids) {
										res += ","+ UserUtil.getUserName(GenericUtil.uInt(s));
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
										res += ","+ UserUtil.getUserDsc(GenericUtil.uInt(s));
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
								if(lookUp.get_detayMap()!=null)for (String q : objs) {
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
												qr.getScd(),
												(short) 1,
												ozs.length > 3 ? ozs[3] : null,
												ozs.length > 4 ? ozs[4] : null))
									buf.append("-");
								buf.append(ozs[2]);
								W5Workflow appr = FrameworkCache.getWorkflow(qr.getScd(), appId);
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
												UserUtil.getUserDsc(GenericUtil.uInt(uid)))
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
					if (qr.getQuery().getShowParentRecordFlag() != 0 && !isMap && ((Object[])o)[((Object[])o).length - 1] != null) {
						buf.append(",\"").append(FieldDefinitions.queryFieldName_HierarchicalData).append("\":")
								.append(serializeTableHelperList(
										qr.getScd(),
										(List<W5TableRecordHelper>) ((Object[])o)[((Object[])o).length - 1]));
					}
					buf.append("}"); // satir
				}
			}
			buf.append("],\n\"browseInfo\":{\"startRow\":")
					.append(qr.getStartRowNumber())
					.append(",\"fetchCount\":")
					.append(qr.getFetchRowCount())
					.append(",\"totalCount\":")
					.append(qr.getResultRowCount()).append("}");
			if (FrameworkSetting.debug && GenericUtil.uInt(qr.getScd().get("mobile"))==0 && qr.getExecutedSql() != null) {
				buf.append(",\n\"sql\":\"")
						.append(GenericUtil.stringToJS2(GenericUtil.replaceSql(
								qr.getExecutedSql(),
								qr.getSqlParams()))).append("\"");
			}
			if (!GenericUtil.isEmpty(qr.getExtraOutMap()))
				buf.append(",\n \"extraOutMap\":").append(
						GenericUtil.fromMapToJsonString2Recursive(qr
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
		if(!GenericUtil.isEmpty(pr.getRequestParams()) && !GenericUtil.isEmpty(pr.getRequestParams().get("_lookUps")))
			b = true;
		else if(!GenericUtil.isEmpty(pr.getPage().get_pageObjectList()))for(W5PageObject po :pr.getPage().get_pageObjectList()) if(po.getObjectType()==6){
			b = true;
			break;			
		}
		if(b) {
			Set<Integer> ls = new HashSet();
			if(!GenericUtil.isEmpty(pr.getRequestParams())){
				String lookUps = pr.getRequestParams().get("_lookUps");
				if(!GenericUtil.isEmpty(lookUps)) {
					String[] ids = lookUps.split(",");
					for (String lookUpId : ids) {
						ls.add(GenericUtil.uInt(lookUpId));
					}
				}
			}
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
		String code = null;
		boolean dev = GenericUtil.uInt(pr.getRequestParams(),"_dev")!=0;
		int customizationId = dev ? 0:(Integer) pr.getScd().get("customizationId");
		String xlocale = (String) pr.getScd().get("locale");
		if (page.getPageType() != 0) { // html degilse
			// notification Control
			// masterRecord Control
			if (pr.getMasterRecordList() != null
					&& !pr.getMasterRecordList().isEmpty())
				buf.append("\n_mrl=")
						.append(serializeTableHelperList(pr.getScd(), pr.getMasterRecordList()))
						.append(";\n");
			// request
			buf.append("var _request=")
					.append(GenericUtil.fromMapToJsonString(pr.getRequestParams())).append("\n");
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
				buf.append("iwb.addCss(\"")
				.append(GenericUtil.stringToJS2(pr.getPage().getCssCode().trim())).append("\",").append(pr.getPageId()).append(");\n");
			}
			
			if (page.getPageType() != 8) { // wizard degilse
				int customObjectCount = 1, tabOrder = 1;
				for (Object i : pr.getPageObjectList()) {
					if (i instanceof W5BIGraphDashboard) {
						W5BIGraphDashboard gd = (W5BIGraphDashboard) i;
						buf.append("\nvar graph")
								.append(gd.getGraphDashboardId())
								.append("=")
								.append(serializeGraphDashboard(gd, pr.getScd()))
								.append(";\n");
					} else if (i instanceof W5GridResult) { // objectType=1
						W5GridResult gr = (W5GridResult) i;
						buf.append(serializeGrid(gr));
						buf.append("\n").append(gr.getGrid().getDsc()).append(".tabOrder=").append(tabOrder++).append(";\n") // template  grid sirasi icin.
						.append(gr.getGrid().getDsc()).append(".tplInfo={id:").append(gr.getTplObj().getPageId()).append(", objId:").append(gr.getTplObj().getPageObjectId()).append("};"); // template  grid sirasi icin.
						
						if (gr.getGridId() < 0) {
							buf.append("\nvar _grid")
									.append(customObjectCount++).append("=")
									.append(gr.getGrid().getDsc()).append("\n");
						}
						if(gr.getTplObj()!=null){
							
						}
						// if(replacePostJsCode)
					} else if (i instanceof W5CardResult) {// objectType=2
						W5CardResult dr = (W5CardResult) i;
						buf.append(serializeCard(dr));
						if (dr.getCardId() < 0) {
							buf.append("\nvar _dataView")
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
		} else { //html
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
						FrameworkCache.getAppSettingStringValue(0, key));
			}
			if(customizationId>0)for (String key : FrameworkCache.publishAppSettings) {
				
				String val = FrameworkCache.getAppSettingStringValue(customizationId, key);
				if(!GenericUtil.isEmpty(val))
						publishedAppSetting.put(
						key, val
						);
			}
			buf2.append("var _app=")
					.append(GenericUtil.fromMapToJsonString(publishedAppSetting))
					.append(";\n");

/*			if (!FrameworkCache.publishLookUps.isEmpty()) {
				buf2.append("var _lookups={");
				boolean b2 = false;
				for (Integer lookUpId : FrameworkCache.publishLookUps) {
					W5LookUp lu = FrameworkCache.getLookUp(customizationId, lookUpId);
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
								LocaleMsgCache.get2(customizationId, xlocale,
												lud.getDsc()));
					buf2.append(GenericUtil.fromMapToJsonString(tempMap));
				}
				buf2.append("};\n");
			}
*/
			buf2.append(serializeLookUp(pr));

			for (Object i : pr.getPageObjectList()) {
				if (i instanceof W5GridResult) {
					W5GridResult gr = (W5GridResult) i;
					buf2.append(serializeGrid(gr));
				} else if (i instanceof W5CardResult) {// objectType=2
					W5CardResult dr = (W5CardResult) i;
					buf2.append(serializeCard(dr));
				} else if (i instanceof W5ListViewResult) {// objectType=7
					W5ListViewResult lr = (W5ListViewResult) i;
					buf2.append(serializeListView(lr));
				} else if (i instanceof W5FormResult) {
					W5FormResult fr = (W5FormResult) i;
					if (fr.getObjectType() == 3) { // form
						buf2.append("\nvar ").append(fr.getForm().getDsc())
								.append("=").append(serializeGetForm(fr));
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
				} else if (i instanceof String) {
					buf2.append("\nvar ").append(i).append("={};");
				}
				buf2.append("\n");
			}
			StringBuilder buf4 = new StringBuilder();
			if (pr.getScd().containsKey("userId")) { // login olmus
																	// demek ki
				buf2.append("\nvar _widgetMap={};\n");



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
				code = code.replace("${promis-css}", " <link rel=\"stylesheet\" type=\"text/css\" href=\"dyn-res/css-"+page.getPageId()+".css?.x="+page.getVersionNo()+"\" />");

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
		buf.append("\n");
		if(!GenericUtil.isEmpty(code)){
			if(page.getPageType()==2 && FrameworkSetting.debug)buf.append("\n/*iwb:start:template:").append(page.getPageId()).append(":Code*/\n");
			buf.append(code.startsWith("!") ? code.substring(1) : code);
			if(page.getPageType()==2 && !GenericUtil.isEmpty(code) && FrameworkSetting.debug)buf.append("\n/*iwb:end:template:").append(page.getPageId()).append(":Code*/");
		}
//		short ttip= page.getTemplateTip();
//		if((ttip==2 || ttip==4) && !GenericUtil.isEmpty(pr.getPageObjectList()))buf.append("\n").append(renderTemplateObject(pr));
		if(!GenericUtil.isEmpty(pr.getPageObjectList()))switch(page.getPageType()){
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
		}
		buf.append("});");
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
			buf.append(",\n\"profile_picture_id\":").append(
					UserUtil.getUserProfilePicture(tableRecordInfoResult.getInsertUserId()));
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
			buf.append(",\n\"fileAttachFlag\":true, \"fileAttachCount\":").append(
					tableRecordInfoResult.getFileAttachmentCount());
		if (tableRecordInfoResult.getCommentCount() != -1)
			buf.append(",\n\"commentFlag\":true, \"commentCount\":").append(
					tableRecordInfoResult.getCommentCount());
		if (tableRecordInfoResult.getAccessControlCount() != -1)
			buf.append(",\n\"accessControlFlag\":true, \"accessControlCount\":")
					.append(tableRecordInfoResult.getAccessControlCount());
		if (tableRecordInfoResult.getFormMailSmsCount() > 0)
			buf.append(",\n\"formSmsMailCount\":").append(
					tableRecordInfoResult.getFormMailSmsCount());
		if (tableRecordInfoResult.getConversionCount() > 0)
			buf.append(",\n\"conversionCount\":").append(
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
		if (!GenericUtil.isEmpty(dbFuncResult.getErrorMap()))
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
					if (!FrameworkCache.roleAccessControl(scd, 0))
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
					.append(UserUtil.getUserProfilePicture(feed.getInsertUserId()))
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

	private StringBuilder serializeManualConversions(Map scd, List<W5Conversion> l, boolean dev) {
		StringBuilder s = new StringBuilder();
		int customizationId = dev ? 0 :(Integer) scd.get("customizationId");
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

	public StringBuilder serializeTreeQueryDataNewNotWorking(W5QueryResult qr) {
		String children = qr.getRequestParams().get("_children") != null ? qr
				.getRequestParams().get("_children") : "children";
		int customizationId = (Integer) qr.getScd().get(
				"customizationId");
		String xlocale = (String) qr.getScd().get("locale");
		StringBuilder buf = new StringBuilder();
		boolean json = GenericUtil.uInt(qr.getRequestParams(), "_json")!=0;
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
						continue;
					}
					if (parentField == -1 && field.getDsc().equals("parent_id")) {
						parentField = field.getTabOrder() - 1;
						continue;
					}
				}
				if (idField == -1 || parentField==-1)
					throw new IWBException("sql", "Query(Tree)",
							qr.getQueryId(), GenericUtil.replaceSql(
									qr.getExecutedSql(),
									qr.getSqlParams()),
							"TreeQueryField does'nt exist: [id || parent_id]", null);

				List<StringBuilder> treeData = new ArrayList();
				Map<String, List> mapOfParent = new HashMap<String, List>();
				
				List<Object[]> datas = qr.getData();
				if (datas != null && datas.size() > 0) {
					for (Object[] o : datas) {
						String id = o[idField].toString();
						String parent = o[parentField].toString();
						mapOfParent.put(id, new ArrayList());
						List childTree = mapOfParent.get(parent);
						if(childTree==null)childTree=treeData;
						
						
						boolean b = false;
						StringBuilder buf2= new StringBuilder();
						buf2.append("\"").append(id).append("\":");//ilk bastaki
						for (W5QueryField f : qr.getNewQueryFields()) {

							if (b)
								buf2.append(",\"");
							else {
								b = true;
								buf2.append("\"");
							}
							Object obj = o[f.getTabOrder() - 1];
							if (f.getPostProcessType() == 9)
								buf2.append("_");
							if (f.getFieldType() == 5) {
								buf2.append(f.getDsc()).append("\":").append(GenericUtil.uInt(obj) != 0);
								continue;
							}
							
							buf2.append(f.getPostProcessType() == 6 ? f.getDsc().substring(1):f.getDsc()).append("\":");
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
											.append("\",")
											.append(f.getDsc())
											.append("_qw_:\"")
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
										buf2.append(obj).append("\",")
												.append(f.getDsc())
												.append("_qw_:'")
												.append(res.substring(1));
									}
									break;
								case 53: // User LookUp Real Name
									buf2.append(obj)
											.append("\",")
											.append(f.getDsc())
											.append("_qw_:\"")
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
										buf2.append(obj).append("\",")
												.append(f.getDsc())
												.append("_qw_:'")
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
								case 11: // demek ki static lookup'li deger
											// tutulacak
									buf2.append(GenericUtil.stringToJS(obj
											.toString()));
									if (f.getLookupQueryId() == 0)
										break;
									W5LookUp lookUp = FrameworkCache.getLookUp(
											qr.getScd(),
											f.getLookupQueryId());
									if (lookUp == null)
										break;
									buf2.append("\",").append(f.getDsc())
											.append("_qw_:\"");
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
														.stringToJS(s));
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
										buf2.append("\",").append(f.getDsc())
												.append("_qw_:'Reddedildi");
									else
										buf2.append("\",")
												.append(f.getDsc())
												.append("_qw_:'")
												.append(FrameworkCache.getWorkflow(qr.getScd(), f.getLookupQueryId())
														.get_approvalStepMap()
														.get(id2).getDsc());
									break;
							
								default:
									buf2.append(GenericUtil.stringToJS(obj
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
			if(json)buf.append("}");
			return buf;
		} else {
			return buf
					.append("{success:false,errorType:'validation',\nerrors:")
					.append(serializeValidatonErrors(qr.getErrorMap(),
							xlocale)).append("}");

		}
	}
	
	private StringBuilder serializeGraphDashboard(W5BIGraphDashboard gd, Map<String, Object> scd){
		StringBuilder buf = new StringBuilder();
		buf.append("{dashId:").append(gd.getGraphDashboardId())
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

}
