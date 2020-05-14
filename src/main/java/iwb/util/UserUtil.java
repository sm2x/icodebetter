/*
 * Created on 21.Mar.2005
 *
 */
package iwb.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.domain.db.Log5Notification;
import iwb.domain.db.W5Project;
import iwb.domain.helper.W5DeferredResult;
import iwb.domain.helper.W5QueuedPushMessageHelper;
import iwb.domain.helper.W5SynchAfterPostHelper;
import iwb.exception.IWBException;

class	SyncGridMapHelper3{
	private	Set<Integer> keys;
	private	Map	requestParams;
	private	int	tableId;
	private	int	gridId;
	public Set<Integer> getKeys() {
		return keys;
	}
	public void setKeys(Set<Integer> keys) {
		this.keys = keys;
	}
	public Map getRequestParams() {
		return requestParams;
	}
	public void setRequestParams(Map requestParams) {
		this.requestParams = requestParams;
	}
	public int getTableId() {
		return tableId;
	}
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
	public int getGridId() {
		return gridId;
	}
	public void setGridId(int gridId) {
		this.gridId = gridId;
	}
	public SyncGridMapHelper3(int tableId, int gridId) {
		super();
		this.tableId = tableId;
		this.gridId = gridId;
		this.keys = new HashSet();
	}
	
	
}

class	SyncTabMapHelper3{
	private	List<Map>	messages;
	private	long	lastActionTime;
	private	String	webPageId;
	private	String	tabId;
	private	int		userId;
	int	messageCount=0;
	private	short		syncTip;
	private	Map<Integer, SyncGridMapHelper3>	gridMap; // TODO simdilik boyle ama ilerde detay gridleri daha duzgun tutacagiz
	//gridId/formCellId, keySet
	
	
	public SyncTabMapHelper3() {
		super();
	}
	public short getSyncTip() {
		return syncTip;
	}
	public void setSyncTip(short syncTip) {
		this.syncTip = syncTip;
	}
	public SyncTabMapHelper3(String webPageId, String tabId, int userId) {
		super();
		this.webPageId = webPageId;
		this.tabId = tabId;
		this.userId = userId;
	}
	public List getMessages(boolean	clear) {
		if(clear){
			messageCount = 0;
			List z = messages;
			messages = null;
			return z;
		}else
			return messages;
	}
	
	public int getMessageCount() {
		return messageCount;
	}
	public void addMessages(Map msg) {
		messageCount++;
		if(messages==null)messages= new ArrayList();
		else	{
			int	grFcId1 =  (Integer)msg.get(msg.containsKey("gridId") ? "gridId" : "formCellId");
			for(Map m:messages){
				int	grFcId2 =  (Integer)m.get(m.containsKey("gridId") ? "gridId" : "formCellId");
				if(grFcId1==grFcId2){
					m.putAll(msg);
					return;
				}
			}
		}
		messages.add(msg);
	}

	public long getLastActionTime() {
		return lastActionTime;
	}
	public void setLastActionTime(long lastActionTime) {
		this.lastActionTime = lastActionTime;
	}
	public String getWebPageId() {
		return webPageId;
	}
	public void setWebPageId(String webPageId) {
		this.webPageId = webPageId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public Map<Integer, SyncGridMapHelper3> getGridMap() {
		return gridMap;
	}
	public void setGridMap(Map<Integer, SyncGridMapHelper3> loadedKeys) {
		this.gridMap = loadedKeys;
	}
	public String getTabId() {
		return tabId;
	}
	public void setTabId(String tabId) {
		this.tabId = tabId;
	}
	
	
	
}

class SyncWebPageMapHelper3{
	private	long lastAsyncActionTime;
	private	long lastActionTime;
	private	String activeTab;
	//private	Map<String, Map<Integer, SyncTableRecordMapHelper3>> syncTableRecordMap; //tabId, gridId
	private	Map<String, SyncTabMapHelper3> syncTabMap;
	private	W5DeferredResult deferredResult;	
	private	List<String> toBeBroadcast;
	
	
	public long getLastAsyncActionTime() {
		return lastAsyncActionTime;
	}
	public void setLastAsyncActionTime(long lastAsyncActionTime) {
		this.lastAsyncActionTime = lastAsyncActionTime;
	}
	public long getLastActionTime() {
		return lastActionTime;
	}
	public void setLastActionTime(long lastActionTime) {
		this.lastActionTime = lastActionTime;
	}

	public W5DeferredResult getDeferredResult() {
		return deferredResult;
	}
	public void setDeferredResult(W5DeferredResult deferredResult) {
		this.deferredResult = deferredResult;
	}
	public List<String> getToBeBroadcast() {
		return toBeBroadcast;
	}
	public void setToBeBroadcast(List<String> toBeBroadcast) {
		this.toBeBroadcast = toBeBroadcast;
	}
	public void addToBeBroadcast(String s) {
		if(lastAsyncActionTime > System.currentTimeMillis() - FrameworkSetting.asyncToleranceTimeout){
			if(this.toBeBroadcast==null)this.toBeBroadcast = new ArrayList();
			this.toBeBroadcast.add(s);
		} else this.toBeBroadcast=null;
	}

	public	int	broadCast(Map m){
		if(deferredResult!=null && !deferredResult.isSetOrExpired()){
			deferredResult.setResult(GenericUtil.fromMapToJsonString2Recursive(m));
			return	1;
		} else {
			addToBeBroadcast(GenericUtil.fromMapToJsonString2Recursive(m));
			return	0;
		}
	}
	public	int	broadCast(String s){
		if(deferredResult!=null && !deferredResult.isSetOrExpired()){
			deferredResult.setResult(s);
			return	1;
		} else {
			addToBeBroadcast(s);
			return	0;
		}
	}
	public boolean hasToBeBroadcast() {
		return !GenericUtil.isEmpty(toBeBroadcast);
	}
	public synchronized String  getFIFOBroadcast() {
		if(!GenericUtil.isEmpty(toBeBroadcast)){
			String s = this.toBeBroadcast.get(0);
			this.toBeBroadcast.remove(0);
			return s;
		}
		return null;
	}
	public String getActiveTab() {
		return activeTab;
	}
	public void setActiveTab(String activeTab) {
		this.activeTab = activeTab;
	}
	
	public Map<String, SyncTabMapHelper3> getSyncTabMap() {
		return syncTabMap;
	}
	public void setSyncTabMap(Map<String, SyncTabMapHelper3> syncTabMap) {
		this.syncTabMap = syncTabMap;
	}
	
	
}

class SyncSessionMapHelper3{
	private	long lastActionTime;
	private	long lastAsyncActionTime;
	private String lastUrl;
	private Map scd;
	private String remoteIP;
//	private String sessionId;
	private short deviceType;//0:browser, 1: iphone/ipad, 2:android, 3:windows
//	private String mobileDeviceId;//aksi
	private	Map<String, SyncWebPageMapHelper3> syncWebPageMap; //webPageId
	
	
	public SyncSessionMapHelper3(String lastUrl, String remoteIP, Map scd) {
		super();
		this.lastUrl = lastUrl;
		this.scd = scd;
		if(scd.containsKey("mobile"))this.deviceType= (short)GenericUtil.uInt(scd.get("mobile"));
		this.remoteIP = remoteIP;
		this.lastActionTime = System.currentTimeMillis();
	}
	public SyncSessionMapHelper3() {
		super();
	}
	public long getLastActionTime() {
		return lastActionTime;
	}
	public void setLastActionTime(long lastActionTime) {
		this.lastActionTime = lastActionTime;
	}
	public long getLastAsyncActionTime() {
		return lastAsyncActionTime;
	}
	public void setLastAsyncActionTime(long lastAsyncActionTime) {
		this.lastAsyncActionTime = lastAsyncActionTime;
	}
	public String getLastUrl() {
		return lastUrl;
	}
	public void setLastUrl(String lastUrl) {
		this.lastUrl = lastUrl;
	}
	public Map getScd() {
		return scd;
	}
	public void setScd(Map scd) {
		this.scd = scd;
	}
	public String getRemoteIP() {
		return remoteIP;
	}
	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	public short getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(short deviceType) {
		this.deviceType = deviceType;
	}
/*	public String getDeviceToken() {
		return mobileDeviceId;
	}
	public void setDeviceToken(String mobileDeviceId) {
		this.mobileDeviceId = mobileDeviceId;
	} */
	public Map<String, SyncWebPageMapHelper3> getSyncWebPageMap() {
		return syncWebPageMap;
	}
	public void setSyncWebPageMap(Map<String, SyncWebPageMapHelper3> syncWebPageMap) {
		this.syncWebPageMap = syncWebPageMap;
	}
	
	
}

class CachedUserBean3{
	private	long lastActionTime;
	private	long lastAsyncActionTime;
	private	long lastMobileActionTime;
	private String userName = "";
	private String dsc = "";
	private int profilePictureId = 0;
	private	short chatStatusTip;
	private	boolean canMultiLogin;
//	private	Map<String, SyncSessionMapHelper3> syncSessionMap; //sessionId/mobileDeviceId,
	private	Map<String, SyncSessionMapHelper3> syncSessionMap; //sessionId/mobileDeviceId,

	public	String	findSessionIdFromWebPageId(String	webPageId){
		if(webPageId==null || getSyncSessionMap()==null)return null;
		for(Map.Entry<String, SyncSessionMapHelper3> sessionEntry:getSyncSessionMap().entrySet())if(sessionEntry.getValue().getSyncWebPageMap()!=null){
			for(String wpi:sessionEntry.getValue().getSyncWebPageMap().keySet())if(wpi.equals(webPageId)){
				return	sessionEntry.getKey();
			}
		}
		return	null;
	}
	
	public long getLastMobileActionTime() {
		return lastMobileActionTime;
	}

	public void setLastMobileActionTime(long lastMobileActionTime) {
		this.lastMobileActionTime = lastMobileActionTime;
	}

	public CachedUserBean3(String userName, String dsc, boolean canMultiLogin) {
		super();
		this.userName = userName;
		this.dsc = dsc;
		this.canMultiLogin = canMultiLogin;
	}

	public long getLastActionTime() {
		return lastActionTime;
	}

	public void setLastActionTime(long lastActionTime) {
		this.lastActionTime = lastActionTime;
	}

	public long getLastAsyncActionTime() {
		return lastAsyncActionTime;
	}

	public void setLastAsyncActionTime(long lastAsyncActionTime) {
		this.lastAsyncActionTime = lastAsyncActionTime;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDsc() {
		if(!FrameworkSetting.showOnlineStatus || chatStatusTip==0)return dsc;
		long limitTime = System.currentTimeMillis() - FrameworkSetting.asyncToleranceTimeout;
		return getLastAsyncActionTime()>limitTime?(dsc+"·"):dsc;
		
	}

	public void setDsc(String dsc) {
		this.dsc = dsc;
	}

	public int getProfilePictureId() {
		return profilePictureId;
	}

	public void setProfilePictureId(int profilePictureId) {
		this.profilePictureId = profilePictureId;
	}

	public short getChatStatusTip() {
		return chatStatusTip;
	}

	public void setChatStatusTip(short chatStatusTip) {
		this.chatStatusTip = chatStatusTip;
	}

	public boolean isCanMultiLogin() {
		return canMultiLogin;
	}

	public void setCanMultiLogin(boolean canMultiLogin) {
		this.canMultiLogin = canMultiLogin;
	}

	public Map<String, SyncSessionMapHelper3> getSyncSessionMap() {
		return syncSessionMap;
	}

	public void setSyncSessionMap(Map<String, SyncSessionMapHelper3> syncMap) {
		this.syncSessionMap = syncMap;
	}

	public int broadCast(Map m) {
		int count = 0;
		if(this.syncSessionMap!=null)for(Map.Entry<String, SyncSessionMapHelper3> sessionEntry:this.syncSessionMap.entrySet()){
//			if(sessionEntry.getValue().getDeviceType()==0){ // browser
			SyncSessionMapHelper3 sess4user = sessionEntry.getValue();
			int subCount = 0;
			if(sess4user.getSyncWebPageMap()!=null)for(SyncWebPageMapHelper3 wp:sess4user.getSyncWebPageMap().values())
				subCount+=wp.broadCast(m);
//			if(subCount==0)if(sess4user.getDeviceType()!=0)count+=UserUtil.sendMobilePushNotification(sess4user.getDeviceType(), sessionEntry.getKey(), m);
			else count += subCount;
//			}
		}
		return count;
	}
	
	

}

class DeviceSyncHelper3{
	private	int	userId;
	private short deviceType;
	private	String	mobileDeviceId;
	private	long lastMobileActionTime;
	private short status; //0:inactive, 1:active/resumed, 2: paused

	
	public DeviceSyncHelper3(int userId, String mobileDeviceId, long lastMobileActionTime, short deviceType) {
		super();
		this.userId = userId;
		this.mobileDeviceId = mobileDeviceId;
		this.lastMobileActionTime = lastMobileActionTime;
		this.deviceType = deviceType;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}


	
	public String getMobileDeviceId() {
		return mobileDeviceId;
	}
	public void setMobileDeviceId(String mobileDeviceId) {
		this.mobileDeviceId = mobileDeviceId;
	}
	public long getLastMobileActionTime() {
		return lastMobileActionTime;
	}
	public void setLastMobileActionTime(long lastMobileActionTime) {
		this.lastMobileActionTime = lastMobileActionTime;
	}
	public short getStatus() {
		return status;
	}
	public void setStatus(short status) {
		this.status = status;
	}
	public short getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(short deviceType) {
		this.deviceType = deviceType;
	}
	
	
}

public class UserUtil {
	public static String androidSenderId = "553372575530";
	public static String androidAPIKey = "AIzaSyBTET2hfQa_6AGQy5ErILBz9IFBAF3tx3E";
	private static int androidSendRetryCount = 1;
//	final private static Map<Integer, Map<String, OnlineUserBean2>> lastUserAction= new ConcurrentHashMap<Integer, Map<String, OnlineUserBean2>>();
	final private static Map<String, Set<Integer>> projectMap3 = new HashMap<String, Set<Integer>>();//users of project
	final private static Map<Integer, CachedUserBean3> userMap3 = new HashMap<Integer, CachedUserBean3>();
	final private static Map<String, DeviceSyncHelper3> deviceMap3 = new HashMap();//mobileDeviceId, 
	final private static Map<String, Map<String, Map<Integer, Map<String, SyncTabMapHelper3>>>> recordEditMap3 = new HashMap<String, Map<String, Map<Integer, Map<String, SyncTabMapHelper3>>>>();
	//projectId, key, userId, webPageId
	final private static Map<String, Map<Integer, Map<String, SyncTabMapHelper3>>> gridSyncMap3 = new HashMap<String, Map<Integer, Map<String, SyncTabMapHelper3>>>();
	//projectId, tableId, tabId, 
	

