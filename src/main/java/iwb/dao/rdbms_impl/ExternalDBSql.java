package iwb.dao.rdbms_impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.domain.db.W5ExternalDb;
import iwb.domain.db.W5Query;
import iwb.domain.db.W5QueryField;
import iwb.domain.db.W5TableField;
import iwb.domain.result.W5QueryResult;
import iwb.exception.IWBException;
import iwb.util.GenericUtil;
import iwb.util.InfluxUtil;

@SuppressWarnings({ "unchecked", "unused" })
@Component
public class ExternalDBSql {

	protected void applyParameters(PreparedStatement s, List<Object> sqlParams) throws SQLException {
		if ((sqlParams == null) || (sqlParams.isEmpty())) {
			return;
		}
		applyParameters(s, sqlParams.toArray());
	}

	protected void applyParameters(PreparedStatement s, Object... sqlParams) throws SQLException {
		if ((sqlParams == null) || (sqlParams.length == 0)) {
			return;
		}
		int i = 1;
		for (Object o : sqlParams) {
			if (o == null) {
				s.setObject(i, null);
			} else if (o instanceof Integer) {
				s.setInt(i, ((Integer) o));
			} else if (o instanceof Long) {
				s.setLong(i, ((Long) o));
			} else if (o instanceof Short) {
				s.setShort(i, ((Short) o));
			} else if (o instanceof java.sql.Timestamp) {
				s.setTimestamp(i, (java.sql.Timestamp) o);
			} else if (o instanceof java.sql.Date) {
				s.setDate(i, (java.sql.Date) o);
			} else if (o instanceof java.util.Date) {
				s.setTimestamp(i, new java.sql.Timestamp(((java.util.Date) o).getTime()));
			} else if (o instanceof BigDecimal) {
				s.setBigDecimal(i, (BigDecimal) o);
			} else if (o instanceof String) {
				s.setString(i, (String) o);
			} else if (o instanceof Float) {
				s.setFloat(i, (Float) o);
			} else if (o instanceof Double) {
				s.setDouble(i, ((Double) o));
			} else if (o instanceof Boolean) {
				s.setBoolean(i, ((Boolean) o));
			}
			i++;
		}
	}
	
	private void prepareInfluxQuery(W5QueryResult queryResult) {
		String sql = GenericUtil.replaceSql(queryResult.getExecutedSql(), queryResult.getSqlParams());
		
		if(queryResult.getFetchRowCount() != 0) {
			sql +=" limit "+ queryResult.getFetchRowCount();
		}
		
		if (queryResult.getStartRowNumber() > 0) {
			sql +=" offset " + queryResult.getStartRowNumber();
		}
		queryResult.setExecutedSql(sql);
	}

