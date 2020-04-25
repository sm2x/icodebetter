package iwb.domain.db;

import java.time.Instant;
import java.util.Map;

import iwb.util.GenericUtil;

public class Log5VisitedPage implements Log5Base {
	private	Map<String, Object> scd;
	private	String pageName;
	private	Object pageId;
	private	String ip;
	private	String transactionId;

	private int duration;
	private long startTime;

	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		s.append("visited_page");
		if(scd!=null && scd.get("projectId")!=null)s.append(",project_uuid=").append(getScd().get("projectId"));
		s.append(" page_name=\"").append(getPageName()).append("\",page_id=").append(getPageId()).append("i,user_id=").append(scd!=null && scd.get("userId")==null?0:(Integer)getScd().get("userId")).append("i,duration=").append(getDuration()).append("i,ip=\"").append(getIp()).append("\"");
		if(!GenericUtil.isEmpty(transactionId))s.append(",trid=\"").append(transactionId).append("\"");
		s.append(" ").append(startTime).append("000000");
		return s.toString();
	}

	
	public Log5VisitedPage(Map<String, Object> scd, String pageName, Object pageId, String ip, String transactionId) {
		this.scd = scd;
		this.pageName = pageName;
		this.pageId = pageId;
		this.ip = ip;
		this.startTime=Instant.now().toEpochMilli();
		this.transactionId = transactionId;
	}
	public Map<String, Object> getScd() {
		return scd;
	}
	public Object getPageId() {
		return pageId;
	}
	public String getIp() {
		return ip;
	}
	public int getDuration() {
		return duration;
	}
	public String getPageName() {
		return pageName;
	}

	public void calcProcessTime() {
		this.duration = (int)(Instant.now().toEpochMilli() - this.startTime);
	}
}
