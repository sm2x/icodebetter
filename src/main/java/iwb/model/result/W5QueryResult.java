package iwb.model.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.enums.FieldDefinitions;
import iwb.model.db.W5Param;
import iwb.model.db.W5Query;
import iwb.model.db.W5QueryField;
import iwb.model.db.W5QueryParam;
import iwb.model.db.W5Table;
import iwb.model.db.W5TableField;
import iwb.model.db.W5TableParam;
import iwb.util.DBUtil;
import iwb.util.GenericUtil;



public class W5QueryResult implements W5MetaResult{
	
	private	int	queryId;
	private	long resultRowCount;
	private	long startRowNumber;
	private	int	fetchRowCount;
	private	boolean	hasPaging;
	private	boolean	displayInfo;
	private	String orderBy;
	private String executedSql;
	private W5Query query;

	private int processTime;
	private Map<String, Object> scd;
	private List<Object[]> data;
	private	List<Object> sqlParams;
	private	Map<String, String>	errorMap;
	private Map<String,String>	requestParams;
	private W5Table mainTable;
	private	List<W5QueryField> newQueryFields;

	private List<W5QueryField> postProcessQueryFields;
	private Map<String,Object>	extraOutMap;	
	private	String	executedSqlFrom;
	private	List<Object> executedSqlFromParams;
	private Map<String, W5QueryField> queryColMap;

	
	
	public	W5QueryResult(int queryId){
		this.queryId = queryId;
	}

	public List<Object[]> getData() {
		return data;
	}

	public void setData(List<Object[]> data) {
		this.data = data;
	}

	public Map<String, String> getErrorMap() {
		return errorMap;
	}

	public void setErrorMap(Map<String, String> errorMap) {
		this.errorMap = errorMap;
	}

	public String getExecutedSql() {
		return executedSql;
	}

	public void setExecutedSql(String executedSql) {
		this.executedSql = executedSql;
	}

	public int getFetchRowCount() {
		return fetchRowCount;
	}

	public void setFetchRowCount(int fetchRowCount) {
		this.fetchRowCount = fetchRowCount;
	}

	public W5Query getQuery() {
		return query;
	}

	public void setQuery(W5Query query) {
		this.query = query;
	}

	public long getResultRowCount() {
		return resultRowCount;
	}

	public void setResultRowCount(long resultRowCount) {
		this.resultRowCount = resultRowCount;
	}

	public long getStartRowNumber() {
		return startRowNumber;
	}

	public void setStartRowNumber(long startRowNumber) {
		this.startRowNumber = startRowNumber;
	}

	public int getQueryId() {
		return queryId;
	}

