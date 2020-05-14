package iwb.engine;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import iwb.cache.FrameworkCache;
import iwb.cache.LocaleMsgCache;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.domain.db.W5Table;
import iwb.domain.db.W5Workflow;
import iwb.domain.db.W5WorkflowRecord;
import iwb.domain.db.W5WorkflowStep;
import iwb.domain.result.W5FormResult;
import iwb.exception.IWBException;
import iwb.util.GenericUtil;

@Component
public class AccessControlEngine {
	@Lazy
	@Autowired
	private PostgreSQL dao;


	@Lazy
	@Autowired
	private QueryEngine queryEngine;
	
	public void accessControl4FormTable(W5FormResult formResult, String paramSuffix) { // TODO:
		// yukariya
		// yonlendirilmesi
		// lazim
		int formId = formResult.getFormId();
		int action = formResult.getAction();
		Map<String, Object> scd = formResult.getScd();
		Map<String, String> requestParams = formResult.getRequestParams();

		W5Table t = FrameworkCache.getTable(scd, formResult.getForm().getObjectId()); // formResult.getForm().get_sourceTable();
		if (t == null)
			return; // TODO

		W5Workflow app = null;
		W5WorkflowRecord appRecord = null;
		W5WorkflowStep approvalStep = null;
		if ((action == 1 || action == 3) && t.get_approvalMap() != null && !t.get_approvalMap().isEmpty()) {
			List<W5WorkflowRecord> ll = dao.find(
					"from W5WorkflowRecord t where t.projectUuid=?0 AND t.tableId=?1 AND t.tablePk=?2",
					scd.get("projectId"), t.getTableId(), GenericUtil.uInt(requestParams
							.get(t.get_tableParamList().get(0).getDsc() + (paramSuffix != null ? paramSuffix : ""))));
			if (!ll.isEmpty()) {
				appRecord = ll.get(0);
				app = FrameworkCache.getWorkflow(scd, appRecord.getApprovalId());

				if (appRecord.getApprovalStepId() > 0 && appRecord.getApprovalStepId() != 998 && appRecord.getApprovalStepId() != 999) {
					if (app != null) {
						approvalStep = app.get_approvalStepMap().get(appRecord.getApprovalStepId());
						if(approvalStep!=null) {
							approvalStep = approvalStep.getNewInstance();
							formResult.setApprovalStep(approvalStep);
						}
					}
				}
			}
		}
		// edit veya delete isleminde, accessViewControl by userFields control
		// var mi?
		if ((action == 1 || action == 3)
				&& (t.getAccessViewTip() != 0 && t.getAccessViewUserFields() != null
						&& !GenericUtil.accessControl(scd, t.getAccessViewTip(), t.getAccessViewRoles(),
								t.getAccessViewUsers())
				&& (t.getAccessViewUserFields() != null && t.getAccessViewUserFields().length() > 0 && dao
						.accessUserFieldControl(t, t.getAccessViewUserFields(), scd, requestParams, paramSuffix)))) { // bu
			// field'da
			throw new IWBException("security", "Form", formId, null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_access_wf_control_view"), null);
		}
		boolean updatableUserFieldFlag = false;
		boolean deletableUserFieldFlag = false;
		switch (action) {
		case 1: // update
			if (t.getAccessUpdateTip() != 0 && t.getAccessUpdateUserFields() != null
					&& t.getAccessUpdateUserFields().length() > 0 && !GenericUtil.accessControl(scd,
							t.getAccessUpdateTip(), t.getAccessUpdateRoles(), t.getAccessUpdateUsers())) { // bu
				// field'da
				if (dao.accessUserFieldControl(t, t.getAccessUpdateUserFields(), scd, requestParams, paramSuffix)) {
					formResult.getOutputMessages().add(
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_access_wf_control_update"));
					formResult.setViewMode(true);
				} else
					updatableUserFieldFlag = true;
			}

			if (appRecord != null) { // eger
				// bir
				// approval
				// sureci
				// icindeyse
				if (appRecord.getApprovalStepId() == 999) {//rejected
					formResult.getOutputMessages().add(
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_wf_control_red_kayit_update"));
					formResult.setViewMode(true);
				} else if(appRecord.getApprovalStepId() == 998) {//approved
					if(GenericUtil.isEmpty(app.getAfterFinUpdateUserIds()) || GenericUtil.hasPartInside2(app.getAfterFinUpdateUserIds(), scd.get("userId"))) {
						formResult.getOutputMessages().add(
								LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_wf_control_red_kayit_update"));
						formResult.setViewMode(true);
					}
					
				} else if (!GenericUtil.accessControl(scd, appRecord.getAccessViewTip(), appRecord.getAccessViewRoles(),
						appRecord.getAccessViewUsers())
						|| (approvalStep != null && ((!GenericUtil.accessControl(scd, approvalStep.getAccessUpdateTip(),
								approvalStep.getAccessUpdateRoles(), approvalStep.getAccessUpdateUsers())
								/*&& approvalStep.getAccessUpdateUserFields() == null*/)
								|| (approvalStep.getAccessUpdateUserFields() != null && dao.accessUserFieldControl(t,
										approvalStep.getAccessUpdateUserFields(), scd, requestParams, paramSuffix))))) {
					// if(appRecord.getApprovalStepId()!=1 ||
					// appRecord.getInsertUserId()!=(Integer)scd.get("userId")){//eger
					// daha ilk asamada ve
					// kaydi kaydeden bunu yapmaya calisirsa, Veli Abi bu ne ya
					// yapma yapma
					// throw new PromisException("security","Workflow",
					// appRecord.getApprovalId(),
					// null, "Onay süreci içerisinde bu kaydı
					// Güncelleyemezsiniz", null);
					formResult.getOutputMessages()
							.add(LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_wf_control_surec_devam"));
					formResult.setViewMode(true);
					// }
				}
				formResult.setApprovalRecord(appRecord);
			} else {
				// bu table'a update hakki var mi?
				if (!updatableUserFieldFlag && !GenericUtil.accessControl(scd, t.getAccessUpdateTip(),
						t.getAccessUpdateRoles(), t.getAccessUpdateUsers())) {
					// throw new PromisException("security","Form", formId,
					// null,
					// PromisLocaleMsg.get2(0,(String)scd.get("locale"),"fw_access_table_control_update"),
					// null);
					formResult.getOutputMessages().add(
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_access_table_control_update"));
					formResult.setViewMode(true);
				}

				
				formResult.setApprovalRecord(appRecord);
			}
			break;
		case 2: // insert
			if (!GenericUtil.accessControl(scd, t.getAccessInsertTip(), t.getAccessInsertRoles(),
					t.getAccessInsertUsers())) { // Table access insert control
				throw new IWBException("security", "Form", formId, null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_access_table_control_record_insert"),
						null);
			}

			break;
		case 3: // delete
			if (t.getAccessDeleteTip() != 0 && !GenericUtil.isEmpty(t.getAccessDeleteUserFields()) && !GenericUtil
					.accessControl(scd, t.getAccessDeleteTip(), t.getAccessDeleteRoles(), t.getAccessDeleteUsers())) { // bu
				// field'da
				if (dao.accessUserFieldControl(t, t.getAccessDeleteUserFields(), scd, requestParams, paramSuffix)) {
					formResult.getOutputMessages()
							.add(LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_access_wf_control_delete"));
					formResult.setViewMode(true);
				} else
					deletableUserFieldFlag = true;
			}
			/*
			 * if(t.getAccessDeleteTip()!=0 &&
			 * t.getAccessDeleteUserFields()!=null &&
			 * t.getAccessDeleteUserFields().length()>0){ // bu field'da
			 * if(dao.accessUserFieldControl(t, t.getAccessDeleteUserFields(),
			 * scd, requestParams, paramSuffix)) throw new
			 * PromisException("security","Form", formId, null,
			 * PromisLocaleMsg.get2(0,(String)scd.get("locale"),
			 * "fw_access_silinemez_kullanici_alan_kisit"), null); }
			 */

			if (appRecord != null) { // eger bir approval sureci icindeyse
				if (!GenericUtil.accessControl(scd, appRecord.getAccessViewTip(), appRecord.getAccessViewRoles(),
						appRecord.getAccessViewUsers())
						|| (approvalStep != null && !GenericUtil.accessControl(scd, approvalStep.getAccessDeleteTip(),
								approvalStep.getAccessDeleteRoles(), approvalStep.getAccessDeleteUsers()))) {
					throw new IWBException("security",
							"Workflow", appRecord.getApprovalId(), null, LocaleMsgCache.get2(0,
									(String) scd.get("locale"), "fw_wf_sureci_icerisindeki_kaydi_silemezsiniz"),
							null);
				}
			} else {
				// bu table'a delete hakki var mi?
				if (!deletableUserFieldFlag && !GenericUtil.accessControl(scd, t.getAccessDeleteTip(),
						t.getAccessDeleteRoles(), t.getAccessDeleteUsers())) {
					throw new IWBException("security", "Form", formId, null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_access_table_control_delete"),
							null);
				} /*
					 * if(!PromisUtil.accessControl(scd, t.getAccessDeleteTip(),
					 * t.getAccessDeleteRoles(), t.getAccessDeleteUsers())){
					 * throw new PromisException("security","Form", formId,
					 * null, PromisLocaleMsg.get2(0,(String)scd.get("locale"),
					 * "fw_access_table_control_delete"), null); }
					 */


			}
		}
		formResult.setApprovalRecord(appRecord);
	}


	
}
