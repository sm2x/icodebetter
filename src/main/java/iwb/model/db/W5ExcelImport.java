package iwb.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;


@Entity
@Table(name="w5_excel_import", schema="iwb")
public class W5ExcelImport implements java.io.Serializable {
/*TABLE_ID: 666*/

	private static final long serialVersionUID = 3333124324231L;
	private int excelImportId;	
	private String dsc;
	private String systemFileName;
	private int insertUserId;
	
	
	@SequenceGenerator(name="sex_excel_import",sequenceName="iwb.seq_excel_import",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_excel_import")
	@Column(name="excel_import_id")
	public int getExcelImportId() {
		return excelImportId;
	}
	public void setExcelImportId(int excelImportId) {
		this.excelImportId = excelImportId;
	}


	@Column(name="dsc")
	public String getDsc() {
		return dsc;
	}
	public void setDsc(String dsc) {
		this.dsc = dsc;
	}	
	
	
	@Column(name="insert_user_id")
	public int getInsertUserId() {
		return insertUserId;
	}
	public void setInsertUserId(int insertUserId) {
		this.insertUserId = insertUserId;
	}
	
	
	@Column(name="system_file_name")
	public String getSystemFileName() {
		return systemFileName;
	}
	public void setSystemFileName(String systemFileName) {
		this.systemFileName = systemFileName;
	}
	
	
	@Transient
	public boolean safeEquals(W5Base q){
		return false;
	}
	
	private String projectUuid;

	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}
}
