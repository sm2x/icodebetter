package iwb.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import iwb.util.GenericUtil;

// StepId: 901: wait4 start approval
// 902: hierarchical
// 903: dynamic
@Entity
@Immutable
@Table(name = "w5_approval_step", schema = "iwb")
public class W5WorkflowStep implements java.io.Serializable, W5Base {
/*TABLE_ID: 390*/

	
	private static final long serialVersionUID = 22993388447511L;
	
	private int approvalStepSeqId;
	private int approvalStepId;
	private int workflowId;
	  private String dsc;
	  private String approvalRoles;
	  private String approvalUsers;
	  private String approvalSql;
	  private int onApproveStepId;
	  private Integer onApproveFormId;
	  private String onApproveStepSql;
	  private short returnFlag;
	  private int onReturnStepId;
	  private Integer onReturnFormId;
	  private String onReturnStepSql;
	  private String onRejectStepSql;
	  private Integer onRejectFormId;
	  private short finalStepFlag;
	  private String visibleFields;
	  private String updatableFields;
	
	
	  private short accessViewTip;
	  private String accessViewRoles;
	  private String accessViewUsers;
	  private String accessViewUserFields;
	
	  private short accessUpdateTip;
	  private String accessUpdateRoles;
	  private String accessUpdateUsers;
	  private String accessUpdateUserFields;
	
	  private short accessDeleteTip;
	  private String accessDeleteRoles;
	  private String accessDeleteUsers;
	  private String accessDeleteUserFields;
	
	
	  private short timeLimitFlag;
	  private int timeLimitDuration;
	  private int onTimeLimitExceedStepId;
	  private short timeLimitDurationTip;
	  private String onEscalationCode;
	
	  private String btnApproveLabel;
	  private String btnReturnLabel;
	  
	  private String approvalNotificationIds;
	  private String onApproveNotificationIds;
	  private String onReturnNotificationIds;
	  private String onRejectNotificationIds;
	  private String onEscalationNotificationIds;


  @Column(name = "approval_id")
  public int getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(int workflowId) {
    this.workflowId = workflowId;
  }

  @Id
  @Column(name = "approval_step_seq_id")
  public int getApprovalStepSeqId() {
    return approvalStepSeqId;
  }

  public void setApprovalStepSeqId(int approvalStepSeqId) {
    this.approvalStepSeqId = approvalStepSeqId;
  }

  @Column(name = "approval_step_id")
  public int getApprovalStepId() {
    return approvalStepId;
  }

  public void setApprovalStepId(int approvalStepId) {
    this.approvalStepId = approvalStepId;
  }

  @Column(name = "approval_roles")
  public String getApprovalRoles() {
    return approvalRoles;
  }

  public void setApprovalRoles(String approvalRoles) {
    this.approvalRoles = approvalRoles;
  }

  @Column(name = "approval_users")
  public String getApprovalUsers() {
    return approvalUsers;
  }

  public void setApprovalUsers(String approvalUsers) {
    this.approvalUsers = approvalUsers;
  }


  @Column(name = "on_approve_step_id")
  public int getOnApproveStepId() {
    return onApproveStepId;
  }

  public void setOnApproveStepId(int onApproveStepId) {
    this.onApproveStepId = onApproveStepId;
  }

  @Column(name = "return_flag")
  public short getReturnFlag() {
    return returnFlag;
  }

  public void setReturnFlag(short returnFlag) {
    this.returnFlag = returnFlag;
  }

  @Column(name = "access_view_tip")
  public short getAccessViewTip() {
    return accessViewTip;
  }

  @Column(name = "access_view_roles")
  public String getAccessViewRoles() {
    return accessViewRoles;
  }

  @Column(name = "access_view_users")
  public String getAccessViewUsers() {
    return accessViewUsers;
  }

  public void setAccessViewTip(short accessViewTip) {
    this.accessViewTip = accessViewTip;
  }

  public void setAccessViewRoles(String accessViewRoles) {
    this.accessViewRoles = accessViewRoles;
  }

  public void setAccessViewUsers(String accessViewUsers) {
    this.accessViewUsers = accessViewUsers;
  }

  @Column(name = "access_update_tip")
  public short getAccessUpdateTip() {
    return accessUpdateTip;
  }

  public void setAccessUpdateTip(short accessUpdateTip) {
    this.accessUpdateTip = accessUpdateTip;
  }

  @Column(name = "access_update_roles")
  public String getAccessUpdateRoles() {
    return accessUpdateRoles;
  }

