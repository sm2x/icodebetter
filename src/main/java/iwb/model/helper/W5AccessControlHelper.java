package iwb.model.helper;


public class W5AccessControlHelper implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3209366499259048981L;
	private int tableId;
	private int tablePk;
	private int customizationId;

	private short accessTip;
	private String accessRoles;
	private String accessUsers;
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
	public short getAccessTip() {
		return accessTip;
	}
	public void setAccessTip(short accessTip) {
		this.accessTip = accessTip;
	}
	public String getAccessRoles() {
		return accessRoles;
	}
	public void setAccessRoles(String accessRoles) {
		this.accessRoles = accessRoles;
	}
	public String getAccessUsers() {
		return accessUsers;
	}
	public void setAccessUsers(String accessUsers) {
		this.accessUsers = accessUsers;
	}
	public W5AccessControlHelper(String accessRoles, String accessUsers) {
		this.accessRoles = accessRoles;
		this.accessUsers = accessUsers;
	}

}