	public static void clearDevices(){
		deviceMap3.clear();
	}
	
	public static void addProjectUser(String projectId, int userId){
		Set<Integer> s = projectMap3.get(projectId);
		if(s==null){
			s = new HashSet();
			projectMap3.put(projectId, s);
		}
		s.add(userId);
	}
	
	public static void addProjectUsers(String projectId, String userIds){
		Set<Integer> s = projectMap3.get(projectId);
		if(s==null){
			s = new HashSet();
			projectMap3.put(projectId, s);
		}
		String[] lu = userIds.split(",");
		for(String u:lu)s.add(GenericUtil.uInteger(u));
	}
	
	public static void addDevice(String id, int userId, String projectId, short deviceType, long t){
		deviceMap3.put(id, new DeviceSyncHelper3(userId, id, t, deviceType));
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return;
		if(cub.getSyncSessionMap()==null)cub.setSyncSessionMap(new HashMap());
		SyncSessionMapHelper3 ses = cub.getSyncSessionMap().get(id);
		if(ses==null){
			Map scd = new HashMap();scd.put("projectId", projectId);scd.put("userId", userId);
			ses = new SyncSessionMapHelper3(); ses.setDeviceType(deviceType);ses.setScd(scd);
			cub.getSyncSessionMap().put(id, ses);
			if(cub.getChatStatusTip()==0)cub.setChatStatusTip((short)1);
		}
	}
	
	public static int broadCastRecordForTemplates(String projectId,
			int tableId, String key, int action, int actionUserId) {
		if(key==null)return 0;
		int intKey = key.indexOf('-')>0 ? GenericUtil.uInt(key.split("-")[1]) : -1;
		if(intKey==0)return 0;
		Map<Integer, Map<String, SyncTabMapHelper3>>	tblMap = gridSyncMap3.get(projectId);
		if(tblMap==null)return 0;
		Map<String, SyncTabMapHelper3> tabMap = tblMap.get(tableId);
		if(tabMap==null)return 0;
		long limitTime = System.currentTimeMillis() - FrameworkSetting.asyncToleranceTimeout;
		int count=0;
		for(String tabKey:tabMap.keySet()){
			SyncTabMapHelper3 tab = tabMap.get(tabKey);
			if(/*actionUserId!=tab.getUserId() && */true || tab.getLastActionTime()>limitTime){
				int msgCount = tab.getMessageCount();
				if(msgCount<FrameworkSetting.liveSyncMaxMessage4Tpl){
					Map	msg = null;
					if(/*intKey!=0 && */tab.getGridMap()!=null)for(SyncGridMapHelper3 g:tab.getGridMap().values()){
						if(g.getTableId()==tableId && (((action ==1 || action== 3 ) && g.getKeys()!=null && g.getKeys().contains(intKey)) || action==2)){
							msg = new HashMap();
							if(action!=2)msg.put("key", intKey);
							msg.put("crudAction", action);
							msg.put("userId", actionUserId);
							msg.put("userDsc", getUserDsc(actionUserId));
							if(g.getGridId()>0)
								msg.put("gridId", g.getGridId());
							else
								msg.put("formCellId", -g.getGridId());
							msg.put("time", System.currentTimeMillis());
							tab.addMessages(msg);					
							break;
						}
					}
					if(msg!=null){
						//TODO tab.addMessages("Mazgallar kirlendi! ");//x grid'e yeni kayit, düzenlendi, silindi. form'daki formcell değişti
						CachedUserBean3 cub = getCachedUserBean(tab.getUserId());
						if(cub.getSyncSessionMap()!=null)for(SyncSessionMapHelper3 ses:cub.getSyncSessionMap().values())if(ses.getSyncWebPageMap()!=null)for(String wps:ses.getSyncWebPageMap().keySet())/*if(!wps.equals(webPageId))*/{
							SyncWebPageMapHelper3 wp = ses.getSyncWebPageMap().get(wps);
							Map m = new HashMap();
							m.put("success", true);
							m.put("liveSyncAction", 15);
							m.put("tabId", tabKey);
							m.put("msgCount", msgCount<FrameworkSetting.liveSyncMaxMessage4Tpl ? (msgCount+1) : ("+"+msgCount));
							if(ses.getDeviceType()==0){
								if(wp.getActiveTab()!=null && wp.getActiveTab().equals(tabKey)){
									m.put("msg", msg);	
								}
							} else {
								m.putAll(msg);
							}
							ses.getSyncWebPageMap().get(wps).broadCast(m);				
						}
					}
				}
			}
		}
		return count;
	}
	
	public static int broadCastRecordForTemplatesOld(String projectId,
			int tableId, String key, int action, int actionUserId) {
		if(key==null)return 0;
		int intKey = key.indexOf('-')>0 ? GenericUtil.uInt(key.split("-")[1]) : -1;
		if(intKey==0)return 0;
		Map<Integer, Map<String, SyncTabMapHelper3>>	tblMap = gridSyncMap3.get(projectId);
		if(tblMap==null)return 0;
		Map<String, SyncTabMapHelper3> tabMap = tblMap.get(tableId);
		if(tabMap==null)return 0;
		long limitTime = System.currentTimeMillis() - FrameworkSetting.asyncToleranceTimeout;
		int count=0;
		for(String tabKey:tabMap.keySet()){
			SyncTabMapHelper3 tab = tabMap.get(tabKey);
			//if(/*actionUserId!=tab.getUserId() && */true || tab.getLastActionTime()>limitTime){
				int msgCount = tab.getMessageCount();
				if(msgCount<FrameworkSetting.liveSyncMaxMessage4Tpl){
					Map	msg = null;
					if(/*intKey!=0 && */tab.getGridMap()!=null)for(SyncGridMapHelper3 g:tab.getGridMap().values()){
						if(g.getTableId()==tableId && (((action ==1 || action== 3 ) && g.getKeys()!=null && g.getKeys().contains(intKey)) || action==2)){
							msg = new HashMap();
							if(action!=2)msg.put("key", intKey);
							msg.put("crudAction", action);
							msg.put("userId", actionUserId);
							msg.put("userDsc", getUserDsc(actionUserId));
							if(g.getGridId()>0)
								msg.put("gridId", g.getGridId());
							else
								msg.put("formCellId", -g.getGridId());
							msg.put("time", System.currentTimeMillis());
							tab.addMessages(msg);					
							break;
						}
					}
					if(msg!=null){
						//TODO tab.addMessages("Mazgallar kirlendi! ");//x grid'e yeni kayit, düzenlendi, silindi. form'daki formcell değişti
						CachedUserBean3 cub = getCachedUserBean(tab.getUserId());
						if(cub.getSyncSessionMap()!=null)
							for(SyncSessionMapHelper3 ses:cub.getSyncSessionMap().values())
								if(ses.getSyncWebPageMap()!=null)
									for(String wps:ses.getSyncWebPageMap().keySet())/*if(!wps.equals(webPageId))*/
									{
										SyncWebPageMapHelper3 wp = ses.getSyncWebPageMap().get(wps);
										Map m = new HashMap();
										m.put("success", true);
										m.put("liveSyncAction", 15);
										m.put("tabId", tabKey);
										m.put("msgCount", msgCount<FrameworkSetting.liveSyncMaxMessage4Tpl ? (msgCount+1) : ("+"+msgCount));
										if(wp.getActiveTab()!=null && wp.getActiveTab().equals(tabKey)){
											m.put("msg", msg);	
										}
										//ses.getSyncWebPageMap().get(wps).broadCast(m);				
									}
					}
				}
			//}
		}
		return count;
	}
	
	public static SyncTabMapHelper3 getTableTab(String projectId, int tableId, int userId,
			String sessionId, String webPageId, String tabId){
		if(userId==0 ||  webPageId==null || tabId==null)return null;
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return null;
		
		if(sessionId==null)sessionId = cub.findSessionIdFromWebPageId(webPageId);
		if(sessionId==null)return null;
		if(cub.getSyncSessionMap()==null)cub.setSyncSessionMap(new HashMap());
		SyncSessionMapHelper3 ses = cub.getSyncSessionMap().get(sessionId);
		if(ses==null){
			ses = new SyncSessionMapHelper3();
			cub.getSyncSessionMap().put(sessionId, ses);
		}
		if(ses.getSyncWebPageMap()==null)ses.setSyncWebPageMap(new HashMap());
		SyncWebPageMapHelper3 web = ses.getSyncWebPageMap().get(webPageId);
		if(web==null){
			web = new SyncWebPageMapHelper3();
			ses.getSyncWebPageMap().put(webPageId, web);
		}
		if(web.getSyncTabMap()==null)web.setSyncTabMap(new HashMap());

		SyncTabMapHelper3 tab = web.getSyncTabMap().get(tabId);
		
		
		Map<Integer, Map<String, SyncTabMapHelper3>>	tblMap = gridSyncMap3.get(projectId);
		if(tblMap==null){
			tblMap = new ConcurrentHashMap<Integer, Map<String, SyncTabMapHelper3>>();
			gridSyncMap3.put(projectId,  tblMap);
		}
		Map<String, SyncTabMapHelper3> tabMap = tblMap.get(tableId);
		if(tabMap==null){
			tabMap = new ConcurrentHashMap<String, SyncTabMapHelper3>();
			tblMap.put(tableId, tabMap);
		}
		SyncTabMapHelper3 tab2 = tabMap.get(tabId);
		
		if(tab==null){
			if(tab2==null){
				tab = new SyncTabMapHelper3(webPageId, tabId, userId);
				tabMap.put(tabId, tab);
			} else {
				tab = tab2;
			}
			web.getSyncTabMap().put(tabId, tab);
		} else {
			if(tab2==null){
				tabMap.put(tabId, tab);
			} else { //TODO acaba ikisi de ayni mi?
				
			}
			
		}
		return tab;
	}
	
	public static Set<Integer> getTableGridFormCellCachedKeys(String projectId, int tableId, int userId,
			String sessionId, String webPageId, String tabId, int gridId, Map requestParams, boolean clear) {
		if(userId==0 ||  webPageId==null || tabId==null || gridId==0)return null;
		SyncTabMapHelper3  tab = getTableTab(projectId, tableId, userId, sessionId, webPageId, tabId);
		if(tab==null)return null;
				
//		tab.setDirty(false);
		tab.setLastActionTime(System.currentTimeMillis());
		
		if(tab.getGridMap()==null)tab.setGridMap(new HashMap());
		SyncGridMapHelper3 g = tab.getGridMap().get(gridId);
		if(g==null){
			g = new SyncGridMapHelper3(tableId, gridId);
			tab.getGridMap().put(gridId, g);
		}
		g.setRequestParams(requestParams);
		if(clear){
			g.getKeys().clear();
			tab.getMessages(true);
		}
		return g.getKeys();		
	}
	
	
	public static Map getTableGridFormCellReqParams(String projectId, int tableId, int userId,
			String sessionId, String webPageId, String tabId, int gridId) {
		if(userId==0 ||  webPageId==null || tabId==null || gridId==0)return null;
		SyncTabMapHelper3  tab = getTableTab(projectId, tableId, userId, sessionId, webPageId, tabId);
		if(tab==null)return null;
//		tab.setDirty(false);
//		tab.setLastActionTime(System.currentTimeMillis());
		
		if(tab.getGridMap()==null)return null;
		SyncGridMapHelper3 g = tab.getGridMap().get(gridId);
		if(g==null){
			return null;
		} else 
			return g.getRequestParams();		
	}
	
