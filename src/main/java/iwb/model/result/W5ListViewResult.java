package iwb.model.result;

import java.util.Map;

import iwb.model.db.W5List;



public class W5ListViewResult implements W5MetaResult{
	
	private	int	listId;
    private int	action;
    private Map<String, Object> scd;    
	private Map<String,String>	requestParams;	
	private	W5List listView;
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


	public W5ListViewResult(int listId) {
		super();
		this.listId = listId;
	}

	public int getListId() {
		return listId;
	}

	public void setListId(int listId) {
		this.listId = listId;
	}

	public W5List getListView() {
		return listView;
	}

	public void setListView(W5List listView) {
		this.listView = listView;
	}

}