  public void setAccessUpdateRoles(String accessUpdateRoles) {
    this.accessUpdateRoles = accessUpdateRoles;
  }

  @Column(name = "access_update_users")
  public String getAccessUpdateUsers() {
    return accessUpdateUsers;
  }

  public void setAccessUpdateUsers(String accessUpdateUsers) {
    this.accessUpdateUsers = accessUpdateUsers;
  }

  @Column(name = "access_delete_tip")
  public short getAccessDeleteTip() {
    return accessDeleteTip;
  }

  public void setAccessDeleteTip(short accessDeleteTip) {
    this.accessDeleteTip = accessDeleteTip;
  }

  @Column(name = "access_delete_roles")
  public String getAccessDeleteRoles() {
    return accessDeleteRoles;
  }

  public void setAccessDeleteRoles(String accessDeleteRoles) {
    this.accessDeleteRoles = accessDeleteRoles;
  }

  @Column(name = "access_delete_users")
  public String getAccessDeleteUsers() {
    return accessDeleteUsers;
  }

  public void setAccessDeleteUsers(String accessDeleteUsers) {
    this.accessDeleteUsers = accessDeleteUsers;
  }

  @Column(name = "dsc")
  public String getDsc() {
    return dsc;
  }

  public void setDsc(String dsc) {
    this.dsc = dsc;
  }

  @Column(name = "updatable_fields")
  public String getUpdatableFields() {
    return updatableFields;
  }

  public void setUpdatableFields(String updatableFields) {
    this.updatableFields = updatableFields;
  }


  @Column(name = "on_approve_step_sql")
  public String getOnApproveStepSql() {
    return onApproveStepSql;
  }

  public void setOnApproveStepSql(String onApproveStepSql) {
    this.onApproveStepSql = onApproveStepSql;
  }

  @Column(name = "on_reject_step_sql")
  public String getOnRejectStepSql() {
    return onRejectStepSql;
  }

  public void setOnRejectStepSql(String onRejectStepSql) {
    this.onRejectStepSql = onRejectStepSql;
  }

  @Column(name = "final_step_flag")
  public short getFinalStepFlag() {
    return finalStepFlag;
  }

  public void setFinalStepFlag(short finalStepFlag) {
    this.finalStepFlag = finalStepFlag;
  }

  @Column(name = "on_return_step_id")
  public int getOnReturnStepId() {
    return onReturnStepId;
  }

  public void setOnReturnStepId(int onReturnStepId) {
    this.onReturnStepId = onReturnStepId;
  }

  @Column(name = "on_return_step_sql")
  public String getOnReturnStepSql() {
    return onReturnStepSql;
  }

  public void setOnReturnStepSql(String onReturnStepSql) {
    this.onReturnStepSql = onReturnStepSql;
  }


  @Column(name = "access_view_user_fields")
  public String getAccessViewUserFields() {
    return accessViewUserFields;
  }

  public void setAccessViewUserFields(String accessViewUserFields) {
    this.accessViewUserFields = accessViewUserFields;
  }

  @Column(name = "access_update_user_fields")
  public String getAccessUpdateUserFields() {
    return accessUpdateUserFields;
  }

  public void setAccessUpdateUserFields(String accessUpdateUserFields) {
    this.accessUpdateUserFields = accessUpdateUserFields;
  }

  @Column(name = "access_delete_user_fields")
  public String getAccessDeleteUserFields() {
    return accessDeleteUserFields;
  }

  public void setAccessDeleteUserFields(String accessDeleteUserFields) {
    this.accessDeleteUserFields = accessDeleteUserFields;
  }