	public	static	Map getRecordEditMapInfo(String projectId){
		 Map<String, Map<Integer, Map<String, SyncTabMapHelper3>>> m1 = recordEditMap3.get(projectId);
         Map<String, Map<Integer, Map<String, Map<String, Object>>>> n1 = new HashMap();
         if(!GenericUtil.isEmpty(m1))for(String key:m1.keySet()){
	       	  Map<Integer, Map<String, SyncTabMapHelper3>> m2 = m1.get(key);
	       	  Map<Integer, Map<String,  Map<String, Object>>> n2 = new HashMap();
	       	  n1.put(key, n2);
	       	  if(!GenericUtil.isEmpty(m2))for(Integer userId:m2.keySet()){
	       		  Map<String, SyncTabMapHelper3> m3 = m2.get(userId);
	           	  Map<String,  Map<String, Object>> n3 = new HashMap();
	           	  n2.put(userId, n3);
	       		  if(!GenericUtil.isEmpty(m3))
	       			  for(String webPageId:m3.keySet()){
	       				  SyncTabMapHelper3 m4 = m3.get(webPageId);
	       				  Map<String, Object> n4 = new HashMap();
	       				  n3.put(webPageId, n4);
	       				  n4.put("tabId", m4.getTabId());
	       				  n4.put("syncTip", m4.getSyncTip());
/*	           		 n4.put("deferredResult", m4.getDeferredResult()!=null);
	           		 if(m4.getDeferredResult()!=null){
		            		 n4.put("deferredResult_active", !m4.getDeferredResult().isSetOrExpired());            		 
		            		 n4.put("deferredResult_userId", m4.getDeferredResult().getUserId());
		            		 n4.put("deferredResult_userName", getUserName(customizationId, m4.getDeferredResult().getUserId()));
		            		 n4.put("deferredResult_webPageId", m4.getDeferredResult().getWebPageId());
	           		 } */
	           	  }
	       	  }
         }
         return n1;
	}
	public	static	Map getGridSyncMapInfo(String projectId){
		Map<Integer, Map<String, SyncTabMapHelper3>> m1 = gridSyncMap3.get(projectId);
        Map n1 = new HashMap();
        if(!GenericUtil.isEmpty(m1))for(Integer key:m1.keySet())try{
        	Map<String, SyncTabMapHelper3> m2 = m1.get(key);
        	Map n2 = new HashMap();
	       	n1.put((key>0 ? ("table."+FrameworkCache.getTable(projectId, key).getDsc()):("lookUp."+FrameworkCache.getLookUp(projectId, -key.intValue()).getDsc()))+"("+key+")", n2);
	       	if(!GenericUtil.isEmpty(m2))for(String tabId:m2.keySet()){
	       		  SyncTabMapHelper3 m3 = m2.get(tabId);
	           	  Map n3 = new HashMap();
	           	  n2.put(getUserName(m3.getUserId())+"."+tabId, n3);
	           	  
	       		  if(!GenericUtil.isEmpty(m3.getGridMap()))for(Integer gridId:m3.getGridMap().keySet()){
	       			SyncGridMapHelper3 m4 = m3.getGridMap().get(gridId);
	       			if(m4.getTableId()==key){
		           		Map n4 = new HashMap();
		           		 n3.put(gridId, n4);
		           		 n4.put("keys", !GenericUtil.isEmpty(m4.getKeys()));
	//	           		 n4.put("user", getUserDsc(customizationId, userId)));
	       			}
	           	  }
	       	  }
        } catch (Exception e) {
        	n1.put("error."+key, "ERROR");
		}
        return n1;
	}
	public	static	Map getUserMapInfo(String projectId){
		long now = System.currentTimeMillis();
		Map<Integer, CachedUserBean3> m1 = userMap3;//.get(projectId);
        Map<String, Map<Integer, Map<String, Map<String, Object>>>> n1 = new HashMap();
        if(!GenericUtil.isEmpty(m1))for(Integer userId:m1.keySet()){
        	CachedUserBean3 m2 = m1.get(userId);
			Map n2 = new HashMap();
			n1.put(getUserName(userId), n2);
			if(m2!=null && !GenericUtil.isEmpty(m2.getSyncSessionMap()))for(String sessionId:m2.getSyncSessionMap().keySet()){
				  SyncSessionMapHelper3 m3 = m2.getSyncSessionMap().get(sessionId);
			   	  Map n3 = new HashMap();
			   	  n3.put("deviceType", m3.getDeviceType());
			   	  if(m3.getDeviceType()!=0){
			   		  n3.put("mobileDeviceId", sessionId);
//			   		  n3.put("lastMobileActionTime", now-m3.getLastMobileActionTime());
			   	  }
		   		  n3.put("lastActionTime", now-m3.getLastActionTime());
				  n3.put("lastAsyncActionTime", now-m3.getLastAsyncActionTime());
			   	  n2.put(sessionId, n3);
				  if(m3!=null && !GenericUtil.isEmpty(m3.getSyncWebPageMap()))for(String webPageId:m3.getSyncWebPageMap().keySet()){
					 SyncWebPageMapHelper3 m4 = m3.getSyncWebPageMap().get(webPageId);
			   		 Map n4 = new HashMap();
			   		 n3.put(webPageId, n4);
			   		 n4.put("lastActionTime", now-m4.getLastActionTime());
					 n4.put("lastAsyncActionTime", now-m4.getLastAsyncActionTime());
					 n4.put("deferredResult", m4.getDeferredResult()!=null);
					 if(m4.getDeferredResult()!=null){
						 n4.put("deferredResult_active", !m4.getDeferredResult().isSetOrExpired());            		 
						 n4.put("deferredResult_userId", m4.getDeferredResult().getUserId());
						 n4.put("deferredResult_userName", getUserName(m4.getDeferredResult().getUserId()));
						 n4.put("deferredResult_webPageId", m4.getDeferredResult().getWebPageId());
			   		 }
					 if(m4.getSyncTabMap()!=null){
						 Map n5 = new HashMap();
				   		 n4.put("tabs", n5);
						 for(SyncTabMapHelper3 tab:m4.getSyncTabMap().values()){
							 n5.put(tab.getTabId(), now - tab.getLastActionTime());
						 }
					 }
			   	  }
			  }
        }
        return n1;
	}

	public	static SyncWebPageMapHelper3 addDeferredResult(String projectId, int userId, String sessionId, String webPageId, String activeTabId, Map scd, W5DeferredResult dr){
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return null;
		if(cub.getSyncSessionMap()==null)cub.setSyncSessionMap(new HashMap());
		boolean mobile = scd.containsKey("mobile");
		if(mobile){
			sessionId = scd.get("mobileDeviceId").toString();
			webPageId = sessionId;
		}
		SyncSessionMapHelper3 ses = cub.getSyncSessionMap().get(sessionId);
		if(ses==null){
			ses = new SyncSessionMapHelper3();
			ses.setScd(scd);
			if(mobile)ses.setDeviceType((short)GenericUtil.uInt(scd.get("mobile")));
			cub.getSyncSessionMap().put(sessionId, ses);
		}
		cub.setLastAsyncActionTime(System.currentTimeMillis());
		if(ses.getSyncWebPageMap()==null)ses.setSyncWebPageMap(new HashMap());
		SyncWebPageMapHelper3 wp = ses.getSyncWebPageMap().get(webPageId);
		if(wp==null){
			wp = new SyncWebPageMapHelper3();
			ses.getSyncWebPageMap().put(webPageId, wp);
		}
		if(!GenericUtil.isEmpty(activeTabId))wp.setActiveTab(activeTabId);
		ses.setLastAsyncActionTime(cub.getLastAsyncActionTime());
		if(!dr.isSetOrExpired() && wp.hasToBeBroadcast()){
			String s = wp.getFIFOBroadcast();
			if(s!=null){
				dr.setResult(s);
			}
		} else wp.setDeferredResult(dr);
		wp.setLastAsyncActionTime(cub.getLastAsyncActionTime());
		return	wp;
		
	}

	//4mobile: webPageId=tabId=mobileDeviceId
	public	static Map<Integer, Map<String, SyncTabMapHelper3>> syncRecordEditMap(String projectId, String key, int userId, String webPageId, String tabId, long time, short syncTip){
		if(key==null || webPageId==null || tabId==null)return null;
		Map<String, Map<Integer, Map<String, SyncTabMapHelper3>>> rec4cus = recordEditMap3.get(projectId);
		if(rec4cus==null){rec4cus= new ConcurrentHashMap<String, Map<Integer, Map<String, SyncTabMapHelper3>>>();recordEditMap3.put(projectId, rec4cus);}
		Map<Integer, Map<String, SyncTabMapHelper3>> record = rec4cus.get(key);//record: webPageId, tabIds
		if(record==null){record= new ConcurrentHashMap<Integer, Map<String, SyncTabMapHelper3>>();rec4cus.put(key, record);}
		Map<String, SyncTabMapHelper3> rec4user = record.get(userId);
		SyncTabMapHelper3 srh = null;
		if(rec4user==null){rec4user= new ConcurrentHashMap<String, SyncTabMapHelper3>();record.put(userId, rec4user);}
		else srh = rec4user.get(webPageId);
		if(srh==null){
			srh = new SyncTabMapHelper3(webPageId, tabId, userId);
			rec4user.put(webPageId, srh);
		} else {
			if(!GenericUtil.isEmpty(tabId))srh.setTabId(tabId);
		}
		srh.setLastActionTime(time);
		if(syncTip!=0)srh.setSyncTip(syncTip);
		return record;
		
	}

	public	static Map<Integer, Map<String, SyncTabMapHelper3>> syncGetRecordEditUsersMap(String projectId, String key){
		Map<String, Map<Integer, Map<String, SyncTabMapHelper3>>> rec4cus = recordEditMap3.get(projectId);
		if(rec4cus==null)return null;
		return rec4cus.get(key);//record
		
	}


	public	static Map<Integer, Map<String, SyncTabMapHelper3>> syncUpdateRecord(String projectId, String key, int userId, String webPageId, boolean makeDirty){
		Map<String, Map<Integer, Map<String, SyncTabMapHelper3>>> m1 = recordEditMap3.get(projectId);
		if(m1==null)return null;
		Map<Integer, Map<String, SyncTabMapHelper3>> m2 = m1.get(key);
		if(m2==null)return null;
		Map<String, SyncTabMapHelper3> m3 = m2.get(userId);
		if(m3==null)return null;
		if(makeDirty){
			m3.remove(webPageId);
			for(Integer u:m2.keySet()){
				m3 = m2.get(u);
				for(String w:m3.keySet()){
					SyncTabMapHelper3 srh = m3.get(webPageId);
//					if(srh!=null)srh.setDirty(true);
				}
			}
		}
		return m2;
	}

	public static  Map<Integer, Map<String, SyncTabMapHelper3>> syncRemoveTab(String projectId, int userId, String webPageId, String key) {
		if(userId==0 || key==null)return null;
		Map<String, Map<Integer, Map<String, SyncTabMapHelper3>>> rec4cus = recordEditMap3.get(projectId);
		if(rec4cus==null)return null;
		Map<Integer, Map<String, SyncTabMapHelper3>> record = rec4cus.get(key);
		//userId, webPageId, SyncTabMapHelper3
		if(record==null)return null;
		Map<String, SyncTabMapHelper3> rec4user = record.get(userId);
		if(rec4user==null)return null;
		rec4user.remove(webPageId);
		return record;		
	}
	public static  List<Map<Integer, Map<String, SyncTabMapHelper3>>> syncRemovePage(String projectId, int userId, String sessionId, String webPageId) {
		if(userId==0 ||  webPageId==null)return null;
		List<Map<Integer, Map<String, SyncTabMapHelper3>>> result = null;
		Map<String, Map<Integer, Map<String, SyncTabMapHelper3>>> m1 = recordEditMap3.get(projectId);
		//key, user, webPage
		Map<Integer, Map<String, SyncTabMapHelper3>> m1b = gridSyncMap3.get(projectId);
		//tableId, tabId, 
		if(m1!=null)for(Map<Integer, Map<String, SyncTabMapHelper3>> m2:m1.values()){
			Map<String, SyncTabMapHelper3> m3 = m2.get(userId);
			if(m3!=null && m3.containsKey(webPageId)){
				SyncTabMapHelper3 m4 = m3.get(webPageId);
				if(m4!=null && m4.getGridMap()!=null && m1b!=null){
					for(SyncGridMapHelper3 g:m4.getGridMap().values()){
						Map<String, SyncTabMapHelper3> m2b = m1b.get(g.getTableId());
						if(m2b!=null){
							m2b.remove(m4.getTabId());
						}
					}
				}				
				m3.remove(webPageId);
				if(m3.isEmpty()){// bu record'da artik edit etmiyor demek ki. bunu bildir
					if(result==null)result = new ArrayList();
					result.add(m2);
				}
			}
		}		
		
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return result;
		
		if(sessionId==null)sessionId = cub.findSessionIdFromWebPageId(webPageId);
		
		if(sessionId==null || cub.getSyncSessionMap()==null){
			onlineUserLogout(userId, sessionId);
			return result;
		}
		SyncSessionMapHelper3 ses = cub.getSyncSessionMap().get(sessionId);
		if(ses==null || ses.getSyncWebPageMap()==null){
			onlineUserLogout(userId, sessionId);
			return result;
		}
		if(ses.getSyncWebPageMap().containsKey(webPageId)){
			ses.getSyncWebPageMap().remove(webPageId);
			if(ses.getSyncWebPageMap().isEmpty()){//logout olmus demek ki
				onlineUserLogout(userId, sessionId);
			}
		}
		return result;
	}

	public static Map<Integer, Map<String, SyncTabMapHelper3>> syncRemoveRecord(
			String projectId, String key, int userId) {
		Map<String, Map<Integer, Map<String, SyncTabMapHelper3>>> rec4cus = recordEditMap3.get(projectId);
		if(rec4cus==null)return null;
		Map<Integer, Map<String, SyncTabMapHelper3>> record = rec4cus.get(key);
		if(record==null)return null;
		record.remove(userId);
		return record;//TODO
	}
	
	public static Map<Integer, Map<String, SyncTabMapHelper3>> syncGetRecord(
			String projectId, String key) {
		Map<String, Map<Integer, Map<String, SyncTabMapHelper3>>> rec4cus = recordEditMap3.get(projectId);
		if(rec4cus==null)return null;
		Map<Integer, Map<String, SyncTabMapHelper3>> record = rec4cus.get(key);
		//only for web
		if(GenericUtil.isEmpty(record))return null;
/*		Map<Integer, Map<String, SyncTabMapHelper3>> record4web = new HashMap();
		for(Integer user:record4web.keySet()){
			
		} */
		return record;//TODO
	}

	
	public static CachedUserBean3 getCachedUserBean(int userId){
		if(userId==0)return null;
		Map<Integer, CachedUserBean3> m = userMap3;//.get(projectId);
		if(m==null)return null;
		CachedUserBean3 r = m.get(userId);
	/*	if(FrameworkSetting.debug && r==null)for(Map<Integer, CachedUserBean3> zz:userMap3.values()){
			r = zz.get(userId);
			if(r!=null)return r;
		} */
		return r;
	}
	
	
	public static Map<String, SyncSessionMapHelper3> getUserSessions(String projectId, int userId){
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return null;
		return cub.getSyncSessionMap();
	}

	
	
	public static boolean removeUserSession(int userId, String sid){
		if(userId==0 || GenericUtil.isEmpty(sid))return false;
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return false;
		Map<String, SyncSessionMapHelper3>  userSessions = cub.getSyncSessionMap();
		if(userSessions==null)return false;
		if(userSessions.containsKey(sid)){
			userSessions.remove(sid);
			return true;
		} else
			return false;
	}
	
	
	
