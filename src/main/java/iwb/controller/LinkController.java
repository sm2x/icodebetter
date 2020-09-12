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
@RequestMapping("/l")
public class LinkController implements InitializingBean {
	private static Logger logger = Logger.getLogger(LinkController.class);

	
	@Override
	public void afterPropertiesSet() throws Exception {
		
	}
	@Autowired
	private FrameworkService service;
	
	@RequestMapping("/create")
	public void hndCreateLink( //project/service/method
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException{
		request.setCharacterEncoding( "UTF-8" );
		response.setCharacterEncoding( "UTF-8" );
		response.setContentType("application/json");
		try {
			Map m = GenericUtil.getParameterMap(request);
			String url = (String)m.get("url");
			
			if(GenericUtil.isEmpty(url)) {// || !url.startsWith("/preview/")
				response.getWriter().write("{\"success\":false,\"error\":\"Wrong link\"}");
				return;
			}

			String linkId = service.generateLinkFromUrl(url);
			if(linkId==null) {
				response.getWriter().write("{\"success\":false,\"error\":\"Wrong link2\"}");
				return;
			}	
			FrameworkCache.addUrlToLinkCache(linkId, url);
			response.getWriter().write("{\"success\":true,\"link\":\""+linkId+"\"}");

		} catch(Exception e) {
			response.getWriter().write("{\"success\":false,\"error\":\"Error :" + e.getMessage()+"\"}");		
		}

	}
	
	
	@RequestMapping("/*")
	public void hndLink( //project/service/method
			HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException{
		request.setCharacterEncoding( "UTF-8" );
		response.setCharacterEncoding( "UTF-8" );
		try {
			String[] u = request.getRequestURI().replace('/', ',').split(",");
			String linkId=u[2]; 
			
			String url = FrameworkCache.getUrlFromLinkId(linkId);
			if(url==null) {
				url = service.loadLink(linkId);
				FrameworkCache.addUrlToLinkCache(linkId, url);
			}
			if(url.equals("-")) {
				response.getWriter().write("Wrong link");
				return;
			}
			response.sendRedirect("/preview/"+url);
		} catch(Exception e) {
			response.getWriter().write("Error :" + e.getMessage());			
		}

	}
}
