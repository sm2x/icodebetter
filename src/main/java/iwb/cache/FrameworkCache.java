package iwb.cache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/*
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;*/

import iwb.domain.db.Log5Feed;
import iwb.domain.db.M5List;
import iwb.domain.db.W5Card;
import iwb.domain.db.W5Component;
import iwb.domain.db.W5Conversion;
import iwb.domain.db.W5Customization;
import iwb.domain.db.W5ExternalDb;
import iwb.domain.db.W5Form;
import iwb.domain.db.W5GlobalFunc;
import iwb.domain.db.W5Grid;
import iwb.domain.db.W5JobSchedule;
import iwb.domain.db.W5List;
import iwb.domain.db.W5LookUp;
import iwb.domain.db.W5Mq;
import iwb.domain.db.W5Page;
import iwb.domain.db.W5Project;
import iwb.domain.db.W5Query;
import iwb.domain.db.W5QueryField;
import iwb.domain.db.W5Table;
import iwb.domain.db.W5TableEvent;
import iwb.domain.db.W5Workflow;
import iwb.domain.db.W5Ws;
import iwb.domain.db.W5WsMethod;
import iwb.domain.db.W5WsServer;
import iwb.exception.IWBException;
import iwb.util.GenericUtil;

public class FrameworkCache {

	final private static Map<String, Map<Integer,W5LookUp>> wLookUps = new HashMap<String, Map<Integer,W5LookUp>>(); //customizationId
	final private static Map<String, Map<Integer, W5Query>> wQueries = new HashMap<String, Map<Integer, W5Query>>();
	final private static Map<String, Map<Integer, W5Conversion>> wConversions = new HashMap<String, Map<Integer, W5Conversion>>();
	final private static Map<String, Map<Integer, W5Grid>> wGrids = new HashMap<String, Map<Integer, W5Grid>>();
	final private static Map<String, Map<Integer, W5List>> wListViews = new HashMap<String, Map<Integer, W5List>>();
	final private static Map<String, Map<Integer, M5List>> mListViews = new HashMap<String, Map<Integer, M5List>>();
	final private static Map<String, Map<Integer, W5Card>> wCards = new HashMap<String, Map<Integer, W5Card>>();
	final private static Map<String, Map<Integer, W5Form>> wForms = new HashMap<String, Map<Integer, W5Form>>();
	final private static Map<String, Map<Integer, W5GlobalFunc>> wGlobalFuncs = new HashMap<String, Map<Integer, W5GlobalFunc>>();
	final private static Map<String, Map<Integer, W5Page>> wTemplates = new HashMap<String, Map<Integer, W5Page>>();
	final private static Map<String, Map<Integer, W5Component>> wComponents = new HashMap<String, Map<Integer, W5Component>>();

	final private static Map<String, Map<Integer, W5Table>> wTables = new HashMap<String, Map<Integer, W5Table>>();
	final private static Map<String, Map<Integer, List<W5TableEvent>>> wTableEvents = new HashMap<String, Map<Integer,List<W5TableEvent>>>();	
/*	final private static Map<String, Map<Integer, Integer>> wTableFieldMap = new HashMap<String, Map<Integer, Integer>>();
	final private static Map<String, List<W5TableParam>> tableParamListMap = new HashMap<String, List<W5TableParam>>();
	final private static Map<String, List<W5TableChild>> tableChildListMap = new HashMap<String, List<W5TableChild>>();//copy
	final private static Map<String, List<W5TableChild>> tableParentListMap = new HashMap<String, List<W5TableChild>>();//watch,feed
*/
	
	final private static Map<String, Map<Integer, String>> wPageCss = new HashMap<String, Map<Integer, String>>();
	final private static Map<String, Map<String, Object>> wGraalFuncs = new HashMap<String, Map<String, Object>>();

	final public static Map<String, Map<Integer, W5Workflow>> wWorkflows = new HashMap<String,Map<Integer, W5Workflow>>();
	final private static List<W5Customization> wCustomization = new ArrayList<W5Customization>();
	final private static Map<String, W5Project> wProjects = new HashMap<String, W5Project>(); //projectUuid

//	final private static Map<String, Map<Integer, W5TsPortlet>> wTsPortlets = new HashMap<String, Map<Integer, W5TsPortlet>>();
//	final private static Map<String, Map<Integer, W5TsMeasurement>> wTsMeasurements = new HashMap<String, Map<Integer, W5TsMeasurement>>();

