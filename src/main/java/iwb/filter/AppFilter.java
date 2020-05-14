package iwb.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.NestedServletException;

import iwb.cache.FrameworkSetting;
import iwb.domain.db.Log5Transaction;
import iwb.domain.db.Log5VisitedPage;
import iwb.exception.IWBException;
import iwb.exception.Log5IWBException;
import iwb.util.GenericUtil;
import iwb.util.LogUtil;

@Component
@WebFilter(urlPatterns = {"/app","/preview"})
public class AppFilter implements Filter {
//	public static int	transactionCount = 0;
	
	
	public void destroy() {
	}

	public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {
		request.setCharacterEncoding( "UTF-8" );
		response.setCharacterEncoding( "UTF-8" );
		if(true){ //mobile
			HttpServletResponse resp2 = (HttpServletResponse)response;
			resp2.addHeader("Access-Control-Allow-Origin", "*");
			resp2.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
			resp2.addHeader("Access-Control-Allow-Methods", "GET,JSON,PUT");
		}
		String uri = ((HttpServletRequest) request).getRequestURI();
		boolean jsonFlag = !uri.contains(".htm") && !uri.contains("/grd/") && !uri.contains("/rpt/") && !uri.contains("/jasper/") && !uri.contains("/dl/") && !uri.contains("ajaxXmlQueryData") && !uri.contains("validateLicense");
		switch(FrameworkSetting.systemStatus){
		case	0://working
			Log5VisitedPage lvp = null;
			Map scd = null;
			try {
				String transactionId =  GenericUtil.getTransactionId();
				request.setAttribute("_trid_", transactionId);
				HttpSession session = ((HttpServletRequest)request).getSession(false);
				scd = session!=null ? (Map)session.getAttribute("scd-dev"): null;
				if(FrameworkSetting.logType>0 && (uri.contains("/app/") || uri.contains("/preview/"))){
					if(scd!=null) {
						String[] uuri = uri.split("/");
						
						if(uuri.length>1) {
							String newUri = uuri[uuri.length-1];
							if(!newUri.equals("ajaxLiveSync")) {
								LogUtil.logObject(new Log5Transaction((String)scd.get("projectId"), newUri, transactionId), true);

								int pageId = 0;
								if(newUri.equals("ajaxQueryData"))pageId=GenericUtil.uInt((HttpServletRequest) request, "_qid");
								else if(newUri.equals("ajaxExecDbFunc"))pageId=GenericUtil.uInt((HttpServletRequest) request, "_did");
								else if(newUri.equals("showForm") || uri.equals("ajaxPostForm"))pageId=GenericUtil.uInt((HttpServletRequest) request, "_fid");
								else if(newUri.equals("showPage"))pageId=GenericUtil.uInt((HttpServletRequest) request, "_tid");
								else if(newUri.startsWith("pic") && newUri.endsWith(".png")) {
									pageId=GenericUtil.uInt(newUri.substring(3,newUri.length()-4));
									newUri = "showPic";
								}
								lvp = new Log5VisitedPage(scd, newUri, pageId, request.getRemoteAddr(), transactionId);
								
							}
						}
					}
				}
				filterChain.doFilter( request, response );
			} catch (NestedServletException e) {
				if(FrameworkSetting.debug) {
					if(e.getMessage().contains("session"))System.out.println("No session: " + ((HttpServletRequest)request).getRequestURI());
					else e.printStackTrace();				
				}
				response.setCharacterEncoding( "UTF-8" );
				response.setContentType(jsonFlag ? "application/json" : "text/html");
				Exception te = e;
				IWBException iw = null; 
				while(te.getCause()!=null && te.getCause() instanceof Exception){
					te = (Exception)te.getCause();
					if(te instanceof IWBException)break;
				}
				if(te!=null && te instanceof  IWBException)iw = (IWBException)te;
				else iw = new IWBException("framework","Unknown", 0, null, "Root Cause --> " + GenericUtil.stringToJS2(te.getMessage()), e);
				
				StringBuilder b = new StringBuilder();
				if(jsonFlag){
					boolean z = false;
					if(uri.contains("showPage") || uri.contains("showForm")){
						b.append("ajaxErrorHandler(");
						z = true;
					}
					b.append(iw.toJsonString(uri, scd));
					
					if(z)b.append(")");
				} else { //
					b.append(iw.toHtmlString(scd));
				}
				
				if(FrameworkSetting.log2tsdb){
					LogUtil.logObject(new Log5IWBException(scd, ((HttpServletRequest) request).getRequestURI(), GenericUtil.getParameterMap((HttpServletRequest)request), request.getRemoteAddr(), iw), true);
				}
				
				try{ response.getWriter().write(b.toString());
				} catch (Exception e2) {}
			} catch (Exception e) {
				if(FrameworkSetting.debug)e.printStackTrace();
				try{ response.getWriter().write("{\"success\":false,\n\"errorType\":\"framework\",\n\"error\":\"Unhandled2 -->"+GenericUtil.stringToJS2(e.getMessage()) + (FrameworkSetting.debug ? ("\",\n\"stack\":\""+GenericUtil.stringToJS2(ExceptionUtils.getFullStackTrace(e))) : "" )+"\"}");
				} catch (Exception e2) {}
			} finally {
				if(FrameworkSetting.logType>0 && lvp!=null){
					lvp.calcProcessTime();
					LogUtil.logObject(lvp, false);
				}				
			}
			break;
		case 1://backup
			if(jsonFlag)response.getWriter().write("{\"success\":false,\n\"errorType\":\"framework\",\n\"error\":\"Backup\"}");
			else response.getWriter().write("<b>System Backup</b>");
			break;
		case 2://suspended
			if(jsonFlag)response.getWriter().write("{\"success\":false,\n\"errorType\":\"framework\",\n\"error\":\"System Suspended\"}");
			else response.getWriter().write("<b>System Suspended</b>");
			break;
		}
		//else filterChain.doFilter( request, response );
	}
	public void init(FilterConfig arg0) throws ServletException {
	}
}