	public static void onlineUserCheck(Map<String, Object> scd, String url, String sid, String webPageId){ // number, precision
		long awayTime = System.currentTimeMillis()-FrameworkSetting.onlineUsersAwayMinute;			
		
		int userId = (Integer)scd.get("userId");
		String projectId = (String)scd.get("projectId");
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return;//TODO
		
		if(scd.containsKey("mobile"))cub.setLastMobileActionTime(System.currentTimeMillis());
		else cub.setLastActionTime(System.currentTimeMillis());// ilerde direk async time'i kontrol et
		
		SyncSessionMapHelper3 sess4user = null;
		Map<String, SyncSessionMapHelper3>  userSessions = cub.getSyncSessionMap();
		if(userSessions==null){
			userSessions = new HashMap();
			cub.setSyncSessionMap(userSessions);
		} else {
			sess4user = userSessions.get(sid);
		}
		if(sess4user!=null){
			sess4user.setLastUrl(url);
//			oub.sessionId = sid;
//			oub.setScd(scd);
			if(awayTime>sess4user.getLastActionTime() && (Integer)scd.get("chatStatusTip")==1){
				publishUserStatus(userId, ((Integer)scd.get("chatStatusTip")).shortValue(), null, projectId);//onceden away idi, simdi normal oldu herkese bildir
			}
 			if(cub.getChatStatusTip()==0 && (Integer)scd.get("chatStatusTip")!=0){
 				cub.setChatStatusTip(((Integer)scd.get("chatStatusTip")).shortValue());
 			}
			sess4user.setLastActionTime(System.currentTimeMillis());
 		} else {
 			sess4user = new SyncSessionMapHelper3(url, "*", /*sid, */scd);
 			if(cub.getChatStatusTip()==0 && (Integer)scd.get("chatStatusTip")!=0){
 				cub.setChatStatusTip(((Integer)scd.get("chatStatusTip")).shortValue());
 			}
 			userSessions.put(sid, sess4user);
 			publishUserStatus(userId, ((Integer)scd.get("chatStatusTip")).shortValue(), null, projectId);//herkese bildir
		}
		if(!GenericUtil.isEmpty(webPageId) && sess4user.getSyncWebPageMap()!=null){
			SyncWebPageMapHelper3 wpi = sess4user.getSyncWebPageMap().get(webPageId);
			if(wpi!=null)wpi.setLastActionTime(System.currentTimeMillis());
		}
	}
	
	
	
	public static boolean onlineUserLogout(int userId, String sessionId){
		CachedUserBean3 cub = getCachedUserBean(userId); //
		if(cub==null)return false;//TODO
		String projectUuid = null;
		if(cub.getSyncSessionMap()!=null){
			SyncSessionMapHelper3 syncSes = cub.getSyncSessionMap().get(sessionId);
			if(syncSes!=null){
				projectUuid = syncSes.getScd()!=null ? (String)syncSes.getScd().get("projectId") : null; 
				if(syncSes.getDeviceType()!=0)deviceMap3.remove(sessionId); // it's mobileDeviceId
				cub.getSyncSessionMap().remove(sessionId);
			}
			if(cub.getChatStatusTip()!=0){ //bu session'dan baska online yoksa herkese bildirelim
				boolean publish = true;
				long limitTime = System.currentTimeMillis()-FrameworkSetting.onlineUsersLimitMinute;
				for(SyncSessionMapHelper3 oub:cub.getSyncSessionMap().values())if(oub.getDeviceType()==0 && oub.getLastAsyncActionTime()>limitTime){ // baska bir instance i var mi kendisinin?
					publish = false;
					break;
				}
				if(publish){
					publishUserStatus(userId, (short)0, null, projectUuid);//herkese bildir, logout oldugunu, eger baska instance'i yoksa kendisinin, logout'tur
					//if(FrameworkSetting.mq)mqPublishUserStatus(customizationId, userId, (short)0);//herkese bildir, logout oldugunu, eger baska instance'i yoksa kendisinin, logout'tur
					
				}
			}
		} else {
			cub.setLastActionTime(0);cub.setLastAsyncActionTime(0);cub.setLastMobileActionTime(0);
			cub.setChatStatusTip((short)0);
			publishUserStatus(userId, (short)0, null, null);//herkese bildir, logout oldugunu, eger baska instance'i yoksa kendisinin, logout'tur
			//if(FrameworkSetting.mq)mqPublishUserStatus(customizationId, userId, (short)0);//herkese bildir, logout oldugunu, eger baska instance'i yoksa kendisinin, logout'tur
		}
		return true;
	}

	


	public static boolean onlineUserLogin(Map<String, Object> scd, String ip, String sessionId, short deviceType, String mobileDeviceId){ // number, precision
		int userId = (Integer)scd.get("userId");
		String projectId = (String)scd.get("projectId");
		int chatStatusTip = (Integer)scd.get("chatStatusTip"); 
	//	if(chatStatusTip==0)return false;

		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return false;

		if(deviceType!=0){
			if(GenericUtil.isEmpty(mobileDeviceId))
				throw new IWBException("token","Invalid MobileDeviceId",0,null, "Invalid MobileDeviceId", null);
			DeviceSyncHelper3	deviceSync = deviceMap3.get(mobileDeviceId);
	//		SyncSessionMapHelper3 syncSes = null;
			if(deviceSync!=null){
				if(deviceSync.getUserId()!=userId){ //farkli bir user'da var. silinmesi lazim ordan
					CachedUserBean3 cubDevice = getCachedUserBean(deviceSync.getUserId());
					if(cubDevice!=null && cubDevice.getSyncSessionMap()!=null){
						cubDevice.getSyncSessionMap().remove(mobileDeviceId);
					}
				} else { //ayni user'da birsey yapmaya gerek yok???
				}
			} else {
				deviceMap3.put(mobileDeviceId, new DeviceSyncHelper3(userId, mobileDeviceId, System.currentTimeMillis(), deviceType));
			}
		}
		
		
		if(deviceType!=0)cub.setLastMobileActionTime(System.currentTimeMillis());
		else {
			cub.setLastActionTime(System.currentTimeMillis());
			cub.setLastAsyncActionTime(cub.getLastActionTime());
		}
		cub.setChatStatusTip((short)chatStatusTip);
		
		Map<String, SyncSessionMapHelper3>  userSessions = cub.getSyncSessionMap();
		if(userSessions==null){
			userSessions = new HashMap();
			cub.setSyncSessionMap(userSessions);
		}
		SyncSessionMapHelper3 syncSes = new SyncSessionMapHelper3("login", ip, /*sid, */scd);
		
		if(deviceType!=0){
			syncSes.setDeviceType(deviceType);
			userSessions.put(mobileDeviceId, syncSes);
		} else if(sessionId!=null)
			userSessions.put(sessionId, syncSes);
//		syncSes.setDeviceToken(mobileDeviceId);
		publishUserStatus(userId, (short)chatStatusTip, ip, projectId);//herkese bildir
		//if(FrameworkSetting.mq)mqPublishUserStatus(customizationId, userId, (short)chatStatusTip);//herkese bildir, logout oldugunu, eger baska instance'i yoksa kendisinin, logout'tur

		return true; //diger session'larin listesi: multiAllow yok ise, onlari iptal et
	}
	
	
	
	
	public static List<Object []> listOnlineUsers(Map<String, Object> scd){
		boolean chat = FrameworkSetting.chat && GenericUtil.uInt(scd.get("chat"))!=0;
		if(!chat)
			throw new IWBException("framework","Chat Not Enabled",0,null, "Chat Not Enabled. Please contact Administrator", null);
		long curTime = System.currentTimeMillis();
		long awayTime = curTime-FrameworkSetting.onlineUsersAwayMinute;			
		long limitTime = curTime-FrameworkSetting.onlineUsersLimitMinute;
		long limitMobileTime = curTime-FrameworkSetting.onlineUsersLimitMobileMinute;
		
		int userId = (Integer)scd.get("userId");
//		String projectId = (Integer)scd.get("customizationId");
		String projectId = (String)scd.get("projectId");
		
		boolean mq = false;
			
		/*Map<Integer, MQOnlineUser> otherOnlineUsers = new HashMap();//userId
		if(FrameworkSetting.mq){
			W5Project po = FrameworkCache.wProjects.get(projectUuid);
			if(po.getMqFlag()!=0){
				Map<String, MQOnlineUserHelper> mqoi = mqOnlineUsers.get(projectId).get(projectUuid);// bu projenin altinda
				if(mqoi!=null)for(String instanceUuid:mqoi.keySet())if(!instanceUuid.equals(FrameworkSetting.instanceUuid)){
					MQOnlineUserHelper mqo = mqoi.get(instanceUuid);
					if(mqo.getRefreshTime()>curTime-(90*1000)){ //eger 1.5 dka icinde refrsh olmussa
						for(MQOnlineUser ou:mqo.getOnlineUsers())if(ou.getUserId()!=userId){
							boolean ekle = true; 
							if(accf!=null)for(String s:accf){
								int ox = GenericUtil.uInt(scd.get(s));
								if(s.equals("roleId")){
								} else if(s.equals("unitId")){
									ekle = ox!=ou.getUnitId();
								} else if(s.equals("userTip")){
									ekle = ox!=ou.getUserTip();
								} else {
									ekle = false;
								}
								if(!ekle)break;
							}
							if(ekle)otherOnlineUsers.put(ou.getUserId(), ou);
						}
					}
				}
				mq = !otherOnlineUsers.isEmpty();
			}
		}*/
		
		
		List<Object[]> data=new ArrayList<Object[]>();
		if(projectMap3.get(projectId)!=null)for(Integer key : projectMap3.get(projectId)){
			CachedUserBean3 cub = userMap3.get(key);
			if(cub==null)continue;
			boolean found = false;
			if(cub.getSyncSessionMap()!=null && key!=userId && cub.getChatStatusTip()!=0){
				Map<String, SyncSessionMapHelper3> m = cub.getSyncSessionMap();
				if(GenericUtil.isEmpty(m))continue;
				SyncSessionMapHelper3 bestOub = new SyncSessionMapHelper3();
				bestOub.setLastActionTime(limitTime);
				bestOub.setLastAsyncActionTime(awayTime);
				int onlineCount = 0; short deviceType = 0; long mobileActionTime = limitMobileTime;
				for(SyncSessionMapHelper3 oub:m.values())if(!GenericUtil.isEmpty(oub.getScd())/* && oub.getScd().containsKey("projectId") && projectId.equals((String)oub.getScd().get("projectId"))*/){
					boolean xb = false;
					if(xb)break;
					if(oub.getDeviceType()!=0){
						deviceType = oub.getDeviceType();
						if(bestOub.getScd()==null)bestOub.setScd(oub.getScd());
						if(oub.getLastActionTime()>mobileActionTime)mobileActionTime = oub.getLastActionTime();
//						onlineCount++;
					}
					boolean b = true;
//					long lastActionTime
					if(oub.getLastActionTime()-bestOub.getLastActionTime()>0){
						bestOub=oub;
						onlineCount++;
						b = false;
					} 
					if(oub.getLastAsyncActionTime()-bestOub.getLastAsyncActionTime()>0){
						bestOub.setLastAsyncActionTime(oub.getLastAsyncActionTime());
						if(b)onlineCount++;
					}
				}
				if(bestOub.getScd()!=null && ((bestOub.getDeviceType()==0 && (bestOub.getLastActionTime()>limitTime || bestOub.getLastAsyncActionTime()>awayTime)) || (deviceType!=0 && mobileActionTime>limitMobileTime))){
//					if(!projectId.equals((String)bestOub.getScd().get("projectId")))continue;
					short chatStatusTip = cub.getChatStatusTip();
					if(chatStatusTip==1 && bestOub.getLastActionTime()<awayTime)chatStatusTip=(short)3;//away
	//				CachedUserBean2 oz = cachedUserMap.get(mcub.getKey().toString());
	    			data.add(new Object[]{key,cub.getUserName(),cub.getDsc(),bestOub.getLastActionTime(), null,chatStatusTip,bestOub.getScd()==null ? "error": bestOub.getScd().get("roleDsc"), 0, cub.getProfilePictureId(), deviceType!=0 ? 1:0});
	    			found = true;
				} 
			}
			/*if(mq && !found){
				MQOnlineUser ou = otherOnlineUsers.get(mcub.getKey());
				if(ou!=null){
	    			data.add(new Object[]{ou.getUserId(),cub.getUserName(),cub.getDsc(),0, null,ou.getChatStatusTip(),".", 0, cub.getProfilePictureId(), ou.getMobile()});
	    			otherOnlineUsers.remove(mcub.getKey());
				}
			}*/
		}
		return data;
	}
	