	final public static Map<String, Map<Integer, W5ExternalDb>> wExternalDbs = new HashMap<String, Map<Integer, W5ExternalDb>>();
	final public static Map<String, Map<Integer, W5Mq>> wMqs = new HashMap<String, Map<Integer, W5Mq>>();

	
	final public static Map<String, String> wExceptions = new HashMap<String, String>();
	final public static Map<Integer, Map<String, String>> appSettings= new HashMap<Integer, Map<String, String>>();
	final public static List<String> publishAppSettings= new ArrayList<String>();
//	final public static Map<String, List<Integer>> publishLookUps= new HashMap<String, List<Integer>>();
	final public static Map<Integer, Map<Integer, String>> wRoles = new HashMap<Integer, Map<Integer, String>>();
	final public static Map<String, List<Log5Feed>> wFeeds = new HashMap<String, List<Log5Feed>>();
	final public static Map<String, Map<Integer, W5JobSchedule>> wJobs= new HashMap<String, Map<Integer, W5JobSchedule>>();
	final private static Map<String, Map<String, Long>> wQueuedReloadCache = new HashMap<String, Map<String, Long>>();
//	final public static HashMap<String , List<Object> > lastUserAction=new HashMap<String, List<Object>>();
	final public static Map<Integer, W5Customization> wCustomizationMap = new HashMap<Integer, W5Customization>();
	final private static Map<String, Map<String, W5Ws>> wWsClients = new HashMap<String, Map<String, W5Ws>>(); //wsId
	final private static Map<String, Map<Integer, W5WsMethod>> wWsMethods = new HashMap<String, Map<Integer, W5WsMethod>>(); //wsId
	final private static Set<String> wDevEntityKeys = new HashSet<String>();

	public static List<W5QueryField> cachedOnlineQueryFields = null;
	
	
	final public static Map<String, Date> wRequestUrls= new HashMap<String, Date>();
	final public static Map<String, Map<Integer,Set<String>>> wValidateLookupMap= new HashMap<String, Map<Integer,Set<String>>>();
	
	final private static Map<String, Map<String, W5WsServer>> wWsServers = new HashMap<String, Map<String, W5WsServer>>(); //wsId

	public static void setDevEntityKeys(Set<String> m){
		wDevEntityKeys.clear();
		wDevEntityKeys.addAll(m);
	}

	public static boolean isDevEntity(String key){
		return wDevEntityKeys.contains(key);
	}

	public static W5Customization getCustomization(int customizationId){
		if(wCustomization==null)return null;
		for(W5Customization c:wCustomization)if(c.getCustomizationId()==customizationId)return c;
		return null;
	}

	public	static void clearPreloadCache(Object o){
		String projectId = getProjectId(o, null);
		Map wq = wQueries.get(projectId); if(wq!=null)wq.clear();
		wq = wGrids.get(projectId); if(wq!=null)wq.clear();
		wq = wForms.get(projectId); if(wq!=null)wq.clear();
		wq = wGlobalFuncs.get(projectId); if(wq!=null)wq.clear();
		wq = wTemplates.get(projectId); if(wq!=null)wq.clear();
		wq = wCards.get(projectId); if(wq!=null)wq.clear();
		wq = wListViews.get(projectId); if(wq!=null)wq.clear();
		wq = mListViews.get(projectId); if(wq!=null)wq.clear();
		wq = wConversions.get(projectId); if(wq!=null)wq.clear();
//		wq = wComponents.get(projectId); if(wq!=null)wq.clear();
		wq = wGraalFuncs.get(projectId); if(wq!=null)wq.clear();
		
	}

	
	public	static int getCustomizationId(Object o){
		if(o==null)return 0;
		if(o instanceof Integer)return (Integer)o;
		if(o instanceof String)return GenericUtil.uInt((String)o);
		if(o instanceof HashMap)return GenericUtil.uInt(((HashMap)o).get("customizationId"));;
		return 0;
	}
	
	
	public	static String getProjectId(Object o, String devKey){
		if(devKey!=null&& wDevEntityKeys.contains(devKey))return FrameworkSetting.devUuid;
		if(o==null)return null;
		if(o instanceof String)return (String)o;
		if(o instanceof HashMap)return (String)(((HashMap)o).get("projectId"));;
		return null;
	}
	
	
	public	static void addProject(W5Project p){
		wProjects.put(p.getProjectUuid(), p);
	}
	
