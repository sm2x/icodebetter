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
@Table(name="log5_mq",schema="iwb")
public class Log5Mq implements Serializable, Log5Base{
	private static final long serialVersionUID = 134200001872912873L;

	private int logId;
	private int mqId;
	private String message;  
	private String topic;  
	private String error;  
	private String projectUuid;  
	private	String transactionId;

	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		s.append("mq_action,project_uuid=").append(projectUuid).append(",mq_id=").append(mqId).append(" topic=\"").append(topic).append("\",trid=\"").append(transactionId)
		.append("\",message=\"");
		if(true) {
			s.append(GenericUtil.stringToJS2(message));
		} else {
			byte[] bar = message.getBytes();
			for(int qi=0;qi<bar.length;qi++)s.append(bar[qi]).append(",");
		}
		s.append("\"");
		if(!GenericUtil.isEmpty(error))
			s.append(",message=\"").append(GenericUtil.stringToJS2(message)).append("\"");
		return s.toString();	}

	
	public Log5Mq() {
	}
	


	public Log5Mq(String projectUuid, int mqId, String topic, String message, String transactionId) {
	    super();
	    this.mqId = mqId;
	    this.message = message;
	    this.topic = topic;
	    this.projectUuid = projectUuid;
		this.transactionId = transactionId;
	  }


  @SequenceGenerator(name="sex_log5_mq",sequenceName="iwb.seq_log5_mq",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_log5_mq")
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

	@Column(name="mq_id")
  public int getMqId() {
  return mqId;}


  public void setMqId(int mqId) {
  this.mqId = mqId;}

	@Column(name="message")
  public String getMessage() {
  return message;}


  public void setMessage(String message) {
  this.message = message;}


	@Column(name="topic")
  public String getTopic() {
  return topic;}


  public void setTopic(String topic) {
  this.topic = topic;}


	@Column(name="error")
	public String getError() {
  return error;}


  public void setError(String error) {
  this.error = error;}
	
	
	
}
