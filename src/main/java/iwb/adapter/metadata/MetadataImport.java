package iwb.adapter.metadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.model.db.M5List;
import iwb.model.db.M5Menu;
import iwb.model.db.W5Card;
import iwb.model.db.W5Conversion;
import iwb.model.db.W5ConversionCol;
import iwb.model.db.W5CustomGridColumnCondition;
import iwb.model.db.W5CustomGridColumnRenderer;
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
import iwb.model.db.W5LookUp;
import iwb.model.db.W5LookUpDetay;
import iwb.model.db.W5Menu;
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

public class MetadataImport {
	
	private Object json2java(JSONObject j, Class c) {
		String mname = null;
		try {
			Constructor ct = c.getConstructor();
			Object o = ct.newInstance();
			Method[] ms = c.getMethods();
			for(String k:j.keySet()) {
				mname = "set"+k.substring(0,1).toUpperCase(FrameworkSetting.appLocale) + k.substring(1);
				for(Method m:ms)if(m.getName().equals(mname)) {
					Object value = j.get(k);
					Parameter p = m.getParameters()[0];
					if(p.getType()==Short.TYPE || p.getType()==Short.class) {
						m.invoke(o, (short)((int)value));
					} else if(p.getType()==BigDecimal.class) {
						m.invoke(o, new BigDecimal(value.toString()));
					} else m.invoke(o, value);
					break;					
				}
			}
			return o;
		}catch(Exception e) {
			System.out.println(c.getName() + "." + mname + " : " + e.getMessage());
			return null;
		}
	}
	