	public static boolean publishWidgetStatus(Map<String, Object> scd, Map<Integer, Map<Integer, Integer>> mapWidgetCount){
		if(GenericUtil.isEmpty(mapWidgetCount))return false;
//		String projectId = (Integer)scd.get("customizationId");
//		int userId = (Integer)scd.get("userId");
		for(Map.Entry<Integer,Map<Integer,Integer>> entry:mapWidgetCount.entrySet()){
			CachedUserBean3 cub = getCachedUserBean(entry.getKey());
			if(cub==null)continue;
			String widgedIds = "";
			for(Integer key:entry.getValue().keySet()){
				widgedIds+=","+key;
			}
			if(GenericUtil.isEmpty(widgedIds))continue;
			widgedIds=widgedIds.substring(1);
			Map m = new HashMap();
			m.put("success", true);
			m.put("widgetUpdate", true);
			m.put("widgetIds", widgedIds);
			cub.broadCast(m);			
		}
		return true;
	}
	
	
	public static boolean updateChatStatus(Map<String, Object> scd, int chatStatusTip) {
		if(chatStatusTip<0 || chatStatusTip>3/* || chatStatusTip==(Integer)scd.get("chatStatusTip")*/) return false;
		scd.put("chatStatusTip",chatStatusTip);
		String projectId = (String)scd.get("projectId");
		int userId = (Integer)scd.get("userId");
		CachedUserBean3 cub = getCachedUserBean(userId);
		cub.setChatStatusTip((short)chatStatusTip);
		publishUserStatus(userId, (short)chatStatusTip, null, projectId);
		//if(FrameworkSetting.mq)mqPublishUserStatus(customizationId, userId, (short)chatStatusTip);//herkese bildir, logout oldugunu, eger baska instance'i yoksa kendisinin, logout'tur
		return true;
	}
	

	
	public static boolean publishUserStatus(int userId, short chatStatusTip, String remoteIp, String projectUuid){		//TODO. karisik buralar
//		CachedUserBean2 cub = getCachedUserBean2(userId);
		/*Map<Integer, CachedUserBean3> m1 = userMap3.get(projectId);
		if(m1==null)return false;
		if(chatStatusTip==0 && FrameworkSetting.mq && projectUuid!=null){//eger offline olduysa bir zahmet digerlerine de bak
			W5Project po = FrameworkCache.wProjects.get(projectUuid);
			if(po.getMqFlag()!=0){
				long limitTime = System.currentTimeMillis() - (90*1000);
	        	Map<String, MQOnlineUserHelper> mmqo = mqOnlineUsers.get(po.getCustomizationId()).get(po.getProjectUuid());
	        	for(MQOnlineUserHelper oh:mmqo.values()){
	        		if(oh.getRefreshTime()>limitTime && oh.getOnlineUsers()!=null)for(MQOnlineUser ou:oh.getOnlineUsers())if(ou.getUserId()==userId && ou.getChatStatusTip()>0){
	        			if(chatStatusTip == 0)chatStatusTip = ou.getChatStatusTip();
	        			else if(chatStatusTip > ou.getChatStatusTip())chatStatusTip = ou.getChatStatusTip();
	        		}
	        	}				
			}
		}
		
		for (Map.Entry<Integer, CachedUserBean3> userEntry : m1.entrySet()){
//			CachedUserBean3 cub = userEntry.getValue(); //getCachedUserBean(entry.getKey());
			if(userEntry.getValue().getChatStatusTip()==0 && userId!=userEntry.getKey())continue; // eger bu user offline ise, ona bilgi gonderme (kendisine gonder ama, bgelki de status degistirmistir)
			Map m = new HashMap();
			m.put("success", true);
			m.put("userStatusUpdate", true);
			m.put("userId", userId);
			m.put("chatStatusTip", chatStatusTip);
			if(!GenericUtil.isEmpty(remoteIp) && userId==userEntry.getKey()){
				m.put("onlineCount", userEntry.getValue().getSyncSessionMap().size());
				m.put("remoteIp", remoteIp);
			}
			userEntry.getValue().broadCast(m);
			
		}*/
		return true;
	}
	
	
	public static List<W5QueuedPushMessageHelper> publishUserChatMsg(int fromUserId, int toUserId, String msg, Object chatId){
		CachedUserBean3 cub = getCachedUserBean(toUserId);
		if(cub==null)return null;
		int b=0;
		if(true || cub.getChatStatusTip()!=0){
			Map m = new HashMap();
			m.put("success", true);
			m.put("userChatMsg", true);
			m.put("chatId", chatId);
			m.put("userId", fromUserId);
			String title = getUserDsc(fromUserId);
			m.put("userDsc", title);
			m.put("title", title);
			m.put("msg", msg);
			m.put("message", msg);
			b+=cub.broadCast(m);
		}
		/*
		boolean mobilePush = FrameworkSetting.mobilePush && (customizationId==0 || FrameworkCache.getAppSettingIntValue(customizationId, "mobile_push_flag")!=0);
		if(mobilePush && cub.getSyncSessionMap()!=null){
			List<W5QueuedPushMessageHelper> l = new ArrayList<W5QueuedPushMessageHelper>();
			for(SyncSessionMapHelper3 val:cub.getSyncSessionMap().values())if(val.getDeviceType()!=0){
				l.add(new W5QueuedPushMessageHelper(0, 2, 935, fromUserId, "["+getUserDsc(customizationId, fromUserId)+"]: "+msg , 0, val.getDeviceType(), val.getDeviceToken()));
				b++;
			}
			return l;
		}*/
		
		return null;
	}

	public static List<W5QueuedPushMessageHelper> publishNotification(Log5Notification n, boolean allFlag){
		List<Integer> ln = new ArrayList(n.getNotificationId()==-1 ? 1: userMap3.size());
		if(n.getNotificationId()==-1)ln.addAll(userMap3.keySet());
		else ln.add(n.getCustomizationId());
		Map m = new HashMap();
		m.put("success", true);
		m.put("notification", n.toMap());
		boolean mobilePush = FrameworkSetting.mobilePush && (n.getCustomizationId()==0 || FrameworkCache.getAppSettingIntValue(n.getCustomizationId(), "mobile_push_flag")!=0);
		List<W5QueuedPushMessageHelper> l = mobilePush ? new ArrayList<W5QueuedPushMessageHelper>() : null;
		for(Integer customizationId:ln){
			Map<Integer, CachedUserBean3> m1 = userMap3;//.get(projectId);
			if(m1==null)continue;
			if(allFlag){
				for(Map.Entry<Integer, CachedUserBean3> userBean:m1.entrySet()){
					userBean.getValue().broadCast(m);
//					if(mobilePush && userBean.getValue().getSyncSessionMap()!=null)for(SyncSessionMapHelper3 val:userBean.getValue().getSyncSessionMap().values())if(val.getDeviceType()!=0)l.add(new W5QueuedPushMessageHelper(n.getCustomizationId(), 1/*notification*/, n.getTableId(), n.getTablePk(), GenericUtil.isEmpty(n.get_tmpStr()) ? n.get_notificationTipStr():n.get_tmpStr(), 0, val.getDeviceType(), val.getDeviceToken()));
				}
			} else {
				CachedUserBean3 userBean = m1.get(n.getUserId());
				if(userBean!=null){
					userBean.broadCast(m);
//					if(mobilePush && userBean.getSyncSessionMap()!=null)for(SyncSessionMapHelper3 val:userBean.getSyncSessionMap().values())if(val.getDeviceType()!=0)l.add(new W5QueuedPushMessageHelper(n.getCustomizationId(), 1/*notification*/, n.getTableId(), n.getTablePk(), GenericUtil.isEmpty(n.get_tmpStr()) ? n.get_notificationTipStr():n.get_tmpStr(), 0, val.getDeviceType(), val.getDeviceToken()));
				}
			}
		}
		return l;
	}
	/*

	public static boolean userLoginControl(String userName,String remoteIp,String sessionId,String projectId){
		boolean control=true;
		if(PromisCache.getAppSettingIntValue(customizationId, "allow_multi_login_flag")==0){
			for(String key :lastUserAction.keySet()){	
				if(userName.equals(key)){
					long limitTime = 1000*60*PromisCache.getAppSettingIntValue(0,"online_users_limit_minute");			
					long curTime = new Date().getTime();
					Date d = (Date) getUserSessions(key).get(1);
					boolean timeOutFlag=(curTime - d.getTime())>limitTime ? true :false;
				
					if(!getUserSessions(key).get(2).equals(remoteIp) && !timeOutFlag )	{				
						control =false;		
					}
					else if (!getUserSessions(key).get(2).equals(remoteIp) && timeOutFlag){
						control=true;
						lastUserAction.remove(key);
					}
					else control=true;
				}
			}
		}else control=true;
				
		return control;		
	}

	 */
	
	public static Map<String, Object> getScd(HttpServletRequest request, String scdKey, boolean onlineCheck){
		Map<String, Object> scd = null;
		HttpSession session = null;
		session = request.getSession(false);
		if(session==null || session.getAttribute(scdKey)==null || ((HashMap<String,Object>)session.getAttribute(scdKey)).isEmpty()){
			throw new IWBException("session","No Session",0,null, "No valid session", null);
		}
		scd =(Map<String, Object>)session.getAttribute(scdKey);
		boolean mobile = scd.containsKey("mobile");
		String sessionId = mobile ? (String)scd.get("mobileDeviceId") : (String)scd.get("sessionId");
		String webPageId = mobile ? (String)scd.get("mobileDeviceId") : request.getParameter(".w");
		String projectId = request.getParameter(".p");
		if(onlineCheck)onlineUserCheck(scd, request.getRequestURI(), sessionId, webPageId);
		if(!GenericUtil.isEmpty(projectId) && scd.containsKey("projectId") && !projectId.equals(scd.get("projectId").toString())) { //TODO. check for security
			W5Project po = FrameworkCache.getProject(projectId);
			if(po==null/* || po.getCustomizationId()!=(Integer)scd.get("customizationId")*/)//TODO: security problem
				throw new IWBException("security","Wrong.Project",0,null, "Not allowed to access this project", null);
			Map newScd = new HashMap();
			newScd.putAll(scd);
			newScd.put("projectId", projectId);
			scd = newScd;
		}
	
		if(scd.containsKey("projectId") && FrameworkSetting.projectSystemStatus.get((String)scd.get("projectId"))!=0){
			throw new IWBException("framework","System Suspended",0,null, "System Suspended. Please wait", null);
		}
		return scd;
	}
	
	public static Map<String, Object> getScd4Preview(HttpServletRequest request, String scdKey, boolean onlineCheck){
		Map<String, Object> scd = null; //only in developer mode
		String pid = getProjectId(request, "preview");
		W5Project po = FrameworkCache.getProject(pid,"Wrong Project");
		
		if(FrameworkSetting.projectSystemStatus.get(pid)!=0){
			throw new IWBException("framework","System Suspended",0,null, "System Suspended. Please wait", null);
		}
		String newScdKey = "preview-"+pid;

		HttpSession session = request.getSession(false);
		Map newScd = null;
		if(session!=null){
			newScd =(Map<String, Object>)session.getAttribute(newScdKey);
			if(newScd==null || GenericUtil.uInt(newScd.get("renderer"))!=po.getUiWebFrontendTip() || GenericUtil.uInt(newScd.get("mainTemplateId"))!=po.getUiMainTemplateId()){
				scd =(Map<String, Object>)session.getAttribute(scdKey); //developer
				if(scd!=null){
					if((Integer)scd.get("roleId")!=0){
						throw new IWBException("security","Only for Developers",0,null, "Only for Developers", null);
					}
					newScd=new HashMap<String, Object>();
					newScd.putAll(scd);
					newScd.put("customizationId",po.getCustomizationId());
					newScd.put("projectId",po.getProjectUuid());newScd.put("projectName", po.getDsc());newScd.put("roleId",999999);
					int deviceType = GenericUtil.uInt(request, "d");
					if(deviceType==0)deviceType = po.getUiWebFrontendTip();
					else {
						scd.put("mobile", deviceType);
						scd.put("mobileDeviceId", request.getParameter("_mobile_device_id"));
					}
					newScd.put("renderer", po.getUiWebFrontendTip());
					newScd.put("_renderer", GenericUtil.getRenderer(deviceType));
					newScd.put("mainTemplateId", po.getUiMainTemplateId());
					newScd.put("path", "../");
					newScd.put("userTip",po.get_defaultRoleGroupId());
					newScd.put("sessionId", "nosession");
					String xlocale = "en";//GenericUtil.uStrNvl(request.getParameter("locale"), FrameworkCache.getAppSettingStringValue(0, "locale"));
					if(po.getLocaleMsgKeyFlag()!=0 && !GenericUtil.isEmpty(po.getLocales())) {
						xlocale = po.getLocales().split(",")[0];
					}
					newScd.put("locale", xlocale);
					session.setAttribute(newScdKey, newScd);
				}
			}
		}
		if(newScd!=null)return newScd;
		if(po.getSessionQueryId()==0){
			newScd=new HashMap<String, Object>();
			newScd.put("customizationId",po.getCustomizationId());newScd.put("ocustomizationId",po.getCustomizationId());newScd.put("userId",10);newScd.put("completeName","XXX");
			newScd.put("projectId",po.getProjectUuid());newScd.put("projectName", po.getDsc());newScd.put("roleId",10);newScd.put("roleDsc", "XXX Role");
			newScd.put("renderer", po.getUiWebFrontendTip());
			newScd.put("_renderer", GenericUtil.getRenderer(po.getUiWebFrontendTip()));
			newScd.put("mainTemplateId", po.getUiMainTemplateId());
			newScd.put("userName", "Demo User");
			newScd.put("email", "demo@icodebetter.com");newScd.put("locale", "en");
			newScd.put("chat", 1);newScd.put("chatStatusTip", 1);
			newScd.put("userTip",po.get_defaultRoleGroupId());
			newScd.put("path", "../");
			if(session==null)session = request.getSession(true);
			newScd.put("sessionId", "nosession");
			session.setAttribute(newScdKey, newScd);
			return newScd;
		}
		
		throw new IWBException("session","No Session",0,null, "No valid session", null);
	}
	public static String getProjectId(HttpServletRequest request, String prefix){
		String uri = request.getRequestURI();
		String pid = null;
		if(GenericUtil.isEmpty(prefix))prefix = "";
		if(!prefix.endsWith("/"))prefix+="/";
		int ix = uri.indexOf(prefix);
		if(ix>-1){
			pid = uri.substring(ix+prefix.length());
			return pid.substring(0,pid.indexOf('/'));
		}
		return null;		
	}
	
	public static Map<String, Object> getScd4PAppSpace(HttpServletRequest request){
		HttpSession session = null;
		session = request.getSession(false);
		String pid = getProjectId(request, "space/");
		W5Project po = FrameworkCache.getProject(pid,"Wrong Project");
		if(FrameworkSetting.projectSystemStatus.get(pid)!=0){
			throw new IWBException("framework","System Suspended",0,null, "System Suspended. Please wait", null);
		}
		String scdKey = "space-"+pid;

		if(session==null || session.getAttribute(scdKey)==null || ((HashMap<String,Object>)session.getAttribute(scdKey)).isEmpty()){
			if(po.getSessionQueryId()>0){
				throw new IWBException("session","No Session",0,null, "No valid session", null);
			}
			Map scd=new HashMap<String, Object>();
			scd.put("customizationId", po.getCustomizationId());
			scd.put("ocustomizationId", po.getCustomizationId());
			scd.put("userId", 10);scd.put("completeName","XXX");
			scd.put("roleId", 10);
			scd.put("userRoleId", 0);
			scd.put("roleDsc", "Demo Role");
			scd.put("chat", 1);
			scd.put("chatStatusTip", 1);
			scd.put("projectId", po.getProjectUuid());scd.put("projectName", po.getDsc());
			scd.put("locale", "en");
			scd.put("userName", "Demo User");
			scd.put("email", "demo@icodebetter.com");
			scd.put("renderer", po.getUiWebFrontendTip());
			scd.put("_renderer", GenericUtil.getRenderer(po.getUiWebFrontendTip()));
			scd.put("mainTemplateId", po.getUiMainTemplateId());
			scd.put("userTip",po.get_defaultRoleGroupId());
			scd.put("path", "../");
			if(session==null)session = request.getSession(true);
			scd.put("sessionId", "nosession");
			session.setAttribute(scdKey, scd);
			return scd;
		}
		
		Map<String, Object> scd = (Map<String, Object>)session.getAttribute(scdKey);
		if(!GenericUtil.safeEquals(pid, scd.get("projectId"))) {
			throw new IWBException("session","No Session",0,null, "No valid session", null);
		}
		
		
		return scd;
	}
/*	public static String generateTokenFromScd(Map<String, Object> scd, int integrationObjectId, String ip, int validTimeMillis) {
		String qq = ""+(System.currentTimeMillis()+validTimeMillis)+"-"+(scd.get("customizationId")!=null ?scd.get("customizationId"):0) +"-"+scd.get("userId")+"-"+scd.get("roleId")+"-"+scd.get("userRoleId")+"-"+scd.get("userTip")+"-"+scd.get("locale")+"-"+integrationObjectId+"-"+ip;
		int checksum = 0;
		for(int i=0;i<qq.length();i++)checksum+=qq.charAt(i);
		qq = GenericUtil.PRMEncrypt(checksum + "-"+qq);
		return qq;
	}
	
	
	public static Map<String, Object> getScdFromToken(String promisToken, String ip) {
		String qq = GenericUtil.PRMDecrypt(promisToken);
		String[] o = qq.split("-");

		int checksum = 0;
		for(int i=qq.indexOf("-")+1;i<qq.length();i++)checksum+=qq.charAt(i);
		if(checksum!=Integer.parseInt(o[0]))return null;
		
		long tokentTime = Long.parseLong(o[1]);
		long currentTime = System.currentTimeMillis();
		if(currentTime>tokentTime)return null;//5 sn
		
		Map<String, Object> scd = new HashMap();
		scd.put("customizationId", Integer.parseInt(o[2]));
		scd.put("userId", Integer.parseInt(o[3]));
		scd.put("roleId", Integer.parseInt(o[4]));
		scd.put("userRoleId", Integer.parseInt(o[5]));
		scd.put("userTip", Integer.parseInt(o[6]));
		scd.put("locale", o[7]);
		return scd;
	}*/


	
	public static String getUserName(int userId){
		if(userId==0)return "";
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return "";//"???";
		return cub.getUserName();
		
	}
	
	
	public static String getUserDsc(int userId){
		if(userId==0)return "";
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return "";//"???";
		return cub.getDsc();
		
	}
	
	
	public static void setUserProfilePicture(int userId, int profilePictureId){
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return;
		cub.setProfilePictureId(profilePictureId);
	}
	
