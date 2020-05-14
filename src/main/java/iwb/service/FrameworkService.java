package iwb.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.metadata.rdbms.PostgreSQLWriter;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.domain.db.Log5Feed;
import iwb.domain.db.Log5GlobalNextval;
import iwb.domain.db.Log5JobAction;
import iwb.domain.db.Log5Transaction;
import iwb.domain.db.W5FileAttachment;
import iwb.domain.db.W5JobSchedule;
import iwb.domain.db.W5Project;
import iwb.domain.db.W5WorkflowRecord;
import iwb.domain.db.W5WorkflowStep;
import iwb.domain.db.W5WsServer;
import iwb.domain.helper.W5FormCellHelper;
import iwb.domain.helper.W5GridReportHelper;
import iwb.domain.helper.W5QueuedActionHelper;
import iwb.domain.helper.W5ReportCellHelper;
import iwb.domain.result.M5ListResult;
import iwb.domain.result.W5FormResult;
import iwb.domain.result.W5GlobalFuncResult;
import iwb.domain.result.W5PageResult;
import iwb.domain.result.W5QueryResult;
import iwb.domain.result.W5TableRecordInfoResult;
import iwb.engine.AccessControlEngine;
import iwb.engine.CRUDEngine;
import iwb.engine.ConversionEngine;
import iwb.engine.DebugEngine;
import iwb.engine.GlobalScriptEngine;
import iwb.engine.NotificationEngine;
import iwb.engine.QueryEngine;
import iwb.engine.RESTEngine;
import iwb.engine.UIEngine;
import iwb.engine.WorkflowEngine;
import iwb.exception.IWBException;
import iwb.util.DBUtil;
import iwb.util.GenericUtil;
import iwb.util.LogUtil;
import iwb.util.UserUtil;

@Service
@Transactional
public class FrameworkService {
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
	private CRUDEngine crudEngine;

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

	@Lazy
	@Autowired
	private UIEngine uiEngine;

	@Lazy
	@Autowired
	private RESTEngine restEngine;

	@Lazy
	@Autowired
	private DebugEngine debugEngine;

	@Transactional(propagation=Propagation.NEVER)
	public synchronized void reloadCache(int cid) {
		try {
			if (cid == -1)
				FrameworkSetting.systemStatus = 2; // suspended
			// dao.setEngine(this);
			metadataLoader.reloadFrameworkCaches(cid);
		} catch (Exception e) {
			if (FrameworkSetting.debug)
				e.printStackTrace();
		} finally {
			if (cid == -1)
				FrameworkSetting.systemStatus = 0;
		}
	}


	public W5FormResult getFormResultByQuery(Map<String, Object> scd, int formId, int queryId,
			Map<String, String> requestParams) {
		return uiEngine.getFormResultByQuery(scd, formId, queryId, requestParams);
	}

