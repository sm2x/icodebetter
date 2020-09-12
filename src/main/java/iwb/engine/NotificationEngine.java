package iwb.engine;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.exception.IWBException;
import iwb.model.db.Log5Feed;
import iwb.model.db.W5Email;
import iwb.model.db.W5Form;
import iwb.model.db.W5FormSmsMail;
import iwb.model.db.W5FormSmsMailAlarm;
import iwb.model.db.W5Table;
import iwb.model.db.W5TableField;
import iwb.model.helper.W5AccessControlHelper;
import iwb.model.helper.W5CommentHelper;
import iwb.model.helper.W5QueuedActionHelper;
import iwb.model.helper.W5TableRecordHelper;
import iwb.model.result.W5FormResult;
import iwb.util.GenericUtil;
import iwb.util.MailUtil;

@Component
public class NotificationEngine {
	@Lazy
	@Autowired
	private PostgreSQL dao;


	@Lazy
	@Autowired
	private MetadataLoader metadataLoader;
	


	@Lazy
	@Autowired
	private CRUDEngine crudEngine;
	
	
	public void sendSms(int customizationId, int userId, String phoneNumber, String message, int tableId, int tablePk) {
		Map<String, String> smsMap = new HashMap<String, String>();
		smsMap.put("customizationId", customizationId + "");
		smsMap.put("userId", userId + "");
		smsMap.put("tableId", tableId + "");
		smsMap.put("tablePk", tablePk + "");
		smsMap.put("phoneNumber", phoneNumber);
		smsMap.put("message", message);

		// messageSender.sendMessage("SEND_SMS","BMPADAPTER", smsMap);

	}

