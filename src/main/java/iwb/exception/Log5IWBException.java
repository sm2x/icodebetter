package iwb.exception;

import java.util.Map;

import iwb.model.db.Log5Base;
import iwb.util.GenericUtil;


public class Log5IWBException extends IWBException implements Log5Base {
	private Map<String, Object> scd;
	private String remoteAddress;
	private String requestUrl;
	private Map parameterMap;
	


	public Log5IWBException(Map<String, Object> scd, String requestUrl, Map parameterMap, String remoteAddress, IWBException iwb) {
		super(iwb.getErrorType(), iwb.getObjectType(), iwb.getObjectId(), iwb.getSql(), iwb.getMessage(), iwb.getCause());
		this.scd = scd;
		this.remoteAddress = remoteAddress;
		this.requestUrl = requestUrl;
		this.parameterMap = parameterMap;
	}

	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		if(scd==null || !scd.containsKey("projectId")){
			s.append("exception2 error_type=\"").append(getErrorType());
		} else {
			s.append("exception,project_uuid=").append(scd.get("projectId")).append(" user_id=").append(scd.get("userId")).append(",error_type=\"").append(getErrorType());
		}
		s.append("\",request_url=\"").append(GenericUtil.stringToJS2(requestUrl));
		s.append("\",object_type=\"").append(GenericUtil.stringToJS2(getObjectType())).append("\",object_id=").append(getObjectId()).append(",message=\"").append(GenericUtil.stringToJS2(getMessage()));
		if(!GenericUtil.isEmpty(getSql()))s.append("\",sql=\"").append(GenericUtil.stringToJS2(getSql()));
		s.append("\",ip=\"").append(GenericUtil.isEmpty(remoteAddress)?"(null)":remoteAddress).append("\"");
		s.append(",param_map=\"").append(GenericUtil.stringToJS2(GenericUtil.fromMapToJsonString2(parameterMap))).append("\"");
		if(parameterMap.containsKey("_trid_"))s.append(",trid=\"").append(parameterMap.get("_trid_")).append("\"");
		return s.toString();
	}


}
