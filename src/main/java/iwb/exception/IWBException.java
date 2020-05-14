package iwb.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.util.GenericUtil;


public class IWBException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 16432198746533L;
	private	String errorType;
	private	String objectType;
	private	int objectId;
	private	String sql;
	private List<IWBException> stack;
	
	public IWBException(String errorType, String objectType, int objectId, String sql, String message, Throwable cause) {
		super(cause!=null && cause.getMessage()!=null && cause.getMessage().contains("(unnamed script#") ? message + " #{"+cause.getMessage().substring(cause.getMessage().indexOf("(unnamed script#")+"(unnamed script#".length(),cause.getMessage().length()-1)+"}#" : message, cause);
		this.errorType=errorType;//security, validation, framework, definition
		this.objectType=objectType;
		this.objectId=objectId;
		this.sql=sql;
		if(cause!=null){
			this.stack = convertToIWBException((Exception)cause).getStack();
		} else 
			this.stack = new ArrayList<IWBException>();

		this.stack.add(this);
	}

	public static IWBException convertToIWBException(Exception e){
		if(e instanceof IWBException)return (IWBException)e;
		if(e.getCause()==null || !(e.getCause() instanceof Exception))
			return new IWBException("framework",e.getClass().getName(), 0, null, e.getMessage(), null);
		
		Exception te = e;//e.printStackTrace()
		
		while(te.getCause()!=null && te.getCause() instanceof Exception){
			te = (Exception)te.getCause();
			if(te instanceof IWBException)return (IWBException)te;
		}
		
		String newObjectType = te.getClass().getName();
		if(newObjectType.equals("org.postgresql.util.PSQLException"))newObjectType="DataBase.Exception";
		
		if(newObjectType.equals("jdk.nashorn.internal.runtime.ParserException"))newObjectType="Scripting.Parsing";
		if(newObjectType.equals("jdk.nashorn.internal.runtime.ECMAException") || 
				newObjectType.equals("org.graalvm.polyglot.PolyglotException"))newObjectType="Scripting.RuntTime";
		return new IWBException("framework",newObjectType, 0, null, te.getMessage(), null);
	}

	public String toHtmlString(Map scd){
		StringBuilder b = new StringBuilder();
		b.append("<table>");
		b.append("<tr><td><b>Error Type</b></td><td>").append(errorType).append("</td></tr>");
		b.append("<tr><td><b>Error</b></td><td>").append(FrameworkCache.getExceptionMessage(scd, getMessage())).append("</td></tr>");
		if(FrameworkCache.getAppSettingIntValue(0, "debug")!=0){
			b.append("<tr><td><b>Object Type</b></td><td>").append(objectType).append("</td></tr>");
			b.append("<tr><td><b>Object Id</b></td><td>").append(objectId).append("</td></tr>");
		}
		b.append("</table>");
		return b.toString();
	}

	public String toJsonString(String uri, Map scd){
		StringBuilder b = new StringBuilder();
		IWBException e = GenericUtil.isEmpty(this.stack) ? this : this.stack.get(0);
		b.append("{\"success\":false,\n\"errorType\":\"").append(e.getErrorType()).append("\"");
		String msg = FrameworkCache.getExceptionMessage(scd, e.getMessage());
		if(msg!=null){
			b.append(",\"error\":\"").append(GenericUtil.stringToJS2(msg)).append("\"");
		}
		
		if(e.getObjectType()!=null){
			b.append(",\n\"objectType\":\"").append(GenericUtil.stringToJS2(e.getObjectType())).append("\"");
			if(e.getObjectId()!=0){
				b.append(",\n\"objectId\":").append(e.getObjectId());
			}
		}

		if(FrameworkSetting.debug){
			b.append(",\n\"stack\":\"").append(GenericUtil.stringToJS2(ExceptionUtils.getFullStackTrace(this))).append("\"");
			if(sql!=null)b.append(",\n\"sql\":\"").append(GenericUtil.stringToJS2(sql)).append("\"");
			if(!GenericUtil.isEmpty(this.stack) && this.stack.size()>1){
				b.append(",\n\"icodebetter\":[");
				boolean q = false;
				if(!GenericUtil.isEmpty(uri)){
					b.append("{\"errorType\":\"request\",\"objectType\":\"Web.Request\",\"error\":\"").append(uri).append("\"}");
					q = true;;					
				}
				for(int qi=stack.size()-1;qi>=0;qi--){
					if(q)b.append(","); else q=true;
					IWBException iw = (IWBException)stack.get(qi);
//					if(lastErrorMsg.equals(iw.getMessage()))continue;
					//lastErrorMsg = iw.getMessage();
					b.append("{\"errorType\":\"").append(iw.getErrorType()).append("\"");
					if(!GenericUtil.isEmpty(iw.getMessage()))b.append(",\"error\":\"").append(GenericUtil.stringToJS2(iw.getMessage())).append("\"");
					if(!GenericUtil.isEmpty(iw.getSql()))b.append(",\"sql\":\"").append(GenericUtil.stringToJS2(iw.getSql())).append("\"");
					if(!GenericUtil.isEmpty(iw.getObjectType())){
						b.append(",\"objectType\":\"").append(GenericUtil.stringToJS2(iw.getObjectType())).append("\",\"objectId\":").append(iw.getObjectId());
					}
					b.append("}");
				}
				b.append("]");
			}
		}
		
	

		return b.append("}").toString();
	}

	public String getErrorType() {
		return errorType;
	}


	public String getObjectType() {
		return objectType;
	}


	public int getObjectId() {
		return objectId;
	}


	public String getSql() {
		return sql;
	}

	public List<IWBException> getStack() {
		return stack;
	}



}
