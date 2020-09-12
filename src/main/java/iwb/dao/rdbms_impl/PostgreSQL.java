package iwb.dao.rdbms_impl;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
// import org.influxdb.InfluxDBFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.dao.metadata.MetadataLoader;
import iwb.engine.GlobalScriptEngine;
import iwb.enums.FieldDefinitions;
import iwb.exception.IWBException;
import iwb.model.db.Log5GlobalFuncAction;
import iwb.model.db.Log5QueryAction;
import iwb.model.db.W5Email;
import iwb.model.db.W5Form;
import iwb.model.db.W5FormCell;
import iwb.model.db.W5FormCellProperty;
import iwb.model.db.W5FormModule;
import iwb.model.db.W5FormSmsMail;
import iwb.model.db.W5FormValue;
import iwb.model.db.W5FormValueCell;
import iwb.model.db.W5GlobalFuncParam;
import iwb.model.db.W5Grid;
import iwb.model.db.W5LookUp;
import iwb.model.db.W5LookUpDetay;
import iwb.model.db.W5Param;
import iwb.model.db.W5Project;
import iwb.model.db.W5Query;
import iwb.model.db.W5QueryField;
import iwb.model.db.W5QueryParam;
import iwb.model.db.W5Table;
import iwb.model.db.W5TableChild;
import iwb.model.db.W5TableField;
import iwb.model.db.W5TableFieldCalculated;
import iwb.model.db.W5TableParam;
import iwb.model.db.W5VcsCommit;
import iwb.model.db.W5VcsObject;
import iwb.model.db.W5Workflow;
import iwb.model.db.W5WorkflowStep;
import iwb.model.db.W5WsMethod;
import iwb.model.db.W5WsMethodParam;
import iwb.model.helper.W5FormCellHelper;
import iwb.model.helper.W5TableChildHelper;
import iwb.model.helper.W5TableRecordHelper;
import iwb.model.result.W5FormResult;
import iwb.model.result.W5GlobalFuncResult;
import iwb.model.result.W5QueryResult;
import iwb.model.result.W5TableRecordInfoResult;
import iwb.service.FrameworkService;
import iwb.util.DBUtil;
import iwb.util.EncryptionUtil;
import iwb.util.GenericUtil;
import iwb.util.LogUtil;
import iwb.util.MailUtil;
import iwb.util.UserUtil;

@SuppressWarnings({ "unchecked", "unused" })
@Repository
public class PostgreSQL extends BaseDAO {
	final public static String[] dateFormatMulti = new String[] {"dd/mm/yyyy","mm/dd/yyyy","yyyy/mm/dd"};

	@Lazy
	@Autowired
	private MetadataLoader metadataLoader;

	private static Logger logger = Logger.getLogger(PostgreSQL.class);
	@Autowired
	private FrameworkService service;

	@Lazy
	@Autowired
	private GlobalScriptEngine scriptEngine;
	
	@Lazy
	@Autowired
	private ExternalDBSql externalDB;

	


	/*
	 * public void setEngine(FrameworkEngine engine) { this.engine = engine; }
	 */

