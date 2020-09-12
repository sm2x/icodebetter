package iwb.model.db;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import graphql.GraphQL;

@Entity
@Immutable
@Table(name="w5_ws_server",schema="iwb")
public class W5WsServer  implements java.io.Serializable, W5Base {

	private static final long serialVersionUID = 134252091872912873L;
	
	private int wsServerId;
	private String dsc;
	private String wsUrl;
	private short wsTip;
	private String projectUuid;
	private short accessExecuteTip;
	private String accessExecuteRoles;
	private String accessExecuteUsers;
	private String targetNamespace;
	
	private List<W5WsServerMethod> _methods;
	private String _graphQLSchema;
	private GraphQL _graphQLBuild;
	
	
	
	@Id
	@Column(name="ws_server_id")
	public int getWsServerId() {
		return wsServerId;
	}
	public void setWsServerId(int wsServerId) {
		this.wsServerId = wsServerId;
	}
	
	
	public W5WsServer() {
		super();
		_graphQLSchema = null;
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
	
	@Id	
	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}
	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
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
	
	@Transient
	public List<W5WsServerMethod> get_methods() {
		return _methods;
	}
	public void set_methods(List<W5WsServerMethod> _methods) {
		this._methods = _methods;
	}
	
	@Column(name="target_namespace")
	public String getTargetNamespace() {
		return targetNamespace;
	}
	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}
	
	@Transient
	public String get_graphQLSchema() {
		return _graphQLSchema;
	}
	public void set_graphQLSchema(String _graphQLSchema) {
		this._graphQLSchema = _graphQLSchema;
	}
	@Transient
	public GraphQL get_graphQLBuild() {
		return _graphQLBuild;
	}
	public void set_graphQLBuild(GraphQL _graphQLBuild) {
		this._graphQLBuild = _graphQLBuild;
	}
	
	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5WsServer))return false;
		W5WsServer c = (W5WsServer)o;
		return c!=null && c.getWsServerId()==getWsServerId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getWsServerId();
	}
	

	@Transient
	public boolean safeEquals(W5Base q) {

			return false;
	}
}