	public void extFormAlarm(W5FormResult formResult, int action, Map<String, Object> scd,
			Map<String, String> requestParams, W5Table t, boolean mobile, String ptablePk) {
		if (!FrameworkSetting.alarm)
			return;
		Map<Integer, W5FormSmsMailAlarm> alMap = null;
		if (action == 1 && FrameworkSetting.alarm && !GenericUtil.isEmpty(formResult.getForm().get_formSmsMailList())) {
			boolean alarm = false;
			for (W5FormSmsMail o : formResult.getForm().get_formSmsMailList())
				if (o.getAlarmFlag() != 0) {
					alarm = true;
					break;
				}
			if (alarm) {
				alMap = new HashMap();
				List<W5FormSmsMailAlarm> l = (List<W5FormSmsMailAlarm>) dao.find(
						"from W5FormSmsMailAlarm a where a.projectUuid=?0 AND a.insertUserId=?1 AND a.tableId=?2 AND a.tablePk=?3 ",
						scd.get("projectId"), scd.get("userId"), formResult.getForm().getObjectId(),
						GenericUtil.uInt(ptablePk));
				for (W5FormSmsMailAlarm a : l) {
					alMap.put(a.getFormSmsMailId(), a);
				}
			}
		}

		if (FrameworkSetting.alarm && !GenericUtil.isEmpty(formResult.getForm().get_formSmsMailList()))
			try { // alarm
				String almStr = requestParams.get("_almStr");
				if (GenericUtil.isEmpty(almStr)) {
					for (W5FormSmsMail fsm : formResult.getForm().get_formSmsMailList())
						if (fsm.getAlarmFlag() != 0 && GenericUtil.hasPartInside2(fsm.getActionTypes(), action)
								&& GenericUtil.hasPartInside2(fsm.getWebMobileTips(), mobile ? "2" : "1")
								&& (fsm.getSmsMailSentType() == 0 || fsm.getSmsMailSentType() == 3)) {
							if (GenericUtil.isEmpty(almStr))
								almStr = "" + fsm.getFormSmsMailId();
							else
								almStr += "," + fsm.getFormSmsMailId();
						}
					if (GenericUtil.isEmpty(almStr))
						return;
				}

				List<Map<String, String>> previewMapList = null;
				String[] arSmsStr = almStr.split(",");
				if (arSmsStr != null && arSmsStr.length > 0)
					for (String ass : arSmsStr) {
						String dttm = null;
						if (ass.contains("-")) {
							dttm = ass.substring(ass.indexOf('-') + 1);
							ass = ass.substring(0, ass.indexOf('-'));
						}
						W5FormSmsMail fsm = formResult.getForm().get_formSmsMailMap().get(GenericUtil.uInt(ass));
						if (fsm == null || !GenericUtil.hasPartInside2(fsm.getActionTypes(), action)
								|| !GenericUtil.hasPartInside2(fsm.getWebMobileTips(), mobile ? "2" : "1"))
							continue;
						Date alarmDttm = null;
						if (dttm != null) {
							alarmDttm = GenericUtil.uDateTm(dttm);
						}
						boolean alarmDttmNull = false; // eger update olurken,
														// alarmDttm null ise
														// onu degistirme
						if (alarmDttm == null) {
							W5TableField atf = t.get_tableFieldMap().get(fsm.getAlarmDttmFieldId());
							if (atf != null) {
								String adt = requestParams.get(atf.getDsc());
								if (!GenericUtil.isEmpty(adt)) {
									Date ad = GenericUtil.uDate(adt);
									if (ad != null) {
										if (!GenericUtil.isEmpty(fsm.getAlarmTm()))
											switch (fsm.getAlarmTmTip()) {
											case 0: // sabit
												ad.setTime((1000 * 60 * 60 * 24)
														* (int) (ad.getTime() / (1000 * 60 * 60 * 24))
														+ GenericUtil.uTime2Millis(fsm.getAlarmTm()));
												break;
											case 1: // +
												ad.setTime(ad.getTime() + GenericUtil.uTime2Millis(fsm.getAlarmTm()));
												break;
											case 2: // -
												ad.setTime(ad.getTime() - GenericUtil.uTime2Millis(fsm.getAlarmTm()));
												break;
											}
										alarmDttm = ad;
										alarmDttmNull = true;
									}
								}
							}
						}
						if (alarmDttm != null) { // TODO
							if (!GenericUtil.isEmpty(fsm.getConditionSqlCode())) {
								boolean conditionCheck = dao.conditionRecordExistsCheck(scd, requestParams,
										t.getTableId(), GenericUtil.uInt(ptablePk), fsm.getConditionSqlCode());
								if (!conditionCheck)
									continue;
							}
							W5FormSmsMailAlarm fsma = null;
							if (action == 1 && alMap != null && alMap.containsKey(fsm.getFormSmsMailId())) {
								fsma = alMap.get(fsm.getFormSmsMailId());
								if (fsma.getStatus() == 1) {
									if (!alarmDttmNull)
										fsma.setAlarmDttm(new Timestamp(alarmDttm.getTime()));
									fsma.setDsc(dao.getSummaryText4Record(scd, t.getTableId(), fsma.getTablePk()));
									dao.updateObject(fsma);
								}
								alMap.remove(fsm.getFormSmsMailId());
							} else {
								fsma = new W5FormSmsMailAlarm(scd);
								fsma.setTableId(t.getTableId());
								fsma.setTablePk(GenericUtil.uInt(ptablePk));
								fsma.setFormSmsMailId(fsm.getFormSmsMailId());
								fsma.setStatus((short) 1); // planned
								fsma.setAlarmDttm(new Timestamp(alarmDttm.getTime()));
								fsma.setDsc(dao.getSummaryText4Record(scd, t.getTableId(), fsma.getTablePk()));
								if (fsm.getPreviewFlag() == 0) {
									dao.saveObject(fsma);
									formResult.getOutputMessages().add(LocaleMsgCache.get2(scd, "new_alarm_added_at")
											+ " " + GenericUtil.uFormatDateTime(alarmDttm));

								} else {
									if (GenericUtil.isEmpty(formResult.getFormAlarmList()))
										formResult.setFormAlarmList(new ArrayList());
									formResult.getFormAlarmList().add(fsma);
								}
							}
						}
					}

			} catch (Exception e) {
				if (FrameworkSetting.debug)
					e.printStackTrace();
				formResult.getOutputMessages().add("ALARM EXCEPTION: " + e.getMessage());
			}
		if (action == 1 && FrameworkSetting.alarm && !GenericUtil.isEmpty(alMap))
			for (W5FormSmsMailAlarm a : alMap.values())
				if (a.getStatus() == 1) {
					dao.removeObject(a);
				}
	}

