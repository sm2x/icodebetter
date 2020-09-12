package iwb.dao.metadata.rdbms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.hibernate.jdbc.Work;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.rdbms_impl.BaseDAO;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.engine.CRUDEngine;
import iwb.exception.IWBException;
import iwb.model.db.W5Component;
import iwb.model.db.W5Customization;
import iwb.model.db.W5ExternalDb;
import iwb.model.db.W5Project;
import iwb.model.db.W5Query;
import iwb.model.db.W5QueryFieldCreation;
import iwb.model.db.W5Table;
import iwb.model.db.W5TableField;
import iwb.model.db.W5TableParam;
import iwb.model.db.W5VcsCommit;
import iwb.model.db.W5VcsObject;
import iwb.model.db.W5WsMethodParam;
import iwb.model.result.W5FormResult;
import iwb.util.DBUtil;
import iwb.util.GenericUtil;
import iwb.util.NashornUtil;
import iwb.util.UserUtil;

@Repository
public class PostgreSQLWriter extends BaseDAO {
	@Lazy
	@Autowired
	private MetadataLoader metadataLoader;
	

	@Lazy
	@Autowired
	private CRUDEngine crudEngine;
	
	public void deleteProjectMetadata(String delProjectId) {
		String[] tables = new String[] { "w5_access_delegation", "w5_form_cell_property", 
				"w5_ws_model","w5_ws_model_param",
				"m5_list_template", "w5_ws_server_token", "w5_ws_server_method_param", "w5_ws_server_method",
				"w5_ws_server", "w5_ws_method_param", "w5_ws_method", "w5_ws", "m5_list", 
				"w5_table_trigger", "w5_form_hint",
				"w5_form_sms_mail_alarm", "w5_table_field_calculated", "w5_list_column", "w5_list",
				"w5_data_view", "w5_converted_object", "w5_form_sms_mail", "w5_custom_grid_column_condtion",
				"w5_custom_grid_column_renderer", "w5_conversion_col", "w5_conversion", "w5_feed",
				"w5_excel_import_sheet_data", "w5_excel_import_sheet", "w5_excel_import",
				"w5_table_child", "w5_bi_graph_dashboard", "w5_mobile_device",
				"w5_approval_record", "w5_approval_step", "w5_approval", "w5_jasper_report",
				"w5_user_tip", "w5_role"
				// ,"w5_user"
				, "w5_user_role", "w5_jasper_object", "w5_jasper",
				"w5_comment", "w5_form_value_cell", "w5_form_value", "w5_object_menu_item", 
				"w5_form_module", "w5_object_toolbar_item", "w5_exception", "w5_xform_builder_detail",
				"w5_xform_builder", "w5_component", "m5_menu", "w5_menu", "w5_template_object", "w5_template", "w5_sms",
				"w5_table_param", "w5_form_cell", "w5_form", "w5_db_func_param", "w5_db_func", "w5_table_field",
				"w5_table", "w5_look_up_detay", "w5_look_up", "w5_locale_msg", "w5_query_param", "w5_query_field",
				"w5_query", "w5_grid", "w5_grid_column", "w5_vcs_object", "w5_vcs_commit", "w5_project_task",
				"w5_project_invitation", "w5_project_related_project", 
				"w5_test", "w5_k8s_server", "w5_k8s_deploy", "w5_k8s_deploy_step", "w5_docker_host", 
				"w5_docker_deploy", "w5_docker_container", "w5_external_db", "w5_mq", "w5_mq_callback", "w5_job_schedule"};
		for (int qi = 0; qi < tables.length; qi++)
			executeUpdateSQLQuery("delete from iwb." + tables[qi] + " where project_uuid=?", delProjectId);

		executeUpdateSQLQuery("delete from iwb.w5_user_related_project where related_project_uuid=?", delProjectId);
	}
	

	public void deleteProjectMetadataAndDB(String delProjectId, boolean force) {
		W5Project po = FrameworkCache.getProject(delProjectId);
		if(po==null) po = metadataLoader.loadProject(delProjectId);
		if(po!=null && (force || GenericUtil.uInt(executeSQLQuery("select count(1) from iwb.w5_project x where x.customization_id=?", po.getCustomizationId()).get(0))>1)){
			deleteProjectMetadata(delProjectId);
			if(force)executeUpdateSQLQuery("delete from iwb.w5_project where project_uuid=?",delProjectId);
			executeUpdateSQLQuery("DROP SCHEMA IF EXISTS "+po.getRdbmsSchema()+" CASCADE");
		} else if(po==null && force) {
			deleteProjectMetadata(delProjectId);
			executeUpdateSQLQuery("delete from iwb.w5_project where project_uuid=?",delProjectId);
		}
	}
	

