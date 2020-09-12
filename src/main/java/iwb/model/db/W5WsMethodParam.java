package iwb.model.db;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name="w5_ws_method_param",schema="iwb")
public class W5WsMethodParam  implements java.io.Serializable, W5Param, W5Base {
/*TABLE_ID: 1377*/


	private static final long serialVersionUID = 18172625342L;
	
	private int wsMethodParamId;
	private int wsMethodId;
	private int parentWsMethodParamId;
	private String dsc;
	private short paramType;//integer, string etc
	private short outFlag;
	private short paramSendType;
	private short notNullFlag;
	private short tabOrder;
	private short sourceType;

	private String defaultValue;
	private	BigDecimal	minValue;
	private	BigDecimal	maxValue;
	
	private	Short	minLength;
	private	Integer	maxLength;

	@Id
	@Column(name="ws_method_param_id")
	public int getWsMethodParamId() {
		return wsMethodParamId;
	}
	public void setWsMethodParamId(int wsMethodParamId) {
		this.wsMethodParamId = wsMethodParamId;
	}
	@Column(name="param_tip")
	public short getParamType() {
		return paramType;
	}
	public void setParamType(short paramType) {
		this.paramType = paramType;
	}
	@Column(name="ws_method_id")
	public int getWsMethodId() {
		return wsMethodId;
	}
	public void setWsMethodId(int wsMethodId) {
		this.wsMethodId = wsMethodId;
	}

	@Column(name="dsc")
	public String getDsc() {
		return dsc;
	}
	public void setDsc(String dsc) {
		this.dsc = dsc;
	}
	
	@Column(name="out_flag")
	public short getOutFlag() {
		return outFlag;
	}
	public void setOutFlag(short outFlag) {
		this.outFlag = outFlag;
	}
	
	@Column(name="not_null_flag")
	public short getNotNullFlag() {
		return notNullFlag;
	}
	public void setNotNullFlag(short notNullFlag) {
		this.notNullFlag = notNullFlag;
	}
	
	@Column(name="tab_order")
	public short getTabOrder() {
		return tabOrder;
	}
	public void setTabOrder(short tabOrder) {
		this.tabOrder = tabOrder;
	}

	@Column(name="min_value")
	public BigDecimal getMinValue() {
		return minValue;
	}

	public void setMinValue(BigDecimal minValue) {
		this.minValue = minValue;
	}

	@Column(name="max_value")
	public BigDecimal getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(BigDecimal maxValue) {
		this.maxValue = maxValue;
	}

	@Column(name="min_length")
	public Short getMinLength() {
		return minLength;
	}
	
	public void setMinLength(Short minLength) {
		this.minLength = minLength;
	}

	@Column(name="max_length")
	public Integer getMaxLength() {
		return maxLength;
	}


	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}
	@Column(name="source_tip")
	public short getSourceType() {
		return sourceType;
	}

	public void setSourceType(short sourceType) {
		this.sourceType = sourceType;
	}

	
	@Column(name="default_value")
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	@Column(name="parent_ws_method_param_id")
	public int getParentWsMethodParamId() {
		return parentWsMethodParamId;
	}
	public void setParentWsMethodParamId(int parentWsMethodParamId) {
		this.parentWsMethodParamId = parentWsMethodParamId;
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
	@Column(name="credentials_flag")
	public short getParamSendType() {
		return paramSendType;
	}
	public void setParamSendType(short paramSendType) {
		this.paramSendType = paramSendType;
	}

	
	
	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5WsMethodParam))return false;
		W5WsMethodParam c = (W5WsMethodParam)o;
		return c!=null && c.getWsMethodParamId()==getWsMethodParamId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getWsMethodParamId();
	}
	
	@Transient
	public int getParentId() {
		return parentWsMethodParamId;
	}	


	@Transient
	public boolean safeEquals(W5Base q) {

			return false;
	}
}
