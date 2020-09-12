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
@Table(name="w5_excel_import_sheet", schema="iwb")
public class W5ExcelImportSheet implements java.io.Serializable {
/*TABLE_ID: 5599*/

	private static final long serialVersionUID = 777324234231L;
	private int excelImportSheetId;	
	private int excelImportId;	
	private String dsc;
	private short tabOrder;
	
	

	@SequenceGenerator(name="sex_excel_import_sheet",sequenceName="iwb.seq_excel_import_sheet",allocationSize=1)
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="sex_excel_import_sheet")
	@Column(name="excel_import_sheet_id")
	public int getExcelImportSheetId() {
		return excelImportSheetId;
	}
	public void setExcelImportSheetId(int excelImportSheetId) {
		this.excelImportSheetId = excelImportSheetId;
	}
	
	
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
	
	private String projectUuid;

	@Column(name="project_uuid")
	public String getProjectUuid() {
		return projectUuid;
	}

	public void setProjectUuid(String projectUuid) {
		this.projectUuid = projectUuid;
	}
}
