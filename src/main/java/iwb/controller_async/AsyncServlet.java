package iwb.controller_async;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.domain.db.W5Project;
import iwb.domain.helper.W5DeferredResult;
import iwb.exception.IWBException;
import iwb.util.GenericUtil;
import iwb.util.UserUtil;

@Controller
@RequestMapping("/async")
public class AsyncServlet {
	private static Logger logger = Logger.getLogger(AsyncServlet.class);


	@RequestMapping("/ajaxNotifications")
	@ResponseBody
	public W5DeferredResult hndAjaxNotifications(HttpServletRequest request,
			HttpServletResponse response) {
		if(false && FrameworkSetting.debug) {
			String p = request.getParameter(".p");
			if(p!=null) {
					logger.info("LongPoll - " + request.getParameter(".p") + " - " + FrameworkCache.getProject(request.getParameter(".p")).getCustomizationId());		
			}
			else logger.info("LongPoll - " + request.getParameter(".w"));		
		}
		Map<String, Object> scd = null;
		try {
			scd =  false && GenericUtil.uInt(request,"_preview")!=0 ? UserUtil.getScd4Preview(request, "scd-dev", false):UserUtil.getScd(request, "scd-dev", false);
		} catch(IWBException e){
			try {
				response.getWriter().write(e.toJsonString(null));
				response.getWriter().close();
				
				if(false && FrameworkSetting.debug) {
					String p = request.getParameter(".p");
					if(p!=null) {
						W5Project po2 = FrameworkCache.getProject(request.getParameter(".p"));
						logger.info("LongPollError - " + po2.getCustomizationId() + " - " + request.getParameter(".p") + " - " + po2.getDsc());		
					}
					else logger.info("LongPollError - " + request.getParameter(".w"));		
				}
			} catch (IOException e1) {
				if(FrameworkSetting.debug)e1.printStackTrace();
			}
			return null;
		}
		final String projectId = (String)scd.get("projectId");
		final Integer userId = (Integer)scd.get("userId");
		final String webPageId = request.getParameter(".w");
/*		final String webPageId2 = GenericUtil.uInt(scd.get("mobile"))!=0 ? (String)scd.get("mobileDeviceId") : request.getParameter(".w");
		if(!webPageId.equals(webPageId2)){
			throw new IWBException("framework", "WebPageId", 0, null, "Wrong WebPageID", null);
		}
		*/
		final String activeTabId = request.getParameter(".at");
		String rpid = request.getParameter(".p");
		if(GenericUtil.isEmpty(rpid))rpid = projectId;
		else if(!GenericUtil.safeEquals(rpid, scd.get("projectId"))){
		//	deferredResult.setResult("{\"success\":true, \"changeProjectId\":\""+scd.get("projectId")+"\"}"); //TODO
		}

		final W5DeferredResult deferredResult = new W5DeferredResult(rpid, userId, webPageId, FrameworkSetting.asyncTimeout*1000L, Collections.emptyList());
		
		
		 /*	
	    if(UserUtil.wLongPollRequests.containsKey(userId)) {
	    	UserUtil.wLongPollRequests.get(userId).add(deferredResult);
	    } else {
	    	UserUtil.wLongPollRequests.put(userId, new ArrayList<W5DeferredResult>());
	    	UserUtil.wLongPollRequests.get(userId).add(deferredResult);
	    }
	 
	   deferredResult.onCompletion(new Runnable() {
	        @Override
	        public void run() {
	        	UserUtil.wLongPollRequests.get(userId).remove(deferredResult);
	        }
	    });*/
	    
	    deferredResult.onTimeout(new Runnable() {
	        @Override
	        public void run() {
	        	deferredResult.setResult("{\"success\":true}");
	        }
	    });

	    Object o = UserUtil.addDeferredResult(rpid, userId, request.getSession().getId(), webPageId, activeTabId, scd, deferredResult);
	    
	    if(FrameworkSetting.liveSyncRecord && !GenericUtil.isEmpty(webPageId)){
	    	int	cnt =  GenericUtil.uInt(request, ".c");
	    	long now = System.currentTimeMillis();
	    	for(int i=0;i<cnt;i++){
	    		String	s = request.getParameter(".s"+i);
	    		if(!GenericUtil.isEmpty(s)){
	    			String[] sa = s.split(";");
	    			String key = sa[0];
	    			String tabId = sa[1];
	    			short syncTip = sa.length>2 ? GenericUtil.uShort(sa[2]) : 0; //0:yok, 1:var, 2:hybrid
	    			
	    			UserUtil.syncRecordEditMap( rpid,  key, userId, webPageId, tabId,  now,  syncTip);
	    			
					
	    		}
	    	}
	    	
	    }
	    return deferredResult;
	}
}
