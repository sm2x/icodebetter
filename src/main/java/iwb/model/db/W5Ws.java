package iwb.model.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

//import iwb.soap.generic.SoapService;

@Entity
@Immutable
@Table(name="w5_ws",schema="iwb")
public class W5Ws  implements java.io.Serializable, W5Base {
/*TABLE_ID: 1375*/
	
	private static final long serialVersionUID = 213123123451L;
	private int wsId;
	private String dsc;
	private String wsUrl;
	private short accessExecuteTip;
	private String accessExecuteRoles;
	private String accessExecuteUsers;
	private short activeFlag;
	//private	SoapService _service;
	private int timeout;
	private short wsSecurityType;//security: 0:none, 1:credentials
	private String wssCredentials;

	private List<W5WsMethod> _methods;

	@Id
	@Column(name="ws_id")
	public int getWsId() {
		return wsId;
	}
	public void setWsId(int wsId) {
		this.wsId = wsId;
	}
	
	
	@Column(name="dsc")
	public String getDsc() {
		return dsc;
	}
	public void setDsc(String dsc) {
		this.dsc = dsc;
	}
	
	@Column(name="ws_url")
	public String getWsUrl() {
		return wsUrl;
	}
	public void setWsUrl(String url) {
		this.wsUrl = url;
	}

	@Column(name="access_execute_tip")
	public short getAccessExecuteTip() {
		return accessExecuteTip;
	}
	public void setAccessExecuteTip(short accessExecuteTip) {
		this.accessExecuteTip = accessExecuteTip;
	}
	
	@Column(name="access_execute_roles")
	public String getAccessExecuteRoles() {
		return accessExecuteRoles;
	}
	public void setAccessExecuteRoles(String accessExecuteRoles) {
		this.accessExecuteRoles = accessExecuteRoles;
	}
	
	@Column(name="access_execute_users")
	public String getAccessExecuteUsers() {
		return accessExecuteUsers;
	}
	public void setAccessExecuteUsers(String accessExecuteUsers) {
		this.accessExecuteUsers = accessExecuteUsers;
	}
	
	@Column(name="active_flag")
	public short getActiveFlag() {
		return activeFlag;
	}
	public void setActiveFlag(short activeFlag) {
		this.activeFlag = activeFlag;
	}
	
	@Transient
	public List<W5WsMethod> get_methods() {
		return _methods;
	}
	public void set_methods(List<W5WsMethod> _methods) {
		this._methods = _methods;
	}
	
	
	@Column(name="wss_tip")
	public short getWsSecurityType() {
		return wsSecurityType;
	}
	public void setWsSecurityType(short wsSecurityType) {
		this.wsSecurityType = wsSecurityType;
	}

	
	@Column(name="timeout")
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	private String projectUuid;
	@Id	
	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}
	
	@Column(name="wss_credentials")
	public String getWssCredentials() {
		return wssCredentials;
	}
	public void setWssCredentials(String wssCredentials) {
		this.wssCredentials = wssCredentials;
	}
	
	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5Ws))return false;
		W5Ws c = (W5Ws)o;
		return c!=null && c.getWsId()==getWsId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getWsId();
	}
	

	@Transient
	public boolean safeEquals(W5Base q) {

			return false;
	}
}
