package iwb.model.helper;




public class W5SyncRecordHelper3 {
	private	long lastProcessTime;
	private String tabId;
	private short syncTip;
	private short active;
	private short dirty;
	private	W5DeferredResult deferredResult;
	
	
	public W5SyncRecordHelper3(String tabId, long lastProcessTime, short syncTip,
			short active, W5DeferredResult deferredResult) {
		super();
		this.tabId = tabId;
		this.lastProcessTime = lastProcessTime;
		this.syncTip = syncTip;
		this.active = active;
		this.deferredResult = deferredResult;
	}
	public W5SyncRecordHelper3() {
		super();
	}
	public long getLastProcessTime() {
		return lastProcessTime;
	}
	public void setLastProcessTime(long lastProcessTime) {
		this.lastProcessTime = lastProcessTime;
	}
	public short getSyncTip() {
		return syncTip;
	}
	public void setSyncTip(short syncTip) {
		this.syncTip = syncTip;
	}
	public W5DeferredResult getDeferredResult() {
		return deferredResult;
	}
	public void setDeferredResult(W5DeferredResult deferredResult) {
		this.deferredResult = deferredResult;
	}
	public short getActive() {
		return active;
	}
	public void setActive(short active) {
		this.active = active;
	}
	public String getTabId() {
		return tabId;
	}
	public void setTabId(String tabId) {
		this.tabId = tabId;
	}
	public short getDirty() {
		return dirty;
	}
	public void setDirty(short dirty) {
		this.dirty = dirty;
	}

}
