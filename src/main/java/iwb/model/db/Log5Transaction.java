package iwb.model.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import iwb.util.GenericUtil;

@Entity
@Table(name="log5_transaction",schema="iwb")
public class Log5Transaction implements Serializable, Log5Base{
	private static final long serialVersionUID = 134253545816162873L;

	private int logId;
	private String projectUuid;  
	private String source;  
	private String error;  
	private	String transacionId;

	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		s.append("transaction,project_uuid=").append(projectUuid).append(" source=\"").append(source).append("\",trid=\"").append(transacionId).append("\"");
		if(error!=null)s.append(",msg=\"").append(GenericUtil.stringToJS2(error)).append("\"");
		return s.toString();
	}

	
	public Log5Transaction() {
	}
	
	public Log5Transaction(String projectUuid,String source, String transacionId) {
		this.projectUuid = projectUuid;
		this.source = source;
		this.transacionId = transacionId;
	}

	@SequenceGenerator(name="sex_log5_transaction",sequenceName="iwb.seq_log5_transaction",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_log5_transaction")
	@Column(name="log_id")  
	public int getLogId() {
		return logId;
	}
	public void setLogId(int logId) {
		this.logId = logId;
	}



	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}
	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}


	@Column(name="source")
	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}


	@Column(name="error")
	public String getError() {
		return error;
	}


	public void setError(String error) {
		this.error = error;
	}


	@Column(name="transaction_id")
	public String getTransacionId() {
		return transacionId;
	}


	public void setTransacionId(String transacionId) {
		this.transacionId = transacionId;
	}
	
	
	
}
