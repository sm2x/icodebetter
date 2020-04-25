package iwb.domain.result;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import iwb.domain.db.W5ConvertedObject;
import iwb.domain.db.W5Form;
import iwb.domain.db.W5FormCell;
import iwb.domain.db.W5FormSmsMailAlarm;
import iwb.domain.db.W5WorkflowRecord;
import iwb.domain.db.W5WorkflowStep;
import iwb.domain.helper.W5FormCellHelper;
import iwb.domain.helper.W5QueuedActionHelper;
import iwb.domain.helper.W5QueuedPushMessageHelper;
import iwb.domain.helper.W5SynchAfterPostHelper;


public class W5FormResult implements W5MetaResult{

	private	int	formId;
    private int action;
    private int	versionNo = 0;
    private short objectTip;
    
	private	W5Form form;
	private	List<W5FormCellHelper>	formCellResults;	
    private Map<String, Object> scd;
	private	Map<String, String>	errorMap;
	private Map<String,String> requestParams;
	private Map<String,Object> outputFields;
	private Map<String,Object> pkFields;
	private	List<String> outputMessages;
    private	W5WorkflowRecord approvalRecord;
    private	W5WorkflowStep approvalStep;
	private	int	commentCount;
	private	String	commentExtraInfo;
	private	int	fileAttachmentCount;
	private	int	accessControlCount;
	private	int	mailSettingId;
	private	boolean	viewMode;
	private String liveSyncKey;
	private Map<Integer,W5FormResult> moduleFormMap;
	private	W5QueryResult queryResult4FormCell;
	private	List<W5FormCell> extraFormCells;
	private Map<Integer,W5GridResult> moduleGridMap;
	private Map<Integer,M5ListResult> moduleListMap;
	private String uniqueId;
	private List<W5QueuedActionHelper> queuedActionList;
	private List<W5QueuedPushMessageHelper> queuedPushMessageList;
	private List<Map<String, String>> previewMapList;
	private List<Map<String, String>> previewConversionMapList;
	private Map<Integer, List<W5ConvertedObject>> mapConvertedObject;
	private List<W5FormSmsMailAlarm> formAlarmList;

	public Map<Integer, M5ListResult> getModuleListMap() {
		return moduleListMap;
	}
	public void setModuleListMap(Map<Integer, M5ListResult> moduleListMap) {
		this.moduleListMap = moduleListMap;
	}
	public W5FormResult(int formId) {
		this.formId=formId;
	}
	public int getFormId() {
		return formId;
	}
	public void setFormId(int formId) {
		this.formId = formId;
	}
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	public int getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}
	public W5Form getForm() {
		return form;
	}
	public void setForm(W5Form form) {
		this.form = form;
	}
	public List<W5FormCellHelper> getFormCellResults() {
		return formCellResults;
	}
	public void setFormCellResults(List<W5FormCellHelper> formCellResults) {
		this.formCellResults = formCellResults;
	}
	public Map<String, Object> getScd() {
		return scd;
	}
	public void setScd(Map<String, Object> scd) {
		this.scd = scd;
	}
	public Map<String, String> getErrorMap() {
		return errorMap;
	}
	public void setErrorMap(Map<String, String> errorMap) {
		this.errorMap = errorMap;
	}
	public Map<String, String> getRequestParams() {
		return requestParams;
	}
	public void setRequestParams(Map<String, String> requestParams) {
		this.requestParams = requestParams;
	}