	public	static W5Project getProject(Object o){
		return wProjects.get(getProjectId(o, null));
	}
	
	public	static W5Project getProject(Object o, String error){
		W5Project po = wProjects.get(getProjectId(o, null));
		if(po==null)
			throw new IWBException("framework", "Project", 0, o.toString(), error, null);
		return po;
	}
	
	
	public static W5Table getTable(Object o, int tableId){
		String projectId = getProjectId(o, "15."+tableId);
		if(false && FrameworkSetting.debug && FrameworkCache.hasQueuedReloadCache(projectId,"15."+tableId)){
			Integer status = FrameworkSetting.projectSystemStatus.get(projectId);
			if(status!=null && status==0)throw new IWBException("cache","Table",tableId, null, "Cache not reloaded. Please Reload Cache", null);
		}
		Map<Integer, W5Table> map = wTables.get(projectId);
		if(map==null)return null;
		return map.get(tableId);
	}
	
	public static W5Card getCard(Object o, int dataViewId) {
		String projectId = getProjectId(o, "930."+dataViewId);
		if(!wCards.containsKey(projectId)){
			wCards.put(projectId, new HashMap());
			return null;
		} else
			return wCards.get(projectId).get(dataViewId);
	}
	public static void addCard(Object o, W5Card d){
		int dataViewId = d.getDataViewId();
		String projectId = getProjectId(o, "930."+dataViewId);
		//addX((Map)wDataViews, projectId, dataViewId, d);
		if(wCards.get(projectId)==null){
			wCards.put(projectId, new HashMap());
		}
		wCards.get(projectId).put(dataViewId, d);
	}


	public static W5Component getComponent(Object o, int componentId) {
		String projectId = getProjectId(o, "3351."+componentId);
		if(!wComponents.containsKey(projectId)){
			wComponents.put(projectId, new HashMap());
			return null;
		} else
			return wComponents.get(projectId).get(componentId);
	}
	public static void setComponentMap(Object o, Map<Integer, W5Component> m){
		String projectId = getProjectId(o, null);
		//addX((Map)wDataViews, projectId, dataViewId, d);
		wComponents.put(projectId, m);
	}

	public static W5Workflow getWorkflow(Object o, int approvalId) {
		String projectId = getProjectId(o, "389."+approvalId);
		if(!wWorkflows.containsKey(projectId)){
			wWorkflows.put(projectId, new HashMap());
			return null;
		} else
			return wWorkflows.get(projectId).get(approvalId);
	}

	public static void addWorkflow(Object o, W5Workflow w) {
		String projectId = getProjectId(o, "389."+w.getApprovalId());
		if(!wWorkflows.containsKey(projectId)){
			wWorkflows.put(projectId, new HashMap());
		} else
		wWorkflows.get(projectId).put(w.getApprovalId(), w);
	}
	

	public static void clearProjectWorkflows(String projectId) {
		wWorkflows.put(projectId, new HashMap());

	}
	
	public static W5WsMethod getWsMethod(Object o, int methodId) {
		String projectId = getProjectId(o, "1376."+methodId);
		if(!wWsMethods.containsKey(projectId)){
			wWsMethods.put(projectId, new HashMap());
			return null;
		} else
			return wWsMethods.get(projectId).get(methodId);
	}
	
	public static void addWsMethod(Object o, W5WsMethod method){
		int methodId = method.getWsMethodId();
		String projectId = getProjectId(o, "1376."+methodId);
		if(wWsMethods.get(projectId)==null){
			wWsMethods.put(projectId, new HashMap());
		}
		wWsMethods.get(projectId).put(methodId, method);
	}
	