	public static boolean addUser(String projectId, int userId, String userName, String userDsc, boolean canMultiLogin){
		if(userId==0)return false;
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null){
			cub = new CachedUserBean3(userName, userDsc, canMultiLogin);
//			if(userMap3.get(projectId)==null)userMap3.put(customizationId, new HashMap());
			userMap3.put(userId, cub);
		} else {
			cub.setDsc(userDsc);
			cub.setUserName(userName);
			cub.setCanMultiLogin(canMultiLogin);
		}
		return true;
	}
	
	
	
	public static boolean addUserWithProfilePicutre(int userId, String userName, String userDsc, boolean canMultiLogin, int profilePictureId){
		if(userId==0)return false;
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null){
			cub = new CachedUserBean3(userName, userDsc, canMultiLogin);
			cub.setProfilePictureId(profilePictureId);
//			if(userMap3.get(projectId)==null)userMap3.put(customizationId, new HashMap());
			userMap3.put(userId, cub);
		} else {
			cub.setDsc(userDsc);
			cub.setUserName(userName);
			cub.setCanMultiLogin(canMultiLogin);
			cub.setProfilePictureId(profilePictureId);
		}
		return true;
	}

	
	public static int getUserProfilePicture(int userId){
		if(userId==0)return 0;
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return 0;
		return cub.getProfilePictureId();
	}

	//4mobile: sessionId = webPageId = mobileDeviceId 
	public static boolean removeTemplateTab(
			String projectId, int userId, String sessionId, String webPageId, String tabId) {
		if(userId==0 ||  webPageId==null || tabId==null)return false;
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return false;
		Map<Integer, Map<String, SyncTabMapHelper3>> tableMap = gridSyncMap3.get(projectId);
		if(sessionId==null)sessionId = cub.findSessionIdFromWebPageId(webPageId);
		if(sessionId==null)return false;
		if(cub.getSyncSessionMap()==null)return false;//cub.setSyncSessionMap(new HashMap());
		SyncSessionMapHelper3 ses = cub.getSyncSessionMap().get(sessionId);
		if(ses==null)return false;//{ses = new SyncSessionMapHelper3();cub.getSyncSessionMap().put(sessionId, ses);}
		if(ses.getSyncWebPageMap()==null)return false;//ses.setSyncWebPageMap(new HashMap());
		SyncWebPageMapHelper3 web = ses.getSyncWebPageMap().get(webPageId);
		if(web!= null && web.getSyncTabMap()!=null){
			SyncTabMapHelper3 tab = web.getSyncTabMap().get(tabId);
			if(tab!=null){
				web.getSyncTabMap().remove(tabId);
				if(tab.getGridMap()!=null && tableMap!=null)for(SyncGridMapHelper3 g:tab.getGridMap().values()){
					Map<String, SyncTabMapHelper3> tabMap = tableMap.get(g.getTableId());
					if(tabMap!=null)tabMap.remove(tabId);
				}
				return true;
			}
			
		}
		return false;
	}
	public static int broadCast(String projectId, int userId, String sessionId, String webPageId,
			Map m) {
		if(userId==0 ||  webPageId==null || m==null)return 0;
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return 0;
		if(sessionId==null)sessionId = cub.findSessionIdFromWebPageId(webPageId);
		if(sessionId==null)return 0;
		SyncSessionMapHelper3 ses = cub.getSyncSessionMap().get(sessionId);
		if(ses==null)return 0;
		SyncWebPageMapHelper3 web = ses.getSyncWebPageMap().get(webPageId);
		if(web==null && ses.getDeviceType()==0)return 0;
		int count = web.broadCast(m);
		if(count==0 && ses.getDeviceType()!=0)return sendMobilePushNotification(ses.getDeviceType(), sessionId, m);
		return count;
	}
	
	public static int sendMobilePushNotification(short deviceType, String mobileDeviceId, Map m) {
		/*if(deviceType==2)try{
			Sender sender = new Sender(androidAPIKey);
			ArrayList<String> devicesList = new ArrayList<String>();
			//		     devicesList.add("dljX0qm_8YQ:1APA91bGTInpTCilHYLbt6cQke0vMUU96l3eCyGi_jDehN_gOnfL7hWchEiEwlFyFr7bGU7BRPfTnbMHRx-QU1uUN7FGFxkupM-kFZLbcL_LXuz698DFFxfpQXjtrXbQ41o0ThwAzlwq1");
			devicesList.add(mobileDeviceId);
			Builder builder = new Message.Builder().collapseKey("1").timeToLive(3).delayWhileIdle(true);
			for(String key:((Map<String, Object>)m).keySet()){
			 builder.addData(key, m.get(key).toString());
			}
			
			Message message = builder.build();
			MulticastResult result = sender.send(message, devicesList, UserUtil.androidSendRetryCount );

//			System.out.println(result.toString());
			if (result.getResults() != null) {
			    int canonicalRegId = result.getCanonicalIds();
			    if (canonicalRegId != 0) {
			    }
			//    result.getResults().
			} else {
			    int error = result.getFailure();
			    if(error>0)return 0;
			}
			return result.getSuccess();
		} catch (Exception e){
			e.printStackTrace();
			return 0;
		}
		*/
		return 0;
		
	}

	public static boolean syncTabActivate(String projectId, int userId, String sessionId, String webPageId, String tabId, long currentTime) {
		if(userId==0 ||  webPageId==null || tabId==null)return false;
		CachedUserBean3 cub = getCachedUserBean(userId);
		if(cub==null)return false;
		
		if(sessionId==null)sessionId = cub.findSessionIdFromWebPageId(webPageId);
		if(sessionId==null)return false;
		if(cub.getSyncSessionMap()==null)cub.setSyncSessionMap(new HashMap());
		cub.setLastActionTime(currentTime);
		SyncSessionMapHelper3 ses = cub.getSyncSessionMap().get(sessionId);
		if(ses==null){
			ses = new SyncSessionMapHelper3();
			cub.getSyncSessionMap().put(sessionId, ses);
		}
		if(ses.getSyncWebPageMap()==null)ses.setSyncWebPageMap(new HashMap());
		SyncWebPageMapHelper3 web = ses.getSyncWebPageMap().get(webPageId);
		if(web!= null){
			web.setActiveTab(tabId);
			web.setLastActionTime(currentTime);
			return true;
		}
		else	
			return false;
	}
	public	static Map syncGetTabNotifications(String projectId, int userId, String sessionId,
			String webPageId, String tabId){
		Map	m = new HashMap();
		m.put("success",true);
		if(userId==0 ||  webPageId==null || tabId==null)return m;
		CachedUserBean3 cub = getCachedUserBean(userId);
		long now = System.currentTimeMillis();
		if(sessionId==null)sessionId = cub.findSessionIdFromWebPageId(webPageId);
		if(sessionId==null)return m;
		if(cub.getSyncSessionMap()==null)cub.setSyncSessionMap(new HashMap());
		cub.setLastActionTime(now);
		SyncSessionMapHelper3 ses = cub.getSyncSessionMap().get(sessionId);
		if(ses==null){
			ses = new SyncSessionMapHelper3();
			cub.getSyncSessionMap().put(sessionId, ses);
		}
		m.put("tabId", tabId);
		if(ses.getSyncWebPageMap()==null)ses.setSyncWebPageMap(new HashMap());
		SyncWebPageMapHelper3 web = ses.getSyncWebPageMap().get(webPageId);
		if(web!= null){
			web.setLastActionTime(now);
			if(web.getSyncTabMap()!=null){
				SyncTabMapHelper3 t = web.getSyncTabMap().get(tabId);
				if(t!=null){
					t.setLastActionTime(now);
					m.put("msgs", t.getMessages(true));
					m.put("time", now);
				}
				
			}
		}
		return m;
	}
	
	public static List<Object> syncGetListOfRecordEditUsers(
			String projectId, String key, String webPageId) {
		Map<Integer, Map<String, SyncTabMapHelper3>> others = syncGetRecordEditUsersMap(projectId,  key);
		if(others!=null){
			//userId, webPageId, SyncWebPageMapHelper3
			List<Object> l = new ArrayList<Object>();
//							Map<W5DeferredResult, Map> broadcastMap = new HashMap();
			long limitTime = System.currentTimeMillis() - FrameworkSetting.asyncToleranceTimeout; 
			for(Integer u:others.keySet()){
				Map<String, SyncTabMapHelper3> webPageMap = others.get(u);
				Set<String>  keySet = webPageMap.keySet(); 
				Set<Integer> us = new HashSet<Integer>();
				for(String w:keySet){
					if(!webPageId.equals(w)){
						SyncTabMapHelper3 srh = webPageMap.get(w);
						if(srh==null || srh.getLastActionTime()<limitTime)continue;
						if(!us.contains(u)){
							us.add(u);
							Map m2 = new HashMap();
							m2.put("userId", u);
							m2.put("userDsc", getUserDsc(u));
							l.add(m2);
						}
					}
				}
			}
			return l;
		}
		return null;
	}

	public	static void	liveSyncAction(Map scd, Map<String,String> paramMap){
		int userId = (Integer)scd.get("userId");
		String projectId = (String)scd.get("projectId");
		boolean mobile = scd.containsKey("mobile");
		String sessionId = mobile ? (String)scd.get("mobileDeviceId") : (String)scd.get("sessionId");
		String webPageId = mobile ? (String)scd.get("mobileDeviceId") : paramMap.get(".w");
		String tabId = paramMap.get(".t");
		String key = paramMap.get(".pk");
		int action = GenericUtil.uInt(paramMap, ".a");
		Map<Integer, Map<String, SyncTabMapHelper3>> toBeUpdated = null;
		List<Map<Integer, Map<String, SyncTabMapHelper3>>> toBeUpdatedList = null;
		long currentTime = System.currentTimeMillis();
		switch(action){
		case	666://clearZombiUsers
			clearZombiUsers(GenericUtil.uInt(paramMap,".l"));//(PromisSetting.onlineUsersLimitMinute+5)*60*1000
			return;
		case	31://send chat message
			return;
		case	101://web page closed
			toBeUpdatedList = syncRemovePage(projectId, userId, sessionId, webPageId);
			if(GenericUtil.isEmpty(toBeUpdatedList))return;
			break;
		case	102://mobile status
			syncMobileStatus(projectId, userId, (String)scd.get("mobileDeviceId"), (short)GenericUtil.uInt(paramMap, ".s"));
			return;
		//case	15: //not used -> server2client for templateGrid dirty
		//	break;
		case	16: // client2server clear tabMessages
			syncTabClearMessages(projectId, userId, sessionId, webPageId, tabId, currentTime);
			break;

		case	10: // record opened(tab/form show) for update
			toBeUpdated = syncRecordEditMap(projectId, key, userId, webPageId, tabId, currentTime, (short)1);
			break;
		//case	13: // template opened(show) for update: TODO not used
//			openTemplateTab(customizationId, userId, webPageId, tabId, currentTime, request.getParameter(".g"));
		//	break;
		case	14: // tab activated
			syncTabActivate(projectId, userId, sessionId, webPageId, tabId, currentTime);
			break;
		
		case	1://record updated
			toBeUpdated = syncRemoveTab(projectId, userId, webPageId, key);
			removeTemplateTab(projectId, userId, sessionId, webPageId, tabId);
			break;
		case	2://record update cancelled(tab closed)
			toBeUpdated = syncRemoveTab(projectId, userId, webPageId, key);
			removeTemplateTab(projectId, userId, sessionId, webPageId, tabId);
			break;
		case	12://remove tab from template
			removeTemplateTab(projectId, userId, sessionId, webPageId, tabId);
			break;
		case	3://record deleted
			toBeUpdated = syncRemoveRecord(projectId, key, userId);
			break;
		case	4://record field FOCUSED
		case	5://record field CHANGED (and blurred)
		case	6://record field BLURRED (not changed)
			toBeUpdated = syncGetRecord(projectId, key);
			break;
		case	7://tab sync UPDATED (0:yok, 1:var, 2:hybrid)
			toBeUpdated = syncRecordEditMap(projectId, key, userId, webPageId, tabId, currentTime, GenericUtil.uShort(paramMap.get(".s")));
			break;
		case	8://record detailGrid field FOCUSED
			break;
		case	9://record detailGrid field CHANGED
			break;  
		case	11://record extra info changed (comment, fileAttachment, recordAccess, tableRelation, approval)
			toBeUpdated = syncUpdateRecord(projectId, key, userId, webPageId, false);
			break;
		case	17: // typing to otherUserId
			publishChatTyping(projectId, userId, sessionId, webPageId, GenericUtil.uInt(paramMap,".ou"), currentTime);
			break;
		//case	18:// not used -> server2client chat msg read notify
		//	break;

		}
		if(action==101){ // TODO: yanlis, cunku pek cok kaydi iptal etmis olabilir. dogrusu her bir kayit icin iptal edildigi bilgisini gondermek
			action = 2;
		} else if(GenericUtil.isEmpty(toBeUpdated)){
			return;
		} else {
			toBeUpdatedList = new ArrayList();
			toBeUpdatedList.add(toBeUpdated);
		}
		for(Map<Integer, Map<String, SyncTabMapHelper3>> toBeUpdated2:toBeUpdatedList)if(!GenericUtil.isEmpty(toBeUpdated2)){
			Map<W5DeferredResult, Map> broadcastMap = new HashMap();
			for(Integer u:toBeUpdated2.keySet()){
				Map<String, SyncTabMapHelper3> webPageMap = toBeUpdated2.get(u);
				Set<String>  keySet = webPageMap.keySet(); 
				for(String w:keySet)if(!webPageId.equals(w)){
					SyncTabMapHelper3 srh = webPageMap.get(w);
					if(srh==null){
						continue;
					} else {
//						if(action==1 || action==3)srh.setDirty(true);
					}
					
					Map m = new HashMap();
					m.put("success", true);
					m.put("liveSyncAction", action);
					if(action==1 || action==2 || action==3 || action==10){
						List<Map> users = new ArrayList<Map>();
						Set<Integer> us = new HashSet<Integer>();
						us.add(userId);
						us.add(u);
						for(Integer u2:toBeUpdated2.keySet())if(!us.contains(u2)){
							us.add(u2);
							Map<String, SyncTabMapHelper3> webPageMap2 = toBeUpdated2.get(u2);
							for(String w2 : webPageMap2.keySet())/*if(!w.equals(w2))*/{
								SyncTabMapHelper3 srh2 = webPageMap2.get(w2);
//								if(srh2.getDeferredResult()!=null){
									Map m2 = new HashMap();
									m2.put("userId", u2);
									m2.put("userDsc", getUserDsc(u2));
									users.add(m2);
//								}
							}
						}
						if(!users.isEmpty())m.put("users", users);
					}
					m.put("userId", userId);
					m.put("userDsc", getUserDsc(userId));
					m.put("tabId", srh.getTabId());
					m.put("pk", key);
					switch(action){
					case	4:case	5:case	6:
						m.put("fieldName", paramMap.get(".f"));
						if(action==5){
							m.put("newValue", paramMap.get(".nv"));
						}
						break;
					case	11:
						m.put("extra", paramMap.get(".e"));
						break;
					}
					broadCast(projectId, u, null, w, m);
				}								
			}
		}
	}

