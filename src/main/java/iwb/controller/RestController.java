package iwb.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

//import iwb.adapter.soap.SoapAdapter;
//import iwb.adapter.soap.impl.AxisSoap1_4;
import iwb.adapter.ui.ViewAdapter;
import iwb.adapter.ui.extjs.ExtJs3_4;
import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.exception.IWBException;
import iwb.model.db.Log5Transaction;
import iwb.model.db.W5FormCell;
import iwb.model.db.W5GlobalFunc;
import iwb.model.db.W5GlobalFuncParam;
import iwb.model.db.W5JobSchedule;
import iwb.model.db.W5Project;
import iwb.model.db.W5QueryField;
import iwb.model.db.W5QueryParam;
import iwb.model.db.W5Table;
import iwb.model.db.W5TableParam;
import iwb.model.db.W5WsServer;
import iwb.model.db.W5WsServerMethod;
import iwb.model.db.W5WsServerMethodParam;
import iwb.model.result.W5FormResult;
import iwb.model.result.W5GlobalFuncResult;
import iwb.model.result.W5QueryResult;
import iwb.service.FrameworkService;
import iwb.util.EncryptionUtil;
import iwb.util.GenericUtil;
import iwb.util.HttpUtil;
import iwb.util.LogUtil;
import iwb.util.UserUtil;

@Controller
@RequestMapping("/rest")
public class RestController implements InitializingBean {
	private static Logger logger = Logger.getLogger(RestController.class);

	private ViewAdapter ext3_4 = new ExtJs3_4();
	
