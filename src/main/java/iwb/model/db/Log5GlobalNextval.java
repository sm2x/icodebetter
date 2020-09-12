package iwb.model.db;

import java.io.Serializable;

import javax.persistence.Column;

public class Log5GlobalNextval implements Serializable, Log5Base{
	private static final long serialVersionUID = 134252022222912873L;

	private int userId;
	private int customizationId;  
	private String seq;  
	private String ip;  
	private String projectUuid;  

	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		s.append("global_nextval,project_uuid=").append(projectUuid).append(",seq=").append(seq).append(" user_id=").append(userId).append(",customizationId=").append(customizationId).append(",ip=\"").append(ip).append("\"");
		return s.toString();
	}

	
	public Log5GlobalNextval() {
	}
	


	public Log5GlobalNextval(int userId, int customizationId, String seq, String ip, String projectUuid) {
		super();
		this.userId = userId;
		this.customizationId = customizationId;
		this.seq = seq;
		this.ip = ip;
		this.projectUuid = projectUuid;
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

	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}
	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}


	@Column(name="seq")
	public String getSeq() {
		return seq;
	}


	public void setSeq(String seq) {
		this.seq = seq;
	}


	@Column(name="ip")
	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}
	
}