	private static boolean syncMobileStatus(String projectId, int userId, String mobileDeviceId, short status) {
		DeviceSyncHelper3 device = deviceMap3.get(mobileDeviceId);
		if(device==null)return false;
		if(device.getUserId()!=userId){
			CachedUserBean3 cub = getCachedUserBean(userId);
			if(cub.getSyncSessionMap()!=null)cub.getSyncSessionMap().remove(mobileDeviceId);
			device.setUserId(userId);
		}
		device.setStatus(status);
		switch(status){
		case	1://resume
			device.setLastMobileActionTime(System.currentTimeMillis());
			break;
		case	2://pause
			break;
			
		}
		return true;
	}

	private static void publishChatTyping(String projectId, int userId,
			String sessionId, String webPageId, int otherUserId, long currentTime) {
		if(userId==0 ||  webPageId==null || otherUserId==0)return;
		CachedUserBean3 ocub = getCachedUserBean(otherUserId);
		
		if(ocub!=null && ocub.getChatStatusTip()!=0 && !GenericUtil.isEmpty(ocub.getSyncSessionMap())){
			long limitTime = System.currentTimeMillis() - FrameworkSetting.asyncToleranceTimeout; 
			if(ocub.getLastAsyncActionTime()>limitTime){
				Map m = new HashMap();
				m.put("success", true);
				m.put("liveSyncAction", 17);
				m.put("userId", userId);
				ocub.broadCast(m);
			}
		}
	}

	private static boolean syncTabClearMessages(String projectId, int userId,
			String sessionId, String webPageId, String tabId, long currentTime) {
		if(userId==0 ||  webPageId==null || tabId==null)return false;
		CachedUserBean3 cub = getCachedUserBean(userId);
		
		if(sessionId==null)sessionId = cub.findSessionIdFromWebPageId(webPageId);
		if(sessionId==null)return false;
		if(cub.getSyncSessionMap()==null)cub.setSyncSessionMap(new HashMap());
		cub.setLastActionTime(currentTime);
		SyncSessionMapHelper3 ses = cub.getSyncSessionMap().get(sessionId);
		if(ses==null){
			ses = new SyncSessionMapHelper3();
			cub.getSyncSessionMap().put(sessionId, ses);
		}
		if(ses.getSyncWebPageMap()==null)ses.setSyncWebPageMap(new HashMap());
		SyncWebPageMapHelper3 web = ses.getSyncWebPageMap().get(webPageId);
		if(web!= null){
//			web.setActiveTab(tabId);
			web.setLastActionTime(currentTime);
			if(web.getSyncTabMap()!=null){
				SyncTabMapHelper3 tab = web.getSyncTabMap().get(tabId);
				if(tab!=null){
					tab.getMessages(true);
					return true;
				} else	
					return false;
				
			}
			return true;
		} else	
			return false;
	}

	public static void syncAfterPostFormAll(List<W5SynchAfterPostHelper> l){
		if(l==null)return;
		for(W5SynchAfterPostHelper o:l)syncAfterPostForm(o.getProjectId(), o.getTableId(), o.getKey(), o.getUserId(), o.getWebPageId(), o.getAction());
	}
	
	public static void syncAfterPostForm(String projectId, int tableId, String key, int userId, String webPageId, short action) {
		if(action==1 || action==3){
			Map<Integer, Map<String, SyncTabMapHelper3>> others =syncUpdateRecord( projectId,  key, userId, webPageId, true);
			long limitTime = System.currentTimeMillis() - FrameworkSetting.asyncToleranceTimeout;
			if(!GenericUtil.isEmpty(others)){
	//			Map<W5DeferredResult, Map> broadcastMap = new HashMap();
				for(Integer u:others.keySet()){
					Map<String, SyncTabMapHelper3> webPageMap = others.get(u);
					for(String w:webPageMap.keySet()){
						if(!webPageId.equals(w)){
							SyncTabMapHelper3 srh = webPageMap.get(w);
							if(srh!=null && srh.getLastActionTime()>limitTime){
								List<Map> users = new ArrayList();
								Set<Integer> us2 = new HashSet<Integer>();
								us2.add(userId);
								us2.add(u);
								for(Integer u2:others.keySet())if(!us2.contains(u2)){
									us2.add(u2);
									Map<String, SyncTabMapHelper3> webPageMap2 = others.get(u2);
									for(String w2 : webPageMap2.keySet())/*if(!w.equals(w2))*/{
										SyncTabMapHelper3 srh2 = webPageMap2.get(w2);
										if(srh2!=null && srh2.getLastActionTime()>limitTime){
											Map m = new HashMap();
											m.put("userId", u2);
											m.put("userDsc", getUserDsc(u2));
											users.add(m);
										}
									}
								}
								Map m = new HashMap();
								m.put("success", true);
								m.put("liveSyncAction", action==1 ? 1:3);
								if(!users.isEmpty())m.put("users", users);
								m.put("userId", userId);
								m.put("userDsc", getUserDsc(userId));
								m.put("tabId", srh.getTabId());
								m.put("pk", key);
								broadCast(projectId, u, null, w, m);
							}
						}
	
	
					}
				}
	//			for(W5DeferredResult d:broadcastMap.keySet())d.setResult(broadcastMap.get(d));
			}
		}
		broadCastRecordForTemplates(projectId, tableId, key, action, userId);
		
	}
	public	static boolean clearZombiUsers(long limit){
		if(!FrameworkSetting.liveSyncRecord)return false;
		long limitTime = System.currentTimeMillis()-limit; //30dka
//		for(Integer customizationId:userMap3.keySet()){
			Map<Integer, CachedUserBean3> um=userMap3;//.get(projectId);
			for(CachedUserBean3 cub:um.values())if(cub.getSyncSessionMap()!=null)for(String sessionId:cub.getSyncSessionMap().keySet()){
				SyncSessionMapHelper3 sm = cub.getSyncSessionMap().get(sessionId);
				if(sm.getLastActionTime()<limitTime && sm.getLastAsyncActionTime()<limitTime){// bu sessionin bitirilmesi lazim
					clearSyncSessionData((String)sm.getScd().get("projectId"), sm);
					cub.getSyncSessionMap().remove(sessionId);
				} else if(sm.getSyncWebPageMap()!=null)for(String webPageId:sm.getSyncWebPageMap().keySet()){
					SyncWebPageMapHelper3 web = sm.getSyncWebPageMap().get(webPageId);
					if(web.getLastActionTime()<limitTime && web.getLastAsyncActionTime()<limitTime){//temizlik yapilacak
						clearSyncWebPage((String)sm.getScd().get("projectId"), web);
						sm.getSyncWebPageMap().remove(webPageId);
					}
				}
				
			}
//		}
		return true;
		
	}

	private static void clearSyncSessionData(String projectId, SyncSessionMapHelper3 sm) {
		if(sm.getSyncWebPageMap()!=null)for(SyncWebPageMapHelper3 web:sm.getSyncWebPageMap().values())
			clearSyncWebPage(projectId, web);
		
	}

	private static void clearSyncWebPage(String projectId, SyncWebPageMapHelper3 web) {
		if(web.getSyncTabMap()!=null)for(SyncTabMapHelper3 tab:web.getSyncTabMap().values()){
			Map<Integer, Map<String, SyncTabMapHelper3>> tableMap = gridSyncMap3.get(projectId);
			if(tableMap==null)continue;
			for(Map<String, SyncTabMapHelper3> tabMap : tableMap.values()){
				tabMap.remove(tab.getTabId());
			}
		}
	}

