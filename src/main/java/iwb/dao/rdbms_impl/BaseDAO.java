package iwb.dao.rdbms_impl;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import iwb.cache.FrameworkSetting;
import iwb.exception.IWBException;
import iwb.model.db.Log5Base;
import iwb.util.GenericUtil;
import iwb.util.LogUtil;

/**
 * Base dao class based on hibernate session
 * 
 * @author muhammed 3/7/2018
 */
public abstract class BaseDAO {

	@Autowired
	@Qualifier("entityManagerFactory")
	private EntityManager entityManager;
	


	protected Session getCurrentSession() {
		return entityManager.unwrap(Session.class);
	}

	public List find(String query, Object... params) {
		Query qry = getCurrentSession().createQuery(query);
		if (params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				qry.setParameter(i, params[i]);
			}
		}
		return qry.list();
	}

	public <T> T getObject(Class<T> clazz, Serializable id) {
		return getCurrentSession().get(clazz, id);
	}

	public void saveObject(Object o) {
		if (o instanceof Log5Base && FrameworkSetting.log2tsdb)
			LogUtil.logObject(((Log5Base) o), false);
		else
			getCurrentSession().save(o);
	}

	public void updateObject(Object o) {
		getCurrentSession().update(o);
	}

	public void removeObject(Class clazz, Serializable id) {
		Session session = getCurrentSession();
		Object o = session.get(clazz, id);
		if (o != null) {
			session.delete(o);
		}
	}

	public void removeObject(Object obj) {
		getCurrentSession().delete(obj);
	}

	public void removeAllObjects(List<?> l) {
		Session session = getCurrentSession();
		for(Object o:l)
			session.delete(o);
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

	protected void applyParameters(PreparedStatement s, List<Object> sqlParams) throws SQLException {
		if ((sqlParams == null) || (sqlParams.isEmpty())) {
			return;
		}
		applyParameters(s, sqlParams.toArray());
	}

	protected Query applyParameters(Query query, List<Object> sqlParams) {
		if (sqlParams == null) {
			return query;
		}
		int i = 1;
		for (Object o : sqlParams) {
			if (o instanceof Integer) {
				query = query.setInteger(i, ((Integer) o).intValue());
			} else if (o instanceof Long) {
				query = query.setLong(i, ((Long) o));
			} else if (o instanceof Short) {
				query = query.setShort(i, ((Short) o));
			} else if (o instanceof Date) {
				query = query.setDate(i, new java.sql.Date(((Date) o).getTime()));
			} else if (o instanceof BigDecimal) {
				query = query.setBigDecimal(i, (BigDecimal) o);
			} else if (o instanceof String) {
				query = query.setString(i, (String) o);
			} else if (o instanceof Float) {
				query = query.setFloat(i, (Float) o);
			} else if (o instanceof Double) {
				query = query.setDouble(i, ((Double) o));
			} else if (o instanceof Boolean) {
				query = query.setBoolean(i, ((Boolean) o));
			}
			i++;
		}
		return query;
	}

	protected SQLQuery applyParameters2(SQLQuery query, Object... sqlParams) {
		if (sqlParams == null || sqlParams.length == 0) {
			return query;
		}
		int i = 0;
		for (Object o : sqlParams) {
			if (o instanceof Integer) {
				query.setInteger(i, ((Integer) o));
			} else if (o instanceof Long) {
				query.setLong(i, ((Long) o));
			} else if (o instanceof Short) {
				query.setShort(i, ((Short) o));
			} else if (o instanceof Date) {
				query.setDate(i, new java.sql.Date(((Date) o).getTime()));
			} else if (o instanceof BigDecimal) {
				query.setBigDecimal(i, (BigDecimal) o);
			} else if (o instanceof String) {
				query.setString(i, (String) o);
			} else if (o instanceof Float) {
				query.setFloat(i, (Float) o);
			} else if (o instanceof Double) {
				query.setDouble(i, ((Double) o));
			} else if (o instanceof Boolean) {
				query.setBoolean(i, ((Boolean) o));
			}
			i++;
		}
		return query;
	}

	protected SQLQuery applyParameters2(SQLQuery query, List<Object> sqlParams) {
		if (sqlParams == null) {
			return query;
		}
		return applyParameters2(query, sqlParams.toArray());
	}

	public List executeSQLQuery(final String sql, final Object... params) {
		try {
			return getCurrentSession().doReturningWork(new ReturningWork<List>() {
				public List execute(Connection conn) throws SQLException {
					PreparedStatement s = conn.prepareStatement(sql);

					if (params != null && params.length > 0) {
						for (int i = 0; i < params.length; i++) {
							if (params[i] == null) {
								s.setObject(i + 1, null);
							} else if (params[i] instanceof Date) {
								s.setDate(i + 1, new java.sql.Date(((Date) params[i]).getTime()));
							} else {
								s.setObject(i + 1, params[i]);
							}
						}
					}

					ResultSet rs = s.executeQuery();
					int columnCount = rs.getMetaData().getColumnCount();
					List l = new ArrayList();
					while (rs.next()) {
						if (columnCount == 1) {
							l.add(rs.getObject(1));
						} else {
							Object[] oz = new Object[columnCount];
							for (int i = 0; i < columnCount; i++) {
								oz[i] = rs.getObject(i + 1);
							}
							l.add(oz);
						}
					}
					rs.close();
					s.close();
					if (FrameworkSetting.hibernateCloseAfterWork) {
						conn.close();
					}
					return l.isEmpty() ? null : l;
				}
			});
		} catch (Exception e) {
			throw new IWBException("sql", "Custom.Query.List", 0, sql, "Custom Query2List: "  + (e.getCause()!=null ? e.getCause().getMessage():e.getMessage()), e);
		}
	}

	public List executeSQLQuery2(final String sql, final List params) {
		return executeSQLQuery(sql, params == null ? null : params.toArray());
	}

	public List executeSQLQuery2Map(final String sql, final List params) {
		try {
			return getCurrentSession().doReturningWork(new ReturningWork<List>() {
				public List execute(Connection conn) throws SQLException {
					// (Connection conn) -> {
					PreparedStatement s = conn.prepareStatement(sql);
					if (params != null && params.size() > 0) {
						for (int i = 0; i < params.size(); i++) {
							if (params.get(i) == null) {
								s.setObject(i + 1, null);
							} else if (params.get(i) instanceof Date) {
								s.setDate(i + 1, new java.sql.Date(((Date) params.get(i)).getTime()));
							} else {
								s.setObject(i + 1, params.get(i));
							}
						}
					}

					ResultSet rs = s.executeQuery();
					int columnCount = rs.getMetaData().getColumnCount();
					List l = new ArrayList();
					Map<String, Object> result = null;
					ResultSetMetaData rsm = null;

					while (rs.next()) {
						if (rsm == null) {
							rsm = rs.getMetaData();
						}
						result = new HashMap<String, Object>();
						for (int columnIndex = 1; columnIndex <= rsm.getColumnCount(); columnIndex++) {
							String columnName = rsm.getColumnName(columnIndex).toLowerCase(FrameworkSetting.appLocale);
							Object obj = rs.getObject(columnIndex);
							if (obj == null) {
								continue;
							}
							if (obj instanceof java.sql.Timestamp) {
								try {
									result.put(columnName, GenericUtil.uFormatDateTimeSade((java.sql.Timestamp) obj));
								} catch (Exception e) {
								}
							} else if (obj instanceof java.sql.Date) {
								try {
									result.put(columnName, GenericUtil.uFormatDateSade((java.sql.Date) obj));
								} catch (Exception e) {
								}
							} else {
								result.put(columnName, obj.toString());
							}
						}
						l.add(result);
					}
					rs.close();
					s.close();
					if (FrameworkSetting.hibernateCloseAfterWork) {
						conn.close();
					}
					return l.isEmpty() ? null : l;
				}
			});
		} catch (Exception e) {
			throw new IWBException("sql", "Custom.Query.ListOfMap", 0, GenericUtil.replaceSql(sql, params),
					"Custom Query2Map: " + (e.getCause()!=null ? e.getCause().getMessage():e.getMessage()), e);
		}
	}

	public int executeUpdateSQLQuery(final String sql, final Object... params) {
		return getCurrentSession().doReturningWork(new ReturningWork<Integer>() {
			public Integer execute(Connection conn) throws SQLException {
				try {
					PreparedStatement s = conn.prepareStatement(sql);
					if (params != null && params.length > 0) {
						if (params.length == 1 && params[0] instanceof List) {
							List l = (List) params[0];
							int i = 0;
							for (Object p : l) {
								if (p == null) {
									s.setObject(i + 1, null);
								} else if (p instanceof Date) {
									s.setDate(i + 1, new java.sql.Date(((Date) p).getTime()));
								} else if (p instanceof Double) {
									s.setDouble(i + 1, Double.valueOf(p.toString()));
								} else if (p instanceof Integer) {
									s.setInt(i + 1, Integer.valueOf(p.toString()));
								} else {
									s.setObject(i + 1, p);
								}
								i++;
							}
						} else {
							for (int i = 0; i < params.length; i++) {
								if (params[i] == null) {
									s.setObject(i + 1, null);
								} else if (params[i] instanceof Date) {
									s.setDate(i + 1, new java.sql.Date(((Date) params[i]).getTime()));
								} else if (params[i] instanceof Double) {
									s.setDouble(i + 1, Double.valueOf(params[i].toString()));
								} else if (params[i] instanceof Integer) {
									s.setInt(i + 1, Integer.valueOf(params[i].toString()));
								} else {
									s.setObject(i + 1, params[i]);
								}
							}
						}
					}
					s.execute();
					s.close();
					if (FrameworkSetting.hibernateCloseAfterWork) {
						conn.close();
					}
				} catch (Exception e) {
					if (FrameworkSetting.debug)
						e.printStackTrace();
					throw new IWBException("sql", "Custom.Query.Update", 0, sql, "Custom Query Update: "  + (e.getCause()!=null ? e.getCause().getMessage():e.getMessage()), e);
				}
				return 1;
			}
		});
	}
}