	public void extFormVcsControl(W5FormResult formResult, int action, Map<String, Object> scd,
			Map<String, String> requestParams, W5Table t, String ptablePk) {
		if (!FrameworkSetting.vcs || t.getVcsFlag() == 0)
			return;
		int tablePk = GenericUtil.uInt(ptablePk);
		if (tablePk == 0)
			return;
		switch (action) {
		case 5: // copy
		case 2: // insert
			W5VcsObject ivo = new W5VcsObject(scd, t.getTableId(), tablePk);
			saveObject(ivo);
			break;
		case 1: // update
		case 3: // delete
			List l = find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.projectUuid=?2",
					t.getTableId(), tablePk, scd.get("projectId"));
			if (l.isEmpty())
				break;
			W5VcsObject vo = (W5VcsObject) l.get(0);
			vo.setVersionDttm(new Timestamp(new Date().getTime()));
			vo.setVersionUserId((Integer) scd.get("userId"));
			switch (vo.getVcsObjectStatusType()) { // zaten insert ise
			case 0:// ignored
			case 2: // insert: direk sil
			case 3: // zaten silinmisse boyle birsey olmamali
				if (action == 3) {
					removeObject(vo);
				}
				if (vo.getVcsObjectStatusType() == 3)
					formResult.getOutputMessages().add("VCS WARNING: Already Deleted VCS Object????");
				break;

			case 1:
			case 9: // synched ve/veya edit durumunda ise
				if (action == 3) { // delete edilidliyse
					vo.setVcsObjectStatusType((short) 3);
					vo.setVcsCommitRecordHash(requestParams.get("_iwb_vcs_dsc").toString());
				} else { // update edildise simdi
					String newHash = getObjectVcsHash(scd, t.getTableId(), tablePk);
					vo.setVcsObjectStatusType((short) (vo.getVcsCommitRecordHash().equals(newHash) ? 9 : 1));
				}
				updateObject(vo);
				break;
			}
			break;
		}
	}

	public String getObjectVcsHash(Map<String, Object> scd, int tableId, int tablePk) {
		W5Table t = FrameworkCache.getTable(scd, tableId);
		StringBuilder s = new StringBuilder();
		s.append("select ").append(DBUtil.getTableFields4VCS(t, "x")).append(" xhash from ").append(t.getDsc())
				.append(" x where x.").append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
		List p = new ArrayList();
		p.add(tablePk);
		s.append(DBUtil.includeTenantProjectPostSQL(scd, t));
		List l = executeSQLQuery2Map(s.toString(), p);
		if (GenericUtil.isEmpty(l))
			return "!";
		else
			return GenericUtil.getMd5Hash((String) ((Map) l.get(0)).get("xhash"));
	}
	
	public boolean saveVcsObject(Map<String, Object> scd, int tableId, int tablePk, int action, JSONObject o) { // TODO
		// updatePlainTableRecord(t, o, vo.getTablePk(), srvCommitUserId);
		try {
			W5Table t = FrameworkCache.getTable(scd, tableId);
			StringBuilder s = new StringBuilder();
			List p = new ArrayList();
			switch (action) {
			case 1: // update
				s.append("update ").append(t.getDsc()).append(" x set ");
				for (W5TableField f : t.get_tableFieldList())
					if (f.getTabOrder() > 1) {
						if (f.getDsc().equals("insert_user_id") || f.getDsc().equals("insert_dttm")
								|| f.getDsc().equals("customization_id") || f.getDsc().equals("project_uuid"))
							continue;
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
				break;
			case 2: // insert
				s.append("insert into ").append(t.getDsc()).append("(");
				StringBuilder s2 = new StringBuilder();
				for (W5TableField f : t.get_tableFieldList())
					if (f.getTabOrder() > 0) {
						if (GenericUtil.hasPartInside2("insert_dttm,version_dttm", f.getDsc())) {
							s.append(f.getDsc()).append(",");
							s2.append("current_timestamp,");
						} else {
							s.append(f.getDsc()).append(",");
							s2.append("?,");
							if (f.getDsc().equals("project_uuid"))
								p.add(scd.get("projectId"));
							else
								try {
									if (o.has(f.getDsc())) {
										p.add(GenericUtil.getObjectByControl((String) o.get(f.getDsc()),
												f.getParamType()));
									} else
										p.add(null);
								} catch (JSONException e) {
									throw new IWBException("vcs", "JSONException : saveVcsObject", t.getTableId(),
											f.getDsc(), e.getMessage(), e);
								}
						}
					}
				s.setLength(s.length() - 1);
				s2.setLength(s2.length() - 1);
				s.append(") values (").append(s2).append(")");

				break;
			case 3: // delete
				s.append("delete from ").append(t.getDsc()).append(" x where ")
						.append(t.get_tableParamList().get(0).getExpressionDsc()).append("=?");
				p.add(tablePk);
				s.append(DBUtil.includeTenantProjectPostSQL(scd, t));
				break;
			}
			executeUpdateSQLQuery(s.toString(), p);
		} catch (Exception e) {
			throw new IWBException("framework", "Save.VCSObject", tablePk, null, "[" + tableId + "," + tablePk + "] ",
					e);
		}

		return true;
	}
	
	public void makeDirtyVcsObject(Map<String, Object> scd, int tableId, int tablePk) {
		if (FrameworkSetting.vcsServer)
			throw new IWBException("vcs", "makeDirtyVcsObject", tableId, null,
					"VCS Server not allowed to make Dirt VCS Object", null);
		List l = find("from W5VcsObject t where t.tableId=?0 AND t.tablePk=?1 AND t.projectUuid=?2", tableId, tablePk,
				scd.get("projectId"));
		if (!l.isEmpty()) {
			W5VcsObject o = (W5VcsObject) l.get(0);
			if (o.getVcsObjectStatusType() == 9) { // 1, 2, 3, 8 durumunda
													// hicbirsey degismiyor
				o.setVcsObjectStatusType((short) 1);
				updateObject(o);
			}
		} else
			saveObject(new W5VcsObject(scd, tableId, tablePk));
	}
	

	public void organizeQueryFields(Map<String, Object> scd, W5Query query, String sqlStr, List<Object> sqlParams,
			Map<String, W5QueryFieldCreation> existField, List<W5QueryFieldCreation> updateList,
			List<W5QueryFieldCreation> insertList) {
//		String projectId = (String) scd.get("projectId");
		int userId = (Integer) scd.get("userId");
		W5ExternalDb edb = FrameworkCache.getExternalDb(scd, query.getSourceObjectId());//wExternalDbs.get(projectId).get(query.getMainTableId());
		if(edb.getLkpDbType()==11) {
			String[] chunks = query.getSqlSelect().split(",");
			int i = 1, j = 0;
			for(String c:chunks) {
				String[] s = c.trim().split(" ");
				String columnName = s[s.length-1];
				if (existField.get(columnName) == null) { // eger
					W5QueryFieldCreation field = new W5QueryFieldCreation();
					field.setDsc(columnName);
					field.setCustomizationId((Integer) scd.get("customizationId"));
					if (columnName.equals("insert_user_id") || columnName.equals("version_user_id"))
						field.setPostProcessType((short) 53);
					field.setTabOrder((short) (i));
					field.setQueryId(query.getQueryId());
					field.setFieldType((short) 1);
					field.setInsertUserId(userId);
					field.setVersionUserId(userId);
					field.setVersionDttm(new java.sql.Timestamp(new java.util.Date().getTime()));
					field.setProjectUuid((String) scd.get("projectId"));
					field.setOprojectUuid((String) scd.get("projectId"));

					field.setQueryFieldId(
							GenericUtil.getGlobalNextval("iwb.seq_query_field", (String) scd.get("projectId"),
									(Integer) scd.get("userId"), (Integer) scd.get("customizationId")));
					insertList.add(field);
					j++; i++;
				} else if (existField.get(columnName) != null && (existField.get(columnName).getTabOrder() != i)) {
					W5QueryFieldCreation field = existField.get(columnName);
					field.setTabOrder((short) (i));
					field.setVersionUserId(userId);
					field.setVersionDttm(new java.sql.Timestamp(new java.util.Date().getTime()));
					updateList.add(field);
				}
				existField.remove(columnName);
				
			}
			return;
		}
		
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		StringBuilder sql2 = new StringBuilder();
		try {
			con = edb.getConnection();
			stmt = con.prepareStatement(sqlStr);
			if (sqlParams.size() > 0)
				applyParameters(stmt, sqlParams);
			rs = stmt.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();

			int columnNumber = meta.getColumnCount();
			for (int i = 1, j = 0; i <= columnNumber; i++) {
				String columnName = meta.getColumnName(i).toLowerCase(FrameworkSetting.appLocale);
				if (existField.get(columnName) == null) { // eger
					W5QueryFieldCreation field = new W5QueryFieldCreation();
					field.setDsc(columnName);
					field.setCustomizationId((Integer) scd.get("customizationId"));
					if (columnName.equals("insert_user_id") || columnName.equals("version_user_id"))
						field.setPostProcessType((short) 53);
					field.setTabOrder((short) (i));
					field.setQueryId(query.getQueryId());
					field.setFieldType((short) DBUtil.java2iwbType(meta.getColumnType(i)));
					if (field.getFieldType() == 4) {
						// numeric değerde ondalık varsa tipi 3 yap
						int sc = meta.getScale(i);
						if (sc > 0)
							field.setFieldType((short) 3);
					}
					field.setInsertUserId(userId);
					field.setVersionUserId(userId);
					field.setVersionDttm(new java.sql.Timestamp(new java.util.Date().getTime()));
					field.setProjectUuid((String) scd.get("projectId"));
					field.setOprojectUuid((String) scd.get("projectId"));

					field.setQueryFieldId(
							GenericUtil.getGlobalNextval("iwb.seq_query_field", (String) scd.get("projectId"),
									(Integer) scd.get("userId"), (Integer) scd.get("customizationId")));
					insertList.add(field);
					j++;
				} else if (existField.get(columnName) != null && (existField.get(columnName).getTabOrder() != i)) {
					W5QueryFieldCreation field = existField.get(columnName);
					field.setTabOrder((short) (i));
					field.setVersionUserId(userId);
					field.setVersionDttm(new java.sql.Timestamp(new java.util.Date().getTime()));
					updateList.add(field);
				}
				existField.remove(columnName);
			}
			rs.close();
			stmt.close();
			if (FrameworkSetting.hibernateCloseAfterWork)
				con.close();
		} catch (Exception e) {
//			error = e.getMessage();
			throw new IWBException("sql", "External.Query", query.getQueryId(),
					GenericUtil.replaceSql(sqlStr, sqlParams), "[8," + query.getQueryId() + "] " + query.getDsc(), e);
		}

	}
	
	public void organizeQueryFields(final Map<String, Object> scd, final int queryId, final short insertFlag) {
		W5Project po = FrameworkCache.getProject(scd);

		executeUpdateSQLQuery("set search_path=" + po.getRdbmsSchema());
		final int userId = (Integer) scd.get("userId");
		final List<W5QueryFieldCreation> updateList = new ArrayList<W5QueryFieldCreation>();
		final List<W5QueryFieldCreation> insertList = new ArrayList<W5QueryFieldCreation>();

		for (final W5Query query : (List<W5Query>) find("from W5Query t where t.queryId=?0 AND t.projectUuid=?1",
				queryId, po.getProjectUuid())) {

			if (query.getQuerySourceType() == 1376) {
				organizeQueryFields4WSMethod(scd, query, insertFlag);
				continue;
			}
			final Map<String, W5QueryFieldCreation> existField = new HashMap<String, W5QueryFieldCreation>();
			final List<Object> sqlParams = new ArrayList();
			List<W5QueryFieldCreation> existingQueryFields = find(
					"from W5QueryFieldCreation t where t.queryId=?0 AND t.projectUuid=?1", queryId,
					po.getProjectUuid());
			for (W5QueryFieldCreation field : existingQueryFields) {
				existField.put(field.getDsc().toLowerCase(FrameworkSetting.appLocale), field);
			}

			if (query.getQuerySourceType() == 0) {
				String[] fieldNames = query.getSqlSelect().split(",");
				int i = 1;
				for(String columnName:fieldNames) if(existField.get(columnName.toLowerCase(FrameworkSetting.appLocale)) == null){
					W5QueryFieldCreation field = new W5QueryFieldCreation();
					field.setDsc(columnName);
					field.setCustomizationId((Integer) scd.get("customizationId"));
					if (columnName.equals("insert_user_id") || columnName.equals("version_user_id"))
						field.setPostProcessType((short) 53);
					field.setTabOrder((short) (i));
					field.setQueryId(query.getQueryId());
					field.setFieldType((short)1);
					field.setInsertUserId(userId);
					field.setVersionUserId(userId);
					field.setVersionDttm(new java.sql.Timestamp(new java.util.Date().getTime()));
					field.setProjectUuid((String) scd.get("projectId"));
					field.setOprojectUuid((String) scd.get("projectId"));
					field.setQueryFieldId(GenericUtil.getGlobalNextval("iwb.seq_query_field",
							(String) scd.get("projectId"), (Integer) scd.get("userId"),
							(Integer) scd.get("customizationId")));
					insertList.add(field);
					i++;
				}
			} else {
				StringBuilder sql = new StringBuilder();
				sql.append("select ").append(query.getSqlSelect());
				sql.append(" from ");
				sql.append(query.getSqlFrom());
//				if (query.getSqlWhere() != null && query.getSqlWhere().trim().length() > 0)sql.append(" where ").append(query.getSqlWhere().trim());
				sql.append(" where 1=2");// .append(query.getSqlWhere().trim());
				if (query.getSqlGroupby() != null && query.getSqlGroupby().trim().length() > 0
						&& query.getQueryType() != 9) // group
														// by
														// connect
														// olmayacak
					sql.append(" group by ").append(query.getSqlGroupby().trim());

	
				Object[] oz = DBUtil.filterExt4SQL(sql.toString(), scd, null, null);
				final String sqlStr = ((StringBuilder) oz[0]).toString();
				if (oz[1] != null)
					sqlParams.addAll((List) oz[1]);
				else
					for (int qi = 0; qi < sqlStr.length(); qi++)
						if (sqlStr.charAt(qi) == '?')
							sqlParams.add(null);
	
				try {
					if (query.getQuerySourceType() == 4658) { // externalDB
						organizeQueryFields(scd, query, sqlStr, sqlParams, existField, updateList, insertList);
	
					} else
						getCurrentSession().doWork(new Work() {
	
							public void execute(Connection conn) throws SQLException {
								PreparedStatement stmt = null;
								ResultSet rs = null;
								stmt = conn.prepareStatement(sqlStr);
								if (sqlParams.size() > 0)
									applyParameters(stmt, sqlParams);
								rs = stmt.executeQuery();
								ResultSetMetaData meta = rs.getMetaData();
								Map<String, W5TableField> fieldMap = new HashMap<String, W5TableField>();
								W5Table t = FrameworkCache.getTable(scd, query.getSourceObjectId());
								if (t != null)
									for (W5TableField f : t.get_tableFieldList()) {
										fieldMap.put(f.getDsc().toLowerCase(), f);
									}
	
								int columnNumber = meta.getColumnCount();
								for (int i = 1, j = 0; i <= columnNumber; i++) {
									String columnName = meta.getColumnName(i).toLowerCase(FrameworkSetting.appLocale);
									if (insertFlag != 0 && existField.get(columnName) == null) { // eger
																									// daha
																									// onceden
																									// boyle
																									// tanimlanmis
																									// bir
																									// field
																									// yoksa
										W5QueryFieldCreation field = new W5QueryFieldCreation();
										field.setDsc(columnName);
										field.setCustomizationId((Integer) scd.get("customizationId"));
										if (columnName.equals("insert_user_id") || columnName.equals("version_user_id"))
											field.setPostProcessType((short) 53);
										field.setTabOrder((short) (i));
										field.setQueryId(query.getQueryId());
										field.setFieldType((short) DBUtil.java2iwbType(meta.getColumnType(i)));
										if (field.getFieldType() == 4) {
											// numeric değerde ondalık varsa tipi 3 yap
											int sc = meta.getScale(i);
											if (sc > 0)
												field.setFieldType((short) 3);
										}
										field.setInsertUserId(userId);
										field.setVersionUserId(userId);
										field.setVersionDttm(new java.sql.Timestamp(new java.util.Date().getTime()));
										field.setProjectUuid((String) scd.get("projectId"));
										field.setOprojectUuid((String) scd.get("projectId"));
										if (fieldMap.containsKey(columnName.toLowerCase())) {
											W5TableField tf = fieldMap.get(columnName.toLowerCase());
											field.setMainTableFieldId(tf.getTableFieldId());
											if (tf.getDefaultControlType() == 71) {
												field.setPostProcessType(tf.getDefaultControlType());
											} else if (tf.getDefaultLookupTableId() > 0) {
												switch (tf.getDefaultControlType()) {
												case 6:
													field.setPostProcessType((short) 10);
													break; // combo static
												case 8:
												case 58:
													field.setPostProcessType((short) 11);
													break; // lov-combo static
												case 7:
												case 10:
													field.setPostProcessType((short) 12);
													break; // combo query
												case 15:
												case 59:
													field.setPostProcessType((short) 13);
													break; // lov-combo query
												case 51:
												case 52:
													field.setPostProcessType(tf.getDefaultControlType());
													break; // combo static
												}
												if (tf.getDefaultControlType() != 0)
													field.setLookupQueryId(tf.getDefaultLookupTableId());
											}
										}
										field.setQueryFieldId(GenericUtil.getGlobalNextval("iwb.seq_query_field",
												(String) scd.get("projectId"), (Integer) scd.get("userId"),
												(Integer) scd.get("customizationId")));
										insertList.add(field);
										j++;
									} else if (existField.get(columnName) != null
											&& (existField.get(columnName).getTabOrder() != i
													|| (existField.get(columnName).getMainTableFieldId() == 0
															&& fieldMap.containsKey(columnName.toLowerCase())))) {
										W5QueryFieldCreation field = existField.get(columnName);
										field.setTabOrder((short) (i));
										field.setVersionUserId(userId);
										field.setVersionDttm(new java.sql.Timestamp(new java.util.Date().getTime()));
										if (field.getMainTableFieldId() == 0
												&& fieldMap.containsKey(columnName.toLowerCase())) {
											field.setMainTableFieldId(
													fieldMap.get(columnName.toLowerCase()).getTableFieldId());
										}
										updateList.add(field);
									}
									existField.remove(columnName);
								}
								rs.close();
								stmt.close();
								if (FrameworkSetting.hibernateCloseAfterWork)
									conn.close();
							}
						});
	
					for (W5QueryFieldCreation field : existField.values()) { // icinde bulunmayanlari negatif olarak koy
						field.setTabOrder((short) -Math.abs(field.getTabOrder()));
						field.setPostProcessType((short) 99);
						field.setVersionUserId(userId);
						field.setVersionDttm(new java.sql.Timestamp(new java.util.Date().getTime()));
						updateList.add(field);
					}
				} catch (Exception e) {
					if (FrameworkSetting.debug)
						e.printStackTrace();
					if (queryId != -1)
						throw new IWBException("sql", "QueryField.Creation", queryId, sql.toString(), "Error Creating", e);
				}
			}
		}
		boolean vcs = FrameworkSetting.vcs;
		if (insertFlag != 0 && insertList != null)
			for (W5QueryFieldCreation field : insertList) {
				saveObject(field);
				if (vcs)
					saveObject(new W5VcsObject(scd, 9, field.getQueryFieldId()));
			}
		for (W5QueryFieldCreation field : updateList) {
			updateObject(field);
			if (vcs)
				makeDirtyVcsObject(scd, 9, field.getQueryFieldId());
		}
	}
	


	private void organizeQueryFields4WSMethod(Map<String, Object> scd, final W5Query q, final short insertFlag) {
		final List<W5QueryFieldCreation> updateList = new ArrayList<W5QueryFieldCreation>();
		final List<W5QueryFieldCreation> insertList = new ArrayList<W5QueryFieldCreation>();
		final Map<String, W5QueryFieldCreation> existField = new HashMap<String, W5QueryFieldCreation>();
		final List<Object> sqlParams = new ArrayList();
		String projectId = (String) scd.get("projectId");
		List<W5QueryFieldCreation> existingQueryFields = find(
				"from W5QueryFieldCreation t where t.queryId=?0 AND t.projectUuid=?1", q.getQueryId(), projectId);
		for (W5QueryFieldCreation field : existingQueryFields) {
			existField.put(field.getDsc().toLowerCase(FrameworkSetting.appLocale), field);
		}
		if (q.getSqlSelect().equals("*")) {
			W5WsMethodParam parentParam = (W5WsMethodParam) metadataLoader.getMetadataObject(
					"W5WsMethodParam","outFlag=1 AND t.paramType=10 AND t.wsMethodId",
					q.getSourceObjectId(), projectId, "Parent WSMethodParam");
			List<W5WsMethodParam> outParams = find(
					"from W5WsMethodParam p where p.outFlag=1 AND p.wsMethodId=?0 AND p.parentWsMethodParamId=?1 AND p.projectUuid=?2 order by p.tabOrder",
					q.getSourceObjectId(), parentParam.getWsMethodParamId(), projectId);
			int j = 0;
			for (W5WsMethodParam wsmp : outParams) {
				String columnName = wsmp.getDsc().toLowerCase(FrameworkSetting.appLocale);
				if (insertFlag != 0 && existField.get(columnName) == null) {
					W5QueryFieldCreation field = new W5QueryFieldCreation();
					field.setDsc(columnName);
					field.setTabOrder((short) (j + 1));
					field.setQueryId(q.getQueryId());
					field.setFieldType(wsmp.getParamType());
					field.setInsertUserId((Integer) scd.get("userId"));
					field.setVersionUserId((Integer) scd.get("userId"));
					field.setVersionDttm(new java.sql.Timestamp(new java.util.Date().getTime()));
					field.setProjectUuid((String) scd.get("projectId"));
					field.setOprojectUuid((String) scd.get("projectId"));
					field.setMainTableFieldId(wsmp.getWsMethodParamId());
					field.setQueryFieldId(GenericUtil.getGlobalNextval("iwb.seq_query_field", projectId,
							(Integer) scd.get("userId"), (Integer) scd.get("customizationId")));
					insertList.add(field);
					j++;
				}
				existField.remove(columnName);
			}
		} else {
			String[] lines = q.getSqlSelect().split("\n");
			int j = 0;
			for (String p : lines)
				if (!GenericUtil.isEmpty(p)) {
					String columnName = p.substring(0, p.indexOf(':'));
					if (insertFlag != 0 && existField.get(columnName) == null) {
						W5QueryFieldCreation field = new W5QueryFieldCreation();
						field.setDsc(columnName);
						field.setTabOrder((short) (j + 1));
						field.setQueryId(q.getQueryId());
						field.setFieldType((short) 1);
						field.setInsertUserId((Integer) scd.get("userId"));
						field.setVersionUserId((Integer) scd.get("userId"));
						field.setVersionDttm(new java.sql.Timestamp(new java.util.Date().getTime()));
						field.setProjectUuid(projectId);
						field.setOprojectUuid(projectId);
						field.setMainTableFieldId(0);
						field.setQueryFieldId(GenericUtil.getGlobalNextval("iwb.seq_query_field", projectId,
								(Integer) scd.get("userId"), (Integer) scd.get("customizationId")));
						insertList.add(field);
						j++;
					}
					existField.remove(columnName);
				}
		}
		boolean vcs = FrameworkSetting.vcs;
		if (insertFlag != 0 && insertList != null)
			for (W5QueryFieldCreation field : insertList) {
				saveObject(field);
				if (vcs)
					saveObject(new W5VcsObject(scd, 9, field.getQueryFieldId()));
			}
		for (W5QueryFieldCreation field : updateList) {
			updateObject(field);
			if (vcs)
				makeDirtyVcsObject(scd, 9, field.getQueryFieldId());
		}
	}
	
	public boolean organizeTable(Map<String, Object> scd, String fullTableName) {
		if (FrameworkSetting.vcs && FrameworkSetting.vcsServer)
			return false;
		int customizationId = (Integer) scd.get("customizationId");
		int userId = (Integer) scd.get("userId");
		String projectUuid = (String) scd.get("projectId");
		W5Project prj = FrameworkCache.getProject(projectUuid);
		String schema = prj.getRdbmsSchema();
		executeUpdateSQLQuery("set search_path=" + schema);

		fullTableName = fullTableName.toLowerCase(FrameworkSetting.appLocale);
		String tableName = fullTableName;
		if (tableName.contains(".")) {
			schema = tableName.substring(0, tableName.indexOf('.'));
			tableName = tableName.substring(schema.length() + 1);
		}
		boolean vcs = FrameworkSetting.vcs;

		int cnt = GenericUtil.uInt(executeSQLQuery(
				"select count(1) from information_schema.tables qx where qx.table_name = ? and qx.table_schema = ?",
				tableName, schema).get(0));
		if (cnt == 0)
			throw new IWBException("framework", "No Such Table to Define", 0, tableName, "No Such Table to Define",
					null);

		int tableId = 0;
		List l = executeSQLQuery(
				"select qx.table_id from iwb.w5_table qx where qx.dsc = ? and qx.customization_id = ? AND qx.project_uuid=?",
				fullTableName, customizationId, projectUuid);
		if (!GenericUtil.isEmpty(l)) {
			tableId = GenericUtil.uInt(l.get(0));
		}
		if (tableId == 0) {
			tableId = GenericUtil.getGlobalNextval("iwb.seq_table", projectUuid, userId, customizationId);
			int rq = executeUpdateSQLQuery("insert into iwb.w5_table"
					+ "(table_id, dsc, insert_user_id, version_user_id, customization_id, project_uuid, oproject_uuid, file_attachment_flag, make_comment_flag)values"
					+ "(?       , ?  , ?             , ?              , ?               , ?           , ?            , 0                   , 0)",
					tableId, tableName, userId, userId, customizationId, projectUuid, projectUuid);
			if (vcs)
				saveObject(new W5VcsObject(scd, 15, tableId));

			String firstField = (String) executeSQLQuery(
					"SELECT lower(qz.COLUMN_NAME) from information_schema.columns qz where qz.table_name = ? and qz.table_schema = ? and qz.ordinal_position=1",
					tableName, schema).get(0);

			int tableParamId = GenericUtil.getGlobalNextval("iwb.seq_table_param", projectUuid, userId,
					customizationId);
			rq = executeUpdateSQLQuery("insert into iwb.w5_table_param "
					+ "(table_param_id, table_id, dsc, expression_dsc, tab_order, param_tip, operator_tip, not_null_flag, source_tip, insert_user_id, version_user_id, project_uuid, customization_id, oproject_uuid)values"
					+ "(?             , ?       , ?  , ?             , ?        , ?        , ?           , ?            , ?         , ?             , ?              , ?           , ?               , ?)",
					tableParamId, tableId, "t" + firstField, firstField, 1, 4, 0, 1, 1, userId, userId, projectUuid,
					customizationId, projectUuid);
			if (vcs)
				saveObject(new W5VcsObject(scd, 42, tableParamId));

			cnt = GenericUtil.uInt(executeSQLQuery(
					"SELECT count(1) from information_schema.columns qz where qz.table_name = ? and qz.table_schema = ? and lower(qz.COLUMN_NAME)='customization_id'",
					tableName, schema).get(0));

			if (cnt > 0) {
				tableParamId = GenericUtil.getGlobalNextval("iwb.seq_table_param", projectUuid, userId,
						customizationId);
				rq = executeUpdateSQLQuery("insert into iwb.w5_table_param "
						+ "(table_param_id, table_id, dsc, expression_dsc, tab_order, param_tip, operator_tip, not_null_flag, source_tip, insert_user_id, version_user_id, project_uuid, customization_id, oproject_uuid)values"
						+ "(?             , ?       , ?  , ?             , ?        , ?        , ?           , ?            , ?         , ?             , ?              , ?           , ?               , ?)",
						tableParamId, tableId, "customizationId", "customization_id", 2, 4, 0, 1, 2, userId, userId,
						projectUuid, customizationId, projectUuid);
				if (vcs)
					saveObject(new W5VcsObject(scd, 42, tableParamId));
			}
		}

		List p = new ArrayList();
		p.add(tableId);
		p.add(customizationId);
		p.add(projectUuid);
		p.add(tableName);
		p.add(schema);
		l = executeSQLQuery2Map("select x.*"
				+ ", coalesce((select tf.table_field_id from iwb.w5_table_field tf where tf.table_id = ? and lower(tf.dsc) = x.column_name and tf.customization_id= ? AND tf.project_uuid=?),0) as table_field_id"
				+ ", coalesce((SELECT w.system_type_id FROM iwb.sys_postgre_types w where w.dsc = x.DATA_TYPE),0) xlen"
				+ ", coalesce((SELECT w.framework_type from iwb.sys_postgre_types w where w.dsc = x.DATA_TYPE),0) xtyp"
				+ " from information_schema.columns x where x.table_name = ? and x.table_schema = ?", p);
		for (Map m : (List<Map>) l) {
			int tfId = GenericUtil.uInt(m.get("table_field_id"));
			int xlen = GenericUtil.uInt(m.get("xlen"));
			int xtyp = GenericUtil.uInt(m.get("xtyp"));
			int tabOrder = GenericUtil.uInt(m.get("ordinal_position"));
			if (tfId == 0) {
				String fieldName = ((String) m.get("column_name")).toLowerCase(FrameworkSetting.appLocale);
				int tableFieldId = GenericUtil.getGlobalNextval("iwb.seq_table_field", projectUuid, userId,
						customizationId);
				boolean sessField = fieldName.equals("customization_id") || fieldName.equals("project_uuid")
						|| fieldName.equals("oproject_uuid");
				int rq = executeUpdateSQLQuery("insert into iwb.w5_table_field "
						+ "(table_field_id, table_id, dsc, field_tip, not_null_flag, max_length, tab_order, insert_user_id, version_user_id, customization_id, project_uuid, source_tip, default_value, can_update_flag, can_insert_flag, copy_source_tip, default_control_tip, default_lookup_table_id, oproject_uuid) values"
						+ "(?             , ?       , ?  , ?        , ?            , ?         , ?        , ?             , ?              , ?               , ?           , ?         , ?            , ?              , ?              , ?              , ?                  , ?                      , ?)",
						tableFieldId, tableId, fieldName,
						fieldName.endsWith("_flag") ? 5
								: (xtyp == 3 && GenericUtil.uInt(m.get("numeric_scale")) == 0 ? 4 : xtyp),
						((String) m.get("is_nullable")).equals("YES") ? 0 : 1,
						xlen == 0 ? 0 : (xlen == -1 ? GenericUtil.uInt(m.get("character_maximum_length")) : xlen),
						tabOrder, userId, userId, customizationId, projectUuid, sessField ? 2 : (tabOrder == 1 ? 4 : 1),
						fieldName.endsWith("_flag") ? "0"
								: (fieldName.equals("customization_id") ? "customizationId"
										: (tabOrder == 1 ? "nextval('seq_" + tableName + "')" : null)),
						GenericUtil.hasPartInside2(
								"customization_id,version_no,insert_user_id,insert_dttm,version_user_id,version_dttm",
								fieldName) || tabOrder == 1 ? 0 : 1,
						GenericUtil.hasPartInside2("version_no,insert_user_id,insert_dttm,version_user_id,version_dttm",
								fieldName) ? 0 : 1,
						sessField ? 2 : (tabOrder == 1 ? 4 : 6),
						GenericUtil.hasPartInside2("insert_user_id,version_user_id", fieldName) ? 10 : 0,
						GenericUtil.hasPartInside2("insert_user_id,version_user_id", fieldName) ? 336 : 0, projectUuid);
				if (vcs)
					saveObject(new W5VcsObject(scd, 16, tableFieldId));

			} else {
				int rq = executeUpdateSQLQuery(
						"update iwb.w5_table_field " + " set tab_order       = ?, " + " version_user_id = ?, "
								+ "version_dttm    = LOCALTIMESTAMP, " + " version_no      = version_no+1, "
								+ " not_null_flag =  ?, " + " max_length =  ? "
								+ " where table_field_id =  ? AND  project_uuid=?",
						tabOrder, userId, ((String) m.get("is_nullable")).equals("YES") ? 0 : 1,
						xlen == 0 ? 0 : (xlen == -1 ? GenericUtil.uInt(m.get("character_maximum_length")) : xlen), tfId,
						projectUuid);
				if (vcs)
					makeDirtyVcsObject(scd, 16, tfId);
			}
		}

		int rq = executeUpdateSQLQuery("update iwb.w5_table_field " + "set tab_order       = -abs(tab_order), "
				+ "version_user_id = ?, " + "version_dttm    = LOCALTIMESTAMP, " + "version_no      = version_no+1 "
				+ "where table_id = ?  AND tab_order > 0  AND project_uuid=? "
				+ " AND (lower(dsc) not in (SELECT lower(q.COLUMN_NAME) from information_schema.columns q where q.table_name = ? and q.table_schema = ?))",
				userId, tableId, projectUuid, tableName, schema);

		return true;
	}
	
	public int buildForm(Map<String, Object> scd, String parameter) throws JSONException {
		int customizationId = (Integer) scd.get("customizationId");

		String projectUuid = (String) scd.get("projectId");
		W5Project po = FrameworkCache.getProject(projectUuid);
		int userId = (Integer) scd.get("userId");
		// boolean vcs = FrameworkSetting.vcs && po.getVcsFlag()!=0;
		String createTableSql = "", tableName, fullTableName;
		JSONObject main;
		JSONArray detail;
		int parentTableId;
		boolean vcs = true;
		Locale en = new Locale("en");

		List p = new ArrayList();
		p.add(customizationId);

		JSONObject json = new JSONObject(parameter);
		String webPageId = json.has("_webPageId") ? json.getString("_webPageId") : null;
		int userTip = json.getInt("user_tip");
		main = json.getJSONObject("main");
		detail = json.getJSONArray("detail");

		StringBuilder s = new StringBuilder();
		String schema = po.getRdbmsSchema();
		if (GenericUtil.isEmpty(schema))
			schema = "";
		else
			schema += ".";
		String formName = main.getString("form_name");
		String iconName = main.has("icon") ? main.getString("icon"):null;
		tableName = GenericUtil.uStr2Alpha2(GenericUtil.uStr2English(formName), "x").toLowerCase(en);
		String gridName = GenericUtil.uStr2Alpha2(GenericUtil.uStr2English(main.getString("grid_name")), "x")
				.toLowerCase(en);
		// gridName = main.getString("grid_name");
		String tablePrefix = FrameworkCache.getAppSettingStringValue(0, "form_builder_table_prefix", "x");
		if (!tablePrefix.endsWith("_"))
			tablePrefix += "_";
		String tableName2 = tablePrefix + tableName;
		fullTableName = schema + tableName2;
		parentTableId = main.has("parent_table_id") ? GenericUtil.uInt(main.get("parent_table_id")) : 0;
		s.append("create table ").append(tableName2).append(" (");
		s.append(tableName).append("_id integer not null");
		String relParentFieldName = null;
		if (parentTableId != 0) {
			W5Table pt = FrameworkCache.getTable(scd, parentTableId);
			if (pt != null) {
				relParentFieldName = pt.get_tableFieldList().get(0).getDsc();
				s.append(",\n ").append(relParentFieldName).append(" integer not null");
			} else
				parentTableId = 0;
		}
		for (int qi = 0; qi < detail.length(); qi++) {
			JSONObject d = detail.getJSONObject(qi);
			int controlTip = GenericUtil.uInt(d.get("real_control_tip"));
			if (controlTip == 102)
				continue;
			String fieldDsc = d.getString("real_dsc");
			if (GenericUtil.isEmpty(fieldDsc))
				fieldDsc = d.getString("dsc");
			fieldDsc = fieldDsc.toLowerCase();
			s.append(",\n ").append(fieldDsc);
			int maxLen = GenericUtil.uInt(d.get("max_length"));
			switch (controlTip) {
			case 2:
				if (!fieldDsc.endsWith("_dt")) {
					s.append("_dt");
					fieldDsc = fieldDsc + "_dt";
				}
				s.append(" date");
				break; // date
			case 3:
				s.append(" numeric");
				if (maxLen > 0) {
					if (maxLen > 22)
						maxLen = 22;
					s.append("(").append(maxLen);
					int decimalPrecision = d.has("decimal_precision") ? GenericUtil.uInt(d.get("decimal_precision"))
							: 0;
					if (decimalPrecision > 18)
						decimalPrecision = 18;
					s.append(",").append(decimalPrecision > 0 ? decimalPrecision : 2);
					s.append(")");
				}
				break; // float
			case 4:
				s.append(maxLen < 5 ? " smallint" : " integer");
				break; // integer
			case 5:
				if (!fieldDsc.endsWith("_flag")) {
					s.append("_flag");
					fieldDsc = fieldDsc + "_flag";
				}
				s.append(" smallint default 0");
				break; // checkbox
			case 6:
			case 8:
			case 58:
				if (GenericUtil.uInt(d.get("look_up_id")) > 0) {
					int lookUpId = GenericUtil.uInt(d.get("look_up_id"));
					if (FrameworkCache.getLookUp(scd, lookUpId) == null)
						throw new IWBException("framework", "Form+ Builder", lookUpId, null,
								"Wrong Static LookupID: " + lookUpId, null);
				} else {
					if (!d.has("list_of_values") || GenericUtil.isEmpty(d.get("list_of_values")))
						throw new IWBException("framework", "Form+ Builder", 0, null,
								"LookupID OR Combo Values Not Defined", null);
					String lov = d.getString("list_of_values");
					String[] vz = lov.split("\\r?\\n");

					int lookUpId = GenericUtil.getGlobalNextval("iwb.seq_look_up", projectUuid, userId,
							customizationId);
					executeUpdateSQLQuery("insert into iwb.w5_look_up "
							+ "(look_up_id, customization_id, dsc, version_no, insert_user_id, insert_dttm, version_user_id, version_dttm, project_uuid, oproject_uuid)"
							+ "values (?         , ?               , ?  , 1         , ?             , current_timestamp    , ?              , current_timestamp     , ?, ?)",
							lookUpId, scd.get("customizationId"), "lkp_" + fieldDsc, scd.get("userId"),
							scd.get("userId"), projectUuid, projectUuid);
					if (vcs)
						saveObject(new W5VcsObject(scd, 13, lookUpId));
					int tabOrder = 0;
					for (String sx : vz)
						if (!GenericUtil.isEmpty(sx) && !GenericUtil.isEmpty(sx.trim())) {
							int lookUpIdDetail = GenericUtil.getGlobalNextval("iwb.seq_look_up_detay", projectUuid,
									userId, customizationId);
							executeUpdateSQLQuery("insert into iwb.w5_look_up_detay "
									+ "(look_up_detay_id, look_up_id, tab_order, val      , dsc, version_no, insert_user_id, insert_dttm, version_user_id, version_dttm, customization_id, project_uuid, oproject_uuid)"
									+ "values (?,        ?,                 ?        , ?        , ?  , 1         , ?             , current_timestamp    , ?              , current_timestamp     , ?, ?, ?)",
									lookUpIdDetail, lookUpId, tabOrder, "" + tabOrder, sx.trim(), scd.get("userId"),
									scd.get("userId"), scd.get("customizationId"), projectUuid, projectUuid);
							tabOrder++;
							if (vcs)
								saveObject(new W5VcsObject(scd, 14, lookUpIdDetail));
						}
					d.put("look_up_id", "" + lookUpId);
				}
				s.append(controlTip == 6 || controlTip == 7 || controlTip == 10 || controlTip == 51 ? " integer"
						: " character varying(256)");
				break;
			case 7:// query
			case 10:// autocomplete
			case 15:// multi selection
			case 59:
				if (GenericUtil.uInt(d.get("look_up_id")) > 0) {
					int queryId = GenericUtil.uInt(d.get("look_up_id"));
					if (executeSQLQuery("select 1 from iwb.w5_query x where x.query_id=? AND x.query_tip=3",
							queryId) == null)
						throw new IWBException("framework", "Form+ Builder", queryId, null, "Wrong QueryID: " + queryId,
								null);
				} else
					throw new IWBException("framework", "Form+ Builder", 0, null, "QueryID not defined", null);
				s.append(controlTip == 6 || controlTip == 7 || controlTip == 10 || controlTip == 51 ? " integer"
						: " character varying(256)");
				break;
			case 71:// file attachment
				s.append(" integer");
				break;

			default:
				// case 1:case 11:case 12:
				if (maxLen == 0 || maxLen > 3999)
					s.append(" text");
				else
					s.append(" character varying(").append(maxLen < 1024 ? 1024 : maxLen).append(")");
				break; // string, textarea, htmleditor
			}

			Object notNull = d.get("not_null_flag");
			if (notNull != null) { // sonra degistirmek isteyebilir, o yzden koyma
				if (notNull instanceof Boolean) {
					if ((Boolean) notNull)
						s.append(" not null");
				} else if (GenericUtil.uInt(notNull) != 0)
					s.append(" not null");
			}

			d.put("real_dsc", fieldDsc);
		}
		s.append(",\n version_no integer NOT NULL DEFAULT 1").append(",\n insert_user_id integer NOT NULL DEFAULT 1")
				.append(",\n  insert_dttm timestamp without time zone NOT NULL DEFAULT ('now'::text)::timestamp without time zone")
				.append(",\n version_user_id integer NOT NULL DEFAULT 1,\n version_dttm timestamp without time zone NOT NULL DEFAULT ('now'::text)::timestamp without time zone");

		s.append(",\n CONSTRAINT pk_").append(tableName).append(" PRIMARY KEY (").append(tableName).append("_id)");
		s.append(")");

		createTableSql = s.toString();
		executeUpdateSQLQuery("set search_path=" + po.getRdbmsSchema());

		Map msg = null, nt = null;
		if (webPageId != null) {
			msg = new HashMap();
			msg.put("success", true);
			nt = new HashMap();
			msg.put("notification", nt);
		}

		try {
			executeUpdateSQLQuery(createTableSql);
			String createSeqSql = "create sequence seq_" + tablePrefix + tableName;
			executeUpdateSQLQuery(createSeqSql);

			if (vcs) {
				W5VcsCommit commit = new W5VcsCommit();
				commit.setCommitTip((short) 2);
				commit.setExtraSql(createTableSql + ";\n\n" + createSeqSql + ";");
				commit.setProjectUuid(projectUuid);
				commit.setComment("iWB. AutoCreate Scripts for Table: " + fullTableName);
				commit.setCommitUserId((Integer) scd.get("userId"));
				Object oi = executeSQLQuery("select nextval('iwb.seq_vcs_commit')").get(0);
				commit.setVcsCommitId(-GenericUtil.uInt(oi));
				saveObject(commit);
			}

			s.setLength(0);

			if (webPageId != null) {
				nt.put("_tmpStr", "Table Created on RDBMS");
				UserUtil.broadCast(projectUuid, (Integer) scd.get("userId"), (String) scd.get("sessionId"), webPageId,
						msg);
			}

		} catch (Exception e2) {
			throw new IWBException("framework", "Create Table&Seq", 0, createTableSql, e2.getMessage(), e2);
		}

		boolean b = organizeTable(scd, fullTableName);
		if (!b)
			throw new IWBException("framework", "Define Table", 0, parameter, "Define Table", null);
		if (webPageId != null) {
			nt.put("_tmpStr", "Table Imported to Code2");
			UserUtil.broadCast(projectUuid, (Integer) scd.get("userId"), (String) scd.get("sessionId"), webPageId, msg);
		}

		int tableId = GenericUtil.uInt(executeSQLQuery(
				"select t.table_id from iwb.w5_table t where t.customization_id=? AND t.dsc=? AND t.project_uuid=?",
				customizationId, tableName2, projectUuid).get(0));

		try {
			main.put("table_id", tableId);
		} catch (JSONException e) {
		}
		// executeUpdateSQLQuery("supdate iwb.w5_table t set where
		// t.customization_id=? AND
		// t.table_id=?", customizationId, tableId);
		main.put("form_name", tableName);

		W5FormResult fr = crudEngine.postFormAsJson(scd, 181, 2, main, 182, detail); //TODO. not from here
		if (!fr.getErrorMap().isEmpty())
			throw new IWBException("framework", "Save FormBuilder Data", 0, parameter,
					GenericUtil.fromMapToJsonString(fr.getErrorMap()), null);

		int xformBuilderId = GenericUtil.uInt(fr.getOutputFields().get("xform_builder_id").toString());
		int parentTemplateId = parentTableId == 0 || !main.has("template_id") ? 0 : main.getInt("template_id");
		int parentTemplateObjectId = parentTableId == 0 || !main.has("parent_object_id") ? 0
				: main.getInt("parent_object_id");
		int formId = GenericUtil.getGlobalNextval("iwb.seq_form", projectUuid, userId, customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
		// nextval('seq_form')").get(0));
		// XFORM_ID := nextval('seq_form');
		executeUpdateSQLQuery("INSERT INTO iwb.w5_form("
				+ "form_id, customization_id, object_tip, object_id, dsc, locale_msg_key, "
				+ "default_width, default_height, tab_order, render_tip, code, label_width,"
				+ "label_align_tip,  cont_entry_flag, " + "version_no, insert_user_id, insert_dttm, version_user_id,"
				+ "version_dttm, render_template_id, project_uuid, oproject_uuid)"
				+ "\nselect ?, XFORM_BUILDER.customization_id, 2, XFORM_BUILDER.table_id, 'frm_'||XFORM_BUILDER.form_name, ? ,"
				+ "400, 300, 1, 1, null, XFORM_BUILDER.label_width," + "XFORM_BUILDER.label_align, 0,"
				+ "1, ?, current_timestamp, ?,"
				+ "current_timestamp, 0, XFORM_BUILDER.project_uuid,XFORM_BUILDER.project_uuid from iwb.w5_xform_builder XFORM_BUILDER where XFORM_BUILDER.xform_builder_id=? AND XFORM_BUILDER.customization_id=?",
				formId, formName, userId, userId, xformBuilderId, customizationId);
		if (vcs)
			saveObject(new W5VcsObject(scd, 40, formId));

		List lp = new ArrayList();
		lp.add(xformBuilderId);
		List<Map> lm = executeSQLQuery2Map(
				"select x.* from iwb.w5_xform_builder_detail x where x.xform_builder_id=? order by 1", lp);
		int tabOrder = 1;
		for (Map m : lm) {
			int formCellId = GenericUtil.getGlobalNextval("iwb.seq_form_cell", projectUuid, userId, customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
			// nextval('seq_form_cell')").get(0));
			executeUpdateSQLQuery("INSERT INTO iwb.w5_form_cell("
					+ "form_cell_id, customization_id, form_id, dsc, locale_msg_key,"
					+ "control_tip, vtype, source_tip, not_null_flag, tab_order, control_width,"
					+ "max_length, nrd_tip, lookup_query_id, lookup_included_params,"
					+ "lookup_included_values, default_value, initial_value, initial_source_tip,"
					+ "extra_definition, object_id, object_detail_id, version_no, insert_user_id,"
					+ "insert_dttm, version_user_id, version_dttm, form_module_id, out_flag,"
					+ "dialog_grid_id, x_order, parent_form_cell_id, active_flag, lookup_edit_form_id,"
					+ "project_uuid, oproject_uuid)"
					+ "\nselect  ?, x.customization_id, ?, coalesce(x.real_dsc, x.dsc), x.label,"
					+ "case when x.real_control_tip!=0 then x.real_control_tip else  x.control_tip end, null, 1, x.not_null_flag, x.tab_order, x.width,"
					+ " 0, 0, x.look_up_id, null, " + " null, null, x.initial_value, 0,"
					+ " null, ?, (select f.table_field_id from iwb.w5_table_field f where f.customization_id=x.customization_id AND f.table_id=? AND f.dsc=coalesce(x.real_dsc, x.dsc)), 1, ?,"
					+ " current_timestamp, ?, current_timestamp, 0, 0," + " 0, 1, 0, 1, 0,"
					+ " x.project_uuid,x.project_uuid from iwb.w5_xform_builder_detail x where x.xform_builder_detail_id=? AND x.customization_id=?",
					formCellId, formId, tableId, tableId, userId, userId,
					GenericUtil.uInt(m.get("xform_builder_detail_id")), customizationId);
			if (vcs)
				saveObject(new W5VcsObject(scd, 41, formCellId));
		}
		if (relParentFieldName != null) {
			int formCellId = GenericUtil.getGlobalNextval("iwb.seq_form_cell", projectUuid, userId, customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
			// nextval('seq_form_cell')").get(0));
			executeUpdateSQLQuery("INSERT INTO iwb.w5_form_cell("
					+ "form_cell_id, customization_id, form_id, dsc, locale_msg_key,"
					+ "control_tip, vtype, source_tip, not_null_flag, tab_order, control_width,"
					+ "max_length, nrd_tip, lookup_query_id, lookup_included_params,"
					+ "lookup_included_values, default_value, initial_value, initial_source_tip,"
					+ "extra_definition, object_id, object_detail_id, version_no, insert_user_id,"
					+ "insert_dttm, version_user_id, version_dttm, form_module_id, out_flag,"
					+ "dialog_grid_id, x_order, parent_form_cell_id, active_flag, lookup_edit_form_id,"
					+ "project_uuid, oproject_uuid)" + "\nvalues(?, ?, ?, ?, ? ," + "0, null, 1, 1, 10*?, 100,"
					+ " 0, 0, 0, null, " + " null, null, null, 0,"
					+ " null, ?, (select f.table_field_id from iwb.w5_table_field f where f.customization_id=? AND f.table_id=? AND f.dsc=?), 1, ?,"
					+ " current_timestamp, ?, current_timestamp, 0, 0," + " 0, 1, 0, 1, 0," + " ?, ? )", formCellId,
					customizationId, formId, relParentFieldName, relParentFieldName, tabOrder++, tableId,
					customizationId, tableId, relParentFieldName, userId, userId, projectUuid, projectUuid);
			if (vcs)
				saveObject(new W5VcsObject(scd, 41, formCellId));
		}
		if (webPageId != null) {
			nt.put("_tmpStr", "Form Created for CRUD Operations");
			UserUtil.broadCast(projectUuid, (Integer) scd.get("userId"), (String) scd.get("sessionId"), webPageId, msg);
		}

		// XQUERY_ID := nextval('seq_query');
		int queryId = GenericUtil.getGlobalNextval("iwb.seq_query", projectUuid, userId, customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
		// nextval('seq_query')").get(0));
		executeUpdateSQLQuery("INSERT INTO iwb.w5_query("
				+ "query_id, dsc, main_table_id, sql_select, sql_from, sql_where,"
				+ "sql_groupby, sql_orderby, query_tip, log_level_tip, version_no,"
				+ "insert_user_id, insert_dttm, version_user_id, version_dttm,"
				+ "show_parent_record_flag, "
				+ "data_fill_direction_tip, opt_query_field_ids, opt_tip, project_uuid,oproject_uuid, customization_id)"
				+ "select ?, 'qry_'||XFORM_BUILDER.form_name||'1', XFORM_BUILDER.table_id, 'x.*', (select t.dsc from iwb.w5_table t where t.table_id=XFORM_BUILDER.table_id AND t.customization_id=?)||' x', null,"
				+ "null, 1, 1, 1, 1," + "?, current_timestamp, ?, current_timestamp, 0,"
				+ "0, null, 0, XFORM_BUILDER.project_uuid,XFORM_BUILDER.project_uuid, XFORM_BUILDER.customization_id from iwb.w5_xform_builder XFORM_BUILDER where XFORM_BUILDER.xform_builder_id=?",
				queryId, customizationId, userId, userId, xformBuilderId);
		if (vcs)
			saveObject(new W5VcsObject(scd, 8, queryId));

		organizeQueryFields(scd, queryId, (short) 1);

		if (webPageId != null) {
			nt.put("_tmpStr", "Query Created");
			UserUtil.broadCast(projectUuid, (Integer) scd.get("userId"), (String) scd.get("sessionId"), webPageId, msg);
		}

		executeUpdateSQLQuery("set search_path=iwb");

		List llo = find("from W5QueryFieldCreation t where queryFieldId<?0", 500);

		int gridId = GenericUtil.getGlobalNextval("iwb.seq_grid", projectUuid, userId, customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
		// nextval('seq_grid')").get(0));
		executeUpdateSQLQuery("INSERT INTO iwb.w5_grid("
				+ "grid_id, customization_id, dsc, query_id, locale_msg_key, grid_tip,"
				+ "default_page_record_number, selection_mode_tip, pk_query_field_id," + "auto_expand_field_id, "
				+ "default_width, default_height, version_no, insert_user_id, insert_dttm,"
				+ "version_user_id, version_dttm, default_sql_order_by, default_crud_form_id,"
				+ "column_render_tip, grouping_field_id, " + "insert_edit_mode_flag, move_up_down_flag,"
				+ "tree_master_field_id, summary_tip, row_color_fx_tip, row_color_fx_query_field_id,"
				+ "row_color_fx_render_tip, row_color_fx_render_field_ids, code, project_uuid, oproject_uuid)"
				+ "select ?, XFORM_BUILDER.customization_id, ? , ?, XFORM_BUILDER.grid_name, 0,"
				+ "?, 1, (select min(qf.query_field_id) from iwb.w5_query_field qf where qf.query_id=?)," + "0, "
				+ "400, 300, 1, ?, current_timestamp," + "?, current_timestamp, null, ?," + "0, 0, " + "0, 0,"
				+ "0, 0, 0, 0," + "0, null, null, XFORM_BUILDER.project_uuid,XFORM_BUILDER.project_uuid "
				+ " from iwb.w5_xform_builder XFORM_BUILDER where XFORM_BUILDER.xform_builder_id=? AND XFORM_BUILDER.customization_id=?",
				gridId, "grd_" + gridName, queryId, parentTableId == 0 ? 20 : 0, queryId, userId, userId, formId,
				xformBuilderId, customizationId);
		if (vcs)
			saveObject(new W5VcsObject(scd, 5, gridId));

		tabOrder = 1;
		for (Map m : lm) {
			int gridColumnId = GenericUtil.getGlobalNextval("iwb.seq_grid_column", projectUuid, userId,
					customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
			// nextval('seq_grid_column')").get(0));
			executeUpdateSQLQuery("INSERT INTO iwb.w5_grid_column("
					+ "grid_column_id, query_field_id, grid_id, customization_id, locale_msg_key, tab_order,"
					+ "visible_flag, sortable_flag, width, renderer, align_tip, version_no,"
					+ "insert_user_id, insert_dttm, version_user_id, version_dttm, extra_definition,"
					+ "grid_module_id, form_cell_id, filter_flag, project_uuid, oproject_uuid)"
					+ "\nselect ?, (select f.query_field_id from iwb.w5_query_field f where f.dsc=coalesce(x.real_dsc, x.dsc) AND f.query_id=?), ?, x.customization_id, coalesce(x.grd_label, x.label), 10*?,"
					+ "x.grd_visible_flag, 1, x.grd_width, null, x.grd_align_tip, 1,"
					+ "?, current_timestamp, ?, current_timestamp, null,"
					+ "0, case when y.grid_edit=1 AND x.grd_editable_flag=1 then (select c.form_cell_id from iwb.w5_form_cell c where c.dsc=coalesce(x.real_dsc, x.dsc) AND c.form_id=? AND c.customization_id=x.customization_id) else 0 end, 0, x.project_uuid, x.project_uuid"
					+ " from iwb.w5_xform_builder_detail x,iwb.w5_xform_builder y "
					+ "where x.xform_builder_id = y.xform_builder_id AND x.customization_id=y.customization_id "
					+ "AND x.xform_builder_detail_id=?  AND x.customization_id=?", gridColumnId, queryId, gridId,
					tabOrder++, userId, userId, formId, GenericUtil.uInt(m.get("xform_builder_detail_id")),
					customizationId);
			if (vcs)
				saveObject(new W5VcsObject(scd, 4, gridColumnId));
		}

		if (webPageId != null) {
			nt.put("_tmpStr", "Grid Created");
			UserUtil.broadCast(projectUuid, (Integer) scd.get("userId"), (String) scd.get("sessionId"), webPageId, msg);
		}

		// if(pmaster_flag=1 AND XFORM_BUILDER.grid_search=1 AND (select
		// count(1) from
		// iwb.w5_xform_builder_detail x where
		// x.xform_builder_id=pxform_builder_id AND
		// x.customization_id=XUSER_ROLE.customization_id AND
		// x.project_uuid=pproject_uuid AND
		// x.grd_search_flag=1)>0) then
		if (parentTableId == 0) {
			tabOrder = 1;
			for (Map m : lm)
				if (GenericUtil.uInt(m.get("grd_search_flag")) != 0) {
					int queryParamId = GenericUtil.getGlobalNextval("iwb.seq_query_param", projectUuid, userId,
							customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
					// nextval('seq_query_param')").get(0));
					executeUpdateSQLQuery("INSERT INTO iwb.w5_query_param("
							+ "query_param_id, query_id, dsc, param_tip, expression_dsc, operator_tip,"
							+ "not_null_flag, tab_order, source_tip, default_value, min_length,"
							+ "max_length, version_no, insert_user_id, insert_dttm, version_user_id,"
							+ "version_dttm, related_table_field_id, min_value, max_value, project_uuid, oproject_uuid, customization_id)"
							+ "SELECT  ?, ?, 'x'||coalesce(x.real_dsc, x.dsc), case when x.control_tip in (1,2,3,4) then x.control_tip else 1 end, 'x.'||coalesce(x.real_dsc, x.dsc), 0,"
							+ "0, 10*?, 1, null, 0," + "0, 1, ?, current_timestamp, ?,"
							+ "current_timestamp, (select f.table_field_id from iwb.w5_table_field f where f.dsc=coalesce(x.real_dsc, x.dsc) AND f.table_id=? AND f.customization_id=x.customization_id), null, null, x.project_uuid, x.project_uuid, x.customization_id "
							+ "from iwb.w5_xform_builder_detail x where "
							+ "x.xform_builder_detail_id=? AND x.customization_id=?", queryParamId, queryId, tabOrder++,
							userId, userId, tableId, GenericUtil.uInt(m.get("xform_builder_detail_id")),
							customizationId);
					if (vcs)
						saveObject(new W5VcsObject(scd, 10, queryParamId));
				}

			// XSFORM_ID := nextval('seq_form');
			int sformId = GenericUtil.getGlobalNextval("iwb.seq_form", projectUuid, userId, customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
			// nextval('seq_form')").get(0));
			executeUpdateSQLQuery("INSERT INTO iwb.w5_form("
					+ "form_id, customization_id, object_tip, object_id, dsc, locale_msg_key, "
					+ "default_width, default_height, tab_order, render_tip, code, label_width,"
					+ "label_align_tip, cont_entry_flag, " + "version_no, insert_user_id, insert_dttm, version_user_id,"
					+ "version_dttm, render_template_id, project_uuid, oproject_uuid)"
					+ "\nselect ?, XFORM_BUILDER.customization_id, 1, ?, 'sfrm_'||XFORM_BUILDER.form_name, 'search_criteria',"
					+ "400, 300, 1, 1, null, XFORM_BUILDER.label_width," + "XFORM_BUILDER.label_align, 0,"
					+ "1, ?, current_timestamp, ?, current_timestamp, 0, XFORM_BUILDER.project_uuid, XFORM_BUILDER.project_uuid from iwb.w5_xform_builder XFORM_BUILDER where XFORM_BUILDER.xform_builder_id=? AND XFORM_BUILDER.customization_id=?",
					sformId, gridId, userId, userId, xformBuilderId, customizationId);
			if (vcs)
				saveObject(new W5VcsObject(scd, 40, sformId));

			for (Map m : lm)
				if (GenericUtil.uInt(m.get("grd_search_flag")) != 0) {
					int formCellId = GenericUtil.getGlobalNextval("iwb.seq_form_cell", projectUuid, userId,
							customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
					// nextval('seq_form_cell')").get(0));
					int controlTip = GenericUtil.uInt(m.get("real_control_tip"));
					if (controlTip == 0)
						controlTip = GenericUtil.uInt(m.get("control_tip"));
					int lookUpId = GenericUtil.uInt(m.get("look_up_id"));
					switch (controlTip) {
					case 5:
						controlTip = 6;
						lookUpId = 143;
						break;
					case 11:
					case 12:
						controlTip = 1;
						break;
					}
					executeUpdateSQLQuery("INSERT INTO iwb.w5_form_cell("
							+ "form_cell_id, customization_id, form_id, dsc, locale_msg_key,"
							+ "control_tip, vtype, source_tip, not_null_flag, tab_order, control_width,"
							+ "max_length, nrd_tip, lookup_query_id, lookup_included_params,"
							+ "lookup_included_values, default_value, initial_value, initial_source_tip,"
							+ "extra_definition, object_id, object_detail_id, version_no, insert_user_id,"
							+ "insert_dttm, version_user_id, version_dttm, form_module_id, out_flag,"
							+ "dialog_grid_id, x_order, parent_form_cell_id, active_flag, lookup_edit_form_id, project_uuid, oproject_uuid)"
							+ "\nselect  ?, x.customization_id, ?, 'x'||coalesce(x.real_dsc, x.dsc), x.label,"
							+ "?, null, 1, 0, 10*?, 200," + "0, 0, ?, null," + "null, null, null, 0,"
							+ "null, ?, (select f.query_param_id from iwb.w5_query_param f where f.query_id=? AND f.dsc='x'||coalesce(x.real_dsc, x.dsc)), 1, ?,"
							+ "current_timestamp, ?, current_timestamp, 0, 0,"
							+ "0, 1, 0, 1, 0, x.project_uuid, x.project_uuid "
							+ "from iwb.w5_xform_builder_detail x where x.grd_search_flag=1 AND x.xform_builder_detail_id=? AND x.customization_id=?",
							formCellId, sformId, controlTip, tabOrder++, lookUpId, gridId, queryId, userId, userId,
							GenericUtil.uInt(m.get("xform_builder_detail_id")), customizationId);
					if (vcs)
						saveObject(new W5VcsObject(scd, 41, formCellId));
				}

			if (webPageId != null) {
				nt.put("_tmpStr", "Form Created for Grid Search");
				UserUtil.broadCast(projectUuid, (Integer) scd.get("userId"), (String) scd.get("sessionId"), webPageId,
						msg);
			}
		}

		// if(pmaster_flag=1)then
		int pageId = 0, menuId = 0;
		if (parentTableId == 0) {
			// XTEMPLATE_ID := nextval('seq_template');
			pageId = GenericUtil.getGlobalNextval("iwb.seq_template", projectUuid, userId, customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
			// nextval('seq_template')").get(0));
			executeUpdateSQLQuery(
					"INSERT INTO iwb.w5_template(" + "template_id, customization_id, template_tip, dsc, object_id,"
							+ "object_tip, code, version_no, insert_user_id, insert_dttm, version_user_id,"
							+ "version_dttm, locale_msg_flag, project_uuid, oproject_uuid)"
							+ "VALUES (?, ?, 2, 'pg_'||?||'1', 0, " + "0, null, 1, ?, current_timestamp, ?,"
							+ "current_timestamp, 1, ?, ?)",
					pageId, customizationId, tableName, userId, userId, projectUuid, projectUuid);
			if (vcs)
				saveObject(new W5VcsObject(scd, 63, pageId));

			int templateObjectId = GenericUtil.getGlobalNextval("iwb.seq_template_object", projectUuid, userId,
					customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
			// nextval('seq_template_object')").get(0));
			executeUpdateSQLQuery("INSERT INTO iwb.w5_template_object("
					+ "template_object_id, template_id, customization_id, object_id, tab_order, object_tip,"
					+ "version_no, insert_user_id, insert_dttm, version_user_id, version_dttm,"
					+ "access_view_users, access_view_roles, access_view_tip, post_js_code,"
					+ "parent_object_id, src_query_field_id, dst_query_param_id,"
					+ "dst_static_query_param_val, dst_static_query_param_id, active_flag, project_uuid, oproject_uuid)"
					+ "VALUES (?, ?, ?, ?, 1, 1," + "1, ?, current_timestamp, ?, current_timestamp,"
					+ "null, null, 0, null," + "0, null, null,null, null, 1, ?, ?)", templateObjectId, pageId,
					customizationId, gridId, userId, userId, projectUuid, projectUuid);
			if (vcs)
				saveObject(new W5VcsObject(scd, 64, templateObjectId));

			menuId = GenericUtil.getGlobalNextval("iwb.seq_menu", projectUuid, userId, customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
			// nextval('seq_template_object')").get(0));
			executeUpdateSQLQuery("INSERT INTO iwb.w5_menu("
					+ "menu_id, parent_menu_id, user_tip, node_tip, locale_msg_key,"
					+ "tab_order, img_icon, url, version_no, insert_user_id, insert_dttm,"
					+ "version_user_id, version_dttm, customization_id, access_view_tip, project_uuid, oproject_uuid)"
					+ "VALUES (?, 0, ?, 4, ?, "
					+ "coalesce((select max(q.tab_order) from iwb.w5_menu q where q.customization_id=? AND q.user_tip=?),0)+10, ?, 'showPage?_tid='||?::text, 1, ?, current_timestamp, "
					+ "?, current_timestamp, ?, 0, ?, ?)", menuId, userTip, gridName, customizationId, userTip, iconName,
					pageId, userId, userId, customizationId, projectUuid, projectUuid);
			if (vcs)
				saveObject(new W5VcsObject(scd, 65, menuId));
		} else {
			Object[] loo = (Object[]) executeSQLQuery(
					"select f.dsc, f.table_field_id "
							+ "from iwb.w5_table_field f where f.customization_id=? AND f.table_id=? AND f.tab_order=2",
					customizationId, tableId).get(0);
			executeUpdateSQLQuery(
					"UPDATE iwb.w5_table_field f SET can_update_flag=0 WHERE f.customization_id=? AND f.table_id=? AND f.tab_order=2",
					customizationId, tableId);
			executeUpdateSQLQuery(
					"UPDATE iwb.w5_grid f SET code=f.dsc||'._postInsert=function(sel,url,a){var m=getMasterGridSel(a,sel);if(m)return url+\"&"
							+ loo[0] + "=\" +(m." + loo[0] + " || m.get(\"" + loo[0]
							+ "\"));};' WHERE f.customization_id=? AND f.grid_id=?",
					customizationId, gridId);
			int tableChildId = GenericUtil.getGlobalNextval("iwb.seq_table_relation", projectUuid, userId,
					customizationId);
			executeUpdateSQLQuery("insert INTO iwb.w5_table_child "
					+ "(table_child_id, locale_msg_key, relation_tip, table_id, table_field_id, related_table_id, related_table_field_id, related_static_table_field_id, related_static_table_field_val, version_no, insert_user_id, insert_dttm, version_user_id, version_dttm, copy_strategy_tip, on_readonly_related_action, on_invisible_related_action, on_delete_action, tab_order, on_delete_action_value, child_view_tip, child_view_object_id, revision_flag, project_uuid, customization_id) "
					+ "values(?, ?, 2, ?     , ?             , ?               , ?                     , 0                            , 0                             , 1         , ?             , current_timestamp  , ?      , current_timestamp , 0          , 0                         , 0                          , 0               , 10       , null                  , 0             , 0                   , 0            , ?           , ?)",
					tableChildId, "rel_xxx2" + tableName, parentTableId,
					FrameworkCache.getTable(scd, parentTableId).get_tableFieldList().get(0).getTableFieldId(), tableId,
					loo[1], userId, userId, projectUuid, customizationId);
			if (vcs)
				saveObject(new W5VcsObject(scd, 657, tableChildId));

			int queryParamId = GenericUtil.getGlobalNextval("iwb.seq_query_param", projectUuid, userId,
					customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
			// nextval('seq_query_param')").get(0));
			executeUpdateSQLQuery("INSERT INTO iwb.w5_query_param("
					+ "query_param_id, query_id, dsc, param_tip, expression_dsc, operator_tip,"
					+ "not_null_flag, tab_order, source_tip, default_value, min_length,"
					+ "max_length, version_no, insert_user_id, insert_dttm, version_user_id,"
					+ "version_dttm, related_table_field_id, min_value, max_value, project_uuid, oproject_uuid, customization_id)"
					+ "values (" + "?, ?, 'x'||?, 4, 'x.'||?, 0, " + "1, 1, 1, null, 0,"
					+ "0, 1, ?, current_timestamp, ?," + "current_timestamp, ?, null, null, ?, ?, ?)", queryParamId,
					queryId, loo[0], loo[0], userId, userId, GenericUtil.uInt(loo[1]), projectUuid, projectUuid,
					customizationId);
			if (vcs)
				saveObject(new W5VcsObject(scd, 10, queryParamId));

			int parentQueryId = GenericUtil.uInt(executeSQLQuery(
					"select g.query_id from iwb.w5_template_object q, iwb.w5_grid g where q.template_object_id=? AND q.customization_id=? "
							+ " AND g.customization_id=q.customization_id AND q.object_id=g.grid_id",
					parentTemplateObjectId, customizationId).get(0));

			int templateObjectId = GenericUtil.getGlobalNextval("iwb.seq_template_object", projectUuid, userId,
					customizationId); // 1000000+GenericUtil.uInt(executeSQLQuery("select
			// nextval('seq_template_object')").get(0));
			executeUpdateSQLQuery("INSERT INTO iwb.w5_template_object("
					+ "template_object_id, template_id, customization_id, object_id, tab_order, object_tip,"
					+ "version_no, insert_user_id, insert_dttm, version_user_id, version_dttm,"
					+ "access_view_users, access_view_roles, access_view_tip, post_js_code,"
					+ "parent_object_id, src_query_field_id, dst_query_param_id,"
					+ "dst_static_query_param_val, dst_static_query_param_id, active_flag, project_uuid, oproject_uuid)"
					+ "VALUES ("
					+ "?, ?, ?, ?, (select coalesce(max(q.tab_order),0)+1 from iwb.w5_template_object q where q.template_id=? AND q.customization_id=? ), 1,"
					+ "1, ?, current_timestamp, ?, current_timestamp," + "null, null, 0, null,"
					+ "?, (select min(r.query_field_id) from iwb.w5_query_field r where r.query_id=? AND  "
					+ "r.tab_order=(select min(f.tab_order) from iwb.w5_query_field f where f.query_id=?)), ?,"
					+ "null, null, 1, ?, ?)", templateObjectId, parentTemplateId, customizationId, gridId,
					parentTemplateId, customizationId, userId, userId, parentTemplateObjectId, parentQueryId,
					parentQueryId, queryParamId, projectUuid, projectUuid);
			if (vcs)
				saveObject(new W5VcsObject(scd, 64, templateObjectId));

			if (webPageId != null) {
				nt.put("_tmpStr", "Page & Menu Created");
				UserUtil.broadCast(projectUuid, (Integer) scd.get("userId"), (String) scd.get("sessionId"), webPageId,
						msg);
			}
		}
		executeUpdateSQLQuery("update iwb.w5_table t "
				+ "set default_insert_form_id=?, default_update_form_id=?, default_view_grid_id=?, summary_record_sql='x.'||(select tf.dsc from iwb.w5_table_field tf where tf.tab_order=2 AND tf.table_id=t.table_id AND t.customization_id=tf.customization_id)||'::text' "
				+ "where t.table_id=? AND t.customization_id=? ", formId, formId, gridId, tableId, customizationId);

		executeUpdateSQLQuery("update iwb.w5_table_field tf "
				+ "set default_control_tip=(select fc.control_tip from iwb.w5_form_cell fc, iwb.w5_form f where fc.customization_id=f.customization_id AND f.form_id=fc.form_id AND f.object_tip=2 AND f.object_id=tf.table_id AND fc.dsc=tf.dsc), default_lookup_table_id=(select fc.lookup_query_id from iwb.w5_form_cell fc, iwb.w5_form f where fc.customization_id=f.customization_id AND f.form_id=fc.form_id AND f.object_tip=2 AND f.object_id=tf.table_id AND fc.dsc=tf.dsc) "
				+ "where tf.table_id=? AND tf.customization_id=? AND tf.tab_order>1 AND tf.dsc not in ('version_no','insert_user_id','insert_dttm','version_user_id','version_dttm') ",
				tableId, customizationId);

		executeUpdateSQLQuery("update iwb.w5_query_field tf "
				+ "set post_process_tip=(select case when f.default_control_tip in (6) then 10 when f.default_control_tip in (8) then 11 else 0 end from w5_table_field f where tf.customization_id=f.customization_id AND f.table_id=? AND f.dsc=tf.dsc)"
				+ ", lookup_query_id=(select case when f.default_control_tip in (6,8) then f.default_lookup_table_id else 0 end from w5_table_field f where tf.customization_id=f.customization_id AND f.table_id=? AND f.dsc=tf.dsc)  "
				+ ", main_table_field_id=(select f.table_field_id from w5_table_field f where tf.customization_id=f.customization_id AND f.table_id=? AND f.dsc=tf.dsc)  "
				+ "where tf.query_id=? AND tf.customization_id=?", tableId, tableId, tableId, queryId, customizationId);

		if (parentTableId == 0) { // main Template
			return pageId;
		} else {
			return gridId;
		}
	}
	
	public void beforePostForm(W5FormResult formResult, PostgreSQL dao, String prefix){
		if(prefix==null)prefix="";
		if(formResult.getErrorMap()!=null && formResult.getErrorMap().isEmpty())switch(formResult.getFormId()){
		case	2491://SQL Script
			String sql = formResult.getRequestParams().get("extra_sql");
			if(FrameworkSetting.cloud && !GenericUtil.isEmpty(sql) && ((Integer)formResult.getScd().get("customizationId")==0 || (Integer)formResult.getScd().get("customizationId")==1095) && DBUtil.checkTenantSQLSecurity(sql)) {
				throw new IWBException("security","SQL", 0, null, "Suspicious Command! Download the platform and feel free to run all the commands ;)", null);
			}
			if(GenericUtil.uCheckBox(formResult.getRequestParams().get("orun_local_flag"))!=0){
				W5Project prj = FrameworkCache.getProject(formResult.getScd());
				executeUpdateSQLQuery("set search_path="+prj.getRdbmsSchema());
				executeUpdateSQLQuery(sql);
			}
			break;
		}
		if(formResult.getScd()==null)return;
		W5Project prj = FrameworkCache.getProject(formResult.getScd());
		int customizationId = (Integer)formResult.getScd().get("customizationId");
		if(formResult.getErrorMap()!=null && formResult.getErrorMap().isEmpty() && formResult.getForm()!=null)switch(formResult.getForm().getObjectId()){
		case	15://table
			if(formResult.getAction()==1 || formResult.getAction()==3){
				List<Object> ll =  executeSQLQuery("select lower(t.dsc) tdsc from iwb.w5_table t where t.table_id=? AND t.project_uuid=?"
						, GenericUtil.uInt(formResult.getRequestParams(),"ttable_id"+prefix), prj.getProjectUuid());
				String tableName = ll.get(0).toString();
				if(tableName.indexOf('.')>0)tableName = tableName.substring(tableName.indexOf('.')+1);
				int cntField = GenericUtil.uInt(executeSQLQuery("SELECT count(1) from information_schema.tables qz where lower(qz.table_name) = ? and qz.table_schema = ?"
						, tableName, prj.getRdbmsSchema()).get(0));
				if(cntField>0){
					switch(formResult.getAction()){
					case	3://delete
						String dropTableSql = "drop table " + tableName;
						
						if(customizationId!=0/* && customizationId!=140*/)executeUpdateSQLQuery(dropTableSql);
	
						if(FrameworkSetting.vcs){
							W5VcsCommit commit = new W5VcsCommit();
							commit.setCommitTip((short)2);
							commit.setExtraSql(dropTableSql);
							commit.setProjectUuid(prj.getProjectUuid());
							commit.setComment("AutoDrop Scripts for Table: " + tableName);
							commit.setCommitUserId((Integer)formResult.getScd().get("userId"));
							Object oi = executeSQLQuery("select nextval('iwb.seq_vcs_commit')").get(0);
							commit.setVcsCommitId(-GenericUtil.uInt(oi));commit.setRunLocalFlag((short)1);
							saveObject(commit);
						}
						break;
					case	1://update
						String newTableName = formResult.getRequestParams().get("dsc"+prefix);
						if(newTableName!=null && !newTableName.toLowerCase().equals(tableName)){
							String renameTableColumnSql = "alter table " + tableName + " RENAME TO " + newTableName;
							if(customizationId!=0/* && customizationId!=140*/)executeUpdateSQLQuery(renameTableColumnSql);
		
							if(FrameworkSetting.vcs){
								W5VcsCommit commit = new W5VcsCommit();
								commit.setCommitTip((short)2);
								commit.setExtraSql(renameTableColumnSql);
								commit.setProjectUuid(prj.getProjectUuid());
								commit.setComment("AutoRename Scripts for Table: " + tableName);
								commit.setCommitUserId((Integer)formResult.getScd().get("userId"));
								Object oi = executeSQLQuery("select nextval('iwb.seq_vcs_commit')").get(0);
								commit.setVcsCommitId(-GenericUtil.uInt(oi));commit.setRunLocalFlag((short)1);
								saveObject(commit);
							}
							
						}
						break;
					}
				}
			}
			break;
		case	16://table_field
			if(formResult.getAction()==1 || formResult.getAction()==3){
				List<Object[]> ll =  executeSQLQuery("select lower(tf.dsc), lower(t.dsc) tdsc from iwb.w5_table_field tf, iwb.w5_table t where tf.table_id=t.table_id AND tf.project_uuid=t.project_uuid AND tf.table_field_id=? AND tf.project_uuid=?"
						, GenericUtil.uInt(formResult.getRequestParams(),"ttable_field_id"+prefix), prj.getProjectUuid());
				String tableFieldName = ll.get(0)[0].toString(), tableName = ll.get(0)[1].toString();
				if(tableName.indexOf('.')>0)tableName = tableName.substring(tableName.indexOf('.')+1);
				int cntField = GenericUtil.uInt(executeSQLQuery("SELECT count(1) from information_schema.columns qz where lower(qz.COLUMN_NAME)=? AND lower(qz.table_name) = ? and qz.table_schema = ?"
						, tableFieldName, tableName, prj.getRdbmsSchema()).get(0));
				if(cntField>0){
					switch(formResult.getAction()){
					case	3://delete
						String dropTableColumnSql = "alter table " + tableName + " drop column " + tableFieldName;
					
						if(customizationId!=0/* && customizationId!=140*/)executeUpdateSQLQuery(dropTableColumnSql);
	
						if(FrameworkSetting.vcs){
							W5VcsCommit commit = new W5VcsCommit();
							commit.setCommitTip((short)2);
							commit.setExtraSql(dropTableColumnSql);
							commit.setProjectUuid(prj.getProjectUuid());
							commit.setComment("AutoDropColumn Scripts for TableField: " + tableName + "." + tableFieldName);
							commit.setCommitUserId((Integer)formResult.getScd().get("userId"));
							Object oi = executeSQLQuery("select nextval('iwb.seq_vcs_commit')").get(0);
							commit.setVcsCommitId(-GenericUtil.uInt(oi));commit.setRunLocalFlag((short)1);
							saveObject(commit);
						}
						break;
					case	1://update
						String newTableFieldName = formResult.getRequestParams().get("dsc"+prefix);
						if(newTableFieldName!=null && !newTableFieldName.toLowerCase().equals(tableFieldName)){
							String renameTableColumnSql = "alter table " + tableName + " RENAME " + tableFieldName + " TO " + newTableFieldName;
							if(customizationId!=0/* && customizationId!=140*/)executeUpdateSQLQuery(renameTableColumnSql);
		
							if(FrameworkSetting.vcs){
								W5VcsCommit commit = new W5VcsCommit();
								commit.setCommitTip((short)2);
								commit.setExtraSql(renameTableColumnSql);
								commit.setProjectUuid(prj.getProjectUuid());
								commit.setComment("AutoRenameColumn Scripts for TableField: " + tableName + "." + tableFieldName);
								commit.setCommitUserId((Integer)formResult.getScd().get("userId"));
								Object oi = executeSQLQuery("select nextval('iwb.seq_vcs_commit')").get(0);
								commit.setVcsCommitId(-GenericUtil.uInt(oi));commit.setRunLocalFlag((short)1);
								saveObject(commit);
							}
							
						}
						break;
					}
				}
			} else if(true){//insert
				String tableFieldName = formResult.getRequestParams().get("dsc"+prefix);
				if(!GenericUtil.isEmpty(tableFieldName)){
					List<Object> ll =  executeSQLQuery("select lower(t.dsc) tdsc from iwb.w5_table t where t.table_id=? AND t.project_uuid=?"
							, GenericUtil.uInt(formResult.getRequestParams(),"table_id"+prefix), prj.getProjectUuid());
					if(!GenericUtil.isEmpty(ll)) {
						String tableName = ll.get(0).toString();
						if(tableName.indexOf('.')>0)tableName = tableName.substring(tableName.indexOf('.')+1);
						int cntField = GenericUtil.uInt(executeSQLQuery("SELECT count(1) from information_schema.columns qz where lower(qz.COLUMN_NAME)=? AND lower(qz.table_name) = ? and qz.table_schema = ?"
								, tableFieldName, tableName, prj.getRdbmsSchema()).get(0));
						if(cntField==0){
							int fieldType = GenericUtil.uInt(formResult.getRequestParams().get("field_tip"+prefix));
	//						int defaultControlType = GenericUtil.uInt(formResult.getRequestParams().get("default_control_tip"+prefix));
							
							String addTableColumnSql = "alter table " + tableName + " add column " + tableFieldName + " " + DBUtil.iwb2dbType(fieldType, GenericUtil.uInt(formResult.getRequestParams().get("max_length"+prefix)));
							if(customizationId!=0/* && customizationId!=140*/)executeUpdateSQLQuery(addTableColumnSql);
		
							if(FrameworkSetting.vcs){
								W5VcsCommit commit = new W5VcsCommit();
								commit.setCommitTip((short)2);
								commit.setExtraSql(addTableColumnSql);
								commit.setProjectUuid(prj.getProjectUuid());
								commit.setComment("AutoAddColumn Scripts for TableField: " + tableName + "." + tableFieldName);
								commit.setCommitUserId((Integer)formResult.getScd().get("userId"));
								Object oi = executeSQLQuery("select nextval('iwb.seq_vcs_commit')").get(0);
								commit.setVcsCommitId(-GenericUtil.uInt(oi));commit.setRunLocalFlag((short)1);
								saveObject(commit);
							}
						}
					}
				}
			
				
			}

			break;
		}
	}
	
	
	public void afterPostForm(W5FormResult fr, String prefix){
		String msg;
		Map scd = fr.getScd();
		String projectId = scd!=null ? (String)scd.get("projectId"):null;
		if(fr.getErrorMap()!=null && fr.getErrorMap().isEmpty() && fr.getForm()!=null)switch(fr.getFormId()){
	
		
		case	551://comment
			break;
/*		case	44://file attachment
			FrameworkCache.getAppSettingIntValue(formResult.getScd(), "feed_flag");
	            // fall through
			//if(FrameworkCache.getAppSettingIntValue(formResult.getScd(), "feed_flag")!=0){
			//}
		    default:
			break;*/
		}
    	msg = LocaleMsgCache.get2(scd, "reload_cache_manually");
		if(fr.getErrorMap()!=null && fr.getErrorMap().isEmpty() && fr.getForm()!=null)switch(fr.getForm().getObjectId()){
		case	3351: //component
			if(fr.getAction()!=3) {
				W5Component comp = (W5Component)metadataLoader.getMetadataObject("W5Component", "componentId", GenericUtil.uInt(fr.getRequestParams().get("tcomponent_id")), projectId, null);
						
				if(!GenericUtil.isEmpty(comp.getCode())) try{
					if(comp.getFrontendLang()!=1 && GenericUtil.hasPartInside2("5,8,9", FrameworkCache.getProject(projectId).getUiWebFrontendTip())) {
						comp.setCode(NashornUtil.babelTranspileJSX(comp.getCode()));
						executeUpdateSQLQuery("update iwb.w5_component t set js_code=?::text where project_uuid=?::text AND component_id=?::integer", 
								comp.getCode(), comp.getProjectUuid(), comp.getComponentId());
					}
					FrameworkCache.setComponent(projectId, comp);
				} catch(Exception ee) {
					if (!fr.getRequestParams().containsKey("_confirmId_" + comp.getComponentId()))
						throw new IWBException("confirm", "ConfirmId", comp.getComponentId(), null, "Error while transpiling JSX (" +ee.getCause().getMessage()+"). Still want to save?", null);
				}
			}
			
			break;
		case	1277: //user_related_project
			if(fr.getAction()==2){
				int userId = GenericUtil.uInt(fr.getRequestParams().get("user_id"));
				if(userId>0){
					UserUtil.addProjectUser((String)scd.get("projectId"), userId);
				}
			}
			break;
		case	1407://project
			if((fr.getAction()==1 || fr.getAction()==3) && GenericUtil.uInt(scd.get("ocustomizationId"))!=0 && scd.containsKey("ocustomizationId") && GenericUtil.uInt(scd.get("ocustomizationId"))!=GenericUtil.uInt(scd.get("customizationId"))){
				throw new IWBException("security","Project", 0, null, "Forbidden Command. Can not manipulate a project on another tenant.", null);
			}
			switch(fr.getAction()){
			case	5://clone
			case	2://insert
				String newProjectId = fr.getOutputFields().get("project_uuid").toString();
				int customizationId = GenericUtil.uInt(scd.get("ocustomizationId"));
				String schema = "c"+GenericUtil.lPad(customizationId+"", 5, '0')+"_"+newProjectId.replace('-', '_');
				//validate from vcs server
				executeUpdateSQLQuery("update iwb.w5_project set rdbms_schema=?, vcs_url=?, vcs_user_name=?, vcs_password=?, customization_id=? where project_uuid=?", schema, FrameworkCache.getAppSettingStringValue(0, "vcs_url_new_project","http://34.68.231.169/app/"), scd.get("userName"), "1", customizationId, newProjectId);
				executeUpdateSQLQuery("create schema IF NOT EXISTS "+schema + " AUTHORIZATION iwb");
				if(fr.getAction()==5){ //clone
					Map<String, Object> newScd = new HashMap();
					newScd.putAll(scd);newScd.put("projectId", fr.getRequestParams().get("tproject_uuid"));
					copyProject(scd, newProjectId, customizationId);
				} else {//insert
					int userTip = GenericUtil.getGlobalNextval("iwb.seq_user_tip", projectId, 0, customizationId);
					executeUpdateSQLQuery("insert into iwb.w5_user_tip(user_tip, dsc, customization_id, project_uuid, web_frontend_tip, default_main_template_id) values (?,?,?, ?, 5, 2307)", userTip, "Role Group 1", customizationId, newProjectId);
					Map<String, Object> newScd = new HashMap();
					newScd.putAll(scd);newScd.put("projectId", newProjectId);
					saveObject(new W5VcsObject(newScd, 369, userTip));
				}
				metadataLoader.addProject2Cache(newProjectId);
				FrameworkSetting.projectSystemStatus.put(newProjectId,0);
				break;
			case	3://delete all metadata
				String delProjectId = fr.getRequestParams().get("tproject_uuid").toLowerCase();
				deleteProjectMetadataAndDB(delProjectId, false);
//				FrameworkSetting.projectSystemStatus.remove(delProjectId);
				break;
			case	1:
				metadataLoader.addProject2Cache(fr.getRequestParams().get("tproject_uuid"));
			}
			break;
		default:
            break;
	
		case	13:
		case	14://lookup,detay		
			fr.getOutputMessages().add(msg);
			int lookUpId = GenericUtil.uInt(fr.getOutputFields().get("look_up_id"+prefix));
			if(lookUpId==0)lookUpId = GenericUtil.uInt((Object)fr.getRequestParams().get("look_up_id"+prefix));
			if(lookUpId==0)lookUpId = GenericUtil.uInt((Object)fr.getRequestParams().get("tlook_up_id"+prefix));
			if(fr.getForm().getObjectId()==14 && lookUpId==0){
				int detayId = GenericUtil.uInt((Object)fr.getRequestParams().get("tlook_up_detay_id"+prefix));
				if(detayId==0)detayId=GenericUtil.uInt((Object)fr.getOutputFields().get("look_up_detay_id"+prefix));
				if(detayId>0){
					List qq = executeSQLQuery("select x.look_up_id from iwb.w5_look_up_detay x where x.project_uuid=? AND x.look_up_detay_id=?", projectId, detayId);
					if(!GenericUtil.isEmpty(qq))lookUpId = GenericUtil.uInt(qq.get(0));
				}
			}
			FrameworkCache.addQueuedReloadCache(projectId,"13."+lookUpId);
			break;	
		case	79://apsetting
			fr.getOutputMessages().add(msg);
//			FrameworkCache.reloadCacheQueue.put("2-"+fr.getForm().getCustomizationId(), System.currentTimeMillis());			
			break;
			
			
		case	15://table
		case	16://tablefield
		case	945://tablefieldcalc
		case	42://tableparam
		case	657://w5_table_child	
//		case	764://w5_table_filter	
		case	1209: //tableevent
		case	1217://w5_table_access_condition_sql		
			FrameworkCache.clearPreloadCache(scd);
			fr.getOutputMessages().add(msg);
			int tableId = GenericUtil.uInt(fr.getOutputFields().get("table_id"+prefix));
			if(tableId==0)lookUpId = GenericUtil.uInt(fr.getRequestParams().get("table_id"+prefix));
			if(tableId==0)lookUpId = GenericUtil.uInt(fr.getRequestParams().get("ttable_id"+prefix));
			if(tableId==0){
				W5Table tx = FrameworkCache.getTable(projectId, fr.getForm().getObjectId());
				W5TableParam tp = tx.get_tableParamList().get(0);
				int detayId = GenericUtil.uInt(fr.getRequestParams().get(tp.getDsc()+prefix));
				if(detayId==0)detayId=GenericUtil.uInt(fr.getOutputFields().get(tp.getExpressionDsc()+prefix));
				if(detayId>0){
					List qq = executeSQLQuery("select x.table_id from "+tx.getDsc()+" x where x.project_uuid=? AND x."+tp.getExpressionDsc()+"=?", projectId, detayId);
					if(!GenericUtil.isEmpty(qq))tableId = GenericUtil.uInt(qq.get(0));
				}
			}
			FrameworkCache.addQueuedReloadCache(projectId,"15."+tableId);
			break;	

		case	389:
		case	390://workflow/step
			FrameworkCache.clearPreloadCache(scd);
			int workflowId = GenericUtil.uInt(fr.getOutputFields().get("approval_id"+prefix));
			fr.getOutputMessages().add(msg);
			FrameworkCache.addQueuedReloadCache(projectId,"389."+workflowId);			
			break;

		case	336://w5_user	
//			metadataLoader.reloadUsersCache(projectId);	
			break;
		case	338://w5_customization

			break;

		case    674://w5_job_schedule
			fr.getOutputMessages().add(msg);
//			FrameworkCache.reloadCacheQueue.put("7-"+fr.getForm().getCustomizationId(), System.currentTimeMillis());
			break;
		case	4:
		case	5://grid,grid_column
		case	8:
		case	9:
		case	10://query/field/param
		case	20:
		case	21://dbfunc/param
		case	40:
		case	41:	
		case	63:	
		case	64://template,template object	
		case	229:
		case	230:
		case	231://form_module/grid_module
		case	254://toolbar_item/menu_item
		case	634:
		case	707:
		case	708:	
		case	730://w5_query_param_ext
		case	790://help form			
		case	872:
		case	873://grid_custom_grid_column_condition,grid_custom_grid_column_renderer
		case    877:	
		case	930: //dataview
		case	936:
		case    937: //list, list_column	
		case    1168://form/cell/code_detail/form_sms_mail
		case	1173://condition_group
		case    1198://conversion,conversion_col,conversion_detail
		case	1345://m5_list
		case	5621://form_cell_property
			FrameworkCache.clearPreloadCache(scd);
		}
		
		if(fr.getAction()==2 && (fr.getForm().getObjectId()==14)){
			int lookUpId=GenericUtil.uInt((Object)fr.getRequestParams().get("look_up_id"));
			if(lookUpId!=0)UserUtil.broadCastRecordForTemplates(projectId, -lookUpId, "", 2, (Integer)scd.get("userId"));
		}
		
		
	}
	

	public boolean copyProject(Map scd, String dstProjectId, int dstCustomizationId) { // from,
																						// to
		String srcProjectId = (String) scd.get("projectId");
		if (srcProjectId.equals(dstProjectId))
			return false;
		W5Project po = FrameworkCache.getProject(srcProjectId), npo = null;
		// int customizationId = (Integer)scd.get("customizationId");
		int userId = (Integer) scd.get("userId");
		// int dstCustomizationId = customizationId;
		int smaxSqlCommit = 0;
		String schema = "c" + GenericUtil.lPad(dstCustomizationId + "", 5, '0') + "_" + dstProjectId.replace('-', '_');
		executeUpdateSQLQuery("set search_path=" + schema);

		npo = (W5Project) metadataLoader.loadProject(dstProjectId);

		List<String> doneCommits = find(
				"select t.extraSql from W5VcsCommit t where t.commitTip=2 AND t.projectUuid=?0 AND (t.vcsCommitId>0 OR t.runLocalFlag!=0) AND length(t.extraSql)>2",
				dstProjectId);
		Set<String> doneSet = new HashSet();
		for (String co : doneCommits)
			doneSet.add(co);
		List<W5VcsCommit> sqlCommits = find(
				"from W5VcsCommit t where t.commitTip=2 AND t.projectUuid=?0 order by abs(t.vcsCommitId)", srcProjectId);
		for (W5VcsCommit o : sqlCommits)if(o.getVcsCommitId()>0) {
			if (!GenericUtil.isEmpty(o.getExtraSql()) && !doneSet.contains(o.getExtraSql())) {
				W5VcsCommit no = o.newInstance(dstProjectId);
				if (o.getVcsCommitId() > 0)
					no.setVcsCommitId(-no.getVcsCommitId());
				while (no.getExtraSql().contains(po.getRdbmsSchema()))
					no.setExtraSql(no.getExtraSql().replace(po.getRdbmsSchema(), schema));
				if ((o.getVcsCommitId() > 0 || o.getRunLocalFlag() != 0)) {
					if (no.getRunLocalFlag() == 0)
						no.setRunLocalFlag((short) 1);
					executeUpdateSQLQuery(no.getExtraSql());
					smaxSqlCommit = o.getVcsCommitId();
				}
				saveObject(no);
			}
		}

		executeUpdateSQLQuery("update iwb.w5_vcs_object x "
				+ "set vcs_object_status_tip=(select case when z.vcs_object_status_tip in (3,8) then z.vcs_object_status_tip when x.vcs_commit_id!=2 AND z.vcs_commit_id!=x.vcs_commit_id then 1 else x.vcs_object_status_tip end from iwb.w5_vcs_object z where z.project_uuid=? AND z.table_id=x.table_id AND z.table_pk=x.table_pk),"
				+ "vcs_commit_id=(select z.vcs_commit_id from iwb.w5_vcs_object z where z.project_uuid=? AND z.table_id=x.table_id AND z.table_pk=x.table_pk) "
				+ "where x.project_uuid=? AND exists(select 1 from iwb.w5_vcs_object z where z.project_uuid=? AND z.table_id=x.table_id AND z.table_pk=x.table_pk)",
				srcProjectId, srcProjectId, dstProjectId, srcProjectId);

		executeUpdateSQLQuery(
				"INSERT INTO iwb.w5_vcs_object(vcs_object_id, table_id, table_pk, customization_id, project_uuid, vcs_commit_id, vcs_commit_record_hash, vcs_object_status_tip) "
						+ "select nextval('iwb.seq_vcs_object'), x.table_id, x.table_pk, ?, ?, 1, x.vcs_commit_record_hash, 2 from iwb.w5_vcs_object x where x.vcs_object_status_tip not in (3,8) AND x.project_uuid=? AND not exists(select 1 from iwb.w5_vcs_object z where z.project_uuid=? AND z.table_id=x.table_id AND z.table_pk=x.table_pk)",
				dstCustomizationId, dstProjectId, srcProjectId, dstProjectId);

		List<Object> ll3 = executeSQLQuery(
				"select x.table_id from iwb.w5_vcs_object x where x.project_uuid=? group by x.table_id order by x.table_id",
				srcProjectId);
		if (ll3 != null)
			for (Object o : ll3) {
				StringBuilder sql = new StringBuilder();
				W5Table t = FrameworkCache.getTable(0, GenericUtil.uInt(o));
				String pkField = t.get_tableParamList().get(0).getExpressionDsc();
				sql.append("delete from ").append(t.getDsc())
						.append(" x where x.project_uuid=? AND x.oproject_uuid=? AND not exists(select 1 from ")
						.append(t.getDsc()).append(" z where z.project_uuid=? AND z.").append(pkField).append("=x.")
						.append(pkField);
				for (W5TableField tf : t.get_tableFieldList())
					if (tf.getTabOrder() > 1 && !GenericUtil.hasPartInside2(
							"customization_id,project_uuid,oproject_uuid,version_no,insert_user_id,version_user_id,insert_dttm,version_dttm",
							tf.getDsc())) {
						sql.append(" AND x.").append(tf.getDsc()).append("=z.").append(tf.getDsc());
					}
				sql.append(")");
				executeUpdateSQLQuery(sql.toString(), dstProjectId, srcProjectId, srcProjectId);

				sql.setLength(0);
				StringBuilder sql2 = new StringBuilder();
				sql.append("insert into ").append(t.getDsc()).append("(");
				for (W5TableField tf : t.get_tableFieldList())
					if (!GenericUtil.hasPartInside2(
							"customization_id,project_uuid,oproject_uuid,version_no,insert_user_id,version_user_id,insert_dttm,version_dttm",
							tf.getDsc())) {
						sql.append(tf.getDsc()).append(",");
						sql2.append(tf.getDsc()).append(",");
					}
				sql.append("customization_id,project_uuid,oproject_uuid)");
				sql2.append("?,?,?");
				sql.append(" select ").append(sql2).append(" from ").append(t.getDsc())
						.append(" x where x.project_uuid=? AND not exists(select 1 from ").append(t.getDsc())
						.append(" z where z.project_uuid=? AND z.").append(pkField).append("=x.").append(pkField)
						.append(")");
				executeUpdateSQLQuery(sql.toString(), dstCustomizationId, dstProjectId, srcProjectId, srcProjectId,
						dstProjectId);
			}
		if (dstCustomizationId > 1) {
			List ll = executeSQLQuery(
					"select 1 from iwb.w5_project_related_project where project_uuid=? AND related_project_uuid=?",
					dstProjectId, srcProjectId);
			if (GenericUtil.isEmpty(ll))
				executeUpdateSQLQuery(
						"INSERT INTO iwb.w5_project_related_project(project_uuid, related_project_uuid, insert_user_id,version_user_id) "
								+ "VALUES (?, ?, ?, ?)",
						dstProjectId, srcProjectId, userId, userId);
		}

		executeUpdateSQLQuery(
				"INSERT INTO iwb.w5_project_related_project(project_uuid, related_project_uuid, insert_user_id,version_user_id) "
						+ "select ?, related_project_uuid, ?,? from iwb.w5_project_related_project x where x.project_uuid=? and not exists(select 1 from iwb.w5_project_related_project z where z.project_uuid=? AND z.related_project_uuid=x.related_project_uuid)",
				dstProjectId, userId, userId, srcProjectId, dstProjectId);
		return true;
	}
	

	public void saveCredentials(int cusId, int userId, String picUrl, String fullName, int socialNet, String email,
			String nickName, List<Map> projects, List<Map> userTips) {
		if (find("select 1 from W5Customization t where t.customizationId=?0", cusId).isEmpty())
			executeUpdateSQLQuery(
					"insert into iwb.w5_customization(customization_id, dsc, sub_domain) values (?,?,?)", cusId,
					socialNet, nickName.replace('.', '_').replace('-', '_'));
		FrameworkCache.wCustomizationMap.put(cusId,
				(W5Customization) find("from W5Customization t where t.customizationId=?0", cusId).get(0));

		FrameworkSetting.projectSystemStatus.put(projects.get(0).get("project_uuid").toString(), 0);
		if (GenericUtil.isEmpty(executeSQLQuery("select 1 from iwb.w5_user u where u.user_id=?", userId))) {
			executeUpdateSQLQuery(
					"insert into iwb.w5_user(user_id, customization_id, user_name, email, pass_word, user_status, dsc,login_rule_id, lkp_auth_external_source, auth_external_id, project_uuid) values (?,?,?,?,?,?,?,?,?,?,?)",
					userId, cusId, nickName, email, GenericUtil.getMd5Hash(nickName + 1), 1, nickName, 1, socialNet, email,
					projects.get(0).get("project_uuid"));
			int userRoleId = GenericUtil.getGlobalNextval("iwb.seq_user_role",
					(String) projects.get(0).get("project_uuid"), userId, cusId);
			executeUpdateSQLQuery(
					"insert into iwb.w5_user_role(user_role_id, user_id, role_id, customization_id,unit_id, project_uuid) values(?, ?, 0, ?,?, ?)",
					userRoleId, userId, cusId, 0, projects.get(0).get("project_uuid"));
		}

		for (Map p : projects) {
			String projectId = (String) p.get("project_uuid");
			String oprojectId = (String) p.get("oproject_uuid");
			if (oprojectId == null)
				oprojectId = projectId;
			String vcsUrl = (String) p.get("vcs_url");

			if (GenericUtil
					.isEmpty(executeSQLQuery("select 1 from iwb.w5_project p where p.project_uuid=?", projectId))) {
				String schema = "c" + GenericUtil.lPad(cusId + "", 5, '0') + "_" + projectId.replace('-', '_');
				executeUpdateSQLQuery(
						"insert into iwb.w5_project(project_uuid, customization_id, dsc, access_users,  rdbms_schema, vcs_url, vcs_user_name, vcs_password, oproject_uuid)"
								+ " values (?,?,?, ?, ?,?,?,?, ?)",
						projectId, cusId, p.get("dsc"), "" + userId, schema, vcsUrl, nickName, "1", oprojectId);
				executeUpdateSQLQuery("create schema IF NOT EXISTS " + schema + " AUTHORIZATION iwb");
			}

			metadataLoader.addProject2Cache(projectId);
			FrameworkSetting.projectSystemStatus.put(projectId, 0);
			break;
		}

		for (Map t : userTips) {
			String projectId = (String) t.get("project_uuid");
			String oprojectId = (String) t.get("oproject_uuid");
			if (oprojectId == null)
				oprojectId = projectId;
			int userTip = GenericUtil.uInt(t.get("user_tip"));
			// List list = executeSQLQuery("select 1 from iwb.w5_user_tip p
			// where
			// p.user_tip=?",userTip);
			if (GenericUtil.isEmpty(executeSQLQuery(
					"select 1 from iwb.w5_user_tip p where p.user_tip=? AND p.project_uuid=?", userTip, projectId))) {
				executeUpdateSQLQuery(
						"insert into iwb.w5_user_tip(user_tip, dsc, customization_id, project_uuid, oproject_uuid, web_frontend_tip, default_main_template_id)"
								+ " values (?,?,?, ?, ?, 5, 2307)",
						userTip, "Role Group 1", cusId, projectId, oprojectId);
				Map newScd = new HashMap();
				newScd.put("projectId", projectId);
				newScd.put("customizationId", cusId);
				newScd.put("userId", userId);
				W5VcsObject vo = new W5VcsObject(newScd, 369, userTip);
				vo.setVcsObjectStatusType((short) 9);
				saveObject(vo);
				if (GenericUtil.isEmpty(executeSQLQuery(
						"select 1 from iwb.w5_role p where p.role_id=0 AND customization_id=?", cusId))) {
					executeUpdateSQLQuery(
							"insert into iwb.w5_role(role_id, customization_id, dsc, user_tip, project_uuid) values (0,?,?,?,?)",
							cusId, "Role " + System.currentTimeMillis(), userTip, projectId);
				}
			}
		}
		metadataLoader.reloadFrameworkCaches(cusId);
	}
	
	public boolean changeChangeProjectStatus(Map<String, Object> scd, String projectUuid, int newStatus) {
		List params = new ArrayList();
		params.add(projectUuid);
		List<Map<String, Object>> l = executeSQLQuery2Map(
				"SELECT x.*,(select q.customization_id from iwb.w5_project q where q.project_uuid=x.oproject_uuid) qcus_id FROM iwb.w5_project x WHERE x.project_uuid=? ",
				params);
		if (GenericUtil.isEmpty(l))
			return false;
		Map m = l.get(0);
		if (GenericUtil.uInt(m.get("customization_id")) == 1) {
			if ((Integer) scd.get("customizationId") == 1
					|| (newStatus == 2 && GenericUtil.uInt(m.get("qcus_id")) == (Integer) scd.get("customizationId"))) {
				executeUpdateSQLQuery("update iwb.w5_project set project_status_tip=? WHERE project_uuid=?",
						newStatus, projectUuid);
				metadataLoader.addProject2Cache(projectUuid);
				return true;
			}
		}
		return false;
	}
	
	public void addToProject(int userId, String projectId, String email) {
		List<Object[]> list = 
				executeSQLQuery("select u.user_id, u.related_project_uuid from iwb.w5_user_related_project u"
						+ " where u.user_id=? AND u.related_project_uuid=?", userId, projectId);
		if (!GenericUtil.isEmpty(list)) {
			executeUpdateSQLQuery(
					"insert into iwb.w5_user_related_project(user_id, related_project_uuid) values (?,?)", userId,
					projectId);
			executeUpdateSQLQuery("update iwb.w5_user set email=? where user_id=?", email, userId);
		}
	}
}