	public static void publishUserChatMsgRead(Map<String, Object> scd,
			int toUserId, int msgId) {
		CachedUserBean3 ocub = getCachedUserBean(toUserId);
		long limitTime = System.currentTimeMillis() - FrameworkSetting.asyncToleranceTimeout;
		if(ocub!=null && ocub.getChatStatusTip()!=0 && ocub.getSyncSessionMap()!=null)for(SyncSessionMapHelper3 ses:ocub.getSyncSessionMap().values())if(ses.getDeviceType()==0 && ses.getSyncWebPageMap()!=null && ses.getLastAsyncActionTime()>limitTime){ //web icinse okudum olarak isaretle
			Map m = new HashMap();
			m.put("success", true);
			m.put("liveSyncAction", 18); //chat msg read
			m.put("fromUserId", (Integer)scd.get("userId"));
			ocub.broadCast(m);
			return;
		}
		
	}

/*
	public static List publishObject2User(String projectId, int fromUserId, int toUserId, Map o){
	        CachedUserBean3 cub = getCachedUserBean(toUserId);
	        if(cub == null)
	            return null;
	        int b = 0;
	        if(cub.getChatStatusTip() != 0)
	        {
	            Map m = new HashMap();
	            m.put("success", Boolean.valueOf(true));
	            m.putAll(o);
	            b += cub.broadCast(m);
	        }
	        return null;
	}

	public	static Map<Integer, Map<String, Map<String, MQOnlineUserHelper>>> mqOnlineUsers = new HashMap<Integer, Map<String, Map<String, MQOnlineUserHelper>>>();//cusId, projectId, instanceId, List of Users

	public static void activateMQs() {
		if(!FrameworkSetting.mq)return;
		Map<String, Channel> hostChannelMap = new HashMap();
		for(W5Project po:FrameworkCache.wProjects.values())if(po.getMqFlag()!=0)try{
			Channel channel = hostChannelMap.get(po.getMqUrl());
			if(channel==null){
				ConnectionFactory factory = new ConnectionFactory();
				factory.setHost(po.getMqUrl());
				Connection connection = factory.newConnection();
				channel = connection.createChannel();
				hostChannelMap.put(po.getMqUrl(), channel);
			}
			channel.exchangeDeclare(po.getProjectUuid(), "fanout");
			
			if(!mqOnlineUsers.containsKey(po.getCustomizationId()))mqOnlineUsers.put(po.getCustomizationId(), new HashMap());
			mqOnlineUsers.get(po.getCustomizationId()).put(po.getProjectUuid(), new HashMap());
			
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("x-message-ttl", 5000);//5 sn
			channel.queueDeclare(FrameworkSetting.instanceUuid,false, false, false, args);
			channel.queueBind(FrameworkSetting.instanceUuid, po.getProjectUuid(), "");

		    
			Consumer consumer = new DefaultConsumer(channel) {
			      @Override
			      public void handleDelivery(String consumerTag, Envelope envelope,
			                                 AMQP.BasicProperties properties, byte[] body) throws IOException {
			        String m = new String(body, "UTF-8");
			        if(FrameworkSetting.debug){
				      //  System.out.println(" [x] Received '" + m + "'");
			        }
			        if(m==null || m.length()<9)return;
			        if(!m.startsWith("iwb:")){
//			        	for(W5Project po:FrameworkCache.wProjects.values())if(po.getMqFlag()!=0);
			        	return;
			        }

			        int i1 = m.indexOf(','), i2 = m.indexOf(',', i1+1), i3 = m.indexOf(',', i2+1);
			        int action = GenericUtil.uInt(m.substring(4,i1));
			        String projectId = GenericUtil.uInt(m.substring(i1+1,i2));
			        String projectUuid = m.substring(i2+1,i3);

					W5Project po = FrameworkCache.wProjects.get(projectUuid);
			        if(po==null || po.getMqFlag()==0)return;
			        String instanceUuid = m.substring(i3+1,37+i3);
			        if(instanceUuid.equals(FrameworkSetting.instanceUuid))return; //kendisi ise yapma
			        
			        switch(action){
			        case	69://reloadCache : TODO
			        	
			        	break;
			        case	32: //change userChatStatus
//			s.append("iwb:32,").append(po.getCustomizationId()).append(",").append(po.getProjectUuid()).append(",").append(FrameworkSetting.instanceUuid).append(",").append(userId).append(",").append(chatStatus);
				        	int cmi4 = m.indexOf(',', i3+1), cmi5 = m.indexOf(',', cmi4+1);
				        	int cUserId = GenericUtil.uInt(m.substring(cmi4+1,cmi5));
				        	short cchatStatus = (short)GenericUtil.uInt(m.substring(cmi5+1));

				        	publishUserStatus(customizationId, cUserId, cchatStatus, null, projectUuid);

				        	Map<String, MQOnlineUserHelper> mmqo2 = mqOnlineUsers.get(po.getCustomizationId()).get(po.getProjectUuid());
				        	if(mmqo2==null)return;
				        	MQOnlineUserHelper mqo2 = mmqo2.get(instanceUuid);
				        	
							long limitTime = System.currentTimeMillis() - (90*1000);
				        	if(mqo2==null || mqo2.getOnlineUsers()==null && mqo2.getRefreshTime()<limitTime)return;
				        	MQOnlineUser csFound = null;
				        	for(int qi=0;qi<mqo2.getOnlineUsers().size();qi++){
				        		MQOnlineUser ou = mqo2.getOnlineUsers().get(qi);
				        		if(ou.getUserId()==cUserId){
					        		if(cchatStatus==0){
					        			mqo2.getOnlineUsers().remove(qi);
					        			return;
					        		} else {
					        			ou.setChatStatusTip(cchatStatus);
					        			return;
					        		}
					        	}
				        	}
				        	mqo2.getOnlineUsers().add(new MQOnlineUser(cUserId, cchatStatus));

			        	
			        	break;

			        case	31: //post chat message
			        	int mi4 = m.indexOf(',', i3+1), mi5 = m.indexOf(',', mi4+1), mi6 = m.indexOf(',', mi5+1), mi7 = m.indexOf(',', mi6+1), mi8 = m.indexOf(',', mi7+1);
			        	int unitId = GenericUtil.uInt(m.substring(mi4+1,mi5));
			        	int fromUserId = GenericUtil.uInt(m.substring(mi5+1,mi6));
			        	int toUserId = GenericUtil.uInt(m.substring(mi6+1,mi7));
			        	String chatId = m.substring(mi7+1, mi8);
			        	String msg = m.substring(mi8+1);
			        	publishUserChatMsg(customizationId, fromUserId, toUserId, msg, chatId);
			        	break;
			        case	30: //online user list
			        	Map<String, MQOnlineUserHelper> mmqo = mqOnlineUsers.get(po.getCustomizationId()).get(po.getProjectUuid());
			        	if(mmqo==null)return;
			        	MQOnlineUserHelper mqo = mmqo.get(instanceUuid);
			        	if(mqo==null){
			        		mqo = new MQOnlineUserHelper();
			        		mmqo.put(instanceUuid, mqo);
			        	} else {
			        		mqo.reset();
			        	}
			        	String[] ochunks = m.split(";");
			        	if(ochunks.length>1)for(int qi=1;qi<ochunks.length;qi++)mqo.getOnlineUsers().add(new MQOnlineUser(ochunks[qi]));

			        	break;
			        case	41://SyncAfterPostFormAll
			        	String[] sochunks = m.split(";");
			        	if(sochunks.length>1){
				        	int xmi4 = m.indexOf(',', i3+1), xmi5 = m.indexOf(',', xmi4+1), xmi6 = m.indexOf(';', xmi5+1);
				        	int xunitId = GenericUtil.uInt(m.substring(xmi4+1,xmi5));
				        	int xUserId = GenericUtil.uInt(m.substring(xmi5+1,xmi6));

				        	List<W5SynchAfterPostHelper> sl = new ArrayList();
			        		for(int qi=1;qi<sochunks.length;qi++)sl.add(new W5SynchAfterPostHelper(customizationId, xUserId, sochunks[qi]));
			        		syncAfterPostFormAll(sl);
			        	}
			        	break;
			        case	99://special
			        	Map m2 = new HashMap();
						m2.put("success", true);
						m2.put("liveSyncAction", 99);
						m2.put("msg", m.substring(38+i3));
						m2.put("instanceId", instanceUuid);
						getCachedUserBean(10).broadCast(m2);
			        	break;
			        	
			        }
			      }
			};
			channel.basicConsume(FrameworkSetting.instanceUuid, true, consumer);
			po.set_mqChannel(channel);
		} catch (Exception e) {
			if(FrameworkSetting.debug)e.printStackTrace();
			po.setMqFlag((short)0);
		}
		
	}

	public static boolean mqPublishUserChatMsg(Map<String, Object> scd, int userId, String msg, Object chatId) {
		if(!FrameworkSetting.mq)return false;
		W5Project po = FrameworkCache.wProjects.get((String)scd.get("projectId"));
		if(po.getMqFlag()==0 || po.get_mqChannel()==null)return false;
		// TODO Auto-generated method stub
		int cid = (Integer) scd.get("customizationId"),	fuserId =	(Integer) scd.get("userId"),	userUnitId =	GenericUtil.uInt(scd.get("unitId"));
		
		StringBuilder s = new StringBuilder();
		s.append("iwb:31,").append(cid).append(",").append((String)scd.get("projectId"))
		.append(",").append(FrameworkSetting.instanceUuid).append(",").append(userUnitId).append(",").append(fuserId).append(",").append(userId).append(",").append(chatId).append(",").append(msg);
//		Map m = new HashMap(); m.put("ali", "veli")
		try {
			po.get_mqChannel().basicPublish(po.getProjectUuid(), "", null, s.toString().getBytes());
			
//			mqPublishOnlineUsers();
			
			return true;
		} catch (IOException e) {
			if(FrameworkSetting.debug)e.printStackTrace();
			return false;
		}
	}

	public static boolean mqSyncAfterPostFormAll(Map<String, Object> scd, List<W5SynchAfterPostHelper> l) {
		if(GenericUtil.isEmpty(l))return false;
		if(!FrameworkSetting.mq)return false;
		W5Project po = FrameworkCache.wProjects.get((String)scd.get("projectId"));
		if(po.getMqFlag()==0 || po.get_mqChannel()==null)return false;
		// TODO Auto-generated method stub
		int cid = (Integer) scd.get("customizationId"),	userId =	(Integer) scd.get("userId"),	userUnitId =	GenericUtil.uInt(scd.get("unitId"));
		
		StringBuilder s = new StringBuilder();
		s.append("iwb:41,").append(cid).append(",").append((String)scd.get("projectId"))
		.append(",").append(FrameworkSetting.instanceUuid).append(",").append(userUnitId).append(",").append(userId);//.append(",").append(userId).append(",").append(chatId).append(",").append(msg);
		for(W5SynchAfterPostHelper o:l)s.append(";").append(o.getTableId()).append(",").append(o.getKey()).append(",").append(o.getWebPageId()).append(",").append(o.getAction());

//		Map m = new HashMap(); m.put("ali", "veli")
		try {
			po.get_mqChannel().basicPublish(po.getProjectUuid(), "", null, s.toString().getBytes());
			return true;
		} catch (IOException e) {
			if(FrameworkSetting.debug)e.printStackTrace();
			return false;
		}
	}

	

	private static void mqPublishUserStatus(String projectId, int userId, short chatStatus) {
		if(!FrameworkSetting.mq)return;
		for(W5Project po:FrameworkCache.wProjects.values())if(po.getMqFlag()!=0 && po.getCustomizationId()==customizationId)try {
			StringBuilder s = new StringBuilder();
			s.append("iwb:32,").append(po.getCustomizationId()).append(",").append(po.getProjectUuid()).append(",").append(FrameworkSetting.instanceUuid).append(",").append(userId).append(",").append(chatStatus);
			po.get_mqChannel().basicPublish(po.getProjectUuid(), "", null, s.toString().getBytes());
		} catch (Exception e) {
			if(FrameworkSetting.debug)e.printStackTrace();
			po.set_mqChannel(null);
			po.setMqFlag((short)0);
		}
		
	}
	
	public static void mqPublishOnlineUsers() {
		if(!FrameworkSetting.mq)return;
//		System.out.println("mqPublishOnlineUsers");
		long curTime = System.currentTimeMillis();
		long awayTime = curTime-FrameworkSetting.onlineUsersAwayMinute;			
		long limitTime = curTime-FrameworkSetting.onlineUsersLimitMinute;
		long limitMobileTime = curTime-FrameworkSetting.onlineUsersLimitMobileMinute;
		for(W5Project po:FrameworkCache.wProjects.values())if(po.getMqFlag()!=0 && po.get_mqChannel()!=null){
			
			StringBuilder s = new StringBuilder();
			s.append("iwb:30,").append(po.getCustomizationId()).append(",").append(po.getProjectUuid())
			.append(",").append(FrameworkSetting.instanceUuid);//.append(",").append(userUnitId).append(",").append(userId);//.append(",").append(userId).append(",").append(chatId).append(",").append(msg);
//			for(W5SynchAfterPostHelper o:l)s.append(";").append(o.getTableId()).append(",").append(o.getKey()).append(",").append(o.getWebPageId()).append(",").append(o.getAction());
			
			String projectId = po.getCustomizationId();
				
			
			List<Object[]> data=new ArrayList<Object[]>();
			for(Map.Entry<Integer, CachedUserBean3> mcub : userMap3.get(projectId).entrySet()){
				CachedUserBean3 cub = mcub.getValue();
				if(cub.getSyncSessionMap()!=null && cub.getChatStatusTip()!=0){
					Map<String, SyncSessionMapHelper3> m = cub.getSyncSessionMap();
					if(GenericUtil.isEmpty(m))continue;
					SyncSessionMapHelper3 bestOub = new SyncSessionMapHelper3();
					bestOub.setLastActionTime(limitTime);
					bestOub.setLastAsyncActionTime(awayTime);
					int onlineCount = 0; short deviceType = 0; long mobileActionTime = limitMobileTime;
					for(SyncSessionMapHelper3 oub:m.values())if(!GenericUtil.isEmpty(oub.getScd()) && oub.getScd().get("projectId")!=null && oub.getScd().get("projectId").equals(po.getProjectUuid())){
						if(oub.getDeviceType()!=0){
							deviceType = oub.getDeviceType();
							if(bestOub.getScd()==null)bestOub.setScd(oub.getScd());
							if(oub.getLastActionTime()>mobileActionTime)mobileActionTime = oub.getLastActionTime();
//							onlineCount++;
						}
						boolean b = true;
//						long lastActionTime
						if(oub.getLastActionTime()-bestOub.getLastActionTime()>0){
							bestOub=oub;
							onlineCount++;
							b = false;
						} 
						if(oub.getLastAsyncActionTime()-bestOub.getLastAsyncActionTime()>0){
							bestOub.setLastAsyncActionTime(oub.getLastAsyncActionTime());
							if(b)onlineCount++;
						}
					}
					if(bestOub.getScd()==null)continue;
					if((bestOub.getDeviceType()==0 && (bestOub.getLastActionTime()>limitTime || bestOub.getLastAsyncActionTime()>awayTime)) || (deviceType!=0 && mobileActionTime>limitMobileTime)){
						if((Integer)bestOub.getScd().get("customizationId")!=customizationId)continue;
						short chatStatusTip = cub.getChatStatusTip();
						if(chatStatusTip==1 && bestOub.getLastActionTime()<awayTime)chatStatusTip=(short)3;//away
						s.append(";").append(mcub.getKey()).append(",").append(bestOub.getScd().get("userTip")).append(",").append(bestOub.getScd().get("roleId")).append(",").append(bestOub.getScd().get("unitId")).append(",").append(chatStatusTip).append(deviceType!=0 ? 1:0);
					}
				}
			}

//			Map m = new HashMap(); m.put("ali", "veli")
			try {
				po.get_mqChannel().basicPublish(po.getProjectUuid(), "", null, s.toString().getBytes());
			} catch (IOException e) {
				if(FrameworkSetting.debug)e.printStackTrace();
			}
			
		}
	}
*/
}

/*
class	MQOnlineUser{
	private int userId;
	private int userTip;
	private int roleId;
	private int unitId;
	private short chatStatusTip;
	private short mobile;
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getUnitId() {
		return unitId;
	}
	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}
	public short getChatStatusTip() {
		return chatStatusTip;
	}
	public void setChatStatusTip(short chatStatusTip) {
		this.chatStatusTip = chatStatusTip;
	}
	public short getMobile() {
		return mobile;
	}
	public void setMobile(short mobile) {
		this.mobile = mobile;
	}
//s.append(";").append(mcub.getKey()).append(",").append(bestOub.getScd().get("userTip")).append(",").append(bestOub.getScd().get("roleId")).append(",").append(bestOub.getScd().get("unitId")).append(",").append(chatStatusTip).append(deviceType!=0 ? 1:0);

	
	public MQOnlineUser(String chunk) {
		if(chunk==null)return;
		String[] s=chunk.split(",");
		this.userId = GenericUtil.uInt(s[0]); 
		this.userTip = GenericUtil.uInt(s[1]); 
		this.roleId = GenericUtil.uInt(s[2]); 
		this.unitId = GenericUtil.uInt(s[3]); 
		this.chatStatusTip = (short)GenericUtil.uInt(s[4].substring(0, 1)); 
		this.mobile = (short)GenericUtil.uInt(s[4].substring(1, 2));		
	}
	
	
	public MQOnlineUser(int userId, short chatStatusTip) {
		super();
		this.userId = userId;
		this.chatStatusTip = chatStatusTip;
	}
	public int getUserTip() {
		return userTip;
	}
	public void setUserTip(int userTip) {
		this.userTip = userTip;
	}
	public int getRoleId() {
		return roleId;
	}
	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}
	
	
	
}

class	MQOnlineUserHelper{
	private	long refreshTime;
	private	List<MQOnlineUser>	onlineUsers;
	
	public long getRefreshTime() {
		return refreshTime;
	}
	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}
	public List<MQOnlineUser> getOnlineUsers() {
		return onlineUsers;
	}
	public void setOnlineUsers(List<MQOnlineUser> onlineUsers) {
		this.onlineUsers = onlineUsers;
	}
	public MQOnlineUserHelper() {
		this.refreshTime =System.currentTimeMillis(); 
		this.onlineUsers = new ArrayList();
	}
	public void reset(){
		this.refreshTime =System.currentTimeMillis(); 
		this.onlineUsers.clear();
	}
	
}*/
