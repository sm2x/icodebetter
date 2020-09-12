package iwb.model.result;

import java.util.Map;

import iwb.model.db.W5Grid;
import iwb.model.db.W5PageObject;
import iwb.model.helper.W5FormCellHelper;



public class W5GridResult implements W5MetaResult{
	
	private	int	gridId;
    private int	action;
    private Map<String, Object> scd;    
	private Map<String,String>	requestParams;	
	private	W5Grid grid;
	private	Map<Integer, W5FormCellHelper>	formCellResultMap;	
	private	W5FormResult searchFormResult;
	private	boolean viewReadOnlyMode;
	private Map<String,Object>	extraOutMap;	
    private W5PageObject	tplObj;
    
	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}


	public W5GridResult(int gridId) {
		this.gridId = gridId;
	}

	public W5GridResult() {}

	public W5Grid getGrid() {
		return grid;
	}

	public void setGrid(W5Grid grid) {
		this.grid = grid;
	}

	public int getGridId() {
		return gridId;
	}

	public void setGridId(int gridId) {
		this.gridId = gridId;
	}
	
	public Map<String, Object> getScd() {
		return scd;
	}

	public void setScd(Map<String, Object> scd) {
		this.scd = scd;
	}




    public	String toJsonGridDetailView(){
    	StringBuilder html = new StringBuilder();
//    	html.append("new Ext.Template('").append(PromisUtil.stringToJS(detailTemplate.getCode())).append("')");
    	
    	return html.toString();
    }

	public Map<String, String> getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(Map<String, String> requestParams) {
		this.requestParams = requestParams;
	}

	public Map<Integer, W5FormCellHelper> getFormCellResultMap() {
		return formCellResultMap;
	}

	public void setFormCellResultMap(Map<Integer, W5FormCellHelper> formCellResultMap) {
		this.formCellResultMap = formCellResultMap;
	}

	public W5FormResult getSearchFormResult() {
		return searchFormResult;
	}

	public void setSearchFormResult(W5FormResult searchFormResult) {
		this.searchFormResult = searchFormResult;
	}

	public boolean isViewReadOnlyMode() {
		return viewReadOnlyMode;
	}

	public void setViewReadOnlyMode(boolean viewReadOnlyMode) {
		this.viewReadOnlyMode = viewReadOnlyMode;
	}


	public Map<String, Object> getExtraOutMap() {
		return extraOutMap;
	}

	public void setExtraOutMap(Map<String, Object> extraOutMap) {
		this.extraOutMap = extraOutMap;
	}

	public W5PageObject getTplObj() {
		return tplObj;
	}

	public void setTplObj(W5PageObject tplObj) {
		this.tplObj = tplObj;
	}
}
