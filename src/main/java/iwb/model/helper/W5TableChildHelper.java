package iwb.model.helper;

import iwb.model.db.W5TableChild;


public class W5TableChildHelper implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2368339249341046757L;
	private int childCount;
	private int totalCommentCount;
	private int totalFileAttachmentCount;
	private W5TableChild tableChild;


	public int getChildCount() {
		return childCount;
	}

	public void setChildCount(int childCount) {
		this.childCount = childCount;
	}

	public int getTotalCommentCount() {
		return totalCommentCount;
	}

	public void setTotalCommentCount(int totalCommentCount) {
		this.totalCommentCount = totalCommentCount;
	}

	public int getTotalFileAttachmentCount() {
		return totalFileAttachmentCount;
	}

	public void setTotalFileAttachmentCount(int totalFileAttachmentCount) {
		this.totalFileAttachmentCount = totalFileAttachmentCount;
	}

	public W5TableChild getTableChild() {
		return tableChild;
	}

	public void setTableChild(W5TableChild tableChild) {
		this.tableChild = tableChild;
	}

	public W5TableChildHelper(W5TableChild tableChild, int childCount, int totalCommentCount,
			int totalFileAttachmentCount) {
		super();
		this.childCount = childCount;
		this.totalCommentCount = totalCommentCount;
		this.totalFileAttachmentCount = totalFileAttachmentCount;
		this.tableChild = tableChild;
	}



}
