package iwb.domain.db;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


@Entity
@Table(name="w5_vcs_object", schema="iwb")
public class W5VcsObject  implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 166622234734L;
	private int vcsObjectId;
	private int tableId;
	private int tablePk;
	private int customizationId;
	private String projectUuid;
	private int vcsCommitId;
	private String vcsCommitRecordHash;
	private short vcsObjectStatusType;
	private short versionNo;
	private int insertUserId;
	private int versionUserId;
	private	Timestamp versionDttm;

	

	public W5VcsObject() {
		super();
	}

	public W5VcsObject(Map<String, Object> scd, int tableId2, int tablePk2) {
		this.tableId = tableId2;
		this.tablePk = tablePk2;
		this.vcsObjectStatusType = (short)2; //insert
		this.vcsCommitRecordHash = "!123456789012345678901234567890!";
		this.vcsCommitId = 1;
		this.versionNo = (short)1;
		this.insertUserId = this.versionUserId = (Integer)scd.get("userId");
		this.customizationId = (Integer)scd.get("customizationId");
		this.projectUuid = (String)scd.get("projectId");
		this.versionDttm = new Timestamp(new Date().getTime());
	}
	
	@Column(name="customization_id")
	public int getCustomizationId() {
		return customizationId;
	}
	public void setCustomizationId(int customizationId) {
		this.customizationId = customizationId;
	}
	
    @SequenceGenerator(name="sex_vcs_object",sequenceName="iwb.seq_vcs_object",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_vcs_object")
    @Column(name="vcs_object_id")
	public int getVcsObjectId() {
		return vcsObjectId;
	}
	public void setVcsObjectId(int vcsObjectId) {
		this.vcsObjectId = vcsObjectId;
	}
	
	@Column(name="table_id")
	public int getTableId() {
		return tableId;
	}
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
	
	@Column(name="table_pk")
	public int getTablePk() {
		return tablePk;
	}
	public void setTablePk(int tablePk) {
		this.tablePk = tablePk;
	}
	
	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}
	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}
	
	@Column(name="vcs_commit_id")
	public int getVcsCommitId() {
		return vcsCommitId;
	}
	public void setVcsCommitId(int vcsCommitId) {
		this.vcsCommitId = vcsCommitId;
	}
	
	@Column(name="vcs_commit_record_hash")
	public String getVcsCommitRecordHash() {
		return vcsCommitRecordHash;
	}
	public void setVcsCommitRecordHash(String vcsCommitRecordHash) {
		this.vcsCommitRecordHash = vcsCommitRecordHash;
	}
	
	@Column(name="vcs_object_status_tip")
	public short getVcsObjectStatusType() {
		return vcsObjectStatusType;
	}
	public void setVcsObjectStatusType(short vcsObjectStatusType) {
		this.vcsObjectStatusType = vcsObjectStatusType;
	}
	@Column(name="version_no")
	public short getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(short versionNo) {
		this.versionNo = versionNo;
	}
	@Column(name="insert_user_id")
	public int getInsertUserId() {
		return insertUserId;
	}
	public void setInsertUserId(int insertUserId) {
		this.insertUserId = insertUserId;
	}
	@Column(name="version_user_id")
	public int getVersionUserId() {
		return versionUserId;
	}
	public void setVersionUserId(int versionUserId) {
		this.versionUserId = versionUserId;
	}

	@Column(name="version_dttm")
	public Timestamp getVersionDttm() {
		return versionDttm;
	}

	public void setVersionDttm(Timestamp versionDttm) {
		this.versionDttm = versionDttm;
	}
	

	public W5VcsObject newInstance(int newCustomizationId, String newProjectUuid){
		W5VcsObject n = new W5VcsObject();
		n.setCustomizationId(newCustomizationId);
		n.setInsertUserId(insertUserId);
		n.setProjectUuid(newProjectUuid);
		n.setTableId(tableId);
		n.setTablePk(tablePk);
		n.setVcsCommitId(vcsCommitId);
		n.setVcsCommitRecordHash(vcsCommitRecordHash);
		n.setVcsObjectStatusType(vcsObjectStatusType);
		n.setVersionUserId(versionUserId);
		n.setVersionDttm(versionDttm);
		return n;
		
	}

}
