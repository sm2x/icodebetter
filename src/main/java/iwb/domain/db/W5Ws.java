package iwb.domain.db;

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
public class W5Ws  implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 213123123451L;
	private int wsId;
	private String dsc;
	private String wsUrl;
	private short wsTip;
	private short accessExecuteTip;
	private String accessExecuteRoles;
	private String accessExecuteUsers;
	private short activeFlag;
	private List<W5WsMethod> _methods;
	//private	SoapService _service;
	private	Map<String, Object> _map;
	private int timeout;
	private short wssTip;//0:none, 1:credentials, 2:token
	private String wssCredentials;
	
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
	
	@Column(name="ws_tip")
	public short getWsTip() {
		return wsTip;
	}
	public void setWsTip(short wsTip) {
		this.wsTip = wsTip;
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
	
	/*@Transient
	public SoapService get_service() {
		return _service;
	}
	public void set_service(SoapService _service) {
		this._service = _service;
	}*/
	
	@Transient
	public void storeValue(String key, Object value){
		if(_map==null)_map=new HashMap<String, Object>();
		_map.put(key, value);
	}
	
	@Transient
	public Object loadValue(String key){
		if(_map==null)return null;
		return _map.get(key);
	}
	@Column(name="wss_tip")
	public short getWssTip() {
		return wssTip;
	}
	public void setWssTip(short wssTip) {
		this.wssTip = wssTip;
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
}
