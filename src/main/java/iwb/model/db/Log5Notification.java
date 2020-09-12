package iwb.model.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Immutable;

import iwb.cache.FrameworkCache;
import iwb.cache.LocaleMsgCache;
import iwb.model.helper.W5TableRecordHelper;
import iwb.util.GenericUtil;


@Entity
@Immutable
@Table(name="w5_notification",schema="iwb")
public class Log5Notification implements java.io.Serializable, Log5Base {
	private static final long serialVersionUID = 134252033332912873L;

	private int notificationId;
	
	private int customizationId;
	private int tableId;
	private int tablePk;
	private short notificationTip;
	private int userId;
	private short userTip;
	private String showUrl;
	private int actionUserId;
	private short notificationLevel;
	private List<W5TableRecordHelper> _tableRecordList;
	private String _tmpStr;
	
	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		return s.toString();
	}
    public Log5Notification(Map<String, Object> m) {
//    	private int notificationId;
    	
		super();
    	this.customizationId = GenericUtil.uInt(m.get("customizationId"));
    	this.tableId = GenericUtil.uInt(m.get("ptable_id"));
    	this.tablePk = GenericUtil.uInt(m.get("ptable_pk"));
    	this.notificationTip = (short)GenericUtil.uInt(m.get("pnotification_tip"));
    	this.userId = GenericUtil.uInt(m.get("puser_id"));
    	this.userTip = (short)GenericUtil.uInt(m.get("puser_tip"));
    	this.showUrl = (String)m.get("pshow_url");
    	this.actionUserId = GenericUtil.uInt(m.get("paction_user_id"));
    	this.notificationLevel = (short)GenericUtil.uInt(m.get("pnotification_level"));
	}
	@SequenceGenerator(name="sex_notification",sequenceName="iwb.seq_notification",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_notification")
	@Column(name="notification_id")
	public int getNotificationId() {
		return notificationId;
	}
	public void setNotificationId(int notificationId) {
		this.notificationId = notificationId;
	}
	@Column(name="customization_id")
	public int getCustomizationId() {
		return customizationId;
	}
	public void setCustomizationId(int customizationId) {
		this.customizationId = customizationId;
	}

	@Column(name="table_id")
	public int getTableId() {
		return tableId;
	}
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
	@Column(name="notification_tip")
	public short getNotificationTip() {
		return notificationTip;
	}
	public void setNotificationTip(short notificationTip) {
		this.notificationTip = notificationTip;
	}
	
	@Column(name="table_pk")
	public int getTablePk() {
		return tablePk;
	}
	public void setTablePk(int tablePk) {
		this.tablePk = tablePk;
	}
	
	@Column(name="user_id")
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	@Column(name="user_tip")
	public short getUserTip() {
		return userTip;
	}
	public void setUserTip(short userTip) {
		this.userTip = userTip;
	}
	@Column(name="show_url")
	public String getShowUrl() {
		return showUrl;
	}
	public void setShowUrl(String showUrl) {
		this.showUrl = showUrl;
	}
	
	@Column(name="action_user_id")
	public int getActionUserId() {
		return actionUserId;
	}
	public void setActionUserId(int actionUserId) {
		this.actionUserId = actionUserId;
	}
	
	public Log5Notification(Map<String, Object> scd, int userId,  short notificationTip, int tableId, int tablePk, int actionUserId, String showUrl, int notLevel) {
		this.customizationId = (Integer)scd.get("customizationId");
		this.userId =  userId;
		this.userTip =  ((Integer)scd.get("userTip")).shortValue();
		this.tableId = tableId;
		this.tablePk = tablePk;
		this.notificationTip = notificationTip;
		this.showUrl = showUrl;
		this.actionUserId = actionUserId;
		this.notificationLevel = (short)notLevel;
	}
	public Log5Notification(Map<String, Object> scd, int userId,  short notificationTip, int tableId, int tablePk, int actionUserId, String showUrl, int notLevel, String _tmpStr) {
		this.customizationId = (Integer)scd.get("customizationId");
		this.userId =  userId;
		this.userTip =  ((Integer)scd.get("userTip")).shortValue();
		this.tableId = tableId;
		this.tablePk = tablePk;
		this.notificationTip = notificationTip;
		this.showUrl = showUrl;
		this.actionUserId = actionUserId;
		this._tmpStr = _tmpStr;
		this.notificationLevel = (short)notLevel;
	}
	public Log5Notification() {
		super();
	}
	public Log5Notification(W5FormSmsMailAlarm a) {
		super();
	//	this.customizationId = a.getCustomizationId();
		this.actionUserId = this.userId = a.getInsertUserId();
		this.tableId = a.getTableId();
		this.tablePk = a.getTablePk();
		this.userTip = (short) 2;
		this.notificationTip = 21;
		this._tmpStr = a.getDsc();
	}
	public Log5Notification(String _tmpStr, int notLevel, int customizationId) {
		this._tmpStr = _tmpStr;
		this.notificationLevel = (short)notLevel;
		this.customizationId = customizationId;
	}
	@Transient
	public String get_notificationTipStr() {
		W5LookUp lu = FrameworkCache.getLookUp(customizationId, 518);
		if(lu==null)return null;
		W5LookUpDetay lud = lu.get_detayMap().get(new Short(notificationTip).toString()); 
		if(lud==null)return null;
		return LocaleMsgCache.get2(customizationId, "tr", lud.getDsc());
	}
	@Transient
	public List<W5TableRecordHelper> get_tableRecordList() {
		return _tableRecordList;
	}
	public void set_tableRecordList(List<W5TableRecordHelper> _tableRecordList) {
		this._tableRecordList = _tableRecordList;
	}
	@Transient
	public String get_tmpStr() {
		return _tmpStr;
	}
	public void set_tmpStr(String tmpStr) {
		_tmpStr = tmpStr;
	}
	@Column(name="notification_level")
	public short getNotificationLevel() {
		return notificationLevel;
	}
	public void setNotificationLevel(short notificationLevel) {
		this.notificationLevel = notificationLevel;
	}
	public Map toMap() {
		Map m = new HashMap();
		m.put("notificationTip", notificationTip);
		m.put("level", new String[]{"info","success","warning","error"}[(notificationTip>=0 && notificationTip<=3) ? notificationTip:0]);
		if(!GenericUtil.isEmpty(_tmpStr))m.put("_tmpStr", _tmpStr);
		if(!GenericUtil.isEmpty(showUrl))m.put("showUrl", showUrl);
		return m;
	}
	
	
}
