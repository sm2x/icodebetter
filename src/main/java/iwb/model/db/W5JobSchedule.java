package iwb.model.db;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import iwb.util.GenericUtil;

@Entity
@Immutable
@Table(name = "W5_JOB_SCHEDULE",schema="iwb")
public class W5JobSchedule implements java.io.Serializable, W5Base {
/*TABLE_ID: 674*/

	private static final long serialVersionUID = 441165423423421L;
	private int jobScheduleId;
	private String dsc;
	private int actionType;
	private int activeFlag;
	private int actionDbFuncId;
	private int formValueId;
	private int executeUserId;
	private int executeRoleId;
	private String actionSendEmailTo;
	private String actionSendEmailSubject;
	private String actionSendEmailBody;
	private int actionStartTip;
	private Timestamp actionStartDttm;
	private int actionFrequency;
	private int actionDelay;
	private Timestamp actionEndDttm;
	private String actionMonths;
	private String actionMonthsDays;
	private String actionWeekDays;
	private String reports;
	private String repeatTime;
	private String locale;
	private int transactionalFlag;
	private int todayFlag;
	private int todayAddDayValue;
	private long _lastRunTime;
	private boolean _running;
	private int _userRoleId;

	public W5JobSchedule() {
	}

	@Id
	@Column(name = "JOB_SCHEDULE_ID")
	public int getJobScheduleId() {
		return this.jobScheduleId;
	}

	public void setJobScheduleId(int jobScheduleId) {
		this.jobScheduleId = jobScheduleId;
	}

	@Column(name = "DSC")
	public String getDsc() {
		return dsc;
	}

	public void setDsc(String dsc) {
		this.dsc = dsc;
	}
	

	@Column(name = "ACTION_TIP")
	public int getActionType() {
		return actionType;
	}

	public void setActionType(int actionType) {
		this.actionType = actionType;
	}

	@Column(name = "ACTIVE_FLAG")
	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

	@Column(name = "ACTION_DB_FUNC_ID")
	public int getActionDbFuncId() {
		return actionDbFuncId;
	}

	public void setActionDbFuncId(int actionDbFuncId) {
		this.actionDbFuncId = actionDbFuncId;
	}

	@Column(name = "FORM_VALUE_ID")
	public int getFormValueId() {
		return formValueId;
	}

	public void setFormValueId(int formValueId) {
		this.formValueId = formValueId;
	}

	@Column(name = "EXECUTE_USER_ID")
	public int getExecuteUserId() {
		return executeUserId;
	}

	public void setExecuteUserId(int executeUserId) {
		this.executeUserId = executeUserId;
	}

	@Column(name = "EXECUTE_ROLE_ID")
	public int getExecuteRoleId() {
		return executeRoleId;
	}

	public void setExecuteRoleId(int executeRoleId) {
		this.executeRoleId = executeRoleId;
	}

	@Column(name = "ACTION_SEND_EMAIL_TO")
	public String getActionSendEmailTo() {
		return actionSendEmailTo;
	}

	public void setActionSendEmailTo(String actionSendEmailTo) {
		this.actionSendEmailTo = actionSendEmailTo;
	}

	@Column(name = "ACTION_SEND_EMAIL_SUBJECT")
	public String getActionSendEmailSubject() {
		return actionSendEmailSubject;
	}


	public void setActionSendEmailSubject(String actionSendEmailSubject) {
		this.actionSendEmailSubject = actionSendEmailSubject;
	}

	@Column(name = "ACTION_SEND_EMAIL_BODY")
	public String getActionSendEmailBody() {
		return actionSendEmailBody;
	}
	
	public void setActionSendEmailBody(String actionSendEmailBody) {
		this.actionSendEmailBody = actionSendEmailBody;
	}

	@Column(name = "ACTION_START_TIP")
	public int getActionStartTip() {
		return actionStartTip;
	}

	public void setActionStartTip(int actionStartTip) {
		this.actionStartTip = actionStartTip;
	}

	@Column(name = "ACTION_START_DTTM")
	public Timestamp getActionStartDttm() {
		return actionStartDttm;
	}

	public void setActionStartDttm(Timestamp actionStartDttm) {
		this.actionStartDttm = actionStartDttm;
	}

	@Column(name = "ACTION_FREQUENCY")
	public int getActionFrequency() {
		return actionFrequency;
	}

	public void setActionFrequency(int actionFrequency) {
		this.actionFrequency = actionFrequency;
	}

	@Column(name = "ACTION_DELAY")
	public int getActionDelay() {
		return actionDelay;
	}

	public void setActionDelay(int actionDelay) {
		this.actionDelay = actionDelay;
	}

	@Column(name = "ACTION_END_DTTM")
	public Timestamp getActionEndDttm() {
		return actionEndDttm;
	}

	public void setActionEndDttm(Timestamp actionEndDttm) {
		this.actionEndDttm = actionEndDttm;
	}

	@Column(name = "ACTION_MONTHS")
	public String getActionMonths() {
		return actionMonths;
	}

	public void setActionMonths(String actionMonths) {
		this.actionMonths = actionMonths;
	}

	@Column(name = "ACTION_MONTHS_DAYS")
	public String getActionMonthsDays() {
		return actionMonthsDays;
	}

	public void setActionMonthsDays(String actionMonthsDays) {
		this.actionMonthsDays = actionMonthsDays;
	}
	
	@Column(name = "ACTION_WEEK_DAYS")
	public String getActionWeekDays() {
		return actionWeekDays;
	}

	public void setActionWeekDays(String actionWeekDays) {
		this.actionWeekDays = actionWeekDays;
	}

