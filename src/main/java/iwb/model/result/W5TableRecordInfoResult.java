package iwb.model.result;

import java.util.List;
import java.util.Map;

import iwb.model.helper.W5TableChildHelper;
import iwb.model.helper.W5TableRecordHelper;

public class W5TableRecordInfoResult  implements W5MetaResult{
	private int tableId;
	private int tablePk;
	private	int versionNo;
	private	int insertUserId;
	private	int versionUserId;
	private	String insertDttm;
	private	String versionDttm;

	private Map<String, Object> scd;
	private List<W5TableRecordHelper> parentList;
	private	int commentCount;
	private	int fileAttachmentCount;
	private	int allFileAttachmentCount;
	private	int conversionCount;
	private	int formMailSmsCount;
	private	int accessControlCount;
	
	private List<W5TableChildHelper> childList;

	public int getTableId() {
		return tableId;
	}
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
	public int getTablePk() {
		return tablePk;
	}
	public void setTablePk(int tablePk) {
		this.tablePk = tablePk;
	}
	public Map<String, Object> getScd() {
		return scd;
	}
	public void setScd(Map<String, Object> scd) {
		this.scd = scd;
	}


	public List<W5TableRecordHelper> getParentList() {
		return parentList;
	}
	public void setParentList(List<W5TableRecordHelper> parentList) {
		this.parentList = parentList;
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
	public int getAllFileAttachmentCount() {
		return allFileAttachmentCount;
	}
	public void setAllFileAttachmentCount(int allFileAttachmentCount) {
		this.allFileAttachmentCount = allFileAttachmentCount;
	}
	public List<W5TableChildHelper> getChildList() {
		return childList;
	}
	public void setChildList(List<W5TableChildHelper> childList) {
		this.childList = childList;
	}
	public W5TableRecordInfoResult(Map<String, Object> scd,int tableId, int tablePk) {
		super();
		this.tableId = tableId;
		this.tablePk = tablePk;
		this.scd = scd;
	}
	public int getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}
	public int getInsertUserId() {
		return insertUserId;
	}
	public void setInsertUserId(int insertUserId) {
		this.insertUserId = insertUserId;
	}
	public int getVersionUserId() {
		return versionUserId;
	}
	public void setVersionUserId(int versionUserId) {
		this.versionUserId = versionUserId;
	}
	public String getInsertDttm() {
		return insertDttm;
	}
	public void setInsertDttm(String insertDttm) {
		this.insertDttm = insertDttm;
	}
	public String getVersionDttm() {
		return versionDttm;
	}
	public void setVersionDttm(String versionDttm) {
		this.versionDttm = versionDttm;
	}
	public int getConversionCount() {
		return conversionCount;
	}
	public void setConversionCount(int conversionCount) {
		this.conversionCount = conversionCount;
	}
	public int getFormMailSmsCount() {
		return formMailSmsCount;
	}
	public void setFormMailSmsCount(int formMailSmsCount) {
		this.formMailSmsCount = formMailSmsCount;
	}

	public int getAccessControlCount() {
		return accessControlCount;
	}
	public void setAccessControlCount(int accessControlCount) {
		this.accessControlCount = accessControlCount;
	}

	public Map<String,String> getRequestParams(){return null;};
}
