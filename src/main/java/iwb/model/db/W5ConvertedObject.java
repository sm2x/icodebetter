package iwb.model.db;

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

import iwb.model.helper.W5TableRecordHelper;

// Generated Jun 17, 2007 5:12:14 PM by Hibernate Tools 3.2.0.b9


@Entity
@Immutable
@Table(name="w5_converted_object",schema="iwb")
public class W5ConvertedObject implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 166612112131L;

	public W5ConvertedObject() {
		super();
	}
	private int convertedObjectId;
	private int conversionId;
	private int srcTablePk;
	private int dstTablePk;
	private int versionNo;
	private int insertUserId;
	private int versionUserId;
	private	List<W5TableRecordHelper> _relatedRecord;
	
    @SequenceGenerator(name="sex_converted_object",sequenceName="iwb.seq_converted_object",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_converted_object")
	@Column(name="converted_object_id")
	public int getConvertedObjectId() {
		return convertedObjectId;
	}
	public void setConvertedObjectId(int convertedObjectId) {
		this.convertedObjectId = convertedObjectId;
	}
	
	public String toInfluxDB() {
		StringBuilder s=new StringBuilder();
		s.append("converted_object,conversion_id=").append(conversionId).append(",project_uuid=").append(projectUuid).append(" user_id=").append(insertUserId).append(",src_table_pk=").append(srcTablePk).append(",dst_table_pk=").append(dstTablePk);
		return s.toString();
	}

	
	@Column(name="conversion_id")
	public int getConversionId() {
		return conversionId;
	}
	public void setConversionId(int conversionId) {
		this.conversionId = conversionId;
	}
	
	@Column(name="src_table_pk")
	public int getSrcTablePk() {
		return srcTablePk;
	}
	public void setSrcTablePk(int srcTablePk) {
		this.srcTablePk = srcTablePk;
	}
	
	@Column(name="dst_table_pk")
	public int getDstTablePk() {
		return dstTablePk;
	}
	public void setDstTablePk(int dstTablePk) {
		this.dstTablePk = dstTablePk;
	}

	@Column(name="version_no")
	public int getVersionNo() {
		return versionNo;
	}
	public void setVersionNo(int versionNo) {
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
	public W5ConvertedObject(Map<String, Object> scd, int conversionId, int srcTablePk, int dstTablePk) {
		super();
		this.versionNo = 1;
		this.versionUserId = (Integer)scd.get("userId");
		this.insertUserId = (Integer)scd.get("userId");
		this.projectUuid = (String)scd.get("projectId");
		this.conversionId = conversionId;
		this.srcTablePk = srcTablePk;
		this.dstTablePk = dstTablePk;
	}
	@Transient
	public List<W5TableRecordHelper> get_relatedRecord() {
		return _relatedRecord;
	}
	public void set_relatedRecord(List<W5TableRecordHelper> _relatedRecord) {
		this._relatedRecord = _relatedRecord;
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
	
	public boolean equals(Object o) {
		if(o==null || !(o instanceof W5ConvertedObject))return false;
		W5ConvertedObject c = (W5ConvertedObject)o;
		return c!=null && c.getConvertedObjectId()==getConvertedObjectId() && c.getProjectUuid().equals(projectUuid);
	}
	
	public int hashCode() {
		return projectUuid.hashCode() + 100*getConvertedObjectId();
	}
}