	public static W5GlobalFunc getGlobalFunc(Object o, int funcId) {
		String projectId = getProjectId(o, "20."+funcId);
		if(!wGlobalFuncs.containsKey(projectId)){
			wGlobalFuncs.put(projectId, new HashMap());
			return null;
		} else
			return wGlobalFuncs.get(projectId).get(funcId);
	}
	public static void addGlobalFunc(Object o, W5GlobalFunc func){
		int funcId = func.getDbFuncId();
		String projectId = getProjectId(o, "20."+funcId);
		if(wGlobalFuncs.get(projectId)==null){
			wGlobalFuncs.put(projectId, new HashMap());
		}
		wGlobalFuncs.get(projectId).put(funcId, func);
	}

	public static void addQueuedReloadCache(Object o, String key){
		String projectId = getProjectId(o, null);
		if(wQueuedReloadCache.get(projectId)==null){
			wQueuedReloadCache.put(projectId, new HashMap());
		}
		wQueuedReloadCache.get(projectId).put(key, System.currentTimeMillis());
	}
	public static boolean hasQueuedReloadCache(Object o, String key){
		Map m = wQueuedReloadCache.get(getProjectId(o, null));
		return m!=null && m.containsKey(key);
	}

	public static void clearReloadCache(Object o){
		Map m = wQueuedReloadCache.get(getProjectId(o, null));
		if(m!=null)m.clear();
	}
	
	public static W5List getListView(Object o, int listId) {
		String projectId = getProjectId(o, "936."+listId);
		if(!wListViews.containsKey(projectId)){
			wListViews.put(projectId, new HashMap());
			return null;
		} else
			return wListViews.get(projectId).get(listId);
	}
	public static void addListView(Object o, W5List list){
		int listId = list.getListId();
		String projectId = getProjectId(o, "936."+listId);
		if(wListViews.get(projectId)==null){
			wListViews.put(projectId, new HashMap());
		}
		wListViews.get(projectId).put(listId, list);
	}

	
	public static W5Conversion getConversion(Object o, int conversionId) {
		String projectId = getProjectId(o, "707."+conversionId);
		if(!wConversions.containsKey(projectId)){
			wConversions.put(projectId, new HashMap());
			return null;
		} else
			return wConversions.get(projectId).get(conversionId);
	}
	
	
	public static List<W5Conversion> listConversion4Form(Object o, int formId, int tableId) {
		List<W5Conversion> r = new ArrayList();
		String projectId = getProjectId(o, "40."+formId);
		if(!wConversions.containsKey(projectId)){
			wConversions.put(projectId, new HashMap());
			return r;
		}
		Map<Integer, W5Conversion> cMap = wConversions.get(projectId);
		for(W5Conversion c:cMap.values())if((c.getSrcDstTip()==0 && c.getSrcFormId() == formId) || (c.getSrcDstTip()==1 && c.getSrcTableId()==tableId)){
			r.add(c);
		}
		return r;
	}
	
	public static void addConversion(Object o, W5Conversion cnv){
		int conversionId = cnv.getConversionId();
		String projectId = getProjectId(o, "707."+conversionId);
		if(wConversions.get(projectId)==null){
			wConversions.put(projectId, new HashMap());
		}
		wConversions.get(projectId).put(conversionId, cnv);
	}

	
	public static M5List getMListView(Object o, int listId) {
		String projectId = getProjectId(o, "1345."+listId);
		if(!mListViews.containsKey(projectId)){
			mListViews.put(projectId, new HashMap());
			return null;
		} else
			return mListViews.get(projectId).get(listId);
	}
	
	public static void addMListView(Object o, M5List list) {
		int listId = list.getListId();
		String projectId = getProjectId(o, "1345."+listId);
		if(mListViews.get(projectId)==null){
			mListViews.put(projectId, new HashMap());
		}
		mListViews.get(projectId).put(listId, list);
	}
	
	public static W5Grid getGrid(Object o, int gridId){
		String projectId = getProjectId(o, "5."+gridId);
		if(wGrids.get(projectId)==null){
			wGrids.put(projectId, new HashMap());
			return null;
		} else
			return wGrids.get(projectId).get(gridId);
	}
	
