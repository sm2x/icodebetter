package iwb.engine;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.rdbms_impl.ExternalDBSql;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.domain.db.W5ExternalDb;
import iwb.domain.db.W5GridColumn;
import iwb.domain.db.W5LookUp;
import iwb.domain.db.W5LookUpDetay;
import iwb.domain.db.W5Query;
import iwb.domain.db.W5QueryField;
import iwb.domain.db.W5QueryParam;
import iwb.domain.db.W5Table;
import iwb.domain.db.W5Workflow;
import iwb.domain.db.W5WsMethod;
import iwb.domain.db.W5WsMethodParam;
import iwb.domain.helper.W5GridReportHelper;
import iwb.domain.helper.W5ReportCellHelper;
import iwb.domain.result.W5GridResult;
import iwb.domain.result.W5QueryResult;
import iwb.enums.FieldDefinitions;
import iwb.exception.IWBException;
import iwb.util.GenericUtil;
import iwb.util.UserUtil;

@Component
public class QueryEngine {
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
	

	@Lazy
	@Autowired
	private RESTEngine restEngine;
	
	
	public Map executeQuery4Stat(Map<String, Object> scd, int gridId, Map<String, String> requestParams) {
		dao.checkTenant(scd);
		return dao.executeQuery4Stat(scd, gridId, requestParams);
	}

	public Map executeQuery4StatTree(Map<String, Object> scd, int gridId, Map<String, String> requestParams) {
		dao.checkTenant(scd);
		return dao.executeQuery4StatTree(scd, gridId, requestParams);
	}

	public W5QueryResult executeQuery(Map<String, Object> scd, int queryId, Map<String, String> requestParams) {
		boolean developer = scd.get("roleId") != null && GenericUtil.uInt(scd.get("roleId")) != 0;
		W5QueryResult queryResult = metadataLoader.getQueryResult(scd, queryId);
		if (queryId != 1 && queryId != 824 && queryResult.getMainTable() != null
				&& (!FrameworkSetting.debug || developer)) {
			switch (queryResult.getQuery().getQuerySourceType()) {
			case 0:
			case 15:
			case 8: // rhino, rdb table, group of query
				W5Table t = queryResult.getMainTable();
				if (t.getAccessViewTip() == 0 && !FrameworkCache.roleAccessControl(scd, 0)) {
					throw new IWBException("security", "Module", 0, null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_module_control"), null);
				}
				if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
						t.getAccessViewRoles(), t.getAccessViewUsers())) {
					throw new IWBException("security", "Query", queryId, null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_table_control_view"),
							null);
				}

				break;
			}
		}
		/*
		 * StringBuilder tmpx = new StringBuilder(
		 * "ali baba ${obj.dsc} ve 40 haramiler ${lnk.pk_query_field_id.dsc} olmus"
		 * ); dao.interprateTemplate(scd, 5,1294, tmpx, true);
		 */

