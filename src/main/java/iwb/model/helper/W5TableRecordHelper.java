package iwb.model.helper;

import iwb.cache.FrameworkCache;
import iwb.cache.LocaleMsgCache;
import iwb.model.db.W5Table;


public class W5TableRecordHelper implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2149428019721007186L;
	private int tableId;
	private int tablePk;
	private int customizationId;
	private int parentTableId;
	private int parentTablePk;
	private int commentCount;
	private	long cachedTime;
	private	long lastAccessTime;
	private W5AccessControlHelper viewAccessControl;

	private String recordDsc;

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

	public int getCustomizationId() {
		return customizationId;
	}

	public void setCustomizationId(int customizationId) {
		this.customizationId = customizationId;
	}

	public String getRecordDsc() {
		return recordDsc;
	}

	public void setRecordDsc(String recordDsc) {
		this.recordDsc = recordDsc;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public int getParentTableId() {
		return parentTableId;
	}

	public void setParentTableId(int parentTableId) {
		this.parentTableId = parentTableId;
	}

	public int getParentTablePk() {
		return parentTablePk;
	}

	public void setParentTablePk(int parentTablePk) {
		this.parentTablePk = parentTablePk;
	}

	public long getCachedTime() {
		return cachedTime;
	}

	public void setCachedTime(long cachedTime) {
		this.cachedTime = cachedTime;
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}

	public W5AccessControlHelper getViewAccessControl() {
		return viewAccessControl;
	}

	public void setViewAccessControl(W5AccessControlHelper viewAccessControl) {
		this.viewAccessControl = viewAccessControl;
	}
	
	public String get_tableStr() {
		W5Table t = FrameworkCache.getTable(0, tableId);
		return (t!=null) ? LocaleMsgCache.get2(0, "tr", t.getDsc()) : "Not defined(" + tableId + ")";
	}
}
