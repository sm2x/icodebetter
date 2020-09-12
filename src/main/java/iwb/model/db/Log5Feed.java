package iwb.model.db;

import java.sql.Timestamp;
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

import iwb.model.helper.W5AccessControlHelper;
import iwb.model.helper.W5CommentHelper;
import iwb.model.helper.W5TableRecordHelper;


@Entity
@Table(name="w5_feed",schema="iwb")
public class Log5Feed implements java.io.Serializable, Log5Base {
	private static final long serialVersionUID = 134252091323212873L;

	private int feedId;
	
	private int customizationId;
	private int tableId;
	private int tablePk;
	private int detailTableId;
	private int detailTablePk;
	private short feedTip;
	private int insertUserId;
	private int insertRoleId;
	private int insertUserTip;
	private String dsc;
	private String projectId;
	private W5AccessControlHelper _viewAccessControl;
	private	Map<Integer,Log5Feed> _relatedFeedMap;
	private List<W5TableRecordHelper> _tableRecordList;
	private short _showFeedTip;
	private long _insertTime;
	private	int _commentCount;
	private List<W5CommentHelper> _tableCommentList;
	private	Timestamp insertDttm;
	
	
	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		s.append("feed,project_uuid=").append(projectId).append(" user_id=").append(insertUserId).append("i,feed_tip=").append(feedTip)
			.append("i,table_id=").append(tableId).append("i,table_pk=").append(tablePk)
			.append("i,dtable_id=").append(detailTableId).append("i,dtable_pk=").append(detailTablePk).append("i,dsc=\"").append(dsc).append("\"");
		return s.toString();
	}
	
	public Log5Feed(){
	}
	
	public Log5Feed(Map<String, Object> scd) {
		customizationId=((Integer)scd.get("customizationId"));
		insertUserId=((Integer)scd.get("userId"));
		insertRoleId=((Integer)scd.get("roleId"));
		insertUserTip=((Integer)scd.get("userTip"));
		projectId=((String)scd.get("projectId"));
		_insertTime = System.currentTimeMillis();
	}
	@SequenceGenerator(name="sex_feed",sequenceName="iwb.seq_feed",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_feed")
	@Column(name="feed_id")
	public int getFeedId() {
		return feedId;
	}
	public void setFeedId(int feedId) {
		this.feedId = feedId;
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
	@Column(name="feed_tip")
	public short getFeedTip() {
		return feedTip;
	}
	public void setFeedTip(short feedTip) {
		this.feedTip = feedTip;
	}
	
	@Column(name="table_pk")
	public int getTablePk() {
		return tablePk;
	}
	public void setTablePk(int tablePk) {
		this.tablePk = tablePk;
	}
	

	@Column(name="dsc")
	public String getDsc() {
		return dsc;
	}
	public void setDsc(String dsc) {
		this.dsc = dsc;
	}
	@Column(name="insert_role_id")
	public int getInsertRoleId() {
		return insertRoleId;
	}
	public void setInsertRoleId(int insertRoleId) {
		this.insertRoleId = insertRoleId;
	}
	@Column(name="insert_user_id")
	public int getInsertUserId() {
		return insertUserId;
	}
	public void setInsertUserId(int insertUserId) {
		this.insertUserId = insertUserId;
	}
	@Transient
	public W5AccessControlHelper get_viewAccessControl() {
		return _viewAccessControl;
	}
	public void set_viewAccessControl(W5AccessControlHelper _viewAccessControl) {
		this._viewAccessControl = _viewAccessControl;
	}
	@Transient
	public Map<Integer,Log5Feed> get_relatedFeedMap() {
		return _relatedFeedMap;
	}
	public void set_relatedFeedMap(Map<Integer,Log5Feed> _relatedFeedMap) {
		this._relatedFeedMap = _relatedFeedMap;
	}
	@Transient
	public List<W5TableRecordHelper> get_tableRecordList() {
		return _tableRecordList;
	}
	public void set_tableRecordList(List<W5TableRecordHelper> _tableRecordList) {
		this._tableRecordList = _tableRecordList;
	}
	@Transient
	public short get_showFeedTip() {
		return _showFeedTip;
	}
	public void set_showFeedTip(short showFeedTip) {
		_showFeedTip = showFeedTip;
	}
	
	@Column(name="detail_table_id")
	public int getDetailTableId() {
		return detailTableId;
	}
	public void setDetailTableId(int detailTableId) {
		this.detailTableId = detailTableId;
	}
	@Column(name="detail_table_pk")
	public int getDetailTablePk() {
		return detailTablePk;
	}
	public void setDetailTablePk(int detailTablePk) {
		this.detailTablePk = detailTablePk;
	}
	@Transient
	public long get_insertTime() {
		return _insertTime;
	}
	public void set_insertTime(long insertTime) {
		_insertTime = insertTime;
	}
	
	@Column(name="insert_user_tip")
	public int getInsertUserTip() {
		return insertUserTip;
	}
	public void setInsertUserTip(int insertUserTip) {
		this.insertUserTip = insertUserTip;
	}
	@Transient
	public int get_commentCount() {
		return _commentCount;
	}
	public void set_commentCount(int commentCount) {
		_commentCount = commentCount;
	}
	@Transient
	public List<W5CommentHelper> get_tableCommentList() {
		return _tableCommentList;
	}
	public void set_tableCommentList(List<W5CommentHelper> tableCommentList) {
		_tableCommentList = tableCommentList;
	}
	@Column(name="insert_dttm", insertable=false)
	public Timestamp getInsertDttm() {
		return insertDttm;
	}

	public void setInsertDttm(Timestamp insertDttm) {
		this.insertDttm = insertDttm;
	}

	@Transient
	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

}