		dao.checkTenant(scd);
		queryResult.setErrorMap(new HashMap());
		queryResult.setRequestParams(requestParams);
		W5Query query = queryResult.getQuery(); 
		switch (query.getQuerySourceType()) {
		case 2709://google sheet
			if(!query.getSqlFrom().startsWith("/*")) {
				StringBuilder gc = new StringBuilder();
				W5ExternalDb source = FrameworkCache.getExternalDb(scd, query.getSourceObjectId());
				
				gc.append("/*!*/function _xx_(row, ix){\nreturn {_row_no_:ix");
				//.append(query.getSqlSelect())
				for(W5QueryField f:query.get_queryFields())if(f.getTabOrder()>0)
					gc.append(",").append(f.getDsc()).append(":row[").append(f.getTabOrder()-1).append("]");
				gc.append("\n}}\nvar result=[], q=$.REST('GoogleSheetAPI.values_get',{majorDimension:'ROWS',range:\"'")
						.append(query.getSqlWhere()).append("'!").append(query.getSqlSelect())
						.append("\",spreadsheetId:'").append(query.getSqlFrom())
						.append("',access_token:'").append(source.getDbPassword())
						.append("'});\nif(q && q.values){var q2=q.values;for(var i=1;i<q2.length;i++)result.push(_xx_(q2[i], i));} else if (q.error)throw new Error(q.error.message)");
				query.setSqlFrom(gc.toString());
				
			}
			scriptEngine.executeQueryAsScript(queryResult, null);
			break;
		case 1376: // WS Method
			W5WsMethod wsm = FrameworkCache.getWsMethod(scd, query.getSourceObjectId());
			W5WsMethodParam parentParam = null;
			for (W5WsMethodParam px : wsm.get_params())
				if (px.getOutFlag() != 0 && px.getParamType() == 10) {
					parentParam = px;
					break;
				}
			Map<String, String> m2 = new HashMap();
			if (requestParams.get("filter[value]") != null) {//webix filter
				requestParams.put("xdsc", requestParams.get("filter[value]"));
				requestParams.remove("filter[value]");
			}
			for (W5QueryParam qp : query.get_queryParams())
				if (!GenericUtil.isEmpty(requestParams.get(qp.getDsc()))) {
					m2.put(qp.getExpressionDsc(), requestParams.get(qp.getDsc()));
				}
			if(query.getSqlFrom().equals("!")) {
				m2.put("_iwb_raw", "1");
				Map res = restEngine.REST(scd, wsm.get_ws().getDsc()+"."+wsm.getDsc(), m2);
				queryResult.setExtraOutMap(res);
			} else {
				StringBuilder rc = new StringBuilder();
				rc.append("function _x_(x){\nreturn {").append(query.getSqlSelect())
						.append("\n}}\nvar result=[], q=$.REST('").append(wsm.get_ws().getDsc() + "." + wsm.getDsc())
						.append("',").append(GenericUtil.fromMapToJsonString2(m2))
						.append(");\nif(q && q.get('success')){var q2=q.get('").append(parentParam.getDsc())
						.append("');if(q2)for(var i=0;i<q2.size();i++)result.push(_x_(q2.get(i)));}");
				scriptEngine.executeQueryAsScript(queryResult, rc.toString());
			}
			break;

		case 0: // Rhino Query
			scriptEngine.executeQueryAsScript(queryResult, null);
			break;
		default:
			queryResult.setViewLogModeTip((short) GenericUtil.uInt(requestParams, "_vlm"));
			if (!GenericUtil.isEmpty(requestParams.get("sort"))) {//sql injection check
				if (requestParams.get("sort").equals(FieldDefinitions.queryFieldName_Comment)) {
					queryResult.setOrderBy(FieldDefinitions.queryFieldName_Comment); 
				} else if (!requestParams.get("sort").contains("_qw_")) {
					queryResult.setOrderBy(requestParams.get("sort"));
					if (requestParams.get("dir") != null) {
						if (queryResult.getMainTable() != null)
							for (W5QueryField f : query.get_queryFields())
								if (queryResult.getOrderBy().equals(f.getDsc())) {
									if (f.getMainTableFieldId() != 0 && queryResult.getMainTable().get_tableFieldMap()
											.containsKey(f.getMainTableFieldId())) {
										queryResult.setOrderBy("x." + queryResult.getOrderBy());
									}
									break;
								}
						// queryResult.setOrderBy(((!query.getSqlFrom().contains(",")
						// &&
						// !query.getSqlFrom().contains("join")
						// &&
						// query.getSqlFrom().contains(" x")) ?
						// "x." : "") +
						// queryResult.getOrderBy() + " " +
						// requestParams.get("dir"));
						queryResult.setOrderBy(queryResult.getOrderBy() + " " + requestParams.get("dir"));
					}
				} else
					queryResult.setOrderBy(query.getSqlOrderby());
			} else
				queryResult.setOrderBy(query.getSqlOrderby());
			switch (query.getQueryType()) {
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
			if (queryResult.getErrorMap().isEmpty()) {
				queryResult.setFetchRowCount(GenericUtil.uIntNvl(requestParams, "limit", GenericUtil.uInt(requestParams, "firstLimit")));
				queryResult.setStartRowNumber(GenericUtil.uInt(requestParams, "start"));
				if(query.getQuerySourceType()!=4658) {
					dao.runQuery(queryResult);
					if (query.getShowParentRecordFlag() != 0 && queryResult.getData() != null) {
						for (Object[] oz : queryResult.getData()) {
							int tableId = GenericUtil.uInt(oz[query.get_tableIdTabOrder() - 1]);
							int tablePk = GenericUtil.uInt(oz[query.get_tablePkTabOrder() - 1]);
							if (tableId != 0 && tablePk != 0)
								oz[oz.length - 1] = dao.findRecordParentRecords(scd, tableId, tablePk, 0, true);
						}
					}
				} else {
					externalDB.runQuery(queryResult);

				}
			}
		}

		// postProcessFields : LookupQuery Control
		if (queryResult.getErrorMap().isEmpty() && !GenericUtil.isEmpty(queryResult.getNewQueryFields())
				&& !GenericUtil.isEmpty(queryResult.getData())) {
			Map<Integer, W5QueryResult> qrm = new HashMap(); // cache
			for (W5QueryField qf : queryResult.getNewQueryFields())
				if ((qf.getPostProcessType() == 16 || qf.getPostProcessType() == 17) && qf.getLookupQueryId() != 0) { // LookupQuery
																													// den
																													// alinacak
					W5QueryResult lookupQueryResult = qrm.get(qf.getLookupQueryId());
					if (lookupQueryResult == null) {
						lookupQueryResult = metadataLoader.getQueryResult(scd, qf.getLookupQueryId());
						lookupQueryResult.setErrorMap(new HashMap());
						lookupQueryResult.setRequestParams(new HashMap());
						lookupQueryResult.setOrderBy(lookupQueryResult.getQuery().getSqlOrderby());
						switch (lookupQueryResult.getQuery().getQuerySourceType()) {
						case 1376: // WS Method
							W5WsMethod wsm = FrameworkCache.getWsMethod(scd,
									lookupQueryResult.getQuery().getSourceObjectId());
							W5WsMethodParam parentParam = null;
							for (W5WsMethodParam px : wsm.get_params())
								if (px.getOutFlag() != 0 && px.getParamType() == 10) {
									parentParam = px;
									break;
								}
							Map<String, String> m2 = new HashMap();
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
							break;
						case 15: // table
							switch (lookupQueryResult.getQuery().getQueryType()) {
							case 12:
								lookupQueryResult.prepareTreeQuery(new HashMap());
								break; // lookup tree query
							default:
								lookupQueryResult.prepareQuery(new HashMap());
							}
							if (lookupQueryResult.getErrorMap().isEmpty()) {
								dao.runQuery(lookupQueryResult);
							}
						}
						qrm.put(qf.getLookupQueryId(), lookupQueryResult);
						;
					}

					if (lookupQueryResult.getErrorMap().isEmpty()
							&& !GenericUtil.isEmpty(lookupQueryResult.getData())) {
						for (W5QueryField qf2 : queryResult.getNewQueryFields())
							if (qf2.getDsc().equals(qf.getDsc() + "_qw_")) {
								int nqfi = qf2.getTabOrder() - 1;
								Map<String, Object[]> rmap = new HashMap();
								for (Object[] oo : lookupQueryResult.getData())
									if (oo[0] != null && oo[1] != null)
										rmap.put(oo[1].toString(), oo);
								for (Object[] oo : queryResult.getData()) {
									Object so = oo[qf.getTabOrder() - 1];
									if (so != null) {
										if (qf.getPostProcessType() == 16) {
											Object[] loo = rmap.get(so.toString()); // single
											if (loo != null) {
												oo[nqfi] = loo[0].toString();
												if (loo.length > 2)
													for (int zi = 2; zi < loo.length; zi++) {
														oo[nqfi + zi - 1] = loo[zi];
													}
											}
										} else {
											String[] lso = so.toString().split(",");
											StringBuilder sb = new StringBuilder();
											for (String s : lso)
												if (!GenericUtil.isEmpty(s)) {
													Object[] loo = rmap.get(s);
													if (loo != null)
														sb.append(loo[0]).append(",");
												}
											if (sb.length() > 0) {
												sb.setLength(sb.length() - 1);
												oo[nqfi] = sb.toString();
											}
										}
									}
								}
								break;
							}
					}
				}
		}
		return queryResult;
	}