	public void extFormSmsMail(W5FormResult formResult, List<W5QueuedActionHelper> result, int action,
			Map<String, Object> scd, Map<String, String> requestParams, W5Table t, boolean mobile, String ptablePk) {
		if (!GenericUtil.isEmpty(formResult.getForm().get_formSmsMailList())) {
			Set<Integer> tplSet = new HashSet();
			for (W5FormSmsMail m : formResult.getForm().get_formSmsMailList()) {
				if (m.getAlarmFlag() == 0 && m.getActiveFlag() == 1
						&& (m.getSmsMailSentType() == 0 || m.getSmsMailSentType() == 3)
						&& GenericUtil.hasPartInside2(m.getActionTypes(), action)) {
					tplSet.add(m.getFormSmsMailId());
				}
			}
			String smsStr = requestParams.get("_smsStr");
			if (!GenericUtil.isEmpty(smsStr)) {
				String[] arSmsStr = requestParams.get("_smsStr").split(",");
				if (arSmsStr != null && arSmsStr.length > 0)
					for (String ass : arSmsStr) {
						int fsmId = GenericUtil.uInt(ass);
						if (fsmId != 0)
							tplSet.add(fsmId);
					}
			}
			if (!tplSet.isEmpty()) { // smsMail
				List<Map<String, String>> previewMapList = null;
				for (int fsmId : tplSet)
					try {
						W5FormSmsMail fsm = formResult.getForm().get_formSmsMailMap().get(fsmId);
						if (fsm == null || fsm.getAlarmFlag() != 0
								|| !GenericUtil.hasPartInside2(fsm.getActionTypes(), action)
								|| !GenericUtil.hasPartInside2(fsm.getWebMobileTips(), mobile ? "2" : "1"))
							continue;
						if (!GenericUtil.isEmpty(fsm.getConditionSqlCode())) {
							boolean conditionCheck = dao.conditionRecordExistsCheck(scd, requestParams, t.getTableId(),
									GenericUtil.uInt(ptablePk), fsm.getConditionSqlCode());
							if (!conditionCheck)
								continue;
						}
						Map<String, String> m = new HashMap();
						m.put("_tableId", "" + t.getTableId());
						m.put("_tablePk", ptablePk);
						if (fsm.getPreviewFlag() != 0 && !mobile) { // simdi
																	// gonderilmeyecek,
																	// formda
																	// geri
																	// donecek
							m.put("_fsmId", "" + fsm.getFormSmsMailId());
							m.put("_fsmTip", "" + fsm.getSmsMailType());
							if (previewMapList == null) {
								previewMapList = new ArrayList();
								formResult.setPreviewMapList(previewMapList);
							}
							previewMapList.add(m);
							continue;
						}
						switch (fsm.getSmsMailType()) {
						case 0: // sms
							// parameterMap.get("phone"),parameterMap.get("body")
							// m.putAll(dao.interprateSmsTemplate(fsm, scd,
							// requestParams, t.getTableId(),
							// GenericUtil.uInt(ptablePk)));
							// if(!GenericUtil.isEmpty(m.get("phone")))result.add(new
							// W5QueuedActionHelper(scd,
							// -631, m, (short)1));
							break;
						case 1: // mail
							// W5Email email= new
							// W5Email(parameterMap.get("pmail_to"),parameterMap.get("pmail_cc"),parameterMap.get("pmail_bcc"),parameterMap.get("pmail_subject"),parameterMap.get("pmail_body"),
							// parameterMap.get("pmail_keep_body_original"),
							// fileAttachments);
							W5Email email = dao.interprateMailTemplate(fsm, scd, requestParams, t.getTableId(),
									GenericUtil.uInt(ptablePk));

							email.set_oms(metadataLoader.findObjectMailSetting(scd, email.getMailSettingId()));
							if (fsm.getAsyncFlag() != 0)
								result.add(new W5QueuedActionHelper(email));
							else
								sendMail(scd, email, requestParams);
							break;
						}
					} catch (Exception e) {
						if (FrameworkSetting.debug)
							e.printStackTrace();
						formResult.getOutputMessages()
								.add("CUSTOMIZED SMS/MAIL(" + fsmId + ") EXCEPTION: " + e.getMessage());
					}
			}
		}
	}

