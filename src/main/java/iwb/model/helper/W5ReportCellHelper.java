package iwb.model.helper;

public class W5ReportCellHelper  implements java.io.Serializable{
	int	rowId, columnId;
	String  deger, tag;
	short rowTip ,  cellTip ,  colspan ;
	
	
	public W5ReportCellHelper(int rowId, int columnId, String deger, short rowTip, short cellTip, short colspan, String tag) {
		this.rowId = rowId;
		this.columnId = columnId;
		this.deger = deger;
		this.tag = tag;
		this.rowTip = rowTip;
		this.cellTip = cellTip;
		this.colspan = colspan;
	}
	
	public W5ReportCellHelper(int rowId, int columnId, String deger) {
		this.rowId = rowId;
		this.columnId = columnId;
		this.deger = deger;
	}
	
	public W5ReportCellHelper() {
	}

	public short getCellTip() {
		return cellTip;
	}
	public void setCellTip(short cellTip) {
		this.cellTip = cellTip;
	}
	public short getColspan() {
		return colspan;
	}
	public void setColspan(short colspan) {
		this.colspan = colspan;
	}
	public int getColumnId() {
		return columnId;
	}
	public void setColumnId(int columnId) {
		this.columnId = columnId;
	}
	public String getDeger() {
		return deger;
	}
	public void setDeger(String deger) {
		this.deger = deger;
	}
	public short getRowTip() {
		return rowTip;
	}
	public void setRowTip(short rowTip) {
		this.rowTip = rowTip;
	}
	public int getRowId() {
		return rowId;
	}
	public void setRowId(int rowId) {
		this.rowId = rowId;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
}