	public Map<String, Object> executeQuery2Map(Map<String, Object> scd, int queryId,
			Map<String, String> requestParams) {
		W5QueryResult queryResult = metadataLoader.getQueryResult(scd, queryId);
		queryResult.setErrorMap(new HashMap());
		queryResult.setRequestParams(requestParams);
		queryResult.prepareQuery(null);
		if (queryResult.getErrorMap().isEmpty()) {
			Map<String, Object> m = dao.runSQLQuery2Map(queryResult.getExecutedSql(), queryResult.getSqlParams(),
					queryResult.getQuery().get_queryFields());
			return m;
		}
		return null;
	}
	

	public List<W5ReportCellHelper> getGridReportResult(Map<String, Object> scd, int gridId, String gridColumns,
			Map<String, String> requestParams) {
		String xlocale = (String) scd.get("locale");
		W5GridResult gridResult = metadataLoader.getGridResult(scd, gridId, requestParams, true);
		int queryId = gridResult.getGrid().getQueryId();
		requestParams.remove("firstLimit");
		requestParams.remove("limit");
		requestParams.remove("start");
		requestParams.put("grid_report_flag", "1");
		W5QueryResult queryResult = executeQuery(scd, queryId, requestParams);

		if (queryResult.getErrorMap().isEmpty()) {

			List<W5ReportCellHelper> list = new ArrayList<W5ReportCellHelper>();
			// list.add(new WReportResult(row_id, column_id, deger, row_tip,
			// cell_tip, colspan, tag));
			list.add(
					new W5ReportCellHelper(0, 1,
							LocaleMsgCache.get2(scd,
									gridResult.getGrid().getLocaleMsgKey()),
							(short) 0, (short) 1, (short) 20, "1;30,70")); // baslik:
																			// id,
																			// font_size,
																			// dsc,
																			// row_tip,
																			// yatay?,
			list.add(new W5ReportCellHelper(1, 1, GenericUtil.uFormatDateTime(new Date()), (short) 1, (short) 1,
					(short) -1, LocaleMsgCache.get2(scd, "report_time"))); // param:
																			// id,
																			// sira,
																			// dsc,
																			// row_tip,

			Map<String, W5QueryField> m1 = new HashMap<String, W5QueryField>();
			if(queryResult.getNewQueryFields()==null) {
				queryResult.setNewQueryFields(new ArrayList());
				queryResult.getNewQueryFields().addAll(queryResult.getQuery().get_queryFields());
			}
			for (W5QueryField f : queryResult.getNewQueryFields()) {
				m1.put(f.getDsc(), f);
			}
			Map<Integer, W5GridColumn> m2 = new HashMap<Integer, W5GridColumn>();
			int nxtTmp = -1;
			for (W5GridColumn c : gridResult.getGrid().get_gridColumnList()) {
				if (c.getQueryFieldId() == 0) {
					c.setQueryFieldId(nxtTmp--);
				}
				m2.put(c.getQueryFieldId(), c);
			}
			List<W5QueryField> l1 = new ArrayList<W5QueryField>(queryResult.getNewQueryFields().size());
			String[] gcs = gridColumns.split(";");
			int startRow = 2;
			int startCol = 0;
			for (int g = 0; g < gcs.length; g++) {
				String[] cs = gcs[g].split(",");

				W5QueryField f = m1.get(cs[0]);
				if (f != null) {
					W5GridColumn c = m2.get(f.getQueryFieldId());
					if (f.getDsc().equals(FieldDefinitions.queryFieldName_Workflow)) { // onay
																						// varsa,raporda
																						// gorunmesi
																						// icin
						c = new W5GridColumn();
						c.setLocaleMsgKey(
								LocaleMsgCache.get2((Integer) scd.get("customizationId"), xlocale, "approval_status"));
						c.setAlignType((short) 1);
					}
					if (c != null) {
						list.add(new W5ReportCellHelper(startRow, (startCol + 1),
								LocaleMsgCache.get2((Integer) scd.get("customizationId"), xlocale, c.getLocaleMsgKey()),
								(short) 2, (short) GenericUtil.uInt(cs[1]), c.getAlignType(), "")); // column:
																									// id,
																									// font_size,
																									// dsc,
																									// row_tip,
						l1.add(f);
						startCol++;
					}
				}
			}

			int customizationId = (Integer) scd.get("customizationId");
			// startRow = gcs.length + 2;
			startRow = 3;
			for (int i = 0; i < queryResult.getData().size(); i++) {
				int g = 1;
				for (W5QueryField f : l1)
					if (f.getTabOrder() > 0) {
						String dataType = "";
						Object obj = queryResult.getData().get(i)[f.getTabOrder() - 1];
						if (obj != null && f.getDsc().equals(FieldDefinitions.queryFieldName_Workflow)) {
							String[] ozs = ((String) queryResult.getData().get(i)[f.getTabOrder() - 1]).split(";");
							int appId = GenericUtil.uInt(ozs[1]); // workflowId:
																	
							int appStepId = GenericUtil.uInt(ozs[2]); // approvalStepId
							W5Workflow appr = FrameworkCache.getWorkflow(scd, appId);
							String appStepDsc = "";
							if (appr != null
									&& appr.get_approvalStepMap().get(Math.abs(appStepId)).getNewInstance() != null)
								appStepDsc = appr.get_approvalStepMap().get(Math.abs(appStepId)).getNewInstance()
										.getDsc();
							obj = (LocaleMsgCache.get2(scd, appStepDsc));
						}
						String res = null;
						if (obj != null) {
							res = obj.toString();

							if (f.getDsc().contains("_flag")) {
								res = GenericUtil.uInt(res) != 0 ? "x" : "o";
							} else {
								switch (f.getPostProcessType()) {
								case 20:
									res = UserUtil.getUserName(GenericUtil.uInt(obj));
									break;
								case 53:
									res = UserUtil.getUserDsc(GenericUtil.uInt(obj));
									break;
								case 10:
								case 11: // demek ki lookup'li deger tutulacak
									W5LookUp lookUp = FrameworkCache.getLookUp(scd, f.getLookupQueryId());
									if (lookUp != null) {
										String[] ids = res.split(",");
										res = "";
										for (String sz : ids) {
											res += ", ";
											W5LookUpDetay d = lookUp.get_detayMap().get(sz);
											if (d != null) {
												String s = d.getDsc();
												if (s != null) {
													res += LocaleMsgCache.get2(scd, s);
												}
											} else if (d == null && f.getLookupQueryId() == 12) { // lookup
																									// static
																									// or
																									// lookup
																									// static(multi)
																									// ve
																									// empty
												for (W5QueryField ff : queryResult.getNewQueryFields()) {
													if (ff.getDsc().compareTo(f.getDsc() + "_qw_") == 0) {
														res += queryResult.getData().get(i)[ff.getTabOrder() - 1];
														break;
													}
												}
											} else {
												res += "???: " + sz;
											}
										}
										if (res.length() > 0)
											res = res.substring(1);
									}
									break;
								case 12:
									for (W5QueryField ff : queryResult.getNewQueryFields()) {
										if (ff.getDsc().compareTo(f.getDsc() + "_qw_") == 0) {
											res = queryResult.getData().get(i)[ff.getTabOrder() - 1] != null
													? queryResult.getData().get(i)[ff.getTabOrder() - 1].toString()
													: "";
											break;
										}
									}
									break;

								default:
									if (f.getFieldType() == 3 || f.getFieldType() == 4) {
										dataType = "T:1";
									}
									;
									for (W5QueryField ff : queryResult.getNewQueryFields()) {
										if (ff.getDsc().compareTo(f.getDsc() + "_qw_") == 0) {
											res = queryResult.getData().get(i)[ff.getTabOrder() - 1] != null
													? queryResult.getData().get(i)[ff.getTabOrder() - 1].toString()
													: "";
											break;
										}
									}
									break;
								}
							}
						}
						list.add(new W5ReportCellHelper(i + startRow, g++, res, (short) 3, (short) 0, (short) 1,
								dataType)); // data: id, font_size, dsc,
											// row_tip,
					}
			}
			return list;
		}
		return null;
	}
	public List executeQuery4DataList(Map<String, Object> scd, int tableId, Map<String, String> requestParams) {
		W5Table t = FrameworkCache.getTable(scd, tableId);
		if (t.getAccessViewTip() == 0 && !FrameworkCache.roleAccessControl(scd, 0)) {
			throw new IWBException("security", "Module", 0, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_module_control"), null);
		}
		if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
				t.getAccessViewRoles(), t.getAccessViewUsers())) {
			throw new IWBException("security", "Table", tableId, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_table_control_view"), null);
		}
		dao.checkTenant(scd);
		return dao.executeQuery4DataList(scd, t, requestParams);
	}
	
