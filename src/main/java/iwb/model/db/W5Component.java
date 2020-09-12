package iwb.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

// Generated Feb 5, 2007 3:58:07 PM by Hibernate Tools 3.2.0.b9

@Entity
@Immutable
@Table(name="w5_component",schema="iwb")
public class W5Component implements java.io.Serializable, W5Base{

	/**
	 * 
	 */
	private static final long serialVersionUID = 66623434121L;
	private int componentId;

	private String dsc;

	private short status;
	private short frontendTip;
	private short frontendLang;
	private short lkpComponentType;
	private	String componentVersion;	

	private	String code;	
	private	String cssCode;	
	private	String jsCode;	

	
	@Id
	@Column(name="component_id")
	public int getComponentId() {
		return componentId;
	}

	public void setComponentId(int componentId) {
		this.componentId = componentId;
	}


	@Column(name="dsc")
	public String getDsc() {
		return dsc;
	}

	public void setDsc(String dsc) {
		this.dsc = dsc;
	}

	@Column(name="status")
	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		this.status = status;
	}

	@Column(name="frontend_tip")
	public short getFrontendTip() {
		return frontendTip;
	}

	public void setFrontendTip(short frontendTip) {
		this.frontendTip = frontendTip;
	}



	@Column(name="frontend_lang")
	public short getFrontendLang() {
		return frontendLang;
	}




	public void setFrontendLang(short frontendLang) {
		this.frontendLang = frontendLang;
	}



	@Column(name="component_version")
	public String getComponentVersion() {
		return componentVersion;
	}




	public void setComponentVersion(String componentVersion) {
		this.componentVersion = componentVersion;
	}




	@Column(name="code")
	public String getCode() {
		return code;
	}




	public void setCode(String code) {
		this.code = code;
	}



	@Column(name="css_code")
	public String getCssCode() {
		return cssCode;
	}




	public void setCssCode(String cssCode) {
		this.cssCode = cssCode;
	}




	@Column(name="js_code")
	public String getJsCode() {
		return jsCode;
	}

	public void setJsCode(String jsCode) {
		this.jsCode = jsCode;
	}




	@Transient
	public boolean safeEquals(W5Base q){
		return false;
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
		if(o==null || !(o instanceof W5Component))return false;
		W5Component c = (W5Component)o;
		return c!=null && c.getComponentId()==getComponentId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getComponentId();
	}

/*	@Column(name="lkp_component_type") */
	
	@Transient
	public short getLkpComponentType() {
		return lkpComponentType;
	}

	public void setLkpComponentType(short lkpComponentType) {
		this.lkpComponentType = lkpComponentType;
	}
	
	
}
