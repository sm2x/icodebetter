package iwb.model.helper;


public class W5SmsHelper implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4205472792253646646L;
	private String body;
	private String phone;
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public W5SmsHelper(String phone,String body) {
		super();
		this.body = body;
		this.phone = phone;
	}

}