	private void extFormFeed(W5FormResult formResult, Map<String, Object> scd, Map<String, String> requestParams,
			W5Table t, String ptablePk, Log5Feed feed, int feedTableId, int feedTablePk) {
		if (FrameworkSetting.feed && FrameworkCache.getAppSettingIntValue(scd, "feed_flag") != 0
				&& t.getShowFeedTip() != 0)
			try {
				// buraya diger yerlerden de gelmesi lazim:
				// dbFunc(email,sms),fileAttach, checkMailz
				// W5Feed feed = null;
				boolean dontSaveFeed = false;
				switch (formResult.getAction()) { // post action'a gore
				case 2: // post action:insert
					switch (t.getTableId()) {
					case 671: // feed: signal:
						feed = new Log5Feed(scd);
						feed.setFeedTip((short) 0); // feed:signal
						feed.setDsc(requestParams.get("dsc"));
						feed.setFeedId(GenericUtil.uInt(ptablePk));
						if (requestParams.get("access_roles") != null || requestParams.get("access_users") != null) {
							W5AccessControlHelper ach = new W5AccessControlHelper(requestParams.get("access_roles"),
									requestParams.get("access_users"));
							feed.set_viewAccessControl(ach);
						}
						dontSaveFeed = true;
						break;
					case 329: // comment:eger wall 'dayse oraya comment olarak
								// koy, ve yukari tasi; aksi
						// halde yeni bir comment koy
						feedTableId = GenericUtil.uInt(requestParams, "table_id");
						feedTablePk = GenericUtil.uInt(requestParams, "table_pk");
						List<Log5Feed> lx = FrameworkCache.wFeeds.get((Integer) scd.get("customizationId"));
						if (lx != null)
							for (int qz = lx.size() - 1; qz >= 0; qz--) {
								Log5Feed lf = lx.get(qz);
								if (lf != null && lf.getTableId() == feedTableId && lf.getTablePk() == feedTablePk
										&& lf.getCustomizationId() == (Integer) scd.get("customizationId")) {
									if (lf.get_tableCommentList() == null)
										lf.set_tableCommentList(new ArrayList());
									W5CommentHelper ch = new W5CommentHelper(scd);
									ch.setDsc(requestParams.get("dsc"));
									lf.get_tableCommentList().add(ch);
									lf.set_commentCount(lf.get_commentCount() + 1);
									lx.add(lf);
									lx.set(qz, null); // basa aliyor, ama
														// isterse parametreik
														// olabailr. basa
														// almadan
									// oldugu yerde
									feed = lf;
									dontSaveFeed = true;
									break;
								}
							}
						if (feed == null) { // demek ki, bulamamis bir yerde
											// simdikileri yapacak
							feed = new Log5Feed(scd);
							feed.setFeedTip((short) 11); // comment
							// feed.setDsc(requestParams.get("dsc"));
							feed.setTableId(feedTableId = GenericUtil.uInt(requestParams, "table_id"));
							feed.setTablePk(feedTablePk = GenericUtil.uInt(requestParams, "table_pk"));
							feed.set_showFeedTip((short) 2); // master ???
																// PromisCache.getTable(scd,
																// feedTableId).getShowFeedTip()
							W5CommentHelper ch = new W5CommentHelper(scd);
							ch.setDsc(requestParams.get("dsc"));
							feed.set_tableCommentList(new ArrayList());
							feed.get_tableCommentList().add(ch);
							break;
						}
						break;
					case 44: // file_attach:TODO
						break;
					default: // diger herseyde
						feed = new Log5Feed(scd);
						feed.set_showFeedTip(t.getShowFeedTip());
						switch (feed.get_showFeedTip()) {
						case 2: // master
							feed.setFeedTip((short) 2); // insert:master icin
							feed.setTableId(feedTableId = t.getTableId());
							feed.setTablePk(feedTablePk = GenericUtil.uInt(ptablePk));
							break;
						case 1: // detail
							feed.setFeedTip((short) 4); // edit:detaya gore
							feed.setDetailTableId(feedTableId = t.getTableId());
							feed.setDetailTablePk(feedTablePk = GenericUtil.uInt(ptablePk));
							break;
						}
					}

					break;
				case 1: // post action:edit
					feed = new Log5Feed(scd);
					feed.set_showFeedTip(t.getShowFeedTip());
					switch (feed.get_showFeedTip()) {
					case 2: // master
						feed.setFeedTip((short) 1); // insert:master icin
						feed.setTableId(feedTableId = t.getTableId());
						feed.setTablePk(feedTablePk = GenericUtil.uInt(ptablePk));
						break;
					case 1: // detail
						feed.setFeedTip((short) 4); // edit:detaya gore
						feed.setDetailTableId(feedTableId = t.getTableId());
						feed.setDetailTablePk(feedTablePk = GenericUtil.uInt(ptablePk));
						break;
					}
					break;
				case 3: // post action:remove:TODO yukarida yapilmasi lazim
					break;
				}
				if (feed != null) {
					if (!dontSaveFeed) { // eger signal degilse
						if (formResult.getAction() != 3 && feedTableId != 0 && feedTablePk != 0) { // detail
																									// icinse
							feed.set_tableRecordList(
									dao.findRecordParentRecords(scd, feedTableId, feedTablePk, 0, true));
							if (feed.get_tableRecordList() != null && feed.get_tableRecordList().size() > 1
									&& feed.get_showFeedTip() == 1) {
								W5TableRecordHelper trh = feed.get_tableRecordList().get(1);
								feed.setTableId(trh.getTableId());
								feed.setTablePk(trh.getTablePk());
								feed.set_commentCount(trh.getCommentCount());
							} else if (feed.get_tableRecordList() != null && feed.get_tableRecordList().size() > 0)
								feed.set_commentCount(feed.get_tableRecordList().get(0).getCommentCount());
						}
						// saveObject(feed);
					}
					FrameworkCache.addFeed(scd, feed, true);
				}

			} catch (Exception e) {
				if (FrameworkSetting.debug)
					e.printStackTrace();
				formResult.getOutputMessages().add("FEED EXCEPTION: " + e.getMessage());
			}
	}