  @Transient
  public W5WorkflowStep getNewInstance() {
    W5WorkflowStep a = new W5WorkflowStep();
    a.approvalStepId = this.approvalStepId;
    a.approvalStepSeqId = this.approvalStepSeqId;
    a.workflowId = this.workflowId;
    a.dsc = this.dsc;
    a.approvalRoles = this.approvalRoles;
    a.approvalUsers = this.approvalUsers;
    a.onApproveStepId = this.onApproveStepId;
    a.onApproveStepSql = this.onApproveStepSql;
    a.returnFlag = this.returnFlag;
    a.onReturnStepId = this.onReturnStepId;
    a.onReturnStepSql = this.onReturnStepSql;
    a.onRejectStepSql = this.onRejectStepSql;
    a.finalStepFlag = this.finalStepFlag;
    a.accessViewTip = this.accessViewTip;
    a.accessViewRoles = this.accessViewRoles;
    a.accessViewUsers = this.accessViewUsers;
    a.accessViewUserFields = this.accessViewUserFields;
    a.accessUpdateTip = this.accessUpdateTip;
    a.accessUpdateRoles = this.accessUpdateRoles;
    a.accessUpdateUsers = this.accessUpdateUsers;
    a.accessUpdateUserFields = this.accessUpdateUserFields;
    a.accessDeleteTip = this.accessDeleteTip;
    a.accessDeleteRoles = this.accessDeleteRoles;
    a.accessDeleteUsers = this.accessDeleteUsers;
    a.accessDeleteUserFields = this.accessDeleteUserFields;

    a.timeLimitFlag = this.timeLimitFlag;
    a.timeLimitDuration = this.timeLimitDuration;
    a.onTimeLimitExceedStepId = this.onTimeLimitExceedStepId;
    a.timeLimitDurationTip = this.timeLimitDurationTip;
    a.onEscalationCode = this.onEscalationCode;
    a.visibleFields = this.visibleFields;
    a.updatableFields = this.updatableFields;
    a.btnApproveLabel = this.btnApproveLabel;
    a.btnReturnLabel = this.btnReturnLabel;
    
    a.onApproveFormId = this.onApproveFormId;
    a.onReturnFormId = this.onReturnFormId;
    a.onRejectFormId = this.onRejectFormId;
    
    a.approvalNotificationIds = this.approvalNotificationIds;
    a.onApproveNotificationIds = this.onApproveNotificationIds;
    a.onReturnNotificationIds = this.onReturnNotificationIds;
    a.onRejectNotificationIds = this.onRejectNotificationIds;
    a.onEscalationNotificationIds = this.onEscalationNotificationIds;
    
    a.approvalSql = this.approvalSql;
    return a;
  }

  @Transient
  public boolean safeEquals(W5Base q) {
    if (q == null) return false;
    W5WorkflowStep a = (W5WorkflowStep) q;
    return this.approvalStepId == a.approvalStepId
        && this.approvalStepSeqId == a.approvalStepSeqId
        && this.workflowId == a.workflowId
        && GenericUtil.safeEquals(this.dsc, a.dsc)
        && GenericUtil.safeEquals(this.approvalRoles, a.approvalRoles)
        && GenericUtil.safeEquals(this.approvalUsers, a.approvalUsers)
        && this.onApproveStepId == a.onApproveStepId
        && GenericUtil.safeEquals(this.onApproveStepSql, a.onApproveStepSql)
        && this.returnFlag == a.returnFlag
        && this.onReturnStepId == a.onReturnStepId
        && GenericUtil.safeEquals(this.onReturnStepSql, a.onReturnStepSql)
        && GenericUtil.safeEquals(this.onRejectStepSql, a.onRejectStepSql)
        && this.finalStepFlag == a.finalStepFlag
        && GenericUtil.safeEquals(this.updatableFields, a.updatableFields)
        && this.accessViewTip == a.accessViewTip
        && GenericUtil.safeEquals(this.accessViewRoles, a.accessViewRoles)
        && GenericUtil.safeEquals(this.accessViewUsers, a.accessViewUsers)
        && GenericUtil.safeEquals(this.accessViewUserFields, a.accessViewUserFields)
        && this.accessUpdateTip == a.accessUpdateTip
        && GenericUtil.safeEquals(this.accessUpdateRoles, a.accessUpdateRoles)
        && GenericUtil.safeEquals(this.accessUpdateUsers, a.accessUpdateUsers)
        && GenericUtil.safeEquals(this.accessUpdateUserFields, a.accessUpdateUserFields)
        && this.accessDeleteTip == a.accessDeleteTip
        && GenericUtil.safeEquals(this.accessDeleteRoles, a.accessDeleteRoles)
        && GenericUtil.safeEquals(this.accessDeleteUsers, a.accessDeleteUsers)
        && GenericUtil.safeEquals(this.accessDeleteUserFields, a.accessDeleteUserFields);
  }

  private String projectUuid;

  @Id
  @Column(name = "project_uuid")
  public String getProjectUuid() {
    return projectUuid;
  }

  public void setProjectUuid(String projectUuid) {
    this.projectUuid = projectUuid;
  }

  @Column(name = "on_approve_form_id")
  public Integer getOnApproveFormId() {
    return onApproveFormId;
  }

  public void setOnApproveFormId(Integer onApproveFormId) {
    this.onApproveFormId = onApproveFormId;
  }

  @Column(name = "on_return_form_id")
  public Integer getOnReturnFormId() {
    return onReturnFormId;
  }

