package iwb.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
/*
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;*/

import iwb.exception.IWBException;
import iwb.model.db.Log5Feed;
import iwb.model.db.M5List;
import iwb.model.db.W5Card;
import iwb.model.db.W5Component;
import iwb.model.db.W5Conversion;
import iwb.model.db.W5ConversionCol;
import iwb.model.db.W5CustomGridColumnCondition;
import iwb.model.db.W5CustomGridColumnRenderer;
import iwb.model.db.W5Customization;
import iwb.model.db.W5Exception;
import iwb.model.db.W5ExternalDb;
import iwb.model.db.W5Form;
import iwb.model.db.W5FormCell;
import iwb.model.db.W5FormCellProperty;
import iwb.model.db.W5FormHint;
import iwb.model.db.W5FormModule;
import iwb.model.db.W5FormSmsMail;
import iwb.model.db.W5GlobalFunc;
import iwb.model.db.W5GlobalFuncParam;
import iwb.model.db.W5Grid;
import iwb.model.db.W5GridColumn;
import iwb.model.db.W5JobSchedule;
import iwb.model.db.W5List;
import iwb.model.db.W5LookUp;
import iwb.model.db.W5LookUpDetay;
import iwb.model.db.W5Menu;
import iwb.model.db.W5Mq;
import iwb.model.db.W5ObjectMenuItem;
import iwb.model.db.W5ObjectToolbarItem;
import iwb.model.db.W5Page;
import iwb.model.db.W5PageObject;
import iwb.model.db.W5Project;
import iwb.model.db.W5Query;
import iwb.model.db.W5QueryField;
import iwb.model.db.W5QueryParam;
import iwb.model.db.W5RoleGroup;
import iwb.model.db.W5Table;
import iwb.model.db.W5TableChild;
import iwb.model.db.W5TableEvent;
import iwb.model.db.W5TableField;
import iwb.model.db.W5TableFieldCalculated;
import iwb.model.db.W5TableParam;
import iwb.model.db.W5Workflow;
import iwb.model.db.W5WorkflowStep;
import iwb.model.db.W5Ws;
import iwb.model.db.W5WsMethod;
import iwb.model.db.W5WsMethodParam;
import iwb.model.db.W5WsServer;
import iwb.model.result.W5QueryResult;
import iwb.util.GenericUtil;

public class FrameworkCache {

	final private static Map<String, Map<Integer,W5LookUp>> wLookUps = new HashMap<String, Map<Integer,W5LookUp>>(); //customizationId
	final public static Map<String, Map<Integer, W5Query>> wQueries = new HashMap<String, Map<Integer, W5Query>>();
	final private static Map<String, Map<Integer, W5Conversion>> wConversions = new HashMap<String, Map<Integer, W5Conversion>>();
	final private static Map<String, Map<Integer, W5Grid>> wGrids = new HashMap<String, Map<Integer, W5Grid>>();
	final private static Map<String, Map<Integer, W5List>> wListViews = new HashMap<String, Map<Integer, W5List>>();
	final private static Map<String, Map<Integer, M5List>> mListViews = new HashMap<String, Map<Integer, M5List>>();
	final private static Map<String, Map<Integer, W5Card>> wCards = new HashMap<String, Map<Integer, W5Card>>();
	final private static Map<String, Map<Integer, W5Form>> wForms = new HashMap<String, Map<Integer, W5Form>>();
	final private static Map<String, Map<Integer, W5GlobalFunc>> wGlobalFuncs = new HashMap<String, Map<Integer, W5GlobalFunc>>();
	final public static Map<String, Map<Integer, W5Page>> wTemplates = new HashMap<String, Map<Integer, W5Page>>();
	final public static Map<String, Map<String, W5Page>> wTemplates2 = new HashMap<String, Map<String, W5Page>>();
	final private static Map<String, Map<Integer, W5Component>> wComponents = new HashMap<String, Map<Integer, W5Component>>();

	final private static Map<String, Map<Integer, W5Table>> wTables = new HashMap<String, Map<Integer, W5Table>>();
	final private static Map<String, Map<Integer, List<W5TableEvent>>> wTableEvents = new HashMap<String, Map<Integer,List<W5TableEvent>>>();	
	final private static Map<String, List<W5RoleGroup>> wRoleGroups = new HashMap<String, List<W5RoleGroup>>();

	
	final private static Map<String, Map<String, String>> wPageDynResource = new HashMap<String, Map<String, String>>();
	final private static Map<String, Map<String, Object>> wGraalFuncs = new HashMap<String, Map<String, Object>>();

	final public static Map<String, Map<Integer, W5Workflow>> wWorkflows = new HashMap<String,Map<Integer, W5Workflow>>();
	final private static List<W5Customization> wCustomization = new ArrayList<W5Customization>();
	final private static Map<String, W5Project> wProjects = new HashMap<String, W5Project>(); //projectUuid

	final private static Map<String, Map<Integer, List>> wMenus = new HashMap<String, Map<Integer, List>>();


