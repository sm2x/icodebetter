package iwb.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.domain.db.W5Table;
import iwb.domain.db.W5TableField;
import iwb.domain.db.W5TableParam;
import iwb.exception.IWBException;

public class DBUtil {
	public static boolean checkTenantSQLSecurity(String sql) {
		if(!FrameworkSetting.cloud)return false;
		String sql2=sql.toLowerCase(FrameworkSetting.appLocale);
		return (sql2.contains("iwb.") || sql2.contains("information_schema.") || sql2.contains("drop") || /*sql2.contains("delete") || */sql2.contains("truncate") || sql2.contains("search_path") || sql2.contains("grant") || sql2.contains("vacuum") || sql2.contains("lock") || sql2.contains("execute"));
	}
	public static Object[] filterExt4SQL(String code, Map<String, Object> scd, Map<String, String> requestParams, Map<String, Object> obj) {
		StringBuilder sql = new StringBuilder();
		List<Object> params = new ArrayList();
		Object[] res = new Object[2];
		res[0]=sql;res[1]=params;
		if(code==null || code.length()==0)return res;
		sql.append(code);
		for(int bas = sql.indexOf("${"); bas>=0; bas=sql.indexOf("${",bas+2)){
			int bit = sql.indexOf("}", bas+2);
			String subStr = sql.substring(bas+2, bit);
			String o = null;
			if(subStr.startsWith("scd.")){
				if(scd!=null && scd.get(subStr.substring(4))!=null)o=scd.get(subStr.substring(4)).toString(); // session
			} else if(subStr.startsWith("req.")){
				if(requestParams!=null && requestParams.get(subStr.substring(4))!=null) {
					Object oo=requestParams.get(subStr.substring(4)); // request
					o=oo.toString();
				}
			} else if(subStr.startsWith("obj.")){
				if(obj!=null && obj.get(subStr.substring(4))!=null)o=obj.get(subStr.substring(4)).toString(); // object
			} else if(subStr.startsWith("app.")){
				o = FrameworkCache.getAppSettingStringValue(scd, subStr.substring(4));
			} else {
				//o = LocaleMsgCache.get2((Integer)scd.get("customizationId"),(String)scd.get("locale"), subStr); // getMsgHTML de olabilirdi
				if(requestParams!=null && requestParams.get(subStr)!=null) {
					Object oo=requestParams.get(subStr); // request
					o=oo.toString();
				}
			}
			if(bas>0 &&  sql.charAt(bas-1)=='\'' && bit<sql.length()-1 && sql.charAt(bit+1)=='\''){ // string'tir
				params.add(o);bas--;
				sql.replace(bas, bit+2, "?");
			} else try{ //number				
				params.add(o==null || o.length()==0 ? null : new BigDecimal(o));
				sql.replace(bas, bit+1, "?");
			} catch (Exception e){
				throw new IWBException("sql","filterExt4SQL", 0, sql.toString(), "String2BigDecimal("+o+")", null);
			}
		}
		return res;
	}
	
	public static Object[] filterExt4InfluxQL(String code, Map<String, Object> scd, Map<String, String> requestParams, Map<String, Object> obj) {
		StringBuilder sql = new StringBuilder();
		List<Object> params = new ArrayList();
		Object[] res = new Object[2];
		res[0]=sql;res[1]=params;
		if(code==null || code.length()==0)return res;
		sql.append(code);
		for(int bas = sql.indexOf("${"); bas>=0; bas=sql.indexOf("${",bas+2)){
			int bit = sql.indexOf("}", bas+2);
			String subStr = sql.substring(bas+2, bit);
			String o = null;
			if(subStr.startsWith("scd.")){
				if(scd!=null && scd.get(subStr.substring(4))!=null)o=scd.get(subStr.substring(4)).toString(); // session
			} else if(subStr.startsWith("req.")){
				if(requestParams!=null && requestParams.get(subStr.substring(4))!=null)o=requestParams.get(subStr.substring(4)).toString(); // request
			} else if(subStr.startsWith("obj.")){
				if(obj!=null && obj.get(subStr.substring(4))!=null)o=obj.get(subStr.substring(4)).toString(); // object
			} else if(subStr.startsWith("app.")){
				o = FrameworkCache.getAppSettingStringValue(scd, subStr.substring(4));
			} else {
				o = LocaleMsgCache.get2((Integer)scd.get("customizationId"),(String)scd.get("locale"), subStr); // getMsgHTML de olabilirdi
			}
			params.add(o==null || o.length()==0 ? null : o);
			sql.replace(bas, bit+1, "?");
		}
		return res;
	}
	
