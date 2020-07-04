/*


 * Created on 07.Nis.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package iwb.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.json.JSONException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import iwb.adapter.ui.ViewAdapter;
import iwb.adapter.ui.ViewMobileAdapter;
import iwb.adapter.ui.extjs.ExtJs3_4;
import iwb.adapter.ui.f7.F7_4;
import iwb.adapter.ui.react.React16;
import iwb.adapter.ui.vue.Vue2;
import iwb.adapter.ui.webix.Webix3_3;
import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.domain.db.Log5UserAction;
import iwb.domain.db.W5FileAttachment;
import iwb.domain.db.W5LookUpDetay;
import iwb.domain.db.W5Project;
import iwb.domain.db.W5Query;
import iwb.domain.db.W5SmsValidCode;
import iwb.domain.helper.W5FormCellHelper;
import iwb.domain.helper.W5QueuedActionHelper;
import iwb.domain.helper.W5QueuedPushMessageHelper;
import iwb.domain.helper.W5ReportCellHelper;
import iwb.domain.result.M5ListResult;
import iwb.domain.result.W5FormResult;
import iwb.domain.result.W5GlobalFuncResult;
import iwb.domain.result.W5PageResult;
import iwb.domain.result.W5QueryResult;
import iwb.domain.result.W5TableRecordInfoResult;
import iwb.exception.IWBException;
import iwb.report.RptExcelRenderer;
import iwb.report.RptPdfRenderer;
import iwb.service.FrameworkService;
import iwb.service.ImportService;
import iwb.timer.Action2Execute;
import iwb.util.EncryptionUtil;
import iwb.util.ExcelUtil;
import iwb.util.GenericUtil;
import iwb.util.MQUtil;
import iwb.util.UserUtil;


@Controller
@RequestMapping("/app")
public class AppController implements InitializingBean {
	private static Logger logger = Logger.getLogger(AppController.class);

	@Autowired
	private FrameworkService service;


	@Autowired
	private ImportService importService;

	@Autowired
	private TaskExecutor taskExecutor;
	

	private ViewAdapter ext3_4;
	private	ViewAdapter	webix3_3;
	private	ViewAdapter	react16;
	private	ViewAdapter	vue2;
	private ViewMobileAdapter f7;
	public static String manPicPath = null;
	public static String womanPicPath = null;
	public static String brokenPicPath = null;

	@Override
	public void afterPropertiesSet() throws Exception {
		ext3_4 = new ExtJs3_4();
		webix3_3 = new Webix3_3();
		f7 = new F7_4();
		react16 = new React16();
		vue2 = new Vue2();
	//	FrameworkCache.activeProjectsStr = "067e6162-3b6f-4ae2-a221-2470b63dff00,29a3d378-3c59-4b5c-8f60-5334e3729959";
	/*	if(FrameworkSetting.projectId!=null) {
			vcsService.icbVCSUpdateSqlAndFields();
			boolean b = vcsService.projectVCSUpdate("067e6162-3b6f-4ae2-a221-2470b63dff00");
			if(b && FrameworkSetting.projectId!=null) {
//				W5Project po = FrameworkCache.getProject(FrameworkSetting.projectId);
				if(FrameworkSetting.projectId.length()==36) {
					boolean clean = GenericUtil.uInt(FrameworkSetting.argMap.get("clean"))!=0;
					if(clean)vcsService.deleteProject(FrameworkSetting.projectId);
					vcsService.projectVCSUpdate(FrameworkSetting.projectId);
				}
			}
		}
		if(FrameworkSetting.log2tsdb)LogUtil.activateInflux4Log();
		if(FrameworkSetting.logType==2)LogUtil.activateMQ4Log();
		
		service.reloadCache(-1);
		// if(PromisSetting.checkLicenseFlag)engine.checkLicences();
		// dao.organizeAudit();
		//service.setJVMProperties(0);
		 
		 */
		try{
			manPicPath = new ClassPathResource("static/ext3.4.1/custom/images/man-64.png").getFile().getPath();
			brokenPicPath = new ClassPathResource("static/ext3.4.1/custom/images/broken-64.png").getFile().getPath();
			womanPicPath = new ClassPathResource("static/images/custom/ppicture/default_woman_mini.png").getFile().getPath();
		} catch(Exception e){}
//		RhinoScript.taskExecutor = this.taskExecutor;
//		if(FrameworkSetting.mq)MQUtil.activateMQs(service, null);
	}
        
	private ViewAdapter getViewAdapter(Map<String, Object> scd, HttpServletRequest request, ViewAdapter defaultRenderer){
		if(GenericUtil.uInt(scd.get("mobile"))!=0)return ext3_4;
		if(request!=null){
			String renderer = request.getParameter("_renderer");
			if(renderer!=null && renderer.equals("ext3_4"))return ext3_4;
			if(renderer!=null && renderer.startsWith("webix"))return webix3_3;
			if(renderer!=null && renderer.equals("react16"))return react16;
			if(renderer!=null && renderer.equals("vue2"))return vue2;
		}
		if(scd!=null){
			String renderer = (String)scd.get("_renderer");
			if(renderer!=null && renderer.equals("ext3_4"))return ext3_4;
			if(renderer!=null && renderer.startsWith("webix"))return webix3_3;			
			if(renderer!=null && renderer.equals("react16"))return react16;
			if(renderer!=null && renderer.equals("vue2"))return vue2;
		}
		return defaultRenderer;
	}
	
	private ViewAdapter getViewAdapter(Map<String, Object> scd, HttpServletRequest request){
		return getViewAdapter(scd, request, ext3_4);
	}
	
	@RequestMapping("/ajaxChangeActiveProject")
	public void hndAjaxChangeActiveProject(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxChangeActiveProject"); 
	    Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
	    String uuid= request.getParameter("_uuid");
	    boolean b = service.changeActiveProject(scd, uuid);
		response.getWriter().write("{\"success\":"+b+", \"customizationId\":"+scd.get("customizationId")+",\"scd\":"+GenericUtil.fromMapToJsonString2Recursive(scd)+"}");
		response.getWriter().close();		
	}
	
	
	
	
	@RequestMapping("/ajaxChangeProjectStatus")
	public void hndAjaxChangeProjectStatus(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxChangeProjectStatus"); 
	    Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
	    String uuid= request.getParameter("project_uuid");
	    boolean b = service.changeChangeProjectStatus(scd, request.getParameter("project_uuid"), GenericUtil.uInt(request, "new_status"));
		response.getWriter().write("{\"success\":"+b+"}");
		response.getWriter().close();		
	}
	
	@RequestMapping("/ajaxRunTest")
	public void hndAjaxRunTest(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("ajaxRunTest"); 
	    Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
	    String testIds= request.getParameter("_tids");
	    Map m = service.runTests(scd, testIds, request.getParameter(".w"));
		response.getWriter().write(GenericUtil.fromMapToJsonString2Recursive(m));
		response.getWriter().close();		
	}
	
	@RequestMapping("/ajaxDebugSyncData")
	public void hndAjaxDebugSyncData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxDebugSyncData");
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		response.setContentType("application/json");
		String projectId = (String) scd.get("projectId");
		Map m = null;
		switch (GenericUtil.uInt(request, "t")) {
		case 0:
			m = UserUtil.getRecordEditMapInfo(projectId);
			break;
		case 1:
			m = UserUtil.getUserMapInfo(projectId);
			break;
		case 2:
			m = UserUtil.getGridSyncMapInfo(projectId);
			break;

		}
		response.getWriter().write(GenericUtil.fromMapToJsonString2Recursive(m));
		response.getWriter().close();
	}



	@RequestMapping("/ajaxChangeChatStatus")
	public void hndAjaxChangeChatStatus(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxChangeChatStatus");
		response.setContentType("application/json");
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		int chatStatusTip = GenericUtil.uInt(request, "chatStatusTip");
		response.getWriter().write("{\"success\":" + UserUtil.updateChatStatus(scd, chatStatusTip) + "}");
		response.getWriter().close();
	}

	@RequestMapping("/ajaxGetTableRelationData")
	public void hndAjaxGetTableRelationData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxGetTableRelationData");
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		int tableId = GenericUtil.uInt(request, "_tb_id");
		int tablePk = GenericUtil.uInt(request, "_tb_pk");
		int relId = GenericUtil.uInt(request, "_rel_id");

		response.setContentType("application/json");
		response.getWriter()
				.write(getViewAdapter(scd, request).serializeQueryData(service.getTableRelationData(scd, tableId, tablePk, relId)).toString());
		response.getWriter().close();
	}
	@RequestMapping("/ajaxQueryData4Stat")
	public void hndAjaxQueryData4Stat(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int gridId = GenericUtil.uInt(request, "_gid");
		if(gridId==0)gridId = -GenericUtil.uInt(request, "_qid");
		logger.info("hndAjaxQueryData4Stat(" + gridId + ")");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		response.setContentType("application/json");
		Map m = service.executeQuery4Stat(scd, gridId, GenericUtil.getParameterMap(request));
		response.getWriter().write(GenericUtil.fromMapToJsonString2Recursive(m));
		response.getWriter().close();
	}
	@RequestMapping("/ajaxQueryData4StatTree")
	public void hndAjaxQueryData4StatTree(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int gridId = GenericUtil.uInt(request, "_gid");
		logger.info("hndAjaxQueryData4StatTree(" + gridId + ")");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		response.setContentType("application/json");
		Map m = service.executeQuery4StatTree(scd, gridId, GenericUtil.getParameterMap(request));
		response.getWriter().write(GenericUtil.fromMapToJsonString2Recursive(m));
		response.getWriter().close();
	}
	public static Map<String, String> mockData = new HashMap();
	@RequestMapping("/ajaxMockData")
	public void hndAjaxMockData(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		response.setContentType("application/json");
		int queryId = GenericUtil.uInt(request, "_qid");
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		W5QueryResult queryResult = service.executeQuery(scd, queryId, GenericUtil.getParameterMap(request));

		response.setContentType("application/json");
		String id = UUID.randomUUID().toString();
		mockData.put(id, getViewAdapter(scd, request).serializeQueryData(queryResult).toString());
		response.getWriter().write("{\"success\":true, \"id\":\""+id+"\"}");
		response.getWriter().close();
	}
	
	@RequestMapping("/ajaxQueryMockData")
	public void hndAjaxMockQueryData(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
		response.setContentType("application/json");
		
		String id=request.getParameter("_id");
		if(GenericUtil.isEmpty(id))id="_";
		String s = mockData.get(id);
		response.getWriter().write(s!=null ? s : "{success:false,error:\"Wrong MockID\"}");
		response.getWriter().close();
	}
	@RequestMapping("/ajaxQueryData")
	public void hndAjaxQueryData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int queryId = GenericUtil.uInt(request, "_qid");
//		JSONObject jo = null;
		Map<String,String> requestMap = GenericUtil.getParameterMap(request);