	public Map sendFormSmsMail(Map<String, Object> scd, int formSmsMailId, Map<String, String> requestParams) {
		String projectId = FrameworkCache.getProjectId(scd, null);
		W5FormSmsMail fsm = (W5FormSmsMail) metadataLoader.getMetadataObject(
				"W5FormSmsMail","formSmsMailId", formSmsMailId, projectId,
				"FormSmsMail");
		int tableId = 0;
		W5Table t = null;
		if(fsm.getFormId()> 0) {
			W5Form f = (W5Form) metadataLoader.getMetadataObject("W5Form","formId",
					fsm.getFormId(), projectId, "Form");
			tableId = f.getObjectId();
			t = FrameworkCache.getTable(scd, tableId);
			if (!FrameworkCache.roleAccessControl(scd, 0)) {
				throw new IWBException("security", "Module", 0, null,
						"No Authorization for SMS/Email. Please contact Administrator", null);
			}
		}

		Map r = new HashMap();
		if (fsm.getSmsMailType() == 0) { // sms
			r.put("success", false);
			r.put("error", "SMS Adapter Not Defined");
			return r;
		} else { // email
			int tablePk = 0;
			if(t!=null) {
				tablePk = GenericUtil.uInt(requestParams.get(t.get_tableFieldList().get(0).getDsc()));
				if(tablePk==0)tablePk = GenericUtil.uInt(requestParams.get("table_pk"));
			}
			W5Email email = dao.interprateMailTemplate(fsm, scd, requestParams, tableId,tablePk);

			email.set_oms(metadataLoader.findObjectMailSetting(scd, email.getMailSettingId()));
			String error = sendMail(scd, email, requestParams);
			if (GenericUtil.isEmpty(error)) {
				r.put("success", true);
			} else {
				r.put("success", false);
				r.put("error", error);
			}
		}
		return r;
	}

	
	public String sendMail(Map<String, Object> scd, W5Email email, Map<String, String> requestParams){
		String error = MailUtil.sendMail(scd, email);
		if(FrameworkCache.getTable(scd, FrameworkSetting.customEmailTableId)!=null) {
			Map newRequestMap = new HashMap();
			newRequestMap.putAll(requestParams);
			if(GenericUtil.isEmpty(error)) {
				email.setStatus((short)1);
				newRequestMap.put("send_retry_count", 0);
			} else {
				email.setStatus((short)2);
				newRequestMap.put("last_error_msg", error);
				newRequestMap.put("send_retry_count", 1);
			}
			newRequestMap.putAll(email.get_asHashMap());
			newRequestMap.put("from_user_id", scd.get("userId"));
			W5FormResult fr =crudEngine.postForm4Table(scd, 10235, 2, newRequestMap, "");
			fr = null;
		}
		return error;
	}
}
