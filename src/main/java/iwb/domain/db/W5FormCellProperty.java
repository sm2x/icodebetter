package iwb.domain.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "W5_FORM_CELL_PROPERTY",schema="iwb")
public class W5FormCellProperty implements java.io.Serializable, W5Base {
/*TABLE_ID: 5621*/
	
	private static final long serialVersionUID = 129834567825321L;
	private int formCellProperyId;
	private int formCellId;
	private int relatedFormCellId;
	private String val;
	private short lkpPropertyType;
	private short lkpOperatorType;
	private short otherSetValueFlag;
	private String otherValue;


	
	@Id
	@Column(name = "FORM_CELL_PROPERTY_ID")
	public int getFormCellPropertyId() {
		return formCellProperyId;
	}

	public void setFormCellPropertyId(int formCellProperyId) {
		this.formCellProperyId = formCellProperyId;
	}

	@Column(name = "RELATED_FORM_CELL_ID")
	public int getRelatedFormCellId() {
		return relatedFormCellId;
	}

	public void setRelatedFormCellId(int relatedFormCellId) {
		this.relatedFormCellId = relatedFormCellId;
	}

	@Column(name = "VAL")
	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	@Column(name = "LKP_PROPERTY_TIP")
	public short getLkpPropertyType() {
		return lkpPropertyType;
	}

	public void setLkpPropertyType(short lkpPropertyType) {
		this.lkpPropertyType = lkpPropertyType;
	}

	@Column(name = "LKP_OPERATOR_TIP")
	public short getLkpOperatorType() {
		return lkpOperatorType;
	}

	public void setLkpOperatorType(short lkpOperatorType) {
		this.lkpOperatorType = lkpOperatorType;
	}

	@Id
	@Column(name = "FORM_CELL_ID")
	public int getFormCellId() {
		return formCellId;
	}

	public void setFormCellId(int formCellId) {
		this.formCellId = formCellId;
	}


	public W5FormCellProperty(int formCellId) {
		super();
		this.formCellId = formCellId;
	}

	public W5FormCellProperty() {
		super();
	}

	@Transient
	public boolean safeEquals(W5Base q) {

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

	@Column(name="other_set_value_flag")
	public short getOtherSetValueFlag() {
		return otherSetValueFlag;
	}

	public void setOtherSetValueFlag(short otherSetValueFlag) {
		this.otherSetValueFlag = otherSetValueFlag;
	}

	
	@Column(name="other_value")
	public String getOtherValue() {
		return otherValue;
	}

	public void setOtherValue(String otherValue) {
		this.otherValue = otherValue;
	}

	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5FormCellProperty))return false;
		W5FormCellProperty c = (W5FormCellProperty)o;
		return c!=null && c.getFormCellPropertyId()==getFormCellPropertyId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getFormCellPropertyId();
	}	
	
}