	public static void addGrid(Object o, W5Grid grid){
		int gridId = grid.getGridId();
		String projectId = getProjectId(o, "5."+gridId);
		if(wGrids.get(projectId)==null){
			wGrids.put(projectId, new HashMap());
		}
		wGrids.get(projectId).put(gridId, grid);
	}
	
	public static W5Form getForm(Object o, int formId){
		String projectId = getProjectId(o, "40."+formId);
		if(wForms.get(projectId)==null){
			wForms.put(projectId, new HashMap());
			return null;
		} else
			return wForms.get(projectId).get(formId);
	}
	
	public static void addForm(Object o, W5Form form){
		int formId = form.getFormId();
		String projectId = getProjectId(o, "40."+formId);
		if(wForms.get(projectId)==null){
			wForms.put(projectId, new HashMap());
		}
		wForms.get(projectId).put(formId, form);
	}
	
	public static W5Query getQuery(Object o, int queryId){
		String projectId = getProjectId(o, "8."+queryId);
		if(wQueries.get(projectId)==null){
			wQueries.put(projectId, new HashMap());
			return null;
		} else
			return wQueries.get(projectId).get(queryId);
	}
	
	public static void addQuery(Object o, W5Query query){
		int queryId = query.getQueryId();
		String projectId = getProjectId(o, "8."+queryId);
		if(wQueries.get(projectId)==null){
			wQueries.put(projectId, new HashMap());
		}
		wQueries.get(projectId).put(queryId, query);
	}
	
	public static W5Page getPage(Object o, int pageId){
		String projectId = getProjectId(o, "63."+pageId);
		if(wTemplates.get(projectId)==null){
			wTemplates.put(projectId, new HashMap());
			return null;
		} else
			return wTemplates.get(projectId).get(pageId);
	}
	
	public static void addX(Map m,String projectId, int id, Object obj){
		if(m.get(projectId)==null){
			m.put(projectId, new HashMap());
		}
		((Map<Integer, Object>)m.get(projectId)).put(id, obj);
	}
	
	public static void addPage(Object o, W5Page page){
		int pageId = page.getTemplateId();
		String projectId = getProjectId(o, "63."+pageId);
		//addX((Map)wTemplates, projectId, pageId, page);
		if(wTemplates.get(projectId)==null){
			wTemplates.put(projectId, new HashMap());
		}
		wTemplates.get(projectId).put(pageId, page);
	}
	
	public static W5LookUp getLookUp(Object o, int lookUpId){
		String projectId = getProjectId(o, "13."+lookUpId);
		if(FrameworkSetting.debug && FrameworkCache.hasQueuedReloadCache(projectId,"13."+lookUpId))
			throw new IWBException("cache","LookUp",lookUpId, null, "Cache not reloaded. Please Reload Cache", null);
		Map<Integer,W5LookUp> map = wLookUps.get(projectId);
		if(map==null)map = wLookUps.get(FrameworkSetting.devUuid);
		return map.get(lookUpId);
	}
	
	public static void setLookUpMap(Object o, Map m){
		String projectId = getProjectId(o, null);
		wLookUps.put(projectId, m);
	}
	
	public static void setTableMap(Object o, Map m){
		String projectId = getProjectId(o, null);
		wTables.put(projectId, m);
	}
	
	public static void setTableEventMap(Object o, Map m){
		String projectId = getProjectId(o, null);
		wTableEvents.put(projectId, m);
	}
	
	public static List<W5TableEvent> getTableEvents(Object o, int tableId){
		String projectId = getProjectId(o, "15."+tableId);
		Map<Integer, List<W5TableEvent>> m = wTableEvents.get(projectId);
		if(m!=null)return m.get(tableId);
		return null;
	}
	
	public static void setConversionMap(Object o, Map m){
		String projectId = getProjectId(o, null);
		wConversions.put(projectId, m);
	}
	