  public void setOnReturnFormId(Integer onReturnFormId) {
    this.onReturnFormId = onReturnFormId;
  }

  @Column(name = "on_reject_form_id")
  public Integer getOnRejectFormId() {
    return onRejectFormId;
  }

  public void setOnRejectFormId(Integer onRejectFormId) {
    this.onRejectFormId = onRejectFormId;
  }

  @Column(name = "time_limit_flag")
  public short getTimeLimitFlag() {
    return timeLimitFlag;
  }

  public void setTimeLimitFlag(short timeLimitFlag) {
    this.timeLimitFlag = timeLimitFlag;
  }

  @Column(name = "time_limit_duration")
  public int getTimeLimitDuration() {
    return timeLimitDuration;
  }

  public void setTimeLimitDuration(int timeLimitDuration) {
    this.timeLimitDuration = timeLimitDuration;
  }

  @Column(name = "on_time_limit_exceed_step_id")
  public int getOnTimeLimitExceedStepId() {
    return onTimeLimitExceedStepId;
  }

  public void setOnTimeLimitExceedStepId(int onTimeLimitExceedStepId) {
    this.onTimeLimitExceedStepId = onTimeLimitExceedStepId;
  }

  @Column(name = "time_limit_duration_tip")
  public short getTimeLimitDurationTip() {
    return timeLimitDurationTip;
  }

  public void setTimeLimitDurationTip(short timeLimitDurationTip) {
    this.timeLimitDurationTip = timeLimitDurationTip;
  }

  @Column(name = "on_escalation_code")
  public String getOnEscalationCode() {
    return onEscalationCode;
  }

  public void setOnEscalationCode(String onEscalationCode) {
    this.onEscalationCode = onEscalationCode;
  }

  @Column(name = "visible_fields")
  public String getVisibleFields() {
    return visibleFields;
  }

  public void setVisibleFields(String visibleFields) {
    this.visibleFields = visibleFields;
  }

  @Column(name = "btn_approve_label")
  public String getBtnApproveLabel() {
    return btnApproveLabel;
  }

  public void setBtnApproveLabel(String btnApproveLabel) {
    this.btnApproveLabel = btnApproveLabel;
  }

  @Column(name = "btn_return_label")
  public String getBtnReturnLabel() {
    return btnReturnLabel;
  }

  public void setBtnReturnLabel(String btnReturnLabel) {
    this.btnReturnLabel = btnReturnLabel;
  }

  	@Column(name = "approval_notification_ids")
	public String getApprovalNotificationIds() {
		return approvalNotificationIds;
	}
	
	public void setApprovalNotificationIds(String approvalNotificationIds) {
		this.approvalNotificationIds = approvalNotificationIds;
	}
	
	@Column(name = "on_approve_notification_ids")
	public String getOnApproveNotificationIds() {
		return onApproveNotificationIds;
	}
	
	public void setOnApproveNotificationIds(String onApproveNotificationIds) {
		this.onApproveNotificationIds = onApproveNotificationIds;
	}
	
	@Column(name = "on_return_notification_ids")
	public String getOnReturnNotificationIds() {
		return onReturnNotificationIds;
	}
	
	public void setOnReturnNotificationIds(String onReturnNotificationIds) {
		this.onReturnNotificationIds = onReturnNotificationIds;
	}
	
	@Column(name = "on_reject_notification_ids")
	public String getOnRejectNotificationIds() {
		return onRejectNotificationIds;
	}
	
	public void setOnRejectNotificationIds(String onRejectNotificationIds) {
		this.onRejectNotificationIds = onRejectNotificationIds;
	}
	
	@Column(name = "on_escalation_notification_ids")
	public String getOnEscalationNotificationIds() {
		return onEscalationNotificationIds;
	}
	
	public void setOnEscalationNotificationIds(String onEscalationNotificationIds) {
		this.onEscalationNotificationIds = onEscalationNotificationIds;
	}
  
	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5WorkflowStep))return false;
		W5WorkflowStep c = (W5WorkflowStep)o;
		return c!=null && c.getApprovalStepSeqId()==getApprovalStepSeqId() && c.getProjectUuid().equals(projectUuid);
	}
	
	@Column(name = "approval_sql")
	public String getApprovalSql() {
		return approvalSql;
	}

	public void setApprovalSql(String approvalSql) {
		this.approvalSql = approvalSql;
	}

	public int hashCode() {
		return projectUuid.hashCode() + 100*getApprovalStepSeqId();
	}	  
}