	@Autowired
	private FrameworkService service;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
	}

	@RequestMapping("/*/*/*")
	public void hndREST( //project/service/method
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException{
		request.setCharacterEncoding( "UTF-8" );
		response.setCharacterEncoding( "UTF-8" );
		try {
			String[] u = request.getRequestURI().replace('/', ',').split(",");
			String token = (String)request.getParameter("tokenKey");
			String projectId=u[2]; 
			W5Project po = FrameworkCache.getProject(projectId);
			if(po==null) {
				throw new IWBException("ws","Invalid Project",0,null, "Invalid Project", null);
			}
			String serviceName=u[3];
			String methodName=u[4];
			if(serviceName.equals("job")) {//project jobs
				W5JobSchedule job = FrameworkCache.getJob(projectId, GenericUtil.uInt(methodName));
				if(job != null && job.runCheck()) {
					response.getWriter().write("{\"success\":"+ service.runJob(job) +"}"); // hersey duzgun
					return;
				}
			} else if(methodName.equals("login")){
				Map requestParams = GenericUtil.getParameterMap(request);
				requestParams.put("_remote_ip", request.getRemoteAddr());
				String transactionId =  GenericUtil.getTransactionId();
				requestParams.put("_trid_", transactionId);
				if(FrameworkSetting.logType>0)LogUtil.logObject(new Log5Transaction(po.getProjectUuid(), "rest("+serviceName+".login)", transactionId), true);
				String xlocale = GenericUtil.uStrNvl(request.getParameter("locale"),FrameworkCache.getAppSettingStringValue(0, "locale"));
				Map scd = new HashMap();
				scd.put("customizationId", po.getCustomizationId());
				scd.put("projectId", po.getProjectUuid());
				scd.put("locale", xlocale);
				W5GlobalFuncResult result = service.executeFunc(scd, po.getCustomizationId()==0 ? 1:1252, requestParams, (short) 7); // user Authenticate DbFunc:1
				W5GlobalFuncResult dfr = new W5GlobalFuncResult(-1);dfr.setResultMap(new HashMap());dfr.setErrorMap(new HashMap());
				List<W5GlobalFuncParam> arl = new ArrayList();
				dfr.setGlobalFunc(new W5GlobalFunc());dfr.getGlobalFunc().set_dbFuncParamList(arl);
				arl.add(new W5GlobalFuncParam("tokenKey"));arl.add(new W5GlobalFuncParam("errorMsg"));
//				W5WsServerMethod wsm = wss.get_methods().get(0);
				// 4 success 5 errorMsg 6 userId 7 expireFlag 8 smsFlag 9 roleCount
				boolean success = GenericUtil.uInt(result.getResultMap().get("success")) != 0;
				boolean expireFlag = GenericUtil.uInt(result.getResultMap().get("expireFlag")) != 0;
				if (!success || expireFlag){
					String errorMsg = LocaleMsgCache.get2(0, xlocale, expireFlag ? "pass_expired":(String)result.getResultMap().get("errorMsg"));
					response.getWriter().write("{\"success\":false,\"error\":\"" + GenericUtil.stringToJS2(errorMsg) + "\"}");
					return;
				}
				
				int userId = GenericUtil.uInt(result.getResultMap().get("userId"));
				int roleCount = GenericUtil.uInt(result.getResultMap().get("roleCount"));
				int forceUserRoleId = GenericUtil.uInt(requestParams.get("userRoleId"));
				if (roleCount < 0 || forceUserRoleId != 0) {
					if(po.getCustomizationId()==0) {
						if (forceUserRoleId == 0)forceUserRoleId = -roleCount;
						scd = service.userRoleSelect(userId, forceUserRoleId,
							GenericUtil.uInt(requestParams.get("customizationId")), null, null);
					} else {
						scd = service.userRoleSelect4App2(po, userId, forceUserRoleId, result.getResultMap());
					}
					if (scd == null){
						response.getWriter().write("{\"success\":false, \"msg\":\"no role found\"}"); // bir hata var
						return;
					}
					scd.put("locale", xlocale);
					String tokenKey = EncryptionUtil.encryptAES(GenericUtil.fromMapToJsonString2Recursive(scd));
					dfr.getResultMap().put("tokenKey", tokenKey);
					response.getWriter().write("{\"success\":true,\"token\":\""+tokenKey+"\",\"session\":" + GenericUtil.fromMapToJsonString2(scd)+"}"); // hersey duzgun
					
//					response.getWriter().write("{\"success\":true,\"token\":\""+UserUtil.generateTokenFromScd(scd, 0, request.getRemoteAddr(), 24 * 60 * 60 * 1000)+"\",\"session\":" + GenericUtil.fromMapToJsonString2(scd)+"}"); // hersey duzgun
	
					return;
	
				} else {
					dfr.getResultMap().put("errorMsg", "Too many roles. Use [forceUserRoleId]");
					response.getWriter().write("{\"success\":false,\"error\":\"Too many roles. Use [forceUserRoleId]\"}");
					return;
				}
			}
			response.setContentType("application/json");

			W5WsServer wss = FrameworkCache.getWsServer(projectId, serviceName);
			if(wss==null)
				throw new IWBException("framework","WrongService",0,null, "Wrong Service: Should Be [ServiceName].[MethodName]", null);
			W5WsServerMethod wsm = null;
			for(W5WsServerMethod wsmx:wss.get_methods())if(wsmx.getDsc().equals(methodName)){
				wsm = wsmx;
				break;
			}
			if(wsm==null)
				throw new IWBException("framework","WrongMethod",0,null, "Wrong Method: Should Be [ServiceName].[MethodName]", null);
			
			Map<String, Object> scd = null;
			if(GenericUtil.isEmpty(wsm.getAccessSourceTypes()) || GenericUtil.hasPartInside2(wsm.getAccessSourceTypes(), "1")){
				if(!GenericUtil.isEmpty(token)) {
					token = EncryptionUtil.decryptAES(token);
					if(!GenericUtil.isEmpty(token)) try{
						scd = GenericUtil.fromJSONObjectToMap(new JSONObject(token));
					} catch(Exception ee) {
						if(FrameworkSetting.debug)ee.printStackTrace();
						throw new IWBException("session","Invalid.Token",0,null, "No valid token: " + ee.getMessage(), ee);
					}
				}
				if(!GenericUtil.hasPartInside2(wsm.getAccessSourceTypes(), "6") && GenericUtil.isEmpty(scd)){
					throw new IWBException("session","No Session",0,null, "No valid token", null);
				}
			}
			if(scd==null){
				scd = new HashMap();
				scd.put("userId",1);
				scd.put("locale", FrameworkCache.getAppSettingStringValue(0, "locale", "en"));
			}
			scd.put("projectId", projectId);
			scd.put("customizationId",po.getCustomizationId());
			Map requestParams = null;

			if(wsm.getDataAcceptTip()==2){//JSON
				JSONObject jo = HttpUtil.getJson(request);
				if(jo==null)jo = new JSONObject("{}");
				requestParams = new HashMap();
				if(wsm.getObjectType()==4)
					requestParams.put("_json", jo);
				else
					requestParams.putAll(GenericUtil.fromJSONObjectToMap(jo));
			} else 
				requestParams = GenericUtil.getParameterMap(request);
			String transactionId =  (String)request.getAttribute("_trid_");
			if(transactionId==null) transactionId = GenericUtil.getTransactionId();
			requestParams.put("_trid_", transactionId);
			if(FrameworkSetting.logType>0)LogUtil.logObject(new Log5Transaction(po.getProjectUuid(), "rest("+serviceName+"."+methodName+")", transactionId), true);

			W5FormResult fr=null; 
			switch(wsm.getObjectType()){
			case	0://show Record
				fr = service.getFormResult(scd, wsm.getObjectId(), 1, requestParams);
				response.getWriter().write(ext3_4.serializeGetFormSimple(fr).toString());
				response.getWriter().close();
				break;
			case	1://update Record by Form
			case	2://insert Record by Form
			case	3://delete Record by Form
				if(FrameworkSetting.liveSyncRecord4WS)requestParams.put(".w","ws-server");
				fr = service.postForm4Table(scd, wsm.getObjectId(), wsm.getObjectType(), requestParams, "");
				response.getWriter().write(ext3_4.serializePostForm(fr).toString());
				response.getWriter().close();		
				if (FrameworkSetting.liveSyncRecord4WS && fr.getErrorMap().isEmpty()){
					UserUtil.syncAfterPostFormAll(fr.getListSyncAfterPostHelper());
				}

				break;
			case	4://run Rhino
				response.getWriter().write(ext3_4.serializeGlobalFunc(service.executeFunc(scd, wsm.getObjectId(), requestParams
						, GenericUtil.hasPartInside2(wsm.getAccessSourceTypes(), "1") ? (short)1:(short)6)).toString());
				response.getWriter().close();
				break;
			case	19: //run Query
				W5QueryResult qr = service.executeQuery(scd, wsm.getObjectId(), requestParams);
				if(wsm.get_params()!=null){
					List<W5QueryField> lqf = new ArrayList();
					Map<String,W5QueryField> qfm = new HashMap();
					for(W5QueryField qf:qr.getQuery().get_queryFields()){
						qfm.put(qf.getDsc(), qf);
					}
					for(W5WsServerMethodParam wsmp:wsm.get_params())if(wsmp.getOutFlag()!=0 && wsmp.getParamType()!=10){
						lqf.add(qfm.get(wsmp.getDsc()));
					}
					qr.setNewQueryFields(lqf);								
				}
				response.getWriter().write(ext3_4.serializeQueryData(qr).toString());
				response.getWriter().close();
				break;
			case	31:case	32:case	33:
				throw new IWBException("ws","TODO",0,null, "Methods Not Implemented", null);
			}
		} catch (Exception e) {
			if(FrameworkSetting.debug)e.printStackTrace();
//			response.getWriter().write("{\"error\":\""+GenericUtil.stringToJS(e.getMessage())+"\"}");
			response.getWriter().write(new IWBException("framework","REST Def",0,null, "Error", e).toJsonString(request.getRequestURI(), null));
		}
	}
	
	@RequestMapping("/*/*")
	public void hndRESTRoot(
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("hndRESTRoot:"+request.getRequestURI());
		request.setCharacterEncoding( "UTF-8" );
		response.setCharacterEncoding( "UTF-8" );
		try {
			String[] u = request.getRequestURI().replace('/', ',').split(",");
			Map<String, String> requestParams = GenericUtil.getParameterMap(request);
			String token = requestParams.get("tokenKey");
			String projectId=u[2];
			W5Project po = FrameworkCache.getProject(projectId);
			if(po==null) {
				throw new IWBException("ws","Invalid Project",0,null, "Invalid Project", null);
			}
			String serviceName=u[3]; 
			response.setContentType("application/json");

			if(serviceName.endsWith(".json") || serviceName.endsWith(".JSON")){
				if(serviceName.endsWith(".json") || serviceName.endsWith(".JSON"))serviceName=serviceName.substring(0, serviceName.length()-5);
			    W5WsServer wss = FrameworkCache.getWsServer(projectId, serviceName);
				if(wss==null)throw new IWBException("rest","WS Not Found",0,serviceName, "WS Not Found", null);
				Map<String, Object> wsmoMap = service.getWsServerMethodObjects(wss);
				response.getWriter().write(serializeRestSwagger(wss, wsmoMap).toString());
				return;
			}  else if(serviceName.equals("ping")){
				response.getWriter().write("{\"success\":true,\"session\":" + !(GenericUtil.isEmpty(token)) + "}");
				return;
			}
			response.getWriter().write("{\"success\":false,\"error\":\"Wrong Request\"}");
		} catch (Exception e) {
			response.getWriter().write(new IWBException("framework","WADL Def",0,null, "Error", e).toJsonString(request.getRequestURI(), null));
		}
	}

			

	public	StringBuilder serializeRestSwagger(W5WsServer ws, Map<String, Object> wsmoMap){
		String[] elementTypes = new String[]{"string","string","string","float","integer","boolean","string","number","object","object","array"};

		StringBuilder buf = new StringBuilder();
		buf.append("{\"swagger\": \"2.0\",\"basePath\":\"/rest/").append(ws.getProjectUuid())
		.append("/").append(ws.getDsc()).append("\",\"paths\": {");
		StringBuilder definitions = new StringBuilder();
		boolean b = false;
		for(W5WsServerMethod wsm:ws.get_methods()){
			List<W5WsServerMethodParam> lwsmp = new ArrayList();
			if(wsm.getObjectType()!=4 || wsm.getObjectId()!=3){
			}
			if(b)buf.append(","); else b = true;
			String methodType = "get";
			switch(wsm.getObjectType()){
			case	1:methodType = "put";break;//update
			case	2:case 4:methodType = "post";break;//insert, globalFunc
			case	3:methodType = "delete";break;//update
			}
			String consumes = "application/json";
			switch(wsm.getDataAcceptTip()){
			case	1:case 3:consumes = "application/x-www-form-urlencoded";break;
//			case	2:consumes = "application/json";
			case	6:consumes = "application/x-yaml";break;
			}
			buf.append("\n\"/").append(wsm.getDsc()).append("\":{ \"").append(methodType).append("\":{\"produces\": [\"application/json\"],\"consumes\":[\"")
				.append(consumes).append("\"],\"parameters\": [");
//			buf.append("\n<method name=\"").append(wsm.getObjectTip()<19 ? "POST":"GET").append("\" id=\"").append(wsm.getDsc()).append("\">");
			
			W5Table t = null;
			Object o = wsmoMap.get(wsm.getDsc());
			if(o==null){//TODO ne yapilabilir?
				buf.append("]}}");
				continue;
			} else if(o instanceof String ){//TODO ne yapilabilir?
				buf.append("]}}");
				continue;
			}else switch(wsm.getObjectType()){
			case	0:case 1:case 2:case 3://TODO
				if(GenericUtil.isEmpty(wsm.getAccessSourceTypes()) || GenericUtil.hasPartInside(wsm.getAccessSourceTypes(), "1")) {
					W5WsServerMethodParam tokenKey =new W5WsServerMethodParam(-998, "tokenKey", (short)1);tokenKey.setOutFlag((short)0);tokenKey.setNotNullFlag((short)1);
					lwsmp.add(tokenKey);
				}
				W5FormResult fr=(W5FormResult)o;
				lwsmp.add(new W5WsServerMethodParam(-999, "result", (short)9));
				t = FrameworkCache.getTable(ws.getProjectUuid(), fr.getForm().getObjectId());
				if(wsm.getObjectType()!=2)for(W5TableParam tp:t.get_tableParamList())if(tp.getSourceType()==1)lwsmp.add(new W5WsServerMethodParam(tp, (short)(wsm.getObjectType()==2 ? 1:0),wsm.getObjectType()==2?-999:0));
				if(wsm.getObjectType()!=3)for(W5FormCell fc:fr.getForm().get_formCells())if(fc.getActiveFlag()!=0 && fc.get_sourceObjectDetail()!=null){
					lwsmp.add(new W5WsServerMethodParam(fc, (short)(wsm.getObjectType()==0 ? 1:0), wsm.getObjectType()==0 ? -999:0));
				}
				W5WsServerMethodParam outMsg =new W5WsServerMethodParam(-999, "outMsg", (short)1);outMsg.setParentWsMethodParamId(-999);
				lwsmp.add(outMsg);
				break;
			case	4:
				W5GlobalFuncResult dfr=(W5GlobalFuncResult)o;
				for(W5GlobalFuncParam dfp:dfr.getGlobalFunc().get_dbFuncParamList())if(dfp.getOutFlag()!=0){
					lwsmp.add(new W5WsServerMethodParam(-999, "result", (short)9));
					
					definitions.append("\"").append(wsm.getDsc()).append("Result\":{\"type\":\"object\",\"properties\":{");
					for(W5GlobalFuncParam dfp2:dfr.getGlobalFunc().get_dbFuncParamList())if(dfp2.getOutFlag()!=0){
							definitions.append("\"").append(dfp2.getDsc()).append("\":{\"type\":\"")
								.append(elementTypes[dfp2.getParamType()]).append("\"},");
					}
					if(definitions.charAt(definitions.length()-1)==',')
						definitions.setLength(definitions.length()-1);
					definitions.append("}}\n,");
					
					break;
				}
				if(dfr.getGlobalFunc().getGlobalFuncId()!=3 && (GenericUtil.isEmpty(wsm.getAccessSourceTypes()) || GenericUtil.hasPartInside(wsm.getAccessSourceTypes(), "1"))) {
					W5WsServerMethodParam tokenKey =new W5WsServerMethodParam(-998, "tokenKey", (short)1);tokenKey.setOutFlag((short)0);tokenKey.setNotNullFlag((short)1);
					lwsmp.add(tokenKey);
				}

				for(W5GlobalFuncParam dfp:dfr.getGlobalFunc().get_dbFuncParamList())if(dfp.getSourceType()==1 && dfp.getOutFlag()==0){
					lwsmp.add(new W5WsServerMethodParam(dfp, dfp.getOutFlag(), 0));
				}
				break;
			case	19:
				W5QueryResult qr=(W5QueryResult)o;
				if(qr.getQuery().getSourceObjectId()!=0)t = FrameworkCache.getTable(ws.getProjectUuid(), qr.getQuery().getSourceObjectId());
				if(!GenericUtil.isEmpty(wsm.get_params())) {
					lwsmp.addAll(wsm.get_params());
				} else {
					W5WsServerMethodParam tokenKey =new W5WsServerMethodParam(-998, "tokenKey", (short)1);tokenKey.setOutFlag((short)0);tokenKey.setNotNullFlag((short)1);
					lwsmp.add(tokenKey);
					lwsmp.add(new W5WsServerMethodParam(-999, "data", (short)10));
					for(W5QueryParam qp:qr.getQuery().get_queryParams())if(qp.getSourceType()==1){
					
						lwsmp.add(new W5WsServerMethodParam(qp, (short)0, 0));
					}
					for(W5QueryField qf:qr.getQuery().get_queryFields()){
						lwsmp.add(new W5WsServerMethodParam(qf, (short)1, -999));
					}
				}
				definitions.append("\"").append(wsm.getDsc()).append("Result\":{\"type\":\"object\",\"properties\":{\"data\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/");
				definitions.append(wsm.getDsc()).append("Data\"}}}}\n,\"").append(wsm.getDsc()).append("Data\":{\"type\":\"object\",\"properties\":{");
				for(W5WsServerMethodParam px:lwsmp)if(px.getDsc().equals("data") && px.getParamType()==10 && px.getOutFlag()!=0) {
					for(W5WsServerMethodParam px2:lwsmp)if(px2.getParentWsMethodParamId()==px.getWsServerMethodParamId()) {
						definitions.append("\"").append(px2.getDsc()).append("\":{\"type\":\"")
							.append(elementTypes[px2.getParamType()]).append("\"},");
					}
					break;
				}
				if(definitions.charAt(definitions.length()-1)==',')
					definitions.setLength(definitions.length()-1);
				definitions.append("}}\n,");
				break;
				
			
			}
			wsm.set_params(lwsmp);
		
//			buf.append("\n<request>");
			for(W5WsServerMethodParam wsmp:wsm.get_params())if(wsmp.getOutFlag()==0 && wsmp.getParentWsMethodParamId()==0){
				buf.append("\n{\"in\":\"formData\"").append(wsmp.getNotNullFlag()==0 ? "":" ,\"required\":true").append(",\"name\":\"")
				.append(wsmp.getDsc()).append("\",\"type\":\"");
				buf.append(elementTypes[wsmp.getParamType()]);
				buf.append("\"},");
			}
			if(buf.charAt(buf.length()-1)==',')buf.setLength(buf.length()-1);
			buf.append("]");
			
			buf.append("\n,\"responses\":{\"200\":{\"schema\":{");
			for(W5WsServerMethodParam wsmp:wsm.get_params())if(wsmp.getOutFlag()!=0 && wsmp.getParentWsMethodParamId()==0){
//				buf.append("\n{\"name\":\"")
				if(wsmp.getOutFlag()!=0) {
					if(wsmp.getDsc().equals("data") && wsmp.getParamType()==10 )
						buf.append("\"$ref\":\"#/definitions/").append(wsm.getDsc()).append("Result\"");
					else if(wsmp.getDsc().equals("result") && wsmp.getParamType()==9 ) 
						buf.append("\"$ref\":\"#/definitions/").append(wsm.getDsc()).append("Result\"");
				} 
				break;
			}
			buf.append("}}");
//			if(buf.charAt(buf.length()-1)==',')buf.setLength(buf.length()-1);
			buf.append("}}}");
		}
		buf.append("}");
		if(definitions.length()>0) {
			definitions.setLength(definitions.length()-1);
			buf.append(",\n\"definitions\":{").append(definitions).append("}");
		}
		buf.append("}");
		return buf;
		

	}
}