	public static int java2iwbType(int javaType){
		switch(javaType){
		case java.sql.Types.VARCHAR:
			return 1;
		case java.sql.Types.DATE: 
		case java.sql.Types.TIMESTAMP:
		case java.sql.Types.TIME:
			return 2;
		case java.sql.Types.DOUBLE:
		case java.sql.Types.FLOAT:
		case java.sql.Types.DECIMAL:
			return 3;
		case java.sql.Types.INTEGER:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.TINYINT:
		case java.sql.Types.NUMERIC:
			return 4;
		case java.sql.Types.BOOLEAN:
			return 5;
		default :return 0;
		}
	}
	
	
	public static String includeTenantProjectPostSQL(Map scd, W5Table t, String... alias){
		String x = alias.length>0 ? alias[0]:"x";
		StringBuilder sql = new StringBuilder();
		if(t!=null)for(W5TableParam tp:t.get_tableParamList()){
			if(tp.getDsc().equals("customizationId"))sql.append(" AND ").append(x).append(".customization_id=").append(scd.get("customizationId"));
			if(tp.getDsc().equals("projectId"))sql.append(" AND ").append(x).append(".project_uuid='").append(scd.get("projectId")).append("'");
		}
		return sql.toString();
	}
	public static String iwb2dbType(int iwbType, int len){
		if(len==0)len=1024;
		switch(iwbType){
		case	1://string
			return "character varying("+len+")";
		case	2://date
			return "date";
		case	3://double
			return "numeric(20,4)";
		case	4://integer
			return "integer";
		case	5://boolean
			return "smallint not null default 0";
		}
		
		return "character varying("+len+")";
		
	}
	
	

	public static String getTableFields4VCS(W5Table t, String prefix) {
		StringBuilder s = new StringBuilder();
		if (t == null || GenericUtil.isEmpty(t.get_tableFieldList()))
			return s.toString();
		for (W5TableField f : t.get_tableFieldList()) {
			if (f.getTabOrder() < 0 || f.getDsc().equals(t.get_tableFieldList().get(0).getDsc()))
				continue;
			if (f.getDsc().equals("version_no") || f.getDsc().equals("insert_user_id")
					|| f.getDsc().equals("insert_dttm") || f.getDsc().equals("version_user_id")
					|| f.getDsc().equals("version_dttm") || f.getDsc().equals("customization_id")
					|| f.getDsc().equals("project_uuid"))
				continue;

			if (s.length() > 0)
				s.append(" || '-iwb-' || ");
			switch (f.getParamTip()) {
			case 1:
				s.append("coalesce(").append(prefix).append(".").append(f.getDsc()).append(",'')");
				break;
			case 4: // integer
			case 5: // boolean
			case 3: // double
				if (f.getNotNullFlag() != 0)
					s.append(prefix).append(".").append(f.getDsc()).append("::text");
				else
					s.append("case when ").append(prefix).append(".").append(f.getDsc())
							.append(" is null then '' else ").append(prefix).append(".").append(f.getDsc())
							.append("::text end");
				break;
			case 2:
				s.append("case when ").append(prefix).append(".").append(f.getDsc())
						.append(" is null then '' else to_char(").append(prefix).append(".").append(f.getDsc())
						.append(",'ddmmyyy-hh24miss') end");
				break;
			default:
				s.append("coalesce(").append(prefix).append(".").append(f.getDsc()).append(",'')");
				break;
			}
		}

		return s.toString();
	}
}
