package iwb.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;
//import org.influxdb.InfluxDB;

@Entity
@Immutable
@Table(name="w5_project",schema="iwb")
public class W5Project  implements java.io.Serializable {
/*TABLE_ID: 1407*/

	private static final long serialVersionUID = 98981231241231L;
	private String projectUuid;
	private int customizationId;

	private String dsc;
	private String vcsUrl;
	private String vcsUserName;
	private String vcsPassword;
	
	private String rdbmsSchema;

	
	private short uiWebFrontendTip;
	private int uiMainTemplateId;
	private int uiLoginTemplateId;
	private int sessionQueryId;
	private int authenticationFuncId;
	private int _defaultRoleGroupId;
	private short _customFile;
	private short lkpDateFormat;
	
	private short localeMsgKeyFlag;
	private String locales;

	/*
	  ui_web_frontend_tip smallint NOT NULL DEFAULT 1,
	  ui_main_template_id integer NOT NULL DEFAULT 0,
	  session_query_id integer NOT NULL DEFAULT 0,
	  authentication_func_id integer NOT NULL DEFAULT 0,*/
	/*private Channel _mqChannel;
	private InfluxDB _tsdb;*/
	
	
	@Column(name="ui_login_template_id")
	public int getUiLoginTemplateId() {
		return uiLoginTemplateId;
	}
	public void setUiLoginTemplateId(int uiLoginTemplateId) {
		this.uiLoginTemplateId = uiLoginTemplateId;
	}
	
	@Column(name="ui_web_frontend_tip")
	public short getUiWebFrontendTip() {
		return uiWebFrontendTip;
	}
	public void setUiWebFrontendTip(short uiWebFrontendTip) {
		this.uiWebFrontendTip = uiWebFrontendTip;
	}
	@Column(name="ui_main_template_id")
	public int getUiMainTemplateId() {
		return uiMainTemplateId;
	}
	public void setUiMainTemplateId(int uiMainTemplateId) {
		this.uiMainTemplateId = uiMainTemplateId;
	}
	@Column(name="session_query_id")
	public int getSessionQueryId() {
		return sessionQueryId;
	}
	public void setSessionQueryId(int sessionQueryId) {
		this.sessionQueryId = sessionQueryId;
	}
	@Column(name="authentication_func_id")
	public int getAuthenticationFuncId() {
		return authenticationFuncId;
	}
	public void setAuthenticationFuncId(int authenticationFuncId) {
		this.authenticationFuncId = authenticationFuncId;
	}
	
	@Id
	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}
	
	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}
	
	@Column(name="customization_id")
	public int getCustomizationId() {
		return customizationId;
	}
	public void setCustomizationId(int customizationId) {
		this.customizationId = customizationId;
	}

	@Column(name="vcs_url")
	public String getVcsUrl() {
		return vcsUrl;
	}
	public void setVcsUrl(String vcsUrl) {
		this.vcsUrl = vcsUrl;
	}
	@Column(name="vcs_user_name")
	public String getVcsUserName() {
		return vcsUserName;
	}
	public void setVcsUserName(String vcsUserName) {
		this.vcsUserName = vcsUserName;
	}
	@Column(name="vcs_password")
	public String getVcsPassword() {
		return vcsPassword;
	}
	public void setVcsPassword(String vcsPassword) {
		this.vcsPassword = vcsPassword;
	}
	
	@Column(name="rdbms_schema")
	public String getRdbmsSchema() {
		return rdbmsSchema;
	}
	public void setRdbmsSchema(String rdbmsSchema) {
		this.rdbmsSchema = rdbmsSchema;
	}

	
	/*@Transient
	public InfluxDB get_tsdb() {
		return _tsdb;
	}
	public void set_tsdb(InfluxDB _tsdb) {
		this._tsdb = _tsdb;
	}*/
	

	@Column(name="dsc")
	public String getDsc() {
		return dsc;
	}
	public void setDsc(String dsc) {
		this.dsc = dsc;
	}
	
	@Transient
	public int get_defaultRoleGroupId() {
		return _defaultRoleGroupId;
	}
	public void set_defaultRoleGroupId(int _defaultRoleGroupId) {
		this._defaultRoleGroupId = _defaultRoleGroupId;
	}

	@Column(name="locale_msg_key_flag")
	public short getLocaleMsgKeyFlag() {
		return localeMsgKeyFlag;
	}
	public void setLocaleMsgKeyFlag(short localeMsgKeyFlag) {
		this.localeMsgKeyFlag = localeMsgKeyFlag;
	}
	@Column(name="locales")
	public String getLocales() {
		return locales;
	}
	public void setLocales(String locales) {
		this.locales = locales;
	}
	//@Transient
	@Column(name="lkp_date_format")
	public short getLkpDateFormat() {
		return lkpDateFormat;
	}
	public void setLkpDateFormat(short lkpDateFormat) {
		this.lkpDateFormat = lkpDateFormat;
	}
	
	@Transient
	public short get_customFile() {
		return _customFile;
	}
	public void set_customFile(short _customFile) {
		this._customFile = _customFile;
	}

}

