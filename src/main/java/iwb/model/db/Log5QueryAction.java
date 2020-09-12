package iwb.model.db;

import java.time.Instant;

// Generated Feb 4, 2007 3:49:13 PM by Hibernate Tools 3.2.0.b9

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import iwb.model.result.W5QueryResult;
import iwb.util.GenericUtil;

@Entity
@Table(name="log5_query_action",schema="iwb")
public class Log5QueryAction implements java.io.Serializable, Log5Base {
	private static final long serialVersionUID = 134252091777912873L;

	private int logId;

	private int userId;
	
	private int queryId;
	private String projectUuid;  

	private String dsc;
	private	String transactionId;


	private int processTime;
	private long startTime;

	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		switch(getQueryId()){
		case -999:s.append("sql_query duration=").append(getProcessTime()).append(",sql=\"").append(GenericUtil.stringToJS2(getDsc())).append("\"");
			break;
		case -998:s.append("sql_execute duration=").append(getProcessTime()).append(",sql=\"").append(GenericUtil.stringToJS2(getDsc())).append("\"");
			break;
		default:
			s.append("xquery");
			if(projectUuid!=null)s.append(",project_uuid=").append(projectUuid);
			s.append(" query_id=").append(getQueryId()).append("i,user_id=").append(getUserId()).append("i,duration=").append(getProcessTime()).append("i,sql=\"").append(GenericUtil.stringToJS2(getDsc())).append("\"");

		}
		if(!GenericUtil.isEmpty(transactionId))s.append(",trid=\"").append(transactionId).append("\"");
		s.append(" ").append(startTime).append("000000");
		return s.toString();
	}

	
	
	@Column(name="dsc")
	public String getDsc() {
		return dsc;
	}

	public void setDsc(String dsc) {
		this.dsc = dsc;
	}
	
	
	public Log5QueryAction(String transactionId, String dsc) {
		super();
		this.queryId = -999;
		this.startTime=Instant.now().toEpochMilli();
		this.dsc = dsc;
		this.processTime = -1;
		this.transactionId = transactionId;
	}



	public Log5QueryAction() {

	}

	public Log5QueryAction(W5QueryResult r) {
		this.queryId = r.getQueryId();
		this.userId = (Integer)r.getScd().get("userId");
		this.startTime=Instant.now().toEpochMilli();
		this.processTime = -1;
		this.projectUuid = r.getScd().get("projectId")!=null ?(String)r.getScd().get("projectId"):"0123456789";
		this.transactionId =r.getRequestParams()!=null ? r.getRequestParams().get("_trid_"):null;
	}
    @SequenceGenerator(name="sex_log_query_action",sequenceName="iwb.seq_log_query_action",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_log_query_action")
	@Column(name="log_id")
	public int getLogId() {
		return this.logId;
	}


	@Column(name="query_id")
	public int getQueryId() {
		return queryId;
	}

	public void setQueryId(int queryId) {
		this.queryId = queryId;
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

	@Transient
	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	@Column(name="project_uuid")  
	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}
}
