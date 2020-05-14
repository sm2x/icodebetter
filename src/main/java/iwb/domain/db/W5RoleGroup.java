package iwb.domain.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;


@Entity
@Immutable
@Table(name="w5_user_tip", schema="iwb")
public class W5RoleGroup implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 333319124324231L;
	private int roleGroupId;	
	private short webFrontendType;	
	private short activeFlag;	
	private int defaultMainTemplateId;
	
	private String dsc;

	
	
	private String projectUuid;

	@Id
	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}

	
	@Id
	@Column(name="user_tip")
	public int getRoleGroupId() {
		return roleGroupId;
	}

	public void setRoleGroupId(int roleGroupId) {
		this.roleGroupId = roleGroupId;
	}

	@Column(name="web_frontend_tip")
	public short getWebFrontendType() {
		return webFrontendType;
	}

	public void setWebFrontendType(short webFrontendType) {
		this.webFrontendType = webFrontendType;
	}

	@Column(name="default_main_template_id")
	public int getDefaultMainTemplateId() {
		return defaultMainTemplateId;
	}

	public void setDefaultMainTemplateId(int defaultMainTemplateId) {
		this.defaultMainTemplateId = defaultMainTemplateId;
	}

	@Column(name="dsc")
	public String getDsc() {
		return dsc;
	}

	public void setDsc(String dsc) {
		this.dsc = dsc;
	}

	@Column(name="active_flag")
	public short getActiveFlag() {
		return activeFlag;
	}


	public void setActiveFlag(short activeFlag) {
		this.activeFlag = activeFlag;
	}


	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}

	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5RoleGroup))return false;
		W5RoleGroup c = (W5RoleGroup)o;
		return c!=null && c.getRoleGroupId()==getRoleGroupId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getRoleGroupId();
	}

	public boolean safeEquals(W5Base q) {
		return false;
	}
	

}