	public static W5LookUp getLookUp(Object o, int lookUpId, String onNotFoundThrowMsg){
		String projectId = getProjectId(o, "13."+lookUpId);
		if(FrameworkSetting.debug && FrameworkCache.hasQueuedReloadCache(projectId,"13."+lookUpId))
			throw new Error("LookUp Cache not reloaded. Please Reload Cache");
		Map<Integer,W5LookUp> map = wLookUps.get(projectId);
		if(map==null)map = wLookUps.get(0);
		W5LookUp l = map.get(lookUpId);
		if(onNotFoundThrowMsg!=null && l==null)
			throw new IWBException("framework", "LookUp", lookUpId, null, onNotFoundThrowMsg, null);
		return l;
	}
	
	public static int getAppSettingIntValue(Object customizationId, String key){
		if(FrameworkSetting.argMap.containsKey(key))return GenericUtil.uInt(FrameworkSetting.argMap.get(key));
		Map<String, String> map = appSettings.get(getCustomizationId(customizationId));
		if(map==null)map = appSettings.get(0);
		return GenericUtil.uInt(map.get(key));
	}
	public static int getAppSettingIntValue(Object customizationId, String key, int defaultValue){
		if(FrameworkSetting.argMap.containsKey(key))return GenericUtil.uInt(FrameworkSetting.argMap.get(key));
		Map<String, String> map = appSettings.get(getCustomizationId(customizationId));
		if(map==null)map = appSettings.get(0);
		String res = map.get(key);
		return res==null ? defaultValue : GenericUtil.uInt(res);
	}
	public static String getAppSettingStringValue(Object customizationId, String key){
		if(FrameworkSetting.argMap.containsKey(key))return FrameworkSetting.argMap.get(key);
		Map<String, String> map = appSettings.get(getCustomizationId(customizationId));
		if(map==null)map = appSettings.get(0);
		return map.get(key);
	}	
	public static String getAppSettingStringValue(Object customizationId, String key, String defaultValue){
		if(FrameworkSetting.argMap.containsKey(key))return FrameworkSetting.argMap.get(key);
		Map<String, String> map = appSettings.get(getCustomizationId(customizationId));
		if(map==null)map = appSettings.get(0);
		String res = map.get(key);
		return res == null ? defaultValue : res;
	}

/*	public static W5LookUp getLookUp(int customizationId, int lookUpId){
		Map<Integer,W5LookUp> map = wLookUps.get(customizationId);
		if(map==null)map = wLookUps.get(0);
		return map.get(lookUpId);
	}*/
	
	public static boolean roleAccessControl(Map scd,  int action){ 
		return true;
		//0:view, 1:edit, 2:insert, 3:delete, 11:bulkUpdateFlag; 
		//101:fileViewFlag;102:fileUploadFlag;103:commentMakeFlag;104:bulkEmailFlag; 105:gridReportViewFlag;106:showRelatedEmailFlag;107:lookupManageFlag;108:logViewFlag;109:smsEmailTemplateCrudFlag

	}