	final public static Map<String, Map<Integer, W5ExternalDb>> wExternalDbs = new HashMap<String, Map<Integer, W5ExternalDb>>();
	final public static Map<String, Map<Integer, W5Mq>> wMqs = new HashMap<String, Map<Integer, W5Mq>>();

	
	final public static Map<String, List<W5Exception>> wExceptions = new HashMap<String, List<W5Exception>>();
	final public static Map<Integer, Map<String, String>> appSettings= new HashMap<Integer, Map<String, String>>();
	final public static List<String> publishAppSettings= new ArrayList<String>();
//	final public static Map<String, List<Integer>> publishLookUps= new HashMap<String, List<Integer>>();
	final public static Map<Integer, Map<Integer, String>> wRoles = new HashMap<Integer, Map<Integer, String>>();
	final public static Map<Integer, Set<Integer>> xRoleACL = new HashMap<Integer, Set<Integer>>();//RoleId, ACLType
	final public static Map<String, List<Log5Feed>> wFeeds = new HashMap<String, List<Log5Feed>>();
	final public static Map<String, Map<Integer, W5JobSchedule>> wJobs= new HashMap<String, Map<Integer, W5JobSchedule>>();
	final private static Map<String, Map<String, Long>> wQueuedReloadCache = new HashMap<String, Map<String, Long>>();
//	final public static HashMap<String , List<Object> > lastUserAction=new HashMap<String, List<Object>>();
	final public static Map<Integer, W5Customization> wCustomizationMap = new HashMap<Integer, W5Customization>();
	final private static Map<String, Map<String, W5Ws>> wWsClients = new HashMap<String, Map<String, W5Ws>>(); //wsId
	final private static Map<String, Map<Integer, W5WsMethod>> wWsMethods = new HashMap<String, Map<Integer, W5WsMethod>>(); //wsId
	final private static Set<String> wDevEntityKeys = new HashSet<String>();

