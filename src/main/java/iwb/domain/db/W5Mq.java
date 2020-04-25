package iwb.domain.db;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

// Generated Feb 5, 2007 3:58:07 PM by Hibernate Tools 3.2.0.b9

@Entity
@Immutable
@Table(name = "w5_mq", schema = "iwb")
public class W5Mq implements java.io.Serializable, W5Base {

	/**
	 * 
	 */
	private static final long serialVersionUID = 77336434351L;

	private int mqId;

	private short lkpMqType;// amqp, mqtt

	private String mqUrl;

	private String mqUsername;

	private String mqPassword;

	private String mqProperties;

	private short activeFlag;

	private String projectUuid;

	private List<W5MqCallback> _callbacks;
	

	@Id 
	@Column(name = "project_uuid") 
	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}

	@Transient
	public boolean safeEquals(W5Base q) {
		return false;
	}

	@Id
	@Column(name = "mq_id")
	public int getMqId() {
		return mqId;
	}

	public void setMqId(int mqId) {
		this.mqId = mqId;
	}

	@Column(name = "lkp_mq_type")
	public short getLkpMqType() {
		return lkpMqType;
	}

	public void setLkpMqType(short lkpMqType) {
		this.lkpMqType = lkpMqType;
	}

	@Column(name = "mq_url")
	public String getMqUrl() {
		return mqUrl;
	}

	public void setMqUrl(String mqUrl) {
		this.mqUrl = mqUrl;
	}

	@Column(name = "mq_username")
	public String getMqUsername() {
		return mqUsername;
	}

	public void setMqUsername(String mqUsername) {
		this.mqUsername = mqUsername;
	}

	@Column(name = "mq_password")
	public String getMqPassword() {
		return mqPassword;
	}

	public void setMqPassword(String mqPassword) {
		this.mqPassword = mqPassword;
	}

	@Column(name = "mq_properties")
	public String getMqProperties() {
		return mqProperties;
	}

	public void setMqProperties(String mqProperties) {
		this.mqProperties = mqProperties;
	}

	@Column(name = "active_flag")
	public short getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(short activeFlag) {
		this.activeFlag = activeFlag;
	}

	@Transient
	public List<W5MqCallback> get_callbacks() {
		return _callbacks;
	}

	public void set_callbacks(List<W5MqCallback> _callbacks) {
		this._callbacks = _callbacks;
	}

	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5Mq))return false;
		W5Mq c = (W5Mq)o;
		return c!=null && c.getMqId()==getMqId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getMqId();
	}	

}