/*	public String getPkJson() {
		return pkJson;
	}
	public void setPkJson(String pkJson) {
		this.pkJson = pkJson;
	} */

	public Map<String, Object> getOutputFields() {
		return outputFields;
	}
	public void setOutputFields(Map<String, Object> outputFields) {
		this.outputFields = outputFields;
	}
	public Map<String, Object> getPkFields() {
		return pkFields;
	}
	public void setPkFields(Map<String, Object> pkFields) {
		this.pkFields = pkFields;
	}
	public short getObjectTip() {
		return objectTip;
	}
	public void setObjectTip(short objectTip) {
		this.objectTip = objectTip;
	}
	public List<String> getOutputMessages() {
		return outputMessages;
	}
	public void setOutputMessages(List<String> outputMessages) {
		this.outputMessages = outputMessages;
	}
	public W5WorkflowRecord getApprovalRecord() {
		return approvalRecord;
	}
	public void setApprovalRecord(W5WorkflowRecord approvalRecord) {
		this.approvalRecord = approvalRecord;
	}
	public int getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
	public int getFileAttachmentCount() {
		return fileAttachmentCount;
	}
	public void setFileAttachmentCount(int fileAttachmentCount) {
		this.fileAttachmentCount = fileAttachmentCount;
	}
	public boolean isViewMode() {
		return viewMode;
	}
	public void setViewMode(boolean viewMode) {
		this.viewMode = viewMode;
	}
	public int getAccessControlCount() {
		return accessControlCount;
	}
	public void setAccessControlCount(int accessControlCount) {
		this.accessControlCount = accessControlCount;
	}
	public Map<Integer, W5FormResult> getModuleFormMap() {
		return moduleFormMap;
	}
	public void setModuleFormMap(Map<Integer, W5FormResult> moduleFormMap) {
		this.moduleFormMap = moduleFormMap;
	}
	public int getMailSettingId() {
		return mailSettingId;
	}
	public void setMailSettingId(int mailSettingId) {
		this.mailSettingId = mailSettingId;
	}
	public W5QueryResult getQueryResult4FormCell() {
		return queryResult4FormCell;
	}
	public void setQueryResult4FormCell(W5QueryResult queryResult4FormCell) {
		this.queryResult4FormCell = queryResult4FormCell;
	}
	public List<W5FormCell> getExtraFormCells() {
		return extraFormCells;
	}
	public void setExtraFormCells(List<W5FormCell> extraFormCells) {
		this.extraFormCells = extraFormCells;
	}
	public Map<Integer, W5GridResult> getModuleGridMap() {
		return moduleGridMap;
	}
	public void setModuleGridMap(Map<Integer, W5GridResult> moduleGridMap) {
		this.moduleGridMap = moduleGridMap;
	}
	public String getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	public List<W5QueuedActionHelper> getQueueActionList() {
		return queuedActionList;
	}
	public void setQueuedActionList(List<W5QueuedActionHelper> queuedDbFuncList) {
		this.queuedActionList = queuedDbFuncList;
	}
	public W5WorkflowStep getApprovalStep() {
		return approvalStep;
	}
	public void setApprovalStep(W5WorkflowStep approvalStep) {
		this.approvalStep = approvalStep;
	}

	public List<Map<String, String>> getPreviewMapList() {
		return previewMapList;
	}
	public void setPreviewMapList(List<Map<String, String>> previewMapList) {
		this.previewMapList = previewMapList;
	}
	public List<Map<String, String>> getPreviewConversionMapList() {
		return previewConversionMapList;
	}
	public void setPreviewConversionMapList(
			List<Map<String, String>> previewConversionMapList) {
		this.previewConversionMapList = previewConversionMapList;
	}
	public Map<Integer, List<W5ConvertedObject>> getMapConvertedObject() {
		return mapConvertedObject;
	}
	public void setMapConvertedObject(
			Map<Integer, List<W5ConvertedObject>> mapConvertedObject) {
		this.mapConvertedObject = mapConvertedObject;
	}
	public List<W5QueuedPushMessageHelper> getQueuedPushMessageList() {
		return queuedPushMessageList;
	}
	public void setQueuedPushMessageList(
			List<W5QueuedPushMessageHelper> queuedPushMessageList) {
		this.queuedPushMessageList = queuedPushMessageList;
	}
	public List<W5FormSmsMailAlarm> getFormAlarmList() {
		return formAlarmList;
	}
	public void setFormAlarmList(List<W5FormSmsMailAlarm> formAlarmList) {
		this.formAlarmList = formAlarmList;
	}
	public String getLiveSyncKey() {
		return liveSyncKey;
	}
	public void setLiveSyncKey(String liveSyncKey) {
		this.liveSyncKey = liveSyncKey;
	}
	public String getCommentExtraInfo() {
		return commentExtraInfo;
	}
	public void setCommentExtraInfo(String commentExtraInfo) {
		this.commentExtraInfo = commentExtraInfo;
	}
	
	private	List<W5SynchAfterPostHelper> listSyncAfterPostHelper;
	
	public void addSyncRecordAll(List<W5SynchAfterPostHelper> l) {
		if(l==null)return;
		if(listSyncAfterPostHelper==null)listSyncAfterPostHelper=new ArrayList<W5SynchAfterPostHelper>();
		listSyncAfterPostHelper.addAll(l);
	}
	public void addSyncRecord(W5SynchAfterPostHelper r) {
		if(listSyncAfterPostHelper==null)listSyncAfterPostHelper=new ArrayList<W5SynchAfterPostHelper>();
		listSyncAfterPostHelper.add(r);
	}
	public List<W5SynchAfterPostHelper> getListSyncAfterPostHelper() {
		return listSyncAfterPostHelper;
	}
	public void setListSyncAfterPostHelper(
			List<W5SynchAfterPostHelper> listSyncAfterPostHelper) {
		this.listSyncAfterPostHelper = listSyncAfterPostHelper;
	}

	
}

