package iwb.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;


@Entity
@Immutable
@Table(name="w5_exception",schema="iwb")
public class W5Exception implements java.io.Serializable, W5Base {
/*TABLE_ID: 203*/

	
	private static final long serialVersionUID = 1004491283746L;
	private int exceptionId;
	
	private String exceptionMessage;

	private String locale;

	private String userMessage;

	private short tabOrder;

	private String projectUuid;
	

	@Id
	@Column(name="exception_id")
	public int getExceptionId() {
		return exceptionId;
	}
	public void setExceptionId(int exceptionId) {
		this.exceptionId = exceptionId;
	}

	@Column(name="exception_message")
	public String getExceptionMessage() {
		return exceptionMessage;
	}
	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	@Column(name="locale")
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	@Column(name="user_message")
	public String getUserMessage() {
		return userMessage;
	}
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}
	@Column(name="tab_order")
	public short getTabOrder() {
		return tabOrder;
	}
	public void setTabOrder(short tabOrder) {
		this.tabOrder = tabOrder;
	}

	@Transient
	public boolean safeEquals(W5Base q){
		return false;
	}
	
	@Id
	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}

	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5Exception))return false;
		W5Exception c = (W5Exception)o;
		return c!=null && c.getExceptionId()==getExceptionId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getExceptionId();
	}	
}
