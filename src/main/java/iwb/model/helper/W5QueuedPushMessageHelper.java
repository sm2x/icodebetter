package iwb.model.helper;



public class W5QueuedPushMessageHelper implements java.io.Serializable {

	private static final long serialVersionUID = 5976684064334057414L;
	private int customizationId;
	private int messageTip;
	private int tableId;
	private int tablePk;
	private String msg;
	private int sound;
	private short deviceType;
	private String deviceToken;
	


	public int getCustomizationId() {
		return customizationId;
	}

	public void setCustomizationId(int customizationId) {
		this.customizationId = customizationId;
	}

	public W5QueuedPushMessageHelper(int customizationId, int messageTip,
			int tableId, int tablePk, String msg, int sound, short deviceType,
			String deviceToken) {
		super();
		this.customizationId = customizationId;
		this.messageTip = messageTip;
		this.tableId = tableId;
		this.tablePk = tablePk;
		this.msg = msg;
		this.sound = sound;
		this.deviceType = deviceType;
		this.deviceToken = deviceToken;
	}

	public int getMessageTip() {
		return messageTip;
	}
	public void setMessageTip(int messageTip) {
		this.messageTip = messageTip;
	}
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
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getSound() {
		return sound;
	}
	public void setSound(int sound) {
		this.sound = sound;
	}
	public short getDeviceName() {
		return deviceType;
	}
	public void setDeviceName(short deviceType) {
		this.deviceType = deviceType;
	}
	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

}
