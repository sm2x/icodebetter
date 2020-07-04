package iwb.domain.result;

import java.util.List;
import java.util.Map;

import iwb.domain.db.W5GlobalFunc;
import iwb.domain.helper.W5QueuedActionHelper;
import iwb.domain.helper.W5ReportCellHelper;



public class W5GlobalFuncResult implements W5MetaResult{
	
	private	int	globalFuncId;
	private W5GlobalFunc globalFunc;
	
	private String executedSql;
	private	List<Object> sqlParams;
	private Map<String, Object> scd;
	private Map<String,String>	requestParams;
	private Map<String,Object>	resultMap;
	private Map<String,String>	errorMap;
	private	boolean success;
	private List<W5ReportCellHelper> reportList;
	private List<W5QueuedActionHelper> queuedGlobalFuncList;
	private int processTime;
	
	

	public W5GlobalFuncResult(int globalFuncId) {
		this.globalFuncId = globalFuncId;
	}
	public int getGlobalFuncId() {
		return globalFuncId;
	}
	public void setGlobalFuncId(int globalFuncId) {
		this.globalFuncId = globalFuncId;
	}
	public W5GlobalFunc getGlobalFunc() {
		return globalFunc;
	}
	public void setGlobalFunc(W5GlobalFunc dbFunc) {
		this.globalFunc = dbFunc;
	}

	public Map<String, Object> getScd() {
		return scd;
	}
	public void setScd(Map<String, Object> scd) {
		this.scd = scd;
	}
	public Map<String, String> getRequestParams() {
		return requestParams;
	}
	public void setRequestParams(Map<String, String> requestParams) {
		this.requestParams = requestParams;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public Map<String, Object> getResultMap() {
		return resultMap;
	}
	public void setResultMap(Map<String, Object> resultMap) {
		this.resultMap = resultMap;
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
	public List<Object> getSqlParams() {
		return sqlParams;
	}
	public void setSqlParams(List<Object> sqlParams) {
		this.sqlParams = sqlParams;
	}
	public List<W5ReportCellHelper> getReportList() {
		return reportList;
	}
	public void setReportList(List<W5ReportCellHelper> reportList) {
		this.reportList = reportList;
	}


	public List<W5QueuedActionHelper> getQueuedGlobalFuncList() {
		return queuedGlobalFuncList;
	}
	public void setQueuedGlobalFuncList(List<W5QueuedActionHelper> queuedGlobalFuncList) {
		this.queuedGlobalFuncList = queuedGlobalFuncList;
	}

	public int getProcessTime() {
		return processTime;
	}
	public void setProcessTime(int processTime) {
		this.processTime = processTime;
	}
}