	public W5FormResult getFormResult(Map<String, Object> scd, int formId, int action,
			Map<String, String> requestParams) {
		return uiEngine.getFormResult(scd, formId, action, requestParams);
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private List<W5QueuedActionHelper> postForm4Table(W5FormResult formResult, String paramSuffix,
			Set<String> checkedParentRecords) {
		return crudEngine.postForm4Table(formResult, paramSuffix, checkedParentRecords);
	}



	public W5FormResult postForm4Table(Map<String, Object> scd, int formId, int action,
			Map<String, String> requestParams, String prefix) {
		return crudEngine.postForm4Table(scd, formId, action, requestParams, prefix);
	}

	public List<W5ReportCellHelper> getGridReportResult(Map<String, Object> scd, int gridId, String gridColumns,
			Map<String, String> requestParams) {
		return queryEngine.getGridReportResult(scd, gridId, gridColumns, requestParams);
	}

	private Map<String, Object> executeQuery2Map(Map<String, Object> scd, int queryId,
			Map<String, String> requestParams) {

		return queryEngine.executeQuery2Map(scd, queryId, requestParams);
	}

	public W5QueryResult executeQuery(Map<String, Object> scd, int queryId, Map<String, String> requestParams) {
		return queryEngine.executeQuery(scd, queryId, requestParams);

	}


	public W5PageResult getPageResult(Map<String, Object> scd, int pageId, Map<String, String> requestParams) {
		return uiEngine.getPageResult(scd, pageId, requestParams);
	}


	public W5GlobalFuncResult executeFunc(Map<String, Object> scd, int globalFuncId, Map<String, String> parameterMap,
			short accessSourceType) {
		return scriptEngine.executeGlobalFunc(scd, globalFuncId, parameterMap, accessSourceType);

	}
	

	@Transactional(propagation=Propagation.NEVER)
	public W5GlobalFuncResult executeFuncNT(Map<String, Object> scd, int globalFuncId, Map<String, String> parameterMap,
			short accessSourceType) {
		return scriptEngine.executeGlobalFunc(scd, globalFuncId, parameterMap, accessSourceType);

	}

	public W5FormResult bookmarkForm(Map<String, Object> scd, int formId, int action,
			Map<String, String> parameterMap) {
		W5FormResult formResult = metadataLoader.getFormResult(scd, formId, 2, parameterMap);
		dao.bookmarkForm(scd, parameterMap.get("_dsc"), action > 10 ? -formId : formId, (Integer) scd.get("userId"),
				formResult);

		return formResult;
	}

	public void saveObject(Object o) {
		dao.saveObject(o);
		if (o instanceof W5FileAttachment) { // bununla ilgili islemler
			W5FileAttachment fa = (W5FileAttachment) o;
			if (fa.getTableId() == 336 && fa.getFileTypeId() != null && fa.getFileTypeId() == -999) { // profile
																										// picture
				makeProfilePicture(GenericUtil.uInt(fa.getTablePk()), fa);
			}
		}
	}

	private boolean makeProfilePicture(int userId, W5FileAttachment fa) {
		if (FrameworkSetting.feed && FrameworkCache.getAppSettingIntValue(fa.getCustomizationId(), "feed_flag") != 0) {
			Map scd = new HashMap();
			scd.put("userId", fa.getUploadUserId());
			scd.put("roleId", 2);
			scd.put("customizationId", fa.getCustomizationId());
			scd.put("userTip", 2);
			Log5Feed feed = new Log5Feed(scd);
			feed.set_showFeedTip((short) 1);
			feed.setFeedTip((short) 24); // remove:master icin
			dao.saveObject(feed);
			FrameworkCache.addFeed(scd, feed, true);
		}
		UserUtil.setUserProfilePicture(userId, fa.getFileAttachmentId());

		return dao.executeUpdateSQLQuery("update iwb.w5_user set profile_picture_id=? where user_id=?",
				fa.getFileAttachmentId(), userId) == 1;
	}

	public void updateObject(Object o) {
		dao.updateObject(o);
	}

	public W5FileAttachment loadFile(Map<String, Object> scd, int fileAttachmentId) { // +:fileId,
																						// -:userId
																						// :
																						// Map<String,
																						// Object>
																						// scd,
		if (fileAttachmentId < 0) {
			int newFileAttachmentId = UserUtil.getUserProfilePicture(-fileAttachmentId);
			if (newFileAttachmentId == 0) {
				List l = dao.executeSQLQuery("select t.profile_picture_id from iwb.w5_user t where t.user_id=?",
						-fileAttachmentId);
				if (!GenericUtil.isEmpty(l)) {
					fileAttachmentId = GenericUtil.uInt(l.get(0));
				}
			} else
				fileAttachmentId = newFileAttachmentId;
			if (fileAttachmentId == 1 || fileAttachmentId == 2) {
				W5FileAttachment fa2 = new W5FileAttachment();
				fa2.setFileAttachmentId(fileAttachmentId);
				return fa2;
			}
		}
		if (fileAttachmentId <= 0)
			return null;
		List<W5FileAttachment> fal = dao.find("from W5FileAttachment t where t.fileAttachmentId=?0", fileAttachmentId);
		if (GenericUtil.isEmpty(fal))
			return null;
		W5FileAttachment fa = fal.get(0);
		// if(scd==null){
		scd = new HashMap();
		scd.put("customizationId", fa.getCustomizationId());
		// } else
		// if((Integer)scd.get("customizationId")!=fa.getCustomizationId()){
		// return null;
		// }
		if (fa != null) { // bununla ilgili islemler
			if (fa.getCustomizationId() != GenericUtil.uInt(scd.get("customizationId"))) {
				throw new IWBException("security", "File Attachment", fa.getFileAttachmentId(), null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_file_authorization"), null);
			}
		}
		return fa;
	}

	public W5FormResult postBulkConversion(Map<String, Object> scd, int conversionId, int dirtyCount,
			Map<String, String> requestParams, String prefix) {
		return conversionEngine.postBulkConversion(scd, conversionId, dirtyCount, requestParams, prefix);
	}

	public W5FormResult postEditGrid4Table(Map<String, Object> scd, int formId, int dirtyCount,
			Map<String, String> requestParams, String prefix, Set<String> checkedParentRecords) {
		return crudEngine.postEditGrid4Table(scd, formId, dirtyCount, requestParams, prefix, checkedParentRecords);

	}

	public W5GlobalFuncResult postEditGridGlobalFunc(Map<String, Object> scd, int globalFuncId, int dirtyCount,
			Map<String, String> requestParams, String prefix) {
		return scriptEngine.postEditGridGlobalFunc(scd, globalFuncId, dirtyCount, requestParams, prefix);
	}

	public Map<String, Object> userRoleSelect(int userId, int userRoleId, int customizationId, String projectId,
			String mobileDeviceId) {
		Map<String, Object> scd = new HashMap<String, Object>();
		scd.put("userId", userId);
		scd.put("userRoleId", userRoleId);
		scd.put("customizationId", customizationId);
		scd.put("projectId", FrameworkSetting.devUuid);
		Map<String, String> rm = new HashMap();
		if (!GenericUtil.isEmpty(projectId))
			rm.put("projectId", projectId);
		Map<String, Object> m = executeQuery2Map(scd, 2, rm); // mainSessionQuery
		if (m == null)
			return null;

		return m;
	}

	public Map<String, Object> userRoleSelect4App(W5Project po, int userId, int userRoleId, String mobileDeviceId) {
		Map<String, Object> scd = new HashMap<String, Object>();
		scd.put("userId", userId);
		scd.put("userRoleId", userRoleId);
		scd.put("customizationId", po.getCustomizationId());
		scd.put("projectId", po.getProjectUuid());
		Map<String, String> rm = new HashMap();
		Map<String, Object> m = executeQuery2Map(scd, po.getSessionQueryId(), rm); // mainSessionQuery
		if (m == null)
			return null;
		return m;
	}


	public Map<String, Object> userRoleSelect4App2(W5Project po, int userId, int userRoleId, Map rm) {
		Map<String, Object> scd = new HashMap<String, Object>();
		scd.put("userId", userId);
		scd.put("userRoleId", userRoleId);
		scd.put("customizationId", po.getCustomizationId());
		scd.put("projectId", po.getProjectUuid());
		int sessionQueryId = po.getSessionQueryId();
		if(rm!=null && GenericUtil.uInt(rm.get("sessionQueryId"))!=0)
			sessionQueryId =  GenericUtil.uInt(rm.get("sessionQueryId"));
		Map<String, Object> m = executeQuery2Map(scd, sessionQueryId, rm); // mainSessionQuery
		if (m == null)
			return null;
		return m;
	}
	public Map<String, Object> userSession4Auth(int userId, int customizationId) {
		Map<String, Object> scd = new HashMap<String, Object>();
		scd.put("userId", userId);
		scd.put("customizationId", customizationId);
		Map<String, String> rm = new HashMap();
		return executeQuery2Map(scd, 4546, rm); // auth.SessionQuery
	}

	// TODO: onayda, iade'de, reject'te notification gitsin
	public Map<String, Object> approveRecord(Map<String, Object> scd, int approvalRecordId, int approvalAction,
			Map<String, String> parameterMap) {
		return workflowEngine.approveRecord(scd, approvalRecordId, approvalAction, parameterMap);
	}


	public boolean runJob(W5JobSchedule job) {

		job.set_running(true);
		W5GlobalFuncResult res = null;
		String transactionId =  GenericUtil.getTransactionId();
		if(FrameworkSetting.logType>0)LogUtil.logObject(new Log5Transaction(job.getProjectUuid(), "job", transactionId), true);

		Log5JobAction logJob = new Log5JobAction(job.getJobScheduleId(), job.getProjectUuid(), transactionId);
		try {// fonksiyon çalıştırılacak ise
			Map<String, String> requestParams = new HashMap<String, String>();
			requestParams.put("_trid_", transactionId);

			Map<String, Object> scd = new HashMap<String, Object>();
			W5Project po = FrameworkCache.getProject(job.getProjectUuid());
			scd.put("projectId", job.getProjectUuid());
			scd.put("locale", job.getLocale());
			scd.put("customizationId", po.getCustomizationId());
			scd.put("userRoleId", job.get_userRoleId());
			scd.put("roleId", job.getExecuteRoleId());
			scd.put("userId", job.getExecuteUserId());
			scd.put("administratorFlag", 1);
			res = scriptEngine.executeGlobalFunc(scd, job.getActionDbFuncId(), requestParams, (short) 7);
			if (FrameworkSetting.debug && res.isSuccess()) {
				System.out.println("Scheduled function is executed (funcId=" + job.getActionDbFuncId() + ")");
			}
		} catch (Exception e) {
			if (FrameworkSetting.debug)
				e.printStackTrace();
			logJob.setError(e.getMessage());
			return false;
		} finally {
			job.set_running(false);
			LogUtil.logObject(logJob, false);
		}
		return res.isSuccess();
	}
	
	
	@Transactional(propagation=Propagation.NEVER)
	public boolean runJobNT(W5JobSchedule job) {

		job.set_running(true);
		W5GlobalFuncResult res = null;
		String transactionId =  GenericUtil.getTransactionId();
		if(FrameworkSetting.logType>0)LogUtil.logObject(new Log5Transaction(job.getProjectUuid(), "job", transactionId), true);

		Log5JobAction logJob = new Log5JobAction(job.getJobScheduleId(), job.getProjectUuid(), transactionId);
		try {// fonksiyon çalıştırılacak ise
			Map<String, String> requestParams = new HashMap<String, String>();
			requestParams.put("_trid_", transactionId);

			Map<String, Object> scd = new HashMap<String, Object>();
			W5Project po = FrameworkCache.getProject(job.getProjectUuid());
			scd.put("projectId", job.getProjectUuid());
			scd.put("locale", job.getLocale());
			scd.put("customizationId", po.getCustomizationId());
			scd.put("userRoleId", job.get_userRoleId());
			scd.put("roleId", job.getExecuteRoleId());
			scd.put("userId", job.getExecuteUserId());
			scd.put("administratorFlag", 1);
			res = scriptEngine.executeGlobalFunc(scd, job.getActionDbFuncId(), requestParams, (short) 7);
			if (FrameworkSetting.debug && res.isSuccess()) {
				System.out.println("Scheduled function is executed (funcId=" + job.getActionDbFuncId() + ")");
			}
		} catch (Exception e) {
			if (FrameworkSetting.debug)
				e.printStackTrace();
			logJob.setError(e.getMessage());
			return false;
		} finally {
			job.set_running(false);
			LogUtil.logObject(logJob, false);
		}
		return res.isSuccess();
	}

	public W5TableRecordInfoResult getTableRecordInfo(Map<String, Object> scd, int tableId, int tablePk) {
		// if(t==null)
		W5TableRecordInfoResult result = dao.getTableRecordInfo(scd, tableId, tablePk);
		if (result != null) {
			result.setParentList(dao.findRecordParentRecords(scd, tableId, tablePk, 10, false));
			result.setChildList(dao.findRecordChildRecords(scd, tableId, tablePk));
			return result;
		} else
			return null;
	}

	public W5QueryResult getTableRelationData(Map<String, Object> scd, int tableId, int tablePk, int relId) {
		return dao.getTableRelationData(scd, tableId, tablePk, relId);
	}



	public void checkAlarms(Map<String, Object> scd) {
		if (true)
			return;
		/*
		 * List<W5FormSmsMailAlarm> l = dao.find(
		 * "from W5FormSmsMailAlarm t where t.status=1 AND t.customizationId=? order by t.alarmDttm"
		 * , scd.get("customizationId")); long d = new Date().getTime();
		 * for(W5FormSmsMailAlarm a:l)if(d-a.getAlarmDttm().getTime()>1000*30)try{
		 * scd.put("userId", a.getInsertUserId()); scd.put("userTip", 2); List l2 =
		 * dao.find(
		 * "from W5FormSmsMail x where x.formSmsMailId=? AND x.customizationId=?" ,
		 * a.getFormSmsMailId(), a.getCustomizationId()); W5GlobalFuncResult rdb = null;
		 * if(l2.size()==1){ W5FormSmsMail fsm = (W5FormSmsMail)l2.get(0); Map m = new
		 * HashMap(); m.put("ptable_id", a.getTableId());m.put("ptable_pk",
		 * a.getTablePk()); switch(fsm.getSmsMailTip()){ case 0://sms
		 * //parameterMap.get("phone"),parameterMap.get("body") //
		 * m.putAll(dao.interprateSmsTemplate(fsm, scd, new HashMap(), a.getTableId(),
		 * a.getTablePk())); // rdb = executeFunc(scd, -631, m, (short)1); break; case
		 * 1://mail //W5Email email= new
		 * W5Email(parameterMap.get("pmail_to"),parameterMap.get("pmail_cc"),
		 * parameterMap.get("pmail_bcc"),parameterMap.get("pmail_subject"),
		 * parameterMap.get("pmail_body"), parameterMap.get("pmail_keep_body_original"),
		 * fileAttachments); // m.put("pmail_setting_id",
		 * FrameworkCache.getAppSettingStringValue(scd, "default_outbox_id")); //
		 * m.putAll(dao.interprateMailTemplate(fsm, scd, new HashMap(), a.getTableId(),
		 * a.getTablePk())); // rdb = executeFunc(scd, -650, m, (short)1); break;
		 * default: break; } Log5Notification n = new Log5Notification(a);
		 * n.set_tableRecordList(dao.findRecordParentRecords(scd,a.getTableId(),
		 * a.getTablePk(),0, true)); UserUtil.publishNotification(n, false);
		 * a.setStatus(rdb== null || rdb.isSuccess() ? (short)0 : (short)2); // 0:done,
		 * 1: active, 2:error sending, 3:canceled } else a.setStatus((short)2); }
		 * catch(Exception e) { a.setStatus((short)2); // 0:done, 1: active, 2:error
		 * sending, 3:canceled } finally{ dao.updateObject(a); } else break;
		 */
	}

	public int getSubDomain2CustomizationId(String subDomain) {
		int res = 0;
		try {
			List l = dao.executeSQLQuery("select c.customization_id from iwb.w5_customization c where c.sub_domain=?",
					subDomain);
			if (!GenericUtil.isEmpty(l))
				for (Object o : l) {
					res = GenericUtil.uInt(o);
					break;
				}
		} catch (Exception e) {
			if (FrameworkSetting.debug)
				e.printStackTrace();
		}
		return res;
	}

	public HashMap<String, Object> getUser(int customizationId, int userId) {
		HashMap<String, Object> user = new HashMap<String, Object>();
		List<Object[]> userObject = dao.executeSQLQuery(
				"select x.dsc,x.user_id,x.gsm,x.email from iwb.w5_user x where x.customization_id=? and x.user_id = ?",
				customizationId, userId);
		if (userObject != null && userObject.size() > 0) {
			user.put("dsc", userObject.get(0)[0]);
			user.put("id", userObject.get(0)[1]);
			user.put("gsm", userObject.get(0)[2]);
			user.put("email", userObject.get(0)[3]);
		}
		return user;
	}


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

	public W5FormCellHelper reloadFormCell(Map<String, Object> scd, int fcId, String webPageId, String tabId) {
		return uiEngine.reloadFormCell(scd, fcId, webPageId, tabId);
	}

	public int notifyChatMsgRead(Map<String, Object> scd, int userId, int chatId) {
		int cnt1 = dao.executeUpdateSQLQuery(
				"update iwb.w5_chat set DELIVER_STATUS_TIP=2, DELIVER_DTTM=iwb.fnc_sysdate(?) where RECEIVER_USER_ID=? AND SENDER_USER_ID=? AND DELIVER_STATUS_TIP in (0,1)",
				scd.get("customizationId"), scd.get("userId"), userId);
		return 0;
	}

	public W5FormResult postBulkConversionMulti(Map<String, Object> scd, int conversionCount,
			Map<String, String> parameterMap) {
		return conversionEngine.postBulkConversionMulti(scd, conversionCount, parameterMap);
	}

	public Map sendFormSmsMail(Map<String, Object> scd, int formSmsMailId, Map<String, String> requestParams) {
		return notificationEngine.sendFormSmsMail(scd, formSmsMailId, requestParams);
	}

	public boolean changeActiveProject(Map<String, Object> scd, String projectUuid) {
		return metadataLoader.changeActiveProject(scd, projectUuid);
	}

	@Transactional(propagation=Propagation.NEVER)
	public int getGlobalNextval(String id, String projectUuid, int userId, int customizationId, String remoteAddr) {

		if (FrameworkSetting.log2tsdb) {
			LogUtil.logObject(new Log5GlobalNextval(userId, customizationId, id, remoteAddr, projectUuid), true);
		}

		return metadataLoader.getGlobalNextval(id);
	}

	public boolean organizeTableFields(Map<String, Object> scd, String tableName) {
		dao.checkTenant(scd);
		boolean b = metadataWriter.organizeTable(scd, tableName);
		FrameworkCache.clearPreloadCache(scd);
		return b;
	}

	public void organizeQueryFields(Map<String, Object> scd, int queryId, short insertFlag) {
		dao.checkTenant(scd);
		metadataWriter.organizeQueryFields(scd, queryId, insertFlag);
		FrameworkCache.clearPreloadCache(scd);
	}



	public W5FormResult postFormAsJson(Map<String, Object> scd, int mainFormId, int action, JSONObject mainFormData,
			int detailFormId, JSONArray detailFormData) {
		return crudEngine.postFormAsJson(scd, mainFormId, action, mainFormData, detailFormId, detailFormData);
	}

	public M5ListResult getMListResult(Map<String, Object> scd, int listId, Map<String, String> parameterMap) {
		return uiEngine.getMListResult(scd, listId, parameterMap);
	}

	public Map getUserNotReadChatMap(Map<String, Object> scd) {
		String s = "select k.sender_user_id user_id , count(1) cnt from iwb.w5_chat k where k.receiver_user_id=${scd.userId}::integer AND k.deliver_status_tip in (0,1) "// AND
																																											// k.customization_id=${scd.customizationId}::integer
																																											// "
				+ "AND k.sender_user_id in (select u.user_id from iwb.w5_user u where ((u.customization_id=${scd.customizationId}::integer AND (u.global_flag=1 OR u.project_uuid='${scd.projectId}') AND u.user_status=1)) OR exists(select 1 from iwb.w5_user_related_project rp where rp.user_id=u.user_id AND rp.related_project_uuid='${scd.projectId}'))"
				+ " group by k.sender_user_id";
		Object[] oz = DBUtil.filterExt4SQL(s, scd, null, null);
		List<Object[]> l = dao.executeSQLQuery2(oz[0].toString(), (List) oz[1]);
		Map r = new HashMap();
		if (l != null)
			for (Object[] o : l)
				r.put(o[0], o[1]);
		return r;
	}

	public Map executeQuery4Stat(Map<String, Object> scd, int gridId, Map<String, String> requestParams) {
		requestParams.remove("firstLimit");
		requestParams.remove("limit");
		requestParams.remove("start");
		requestParams.remove("sort");
		return queryEngine.executeQuery4Stat(scd, gridId, requestParams);
	}

	public Map executeQuery4StatTree(Map<String, Object> scd, int gridId, Map<String, String> requestParams) {
		requestParams.remove("firstLimit");
		requestParams.remove("limit");
		requestParams.remove("start");
		requestParams.remove("sort");
		return queryEngine.executeQuery4StatTree(scd, gridId, requestParams);
	}


	public Object executeQuery4Debug(Map<String, Object> scd, int queryId, Map<String, String> requestParams) {
		return debugEngine.executeQuery4Debug(scd, queryId, requestParams);
	}

	public List executeQuery4DataList(Map<String, Object> scd, int tableId, Map<String, String> requestParams) {
		return queryEngine.executeQuery4DataList(scd, tableId, requestParams);
	}

	public List executeQuery4Pivot(Map<String, Object> scd, int tableId, Map<String, String> requestParams) {
		return queryEngine.executeQuery4Pivot(scd, tableId, requestParams);
	}

	public W5GlobalFuncResult executeGlobalFunc4Debug(Map<String, Object> scd, int globalFuncId,
			Map<String, String> parameterMap) {
		return debugEngine.executeGlobalFunc4Debug(scd, globalFuncId, parameterMap);
	}

	public Map<String, Object> getWsServerMethodObjects(W5WsServer wss) {
		return restEngine.getWsServerMethodObjects(wss);

	}

	public Map REST(Map<String, Object> scd, String name, Map requestParams) throws IOException {
		return restEngine.REST(scd, name, requestParams);
	}

	

	

	public Map generateScdFromAuth(int socialCon, String token) {
		List<Object[]> list = dao.executeSQLQuery(
				"select u.user_id, u.customization_id from iwb.w5_user u"
						+ " where u.lkp_auth_external_source=? AND u.user_status=1 AND u.auth_external_id=?",
				socialCon, token);
		if (!GenericUtil.isEmpty(list)) {
			Object[] oz = list.get(0);
			Map<String, Object> scd = userSession4Auth(GenericUtil.uInt(oz[0]), GenericUtil.uInt(oz[1]));
			if (scd != null) {
				dao.executeUpdateSQLQuery(
						"update iwb.w5_user set last_succesful_login_dttm=current_timestamp, succesful_login_count=succesful_login_count+1 where user_id=?",
						scd.get("userId"));
			}
			return scd;
		} else {
			return null;
		}
	}


	public void saveImage(String imageUrl, int userId, int cusId, String projectUuid) {
		try {
			List lf = dao.find(
					"select t.fileAttachmentId from W5FileAttachment t where t.tableId=336 AND t.fileTypeId=-999 AND t.tablePk=?0 AND t.customizationId=?1 AND t.orijinalFileName=?2",
					"" + userId, cusId, imageUrl);
			if (!lf.isEmpty()) {
				if (UserUtil.getUserProfilePicture(userId) == (Integer) lf.get(0))
					return;
			}
			URL url = new URL(imageUrl);
			int length;
			int totalBytesRead = 0;
			InputStream is = url.openStream();
			long fileId = new Date().getTime();
			W5FileAttachment fa = new W5FileAttachment();

			fa.setSystemFileName(fileId + "." + GenericUtil.strUTF2En(FilenameUtils.getBaseName(url.getPath())));
			String testPath = FrameworkCache.getAppSettingStringValue(0, "file_local_path") + File.separator + cusId;
			File f = new File(testPath);

			if (!f.exists()) {
				boolean cDir = new File(testPath).mkdirs();
				boolean aDir = new File(testPath + File.separator + "attachment").mkdirs();
			}

			String filePath = FrameworkCache.getAppSettingStringValue(0, "file_local_path") + File.separator + cusId
					+ File.separator + "attachment" + File.separator + fa.getSystemFileName();

			OutputStream os = new FileOutputStream(filePath);
			byte[] b = new byte[2048];

			while ((length = is.read(b)) != -1) {
				totalBytesRead += length;
				os.write(b, 0, length);
			}
			is.close();
			os.close();

			fa.setCustomizationId(cusId);
			fa.setOrijinalFileName(imageUrl);
			fa.setUploadUserId(userId);
			fa.setFileSize(totalBytesRead);
			fa.setFileTypeId(-999);
			fa.setTabOrder((short) 1);
			fa.setActiveFlag((short) 1);
			fa.setTableId(336);
			fa.setTablePk("" + userId);
			fa.setProjectUuid(projectUuid == null ? FrameworkSetting.devUuid : projectUuid);
			saveObject(fa);

		} catch (Exception io) {
			io.printStackTrace();
		}
	}

	public Map userExists(String email) {
		List<Object[]> list = dao.executeSQLQuery("select u.user_id, u.customization_id, (select min(r.user_role_id)"
				+ " from iwb.w5_user_role r where r.customization_id=u.customization_id AND r.user_id=u.user_id) user_role_id from iwb.w5_user u where "
				+ "u.user_status=1 AND u.auth_external_id=?", email);
		if (!GenericUtil.isEmpty(list)) {
			Object[] oz = list.get(0);
			return userRoleSelect(GenericUtil.uInt(oz[0]), GenericUtil.uInt(oz[2]), GenericUtil.uInt(oz[1]), null,
					null);
		} else {
			return null;
		}
	}

	public void addToProject(int userId, String projectId, String email) {
		metadataWriter.addToProject(userId, projectId, email);
	}

	public Map runTests(Map<String, Object> scd, String testIds, String webPageId) {
		String projectUuid = scd.get("projectId").toString();
		Map m = new HashMap();
		m.put("success", true);
		List<Object[]> l = null;
		if (GenericUtil.isEmpty(testIds))
			l = dao.executeSQLQuery(
					"select x.test_id, x.dsc, x.code from iwb.w5_test x where x.lkp_test_type=0 AND x.project_uuid=? order by x.tab_order ",
					projectUuid);
		else {
			List params = new ArrayList();
			params.add(projectUuid);
			String[] xx = testIds.split(",");
			StringBuilder sql = new StringBuilder();
			sql.append(
					"select x.test_id, x.dsc, x.code from iwb.w5_test x where x.lkp_test_type=0 AND x.project_uuid=? AND x.test_id in(-1");
			for (String s : xx) {
				params.add(GenericUtil.uInt(s));
				sql.append(",?");
			}
			sql.append(") order by x.tab_order ");
			l = dao.executeSQLQuery(sql.toString(), params);
		}

		if (l != null) {
			Map tmp = new HashMap();
			Map msg = null, nt = null;
			if (webPageId != null) {
				msg = new HashMap();
				msg.put("success", true);
				nt = new HashMap();
				msg.put("notification", nt);
			}
			for (Object[] o : l)
				try {
					Object result = scriptEngine.executeScript(scd, tmp, o[2].toString(), tmp, "3243t" + o[0]);
					if (result != null) {
						if (result instanceof Double || result instanceof Integer || result instanceof Float
								|| result instanceof BigDecimal) {
							if (GenericUtil.uInt(result) == 0)
								result = null;
						} else if (result instanceof Boolean) {
							if (!((Boolean) result))
								result = null;
						} else if (result instanceof String) {
							if (((String) result).length() == 0)
								result = null;
						}
					}
					if (result != null) {
						m.put("dsc", o[1]);
						m.put("failId", o[0]);
						m.put("msg", result);
						return m;
					}
					if (webPageId != null) {
						nt.put("_tmpStr", "Passed: " + o[1].toString());
						UserUtil.broadCast(projectUuid, (Integer) scd.get("userId"), (String) scd.get("sessionId"),
								webPageId, msg);
					}
				} catch (Exception e) {
					m.put("dsc", o[1]);
					m.put("failId", o[0]);
					m.put("msg", e.getMessage());
					return m;
				}
		}
		return m;
	}

	public boolean changeChangeProjectStatus(Map<String, Object> scd, String projectUuid, int newStatus) {
		return metadataWriter.changeChangeProjectStatus(scd, projectUuid, newStatus);
	}

	public Map organizeREST(Map<String, Object> scd, String serviceName) {
		return restEngine.organizeREST(scd, serviceName);
	}

	public String getServerDttm() {
		return dao.executeSQLQuery("select to_char(current_timestamp,'dd/mm/yyyy hh24:mi:ss')").get(0).toString();
	}

	public void updateWorkflowEscalatedRecords(W5WorkflowStep step, W5WorkflowStep nextStep) {
		workflowEngine.updateWorkflowEscalatedRecords(step, nextStep);
	}

	public List<W5WorkflowRecord> listWorkflowEscalatedRecords(W5WorkflowStep step) {
		return workflowEngine.listWorkflowEscalatedRecords(step);
	}

	public void updateWorkflowEscalatedRecord(W5WorkflowStep step, W5WorkflowRecord rec) {
		workflowEngine.updateWorkflowEscalatedRecord(step, rec);
	}

	public W5GridReportHelper prepareGridReport(Map<String, Object> scd, int gridId, String gridColumns,
			Map<String, String> requestParams) {
		return queryEngine.prepareGridReport(scd, gridId, gridColumns, requestParams);
	}


	public int buildForm(Map<String, Object> scd, String parameter) {
		return metadataWriter.buildForm(scd, parameter);
	}
	
	public void saveCredentials(int cusId, int userId, String picUrl, String fullName, int socialNet, String email,
			String nickName, List<Map> projects, List<Map> userTips) {
		metadataWriter.saveCredentials(cusId, userId, picUrl, fullName, socialNet, email, nickName, projects, userTips);
		saveImage(picUrl, userId, cusId, null);
	}
}