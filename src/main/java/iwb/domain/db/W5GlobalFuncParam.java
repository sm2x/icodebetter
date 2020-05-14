package iwb.domain.db;

// Generated Feb 25, 2007 1:41:05 PM by Hibernate Tools 3.2.0.b9

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;



@Entity
@Immutable
@Table(name="w5_db_func_param",schema="iwb")
public class W5GlobalFuncParam implements java.io.Serializable, W5Param, W5Base {
/*TABLE_ID: 21*/

	
	private static final long serialVersionUID = 193784239632947L;

	private int globalFuncParamId;

	private int globalFuncId;

	private String dsc;

	private short paramType;

	private short tabOrder;

	private short notNullFlag;

	private String defaultValue;

	private Short minLength;

	private Integer maxLength;

	private BigDecimal minValue;

	private BigDecimal maxValue;
	
	private short sourceType;

	private short outFlag;
	
	
	
	@Column(name="out_flag")
	public short getOutFlag() {
		return outFlag;
	}

	public void setOutFlag(short outFlag) {
		this.outFlag = outFlag;
	}

	public W5GlobalFuncParam() {
	}

	@Id
	@Column(name="db_func_param_id")
	public int getGlobalFuncParamId() {
		return this.globalFuncParamId;
	}

	public void setGlobalFuncParamId(int globalFuncParamId) {
		this.globalFuncParamId = globalFuncParamId;
	}

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

	@Column(name="param_tip")
	public short getParamType() {
		return this.paramType;
	}
	

	public void setParamType(short paramType) {
		this.paramType = paramType;
	}

	@Column(name="tab_order")
	public short getTabOrder() {
		return this.tabOrder;
	}

	public void setTabOrder(short tabOrder) {
		this.tabOrder = tabOrder;
	}

	@Column(name="default_value")
	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Column(name="min_length")
	public Short getMinLength() {
		return this.minLength;
	}

	public void setMinLength(Short minLength) {
		this.minLength = minLength;
	}

	@Column(name="max_length")
	public Integer getMaxLength() {
		return this.maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	@Column(name="min_value")
	public BigDecimal getMinValue() {
		return this.minValue;
	}

	public void setMinValue(BigDecimal minValue) {
		this.minValue = minValue;
	}

	@Column(name="max_value")
	public BigDecimal getMaxValue() {
		return this.maxValue;
	}

	public void setMaxValue(BigDecimal maxValue) {
		this.maxValue = maxValue;
	}


	@Column(name="source_tip")
	public short getSourceType() {
		return sourceType;
	}

	public void setSourceType(short sourceType) {
		this.sourceType = sourceType;
	}

	@Column(name="not_null_flag")
	public short getNotNullFlag() {
		return notNullFlag;
	}

	public void setNotNullFlag(short notNullFlag) {
		this.notNullFlag = notNullFlag;
	}

	public W5GlobalFuncParam(String dsc) {
		super();
		this.dsc = dsc;
		this.outFlag=(short)1;
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
		if(o==null || !(o instanceof W5GlobalFuncParam))return false;
		W5GlobalFuncParam c = (W5GlobalFuncParam)o;
		return c!=null && c.getGlobalFuncParamId()==getGlobalFuncParamId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getGlobalFuncParamId();
	}
	
	
	@Transient
	public int getParentId() {
		return 0;
	}
	
	public boolean safeEquals(W5Base q) {
		return false;
	}
	
}