	@Column(name = "REPORTS")
	public String getReports() {
		return reports;
	}

	public void setReports(String reports) {
		this.reports = reports;
	}
	
	@Column(name = "REPEAT_TIME")
	public String getRepeatTime() {
		return repeatTime;
	}

	public void setRepeatTime(String repeatTime) {
		this.repeatTime = repeatTime;
	}
	
	@Column(name = "LOCALE")
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	@Transient
	public int get_userRoleId() {
		return _userRoleId;
	}

	public void set_userRoleId(int userRoleId) {
		_userRoleId = userRoleId;
	}

	
	@Column(name = "TODAY_FLAG")
	public int getTodayFlag() {
		return todayFlag;
	}

	public void setTodayFlag(int todayFlag) {
		this.todayFlag = todayFlag;
	}
	
	@Column(name = "TODAY_ADD_DAY_VALUE")
	public int getTodayAddDayValue() {
		return todayAddDayValue;
	}

	public void setTodayAddDayValue(int todayAddDayValue) {
		this.todayAddDayValue = todayAddDayValue;
	}
	
	private String projectUuid;
	@Id
	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}

	@Transient
	public boolean is_running() {
		return _running;
	}

	public void set_running(boolean _running) {
		this._running = _running;
		if(_running) {
			this._lastRunTime = System.currentTimeMillis();
		}
	}

	public boolean runCheck() {
		if(this._running)return false;
		if(this.getActionType()!=3 ||  this.getActionDbFuncId()==0)return false;//TODO : only run global func
		
		Date dateNow = Calendar.getInstance().getTime();
		Timestamp actionStartDttm = this.getActionStartDttm();					
		Timestamp actionEndDttm = (this.getActionEndDttm()==null ? new Timestamp(dateNow.getTime()) : this.getActionEndDttm());					
		Timestamp dt = new Timestamp(dateNow.getTime());
		int frequency = this.getActionFrequency();					
		
		if (dt.compareTo(actionStartDttm)>0){ //başlangic tarihi kontrol ediliyor
			if (dt.compareTo(actionEndDttm)<=0){//bitiş tarihi kontrol ediliyor
				if (frequency != 3){
					//haftanin gunleri icerisinde mi? kontrol ediliyor
					String actionWeekDay = this.getActionWeekDays();					  
					String weekday = Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == 1 ? 7 : Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1);							 
					if (actionWeekDay.indexOf(weekday)<0){
						return false;
					}
				}
				//aylar icerisinde mi? kontrol ediliyor
				if(!GenericUtil.isEmpty(this.getActionMonths())){
					String[] actionMonths = this.getActionMonths().split(",");
					Arrays.sort(actionMonths);
					String month = Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1);				
					if (Arrays.binarySearch(actionMonths,month)<0){
						return false;
					}
				}
			}
			else{
				return false;
			}						
		}
		else{
			return false;
		}
		
		long lastRunTime = this._lastRunTime;
		long dateNow2 = System.currentTimeMillis();
		
		if ((frequency == 2) || (frequency == 3) || (frequency == 4)){	//once, monthly, daily
			if (this.getRepeatTime() == null)
				return false;			
			//Time nowTime = new Time(dateNow.getHours(), dateNow.getMinutes(), 0);
			long nowTime2 = dateNow2 % (24*60*60*1000);
			//Time rTime = new Time(Integer.parseInt(this.getRepeatTime().substring(0,2)), Integer.parseInt(this.getRepeatTime().substring(3,5)), 0);
			long rTime2 = GenericUtil.uInt(this.getRepeatTime().substring(0,2))*60*60*1000 + GenericUtil.uInt(this.getRepeatTime().substring(3,5))*60*1000;
								
			//SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			//String tmpDate = sdf.format(dateNow);
			//dateNow = sdf.parse(tmpDate);
			
			if (frequency==3){ //monthly
				//ayin kacinci gunu gönderilmesi gerektiği kontrol ediliyor.
				String[] actionMonthsDays = this.getActionMonthsDays().split(",");
				String day = Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
				if (Arrays.binarySearch(actionMonthsDays,day)<0){
					return false;
				}												
			}else if (frequency == 2){//once
				if (lastRunTime != 0) return false;
			}
			
			if (lastRunTime != 0){ 
				//int lastDay = lastRunTime.getDay();
				//int nowDay = dateNow.getDay();
				//if (nowDay == lastDay) return false; //bugun zaten gönderilmiş
				if((int)(lastRunTime / (24*60*60*1000)) == (int)(dateNow2 / (24*60*60*1000)))return false;
			}	
			//if (!(nowTime.compareTo(rTime)>=0)) return false;
			if (nowTime2 < rTime2) return false;
			
			
		} else { //repeat always
			int repeatTime = this.getActionDelay();
			if (repeatTime == 0)
				return false;	
			if (lastRunTime != 0){ 
//			    long diff = dt.getTime() - lastRunTime.getTime();
			    long diff = dateNow2 - lastRunTime;
			    long duration = (diff / (1000 * 60));//minute
			    
			    if (duration<repeatTime){
			    	return false;
			    }
			}
		}
		
		return true;
	}

	@Column(name="transactional_flag")
	public int getTransactionalFlag() {
		return transactionalFlag;
	}

	public void setTransactionalFlag(int transactionalFlag) {
		this.transactionalFlag = transactionalFlag;
	}

	
	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5JobSchedule))return false;
		W5JobSchedule c = (W5JobSchedule)o;
		return c!=null && c.getJobScheduleId()==getJobScheduleId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getJobScheduleId();
	}

	@Transient
	public boolean safeEquals(W5Base q) {

			return false;
	}
}