	public void runQuery(W5QueryResult queryResult) {
//		String projectId = (String) queryResult.getScd().get("projectId");
		W5Query q = queryResult.getQuery();
		W5ExternalDb edb = FrameworkCache.getExternalDb(queryResult.getScd(), q.getMainTableId());//FrameworkCache.wExternalDbs.get(projectId).get(q.getMainTableId());
		if(edb.getLkpDbType()==11) { //influxDB
			prepareInfluxQuery(queryResult);
			String influxQL = queryResult.getExecutedSql();
			String checkInfluxQL = influxQL.toLowerCase(FrameworkSetting.appLocale);
			if(checkInfluxQL.contains("group") && checkInfluxQL.contains("time('")) {
				int ix= checkInfluxQL.indexOf("time('");
				int ex= checkInfluxQL.indexOf("')", ix + 6);
				if(ix>-1 && ex>-1 && ex-ix>5 && ex-ix>5) {
					String res = influxQL.substring(ix+6, ex);
					influxQL = influxQL.substring(0,ix+5) + res + influxQL.substring(ex+1);
					queryResult.setExecutedSql(influxQL);
				} else 
					throw new IWBException("sql", "External.InfluxQuery", queryResult.getQueryId(),
							influxQL,
							"[8," + queryResult.getQueryId() + "] " + queryResult.getQuery().getDsc() + ": time dimension not defined properly", null);
			}
			if(!influxQL.contains("limit") && queryResult.getRequestParams()!=null && GenericUtil.uInt(queryResult.getRequestParams(), "limit")>0)
				influxQL+=" limit " + GenericUtil.uInt(queryResult.getRequestParams(), "limit");
			List l = InfluxUtil.query(edb.getDbUrl(), edb.getDefaultSchema(), influxQL);
			queryResult.setData(l);
			queryResult.setNewQueryFields(q.get_queryFields());
			queryResult.setResultRowCount(GenericUtil.isEmpty(l)  ? queryResult.getStartRowNumber():(queryResult.getStartRowNumber()+l.size()+1));
			return;
		}
		Connection con = null;
		PreparedStatement s = null;
		ResultSet rs = null;
		StringBuilder sql2 = new StringBuilder();
		try {
			con = edb.getConnection();
			if (queryResult.getFetchRowCount() != 0) {
				String countSQL = "select count(1) from (" + queryResult.getExecutedSql() + " ) x";
				s = con.prepareStatement(countSQL);
				applyParameters(s, queryResult.getSqlParams());

				rs = s.executeQuery();
				rs.next();

				int resultRowCount = rs.getBigDecimal(1).intValue();
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


				switch (edb.getLkpDbType()) {
				case 2:// postgresql
					sql2.append("select z.* from (").append(queryResult.getExecutedSql()).append(" limit ?");
					if (queryResult.getStartRowNumber() > 0) {
						sql2.append(" offset ?");
					}
					sql2.append(") z");
					break;
				case	1://oracle
					sql2.append("select r.* from (select z.*, rownum rx_rx_rx_qwq from (").append(queryResult.getExecutedSql()).append(") z) r where rownum<=?");
					if (queryResult.getStartRowNumber() > 0) {
						sql2.append(" AND rx_rx_rx_qwq>?");
					}
					break;
				case	3://ms sql server ROW_NUMBER() OVER ( ORDER BY OrderDate ) AS RowNum
					sql2.append("select r.* from (select z.*, ROW_NUMBER() OVER ( ORDER BY null ) AS rx_rx_rx_qwq from (").append(queryResult.getExecutedSql()).append(") z) r where rx_rx_rx_qwq<=?");
					if (queryResult.getStartRowNumber() > 0) {
						sql2.append(" AND rx_rx_rx_qwq>?");
					}
				}

			} else {
				sql2.append(queryResult.getExecutedSql());
			}

			List<Object[]> resultData = queryResult.getFetchRowCount() == 0 ? new ArrayList<Object[]>()
					: new ArrayList<Object[]>(queryResult.getFetchRowCount());
			// sql = sql2.toString();
			s = con.prepareStatement(sql2.toString());
			applyParameters(s, queryResult.getSqlParams());
			queryResult.setExecutedSql(sql2.toString());
			//
			// if(PromisSetting.debug)logger.info(PromisUtil.replaceSql(sql2.toString(),queryResult.getSqlParams()));
			rs = s.executeQuery();
			int maxTabOrder = 0;

			List<W5QueryField> newQueryFields = null;
			while (rs.next() /*
								 * && (maxFetchedCount==0 || totalFetchedCount<maxFetchedCount )
								 */) {
				if (newQueryFields == null) {
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
								W5TableField tf = queryResult.getMainTable() != null && qf.getMainTableFieldId() > 0
										? queryResult.getMainTable().get_tableFieldMap().get(qf.getMainTableFieldId())
										: null;
								if (tf == null || (GenericUtil.accessControl4SessionField(queryResult.getScd(), tf.getRelatedSessionField())
										&& (tf.getAccessViewUserFields() != null || GenericUtil.accessControl(
												queryResult.getScd(), tf.getAccessViewTip(), tf.getAccessViewRoles(),
												tf.getAccessViewUsers())))) { // access
																				// control
									newQueryFields.add(qf);
									if (maxTabOrder < qf.getTabOrder())
										maxTabOrder = qf.getTabOrder();
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
				for (W5QueryField field : newQueryFields) {
					Object obj = rs.getObject(field.getDsc());
					if (obj != null) {
						if (obj instanceof java.sql.Timestamp) {
							try {
								obj = (queryResult.getQuery().getQueryTip() == 2 && field.getFieldTip() == 2)
										? (java.sql.Timestamp) obj
										: GenericUtil.uFormatDateTime((java.sql.Timestamp) obj);
							} catch (Exception e) {
								obj = "java.sql.Timestamp";
							}
						} else if (obj instanceof java.sql.Date) {
							try {
								obj = (queryResult.getQuery().getQueryTip() == 2 && field.getFieldTip() == 2)
										? rs.getTimestamp(field.getDsc())
										: GenericUtil.uFormatDateTime(rs.getTimestamp(field.getDsc()));
							} catch (Exception e) {
								obj = "java.sql.Date";
							}
						} else if (obj instanceof Boolean) {
							obj = (Boolean) obj ? 1 : 0;
						}
					}
					o[field.getTabOrder() - 1] = obj;
				}
				resultData.add(o);
			}
			if (queryResult.getFetchRowCount() == 0 && resultData != null) {
				queryResult.setResultRowCount(resultData.size());
			}
			queryResult.setData(resultData);

			if (rs != null)
				rs.close();
			if (s != null)
				s.close();
			if (con != null)
				con.close();

		} catch (Exception e) {
//			error = e.getMessage();
			throw new IWBException("sql", "External.Query", queryResult.getQueryId(),
					GenericUtil.replaceSql(sql2.length() == 0 ? queryResult.getExecutedSql() : sql2.toString(),
							queryResult.getSqlParams()),
					"[8," + queryResult.getQueryId() + "] " + queryResult.getQuery().getDsc(), e);
		}
	}

	public ExternalDBSql() {
		if(true || !FrameworkSetting.externalDb)return;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			System.out.println("Oracle Driver not found");
		}
		if(false)try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			System.out.println("SQL Server Driver not found");
		}
	}


}
