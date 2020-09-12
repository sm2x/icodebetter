package iwb.model.db;

import java.io.Serializable;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import iwb.util.GenericUtil;

@Entity
@Table(name="log5_job_action",schema="iwb")
public class Log5JobAction implements Serializable, Log5Base{
	private static final long serialVersionUID = 131111091872912873L;

	private int logId;
	private int jobScheduleId;
	private String projectUuid;
	private String error;
	private	String transactionId;

	private int processTime;
	private long startTime;
	
	
	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		s.append("job_action,job_id=").append(jobScheduleId).append(",project_uuid=").append(projectUuid).append(" duration=").append(getProcessTime()).append("i");
		if(!GenericUtil.isEmpty(transactionId))s.append(",trid=\"").append(transactionId).append("\"");
		if(!GenericUtil.isEmpty(error))s.append(",error=\"").append(GenericUtil.stringToJS2(error)).append("\"");
		s.append(" ").append(startTime).append("000000");
		return s.toString();
	}
	
	public Log5JobAction() {
	}
	

	public Log5JobAction(int jobScheduleId, String projectUuid, String transactionId) {
		super();
		this.jobScheduleId = jobScheduleId;
		this.projectUuid = projectUuid;
		this.startTime=Instant.now().toEpochMilli();
		this.processTime = -1;
		this.transactionId = transactionId;
	}

	@SequenceGenerator(name="sex_log5_job_action",sequenceName="iwb.seq_log5_job_action",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_log5_job_action")
	@Column(name="log_id")  
	public int getLogId() {
		return logId;
	}
	public void setLogId(int logId) {
		this.logId = logId;
	}

	@Column(name="job_schedule_id")  
	public int getJobScheduleId() {
		return jobScheduleId;
	}

	public void setJobScheduleId(int jobScheduleId) {
		this.jobScheduleId = jobScheduleId;
	}

	@Column(name="project_uuid")  
	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}

	@Column(name="error")  
	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	@Column(name="process_time")  
	public int getProcessTime() {
		return processTime;
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
}
