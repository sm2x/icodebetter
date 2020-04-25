package iwb.domain.db;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import iwb.util.GenericUtil;

@Entity
@Table(name="log5_console",schema="iwb")
public class Log5Console implements Serializable, Log5Base{
	private static final long serialVersionUID = 184252091816162873L;

	private int logId;
	private int userId;
	private int customizationId;  
	private String msg;  
	private String level;  
	private String projectUuid;  
	private	String transactionId;

	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		s.append("console,project_uuid=").append(projectUuid).append(" user_id=").append(userId).append("i,level=\"").append(level).append("\",msg=\"").append(GenericUtil.stringToJS2(msg)).append("\"");
		if(!GenericUtil.isEmpty(transactionId))s.append(",trid=\"").append(transactionId).append("\"");
		return s.toString();
	}

	
	public Log5Console() {
	}
	
	public Log5Console(Map<String, Object> scd,String msg,String level, String transactionId) {
		this.customizationId = scd.containsKey("customizationId") ? (Integer)scd.get("customizationId") : 0;
		this.projectUuid = (String)scd.get("projectId");
		this.userId = scd.containsKey("userId") ? (Integer)scd.get("userId") : 0;
		this.msg = msg;
		this.level = GenericUtil.isEmpty(level) ? "info":level;
		this.transactionId = transactionId;
	}

	@SequenceGenerator(name="sex_log5_console",sequenceName="iwb.seq_log5_console",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_log5_console")
	@Column(name="log_id")  
	public int getLogId() {
		return logId;
	}
	public void setLogId(int logId) {
		this.logId = logId;
	}

	@Column(name="customization_id")
	public int getCustomizationId() {
		return customizationId;
	}
	public void setCustomizationId(int customizationId) {
		this.customizationId = customizationId;
	}
	@Column(name="user_id")
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}


	@Column(name="msg")
	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}


	@Column(name="level")
	public String getLevel() {
		return level;
	}


	public void setLevel(String level) {
		this.level = level;
	}


	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}
	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}
	
}