	private List jsonArray2java(JSONObject j, String key, Class c) {
		if(!j.has(key))return null;
		JSONArray ar = j.getJSONArray(key);
		List l = new ArrayList(ar.length());
		for(int qi=0;qi<ar.length();qi++)l.add(json2java(ar.getJSONObject(qi), c));
		return l;
	}
	

	
	public boolean fromJson(String s) {
		long startTime = System.currentTimeMillis();
		JSONObject j = new JSONObject(s);

		String projectId = j.getString("projectId");
		W5Project po = (W5Project)json2java(j.getJSONObject("project"), W5Project.class);
		po.setProjectUuid(projectId);

		List<W5RoleGroup> roleGroups = jsonArray2java(j, ("roleGroups"), W5RoleGroup.class);
		FrameworkCache.addRoleGroups2Cache(projectId, roleGroups);
		if(roleGroups!=null)for(W5RoleGroup rg:roleGroups)if(rg.getActiveFlag()!=0 && rg.getRoleGroupId()!=122) {
			po.set_defaultRoleGroupId(rg.getRoleGroupId());
			break;
		}
		FrameworkCache.addProject(po);
		
		Map<String, String> appSettings = jsonArray2map(j, ("appSettings"));
		FrameworkCache.addAppSettings2Cache(po.getCustomizationId(), appSettings);
		
		LocaleMsgCache.addLocaleMsgs2Cache(0, "en", new HashMap());
		if(po.getLocaleMsgKeyFlag()!=0) for(String locale:po.getLocales().split(",")){
			if(j.has("localeMsgs_"+locale)) {
				Map<String, String> res  = jsonArray2map(j, "localeMsgs_"+locale);
				LocaleMsgCache.addLocaleMsgs2Cache(po.getCustomizationId(), locale, res);
			} else 
				LocaleMsgCache.addLocaleMsgs2Cache(po.getCustomizationId(), "en", new HashMap());
		} else 
			LocaleMsgCache.addLocaleMsgs2Cache(po.getCustomizationId(), "en", new HashMap());
		
		
		
		List<W5LookUp> lookUps = jsonArray2java(j, ("lookUps"), W5LookUp.class);
		List<W5LookUpDetay> lookUpDetays = jsonArray2java(j, ("lookUpDetays"), W5LookUpDetay.class);
		FrameworkCache.addLookUps2Cache(projectId, lookUps, lookUpDetays);
		
		List<W5Table> tables = jsonArray2java(j, ("tables"), W5Table.class);
		List<W5TableField> tableFields = jsonArray2java(j, ("tableFields"), W5TableField.class);
		List<W5TableParam> tableParams = jsonArray2java(j, ("tableParams"), W5TableParam.class);
		List<W5TableEvent> tableEvents = jsonArray2java(j, ("tableEvents"), W5TableEvent.class);
		List<W5TableFieldCalculated> tableFieldCalculateds = jsonArray2java(j, ("tableFieldCalculateds"), W5TableFieldCalculated.class);
		List<W5TableChild> tableChilds = jsonArray2java(j, ("tableChilds"), W5TableChild.class);
		FrameworkCache.addTables2Cache(projectId, tables, tableFields, tableParams, tableEvents, tableFieldCalculateds, tableChilds);
		
		List<W5Ws> wss = jsonArray2java(j, ("wss"), W5Ws.class);
		List<W5WsMethod> wsMethods = jsonArray2java(j, ("wsMethods"), W5WsMethod.class);
		List<W5WsMethodParam> wsMethodParams = jsonArray2java(j, ("wsMethodParams"), W5WsMethodParam.class);
		FrameworkCache.addWss2Cache(projectId, wss, wsMethods, wsMethodParams);
		
		List<W5GlobalFunc> funcs = jsonArray2java(j, ("funcs"), W5GlobalFunc.class);
		List<W5GlobalFuncParam> funcParams = jsonArray2java(j, ("funcParams"), W5GlobalFuncParam.class);
		FrameworkCache.addFuncs2Cache(projectId, funcs, funcParams);
		
		List<W5Query> queries = jsonArray2java(j, ("queries"), W5Query.class);
		List<W5QueryField> queryFields = jsonArray2java(j, ("queryFields"), W5QueryField.class);
		List<W5QueryParam> queryParams = jsonArray2java(j, ("queryParams"), W5QueryParam.class);
		FrameworkCache.addQueries2Cache(projectId, queries, queryFields, queryParams);

		List<W5ObjectToolbarItem> toolbarItems = jsonArray2java(j, ("toolbarItems"), W5ObjectToolbarItem.class);
		List<W5ObjectMenuItem> menuItems = jsonArray2java(j, ("menuItems"), W5ObjectMenuItem.class);

		List<W5Form> forms = jsonArray2java(j, ("forms"), W5Form.class);
		List<W5FormCell> formCells = jsonArray2java(j, ("formCells"), W5FormCell.class);
		List<W5FormModule> formModules = jsonArray2java(j, ("formModules"), W5FormModule.class);
		List<W5FormCellProperty> formCellProperties = jsonArray2java(j, ("formCellProperties"), W5FormCellProperty.class);
		List<W5FormSmsMail> formSmsMails = jsonArray2java(j, ("formSmsMails"), W5FormSmsMail.class);
		List<W5FormHint> formHints = jsonArray2java(j, ("formHints"), W5FormHint.class);
		FrameworkCache.addForms2Cache(projectId, forms, formCells, formModules, formCellProperties, formSmsMails, formHints, toolbarItems);

		
		List<W5Grid> grids = jsonArray2java(j, ("grids"), W5Grid.class);
		List<W5GridColumn> gridColumns = jsonArray2java(j, ("gridColumns"), W5GridColumn.class);
		List<W5CustomGridColumnCondition> gridColumnCustomConditions = jsonArray2java(j, ("gridColumnCustomConditions"), W5CustomGridColumnCondition.class);
		List<W5CustomGridColumnRenderer> gridColumnCustomRenderers = jsonArray2java(j, ("gridColumnCustomRenderers"), W5CustomGridColumnRenderer.class);
		FrameworkCache.addGrids2Cache(projectId, grids, gridColumns, gridColumnCustomConditions, gridColumnCustomRenderers, toolbarItems, menuItems, formCells);


		List<M5List> mobileLists = jsonArray2java(j, ("mobileLists"), M5List.class);
		FrameworkCache.addMobileLists2Cache(projectId, mobileLists);
		
		List<W5Card> cards = jsonArray2java(j, ("cards"), W5Card.class);
		FrameworkCache.addCards2Cache(projectId, cards, toolbarItems, menuItems);
		
		List<W5Workflow> workflows = jsonArray2java(j, ("workflows"), W5Workflow.class);
		List<W5WorkflowStep> workflowSteps = jsonArray2java(j, ("workflowSteps"), W5WorkflowStep.class);
		FrameworkCache.addWorkflows2Cache(projectId, workflows, workflowSteps);

		List<W5Conversion> conversions = jsonArray2java(j, ("conversions"), W5Conversion.class);
		List<W5ConversionCol> conversionCols = jsonArray2java(j, ("conversionCols"), W5ConversionCol.class);
		FrameworkCache.addConversions2Cache(projectId, conversions, conversionCols);
		
		List<W5Page> pages = jsonArray2java(j, ("pages"), W5Page.class);
		List<W5PageObject> pageObjects = jsonArray2java(j, ("pageObjects"), W5PageObject.class);
		FrameworkCache.addPages2Cache(projectId, pages, pageObjects);

		List<W5Menu> menus = jsonArray2java(j, ("menus"), W5Menu.class);
		FrameworkCache.addMenus2Cache(projectId, menus);
		
		List<M5Menu> mmenus = jsonArray2java(j, ("mmenus"), M5Menu.class);

//		List<M5Menu> mmenus"), M5Menu.class);

		List<W5ExternalDb> externalDbs = jsonArray2java(j, ("externalDbs"), W5ExternalDb.class);
		FrameworkCache.addExternalDbs2Cache(projectId, externalDbs);

		List<W5Exception> exceptions = jsonArray2java(j, ("exceptions"), W5Exception.class);
		FrameworkCache.addExceptions2Cache(projectId, exceptions);
		System.out.println("Imported " + projectId + " in " + (System.currentTimeMillis()-startTime) + "ms");
		return true;		
	}

	private Map<String, String> jsonArray2map(JSONObject j, String key) {
		if(!j.has(key))return null;
		JSONObject ar = j.getJSONObject(key);
		Map m = new HashMap();
		for(String k:ar.keySet())
			m.put(k, ar.get(k));
		return m;
	}
	
	
}
