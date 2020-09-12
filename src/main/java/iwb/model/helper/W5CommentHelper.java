package iwb.model.helper;

import java.util.Map;


public class W5CommentHelper implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7397127874720193908L;
	private int insertUserId;
	private long insertTime;
	private String dsc;
	
	public int getInsertUserId() {
		return insertUserId;
	}
	public void setInsertUserId(int insertUserId) {
		this.insertUserId = insertUserId;
	}
	public long getInsertTime() {
		return insertTime;
	}
	public void setInsertTime(long insertTime) {
		this.insertTime = insertTime;
	}
	public String getDsc() {
		return dsc;
	}
	public void setDsc(String dsc) {
		this.dsc = dsc;
	}
	public W5CommentHelper(Map<String, Object> scd) {
		insertUserId=((Integer)scd.get("userId"));
		insertTime = System.currentTimeMillis();
	}

}