/*		if(GenericUtil.safeEquals(request.getContentType(),"application/json")){
			JSONObject jo = HttpUtil.getJson(request);
			if(jo.has("_qid"))queryId = jo.getInt("_qid");
			requestMap.putAll(GenericUtil.fromJSONObjectToMap(jo));
		} */
		logger.info("hndAjaxQueryData(" + queryId + ")");
		Map<String, Object> scd = null;
		HttpSession session = request.getSession(false);
		if ((queryId == 1 || queryId == 824) && (session == null || session.getAttribute("scd-dev") == null
				|| ((HashMap<String, String>) session.getAttribute("scd-dev")).size() == 0)) { // select
																							// role
			if (session == null) {
				response.getWriter().write("{\"success\":false,\"error\":\"no_session\"}");
				return;
			}
			scd = new HashMap<String, Object>();
			scd.put("locale", session.getAttribute("locale"));
			scd.put("userId", session.getAttribute("userId"));
			if (GenericUtil.uInt(session.getAttribute("mobile"))!=0)
				scd.put("mobile", session.getAttribute("mobile"));
			scd.put("customizationId", session.getAttribute("customizationId"));
		} else {
			if (queryId == 142) { // online users
				scd = UserUtil.getScd(request, "scd-dev", false);
				W5QueryResult qr = new W5QueryResult(142);
				W5Query q = new W5Query();
				q.setQueryType((short) 0);
				qr.setQuery(q);
				qr.setScd(scd);
				qr.setErrorMap(new HashMap());
				qr.setNewQueryFields(FrameworkCache.cachedOnlineQueryFields);
				List<Object[]> lou = UserUtil.listOnlineUsers(scd);
				if (FrameworkSetting.chatShowAllUsers) {
					Map<Integer, Object[]> slou = new HashMap();
					slou.put((Integer) scd.get("userId"), new Object[] { scd.get("userId") });
					for (Object[] o : lou)
						slou.put(GenericUtil.uInt(o[0]), o);
					W5QueryResult allUsers = service.executeQuery(scd, queryId, requestMap);
					for (Object[] o : allUsers.getData()) {
						String msg = (String) o[6];
						if (msg != null && msg.length() > 18) {
							o[3] = msg.substring(0, 19); // last_msg_date_time
							if (msg.length() > 19)
								o[6] = msg.substring(20);// msg
							else
								o[6] = null;
						} else {
							o[6] = null;
							o[3] = null;
						}

						int u = GenericUtil.uInt(o[0]);

						Object[] o2 = slou.get(u);
						if (o2 == null)
							lou.add(o);
						else if (u != (Integer) scd.get("userId")) {
							if (o2.length > 3)
								o2[3] = o[3];
							if (o2.length > 6)
								o2[6] = o[6];
							if (o2.length > 7)
								o2[7] = o[7];
						}
					}
				}
				qr.setData(lou);
				response.setContentType("application/json");
				response.getWriter().write(getViewAdapter(scd, request).serializeQueryData(qr).toString());
				response.getWriter().close();
				return;
			} else
				scd = UserUtil.getScd(request, "scd-dev", true);// TODO not auto
		}

		ViewAdapter va = getViewAdapter(scd, request);
		if(va instanceof Webix3_3){
			for(String s:requestMap.keySet())if(s.startsWith("sort[") && s.endsWith("]")){
				requestMap.put("sort", s.substring(5,  s.length()-1));
				requestMap.put("dir",requestMap.get(s));
				break;
			}
			
		}
		W5QueryResult queryResult = service.executeQuery(scd, queryId, requestMap);

		response.setContentType("application/json");
		response.getWriter().write(va.serializeQueryData(queryResult).toString());
		response.getWriter().close();
	}

	
	@RequestMapping("/ajaxApproveRecord")
	public void hndAjaxApproveRecord(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxApproveRecord");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		if (FrameworkCache.getAppSettingIntValue(scd, "approval_flag") == 0) {
			response.setContentType("application/json");
			response.getWriter().write("{\"success\":false}");
			return;
		}

		String[] app_rec_ids = request.getParameterValues("_arids");
		Map<String, Object> b = null;
		int approvalAction = GenericUtil.uInt(request, "_aa"); // aprovalAction
		Map<String, String> parameterMap = GenericUtil.getParameterMap(request);

		if (app_rec_ids == null) {
			int approvalRecordId = GenericUtil.uInt(request, "_arid");
			b = service.approveRecord(scd, approvalRecordId, approvalAction, parameterMap);
		} else {
			String[] version_ids = request.getParameterValues("_avnos");
			for (int i = 0; i < app_rec_ids.length; i++) {
				int approvalRecordId = GenericUtil.uInt(app_rec_ids[i]);
				parameterMap.put("_avno", "" + version_ids[i]);
				parameterMap.put("_arid", "" + approvalRecordId); // dbfunc
																	// varsa
																	// parametre
																	// olarak
																	// kullanılıyor
				b = service.approveRecord(scd, approvalRecordId, approvalAction, parameterMap);
			}
		}

		response.setContentType("application/json");
		response.getWriter().write("{\"success\":\"" + b.get("status") + "\"");
		if (b.get("fileHash") != null)
			response.getWriter()
					.write(",\"fileHash\":\"" + b.get("fileHash") + "\",\"fileId\":\"" + b.get("fileId") + "\"");
		response.getWriter().write("}");
		response.getWriter().close();
	}

	@RequestMapping("/ajaxLiveSync")
	public void hndAjaxLiveSync(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("ajaxLiveSync");
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", false);
		response.setContentType("application/json");
		response.getWriter().write("{\"success\":" + FrameworkSetting.liveSyncRecord + "}");
		response.getWriter().close();

		UserUtil.liveSyncAction(scd, GenericUtil.getParameterMap(request));
	}

	@RequestMapping("/ajaxGetTabNotifications")
	public void hndAjaxGetTabNotifications(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxGetTabNotifications");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		String webPageId = request.getParameter(".w");
		String tabId = request.getParameter(".t");
		int userId = (Integer) scd.get("userId");
		String projectId = (String) scd.get("projectId");
		String s = GenericUtil.fromMapToJsonString2Recursive(UserUtil.syncGetTabNotifications(projectId, userId,
				(String) scd.get("sessionId"), webPageId, tabId));
		response.setContentType("application/json");
		response.getWriter().write(s);
		response.getWriter().close();

	}

	@RequestMapping("/ajaxSelectUserRole")
	public void hndAjaxSelectUserRole(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxSelectUserRole");
		HttpSession session = request.getSession(false);
		response.setContentType("application/json");
		int deviceType = GenericUtil.uInt(request.getParameter("_mobile")); //0.web, 1.iphone, 2.android, 3. mobile-web
		if (session == null || (session.getAttribute("userId") == null && session.getAttribute("scd-dev") == null)
				|| (session.getAttribute("scd-dev") == null && !"selectRole".equals(session.getAttribute("waitFor")))) { // sorun
																														// var
			response.getWriter().write("{\"success\":false}"); // tekrar ana login  sayfasina gidecek
			if (session != null)
				session.removeAttribute("scd-dev");
		} else {
			int userId = GenericUtil.uInt(session.getAttribute("scd-dev") == null ? session.getAttribute("userId")
					: ((Map) session.getAttribute("scd-dev")).get("userId"));
			int customizationId = GenericUtil.uInt(session.getAttribute("scd-dev") == null ? session.getAttribute("customizationId")
							: ((Map) session.getAttribute("scd-dev")).get("customizationId"));
			Map<String, Object> oldScd = (Map<String, Object>)session.getAttribute("scd-dev"); 
			Map<String, Object> scd = service.userRoleSelect(userId, GenericUtil.uInt(request, "userRoleId"),
					customizationId, request.getParameter("projectId"), deviceType != 0 ? request.getParameter("_mobile_device_id") : null);
			if (scd == null) {
				response.getWriter().write("{\"success\":false}"); // bir hata
																	// var
				session.removeAttribute("scd-dev");
			} else {
				scd.put("locale", oldScd == null ? session.getAttribute("locale"): oldScd.get("locale"));
				UserUtil.removeUserSession((Integer) scd.get("userId"), session.getId());
				session.removeAttribute("scd-dev");
				if (FrameworkCache.getAppSettingIntValue(0, "interactive_tutorial_flag") != 0) {
					String ws = (String) scd.get("widgetIds");
					if (ws == null)
						scd.put("widgetIds", "10");
					else if (!GenericUtil.hasPartInside(ws, "10"))
						scd.put("widgetIds", ws + ",10");
				}
				session = request.getSession(true);
				scd.put("sessionId", session.getId());
				if(deviceType!=0){
					scd.put("mobile", deviceType);
					scd.put("mobileDeviceId", request.getParameter("_mobile_device_id"));
				}
				
				if(GenericUtil.uInt(scd.get("renderer"))>1)scd.put("_renderer",GenericUtil.getRenderer(scd.get("renderer")));
				session.setAttribute("scd-dev", scd);
				UserUtil.onlineUserLogin(scd, request.getRemoteAddr(), session.getId(), (short) deviceType, deviceType != 0 ? request.getParameter("_mobile_device_id") : request.getParameter(".w"));
				response.getWriter().write("{\"success\":true"); // hersey duzgun
				if(GenericUtil.uInt(request, "c")!=0){
					response.getWriter().write(",\"newMsgCnt\":"+ GenericUtil.fromMapToJsonString2Recursive(service.getUserNotReadChatMap(scd)));
				}
				if(GenericUtil.uInt(request, "d")!=0){
					response.getWriter().write(",\"session\":"+ GenericUtil.fromMapToJsonString2Recursive(scd));
				}
				response.getWriter().write("}");
			}
		}
		response.getWriter().close();

	}

	@RequestMapping("/ajaxChangePassword")
	public void hndAjaxChangePassword(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxChangePassword");
		HttpSession session = request.getSession(false);
		Map<String, Object> scd = null;
		response.setContentType("application/json");
		if (session != null && session.getAttribute("scd-dev") == null && session.getAttribute("userId") != null) {
			scd = new HashMap<String, Object>();
			if (!"expirePassword".equals(session.getAttribute("waitFor"))) {
				response.getWriter().write(
						"{\"success\":false,\"errrorMsg\":\"Waiting 4: " + session.getAttribute("waitFor") + "\"}");
				return;
			}
			scd.put("userId", session.getAttribute("userId"));
			scd.put("locale", session.getAttribute("locale"));
			scd.put("customizationId", session.getAttribute("customizationId"));
		} else
			scd = UserUtil.getScd(request, "scd-dev", true);
		Map<String, String> requestParams = GenericUtil.getParameterMap(request);
		requestParams.put("_remote_ip", request.getRemoteAddr());
		W5GlobalFuncResult result = service.executeFunc(scd, 250, requestParams, (short) 7);
		boolean success = GenericUtil.uInt(result.getResultMap().get("success")) != 0;
		String errorMsg = (String)result.getResultMap().get("errorMsg");
		if (!success)
			errorMsg = LocaleMsgCache.get2(0, GenericUtil.uStrNvl((String) scd.get("locale"),
					FrameworkCache.getAppSettingStringValue(0, "locale")), errorMsg);

		response.setContentType("application/json");
		if (success) { // basarili simdi sira diger islerde
			if (session.getAttribute("scd-dev") == null) {
				session.setAttribute("waitFor", "selectRole");
			}
			response.getWriter().write("{\"success\":true}");
		} else {
			response.getWriter().write("{\"success\":false,\"errrorMsg\":\"" + errorMsg + "\"}");
		}
		response.getWriter().close();
	}



	@RequestMapping("/ajaxAuthenticateUser")
	public void hndAjaxAuthenticateUser(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxAuthenticateUser(" + request.getParameter("userName") + ")");

		// System.out.println(request.getParameter("userName")+" "+"
		// "+request.getParameter("passWord")+"
		// "+request.getParameter("locale"));

		Map<String, String> requestParams = GenericUtil.getParameterMap(request);
		requestParams.put("_remote_ip", request.getRemoteAddr());
	/*	if (request.getSession(false) != null && request.getSession(false).getAttribute("securityWordId") != null)
			requestParams.put("securityWordId", request.getSession(false).getAttribute("securityWordId").toString());
*/
		if (request.getSession(false) != null) {
			request.getSession(false).removeAttribute("scd-dev");
		}

		Map<String, Object> scd = new HashMap();
		scd.put("projectId", FrameworkSetting.devUuid);
		W5GlobalFuncResult result = service.executeFunc(scd, 1, requestParams, (short) 7); // user Authenticate DbFunc:1
		scd = null;
		/*
		 * 4 success 5 errorMsg 6 userId 7 expireFlag 8 smsFlag 9 roleCount
		 */
		boolean success = GenericUtil.uInt(result.getResultMap().get("success")) != 0;
		String errorMsg = (String)result.getResultMap().get("errorMsg");
		int userId = GenericUtil.uInt(result.getResultMap().get("userId"));
		boolean expireFlag = GenericUtil.uInt(result.getResultMap().get("expireFlag")) != 0;
		boolean smsFlag = GenericUtil.uInt(result.getResultMap().get("smsFlag")) != 0;
		int roleCount = GenericUtil.uInt(result.getResultMap().get("roleCount"));
		String xlocale = GenericUtil.uStrNvl(request.getParameter("locale"),
				FrameworkCache.getAppSettingStringValue(0, "locale", "en"));
		int deviceType = GenericUtil.uInt(request.getParameter("_mobile"));
		if (!success)
			errorMsg = LocaleMsgCache.get2(0, xlocale, errorMsg);
		int forceUserRoleId = GenericUtil.uInt(requestParams.get("userRoleId"));
		response.setContentType("application/json");
		boolean genToken = GenericUtil.uInt(request, "generate_token") != 0;
		if (success) { // basarili simdi sira diger islerde
			HttpSession session = request.getSession(true);
			session.setAttribute("locale", xlocale);
			if(deviceType!=0)session.setAttribute("mobile", deviceType);
			session.setAttribute("forceUserRoleId", forceUserRoleId);
			// session.setAttribute("customizationId",
			// GenericUtil.uInt(result.getResultMap().get("customizationId")));
			int customizationId = GenericUtil.uInt(result.getResultMap().get("customizationId"));
			session.setAttribute("customizationId", customizationId);
			if (smsFlag) {
				session.setAttribute("userId", userId);
				session.setAttribute("waitFor", "sms");
				W5SmsValidCode c = new W5SmsValidCode();
				c.setCustomizationId(customizationId);
				c.setUserId(userId);
				c.setSmsCode(GenericUtil.smsCodeGenerator(
						FrameworkCache.getAppSettingIntValue(customizationId,
								"sms_validation_code_type"),
						FrameworkCache.getAppSettingIntValue(customizationId,
								"sms_validation_code_length")));
				service.saveObject(c);

				// SMS Gönderme İşlemi //
				HashMap<String, Object> user = service.getUser(customizationId, userId);
				String messageBody = LocaleMsgCache.get2(customizationId, xlocale, "mobil_onay_kodu") + ": " + c.getSmsCode();

				service.sendSms(customizationId, userId, user.get("gsm") + "", messageBody, 1197, c.getSmsValidCodeId());
				/////////////////////////

				response.getWriter()
						.write("{\"success\":true,\"smsFlag\":true,\"smsValidationId\":" + c.getSmsValidCodeId());
			} else if (expireFlag) {
				session.setAttribute("userId", userId);
				session.setAttribute("waitFor", "expirePassword");
				response.getWriter()
						.write("{\"success\":true,\"expireFlag\":true,\"roleCount\":" + roleCount
								+ ",\"defaultUserCustomizationId\":"
								+ GenericUtil.uInt(result.getResultMap().get("defaultUserCustomizationId")));
			} else if (roleCount < 0 || forceUserRoleId != 0) { // simdi rolunu
																// sec ve login
																// ol
				if (forceUserRoleId == 0)
					forceUserRoleId = -roleCount;
				scd = service.userRoleSelect(userId, forceUserRoleId,
						customizationId, requestParams.get("projectId"), deviceType != 0 ? request.getParameter("_mobile_device_id") : null);
				if (scd == null) {
					if (FrameworkSetting.debug)
						logger.info("empty scd");
					response.getWriter().write("{\"success\":false"); // bir
																		// hata
																		// var
					session.removeAttribute("scd-dev");
				} else {
					scd.put("locale", session.getAttribute("locale"));
					session.removeAttribute("scd-dev");
					session = request.getSession(true);

					if(GenericUtil.uInt(scd.get("renderer"))>1)scd.put("_renderer",GenericUtil.getRenderer(scd.get("renderer")));
					scd.put("sessionId", session.getId());
					session.setAttribute("scd-dev", scd);
					if (deviceType != 0) {
						session.setMaxInactiveInterval(FrameworkCache.getAppSettingIntValue(0, "mobile_session_timeout", 1 * 60) * 60); // 1 saat default
						scd.put("mobileDeviceId", request.getParameter("_mobile_device_id"));
						scd.put("mobile", deviceType);
					}
					scd.put("sessionId", session.getId());
					if(request.getParameter("projectId")!=null)scd.put("projectId", request.getParameter("projectId"));

					UserUtil.onlineUserLogin(scd, request.getRemoteAddr(), session.getId(), (short) deviceType, deviceType != 0 ? request.getParameter("_mobile_device_id") : request.getParameter(".w"));
					response.getWriter().write("{\"success\":true,\"session\":" + GenericUtil.fromMapToJsonString2(scd)); // hersey duzgun
				}
			} else {
				// o zaman once role'u sececek
				/*
				 * if
				 * (GenericUtil.userLoginControl(userId,request.getRemoteAddr(),
				 * request.getSession().getId(),GenericUtil.uInt(requestParams.
				 * get("customizationId")))==false){ response.getWriter().write(
				 * "{\"success\":true,\"loginUserUnique\":true}"); } else
				 */ {
					session.setAttribute("userId", userId);
					session.setAttribute("waitFor", "selectRole");
					response.getWriter().write("{\"success\":true,\"roleCount\":" + roleCount + ",\"defaultUserCustomizationId\":"
									+ GenericUtil.uInt(result.getResultMap().get("defaultUserCustomizationId")));
					// GenericUtil.onlineUserLogin();
					/*
					 * List l=new ArrayList<Object>(); l.add((String)
					 * requestParams.get("userName")); l.add( new Date());
					 * l.add(request.getRemoteAddr());
					 * l.add(request.getSession().getId());
					 * GenericUtil.lastUserAction.put((String)
					 * requestParams.get("userName"),l);
					 */
				}
			}

			if(false && GenericUtil.uInt(request, "c")!=0){
				response.getWriter().write(",\"newMsgCnt\":"+ GenericUtil.fromMapToJsonString2Recursive(service.getUserNotReadChatMap(scd)));
			}
			if (genToken && scd != null)
				response.getWriter().write(",\"tokenKey\":\""
						+ EncryptionUtil.encryptAES(GenericUtil.fromMapToJsonString2Recursive(scd)) + "\"");
			response.getWriter().write("}");
		} else {
			response.getWriter().write("{\"success\":false,\"errorMsg\":\"" + errorMsg + "\"}");
		}
		response.getWriter().close();
	}

	@RequestMapping("/reloadCache")
	public void hndReloadCache(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		
		logger.info("hndReloadCache");
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		/*
		 * bus.reloadCache(GenericUtil.uInt(scd.get("customizationId")));
		 * response.getWriter().write("{\"success\":true}");
		 * response.getWriter().close();
		 */
		// bus.daoReloadJobsCache();
		response.setContentType("application/json");
		int roleId = (Integer) scd.get("roleId");
		if (roleId == 0 || roleId == 2) {
			service.reloadCache(GenericUtil.uInt(scd.get("customizationId")));
			response.getWriter().write("{\"success\":true}");
/*			if(FrameworkSetting.mq)try{
				String projectUuid = "067e6162-3b6f-4ae2-a221-2470b63dff00";
				FrameworkCache.wProjects.get(projectUuid).get_mqChannel().basicPublish(projectUuid, "", null, ("iwb:69,0"+projectUuid+","+FrameworkSetting.instanceUuid).getBytes());
			}catch (Exception e) {
			}*/
		} else
			response.getWriter().write("{\"success\":false}");
		response.getWriter().close();
	}


	@RequestMapping("/ajaxPostChatMsg")
	public void hndAjaxPostChatMsg(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		logger.info("hndAjaxPostChatMsg");
		response.setContentType("application/json");
		String msg = request.getParameter("msg");
		int userId = GenericUtil.uInt(request, "receiver_user_id");
		if (userId == 0 || GenericUtil.isEmpty(msg)) {
			response.getWriter().write("{\"success\":false}");
			return;
		}
		Map<String, String> m = GenericUtil.getParameterMap(request);
		String s = m.get("msg");
		if (GenericUtil.uInt(scd.get("mobile")) == 2)
			s = GenericUtil.encodeGetParamsToUTF8(s);// hack for android mobile app
		m.put("msg", s.contains("\\") ? s.replace('\\', '/') : s);
		W5FormResult formResult = service.postForm4Table(scd, 1703, 2, m, "");

		response.setContentType("application/json");
		if (!GenericUtil.isEmpty(formResult.getErrorMap())) {
			response.getWriter().write("{\"success\":false}");
			response.getWriter().close();
			return;
		}

		Object chatId = formResult.getOutputFields().get("chat_id");
		List<W5QueuedPushMessageHelper> l = UserUtil.publishUserChatMsg(
				(Integer) scd.get("userId"), userId, msg, chatId);
		response.getWriter().write("{\"success\":true, \"delivered_cnt\":1, \"chatId\":"+chatId+"}");
		response.getWriter().close();
		
//		if(FrameworkSetting.mq)UserUtil.mqPublishUserChatMsg(scd, userId, msg, chatId);
		/*
		 * if(!GenericUtil.isEmpty(l)){ executeQueuedMobilePushMessage eqf = new
		 * executeQueuedMobilePushMessage(l); taskExecutor.execute(eqf); }
		 */
	}

	@RequestMapping("/ajaxNotifyChatMsgRead")
	public void hndAjaxNotifyChatMsgRead(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		logger.info("hndAjaxNotifyChatMsgRead");
		int userId = GenericUtil.uInt(request, "u");
		int msgId = GenericUtil.uInt(request, "m");
		if (userId == 0 || msgId == 0) {
			response.getWriter().write("{\"success\":false}");
			return;
		}
		int countLeft = service.notifyChatMsgRead(scd, userId, msgId);

		response.setContentType("application/json");
		response.getWriter().write("{\"success\":true, \"countLeft\":" + countLeft + "}");
		response.getWriter().close();

		if (countLeft == 0) {
			UserUtil.publishUserChatMsgRead(scd, userId, msgId);
		}
	}

	@RequestMapping("/ajaxPostForm")
	public void hndAjaxPostForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int formId = GenericUtil.uInt(request, "_fid");
		logger.info("hndAjaxPostForm(" + formId + ")");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		int action = GenericUtil.uInt(request, "a");
		Map<String,String> requestMap = GenericUtil.getParameterMap(request);
