package iwb.domain.db;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;


@Entity
@Immutable
@Table(name="w5_db_func",schema="iwb")
public class W5GlobalFunc implements java.io.Serializable, W5Base {
/*TABLE_ID: 20*/
	
	private static final long serialVersionUID = 182569357739547L;

	private int globalFuncId;

	private String dsc;
	
	private short logLevelType;
	
	private short accessExecTip;
	private String accessExecRoles;
	private String accessExecUsers;
	
	private String accessSourceTypes;
	private int timeout;
	private short lkpCodeType;
	private String code;

	
	private List<W5GlobalFuncParam> _dbFuncParamList;


	
	
	@Column(name="log_level_tip")	
	public short getLogLevelType() {
		return logLevelType;
	}

	public void setLogLevelType(short logLevelType) {
		this.logLevelType = logLevelType;
	}

	public W5GlobalFunc() {
	}

	@Id
	@Column(name="db_func_id")
	public int getGlobalFuncId() {
		return this.globalFuncId;
	}

	public void setGlobalFuncId(int globalFuncId) {
		this.globalFuncId = globalFuncId;
	}


	@Column(name="dsc")
	public String getDsc() {
		return this.dsc;
	}

	public void setDsc(String dsc) {
		this.dsc = dsc;
	}
	
	@Transient
	public List<W5GlobalFuncParam> get_dbFuncParamList() {
		return _dbFuncParamList;
	}

	public void set_dbFuncParamList(List<W5GlobalFuncParam> dbFuncParamList) {
		_dbFuncParamList = dbFuncParamList;
	}

	@Column(name="rhino_script_code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}	
	

	@Column(name="access_source_types")
	public String getAccessSourceTypes() {
		return accessSourceTypes;
	}

	public void setAccessSourceTypes(String accessSourceTypes) {
		this.accessSourceTypes = accessSourceTypes;
	}

	@Column(name="access_exec_tip")
	public short getAccessExecTip() {
		return accessExecTip;
	}

	public void setAccessExecTip(short accessExecTip) {
		this.accessExecTip = accessExecTip;
	}

	@Column(name="access_exec_roles")
	public String getAccessExecRoles() {
		return accessExecRoles;
	}

	public void setAccessExecRoles(String accessExecRoles) {
		this.accessExecRoles = accessExecRoles;
	}

	@Column(name="access_exec_users")
	public String getAccessExecUsers() {
		return accessExecUsers;
	}

	public void setAccessExecUsers(String accessExecUsers) {
		this.accessExecUsers = accessExecUsers;
	}

	@Column(name="timeout")
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Column(name="lkp_code_type")
	public short getLkpCodeType() {
		return lkpCodeType;
	}

	public void setLkpCodeType(short lkpCodeType) {
		this.lkpCodeType = lkpCodeType;
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

	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5GlobalFunc))return false;
		W5GlobalFunc c = (W5GlobalFunc)o;
		return c!=null && c.getGlobalFuncId()==getGlobalFuncId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getGlobalFuncId();
	}

	public boolean safeEquals(W5Base q) {
		return false;
	}
	
}
