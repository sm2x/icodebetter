package iwb.domain.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;


@Entity
@Immutable
@Table(name="w5_form_module",schema="iwb")
public class W5FormModule implements java.io.Serializable, W5Base {

	/**
	 * 
	 */
	private static final long serialVersionUID = 100231238127L;
	private int formModuleId;
	private int formId;
	private String localeMsgKey;
	private short tabOrder;
	private short moduleTip;
	private short moduleViewTip;
	private int objectId;
	private int minRow;
	private int maxRow;
	
	
	
	
	@Id
	@Column(name="form_module_id")
	public int getFormModuleId() {
		return formModuleId;
	}
	public void setFormModuleId(int formModuleId) {
		this.formModuleId = formModuleId;
	}


	@Column(name="form_id")
	public int getFormId() {
		return formId;
	}
	public void setFormId(int formId) {
		this.formId = formId;
	}	
	
	@Column(name="locale_msg_key")
	public String getLocaleMsgKey() {
		return localeMsgKey;
	}
	public void setLocaleMsgKey(String localeMsgKey) {
		this.localeMsgKey = localeMsgKey;
	}
	@Column(name="tab_order")
	public short getTabOrder() {
		return tabOrder;
	}
	public void setTabOrder(short tabOrder) {
		this.tabOrder = tabOrder;
	}
	@Column(name="module_tip")
	public short getModuleTip() {
		return moduleTip;
	}
	public void setModuleTip(short moduleTip) {
		this.moduleTip = moduleTip;
	}

	
	@Column(name="object_id")
	public int getObjectId() {
		return objectId;
	}
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}

	/*
	@Transient
	public W5FormResult get_sourceFormResult() {
		return _sourceFormResult;
	}
	public void set_sourceFormResult(W5FormResult sourceFormResult) {
		_sourceFormResult = sourceFormResult;
	}*/
	@Column(name="module_view_tip")
	public short getModuleViewTip() {
		return moduleViewTip;
	}
	public void setModuleViewTip(short moduleViewTip) {
		this.moduleViewTip = moduleViewTip;
	}
	
	@Transient
	public boolean safeEquals(W5Base q){
		if(q==null)return false;
		W5FormModule f = (W5FormModule)q;
		/*return 
		private int formModuleId;
		private int formId;
		private String dsc;
		private String localeMsgKey;
		private short tabOrder;
		private short moduleTip;
		private short moduleViewTip;
		private int objectId;
		private short accessViewTip;
		private String accessViewRoles;
		private String accessViewUsers;
*/ return false;
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
	
	@Column(name="min_row")
	public int getMinRow() {
		return minRow;
	}
	public void setMinRow(int minRow) {
		this.minRow = minRow;
	}
	@Column(name="max_row")
	public int getMaxRow() {
		return maxRow;
	}
	public void setMaxRow(int maxRow) {
		this.maxRow = maxRow;
	}

	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5FormModule))return false;
		W5FormModule c = (W5FormModule)o;
		return c!=null && c.getFormModuleId()==getFormModuleId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getFormModuleId();
	}	
}