	public W5QueryResult executeQuery(Map<String, Object> scd, int queryId, Map<String, String> requestParams) {
		W5QueryResult queryResult = metadataLoader.getQueryResult(scd, queryId);
		if (queryId != 1 && queryId != 824 && queryResult.getMainTable() != null && (!FrameworkSetting.debug
				|| (scd.get("roleId") != null && GenericUtil.uInt(scd.get("roleId")) != 0)) && queryResult.getQuery().getQuerySourceType()!=4658) {
			W5Table t = queryResult.getMainTable();
			if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
					t.getAccessViewRoles(), t.getAccessViewUsers())) {
				throw new IWBException("security", "Query", queryId, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_table_control_view"),
						null);
			}
		}
		queryResult.setErrorMap(new HashMap());
		queryResult.setRequestParams(requestParams);

		//
		// queryResult.setOrderBy(PromisUtil.uStrNvl(requestParams.get(PromisUtil.uStrNvl(PromisSetting.appSettings.get("sql_sort"),"sort")),
		// queryResult.getQuery().getSqlOrderby()));
		if (requestParams.get("sort") != null) {
			queryResult.setOrderBy(requestParams.get("sort"));
			if (requestParams.get("dir") != null)
				queryResult.setOrderBy(queryResult.getOrderBy() + " " + requestParams.get("dir"));
		} else
			queryResult.setOrderBy(queryResult.getQuery().getSqlOrderby());
		switch (queryResult.getQuery().getQueryType()) {
		case 9:
		case 10:
			queryResult.prepareTreeQuery(null);
			break;
		default:
			queryResult.prepareQuery(null);
		}
		if (queryResult.getErrorMap().isEmpty()) {
			queryResult.setFetchRowCount(
					GenericUtil.uIntNvl(requestParams, "limit", GenericUtil.uInt(requestParams, "firstLimit")));
			queryResult.setStartRowNumber(GenericUtil.uInt(requestParams, "start"));
			
			if(queryResult.getQuery().getQuerySourceType()!=4658) {
				runQuery(queryResult);
				if (queryResult.getQuery().getShowParentRecordFlag() != 0 && queryResult.getData() != null) {
					for (Object[] oz : queryResult.getData()) {
						int tableId = GenericUtil.uInt(oz[queryResult.getQuery().get_tableIdTabOrder()]);
						int tablePk = GenericUtil.uInt(oz[queryResult.getQuery().get_tablePkTabOrder()]);
						oz[oz.length - 1] = findRecordParentRecords(scd, tableId, tablePk, 0, true);
					}
				}
			} else {
				externalDB.runQuery(queryResult);
			}
		}
		return queryResult;
	}

	private void logTableRecord(final W5FormResult fr, String paramSuffix) {
		W5Table t = FrameworkCache.getTable(fr.getScd(), fr.getForm().getObjectId());
		final StringBuilder sql = new StringBuilder();
		int userId = (Integer) fr.getScd().get("userId");
		int action = fr.getAction();
		String table = t.getDsc();

		if (FrameworkSetting.log2tsdb) {
			String schema = FrameworkCache.getProject(fr.getScd()).getRdbmsSchema();
			sql.append("select * from ").append(table).append(" t ");
			List<Object> whereParams = new ArrayList<Object>(fr.getPkFields().size());

			if (fr.getPkFields().size() > 0) {
				sql.append(" where ");
				boolean b = false;
				StringBuilder startQL = new StringBuilder();
				startQL.append(schema).append("_").append(t.getDsc().replace('.', '_'));
				for (W5TableParam px : t.get_tableParamList()) {
					if (b)
						sql.append(" AND ");
					else
						b = true;
					sql.append("t.").append(px.getExpressionDsc()).append("=?");
					whereParams.add(fr.getPkFields().get(px.getDsc()));
					startQL.append(",").append(px.getExpressionDsc()).append("=")
							.append(fr.getPkFields().get(px.getDsc()));
				}
				List<Map> l = executeSQLQuery2Map(sql.toString(), whereParams);
				if (!GenericUtil.isEmpty(l)) {
					Map m = l.get(0);
					for (W5TableParam px : t.get_tableParamList())
						m.remove(px.getExpressionDsc());
					m.put("_action", fr.getAction());
					startQL.append(" ").append(GenericUtil.fromMapToInfluxFields(m));
					LogUtil.logCrud(startQL.toString());
					// influxDao.insert(pr, "log_table.."+table, p, l.get(0),
					// null); TODO
				}
			}
			return;
		}

		String logTable = table;
		if (logTable.contains(".")) {
			logTable = logTable.substring(logTable.lastIndexOf('.') + 1);
		}
		Session session = getCurrentSession();
		final W5Project po = FrameworkCache.getProject(fr.getScd().get("projectId"));

		sql.append("select count(*)  from information_schema.tables qx where lower(qx.table_name) = '")
				.append(logTable.toLowerCase()).append("' and lower(qx.table_schema) = '").append(po.getRdbmsSchema())
				.append("_log'");
		final int count = GenericUtil.uInt(session.createSQLQuery(sql.toString()).uniqueResult());
		final int schemaCount = count == 0 ? GenericUtil.uInt(session.createSQLQuery(
				"select count(1) from information_schema.schemata where schema_name='" + po.getRdbmsSchema() + "_log'")
				.uniqueResult()) : 1;

		sql.setLength(0);
		sql.append(" select nextval('iwb.seq_log') ").append(FieldDefinitions.tableFieldName_LogId).append(",")
				.append(userId).append(" ").append(FieldDefinitions.tableFieldName_LogUserId).append(",").append(action)
				.append(" ").append(FieldDefinitions.tableFieldName_LogAction).append(",iwb.fnc_sysdate(")
				.append(fr.getScd().get("customizationId")).append(") ")
				.append(FieldDefinitions.tableFieldName_LogDateTime).append(",t.*");

		sql.append(" from ").append(table).append(" t");

		final List<Object> whereParams = new ArrayList<Object>(fr.getPkFields().size());

		if (fr.getPkFields().size() > 0) {
			sql.append(" where ");
			boolean b = false;

			for (W5TableParam p : t.get_tableParamList()) {
				if (b)
					sql.append(" AND ");
				else
					b = true;
				sql.append("t.").append(p.getExpressionDsc()).append("=?");
				whereParams.add(fr.getPkFields().get(p.getDsc()));
			}
		}

		final String flogTable = logTable;
		getCurrentSession().doWork(new Work() {

			public void execute(Connection conn) throws SQLException {
				try {
					if (schemaCount == 0) {
						PreparedStatement s = conn
								.prepareStatement("CREATE SCHEMA IF NOT EXISTS " + po.getRdbmsSchema() + "_log AUTHORIZATION iwb");
						s.execute();
						s.close();
					}
					String createSql = GenericUtil.replaceSql("create table " + po.getRdbmsSchema() + "_log."
							+ FrameworkSetting.crudLogTablePrefix + flogTable + " as " + sql.toString(), whereParams);
					if (count == 0) {
						PreparedStatement s = conn.prepareStatement(createSql);
						s.execute();
						s.close();
					} else {
						Savepoint savepoint = conn.setSavepoint("spx-1");
						PreparedStatement s = conn.prepareStatement(
								"insert into " + po.getRdbmsSchema() + "_log." + FrameworkSetting.crudLogTablePrefix
										+ flogTable + GenericUtil.replaceSql(sql.toString(), whereParams));
						try {
							s.execute();
							s.close();
						} catch (SQLException e) {
							if (conn != null && savepoint != null) {
								conn.rollback(savepoint);
							}

							s = conn.prepareStatement(
									"alter table " + po.getRdbmsSchema() + "_log." + FrameworkSetting.crudLogTablePrefix
											+ flogTable + " rename to lt5_" + fr.getForm().getObjectId() + "_"
											+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
							s.execute();
							s.close();

							s = conn.prepareStatement(createSql);
							s.execute();
							s.close();
						}

					}
				} catch (Exception e) {
					if (FrameworkSetting.debug)
						e.printStackTrace();
					throw new IWBException("sql", "Form Log", fr.getFormId(),
							GenericUtil.replaceSql(sql.toString(), whereParams), e.getMessage(), e.getCause());
				}
			}
		});
	}

	private void prepareLookupTableQuery(W5QueryResult queryResult, StringBuilder sql2, AtomicInteger paramIndex) { // TODO
		List<Object> preParams = new ArrayList<Object>();
		for (W5QueryField qf : queryResult.getQuery().get_queryFields())
			if (qf.getPostProcessType() == 12 && qf.getLookupQueryId() != 0 && (queryResult.getQueryColMap() == null
					|| queryResult.getQueryColMap().containsKey(qf.getDsc()))) { // queryField'da
																					// postProcessType=lookupTable
																					// 
				// saptaniyor
				W5Table tqf = FrameworkCache.getTable(queryResult.getScd(), qf.getLookupQueryId());
				if (tqf != null && tqf.getSummaryRecordSql() != null) {
					sql2.append(",(select ");

					if (tqf.getSummaryRecordSql().contains("${")) {
						Object[] oz = DBUtil.filterExt4SQL(tqf.getSummaryRecordSql(), queryResult.getScd(),
								queryResult.getRequestParams(), null);
						sql2.append(oz[0]);
						if (oz[1] != null)
							preParams.addAll((List) oz[1]);
					} else
						sql2.append(tqf.getSummaryRecordSql());

					sql2.append(" from ").append(tqf.getDsc()).append(" x where x.")
							.append(tqf.get_tableParamList().get(0).getExpressionDsc()).append("=z.")
							.append(qf.getDsc());

					sql2.append(DBUtil.includeTenantProjectPostSQL(queryResult.getScd(), tqf));

					sql2.append(") ").append(qf.getDsc()).append("_qw_ ");
					W5QueryField field = new W5QueryField();
					field.setDsc(qf.getDsc() + "_qw_");
					field.setMainTableFieldId(qf.getMainTableFieldId());
					if (queryResult.getPostProcessQueryFields() == null)
						queryResult.setPostProcessQueryFields(new ArrayList());
					queryResult.getPostProcessQueryFields().add(field);
				}
			} else if (qf.getPostProcessType() == 13 && qf.getLookupQueryId() != 0
					&& (queryResult.getQueryColMap() == null
							|| queryResult.getQueryColMap().containsKey(qf.getDsc()))) { // queryField'da
																							// postProcessType=lookupTable
																							// 
				// saptaniyor
				W5Table tqf = FrameworkCache.getTable(queryResult.getScd(), qf.getLookupQueryId());
				if (tqf != null && tqf.getSummaryRecordSql() != null) {
					sql2.append(",(select ");

					if (tqf.getSummaryRecordSql().contains("${")) {
						Object[] oz = DBUtil.filterExt4SQL(tqf.getSummaryRecordSql(), queryResult.getScd(),
								queryResult.getRequestParams(), null);
						sql2.append("string_agg(").append(oz[0]).append(",',')");
						if (oz[1] != null)
							preParams.addAll((List) oz[1]);
					} else
						sql2.append("string_agg(").append(tqf.getSummaryRecordSql()).append(",',')");

					sql2.append(" from ").append(tqf.getDsc()).append(" x where x.")
							.append(tqf.get_tableParamList().get(0).getExpressionDsc())
							.append(" in (select q.satir::int from iwb.tool_parse_numbers(z.").append(qf.getDsc())
							.append(",',') q)");

					sql2.append(DBUtil.includeTenantProjectPostSQL(queryResult.getScd(), tqf));

					sql2.append(") ").append(qf.getDsc()).append("_qw_ ");
					W5QueryField field = new W5QueryField();
					field.setDsc(qf.getDsc() + "_qw_");
					field.setMainTableFieldId(qf.getMainTableFieldId());
					if (queryResult.getPostProcessQueryFields() == null)
						queryResult.setPostProcessQueryFields(new ArrayList());
					queryResult.getPostProcessQueryFields().add(field);
				}
			}
		if (preParams.size() > 0)
			queryResult.getSqlParams().addAll(paramIndex.intValue(), preParams);
	}

	public void runQuery(final W5QueryResult queryResult) {

		final W5Query query = queryResult.getQuery();
		Log5QueryAction queryAction = new Log5QueryAction(queryResult);

		// String sql = null;
		final W5Table mainTable = queryResult.getMainTable();
		final StringBuilder sql2 = new StringBuilder();
		final AtomicInteger paramIndex = new AtomicInteger(0);
		String error = null;

		try {

			getCurrentSession().doWork(new Work() {

				public void execute(Connection conn) throws SQLException {
					PreparedStatement s = null;
					ResultSet rs = null;
					Set<String> errorFieldSet = new HashSet();
					Map<String, Object> aggResultMap = query.get_aggQueryFields()!=null ? new HashMap():null;
					
					if (queryResult.getFetchRowCount() != 0) {
						if (false && !GenericUtil.isEmpty(queryResult.getExecutedSqlFrom())) {
							s = conn.prepareStatement("select count(1) " + queryResult.getExecutedSqlFrom());
							applyParameters(s, queryResult.getExecutedSqlFromParams());
						} else {
							StringBuilder aggSql = new StringBuilder();
							aggSql.append("select count(1) cnt");
							
							if(query.get_aggQueryFields()!=null)for(W5QueryField f:query.get_aggQueryFields()){
								aggSql.append(", sum(").append(f.getDsc()).append(") code2agg_sum_").append(f.getDsc());
							}
							aggSql.append(" from (").append(queryResult.getExecutedSql()).append(") x");
							
							s = conn.prepareStatement(aggSql.toString());
							applyParameters(s, queryResult.getSqlParams());
						}
						rs = s.executeQuery();
						rs.next();

						int resultRowCount = rs.getBigDecimal(1).intValue();
						if(query.get_aggQueryFields()!=null)for(int qi=0;qi<query.get_aggQueryFields().size();qi++){
							aggResultMap.put(query.get_aggQueryFields().get(qi).getDsc(), rs.getObject(qi+2));
						}
						rs.close();
						s.close();
						if (resultRowCount < queryResult.getStartRowNumber()) {
							queryResult.setStartRowNumber(0);
						}
						queryResult.setResultRowCount(resultRowCount);
						if (resultRowCount < queryResult.getStartRowNumber() + queryResult.getFetchRowCount()) {
							queryResult.setFetchRowCount((int) (resultRowCount - queryResult.getStartRowNumber()));
						}

						queryResult.getSqlParams().add(queryResult.getFetchRowCount()); // queryResult.getStartRowNumber()+
						if (queryResult.getStartRowNumber() > 0)
							queryResult.getSqlParams().add(queryResult.getStartRowNumber());

						sql2.append("select z.*");
						if (queryResult.getPostProcessQueryFields() != null && mainTable != null) {
							addPostQueryFields(queryResult, sql2, paramIndex);
						}

						for (W5QueryField qf : queryResult.getQuery().get_queryFields())
							if ((qf.getPostProcessType() == 12 || qf.getPostProcessType() == 13)
									&& qf.getLookupQueryId() != 0) { // queryField'da
																		// postProcessType=lookupTable
																		// 
																		// saptaniyor
								prepareLookupTableQuery(queryResult, sql2, paramIndex);
								break;
							}
						for (W5QueryField qf : queryResult.getQuery().get_queryFields())
							if (qf.getPostProcessType() == 71) { // fileAttach
								if(GenericUtil.isEmpty(queryResult.getScd()) || FrameworkCache.getTable(queryResult.getScd(), FrameworkSetting.customFileTableId)==null)sql2.append(
										",(select q.original_file_name from iwb.w5_file_attachment q where q.project_uuid='")
										.append(queryResult.getScd().get("projectId"))
										.append("' AND q.file_attachment_id=z.").append(qf.getDsc())
										.append("::integer) ").append(qf.getDsc()).append("_qw_ ");
								else sql2.append(
										",(select q.dsc from x_file q where q.file_id=z.").append(qf.getDsc()).append("::integer) ").append(qf.getDsc()).append("_qw_ ");
								W5QueryField field = new W5QueryField();
								field.setDsc(qf.getDsc() + "_qw_");
								field.setFieldType((short) 10);
								field.setMainTableFieldId(qf.getMainTableFieldId());
								if (queryResult.getPostProcessQueryFields() == null)
									queryResult.setPostProcessQueryFields(new ArrayList());
								queryResult.getPostProcessQueryFields().add(field);

							}
						for (W5QueryField qf : queryResult.getQuery().get_queryFields())
							if ((qf.getPostProcessType() == 16 || qf.getPostProcessType() == 17)
									&& qf.getLookupQueryId() != 0) { // queryField'da
																		// postProcessType=lookupQuery
																		// 
																		// saptaniyor
								W5QueryResult queryFieldLookupQueryResult = metadataLoader
										.getQueryResult(queryResult.getScd(), qf.getLookupQueryId());
								if (queryFieldLookupQueryResult != null
										&& queryFieldLookupQueryResult.getQuery() != null) {
									W5QueryField field = new W5QueryField();
									field.setDsc(qf.getDsc() + "_qw_");
									errorFieldSet.add(field.getDsc());
									field.setMainTableFieldId(qf.getMainTableFieldId());
									if (queryResult.getPostProcessQueryFields() == null)
										queryResult.setPostProcessQueryFields(new ArrayList());
									queryResult.getPostProcessQueryFields().add(field);
									if (qf.getPostProcessType() == 16
											&& queryFieldLookupQueryResult.getQuery().get_queryFields().size() > 2)
										for (int qi = 2; qi < queryFieldLookupQueryResult.getQuery().get_queryFields()
												.size(); qi++) {
											W5QueryField qf2 = queryFieldLookupQueryResult.getQuery().get_queryFields()
													.get(qi);
											field = new W5QueryField();
											field.setDsc(qf.getDsc() + "__" + qf2.getDsc());
											errorFieldSet.add(field.getDsc());
											field.setMainTableFieldId(qf2.getMainTableFieldId());
											queryResult.getPostProcessQueryFields().add(field);
										}
								}
							}

						sql2.append("from (").append(queryResult.getExecutedSql()).append(" limit ?");
						if (queryResult.getStartRowNumber() > 0) {
							sql2.append(" offset ?");
						}
						sql2.append(") z");

					} else {
						if(query.get_aggQueryFields()!=null) {//aggregation Map
							StringBuilder aggSql = new StringBuilder();
							aggSql.append("select 1 cnt");
							
							for(W5QueryField f:query.get_aggQueryFields()){
								aggSql.append(", sum(").append(f.getDsc()).append(") code2agg_sum_").append(f.getDsc());
							}
							aggSql.append(" from (").append(queryResult.getExecutedSql()).append(") x");
							
							s = conn.prepareStatement(aggSql.toString());
							applyParameters(s, queryResult.getSqlParams());
							
							rs = s.executeQuery();
							rs.next();
		
//							int resultRowCount = rs.getBigDecimal(1).intValue();
							if(query.get_aggQueryFields()!=null)for(int qi=0;qi<query.get_aggQueryFields().size();qi++){
								aggResultMap.put(query.get_aggQueryFields().get(qi).getDsc(), rs.getObject(qi+2));
							}
							rs.close();
							s.close();
						}
						
						if (queryResult.getPostProcessQueryFields() != null && mainTable != null) {
							sql2.append("select z.*"); //
							addPostQueryFields(queryResult, sql2, paramIndex);

							for (W5QueryField qf : queryResult.getQuery().get_queryFields())
								if ((qf.getPostProcessType() == 12 || qf.getPostProcessType() == 13)
										&& qf.getLookupQueryId() != 0) { // queryField'da
																			// postProcessType=lookupTable
																			// 
									// saptaniyor
									prepareLookupTableQuery(queryResult, sql2, paramIndex);
									break;
								}

							for (W5QueryField qf : queryResult.getQuery().get_queryFields())
								if ((qf.getPostProcessType() == 16 || qf.getPostProcessType() == 17)
										&& qf.getLookupQueryId() != 0) { // queryField'da
																			// postProcessType=lookupQuery
																			// 
									// saptaniyor
									W5QueryResult queryFieldLookupQueryResult = metadataLoader
											.getQueryResult(queryResult.getScd(), qf.getLookupQueryId());
									if (queryFieldLookupQueryResult != null
											&& queryFieldLookupQueryResult.getQuery() != null) {
										W5QueryField field = new W5QueryField();
										field.setDsc(qf.getDsc() + "_qw_");
										errorFieldSet.add(field.getDsc());
										field.setMainTableFieldId(qf.getMainTableFieldId());
										if (queryResult.getPostProcessQueryFields() == null)
											queryResult.setPostProcessQueryFields(new ArrayList());
										queryResult.getPostProcessQueryFields().add(field);
										if (qf.getPostProcessType() == 16
												&& queryFieldLookupQueryResult.getQuery().get_queryFields().size() > 2)
											for (int qi = 2; qi < queryFieldLookupQueryResult.getQuery()
													.get_queryFields().size(); qi++) {
												W5QueryField qf2 = queryFieldLookupQueryResult.getQuery()
														.get_queryFields().get(qi);
												field = new W5QueryField();
												field.setDsc(qf.getDsc() + "__" + qf2.getDsc());
												errorFieldSet.add(field.getDsc());
												field.setMainTableFieldId(qf2.getMainTableFieldId());
												queryResult.getPostProcessQueryFields().add(field);
											}
									}
								}

							sql2.append(" from (").append(queryResult.getExecutedSql()).append(" ) z");

						} else {
							sql2.append("select z.*");
							for (W5QueryField qf : queryResult.getQuery().get_queryFields())
								if (qf.getPostProcessType() == 12 && qf.getLookupQueryId() != 0) { // queryField'da
																									// postProcessType=lookupTable
																									// 
									// saptaniyor
									prepareLookupTableQuery(queryResult, sql2, paramIndex);
									break;
								}
							for (W5QueryField qf : queryResult.getQuery().get_queryFields())
								if ((qf.getPostProcessType() == 16) && qf.getLookupQueryId() != 0) { // queryField'da
																									// postProcessType=lookupQuery
																									// 
									// saptaniyor
									W5QueryResult queryFieldLookupQueryResult = metadataLoader
											.getQueryResult(queryResult.getScd(), qf.getLookupQueryId());
									if (queryFieldLookupQueryResult != null
											&& queryFieldLookupQueryResult.getQuery() != null) {
										W5QueryField field = new W5QueryField();
										field.setDsc(qf.getDsc() + "_qw_");
										errorFieldSet.add(field.getDsc());
										field.setMainTableFieldId(qf.getMainTableFieldId());
										if (queryResult.getPostProcessQueryFields() == null)
											queryResult.setPostProcessQueryFields(new ArrayList());
										queryResult.getPostProcessQueryFields().add(field);
										if (qf.getPostProcessType() == 16
												&& queryFieldLookupQueryResult.getQuery().get_queryFields().size() > 2)
											for (int qi = 2; qi < queryFieldLookupQueryResult.getQuery()
													.get_queryFields().size(); qi++) {
												W5QueryField qf2 = queryFieldLookupQueryResult.getQuery()
														.get_queryFields().get(qi);
												field = new W5QueryField();
												field.setDsc(qf.getDsc() + "__" + qf2.getDsc());
												errorFieldSet.add(field.getDsc());
												field.setMainTableFieldId(qf2.getMainTableFieldId());
												queryResult.getPostProcessQueryFields().add(field);
											}
									}
								}

							sql2.append(" from (").append(queryResult.getExecutedSql()).append(" ) z");
						}
					}

					List<Object[]> resultData = queryResult.getFetchRowCount() == 0 ? new ArrayList<Object[]>()
							: new ArrayList<Object[]>(queryResult.getFetchRowCount());
					// sql = sql2.toString();
					s = conn.prepareStatement(sql2.toString());
					applyParameters(s, queryResult.getSqlParams());
					queryResult.setExecutedSql(sql2.toString());
					//
					// if(PromisSetting.debug)logger.info(PromisUtil.replaceSql(sql2.toString(),queryResult.getSqlParams()));
					rs = s.executeQuery();
					int maxTabOrder = 0;
					Set liveSyncKeys = null;
					if (FrameworkSetting.liveSyncRecord && mainTable != null && mainTable.getLiveSyncFlag() != 0
							&& FrameworkCache.isDevEntity("15." + mainTable.getTableId())
							&& queryResult.getRequestParams() != null && queryResult.getScd() != null
							&& queryResult.getRequestParams().containsKey(".t")
							&& queryResult.getRequestParams().containsKey(".w")) {
						int grdOrFcId = GenericUtil.uInt(queryResult.getRequestParams().get("_gid"));
						if (grdOrFcId == 0)
							grdOrFcId = -GenericUtil.uInt(queryResult.getRequestParams().get("_fdid"));
						if (grdOrFcId != 0) {
							boolean mobile = GenericUtil.uInt(queryResult.getScd().get("mobile")) != 0;
							String sessionId = mobile ? (String) queryResult.getScd().get("mobileDeviceId")
									: (String) queryResult.getScd().get("sessionId");
							liveSyncKeys = UserUtil.getTableGridFormCellCachedKeys(
									(String) queryResult.getScd().get("projectId"), mainTable.getTableId(),
									(Integer) queryResult.getScd().get("userId"), sessionId,
									mobile ? sessionId : queryResult.getRequestParams().get(".w"),
									queryResult.getRequestParams().get(".t"), grdOrFcId, queryResult.getRequestParams(),
									true);
							// if(liveSyncKeys!=null)liveSyncKeys.clear();
							// !queryResult.getScd().containsKey("mobile") ||
						}
					}
					List<W5QueryField> newQueryFields = null;
					int dateFormat = 0;
					while (rs.next() /*
										 * && (maxFetchedCount==0 || totalFetchedCount<maxFetchedCount )
										 */) {
						if (newQueryFields == null) {
							if(queryResult.getScd()!=null)dateFormat = GenericUtil.uInt(queryResult.getScd().get("date_format"));
							newQueryFields = new ArrayList(queryResult.getQuery().get_queryFields().size()
									+ (queryResult.getPostProcessQueryFields() != null
											? queryResult.getPostProcessQueryFields().size()
											: 0));
							if (queryResult.getQueryColMap() != null) {
								for (W5QueryField qf : queryResult.getQuery().get_queryFields())
									if (queryResult.getQueryColMap().containsKey(qf.getDsc())) {
										newQueryFields.add(qf);
										if (maxTabOrder < qf.getTabOrder())
											maxTabOrder = qf.getTabOrder();
									}
							} else {
								for (W5QueryField qf : queryResult.getQuery().get_queryFields())
									if (qf.getTabOrder() > 0) {
										W5TableField tf = queryResult.getMainTable() != null
												&& qf.getMainTableFieldId() > 0
														? queryResult.getMainTable().get_tableFieldMap().get(
																qf.getMainTableFieldId())
														: null;
										if (tf == null || (GenericUtil.accessControl4SessionField(queryResult.getScd(), tf.getRelatedSessionField())
												&& (tf.getAccessViewUserFields() != null || GenericUtil.accessControl(
														queryResult.getScd(), tf.getAccessViewTip(),
														tf.getAccessViewRoles(), tf.getAccessViewUsers())))) { // access
																												// control
											newQueryFields.add(qf);
											if (maxTabOrder < qf.getTabOrder())
												maxTabOrder = qf.getTabOrder();
											if(tf!=null && qf.getPostProcessType()==0) {
												if(tf.getLkpEncryptionType()!=0) {
													qf.setPostProcessType((short)5);
													qf.setLookupQueryId(tf.getLkpEncryptionType());
													if(tf.getAccessMaskTip()>0)
														qf.setPostProcessType((short)14);
												} else if(tf.getAccessMaskTip()>0) {
													qf.setPostProcessType((short)4);
													qf.setLookupQueryId(tf.getAccessMaskTip());
												}
											}
										}
									}
							}
							// if(!GenericUtil.isEmpty(tf.getRelatedSessionField())
							// &&
							// GenericUtil.uInt(formResult.getScd().get(tf.getRelatedSessionField()))==0)continue;

							// post process fields:comment, file attachment,
							// access_control, approval

							if (queryResult.getPostProcessQueryFields() != null) {
								for (W5QueryField qf : queryResult.getPostProcessQueryFields()) {
									qf.setTabOrder((short) (++maxTabOrder));
									newQueryFields.add(qf);
								}
							}
							if (queryResult.getQuery().getShowParentRecordFlag() != 0)
								maxTabOrder++;

							queryResult.setNewQueryFields(newQueryFields);
						}

						Object[] o = new Object[maxTabOrder];
						for (W5QueryField field : newQueryFields)
							if (!errorFieldSet.contains(field.getDsc()))
								try {
									Object obj = rs.getObject(field.getDsc());
									if (obj != null) {
										if (obj instanceof java.sql.Timestamp) {
											try {
												obj = (queryResult.getQuery().getQueryType() == 2
														&& field.getFieldType() == 2) ? (java.sql.Timestamp) obj
																: GenericUtil.uFormatDateTime((java.sql.Timestamp) obj, dateFormat);
											} catch (Exception e) {
												obj = "java.sql.Timestamp";
											}
										} else if (obj instanceof java.sql.Date) {
											try {
												obj = (queryResult.getQuery().getQueryType() == 2
														&& field.getFieldType() == 2) ? rs.getTimestamp(field.getDsc())
																: GenericUtil.uFormatDateTime(
																		rs.getTimestamp(field.getDsc()), dateFormat);
											} catch (Exception e) {
												obj = "java.sql.Date";
											}
										} else if (obj instanceof Boolean) {
											obj = (Boolean) obj ? 1 : 0;
										}
									}
									o[field.getTabOrder() - 1] = obj;
								} catch (Exception ez) {
									if (FrameworkSetting.debug)
										throw ez;
									errorFieldSet.add(field.getDsc());
								}
						if (query.getDataFillDirectionType() != 0)
							resultData.add(0, o);
						else
							resultData.add(o);
						if (liveSyncKeys != null)
							liveSyncKeys.add(GenericUtil.uInt(o[query.getQueryType() == 3 ? 1 : 0]));
					}
					if (queryResult.getFetchRowCount() == 0 && resultData != null) {
						queryResult.setResultRowCount(resultData.size());
					}
					queryResult.setData(resultData);
					if(aggResultMap!=null) {
						if(queryResult.getExtraOutMap()==null)queryResult.setExtraOutMap(aggResultMap);
						else queryResult.getExtraOutMap().putAll(aggResultMap);
					}

					if (rs != null)
						rs.close();
					if (s != null)
						s.close();
					if (FrameworkSetting.hibernateCloseAfterWork)
						if (conn != null)
							conn.close();
				}

			});
			// } catch(IWBException pe){error = pe.getMessage();throw pe;
		} catch (Exception e) {
			error = e.getMessage();
			throw new IWBException("sql", "Query", queryResult.getQueryId(),
					GenericUtil.replaceSql(sql2.length() == 0 ? queryResult.getExecutedSql() : sql2.toString(),
							queryResult.getSqlParams()),
					"[8," + queryResult.getQueryId() + "] " + queryResult.getQuery().getDsc(), e);
		} finally {
			logQueryAction(queryAction, queryResult, error);
		}
	}

	public Map<String, Object> runSQLQuery2Map(String code, Map<String, Object> scd, Map<String, String> requestParams,
			Map<String, Object> obj) {
		Object[] oz = DBUtil.filterExt4SQL(code, scd, requestParams, obj);
		return runSQLQuery2Map(oz[0].toString(), (List) oz[1], null);
	}

	public Map<String, Object> runSQLQuery2Map(String sql, List params, List<W5QueryField> queryFields) {
		return runSQLQuery2Map(sql, params, queryFields, true);
	}

	public List runQuery2Map(Map<String, Object> scd, int queryId, Map<String, String> requestParams) {
		W5QueryResult queryResult = metadataLoader.getQueryResult(scd, queryId);
		queryResult.setErrorMap(new HashMap());
		queryResult.setRequestParams(requestParams);

		if (queryResult.getQuery().getQuerySourceType() == 0) { // if JavaScript
			scriptEngine.executeQueryAsScript(queryResult, null);
			return queryResult.getData();
		}

		if (queryId != 1 && queryId != 824 && queryResult.getMainTable() != null && (!FrameworkSetting.debug
				|| (scd.get("roleId") != null && GenericUtil.uInt(scd.get("roleId")) != 0))) {
			W5Table t = queryResult.getMainTable();
			if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
					t.getAccessViewRoles(), t.getAccessViewUsers())) {
				throw new IWBException("security", "Query", queryId, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_table_control_view"),
						null);
			}
		}
		/*
		 * StringBuilder tmpx = new StringBuilder(
		 * "ali baba ${obj.dsc} ve 40 haramiler ${lnk.pk_query_field_id.dsc} olmus" );
		 * dao.interprateTemplate(scd, 5,1294, tmpx, true);
		 */


		//
		// queryResult.setOrderBy(PromisUtil.uStrNvl(requestParams.get(PromisUtil.uStrNvl(PromisSetting.appSettings.get("sql_sort"),"sort")),
		// queryResult.getQuery().getSqlOrderby()));
		if (!GenericUtil.isEmpty(requestParams.get("sort"))) {
			if (requestParams.get("sort").equals(FieldDefinitions.queryFieldName_Comment)) {
				// queryResult.setOrderBy("coalesce((select qz.last_comment_id
				// from iwb.w5_comment_summary
				// qz where qz.table_id= AND qz.table_pk= AND
				// qz.customization_id=),0) DESC");
				queryResult.setOrderBy(FieldDefinitions.queryFieldName_Comment); // +
																					// "
																					// "
																					// +
																					// requestParams.get("dir")
			} else if (!requestParams.get("sort").contains("_qw_")) {
				queryResult.setOrderBy(requestParams.get("sort"));
				if (requestParams.get("dir") != null) {
					if (queryResult.getMainTable() != null)
						for (W5QueryField f : queryResult.getQuery().get_queryFields())
							if (queryResult.getOrderBy().equals(f.getDsc())) {
								if (f.getMainTableFieldId() != 0 && queryResult.getMainTable().get_tableFieldMap()
										.containsKey(f.getMainTableFieldId())) {
									queryResult.setOrderBy("x." + queryResult.getOrderBy());
								}
								break;
							}
					// queryResult.setOrderBy(((!queryResult.getQuery().getSqlFrom().contains(",")
					// &&
					// !queryResult.getQuery().getSqlFrom().contains("join") &&
					// queryResult.getQuery().getSqlFrom().contains(" x")) ?
					// "x." : "") +
					// queryResult.getOrderBy() + " " +
					// requestParams.get("dir"));
					queryResult.setOrderBy(queryResult.getOrderBy() + " " + requestParams.get("dir"));
				}
			} else
				queryResult.setOrderBy(queryResult.getQuery().getSqlOrderby());
		} else
			queryResult.setOrderBy(queryResult.getQuery().getSqlOrderby());
		switch (queryResult.getQuery().getQueryType()) {
		case 9:
		case 10:
		case 12:
		case 13:
			queryResult.prepareTreeQuery(null);
			break;
		case 15:
			queryResult.prepareDataViewQuery(null);
			break;
		default:
			queryResult.prepareQuery(null);
		}
		List l = null;
		if (queryResult.getErrorMap().isEmpty()) {
			queryResult.setFetchRowCount(
					GenericUtil.uIntNvl(requestParams, "limit", GenericUtil.uInt(requestParams, "firstLimit")));
			queryResult.setStartRowNumber(GenericUtil.uInt(requestParams, "start"));
			l = executeSQLQuery2Map(queryResult.getExecutedSql(), queryResult.getSqlParams());
		}

		return l;
	}

	public Map<String, Object> runSQLQuery2Map(final String sql, final List params,
			final List<W5QueryField> queryFields, final boolean closeConnectionAfterRun) {
		try {
			return getCurrentSession().doReturningWork(new ReturningWork<Map<String, Object>>() {

				public Map<String, Object> execute(Connection conn) throws SQLException {
					PreparedStatement s = conn.prepareStatement("select x.* from (" + sql + ")  x  limit 1");
					if (params != null)
						applyParameters(s, params);
					ResultSet rs = s.executeQuery();
					Map<String, Object> result = null;
					if (rs.next()) {
						result = new HashMap<String, Object>();
						if (queryFields == null || queryFields.size() == 0) {
							ResultSetMetaData rsm = rs.getMetaData();
							for (int columnIndex = 1; columnIndex <= rsm.getColumnCount(); columnIndex++) {
								String columnName = rsm.getColumnName(columnIndex)
										.toLowerCase(FrameworkSetting.appLocale);
								Object obj = rs.getObject(columnIndex);
								if (obj == null)
									continue;
								if (obj instanceof java.sql.Timestamp) {
									try {
										result.put(columnName, GenericUtil.uFormatDateTime((java.sql.Timestamp) obj));
									} catch (Exception e) {
									}
								} else if (obj instanceof java.sql.Date) {
									try {
										obj = rs.getTimestamp(columnIndex);
										result.put(columnName, GenericUtil.uFormatDateTime((java.sql.Timestamp) obj));
									} catch (Exception e) {
										obj = rs.getObject(columnIndex);
										try {
											result.put(columnName, GenericUtil.uFormatDateSade((java.sql.Date) obj));
										} catch (Exception e2) {
										}
									}
								} else
									result.put(columnName, obj);
							}
						} else
							for (W5QueryField f : queryFields) {
								Object obj = rs.getObject(f.getDsc());
								if (obj == null)
									continue;
								String columnName = f.getDsc();
								if (obj instanceof java.sql.Timestamp) {
									try {
										result.put(columnName,
												GenericUtil.uFormatDateTimeSade((java.sql.Timestamp) obj));
									} catch (Exception e) {
									}
								} else if (obj instanceof java.sql.Date) {
									try {
										result.put(columnName, GenericUtil.uFormatDateSade((java.sql.Date) obj));
									} catch (Exception e) {
									}
								} else {
									Object o = GenericUtil.getObjectByTip(obj.toString(), f.getFieldType());
									switch (f.getPostProcessType()) {
									case 9: // _fieldName
										result.put("_" + columnName, o);
										break;
									case 2: // locale
										result.put(columnName, LocaleMsgCache.get2(0,
												FrameworkCache.getAppSettingStringValue(0, "locale"), (String) o)); // TODO.
																													// burasi
																													// scd'deki
																													// degerler
																													// olacak
										break;
									default:
										result.put(columnName, o);
									}
								}
							}
					}
					if (rs != null)
						rs.close();
					if (s != null)
						s.close();
					if (FrameworkSetting.hibernateCloseAfterWork)
						if (closeConnectionAfterRun && conn != null)
							conn.close();
					return result;
				}
			});
			// s = conn.prepareStatement("select x.* from ("+sql+") x where
			// rownum=1");

		} catch (Exception e) {
			// if(FrameworkSetting.debug)e.printStackTrace();
			// logException(PromisUtil.replaceSql(sql, params)+"\n"+
			// e.getMessage(),PromisUtil.uInt(PromisCache.appSettings.get(0).get("default_customization_id")),0);
			throw new IWBException("sql", "Custom.Query.Map", 0, GenericUtil.replaceSql(sql, params), "Error Executing",
					e);
		}
	}

	private void logQueryAction(Log5QueryAction action, W5QueryResult queryResult, String error) {
		if (queryResult.getQuery() == null || !FrameworkSetting.logQueryAction)
			return;
		action.calcProcessTime();

		queryResult.setProcessTime(action.getProcessTime());
		if (queryResult.getQuery().getLogLevelType() == 2 || (queryResult.getQuery().getLogLevelType() == 1
				&& FrameworkSetting.logQueryActionMinTime <= action.getProcessTime())) {
			if (FrameworkSetting.log2tsdb) {
				action.setDsc(GenericUtil.replaceSql(queryResult.getExecutedSql(), queryResult.getSqlParams()));
			} else {
				action.setDsc(GenericUtil.uStrMax(
						GenericUtil.replaceSql(queryResult.getExecutedSql(), queryResult.getSqlParams()), 3999));
			}
			saveObject(action);
		}
	}

	public void logGlobalFuncAction(Log5GlobalFuncAction action, W5GlobalFuncResult fr, String error) {
		if (!FrameworkSetting.log2tsdb && (fr.getGlobalFunc().getLogLevelType() == 0 || FrameworkCache.getAppSettingIntValue(fr.getScd(), "log_db_func_action") == 0))
			return;
		action.calcProcessTime();
		if (FrameworkSetting.log2tsdb  || (fr.getGlobalFunc().getLogLevelType() == 1) || (fr.getGlobalFunc().getLogLevelType() == 2
				&& FrameworkCache.getAppSettingIntValue(fr.getScd(), "log_db_func_action") != 0 && FrameworkCache
						.getAppSettingIntValue(fr.getScd(), "log_db_func_action_mintime") <= action.getProcessTime())) {
			action.setDsc(GenericUtil.replaceSql(fr.getExecutedSql(), fr.getSqlParams()));
			saveObject(action);
		}
	}

	public void loadFormCellLookups(Map<String, Object> scd, List<W5FormCellHelper> formCellResults,
			Map<String, String> requestParams, String tabId) {
		String includedValues;
		String projectId = (String) scd.get("projectId");
		// W5Customization cus =
		// FrameworkCache.wCustomizationMap.get(customizationId);
		checkTenant(scd);
		for (W5FormCellHelper rc : formCellResults)
			try {
				W5FormCell c = rc.getFormCell();
				if (c.getActiveFlag() == 0)
					continue;
				includedValues = c.getLookupIncludedValues();
				Map<String, String> paramMap = new HashMap<String, String>();
				Set<Integer> keys = null;
				switch (c.getControlType()) {
				case 100: // button
					if (c.getInitialSourceType() == 4) { // sql ise
						rc.setExtraValuesMap(runSQLQuery2Map(
								GenericUtil.filterExt(c.getInitialValue(), scd, requestParams, null).toString(), null,
								null));
					}
					break;
				case 71:// file attachment
					int fileId = GenericUtil.uInt(rc.getValue());
					if (fileId != 0) {
						List params = new ArrayList();
						params.add(fileId);
						if(FrameworkCache.getTable(scd, FrameworkSetting.customFileTableId)==null) {
							params.add(scd.get("projectId"));
							rc.setExtraValuesMap(runSQLQuery2Map(
									"select x.file_attachment_id id, x.original_file_name dsc, x.file_size fsize from iwb.w5_file_attachment x where x.file_attachment_id=? AND x.project_uuid=?",
									params, null));
						} else rc.setExtraValuesMap(runSQLQuery2Map(
								"select x.file_id id, x.dsc, x.file_size fsize from x_file x where x.file_id=?", params, null));
 
					}
					break;
				case 60: // remote superboxselect
				case 16: // remote query
				case 9: // remote query
					rc.setLookupQueryResult(metadataLoader.getQueryResult(scd, c.getLookupQueryId()));
					// c.set_lookupListCount(c.getLookupQueryId()); // Fake:
					// Normalde Query Id tutulur, ama
					// su anda kac adet column tutuyor
					break;

				case 58: // superboxselect
				case 8: // lovcombo static
				case 6: // eger static combobox ise listeyi load et
					if(c.getLookupQueryId()==0)
						throw new IWBException("framework", "LookUp", 0, null, "LookUp Static not defined for FormElement ["+c.getDsc()+"]", null);
					W5LookUp lookUp = FrameworkCache.getLookUp(scd, c.getLookupQueryId(), "Form(" + c.getFormId() + ")."
							+ c.getDsc() + "-> LookUp not found: " + c.getLookupQueryId());
					rc.setLocaleMsgFlag((short) 1);
					List<W5LookUpDetay> oldList = !FrameworkCache.hasQueuedReloadCache(projectId,
							"13." + lookUp.getLookUpId())
									? lookUp.get_detayList()
									: metadataLoader.findLookUpDetay(c.getLookupQueryId(), projectId);

					List<W5LookUpDetay> newList = null;
					if (includedValues != null && includedValues.length() > 0) {
						boolean notInFlag = false;
						if (includedValues.charAt(0) == '!') {
							notInFlag = true;
							includedValues = includedValues.substring(1);
						}
						String[] ar1 = includedValues.split(",");
						newList = new ArrayList<W5LookUpDetay>(oldList.size());
						for (W5LookUpDetay p : oldList)
							if ((rc.getValue() != null && p.getVal().equals(rc.getValue())) || p.getActiveFlag() != 0) {
								boolean in = false;
								for (int it4 = 0; it4 < ar1.length; it4++)
									if (ar1[it4].equals(p.getVal())) {
										in = true;
										break;
									}
								if (in ^ notInFlag)
									newList.add(p);
							}
					} else if (requestParams.get("_lsc" + c.getFormCellId()) != null) {
						String[] lsc = requestParams.get("_lsc" + c.getFormCellId()).split(",");
						newList = new ArrayList<W5LookUpDetay>();
						for (String q : lsc) {
							newList.add(lookUp.get_detayMap().get(q));
						}
					} else {
						newList = new ArrayList<W5LookUpDetay>(oldList.size());
						for (W5LookUpDetay p : oldList)
							if ((rc.getValue() != null && p.getVal().equals(rc.getValue())) || p.getActiveFlag() != 0)
								newList.add(p);
						// newList = lookUp.get_detayList();
					}
					List<W5LookUpDetay> newList2 = new ArrayList<W5LookUpDetay>(newList.size());
					if (tabId != null)
						keys = UserUtil.getTableGridFormCellCachedKeys((String) scd.get("projectId"),
								-c.getLookupQueryId(), (Integer) scd.get("userId"), (String) scd.get("sessionId"),
								requestParams.get(".w"), tabId, -c.getFormCellId(), requestParams, false);
					for (W5LookUpDetay ld : newList) {
						newList2.add(ld);
					}
					rc.setLookupListValues(newList2);
					break;
				case 10:
				case 61: // advanced select, advancedselect w/ button
					paramMap.put("xid", rc.getValue());
				case 7:
				case 15:
				case 59: // dynamic query, lovcombo, superbox
				case 23:
				case 24:
				case 26:
				case 55: // tree combo and treepanel
					if(c.getLookupQueryId()==0)
						throw new IWBException("framework", "Query", 0, null, "LookUp Query not defined for FormElement ["+c.getDsc()+"]", null);
					String includedParams = GenericUtil.filterExt(c.getLookupIncludedParams(), scd, requestParams, null)
							.toString();
					if (includedParams != null && includedParams.length() > 2) {
						String[] ar1 = includedParams.split("&");
						for (int it4 = 0; it4 < ar1.length; it4++) {
							String[] ar2 = ar1[it4].split("=");
							if (ar2.length == 2 && ar2[0] != null && ar2[1] != null)
								paramMap.put(ar2[0], ar2[1]);
						}
					}

					W5QueryResult lookupQueryResult = metadataLoader.getQueryResult(scd, c.getLookupQueryId());
					lookupQueryResult.setErrorMap(new HashMap());
					lookupQueryResult.setRequestParams(requestParams);
					lookupQueryResult.setOrderBy(lookupQueryResult.getQuery().getSqlOrderby());
					if (lookupQueryResult.getQuery().getQuerySourceType() != 15)
						switch (lookupQueryResult.getQuery().getQuerySourceType()) {
						case 1376: // WS Method
							W5WsMethod wsm = FrameworkCache.getWsMethod(projectId,
									lookupQueryResult.getQuery().getSourceObjectId());

							W5WsMethodParam parentParam = null;
							for (W5WsMethodParam px : wsm.get_params())
								if (px.getOutFlag() != 0 && px.getParamType() == 10) {
									parentParam = px;
									break;
								}
							Map<String, String> m2 = new HashMap();
							if (requestParams.get("filter[value]") != null) {
								requestParams.put("xdsc", requestParams.get("filter[value]"));
								requestParams.remove("filter[value]");
							}
							for (W5QueryParam qp : lookupQueryResult.getQuery().get_queryParams())
								if (!GenericUtil.isEmpty(requestParams.get(qp.getDsc()))) {
									m2.put(qp.getExpressionDsc(), requestParams.get(qp.getDsc()));
								}
							StringBuilder rc2 = new StringBuilder();
							rc2.append("function _x_(x){\nreturn {").append(lookupQueryResult.getQuery().getSqlSelect())
									.append("\n}}\nvar result=[], q=$.REST('")
									.append(wsm.get_ws().getDsc() + "." + wsm.getDsc()).append("',")
									.append(GenericUtil.fromMapToJsonString2(m2))
									.append(");\nif(q && q.get('success')){q=q.get('").append(parentParam.getDsc())
									.append("');for(var i=0;i<q.size();i++)result.push(_x_(q.get(i)));}");
							scriptEngine.executeQueryAsScript(lookupQueryResult, rc2.toString());
							rc.setLookupQueryResult(lookupQueryResult);
							continue;
						default:
							rc.setLookupQueryResult(lookupQueryResult);
							continue; // burda sadece table icin olur
						}
					if (rc.getValue() != null && rc.getValue().length() > 0
							&& GenericUtil.hasPartInside("7,10,61", Short.toString(c.getControlType())))
						paramMap.put("pmust_load_id", rc.getValue());
					switch (lookupQueryResult.getQuery().getQueryType()) {
					case 12:
//					case 13:
						lookupQueryResult.prepareTreeQuery(paramMap);
						break; // lookup tree query
					default:
						lookupQueryResult.prepareQuery(paramMap);
					}
					rc.setLookupQueryResult(lookupQueryResult);
					if (c.getControlType() == 10 || c.getControlType() == 23 || c.getControlType() == 7) {
						if (c.getDialogGridId() != 0) {
							if (rc.getExtraValuesMap() == null)
								rc.setExtraValuesMap(new HashMap());
							rc.getExtraValuesMap().put("dialogGrid",
									metadataLoader.getGridResult(scd, c.getDialogGridId(), requestParams, true));
						}

						if (c.getControlType() == 10 && GenericUtil.isEmpty(rc.getValue()))
							break; // advanced select ise ve degeri yoksa
									// hicbirsey koyma
					}

					if (lookupQueryResult.getErrorMap().isEmpty()) {
						runQuery(lookupQueryResult);
						if (tabId != null && lookupQueryResult.getQuery().getSourceObjectId() != 0
								&& requestParams.get(".w") != null) {
							keys = UserUtil.getTableGridFormCellCachedKeys((String) scd.get("projectId"),
									lookupQueryResult.getQuery().getSourceObjectId(), (Integer) scd.get("userId"),
									(String) scd.get("sessionId"), requestParams.get(".w"), tabId, -c.getFormCellId(),
									requestParams, true);
							if (keys != null)
								for (Object[] o : lookupQueryResult.getData())
									keys.add(GenericUtil.uInt(o[1]));
						}
						if (paramMap.get("xapproval_id") != null && c.getLookupQueryId() == 606) { // onaylanmis
																									// ve
																									// reddedildiyi
																									// koy
							W5Workflow ta = FrameworkCache.getWorkflow(projectId,
									GenericUtil.uInt(paramMap.get("xapproval_id")));
							if (ta.getApprovalRequestTip() != 1) { // gercek
																	// veri
																	// uzerinde?
								lookupQueryResult
										.getData().add(0,
												new Object[] {
														LocaleMsgCache.get2(scd, ta.getApprovalRequestMsg()),
														901 });
							}
							if (ta.getOnRejectTip() == 1) { // make status
															// rejected
								lookupQueryResult
										.getData().add(
												new Object[] {
														LocaleMsgCache.get2(scd, ta.getRejectedMsg()),
														999 });
							}
						}
					}
					// paramMap.clear();
				}
			} catch (Exception e) {
				throw new IWBException("framework", "Load.FormElement", rc.getFormCell().getFormCellId(), null,
						"[41," + rc.getFormCell().getFormCellId() + "]", e);
			}
	}

	public void loadFormTable(final W5FormResult formResult) {
		W5Form f = formResult.getForm();
		final W5Table t = FrameworkCache.getTable(formResult.getScd(), f.getObjectId());
		final StringBuilder sql = new StringBuilder();
		sql.append("select ");
		String log5LogId = null; // logdan gosterilecekse
		if (formResult.getRequestParams() != null && formResult.getRequestParams().containsKey("_log5_log_id")) {
			log5LogId = formResult.getRequestParams().get("_log5_log_id");
		}
		if (formResult.getFormCellResults() == null)
			formResult.setFormCellResults(new ArrayList<W5FormCellHelper>(formResult.getForm().get_formCells().size()));
		for (W5FormCell cell : formResult.getForm().get_formCells()) {
			if (cell.getObjectDetailId() != 0) {
				W5TableField tf = ((W5TableField) cell.get_sourceObjectDetail());
				if (tf != null && tf.getTabOrder() > 0) {
					if (tf.getAccessViewTip() != 0/* && tf.getAccessViewUserFields() != null*/) {
						if ((!GenericUtil.accessControl(formResult.getScd(), tf.getAccessViewTip(),
								tf.getAccessViewRoles(), tf.getAccessViewUsers())
								&& (GenericUtil.isEmpty(tf.getAccessViewUserFields())
										|| accessUserFieldControl(t, tf.getAccessViewUserFields(), formResult.getScd(),
												formResult.getRequestParams(), null))))
							continue;
					}
					if (!GenericUtil.accessControl4SessionField(formResult.getScd(), tf.getRelatedSessionField()))
						continue;
					if (formResult.getApprovalStep() != null
							&& !GenericUtil.isEmpty(formResult.getApprovalStep().getVisibleFields())
							&& !GenericUtil.hasPartInside2(formResult.getApprovalStep().getVisibleFields(),
									tf.getTableFieldId())) {
						continue;
					}

					W5FormCellHelper result = new W5FormCellHelper(cell);
					formResult.getFormCellResults().add(result);
					if (log5LogId != null) {
						sql.append("t.").append(((W5TableField) cell.get_sourceObjectDetail()).getDsc()).append(",");
					} else {
						sql.append("t.").append(((W5TableField) cell.get_sourceObjectDetail()).getDsc()).append(",");
					}
				}
			} else {
				W5FormCellHelper result = new W5FormCellHelper(cell);
				formResult.getFormCellResults().add(result);
			}
		}
		sql.replace(sql.length() - 1, sql.length(), " from ");
		if (log5LogId != null) {
			sql.append(FrameworkSetting.crudLogSchema).append(".");
		}
		sql.append(t.getDsc()).append(" t");
		boolean b = false;
		sql.append(" where ");
		final List<Object> realParams = new ArrayList<Object>();
		Object pkField = null;
		if (log5LogId != null) {
			sql.append(" t.log5_log_id = ? ");
			realParams.add(GenericUtil.uInt(log5LogId));
			formResult.getPkFields().put("log5_log_id", log5LogId);
		} else {

			for (W5TableParam x : t.get_tableParamList()) {
				if (b) {
					sql.append(" AND ");
				} else
					b = true;
				sql.append("t.").append(x.getExpressionDsc()).append(" = ? ");
				Object presult = GenericUtil.prepareParam((W5Param) x, formResult.getScd(),
						formResult.getRequestParams(), (short) -1, null, (short) 1, null, null,
						formResult.getErrorMap());
				if (pkField == null)
					pkField = presult;
				realParams.add(presult);
				formResult.getPkFields().put(x.getDsc(), presult);
			}
		}

		final Object pkField2 = pkField;
		final int dateFormat = formResult.getScd()!=null ? GenericUtil.uInt(formResult.getScd().get("date_format")):0;
		try {
			getCurrentSession().doWork(new Work() {

				public void execute(Connection conn) throws SQLException {
					PreparedStatement s = conn.prepareStatement(sql.toString());
					applyParameters(s, realParams);
					ResultSet rs = s.executeQuery();
					if (!rs.next())
						throw new IWBException("sql", "Form.Load", formResult.getFormId(),
								GenericUtil.replaceSql(sql.toString(), realParams), "No Record Found", null);

					for (W5FormCellHelper cellResult : formResult.getFormCellResults())
						if (cellResult.getFormCell().getObjectDetailId() != 0) {
							W5TableField tf = (W5TableField) cellResult.getFormCell().get_sourceObjectDetail();
							Object obj = rs.getObject((tf).getDsc());
							if (obj != null) {
								if(tf.getLkpEncryptionType()!=0)obj = EncryptionUtil.decrypt(obj.toString(), tf.getLkpEncryptionType());

								if (tf.getFieldType() == 5 && obj instanceof Boolean) {
									obj = (Boolean) obj ? 1 : 0;
								} else if (tf.getFieldType() == 2 && obj instanceof java.sql.Timestamp) {
									try {
										obj = GenericUtil.uFormatDateTime((java.sql.Timestamp) obj, dateFormat);
									} catch (Exception e) {
									}
								} else if (tf.getFieldType() == 2 && obj instanceof java.sql.Date) {
									try {
										if (cellResult.getFormCell().getControlType() == 18) { // date
																								// time
											obj = rs.getTimestamp(
													((W5TableField) cellResult.getFormCell().get_sourceObjectDetail())
															.getDsc());
											obj = GenericUtil.uFormatDateTime((java.sql.Timestamp) obj, dateFormat);
										} else // date
											obj = GenericUtil.uFormatDate((java.util.Date) obj, dateFormat);
									} catch (Exception e) {
									}
								}
								if (obj != null)cellResult.setValue(obj.toString());
							}
							if (tf.getAccessMaskTip() != 0
									&& !GenericUtil.accessControl(formResult.getScd(), tf.getAccessMaskTip(), tf.getAccessMaskRoles(),
											tf.getAccessMaskUsers())
									&& (GenericUtil.isEmpty(tf.getAccessMaskUserFields()) || accessUserFieldControl(t,
											tf.getAccessMaskUserFields(), formResult.getScd(), formResult.getRequestParams(), null))) {
								cellResult.setHiddenValue("*");
								String strMask = FrameworkCache.getAppSettingStringValue(0, "data_mask", "**********");
								String sobj = cellResult.getValue();
								if(GenericUtil.isEmpty(sobj)) sobj = "x";
								switch(tf.getAccessMaskTip()) {
								case	1://full
									cellResult.setValue(strMask);break;
								case	2://beginning
									cellResult.setValue(sobj.charAt(0)+strMask.substring(1));break;
								case	3://beg + end
									cellResult.setValue(sobj.charAt(0)+strMask.substring(2)+sobj.charAt(sobj.length()-1));break;
								}
							}
						} else if (cellResult.getFormCell().getControlType() == 101) {
							switch (cellResult.getFormCell().getInitialSourceType()) {
							case 0: // yok-sabit
								cellResult.setValue(cellResult.getFormCell().getInitialValue());
								break;
							case 1: // request
								cellResult.setValue(
										formResult.getRequestParams().get(cellResult.getFormCell().getInitialValue()));
								break;
							case 2:
								Object o = formResult.getScd().get(cellResult.getFormCell().getInitialValue());
								cellResult.setValue(o == null ? null : o.toString());
								break;
							case 3: // app_setting
								cellResult.setValue(FrameworkCache.getAppSettingStringValue(formResult.getScd(),
										cellResult.getFormCell().getInitialValue()));
								break;
							case 4: // SQL
								Object[] oz = DBUtil.filterExt4SQL(cellResult.getFormCell().getInitialValue(),
										formResult.getScd(), formResult.getRequestParams(), null);
								if (oz[1] != null && ((List) oz[1]).size() > 0) {
									Map<String, Object> m = runSQLQuery2Map(oz[0].toString(), (List) oz[1], null,
											false);
									if (m != null)
										cellResult.setValue(m.values().iterator().next().toString());
								} else {
									List l = executeSQLQuery(oz[0].toString());
									if (l != null && l.size() > 0 && l.get(0) != null)
										cellResult.setValue(l.get(0).toString());
								}
								if (GenericUtil.isEmpty(cellResult.getValue()))
									cellResult.setValue(" ");
								break;
							case 5: // CustomJS(Rhino)
								Object res = scriptEngine.executeScript(formResult.getScd(),
										formResult.getRequestParams(), cellResult.getFormCell().getDefaultValue(), null,
										"41d" + cellResult.getFormCell().getFormCellId());

//									if (res != null && res instanceof org.mozilla.javascript.Undefined)res = null;
								if (res != null && ((W5Param) cellResult.getFormCell().get_sourceObjectDetail())
										.getParamType() == 4)
									res = "" + new BigDecimal(res.toString()).intValue();
								cellResult.setValue(res == null ? null : res.toString());

								break;
							}
						}
					if (rs.next()) // fazladan kayit geldi
						throw new IWBException("sql", "Table", t.getTableId(),
								GenericUtil.replaceSql(sql.toString(), realParams),
								"[15," + t.getTableId() + "] Loaded more then 1 record", null);
					if (rs != null)
						rs.close();
					if (s != null)
						s.close();

					if (pkField2 != null) {
						int extraSqlCount = 0;
						StringBuilder extraSql = new StringBuilder();
						extraSql.append("select ");
						Set<String> extrInfoSet = new HashSet();
						if (FrameworkCache.getAppSettingIntValue(formResult.getScd(), "file_attachment_flag") != 0
								&& t.getFileAttachmentFlag() != 0) {
							if(FrameworkCache.getTable(formResult.getScd(), FrameworkSetting.customFileTableId)==null) 
								extraSql.append(
										"(select count(1) cnt from iwb.w5_file_attachment x where x.project_uuid=?::text AND x.table_id=?::integer AND x.table_pk=?::text) file_attach_count");
							else 
								extraSql.append(
										"(select count(1) cnt from x_file x where ?::text is not null AND x.table_id=?::integer AND x.table_pk=?::integer) file_attach_count");
							extraSqlCount++;
							extrInfoSet.add("file_attach_count");
						}
						if (FrameworkCache.getAppSettingIntValue(formResult.getScd(), "make_comment_flag") != 0
								&& t.getMakeCommentFlag() != 0) {
							if (extraSql.length() > 10)
								extraSql.append(",");
							if (FrameworkCache.getTable(formResult.getScd(), FrameworkSetting.customCommentTableId)==null) {
								extraSql.append(
										"(select count(1) cnt from iwb.w5_comment x where x.project_uuid=?::text AND x.table_id=?::integer AND x.table_pk=?::integer) comment_count");
							} else {
								extraSql.append(
										"(select count(1) cnt from x_comment x where ?::text is not null AND x.table_id=?::integer AND x.table_pk=?::integer) comment_count");
							}
							extrInfoSet.add("comment_count");
							extraSqlCount++;
						}
						/*
						 * if(FrameworkCache.getAppSettingIntValue(formResult. getScd(),
						 * "row_based_security_flag")!=0 &&
						 * (Integer)formResult.getScd().get("userTip")!=3 && t.getAccessTips()!=null &&
						 * t.getAccessTips().length()>0){ if(extraSql.length()>10)extraSql.append(",");
						 * extraSql.append(
						 * "(select count(1) cnt from iwb.w5_access_control x where x.customization_id=? AND x.table_id=? AND x.table_pk=?) access_count"
						 * ); extrInfoSet.add("access_count"); extraSqlCount++; }
						 */

						if (extraSql.length() > 10) {
							s = conn.prepareStatement(extraSql.append(" ").toString());
							List<Object> params = new ArrayList(extraSqlCount * 3 + 1);
							for (int qi = 0; qi < extraSqlCount; qi++) {
								params.add(formResult.getScd().get("projectId"));
								params.add(t.getTableId());
								params.add(pkField2);
							}
							applyParameters(s, params); // romisUtil.replaceSql(extraSql.toString(),params)
							rs = s.executeQuery();
							if (rs.next()) {
								if (extrInfoSet.contains("file_attach_count"))
									formResult.setFileAttachmentCount(
											GenericUtil.uInt(rs.getObject("file_attach_count")));
								if (extrInfoSet.contains("comment_count")) {
									formResult.setCommentCount(GenericUtil.uInt(rs.getObject("comment_count")));
								} else if (extrInfoSet.contains("comment_extra")) {
									formResult.setCommentExtraInfo((String) rs.getObject("comment_extra"));
								}
								// if(extrInfoSet.contains("access_count"))formResult.setAccessControlCount(GenericUtil.uInt(rs.getObject("access_count")));
							}
							if (rs != null)
								rs.close();
							if (s != null)
								s.close();
						}
					}
					if (FrameworkSetting.hibernateCloseAfterWork)
						if (conn != null)
							conn.close();
				}
			});

		} catch (Exception e) {
			throw new IWBException("framework", "FetchRecord.Form", formResult.getFormId(), null,
					"[40," + formResult.getFormId() + "] Form Load Exception", e);
		} finally {
			// session.close();
		}
	}

	public String getInitialFormCellValue(Map<String, Object> scd, W5FormCell cell, Map<String, String> requestParams) {
		String result = null;
		switch (cell.getInitialSourceType()) {
		case 0: // yok-sabit
			result = (cell.getInitialValue());
			break;
		case 1: // request
			result = (requestParams.get(cell.getInitialValue()));
			break;
		case 2:
			Object o = scd.get(cell.getInitialValue());
			result = (o == null ? null : o.toString());
			break;
		case 3: // app_setting
			result = (FrameworkCache.getAppSettingStringValue(scd, cell.getInitialValue()));
			break;
		case 4: // SQL
			Object[] oz = DBUtil.filterExt4SQL(cell.getInitialValue(), scd, requestParams, null);
			Map<String, Object> m = runSQLQuery2Map(oz[0].toString(), (List) oz[1], null);
			if (m != null)
				result = (m.values().iterator().next().toString());
			break;
		case 5: // CustomJS(Rhino)
			Object res = scriptEngine.executeScript(scd, requestParams, cell.getInitialValue(), null,
					"41i" + cell.getFormCellId());

			if (res != null && ((W5Param) cell.get_sourceObjectDetail()).getParamType() == 4)
				res = "" + new BigDecimal(res.toString()).intValue();
			result = (res == null ? null : res.toString());

			break;
		case 10: // approvalStates
			String selectedItems = "";
			W5Workflow app = FrameworkCache.getWorkflow(scd, GenericUtil.uInt(cell.getInitialValue()));
			for (W5WorkflowStep step : app.get_approvalStepList())
				if (GenericUtil.accessControl(scd, (short) 1, step.getApprovalRoles(), step.getApprovalUsers()))
					selectedItems += "," + step.getApprovalStepId();
			if (selectedItems.length() > 0)
				result = (selectedItems.substring(1));
		}
		return result;
	}

	public Map<String, String> interprateSmsTemplate(W5FormSmsMail fsm, Map<String, Object> scd,
			Map<String, String> requestParams, int fsmTableId, int fsmTablePk) {
		Map<String, String> m = new HashMap<String, String>();
		String phone = fsm.getSmsMailTo();
		if (phone != null && phone.contains("${")) {
			StringBuilder tmp1 = new StringBuilder(phone);
			interprateTemplate(scd, requestParams, fsmTableId, fsmTablePk, tmp1, true, 1, 0);
			phone = tmp1.toString();
		}
		m.put("phone", phone);
		String smsBody = fsm.getSmsMailBody();
		if (smsBody != null && smsBody.contains("${")) {
			StringBuilder tmp1 = new StringBuilder(smsBody);
			interprateTemplate(scd, requestParams, fsmTableId, fsmTablePk, tmp1, true, 0, 0);
			smsBody = tmp1.toString();
		}
		m.put("body", smsBody);
		return m;
	}

	public W5Email interprateMailTemplate(W5FormSmsMail fsm, Map<String, Object> scd, Map<String, String> requestParams,
			int fsmTableId, int fsmTablePk) {

		W5Email email = new W5Email();
		email.setMailSettingId(fsm.getMailSettingId());
		email.setTableId(fsmTableId);email.setTablePk(fsmTablePk);
		String mailTo = fsm.getSmsMailTo();
		if (mailTo != null && mailTo.contains("${")) {
			StringBuilder tmp1 = new StringBuilder(mailTo);
			interprateTemplate(scd, requestParams, fsmTableId, fsmTablePk, tmp1, true, 2, 0);
			mailTo = MailUtil.organizeMailAdress(tmp1.toString());
		}
		email.setMailTo(mailTo);
		String mailCc = fsm.getSmsMailCc();
		if (mailCc != null && mailCc.contains("${")) {
			StringBuilder tmp1 = new StringBuilder(mailCc);
			interprateTemplate(scd, requestParams, fsmTableId, fsmTablePk, tmp1, true, 2, 0);
			mailCc = MailUtil.organizeMailAdress(tmp1.toString());
		}
		email.setMailCc(mailCc);
		String mailBcc = fsm.getSmsMailBcc();
		if (mailBcc != null && mailBcc.contains("${")) {
			StringBuilder tmp1 = new StringBuilder(mailBcc);
			interprateTemplate(scd, requestParams, fsmTableId, fsmTablePk, tmp1, true, 2, 0);
			mailBcc = MailUtil.organizeMailAdress(tmp1.toString());
		}
		email.setMailBcc(mailBcc);
		String mailSubject = fsm.getSmsMailSubject();
		if (mailSubject != null && mailSubject.contains("${")) {
			StringBuilder tmp1 = new StringBuilder(mailSubject);
			interprateTemplate(scd, requestParams, fsmTableId, fsmTablePk, tmp1, true, 0, 0);
			mailSubject = tmp1.toString();
		}
		email.setMailSubject(mailSubject);
		String mailBody = fsm.getSmsMailBody();
		if (mailBody != null && mailBody.contains("${")) {
			StringBuilder tmp1 = new StringBuilder(mailBody);
			interprateTemplate(scd, requestParams, fsmTableId, fsmTablePk, tmp1, true, 0, 0);
			mailBody = tmp1.toString();
		}
		email.setMailBody(mailBody);
		return email;
	}

	public void initializeForm(W5FormResult formResult, boolean onlyFreeFields) {
		W5Form form = formResult.getForm();
		String projectId = FrameworkCache.getProjectId(formResult.getScd(), "40." + formResult.getFormId());
		W5Table t = null;
		switch (form.getObjectType()) {
		case 2:// table
			t = FrameworkCache.getTable(projectId, form.getObjectId());
			break; // table
		case 1:// grid
			W5Grid g = metadataLoader.getGridResult(formResult.getScd(), form.getObjectId(), new HashMap(), true)
					.getGrid();

			if (g != null) {
				W5Query q = metadataLoader.getQueryResult(formResult.getScd(), g.getQueryId()).getQuery();
				if (q != null)
					t = FrameworkCache.getTable(projectId, q.getSourceObjectId()); // grid
			}
			break;
		// case 3: t= PromisCache.getTable(formResult.getScd(),
		// form.getObjectId()); break;
		}
		if (formResult.getFormCellResults() == null)
			formResult.setFormCellResults(new ArrayList<W5FormCellHelper>(form.get_formCells().size()));
		if (form.get_formCells() != null)
			formResult.getExtraFormCells().addAll(0, form.get_formCells());
		for (W5FormCell cell : formResult.getExtraFormCells())
			if (!onlyFreeFields || cell.getObjectDetailId() == 0)
				try {
					if (t != null) {
						W5TableField tf = null;
						if (form.getObjectType() == 2 && cell.get_sourceObjectDetail() != null
								&& cell.get_sourceObjectDetail() instanceof W5TableField) {
							tf = (W5TableField) cell.get_sourceObjectDetail();
						} else if (form.getObjectType() == 1 && cell.get_sourceObjectDetail() != null) {
							if (cell.get_sourceObjectDetail() instanceof W5QueryParam) {
								W5QueryParam qp = (W5QueryParam) cell.get_sourceObjectDetail();
								if (qp.getRelatedTableFieldId() != 0 && t != null) {
									tf = t.get_tableFieldMap().get(qp.getRelatedTableFieldId());
								}
							} else if (cell.get_sourceObjectDetail() instanceof W5TableField) {
								tf = (W5TableField) cell.get_sourceObjectDetail();
							}
						}
						if (tf != null) {
							if (!GenericUtil.accessControl(formResult.getScd(), tf.getAccessInsertTip(),
									tf.getAccessInsertRoles(), tf.getAccessInsertUsers()))
								continue; // access control
							if (!GenericUtil.accessControl4SessionField(formResult.getScd(),tf.getRelatedSessionField()))
								continue;
						}
					}
					W5FormCellHelper result = new W5FormCellHelper(cell);
					switch (cell.getInitialSourceType()) {
					case 0: // yok-sabit
						result.setValue(cell.getInitialValue());
						break;
					case 1: // request
						result.setValue(formResult.getRequestParams().get(cell.getInitialValue()));
						break;
					case 2:
						Object o = formResult.getScd().get(cell.getInitialValue());
						result.setValue(o == null ? null : o.toString());
						break;
					case 3: // app_setting
						result.setValue(
								FrameworkCache.getAppSettingStringValue(formResult.getScd(), cell.getInitialValue()));
						break;
					case 4: // SQL
						Object[] oz = DBUtil.filterExt4SQL(cell.getInitialValue(), formResult.getScd(),
								formResult.getRequestParams(), null);
						if (oz[1] != null && ((List) oz[1]).size() > 0) {
							Map<String, Object> m = runSQLQuery2Map(oz[0].toString(), (List) oz[1], null);
							if (m != null && m.size() > 0)
								result.setValue(m.values().iterator().next().toString());
						} else {
							List l = executeSQLQuery(oz[0].toString());
							if (l != null && l.size() > 0 && l.get(0) != null)
								result.setValue(l.get(0).toString());
						}
						break;
					case 5: // CustomJS(Rhino)
						Object res = scriptEngine.executeScript(formResult.getScd(), formResult.getRequestParams(),
								cell.getInitialValue(), null, "41i" + cell.getFormCellId());

						if (res != null && ((W5Param) cell.get_sourceObjectDetail()).getParamType() == 4)
							res = "" + new BigDecimal(res.toString()).intValue();
						result.setValue(res == null ? null : res.toString());

						break;
					case 10: // approvalStates
						String selectedItems = "";
						W5Workflow app = FrameworkCache.getWorkflow(projectId,
								GenericUtil.uInt(cell.getInitialValue()));
						if (app != null)
							for (W5WorkflowStep step : app.get_approvalStepList())
								if (GenericUtil.accessControl(formResult.getScd(), (short) 1, step.getApprovalRoles(),
										step.getApprovalUsers()))
									selectedItems += "," + step.getApprovalStepId();
						if (selectedItems.length() > 0)
							result.setValue(selectedItems.substring(1));
					}
					formResult.getFormCellResults().add(result);
				} catch (Exception e) {
					throw new IWBException("framework", "Initialize FormElement", cell.getFormCellId(), null,
							"[41," + cell.getFormCellId() + "] " + cell.getDsc(), e);
				}
	}

	public boolean updateFormTable(final W5FormResult formResult, final String paramSuffix) {
		Map<String, Object> scd = formResult.getScd();
		W5Form f = formResult.getForm();
		final W5Table t = FrameworkCache.getTable(scd, f.getObjectId());
		if (FrameworkSetting.vcs && FrameworkSetting.vcsServer && t.getVcsFlag() != 0)
			throw new IWBException("vcs", "Form Update", formResult.getFormId(), null,
					"VCS Server not allowed to update VCS Table", null);
		final StringBuilder sql = new StringBuilder();
		sql.append("update ");
		sql.append(t.getDsc()).append(" set ");
		final List<Object> updateParams = new ArrayList<Object>();
		List<Object> whereParams = new ArrayList<Object>();
		Set<String> usedFields = new HashSet<String>();

		Map<Integer, W5FormModule> moduleMap = null;
		if (formResult.getForm().getRenderType() != 0) {
			moduleMap = new HashMap<Integer, W5FormModule>();
			if (formResult.getForm().get_moduleList() != null)
				for (W5FormModule m : formResult.getForm().get_moduleList())
					moduleMap.put(m.getFormModuleId(), m);
		}
		W5WorkflowStep workflowStep = formResult.getApprovalStep();
		/*
		 * if (formResult.getApprovalRecord() != null) { approvalStep =
		 * FrameworkCache.getWorkflow(scd,
		 * formResult.getApprovalRecord().getApprovalId())
		 * .get_approvalStepMap().get(formResult.getApprovalRecord().getApprovalStepId()
		 * ); }
		 */
		boolean b = false;
		boolean extendedFlag = false;
		for (W5FormCell x : f.get_formCells())
			if (x.getNrdType() != 1 && x.getObjectDetailId() != 0 && x.get_sourceObjectDetail() != null
					&& x.getControlType() < 100) { // normal ve readonly ise
				W5TableField tf = (W5TableField) x.get_sourceObjectDetail();
				if (tf.getCanUpdateFlag() == 0 || tf.getTabOrder() < 1)
					continue; // x.getCanUpdate()!=0
				if (workflowStep != null && ((workflowStep.getVisibleFields() != null
						&& !GenericUtil.hasPartInside(workflowStep.getVisibleFields(), "" + tf.getTableFieldId()))
						|| (workflowStep.getUpdatableFields() != null && !GenericUtil
								.hasPartInside(workflowStep.getUpdatableFields(), "" + tf.getTableFieldId()))))
					continue;
				if (tf.getAccessViewTip() != 0
						&& !GenericUtil.accessControl(scd, tf.getAccessViewTip(), tf.getAccessViewRoles(),
								tf.getAccessViewUsers())
						&& (GenericUtil.isEmpty(tf.getAccessViewUserFields()) || accessUserFieldControl(t,
								tf.getAccessViewUserFields(), scd, formResult.getRequestParams(), paramSuffix)))
					continue;
				if (tf.getAccessUpdateTip() != 0
						&& !GenericUtil.accessControl(scd, tf.getAccessUpdateTip(), tf.getAccessUpdateRoles(),
								tf.getAccessUpdateUsers())
						&& (GenericUtil.isEmpty(tf.getAccessUpdateUserFields()) || accessUserFieldControl(t,
								tf.getAccessUpdateUserFields(), scd, formResult.getRequestParams(), paramSuffix)))
					continue;

				if (tf.getAccessMaskTip() != 0
						&& !GenericUtil.accessControl(scd, tf.getAccessMaskTip(), tf.getAccessMaskRoles(),
								tf.getAccessMaskUsers())
						&& (GenericUtil.isEmpty(tf.getAccessMaskUserFields()) || accessUserFieldControl(t,
								tf.getAccessMaskUserFields(), scd, formResult.getRequestParams(), paramSuffix)))
					continue;
				
				if (!GenericUtil.accessControl4SessionField(formResult.getScd(), tf.getRelatedSessionField()))
					continue;

				if (paramSuffix.length() > 0 && !formResult.getRequestParams().containsKey(x.getDsc() + paramSuffix))
					continue;
				if (x.getControlType() == 31 && GenericUtil.uInt(x.getLookupIncludedValues()) == 1
						&& !GenericUtil.hasPartInside(x.getLookupIncludedParams(), "" + scd.get("userId")))
					continue;
				if(x.getControlType()==42 && GenericUtil.safeEquals(FrameworkSetting.defaultPasswordMask, formResult.getRequestParams().get(x.getDsc())))
					continue;

				short notNullFlag = x.getNotNullFlag();
				if(!GenericUtil.isEmpty(x.get_formCellPropertyList())) for(W5FormCellProperty fcp:x.get_formCellPropertyList()) {
					if(fcp.getLkpPropertyType()==0){//required
						for(W5FormCell fc:f.get_formCells())if(fc.getFormCellId() == fcp.getRelatedFormCellId()) {
							if(fc.getActiveFlag()!=0) {
								notNullFlag = 0;
								if(fc.getSourceType()==1) {
									String value = formResult.getRequestParams().get(fc.getDsc());
									if(fc.getControlType()==5) {
										notNullFlag = (short)(fcp.getLkpOperatorType() == GenericUtil.uCheckBox(value) ? 1:0);
									} else
									notNullFlag = (short)(formElementProperty(fcp.getLkpOperatorType(), value, fcp.getVal()) ? 1:0);
								}
							}
							break;
						}
					}
					if(notNullFlag==0 && fcp.getLkpPropertyType()==1 && fcp.getOtherSetValueFlag()!=0){//visible
						for(W5FormCell fc:f.get_formCells())if(fc.getFormCellId() == fcp.getRelatedFormCellId()) {
							if(fc.getActiveFlag()!=0) {
								short visibleFlag = 0;
								if(fc.getSourceType()==1) {
									String value = formResult.getRequestParams().get(fc.getDsc());
									if(fc.getControlType()==5) {
										visibleFlag = (short)(fcp.getLkpOperatorType() == GenericUtil.uCheckBox(value) ? 1:0);
									} else
										visibleFlag = (short)(formElementProperty(fcp.getLkpOperatorType(), value, fcp.getVal()) ? 1:0);
								}
								
								if(visibleFlag==0) { //not visible
									formResult.getRequestParams().put(x.getDsc() + paramSuffix, fcp.getOtherValue());
								}
							}
							break;
						}
					}

				}
				
				Object presult = GenericUtil.prepareParam(tf, scd, formResult.getRequestParams(), x.getSourceType(), null,
						notNullFlag, x.getDsc() + paramSuffix, x.getDefaultValue(), formResult.getErrorMap());

				if (formResult.getErrorMap().isEmpty()) {
					if(presult!=null && x.getVtype()!=null) {
						if(!GenericUtil.validateVtype(presult.toString(), x.getVtype())) {
							formResult.getErrorMap().put(x.getDsc(), LocaleMsgCache.get2(formResult.getScd(), "invalid_"+x.getVtype()));
							continue;
						}
					}
					if (x.getFormCellId() == 6060 || x.getFormCellId() == 16327 || x.getFormCellId() == 16866) { // mail
																													// sifre
																													// icin
						if (presult != null && presult.toString().startsWith("****"))
							continue;
						/*if (FrameworkSetting.mailPassEncrypt)
							presult = GenericUtil.PRMEncrypt(presult.toString());*/
					}

					if (b)
						sql.append(" , ");
					else
						b = true;
					sql.append(tf.getDsc()).append(" = ? ");
					if(presult==null || tf.getLkpEncryptionType()==0)updateParams.add(presult);
					else 
						updateParams.add(EncryptionUtil.encrypt(presult.toString(), tf.getLkpEncryptionType()));
					usedFields.add(tf.getDsc());
				}
			}

		if (formResult.getErrorMap().size() > 0)
			return false;

		for (W5TableField p1 : t.get_tableFieldList())
			if (p1.getCanUpdateFlag() != 0 && !usedFields.contains(p1.getDsc()))
				switch (p1.getSourceType()) { // geri kalan fieldlar icin
				case 4: // calculated Fieldlar icin
					if (b) {
						sql.append(" , ");
					} else
						b = true;
					usedFields.add(p1.getDsc());
					sql.append(p1.getDsc()).append(" = ? ");
					usedFields.add(p1.getDsc());

					Object[] oo = DBUtil.filterExt4SQL("select (" + p1.getDefaultValue() + ")", scd, new HashMap(),
							null);
					List res = executeSQLQuery2(oo[0].toString(), (List) (oo.length > 1 ? oo[1] : null));
					updateParams.add(GenericUtil.isEmpty(res) ? null : res.get(0));
					break;
				case 2: // session
					Object presult = GenericUtil.prepareParam(p1, scd, formResult.getRequestParams(), (short) -1, null,
							(short) 0, null, null, formResult.getErrorMap());
					if (presult != null) {
						if (b) {
							sql.append(" , ");
						} else
							b = true;
						usedFields.add(p1.getDsc());
						sql.append(p1.getDsc()).append(" = ? ");
						updateParams.add(presult);
						usedFields.add(p1.getDsc());
					}

					break;
				}
		if (usedFields.isEmpty()) { // problems exists
			throw new IWBException("validation", "Form Update", formResult.getFormId(), null, "No Used Fields", null);
		}
		if (f.get_versioningFlag()) { // eger versionin varsa, version'lari
										// degistir ve version_no'yu
			// arttir
			sql.append(", version_no=version_no+1, version_user_id=?, version_dttm=iwb.fnc_sysdate(?)");
			updateParams.add(scd.get("userId"));
			updateParams.add(scd.get("customizationId"));
		}

		b = false;
		sql.append(" where ");
		for (W5TableParam x : t.get_tableParamList())
			if (x.getNotNullFlag() != 0) {
				if (b)
					sql.append(" AND ");
				else
					b = true;
				sql.append(x.getExpressionDsc()).append(" = ? ");
				Object presult = GenericUtil.prepareParam(x, scd, formResult.getRequestParams(), (short) -1, null,
						(short) 0, x.getDsc() + paramSuffix, null, formResult.getErrorMap());
				whereParams.add(presult);
				formResult.getPkFields().put(x.getDsc(), presult);
			}

		updateParams.addAll(whereParams);

		try {
			final boolean extendedFlag2 = extendedFlag;
			final Map<Integer, W5FormModule> moduleMap2 = moduleMap;
			return getCurrentSession().doReturningWork(new ReturningWork<Boolean>() {

				public Boolean execute(Connection conn) throws SQLException {
					PreparedStatement s = conn.prepareStatement(sql.toString());
					applyParameters(s, updateParams);
					int updateCount = s.executeUpdate();
					s.close();
					if (FrameworkSetting.hibernateCloseAfterWork)
						conn.close();
					if (t.getDoUpdateLogFlag() != 0)
						logTableRecord(formResult, paramSuffix);
					return updateCount == 1;
				}
			});

		} catch (Exception e) {
			throw new IWBException("sql", "Form.Update", formResult.getFormId(),
					GenericUtil.replaceSql(sql.toString(), updateParams), "Error Updating", e);
		} finally {
			// session.close();
		}
	}

	public int copyFormTable(final W5FormResult formResult, final String schema, final String paramSuffix,
			boolean copyFlag) {
		final W5Form f = formResult.getForm();
		final W5Table t = FrameworkCache.getTable(formResult.getScd(), f.getObjectId()); // formResult.getForm().get_sourceTable();
		if (FrameworkSetting.vcs && FrameworkSetting.vcsServer && t.getVcsFlag() != 0)
			throw new IWBException("vcs", "Form Insert", formResult.getFormId(), null,
					"VCS Server not allowed to insert VCS Table", null);
		final StringBuilder sql = new StringBuilder(), postSql = new StringBuilder();
		sql.append("insert into ");
		if (schema != null)
			sql.append(schema).append(".");
		sql.append(t.getDsc()).append(" ( ");
		postSql.append(" select ");
		final List<Object> copyParams = new ArrayList<Object>();
		boolean b = false;
		boolean extendedFlag = false;
		int paramCount = 0;
		final Map<Integer, String> calculatedParams = new HashMap<Integer, String>();
		final Map<Integer, String> calculatedParamNames = new HashMap<Integer, String>();
		Set<String> usedFields = new HashSet<String>();

		Map<Integer, W5FormModule> moduleMap = null;
		if (formResult.getForm().getRenderType() != 0) {
			moduleMap = new HashMap<Integer, W5FormModule>();
			if (formResult.getForm().get_moduleList() != null)
				for (W5FormModule m : formResult.getForm().get_moduleList())
					moduleMap.put(m.getFormModuleId(), m);
		}

		for (W5FormCell x : f.get_formCells())
			if (x.getNrdType() != 1 && x.getObjectDetailId() != 0 && x.getControlType() < 100) { // disabled(1)
																								// degil
																								// VE
																								// freeField(getObjectDetailId()!=0)
																								// degilse
				W5TableField p1 = (W5TableField) x.get_sourceObjectDetail();
				if (p1 == null || p1.getCanInsertFlag() == 0)
					continue; // x.getCanInsert()!=0

				// view AND update control
				if (
				/*
				 * !PromisUtil.accessControl(formResult.getScd(), p1.getAccessViewTip(),
				 * p1.getAccessViewRoles(), p1.getAccessViewUsers()) ||
				 */
				!GenericUtil.accessControl(formResult.getScd(), p1.getAccessInsertTip(), p1.getAccessInsertRoles(),
						p1.getAccessInsertUsers()))
					continue; // access control


				Object presult = null;
				switch (p1.getCopySourceTip()) {
				case 7: // object_source (readonly)
				case 6: // object_source
					if (copyFlag || p1.getCopySourceTip() == 7) {
						if (b) {
							sql.append(" , ");
							postSql.append(" , ");
						} else
							b = true;
						usedFields.add(p1.getDsc());
						sql.append(p1.getDsc());
						postSql.append(p1.getDsc());
						continue;
					}
				case 1: // request
					presult = GenericUtil.prepareParam(p1, formResult.getScd(), formResult.getRequestParams(),
							x.getSourceType(), null, x.getNotNullFlag(), x.getDsc() + paramSuffix, x.getDefaultValue(),
							formResult.getErrorMap());
					break;
				default:
					continue;
				}

				if (formResult.getErrorMap().isEmpty()) {
					if (b) {
						sql.append(" , ");
						postSql.append(" , ");
					} else
						b = true;
					usedFields.add(p1.getDsc());
					sql.append(p1.getDsc());
					if (x.getOutFlag() != 0) { // bu field outputParam'a
												// yazilacak
						if (x.getSourceType() == 4) { // calculated, sql
														// calisacak sonra deger
														// verilecek
							calculatedParams.put(paramCount, (String) presult);
							calculatedParamNames.put(paramCount, x.getDsc());
						} else {
							formResult.getOutputFields().put(x.getDsc(), presult);
						}
						postSql.append(" ? ");
						copyParams.add(null);
						paramCount++;
					} else { // calculated, outputa yazilmadan direk
						if (x.getSourceType() == 4) { // calculated, sql
														// calisacak sonra deger
														// verilecek
							postSql.append(" ( ").append(presult).append(" ) ");
						} else {
							postSql.append(" ? ");
							copyParams.add(presult);
							paramCount++;
						}
					}
				}
			}

		for (W5TableField p1 : t.get_tableFieldList())
			if (p1.getCanInsertFlag() != 0 && !usedFields.contains(p1.getDsc()))
				switch (p1.getCopySourceTip()) {
				case 7: //
					if (p1.getSourceType() != 4)
						break;
				case 4: // calculated Fieldlar icin
					if (b) {
						sql.append(" , ");
						postSql.append(" , ");
					} else
						b = true;
					usedFields.add(p1.getDsc());
					sql.append(p1.getDsc());
					calculatedParams.put(paramCount, GenericUtil
							.filterExt(p1.getDefaultValue(), formResult.getScd(), formResult.getRequestParams(), null)
							.toString());
					calculatedParamNames.put(paramCount, p1.getDsc());
					postSql.append(" ? ");
					copyParams.add(null);
					paramCount++;
					break;
				case 2: // session
					Object presult = GenericUtil.prepareParam(p1, formResult.getScd(), formResult.getRequestParams(),
							(short) -1, null, (short) 0, null, null, formResult.getErrorMap());
					if (presult != null) {
						if (b) {
							sql.append(" , ");
							postSql.append(" , ");
						} else
							b = true;
						usedFields.add(p1.getDsc());
						sql.append(p1.getDsc());
						postSql.append(" ? ");
						copyParams.add(presult);
						paramCount++;
					}

					break;
				case 9: // UUID
					Object presult2 = GenericUtil.prepareParam(p1, formResult.getScd(), formResult.getRequestParams(),
							(short) -1, null, (short) 0, null, null, formResult.getErrorMap());
					if (presult2 != null) {
						if (b) {
							sql.append(" , ");
							postSql.append(" , ");
						} else
							b = true;
						usedFields.add(p1.getDsc());
						sql.append(p1.getDsc());
						postSql.append(" ? ");
						copyParams.add(presult2);
						paramCount++;

						formResult.getOutputFields().put(p1.getDsc(), presult2);
					}

					break;
				case 8: // Global Nextval
					Object presult3 = GenericUtil.prepareParam(p1, formResult.getScd(), formResult.getRequestParams(),
							(short) -1, null, (short) 0, null, null, formResult.getErrorMap());
					if (presult3 != null) {
						if (b) {
							sql.append(" , ");
							postSql.append(" , ");
						} else
							b = true;
						usedFields.add(p1.getDsc());
						sql.append(p1.getDsc());
						postSql.append(" ? ");
						copyParams.add(presult3);
						paramCount++;

						formResult.getOutputFields().put(p1.getDsc(), presult3);
					}

					break;
				}

		if (!formResult.getErrorMap().isEmpty())
			return 0;

		if (usedFields.isEmpty()) { // problems exists
			throw new IWBException("validation", "Form Copy", formResult.getFormId(), null, "No Used Fields", null);
		}

		if (f.get_versioningFlag()) { // eger versioning varsa, version'lari
										// degistir ve version_no'yu
			// arttir
			sql.append(", version_no, insert_user_id, version_user_id, insert_dttm, version_dttm ");
			postSql.append(" , 1, ?, ?, iwb.fnc_sysdate(?), iwb.fnc_sysdate(?)");
			copyParams.add(formResult.getScd().get("userId"));
			copyParams.add(formResult.getScd().get("userId"));
			copyParams.add(formResult.getScd().get("customizationId"));
			copyParams.add(formResult.getScd().get("customizationId"));
		}

		sql.append(" ) ").append(postSql).append(" from ");
		if (schema != null)
			sql.append(schema).append(".");
		sql.append(t.getDsc());

		b = false;
		sql.append(" where ");
		for (W5TableParam x : t.get_tableParamList())
			if (x.getNotNullFlag() != 0) {
				if (b)
					sql.append(" AND ");
				else
					b = true;
				sql.append(x.getExpressionDsc()).append(" = ? ");
				Object presult = GenericUtil.prepareParam(x, formResult.getScd(), formResult.getRequestParams(),
						(short) -1, null, (short) 0, x.getDsc() + paramSuffix, null, formResult.getErrorMap());
				copyParams.add(presult);
				formResult.getPkFields().put(x.getDsc(), presult);
			}

		b = false;
		try {
			return getCurrentSession().doReturningWork(new ReturningWork<Integer>() {

				public Integer execute(Connection conn) throws SQLException {
					PreparedStatement s = null;
					for (Integer o : calculatedParams.keySet()) { // calculated
																	// ve output
																	// edilecek
																	// parametreler
																	// hesaplaniyor
						// once
						String seq = calculatedParams.get(o);
						if (seq.endsWith(".nextval"))
							seq = "nextval('" + seq.substring(0, seq.length() - 8) + "')";
						s = conn.prepareStatement("select " + seq + " "); // from
																			// dual
						ResultSet rs = s.executeQuery();
						rs.next();
						Object paramOut = rs.getObject(1);
						if (paramOut != null) {
							if (paramOut instanceof java.sql.Timestamp) {
								try {
									paramOut = (java.sql.Timestamp) paramOut;
								} catch (Exception e) {
									paramOut = "java.sql.Timestamp";
								}
							} else if (paramOut instanceof java.sql.Date) {
								try {
									paramOut = rs.getTimestamp(1);
								} catch (Exception e) {
									paramOut = "java.sql.Date";
								}
							}
						}
						rs.close();
						s.close();
						copyParams.set(o, paramOut);
						formResult.getOutputFields().put(calculatedParamNames.get(o), paramOut);
					}
					int count = 0;

					s = conn.prepareStatement(sql.toString());
					applyParameters(s, copyParams);
					if (schema == null) {
						count = s.executeUpdate();
					} else
						try { // farkli bir yere koymaya calisiyor
							count = s.executeUpdate();
						} catch (Exception e) {
							PreparedStatement s2 = conn.prepareStatement("create table " + schema + "." + t.getDsc()
									+ " as select * from " + t.getDsc() + " where 1=2");
							s2.executeUpdate();
							s2.close();
							String pk = "";
							for (W5TableParam p : t.get_tableParamList()) {
								if (pk.length() > 0)
									pk += ",";
								pk += p.getExpressionDsc();
							}
							s2 = conn.prepareStatement("alter table " + schema + "." + t.getDsc()
									+ " add constraint PK_APPROVAL_" + t.getTableId() + " primary key (" + pk + ")");
							s2.executeUpdate();
							s2.close();
							count = s.executeUpdate();
						}
					s.close();
					if (FrameworkSetting.hibernateCloseAfterWork)
						conn.close();

					return count;
				}
			});

		} catch (Exception e) {
			throw new IWBException("sql", "Form.Copy", formResult.getFormId(),
					GenericUtil.replaceSql(sql.toString(), copyParams), "Error Copying", e);
		} finally {
			// session.close();
		}
	}

	public Object[] listObjectCommentAndAttachUsers(Map<String, Object> scd, Map<String, String> requestParams) {
		if(true)return null;
		int tableId = GenericUtil.uInt(requestParams.get("table_id"));
		int tablePk = GenericUtil.uInt(requestParams.get("table_pk"));
		int customizationId = (Integer) scd.get("customizationId");
		int sessionUserId = (Integer) scd.get("userId");
		int recordInsertUserId = 0, recordVersionUserId = 0, assignedUserId = 0;
		boolean recordInsertUserFlag = false, recordVersionUserFlag = false, assignedUserFlag = false,
				customizationFlag = false;
		Set<Integer> extraUserIds = new HashSet<Integer>();
		List<Object[]> l = executeSQLQuery("select t.dsc, tp.expression_Dsc"
				+ ",(select count(1) from iwb.w5_Table_Field tf where tf.table_Id=t.table_Id AND tf.customization_Id=t.customization_Id AND lower(tf.dsc)='insert_user_id') insert_user_id_count"
				+ ",(select count(1) from iwb.w5_Table_Field tf where tf.table_Id=t.table_Id AND tf.customization_Id=t.customization_Id AND lower(tf.dsc)='version_user_id') version_user_id_count"
				+ ",(select count(1) from iwb.w5_Table_Field tf where tf.table_Id=t.table_Id AND tf.customization_Id=t.customization_Id AND lower(tf.dsc)='assigned_user_id') assigned_user_id_count "
				+ ",(select count(1) from iwb.w5_Table_Field tf where tf.table_Id=t.table_Id AND tf.customization_Id=t.customization_Id AND lower(tf.dsc)='customization_id') customization_id_count "
				+ "from iwb.w5_Table t, iwb.W5_Table_Param tp "
				+ "where t.customization_Id=? AND t.table_Id=? AND t.table_Id=tp.table_Id AND tp.tab_Order=1",
				customizationId, tableId);
		if (!l.isEmpty()) {
			String tableDsc = (l.get(0)[0]).toString();
			String tablePkDsc = (l.get(0)[1]).toString();
			recordInsertUserFlag = GenericUtil.uInt((l.get(0)[2]).toString()) != 0;
			recordVersionUserFlag = GenericUtil.uInt((l.get(0)[3]).toString()) != 0;
			assignedUserFlag = GenericUtil.uInt((l.get(0)[4]).toString()) != 0;
			customizationFlag = GenericUtil.uInt((l.get(0)[5]).toString()) != 0;
			if (recordInsertUserFlag || recordVersionUserFlag || assignedUserFlag) {
				StringBuilder sql = new StringBuilder();
				sql.append("select ");
				int fieldCount = 0;
				if (recordInsertUserFlag) {
					sql.append("x.insert_user_id,");
					fieldCount++;
				}
				if (recordVersionUserFlag) {
					sql.append("x.version_user_id,");
					recordVersionUserId = recordInsertUserFlag ? 1 : 0;
					fieldCount++;
				}
				if (assignedUserFlag) {
					sql.append("x.assigned_user_id,");
					assignedUserId = (recordInsertUserFlag ? 1 : 0) + (recordVersionUserFlag ? 1 : 0);
					fieldCount++;
				}
				sql.setLength(sql.length() - 1);
				sql.append(" from ").append(tableDsc).append(" x where x.").append(tablePkDsc).append("=?");
				if (customizationFlag)
					sql.append(" AND x.customization_id=?");
				List<Object[]> l2 = customizationFlag ? executeSQLQuery(sql.toString(), tablePk, customizationId)
						: executeSQLQuery(sql.toString(), tablePk);
				if (l2 != null && !l2.isEmpty()) {
					if (recordInsertUserFlag)
						extraUserIds.add(GenericUtil.uInt(fieldCount == 1 ? l2.get(0) : l2.get(0)[0]));
					if (recordVersionUserFlag)
						extraUserIds
								.add(GenericUtil.uInt(fieldCount == 1 ? l2.get(0) : l2.get(0)[recordVersionUserId]));
					if (assignedUserFlag)
						extraUserIds.add(GenericUtil.uInt(fieldCount == 1 ? l2.get(0) : l2.get(0)[assignedUserId]));
				}
			}
		}

		extraUserIds.remove(sessionUserId);
		return extraUserIds.isEmpty() ? null : extraUserIds.toArray();
	}

	private boolean formElementProperty(short operatorTip, String elementValue, String value) {
		switch(operatorTip) {
		case -1://is Empty
			return GenericUtil.isEmpty(value);
		case -2://is not empty
			return !GenericUtil.isEmpty(value);
		case	8://in
			if(GenericUtil.isEmpty(value))return false;
			return GenericUtil.hasPartInside2b(value, elementValue);
		case	9://not in
			if(GenericUtil.isEmpty(value))return true;
			return !GenericUtil.hasPartInside2b(value, elementValue);
		case	0://equals
			return GenericUtil.safeEquals(elementValue, value);
		case	1://not equals
			return !GenericUtil.safeEquals(elementValue, value);
		}
		return false;
	}
	public int insertFormTable(final W5FormResult formResult, final String paramSuffix) {
		final W5Form f = formResult.getForm();
		final String projectId = FrameworkCache.getProjectId(formResult.getScd(), "40." + formResult.getFormId());
		final W5Table t = FrameworkCache.getTable(projectId, f.getObjectId()); // formResult.getForm().get_sourceTable();
		if (FrameworkSetting.vcs && FrameworkSetting.vcsServer && t.getVcsFlag() != 0)
			throw new IWBException("vcs", "Form Insert", formResult.getFormId(), null,
					"VCS Server not allowed to insert VCS Table", null);

		final StringBuilder sql = new StringBuilder(), postSql = new StringBuilder();
		sql.append("insert into ");
		sql.append(t.getDsc()).append(" ( ");
		postSql.append(" values (");
		final List<Object> insertParams = new ArrayList<Object>();
		boolean b = false;
		int paramCount = 0;
		final Map<Integer, String> calculatedParams = new HashMap<Integer, String>();
		final Map<Integer, String> calculatedParamNames = new HashMap<Integer, String>();
		Set<String> usedFields = new HashSet<String>();

		Map<Integer, W5FormModule> moduleMap = null;
		if (formResult.getForm().getRenderType() != 0) {
			moduleMap = new HashMap<Integer, W5FormModule>();
			if (formResult.getForm().get_moduleList() != null)
				for (W5FormModule m : formResult.getForm().get_moduleList())
					moduleMap.put(m.getFormModuleId(), m);
		}

		for (W5FormCell x : f.get_formCells())
			if (x.getNrdType() != 1 && x.getObjectDetailId() != 0 && x.getControlType() < 100) { // disabled(1)
																								// degil
																								// VE
																								// freeField(getObjectDetailId()!=0)
																								// degilse
				W5TableField tf = (W5TableField) x.get_sourceObjectDetail();
				if (tf == null)
					continue; // error. actually olmamasi lazim
				if (tf.getCanInsertFlag() == 0)
					continue; // x.getCanInsert()!=0

				// view AND update control
				if (
				/*
				 * !PromisUtil.accessControl(formResult.getScd(), p1.getAccessViewTip(),
				 * p1.getAccessViewRoles(), p1.getAccessViewUsers()) ||
				 */
				!GenericUtil.accessControl(formResult.getScd(), tf.getAccessInsertTip(), tf.getAccessInsertRoles(),
						tf.getAccessInsertUsers()))
					continue; // access control

				// related session field control
				if (!GenericUtil.accessControl4SessionField(formResult.getScd(),tf.getRelatedSessionField()))
					continue;


				short notNullFlag = x.getNotNullFlag();
				if(!GenericUtil.isEmpty(x.get_formCellPropertyList())) for(W5FormCellProperty fcp:x.get_formCellPropertyList()) {
					if(fcp.getLkpPropertyType()==0){//required
						notNullFlag = 0;
						for(W5FormCell fc:f.get_formCells())if(fc.getFormCellId() == fcp.getRelatedFormCellId()) {
							if(fc.getSourceType()==1) {
								String value = formResult.getRequestParams().get(fc.getDsc());
								if(fc.getControlType()==5) {
									notNullFlag = (short)(fcp.getLkpOperatorType() == GenericUtil.uCheckBox(value) ? 1:0);
								} else
									notNullFlag = (short)(formElementProperty(fcp.getLkpOperatorType(), value, fcp.getVal()) ? 1:0);
							}
							break;
						}
					}
					if(notNullFlag==0 && fcp.getLkpPropertyType()==1 && fcp.getOtherSetValueFlag()!=0){//visible
						for(W5FormCell fc:f.get_formCells())if(fc.getFormCellId() == fcp.getRelatedFormCellId()) {
							if(fc.getActiveFlag()!=0) {
								short visibleFlag = 0;
								if(fc.getSourceType()==1) {
									String value = formResult.getRequestParams().get(fc.getDsc() + paramSuffix);
									if(fc.getControlType()==5) {
										visibleFlag = (short)(fcp.getLkpOperatorType() == GenericUtil.uCheckBox(value) ? 1:0);
									} else
										visibleFlag = (short)(formElementProperty(fcp.getLkpOperatorType(), value, fcp.getVal()) ? 1:0);
								}
								
								if(visibleFlag==0) { //not visible
									formResult.getRequestParams().put(x.getDsc() + paramSuffix, fcp.getOtherValue());
								}
							}
							break;
						}
					}
				}
				Object presult = GenericUtil.prepareParam(tf, formResult.getScd(), formResult.getRequestParams(),
						x.getSourceType(), null, notNullFlag, x.getDsc() + paramSuffix, x.getDefaultValue(),
						formResult.getErrorMap());

				if (formResult.getErrorMap().isEmpty()) {
					if(presult!=null && x.getVtype()!=null) {
						if(!GenericUtil.validateVtype(presult.toString(), x.getVtype())) {
							formResult.getErrorMap().put(x.getDsc(), LocaleMsgCache.get2(formResult.getScd(), "invalid_"+x.getVtype()));
							continue;
						}
					}
					if (b) {
						sql.append(" , ");
						postSql.append(" , ");
					} else
						b = true;
					usedFields.add(tf.getDsc());
					sql.append(tf.getDsc());
					if (x.getOutFlag() != 0) { // bu field outputParam'a
												// yazilacak
						if (x.getSourceType() == 4) { // calculated, sql
														// calisacak sonra deger
														// verilecek
							calculatedParams.put(paramCount, (String) presult);
							calculatedParamNames.put(paramCount, x.getDsc());
						} else {
							formResult.getOutputFields().put(x.getDsc(), presult);
						}
						postSql.append(" ? ");
						insertParams.add(null);
						paramCount++;
					} else { // calculated, outputa yazilmadan direk
						if (x.getSourceType() == 4) { // calculated, sql
														// calisacak sonra deger
														// verilecek
							postSql.append(" ( ").append(presult).append(" ) ");
						} else {
							postSql.append(" ? ");
							//insertParams.add(presult);
							if(presult==null || tf.getLkpEncryptionType()==0)insertParams.add(presult);
							else insertParams.add(EncryptionUtil.encrypt(presult.toString(), tf.getLkpEncryptionType()));
							paramCount++;
						}
					}
				}
			}

		for (W5TableField p1 : t.get_tableFieldList())
			if (p1.getCanInsertFlag() != 0 && !usedFields.contains(p1.getDsc()))
				switch (p1.getSourceType()) {

				case 4: // SQL calculated Fieldlar icin
					if (b) {
						sql.append(" , ");
						postSql.append(" , ");
					} else
						b = true;
					usedFields.add(p1.getDsc());
					sql.append(p1.getDsc());
					calculatedParams.put(paramCount, GenericUtil
							.filterExt(p1.getDefaultValue(), formResult.getScd(), formResult.getRequestParams(), null)
							.toString());
					calculatedParamNames.put(paramCount, p1.getDsc());
					postSql.append(" ? ");
					insertParams.add(null);
					paramCount++;
					break;
				case 5:// javascript
				case 2: // session
					Object presult = GenericUtil.prepareParam(p1, formResult.getScd(), formResult.getRequestParams(),
							(short) -1, null, (short) 0, null, null, formResult.getErrorMap());
					if (presult != null) {
						if (b) {
							sql.append(" , ");
							postSql.append(" , ");
						} else
							b = true;
						usedFields.add(p1.getDsc());
						sql.append(p1.getDsc());
						postSql.append(" ? ");
						insertParams.add(presult);
						paramCount++;
					}

					break;
				case 9: // UUID
					Object presult2 = GenericUtil.prepareParam(p1, formResult.getScd(), formResult.getRequestParams(),
							(short) -1, null, (short) 0, null, null, formResult.getErrorMap());
					if (presult2 != null) {
						if (b) {
							sql.append(" , ");
							postSql.append(" , ");
						} else
							b = true;
						usedFields.add(p1.getDsc());
						sql.append(p1.getDsc());
						postSql.append(" ? ");
						insertParams.add(presult2);
						paramCount++;

						formResult.getOutputFields().put(p1.getDsc(), presult2);
					}

					break;
				case 8: // Global Nextval
					Object presult3 = GenericUtil.prepareParam(p1, formResult.getScd(), formResult.getRequestParams(),
							(short) -1, null, (short) 0, null, null, formResult.getErrorMap());
					if (presult3 != null) {
						if (b) {
							sql.append(" , ");
							postSql.append(" , ");
						} else
							b = true;
						usedFields.add(p1.getDsc());
						sql.append(p1.getDsc());
						postSql.append(" ? ");
						insertParams.add(presult3);
						paramCount++;

						formResult.getOutputFields().put(p1.getDsc(), presult3);
					}

					break;
				}

		if (!formResult.getErrorMap().isEmpty())
			return 0;

		if (usedFields.isEmpty()) { // problems exists
			throw new IWBException("validation", "Form Insert", formResult.getFormId(), null, "No Used Fields", null);
		}

		if (f.get_versioningFlag()) { // eger versioning varsa, version'lari
										// degistir ve version_no'yu
			// arttir
			sql.append(", version_no, insert_user_id, version_user_id, insert_dttm, version_dttm ");
			postSql.append(" , 1, ?, ?, iwb.fnc_sysdate(?), iwb.fnc_sysdate(?) ");
			insertParams.add(formResult.getScd().get("userId"));
			insertParams.add(formResult.getScd().get("userId"));
			insertParams.add(formResult.getScd().get("customizationId"));
			insertParams.add(formResult.getScd().get("customizationId"));
		}

		sql.append(" ) ").append(postSql).append(")");

		try {
			final Map<Integer, W5FormModule> moduleMap2 = moduleMap;

			return getCurrentSession().doReturningWork(new ReturningWork<Integer>() {

				public Integer execute(Connection conn) throws SQLException {
					PreparedStatement s = null;
					int count = 0;
					for (Integer o : calculatedParams.keySet()) { // calculated
																	// ve output
																	// edilecek
																	// parametreler
																	// hesaplaniyor
						// once
						String seq = calculatedParams.get(o);
						if (seq.endsWith(".nextval"))
							seq = "nextval('" + seq.substring(0, seq.length() - 8) + "')";
						s = conn.prepareStatement("select " + seq + " "); // from
																			// dual
						ResultSet rs = s.executeQuery();
						rs.next();
						Object paramOut = rs.getObject(1);
						if (paramOut != null) {
							if (paramOut instanceof java.sql.Timestamp) {
								try {
									paramOut = (java.sql.Timestamp) paramOut;
								} catch (Exception e) {
									paramOut = "java.sql.Timestamp";
								}
							} else if (paramOut instanceof java.sql.Date) {
								try {
									paramOut = rs.getTimestamp(1);
								} catch (Exception e) {
									paramOut = "java.sql.Date";
								}
							}
						}

						rs.close();
						s.close();
						insertParams.set(o, paramOut);
						formResult.getOutputFields().put(calculatedParamNames.get(o), paramOut);
					}
					s = conn.prepareStatement(sql.toString());
					applyParameters(s, insertParams);
					count = s.executeUpdate();
					s.close();
					int customizationId = (Integer) formResult.getScd().get("customizationId");

					if (t.getTableId() != 329
							&& FrameworkCache.getAppSettingIntValue(customizationId, "make_comment_flag") != 0
							&& t.getMakeCommentFlag() != 0) {
						PreparedStatement s2 = conn.prepareStatement(
								"update iwb.w5_comment set table_pk=? where project_uuid=? AND table_id=? AND table_pk=?");
						applyParameters(s2,
								formResult.getOutputFields().get(t.get_tableParamList().get(0).getExpressionDsc()),
								projectId, t.getTableId(),
								GenericUtil.uInt(formResult.getRequestParams().get("_tmpId")));
						s2.executeUpdate();
						s2.close();
					}
					

					if (t.getTableId() != 44
							&& FrameworkCache.getAppSettingIntValue(customizationId, "file_attachment_flag") != 0
							&& t.getFileAttachmentFlag() != 0) {
						PreparedStatement s2 = conn.prepareStatement(
								FrameworkCache.getTable(formResult.getScd(), FrameworkSetting.customFileTableId)==null ? 
									"update iwb.w5_file_attachment set table_pk=?::text where project_uuid=? AND table_id=?::integer AND table_pk=?::text" :
									"update x_file set table_pk=?::integer where ? is not null AND table_id=?::integer AND table_pk=?::integer"
								);
						applyParameters(s2,
								formResult.getOutputFields().get(t.get_tableParamList().get(0).getExpressionDsc()),
								projectId, t.getTableId(),
								formResult.getRequestParams().get("_tmpId"));
						s2.executeUpdate();
						s2.close();
					}

					/*
					 * if(t.getTableId()!=370 &&
					 * FrameworkCache.getAppSettingIntValue(customizationId,
					 * "row_based_security_flag")!=0 && !GenericUtil.isEmpty(t.getAccessTips())){
					 * PreparedStatement s2 = conn.prepareStatement(
					 * "update iwb.w5_access_control set table_pk=?::int where customization_id=? AND table_id=? AND table_pk=?::int"
					 * ); applyParameters(s2,
					 * formResult.getOutputFields().get(t.get_tableParamList().
					 * get(0).getExpressionDsc()),customizationId,t.getTableId()
					 * ,formResult.getRequestParams().get("_tmpId")); s2.executeUpdate();
					 * s2.close(); }
					 */

					if (FrameworkSetting.hibernateCloseAfterWork)
						conn.close();

					return count;
				}
			});

		} catch (Exception e) {
			throw new IWBException("sql", "Form.Insert", formResult.getFormId(),
					GenericUtil.replaceSql(sql.toString(), insertParams), "Error Inserting", e);
		} finally {
			// session.close();
		}

		/*
		 * if(formResult.getForm().get_sourceTable() != null &&
		 * formResult.getForm().get_sourceTable().getDoInsertLogFlag()!=0){ Bunun
		 * yapılabilmesi için önce logTableRecord'un değişmesi lazım. for(W5TableParam
		 * x: t.get_tableParamList()){ if(x.getNotNullFlag()!=0){ Object presult =
		 * PromisUtil.prepareParam(x, formResult.getScd(),
		 * formResult.getRequestParams(), (short)-1, null, (short)0, x.getDsc() +
		 * paramSuffix, null, formResult.getErrorMap());
		 * formResult.getPkFields().put(x.getDsc(), presult); } }
		 * logTableRecord(formResult); }
		 */
	}

	public boolean deleteTableRecord(W5FormResult formResult, String paramSuffix) {
		W5Form f = formResult.getForm();
		W5Table t = FrameworkCache.getTable(formResult.getScd(), f.getObjectId());
		if (FrameworkSetting.vcs && FrameworkSetting.vcsServer && t.getVcsFlag() != 0)
			throw new IWBException("vcs", "Form Record Update", formResult.getFormId(), null,
					"VCS Server not allowed to delete VCS Table", null);
		StringBuilder sql = new StringBuilder();
		sql.append("delete from ").append(t.getDsc()).append(" where ");
		List<Object> realParams = new ArrayList<Object>();
		boolean b = false;
		for (W5TableParam x : t.get_tableParamList())
			if (x.getNotNullFlag() != 0) {
				if (b)
					sql.append(" AND ");
				else
					b = true;
				sql.append(x.getExpressionDsc()).append(" = ? ");
				Object presult = GenericUtil.prepareParam(x, formResult.getScd(), formResult.getRequestParams(),
						(short) -1, null, (short) 0, x.getDsc() + paramSuffix, null, formResult.getErrorMap());
				realParams.add(presult);
				formResult.getPkFields().put(x.getDsc(), presult);
			}

		if (t.getDoDeleteLogFlag() != 0)
			logTableRecord(formResult, paramSuffix);
		Session session = getCurrentSession();
		try {
			b = applyParameters(session.createSQLQuery(sql.toString()), realParams).executeUpdate() > 0;

		} catch (Exception e) {
			throw new IWBException("sql", "Form.Delete", formResult.getFormId(),
					GenericUtil.replaceSql(sql.toString(), realParams), "Error Deleting", e);
		} finally {
			// session.close();
		}
		return b;
	}



	public void executeDbFunc(final W5GlobalFuncResult r, final String paramSuffix) {
		Log5GlobalFuncAction action = new Log5GlobalFuncAction(r);
		String error = null;

		final List<Object> sqlParams = new ArrayList<Object>();
		final List<String> sqlNames = new ArrayList<String>();
		final StringBuilder sql = new StringBuilder();
		sql.append("{call ");

		sql.append(r.getGlobalFunc().getDsc());
		boolean hasOutParam = false;
		if (r.getGlobalFunc().get_dbFuncParamList().size() > 0) {
			boolean b = false;
			sql.append("( ");
			for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList()) {
				if (b)
					sql.append(",");
				else
					b = true;
				sql.append(" ? ");
				String pvalue = null;
				if (p1.getOutFlag() != 0) {
					sqlNames.add(p1.getDsc());
					sqlParams.add(null);
					hasOutParam = true;
				} else {
					Object presult = GenericUtil.prepareParam(p1, r.getScd(), r.getRequestParams(), (short) -1, null,
							(short) 0, p1.getSourceType() == 1 ? p1.getDsc() + paramSuffix : null, null,
							r.getErrorMap());
					sqlParams.add(presult);
					sqlNames.add(null);
				}
			}
			sql.append(" )");
		}
		sql.append("}");

		r.setExecutedSql(sql.toString());
		r.setSqlParams(sqlParams);

		if (!r.getErrorMap().isEmpty()) {
			r.setSuccess(false);
			return;
		}
		final boolean hasOutParam2 = hasOutParam;
		try {
			getCurrentSession().doWork(new Work() {

				public void execute(Connection conn) throws SQLException {

					CallableStatement s = conn.prepareCall(sql.toString());
					int i = 1;
					for (int ix = 0; ix < sqlParams.size(); ix++) {
						Object o = sqlParams.get(ix);
						if (sqlNames.get(ix) != null) {
							W5GlobalFuncParam p1 = r.getGlobalFunc().get_dbFuncParamList().get(i - 1);
							short t = p1.getParamType();
							int t1 = java.sql.Types.VARCHAR;
							if (t == 1)
								t1 = java.sql.Types.VARCHAR;
							else if (t == 2)
								t1 = java.sql.Types.DATE;
							else if (t == 3)
								t1 = java.sql.Types.DECIMAL;
							else if (t == 4)
								t1 = java.sql.Types.INTEGER;
							else if (t == 5)
								t1 = java.sql.Types.SMALLINT;

							if (p1.getOutFlag() == 1)
								s.registerOutParameter(i, t1);
						} else {
							if (o == null)
								s.setObject(i, null);
							else if (o instanceof Date)
								s.setTimestamp(i, new java.sql.Timestamp(((Date) o).getTime()));
							else
								s.setObject(i, o);
						}
						i++;
					}
					s.execute();

					if (hasOutParam2) {
						// JSONObject jo=new JSONObject();
						Map<String, Object> res = new HashMap<String, Object>();
						for (int ixx = 0; ixx < sqlNames.size(); ixx++)
							if (sqlNames.get(ixx) != null) {
								Object o = s.getObject(ixx + 1);
								if (o != null) {
									if (o instanceof java.sql.Date) {
										o = GenericUtil.uFormatDate((java.sql.Date) o);
									}
									res.put(sqlNames.get(ixx), o);
								}
							}
						r.setResultMap(res);
					}
					if (s != null)
						s.close();
					r.setSuccess(true);

					if (FrameworkSetting.hibernateCloseAfterWork)
						if (conn != null)
							conn.close();

				}
			});
		} catch (Exception e) {
			throw new IWBException("sql", "DbFunc.Execute", r.getGlobalFuncId(),
					GenericUtil.replaceSql(r.getExecutedSql(), sqlParams),
					"[20," + r.getGlobalFuncId() + "] Error Executing", e);
		} finally {
			logGlobalFuncAction(action, r, error);
		}

	}

	public void bookmarkForm(Map<String, Object> scd, String dsc, int formId, int userId, W5FormResult formResult) {
		W5FormValue formValue = new W5FormValue();
		formValue.setFormId(formId);
		formValue.setDsc(dsc);
		formValue.setInsertUserId(userId);
		formValue.setCustomizationId((Integer)scd.get("customizationId"));
		formValue.setProjectUuid((String)scd.get("projectId"));
		saveObject(formValue);

		for (W5FormCell c : formResult.getForm().get_formCells())
			if (c.getSourceType() == 1) {
				String val = formResult.getRequestParams().get(c.getDsc());
				if (val != null && val.length() > 0 && (formId > 0 || val.length() <= 2048)) {
					W5FormValueCell fvc = new W5FormValueCell();
					if (val.length() > 2048)
						val = val.substring(0, 2048);
					fvc.setVal(val);
					fvc.setFormCellId(c.getFormCellId());
					fvc.setFormValueId(formValue.getFormValueId());
					fvc.setCustomizationId((Integer) scd.get("customizationId"));
					fvc.setProjectUuid((String)scd.get("projectId"));
					saveObject(fvc);
				}
			}
		if (formResult.getPkFields() == null)
			formResult.setPkFields(new HashMap());
		formResult.getPkFields().put("id", formValue.getFormValueId());
	}



	public boolean copyFormTableDetail(W5FormResult masterFormResult, W5TableChild tc, String newMasterTablePk,
			String schema, String prefix) {
		Map<String, Object> scd = masterFormResult.getScd();
		W5Table t = FrameworkCache.getTable(scd, tc.getRelatedTableId());
		if (t == null)
			return false;
		Map<String, String> requestParams = new HashMap();
		requestParams.putAll(masterFormResult.getRequestParams());
		W5Table mt = FrameworkCache.getTable(scd, masterFormResult.getForm().getObjectId()); // .get_sourceTable();
		W5FormResult formResult = metadataLoader.getFormResult(scd, t.getDefaultUpdateFormId(), 2, requestParams);
		if (t.getAccessViewTip() == 0 && !FrameworkCache.roleAccessControl(scd, 0)) {
			// throw new PromisException("security","Module", 0, null, "Modul
			// Kontrol: Erişim kontrolünüz
			// yok", null);
			return false;
		}
		if (!GenericUtil.accessControl(scd, t.getAccessViewTip(), t.getAccessViewRoles(), t.getAccessViewUsers())) {
			// throw new PromisException("security","Form",
			// t.getDefaultUpdateFormId(), null, "Tablo
			// Kontrol: Veri görünteleyemezsiniz", null);
			return false;
		}
		/*
		 * if(!PromisUtil.accessControl(scd, t.getAccessInsertTip(),
		 * t.getAccessInsertRoles(), t.getAccessInsertUsers())){ //throw new
		 * PromisException("security","Form", t.getDefaultUpdateFormId(), null,
		 * "Tablo Kontrol: Yeni kayit yapamazsiniz", null); return false; }
		 */

		List<Object> sqlParams = new ArrayList();
		String masterFieldDsc = mt.get_tableFieldMap().get(tc.getTableFieldId()).getDsc();
		String detailFieldDsc = t.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc();
		String detailStaticFieldDsc = tc.getRelatedStaticTableFieldId() > 0
				? t.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()
				: null;

		String detailSql = "select x." + t.get_tableParamList().get(0).getExpressionDsc();
		boolean multiKey = false;
		boolean cusFlag = false;
		if (t.get_tableParamList().size() > 1) {
			if (!t.get_tableParamList().get(1).getExpressionDsc().equals("project_uuid")) {
				for (W5TableParam tp : t.get_tableParamList())
					if (!t.get_tableParamList().get(1).getExpressionDsc().equals("project_uuid")) {
						if (!multiKey) {
							multiKey = true;
							continue;
						}
						detailSql += ",x." + tp.getExpressionDsc();
					} else
						cusFlag = true;
			} else
				cusFlag = true;
		}
		detailSql += " from " + t.getDsc() + " x where ";
		if (cusFlag) {
			detailSql += "x.project_uuid=? AND ";
			sqlParams.add(scd.get("projectId"));
		}
		detailSql += "exists(select 1 from " + mt.getDsc() + " q where q." + masterFieldDsc + " = x." + detailFieldDsc // detail
																														// ile
																														// iliski
				+ " AND q." + mt.get_tableParamList().get(0).getExpressionDsc() + " = ?"; // master
																							// kayit
																							// ile
																							// iliski
		sqlParams.add(GenericUtil.uInt(masterFormResult.getPkFields().get("t" + masterFieldDsc)));
		// sqlParams.add(PromisUtil.uInt(masterPk));

		detailSql += DBUtil.includeTenantProjectPostSQL(scd, mt, "q");

		if (detailStaticFieldDsc != null)
			detailSql += " AND x." + detailStaticFieldDsc + "=" + tc.getRelatedStaticTableFieldVal();
		detailSql += ")";
		boolean bt = false;
		for (W5TableField tf : t.get_tableFieldList())
			if (tf.getDsc().equals("tab_order")) {
				detailSql += " order by x.tab_order";
				bt = true;
				break;
			}
		if (!bt) {
			detailSql += " order by x." + t.get_tableFieldList().get(0).getDsc();
		}

		List<Object> rl = executeSQLQuery(detailSql, sqlParams.toArray());
		requestParams.put(detailFieldDsc,
				GenericUtil.isEmpty(newMasterTablePk) ? requestParams.get(mt.get_tableParamList().get(0).getDsc())
						: newMasterTablePk);
		if (rl != null)
			for (Object o : rl) {
				if (multiKey) {
					int qi = 0;
					for (W5TableParam tp : t.get_tableParamList())
						if (!t.get_tableParamList().get(1).getExpressionDsc().equals("project_uuid")) {
							requestParams.put(tp.getDsc(), ((Object[]) o)[qi++].toString());
						}
				} else
					requestParams.put(t.get_tableParamList().get(0).getDsc(), o.toString());
				copyFormTable(formResult, schema, prefix, true);
				if(t.getVcsFlag()!=0 && !GenericUtil.isEmpty(formResult.getOutputFields())) {
					int tablePk2 = GenericUtil.uInt(formResult.getOutputFields().get(t.get_tableFieldList().get(0).getDsc()));
					
					W5VcsObject ivo = new W5VcsObject(scd, t.getTableId(), tablePk2);
					saveObject(ivo);
				}
				
				if (!GenericUtil.isEmpty(t.get_tableChildList())) {
					if (!GenericUtil.isEmpty(formResult.getOutputFields()))
						for (String k : formResult.getOutputFields().keySet()) {
							if (formResult.getOutputFields().get(k) != null)
								requestParams.put(k, formResult.getOutputFields().get(k).toString());
						}
					// String pcopyTablePk =
					// requestParams.get(t.get_tableParamList().get(0).getDsc());
					for (W5TableChild tc2 : t.get_tableChildList())
						if (tc2.getCopyStrategyTip() == 1) {
							copyFormTableDetail(formResult, tc2,
									requestParams.get(t.get_tableFieldList().get(0).getDsc()), schema, prefix);
							
						}
				}
			}
		return true;
		// throw new PromisException("security","Form",
		// t.getDefaultUpdateFormId(), null, "Tablo
		// Kontrol: Veri görünteleyemezsiniz", null);
	}

	public List<Map> getMainTableData(Map<String, Object> scd, int tableId, String tablePk) { // TODO:
																								// yetki
																								// eksik

		int count = 0;
		String customizationId = scd.get("customizationId").toString();
		W5Table table = FrameworkCache.getTable(customizationId, tableId);

		StringBuilder sql = new StringBuilder();
		List lp = new ArrayList();
		lp.add(tablePk);
		sql.append("select t.* ,");

		sql.replace(sql.length() - 1, sql.length(), " from " + table.getDsc() + "  t where");
		for (W5TableParam x : table.get_tableParamList()) {
			sql.append(" t." + x.getExpressionDsc() + "= ? and");
			if (x.getExpressionDsc().equals("project_uuid"))
				lp.add(scd.get("projectId"));
		}
		sql.replace(sql.length() - 3, sql.length(), "");

		return executeSQLQuery2Map(sql.toString(), lp);
	}

	public List<W5TableRecordHelper> findRecordParentRecords(Map<String, Object> scd, int tableId, int tablePk,
			int maxLevel, boolean includeSummarySqlFlag) { // TODO:includeSummarySqlFlag
															// ise yaramiyor
		List<W5TableRecordHelper> l = new ArrayList<W5TableRecordHelper>();
		// if(true)return l; bunu kim neden yapti ??
		W5Table t = FrameworkCache.getTable(scd, tableId);
		W5TableChild tc = null;
		long currentMillis = System.currentTimeMillis();
		Map<String, String> requestParams = new HashMap();
		int ptCount = 0; // parentCount
		int level = 0;
		if (maxLevel == 0)
			maxLevel = 10;
		while (t != null && tablePk != 0 && level < maxLevel) {
			level++;
			W5TableRecordHelper trh = null; // FrameworkCache.getTableCacheValue(t.getCustomizationId(),
											// tableId,tablePk);
			if (trh != null && (currentMillis - trh.getLastAccessTime()) < FrameworkSetting.cacheTimeoutRecord) { // caching
																													// den
																													// aliyorum

				trh.setLastAccessTime(currentMillis);
				l.add(trh);
				t = FrameworkCache.getTable(scd, trh.getParentTableId());
				tablePk = trh.getParentTablePk();
			} else if (!GenericUtil.isEmpty(t.get_tableParamList())) {
				requestParams.put(t.get_tableParamList().get(0).getDsc(), "" + tablePk);
				StringBuilder sql = new StringBuilder(512);
				sql.append("select ");
				if (t.getSummaryRecordSql() == null) // bir tane degeriini
														// koyacagiz
					sql.append("'WARNING!!! Not defined SummarySql' dsc");
				else {
					sql.append(t.getSummaryRecordSql());
					if (!t.getSummaryRecordSql().contains(" dsc,") && !t.getSummaryRecordSql().endsWith(" dsc"))
						sql.append(" dsc");
				}
				if (t.get_tableParentList() != null && t.get_tableParentList().size() > 0) {
					tc = t.get_tableParentList().get(0);
					W5TableField tf2 = t.get_tableFieldMap().get(tc.getRelatedTableFieldId());
					if(tf2!=null) {
						sql.append(",x.");
						sql.append(tf2.getDsc()).append(" ptable_pk");
						if (tc.getRelatedStaticTableFieldId() != 0) {
							sql.append(", x.").append(t.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc())
									.append(" pobject_tip");
							ptCount = t.get_tableParentList().size(); // multi:parent
						} else
							ptCount = 1; // single:parent
					} else 
						ptCount = 0;

				} else
					ptCount = 0;
				/*if (t.getMakeCommentFlag() != 0) {
					if (FrameworkCache.getTable(scd, FrameworkSetting.customCommentTableId)==null)
						sql.append(", (select count(1) from iwb.w5_comment cx where cx.table_id=").append(t.getTableId())
							.append(" AND cx.project_uuid='${scd.projectId}' AND cx.table_pk=x.")
							.append(t.get_tableParamList().get(0).getExpressionDsc()).append(") pcomment_count ");
					else 
						sql.append(", (select count(1) from x_comment cx where cx.table_id=").append(t.getTableId())
						.append(" AND cx.table_pk=x.")
						.append(t.get_tableParamList().get(0).getExpressionDsc()).append(") pcomment_count ");
				}*/

				sql.append(" from ").append(t.getDsc()).append(" x");

				sql.append(" where x.").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=${req.")
						.append(t.get_tableParamList().get(0).getDsc()).append("}");
				sql.append(DBUtil.includeTenantProjectPostSQL(scd, t));
				Object[] oz = DBUtil.filterExt4SQL(sql.toString(), scd, requestParams, null);
				Map<String, Object> m = runSQLQuery2Map(oz[0].toString(), (List) oz[1], null);
				trh = new W5TableRecordHelper();
				trh.setTableId(tableId);
				trh.setTablePk(tablePk);
				l.add(trh);

				if (m != null) {
					if (m.size() == 1) {
						Object o = m.values().iterator().next();
						if (o != null)
							trh.setRecordDsc(o.toString());
					} else
						trh.setRecordDsc((String) m.get("dsc"));
					if (t.getMakeCommentFlag() != 0)
						trh.setCommentCount(GenericUtil.uInt(m.get("pcomment_count")));
					trh.setCachedTime(currentMillis);
					trh.setLastAccessTime(trh.getCachedTime());
					// FrameworkCache.putTableCacheValue(t.getCustomizationId(),
					// tableId, tablePk,
					// trh);//cache le objeyi
					switch (ptCount) {
					case 0:
						return l;
					case 1: // single parent
						t = FrameworkCache.getTable(scd, tc.getTableId());
						trh.setParentTableId(tableId = t.getTableId());
						trh.setParentTablePk(tablePk = GenericUtil.uInt(m.get("ptable_pk")));
						break;
					default: // multi parent
						tableId = -1;
						trh.setParentTablePk(tablePk = GenericUtil.uInt(m.get("ptable_pk")));
						int pobjectTip = GenericUtil.uInt(m.get("pobject_tip"));
						for (W5TableChild tc2 : t.get_tableParentList())
							if (tc2.getRelatedStaticTableFieldVal() == pobjectTip) {
								trh.setParentTableId(tableId = tc2.getTableId());
								t = FrameworkCache.getTable(scd, tableId);
								break;
							}
						if (tableId == -1) { // problems, parent bulamamis
							for (W5TableChild tc2 : t.get_tableParentList())
								if (tc2.getRelatedStaticTableFieldId() == 0) {
									trh.setParentTableId(tableId = tc2.getTableId());
									t = FrameworkCache.getTable(scd, tableId);
									break;
								}
							if (tableId == -1) {
								W5TableRecordHelper trhError = new W5TableRecordHelper();
								trhError.setRecordDsc("ERROR: parent not found");
								l.add(trhError);
								return l;
							}
						}
					}

				} else
					t = null;
			}
		}
		return l;
	}

	public Object interprateTemplateExpression(Map<String, Object> scd, Map<String, String> requestParams, int tableId,
			int tablePk, StringBuilder tmp) {
		Map<String, String> res = new HashMap<String, String>();
		String result = "";
		// if(tmp.indexOf("${")<0)return res;
		Map<String, W5TableField> resField = new HashMap<String, W5TableField>();
		Set<String> invalidKeys = new HashSet<String>();
		StringBuilder tmp1 = new StringBuilder();
		tmp1.append(tmp);
		StringBuilder sql = new StringBuilder();
		List<Object> params = new ArrayList();
		sql.append("select ");
		List<W5TableFieldCalculated> ltfc = metadataLoader.findTableCalcFields((String)scd.get("projectId"), tableId);
		W5Table t = FrameworkCache.getTable(scd, tableId);
		for (int bas = tmp1.indexOf("${"); bas >= 0; bas = tmp1.indexOf("${", bas + 2)) {
			int bit = tmp1.indexOf("}", bas + 2);
			String subStr = tmp1.substring(bas + 2, bit);
			if (subStr.startsWith("scd.")) { // session
				Object o = scd.get(subStr.substring(4));
				tmp1.replace(bas, bit + 1, "?"); // .put(subStr, o.toString());
				params.add(o);
			} else if (subStr.startsWith("app.")) { // application settingsden
				String appStr = FrameworkCache.getAppSettingStringValue(scd, subStr.substring(4));
				tmp1.replace(bas, bit + 1, "?"); // .put(subStr, o.toString());
				params.add(appStr);
			} else if (subStr.startsWith("req.")) { // application settingsden
				String reqStr = requestParams.get(subStr.substring(4));
				tmp1.replace(bas, bit + 1, "?"); // .put(subStr, o.toString());
				params.add(reqStr);
			} else if (subStr.startsWith("tbl.")
					|| (subStr.startsWith("lnk.") && subStr.substring(4).replace(".", "&").split("&").length == 1)) { // direk
																														// ise
				String newSubStr = subStr.substring(4);
				boolean fieldFound = false;
				for (W5TableField tf : t.get_tableFieldList())
					if (tf.getDsc().equals(newSubStr)) {
						tmp1.replace(bas, bit + 1, "x." + newSubStr); // .put(subStr,
																		// o.toString());
						fieldFound = true;
						break;
					}

				if (!fieldFound)
					invalidKeys.add(subStr);
				;
			} else if (subStr.startsWith("clc.")) {
				boolean fieldFound = false;
				String newSubStr = subStr.substring(4);
				for (W5TableFieldCalculated tfc : ltfc)
					if (tfc.getDsc().equals(newSubStr)) {
						String sqlCode = tfc.getSqlCode();
						W5TableField ntf = new W5TableField(tfc.getTableFieldCalculatedId());
						ntf.setDefaultControlType((short) -1);
						if (sqlCode.contains("${")) {
							Object[] oz = DBUtil.filterExt4SQL(sqlCode, scd, requestParams, null);
							sqlCode = (String) oz[0];
							if (oz.length > 1)
								params.addAll((List) oz[1]);
						}
						tmp1.replace(bas, bit + 1, "(" + sqlCode + ")"); // .put(subStr,
																			// o.toString());
						fieldFound = true;
						break;
					}
				if (!fieldFound)
					invalidKeys.add(subStr);
				;
			} else if (subStr.startsWith("lnk.") && subStr.substring(4).replace(".", "&").split("&").length > 1) { // burda
																													// bu
																													// field
																													// ile
																													// olan
																													// baglantiyi
																													// cozmek
																													// lazim
																													// TODO
				String newSubStr = subStr.substring(4);

				String[] sss = newSubStr.replace(".", "&").split("&");
				if (sss.length > 1) { // TODO kaldirilmasi lazim
					W5Table newT = t;
					StringBuilder newSub = new StringBuilder();
					StringBuilder newSub2 = new StringBuilder();
					boolean foundSt = false;
					for (int isss = 0; isss < sss.length - 1; isss++) {
						if (isss > 0) {
							newSub2.setLength(0);
							newSub2.append(newSub);
							newSub.setLength(0);
						}
						for (W5TableField tf : newT.get_tableFieldList())
							if (tf.getDsc().equals(sss[isss])) {
								foundSt = false;
								if (tf.getDefaultControlType() == 7 || tf.getDefaultControlType() == 9
										|| tf.getDefaultControlType() == 10 /*
																			 * || tf. getDefaultControlTip ()== 15
																			 */) { // sub
																					// table
									W5Table st = FrameworkCache.getTable(scd, tf.getDefaultLookupTableId());
									if (st == null)
										break; // HATA: gerekli bir alt kademe
												// tabloya ulasilamadi
									int dltId = 0;
									for (W5TableField stf : st.get_tableFieldList())
										if (stf.getDsc().equals(sss[isss + 1])) {
											dltId = stf.getDefaultLookupTableId();
											foundSt = true;
											break;
										}
									if (!foundSt)
										break; // HATA: bir sonraki field
												// bulunamadi
									newSub.append("(select ");
									newSub.append("y").append(isss).append(".").append(sss[isss + 1]);
									newSub.append(" from ").append(st.getDsc()).append(" y").append(isss)
											.append(" where y").append(isss).append(".")
											.append(st.get_tableFieldList().get(0).getDsc()).append("=")
											.append(isss == 0 ? ("x." + sss[isss]) : newSub2);
									if (st.get_tableFieldList().size() > 1)
										for (W5TableField wtf : st.get_tableFieldList())
											if (wtf.getDsc().equals("project_uuid")) {
												newSub.append(" AND y").append(isss).append(".project_uuid=?");
												params.add(scd.get("projectId"));
												break;
											}

									newSub.append(")");
									newT = st;
								}
								break;
							}
						if (!foundSt) { // bulamamis uygun sey
							break;
						}
					}
					if (foundSt && newSub.length() > 0) {
						tmp1.replace(bas, bit + 1, "(" + newSub.toString() + ")"); // .put(subStr,
																					// o.toString());
					}
				}
			}
		}
		sql.append(tmp1).append(" xxx from ").append(t.getDsc()).append(" x where x.")
				.append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
		params.add(tablePk);
		sql.append(DBUtil.includeTenantProjectPostSQL(scd, t));
		Map<String, Object> qres = runSQLQuery2Map(sql.toString(), params, null);
		return GenericUtil.isEmpty(qres) ? "" : qres.get("xxx");
	}

	public Map<String, String> interprateTemplate(Map<String, Object> scd, Map<String, String> requestParams,
			int tableId, int tablePk, StringBuilder tmp, boolean replace, int smsMailReplaceTip, int conversionTip) {
		Map<String, String> res = new HashMap<String, String>();
		if (conversionTip == 4) {
			Object o = interprateTemplateExpression(scd, requestParams, tableId, tablePk, tmp);
			res.put("result", o.toString());
			if (replace) {
				tmp.setLength(0);
				tmp.append(o);
			}
			return res;
		}
		if (tmp.indexOf("${") < 0)
			return res;
		Map<String, W5TableField> resField = new HashMap<String, W5TableField>();
		Set<String> invalidKeys = new HashSet<String>();
		// ${req.param} -> request'ten ne geldiyse
		// ${scd.param} -> session'dan ne geldiyse
		// ${app.param} -> app_setting'ten ne geldiyse
		// ${lcl.locale_msg_key} -> locale_msg tablosundan bakilacak
		// ${tbl.field_name} -> direk field ismi
		// ${lnk.field_name1.field_name2} -> link oldugu tablodaki field ismi
		// ${lnk.field_name1.field_name2.field_name3} -> link oldugu tablodaki
		// field in linkindeki diger
		// vs.vs.
		// smsMailReplaceTip : 0->yok, 1:sms, 2:mail
		// conversionTip : 0->Serbest, 1: lookup-mapping, 2: sql, 3: javascript,
		// 4: expression, 5:
		// serbest&''
		Set<Integer> smsMailTableIds = null;
		if (smsMailReplaceTip != 0) {
			smsMailTableIds = new HashSet<Integer>();
			String qs = FrameworkCache.getAppSettingStringValue(scd, "sms_mail_table_ids");
			if (qs != null) {
				String[] oqs = qs.split(",");
				if (oqs != null)
					for (String toqs : oqs)
						smsMailTableIds.add(GenericUtil.uInteger(toqs));
			}
		}
		String fieldPrefix = "fx_q1_";
		StringBuilder sql = new StringBuilder();
		List<Object> params = new ArrayList();
		sql.append("select ");
		int field_cnt = 1;
		List<W5TableFieldCalculated> ltfc = metadataLoader.findTableCalcFields(
				FrameworkCache.getProjectId(scd.get("projectId"), "15." + tableId), tableId);
		W5Table t = FrameworkCache.getTable(scd, tableId);
		for (int bas = tmp.indexOf("${"); bas >= 0; bas = tmp.indexOf("${", bas + 2)) {
			if (bas > 0 && tmp.charAt(bas - 1) == '$')
				continue;
			int bit = tmp.indexOf("}", bas + 2);
			String subStr = tmp.substring(bas + 2, bit);
			if (res.containsKey(subStr))
				continue; // daha once hesaplandiysa bir daha gerek yok
			if (subStr.startsWith("scd.")) { // session
				Object o = scd.get(subStr.substring(4));
				if (o != null)
					res.put(subStr, o.toString());
			} else if (subStr.startsWith("app.")) { // application settingsden
				String appStr = FrameworkCache.getAppSettingStringValue(scd, subStr.substring(4));
				if (appStr != null)
					res.put(subStr, appStr);
			} else if (subStr.startsWith("req.")) { // requestten
				String reqStr = requestParams.get(subStr.substring(4));
				if (reqStr != null)
					res.put(subStr, reqStr);
			} else if (subStr.startsWith("tbl.") || (smsMailReplaceTip == 0 && subStr.startsWith("lnk.")
					&& conversionTip != 0 && subStr.substring(4).replace(".", "&").split("&").length == 1)) { // direk
																												// tablodan
																												// veya
																												// link'in
																												// ilk
																												// kaydi
																												// ise
				String newSubStr = subStr.substring(4);
				for (W5TableField tf : t.get_tableFieldList())
					if (tf.getDsc().equals(newSubStr)) {
						if (tf.getDefaultControlType() == 15) {
							W5Table st = FrameworkCache.getTable(scd, tf.getDefaultLookupTableId());
							if (st != null && smsMailReplaceTip != 0 && smsMailTableIds.contains(st.getTableId())) { // eger
																														// sms/mail
																														// isi
																														// ve
																														// bu
																														// bir
																														// link
																														// ise
								res.put(subStr, fieldPrefix + field_cnt);
								resField.put(subStr, tf);
								sql.append("iwb.fnc_sms_mail_adress_multi(?,").append(st.getTableId()).append(",")
										.append("x.").append(newSubStr).append(",").append(smsMailReplaceTip)
										.append(") ").append(fieldPrefix).append(field_cnt).append(",");
								;
								params.add(scd.get("userRoleId"));
								field_cnt++;
							}
						} else {
							res.put(subStr, fieldPrefix + field_cnt);
							resField.put(subStr, tf);
							if (tf.getFieldType() == 2)
								sql.append("to_char(x.").append(newSubStr).append(",'").
								append(dateFormatMulti[scd!=null ? GenericUtil.uInt(scd.get("date_format")):0])
								.append("')");
							else
								sql.append("x.").append(newSubStr);
							sql.append(" ").append(fieldPrefix).append(field_cnt).append(",");
							field_cnt++;
						}
						break;
					}
				if (!res.containsKey(subStr))
					invalidKeys.add(subStr);
				;
			} else if (subStr.startsWith("lcl.")) { // locale msg key
				continue;
			} else if (subStr.startsWith("clc.")) { // calculated field
				String newSubStr = subStr.substring(4);
				for (W5TableFieldCalculated tfc : ltfc)
					if (tfc.getDsc().equals(newSubStr)) {
						String sqlCode = tfc.getSqlCode();
						res.put(subStr, fieldPrefix + field_cnt);
						W5TableField ntf = new W5TableField(tfc.getTableFieldCalculatedId());
						ntf.setDefaultControlType((short) -1);
						resField.put(subStr, ntf);
						if (sqlCode.contains("${")) {
							Object[] oz = DBUtil.filterExt4SQL(sqlCode, scd, requestParams, null);
							sqlCode = oz[0].toString();
							if (oz.length > 1)
								params.addAll((List) oz[1]);
						}
						if (tfc.getFieldType() == 2)
							sql.append("to_char((").append(sqlCode).append("),'")
							.append(dateFormatMulti[scd!=null ? GenericUtil.uInt(scd.get("date_format")):0])
							.append("')");
						else
							sql.append("(").append(sqlCode).append(")");
						sql.append(" ").append(fieldPrefix).append(field_cnt).append(",");
						field_cnt++;
						break;
					}
				if (!res.containsKey(subStr))
					invalidKeys.add(subStr);
			} else if (subStr.startsWith("lnk.")) { // this is a link, tels resolve
				String newSubStr = subStr.substring(4);

				String[] sss = newSubStr.replace(".", "&").split("&");
				if (sss.length > 1) {
					W5Table newT = t;
					StringBuilder newSub = new StringBuilder();
					StringBuilder newSub2 = new StringBuilder();
					for (int isss = 0; isss < sss.length - 1; isss++) {
						if (isss > 0) {
							newSub2.setLength(0);
							newSub2.append(newSub);
							newSub.setLength(0);
						}
						for (W5TableField tf : newT.get_tableFieldList())
							if (tf.getDsc().equals(sss[isss])) {
								if (tf.getDefaultControlType() == 7 || tf.getDefaultControlType() == 9
										|| tf.getDefaultControlType() == 10 /*
																			 * || tf. getDefaultControlTip ()== 15
																			 */) { // sub
																					// table
									W5Table st = FrameworkCache.getTable(scd, tf.getDefaultLookupTableId());
									if (st == null)
										break; // HATA: gerekli bir alt kademe
												// tabloya ulasilamadi
									boolean foundSt = false;
									boolean summaryMust = false;
									int dltId = 0;
									for (W5TableField stf : st.get_tableFieldList())
										if (stf.getDsc().equals(sss[isss + 1])) {
											summaryMust = (conversionTip == 0)
													&& (stf.getDefaultControlType() == 7
															|| stf.getDefaultControlType() == 9
															|| stf.getDefaultControlType() == 10)
													&& stf.getDefaultLookupTableId() != 0;
											dltId = stf.getDefaultLookupTableId();
											foundSt = true;
											break;
										}
									if (!foundSt)
										break; // HATA: bir sonraki field
												// bulunamadi
									newSub.append("(select ");
									if (isss == sss.length - 2 && summaryMust) { // burda
																					// t'deki
																					// summary
																					// record
																					// sql
																					// varsa
										if (smsMailReplaceTip != 0) {
											if (smsMailTableIds.contains(dltId)) // eger
																					// sms/mail
																					// isi
																					// ve
																					// bu
																					// bir
																					// link
																					// ise
												newSub.append("iwb.fnc_sms_mail_adress(?,").append(dltId).append(",")
														.append("y").append(isss).append(".").append(sss[isss + 1])
														.append(",").append(smsMailReplaceTip).append(")");
											else
												break; // HATA: sms mail tipinde
														// olmasi gereken link,
														// degil
										} else
											newSub.append("iwb.fnc_lookup_table_summary(?,").append(dltId).append(",")
													.append("y").append(isss).append(".").append(sss[isss + 1])
													.append(")");
										params.add(scd.get("userRoleId"));
									} else
										newSub.append("y").append(isss).append(".").append(sss[isss + 1]);
									newSub.append(" from ").append(st.getDsc()).append(" y").append(isss)
											.append(" where y").append(isss).append(".")
											.append(st.get_tableFieldList().get(0).getDsc()).append("=")
											.append(isss == 0 ? ("x." + sss[isss]) : newSub2);
									if (st.get_tableFieldList().size() > 1)
										for (W5TableField wtf : st.get_tableFieldList())
											if (wtf.getDsc().equals("project_uuid")) {
												newSub.append(" AND y").append(isss).append(".project_uuid=?");
												params.add(scd.get("projectId"));
												break;
											}

									newSub.append(")");
									newT = st;
								}
								break;
							}
						if (newSub.length() == 0) { // bulamamis uygun sey
							break;
						}
					}
					if (newSub.length() > 0) {
						sql.append(newSub).append(" ").append(fieldPrefix).append(field_cnt).append(",");
						res.put(subStr, fieldPrefix + field_cnt);
						field_cnt++;
						for (W5TableField tf : newT.get_tableFieldList())
							if (tf.getDsc().equals(sss[sss.length - 1]))
								resField.put(subStr, tf);
					}
				} else if (sss.length == 1) { // direk lnk.field_name seklinde
												//  icin
					for (W5TableField tf : t.get_tableFieldList())
						if (tf.getDsc().equals(sss[0])) {
							if ((conversionTip == 0) && (tf.getDefaultControlType() == 7
									|| tf.getDefaultControlType() == 9 || tf.getDefaultControlType() == 10)
									&& tf.getDefaultLookupTableId() != 0) { // sub
																			// table
								W5Table st = FrameworkCache.getTable(scd, tf.getDefaultLookupTableId());
								if (st == null)
									break; // HATA: gerekli bir alt kademe
											// tabloya ulasilamadi

								if (smsMailReplaceTip != 0) {
									if (smsMailTableIds.contains(tf.getDefaultLookupTableId())) // it is mail and link
										sql.append("iwb.fnc_sms_mail_adress(?,").append(tf.getDefaultLookupTableId())
												.append(",").append("x.").append(sss[0]).append(",")
												.append(smsMailReplaceTip).append(")");
									else
										break; // HATA: sms mail tipinde olmasi
												// gereken link, degil
								} else
									sql.append("iwb.fnc_lookup_table_summary(?,").append(tf.getDefaultLookupTableId())
											.append(",").append("x.").append(sss[0]).append(")");
								params.add(scd.get("userRoleId"));
							} else
								sql.append("x.").append(sss[0]);
							sql.append(" ").append(fieldPrefix).append(field_cnt).append(",");
							res.put(subStr, fieldPrefix + field_cnt);
							field_cnt++;
						}
				}
				if (!res.containsKey(subStr))
					invalidKeys.add(subStr);

			} else
				invalidKeys.add(subStr);
			;
		}
		Map<String, String> newRes = new HashMap<String, String>();
		Map<String, Object> qres = null;

		if (field_cnt > 1) {
			sql.append("1 from ").append(t.getDsc()).append(" x where x.")
					.append(t.get_tableFieldList().get(0).getDsc()).append("=?");
			params.add(tablePk);
			if (t.get_tableFieldList().size() > 1 && (t.getTableId() != 338))
				for (W5TableField tf2 : t.get_tableFieldList())
					if (tf2.getDsc().equals("project_uuid")) {
						sql.append(" AND x.project_uuid=?");
						params.add(scd.get("projectId"));
						break;
					}
			qres = runSQLQuery2Map(sql.toString(), params, null);
		} else
			qres = new HashMap();

		for (String keyz : res.keySet()) {
			if (res.get(keyz) != null) {
				if (keyz.startsWith("req.") || keyz.startsWith("scd.") || keyz.startsWith("app.")
						|| keyz.equals("company_logo"))
					newRes.put(keyz, res.get(keyz));
				else if (qres != null && qres.get(res.get(keyz)) != null)
					newRes.put(keyz, qres.get(res.get(keyz)).toString());
			}
		}
		if (replace)
			for (int bas = tmp.indexOf("${"); bas >= 0; bas = tmp.indexOf("${", bas + 2)) {
				if (bas > 0 && tmp.charAt(bas - 1) == '$') {
					tmp.replace(bas, bas + 1, "");
					continue;
				}
				int bit = tmp.indexOf("}", bas + 2);
				String subStr = tmp.substring(bas + 2, bit);
				if (subStr.startsWith("lcl.")) { // locale msg key
					tmp.replace(bas, bit + 1, LocaleMsgCache.get2(scd, subStr.substring(4)));
					continue;
				}
				String resStr = newRes.get(subStr);
				if (resStr == null) {
					if (invalidKeys.contains(subStr)) {
						resStr = "[ERR:" + GenericUtil.uStrMax(subStr, 50) + "]";
					} else
						resStr = "";
				} else if (conversionTip == 0) { // eger sadece serbest donusum
													// exists ise
					W5TableField tf = resField.get(subStr);
					if (tf != null)
						switch (tf.getDefaultControlType()) {
						case 6: // combo static
							if (tf.getDefaultLookupTableId() != 0) {
								W5LookUp lu = FrameworkCache.getLookUp(scd, tf.getDefaultLookupTableId());
								if (lu != null) {
									resStr = lu.get_detayMap().get(resStr).getDsc();
									resStr = LocaleMsgCache.get2(scd, resStr);
								}
							}
							break;
						case 8: // tree-combo static
							if (tf.getDefaultLookupTableId() != 0) {
								W5LookUp lu = FrameworkCache.getLookUp(scd, tf.getDefaultLookupTableId());
								if (lu != null) {
									String[] sss = resStr.split(",");
									if (sss != null && sss.length > 0) {
										String tstr = "";
										for (String s : sss) {
											String qs = lu.get_detayMap().get(s).getDsc();
											qs = LocaleMsgCache.get2(scd, qs);
											tstr += qs + ", ";
										}
										if (tstr.length() > 2)
											resStr = tstr.substring(0, tstr.length() - 2);
									}
								}
							}
							break;
						case -1: // demek ki, calculated Field
							break;
						}
				}
				tmp.replace(bas, bit + 1, conversionTip == 0 ? resStr : "'" + resStr + "'");
			}
		else if (conversionTip == 0)
			for (String subStr : resField.keySet()) { // replace yok ama
														// herseyin degismesi
														// gerek
				W5TableField tf = resField.get(subStr);
				if (tf != null)
					switch (tf.getDefaultControlType()) {
					case 6: // combo static
						if (tf.getDefaultLookupTableId() != 0) {
							W5LookUp lu = FrameworkCache.getLookUp(scd, tf.getDefaultLookupTableId());
							if (lu != null) {
								String resStr = lu.get_detayMap().get(newRes.get(subStr)).getDsc();
								resStr = LocaleMsgCache.get2(scd, resStr);
								newRes.put(subStr, resStr);
							}
						}
						break;
					case 8: // tree-combo static
						if (tf.getDefaultLookupTableId() != 0) {
							W5LookUp lu = FrameworkCache.getLookUp(scd, tf.getDefaultLookupTableId());
							if (lu != null) {
								String[] sss = newRes.get(subStr).split(",");
								if (sss != null && sss.length > 0) {
									String tstr = "";
									for (String s : sss) {
										String qs = lu.get_detayMap().get(s).getDsc();
										qs = LocaleMsgCache.get2(scd, qs);
										tstr += qs + ", ";
									}
									if (tstr.length() > 2)
										newRes.put(subStr, tstr.substring(0, tstr.length() - 2));
								}
							}
						}
						break;
					}
			}
		switch (conversionTip) {
		case 3: // JavaScript
			Object resq = scriptEngine.executeScript(scd, requestParams, tmp.toString(), null,
					"cnv_it_" + tableId + "_" + tablePk);

			tmp.setLength(0);
			if (resq != null)
				tmp.append(resq);
			if (tmp.length() > 2) {
				int tl = tmp.length();
				if (tmp.charAt(tl - 2) == '.' && tmp.charAt(tl - 1) == '0') {
					Integer nv = GenericUtil.uInteger(tmp.substring(0, tl - 2));
					if (nv != null)
						tmp.setLength(tl - 2);
				}
			}
			break;
		}
		return newRes;
	}

	public boolean accessUserFieldControl(W5Table t, String accessUserFields, Map<String, Object> scd,
			Map<String, String> requestParams, String paramSuffix) {
		if (paramSuffix == null)
			paramSuffix = "";
		if (accessUserFields == null) {
			return true;
		}
		StringBuilder sql = new StringBuilder();
		sql.append("select 1 a from ").append(t.getDsc()).append(" t where (");
		String[] fieldIdz = accessUserFields.split(",");
		List<Object> params = new ArrayList();
		boolean bq = false;
		for (String s : fieldIdz) {
			int tableFieldId = GenericUtil.uInt(s);
			if (tableFieldId < 0) { // TODO: for conditionSQL
				continue;
			}
			W5TableField tf = t.get_tableFieldMap().get(tableFieldId);
			if (tf != null) {
				if (bq)
					sql.append(" OR ");
				else
					bq = true;
				sql.append("t.").append(tf.getDsc()).append("=?");
				params.add(scd.get("userId"));
			} 
		}
		sql.append(")");

		Map errorMap = new HashMap();
		for (W5TableParam x : t.get_tableParamList()) {
			sql.append("AND t.").append(x.getExpressionDsc()).append(" = ? ");
			Object presult = GenericUtil.prepareParam((W5Param) x, scd, requestParams, (short) -1, null, (short) 1,
					x.getDsc() + paramSuffix, null, errorMap);
			params.add(presult);
		}
		if (!errorMap.isEmpty())
			return true; // baska yerde yapsin error
		Map<String, Object> m = runSQLQuery2Map(sql.toString(), params, null);
		return m == null || m.isEmpty();
	}



	public boolean conditionRecordExistsCheck(Map<String, Object> scd, Map<String, String> requestParams, int tableId,
			int tablePk, String conditionSqlCode) {
		W5Table t = FrameworkCache.getTable(scd, tableId);
		StringBuilder sql = new StringBuilder();
		sql.append("select 1 from ").append(t.getDsc()).append(" x where x.")
				.append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
		List params = new ArrayList();
		params.add(tablePk);
		sql.append(DBUtil.includeTenantProjectPostSQL(scd, t)).append(" AND ");
		Object[] oz = DBUtil.filterExt4SQL(conditionSqlCode, scd, new HashMap(), null);
		sql.append(oz[0]);
		if (oz.length > 1 && oz[1] != null)
			params.addAll((List) oz[1]);
		List l = executeSQLQuery2(sql.toString(), params);
		return !GenericUtil.isEmpty(l);
	}

	private void addPostQueryFields(W5QueryResult queryResult, StringBuilder sql2, AtomicInteger paramIndex) {
		W5Query query = queryResult.getQuery();
		W5Table mainTable = queryResult.getMainTable();
		int customizationId = (Integer) queryResult.getScd().get("customizationId");
		String projectId =(String) queryResult.getScd().get("projectId");
		String pkFieldName = query.getQueryType() == 9 || query.getQueryType() == 21 || query.getQueryType() == 22 ? query.get_queryFields().get(0).getDsc() : "pkpkpk_id";
		if (FrameworkSetting.vcs && mainTable.getVcsFlag() != 0 /*
																 * && query.getSqlSelect().startsWith("x.*")
																 */) { // VCS
			// sql2.append(",(select cx.vcs_object_status_tip from
			// iwb.w5_vcs_object cx where
			// cx.table_id=").append(query.getMainTableId()).append(" AND
			// cx.customization_id=").append(customizationId).append(" AND
			// cx.table_pk=z.pkpkpk_id)
			// ").append(FieldDefinitions.queryFieldName_Vcs).append(" ");
			//
			// sql2.append(",fnc_vcs_status(").append(customizationId).append(",'").append(queryResult.getScd().get("projectId")).append("',").append(query.getMainTableId()).append(",z.pkpkpk_id,").append(FrameworkUtil.getTableFields4VCS(mainTable,"z")).append(")
			// ").append(FieldDefinitions.queryFieldName_Vcs).append(" ");
			sql2.append(",iwb.fnc_vcs_status2(").append(customizationId).append(",'")
					.append(projectId).append("',").append(query.getSourceObjectId())
					.append(",z.").append(pkFieldName).append(") ").append(FieldDefinitions.queryFieldName_Vcs)
					.append(" ");
			W5QueryField field = new W5QueryField();
			field.setDsc(FieldDefinitions.queryFieldName_Vcs);
			queryResult.getPostProcessQueryFields().add(field);
		}
		
		if (FrameworkCache.getAppSettingIntValue(queryResult.getScd(), "file_attachment_flag") != 0
				&& mainTable.getFileAttachmentFlag() != 0 && FrameworkCache.roleAccessControl(queryResult.getScd(), 101)
				&& (queryResult.getQueryColMap() == null
						|| queryResult.getQueryColMap().containsKey(FieldDefinitions.queryFieldName_FileAttachment))) { // fileAttachment
			// sql2.append(",(select 1 from iwb.w5_file_attachment cx where
			// rownum=1 AND
			// cx.table_id=").append(query.getMainTableId()).append(" AND
			// cx.customization_id=").append(customizationId).append(" AND
			// cx.table_pk=to_char(z.pkpkpk_id)) pkpkpk_faf ");
			if(FrameworkCache.getTable(queryResult.getScd(), FrameworkSetting.customFileTableId)==null)
				sql2.append(",(select count(1) from iwb.w5_file_attachment cx where cx.table_id=")
					.append(query.getSourceObjectId()).append(" AND cx.project_uuid='").append(projectId)
					.append("' AND cx.table_pk=z.").append(pkFieldName).append("::text) ");
			else 
				sql2.append(",(select count(1) from x_file cx where cx.table_id=")
				.append(query.getSourceObjectId()).append(" AND cx.table_pk=z.").append(pkFieldName).append(") ");
			
			sql2.append(FieldDefinitions.queryFieldName_FileAttachment).append(" ");
			W5QueryField field = new W5QueryField();
			field.setDsc(FieldDefinitions.queryFieldName_FileAttachment);
			queryResult.getPostProcessQueryFields().add(field);
		}
		if (FrameworkCache.getAppSettingIntValue(queryResult.getScd(), "make_comment_flag") != 0
				&& mainTable.getMakeCommentFlag() != 0 && (queryResult.getQueryColMap() == null
						|| queryResult.getQueryColMap().containsKey(FieldDefinitions.queryFieldName_Comment))) { // comment
			// sql2.append(",(select 1 from iwb.w5_comment cx where rownum=1 AND
			// cx.table_id=").append(query.getMainTableId()).append(" AND
			// cx.customization_id=").append(customizationId).append(" AND
			// cx.table_pk=z.").append(pkFieldName).append(") pkpkpk_cf ");
			W5QueryField field = new W5QueryField();
			field.setDsc(FieldDefinitions.queryFieldName_Comment);
			if (FrameworkCache.getTable(queryResult.getScd(), FrameworkSetting.customCommentTableId)==null) {
				sql2.append(",(select count(1) from iwb.w5_comment cx where cx.table_id=")
				.append(query.getSourceObjectId()).append(" AND cx.project_uuid='")
				.append(projectId).append("'  AND cx.table_pk::int=z.")
				.append(pkFieldName).append(") ").append(FieldDefinitions.queryFieldName_Comment)
				.append(" ");
			} else {
				sql2.append(",(select count(1) from x_comment cx where cx.table_id=")
						.append(query.getSourceObjectId()).append(" AND cx.table_pk=z.")
						.append(pkFieldName).append(") ").append(FieldDefinitions.queryFieldName_Comment)
						.append(" ");
			}
			queryResult.getPostProcessQueryFields().add(field);
		}
		if (FrameworkSetting.workflow
				&& mainTable.get_approvalMap() != null && !mainTable.get_approvalMap().isEmpty()
				&& (queryResult.getQueryColMap() == null
						|| queryResult.getQueryColMap().containsKey(FieldDefinitions.queryFieldName_Workflow))) { // approval
																													// Record
			sql2.append(
					",(select cx.approval_record_id||';'||cx.approval_id||';'||cx.approval_step_id||';'||coalesce(cx.approval_roles,'')||';'||coalesce(cx.approval_users,'') from iwb.w5_approval_record cx where cx.table_id=")
					.append(query.getSourceObjectId()).append(" AND cx.project_uuid='").append(projectId)
					.append("' AND cx.table_pk=z.").append(pkFieldName).append(" limit 1) ")
					.append(FieldDefinitions.queryFieldName_Workflow).append(" ");
			W5QueryField field = new W5QueryField();
			field.setDsc(FieldDefinitions.queryFieldName_Workflow);
			field.setPostProcessType((short) 49); // approvalPostProcessTip2
			queryResult.getPostProcessQueryFields().add(field);
			if (FrameworkCache.getAppSettingIntValue(queryResult.getScd(), "toplu_onay") != 0) {
				sql2.append(",(select cx.version_no from iwb.w5_approval_record cx where cx.table_id=")
						.append(query.getSourceObjectId()).append(" AND cx.project_uuid='").append(projectId)
						.append("' AND cx.table_pk=z.").append(pkFieldName).append(" limit 1) ")
						.append(FieldDefinitions.queryFieldName_ArVersionNo).append(" ");
				field = new W5QueryField();
				field.setDsc(FieldDefinitions.queryFieldName_ArVersionNo);
				queryResult.getPostProcessQueryFields().add(field);
			}
		}
	}


	public List<W5TableChildHelper> findRecordChildRecords(Map<String, Object> scd, int tableId, int tablePk) {
		W5Table t = FrameworkCache.getTable(scd, tableId);
		if (t == null || GenericUtil.isEmpty(t.get_tableChildList()))
			return null;
		List<W5TableChildHelper> r = new ArrayList<W5TableChildHelper>(t.get_tableChildList().size());
		for (W5TableChild tc : t.get_tableChildList()) {
			W5Table ct = FrameworkCache.getTable(scd, tc.getRelatedTableId());
			if (ct == null) {
				logger.error("ERROR(findRecordChildRecords) for relatedTableId=" + tc.getRelatedTableId());
				continue;
			}
			switch (ct.getAccessViewTip()) {
			case 0:
				if (!FrameworkCache.roleAccessControl(scd, 0)) {
					continue;
				}
				break;
			case 1:
				if (ct.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, ct.getAccessViewTip(),
						ct.getAccessViewRoles(), ct.getAccessViewUsers())) {
					continue;
				}
			}
			StringBuilder sql = new StringBuilder();
			sql.append("select count(1) xcount");
			if (ct.getMakeCommentFlag() != 0) {
				if (FrameworkCache.getTable(scd, FrameworkSetting.customCommentTableId)==null)
					sql.append(",sum((select count(1) from iwb.w5_comment c where c.project_uuid='")
						.append(scd.get("projectId")).append("' AND c.table_id=").append(ct.getTableId())
						.append(" AND c.table_pk=x.").append(ct.get_tableParamList().get(0).getExpressionDsc())
						.append(")) xcomment_count");
				else 
					sql.append(",sum((select count(1) from x_comment c where c.table_id=").append(ct.getTableId())
					.append(" AND c.table_pk=x.").append(ct.get_tableParamList().get(0).getExpressionDsc())
					.append(")) xcomment_count");
			}
			if (ct.getFileAttachmentFlag() != 0) {
				if(FrameworkCache.getTable(scd, FrameworkSetting.customFileTableId)==null)
					sql.append(",sum((select count(1) from iwb.w5_file_attachment c where c.project_uuid='")
						.append(scd.get("projectId")).append("' AND c.table_id=").append(ct.getTableId())
						.append(" AND c.table_pk=x.").append(ct.get_tableParamList().get(0).getExpressionDsc())
						.append("::text)) xfile_count");
				else 
					sql.append(",sum((select count(1) from x_file c where c.table_id=").append(ct.getTableId())
					.append(" AND c.table_pk=x.").append(ct.get_tableParamList().get(0).getExpressionDsc())
					.append(")) xfile_count");
			}
			sql.append(" from ").append(ct.getDsc()).append(" x where x.")
					.append(ct.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc()).append("=")
					.append(tablePk);
			if (tc.getRelatedStaticTableFieldId() != 0) {
				sql.append(" AND x.").append(ct.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc())
						.append("=").append(tc.getRelatedStaticTableFieldVal());
			}
			sql.append(DBUtil.includeTenantProjectPostSQL(scd, ct));
			if (FrameworkSetting.tableChildrenMaxRecordNumber > 0)
				sql.append(" limit ").append(FrameworkSetting.tableChildrenMaxRecordNumber);
			/*
			 * //TODO. burda bir de iwb.w5_access_control ve approval yapilacak
			 * if(ct.getAccessTips()!=null && PromisUtil.hasPartInside2(ct.getAccessTips(),
			 * "0")){ sql.append(
			 * " left outer join iwb.w5_access_control ac on ac.ACCESS_TIP=0 AND ac.table_id="
			 * ).append(t.getTableId()).append(
			 * " AND ac.customization_id=${scd.customizationId} AND ac.table_pk=x."
			 * ).append(t.get_tableParamList().get(0).getExpressionDsc()); }
			 */
			List<Map> l = executeSQLQuery2Map(sql.toString(), null);
			if (!GenericUtil.isEmpty(l)) {
				Map m = l.get(0);
				r.add(new W5TableChildHelper(tc, GenericUtil.uInt(m, "xcount"), GenericUtil.uInt(m, "xcomment_count"),
						GenericUtil.uInt(m, "xfile_count")));
			}
		}
		return r;
	}
	

	public void findRecordChildRecords4Copy(Map<String, String> tbMap, Map<String, Object> scd, int tableId, int tablePk) {
		if(tbMap.containsKey(tableId+"."+tablePk))return;
		
		W5Table t = FrameworkCache.getTable(scd, tableId);
		if (t == null)return;
		
		StringBuilder sql2=new StringBuilder();
		
		sql2.append("select o.vcs_commit_record_hash hh from iwb.w5_vcs_object o where o.table_id=").append(tableId)
		.append(" AND o.table_pk=").append(tablePk)
				.append(" AND o.project_uuid='").append(scd.get("projectId")).append("'");
		List<Object> l2 = executeSQLQuery(sql2.toString());
		if(l2==null)return;
		tbMap.put(tableId+"."+tablePk, l2.get(0).toString());
//		if(true)return;
		if(!GenericUtil.isEmpty(t.get_tableChildList()))for (W5TableChild tc : t.get_tableChildList()) {
			W5Table ct = FrameworkCache.getTable(scd, tc.getRelatedTableId());
			
			StringBuilder sql = new StringBuilder();
			sql.append("select x.").append(ct.get_tableFieldList().get(0).getDsc());
			
			sql.append(" id from ").append(ct.getDsc()).append(" x where x.")
					.append(ct.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc()).append("=")
					.append(tablePk);
			if (tc.getRelatedStaticTableFieldId() != 0) {
				sql.append(" AND x.").append(ct.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc())
						.append("=").append(tc.getRelatedStaticTableFieldVal());
			}
			sql.append(DBUtil.includeTenantProjectPostSQL(scd, ct));

			List<Object> l = executeSQLQuery(sql.toString());
			if (!GenericUtil.isEmpty(l)) for(Object o:l){
				findRecordChildRecords4Copy(tbMap, scd, tc.getRelatedTableId(), GenericUtil.uInt(o));
			}
		}
	}


	public boolean accessControlTable(Map<String, Object> scd, W5Table t, Integer tablePk) {
		if (!GenericUtil.accessControlTable(scd, t))
			return false;
		if (tablePk != null && (!GenericUtil.isEmpty(t.get_approvalMap()) || (t.getAccessViewTip() != 0
				))) { // TODO
																					// :
																					// ekstra
																					// record
																					// bazli
																					// kontrol
		}
		return true;
	}

	public W5TableRecordInfoResult getTableRecordInfo(Map<String, Object> scd, int tableId, int tablePk) {
		W5TableRecordInfoResult result = new W5TableRecordInfoResult(scd, tableId, tablePk);
		checkTenant(scd);
		W5Table t = FrameworkCache.getTable(scd, tableId);
		if (t == null || !accessControlTable(scd, t, tablePk))
			return null;
		Map<String, W5TableField> fieldMap1 = new HashMap();
		for (W5TableField tf : (List<W5TableField>) t.get_tableFieldList()) {
			fieldMap1.put(tf.getDsc(), tf);
		}

		if ((fieldMap1.get("INSERT_USER_ID") != null || fieldMap1.get("insert_user_id") != null)
				&& (fieldMap1.get("VERSION_USER_ID") != null || fieldMap1.get("version_user_id") != null)) {
			StringBuilder sql = new StringBuilder();
			List params = new ArrayList();
			sql.append("select x.version_no, x.insert_user_id, x.insert_dttm, x.version_user_id, x.version_dttm from ")
					.append(t.getDsc()).append(" x where x.").append(t.get_tableParamList().get(0).getExpressionDsc())
					.append("=?");
			params.add(tablePk);
			sql.append(DBUtil.includeTenantProjectPostSQL(scd, t));

			List<Map> l = executeSQLQuery2Map(sql.toString(), params);
			if (!GenericUtil.isEmpty(l)) {
				Map m = l.get(0);
				result.setVersionNo(GenericUtil.uInt(m, "version_no"));
				result.setInsertUserId(GenericUtil.uInt(m, "insert_user_id"));
				result.setInsertDttm((String) m.get("insert_dttm"));
				result.setVersionUserId(GenericUtil.uInt(m, "version_user_id"));
				result.setVersionDttm((String) m.get("version_dttm"));
			}
		}
		int extraSqlCount = 0;
		StringBuilder extraSql = new StringBuilder();

		/*
		 * if(FrameworkCache.getAppSettingIntValue(scd, "file_attachment_flag")!=0 &&
		 * t.getFileAttachmentFlag()!=0){ extraSql.append(
		 * "(select count(1) cnt from iwb.w5_file_attachment x where x.customization_id=? AND x.table_id=? AND x.table_pk=?::text) file_attach_count"
		 * ); extraSqlCount++; } else
		 */ result.setFileAttachmentCount(-1);
		if (FrameworkCache.getAppSettingIntValue(scd, "make_comment_flag") != 0 && t.getMakeCommentFlag() != 0) {
			if (extraSql.length() > 0)
				extraSql.append(",");
			if (FrameworkCache.getTable(scd, FrameworkSetting.customCommentTableId)==null)
				extraSql.append(
					"(select count(1) cnt from iwb.w5_comment x where x.project_uuid=? AND x.table_id=? AND x.table_pk=?::integer) comment_count");
			else 
				extraSql.append(
						"(select count(1) cnt from x_comment x where ? is not null AND x.table_id=? AND x.table_pk=?::integer) comment_count");
			extraSqlCount++;
		} else
			result.setCommentCount(-1);

		/*
		 * if(FrameworkCache.getAppSettingIntValue(scd, "row_based_security_flag")!=0 &&
		 * (Integer)scd.get("userTip")!=3 && t.getAccessTips()!=null &&
		 * t.getAccessTips().length()>0){ if(extraSql.length()>0)extraSql.append(",");
		 * extraSql.append(
		 * "(select count(1) cnt from iwb.w5_access_control x where x.customization_id=? AND x.table_id=? AND x.table_pk=?) access_count"
		 * ); extraSqlCount++; } else result.setAccessControlCount(-1);
		 */
		List<Object> params = new ArrayList(extraSqlCount * 3 + 5);
		for (int qi = 0; qi < extraSqlCount; qi++) {
			params.add(scd.get("projectId"));
			params.add(tableId);
			params.add(tablePk);
		}

		if (FrameworkCache.getAppSettingIntValue(scd, "form_conversion_flag") != 0) {
			if (extraSql.length() > 0)
				extraSql.append(",");
			extraSql.append(
					"(select count(1) cnt from iwb.w5_converted_object y, iwb.w5_conversion x where x.active_flag=1 AND x.project_uuid=? AND x.project_uuid=y.project_uuid AND x.conversion_id=y.conversion_id AND x.src_table_id=? AND y.src_table_pk=?) conversion_count");
			params.add(scd.get("projectId"));
			params.add(tableId);
			params.add(tablePk);
			extraSqlCount++;
		} else
			result.setConversionCount(-1);

		if (extraSql.length() > 0) {

			List<Map> l = executeSQLQuery2Map("select " + extraSql.append(" ").toString(), params); // from
																									// dual
			if (!GenericUtil.isEmpty(l)) {
				Map m = l.get(0);
				if (result.getFileAttachmentCount() != -1)
					result.setFileAttachmentCount(GenericUtil.uInt(m.get("file_attach_count")));
				if (result.getCommentCount() != -1)
					result.setCommentCount(GenericUtil.uInt(m.get("comment_count")));
				if (result.getAccessControlCount() != -1)
					result.setAccessControlCount(GenericUtil.uInt(m.get("access_count")));
				if (result.getFormMailSmsCount() != -1)
					result.setFormMailSmsCount(GenericUtil.uInt(m.get("mail_sms_count")));
				if (result.getConversionCount() != -1)
					result.setConversionCount(GenericUtil.uInt(m.get("conversion_count")));
			}
		}
		return result;
	}

	public String getSummaryText4Record(Map<String, Object> scd, int tableId, int tablePk) {
		W5Table t = FrameworkCache.getTable(scd, tableId);
		String summarySql = t.getSummaryRecordSql();
		String summaryText = null;
		if (!GenericUtil.isEmpty(summarySql)) {
			List params = new ArrayList();
			if (summarySql.indexOf("${") > -1) {
				Object[] oz = DBUtil.filterExt4SQL(summarySql, scd, new HashMap(), null);
				summarySql = oz[0].toString();
				if (oz.length > 1 && oz[1] != null)
					params = (List) oz[1];
			}
			String sql = "select " + summarySql + " dsc from " + t.getDsc() + " x where x."
					+ t.get_tableParamList().get(0).getExpressionDsc() + "=?";
			params.add(tablePk);
			sql += DBUtil.includeTenantProjectPostSQL(scd, t);
			Map<String, Object> m = runSQLQuery2Map(sql, params, null);
			if (m != null)
				summaryText = (String) m.get("dsc");
			if (summaryText == null) {
				summaryText = "ERROR: " + sql;
			}
		} else
			summaryText = "TODO: make Summary SQL on Table: " + /* formResult.getForm().get_sourceTable() */ t.getDsc();
		return summaryText;
	}

	public boolean checkIfRecordsExists(Map scd, Map<String, String> requestParams, W5Table t) {
		StringBuilder sql = new StringBuilder();
		sql.append("select 1 from ").append(t.getDsc()).append(" t where ");

		List<Object> params = new ArrayList();
		boolean b = false;
		Map m = new HashMap();
		for (W5TableParam x : t.get_tableParamList()) {
			if (b) {
				sql.append(" AND ");
			} else
				b = true;
			sql.append("t.").append(x.getExpressionDsc()).append(" = ? ");
			Object presult = GenericUtil.prepareParam((W5Param) x, scd, requestParams, (short) -1, null, (short) 1, null,
					null, m);
			params.add(presult);
		}

		return runSQLQuery2Map(sql.toString(), params, null) != null;
	}

	public void removeTableChildRecords(Map<String, Object> scd, int tableId, int tablePk, String dstDetailTableIds) {
		if (tableId == 0 || GenericUtil.isEmpty(dstDetailTableIds))
			return;
		W5Table t = FrameworkCache.getTable(scd, tableId);
		String[] dtsl = dstDetailTableIds.split(",");
		for (String dts : dtsl) {
			int detailTableId = GenericUtil.uInt(dts);
			for (W5TableChild tc : t.get_tableChildList())
				if (tc.getRelatedTableId() == detailTableId) {
					W5Table dt = FrameworkCache.getTable(scd, detailTableId);
					List<Object> params = new ArrayList();
					StringBuilder sql = new StringBuilder();
					sql.append("delete from ").append(dt.getDsc()).append(" x where x.customization_id=?");
					params.add(scd.get("customizationId"));
					sql.append(" AND x.").append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc())
							.append("=?");
					params.add(tablePk);
					if (tc.getRelatedStaticTableFieldId() != 0) {
						sql.append(" AND x.")
								.append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc())
								.append("=?");
						params.add(tc.getRelatedStaticTableFieldVal());
					}
					executeUpdateSQLQuery(sql.toString(), params);
				}
		}
	}

	public Map getTableRecordJson(Map<String, Object> scd, int tableId, int tablePk, int recursiveLevel) {
		W5Table t = FrameworkCache.getTable(scd, tableId);
		StringBuilder s = new StringBuilder();
		s.append("select x.* from ").append(t.getDsc()).append(" x where x.")
				.append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
		s.append(DBUtil.includeTenantProjectPostSQL(scd, t));
		List p = new ArrayList();
		p.add(tablePk);
		List l = executeSQLQuery2Map(s.toString(), p);
		return GenericUtil.isEmpty(l) ? null : (Map) l.get(0);
	}


	public short updateVcsObjectColumn(Map<String, Object> scd, int tableId, int tablePk, String column, JSONObject o) { // TODO
		// dao.updatePlainTableRecord(t, o, vo.getTablePk(), srvCommitUserId);
		try {
			W5Table t = FrameworkCache.getTable(scd, tableId);
			StringBuilder s = new StringBuilder();
			List p = new ArrayList();
			s.append("update ").append(t.getDsc()).append(" x set ");
			for (W5TableField f : t.get_tableFieldList())
				if (f.getTabOrder() > 1 && (f.getDsc().equals(column) || f.getDsc().equals("version_dttm"))) {
					if (f.getDsc().equals("version_dttm")) {
						s.append(f.getDsc()).append("=iwb.fnc_sysdate(0),");
						continue;
					}
					s.append(f.getDsc()).append("=?,");
					try {
						if (o.has(f.getDsc())) {
							p.add(GenericUtil.getObjectByControl((String) o.get(f.getDsc()), f.getParamType()));
						} else
							p.add(null);
					} catch (JSONException e) {
						throw new IWBException("vcs", "JSONException : saveVcsObject", t.getTableId(), f.getDsc(),
								e.getMessage(), e);
					}
				}
			s.setLength(s.length() - 1);
			s.append(" where ").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
			p.add(tablePk);
			s.append(DBUtil.includeTenantProjectPostSQL(scd, t));

			executeUpdateSQLQuery(s.toString(), p);
			
			s.setLength(0);p.clear();
			s.append("select * from ").append(t.getDsc()).append(" x where ").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
			p.add(tablePk);
			s.append(DBUtil.includeTenantProjectPostSQL(scd, t));
			Map<String, Object> m = runSQLQuery2Map(s.toString(), p, null);
			for(String k:m.keySet()) if(!GenericUtil.hasPartInside("insert_user_id,insert_dttm,version_user_id,version_dttm,version_no,oproject_uuid,project_uuid,customization_uuid", k) && !GenericUtil.safeEquals2(m.get(k), o.get(k))){
				return (short)1;
			}
			return (short)9;
		} catch (Exception e) {
			throw new IWBException("framework", "Update.VCSObjectColumn", tablePk, null, "[" + tableId + "," + tablePk + "] ",
					e);
		}

	}

	public String getTableRecordSummary(Map scd, int tableId, int tablePk, int maxLength) {
		W5Table t = FrameworkCache.getTable(scd, tableId);
		if (t == null)
			return "Table not Found ;)";
		if (GenericUtil.isEmpty(t.get_tableParamList()))
			return "TableParam not Found ;)";
		StringBuilder sql = new StringBuilder();
		sql.append("select (").append(t.getSummaryRecordSql()).append(") qqq from ").append(t.getDsc())
				.append(" x where x.").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
		sql.append(DBUtil.includeTenantProjectPostSQL(scd, t));
		Object[] res = DBUtil.filterExt4SQL(sql.toString(), scd, new HashMap(), new HashMap());
		List summaryParams = (List) res[1];
		summaryParams.add(tablePk);
		List l = executeSQLQuery2(((StringBuilder) res[0]).toString(), summaryParams);
		if (GenericUtil.isEmpty(l))
			return "(record not found)(" + tablePk + ")";
		String s = (String) l.get(0);
		if (s == null)
			return "(record not found)(" + tablePk + ")";
		return maxLength == 0 ? s : (s.length() > maxLength ? s.substring(0, maxLength) : s);
	}



	public String getCurrentDate(int customizationId) {
		return (String) executeSQLQuery("select to_char(iwb.fnc_sysdate(?),'" + GenericUtil.dateFormat + "')",
				customizationId).get(0);
	}

	public Object getSqlFunc(String s) {
		return (Object) executeSQLQuery("select " + s).get(0);
	}

	public Map executeSQLQuery2Map4Debug(final Map<String, Object> scd, final W5Table t, final String sql,
			final List params, final int limit, final int startOffset) {
		try {
			return (Map) getCurrentSession().doReturningWork(new ReturningWork<Map>() {

				public Map execute(Connection conn) throws SQLException {
					Map m = new HashMap();
					PreparedStatement s = null;
					ResultSet rs = null;
					try {
						if (limit > 0) {
							s = conn.prepareStatement("select count(1) v from (" + sql + ") q");
							if (params != null && params.size() > 0)
								for (int i = 0; i < params.size(); i++) {
									if (params.get(i) == null)
										s.setObject(i + 1, null);
									else if (params.get(i) instanceof Date)
										s.setDate(i + 1, new java.sql.Date(((Date) params.get(i)).getTime()));
									else
										s.setObject(i + 1, params.get(i));
								}
							rs = s.executeQuery();
							rs.next();
							int cnt = GenericUtil.uInt(rs.getObject(1));
							rs.close();
							s.close();
							Map m2 = new HashMap();
							m2.put("startRow", startOffset);
							m2.put("fetchCount", limit);
							m2.put("totalCount", cnt);
							m.put("browseInfo", m2);
							s = startOffset > 1
									? conn.prepareStatement("select q.* from  (" + sql + ") q limit " + (limit + 1)
											+ ") offset " + startOffset)
									: conn.prepareStatement("select * from (" + sql + ") q limit " + (limit + 1));
						} else
							s = conn.prepareStatement(sql);

						if (params != null && params.size() > 0)
							for (int i = 0; i < params.size(); i++) {
								if (params.get(i) == null)
									s.setObject(i + 1, null);
								else if (params.get(i) instanceof Date)
									s.setDate(i + 1, new java.sql.Date(((Date) params.get(i)).getTime()));
								else
									s.setObject(i + 1, params.get(i));
							}

						long startTm = System.currentTimeMillis();
						rs = s.executeQuery();
						m.put("execTime", System.currentTimeMillis() - startTm);
						startTm = System.currentTimeMillis();
						// int columnCount = rs.getMetaData().getColumnCount();
						List l = new ArrayList();
						Map<String, Object> result = null;
						ResultSetMetaData rsm = rs.getMetaData();
						int columnCount = rsm.getColumnCount();
						String[] columnNames = new String[columnCount];
						List fields = new ArrayList();

						Map<String, Map> map4records = new HashMap<String, Map>();
						for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
							String columnName = rsm.getColumnName(columnIndex).toLowerCase(FrameworkSetting.appLocale);
							columnNames[columnIndex - 1] = columnName;
							Map m2 = new HashMap();
							m2.put("name", columnName);
							switch (rsm.getColumnType(columnIndex)) {
							case java.sql.Types.BIGINT:
							case java.sql.Types.INTEGER:
							case java.sql.Types.SMALLINT:
								m2.put("type", "int");
								break;
							case java.sql.Types.NUMERIC:
								m2.put("type", rsm.getScale(columnIndex) == 0 ? "int" : "float");
								break;
							case java.sql.Types.DATE:
							case java.sql.Types.TIMESTAMP:
								m2.put("type", "date");
								break;
							}
							if (t != null)
								for (W5TableField f : t.get_tableFieldList())
									if (f.getDsc().equals(columnName)) {
										if (f.getDefaultLookupTableId() != 0)
											switch (f.getDefaultControlType()) {
											case 6:
											case 8:
												W5LookUp lu = FrameworkCache.getLookUp(scd,
														f.getDefaultLookupTableId());
												if (lu != null && !GenericUtil.isEmpty(lu.get_detayList())) {
													Map m3 = new HashMap();
													for (W5LookUpDetay d : lu.get_detayList()) {
														m3.put(d.getVal(), LocaleMsgCache.get2(scd, d.getDsc()));
													}
													m2.put(f.getDefaultControlType() == 6 ? "map" : "map2", m3);
												}
												break;
											case 7:
											case 10:
											case 15:
												W5Table tt = FrameworkCache.getTable(scd, f.getDefaultLookupTableId());
												if (tt != null && !GenericUtil.isEmpty(tt.getSummaryRecordSql())) {
													Map mz = new HashMap();
													mz.put("set", new HashSet());
													mz.put("t", tt);
													mz.put("field", m2);
													map4records.put(columnName, mz);
												}
											}

										break;
									}
							fields.add(m2);
						}
						m.put("fields", fields);

						while (rs.next()) {
							result = new HashMap<String, Object>();
							for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
								String columnName = columnNames[columnIndex - 1];
								Object obj = rs.getObject(columnIndex);
								if (obj == null)
									continue;
								if (obj instanceof java.sql.Timestamp) {
									try {
										result.put(columnName,
												GenericUtil.uFormatDateTimeSade((java.sql.Timestamp) obj));
									} catch (Exception e) {
									}
								} else if (obj instanceof java.sql.Date) {
									try {
										result.put(columnName, GenericUtil.uFormatDateSade((java.sql.Date) obj));
									} catch (Exception e) {
									}
								} else {
									String ss = obj.toString();
									if (map4records.containsKey(columnName)) {
										((Set) (map4records.get(columnName).get("set"))).add(ss);
									}
									result.put(columnName, ss);
								}
							}
							l.add(result);
						}
						m.put("data", l);
						m.put("sql", GenericUtil.replaceSql(sql, params));
						m.put("fetchTime", System.currentTimeMillis() - startTm);

						for (String kk : map4records.keySet())
							try {
								Map mm = map4records.get(kk);
								Set set = (Set) mm.get("set");
								if (!set.isEmpty()) {
									String ss = "";
									for (Object sk : set.toArray()) {
										ss += "," + sk;
									}
									ss = ss.substring(1);
									W5Table t = (W5Table) mm.get("t");
									String sql = "select x." + t.get_tableFieldList().get(0).getDsc() + " id, "
											+ t.getSummaryRecordSql() + " dsc from " + t.getDsc() + " x where "
											+ t.get_tableParamList().get(0).getExpressionDsc() + " in (" + ss + ")";
									sql += DBUtil.includeTenantProjectPostSQL(scd, t);
									Object[] oz = DBUtil.filterExt4SQL(sql, scd, new HashMap(), null);
									List<Object[]> lm = executeSQLQuery2(oz[0].toString(), (List) oz[1]);
									if (lm != null) {
										Map m3 = new HashMap();
										for (Object[] oo : lm) {
											m3.put(oo[0], oo[1]);
										}
										((Map) (mm.get("field"))).put("map", m3);
									}
								}

							} catch (Exception ee) {
							}
					} catch (SQLException se) {
						throw se;
					} finally {
						if (rs != null)
							rs.close();
						if (s != null)
							s.close();
						if (FrameworkSetting.hibernateCloseAfterWork)
							if (conn != null)
								conn.close();
					}
					return m;
				}
			});
		} catch (Exception e) {
			throw new IWBException("sql", "Debug.Query", -1, GenericUtil.replaceSql(sql, params), "Error Executing", e);
		}
	}

	public Map executeQuery4Stat(Map<String, Object> scd, int gridId, Map<String, String> requestParams) {

		int customizationId = (Integer) scd.get("customizationId");
		int queryId = gridId > 0 ? metadataLoader.getGridResult(scd, gridId, requestParams, false).getGrid().getQueryId()
				: -gridId;
		W5QueryResult queryResult = metadataLoader.getQueryResult(scd, queryId);
		queryResult.setErrorMap(new HashMap());
		queryResult.setRequestParams(requestParams);
		if (queryResult.getQuery().getQuerySourceType() == 1376)
			return scriptEngine.executeQuery4StatWS(queryResult);

		if (queryId != 1 && queryId != 824 && queryResult.getMainTable() != null && (!FrameworkSetting.debug
				|| (scd.get("roleId") != null && GenericUtil.uInt(scd.get("roleId")) != 0))) {
			W5Table t = queryResult.getMainTable();

			if (t.getAccessViewTip() == 0 && !FrameworkCache.roleAccessControl(scd, 0)) {
				throw new IWBException("security", "Module", 0, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_modul_control"), null);
			}
			if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
					t.getAccessViewRoles(), t.getAccessViewUsers())) {
				throw new IWBException("security", "Query", queryId, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_table_control_view"),
						null);
			}
		}
		/*
		 * StringBuilder tmpx = new StringBuilder(
		 * "ali baba ${obj.dsc} ve 40 haramiler ${lnk.pk_query_field_id.dsc} olmus" );
		 * interprateTemplate(scd, 5,1294, tmpx, true);
		 */

		int statType = GenericUtil.uInt(requestParams, "_stat"); // 0:count,
																	// 1:sum,
																	// 2.avg
		String funcFields = requestParams.get("_ffids"); // statFunctionFields
		if (statType > 0 && GenericUtil.isEmpty(funcFields))
			throw new IWBException("framework", "Query", queryId, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_func_fields"), null);

		int queryFieldId = GenericUtil.uInt(requestParams, "_qfid");
		int stackFieldId = GenericUtil.uInt(requestParams, "_sfid");
		if (stackFieldId > 0 && stackFieldId == queryFieldId)
			stackFieldId = 0;

		W5Query query = queryResult.getQuery();
		W5QueryField qf = null, sf = null;
		for (W5QueryField o : query.get_queryFields())
			if (o.getQueryFieldId() == queryFieldId) {
				qf = o;
				break;
			}
		if (qf == null)
			throw new IWBException("framework", "Query", queryId, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_error"), null);
		if (stackFieldId > 0) {
			for (W5QueryField o : query.get_queryFields())
				if (o.getQueryFieldId() == stackFieldId) {
					sf = o;
					break;
				}
			if (sf == null)
				throw new IWBException("framework", "Query", queryId, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_error2"), null);
		}

		int sortTip = GenericUtil.uInt(requestParams, "_sort");
		String orderBy = null;
		if (sortTip > 0 && sortTip < 3) {
			orderBy = new String[] { "xres", "id" }[sortTip - 1];
			if (GenericUtil.uInt(requestParams, "_dir") > 0)
				orderBy += " desc";
		}

		//
		// queryResult.setOrderBy(PromisUtil.uStrNvl(requestParams.get(PromisUtil.uStrNvl(PromisSetting.appSettings.get("sql_sort"),"sort")),
		// queryResult.getQuery().getSqlOrderby()));
		// queryResult.setOrderBy(queryResult.getQuery().getSqlOrderby());
		String queryFieldSQL = qf.getDsc();
		if (qf.getFieldType() == 2) { // date ise
			queryFieldSQL = "to_char(" + queryFieldSQL + ", '"
					+ (new String[] { "yyyy", "yyyy/Q", "yyyy/mm", "yyyy/WW", "yyyy/mm/dd" }[GenericUtil
							.uInt(requestParams, "_dtt")])
					+ "')";
		}

		switch (queryResult.getQuery().getQueryType()) {
		case 9:
		case 10:
		case 15: // TODO: actually hata. olmamasi lazim
			throw new IWBException("framework", "Query", queryId, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_query_error"), null);
		default:
			queryResult.prepareQuery(null);
			if (!queryResult.getErrorMap().isEmpty())
				throw new IWBException("framework", "Query", queryId, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_query_prepare_error"), null);

			String sql = "select " + queryFieldSQL + " id, ";
			if (sf != null)
				sql += sf.getDsc() + " stack_id, ";
			if (statType != 0) {
				String[] fq = funcFields.split(",");
				Set<Integer> fqs = new HashSet();
				for (String s : fq) {
					fqs.add(GenericUtil.uInt(s));
				}

				int count = 0;
				String[] stats = new String[] { "", "sum", "avg", "max", "min" };
				for (W5QueryField o : queryResult.getQuery().get_queryFields())
					if (fqs.contains(o.getQueryFieldId())) {
						count++;
						if (count > 1)
							sql += "," + stats[statType] + "(" + o.getDsc() + ") xres" + count;
						else
							sql += stats[statType] + "(" + o.getDsc() + ") xres";
					}
				if (count == 0)
					throw new IWBException("framework", "Query", queryId, null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_match_func_fields_error"),
							null);
			} else {
				sql += "count(1) xres";
			}

			sql += " from (" + queryResult.getExecutedSql() + ") mq group by id";
			if (sf != null)
				sql += ", stack_id";
			if (queryFieldSQL.startsWith("to_char("))
				sql += " order by id";
			else if (!GenericUtil.isEmpty(orderBy))
				sql += " order by " + orderBy;
			queryResult.setExecutedSql(sql);
		}
		Map result = new HashMap();
		result.put("success", true);
		List<Map> l = executeSQLQuery2Map(queryResult.getExecutedSql(), queryResult.getSqlParams());
		if (l != null) {
			if (sf != null) {
				Set<String> stackSet = new HashSet();
				List<Map> nl = new ArrayList();
				Map<Object, Map> nom = new HashMap();
				for (Map m : l) {
					Object oid = m.get("id");
					Map nm = nom.get(oid);
					if (nm == null) {
						nm = new HashMap();
						nm.put("id", oid);
						nom.put(oid, nm);
						nl.add(nm);
					}
					for (Object k : m.keySet())
						if (!k.equals("id") && !k.equals("stack_id")) {
							if (!GenericUtil.isEmpty(m.get("stack_id"))) {
								nm.put(k + "_" + m.get("stack_id"), m.get(k));
								stackSet.add(m.get("stack_id").toString());
							}
						}
				}
				l = nl;
				Map lm = new HashMap();
				switch (sf.getPostProcessType()) {
				case 10: // static;
					W5LookUp ld3 = FrameworkCache.getLookUp(customizationId, sf.getLookupQueryId());
					if (ld3 != null)
						for (Object k : stackSet)
							if (ld3.get_detayMap().get(k) != null)
								lm.put(k, GenericUtil
										.uStrMax(LocaleMsgCache.get2(scd, ld3.get_detayMap().get(k).getDsc()), 20));
							else
								throw new IWBException("framework", "QueryField", sf.getQueryFieldId(), null,
										LocaleMsgCache.get2(0, (String) scd.get("locale"),
												"fw_grid_stat_stacked_error"),
										null);
					break;
				case 12: // table
					for (Object k : stackSet) {
						String s = getTableRecordSummary(scd, sf.getLookupQueryId(), GenericUtil.uInt(k), 20);
						lm.put(k, GenericUtil.isEmpty(s) ? "Not found for: " + k : s);
					}
					break;
				case 20:
				case 53: // user
					for (Object k : stackSet)
						lm.put(k, GenericUtil.uStrMax(UserUtil.getUserDsc(GenericUtil.uInt(k)), 20));
				}
				result.put("lookUp", lm);
			} else {
				int maxLegend = GenericUtil.uInt(requestParams, "_max", 30);
				if (l.size() > maxLegend) { // TODO: temizlik
					BigDecimal total = new BigDecimal(0);
					String ids = "";
					for (int qi = l.size() - 1; qi >= maxLegend - 1; qi--) {
						Map m = l.get(qi);
						ids += "," + m.get("id");
						total = total.add(new BigDecimal(m.get("xres").toString()));
						l.remove(qi);
					}
					Map nm = new HashMap();
					nm.put("id", -999999);
					nm.put("dsc", LocaleMsgCache.get2(scd, "others"));
					nm.put("xres", total);
					nm.put("ids", ids.substring(1));
					l.add(nm);
				}
			}
			for (W5QueryField o : queryResult.getQuery().get_queryFields())
				if (o.getQueryFieldId() == queryFieldId) {
					switch (o.getPostProcessType()) {
					case 10: // lookup static
						W5LookUp ld2 = FrameworkCache.getLookUp(customizationId, o.getLookupQueryId());
						if (ld2 != null)
							for (Map m : l) {
								Object o2 = m.get("id");
								if (o2 != null && GenericUtil.uInt(o2) != -999999)
									try {
										m.put("dsc", GenericUtil.uStrMax(
												LocaleMsgCache.get2(scd, ld2.get_detayMap().get(o2).getDsc()), 20));
									} catch (Exception e) {
										m.put("dsc", "Not found for: " + o2);
									}
							}
						break;
					case 12: // lookup table
						for (Map m : l) {
							Object o2 = m.get("id");
							if (o2 != null && GenericUtil.uInt(o2) != -999999) {
								String s = getTableRecordSummary(scd, o.getLookupQueryId(), GenericUtil.uInt(o2), 20);
								m.put("dsc", GenericUtil.isEmpty(s) ? "Not found for: " + o2 : s);
							}
						}
						break;
					case 20:
					case 53: // user
						for (Map m : l) {
							Object o2 = m.get("id");
							if (o2 != null && GenericUtil.uInt(o2) != -999999) {
								m.put("dsc",
										GenericUtil.uStrNvl(UserUtil.getUserDsc(GenericUtil.uInt(o2)), "user-" + o2));
							}
						}
					}

					break;
				}
			for (Map m : l) {
				Object o2 = m.get("dsc");
				if (o2 == null) {
					
					m.put("dsc", m.get("id"));
				}
			}
			result.put("data", l);
		}
		return result;
	}

	public Map executeQuery4StatTree(Map<String, Object> scd, int gridId, Map<String, String> requestParams) {

		String projectId = (String) scd.get("projectId");
		int queryId = metadataLoader.getGridResult(scd, gridId, requestParams, false).getGrid().getQueryId();
		W5QueryResult queryResult = metadataLoader.getQueryResult(scd, queryId);
		W5Table t = queryResult.getMainTable();
		if (queryId != 1 && queryId != 824 && queryResult.getMainTable() != null && (!FrameworkSetting.debug
				|| (scd.get("roleId") != null && GenericUtil.uInt(scd.get("roleId")) != 0))) {
			if (t.getAccessViewTip() == 0 && !FrameworkCache.roleAccessControl(scd, 0)) {
				throw new IWBException("security", "Module", 0, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_modul_control"), null);
			}
			if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
					t.getAccessViewRoles(), t.getAccessViewUsers())) {
				throw new IWBException("security", "Query", queryId, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_table_control_view"),
						null);
			}
		}
		/*
		 * StringBuilder tmpx = new StringBuilder(
		 * "ali baba ${obj.dsc} ve 40 haramiler ${lnk.pk_query_field_id.dsc} olmus" );
		 * interprateTemplate(scd, 5,1294, tmpx, true);
		 */
		queryResult.setErrorMap(new HashMap());
		queryResult.setRequestParams(requestParams);

		int statType = GenericUtil.uInt(requestParams, "_stat"); // 0:count, n:
																	// sum(queryField)
		String tableFieldChain = requestParams.get("_qfid");
		String funcFields = statType == 0 ? null : requestParams.get("_ffids");
		if(statType!=0 && GenericUtil.safeEquals(funcFields,"0"))statType=0;
		if (tableFieldChain.indexOf('-') > 0)
			tableFieldChain = tableFieldChain.split("-")[1];
		String tableFieldSQL = "";
		List params = new ArrayList();
		W5TableField tableField = null;
		Map<String, String> lookUps = new HashMap();

		if (tableFieldChain.startsWith("tbl.") || (tableFieldChain.startsWith("lnk.")
				&& tableFieldChain.substring(4).replace(".", "&").split("&").length == 1)) { // direk
																								// tablodan
																								// veya
																								// link'in
																								// ilk
																								// kaydi
																								// ise
			String newSubStr = tableFieldChain.substring(4);
			boolean fieldFound = false;
			for (W5TableField tf : t.get_tableFieldList())
				if (tf.getDsc().equals(newSubStr)) {
					tableFieldSQL = "x." + newSubStr; // .put(tableFieldChain,
														// o.toString());
					tableField = tf;
					fieldFound = true;
					break;
				}
			if (!fieldFound)
				throw new IWBException("framework", "Query", queryId, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_field_error"), null);
		} else if (tableFieldChain.startsWith("clc.")) { // calculated field
			String newSubStr = tableFieldChain.substring(4);
			boolean fieldFound = false;
			List<W5TableFieldCalculated> ltcf = metadataLoader.findTableCalcFieldByName(projectId,
					t.getTableId(), newSubStr);
			if (ltcf.isEmpty())
				throw new IWBException("framework", "Query", queryId, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_field_error"), null);
			W5TableFieldCalculated tcf = ltcf.get(0);
			Object[] oo = DBUtil.filterExt4SQL(tcf.getSqlCode(), scd, requestParams, null);
			tableFieldSQL = "(" + oo[0].toString() + ")"; // .put(tableFieldChain,
															// o.toString());
			if (oo.length > 1)
				params.addAll((List) oo[1]);
			tableField = t.get_tableFieldList().get(0);
			fieldFound = true;
		} else if (tableFieldChain.startsWith("lnk.")
				&& tableFieldChain.substring(4).replace(".", "&").split("&").length > 1) { // link for children
			String newSubStr = tableFieldChain.substring(4);

			String[] sss = newSubStr.replace(".", "&").split("&");
			if (sss.length > 1) { // TODO kaldirilmasi lazim
				W5Table newT = t;
				StringBuilder newSub = new StringBuilder();
				StringBuilder newSub2 = new StringBuilder();
				boolean foundSt = false;
				for (int isss = 0; isss < sss.length - 1; isss++) {
					if (isss > 0) {
						newSub2.setLength(0);
						newSub2.append(newSub);
						newSub.setLength(0);
					}
					for (W5TableField tf : newT.get_tableFieldList())
						if (tf.getDsc().equals(sss[isss])) {
							foundSt = false;
							if (tf.getDefaultControlType() == 7 || tf.getDefaultControlType() == 9
									|| tf.getDefaultControlType() == 10 /*
																		 * || tf. getDefaultControlTip ()==15
																		 */) { // sub
																				// table
								W5Table st = FrameworkCache.getTable(scd, tf.getDefaultLookupTableId());
								if (st == null)
									break; // HATA: gerekli bir alt kademe
											// tabloya ulasilamadi
								int dltId = 0;
								for (W5TableField stf : st.get_tableFieldList())
									if (stf.getDsc().equals(sss[isss + 1])) {
										dltId = stf.getDefaultLookupTableId();
										tableField = stf;
										foundSt = true;
										break;
									}
								if (!foundSt)
									break; // HATA: bir sonraki field bulunamadi
								newSub.append("(select ");
								newSub.append("y").append(isss).append(".").append(sss[isss + 1]);
								newSub.append(" from ").append(st.getDsc()).append(" y").append(isss).append(" where y")
										.append(isss).append(".").append(st.get_tableFieldList().get(0).getDsc())
										.append("=").append(isss == 0 ? ("x." + sss[isss]) : newSub2);
								if (st.get_tableFieldList().size() > 1)
									for (W5TableField wtf : st.get_tableFieldList())
										if (wtf.getDsc().equals("project_uuid")) {
											newSub.append(" AND y").append(isss).append(".project_uuid=?");
											params.add(scd.get("projectId"));
											break;
										}

								newSub.append(")");
								newT = st;
							}
							break;
						}
					if (!foundSt) { // bulamamis uygun sey
						break;
					}
				}
				if (foundSt && newSub.length() > 0) {
					tableFieldSQL = "(" + newSub.toString() + ")"; // .put(tableFieldChain,
																	// o.toString());
				} else
					throw new IWBException("framework", "Query", queryId, null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_field_error"), null);
			}
		}
		int stackFieldId = GenericUtil.uInt(requestParams, "_sfid");
		String stackField = null;
		W5QueryField stackedQueryField = null;

		if (stackFieldId != 0)
			for (W5QueryField qf : queryResult.getQuery().get_queryFields())
				if (stackFieldId == qf.getQueryFieldId()) {
					stackField = qf.getDsc();
					if (stackField.equals(tableFieldSQL))
						stackField = null;
					else
						stackedQueryField = qf;
					break;
				}
		//
		// queryResult.setOrderBy(PromisUtil.uStrNvl(requestParams.get(PromisUtil.uStrNvl(PromisSetting.appSettings.get("sql_sort"),"sort")),
		// queryResult.getQuery().getSqlOrderby()));
		// queryResult.setOrderBy(queryResult.getQuery().getSqlOrderby());

		if (tableField.getParamType() == 2 || tableField.getDefaultControlType() == 2
				|| tableField.getDefaultControlType() == 18) {
			tableFieldSQL = "to_char(" + tableFieldSQL + ", '"
					+ (new String[] { "yyyy", "yyyy/Q", "yyyy/mm", "yyyy/WW", "yyyy/mm/dd" }[GenericUtil
							.uInt(requestParams, "_dtt")])
					+ "')";
		}
		Map result = new HashMap();
		result.put("success", true);
		switch (queryResult.getQuery().getQueryType()) {
		case 9:
		case 10:
		case 15: // TODO: actually hata. olmamasi lazim
			throw new IWBException("framework", "Query", queryId, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_query_error"), null);
		default:
			String[] stats = new String[] { "", "sum", "avg", "max", "min" };
			queryResult.prepareQuery(null);
			if (!queryResult.getErrorMap().isEmpty())
				throw new IWBException("framework", "Query", queryId, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_error"), null);
			String sql = "select " + tableFieldSQL + " id, ";
			if (!GenericUtil.isEmpty(stackField))
				sql += stackField + " stack_id, ";
			if (statType != 0) {
				result.put("lookUps", lookUps);
				String[] fq = funcFields.split(",");
				int count = 0;
				Set<Integer> fqs = new HashSet();
				for (String s : fq) {
					int isx = GenericUtil.uInt(s);
					if (isx < 0) {
						W5TableFieldCalculated tcf = (W5TableFieldCalculated)metadataLoader.getMetadataObject("W5TableFieldCalculated", "tableFieldCalculatedId", -isx, projectId, "TableFieldCalculated");
						lookUps.put(count+"", LocaleMsgCache.get2(scd, tcf.getDsc()));
						count++;
						Object[] oo = DBUtil.filterExt4SQL(tcf.getSqlCode(), scd, requestParams, null);
						if (oo.length > 1)
							params.addAll((List) oo[1]);

						if (count > 1)
							sql += "," + stats[statType] + "((" + oo[0] + ")) xres" + count;
						else
							sql += stats[statType] + "((" + oo[0] + ")) xres";

					} else
						fqs.add(isx);
				}

				for (W5QueryField o : queryResult.getQuery().get_queryFields()) {
					if (fqs.contains(o.getQueryFieldId())) {
						lookUps.put(count+"", LocaleMsgCache.get2(scd, o.getDsc()));
						count++;
						if (count > 1)
							sql += "," + stats[statType] + "(" + o.getDsc() + ") xres" + count;
						else
							sql += stats[statType] + "(" + o.getDsc() + ") xres";
					}
				}

				if (count == 0)
					throw new IWBException("framework", "Query", queryId, null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_error"), null);
			} else {
				sql += "count(1) xres";
			}

			sql += " from (" + queryResult.getExecutedSql() + ") x group by id";
			if (!GenericUtil.isEmpty(stackField))
				sql += ", stack_id";
			sql += tableFieldSQL.startsWith("to_char(") ? " order by id" : " order by xres desc";
			queryResult.setExecutedSql(sql);
		}
		if (queryResult.getErrorMap().isEmpty()) {
			if (!params.isEmpty())
				queryResult.getSqlParams().addAll(0, params);
			List<Map> l = executeSQLQuery2Map(queryResult.getExecutedSql(), queryResult.getSqlParams());
			if (l != null) {
				if (!GenericUtil.isEmpty(stackField)) {
					Set<String> stackSet = new HashSet();
					List<Map> nl = new ArrayList();
					Map<Object, Map> nom = new HashMap();
					for (Map m : l) {
						Object oid = m.get("id");
						Map nm = nom.get(oid);
						if (nm == null) {
							nm = new HashMap();
							nm.put("id", oid);
							nom.put(oid, nm);
							nl.add(nm);
						}
						for (Object k : m.keySet())
							if (!k.equals("id") && !k.equals("stack_id")) {
								if (!GenericUtil.isEmpty(m.get("stack_id"))) {
									nm.put(k + "_" + m.get("stack_id"), m.get(k));
									stackSet.add(m.get("stack_id").toString());
								}
							}
					}
					l = nl;
					Map lm = new HashMap();
					switch (stackedQueryField.getPostProcessType()) {
					case 10: // static;
						W5LookUp ld3 = FrameworkCache.getLookUp(projectId, stackedQueryField.getLookupQueryId());
						if (ld3 != null)
							for (Object k : stackSet)
								if (ld3.get_detayMap().get(k) != null)
									lm.put(k, GenericUtil
											.uStrMax(LocaleMsgCache.get2(scd, ld3.get_detayMap().get(k).getDsc()), 20));
								else
									throw new IWBException("framework", "QueryField",
											stackedQueryField.getQueryFieldId(), null, LocaleMsgCache.get2(0,
													(String) scd.get("locale"), "fw_grid_stat_stacked_error"),
											null);
						break;
					case 12: // table
						for (Object k : stackSet) {
							String s = getTableRecordSummary(scd, stackedQueryField.getLookupQueryId(),
									GenericUtil.uInt(k), 20);
							lm.put(k, GenericUtil.isEmpty(s) ? "Not found for: " + k : s);
						}
						break;
					case 20:
					case 53: // user
						for (Object k : stackSet)
							lm.put(k, GenericUtil.uStrMax(UserUtil.getUserDsc(GenericUtil.uInt(k)), 20));
					}
					result.put("lookUp", lm);
				} else {
					int maxLegend = (tableField.getParamType() == 2 || tableField.getDefaultControlType() == 2
							|| tableField.getDefaultControlType() == 18) ? 360 : 10;
					if (l.size() > maxLegend) { // TODO: temizlik
						BigDecimal total = new BigDecimal(0);
						String ids = "";
						for (int qi = l.size() - 1; qi >= maxLegend - 1; qi--) {
							Map m = l.get(qi);
							ids += "," + m.get("id");
							if (m.get("xres") != null)
								total = total.add(new BigDecimal(m.get("xres").toString()));
							l.remove(qi);
						}
						Map nm = new HashMap();
						nm.put("id", -999999);
						nm.put("dsc", LocaleMsgCache.get2(scd, "others"));
						nm.put("xres", total);
						nm.put("ids", ids.substring(1));
						l.add(nm);
					}
				}
				switch (tableField.getDefaultControlType()) {
				case 6: // lookup static
					W5LookUp ld2 = FrameworkCache.getLookUp(projectId, tableField.getDefaultLookupTableId());
					if (ld2 != null)
						for (Map m : l) {
							Object o2 = m.get("id");
							if (o2 != null && GenericUtil.uInt(o2) != -999999) {
								if (ld2.get_detayMap().get(o2) != null)
									m.put("dsc", GenericUtil.uStrMax(
											LocaleMsgCache.get2(scd, ld2.get_detayMap().get(o2).getDsc()), 20));
							}
						}
					break;
				case 7:
				case 10: // lookup table
					for (Map m : l) {
						Object o2 = m.get("id");
						if (o2 != null && GenericUtil.uInt(o2) != -999999) {
							String s = getTableRecordSummary(scd, tableField.getDefaultLookupTableId(),
									GenericUtil.uInt(o2), 20);
							m.put("dsc", GenericUtil.isEmpty(s) ? "Not found for: " + o2 : s);
						}
					}
					break;
				default:
					for (Map m : l) {
						Object o2 = m.get("id");
						m.put("dsc", o2 == null ? "" : o2.toString());
					}
				}
			}
			result.put("data", l);
		}
		return result;
	}

	public List executeQuery4DataList(Map<String, Object> scd, W5Table t, Map<String, String> requestParams) {

		StringBuilder sql = new StringBuilder();
		String dateFormat = GenericUtil.uStrNvl(requestParams.get("dtFmt"), "dd/mm/yyyy");
		sql.append("SELECT ");
		String[] cols = requestParams.get("cols").split(",");
		boolean cldFlag = false, cntFlag = false;

		Map<String, W5LookUp> staticLookups = new HashMap();
		String groupBy = "", clcFieldPrefix = "iwb_x_qw_";
		int iwfField = 0;
		Map<String, String> iwbFieldMap = new HashMap();
		Map<String, String> errorMap = new HashMap();
		for (String c : cols) {
			String fieldName = "";

			if (c.startsWith("clc.")) {
				String c2 = c.substring(4);
				List<W5TableFieldCalculated> l = metadataLoader.findTableCalcFieldByName(
						t.getProjectUuid(), t.getTableId(), c2);
				if (!l.isEmpty()) {
					sql.append("(").append(l.get(0).getSqlCode()).append(")");
					iwfField++;
					fieldName = clcFieldPrefix + iwfField;
					iwbFieldMap.put(c, fieldName);
				} else { // TODO ERROR
					errorMap.put(c, "Calculated Field not found");
					continue;
				}
				// sql.append("").append(l.get(0).getSqlCode().replaceAll("x.",
				// "x.")).append(" as
				// ").append(l.get(0).getDsc()).append(",");
			} else if (c.startsWith("cld.")) { // childs: only count and sum

				String[] sss = c.substring(4).replace(".", "&").split("&");
				if (sss.length == 1) {
					errorMap.put(c, "Table Child Field not defined");
					continue;
				}
				StringBuilder newSub = new StringBuilder();
				W5Table detT = null;
				W5TableChild detTc = null;
				for (W5TableChild tc : t.get_tableChildList()) {
					detT = FrameworkCache.getTable(scd, tc.getRelatedTableId());
					if (detT != null && detT.getDsc().equals(sss[0])) {
						detTc = tc;
						break;
					}
				}
				if (detT == null) {
					errorMap.put(c, "Table Child not found");
					continue;
				}
				for (int isss = 1; isss < sss.length && isss < 2; isss++) { // TODO
																			// simdilik
																			// 1
																			// seviye
					if (sss[isss].equals("cld")) {
						isss++;
						if (isss >= sss.length) {
							errorMap.put(c, "Child Tables wrong definition");
							continue;
						}
						if (GenericUtil.isEmpty(detT.get_tableChildList())) {
							errorMap.put(c, "Child Tables does not exist for [" + detT.getDsc() + "]");
							continue;
						}
						for (W5TableChild tc : detT.get_tableChildList()) {
							detT = FrameworkCache.getTable(scd, tc.getRelatedTableId());
							if (detT != null && detT.getDsc().equals(sss[isss])) {
								detTc = tc;
								break;
							}
						}
						errorMap.put(c, "Child Tables not implemented"); // TODO
						continue;

					} else if (sss[isss].equals("clc")) {
						isss++;
						if (isss >= sss.length) {
							errorMap.put(c, "Calculated Field wrong definition");
							continue;
						}
						List<W5TableFieldCalculated> l = metadataLoader.findTableCalcFieldByName(
								t.getProjectUuid(), detT.getTableId(), sss[isss]);
						if (!l.isEmpty()) {
							newSub.append("SELECT ").append("sum" /* valMap.get(c) */).append("((")
									.append(l.get(0).getSqlCode().replaceAll("x.", "z" + isss + ".")).append(")) from ")
									.append(detT.getDsc()).append(" z").append(isss).append(" WHERE ");
							newSub.append(" x.").append(t.get_tableFieldMap().get(detTc.getTableFieldId()).getDsc())
									.append("=z").append(isss).append(".")
									.append(detT.get_tableFieldMap().get(detTc.getRelatedTableFieldId()).getDsc());
							if (detTc.getRelatedStaticTableFieldId() > 0
									&& !GenericUtil.isEmpty(detTc.getRelatedStaticTableFieldVal()))
								newSub.append(" AND z").append(isss).append(".").append(
										detT.get_tableFieldMap().get(detTc.getRelatedStaticTableFieldId()).getDsc())
										.append("=").append(detTc.getRelatedStaticTableFieldVal());
							// if(detT.get_tableParamList().size()>1)newSub.append("
							// AND
							// z").append(isss).append(".").append("customization_id=${scd.customizationId}");
							newSub.append(DBUtil.includeTenantProjectPostSQL(scd, detT, "z" + isss));
							sql.append("(").append(newSub).append(")");
							iwfField++;
							fieldName = clcFieldPrefix + iwfField;
							iwbFieldMap.put(c, fieldName);
							// if(valMap.get(c).equals("count"))valMap.put(c,"sum");
						} else { // TODO ERROR
							errorMap.put(c, "Calculated Field not found");
							continue;
						}

					} else if (sss[isss].equals("0")) {
						newSub.append("SELECT count(1) from ").append(detT.getDsc()).append(" z").append(isss)
								.append(" WHERE ");
						newSub.append(" x.").append(t.get_tableFieldMap().get(detTc.getTableFieldId()).getDsc())
								.append("=z").append(isss).append(".")
								.append(detT.get_tableFieldMap().get(detTc.getRelatedTableFieldId()).getDsc());
						if (detTc.getRelatedStaticTableFieldId() > 0
								&& !GenericUtil.isEmpty(detTc.getRelatedStaticTableFieldVal()))
							newSub.append(" AND z").append(isss).append(".")
									.append(detT.get_tableFieldMap().get(detTc.getRelatedStaticTableFieldId()).getDsc())
									.append("=").append(detTc.getRelatedStaticTableFieldVal());
						// if(detT.get_tableParamList().size()>1)newSub.append("
						// AND
						// z").append(isss).append(".").append("customization_id=${scd.customizationId}");
						newSub.append(DBUtil.includeTenantProjectPostSQL(scd, detT, "z" + isss));
						sql.append("(").append(newSub).append(")");
						iwfField++;
						fieldName = clcFieldPrefix + iwfField;
						iwbFieldMap.put(c, fieldName);
						// if(valMap.get(c).equals("count"))valMap.put(c,"sum");
						break;
					} else
						for (W5TableField tf : detT.get_tableFieldList())
							if (tf.getDsc().equals(sss[isss])) {
								newSub.append("SELECT ").append("sum" /* valMap.get(c) */).append("(z").append(isss)
										.append(".").append(tf.getDsc()).append(") from ").append(detT.getDsc())
										.append(" z").append(isss).append(" WHERE ");
								newSub.append(" x.").append(t.get_tableFieldMap().get(detTc.getTableFieldId()).getDsc())
										.append("=z").append(isss).append(".")
										.append(detT.get_tableFieldMap().get(detTc.getRelatedTableFieldId()).getDsc());
								if (detTc.getRelatedStaticTableFieldId() > 0
										&& !GenericUtil.isEmpty(detTc.getRelatedStaticTableFieldVal()))
									newSub.append(" AND z").append(isss).append(".")
											.append(detT.get_tableFieldMap().get(detTc.getRelatedStaticTableFieldId())
													.getDsc())
											.append("=").append(detTc.getRelatedStaticTableFieldVal());
								// if(detT.get_tableParamList().size()>1)newSub.append("
								// AND
								// z").append(isss).append(".").append("customization_id=${scd.customizationId}");
								newSub.append(DBUtil.includeTenantProjectPostSQL(scd, detT, "z" + isss));
								sql.append("(").append(newSub).append(")");
								iwfField++;
								fieldName = clcFieldPrefix + iwfField;
								iwbFieldMap.put(c, fieldName);
								// if(valMap.get(c).equals("count"))valMap.put(c,"sum");
								break;
							}
				}

			} else if (c.startsWith("lnk.") && c.lastIndexOf('.') > 4) { // parents
				W5TableField tableField = null, mtableField = null;

				String[] sss = c.substring(4).replace(".", "&").split("&");
				W5Table newT = t;
				StringBuilder newSub = new StringBuilder();
				StringBuilder newSub2 = new StringBuilder();
				boolean foundSt = false;
				for (int isss = 0; isss < sss.length - 1; isss++) {
					if (isss > 0) {
						newSub2.setLength(0);
						newSub2.append(newSub);
						newSub.setLength(0);
					}
					for (W5TableField tf : newT.get_tableFieldList())
						if (tf.getDsc().equals(sss[isss])) {
							foundSt = false;
							if (mtableField == null)
								mtableField = tf;
							if (tf.getDefaultControlType() == 7 || tf.getDefaultControlType() == 9
									|| tf.getDefaultControlType() == 10 /*
																		 * || tf. getDefaultControlTip ()==15
																		 */) { // sub
																				// table
								W5Table st = FrameworkCache.getTable(scd, tf.getDefaultLookupTableId());
								if (st == null)
									break; // HATA: gerekli bir alt kademe
											// tabloya ulasilamadi
								for (W5TableField stf : st.get_tableFieldList())
									if (stf.getDsc().equals(sss[isss + 1])) {
										tableField = stf;
										foundSt = true;
										break;
									}
								if (!foundSt)
									break; // HATA: bir sonraki field bulunamadi
								newSub.append("(select ");
								if (isss == sss.length - 2)
									switch (tableField.getDefaultControlType()) {
									case 2: // date
										newSub.append("to_char(y.").append(isss).append(".").append(sss[isss + 1])
												.append(",'").append(dateFormat).append("')");
										break;
									case 7:
									case 10:
									case 9: // lookup
										if (tableField.getDefaultLookupTableId() > 0) {
											W5Table dt = FrameworkCache.getTable(scd,
													tableField.getDefaultLookupTableId());
											if (dt != null && !GenericUtil.isEmpty(dt.getSummaryRecordSql())) {
												newSub.append("(SELECT ")
														.append(dt.getSummaryRecordSql().replaceAll("x.", "qxq."))
														.append(" FROM ").append(dt.getDsc()).append(" qxq WHERE qxq.")
														.append(dt.get_tableFieldList().get(0).getDsc()).append("=y")
														.append(isss).append(".").append(sss[isss + 1]);
												// if(dt.get_tableParamList().size()>1)newSub.append("
												// AND
												// qxq.customization_id=${scd.customizationId}");
												newSub.append(DBUtil.includeTenantProjectPostSQL(scd, dt, "qxq"));

												newSub.append(")");
											} else
												newSub.append("y").append(isss).append(".").append(sss[isss + 1]);
										} else
											newSub.append("y").append(isss).append(".").append(sss[isss + 1]);
										break;
									default:
										newSub.append("y").append(isss).append(".").append(sss[isss + 1]);
									}
								else
									newSub.append("y").append(isss).append(".").append(sss[isss + 1]);
								newSub.append(" from ").append(st.getDsc()).append(" y").append(isss).append(" where y")
										.append(isss).append(".").append(st.get_tableFieldList().get(0).getDsc())
										.append("=").append(isss == 0 ? ("x." + sss[isss]) : newSub2);
								if (st.get_tableFieldList().size() > 1)
									for (W5TableField wtf : st.get_tableFieldList())
										if (wtf.getDsc().equals("project_uuid")) {
											newSub.append(" AND y").append(isss)
													.append(".project_uuid='${scd.projectId}'");
											break;
										}

								newSub.append(")");
								newT = st;
							}
							break;
						}
					if (!foundSt) { // bulamamis uygun sey
						break;
					}
				}
				if (foundSt && newSub.length() > 0) {
					sql.append("(").append(newSub).append(")"); // .put(tableFieldChain,
																// o.toString());
				} else
					throw new IWBException("framework", "Query", 0, null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_pivot_field_error"), null);

				iwfField++;
				fieldName = clcFieldPrefix + iwfField;
				iwbFieldMap.put(c, fieldName);
				if (tableField != null && tableField.getDefaultControlType() == 6) {
					W5LookUp lu = FrameworkCache.getLookUp(scd, tableField.getDefaultLookupTableId());
					if (lu != null)
						staticLookups.put(fieldName, lu);
				}

			} else {
				if (c.startsWith("lnk.")) {
					iwbFieldMap.put(c, c.substring(4));
					c = c.substring(4);
				}
				for (W5TableField f : t.get_tableFieldList())
					if (f.getDsc().equals(c)) {
						if (f.getDefaultControlType() == 6 && f.getDefaultLookupTableId() > 0) {
							W5LookUp lu = FrameworkCache.getLookUp(scd, f.getDefaultLookupTableId());
							if (lu != null)
								staticLookups.put(f.getDsc(), lu);
						}
						if ((f.getDefaultControlType() == 7 || f.getDefaultControlType() == 10)
								&& f.getDefaultLookupTableId() > 0) {
							W5Table dt = FrameworkCache.getTable(scd, f.getDefaultLookupTableId());
							if (dt != null && !GenericUtil.isEmpty(dt.getSummaryRecordSql())) {
								sql.append("(SELECT ").append(dt.getSummaryRecordSql().replaceAll("x.", "qxq."))
										.append(" FROM ").append(dt.getDsc()).append(" qxq WHERE qxq.")
										.append(dt.get_tableFieldList().get(0).getDsc()).append("=x.")
										.append(f.getDsc());
								// if(dt.get_tableParamList().size()>1)sql.append("
								// AND
								// qxq.customization_id=${scd.customizationId}");
								sql.append(DBUtil.includeTenantProjectPostSQL(scd, dt, "qxq"));
								sql.append(")");
							} else {
								sql.append("x.").append(f.getDsc());
							}
						} else {
							if (f.getFieldType() == 2) { // date ise
								sql.append("to_char(x.").append(f.getDsc()).append(",'").append(dateFormat)
										.append("')");
							} else
								sql.append("x.").append(f.getDsc());
						}
						fieldName = f.getDsc();
						break;
					}
			}

			sql.append(" ").append(fieldName).append(",");
		}
		sql.setLength(sql.length() - 1);
		sql.append(" FROM ").append(t.getDsc()).append(" x");
		if (t.get_tableParamList().size() > 1)
			sql.append(" WHERE 1=1").append(DBUtil.includeTenantProjectPostSQL(scd, t));
		// GROUP BY ").append(groupBy.substring(1)
		StringBuilder sql2 = sql;
		sql2.append(" limit 100000"); // simdilik sinir koyalim

		Object[] oz = DBUtil.filterExt4SQL(sql2.toString(), scd, requestParams, null);
		List<Map> lm = executeSQLQuery2Map(oz[0].toString(), (List) oz[1]);
		if (!staticLookups.isEmpty() || !iwbFieldMap.isEmpty())
			for (Map<String, Object> mo : lm) {
				for (String k : staticLookups.keySet()) {
					W5LookUp lu = staticLookups.get(k);
					W5LookUpDetay ld = lu.get_detayMap().get(mo.get(k));
					if (ld != null)
						mo.put(k, LocaleMsgCache.get2(scd, ld.getDsc()));
				}
				for (String k : iwbFieldMap.keySet()) {
					mo.put(k, mo.get(iwbFieldMap.get(k)));
					mo.remove(iwbFieldMap.get(k));
				}
			}
		return lm;
	}

	public List executeQuery4Pivot(Map<String, Object> scd, W5Table t, Map<String, String> requestParams) {


		StringBuilder sql = new StringBuilder();
		String dateFormat = GenericUtil.uStrNvl(requestParams.get("dtFmt"), "yyyy");
		sql.append("SELECT ");
		String[] cols = requestParams.get("cols").split(",");
		String vals = requestParams.get("vals");
		Map<String, String> valMap = new HashMap();
		boolean cldFlag = false, cntFlag = false;
		if (!GenericUtil.isEmpty(vals)) {
			if (vals.equals("count") || vals.equals(":count")) {
				valMap.put("1", "count");
			} else
				for (String s : vals.split(",")) {
					String[] o2 = s.replace(':', ',').split(",");
					String fnc = (o2.length > 1 && GenericUtil.hasPartInside("sum,count,min,max", o2[1].toLowerCase()))
							? o2[1].toLowerCase()
							: "count";
					if (fnc.equals("count")) {
						// valMap.clear();break;
						cntFlag = true;
						// return executeQuery4PivotBasic(scd, tableId,
						// requestParams);
					}
					if (o2[0].startsWith("cld.")) {
						cldFlag = true;
					}
					valMap.put(o2[0], fnc);
				}
		}
		if (!cldFlag && cntFlag) {
			valMap.clear();
		}

		Map<String, W5LookUp> staticLookups = new HashMap();
		String groupBy = "", clcFieldPrefix = "iwb_x_qw_";
		int iwfField = 0;
		Map<String, String> iwbFieldMap = new HashMap();
		Map<String, String> errorMap = new HashMap();
		for (String c : cols) {
			String fieldName = "";

			if (c.startsWith("clc.")) {
				String c2 = c.substring(4);
				List<W5TableFieldCalculated> l = metadataLoader.findTableCalcFieldByName(t.getProjectUuid(), t.getTableId(), c2);
				if (!l.isEmpty()) {
					sql.append("(").append(l.get(0).getSqlCode()).append(")");
					iwfField++;
					fieldName = clcFieldPrefix + iwfField;
					iwbFieldMap.put(c, fieldName);
				} else { // TODO ERROR
					errorMap.put(c, "Calculated Field not found");
					continue;
				}
				// sql.append("").append(l.get(0).getSqlCode().replaceAll("x.",
				// "x.")).append(" as
				// ").append(l.get(0).getDsc()).append(",");
			} else if (c.startsWith("cld.")) { // childs: only count and sum
				if (!valMap.containsKey(c) || GenericUtil.isEmpty(t.get_tableChildList())) {
					errorMap.put(c, "Child Tables does not exist for [" + t.getDsc() + "]");
					continue;
				}
				String[] sss = c.substring(4).replace(".", "&").split("&");
				if (sss.length == 1) {
					errorMap.put(c, "Table Child Field not defined");
					continue;
				}
				StringBuilder newSub = new StringBuilder();
				W5Table detT = null;
				W5TableChild detTc = null;
				for (W5TableChild tc : t.get_tableChildList()) {
					detT = FrameworkCache.getTable(scd, tc.getRelatedTableId());
					if (detT != null && detT.getDsc().equals(sss[0])) {
						detTc = tc;
						break;
					}
				}
				if (detT == null) {
					errorMap.put(c, "Table Child not found");
					continue;
				}
				for (int isss = 1; isss < sss.length && isss < 2; isss++) { // TODO
																			// simdilik
																			// 1
																			// seviye
					if (sss[isss].equals("cld")) {
						isss++;
						if (isss >= sss.length) {
							errorMap.put(c, "Child Tables wrong definition");
							continue;
						}
						if (GenericUtil.isEmpty(detT.get_tableChildList())) {
							errorMap.put(c, "Child Tables does not exist for [" + detT.getDsc() + "]");
							continue;
						}
						for (W5TableChild tc : detT.get_tableChildList()) {
							detT = FrameworkCache.getTable(scd, tc.getRelatedTableId());
							if (detT != null && detT.getDsc().equals(sss[isss])) {
								detTc = tc;
								break;
							}
						}
						errorMap.put(c, "Child Tables not implemented"); // TODO
						continue;

					} else if (sss[isss].equals("clc")) {
						isss++;
						if (isss >= sss.length) {
							errorMap.put(c, "Calculated Field wrong definition");
							continue;
						}
						List<W5TableFieldCalculated> l = metadataLoader.findTableCalcFieldByName(detT.getProjectUuid(), detT.getTableId(), sss[isss]);
						if (!l.isEmpty()) {
							newSub.append("SELECT ").append(valMap.get(c)).append("((")
									.append(l.get(0).getSqlCode().replaceAll("x.", "z" + isss + ".")).append(")) from ")
									.append(detT.getDsc()).append(" z").append(isss).append(" WHERE ");
							newSub.append(" x.").append(t.get_tableFieldMap().get(detTc.getTableFieldId()).getDsc())
									.append("=z").append(isss).append(".")
									.append(detT.get_tableFieldMap().get(detTc.getRelatedTableFieldId()).getDsc());
							if (detTc.getRelatedStaticTableFieldId() > 0
									&& !GenericUtil.isEmpty(detTc.getRelatedStaticTableFieldVal()))
								newSub.append(" AND z").append(isss).append(".").append(
										detT.get_tableFieldMap().get(detTc.getRelatedStaticTableFieldId()).getDsc())
										.append("=").append(detTc.getRelatedStaticTableFieldVal());
							// if(detT.get_tableParamList().size()>1)newSub.append("
							// AND
							// z").append(isss).append(".").append("customization_id=${scd.customizationId}");
							newSub.append(DBUtil.includeTenantProjectPostSQL(scd, detT, "z" + isss));

							sql.append("(").append(newSub).append(")");
							iwfField++;
							fieldName = clcFieldPrefix + iwfField;
							iwbFieldMap.put(c, fieldName);
							if (valMap.get(c).equals("count"))
								valMap.put(c, "sum");
						} else { // TODO ERROR
							errorMap.put(c, "Calculated Field not found");
							continue;
						}

					} else
						for (W5TableField tf : detT.get_tableFieldList())
							if (tf.getDsc().equals(sss[isss])) {
								newSub.append("SELECT ").append(valMap.get(c)).append("(z").append(isss).append(".")
										.append(tf.getDsc()).append(") from ").append(detT.getDsc()).append(" z")
										.append(isss).append(" WHERE ");
								newSub.append(" x.").append(t.get_tableFieldMap().get(detTc.getTableFieldId()).getDsc())
										.append("=z").append(isss).append(".")
										.append(detT.get_tableFieldMap().get(detTc.getRelatedTableFieldId()).getDsc());
								if (detTc.getRelatedStaticTableFieldId() > 0
										&& !GenericUtil.isEmpty(detTc.getRelatedStaticTableFieldVal()))
									newSub.append(" AND z").append(isss).append(".")
											.append(detT.get_tableFieldMap().get(detTc.getRelatedStaticTableFieldId())
													.getDsc())
											.append("=").append(detTc.getRelatedStaticTableFieldVal());
								// if(detT.get_tableParamList().size()>1)newSub.append("
								// AND
								// z").append(isss).append(".").append("customization_id=${scd.customizationId}");
								newSub.append(DBUtil.includeTenantProjectPostSQL(scd, detT, "z" + isss));
								sql.append("(").append(newSub).append(")");
								iwfField++;
								fieldName = clcFieldPrefix + iwfField;
								iwbFieldMap.put(c, fieldName);
								if (valMap.get(c).equals("count"))
									valMap.put(c, "sum");
								break;
							}
				}

			} else if (c.startsWith("lnk.") && c.lastIndexOf('.') > 4) { // parents
				W5TableField tableField = null, mtableField = null;

				String[] sss = c.substring(4).replace(".", "&").split("&");
				W5Table newT = t;
				StringBuilder newSub = new StringBuilder();
				StringBuilder newSub2 = new StringBuilder();
				boolean foundSt = false;
				for (int isss = 0; isss < sss.length - 1; isss++) {
					if (isss > 0) {
						newSub2.setLength(0);
						newSub2.append(newSub);
						newSub.setLength(0);
					}
					for (W5TableField tf : newT.get_tableFieldList())
						if (tf.getDsc().equals(sss[isss])) {
							foundSt = false;
							if (mtableField == null)
								mtableField = tf;
							if (tf.getDefaultControlType() == 7 || tf.getDefaultControlType() == 9
									|| tf.getDefaultControlType() == 10 /*
																		 * || tf. getDefaultControlTip ()==15
																		 */) { // sub
																				// table
								W5Table st = FrameworkCache.getTable(scd, tf.getDefaultLookupTableId());
								if (st == null)
									break; // HATA: gerekli bir alt kademe
											// tabloya ulasilamadi
								for (W5TableField stf : st.get_tableFieldList())
									if (stf.getDsc().equals(sss[isss + 1])) {
										tableField = stf;
										foundSt = true;
										break;
									}
								if (!foundSt)
									break; // HATA: bir sonraki field bulunamadi
								newSub.append("(select ");
								if (isss == sss.length - 2)
									switch (tableField.getDefaultControlType()) {
									case 2: // date
										newSub.append("to_char(y.").append(isss).append(".").append(sss[isss + 1])
												.append(",'").append(dateFormat).append("')");
										break;
									case 7:
									case 10:
									case 9: // lookup
										if (tableField.getDefaultLookupTableId() > 0) {
											W5Table dt = FrameworkCache.getTable(scd,
													tableField.getDefaultLookupTableId());
											if (dt != null && !GenericUtil.isEmpty(dt.getSummaryRecordSql())) {
												newSub.append("(SELECT ")
														.append(dt.getSummaryRecordSql().replaceAll("x.", "qxq."))
														.append(" FROM ").append(dt.getDsc()).append(" qxq WHERE qxq.")
														.append(dt.get_tableFieldList().get(0).getDsc()).append("=y")
														.append(isss).append(".").append(sss[isss + 1]);
												// if(dt.get_tableParamList().size()>1)newSub.append("
												// AND
												// qxq.customization_id=${scd.customizationId}");
												newSub.append(DBUtil.includeTenantProjectPostSQL(scd, dt, "qxq"));

												newSub.append(")");
											} else
												newSub.append("y").append(isss).append(".").append(sss[isss + 1]);
										} else
											newSub.append("y").append(isss).append(".").append(sss[isss + 1]);
										break;
									default:
										newSub.append("y").append(isss).append(".").append(sss[isss + 1]);
									}
								else
									newSub.append("y").append(isss).append(".").append(sss[isss + 1]);
								newSub.append(" from ").append(st.getDsc()).append(" y").append(isss).append(" where y")
										.append(isss).append(".").append(st.get_tableFieldList().get(0).getDsc())
										.append("=").append(isss == 0 ? ("x." + sss[isss]) : newSub2);
								if (st.get_tableFieldList().size() > 1)
									for (W5TableField wtf : st.get_tableFieldList())
										if (wtf.getDsc().equals("project_uuid")) {
											newSub.append(" AND y").append(isss)
													.append(".project_uuid='${scd.projectId}'");
											break;
										}

								newSub.append(")");
								newT = st;
							}
							break;
						}
					if (!foundSt) { // bulamamis uygun sey
						break;
					}
				}
				if (foundSt && newSub.length() > 0) {
					sql.append("(").append(newSub).append(")"); // .put(tableFieldChain,
																// o.toString());
				} else
					throw new IWBException("framework", "Query", 0, null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_pivot_field_error"), null);

				iwfField++;
				fieldName = clcFieldPrefix + iwfField;
				iwbFieldMap.put(c, fieldName);
				if (tableField != null && tableField.getDefaultControlType() == 6) {
					W5LookUp lu = FrameworkCache.getLookUp(scd, tableField.getDefaultLookupTableId());
					if (lu != null)
						staticLookups.put(fieldName, lu);
				}

			} else {
				if (c.startsWith("lnk.")) {
					iwbFieldMap.put(c, c.substring(4));
					c = c.substring(4);
				}
				for (W5TableField f : t.get_tableFieldList())
					if (f.getDsc().equals(c)) {
						if (f.getDefaultControlType() == 6 && f.getDefaultLookupTableId() > 0) {
							W5LookUp lu = FrameworkCache.getLookUp(scd, f.getDefaultLookupTableId());
							if (lu != null)
								staticLookups.put(f.getDsc(), lu);
						}
						if ((f.getDefaultControlType() == 7 || f.getDefaultControlType() == 10)
								&& f.getDefaultLookupTableId() > 0) {
							W5Table dt = FrameworkCache.getTable(scd, f.getDefaultLookupTableId());
							if (dt != null && !GenericUtil.isEmpty(dt.getSummaryRecordSql())) {
								sql.append("(SELECT ").append(dt.getSummaryRecordSql().replaceAll("x.", "qxq."))
										.append(" FROM ").append(dt.getDsc()).append(" qxq WHERE qxq.")
										.append(dt.get_tableFieldList().get(0).getDsc()).append("=x.")
										.append(f.getDsc());
								// if(dt.get_tableParamList().size()>1)sql.append("
								// AND
								// qxq.customization_id=${scd.customizationId}");
								sql.append(DBUtil.includeTenantProjectPostSQL(scd, dt, "qxq"));

								sql.append(")");
							} else {
								sql.append("x.").append(f.getDsc());
							}
						} else {
							if (f.getFieldType() == 2) { // date ise
								sql.append("to_char(x.").append(f.getDsc()).append(",'").append(dateFormat)
										.append("')");
							} else
								sql.append("x.").append(f.getDsc());
						}
						fieldName = f.getDsc();
						break;
					}
			}
			if (!valMap.containsKey(c))
				groupBy += "," + fieldName;

			sql.append(" ").append(fieldName).append(",");
		}
		sql.setLength(sql.length() - 1);
		sql.append(" FROM ").append(t.getDsc()).append(" x");
		if (t.get_tableParamList().size() > 1)
			sql.append(" WHERE 1=1").append(DBUtil.includeTenantProjectPostSQL(scd, t));
		// GROUP BY ").append(groupBy.substring(1)
		StringBuilder sql2;
		if (!valMap.isEmpty()) {
			sql2 = new StringBuilder(sql.length() + 3 * groupBy.length() + 50);
			sql2.append("SELECT ").append(groupBy.substring(1));
			for (String c : valMap.keySet()) {
				String fieldName = iwbFieldMap.containsKey(c) ? iwbFieldMap.get(c) : c;
				sql2.append(",").append(valMap.get(c)).append("(").append(fieldName).append(") ")
						.append(fieldName.equals("1") ? "xxx" : fieldName);
			}
			sql2.append(" FROM (").append(sql).append(") mq GROUP BY ").append(groupBy.substring(1));
		} else
			sql2 = sql;

		Object[] oz = DBUtil.filterExt4SQL(sql2.toString(), scd, requestParams, null);
		List<Map> lm = executeSQLQuery2Map(oz[0].toString(), (List) oz[1]);
		if (!staticLookups.isEmpty() || !iwbFieldMap.isEmpty())
			for (Map<String, Object> mo : lm) {
				for (String k : staticLookups.keySet()) {
					W5LookUp lu = staticLookups.get(k);
					W5LookUpDetay ld = lu.get_detayMap().get(mo.get(k));
					if (ld != null)
						mo.put(k, LocaleMsgCache.get2(scd, ld.getDsc()));
				}
				for (String k : iwbFieldMap.keySet()) {
					mo.put(k, mo.get(iwbFieldMap.get(k)));
					mo.remove(iwbFieldMap.get(k));
				}
			}
		return lm;
	}

	public String deleteSubProject(Map scd, String subProjectId) {
		W5Project po = FrameworkCache.getProject(scd);
		W5Project dpo = FrameworkCache.getProject(subProjectId);
		if (po == null || dpo == null)
			return "Projects do not exist";
		List ll = executeSQLQuery(
				"select 1 from iwb.w5_project_related_project where project_uuid=? AND related_project_uuid=?",
				po.getProjectUuid(), dpo.getProjectUuid());
		if (GenericUtil.isEmpty(ll))
			return "No related project record";

		executeUpdateSQLQuery(
				"delete from iwb.w5_project_related_project where project_uuid=? AND related_project_uuid=?",
				po.getProjectUuid(), dpo.getProjectUuid());

		executeUpdateSQLQuery(
				"delete from iwb.w5_vcs_commit where project_uuid=? AND oproject_uuid=? and vcs_commit_id<0",
				po.getProjectUuid(), dpo.getProjectUuid());

		List<Object[]> tableNames = executeSQLQuery(
				"select t.dsc,(select tf.default_value from iwb.w5_table_field tf where tf.table_id=t.table_id AND tf.tab_order=1 AND tf.project_uuid=t.project_uuid limit 1) from iwb.w5_table t where t.project_uuid=? AND t.oproject_uuid=? order by table_id desc",
				po.getProjectUuid(), dpo.getProjectUuid());
		executeUpdateSQLQuery("set search_path=" + po.getRdbmsSchema());
		if (!GenericUtil.isEmpty(tableNames)) {
			StringBuilder sql = new StringBuilder();
			for (Object[] t : tableNames)
				if (GenericUtil.uInt(executeSQLQuery(
						"select count(1) from information_schema.tables qx where qx.table_name = ? and qx.table_schema = ?",
						t[0], po.getRdbmsSchema()).get(0)) > 0) {
					sql.append("drop table ").append(t[0]).append(";");
					if (!GenericUtil.isEmpty(t[1]) && t[1].toString().toLowerCase().startsWith("nextval(")) {
						String seq = t[1].toString().substring("nextval(".length() + 1, t[1].toString().length() - 2);
						if (GenericUtil.uInt(executeSQLQuery(
								"select count(1) from information_schema.sequences qx where qx.sequence_name = ? and qx.sequence_schema = ?",
								seq, po.getRdbmsSchema()).get(0)) > 0)
							sql.append("drop sequence ").append(seq).append(";");
					}
					sql.append("\n");
				}
			if (sql.length() != 0) {
				executeUpdateSQLQuery(sql.toString());
				W5VcsCommit commit = new W5VcsCommit();
				commit.setProjectUuid(po.getProjectUuid());
				commit.setCommitTip((short) 2);
				commit.setExtraSql(sql.toString());
				commit.setComment("iWB. Clean tables of sub project: " + dpo.getDsc());
				commit.setCommitUserId((Integer) scd.get("userId"));
				Object oi = executeSQLQuery("select nextval('iwb.seq_vcs_commit')").get(0);
				commit.setVcsCommitId(-GenericUtil.uInt(oi));
				saveObject(commit);
			}
		}

		executeUpdateSQLQuery("update iwb.w5_vcs_object x " + "set vcs_object_status_tip=3 "
				+ "where x.project_uuid=? AND x.vcs_object_status_tip in (1,9) AND exists(select 1 from iwb.w5_vcs_object z where z.project_uuid=? AND z.table_id=x.table_id AND z.table_pk=x.table_pk)",
				po.getProjectUuid(), subProjectId);

		executeUpdateSQLQuery("delete from iwb.w5_vcs_object x where "
				+ "x.project_uuid=? AND x.vcs_object_status_tip=2 AND exists(select 1 from iwb.w5_vcs_object z where z.project_uuid=? AND z.table_id=x.table_id AND z.table_pk=x.table_pk)",
				po.getProjectUuid(), subProjectId);

		List<Object> ll3 = executeSQLQuery(
				"select x.table_id from iwb.w5_vcs_object x where x.project_uuid=? group by x.table_id order by x.table_id",
				subProjectId);
		if (ll3 != null)
			for (Object o : ll3) {
				StringBuilder sql = new StringBuilder();
				W5Table t = FrameworkCache.getTable(0, GenericUtil.uInt(o));
				String pkField = t.get_tableParamList().get(0).getExpressionDsc();
				sql.append("delete from ").append(t.getDsc()).append(" x where x.project_uuid=? AND x.oproject_uuid=?");
				executeUpdateSQLQuery(sql.toString(), po.getProjectUuid(), subProjectId);
			}

		return null;
	}



	public void checkTenant(Map<String, Object> scd) {
		W5Project po = FrameworkCache.getProject(scd);
		if (po != null)
			executeUpdateSQLQuery("set search_path=" + po.getRdbmsSchema());
	}

	public void reloadUsersCache(String projectId) { // customizationID ??
		List<Object[]> l = (List<Object[]>) executeSQLQuery(
				"select x.customization_id, x.user_id, x.user_name, x.dsc, 1 allow_multi_login_flag, x.profile_picture_id from iwb.w5_user x "
						+ (projectId != null ? (" where x.project_uuid='" + projectId + "'") : ""));
		if (l != null)
			for (Object[] m : l) {
				UserUtil.addUserWithProfilePicutre(GenericUtil.uInt(m[1]), (String) m[2], (String) m[3],
						GenericUtil.uInt(m[4]) != 0, GenericUtil.uInt(m[5]));
			}
	}

	public void reloadUsersCache(int customizationId) { // customizationID ??
		UserUtil.addUserWithProfilePicutre(0, "code2", "code2", true, 1);
		List<Object[]> l = (List<Object[]>) executeSQLQuery(
				"select x.customization_id, x.user_id, x.user_name, x.dsc, 1 allow_multi_login_flag, x.profile_picture_id from iwb.w5_user x "
						+ (customizationId >= 0 ? (" where x.customization_id=" + customizationId + "") : ""));
		if(FrameworkSetting.projectId!=null && FrameworkSetting.projectId.length()!=1 && !FrameworkSetting.projectId.equals(FrameworkSetting.devUuid)) {
			W5Project po = FrameworkCache.getProject(FrameworkSetting.projectId);
			if(FrameworkCache.getTable(FrameworkSetting.projectId, 3107)!=null) {
				List l2 = (List<Object[]>) executeSQLQuery(
					"select 0 customization_id, x.user_id, x.user_name, x.full_name dsc, 1 allow_multi_login_flag, 1 profile_picture_id from "
					+ po.getRdbmsSchema() + ".x_user x");
				if(l2!=null) {
					if(l==null) l = l2;
					else l.addAll(l2);
				}
			}
			
		}
		if (l != null)for (Object[] m : l) {
			UserUtil.addUserWithProfilePicutre(GenericUtil.uInt(m[1]), (String) m[2], (String) m[3],
					GenericUtil.uInt(m[4]) != 0, GenericUtil.uInt(m[5]));
		}		
	}

  public void copyTableRecord4VCS(Map<String, Object> scd, Map dstScd, int tableId, int tablePk) {
		W5Table t = FrameworkCache.getTable(scd, tableId);
		W5TableParam tp = t.get_tableParamList().get(0);
		StringBuilder preSql = new StringBuilder();
		preSql.append("select count(1) qq from ").append(t.getDsc()).append(" x where x.")
			.append(tp.getExpressionDsc()).append("=").append(tablePk).append(" AND x.project_uuid='")
			.append(dstScd.get("projectId")).append("'");
		Object oo = executeSQLQuery(preSql.toString()).get(0);
		if(GenericUtil.uInt(oo)!=0)return;
		
		StringBuilder b = new StringBuilder();
		b.append("insert into ").append(t.getDsc()).append("(");
		StringBuilder b2 = new StringBuilder();
		for(W5TableField tf:t.get_tableFieldList()) {
			b.append(tf.getDsc()).append(",");
			if(tf.getDsc().equals("customization_id")) {
				b2.append(dstScd.get("customizationId"));
			} else if(tf.getDsc().equals("version_dttm")) {
				b2.append("current_timestamp");
			} else if(GenericUtil.hasPartInside("project_uuid", tf.getDsc())) {
				b2.append("'").append(dstScd.get("projectId")).append("'");
			} else 
				b2.append(tf.getDsc());
			b2.append(",");
			
			
		}
		b.setLength(b.length()-1);b2.setLength(b2.length()-1);
		
		b.append(") select ").append(b2);
		b.append(" from ").append(t.getDsc()).append(" where ").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=").append(tablePk)
		.append(" AND project_uuid='").append(scd.get("projectId")).append("'");
		


		Session session = getCurrentSession();

		try {
			SQLQuery query = session.createSQLQuery(b.toString());
			int res = query.executeUpdate();
			if(res==1) {
				saveObject(new W5VcsObject(dstScd, tableId, tablePk));
			}
		} catch (Exception e) {
			if (FrameworkSetting.debug)
				e.printStackTrace();
			throw new IWBException("sql", "Copy Table Record", tableId, b.toString() + " --> " + tablePk,
						"Error Copying Table to New Project", e);
		} finally {
			// session.close();
		}
    
  }


	public Map insertTableJSON(Map<String, Object> scd, int tableId, Map requestParams) {
		W5Table t = FrameworkCache.getTable(scd, tableId);
		if (!GenericUtil.accessControl(scd, t.getAccessViewTip(), t.getAccessViewRoles(), t.getAccessViewUsers())){
			throw new IWBException("security", "Module", 0, null,
					LocaleMsgCache.get2(scd, "fw_access_control_view"), null);
		}
		if (!GenericUtil.accessControl(scd, t.getAccessInsertTip(), t.getAccessInsertRoles(), t.getAccessInsertUsers())){
			throw new IWBException("security", "Module", 0, null,
					LocaleMsgCache.get2(scd, "fw_access_control_insert"), null);
		}
		
		
		final StringBuilder sql = new StringBuilder(), postSql = new StringBuilder();
		sql.append("insert into ");
		sql.append(t.getDsc()).append(" ( ");
		postSql.append(" values (");
		final List<Object> insertParams = new ArrayList<Object>();
		boolean b = false;
		int paramCount = 0;
		final Map<Integer, String> calculatedParams = new HashMap<Integer, String>();
		final Map<Integer, String> calculatedParamNames = new HashMap<Integer, String>();
		Set<String> usedFields = new HashSet<String>();

		Map errorMap = new HashMap();
		Map outMap = new HashMap();

	
		for (W5TableField p1 : t.get_tableFieldList()) {
			if(!requestParams.containsKey(p1.getDsc()) && p1.getSourceType()==1)continue;
			if(requestParams.containsKey(p1.getDsc())) {
				Object presult = requestParams.get(p1.getDsc());
				if (b) {
					sql.append(" , ");
					postSql.append(" , ");
				} else
					b = true;
				usedFields.add(p1.getDsc());
				sql.append(p1.getDsc());
				
				postSql.append(" ? ");
				//insertParams.add(presult);
				if(presult==null || p1.getLkpEncryptionType()==0) {
					if(presult!=null)switch(p1.getFieldType()) {
					case 2: if(scd!=null && presult!=null) {
						presult = GenericUtil.uDate(presult.toString(), GenericUtil.uInt(scd.get("date_format")));
						break;
					}
					default:
						presult = GenericUtil.getObjectByTip(presult.toString(), p1.getFieldType());
					}
					insertParams.add(presult);
				} else 
					insertParams.add(EncryptionUtil.encrypt(presult.toString(), p1.getLkpEncryptionType()));
				paramCount++;
				
			} else switch (p1.getSourceType()) {
			case 4: // SQL calculated Fieldlar icin
				if (b) {
					sql.append(" , ");
					postSql.append(" , ");
				} else
					b = true;
				usedFields.add(p1.getDsc());
				sql.append(p1.getDsc());
				calculatedParams.put(paramCount, GenericUtil
						.filterExt(p1.getDefaultValue(), scd, requestParams, null)
						.toString());
				calculatedParamNames.put(paramCount, p1.getDsc());
				postSql.append(" ? ");
				insertParams.add(null);
				paramCount++;
				break;
			case 5:// javascript
			case 2: // session
				Object presult = GenericUtil.prepareParam(p1, scd, requestParams,
						(short) -1, null, (short) 0, null, null, errorMap);
				if (presult != null) {
					if (b) {
						sql.append(" , ");
						postSql.append(" , ");
					} else
						b = true;
					usedFields.add(p1.getDsc());
					sql.append(p1.getDsc());
					postSql.append(" ? ");
					insertParams.add(presult);
					paramCount++;
				}

				break;
			case 9: // UUID
				Object presult2 = GenericUtil.prepareParam(p1, scd, requestParams,
						(short) -1, null, (short) 0, null, null, errorMap);
				if (presult2 != null) {
					if (b) {
						sql.append(" , ");
						postSql.append(" , ");
					} else
						b = true;
					usedFields.add(p1.getDsc());
					sql.append(p1.getDsc());
					postSql.append(" ? ");
					insertParams.add(presult2);
					paramCount++;

					outMap.put(p1.getDsc(), presult2);
				}

				break;
			case 8: // Global Nextval
				Object presult3 = GenericUtil.prepareParam(p1, scd, requestParams,
						(short) -1, null, (short) 0, null, null, errorMap);
				if (presult3 != null) {
					if (b) {
						sql.append(" , ");
						postSql.append(" , ");
					} else
						b = true;
					usedFields.add(p1.getDsc());
					sql.append(p1.getDsc());
					postSql.append(" ? ");
					insertParams.add(presult3);
					paramCount++;

					outMap.put(p1.getDsc(), presult3);
				}

				break;
			}
		}



		if (usedFields.isEmpty()) { // problems exists
			throw new IWBException("validation", "Table Insert", tableId, null, "No Used Fields", null);
		}



		sql.append(" ) ").append(postSql).append(")");
		
		
		try {

			getCurrentSession().doReturningWork(new ReturningWork<Integer>() {

				public Integer execute(Connection conn) throws SQLException {
					PreparedStatement s = null;
					int count = 0;
					for (Integer o : calculatedParams.keySet()) { // calculated
																	// ve output
																	// edilecek
																	// parametreler
																	// hesaplaniyor
						// once
						String seq = calculatedParams.get(o);
						if (seq.endsWith(".nextval"))
							seq = "nextval('" + seq.substring(0, seq.length() - 8) + "')";
						s = conn.prepareStatement("select " + seq + " "); // from
																			// dual
						ResultSet rs = s.executeQuery();
						rs.next();
						Object paramOut = rs.getObject(1);
						if (paramOut != null) {
							if (paramOut instanceof java.sql.Timestamp) {
								try {
									paramOut = (java.sql.Timestamp) paramOut;
								} catch (Exception e) {
									paramOut = "java.sql.Timestamp";
								}
							} else if (paramOut instanceof java.sql.Date) {
								try {
									paramOut = rs.getTimestamp(1);
								} catch (Exception e) {
									paramOut = "java.sql.Date";
								}
							}
						}

						rs.close();
						s.close();
						insertParams.set(o, paramOut);
						outMap.put(calculatedParamNames.get(o), paramOut);
					}
					s = conn.prepareStatement(sql.toString());
					applyParameters(s, insertParams);
					count = s.executeUpdate();
					s.close();

					if (FrameworkSetting.hibernateCloseAfterWork)
						conn.close();

					return count;
				}
			});

		} catch (Exception e) {
			throw new IWBException("sql", "Table.Insert", tableId,
					GenericUtil.replaceSql(sql.toString(), insertParams), "Error Inserting", e);
		} finally {
			// session.close();
		}
		return outMap;
	}
	

	public W5QueryResult getTableRelationData(Map<String, Object> scd, int tableId, int tablePk, int relId) {
		W5Table mt = FrameworkCache.getTable(scd, tableId); // master table
		if (!GenericUtil.accessControlTable(scd, mt))
			throw new IWBException("security", "Module", 0, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_modul_control"), null);
		if (relId == 0 || GenericUtil.isEmpty(mt.get_tableChildList()))
			throw new IWBException("security", "Table", tableId, null, "wrong relationId or no children data", null);
		W5TableChild tc = null;
		for (W5TableChild qi : mt.get_tableChildList())
			if (qi.getTableChildId() == relId) {
				tc = qi;
				break;
			}
		if (tc == null)
			throw new IWBException("security", "Table", tableId, null, "relation not found", null);

		W5Table t = FrameworkCache.getTable(scd, tc.getRelatedTableId()); // detail
																			// table
		if (GenericUtil.isEmpty(t.getSummaryRecordSql()))
			throw new IWBException("framework", "Table", tableId, null, "ERROR: summarySql not defined", null);
		W5Query q = new W5Query(t.getTableId());
		q.setSqlSelect("(" + t.getSummaryRecordSql() + ") dsc, x." + t.get_tableFieldList().get(0).getDsc() + " id");
		q.setSqlFrom(t.getDsc() + " x");
		StringBuilder sqlWhere = new StringBuilder();
		sqlWhere.append("x.").append(t.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc())
				.append("=${req.id}");
		if (tc.getRelatedStaticTableFieldId() != 0)
			sqlWhere.append("AND x.").append(t.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc())
					.append("=").append(tc.getRelatedStaticTableFieldVal());
		if (t.get_tableParamList().size() > 1
				&& t.get_tableParamList().get(1).getExpressionDsc().equals("project_uuid"))
			sqlWhere.append("AND x.project_uuid='${scd.projectId}'");
		q.setSqlWhere(sqlWhere.toString());
		Map<String, String> requestParams = new HashMap();
		requestParams.put("id", "" + tablePk);
		
		List<W5QueryField> lf = new ArrayList();
		W5QueryField f1 = new W5QueryField(); f1.setTabOrder((short)1);f1.setDsc("dsc");f1.setFieldType((short)1);
		lf.add(f1);
		W5QueryField f2 = new W5QueryField(); f2.setTabOrder((short)2);f2.setDsc("id");f2.setFieldType((short)4);
		lf.add(f2);
		
		q.set_queryFields(lf); // queryField'in
		// lookUp'i
		q.set_queryParams(new ArrayList());

		W5QueryResult qr = new W5QueryResult(t.getTableId());
		qr.setScd(scd);
		qr.setRequestParams(requestParams);
		qr.setErrorMap(new HashMap());
		qr.setMainTable(t);
		qr.setQuery(q);
		boolean tabOrderFlag = false;
		for (W5TableField tf : t.get_tableFieldList())
			if (tf.getDsc().equals("tab_order")) {
				tabOrderFlag = true;
				break;
			}
		qr.setOrderBy(tabOrderFlag ? "x.tab_order asc,x." + t.get_tableFieldList().get(0).getDsc() + " desc"
				: "x." + t.get_tableFieldList().get(0).getDsc() + " desc");
		qr.prepareQuery(null);

		if (qr.getErrorMap().isEmpty()) {
			qr.setFetchRowCount(10);
			qr.setStartRowNumber(0);
			runQuery(qr);
		}

		return qr;
	}
	
}