	public void setQueryId(int queryId) {
		this.queryId = queryId;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public void setRequestParams(Map<String,String> requestParams) {
		this.requestParams = requestParams;
	}

	public Map<String,String> getRequestParams() {
		return requestParams;
	}

	public List<Object> getSqlParams() {
		return sqlParams;
	}

	public void setSqlParams(List<Object> sqlParams) {
		this.sqlParams = sqlParams;
	}

	public boolean isHasPaging() {
		return hasPaging;
	}

	public void setHasPaging(boolean hasPaging) {
		this.hasPaging = hasPaging;
	}
    public boolean	prepareTreeQuery(Map<String, String> extraParams){ //connect by ile
    	switch(FrameworkSetting.rdbmsTip){
    	case	0:return prepareTreeQuery4Postgre(extraParams);
    	case	1:return prepareTreeQuery4SqlServer(extraParams);
    	}
    	return false;
    }

    private boolean	prepareTreeQuery4Postgre(Map<String, String> extraParams){ //connect by ile
    	//tabOrder<1000 olanlar WHERE'den sonra konulacak
    	//1000<=tabOrder<2000 olanlar START WITH'ten sonra konulacak
    	//2000<=tabOrder<3000 olanlar CONNECT BY PRIOR'dan sonra konulacak
    	//3000<=tabOrder olanlar hepsinin disina konulacak
		Map<String,String>	requestParams2;
		if(extraParams == null || extraParams.isEmpty()){
			requestParams2 = requestParams;
		}  else {
			requestParams2 = new HashMap<String,String>();
			requestParams2.putAll(requestParams);
			requestParams2.putAll(extraParams);
		}

		setSqlParams(new ArrayList<Object>());
    	
    	StringBuilder sqlParentWhere= new StringBuilder();//parent'in where
    	List sqlParentParams= new ArrayList<Object>();
    	StringBuilder sqlJoinOnWhere=new StringBuilder();//child's where
    	List sqlJoinOnParams= new ArrayList<Object>();
    	StringBuilder sqlRecrSelect=new StringBuilder();//en sondaki "select * from recr" den sonra gelen işlemler
    	List sqlRecrParams= new ArrayList<Object>();
    	
    	String sqlSelect = null;
    	List sqlSelectParams= new ArrayList<Object>();
   		if(query.getSqlSelect().contains("${")){
	   		Object[] oz = DBUtil.filterExt4SQL(query.getSqlSelect(), scd, requestParams2, null);
	   		sqlSelect = oz[0].toString();
			if(oz[1]!=null)sqlSelectParams.addAll((List)oz[1]);
   		} else sqlSelect = query.getSqlSelect();
   		
   		String sqlFrom = null;
   		if(query.getSqlFrom().contains("${")){
	   		Object[] oz = DBUtil.filterExt4SQL(query.getSqlFrom(), scd, requestParams2, null);
	   		sqlFrom = oz[0].toString();;
			if(oz[1]!=null)sqlSelectParams.addAll((List)oz[1]);
   		} else sqlFrom=query.getSqlFrom();

    	
		
		for(int tabOrderCount=0;tabOrderCount<4000;tabOrderCount+=1000){
	    	StringBuilder sqlWhere= new StringBuilder();
	    	List sqlParams= new ArrayList<Object>();
	    	
	    	if(!GenericUtil.isEmpty(getQuery().get_queryParams()))for(W5QueryParam p1 : getQuery().get_queryParams())if(p1.getTabOrder()>=tabOrderCount && p1.getTabOrder()<tabOrderCount+1000){
				String pexpressionDsc = p1.getExpressionDsc();
	    		if((p1.getOperatorType()==8 || p1.getOperatorType()==9) && p1.getSourceType()==1){
	    			String value = requestParams2.get(p1.getDsc()); 
	    			if(value!=null && value.length()>0){
		    			String[] pvalues = requestParams2.get(p1.getDsc()).split(",");
		    			if(pvalues.length>0){
		    				sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
							.append(pexpressionDsc)
							.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
							.append(" ( ");
							for(int	q1=0;q1<pvalues.length;q1++){
								sqlWhere.append(q1==0 ? "?": " ,?");
								sqlParams.add(GenericUtil.getObjectByTip(pvalues[q1], p1.getParamType()));
							}
							sqlWhere.append(" ) ) ");
		    			}
	    			}
	    		} else {
					Object psonuc = GenericUtil.prepareParam((W5Param)p1, getScd(), getRequestParams(), (short)-1, extraParams, (short)0, null, null, errorMap);
					
			
					if(errorMap.size()==0 && psonuc!=null){ // no error
						if(p1.getOperatorType()!=10){ // not custom operator
							sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
								.append(pexpressionDsc)
								.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
								.append("? ) ");
							if(p1.getOperatorType()>10)psonuc=psonuc + "%";
							if(p1.getOperatorType()==13)psonuc="%" + psonuc;
							sqlParams.add(psonuc);
		
						} else { //custom operator ise: ornegin "x.value_tip=? OR -1=?", kac adet ? varsa o kadar params a koyacaksin; eger pexpressionDsc numeric degerse o kadar koyacaksin
							if(GenericUtil.uInt(pexpressionDsc)!=0){
								for(int	q9=GenericUtil.uInt(pexpressionDsc);q9>0;q9--){
									sqlParams.add(psonuc);
								}
							} else if(pexpressionDsc.contains("?")){
								sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
								.append(pexpressionDsc)
								.append(" ) ");
								int q8=0;
								for(int	q9=0;q9<pexpressionDsc.length();q9++)if(pexpressionDsc.charAt(q9)=='?'){
									if(psonuc!=null && psonuc instanceof Object[]){ //eger aradarda veri gelirse
										sqlParams.add(((Object[])psonuc)[q8++]);
									} else
										sqlParams.add(psonuc);
								}
							} else if(pexpressionDsc.contains("${")){//usual ${req.xxx}
								Object[] oz = DBUtil.filterExt4SQL(pexpressionDsc, scd, requestParams2, null);
								if(sqlWhere.length()>0)sqlWhere.append(" AND ");
								sqlWhere.append("(").append(oz[0]).append(")");
								if(oz[1]!=null)sqlParams.addAll((List)oz[1]);							
							}
							
						}
					}
	    		}
					 
			}
			if(errorMap.size()>0)return false;
			
			switch(tabOrderCount){
			case	0://WHERE

			//	sql.append(" WHERE level<=").append(PromisCache.getAppSettingIntValue(scd, "max_tree_query_level")); //cok dalmamasi icin
				sqlParentWhere.append(sqlWhere);sqlParentParams.addAll(sqlParams);
				sqlJoinOnWhere.append(sqlWhere);sqlJoinOnParams.addAll(sqlParams);
				
				
				
				if(query.getSqlWhere()!=null){//parent icin
					if(query.getSqlWhere().contains("${")){
				   		Object[] oz = DBUtil.filterExt4SQL(query.getSqlWhere(), scd, requestParams2, null);
				   		sqlParentWhere.append(oz[0]);
						if(oz[1]!=null)sqlParentParams.addAll((List)oz[1]);
			   		} else sqlParentWhere.append(query.getSqlWhere());
				}
		   		setHasPaging(getFetchRowCount()!=0);
		   		break;
			case	1000://group by
				sqlJoinOnWhere.append(sqlWhere);sqlJoinOnParams.addAll(sqlParams);
				if(query.getSqlGroupby()!=null){//child icin
					Object[] oz = DBUtil.filterExt4SQL(query.getSqlGroupby(), scd, requestParams2, null);
					sqlJoinOnWhere.append(oz[0]);
					if(oz.length>1 && oz[1]!=null)sqlJoinOnParams.addAll((List)oz[1]);
				}
				
		//		sqlStartWith.append(sql);
				
				
				break;
			case	2000://order by
				if(sqlWhere.length()>0){
					sqlRecrSelect.append(" WHERE ").append(sqlWhere);
					sqlRecrParams.addAll(sqlParams);
				}
				
				if(query.getSqlOrderby()!=null){
					if(query.getSqlOrderby().contains("${")){
				   		Object[] oz = DBUtil.filterExt4SQL(query.getSqlOrderby(), scd, requestParams2, null);
				   		sqlRecrSelect.append(oz[0]);
						if(oz[1]!=null)sqlRecrParams.addAll((List)oz[1]);
			   		} else sqlRecrSelect.append(query.getSqlOrderby());					
				}
				
				break;
			}
		}
		
		if(sqlParentWhere.length()>0 && sqlJoinOnWhere.length()>0){
			StringBuilder sql=new StringBuilder();
			sql.append("WITH RECURSIVE recr AS ( SELECT ")
				.append(sqlSelect)
				.append(", 1 xlevel FROM ").append(sqlFrom)
				.append(sqlParentWhere.length()>0 ? " WHERE ":"").append(sqlParentWhere)
				.append(" UNION ALL SELECT ")
				.append(sqlSelect)
				.append(", xlevel+1 FROM ").append(sqlFrom).append(" JOIN recr ON ")
				.append(sqlJoinOnWhere)
				.append(") select * from recr ").append(sqlRecrSelect);
	   		setExecutedSql(sql.toString());
			sqlParams.clear();
			sqlParams.addAll(sqlSelectParams);
			sqlParams.addAll(sqlParentParams);
			sqlParams.addAll(sqlSelectParams);
			sqlParams.addAll(sqlJoinOnParams);
			sqlParams.addAll(sqlRecrParams);
		}
		if(mainTable!=null && FrameworkSetting.vcs && mainTable.getVcsFlag()!=0 && query.getQueryType()==9)postProcessQueryFields = new ArrayList();
	//	PromisUtil.replaceSql(sql.toString(), sqlParams)
		

    	return true;
    }
    
    private boolean	prepareTreeQuery4SqlServer(Map<String, String> extraParams){ //connect by ile
    	//tabOrder<1000 olanlar WHERE'den sonra konulacak
    	//1000<=tabOrder<2000 olanlar START WITH'ten sonra konulacak
    	//2000<=tabOrder<3000 olanlar CONNECT BY PRIOR'dan sonra konulacak
    	//3000<=tabOrder olanlar hepsinin disina konulacak
		Map<String,String>	requestParams2;
		if(extraParams == null || extraParams.isEmpty()){
			requestParams2 = requestParams;
		}  else {
			requestParams2 = new HashMap<String,String>();
			requestParams2.putAll(requestParams);
			requestParams2.putAll(extraParams);
		}

		setSqlParams(new ArrayList<Object>());
    	
    	StringBuilder sqlParentWhere= new StringBuilder();//parent'in where
    	List sqlParentParams= new ArrayList<Object>();
    	StringBuilder sqlJoinOnWhere=new StringBuilder();//child's where
    	List sqlJoinOnParams= new ArrayList<Object>();
    	StringBuilder sqlRecrSelect=new StringBuilder();//en sondaki "select * from recr" den sonra gelen işlemler
    	List sqlRecrParams= new ArrayList<Object>();
    	
    	String sqlSelect = null;
    	List sqlSelectParams= new ArrayList<Object>();
   		if(query.getSqlSelect().contains("${")){
	   		Object[] oz = DBUtil.filterExt4SQL(query.getSqlSelect(), scd, requestParams2, null);
	   		sqlSelect = oz[0].toString();
			if(oz[1]!=null)sqlSelectParams.addAll((List)oz[1]);
   		} else sqlSelect = query.getSqlSelect();
   		
   		String sqlFrom = null;
   		if(query.getSqlFrom().contains("${")){
	   		Object[] oz = DBUtil.filterExt4SQL(query.getSqlFrom(), scd, requestParams2, null);
	   		sqlFrom = oz[0].toString();;
			if(oz[1]!=null)sqlSelectParams.addAll((List)oz[1]);
   		} else sqlFrom=query.getSqlFrom();

    	
		
		for(int tabOrderCount=0;tabOrderCount<4000;tabOrderCount+=1000){
	    	StringBuilder sqlWhere= new StringBuilder();
	    	List sqlParams= new ArrayList<Object>();
	    	
	    	for(W5QueryParam p1 : getQuery().get_queryParams())if(p1.getTabOrder()>=tabOrderCount && p1.getTabOrder()<tabOrderCount+1000){
				String pexpressionDsc = p1.getExpressionDsc();
	    		if((p1.getOperatorType()==8 || p1.getOperatorType()==9) && p1.getSourceType()==1){
	    			String value = requestParams2.get(p1.getDsc()); 
	    			if(value!=null && value.length()>0){
		    			String[] pvalues = requestParams2.get(p1.getDsc()).split(",");
		    			if(pvalues.length>0){
		    				sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
							.append(pexpressionDsc)
							.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
							.append(" ( ");
							for(int	q1=0;q1<pvalues.length;q1++){
								sqlWhere.append(q1==0 ? "?": " ,?");
								sqlParams.add(GenericUtil.getObjectByTip(pvalues[q1], p1.getParamType()));
							}
							sqlWhere.append(" ) ) ");
		    			}
	    			}
	    		} else {
					Object psonuc = GenericUtil.prepareParam((W5Param)p1, getScd(), getRequestParams(), (short)-1, extraParams, (short)0, null, null, errorMap);
					
			
					if(errorMap.size()==0 && psonuc!=null){ // no error
						if(p1.getOperatorType()!=10){ // not custom operator
							sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
								.append(pexpressionDsc)
								.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
								.append("? ) ");
							if(p1.getOperatorType()>10)psonuc=psonuc + "%";
							if(p1.getOperatorType()==13)psonuc="%" + psonuc;
							sqlParams.add(psonuc);
		
						} else { //custom operator ise: ornegin "x.value_tip=? OR -1=?", kac adet ? varsa o kadar params a koyacaksin; eger pexpressionDsc numeric degerse o kadar koyacaksin
							if(GenericUtil.uInt(pexpressionDsc)!=0){
								for(int	q9=GenericUtil.uInt(pexpressionDsc);q9>0;q9--){
									sqlParams.add(psonuc);
								}
							} else if(pexpressionDsc.contains("?")){
								sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
								.append(pexpressionDsc)
								.append(" ) ");
								int q8=0;
								for(int	q9=0;q9<pexpressionDsc.length();q9++)if(pexpressionDsc.charAt(q9)=='?'){
									if(psonuc!=null && psonuc instanceof Object[]){ //eger aradarda veri gelirse
										sqlParams.add(((Object[])psonuc)[q8++]);
									} else
										sqlParams.add(psonuc);
								}
							} else if(pexpressionDsc.contains("${")){//usual ${req.xxx}
								Object[] oz = DBUtil.filterExt4SQL(pexpressionDsc, scd, requestParams2, null);
								if(sqlWhere.length()>0)sqlWhere.append(" AND ");
								sqlWhere.append("(").append(oz[0]).append(")");
								if(oz[1]!=null)sqlParams.addAll((List)oz[1]);							
							}
							
						}
					}
	    		}
					 
			}
			if(errorMap.size()>0)return false;
			
			switch(tabOrderCount){
			case	0://WHERE
			//	sql.append(" WHERE level<=").append(PromisCache.getAppSettingIntValue(scd, "max_tree_query_level")); //cok dalmamasi icin
				sqlParentWhere.append(sqlWhere);sqlParentParams.addAll(sqlParams);
				sqlJoinOnWhere.append(sqlWhere);sqlJoinOnParams.addAll(sqlParams);
				
				
				
				if(query.getSqlWhere()!=null){//parent icin
					if(query.getSqlWhere().contains("${")){
				   		Object[] oz = DBUtil.filterExt4SQL(query.getSqlWhere(), scd, requestParams2, null);
				   		sqlParentWhere.append(oz[0]);
						if(oz[1]!=null)sqlParentParams.addAll((List)oz[1]);
			   		} else sqlParentWhere.append(query.getSqlWhere());
				}
		   		setHasPaging(getFetchRowCount()!=0);
		   		break;
			case	1000://group by
				sqlJoinOnWhere.append(sqlWhere);sqlJoinOnParams.addAll(sqlParams);
				if(query.getSqlGroupby()!=null){//child icin
					Object[] oz = DBUtil.filterExt4SQL(query.getSqlGroupby(), scd, requestParams2, null);
					sqlJoinOnWhere.append(oz[0]);
					if(oz.length>1 && oz[1]!=null)sqlJoinOnParams.addAll((List)oz[1]);
				}
				
		//		sqlStartWith.append(sql);
				
				
				break;
			case	2000://order by
				if(sqlWhere.length()>0){
					sqlRecrSelect.append(" WHERE ").append(sqlWhere);
					sqlRecrParams.addAll(sqlParams);
				}
				
				if(query.getSqlOrderby()!=null){
					if(query.getSqlOrderby().contains("${")){
				   		Object[] oz = DBUtil.filterExt4SQL(query.getSqlOrderby(), scd, requestParams2, null);
				   		sqlRecrSelect.append(oz[0]);
						if(oz[1]!=null)sqlRecrParams.addAll((List)oz[1]);
			   		} else sqlRecrSelect.append(query.getSqlOrderby());					
				}
				
				break;
			}
		}
		
		if(sqlParentWhere.length()>0 && sqlJoinOnWhere.length()>0){
			StringBuilder sql=new StringBuilder();
			sql.append("WITH recr AS ( SELECT ")
				.append(sqlSelect)
				.append(", 1 xlevel FROM ").append(sqlFrom)
				.append(sqlParentWhere.length()>0 ? " WHERE ":"").append(sqlParentWhere)
				.append(" UNION ALL SELECT ")
				.append(sqlSelect)
				.append(", xlevel+1 FROM ").append(sqlFrom).append(" INNER JOIN recr ON ")
				.append(sqlJoinOnWhere)
				.append(") select * from recr ").append(sqlRecrSelect);
	   		setExecutedSql(sql.toString());
			sqlParams.clear();
			sqlParams.addAll(sqlSelectParams);
			sqlParams.addAll(sqlParentParams);
			sqlParams.addAll(sqlSelectParams);
			sqlParams.addAll(sqlJoinOnParams);
			sqlParams.addAll(sqlRecrParams);
		}
		if(mainTable!=null && FrameworkSetting.vcs && mainTable.getVcsFlag()!=0 && query.getQueryType()==9)postProcessQueryFields = new ArrayList();
	//	PromisUtil.replaceSql(sql.toString(), sqlParams)
		

    	return true;
    }
    
    public boolean	prepareQuery4Stats(int statType, int queryFieldId, int stackFieldId, String orderBy) throws JSONException{
    	W5QueryField gf = null, sf = null;
    	for(W5QueryField o:query.get_queryFields())if(o.getQueryFieldId()==queryFieldId){
    		gf=o;
    		break;
    	}
    	if(gf==null)return false;
    	prepareQuery(null);
    	if(!errorMap.isEmpty()) return false;
    	String sql = "select " + gf.getDsc() + " id, ";
    	if(statType!=0){
        	for(W5QueryField o:query.get_queryFields())if(o.getQueryFieldId()==statType){
        		sf=o;
        		sql += "sum("+o.getDsc()+")";
        		break;
        	}
        	if(sf==null)return false;
    	} else {
    		sql += "count(1)";
    	}
    	
    	sql+=" xres from (" + executedSql + ") mq group by id";
    	if(!GenericUtil.isEmpty(orderBy))sql+=" order by " + orderBy;
    	executedSql= sql;
    	
    	return true;
    }

    public boolean	prepareQuery(Map<String, String> extraParams){
    	if(query.getQuerySourceType()!=4658)switch(FrameworkSetting.rdbmsTip){
    	case	0:return prepareQuery4Postgre(extraParams);
    	case	1:return prepareQuery4SqlServer(extraParams);
    	} else {
    		prepareQuery4ExternalDb(extraParams);
    	}
    	return false;
    }
	
    private boolean	prepareQuery4Postgre(Map<String, String> extraParams){
    	
    	StringBuilder sql= new StringBuilder();
		setSqlParams(new ArrayList<Object>());
		Map<String,String>	requestParams2;
		if(GenericUtil.isEmpty(extraParams)){
			requestParams2 = requestParams;
		}  else {
			requestParams2 = new HashMap<String,String>();
			requestParams2.putAll(requestParams);
			requestParams2.putAll(extraParams);
		}

    	sql.append(" from ");
    	String sqlFrom = null;
    	if(query.getSqlFrom().contains("${")){
    		Object[] oz = DBUtil.filterExt4SQL(query.getSqlFrom(), scd, requestParams2, null);
    		sqlFrom = ((StringBuilder)oz[0]).toString();
			if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
    	} else sqlFrom=query.getSqlFrom();
    	
   		sql.append(sqlFrom);
    	
    	StringBuilder sqlWhere= new StringBuilder();
    	if(query.getSqlWhere()!=null){
    		if(query.getSqlWhere().contains("${")){
    			Object[] oz = DBUtil.filterExt4SQL(query.getSqlWhere(), scd, requestParams2, null);
    			sqlWhere.append(oz[0]);
    			if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
    		} else
    			sqlWhere.append(query.getSqlWhere());
    	}

		Locale xlocale = new Locale(FrameworkCache.getAppSettingStringValue(scd, "locale","en"));
		List<W5QueryParam> pqs = query.get_queryParams();
    	if(!GenericUtil.isEmpty(pqs))for(W5QueryParam p1 : pqs){
			String pexpressionDsc = p1.getExpressionDsc();
    		if((p1.getOperatorType()==8 || p1.getOperatorType()==9) && p1.getSourceType()==1){
    			String value = requestParams2.get(p1.getDsc()); 
    			if(value!=null && value.length()>0){
	    			String[] pvalues = requestParams2.get(p1.getDsc()).split(",");
	    			if(pvalues.length>0){
	    				sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
						.append(pexpressionDsc)
						.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
						.append(" ( ");
						for(int	q1=0;q1<pvalues.length;q1++){
							sqlWhere.append(q1==0 ? "?": " ,?");
							sqlParams.add(GenericUtil.getObjectByTip(pvalues[q1], p1.getParamType()));
						}
						sqlWhere.append(" ) ) ");
	    			}
    			}
    		} else {
				Object presult = GenericUtil.prepareParam((W5Param)p1, getScd(), getRequestParams(), (short)-1, extraParams, (short)0, null, null, errorMap);
		
				if(errorMap.isEmpty() && presult!=null){ // no error
					if(p1.getOperatorType()!=10){ // not custom operator
						sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
							.append(p1.getOperatorType()>10 ? FrameworkCache.getAppSettingStringValue(getScd(), "db_lower_fnc","lower")+"("+pexpressionDsc+")" : pexpressionDsc)
							.append(FrameworkSetting.operatorMap[p1.getOperatorType()]);
						if(p1.getOperatorType()>10)
							sqlWhere.append(FrameworkCache.getAppSettingStringValue(getScd(), "db_lower_fnc","lower")).append("(?)");
						else sqlWhere.append(" ?");
						sqlWhere.append(") ");
//						if(p1.getOperatorTip()>10)psonuc=((String)psonuc).toLowerCase(xlocale) + "%";
						if(p1.getOperatorType()>10)presult+="%";
						if(p1.getOperatorType()==13)presult="%" + presult;
						sqlParams.add(presult);
	
					} else { //custom operator ise: ornegin "x.value_tip=? OR -1=?", kac adet ? varsa o kadar params a koyacaksin; eger pexpressionDsc numeric degerse o kadar koyacaksin
						if(GenericUtil.uInt(pexpressionDsc)!=0){
							for(int	q9=GenericUtil.uInt(pexpressionDsc);q9>0;q9--){
								sqlParams.add(presult);
							}
						} else if(pexpressionDsc.contains("?")){
							sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ");							
							int q8=0;
							for(int	q9=0;q9<pexpressionDsc.length();q9++)if(pexpressionDsc.charAt(q9)=='?'){
								if(presult!=null && presult instanceof Object[]){ //eger aradarda veri gelirse
									sqlParams.add(((Object[])presult)[q8++]);
								} else
									sqlParams.add(presult);
							}
							if(pexpressionDsc.contains("${")){//? işaretlerinden sonra başka bir parametre varsa
								Object[] oz = DBUtil.filterExt4SQL(pexpressionDsc, scd, requestParams2, null);
								if(oz[0]!=null)pexpressionDsc = oz[0].toString();
								if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
							}
							sqlWhere.append(pexpressionDsc).append(" ) ");
						} else if(pexpressionDsc.contains("${")){//usual ${req.xxx}
							Object[] oz = DBUtil.filterExt4SQL(pexpressionDsc, scd, requestParams2, null);
							if(sqlWhere.length()>0)sqlWhere.append(" AND ");
							sqlWhere.append("(").append(oz[0]).append(")");
							if(oz[1]!=null)sqlParams.addAll((List)oz[1]);							
						} else {//napak inanak mi kanka
							sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
							.append(pexpressionDsc)
							.append(" ) ");
						}
						
					}
				}
    		}
				 
		}



    	
    	if(errorMap.isEmpty() && requestParams2!=null)for(String k:requestParams2.keySet())if(k.startsWith("filter[")&&k.endsWith("]")){ //for grid column filters
    		Object paramValue = requestParams2.get(k);
    		if(paramValue==null || paramValue.toString().length()==0)continue;
    		String[] filter = k.substring("filter[".length(), k.length()-1).replace('.', ',').split(",");
    		String paramName = filter[0];
    		int operator = filter.length>1 ? GenericUtil.uInt(filter[1]):0;
    		for(W5QueryField qf:query.get_queryFields())if(qf.getDsc().equals(paramName)) {
    			sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ");	
    			switch(qf.getFieldType()){
	    		case	3:case	4://integer, double
	    			sqlWhere.append(paramName).append(FrameworkSetting.operatorMap[operator]);
	    			if(operator ==8 || operator==9) {// in, not in
	    				String[] values = paramValue.toString().split(",");
	    				sqlWhere.append(" ( ");
	    				for(String v:values) {
	    					sqlWhere.append("?,");
	    					sqlParams.add(qf.getFieldType()==3 ? GenericUtil.uInt(v) : GenericUtil.uDouble(v.toString()) );
	    				}
	    				sqlWhere.setLength(sqlWhere.length()-1);
	    				sqlWhere.append(" ) ");
	    			} else {
		    			sqlWhere.append(" ? ");
		    			sqlParams.add(qf.getFieldType()==3 ? GenericUtil.uInt(paramValue) : GenericUtil.uDouble(paramValue.toString()) );
	    			}
	    			break;
	    		case	2://date
	    			Object dt = GenericUtil.uDate(paramValue.toString(), scd!=null ? GenericUtil.uInt(scd.get("date_format")):0);
	    			sqlWhere.append(paramName).append(FrameworkSetting.operatorMap[operator]).append(" ?  ");
	    			sqlParams.add(dt);
	    			break;
	    		case	5://boolean
	    			Object bl = GenericUtil.uCheckBox(paramValue.toString());
	    			sqlWhere.append(paramName).append(FrameworkSetting.operatorMap[operator]).append(" ?  ");
	    			sqlParams.add(bl);
	    			break;
	    		case 1://string
	    			if(qf.getPostProcessType()!=11 && qf.getPostProcessType()!=13){//multi degilse
	    				sqlWhere.append(FrameworkCache.getAppSettingStringValue(getScd(), "db_lower_fnc","lower")).append("(").append(paramName).append(") like ?  ");
	    				sqlParams.add((paramValue.toString()).toLowerCase(xlocale) + "%");
	    			} else {
	    				sqlWhere.append("position(','||?||',' in ','||coalesce(").append(paramName).append(",'-')||',')>0 ");
	    				sqlParams.add(paramValue);    				
	    			}
	    			break;
	    		default:
	    			sqlWhere.append("1=1");
	    		}
    			sqlWhere.append(" ) ");	
	    	}
    		
    	}

    	
		if(errorMap.size()>0)return false;


		//icindeki gorulme sadece su fieldlar tarafindan yapilacak
		if(mainTable!=null && mainTable.getAccessViewTip()!=0 && !GenericUtil.isEmpty(mainTable.getAccessViewUserFields()) 
				&& (mainTable.getAccessViewRoles()==null || !GenericUtil.hasPartInside(mainTable.getAccessViewRoles(), scd.get("roleId").toString()))
				&& (mainTable.getAccessViewUsers()==null || !GenericUtil.hasPartInside(mainTable.getAccessViewUsers(), scd.get("userId").toString()))
		){
			String[] fieldIdz = mainTable.getAccessViewUserFields().split(",");
			sqlWhere.append(sqlWhere.length()>0 ? " AND (":" ("); 
			boolean bq=false;
			for(String s:fieldIdz){
				if(s.charAt(0)=='*'){
/*					int accessConditionSqlId = GenericUtil.uInt(s.substring(1));
					W5TableAccessConditionSql accessConditionSql = FrameworkCache.wAccessConditionSqlMap.get(accessConditionSqlId);
					if(accessConditionSql!=null){
						Object[] oz = DBUtil.filterExt4SQL(accessConditionSql.getConditionCode(), scd, requestParams2, null);
			    		sqlFrom = ((StringBuilder)oz[0]).toString();
						if(bq)sqlWhere.append(" OR ");else bq=true;
						sqlWhere.append(oz[0]);
						if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
					}*/
					continue;
				}
				if(s.charAt(0)=='!'){
					/*int accessConditionSqlId = GenericUtil.uInt(s.substring(1));
					W5TableAccessConditionSql accessConditionSql = FrameworkCache.wAccessConditionSqlMap.get(accessConditionSqlId);
					if(accessConditionSql!=null){
						W5Table t2 = FrameworkCache.getTable(scd, accessConditionSql.getTableId());
						if(t2!=null && !GenericUtil.isEmpty(t2.get_tableChildList())){
							for(W5TableChild tc:t2.get_tableChildList())if(tc.getRelatedTableId()==mainTable.getTableId()){
								StringBuilder sql2 = new StringBuilder();
								if(tc.getRelatedStaticTableFieldId()>0){
									sql2.append("(x.").append(mainTable.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal()).append(" AND ");
								}
								sql2.append("exists(select 1 from ").append(t2.getDsc()).append(" hq where hq.customization_id=${scd.customizationId} AND ").append(accessConditionSql.getConditionCode().replace("x.", "hq.")).append(" AND hq.")
								.append(t2.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=x.").append(mainTable.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc());
								if(tc.getRelatedStaticTableFieldId()>0){
									sql2.append(")");
								}
								sql2.append(")");
								
								Object[] oz = DBUtil.filterExt4SQL(sql2.toString(), scd, requestParams2, null);
					    		sqlFrom = ((StringBuilder)oz[0]).toString();
								if(bq)sqlWhere.append(" OR ");else bq=true;
								sqlWhere.append(oz[0]);
								if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
								break;
							}
						}
					}*/
					continue;
				}
				int tableFieldId = GenericUtil.uInt(s);
				boolean hrc = false;
				if(tableFieldId<0){
					hrc = true;
					tableFieldId=-tableFieldId;
				}
				W5TableField tf = mainTable.get_tableFieldMap().get(tableFieldId);
				if(tf!=null){
					if(bq)sqlWhere.append(" OR ");else bq=true;					
					if(hrc){
						sqlWhere.append("exists(select 1 from iwb.w5_user_hrc_map hq where hq.customization_id=? AND hq.user_id=? AND hq.parent_user_id=x.").append(tf.getDsc()).append(")");
						sqlParams.add(scd.get("customizationId"));
					} else {
						sqlWhere.append("x.").append(tf.getDsc()).append("=?");
					}
					sqlParams.add(scd.get("userId"));

				}else {
/*					Integer tbId = FrameworkCache.wTableFieldMap.get(tableFieldId);//TODO
					if(tbId!=null){
						W5Table t2 = FrameworkCache.getTable(mainTable.getCustomizationId(), tbId);
						if(t2!=null && !GenericUtil.isEmpty(t2.get_tableChildList())){
							W5TableField tf2 = t2.get_tableFieldMap().get(tableFieldId);
							for(W5TableChild tc:t2.get_tableChildList())if(tc.getRelatedTableId()==mainTable.getTableId()){
								if(bq)sqlWhere.append(" OR ");else bq=true;
								if(tc.getRelatedStaticTableFieldId()>0){
									sqlWhere.append("(x.").append(mainTable.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal()).append(" AND ");
								}
								sqlWhere.append("exists(select 1 from ").append(t2.getDsc()).append(" hq where hq.customization_id=? AND hq.").append(tf2.getDsc()).append("=?").append(" AND hq.")
								.append(t2.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=x.").append(mainTable.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc());
								if(tc.getRelatedStaticTableFieldId()>0){
									sqlWhere.append(")");
								}
								sqlWhere.append(")");
								sqlParams.add(scd.get("customizationId"));
								sqlParams.add(scd.get("userId"));
								break;
							}
						}
					}*/
				}
			}
			sqlWhere.append(")");
		}
		
		if(mainTable!=null){
			String pkField = mainTable.get_tableFieldList().get(0).getDsc();
			
			
			//workflow row based security
			if(FrameworkSetting.workflow && mainTable.get_hasApprovalViewControlFlag()!=0){
				if(sqlWhere.length()>0)sqlWhere.append(" AND");
				sqlWhere.append(" (not exists(select 1 from iwb.w5_approval_record cx where cx.finished_flag=0 AND cx.table_id=")
					.append(query.getSourceObjectId()).append(" AND cx.project_uuid=? AND cx.table_pk=x.").append(pkField)
					.append(") OR exists(select 1 from iwb.w5_approval_record cx where cx.finished_flag=0 AND cx.table_id=")
					.append(query.getSourceObjectId()).append(" AND cx.project_uuid=? AND cx.table_pk=x.").append(pkField)
					.append(" AND (cx.access_view_tip=0 OR (position(','||?||',' in ','||coalesce(cx.access_view_roles,'-')||',')>0 OR position(','||?||',' in ','||coalesce(cx.access_view_users,'-')||',')>0 )))) ");
				
				sqlParams.add(scd.get("projectId"));
				sqlParams.add(scd.get("projectId"));
				sqlParams.add(scd.get("roleId"));
				sqlParams.add(scd.get("userId"));
			}
			
			//approval_status var mi?
			if(requestParams2!=null && mainTable.get_approvalMap()!=null && !mainTable.get_approvalMap().isEmpty()){ //simdilik manuel4 nakit akis
				String approvalStepIds = mainTable.get_approvalMap().get((short)1)!=null ? requestParams2.get("_approval_step_ids1") : null;//edit icin
				if((approvalStepIds==null || approvalStepIds.length()==0) && mainTable.get_approvalMap().get((short)2)!=null)approvalStepIds = requestParams2.get("_approval_step_ids2");//insert icin
				if((approvalStepIds==null || approvalStepIds.length()==0) && mainTable.get_approvalMap().get((short)3)!=null)approvalStepIds = requestParams2.get("_approval_step_ids3");//delete icin
				if(approvalStepIds!=null && approvalStepIds.length()>0){
					if(sqlWhere.length()>0)sqlWhere.append(" AND");
					sqlWhere.append(" exists(select 1 from iwb.w5_approval_record rz, (select * from iwb.tool_parse_numbers(?,',')) t where rz.project_uuid=? AND rz.table_id=? AND rz.table_pk=x."+query.get_queryFields().get(0).getDsc()+" AND t.satir::integer=rz.approval_step_id) ");
					sqlParams.add(approvalStepIds);
					sqlParams.add(scd.get("projectId"));
					sqlParams.add(query.getSourceObjectId());
				}
			}
			

			
		}

		
		if(sqlWhere.length()>0)sql.append(" where ").append(sqlWhere);
   		setHasPaging(getFetchRowCount()!=0);
   		if(!GenericUtil.isEmpty(query.getSqlGroupby())){
   			if(query.getSqlSelect().contains("${")){
   				if(query.getSqlGroupby()!=null && query.getSqlGroupby().length()>0){
   		   			sql.append(" group by ");
   					Object[] oz = DBUtil.filterExt4SQL(query.getSqlGroupby(), scd, requestParams2, null);
   					sql.append(oz[0]);
   					if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
   		   		}		
   			}else{
   				sql.append(" group by ").append(query.getSqlGroupby());
   			}    		
		}    	   	
    	
   		StringBuilder s = new StringBuilder("select ");
   		String sqlSelect = query.getSqlSelect();

   		
   		
   		if(GenericUtil.isEmpty(query.getSqlGroupby())){
   			executedSqlFromParams = new ArrayList();
   			executedSqlFromParams.addAll(sqlParams);
   		}
   		
   		if(sqlSelect.contains("${")){
	   		Object[] oz = DBUtil.filterExt4SQL(sqlSelect, scd, requestParams2, null);
			s.append(oz[0]);
			if(oz[1]!=null){
				sqlParams.addAll(0,(List)oz[1]);
			}
   		} else {
   			s.append(sqlSelect);
   		}


   		//grid, tree querylerde sadece
   		if((query.getQueryType()==1 || query.getQueryType()==9 || query.getQueryType()==10)&& query.getSqlGroupby()==null && mainTable!=null){
   			postProcessQueryFields = new ArrayList();
			s.append(", x.").append(mainTable.get_tableFieldList().get(0).getDsc()).append(" pkpkpk_id");
   		}//bu heryerde gerekebilir. simdilik accessRecordControl icin, fakat sonra yorum, file attachment vs.vs.
   		if(!GenericUtil.isEmpty(query.getSqlGroupby())){
   			s.append(sql);
   		} else {
   			executedSqlFrom = sql.toString();
   			s.append(sql);
//   			s=sql;
   		}


		
		if(getOrderBy()!=null && getOrderBy().length()>0){
   			s.append(" order by ");
   			String strOrder = null;
   			if(mainTable!=null && getOrderBy().equals(FieldDefinitions.queryFieldName_Comment) && FrameworkCache.getAppSettingIntValue(scd, "make_comment_summary_flag")!=0){
   				//TODO
/*	   				if (FrameworkCache.getTable(scd, FrameworkSetting.customCommentTableId)==null)
	   					strOrder = "coalesce((select qz.last_comment_id from iwb.w5_comment_summary qz where qz.table_pk=x."+mainTable.get_tableFieldList().get(0).getDsc()+" AND qz.table_id="+mainTable.getTableId()+" AND qz.project_uuid='${scd.projectId}'),0) DESC";
	   				else
	   					strOrder = "coalesce((select qz.last_comment_id from iwb.w5_comment_summary qz where qz.table_pk=x."+mainTable.get_tableFieldList().get(0).getDsc()+" AND qz.table_id="+mainTable.getTableId()+" AND qz.project_uuid='${scd.projectId}'),0) DESC";
*/	   					
   			} else {
   				strOrder = getOrderBy();
   			}
   			Object[] oz = DBUtil.filterExt4SQL(strOrder, scd, requestParams2, null);
			s.append(oz[0]);
			if(oz[1]!=null){
			   		sqlParams.addAll((List)oz[1]);
				
			}
   		}
   		

   		setExecutedSql(s.toString());

    	return true;
    }

    private boolean	prepareQuery4SqlServer(Map<String, String> extraParams){
    	
    	StringBuilder sql= new StringBuilder();
		setSqlParams(new ArrayList<Object>());
		Map<String,String>	requestParams2;
		if(extraParams == null || extraParams.isEmpty()){
			requestParams2 = requestParams;
		}  else {
			requestParams2 = new HashMap<String,String>();
			requestParams2.putAll(requestParams);
			requestParams2.putAll(extraParams);
		}
    	W5Query query = getQuery();
    	sql.append(" from ");
    	String sqlFrom = null;
    	if(query.getSqlFrom().contains("${")){
    		Object[] oz = DBUtil.filterExt4SQL(query.getSqlFrom(), scd, requestParams2, null);
    		sqlFrom = ((StringBuilder)oz[0]).toString();
			if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
    	} else sqlFrom=query.getSqlFrom();
    	
   		sql.append(sqlFrom);
    	
    	StringBuilder sqlWhere= new StringBuilder();
    	if(query.getSqlWhere()!=null){
    		if(query.getSqlWhere().contains("${")){
    			Object[] oz = DBUtil.filterExt4SQL(query.getSqlWhere(), scd, requestParams2, null);
    			sqlWhere.append(oz[0]);
    			if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
    		} else
    			sqlWhere.append(query.getSqlWhere());
    	}

		Locale xlocale = new Locale(FrameworkCache.getAppSettingStringValue(scd, "locale","en"));
		List<W5QueryParam> pqs = null;
		pqs=getQuery().get_queryParams();
    	for(W5QueryParam p1 : pqs){
			String pexpressionDsc = p1.getExpressionDsc();
    		if((p1.getOperatorType()==8 || p1.getOperatorType()==9) && p1.getSourceType()==1){
    			String value = requestParams2.get(p1.getDsc()); 
    			if(value!=null && value.length()>0){
	    			String[] pvalues = requestParams2.get(p1.getDsc()).split(",");
	    			if(pvalues.length>0){
	    				sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
						.append(pexpressionDsc)
						.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
						.append(" ( ");
						for(int	q1=0;q1<pvalues.length;q1++){
							sqlWhere.append(q1==0 ? "?": " ,?");
							sqlParams.add(GenericUtil.getObjectByTip(pvalues[q1], p1.getParamType()));
						}
						sqlWhere.append(" ) ) ");
	    			}
    			}
    		} else {
				Object psonuc = GenericUtil.prepareParam((W5Param)p1, getScd(), getRequestParams(), (short)-1, extraParams, (short)0, null, null, errorMap);
		
				if(errorMap.size()==0 && psonuc!=null)switch(p1.getOperatorType()){ // no error
				case	99:break;
				case 10: //custom operator ise: ornegin "x.value_tip=? OR -1=?", kac adet ? varsa o kadar params a koyacaksin; eger pexpressionDsc numeric degerse o kadar koyacaksin
					if(GenericUtil.uInt(pexpressionDsc)!=0){
						for(int	q9=GenericUtil.uInt(pexpressionDsc);q9>0;q9--){
							sqlParams.add(psonuc);
						}
					} else if(pexpressionDsc.contains("?")){
						sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ");							
						int q8=0;
						for(int	q9=0;q9<pexpressionDsc.length();q9++)if(pexpressionDsc.charAt(q9)=='?'){
							if(psonuc!=null && psonuc instanceof Object[]){ //eger aradarda veri gelirse
								sqlParams.add(((Object[])psonuc)[q8++]);
							} else
								sqlParams.add(psonuc);
						}
						if(pexpressionDsc.contains("${")){//? işaretlerinden sonra başka bir parametre varsa
							Object[] oz = DBUtil.filterExt4SQL(pexpressionDsc, scd, requestParams2, null);
							if(oz[0]!=null)pexpressionDsc = oz[0].toString();
							if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
						}
						sqlWhere.append(pexpressionDsc).append(" ) ");
					} else if(pexpressionDsc.contains("${")){//usual ${req.xxx}
						Object[] oz = DBUtil.filterExt4SQL(pexpressionDsc, scd, requestParams2, null);
						if(sqlWhere.length()>0)sqlWhere.append(" AND ");
						sqlWhere.append("(").append(oz[0]).append(")");
						if(oz[1]!=null)sqlParams.addAll((List)oz[1]);							
					} else {//napak inanak mi kanka
						sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
						.append(pexpressionDsc)
						.append(" ) ");
					}
					break;
				default: // not custom operator
					sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
						.append(p1.getOperatorType()>10 ? FrameworkCache.getAppSettingStringValue(getScd(), "db_lower_fnc","lower")+"("+pexpressionDsc+")" : pexpressionDsc)
						.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
						.append("? ) ");
					if(p1.getOperatorType()>10)psonuc=((String)psonuc).toLowerCase(xlocale) + "%";
					if(p1.getOperatorType()==13)psonuc="%" + psonuc;
					sqlParams.add(psonuc);

						
				}
    		}
				 
		}
    /*	TODO: simdilik degil
    	if(PromisSetting.cloud && mainTable!=null && mainTable.get_tableParamList().size()>1 && mainTable.get_tableParamList().get(1).getDsc().equals("customizationId") && sqlWhere.indexOf("x.customization_id=")==-1){
    		if(sqlWhere.length()>0)sqlWhere.append(" AND ");
    		sqlWhere.append(" x.customization_id=? ");
    		sqlParams.add(scd.get("customizationId"));
    	}
*/

    	
		if(errorMap.size()>0)return false;


		//icindeki gorulme sadece su fieldlar tarafindan yapilacak
		if(mainTable!=null && mainTable.getAccessViewTip()!=0 && !GenericUtil.isEmpty(mainTable.getAccessViewUserFields()) 
				&& (mainTable.getAccessViewRoles()==null || !GenericUtil.hasPartInside(mainTable.getAccessViewRoles(), scd.get("roleId").toString()))
				&& (mainTable.getAccessViewUsers()==null || !GenericUtil.hasPartInside(mainTable.getAccessViewUsers(), scd.get("userId").toString()))
		){
			String[] fieldIdz = mainTable.getAccessViewUserFields().split(",");
			sqlWhere.append(sqlWhere.length()>0 ? " AND (":" ("); 
			boolean bq=false;
			for(String s:fieldIdz){
				if(s.charAt(0)=='*'){
					/*int accessConditionSqlId = GenericUtil.uInt(s.substring(1));
					W5TableAccessConditionSql accessConditionSql = FrameworkCache.wAccessConditionSqlMap.get(accessConditionSqlId);
					if(accessConditionSql!=null){
						Object[] oz = DBUtil.filterExt4SQL(accessConditionSql.getConditionCode(), scd, requestParams2, null);
			    		sqlFrom = ((StringBuilder)oz[0]).toString();
						if(bq)sqlWhere.append(" OR ");else bq=true;
						sqlWhere.append(oz[0]);
						if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
					}*/
					continue;
				}
				if(s.charAt(0)=='!'){
					/*int accessConditionSqlId = GenericUtil.uInt(s.substring(1));
					W5TableAccessConditionSql accessConditionSql = FrameworkCache.wAccessConditionSqlMap.get(accessConditionSqlId);
					if(accessConditionSql!=null){
						W5Table t2 = FrameworkCache.getTable(scd, accessConditionSql.getTableId());
						if(t2!=null && !GenericUtil.isEmpty(t2.get_tableChildList())){
							for(W5TableChild tc:t2.get_tableChildList())if(tc.getRelatedTableId()==mainTable.getTableId()){
								StringBuilder sql2 = new StringBuilder();
								if(tc.getRelatedStaticTableFieldId()>0){
									sql2.append("(x.").append(mainTable.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal()).append(" AND ");
								}
								sql2.append("exists(select 1 from ").append(t2.getDsc()).append(" hq where hq.customization_id=${scd.customizationId} AND ").append(accessConditionSql.getConditionCode().replace("x.", "hq.")).append(" AND hq.")
								.append(t2.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=x.").append(mainTable.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc());
								if(tc.getRelatedStaticTableFieldId()>0){
									sql2.append(")");
								}
								sql2.append(")");
								
								Object[] oz = DBUtil.filterExt4SQL(sql2.toString(), scd, requestParams2, null);
					    		sqlFrom = ((StringBuilder)oz[0]).toString();
								if(bq)sqlWhere.append(" OR ");else bq=true;
								sqlWhere.append(oz[0]);
								if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
								break;
							}
						}
					}*/
					continue;
				}
				int tableFieldId = GenericUtil.uInt(s);
				boolean hrc = false;
				if(tableFieldId<0){
					hrc = true;
					tableFieldId=-tableFieldId;
				}
				W5TableField tf = mainTable.get_tableFieldMap().get(tableFieldId);
				if(tf!=null){
					if(bq)sqlWhere.append(" OR ");else bq=true;					
					if(hrc){
						sqlWhere.append("exists(select 1 from iwb.w5_user_hrc_map hq where hq.customization_id=? AND hq.user_id=? AND hq.parent_user_id=x.").append(tf.getDsc()).append(")");
						sqlParams.add(scd.get("customizationId"));
					} else {
						sqlWhere.append("x.").append(tf.getDsc()).append("=?");
					}
					sqlParams.add(scd.get("userId"));

				}else {//TODO
/*					Integer tbId = FrameworkCache.wTableFieldMap.get(tableFieldId);
					if(tbId!=null){
						W5Table t2 = FrameworkCache.getTable(mainTable.getCustomizationId(), tbId);
						if(t2!=null && !GenericUtil.isEmpty(t2.get_tableChildList())){
							W5TableField tf2 = t2.get_tableFieldMap().get(tableFieldId);
							for(W5TableChild tc:t2.get_tableChildList())if(tc.getRelatedTableId()==mainTable.getTableId()){
								if(bq)sqlWhere.append(" OR ");else bq=true;
								if(tc.getRelatedStaticTableFieldId()>0){
									sqlWhere.append("(x.").append(mainTable.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal()).append(" AND ");
								}
								sqlWhere.append("exists(select 1 from ").append(t2.getDsc()).append(" hq where hq.customization_id=? AND hq.").append(tf2.getDsc()).append("=?").append(" AND hq.")
								.append(t2.get_tableFieldMap().get(tc.getTableFieldId()).getDsc()).append("=x.").append(mainTable.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc());
								if(tc.getRelatedStaticTableFieldId()>0){
									sqlWhere.append(")");
								}
								sqlWhere.append(")");
								sqlParams.add(scd.get("customizationId"));
								sqlParams.add(scd.get("userId"));
								break;
							}
						}
					} */
				}
			}
			sqlWhere.append(")");
		}
		
		if(mainTable!=null){
			String pkField = mainTable.get_tableFieldList().get(0).getDsc();

			
			//workflow based row security
			if(FrameworkSetting.workflow && mainTable.get_hasApprovalViewControlFlag()!=0){
				if(sqlWhere.length()>0)sqlWhere.append(" AND");
				sqlWhere.append(" (not exists(select 1 from iwb.w5_approval_record cx where cx.finished_flag=0 AND cx.table_id=")
					.append(query.getSourceObjectId()).append(" AND cx.customization_id=? AND cx.table_pk=x.").append(pkField)
					.append(") OR exists(select 1 from iwb.w5_approval_record cx where cx.finished_flag=0 AND cx.table_id=")
					.append(query.getSourceObjectId()).append(" AND cx.customization_id=? AND cx.table_pk=x.").append(pkField)
					.append(" AND (cx.access_view_tip=0 OR (charindex(','||str(?)||',' , ','||coalesce(cx.access_view_roles,'-')||',')>0 OR charindex(','||str(?)||',' , ','||coalesce(cx.access_view_users,'-')||',')>0 )))) ");
				
				sqlParams.add(scd.get("customizationId"));
				sqlParams.add(scd.get("customizationId"));
				sqlParams.add(scd.get("roleId"));
				sqlParams.add(scd.get("userId"));
			}
			
			//approval_status var mi?
			if(requestParams2!=null && mainTable.get_approvalMap()!=null && !mainTable.get_approvalMap().isEmpty()){ //simdilik manuel4 nakit akis
				String approvalStepIds = mainTable.get_approvalMap().get((short)1)!=null ? requestParams2.get("_approval_step_ids1") : null;//edit icin
				if((approvalStepIds==null || approvalStepIds.length()==0) && mainTable.get_approvalMap().get((short)2)!=null)approvalStepIds = requestParams2.get("_approval_step_ids2");//insert icin
				if((approvalStepIds==null || approvalStepIds.length()==0) && mainTable.get_approvalMap().get((short)3)!=null)approvalStepIds = requestParams2.get("_approval_step_ids3");//delete icin
				if(approvalStepIds!=null && approvalStepIds.length()>0){
					if(sqlWhere.length()>0)sqlWhere.append(" AND");
					sqlWhere.append(" exists(select 1 from iwb.w5_approval_record rz, (select * from dbo.tool_parse_numbers(?,',')) t where rz.customization_id=? AND rz.table_id=? AND rz.table_pk=x."+query.get_queryFields().get(0).getDsc()+" AND t.satir::integer=rz.approval_step_id) ");
					sqlParams.add(approvalStepIds);
					sqlParams.add(scd.get("customizationId"));
					sqlParams.add(query.getSourceObjectId());
				}
			}
			
			

		}

		
		if(sqlWhere.length()>0)sql.append(" where ").append(sqlWhere);
   		setHasPaging(getFetchRowCount()!=0);
   		if(!GenericUtil.isEmpty(query.getSqlGroupby())){
   			if(query.getSqlSelect().contains("${")){
   				if(query.getSqlGroupby()!=null && query.getSqlGroupby().length()>0){
   		   			sql.append(" group by ");
   					Object[] oz = DBUtil.filterExt4SQL(query.getSqlGroupby(), scd, requestParams2, null);
   					sql.append(oz[0]);
   					if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
   		   		}		
   			}else{
   				sql.append(" group by ").append(query.getSqlGroupby());
   			}    		
		}    	   	
    	
   		StringBuilder s = new StringBuilder("select ");
   		String sqlSelect = query.getSqlSelect();

   	
   		
   		if(GenericUtil.isEmpty(query.getSqlGroupby())){
   			executedSqlFromParams = new ArrayList();
   			executedSqlFromParams.addAll(sqlParams);
   		}
   		
   		if(sqlSelect.contains("${")){
	   		Object[] oz = DBUtil.filterExt4SQL(sqlSelect, scd, requestParams2, null);
			s.append(oz[0]);
			if(oz[1]!=null){
				sqlParams.addAll(0,(List)oz[1]);
			}
   		} else {
   			s.append(sqlSelect);
   		}


   		//grid, tree querylerde sadece
   		if((query.getQueryType()==1 || query.getQueryType()==9 || query.getQueryType()==10)&& query.getSqlGroupby()==null && mainTable!=null){
   			postProcessQueryFields = new ArrayList();
			s.append(", x.").append(mainTable.get_tableFieldList().get(0).getDsc()).append(" pkpkpk_id");
   		}//bu heryerde gerekebilir. simdilik accessRecordControl icin, fakat sonra yorum, file attachment vs.vs.
   		if(!GenericUtil.isEmpty(query.getSqlGroupby())){
   			s.append(sql);
   		} else {
   			executedSqlFrom = sql.toString();
   			s.append(sql);
//   			s=sql;
   		}


   			
		if(false && getOrderBy()!=null && getOrderBy().length()>0){
   			s.append(" order by ");
   			String strOrder = getOrderBy();
   			Object[] oz = DBUtil.filterExt4SQL(strOrder, scd, requestParams2, null);
			s.append(oz[0]);
			if(oz[1]!=null){
			   		sqlParams.addAll((List)oz[1]);
				
			}
   		}
	

   		setExecutedSql(s.toString());

    	return true;
    }


    
    public	Map<String, Object> toSingleRecordMap(Object[] o){
    	Map<String, Object> m = new HashMap<String, Object>();
    	for(W5QueryField f : getQuery().get_queryFields()){
			Object obj = o[f.getTabOrder()-1];
    		if(obj!=null){
    			if (obj instanceof java.sql.Timestamp) {
    				try{ 
    					m.put(f.getDsc(), GenericUtil.uFormatDateTime((java.sql.Timestamp) obj));
    				}catch (Exception e) {}
				} else if (obj instanceof java.sql.Date) {
    				try{ 
    					m.put(f.getDsc(), GenericUtil.uFormatDate((java.sql.Date) obj));
    				}catch (Exception e) {}
				} //else if (obj instanceof oracle.sql.CLOB) {
						//oracle.sql.CLOB new_obj = (oracle.sql.CLOB) obj;
        			//	try{ 
        			//		m.put(f.getDsc(), PromisUtil.stringToJS(new_obj.getSubString((int)1, (int)new_obj.length())));
        			//	}catch (Exception e) {}
				//} 
				else 
					m.put(f.getDsc(), GenericUtil.stringToJS(obj.toString()));
    		}
    	}
    	return	m;
    }
    
	public Map<String, Object> getScd() {
		return scd;
	}



	public void setScd(Map<String, Object> scd) {
		this.scd = scd;
	}

	public int getProcessTime() {
		return processTime;
	}

	public void setProcessTime(int processTime) {
		this.processTime = processTime;
	}

	public boolean isDisplayInfo() {
		return displayInfo;
	}

	public void setDisplayInfo(boolean displayInfo) {
		this.displayInfo = displayInfo;
	}

/*	public List<W5TableParam> getExtendedTablePkFields() {
		return extendedTablePkFields;
	}

	public void setExtendedTablePkFields(List<W5TableParam> extendedTablePkFields) {
		this.extendedTablePkFields = extendedTablePkFields;
	}*/

	public List<W5QueryField> getPostProcessQueryFields() {
		return postProcessQueryFields;
	}

	public void setPostProcessQueryFields(List<W5QueryField> postProcessQueryFields) {
		this.postProcessQueryFields = postProcessQueryFields;
	}

	public W5Table getMainTable() {
		return mainTable;
	}

	public void setMainTable(W5Table mainTable) {
		this.mainTable = mainTable;
	}

	public List<W5QueryField> getNewQueryFields() {
		return newQueryFields;
	}

	public void setNewQueryFields(List<W5QueryField> newQueryFields) {
		this.newQueryFields = newQueryFields;
	}




	public boolean prepareDataViewQuery(Map<String, String> extraParams) {

    	StringBuilder sql= new StringBuilder();
		setSqlParams(new ArrayList<Object>());
		Map<String,String>	requestParams2;
		if(extraParams == null || extraParams.isEmpty()){
			requestParams2 = requestParams;
		}  else {
			requestParams2 = new HashMap<String,String>();
			requestParams2.putAll(requestParams);
			requestParams2.putAll(extraParams);
		}
    	W5Query query = getQuery();
    	sql.append(" from ");
    	String sqlFrom = null;
    	if(query.getSqlFrom().contains("${")){
    		Object[] oz = DBUtil.filterExt4SQL(query.getSqlFrom(), scd, requestParams2, null);
    		sqlFrom = ((StringBuilder)oz[0]).toString();
			if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
    	} else sqlFrom=query.getSqlFrom();
    	
   		sql.append(sqlFrom);
    	
    	StringBuilder sqlWhere= new StringBuilder();
    	if(query.getSqlWhere()!=null){
    		if(query.getSqlWhere().contains("${")){
    			Object[] oz = DBUtil.filterExt4SQL(query.getSqlWhere(), scd, requestParams2, null);
    			sqlWhere.append(oz[0]);
    			if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
    		} else
    			sqlWhere.append(query.getSqlWhere());
    	}
    	
    	if(requestParams2.containsKey("firstLimit")){//has Paging
    		sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
    		.append("x.").append(query.get_queryFields().get(0).getDsc());
	    	if(requestParams2.containsKey("_min_pk")){
	    		sqlWhere.append(">?)");
	    		sqlParams.add(GenericUtil.uInt(requestParams2,"_min_pk"));
	    	} else {
	    		sqlWhere.append("<?)");
	    		int maxPk=GenericUtil.uInt(requestParams2,"_max_pk");
	    		if(maxPk==0)maxPk=2000000000; //maximum bu kadar olacak (2 milyar)
	    		sqlParams.add(maxPk);
	    	} 
    	}

		Locale xlocale = new Locale(FrameworkCache.getAppSettingStringValue(scd, "locale","en"));
		List<W5QueryParam> pqs = getQuery().get_queryParams();
    	for(W5QueryParam p1 : pqs){
			String pexpressionDsc = p1.getExpressionDsc();
    		if((p1.getOperatorType()==8 || p1.getOperatorType()==9) && p1.getSourceType()==1){
    			String value = requestParams2.get(p1.getDsc()); 
    			if(value!=null && value.length()>0){
	    			String[] pvalues = requestParams2.get(p1.getDsc()).split(",");
	    			if(pvalues.length>0){
	    				sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
						.append(pexpressionDsc)
						.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
						.append(" ( ");
						for(int	q1=0;q1<pvalues.length;q1++){
							sqlWhere.append(q1==0 ? "?": " ,?");
							sqlParams.add(GenericUtil.getObjectByTip(pvalues[q1], p1.getParamType()));
						}
						sqlWhere.append(" ) ) ");
	    			}
    			}
    		} else {
				Object psonuc = GenericUtil.prepareParam((W5Param)p1, getScd(), getRequestParams(), (short)-1, extraParams, (short)0, null, null, errorMap);
		
				if(errorMap.size()==0 && psonuc!=null){ // no error
					if(p1.getOperatorType()!=10){ // not custom operator
						sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
							.append(p1.getOperatorType()>10 ? FrameworkCache.getAppSettingStringValue(getScd(), "db_lower_fnc","lower")+"("+pexpressionDsc+")" : pexpressionDsc)
							.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
							.append("? ) ");
						if(p1.getOperatorType()>10)psonuc=((String)psonuc).toLowerCase(xlocale) + "%";
						if(p1.getOperatorType()==13)psonuc="%" + psonuc;
						sqlParams.add(psonuc);
	
					} else { //custom operator ise: ornegin "x.value_tip=? OR -1=?", kac adet ? varsa o kadar params a koyacaksin; eger pexpressionDsc numeric degerse o kadar koyacaksin
						if(GenericUtil.uInt(pexpressionDsc)!=0){
							for(int	q9=GenericUtil.uInt(pexpressionDsc);q9>0;q9--){
								sqlParams.add(psonuc);
							}
						} else if(pexpressionDsc.contains("?")){
							sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
							.append(pexpressionDsc)
							.append(" ) ");
							int q8=0;
							for(int	q9=0;q9<pexpressionDsc.length();q9++)if(pexpressionDsc.charAt(q9)=='?'){
								if(psonuc!=null && psonuc instanceof Object[]){ //eger aradarda veri gelirse
									sqlParams.add(((Object[])psonuc)[q8++]);
								} else
									sqlParams.add(psonuc);
							}
						} else if(pexpressionDsc.contains("${")){//usual ${req.xxx}
							Object[] oz = DBUtil.filterExt4SQL(pexpressionDsc, scd, requestParams2, null);
							if(sqlWhere.length()>0)sqlWhere.append(" AND ");
							sqlWhere.append("(").append(oz[0]).append(")");
							if(oz[1]!=null)sqlParams.addAll((List)oz[1]);							
						} else {//napak inanak mi kanka
							sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
							.append(pexpressionDsc)
							.append(" ) ");
						}
						
					}
				}
    		}
				 
		}
    	
		if(errorMap.size()>0)return false;


		//icindeki gorulme sadece su fieldlar tarafindan yapilacak
		if(mainTable!=null && mainTable.getAccessViewTip()!=0 && mainTable.getAccessViewUserFields()!=null 
				&& (mainTable.getAccessViewRoles()==null || !GenericUtil.hasPartInside(mainTable.getAccessViewRoles(), scd.get("roleId").toString()))
				&& (mainTable.getAccessViewUsers()==null || !GenericUtil.hasPartInside(mainTable.getAccessViewUsers(), scd.get("userId").toString()))
		){
			String[] fieldIdz = mainTable.getAccessViewUserFields().split(",");
			sqlWhere.append(sqlWhere.length()>0 ? " AND (":" ("); 
			boolean bq=false;
			for(String s:fieldIdz){
				W5TableField tf = mainTable.get_tableFieldMap().get(GenericUtil.uInt(s));
				if(tf!=null){
					if(bq)sqlWhere.append(" OR ");else bq=true;
					sqlWhere.append("x.").append(tf.getDsc()).append("=?");
					sqlParams.add(scd.get("userId"));
				}
			}
			sqlWhere.append(")");
		}
		
		if(mainTable!=null){
			String pkField = mainTable.get_tableFieldList().get(0).getDsc();

			
			//approval icinde herhangi birinde varsa onu
			if(FrameworkSetting.workflow && mainTable.get_hasApprovalViewControlFlag()!=0){
				if(sqlWhere.length()>0)sqlWhere.append(" AND");
				sqlWhere.append(" (not exists(select 1 from iwb.w5_approval_record cx where cx.finished_flag=0 AND cx.table_id=")
					.append(query.getSourceObjectId()).append(" AND cx.customization_id=? AND cx.table_pk=x.").append(pkField)
					.append(") OR exists(select 1 from iwb.w5_approval_record cx where cx.finished_flag=0 AND cx.table_id=")
					.append(query.getSourceObjectId()).append(" AND cx.customization_id=? AND cx.table_pk=x.").append(pkField)
					.append(" AND (cx.access_view_tip=0 OR (position(','||?||',' in ','||coalesce(cx.access_view_roles,'-')||',')>0 OR position(','||?||',' in ','||coalesce(cx.access_view_users,'-')||',')>0 )))) ");
				
				sqlParams.add(scd.get("customizationId"));
				sqlParams.add(scd.get("customizationId"));
				sqlParams.add(scd.get("roleId"));
				sqlParams.add(scd.get("userId"));
			}
			
			//approval_status var mi?
			if(requestParams2!=null && mainTable.get_approvalMap()!=null && !mainTable.get_approvalMap().isEmpty()){ //simdilik manuel4 nakit akis
				String approvalStepIds = mainTable.get_approvalMap().get((short)1)!=null ? requestParams2.get("_approval_step_ids1") : null;//edit icin
				if((approvalStepIds==null || approvalStepIds.length()==0) && mainTable.get_approvalMap().get((short)2)!=null)approvalStepIds = requestParams2.get("_approval_step_ids2");//insert icin
				if((approvalStepIds==null || approvalStepIds.length()==0) && mainTable.get_approvalMap().get((short)3)!=null)approvalStepIds = requestParams2.get("_approval_step_ids3");//delete icin
				if(approvalStepIds!=null && approvalStepIds.length()>0){
					if(sqlWhere.length()>0)sqlWhere.append(" AND");
					sqlWhere.append(" exists(select 1 from iwb.w5_approval_record rz, (select * from iwb.tool_parse_numbers(?,',')) t where rz.customization_id=? AND rz.table_id=? AND rz.table_pk=x."+query.get_queryFields().get(0).getDsc()+" AND t.satir::integer=rz.approval_step_id) ");
					sqlParams.add(approvalStepIds);
					sqlParams.add(scd.get("customizationId"));
					sqlParams.add(query.getSourceObjectId());
				}
			}
		

			
		}

		
		if(sqlWhere.length()>0)sql.append(" where ").append(sqlWhere);
   		setHasPaging(getFetchRowCount()!=0);
    	if(query.getSqlGroupby()!=null){
    		sql.append(" group by ").append(query.getSqlGroupby());
    	}
   		StringBuilder s = new StringBuilder("select ");

   		if(query.getSqlSelect().contains("${")){
	   		Object[] oz = DBUtil.filterExt4SQL(query.getSqlSelect(), scd, requestParams2, null);
			s.append(oz[0]);
			if(oz[1]!=null)sqlParams.addAll(0,(List)oz[1]);
   		} else s.append(query.getSqlSelect());


		s.append(sql);
/*
   		if(getOrderBy()!=null && getOrderBy().length()>0){
   			s.append(" order by ");
			Object[] oz = PromisUtil.filterExt4SQL(getOrderBy(), scd, requestParams2, null);
			s.append(oz[0]);
			if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
   		} else {
   			
   		}
   		*/
		s.append(" order by ").append(GenericUtil.isEmpty(getOrderBy()) ? query.get_queryFields().get(0).getDsc()+" desc":getOrderBy());
   		setExecutedSql(s.toString());
   		setStartRowNumber(0);

    	return true;
		
	}

	public Map<String, Object> getExtraOutMap() {
		return extraOutMap;
	}

	public void setExtraOutMap(Map<String, Object> extraOutMap) {
		this.extraOutMap = extraOutMap;
	}

	public String getExecutedSqlFrom() {
		return executedSqlFrom;
	}

	public void setExecutedSqlFrom(String executedSqlFrom) {
		this.executedSqlFrom = executedSqlFrom;
	}

	public List<Object> getExecutedSqlFromParams() {
		return executedSqlFromParams;
	}

	public void setExecutedSqlFromParams(List<Object> executedSqlFromParams) {
		this.executedSqlFromParams = executedSqlFromParams;
	}

	public Map<String, W5QueryField> getQueryColMap() {
		return queryColMap;
	}

	public void setQueryColMap(Map<String, W5QueryField> queryColMap) {
		this.queryColMap = queryColMap;
	}
/*
	public boolean prepareQuery4StatsTree(int statType, String tableFieldSQL, String stackField, String funcFields) {
		String[] stats=new String[]{"","sum","avg"};
    	prepareQuery(null);
    	if(!errorMap.isEmpty()) return false;
    	String sql = "select " + tableFieldSQL + " id, ";
    	if(!GenericUtil.isEmpty(stackField))sql+=stackField + " stack_id, ";
    	if(statType!=0){
    		String[] fq=funcFields.split(",");
    		Set<Integer> fqs=new HashSet();
    		for(String s:fq)fqs.add(GenericUtil.uInt(s));
    		
    		int count=0;
        	for(W5QueryField o:query.get_queryFields())if(fqs.contains(o.getQueryFieldId())){
        		count++;
        		if(count>1)
        			sql += ","+stats[statType]+"("+o.getDsc()+") xres"+count;
        		else
        			sql += stats[statType]+"("+o.getDsc()+") xres";
        	}
        	if(count==0)return false;
    	} else {
    		sql += "count(1) xres";
    	}
    	
    	sql+=" from (" + executedSql + ") x group by id";
    	if(!GenericUtil.isEmpty(stackField))sql+=", stack_id";
    	sql+=tableFieldSQL.startsWith("to_char(") ? " order by id":" order by xres desc";
    	executedSql= sql;
    	
    	return true;
	}
	*/
	   public boolean	prepareQuery4Debug(String sqlSelect, String sqlFrom, String sqlWhere2, String sqlGroupby, String sqlOrderby){
	    	
	    	StringBuilder sql= new StringBuilder();
			setSqlParams(new ArrayList<Object>());
			List<Object> sqlParamsLog=new ArrayList<Object>();
			Map<String,String>	requestParams2 = requestParams;
	    	W5Query query = getQuery();
	    	sql.append(" from ");
	    	if(sqlFrom.contains("${")){
	    		Object[] oz = DBUtil.filterExt4SQL(sqlFrom, scd, requestParams2, null);
	    		sqlFrom = ((StringBuilder)oz[0]).toString();
				if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
	    	}
	    	
	   		sql.append(sqlFrom);
	    	
	    	StringBuilder sqlWhere= new StringBuilder();
	    	
	    	if(sqlWhere2!=null){
	    		if(sqlWhere2.contains("${")){
	    			Object[] oz = DBUtil.filterExt4SQL(sqlWhere2, scd, requestParams2, null);
	    			sqlWhere.append(oz[0]);
	    			if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
	    		} else
	    			sqlWhere.append(sqlWhere2);
	    	}

	    	if(query!=null){
				Locale xlocale = new Locale(FrameworkCache.getAppSettingStringValue(scd, "locale","en"));
				List<W5QueryParam> pqs =  getQuery().get_queryParams();
		    	for(W5QueryParam p1 : pqs){
					String pexpressionDsc = p1.getExpressionDsc();
		    		if((p1.getOperatorType()==8 || p1.getOperatorType()==9) && p1.getSourceType()==1){
		    			String value = requestParams2.get(p1.getDsc()); 
		    			if(value!=null && value.length()>0){
			    			String[] pvalues = requestParams2.get(p1.getDsc()).split(",");
			    			if(pvalues.length>0){
			    				sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
								.append(pexpressionDsc)
								.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
								.append(" ( ");
								for(int	q1=0;q1<pvalues.length;q1++){
									sqlWhere.append(q1==0 ? "?": " ,?");
									sqlParams.add(GenericUtil.getObjectByTip(pvalues[q1], p1.getParamType()));
								}
								sqlWhere.append(" ) ) ");
			    			}
		    			}
		    		} else {
						Object psonuc = GenericUtil.prepareParam((W5Param)p1, getScd(), getRequestParams(), (short)-1, null, (short)0, null, null, errorMap);
				
						if(errorMap.size()==0 && psonuc!=null){ // no error
							if(p1.getOperatorType()!=10){ // not custom operator
								sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
									.append(p1.getOperatorType()>10 ? FrameworkCache.getAppSettingStringValue(getScd(), "db_lower_fnc","lower")+"("+pexpressionDsc+")" : pexpressionDsc)
									.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
									.append("? ) ");
								if(p1.getOperatorType()>10)psonuc=((String)psonuc).toLowerCase(xlocale) + "%";
								if(p1.getOperatorType()==13)psonuc="%" + psonuc;
								sqlParams.add(psonuc);
			
							} else { //custom operator ise: ornegin "x.value_tip=? OR -1=?", kac adet ? varsa o kadar params a koyacaksin; eger pexpressionDsc numeric degerse o kadar koyacaksin
								if(GenericUtil.uInt(pexpressionDsc)!=0){
									for(int	q9=GenericUtil.uInt(pexpressionDsc);q9>0;q9--){
										sqlParams.add(psonuc);
									}
								} else if(pexpressionDsc.contains("?")){
									sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ");							
									int q8=0;
									for(int	q9=0;q9<pexpressionDsc.length();q9++)if(pexpressionDsc.charAt(q9)=='?'){
										if(psonuc!=null && psonuc instanceof Object[]){ //eger aradarda veri gelirse
											sqlParams.add(((Object[])psonuc)[q8++]);
										} else
											sqlParams.add(psonuc);
									}
									if(pexpressionDsc.contains("${")){//? i�aretlerinden sonra ba�ka bir parametre varsa
										Object[] oz = DBUtil.filterExt4SQL(pexpressionDsc, scd, requestParams2, null);
										if(oz[0]!=null)pexpressionDsc = oz[0].toString();
										if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
									}
									sqlWhere.append(pexpressionDsc).append(" ) ");
								} else if(pexpressionDsc.contains("${")){//usual ${req.xxx}
									Object[] oz = DBUtil.filterExt4SQL(pexpressionDsc, scd, requestParams2, null);
									if(sqlWhere.length()>0)sqlWhere.append(" AND ");
									sqlWhere.append("(").append(oz[0]).append(")");
									if(oz[1]!=null)sqlParams.addAll((List)oz[1]);							
								} else {//napak inanak mi kanka
									sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
									.append(pexpressionDsc)
									.append(" ) ");
								}
								
							}
						}
		    		}
						 
				}
	    	}
	    /*	TODO: simdilik degil
	    	if(PromisSetting.cloud && mainTable!=null && mainTable.get_tableParamList().size()>1 && mainTable.get_tableParamList().get(1).getDsc().equals("customizationId") && sqlWhere.indexOf("x.customization_id=")==-1){
	    		if(sqlWhere.length()>0)sqlWhere.append(" AND ");
	    		sqlWhere.append(" x.customization_id=? ");
	    		sqlParams.add(scd.get("customizationId"));
	    	}
	*/    	

	    	
			if(errorMap.size()>0)return false;
			
			if(sqlWhere.length()>0)sql.append(" where ").append(sqlWhere);
	   		setHasPaging(getFetchRowCount()!=0);
	   		if(!GenericUtil.isEmpty(sqlGroupby)){
	   			sql.append(" group by ");
				Object[] oz = DBUtil.filterExt4SQL(sqlGroupby, scd, requestParams2, null);
				sql.append(oz[0]);
				if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
			}    	   	
	    	
	   		StringBuilder s = new StringBuilder("select ");
//	   		String sqlSelect = query.getSqlSelect();

	   		
	   		if(GenericUtil.isEmpty(sqlGroupby)){
	   			executedSqlFromParams = new ArrayList();
	   			executedSqlFromParams.addAll(sqlParams);
	   		}
	   		
	   		if(sqlSelect.contains("${")){
		   		Object[] oz = DBUtil.filterExt4SQL(sqlSelect, scd, requestParams2, null);
				s.append(oz[0]);
				if(oz[1]!=null){
					sqlParams.addAll(0,(List)oz[1]);
				}
	   		} else {
	   			s.append(sqlSelect);
	   		}

	   		if(!GenericUtil.isEmpty(sqlGroupby)){
	   			s.append(sql);
	   		} else {
	   			executedSqlFrom = sql.toString();
	   			s.append(sql);
//	   			s=sql;
	   		}

	   			
			if(!GenericUtil.isEmpty(sqlOrderby)){
	   			s.append(" order by ");
	   			Object[] oz = DBUtil.filterExt4SQL(sqlOrderby, scd, requestParams2, null);
				s.append(oz[0]);
				if(oz[1]!=null){
				   		sqlParams.addAll((List)oz[1]);
					
				}
	   		}
	   		setExecutedSql(s.toString());

	    	return true;
	    }


	
    private boolean	prepareQuery4ExternalDb(Map<String, String> extraParams){
    	
    	StringBuilder sql= new StringBuilder();
		setSqlParams(new ArrayList<Object>());
		Map<String,String>	requestParams2;
		if(extraParams == null || extraParams.isEmpty()){
			requestParams2 = requestParams;
		}  else {
			requestParams2 = new HashMap<String,String>();
			requestParams2.putAll(requestParams);
			requestParams2.putAll(extraParams);
		}
    	W5Query query = getQuery();
    	sql.append(" from ");
    	String sqlFrom = null;
    	if(query.getSqlFrom().contains("${")){
    		Object[] oz = DBUtil.filterExt4SQL(query.getSqlFrom(), scd, requestParams2, null);
    		sqlFrom = ((StringBuilder)oz[0]).toString();
			if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
    	} else sqlFrom=query.getSqlFrom();
    	
   		sql.append(sqlFrom);
    	
    	StringBuilder sqlWhere= new StringBuilder();
    	if(query.getSqlWhere()!=null){
    		if(query.getSqlWhere().contains("${")){
    			Object[] oz = DBUtil.filterExt4SQL(query.getSqlWhere(), scd, requestParams2, null);
    			sqlWhere.append(oz[0]);
    			if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
    		} else
    			sqlWhere.append(query.getSqlWhere());
    	}

		Locale xlocale = new Locale(FrameworkCache.getAppSettingStringValue(scd, "locale","en"));
		List<W5QueryParam> pqs = null;
		pqs=getQuery().get_queryParams();
    	if(!GenericUtil.isEmpty(pqs))for(W5QueryParam p1 : pqs){
			String pexpressionDsc = p1.getExpressionDsc();
    		if((p1.getOperatorType()==8 || p1.getOperatorType()==9) && p1.getSourceType()==1){
    			String value = requestParams2.get(p1.getDsc()); 
    			if(value!=null && value.length()>0){
	    			String[] pvalues = requestParams2.get(p1.getDsc()).split(",");
	    			if(pvalues.length>0){
	    				sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
						.append(pexpressionDsc)
						.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
						.append(" ( ");
						for(int	q1=0;q1<pvalues.length;q1++){
							sqlWhere.append(q1==0 ? "?": " ,?");
							sqlParams.add(GenericUtil.getObjectByTip(pvalues[q1], p1.getParamType()));
						}
						sqlWhere.append(" ) ) ");
	    			}
    			}
    		} else {
				Object psonuc = GenericUtil.prepareParam((W5Param)p1, getScd(), getRequestParams(), (short)-1, extraParams, (short)0, null, null, errorMap);
		
				if(errorMap.size()==0 && psonuc!=null){ // no error
					if(p1.getOperatorType()!=10){ // not custom operator
						sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
							.append(p1.getOperatorType()>10 ? FrameworkCache.getAppSettingStringValue(getScd(), "db_lower_fnc","lower")+"("+pexpressionDsc+")" : pexpressionDsc)
							.append(FrameworkSetting.operatorMap[p1.getOperatorType()])
							.append("? ) ");
						if(p1.getOperatorType()>10)psonuc=((String)psonuc).toLowerCase(xlocale) + "%";
						if(p1.getOperatorType()==13)psonuc="%" + psonuc;
						sqlParams.add(psonuc);
	
					} else { //custom operator ise: ornegin "x.value_tip=? OR -1=?", kac adet ? varsa o kadar params a koyacaksin; eger pexpressionDsc numeric degerse o kadar koyacaksin
						if(GenericUtil.uInt(pexpressionDsc)!=0){
							for(int	q9=GenericUtil.uInt(pexpressionDsc);q9>0;q9--){
								sqlParams.add(psonuc);
							}
						} else if(pexpressionDsc.contains("?")){
							sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ");							
							int q8=0;
							for(int	q9=0;q9<pexpressionDsc.length();q9++)if(pexpressionDsc.charAt(q9)=='?'){
								if(psonuc!=null && psonuc instanceof Object[]){ //eger aradarda veri gelirse
									sqlParams.add(((Object[])psonuc)[q8++]);
								} else
									sqlParams.add(psonuc);
							}
							if(pexpressionDsc.contains("${")){//? işaretlerinden sonra başka bir parametre varsa
								Object[] oz = DBUtil.filterExt4SQL(pexpressionDsc, scd, requestParams2, null);
								if(oz[0]!=null)pexpressionDsc = oz[0].toString();
								if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
							}
							sqlWhere.append(pexpressionDsc).append(" ) ");
						} else if(pexpressionDsc.contains("${")){//usual ${req.xxx}
							Object[] oz = DBUtil.filterExt4SQL(pexpressionDsc, scd, requestParams2, null);
							if(sqlWhere.length()>0)sqlWhere.append(" AND ");
							sqlWhere.append("(").append(oz[0]).append(")");
							if(oz[1]!=null)sqlParams.addAll((List)oz[1]);							
						} else {//napak inanak mi kanka
							sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ")
							.append(pexpressionDsc)
							.append(" ) ");
						}
						
					}
				}
    		}
				 
		}
    	
    	if(scd!=null && scd.get("_renderer")!=null && scd.get("_renderer").toString().startsWith("webix") && requestParams2!=null)for(String k:requestParams2.keySet())if(k.startsWith("filter[")&&k.endsWith("]")){
    		Object paramValue = requestParams2.get(k);
    		if(paramValue==null || paramValue.toString().length()==0)continue;
    		String paramName = k.substring("filter[".length(), k.length()-1);
    		for(W5QueryField qf:query.get_queryFields())if(qf.getDsc().equals(paramName))switch(qf.getFieldType()){
    		case	3:case	4://integer, double
    			sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ");
    			sqlWhere.append(paramName).append("= ? ) ");
    			sqlParams.add(qf.getFieldType()==3 ? GenericUtil.uInt(paramValue) : GenericUtil.uDouble(paramValue.toString()) );
    			break;
    		case	2://date:TODO
    			break;
    		case	5://boolean:TODO
    			break;
    		case	1://string
    			sqlWhere.append(sqlWhere.length()>0 ? " AND ( " : " ( ");
    			if(qf.getPostProcessType()!=11 && qf.getPostProcessType()!=13){//multi degilse
    				sqlWhere.append(FrameworkCache.getAppSettingStringValue(getScd(), "db_lower_fnc","lower")).append("(").append(paramName).append(") like ? ) ");
    				sqlParams.add((paramValue.toString()).toLowerCase(xlocale) + "%");
    			} else {
    				sqlWhere.append("position(','||?||',' in ','||coalesce(").append(paramName).append(",'-')||',')>0) ");
    				sqlParams.add(paramValue);    				
    			}
    			break;
    		default://others
    			
    		}
    		
    	}

    	
		if(errorMap.size()>0)return false;



		
		if(sqlWhere.length()>0)sql.append(" where ").append(sqlWhere);
   		setHasPaging(getFetchRowCount()!=0);
   		if(!GenericUtil.isEmpty(query.getSqlGroupby())){
   			if(query.getSqlGroupby().contains("${")){
   				if(query.getSqlGroupby()!=null && query.getSqlGroupby().length()>0){
   		   			sql.append(" group by ");
   					Object[] oz = DBUtil.filterExt4SQL(query.getSqlGroupby(), scd, requestParams2, null);
   					sql.append(oz[0]);
   					if(oz[1]!=null)sqlParams.addAll((List)oz[1]);
   		   		}		
   			}else{
   				sql.append(" group by ").append(query.getSqlGroupby());
   			}    		
		}    	   	
    	
   		StringBuilder s = new StringBuilder("select ");
   		String sqlSelect = query.getSqlSelect();

   		
   		if(GenericUtil.isEmpty(query.getSqlGroupby())){
   			executedSqlFromParams = new ArrayList();
   			executedSqlFromParams.addAll(sqlParams);
   		}
   		
   		if(sqlSelect.contains("${")){
	   		Object[] oz = DBUtil.filterExt4SQL(sqlSelect, scd, requestParams2, null);
			s.append(oz[0]);
			if(oz[1]!=null){
				sqlParams.addAll(0,(List)oz[1]);
			}
   		} else {
   			s.append(sqlSelect);
   		}


   		if(!GenericUtil.isEmpty(query.getSqlGroupby())){
   			s.append(sql);
   		} else {
   			executedSqlFrom = sql.toString();
   			s.append(sql);
//   			s=sql;
   		}

	
		if(getOrderBy()!=null && getOrderBy().length()>0){
   			s.append(" order by ");
   			String strOrder =getOrderBy();
   			Object[] oz = DBUtil.filterExt4SQL(strOrder, scd, requestParams2, null);
			s.append(oz[0]);
			if(oz[1]!=null){
			   		sqlParams.addAll((List)oz[1]);
				
			}
   		}

   		setExecutedSql(s.toString());

    	return true;
    }
    
    
}
