package iwb.model.db;

import java.time.Instant;
import java.util.Map;

// Generated Feb 4, 2007 3:49:13 PM by Hibernate Tools 3.2.0.b9

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import iwb.util.GenericUtil;

@Entity
@Immutable
@Table(name="log5_ws_method_action",schema="iwb")
public class Log5WsMethodAction implements java.io.Serializable, Log5Base {

	private static final long serialVersionUID = 134252346672912873L;

	private int logId;

	private int wsMethodId;
	private int userId;
	private String url;
	private String params;
	private String response;


	private int processTime;
	private long startTime;
	private String projectUuid;  
	private	String transactionId;

	
	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		s.append("ws_method_action,project_uuid=").append(projectUuid).append(" user_id=").append(userId).append("i,ws_method_id=").append(wsMethodId).append("i,process_time=").append(processTime).append("i,url=\"").append(GenericUtil.stringToJS2(url)).append("\",params=\"").append(GenericUtil.stringToJS2(params)).append("\",response=\"").append(GenericUtil.stringToJS2(response)).append("\"");
		if(!GenericUtil.isEmpty(transactionId))s.append(",trid=\"").append(transactionId).append("\"");
		s.append(" ").append(startTime).append("000000");
		return s.toString();
	}

	
	

	public Log5WsMethodAction() {
		super();
	}




	@Column(name="url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


	@Column(name="params")
	public String getParams() {
		return params;
	}


	public void setParams(String params) {
		this.params = params;
	}


	@Column(name="response")
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}


	public Log5WsMethodAction(Map<String, Object> scd, int wsMethodId, String url, String params, String transactionId) {
		this.wsMethodId = wsMethodId;
		if(scd!=null){
			this.userId = (Integer)scd.get("userId");
			this.projectUuid = (String)scd.get("projectId");
		}
		this.url = url;
		this.params = params;
		this.startTime=Instant.now().toEpochMilli();
		this.processTime = -1;
		this.transactionId = transactionId;
	}
    @SequenceGenerator(name="sex_ws_method_action",sequenceName="iwb.seq_ws_method_action",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_ws_method_action")
	@Column(name="log_id")
	public int getLogId() {
		return this.logId;
	}


	public void setLogId(int logId) {
		this.logId = logId;
	}

	@Column(name="user_id")
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Column(name="process_time")
	public int getProcessTime() {
		return this.processTime;
	}

	public void setProcessTime(int processTime) {
		this.processTime = processTime;
	}



	public void calcProcessTime() {
		this.processTime = (int)(Instant.now().toEpochMilli() - this.startTime);
	}

	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}




	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}




	@Column(name="ws_method_id")
	public int getWsMethodId() {
		return wsMethodId;
	}


	public void setWsMethodId(int wsMethodId) {
		this.wsMethodId = wsMethodId;
	}




	@Transient
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

}