	public static List<W5QueryField> cachedOnlineQueryFields = null;
	
	
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
		wq = wTemplates2.get(projectId); if(wq!=null)wq.clear();
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
		if(false && FrameworkSetting.debug && hasQueuedReloadCache(projectId,"15."+tableId)){
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
		int dataViewId = d.getCardId();
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
			return null;
		} else
			return wComponents.get(projectId).get(componentId);
	}

	public static void setComponent(Object o, W5Component component) {
		String projectId = getProjectId(o, "3351."+component.getComponentId());
		if(!wComponents.containsKey(projectId)){
			wComponents.put(projectId, new HashMap());
		}
		wComponents.get(projectId).put(component.getComponentId(), component);
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
		String projectId = getProjectId(o, "389."+w.getWorkflowId());
		if(!wWorkflows.containsKey(projectId)){
			wWorkflows.put(projectId, new HashMap());
		} else
		wWorkflows.get(projectId).put(w.getWorkflowId(), w);
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
		int funcId = func.getGlobalFuncId();
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
	
	
	public static W5Page getPageByName(Object o, String pageName){
		String projectId = getProjectId(o, "63."+pageName);
		if(wTemplates2.get(projectId)==null){
			wTemplates2.put(projectId, new HashMap());
			return null;
		} else
			return wTemplates2.get(projectId).get(pageName);
	}
	
	
	public static void addPage(Object o, W5Page page){
		int pageId = page.getPageId();
		String projectId = getProjectId(o, "63."+pageId);
		//addX((Map)wTemplates, projectId, pageId, page);
		if(wTemplates.get(projectId)==null){
			wTemplates.put(projectId, new HashMap());
		}
		wTemplates.get(projectId).put(pageId, page);
		if(wTemplates2.get(projectId)==null){
			wTemplates2.put(projectId, new HashMap());
		}
		wTemplates2.get(projectId).put(page.getDsc(), page);
	}
	
	public static W5LookUp getLookUp(Object o, int lookUpId){
		String projectId = getProjectId(o, "13."+lookUpId);
		if(FrameworkSetting.debug && hasQueuedReloadCache(projectId,"13."+lookUpId))
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
		if(FrameworkSetting.debug && hasQueuedReloadCache(projectId,"13."+lookUpId))
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
		if(action==0)return true;
		if(FrameworkSetting.projectId==null || FrameworkSetting.projectId.equals("1"))return true;
		if(scd==null)return false;
		int roleId = GenericUtil.uInt(scd.get("roleId"));
		if(roleId==0)return true;
		Set<Integer> ss = xRoleACL.get(roleId);
		if(ss==null)return false;
		return ss.contains(action);
		//0:view, 1:edit, 2:insert, 3:delete, 11:bulkUpdateFlag; 
		//101:fileViewFlag;102:fileUploadFlag;103:commentMakeFlag;104:bulkEmailFlag; 105:gridReportViewFlag;106:showRelatedEmailFlag;107:lookupManageFlag;108:logViewFlag;109:smsEmailTemplateCrudFlag

	}



	public static boolean addFeed(Map scd,  Log5Feed feed, boolean publish){
	/*	List<Log5Feed> lx = wFeeds.get((Integer)scd.get("customizationId"));
		if(lx==null){
			lx = new ArrayList<Log5Feed>(FrameworkSetting.feedMaxDepth+10);
			wFeeds.put((Integer)scd.get("customizationId"), lx);
		}
		int maxDerinlik = getAppSettingIntValue(scd, "feed_control_depth");
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
			return getWsClientById(o, wsm.getWsId()).getDsc()+ "." + wsm.getDsc();
		}
		return null;
	}
	public static void setWsClientsMap(String o, Map m){
		wWsClients.put(getProjectId(o, null), m);
	}
	
	public static void setWsServersMap(String o, Map m){
		wWsServers.put(getProjectId(o, null), m);
	}
	public static void addPageResource(Object o, String key, String res){
		String p = getProjectId(o, null);
		Map<String, String> m = wPageDynResource.get(p);
		if(m==null){
			m = new HashMap();
			wPageDynResource.put(p, m);
		}
		m.put(key, res);
	}

	public static String getPageResource(Object o, String key){
		String p = getProjectId(o,null);
		Map<String, String> m = wPageDynResource.get(p);
		if(m==null)return "";
		String res = m.get(key);
		if(res==null)return "";
		return res;
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
	

	public static String getComponentName(Object o, int componentId){
		String p = getProjectId(o,"3351."+componentId);
		Map<Integer, W5Component> m = wComponents.get(p);
		if(m==null)return "";
		W5Component c = m.get(componentId);
		if(c==null)return "";
		return c.getDsc();
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
		for(W5Table t:wTables.get(projectId!=null ? projectId: FrameworkSetting.devUuid).values()) if(t.getVcsFlag()!=0){
			l.add(t);
		}
		return l;
	}

	public static W5ExternalDb getExternalDb(Object o, int externalDbId) {
		String projectId = getProjectId(o, "4658."+externalDbId);
		W5ExternalDb r = wExternalDbs.get(projectId).get(externalDbId);
		if(r==null)
			throw new IWBException("framework", "ExternalDb", externalDbId, null, "Wrong ExternalDBId: " + externalDbId, null);
		return r;

	}	

	public static W5Mq getMq(Object o, int mqId) {
		String projectId = getProjectId(o, null);
		return wMqs.get(projectId).get(mqId);
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
	

	public static void addTables2Cache(String projectId, List<W5Table> tables,
			List<W5TableField> tableFields,
			List<W5TableParam> tableParams,
			List<W5TableEvent> tableEvents,
			List<W5TableFieldCalculated> tableFieldCalculateds,
			List<W5TableChild> tableChilds
			) {

		// if (PromisCache.wTables.get(customizationId)!=null)
		// PromisCache.wTables.get(customizationId).clear();
		Map<Integer, W5Table> tableMap =  new HashMap<Integer, W5Table>();
		
		if(tables!=null)for (W5Table t : tables) {
			// t.set_cachedObjectMap(new HashMap());
			t.set_tableFieldList(null);
			t.set_tableFieldMap(null);
			t.set_tableParamList(null);
			t.set_tableChildList(null);
			t.set_tableParamList(null);
			tableMap.put(t.getTableId(), t);
		}
		W5Table t = null;
		if(tableFields!=null)for (W5TableField tf : tableFields) {
			if (t == null || tf.getTableId() != t.getTableId())
				t = tableMap.get(tf.getTableId()); // tableMap.get(tf.getTableId());
			if (t != null) {
				if (t.get_tableFieldList() == null) {
					t.set_tableFieldList(new ArrayList<W5TableField>());
					t.set_tableFieldMap(new HashMap<Integer, W5TableField>());
					/*
					 * t.set_tableParamList(tableParamListMap .get
					 * (tf.getTableId())); t.set_tableChildList(tableChildListMap
					 * .get (tf.getTableId())); t.set_tableParentList(
					 * tableParentListMap. get(tf.getTableId()));
					 */
				}
				t.get_tableFieldList().add(tf);
				t.get_tableFieldMap().put(tf.getTableFieldId(), tf);
			}
		}

		// Map<Integer, List<W5TableParam>> tplMap = new HashMap<Integer,
		// List<W5TableParam>>();
		int lastTableId = -1;
		List<W5TableParam> x = null;
		if(tableParams!=null)for (W5TableParam tp : tableParams) {
			if (lastTableId != tp.getTableId()) {
				if (x != null) {
					W5Table tx = tableMap.get(lastTableId);
					if (tx != null)
						tx.set_tableParamList(x);
					// tableParamListMap.put(lastTableId, x);
				}
				x = new ArrayList();
				lastTableId = tp.getTableId();
			}
			x.add(tp);
		}
		if (x != null) {
			W5Table tx = tableMap.get(lastTableId);
			if (tx != null)
				tx.set_tableParamList(x);
		}

		// Map<Integer, List<W5TableChild>> tcMap = new HashMap<Integer,
		// List<W5TableChild>>();//copy
		// Map<Integer, List<W5TableChild>> tpMap = new HashMap<Integer,
		// List<W5TableChild>>();//watch,feed
		lastTableId = -1;
		List<W5TableChild> ltc = null, tpx = null;
		if(tableChilds!=null)for (W5TableChild tc : tableChilds) {
			W5Table tx = tableMap.get(tc.getTableId());
			if (tx == null)
				continue;
			W5Table pr = tableMap.get(tc.getRelatedTableId());
			if (pr == null)
				continue;

			ltc = tx.get_tableChildList();
			if (ltc == null) {
				ltc = new ArrayList<W5TableChild>();
				tx.set_tableChildList(ltc);
			}

			ltc.add(tc);

			tpx = pr.get_tableParentList();
			if (tpx == null) {
				tpx = new ArrayList<W5TableChild>();
				pr.set_tableParentList(tpx);
			}
			tpx.add(tc);

		}

		setTableMap(projectId, tableMap);

		Map<Integer, List<W5TableEvent>> tableEventMap = new HashMap<Integer, List<W5TableEvent>>();
		if(tableEvents!=null)for (W5TableEvent r : tableEvents) {
			List<W5TableEvent> l2 = tableEventMap.get(r.getTableId());
			if (l2 == null) {
				l2 = new ArrayList();
				tableEventMap.put(r.getTableId(), l2);
			}
			l2.add(r);
		}
		setTableEventMap(projectId, tableEventMap);
	}

	public static void addLookUps2Cache(String projectId, List<W5LookUp> lookUps, List<W5LookUpDetay> lookUpDetays) {
		Map<Integer, W5LookUp> lookUpMap = new HashMap<Integer, W5LookUp>();


		/*
		 * try { lookUpMap = (Map) ((Map)
		 * (redisGlobalMap.get(projectId))).get("lookUp");//
		 * getRedissonClient().getMap(String.format(
		 * "icb-cache2:%s:lookUp", // projectId)); } catch (Exception e) { throw new
		 * IWBException("framework", "Redis.LookUps", 0, null,
		 * "Loading LookUps from Redis", e); }
		 */
		if(lookUps!=null)for (W5LookUp lookUp : lookUps) {
			lookUp.set_detayList(new ArrayList());
			lookUp.set_detayMap(new HashMap());
			lookUpMap.put(lookUp.getLookUpId(), lookUp);
		}
	
		if(lookUpDetays!=null)for (W5LookUpDetay lookUpDetay : lookUpDetays) {
			W5LookUp lookUp = lookUpMap.get(lookUpDetay.getLookUpId());
			if (lookUp == null)
				continue;
			lookUp.get_detayList().add(lookUpDetay);
			lookUp.get_detayMap().put(lookUpDetay.getVal(), lookUpDetay);
		}
		setLookUpMap(projectId, lookUpMap);
	}

	public static void addWss2Cache(String projectId, List<W5Ws> wss, List<W5WsMethod> wsMethods, List<W5WsMethodParam> wsMethodParams) {
		Map<String, W5Ws> wsMap = new HashMap();
		Map<Integer, W5Ws> wsMapById = new HashMap();
		if(wss!=null)for (W5Ws w : wss) {
			wsMapById.put(w.getWsId(), w);
			wsMap.put(w.getDsc(), w);
		}
		setWsClientsMap(projectId, wsMap);

		Map<Integer, W5WsMethod> methodMap = new HashMap();
		if(wsMethods!=null)for (W5WsMethod m : wsMethods) {
			W5Ws c = wsMapById.get(m.getWsId());
			if (c != null) {
				methodMap.put(m.getWsMethodId(), m);
				m.set_ws(c);
				if (c.get_methods() == null)
					c.set_methods(new ArrayList());
				addWsMethod(projectId, m);
				c.get_methods().add(m);
				m.set_params(new ArrayList());
				m.set_paramMap(new HashMap());
			}
		}

		if(wsMethodParams!=null)for (W5WsMethodParam mp : wsMethodParams) {
			W5WsMethod c = methodMap.get(mp.getWsMethodId());
			if (c != null) {
				c.get_params().add(mp);
				c.get_paramMap().put(mp.getWsMethodParamId(), mp);
			}
		}

	}

	public static void addFuncs2Cache(String projectId, List<W5GlobalFunc> funcs, List<W5GlobalFuncParam> funcParams) {
		
		Map<Integer, W5GlobalFunc> mm = new HashMap();
		
		if(funcs!=null)for(W5GlobalFunc m:funcs) {
			mm.put(m.getGlobalFuncId(), m);
			m.set_dbFuncParamList(new ArrayList());
		}
		if(funcParams!=null)for(W5GlobalFuncParam d:funcParams) {
			W5GlobalFunc m = mm.get(d.getGlobalFuncId());
			if(m!=null) {
				m.get_dbFuncParamList().add(d);
			}			
		}		
		wGlobalFuncs.put(projectId, mm);		
	}

	public static void addQueries2Cache(String projectId, List<W5Query> queries, List<W5QueryField> queryFields,
			List<W5QueryParam> queryParams) {
		Map<Integer, W5Query> mm = new HashMap();
		
		if(queries!=null)for(W5Query m:queries) {
			mm.put(m.getQueryId(), m);
			m.set_queryFields(new ArrayList());
			m.set_queryParams(new ArrayList());
		}
		
		if(queryFields!=null)for(W5QueryField d:queryFields) {
			W5Query m = mm.get(d.getQueryId());
			if(m!=null) {
				m.get_queryFields().add(d);
			}
			if (d.getPostProcessType() == 31 && (d.getFieldType() == 3 || d.getFieldType() == 4)) {
				if (m.get_aggQueryFields() == null)
					m.set_aggQueryFields(new ArrayList());
				m.get_aggQueryFields().add(d);
			}
		}
		
		if(queryParams!=null)for(W5QueryParam d:queryParams) {
			W5Query m = mm.get(d.getQueryId());
			if(m!=null) {
				m.get_queryParams().add(d);
			}			
		}
		
		if(queries!=null)for(W5Query query:queries)if (query.getShowParentRecordFlag() != 0)
			for (W5QueryField field : query.get_queryFields()) {
				if (field.getDsc().equals("table_id"))
					query.set_tableIdTabOrder(field.getTabOrder());
				if (field.getDsc().equals("table_pk"))
					query.set_tablePkTabOrder(field.getTabOrder());

			}
		
		wQueries.put(projectId, mm);
		
	}

	public static void addForms2Cache(String projectId, List<W5Form> forms, List<W5FormCell> formCells,
			List<W5FormModule> formModules, List<W5FormCellProperty> formCellProperties,
			List<W5FormSmsMail> formSmsMails, List<W5FormHint> formHints, List<W5ObjectToolbarItem> toolbarItems) {
		Map<Integer, W5Form> mm = new HashMap();
		
		if(forms!=null)for(W5Form m:forms) {
			mm.put(m.getFormId(), m);
			m.set_formCells(new ArrayList());
			m.set_moduleList(new ArrayList());
			m.set_toolbarItemList(new ArrayList());
			m.set_formHintList(new ArrayList());
		}
		Map<Integer, W5FormCell> formCellMap = new HashMap();
		if(formCells!=null)for(W5FormCell d:formCells) {
			W5Form m = mm.get(d.getFormId());
			if(m!=null) {
				m.get_formCells().add(d);
				formCellMap.put(d.getFormCellId(), d);
			}
		}
		
		if(formCellProperties!=null)for(W5FormCellProperty d:formCellProperties) {
			W5FormCell m = formCellMap.get(d.getFormCellId());
			if(m!=null) {
				if (m.get_formCellPropertyList() == null)
					m.set_formCellPropertyList(new ArrayList());
				m.get_formCellPropertyList().add(d);
			}			
		}
		
		if(formModules!=null)for(W5FormModule d:formModules) {
			W5Form m = mm.get(d.getFormId());
			if(m!=null && m.getRenderType() != 0) {
				m.get_moduleList().add(d);
			}			
		}

		if(formHints!=null)for(W5FormHint d:formHints) {
			W5Form m = mm.get(d.getFormId());
			if(m!=null) {
				m.get_formHintList().add(d);
			}			
		}
		if(toolbarItems!=null)for(W5ObjectToolbarItem d:toolbarItems) if(d.getObjectType()==40){
			W5Form m = mm.get(d.getObjectId());
			if(m!=null) {
				m.get_toolbarItemList().add(d);
			}			
		}
		
		if(forms!=null)for(W5Form form:forms) { //final makeup

			if (form.getObjectType() != 1 && form.getRenderTemplateId() > 0) { // if not grid(seachForm)
				form.set_renderTemplate(getPage(projectId, form.getRenderTemplateId()));
			}
			Map<Short, W5Workflow> mam = null;
			W5Table mt = null;
			switch (form.getObjectType()) {
			case 6: // conversion icin
				W5Conversion c = getConversion(projectId, form.getObjectId());
				if(c!=null && c.get_conversionColMap()!=null)for (W5FormCell fc : form.get_formCells())
					if (fc.getObjectDetailId() != 0) {
						fc.set_sourceObjectDetail(c.get_conversionColMap().get(fc.getObjectDetailId()));
					}

				break;
			case 2: // table icin ise
				// f.setTable((W5Table)loadObject(W5Table.class,
				// f.getForm().getObjectId()));
				W5Table t = getTable(projectId, form.getObjectId());
				if(t==null) {
					break;
				}
				// f.getForm().set_sourceTable(t);
				Map<String, W5TableField> fieldMap1 = new HashMap();
				for (W5TableField tf : (List<W5TableField>) t.get_tableFieldList()) {
					fieldMap1.put(tf.getDsc(), tf);
				}
				for (W5FormCell fc : form.get_formCells())
					if (fc.getObjectDetailId() != 0) {
						fc.set_sourceObjectDetail(t.get_tableFieldMap().get(fc.getObjectDetailId()));
					}
				if ((fieldMap1.get("INSERT_USER_ID") != null || fieldMap1.get("insert_user_id") != null)
						&& (fieldMap1.get("VERSION_USER_ID") != null || fieldMap1.get("version_user_id") != null)) {
					form.set_versioningFlag(true);
				}
				if (FrameworkSetting.sms || FrameworkSetting.mail) {
					form.set_formSmsMailList(new ArrayList());
					if(formSmsMails!=null)for(W5FormSmsMail fsm:formSmsMails)if(fsm.getFormId()==form.getFormId())
						form.get_formSmsMailList().add(fsm);
					if (GenericUtil.isEmpty(form.get_formSmsMailList()))
						form.set_formSmsMailList(null);
					else {
						form.set_formSmsMailMap(new HashMap());
						for (W5FormSmsMail fsm : form.get_formSmsMailList()) {
							form.get_formSmsMailMap().put(fsm.getFormSmsMailId(), fsm);
						}
					}
				}

				form.set_conversionList(listConversion4Form(projectId, form.getFormId(), form.getObjectId()));

				break;
			case 1: // 
				W5Grid grid = getGrid(projectId, form.getObjectId());
				if(grid == null) {
					break;
				}
				W5Query query = getQuery(projectId, grid.getQueryId());
				int queryId = grid.getQueryId();
				int sourceObjectId = query.getSourceObjectId();
				if (sourceObjectId > 0)
					mt = FrameworkCache.getTable(projectId, sourceObjectId); // f.getForm().set_sourceTable()
				form.set_sourceQuery(query);
				Map<Integer, W5QueryParam> fieldMap2 = new HashMap();
				for (W5QueryParam tf : form.get_sourceQuery().get_queryParams()) {
					fieldMap2.put(tf.getQueryParamId(), tf);
				}
				for (W5FormCell fc : form.get_formCells())
					if (fc.getObjectDetailId() != 0) {
						if (fc.getObjectDetailId() > 0)
							fc.set_sourceObjectDetail(fieldMap2.get(fc.getObjectDetailId())); // queryField'dan
						else if (mt != null) {
							fc.set_sourceObjectDetail(mt.get_tableFieldMap().get(-fc.getObjectDetailId()));
						}
					}
				// onay mekanizmasi icin
				if (mt != null)
					mam = mt.get_approvalMap();

				break;
			case 3:
			case 4: // db func
				W5GlobalFunc dbf = getGlobalFunc(projectId,  form.getObjectId());
				if(dbf == null) {
					break;
				}
				Map<Integer, W5GlobalFuncParam> fieldMap3 = new HashMap();
				for (W5GlobalFuncParam tf : dbf.get_dbFuncParamList()) {
					fieldMap3.put(tf.getGlobalFuncParamId(), tf);
				}
				for (W5FormCell fc : form.get_formCells())
					if (fc.getObjectDetailId() != 0) {
						fc.set_sourceObjectDetail(fieldMap3.get(fc.getObjectDetailId()));
					}
			}


			if (mam != null && !mam.isEmpty()) { // map of ApprovalManagement
				int maxFirstColumnTabOrder = 0;
				for (W5FormCell c : form.get_formCells())
					if (c.getFormModuleId() == 0 && c.getTabOrder() < 1000) {
						maxFirstColumnTabOrder++;
					}
				for (short actionTip : mam.keySet()) {
					W5FormCell approvalCell = new W5FormCell();
					approvalCell.setTabOrder((short) (-actionTip));
					approvalCell.setDsc("_approval_step_ids" + actionTip);
					approvalCell.setControlType((short) 15); // low-combo query
					approvalCell.setLookupQueryId(606); // approval steps
					approvalCell.setLookupIncludedParams("xapproval_id=" + mam.get(actionTip).getWorkflowId());
					approvalCell.setControlWidth((short) 250);
					approvalCell.setLocaleMsgKey("approval_status"); // mam.get(actionTip).getDsc()
					approvalCell.setInitialSourceType((short) 10); // approvalStates
					// approvalCell.setInitialValue(""+mam.get(actionTip).getApprovalId());//approvalId
					approvalCell.setActiveFlag((short) 1);
					form.get_formCells().add(0, /* maxFirstColumnTabOrder, */ approvalCell);
				}
			}

		}

		wForms.put(projectId, mm);
		
	}

	public static void addGrids2Cache(String projectId, List<W5Grid> grids, List<W5GridColumn> gridColumns,
			List<W5CustomGridColumnCondition> gridColumnCustomConditions,
			List<W5CustomGridColumnRenderer> gridColumnCustomRenderers, 
			List<W5ObjectToolbarItem> toolbarItems, 
			List<W5ObjectMenuItem> menuItems,
			List<W5FormCell> formCells
			) {
		Map<Integer, W5Grid> mm = new HashMap();
		
		if(grids!=null)for(W5Grid m:grids) {
			mm.put(m.getGridId(), m);
			m.set_gridColumnList(new ArrayList());
			m.set_toolbarItemList(new ArrayList());
			m.set_menuItemList(new ArrayList());
		}
		
		if(gridColumns!=null)for(W5GridColumn d:gridColumns) {
			W5Grid m = mm.get(d.getGridId());
			if(m!=null) {
				m.get_gridColumnList().add(d);
			}
		}
		if(menuItems!=null)for(W5ObjectMenuItem d:menuItems) if(d.getObjectType()==5){
			W5Grid m = mm.get(d.getObjectId());
			if(m!=null) {
				m.get_menuItemList().add(d);
			}			
		}
		if(toolbarItems!=null)for(W5ObjectToolbarItem d:toolbarItems) if(d.getObjectType()==5){
			W5Grid m = mm.get(d.getObjectId());
			if(m!=null) {
				m.get_toolbarItemList().add(d);
			}			
		}
		
		if(gridColumnCustomRenderers!=null)for(W5CustomGridColumnRenderer d:gridColumnCustomRenderers){
			W5Grid m = mm.get(d.getGridId());
			if(m!=null && m.getRowColorFxType()==1 && m.getRowColorFxQueryFieldId()!=0) {
				if(m.get_listCustomGridColumnRenderer()==null)m.set_listCustomGridColumnRenderer(new ArrayList());
				m.get_listCustomGridColumnRenderer().add(d);
			}			
		}
		
		
		if(gridColumnCustomConditions!=null)for(W5CustomGridColumnCondition d:gridColumnCustomConditions){
			W5Grid m = mm.get(d.getGridId());
			if(m!=null && (m.getRowColorFxType()==3 || (m.getRowColorFxType()==2 && m.getRowColorFxQueryFieldId()!=0))) {
				if(m.get_listCustomGridColumnCondition()==null)m.set_listCustomGridColumnCondition(new ArrayList());
				m.get_listCustomGridColumnCondition().add(d);
			}			
		}
		Map<Integer, W5FormCell> formCellMap = new HashMap();
		if(formCells!=null)for(W5FormCell d:formCells) {
			formCellMap.put(d.getFormCellId(), d);
		}
		
		if(grids!=null)for(W5Grid grid:grids) {
	
	
			W5Query query = getQuery(projectId, grid.getQueryId());
	
			if(query==null)continue;
			
			grid.set_query(query);
	
			grid.set_viewTable(FrameworkCache.getTable(projectId, query.getSourceObjectId()));
	
			Map<Integer, W5QueryField> fieldMap = new HashMap<Integer, W5QueryField>();
			Map<String, W5QueryField> fieldMapDsc = new HashMap<String, W5QueryField>();
			for (W5QueryField field : query.get_queryFields()) {
				fieldMap.put(field.getQueryFieldId(), field);
				fieldMapDsc.put(field.getDsc(), field);
			}
			if (grid.get_viewTable() != null) { // extended fields
				int qi = 0;
				for (qi = 0; qi < grid.get_viewTable().get_tableFieldList().size(); qi++) {
					W5TableField tf = grid.get_viewTable().get_tableFieldList().get(qi);
				}
			}
			grid.set_queryFieldMap(fieldMap);
	
			grid.set_queryFieldMapDsc(fieldMapDsc);
			grid.set_autoExpandField(fieldMap.get(grid.getAutoExpandFieldId()));
			grid.set_pkQueryField(fieldMap.get(grid.getPkQueryFieldId()));
			if (grid.get_pkQueryField() == null) {
				if (false && FrameworkSetting.debug)
					throw new IWBException("framework", "Grid", grid.getGridId(), null, "Grid PK Missing", null);
				if(GenericUtil.isEmpty(query.get_queryFields()))continue;
				grid.set_pkQueryField(query.get_queryFields().get(0));
			}
			grid.set_groupingField(fieldMap.get(grid.getGroupingFieldId()));
			grid.set_fxRowField(fieldMap.get(grid.getRowColorFxQueryFieldId()));
	
			int formCellCounter = 1;

			for (W5GridColumn column : grid.get_gridColumnList()) {
				column.set_queryField(fieldMap.get(column.getQueryFieldId()));
				if (column.getFormCellId() > 0) { // form_cell
					W5FormCell cell = formCellMap.get(column.getFormCellId());
					if (cell != null) {
						column.set_formCell(cell);
					}
				} else if (column.getFormCellId() < 0) { // control
					W5FormCell cell = new W5FormCell(-formCellCounter++);
					cell.setControlType((short) -column.getFormCellId());
					cell.setDsc(column.get_queryField().getDsc());
					cell.setFormCellId(column.getQueryFieldId());
					column.set_formCell(cell);
				}
			}
	
			if (grid.get_toolbarItemList() != null)
				for (W5ObjectToolbarItem c : grid.get_toolbarItemList())
					switch (c.getControlType()) { // TODO:toolbar icine bisey
												// konulacaksa
					case 10:
					case 7:
					case 15:
					case 9:
						break;
					case 14:
					case 8:
					case 6:
						break;
					}
			// if(grid.getSelectionModeTip()==4)
	
			if (grid.getDefaultCrudFormId() != 0) {
				W5Form defaultCrudForm = getForm(projectId, grid.getDefaultCrudFormId());
				grid.set_defaultCrudForm(defaultCrudForm);
	
				if (defaultCrudForm != null && defaultCrudForm.getObjectType() == 2) {
					// defaultCrudForm.set_sourceTable(PromisCache.getTable(customizationId,
					// defaultCrudForm.getObjectId()));
					W5Table t = FrameworkCache.getTable(projectId, defaultCrudForm.getObjectId()); // PromisCache.getTable(f.getScd(),
																									// f.getForm().getObjectId())
					grid.set_crudTable(t);
	
					List<W5FormSmsMail> xcrudFormSmsList = defaultCrudForm.get_formSmsMailList();
					if (xcrudFormSmsList != null) {
						List<W5FormSmsMail> crudFormSmsList = new ArrayList();
						for (W5FormSmsMail x : xcrudFormSmsList)
							if (GenericUtil.hasPartInside2(x.getActionTypes(), 0)) {
								crudFormSmsList.add(x);
							}
						grid.set_crudFormSmsMailList(crudFormSmsList);
					}
	
					List<W5Conversion> xcrudFormConversionList = defaultCrudForm.get_conversionList();
					if (xcrudFormConversionList != null) {
						List<W5Conversion> crudFormConversionList = new ArrayList();
						for (W5Conversion x : xcrudFormConversionList)
							if (GenericUtil.hasPartInside2(x.getActionTypes(), 0)) {
								crudFormConversionList.add(x);
							}
						grid.set_crudFormConversionList(crudFormConversionList);
					}
	
					if (GenericUtil.isEmpty(grid.get_crudFormSmsMailList()))
						grid.set_crudFormSmsMailList(null);
	
					// Gridle ilgili onay mekanizması ataması
//					organizeListPostProcessQueryFields(gr.getScd(), t, grid); TODO
				}
			}
		}
		
		wGrids.put(projectId, mm);
	}

	public static void addWorkflows2Cache(String projectId, List<W5Workflow> workflows,
			List<W5WorkflowStep> workflowSteps) {
		Map<Integer, W5Workflow> mm = new HashMap();
		
		if(workflows!=null)for(W5Workflow m:workflows) {
			mm.put(m.getWorkflowId(), m);
			m.set_approvalStepList(new ArrayList());
			m.set_approvalStepMap(new HashMap());
		}
		
		if(workflowSteps!=null)for(W5WorkflowStep d:workflowSteps) {
			W5Workflow m = mm.get(d.getWorkflowId());
			if(m!=null) {
				m.get_approvalStepList().add(d);
				m.get_approvalStepMap().put(d.getApprovalStepId(), d);
			}
		}
		
		wWorkflows.put(projectId, mm);
		
	}

	public static void addConversions2Cache(String projectId, List<W5Conversion> conversions,
			List<W5ConversionCol> conversionCols) {
		Map<Integer, W5Conversion> mm = new HashMap();
		
		if(conversions!=null)for(W5Conversion m:conversions) {
			mm.put(m.getConversionId(), m);
			m.set_conversionColList(new ArrayList());
			m.set_conversionColMap(new HashMap());
		}
		
		if(conversionCols!=null)for(W5ConversionCol d:conversionCols) {
			W5Conversion m = mm.get(d.getConversionId());
			if(m!=null) {
				m.get_conversionColList().add(d);
				m.get_conversionColMap().put(d.getConversionColId(), d);
			}
		}
		
		wConversions.put(projectId, mm);
		
	}

	public static void addPages2Cache(String projectId, List<W5Page> pages, List<W5PageObject> pageObjects) {
		Map<Integer, W5Page> mm = new HashMap();
		
		if(pages!=null)for(W5Page m:pages) {
			mm.put(m.getPageId(), m);
			m.set_pageObjectList(new ArrayList());
		}
		
		if(pageObjects!=null)for(W5PageObject d:pageObjects) {
			W5Page m = mm.get(d.getPageId());
			if(m!=null) {
				m.get_pageObjectList().add(d);
			}
		}
		
		wTemplates.put(projectId, mm);
		
	}

	public static void addCards2Cache(String projectId, List<W5Card> cards, List<W5ObjectToolbarItem> toolbarItems, List<W5ObjectMenuItem> menuItems) {
		Map<Integer, W5Card> mm = new HashMap();
		
		if(cards!=null)for(W5Card m:cards) {
			m.set_query(getQuery(projectId, m.getQueryId()));
			if(m.get_query()==null)continue;
			mm.put(m.getCardId(), m);
			m.set_menuItemList(new ArrayList());
			m.set_toolbarItemList(new ArrayList());
			
			m.set_crudTable(FrameworkCache.getTable(projectId, m.get_query().getSourceObjectId()));

			Map<Integer, W5QueryField> fieldMap = new HashMap<Integer, W5QueryField>();
			Map<String, W5QueryField> fieldMapDsc = new HashMap<String, W5QueryField>();
			for (W5QueryField field : m.get_query().get_queryFields()) {
				fieldMap.put(field.getQueryFieldId(), field);
				fieldMapDsc.put(field.getDsc(), field);
			}

			m.set_queryFieldMap(fieldMap);

			m.set_queryFieldMapDsc(fieldMapDsc);
			m.set_pkQueryField(fieldMap.get(m.getPkQueryFieldId()));
			
			if (m.getDefaultCrudFormId() != 0) {
				W5Form defaultCrudForm = getForm(projectId, m.getDefaultCrudFormId());

				if (defaultCrudForm != null) {
					// defaultCrudForm.set_sourceTable(PromisCache.getTable(customizationId,
					// defaultCrudForm.getObjectId()));
					W5Table t = FrameworkCache.getTable(projectId, defaultCrudForm.getObjectId()); // PromisCache.getTable(f.getScd(),
																									// f.getForm().getObjectId())
					m.set_defaultCrudForm(defaultCrudForm);
/*
					m.set_crudFormSmsMailList(find(
							"from W5FormSmsMail t where t.activeFlag=1 AND t.actionTips like '%0%' AND t.formId=?0 AND t.projectUuid=?1 order by t.tabOrder",
							m.getDefaultCrudFormId(), projectId));
					m.set_crudFormConversionList(find(
							"from W5Conversion t where t.activeFlag=1 AND t.actionTips like '%0%' AND t.srcFormId=?0 AND t.projectUuid=?1 order by t.tabOrder",
							m.getDefaultCrudFormId(), projectId));
*/
					//organizeListPostProcessQueryFields(cr.getScd(), t, m);

				}
			}
			
		}
		if(menuItems!=null)for(W5ObjectMenuItem d:menuItems) if(d.getObjectType()==8){
			W5Card m = mm.get(d.getObjectId());
			if(m!=null) {
				m.get_menuItemList().add(d);
			}			
		}
		if(toolbarItems!=null)for(W5ObjectToolbarItem d:toolbarItems) if(d.getObjectType()==8){
			W5Card m = mm.get(d.getObjectId());
			if(m!=null) {
				m.get_toolbarItemList().add(d);
			}			
		}
		wCards.put(projectId, mm);
	}

	public static void addMobileLists2Cache(String projectId, List<M5List> mobileLists) {
		Map<Integer, M5List> mm = new HashMap();
		
		if(mobileLists!=null)for(M5List m:mobileLists) {
			mm.put(m.getListId(), m);
		}
		mListViews.put(projectId, mm);		
	}

	public static void addExternalDbs2Cache(String projectId, List<W5ExternalDb> externalDbs) {
		Map<Integer, W5ExternalDb> myEDB = new HashMap();
		if(externalDbs!=null)for (W5ExternalDb j : externalDbs) {
			myEDB.put(j.getExternalDbId(), j);
		}

		wExternalDbs.put(projectId, myEDB);
		
	}

	public static void addExceptions2Cache(String projectId, List<W5Exception> exceptions) {
		
	}

	public static void addAppSettings2Cache(int customizationId, Map<String, String> appSettings2) {
		if(appSettings2 ==null)appSettings2 = new HashMap();
		appSettings.put(customizationId, appSettings2);		
	}

	public static void addMenus2Cache(String projectId, List<W5Menu> menus) {
		Map<Integer, List> mm = new HashMap();
		List<W5RoleGroup> roleGroups = wRoleGroups.get(projectId);
		if(menus!=null && roleGroups!=null) {
			Map<Integer, W5Menu> menuMap = new HashMap();
			for(W5Menu m : menus)menuMap.put(m.getMenuId(), m);
			
			if(roleGroups!=null) {
				for(W5RoleGroup rg:roleGroups) {
					mm.put(rg.getRoleGroupId(), new ArrayList());
				}
			}
			for(W5Menu m : menus) {
				List lm = mm.get(m.getRoleGroupId());
				if(lm!=null) {
					lm.add(m);
				/*	if(m.getParentMenuId()==0)
						lm.add(m);
					else {
						W5Menu pm = menuMap.get(m.getParentMenuId());
						if(pm!=null) {
							if(pm.get_children()==null)pm.set_children(new ArrayList());
							pm.get_children().add(m);
						}
					}*/
				}
			}
		}
		
		wMenus.put(projectId, mm);
		
	}

	public static void addRoleGroups2Cache(String projectId, List<W5RoleGroup> roleGroups) {
		if(roleGroups!=null)wRoleGroups.put(projectId, roleGroups);
		
	}
	

	public static Object getQueryResult4Menu(Map<String, Object> scd) {
		W5Project po = getProject(scd);
		W5Query q = getQuery(po.getProjectUuid(), 2822);
		
		W5QueryResult qr = new W5QueryResult(2822);
		qr.setNewQueryFields(q.get_queryFields());
		qr.setQuery(q); qr.setErrorMap(new HashMap());qr.setRequestParams(new HashMap());
		qr.setScd(scd);
		qr.setData(new ArrayList());
		List<W5Menu> menus = wMenus.get(po.getProjectUuid()).get(po.get_defaultRoleGroupId());
		if(menus!=null)for(W5Menu m:menus) {
			qr.getData().add(new Object[] {"mnu_"+m.getMenuId(), m.getLocaleMsgKey(), m.getNodeType()==4?m.getUrl():"", "", m.getImgIcon(), m.getTabOrder(), m.getMenuId(), "mnu_"+m.getParentMenuId()});
		}
		return qr;
	}
	

	public static String getExceptionMessage(Object o, String exceptionMessage) {
		if(exceptionMessage==null)return null;
		String projectId = o == null ? FrameworkSetting.devUuid : getProjectId(o, "-");
		String locale = null;
		if(o!=null && o instanceof Map)locale = (String)(((Map)o).get("locale"));
		if(locale==null)locale =  "en";
		List<W5Exception> l = wExceptions.get(projectId);
		if(l!=null) {
			for(W5Exception e:l)if(e.getLocale().equals(locale) && exceptionMessage.contains(e.getExceptionMessage()))
				return e.getUserMessage();
		}
		if(!projectId.equals(FrameworkSetting.devUuid)) {
			l = wExceptions.get(FrameworkSetting.devUuid);
			if(l!=null) {
				for(W5Exception e:l)if(e.getLocale().equals(locale) && exceptionMessage.contains(e.getExceptionMessage()))
					return e.getUserMessage();
			}
		}
		return exceptionMessage;
	}

	public static List<W5Table> getVcsTables() {
		List<W5Table> l = new ArrayList(150);
		Map<Integer, W5Table> m = wTables.get(FrameworkSetting.devUuid);
		for(W5Table t:m.values())if(t.getVcsFlag()!=0)l.add(t);
		return l;
	}

	final private static Map<String, String> linkMap = new HashMap<String,String>(); //wsId
	
	public static String getUrlFromLinkId(String linkId) {
		return linkMap.get(linkId);
	}
	
	public static String addUrlToLinkCache(String linkId, String url) {
		return linkMap.put(linkId, url);
	}
}
