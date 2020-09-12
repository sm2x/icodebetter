package iwb.model.helper;

public class W5QueuedActionHelper implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4172465145521537411L;
	private	Object action;
	
	
	public Object getAction() {
		return action;
	}


	public void setAction(Object action) {
		this.action = action;
	}


	public W5QueuedActionHelper(Object action) {
		this.action = action;
	}

}