	synchronized private	static void reorganizeFeedList(List<Log5Feed> lx){
		if(lx.size()>FrameworkSetting.feedMaxDepth){
			List<Log5Feed> lx2 = new ArrayList<Log5Feed>(FrameworkSetting.feedMaxDepth+10);
			for(Log5Feed f:lx)if(f!=null)lx2.add(f);//aradaki null'lari atiyor
			if(lx2.size()>lx.size()/2){
				lx.clear();
				lx.addAll(lx2.subList(lx2.size() - lx.size()/2, lx2.size()));
			} else {
				lx.clear();
				lx.addAll(lx2);
			}
		}
	}
	public static boolean addFeed(Map scd,  Log5Feed feed, boolean publish){
	/*	List<Log5Feed> lx = wFeeds.get((Integer)scd.get("customizationId"));
		if(lx==null){
			lx = new ArrayList<Log5Feed>(FrameworkSetting.feedMaxDepth+10);
			wFeeds.put((Integer)scd.get("customizationId"), lx);
		}
		int maxDerinlik = FrameworkCache.getAppSettingIntValue(scd, "feed_control_depth");
		for(int qi=lx.size()-1;qi>=0 && maxDerinlik>0;maxDerinlik--,qi--){//bir onceki feedlerle iliskisi belirleniyor
			Log5Feed lfeed =lx.get(qi); 
			if(lfeed==null)continue;
			if(lfeed.getTableId()==feed.getTableId() && lfeed.getTablePk()==feed.getTablePk() && lfeed.getFeedTip()==feed.getFeedTip() && feed.get_showFeedTip()==lfeed.get_showFeedTip() &&
					(feed.get_showFeedTip()!=1 || (feed.getDetailTableId()==lfeed.getDetailTableId()))){//edit haricinde birsey veya edit ise ayni tablo uzerinde detay seviyesinde
				lx.set(qi,null);
				feed.set_relatedFeedMap(new HashMap<Integer,Log5Feed>());
				feed.get_relatedFeedMap().put(lfeed.getFeedId(),lfeed);
				if(lfeed.get_relatedFeedMap()!=null)feed.get_relatedFeedMap().putAll(lfeed.get_relatedFeedMap());
				break;
			}
		}
		if(lx.size()>FrameworkSetting.feedMaxDepth)reorganizeFeedList(lx);
		lx.add(feed);
		if(publish && FrameworkSetting.liveSyncRecord){
			UserUtil.syncAfterPostForm((Integer)scd.get("customizationId"), 671, "", (Integer)scd.get("userId"), "-", (short)2);;
		}*/
		return true;
	}
//	final public static Map<DeferredResult<List>, Integer> wLongPollRequests = new HashMap<DeferredResult<List>, Integer>(); //customizationId

	public static boolean requestUrlsControl(String remoteIpAdr){
		Calendar cal = Calendar.getInstance(); // creates calendar
	    cal.setTime(new Date()); 
	    cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
	    Date dt = cal.getTime();	    
			
	    //1 saati dolduran elemanlar temizleniyor 
		for (Object key : wRequestUrls.keySet()) {
			Date dt2 = wRequestUrls.get(key);
			if (dt2.compareTo(dt)>0){
				wRequestUrls.remove(key);
			}			
		}
		
		//server ip listede varmı?
		for (Object key : wRequestUrls.keySet()) {					
			if (key.toString().equals(remoteIpAdr)){
				return false;
			}						
		}		
		return true;
	}

	public static W5WsServer getWsServer(Object o, String serviceName){
		Map<String, W5WsServer> wssMap = wWsServers.get(getProjectId(o, null));
		if(wssMap!=null)for(String sn:wssMap.keySet())if(serviceName.equals(sn))return wssMap.get(sn);
		
		return null;
	}
	public static W5Ws getWsClient(Object o, String serviceName){
		Map<String, W5Ws> wsMap = wWsClients.get(getProjectId(o, null));
		if(wsMap!=null)for(String sn:wsMap.keySet())if(serviceName.equals(sn))return wsMap.get(sn);
		return null;
	}
	public static W5Ws getWsClientById(Object o, int wsId){
		Map<String, W5Ws> wsMap = wWsClients.get(getProjectId(o, null));
		if(wsMap!=null)for(W5Ws ws:wsMap.values())if(ws.getWsId() == wsId)return ws;
		return null;
	}
	
	public static String getServiceNameByMethodId(Object o, int wsMethodId) {
		W5WsMethod wsm = getWsMethod(o, wsMethodId);
		if(wsm!=null){
			return FrameworkCache.getWsClientById(o, wsm.getWsId()).getDsc()+ "." + wsm.getDsc();
		}
		return null;
	}
	public static void setWsClientsMap(String o, Map m){
		wWsClients.put(getProjectId(o, null), m);
	}
	
	public static void setWsServersMap(String o, Map m){
		wWsServers.put(getProjectId(o, null), m);
	}
	public static void addPageCss(Object o, int pageId, String css){
		String p = getProjectId(o,"63."+pageId);
		Map<Integer, String> m = wPageCss.get(p);
		if(m==null){
			m = new HashMap();
			wPageCss.put(p, m);
		}
		m.put(pageId, css);
	}