/*		if(GenericUtil.safeEquals(request.getContentType(),"application/json")){
			JSONObject jo = HttpUtil.getJson(request);
			requestMap.putAll(GenericUtil.fromJSONObjectToMap(jo));
		}*/
		W5FormResult formResult = service.postForm4Table(scd, formId, action, requestMap, "");

		response.setContentType("application/json");
		response.getWriter().write(getViewAdapter(scd, request).serializePostForm(formResult).toString());
		response.getWriter().close();
		
		if (formResult.getQueueActionList() != null)
			for (W5QueuedActionHelper o : formResult.getQueueActionList()) {
				Action2Execute eqf = new Action2Execute(o, scd);
				taskExecutor.execute(eqf);
			}

		
		if (formResult.getErrorMap().isEmpty()){
			UserUtil.syncAfterPostFormAll(formResult.getListSyncAfterPostHelper());
//			UserUtil.mqSyncAfterPostFormAll(formResult.getScd(), formResult.getListSyncAfterPostHelper());
		}
	}
/*

	@RequestMapping("/ajaxPostFormBulkUpdate")
	public void hndAjaxPostFormBulkUpdate(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxPostFormBulkUpdate");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		response.setContentType("application/json");
		int formId = GenericUtil.uInt(request, "_fid");
		if (formId > 0) {
			W5FormResult formResult = engine.postBulkUpdate4Table(scd, formId, GenericUtil.getParameterMap(request));

			for (W5QueuedActionHelper o : formResult.getQueueActionList()) {
				Action2Execute eqf = new Action2Execute(o, scd);
				taskExecutor.execute(eqf);
			}
			response.getWriter().write(getViewAdapter(scd, request).serializePostForm(formResult).toString());
			response.getWriter().close();

			if (formResult.getErrorMap().isEmpty()){
				UserUtil.syncAfterPostFormAll(formResult.getListSyncAfterPostHelper());
//				UserUtil.mqSyncAfterPostFormAll(formResult.getScd(), formResult.getListSyncAfterPostHelper());
			}

		} else {
			int smsMailId = GenericUtil.uInt(request, "_smsMailId");
			W5DbFuncResult dbFuncResult = engine.postBulkSmsMail4Table(scd, smsMailId,
					GenericUtil.getParameterMap(request));
			response.getWriter().write(getViewAdapter(scd, request).serializeDbFunc(dbFuncResult).toString());
		}

	}

	@RequestMapping("/ajaxQueryData4BulkUpdate")
	public void hndAjaxQueryData4BulkUpdate(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxQueryData4BulkUpdate");
		int formId = GenericUtil.uInt(request, "_fid");
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		W5QueryResult queryResult = engine.executeQuery4BulkUpdate(scd, formId, GenericUtil.getParameterMap(request),
				false);

		response.setContentType("application/json");
		response.getWriter().write(getViewAdapter(scd, request).serializeQueryData(queryResult).toString());
		response.getWriter().close();
	}
*/
	@RequestMapping("/ajaxPing")
	public void hndAjaxPing(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxPing");
		HttpSession session = request.getSession(false);
		boolean notSessionFlag = session == null || session.getAttribute("scd-dev") == null
				|| ((HashMap<String, String>) session.getAttribute("scd-dev")).size() == 0;
		response.setContentType("application/json");
		Map cm = null;
		if(FrameworkSetting.chat && !notSessionFlag && GenericUtil.uInt(request, "c")!=0){
			cm = service.getUserNotReadChatMap((Map)session.getAttribute("scd-dev"));
		}
		if(GenericUtil.uInt(request, "d")==0 || notSessionFlag)
			response.getWriter().write("{\"success\":true,\"version\":\"v2\",\"session\":" + !notSessionFlag + (cm!=null ? ", \"newMsgCnt\":"+GenericUtil.fromMapToJsonString2Recursive(cm):"") + "}");
		else {
			Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
			response.getWriter().write("{\"success\":true,\"version\":\"v2\",\"session\":" + GenericUtil.fromMapToJsonString2Recursive(scd) + (cm!=null ? ", \"newMsgCnt\":"+GenericUtil.fromMapToJsonString2Recursive(cm):"") + "}");
		}
		response.getWriter().close();
	}

	@RequestMapping("/ajaxPostConversionGridMulti")
	public void hndAjaxPostConversionGridMulti(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxPostConversionGridMulti");
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		response.setContentType("application/json");
		int conversionCount = GenericUtil.uInt(request, "_ccnt");
		if (conversionCount > 0) {
			W5FormResult formResult = service.postBulkConversionMulti(scd, conversionCount,
					GenericUtil.getParameterMap(request));

			response.getWriter().write(getViewAdapter(scd, request).serializePostForm(formResult).toString());
			response.getWriter().close();

			for (W5QueuedActionHelper o : formResult.getQueueActionList()) {
				Action2Execute eqf = new Action2Execute(o, scd);
				taskExecutor.execute(eqf);
			}
			
			if (formResult.getErrorMap().isEmpty()){
				UserUtil.syncAfterPostFormAll(formResult.getListSyncAfterPostHelper());
//				UserUtil.mqSyncAfterPostFormAll(formResult.getScd(), formResult.getListSyncAfterPostHelper());
			}
		} else
			response.getWriter().write("{\"success\":false}");
	}

	@RequestMapping("/ajaxPostEditGrid")
	public void hndAjaxPostEditGrid(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxPostEditGrid");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		response.setContentType("application/json");
		int dirtyCount = GenericUtil.uInt(request, "_cnt");
		int formId = GenericUtil.uInt(request, "_fid");
		if (formId > 0) {
			W5FormResult formResult = service.postEditGrid4Table(scd, formId, dirtyCount,
					GenericUtil.getParameterMap(request), "", new HashSet<String>());
			response.getWriter().write(getViewAdapter(scd, request).serializePostForm(formResult).toString());
			response.getWriter().close();

			for (W5QueuedActionHelper o : formResult.getQueueActionList()) {
				Action2Execute eqf = new Action2Execute(o, scd);
				taskExecutor.execute(eqf);
			}

			if (formResult.getErrorMap().isEmpty()){
				UserUtil.syncAfterPostFormAll(formResult.getListSyncAfterPostHelper());
//				UserUtil.mqSyncAfterPostFormAll(formResult.getScd(), formResult.getListSyncAfterPostHelper());
				
			}

		} else if (formId < 0) { // negatifse direk -globalFuncId
			// int globalFuncId= GenericUtil.uInt(request, "_did");
			W5GlobalFuncResult dbFuncResult = service.postEditGridGlobalFunc(scd, -formId, dirtyCount,
					GenericUtil.getParameterMap(request), "");
			response.getWriter().write(getViewAdapter(scd, request).serializeGlobalFunc(dbFuncResult).toString());
		} else {
			int conversionId = GenericUtil.uInt(request, "_cnvId");
			if (conversionId > 0) {
				W5FormResult formResult = service.postBulkConversion(scd, conversionId, dirtyCount,
						GenericUtil.getParameterMap(request), "");
				response.getWriter().write(getViewAdapter(scd, request).serializePostForm(formResult).toString());
				response.getWriter().close();

				for (W5QueuedActionHelper o : formResult.getQueueActionList()) {
					Action2Execute eqf = new Action2Execute(o , scd);
					taskExecutor.execute(eqf);
				}
				
				if (formResult.getErrorMap().isEmpty()){
					UserUtil.syncAfterPostFormAll(formResult.getListSyncAfterPostHelper());
//					UserUtil.mqSyncAfterPostFormAll(formResult.getScd(), formResult.getListSyncAfterPostHelper());
				}

			} else {
				response.getWriter().write("{\"success\":false}");
			}
		}
	}

	@RequestMapping("/ajaxBookmarkForm")
	public void hndAjaxBookmarkForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxBookmarkForm");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		int formId = GenericUtil.uInt(request, "_fid");
		int action = GenericUtil.uInt(request, "a");
		W5FormResult formResult = service.bookmarkForm(scd, formId, action, GenericUtil.getParameterMap(request));

		response.setContentType("application/json");
		response.getWriter().write("{\"success\":true,\"id\":" + formResult.getPkFields().get("id") + "}");

	}

	@RequestMapping("/ajaxExecDbFunc")
	public void hndAjaxExecDbFunc(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxExecDbFunc");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		int globalFuncId = GenericUtil.uInt(request, "_did"); // +:globalFuncId,
															// -:formId
		if (globalFuncId == 0) {
			globalFuncId = -GenericUtil.uInt(request, "_fid"); // +:globalFuncId,
															// -:formId
		}
		
		response.setContentType("application/json");
		if(globalFuncId==-1){
			if((Integer)scd.get("roleId")!=0)
				throw new IWBException("security","System DbProc", globalFuncId, null, "Only for developers", null);
			service.organizeQueryFields(scd, GenericUtil.uInt(request,("queryId")), (short)GenericUtil.uInt(request,("insertFlag")));
			response.getWriter().write("{\"success\":true}");
		} else {
			W5GlobalFuncResult dbFuncResult = GenericUtil.uInt(request, "_notran")==0 ? 
					service.executeFunc(scd, globalFuncId, GenericUtil.getParameterMap(request), (short) 1) 
					: service.executeFuncNT(scd, globalFuncId, GenericUtil.getParameterMap(request),
							(short) 1);; //request
			response.getWriter().write(getViewAdapter(scd, request).serializeGlobalFunc(dbFuncResult).toString());
		}

		response.getWriter().close();
	}

	

	@RequestMapping("/ajaxGetFormSimple")
	public void hndGetFormSimple(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int formId = GenericUtil.uInt(request, "_fid");
		logger.info("hndGetFormSimple(" + formId + ")");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		int action = GenericUtil.uInt(request, "a");
		W5FormResult formResult = service.getFormResult(scd, formId, action, GenericUtil.getParameterMap(request));

		response.setContentType("application/json");
		response.getWriter().write(getViewAdapter(scd, request).serializeGetFormSimple(formResult).toString());
		response.getWriter().close();
	}

	@RequestMapping("/ajaxReloadFormCell")
	public void hndAjaxReloadFormCell(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxReloadFormCell");
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		int fcId = GenericUtil.uInt(request, "_fcid");
		String webPageId = request.getParameter(".w");
		String tabId = request.getParameter(".t");
		W5FormCellHelper rc = service.reloadFormCell(scd, fcId, webPageId, tabId);
		response.setContentType("application/json");
		response.getWriter()
				.write(ext3_4
						.serializeFormCellStore(rc, (Integer) scd.get("customizationId"), (String) scd.get("locale"))
						.toString());
		response.getWriter().close();
	}



	@RequestMapping("/ajaxFeed")
	public void hndAjaxFeed(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxFeed");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		response.setContentType("application/json");

		int platestFeedIndex = request.getParameter("_lfi") == null ? -1 : GenericUtil.uInt(request, "_lfi");
		int pfeedTip = request.getParameter("_ft") == null ? -1 : GenericUtil.uInt(request, "_ft");
		int proleId = request.getParameter("_ri") == null ? -1 : GenericUtil.uInt(request, "_ri");
		int puserId = request.getParameter("_ui") == null ? -1 : GenericUtil.uInt(request, "_ui");
		int pmoduleId = request.getParameter("_mi") == null ? -1 : GenericUtil.uInt(request, "_mi");
		// response.setContentType("application/json");
		response.getWriter()
				.write(getViewAdapter(scd, request).serializeFeeds(scd, platestFeedIndex, pfeedTip, proleId, puserId, pmoduleId).toString());
		response.getWriter().close();
		if (FrameworkSetting.liveSyncRecord) {
			UserUtil.getTableGridFormCellCachedKeys((String) scd.get("projectId"),
					/* mainTable.getTableId() */ 671, (Integer) scd.get("userId"), (String) scd.get("sessionId"),
					request.getParameter(".w"), request.getParameter(".t"), /* grdOrFcId */ 919, null, true);
		}
	}
	

	@RequestMapping("/ajaxActivateMq")
	public void hndAjaxActivateMq(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxActivateMq");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		response.setContentType("application/json");

		if(FrameworkSetting.mq)MQUtil.activateMQs(service, (String)scd.get("projectId"), GenericUtil.uInt(request, "d")!=0);
		
		// response.setContentType("application/json");
		response.getWriter().write("{\"success\":true}");
		response.getWriter().close();
	}
	


	@RequestMapping("/showForm")
	public void hndShowForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int formId = GenericUtil.uInt(request, "_fid");
		logger.info("hndShowForm(" + formId + ")");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		int action = GenericUtil.uInt(request, "a");
		W5FormResult formResult = service.getFormResult(scd, formId, action, GenericUtil.getParameterMap(request));

		response.setContentType("application/json");
		response.getWriter().write(getViewAdapter(scd, request).serializeShowForm(formResult).toString());
		response.getWriter().close();
	}
	
	@RequestMapping("/showMForm")
	public void hndShowMForm(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int formId = GenericUtil.uInt(request, "_fid");
		logger.info("hndShowMForm(" + formId + ")");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		int action = GenericUtil.uInt(request, "a");
		W5FormResult formResult = service.getFormResult(scd, formId, action, GenericUtil.getParameterMap(request));

		response.setContentType("application/json");
		response.getWriter().write(f7.serializeGetForm(formResult).toString());
		response.getWriter().close();
	}



	@RequestMapping("/ajaxLogoutUser")
	public void hndAjaxLogoutUser(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxLogoutUser");
		HttpSession session = request.getSession(false);
		response.setContentType("application/json");
		if (session != null) {
			Map<String, Object> scd = (Map) session.getAttribute("scd-dev");
			if (scd != null) {
				UserUtil.onlineUserLogout((Integer) scd.get("userId"), scd.containsKey("mobile") ? (String)scd.get("mobileDeviceId") : session.getId());
				if(scd.containsKey("mobile")){
					Map parameterMap = new HashMap(); parameterMap.put("pmobile_device_id", scd.get("mobileDeviceId"));parameterMap.put("pactive_flag", 0);
					service.executeFunc(scd, 673, parameterMap, (short)7);
				}
			}
			session.removeAttribute("scd-dev");
		}
		if(GenericUtil.uInt(request, "d")!=0)throw new IWBException("session","No Session",0,null, "No valid session", null);
		else response.getWriter().write("{\"success\":true}");
	}

	private String getDefaultLanguage(Map<String, Object> scd, String locale) {
		String res = FrameworkCache.getAppSettingStringValue(0, "locale", "en");
		String active_locales = FrameworkCache.getAppSettingStringValue(scd.get("customizationId"), "active_locales");
		if (GenericUtil.isEmpty(active_locales))
			return res;
		if (active_locales.length() == 2)
			return active_locales;
		for (W5LookUpDetay d : FrameworkCache.getLookUp(scd, 2).get_detayList()) {
			if (d.getActiveFlag() == 1 && active_locales.indexOf(d.getVal()) != -1 && d.getVal().equals(locale)) {
				res = d.getVal();
				break;
			}
		}
		return res;
	}

	@RequestMapping("/login.htm")
	public void hndSuperDeveloperLoginPage(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndLoginPage");
		HttpSession session = request.getSession(false);
		if (session != null) {
			if (session.getAttribute("scd-dev") != null) {
				Map<String, Object> scd = (Map<String, Object>) session.getAttribute("scd-dev");
				if (scd != null)
					UserUtil.onlineUserLogout( (Integer) scd.get("userId"),
							(String) scd.get("sessionId"));
			}
			session.removeAttribute("scd-dev");
		}
		int cusId = FrameworkCache.getAppSettingIntValue(0, "default_customization_id");

		String subDomain = GenericUtil.getSubdomainName(request);
		logger.info("subDomain : " + subDomain);
		if (!subDomain.equals(""))
			cusId = service.getSubDomain2CustomizationId(subDomain);

		Map<String, Object> scd = new HashMap();
		scd.put("userId", 1);
		scd.put("customizationId", cusId);
		scd.put("projectId", FrameworkSetting.devUuid);
		scd.put("path", "");
		Locale blocale = request.getLocale();
		scd.put("locale", FrameworkCache.getAppSettingStringValue(0, "locale", "en"));

		int pageId = 1; // Login Page Template
		if (FrameworkCache.getAppSettingIntValue(0, "mobile_flag") != 0) {
			String requestHeaderUserAgent = request.getHeader("User-Agent");
			// iphone -> Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_1_3 like Mac OS
			// X; en-us) AppleWebKit/528.18 (KHTML, like Gecko) Version/4.0
			// Mobile/7E18 Safari/528.16
			// android -> Mozilla/5.0 (Linux; U; Android 2.2.2; tr-tr; LG-P970
			// Build/FRG83G) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0
			// Mobile Safari/533.1 MMS/LG-Android-MMS-V1.0/1.2
			if (requestHeaderUserAgent != null) {
				requestHeaderUserAgent = requestHeaderUserAgent.toLowerCase();
				if (requestHeaderUserAgent.contains("symbian") || requestHeaderUserAgent.contains("iphone")
						|| requestHeaderUserAgent.contains("ipad") || request.getParameter("iphone") != null
						|| requestHeaderUserAgent.contains("android") || request.getParameter("android") != null) {
					// pageId = 564; //TODO : sencha ile ilgili kısımda
					// hatalar olduğundan burası geçici olarak kapatıldı.
				}
			}
		}

		W5PageResult pageResult = service.getPageResult(scd, pageId, GenericUtil.getParameterMap(request));
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().write(getViewAdapter(scd, request).serializeTemplate(pageResult).toString());
		response.getWriter().close();

	}

	@RequestMapping("/forgotmypass.htm")
	public void hndForgotMyPassPage(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndForgotMyPassPage");

		int cust_id = FrameworkCache.getAppSettingIntValue(0, "default_customization_id");
		Locale blocale = request.getLocale();

		Map<String, Object> scd = new HashMap();
		scd.put("userId", 1);
		scd.put("customizationId", cust_id);
		scd.put("locale", GenericUtil.getParameterMap(request).get("locale") != null
				? GenericUtil.getParameterMap(request).get("locale") : getDefaultLanguage(scd, blocale.getLanguage()));

		int pageId = 7; // Page Template

		W5PageResult pageResult = service.getPageResult(scd, pageId, GenericUtil.getParameterMap(request));
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().write(getViewAdapter(scd, request).serializeTemplate(pageResult).toString());
		response.getWriter().close();

	}

	@RequestMapping("/main.htm")
	public void hndMainPage(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndMainPage");
		
		HttpSession session = request.getSession(false);
		Map<String, Object> scd = null;
		if(session!=null){
			Object token = session.getAttribute("authToken");
			if(token!=null){
				scd = (Map)session.getAttribute("scd-dev");
				if(scd==null || !GenericUtil.safeEquals(scd.get("email"), token)){
					scd = service.generateScdFromAuth(1, token.toString());
				}
				if(scd!=null){
					session.removeAttribute("authToken");
					scd.put("locale", "en");
			        W5Project po = FrameworkCache.getProject(scd);
			        if(po!=null)scd.put("_renderer2", GenericUtil.getRenderer(po.getUiWebFrontendTip()));
					scd.put("sessionId", session.getId());
					session.setAttribute("scd-dev", scd);
				}
				else
					response.sendRedirect("authError.htm");
			} else {
				scd = UserUtil.getScd(request, "scd-dev", true);

			}
		} else { 
			response.sendRedirect("index.html");
			return;
		}
		

		if (scd.get("mobile") != null)
			scd.remove("mobile");

		int pageId = GenericUtil.uInt(scd.get("mainTemplateId")); // Login
		
	
		
																		// Page
																		// Template
		W5PageResult pageResult = service.getPageResult(scd, pageId, GenericUtil.getParameterMap(request));
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().write(getViewAdapter(scd, request).serializeTemplate(pageResult).toString());
		response.getWriter().close();

	}
	
	@RequestMapping("/index.html")
	public void hndLandingPage(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		Map<String, Object> scd = new HashMap();
		scd.put("customizationId", 0);scd.put("userId", 0);scd.put("locale", "en");
		W5PageResult pageResult = service.getPageResult(scd, 2453, new HashMap());
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().write(getViewAdapter(scd, request).serializeTemplate(pageResult).toString());
		response.getWriter().close();

	}
	
	@RequestMapping("/showPage")
	public void hndShowPage(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int pageId = GenericUtil.uInt(request, "_tid");
		logger.info("hndShowPage(" + pageId + ")");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		W5PageResult pageResult = service.getPageResult(scd, pageId, GenericUtil.getParameterMap(request));
		// if(pageResult.getTemplate().getTemplateTip()!=2 && pageId!=218 &&
		// pageId!=611 && pageId!=551 && pageId!=566){ //TODO:cok
		// amele
		// throw new PromisException("security","Template",0,null, "Wrong
		// Template Tip (must be page)", null);
		// }

		if(pageResult.getPage().getPageType()!=0)
			response.setContentType("application/json");

		response.getWriter().write(getViewAdapter(scd, request).serializeTemplate(pageResult).toString());
		response.getWriter().close();
	}
	

	@RequestMapping("/showMList")
	public void hndShowMList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int listId = GenericUtil.uInt(request, "_lid");
		logger.info("hndShowMList(" + listId + ")");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		M5ListResult listResult = service.getMListResult(scd, listId, GenericUtil.getParameterMap(request));
		// if(pageResult.getTemplate().getTemplateTip()!=2 && pageId!=218 &&
		// pageId!=611 && pageId!=551 && pageId!=566){ //TODO:cok
		// amele
		// throw new PromisException("security","Template",0,null, "Wrong
		// Template Tip (must be page)", null);
		// }

		response.setContentType("application/json");
		response.getWriter().write(f7.serializeList(listResult).toString());
		response.getWriter().close();
	}


	@RequestMapping("/showMPage")
	public void hndShowMPage(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int pageId = GenericUtil.uInt(request, "_tid");
		logger.info("hndShowMPage(" + pageId + ")");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		W5PageResult pageResult = service.getPageResult(scd, pageId, GenericUtil.getParameterMap(request));


		if(pageResult.getPage().getPageType()!=0)
			response.setContentType("application/json");

		response.getWriter().write(f7.serializePage(pageResult).toString());
		response.getWriter().close();
	}
	
	@RequestMapping("/grd/*")
	public ModelAndView hndGridReport(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndGridReport");
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		int gridId = GenericUtil.uInt(request, "_gid");
		String gridColumns = request.getParameter("_columns");
		ModelAndView result = null;

		boolean xls = request.getRequestURI().indexOf(".xls") != -1 || "xls".equals(request.getParameter("_fmt"));
		/*if(false && xls) {
			W5GridReportHelper grh = service.prepareGridReport(scd, gridId, gridColumns,
					GenericUtil.getParameterMap(request));
			
			result = null;//TODO yryskul
//			return result;
		}*/
		List<W5ReportCellHelper> list = service.getGridReportResult(scd, gridId, gridColumns,
				GenericUtil.getParameterMap(request));
		if (list != null) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("report", list);
			m.put("scd-dev", scd);
			result = null;
			if (request.getRequestURI().indexOf(".xls") != -1 || "xls".equals(request.getParameter("_fmt")))
				result = new ModelAndView(new RptExcelRenderer(), m);
			else if (request.getRequestURI().indexOf(".pdf") != -1)
				result = new ModelAndView(new RptPdfRenderer(null), m);
			else if (request.getRequestURI().indexOf(".csv") != -1) {
				response.setContentType("application/octet-stream");
				response.getWriter().print(GenericUtil.report2csv(list));
			} else if (request.getRequestURI().indexOf(".txt") != -1) {
				response.setContentType("application/octet-stream");
				response.getWriter().print(GenericUtil.report2text(list));
			}
			return result;
		} else {
			response.getWriter().write("Hata");
			response.getWriter().close();

			return null;
		}

	}


	@RequestMapping("/dl/*")
	public void hndFileDownload(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndFileDownload");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		int fileAttachmentId = GenericUtil.uInt(request, "_fai");
		String customizationId = String.valueOf((scd.get("customizationId") == null) ? 0 : scd.get("customizationId"));
		String local_path = FrameworkCache.getAppSettingStringValue(0, "file_local_path");
		String file_path = "";
		if (fileAttachmentId == -1000) { // default company logo
			file_path = local_path + "/0/jasper/iworkbetter.png";
			response.setContentType("image/png");
		} else {
			W5FileAttachment fa = service.loadFile(scd, fileAttachmentId);
			if (fa == null) {
				throw new IWBException("validation", "File Attachment", fileAttachmentId, null,
						"Invalid Id: " + fileAttachmentId, null);
			}
			ServletOutputStream out = response.getOutputStream();
			file_path = local_path + "/" + customizationId + "/attachment/" + fa.getSystemFileName();
			if(FrameworkSetting.argMap.get("multipart_location")!=null) {
				file_path = FrameworkSetting.argMap.get("multipart_location") + "/"+ file_path;
			}
			if (fa.getFileTypeId() == null || fa.getFileTypeId() != -999)
				response.setContentType("application/octet-stream");
			else {
				long expiry = new Date().getTime() + FrameworkSetting.cacheAge * 1000;
				response.setContentType("image/"
						+ fa.getOrijinalFileName().substring(fa.getOrijinalFileName().lastIndexOf(".") + 1));
				response.setDateHeader("Expires", expiry);
				response.setHeader("Cache-Control", "max-age=" + FrameworkSetting.cacheAge);
			}
		}
		ServletOutputStream out = null;
		InputStream stream = null;
		try {
			stream = new FileInputStream(file_path);
			out = response.getOutputStream();
			// write the file to the file specified
			int bytesRead = 0;
			byte[] buffer = new byte[8192];

			while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			if (FrameworkCache.getAppSettingIntValue(scd, "log_download_flag") != 0) {
				Log5UserAction ua = new Log5UserAction(scd);
				ua.setActionTip((short) 1);
				ua.setTableId(44);
				ua.setTablePk(fileAttachmentId);
				ua.setUserIp(request.getRemoteAddr());
				service.saveObject(ua);

			}
		} catch (Exception e) {
			if (FrameworkSetting.debug)
				e.printStackTrace();
			// bus.logException(e.getMessage(),GenericUtil.uInt(scd.get("customizationId")),GenericUtil.uInt(scd.get("userRoleId")));
			throw new IWBException("generic", "File Attacment", fileAttachmentId, "Unknown Exception",
					e.getMessage(), e.getCause());
		} finally {
			if (out != null)
				out.close();
			if (stream != null)
				stream.close();
		}
	}

	@RequestMapping("/sf/*")
	public void hndShowFile(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int fileAttachmentId = GenericUtil.uInt(request, "_fai");
		logger.info("hndShowFile(" + fileAttachmentId + ")");
		Map<String, Object> scd = null;
		if (fileAttachmentId == 0) {
			scd = UserUtil.getScd(request, "scd-dev", true);
			String spi = request.getRequestURI();
			if (spi.indexOf("/sf/pic")==4 && spi.contains(".")) {
				spi = spi.substring(4+7);
				spi = spi.substring(0, spi.indexOf("."));
				fileAttachmentId = -GenericUtil.uInt(spi);
			}
			if (fileAttachmentId == 0)
				fileAttachmentId = -GenericUtil.uInt(request, "userId");
		}
		InputStream stream = null;
		String filePath = null;
		W5FileAttachment fa = service.loadFile(scd, fileAttachmentId);
		if (fa == null) { // bulunamamis TODO
			System.out.println("Wrong File Id: " + fileAttachmentId);
			//throw new IWBException("validation", "File Attachment", fileAttachmentId, null, "Wrong Id: " + fileAttachmentId, null);
			fa = new W5FileAttachment(scd);
			fa.setFileAttachmentId(1);
		}

		if (fa.getFileAttachmentId() == 1 || fa.getFileAttachmentId() == 2) { // man / woman default picture
//			this.getClass().getClassLoader().getResource("static/ext3.4.1/ext-all.js"); 
//			File folder = new ClassPathResource("static/ext3.4.1/ext-all.js").getFile().getPath();
//			filePath = request.getSession().getServletContext().getRealPath("static/images/custom/ppicture/default_" + (fa.getFileAttachmentId() == 2 ? "wo" : "") + "man_mini.png");
			filePath = fa.getFileAttachmentId() == 2 ? womanPicPath : manPicPath;
		} else {
			String file_path = FrameworkCache.getAppSettingStringValue(0, "file_local_path");
			filePath = file_path + "/" + fa.getCustomizationId() + "/attachment/" + fa.getSystemFileName();
		}

		ServletOutputStream out = response.getOutputStream();
		try {
			/*
			 * if(fileAttachmentId<0)try { stream = new
			 * FileInputStream(filePath); } catch(Exception e0){ stream = new
			 * FileInputStream(request.getRealPath("/images/custom/wv.png")); }
			 * else stream = new FileInputStream(filePath);
			 */

			if (stream == null)
				try {
					stream = new FileInputStream(filePath);
				} catch (Exception e0) {
					stream = new FileInputStream(brokenPicPath);
				}

			// write the file to the file specified
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = stream.read(buffer, 0, 8192)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		} catch (Exception e) {
			if (FrameworkSetting.debug)
				e.printStackTrace();
			// bus.logException(e.getMessage(),GenericUtil.uInt(scd.get("customizationId")),GenericUtil.uInt(scd.get("userRoleId")));
			throw new IWBException("generic", "File Attacment", fileAttachmentId, "Unknown Exception",
					e.getMessage(), e.getCause());
		} finally {
			if(out!=null)out.close();
			if(stream!=null)stream.close();
		}
	}
	@RequestMapping("/dyn-res/*")
	public ModelAndView hndDynResource(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndDynResource"); 
    	Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
    	String uri = request.getRequestURI();
    	if(uri.endsWith(".css")){
    		uri = uri.substring(uri.lastIndexOf('/')+1);
    		uri = uri.substring(0, uri.length()-4);
        	String css = FrameworkCache.getPageResource(scd, uri);
        	if(css!=null){
        		response.setContentType("text/css; charset=UTF-8");
        		response.getWriter().write(css);
        	}
    	}
//    	int pageId =  ;

		response.getWriter().close();
    	return null;
		
	}


	@RequestMapping("/showFormByQuery")
	public void hndShowFormByQuery(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndShowFormByQuery");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		int formId = GenericUtil.uInt(request, "_fid");
		int queryId = GenericUtil.uInt(request, "_qid");
		W5FormResult formResult = service.getFormResultByQuery(scd, formId, queryId,
				GenericUtil.getParameterMap(request));

		response.setContentType("application/json");
		response.getWriter().write(getViewAdapter(scd, request).serializeShowForm(formResult).toString());
		response.getWriter().close();

	}




	@RequestMapping("/getTableRecordInfo")
	public void hndGetTableRecordInfo(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndGetTableRecordInfo");
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		int tableId = GenericUtil.uInt(request, "_tb_id");
		int tablePk = GenericUtil.uInt(request, "_tb_pk");
		W5TableRecordInfoResult r = service.getTableRecordInfo(scd, tableId, tablePk);
		response.setContentType("application/json");
		response.getWriter().write(r != null ? getViewAdapter(scd, request).serializeTableRecordInfo(r).toString() : "{\"success\":false}");
		response.getWriter().close();
	}
	
	@RequestMapping("/ajaxGetLoginLang")
	public void hndGetLoginLang(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndGetLoginLang");
		Map<String, Object> scd = new HashMap<String, Object>();
		Map<String, String> req = GenericUtil.getParameterMap(request);
		scd.put("userId", 1);
		scd.put("customizationId", FrameworkCache.getAppSettingIntValue(0, "default_customization_id"));
		scd.put("locale", getDefaultLanguage(scd, req.get("language")));
		req.put("xlookup_id", "2");

		W5QueryResult queryResult = service.executeQuery(scd, 337, req);

		response.setContentType("application/json");
		response.getWriter().write("{\"success\":true,\"q1\":");
		response.getWriter().write(getViewAdapter(scd, request).serializeQueryData(queryResult).toString());
		response.getWriter().write(",\"_localemsg\":");
		response.getWriter().write(GenericUtil.fromMapToJsonString2(LocaleMsgCache.getPublishLocale2(
				FrameworkCache.getAppSettingIntValue(0, "default_customization_id"), req.get("language"))));
		response.getWriter().write("}");
		response.getWriter().close();

	}

	@RequestMapping(value = "/multiupload.form", method = RequestMethod.POST)
	@ResponseBody
	public String multiFileUpload(@RequestParam("files") MultipartFile[] files,
			@RequestParam("customizationId") Integer customizationId, @RequestParam("userId") Integer userId,
			@RequestParam("table_pk") String table_pk, @RequestParam("table_id") Integer table_id,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		logger.info("multiFileUpload");
		// Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		String path = FrameworkCache.getAppSettingStringValue(0, "file_local_path") + File.separator
				+ customizationId + File.separator + "attachment";

		File dirPath = new File(path);
		if (!dirPath.exists()) {
			if (!dirPath.mkdirs())
				return "{ \"success\":false, \"msg\":\"wrong file path: " + path + "\"}";
		}
		List<W5FileAttachment> lfa = new ArrayList<W5FileAttachment>();
		if (files.length > 0) {
			for (MultipartFile f : files)
				try {
					long fileId = new Date().getTime();
					W5FileAttachment fa = new W5FileAttachment();
					response.setContentType("application/json; charset=UTF-8");
					fa.setSystemFileName(fileId + "." + GenericUtil.strUTF2En(f.getOriginalFilename()));
					f.transferTo(new File(path + File.separator + fa.getSystemFileName()));
					int totalBytesRead = (int) f.getSize();
					fa.setCustomizationId(customizationId);
					fa.setOrijinalFileName(f.getOriginalFilename());
					fa.setTableId(table_id);
					fa.setTablePk(table_pk);
					fa.setTabOrder(Short.parseShort("1"));
					fa.setUploadUserId(userId);
					fa.setFileSize(totalBytesRead);
					fa.setActiveFlag((short) 1);
					lfa.add(fa);
					service.saveObject(fa);
					String webPageId = request.getParameter(".w");
					if (!GenericUtil.isEmpty(webPageId))
						try {
							Map m = new HashMap();
							m.put(".w", webPageId);
							m.put(".pk", table_id + "-" + table_pk);
							m.put(".a", "11");
							m.put(".e", "2");
							Map scd = new HashMap();
							scd.put("customizationId", customizationId);
							scd.put("userId", userId);
							scd.put("sessionId", request.getSession(false).getId());
							UserUtil.liveSyncAction(scd, m);// (customizationId,
															// table_id+"-"+table_pk,
															// userId,
															// webPageId,
															// false);
						} catch (Exception e) {
							if (FrameworkSetting.debug)
								e.printStackTrace();
						}
					return "{ \"success\": true, \"fileId\": " + fa.getFileAttachmentId() + ", \"fileName\": '"
							+ GenericUtil.strUTF2En(GenericUtil.stringToJS(f.getOriginalFilename())) + "'}";

					// out.write("{success: true, fileId: "+
					// fa.getFileAttachmentId() +", fileName:
					// '"+f.getOriginalFilename()+"'}");
				} catch (Exception e) {
					if (FrameworkSetting.debug)
						e.printStackTrace();
					return "{ \"success\": false }";
				} finally {
					try {
						if (f.getInputStream() != null)
							f.getInputStream().close();
					} catch (Exception e2) {
						if (FrameworkSetting.debug)
							e2.printStackTrace();
					}
					// out.close();
				}
			// bus.saveAllObjectz(lfa);

		}
		return "{\"success\": false }";
	}

	@RequestMapping(value = "/upload.form", method = RequestMethod.POST)
	@ResponseBody
	public String singleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("table_pk") String table_pk,
			@RequestParam("table_id") Integer table_id, @RequestParam("profilePictureFlag") Integer profilePictureFlag,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		logger.info("singleFileUpload");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		Map<String, String> requestParams = GenericUtil.getParameterMap(request);

		String path = FrameworkCache.getAppSettingStringValue(0, "file_local_path")
				+ File.separator + scd.get("customizationId") + File.separator + "attachment";

		File dirPath = new File(path);
		if (!dirPath.exists()) {
			dirPath.mkdirs();
		}

		long fileId = new Date().getTime();
		int totalBytesRead = (int) file.getSize();

		W5FileAttachment fa = new W5FileAttachment(scd);
		boolean ppicture = (GenericUtil.uInt(scd.get("customizationId")) == 0 || FrameworkCache
						.getAppSettingIntValue(scd.get("customizationId"), "profile_picture_flag") != 0)
				&& profilePictureFlag != null && profilePictureFlag != 0;
		try {
			if (ppicture) {
				int maxFileSize = FrameworkCache.getAppSettingIntValue(0, "profile_picture_max_file_size", 51200);
				if (maxFileSize < totalBytesRead)
					return "{ \"success\": false , \"msg\":\"" + LocaleMsgCache.get2(scd, "max_file_size") + " = "
							+ Math.round(maxFileSize / 1024) + " KB\"}";
				fa.setFileTypeId(-999);// profile picture upload etti
			} else if (table_id == 338) {
				int maxFileSize = FrameworkCache.getAppSettingIntValue(0, "company_picture_max_file_size", 512000);
				if (maxFileSize < totalBytesRead)
					return "{ \"success\": false , \"msg\":\"" + LocaleMsgCache.get2(scd, "max_file_size") + " = "
							+ Math.round(maxFileSize / 1024) + " KB\"}";
				fa.setFileTypeId(-998);// company picture upload etti
			}
			fa.setSystemFileName(fileId + "." + GenericUtil.strUTF2En(file.getOriginalFilename()));
			file.transferTo(new File(path + File.separator + fa.getSystemFileName()));
			fa.setOrijinalFileName(file.getOriginalFilename());
			fa.setTableId(table_id);
			fa.setTablePk(table_pk);
			fa.setTabOrder((short) 1);
			fa.setFileSize(totalBytesRead);
			fa.setActiveFlag((short) 1);
			try {
				if(!ppicture)if (GenericUtil.uStrNvl(requestParams.get("file_type_id"), "") != null) {
					fa.setFileTypeId(Integer.parseInt(GenericUtil.uStrNvl(requestParams.get("file_type_id"), "")));
				}
				if (GenericUtil.uStrNvl(requestParams.get("file_comment"), "") != null) {
					fa.setFileComment(GenericUtil.uStrNvl(requestParams.get("file_comment"), ""));
				}
			} catch (Exception e) {

			}
			service.saveObject(fa);
			String webPageId = request.getParameter(".w");
			if (!GenericUtil.isEmpty(webPageId)) {
				Map m = new HashMap();
				m.put(".w", webPageId);
				m.put(".pk", table_id + "-" + table_pk);
				m.put(".a", "11");
				m.put(".e", "2");
				UserUtil.liveSyncAction(scd, m);// (customizationId,
												// table_id+"-"+table_pk,
												// userId, webPageId, false);

			}
			return "{ \"success\": true, \"fileId\": " + fa.getFileAttachmentId() + ", \"fileName\": \""
					+ GenericUtil.stringToJS(file.getOriginalFilename()) + "\", \"fileUrl\": \"" + "sf/"
					+ fa.getSystemFileName() + "?_fai=" + fa.getFileAttachmentId() + "\"}";
		} catch (Exception e) {
			if (true || FrameworkSetting.debug)
				e.printStackTrace();
			return "{ \"success\": false }";
		} /*
			 * finally { // transferTo yüzünden zaten hep exceptiona düşüyor.
			 * try {
			 * if(file.getInputStream()!=null)file.getInputStream().close(); }
			 * catch (Exception e2) {
			 * if(PromisSetting.debug)e2.printStackTrace(); } //
			 * response.getWriter().close(); }
			 */

	}

	@RequestMapping(value = "/upload2.form", method = RequestMethod.POST)
	@ResponseBody
	public String singleFileUpload4Webix(@RequestParam("upload") MultipartFile file, @RequestParam("table_pk") String table_pk,
			@RequestParam("table_id") Integer table_id, @RequestParam("profilePictureFlag") Integer profilePictureFlag,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		logger.info("singleFileUpload4Webix");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		Map<String, String> requestParams = GenericUtil.getParameterMap(request);

		String path = FrameworkCache.getAppSettingStringValue(0, "file_local_path")
				+ File.separator + scd.get("customizationId") + File.separator + "attachment";

		File dirPath = new File(path);
		if (!dirPath.exists()) {
			dirPath.mkdirs();
		}

		long fileId = new Date().getTime();
		int totalBytesRead = (int) file.getSize();

		W5FileAttachment fa = new W5FileAttachment();
		boolean ppicture = (GenericUtil.uInt(scd.get("customizationId")) == 0 || FrameworkCache
						.getAppSettingIntValue(scd.get("customizationId"), "profile_picture_flag") != 0)
				&& profilePictureFlag != null && profilePictureFlag != 0;
		try {
			// fa.setFileComment(bean.getFile_comment());
			fa.setCustomizationId(GenericUtil.uInt(scd.get("customizationId")));
			if (ppicture) {
				int maxFileSize = FrameworkCache.getAppSettingIntValue(0, "profile_picture_max_file_size", 51200);
				if (maxFileSize < totalBytesRead)
					return "{ \"success\": false , \"msg\":\"" + LocaleMsgCache.get2(scd, "max_file_size") + " = "
							+ Math.round(maxFileSize / 1024) + " KB\"}";
				fa.setFileTypeId(-999);// profile picture upload etti
			} else if (table_id == 338) {
				int maxFileSize = FrameworkCache.getAppSettingIntValue(0, "company_picture_max_file_size", 512000);
				if (maxFileSize < totalBytesRead)
					return "{ \"success\": false , \"msg\":\"" + LocaleMsgCache.get2(scd, "max_file_size") + " = "
							+ Math.round(maxFileSize / 1024) + " KB\"}";
				fa.setFileTypeId(-998);// company picture upload etti
			}
			fa.setSystemFileName(fileId + "." + GenericUtil.strUTF2En(file.getOriginalFilename()));
			file.transferTo(new File(path + File.separator + fa.getSystemFileName()));
			fa.setOrijinalFileName(file.getOriginalFilename());
			fa.setTableId(table_id);
			fa.setTablePk(table_pk);
			fa.setTabOrder((short) 1);
			fa.setUploadUserId(GenericUtil.uInt(scd.get("userId")));
			fa.setFileSize(totalBytesRead);
			fa.setActiveFlag((short) 1);
			try {
				if(!ppicture)if (GenericUtil.uStrNvl(requestParams.get("file_type_id"), "") != null) {
					fa.setFileTypeId(Integer.parseInt(GenericUtil.uStrNvl(requestParams.get("file_type_id"), "")));
				}
				if (GenericUtil.uStrNvl(requestParams.get("file_comment"), "") != null) {
					fa.setFileComment(GenericUtil.uStrNvl(requestParams.get("file_comment"), ""));
				}

			} catch (Exception e) {

			}
			service.saveObject(fa);
			String webPageId = request.getParameter(".w");
			if (!GenericUtil.isEmpty(webPageId)) {
				Map m = new HashMap();
				m.put(".w", webPageId);
				m.put(".pk", table_id + "-" + table_pk);
				m.put(".a", "11");
				m.put(".e", "2");
				UserUtil.liveSyncAction(scd, m);// (customizationId,
												// table_id+"-"+table_pk,
												// userId, webPageId, false);

			}
			return "{ \"success\": true, \"fileId\": " + fa.getFileAttachmentId() + ", \"fileName\": \""
					+ GenericUtil.stringToJS(file.getOriginalFilename()) + "\", \"fileUrl\": \"" + "sf/"
					+ fa.getSystemFileName() + "?_fai=" + fa.getFileAttachmentId() + "\"}";
		} catch (Exception e) {
			if (true || FrameworkSetting.debug)
				e.printStackTrace();
			return "{ \"success\": false }";
		} /*
			 * finally { // transferTo yüzünden zaten hep exceptiona düşüyor.
			 * try {
			 * if(file.getInputStream()!=null)file.getInputStream().close(); }
			 * catch (Exception e2) {
			 * if(PromisSetting.debug)e2.printStackTrace(); } //
			 * response.getWriter().close(); }
			 */

	}
	/*
	@RequestMapping("/ajaxCacheInfo")
	public void hndAjaxCacheInfo(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", false);
		int customizationId = (Integer) scd.get("customizationId");
		if (customizationId == 0 && GenericUtil.uInt(request, "cusId") != 0)
			customizationId = GenericUtil.uInt(request, "cusId");
		List<Integer> l = new ArrayList();
		if (customizationId != 0)
			l.add(customizationId);
		else
			for (W5Customization c : FrameworkCache.wCustomization)
				l.add(c.getCustomizationId());
		response.setContentType("application/json");
		StringBuilder sb = new StringBuilder();
		sb.append("{\"success\":true,\"result\":{\n");
		// customizationId:{TableChacheCount, FormCacheCount, GridCacheCount,
		// ObjectCacheCount, }
		boolean b1 = false;
		for (Integer c : l) {
			if (b1)
				sb.append(",\n");
			else
				b1 = true;
			sb.append("\"").append(c).append("\":{");
			sb.append("\"tableCacheCount\":").append(GenericUtil.getSafeSize(FrameworkCache.wTables.get(c)));
			sb.append(",\n \"formCacheCount\":").append(GenericUtil.getSafeSize(FrameworkCache.wForms.get(c)));
			sb.append(",\n \"gridCacheCount\":").append(GenericUtil.getSafeSize(FrameworkCache.wGrids.get(c)));
			sb.append(",\n \"queryCacheCount\":").append(GenericUtil.getSafeSize(FrameworkCache.wQueries));
			sb.append(",\n \"listViewCacheCount\":").append(GenericUtil.getSafeSize(FrameworkCache.wListViews.get(c)));
			sb.append(",\n \"dataViewCacheCount\":").append(GenericUtil.getSafeSize(FrameworkCache.wDataViews.get(c)));
			sb.append(",\n \"widgetCacheCount\":0");
			sb.append(",\n \"localeCacheCount\":{");
			boolean b2 = false;
			for (String loc : LocaleMsgCache.localeMap2.keySet()) {
				if (b2)
					sb.append(",");
				else
					b2 = true;
				sb.append("\n  \"").append(loc).append("\":")
						.append(GenericUtil.getSafeSize(LocaleMsgCache.localeMap2.get(loc)));
			}
			sb.append("}");

			int cnt = 0;
			for (Integer t : FrameworkCache.wTables.get(c).keySet()) {
				cnt += GenericUtil.getSafeSize(FrameworkCache.getTableCacheMap(c, t));
			}
			sb.append(",\n \"objectCacheCount\":").append(cnt);
			sb.append(",\n \"feedCacheCount\":").append(GenericUtil.getSafeSize(FrameworkCache.wFeeds.get(c)));
			sb.append(",\n \"jobCacheCount\":").append(GenericUtil.getSafeSize(FrameworkCache.wJobs));

			sb.append("}");

		}

		sb.append("}");

		sb.append("}");
		response.getWriter().write(sb.toString());
		response.getWriter().close();
	}

*/
	@RequestMapping("/ajaxSendFormSmsMail")
	public void hndAjaxSendFormSmsMail(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxSendFormSmsMail");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		response.setContentType("application/json");
		int smsMailId = GenericUtil.uInt(request, "_fsmid");
		Map result = service.sendFormSmsMail(scd, smsMailId, GenericUtil.getParameterMap(request));
		response.getWriter().write(GenericUtil.fromMapToJsonString(result));
		response.getWriter().close();
	}

	
	@RequestMapping("/ajaxGlobalNextVal")
	public void hndAjaxGlobalNextVal( //TODO Add Security
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		if(!FrameworkSetting.vcsServer)
			throw new IWBException("vcs","getGlobalNextval",0,null, "Not VCS Server to getGlobalNextval", null);
		
		
		String id=request.getParameter("id");
		String key=request.getParameter("key");
		int userId =0, customizationId=0;
		String projectUuid = "undefined";
		if(!GenericUtil.isEmpty(key)){
			String[] oz = key.replace('.', ',').split(",");
			if(oz.length==3){
				customizationId = GenericUtil.uInt(oz[0]); 
				userId = GenericUtil.uInt(oz[1]); 
				projectUuid = oz[2]; 
			}
		}
		
		int nextVal = service.getGlobalNextval(id, projectUuid, userId, customizationId, request.getRemoteAddr());
		
		response.getWriter().write("{\"success\":true, \"val\":"+nextVal+"}"); //hersey duzgun
		response.getWriter().close();
	}
	@RequestMapping("/ajaxOrganizeTable")
	public void hndAjaxOrganizeTable(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		String tableName = request.getParameter("ptable_dsc");
		logger.info("hndAjaxOrganizeTable("+tableName+")"); 
    	Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
    	boolean b = (Integer)scd.get("roleId")!=0 ? false : service.organizeTableFields(scd, tableName);
		response.setContentType("application/json");
		response.getWriter().write("{\"success\":"+b+"}");
		response.getWriter().close();
	}
	@RequestMapping("/ajaxOrganizeREST")
	public void hndAjaxOrganizeREST(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndAjaxOrganizeREST"); 
	    Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
	    
		Map m =service.organizeREST(scd, request.getParameter("serviceName"));
		response.getWriter().write(GenericUtil.fromMapToJsonString2Recursive(m));
		response.getWriter().close();		
	}

	
	@RequestMapping("/ajaxBuildForm")
	public void hndAjaxBuildForm(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException, JSONException {
		
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		
		int i = service.buildForm(scd, request.getParameter("data"));
		response.setContentType("application/json");
		response.getWriter().write("{\"success\":true, \"result\":"+i+"}");
		response.getWriter().close();
	}

	@RequestMapping("/ajaxCallWs")
	public void hndAjaxCallWs(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxCallWs"); 
	    Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
	    
		Map m =service.REST(scd, request.getParameter("serviceName"), GenericUtil.getParameterMap(request));
		if(m!=null) {
			if(m.get("data")!=null && m.get("data") instanceof byte[]) {
				response.setContentType("application/octet-stream");
				byte[] r = (byte[])m.get("data");
				response.getOutputStream().write(r, 0, r.length);
				response.getOutputStream().close();
				return;
			} else if(!m.containsKey("success")) {
				if(m.containsKey("exception")) {
					m.put("success", false);
					m.put("errorType", "rest");
					
				} else 
					m.put("success", true);
			}
		}
		response.getWriter().write(GenericUtil.fromMapToJsonString2Recursive(m));
		response.getWriter().close();		
	}


	@RequestMapping("/ajaxQueryData4Debug")
	public void hndAjaxQueryData4Debug(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxQueryData4Debug"); 
		
    	Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		int roleId =(Integer)scd.get("roleId");
		if(roleId!=0 && !FrameworkSetting.debug){
			throw new IWBException("security","Developer",0,null, "You Have to Be Developer TO Run this", null);
		}

		int queryId= GenericUtil.uInt(request, "_qid");

		Object o = service.executeQuery4Debug(scd, queryId, GenericUtil.getParameterMap(request));
		
		response.setContentType("application/json");
		if(o instanceof W5QueryResult)
			response.getWriter().write(getViewAdapter(scd, request).serializeQueryData((W5QueryResult)o).toString());
		else {
			Map m = (Map)o;//new HashMap();
			m.put("success", true);
//			m.put("data", queryResult.getData());
//			Map m2 = new HashMap();m2.put("startRow", 0);m2.put("fetchCount", queryResult.getData().size());m2.put("totalCount", queryResult.getData().size());
//			m.put("browseInfo", m2);
	//		m.put("sql", queryResult.getExecutedSql());
			response.getWriter().write(GenericUtil.fromMapToJsonString2Recursive(m));
		}
		response.getWriter().close();
	}
	
	@RequestMapping("/ajaxQueryData4Pivot")
	public void hndAjaxQueryData4Pivot(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int tableId = GenericUtil.uInt(request, "_tid");
		logger.info("hndAjaxQueryData4Pivot(" + tableId + ")");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		response.setContentType("application/json");
		response.getWriter().write(GenericUtil.fromListToJsonString2Recursive(service.executeQuery4Pivot(scd, tableId, GenericUtil.getParameterMap(request))));
		response.getWriter().close();
	}
	
	@RequestMapping("/ajaxQueryData4DataList")
	public void hndAjaxQueryData4DataList(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		int tableId = GenericUtil.uInt(request, "_tid");
		logger.info("hndAjaxQueryData4DataList(" + tableId + ")");

		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);

		response.setContentType("application/json");
		response.getWriter().write(GenericUtil.fromListToJsonString2Recursive(service.executeQuery4DataList(scd, tableId, GenericUtil.getParameterMap(request))));
		response.getWriter().close();
	}
	
	@RequestMapping("/ajaxExecDbFunc4Debug")
	public void hndAjaxExecDbFunc4Debug(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		logger.info("hndAjaxExecDbFunc4Debug"); 
		
    	Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		int roleId =(Integer)scd.get("roleId");
		if(roleId!=0 && !FrameworkSetting.debug){
			throw new IWBException("security","Developer",0,null, "You Have to Be Developer TO Run this", null);
		}

		int globalFuncId= GenericUtil.uInt(request, "_did"); // +:globalFuncId, -:formId

		W5GlobalFuncResult dbFuncResult = service.executeGlobalFunc4Debug(scd, globalFuncId, GenericUtil.getParameterMap(request));

		response.setContentType("application/json");
		response.getWriter().write(getViewAdapter(scd, request).serializeGlobalFunc(dbFuncResult).toString());
		response.getWriter().close();
	}
	
	@RequestMapping("/ajaxFormatSQL")
	public void hndAjaxFormatSQL(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
    	Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		String sql = request.getParameter("sql");
		response.setContentType("application/json");
		response.getWriter().write("{\"success\":true, \"result\":\""+GenericUtil.stringToJS2(new BasicFormatterImpl().format(sql))+"\"}");
		response.getWriter().close();
	}
	
	@RequestMapping("/ajaxServerDttm")
	public void hndAjaxServerDttm(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
    	Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		response.setContentType("application/json");
		response.getWriter().write("{\"success\":true, \"result\":\""+service.getServerDttm()+"\"}");
		response.getWriter().close();
	}
	
	@RequestMapping("/comp/*")
	public void hndComponent(
			HttpServletRequest request,
			HttpServletResponse response) throws IOException{
		logger.info("hndComponent"); 
    	Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
    	String uri = request.getRequestURI();
    	if(uri.endsWith(".css")){
    		uri = uri.substring(uri.lastIndexOf('/')+1);
    		uri = uri.substring(0, uri.length()-4);
        	String css = FrameworkCache.getComponentCss(scd, GenericUtil.uInt(uri));
    		response.setContentType("text/css; charset=UTF-8");
        	if(css!=null){
        		response.getWriter().write(css);
        	} else {
        		
        	}
    	} else if(uri.endsWith(".js")){
    		uri = uri.substring(uri.lastIndexOf('/')+1);
    		uri = uri.substring(0, uri.length()-3);
        	String js = FrameworkCache.getComponentJs(scd, GenericUtil.uInt(uri));
    		response.setContentType("text/javascript; charset=UTF-8");
        	if(js!=null){
        		response.getWriter().write(js);
        	} else {
        		
        	}
    	}

		response.getWriter().close();
	}
	
	@RequestMapping(value = "/addClusterCert", method = RequestMethod.POST)
	@ResponseBody
	public String hndAddClusterCertificate(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("addClusterCert");
		
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		Map<String, String> requestParams = GenericUtil.getParameterMap(request);
		
		long fileId = new Date().getTime();
		int totalBytesRead = (int) file.getSize();
		
		try {			
			if (6020 < totalBytesRead) {
				return "{ \"success\": false , \"msg\":\"" + LocaleMsgCache.get2(scd, "max_file_size") + " = 5KB\"}";
			}
			final char sep = File.separatorChar;
		    File dir = new File(System.getProperty("java.home") + sep + "lib" + sep + "security");
		    File cacertsFile = new File(dir, "cacerts");
		    
			KeyStore ks = KeyStore.getInstance("JKS");
			char[] pwdArray = "changeit".toCharArray();
			ks.load(new FileInputStream(cacertsFile), pwdArray);
			
			String fileName = file.getOriginalFilename();
			int dotIndex = fileName.indexOf('.');
			String newAlias = fileName.substring(0, dotIndex);
			boolean contains = ks.containsAlias(newAlias);
			
			if(contains) {
				return "{ \"success\": false, \"msg\":"
						+ "\"keystore contains this alias, please upload certificate file with different name\" }";
			}
			InputStream inputStream =  new BufferedInputStream(file.getInputStream());
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			Certificate cert = cf.generateCertificate(inputStream);
			ks.setCertificateEntry(newAlias, cert);
			
			OutputStream out = new FileOutputStream(cacertsFile);
			ks.store(out, pwdArray);
		    out.close();
					    
		    return "{ \"success\": true}";
				
		}catch(Exception e) {
			if (true || FrameworkSetting.debug)
				e.printStackTrace();
			return "{ \"success\": false }";
		}

	}
	
	@RequestMapping(value ="/excelImport", method = RequestMethod.POST)
	@ResponseBody
	public String fileImportHandler(
		@RequestParam("file") MultipartFile file,
		HttpServletRequest request,
		HttpServletResponse response
	)throws IOException{
		logger.info("fileImportHandler");	
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
	    int excelImportId = 0;
		try {
			if(!file.isEmpty()){
				String path = FrameworkCache.getAppSettingStringValue(0, "file_local_path")
						+ File.separator + scd.get("customizationId") + File.separator + "attachment";
		
				File dirPath = new File(path);
			    if (!dirPath.exists()) {
			    	if(!dirPath.mkdirs()) return "{ \"success\":false, \"msg\":\"wrong file path: "+path+"\"}";
			    }
			    // f.transferTo(new File(path + File.separator + fa.getSystemFileName()));
			    String uploadedFilePath = path + File.separator + GenericUtil.strUTF2En(file.getOriginalFilename());
			    String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1).toLowerCase();
			    File tmpFile = new File(uploadedFilePath);
			    file.transferTo(tmpFile);
			    if(extension.compareTo("xls") == 0 || extension.compareTo("xlsx") == 0){
			    	//// VeritabanÄ±na kaydetme bitti 
			    	ExcelUtil p = new ExcelUtil(tmpFile.getPath());
			    	// ilk veri hangi sheet olduÄŸu, sonraki hangi satÄ±r olduÄŸu
			    	LinkedHashMap<String,List<HashMap<String,String>>> parsedData = p.parseExcel();			    	
			    	
			    	if(parsedData != null && parsedData.size() > 0){
			    		excelImportId = importService.saveExcelImport(scd, tmpFile.getName(), uploadedFilePath, parsedData);
			    	}		    	
			    }else if(extension.compareTo("csv") == 0){
			    	Reader in = new FileReader(tmpFile.getPath());
			    	CSVParser parser = CSVParser.parse(in, CSVFormat.DEFAULT);
			    	LinkedHashMap<String,List<HashMap<String,String>>> parsedData = new LinkedHashMap();
			    	List<HashMap<String,String>> sheet = new ArrayList();
			    	parsedData.put("Sheet 1", sheet);
			    	String[] keyz = new String[] {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"
			    			,"AA","AB","AC","AD","AE","AF","AG","AH","AI","AJ","AK","AL","AM","AN","AO","AP","AQ","AR","AS","AT","AU","AV","AW","AX","AY","AZ"};
			    	for (CSVRecord record : parser.getRecords()) {
			    		HashMap<String, String> m = new HashMap();
			    		sheet.add(m);
			    		for(int qi = 0; qi<record.size();qi++) {
			    			m.put(keyz[qi], record.get(qi));			    			
			    		}
			        }
		    		excelImportId = importService.saveExcelImport(scd, tmpFile.getName(), uploadedFilePath, parsedData);
			    	 
			    }
//			    tmpFile.delete();
			}
			return "{ \"success\": true , \"excelImportId\": "+excelImportId+", \"excel_import_id\":"+excelImportId+"}";
		} catch (Exception e){
			if(FrameworkSetting.debug)e.printStackTrace();
			return "{ \"success\": false }";
		}
	}	
	
	@RequestMapping(value ="/appMakerImport", method = RequestMethod.POST)
	@ResponseBody
	public String appMakerHandler(
		@RequestParam("file") MultipartFile file,
		HttpServletRequest request,
		HttpServletResponse response
	)throws IOException{
		logger.info("appMakerImportHandler");	
		Map<String, Object> scd = UserUtil.getScd(request, "scd-dev", true);
		W5Project po = FrameworkCache.getProject(scd);
		if(po.getUiWebFrontendTip()!=8)
			return "{ \"success\": false, \"error\":\"To Import AppMaker project, the FrontendUI of the project must be GReact16\" }";
			
		try {
			if(!file.isEmpty()){
				String path = FrameworkCache.getAppSettingStringValue(0, "file_local_path")
						+ File.separator + scd.get("customizationId") + File.separator + "attachment";
		
				File dirPath = new File(path);
			    if (!dirPath.exists()) {
			    	if(!dirPath.mkdirs()) return "{ \"success\":false, \"msg\":\"wrong file path: "+path+"\"}";
			    }
			    // f.transferTo(new File(path + File.separator + fa.getSystemFileName()));
			    String uploadedFilePath = path + File.separator + GenericUtil.strUTF2En(file.getOriginalFilename());
			    String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1).toLowerCase();
			    File tmpFile = new File(uploadedFilePath);
			    file.transferTo(tmpFile);
			    if(extension.compareTo("zip") == 0)try(ZipFile zfile = new ZipFile(tmpFile.getPath())){
			    	Map m = new HashMap();
		            FileSystem fileSystem = FileSystems.getDefault();
		            //Get file entries
		            Enumeration<? extends ZipEntry> entries = zfile.entries();
		             
		            Map<String, String> scriptHelper = new HashMap();
		             
		            //Iterate over entries
		            while (entries.hasMoreElements()) 
		            {
		                ZipEntry entry = entries.nextElement();
		                //If directory then create a new directory in uncompressed folder

	                    InputStream inputStream = zfile.getInputStream(entry);

	                    ByteArrayOutputStream result = new ByteArrayOutputStream();
	                    byte[] buffer = new byte[1024];
	                    int length;
	                    while ((length = inputStream.read(buffer)) != -1) {
	                        result.write(buffer, 0, length);
	                    }
	                    String fname = entry.getName().toLowerCase(FrameworkSetting.appLocale);
	                    String body = result.toString(StandardCharsets.UTF_8.name());
	                    if(!fname.contains("scripts/"))
	                    	importService.importAppMaker(scd, 
	                    			fname.lastIndexOf('.')>-1? entry.getName().substring(0, entry.getName().lastIndexOf('.')):entry.getName(), 
	                    					body, null);
	                    else {
	                    	String fileName = fname.substring("scripts/".length());
	                    	fileName = fileName.substring(0, fileName.lastIndexOf('.'));
	                    	if(scriptHelper.containsKey(fileName)) {
	                    		boolean b = fname.endsWith(".json");
	                    		String body2 = scriptHelper.get(fileName);
		                    	importService.importAppMaker(scd, entry.getName(), b?body:body2, !b?body:body2);
	                    	} else
	                    		scriptHelper.put(fileName, body);
	                    	
	                    }
//	                    m.put(entry.getName(), result.toString(StandardCharsets.UTF_8.name()));
//	                    System.out.println(result.toString(StandardCharsets.UTF_8.name()));

		            }
		        }
		        catch(IOException e)
		        {
		            e.printStackTrace();
		        }
//			    tmpFile.delete();
			}
			return "{ \"success\": true }";
		} catch (Exception e){
			if(FrameworkSetting.debug)e.printStackTrace();
			return "{ \"success\": false, \"error\":\""+e.getMessage()+"\" }";
		}
	}	
	
}