	public List executeQuery4Pivot(Map<String, Object> scd, int tableId, Map<String, String> requestParams) {
		W5Table t = FrameworkCache.getTable(scd, tableId);

		if (t.getAccessViewTip() == 0 && !FrameworkCache.roleAccessControl(scd, 0)) {
			throw new IWBException("security", "Module", 0, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_module_control"), null);
		}
		if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
				t.getAccessViewRoles(), t.getAccessViewUsers())) {
			throw new IWBException("security", "Table", tableId, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_table_control_view"), null);
		}
		dao.checkTenant(scd);
		return dao.executeQuery4Pivot(scd, t, requestParams);
	}
	
	
	public W5GridReportHelper prepareGridReport(Map<String, Object> scd, int gridId, String gridColumns,
			Map<String, String> requestParams) {
		
		W5GridReportHelper result = new W5GridReportHelper(gridId);
		String xlocale = (String) scd.get("locale");
		W5GridResult gridResult = metadataLoader.getGridResult(scd, gridId, requestParams, true);
		int queryId = gridResult.getGrid().getQueryId();
		requestParams.remove("firstLimit");
		requestParams.remove("limit");
		requestParams.remove("start");
		requestParams.put("grid_report_flag", "1");
		W5QueryResult queryResult = executeQuery(scd, queryId, requestParams);

		if (queryResult.getErrorMap().isEmpty()) {
			result.setRecordCount(queryResult.getData().size());
			result.setReportName(LocaleMsgCache.get2(scd, gridResult.getGrid().getLocaleMsgKey()));



			Map<String, W5QueryField> m1 = new HashMap<String, W5QueryField>();
			for (W5QueryField f : queryResult.getNewQueryFields()) {
				m1.put(f.getDsc(), f);
			}
			Map<Integer, W5GridColumn> m2 = new HashMap<Integer, W5GridColumn>();
			int nxtTmp = -1;
			for (W5GridColumn c : gridResult.getGrid().get_gridColumnList()) {
				if (c.getQueryFieldId() == 0) {
					c.setQueryFieldId(nxtTmp--);
				}
				m2.put(c.getQueryFieldId(), c);
			}
			String[] gcs = gridColumns.split(";");
			List<W5GridColumn> l1 = new ArrayList<W5GridColumn>(gcs.length);

			for (int g = 0; g < gcs.length; g++) {
				String[] cs = gcs[g].split(",");

				W5QueryField f = m1.get(cs[0]);
				if (f != null) {
					W5GridColumn c = m2.get(f.getQueryFieldId());
					if(c!=null) {
						if (f.getDsc().equals(FieldDefinitions.queryFieldName_Workflow)) { // onay
																							// varsa,raporda
																							// gorunmesi
																							// icin
							c = new W5GridColumn();
							c.setLocaleMsgKey("approval_status");
							c.setAlignType((short) 1);
						} else 
							c = c.clone();
						c.setLocaleMsgKey(LocaleMsgCache.get2(scd, c.getLocaleMsgKey()));
						c.setWidth((short) GenericUtil.uInt(cs[1]));
						c.set_queryField(f);
						
						l1.add(c);
					}
				}
			}
			
			result.setColumns(l1);

			int customizationId = (Integer) scd.get("customizationId");
			// startRow = gcs.length + 2;
			
			List<Map> data = new ArrayList<Map>();
			result.setData(data);
			for (int i = 0; i < queryResult.getData().size(); i++) {
				Map row = new HashMap();
				data.add(row);
				for (W5GridColumn gc: l1) {
					W5QueryField f =  gc.get_queryField();
					if (f.getTabOrder() > 0) {
						Object obj = queryResult.getData().get(i)[f.getTabOrder() - 1];
						if (obj != null && f.getDsc().equals(FieldDefinitions.queryFieldName_Workflow)) {
							String[] ozs = ((String) queryResult.getData().get(i)[f.getTabOrder() - 1]).split(";");
							int appId = GenericUtil.uInt(ozs[1]); // workflowId:
																
							int appStepId = GenericUtil.uInt(ozs[2]); // approvalStepId
							W5Workflow appr = FrameworkCache.getWorkflow(scd, appId);
							String appStepDsc = "";
							if (appr != null
									&& appr.get_approvalStepMap().get(Math.abs(appStepId)).getNewInstance() != null)
								appStepDsc = appr.get_approvalStepMap().get(Math.abs(appStepId)).getNewInstance()
										.getDsc();
							obj = (LocaleMsgCache.get2(scd, appStepDsc));
						}
						String res = null;
						if (obj != null) {
							res = obj.toString();

							if (f.getDsc().contains("_flag")) {
								res = GenericUtil.uInt(res) != 0 ? "x" : "o";
							} else {
								switch (f.getPostProcessType()) {
								case 20:
									res = UserUtil.getUserName(GenericUtil.uInt(obj));
									break;
								case 53:
									res = UserUtil.getUserDsc(GenericUtil.uInt(obj));
									break;
								case 10:
								case 11: // demek ki lookup'li deger tutulacak
									W5LookUp lookUp = FrameworkCache.getLookUp(scd, f.getLookupQueryId());
									if (lookUp != null) {
										String[] ids = res.split(",");
										res = "";
										for (String sz : ids) {
											res += ", ";
											W5LookUpDetay d = lookUp.get_detayMap().get(sz);
											if (d != null) {
												String s = d.getDsc();
												if (s != null) {
													res += LocaleMsgCache.get2(scd, s);
												}
											} else if (d == null && f.getLookupQueryId() == 12) { // lookup
																									// static
																									// or
																									// lookup
																									// static(multi)
																									// ve
																									// empty
												for (W5QueryField ff : queryResult.getNewQueryFields()) {
													if (ff.getDsc().compareTo(f.getDsc() + "_qw_") == 0) {
														res += queryResult.getData().get(i)[ff.getTabOrder() - 1];
														break;
													}
												}
											} else {
												res += "???: " + sz;
											}
										}
										if (res.length() > 0)
											res = res.substring(1);
									}
									break;
								case 12:
									for (W5QueryField ff : queryResult.getNewQueryFields()) {
										if (ff.getDsc().compareTo(f.getDsc() + "_qw_") == 0) {
											res = queryResult.getData().get(i)[ff.getTabOrder() - 1] != null
													? queryResult.getData().get(i)[ff.getTabOrder() - 1].toString()
													: "";
											break;
										}
									}
									break;

								default:
									if (f.getFieldType() == 3 || f.getFieldType() == 4) {
										row.put(f.getDsc(), obj);
										continue;
									}
									for (W5QueryField ff : queryResult.getNewQueryFields()) {
										if (ff.getDsc().compareTo(f.getDsc() + "_qw_") == 0) {
											res = queryResult.getData().get(i)[ff.getTabOrder() - 1] != null
													? queryResult.getData().get(i)[ff.getTabOrder() - 1].toString()
													: "";
											break;
										}
									}
									break;
								}
							}
						}
						row.put(f.getDsc(), res);

					}
				}
			}
			return result;
		}
		return null;
	}
}
