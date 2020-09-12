package iwb.model.helper;

import java.util.List;
import java.util.Map;

import iwb.model.db.W5GridColumn;


public class W5GridReportHelper implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2368339849341046757L;
	private int recordCount;
	private int gridId;
	private String reportName;
	private List<W5GridColumn> columns;
	private List<Map> data;
	
	
	public int getRecordCount() {
		return recordCount;
	}
	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}
	public int getGridId() {
		return gridId;
	}
	public void setGridId(int gridId) {
		this.gridId = gridId;
	}
	public String getReportName() {
		return reportName;
	}
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	public List<W5GridColumn> getColumns() {
		return columns;
	}
	public void setColumns(List<W5GridColumn> columns) {
		this.columns = columns;
	}
	public List<Map> getData() {
		return data;
	}
	public void setData(List<Map> data) {
		this.data = data;
	}
	public W5GridReportHelper(int gridId) {
		super();
		this.gridId = gridId;
	}
	

}
