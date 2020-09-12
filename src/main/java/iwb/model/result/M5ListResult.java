package iwb.model.result;

import java.util.Map;

import iwb.model.db.M5List;



public class M5ListResult implements W5MetaResult{
	
	private	int	listId;
    private int	action;
    private Map<String, Object> scd;    
	private Map<String,String>	requestParams;	
	private	M5List list;
	private	W5FormResult searchFormResult;

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
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

	public W5FormResult getSearchFormResult() {
		return searchFormResult;
	}

	public void setSearchFormResult(W5FormResult searchFormResult) {
		this.searchFormResult = searchFormResult;
	}


	public M5ListResult(int listId) {
		super();
		this.listId = listId;
	}

	public int getListId() {
		return listId;
	}

	public void setListId(int listId) {
		this.listId = listId;
	}

	public M5List getList() {
		return list;
	}

	public void setList(M5List list) {
		this.list = list;
	}
}
