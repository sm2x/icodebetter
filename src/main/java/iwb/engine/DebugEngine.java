package iwb.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.rdbms_impl.ExternalDBSql;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.domain.db.W5Query;
import iwb.domain.db.W5Table;
import iwb.domain.result.W5GlobalFuncResult;
import iwb.domain.result.W5QueryResult;
import iwb.exception.IWBException;
import iwb.util.DBUtil;
import iwb.util.GenericUtil;

@Component
public class DebugEngine {
	@Lazy
	@Autowired
	private PostgreSQL dao;
	
	@Lazy
	@Autowired
	private MetadataLoader metadataLoader;


	@Lazy
	@Autowired
	private GlobalScriptEngine scriptEngine;
	
	@Lazy
	@Autowired
	private ExternalDBSql externalDB;
	
	public Object executeQuery4Debug(Map<String, Object> scd, int queryId, Map<String, String> requestParams) {
		W5QueryResult queryResult = queryId < 0 ? new W5QueryResult(queryId) : metadataLoader.getQueryResult(scd, queryId);

		queryResult.setErrorMap(new HashMap());
		queryResult.setScd(scd);
		queryResult.setRequestParams(requestParams);
		if( queryId == -11 || (queryResult.getQuery()!=null && queryResult.getQuery().getQuerySourceTip()==4658)) { //externalDB
			W5Query q = new W5Query();
			queryResult.setQuery(q);
			q.setQuerySourceTip(4658);
			q.setMainTableId(GenericUtil.uInt(requestParams, "external_db_id"));
			q.setSqlSelect(requestParams.get("_sql_select"));
			q.setSqlFrom(requestParams.get("_sql_from"));
			q.setSqlWhere(requestParams.get("_sql_where"));
			q.setSqlGroupby(requestParams.get("_sql_groupby"));
			q.setSqlOrderby(requestParams.get("_sql_orderby"));
			if(queryResult.getQuery()!=null) {
				q.set_queryParams(queryResult.getQuery().get_queryParams());
			}
			queryResult.prepareQuery(null);
			if (queryResult.getErrorMap().isEmpty()) {
				long startTime = System.currentTimeMillis();
				externalDB.runQuery(queryResult);
				Map m = new HashMap();
				m.put("success", true);
				m.put("data", queryResult.getData());
				Map m2 = new HashMap();
				m2.put("startRow", 0);
				m2.put("fetchCount", queryResult.getData().size());
				m2.put("totalCount", queryResult.getData().size());
				m.put("browseInfo", m2);
				m.put("execTime", System.currentTimeMillis() - startTime);
				List<Map> fields = new ArrayList<>();
				if(queryResult.getData().size()>0) {
					Object o = queryResult.getData().get(0);
					if(o!=null) {
						Map<String, Object> row = (Map)o;
						for(String k:row.keySet()) {
							Map mm = new HashMap();
							mm.put("name", k);
							fields.add(mm);
						}
						
					}
					
					
				}
				m.put("fields", fields);
				m.put("influxQL", queryResult.getExecutedSql());
				return m;
			}
			
		} else if (queryId == -1 || queryResult.getQuery().getQuerySourceTip() != 0) {
			String orderBy = requestParams.get("sort");
			if (GenericUtil.isEmpty(orderBy))
				orderBy = requestParams.get("_sql_orderby");
			else if (requestParams.containsKey("dir"))
				orderBy += " " + requestParams.get("dir");
			queryResult.prepareQuery4Debug(requestParams.get("_sql_select"), requestParams.get("_sql_from"),
					requestParams.get("_sql_where"), requestParams.get("_sql_groupby"), orderBy);

			if (queryResult.getErrorMap().isEmpty()) {
				queryResult.setFetchRowCount(20);
				queryResult.setStartRowNumber(0);
				String sqlFrom2 = requestParams.get("_sql_from").toLowerCase();
				W5Table t = null;
				if (!sqlFrom2.contains("select") && !sqlFrom2.contains(",") && !sqlFrom2.contains("left")
						&& !sqlFrom2.contains("(")) {
					String[] ss = sqlFrom2.split(" ");
					if (ss.length < 3) {
						Integer tableId = FrameworkCache.findTableIdByName( ss[0], (String)scd.get("projectId"));
						if (tableId!=null)
							t = FrameworkCache.getTable(scd, tableId);
					}
				}
				if (FrameworkSetting.cloud && (Integer) scd.get("customizationId") > 0
						&& DBUtil.checkTenantSQLSecurity(queryResult.getExecutedSql())) {
					throw new IWBException("security", "SQL", 0, null,
							"Forbidden Command. Please contact Code2 team ;)", null);
				}

				dao.checkTenant(scd);
				return dao.executeSQLQuery2Map4Debug(scd, t, queryResult.getExecutedSql(), queryResult.getSqlParams(),
						GenericUtil.uIntNvl(requestParams, "limit", 50),
						GenericUtil.uIntNvl(requestParams, "start", 0));
				// if(queryResult.getData()==null)queryResult.setData(new
				// ArrayList());
			}
		} else { // rhino
			return scriptEngine.executeQueryAsScript4Debug(queryResult, requestParams.get("_sql_from"));
		}

		return queryResult;
	}

	public W5GlobalFuncResult executeGlobalFunc4Debug(Map<String, Object> scd, int dbFuncId,
			Map<String, String> parameterMap) {
		return scriptEngine.executeGlobalFunc4Debug(scd, dbFuncId, parameterMap);
	}
}