	public static String getPageCss(Object o, int pageId){
		String p = getProjectId(o,"63."+pageId);
		Map<Integer, String> m = wPageCss.get(p);
		if(m==null)return "";
		String css = m.get(pageId);
		if(css==null)return "";
		return css;
	}

	
	public static String getComponentCss(Object o, int componentId){
		String p = getProjectId(o,"3351."+componentId);
		Map<Integer, W5Component> m = wComponents.get(p);
		if(m==null)return "";
		W5Component css = m.get(componentId);
		if(css==null)return "";
		return css.getCssCode()==null ? "":css.getCssCode();
	}

	public static String getComponentJs(Object o, int componentId){
		String p = getProjectId(o,"3351."+componentId);
		Map<Integer, W5Component> m = wComponents.get(p);
		if(m==null)return "";
		W5Component css = m.get(componentId);
		if(css==null)return "";
		return css.getCode()==null ? "":css.getCode();
	}
/*
	private static RedissonClient redissonClient = null;
	
	public static RedissonClient getRedissonClient(){
		if(redissonClient == null){
			Config config = new Config();
			config.useSingleServer().setAddress(String.format("redis://%s:%s", FrameworkSetting.redisHost, 6379)).setTimeout(100000).setConnectionMinimumIdleSize(10).setConnectionPoolSize(10);
			redissonClient = Redisson.create(config);
		}
		return redissonClient;
	}
*/
	public static Object getGraalFunc(Object o, String pk) {
		String p = getProjectId(o, pk);
		Map<String, Object> m = wGraalFuncs.get(p);
		if(m==null)return null;
		return m.get(pk);
	}

	public static void addGraalFunc(Object o, String pk, Object func) {
		String p = getProjectId(o, pk);
		Map<String, Object> m = wGraalFuncs.get(p);
		if(m==null) {
			m = new HashMap();
			wGraalFuncs.put(p, m);
		}
		m.put(pk, func);		
	}

	public static W5JobSchedule getJob(String projectId, int jobId) {
		Map<Integer, W5JobSchedule> m = wJobs.get(projectId);
		if(m==null)return null;
		return m.get(jobId);
	}

	public static List<W5Table> listVcsTables(String projectId) {
		List<W5Table> l = new ArrayList();
		for(W5Table t:wTables.get(projectId!=null ? projectId: "067e6162-3b6f-4ae2-a221-2470b63dff00").values()) if(t.getVcsFlag()!=0){
			l.add(t);
		}
		return l;
	}

	public static W5ExternalDb getExternalDb(Object o, int externalDbId) {
		String projectId = getProjectId(o, "4658."+externalDbId);
		W5ExternalDb r = FrameworkCache.wExternalDbs.get(projectId).get(externalDbId);
		if(r==null)
			throw new IWBException("framework", "ExternalDb", externalDbId, null, "Wrong ExternalDBId: " + externalDbId, null);
		return r;

	}	

	public static W5Mq getMq(Object o, int mqId) {
		String projectId = getProjectId(o, null);
		return FrameworkCache.wMqs.get(projectId).get(mqId);
	}

	public static Integer findTableIdByName(String tableName, String projectId) {
		Map<Integer, W5Table> tableMap = wTables.get(projectId);
		if(tableMap==null)return null;
		for(W5Table t:tableMap.values())if(t.getDsc().equals(tableName))return t.getTableId();
		return null;
	}
	
	
	
/*	public static W5TsPortlet getTsPortlet(Map<String, Object> customizationId, int porletId) {
		int cid = getCustomizationId(customizationId);
		if(cid!=0){
			W5Customization cus = wCustomizationMap.get(cid);
		}
		if(wTsPortlets.get(cid)==null){
			wTsPortlets.put(cid, new HashMap());
			return null;
		} else
			return wTsPortlets.get(cid).get(porletId);
	}
	public static W5TsMeasurement getTsMeasurement(Map<String, Object> customizationId, int measurementId) {
		int cid = getCustomizationId(customizationId);
		if(cid!=0){
			W5Customization cus = wCustomizationMap.get(cid);
		}
		if(wTsMeasurements.get(cid)==null){
			wTsMeasurements.put(cid, new HashMap());
			return null;
		} else
			return wTsMeasurements.get(cid).get(measurementId);
	}*/

}
