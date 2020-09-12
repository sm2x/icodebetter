package iwb.model.result;

import java.util.Map;

import iwb.model.db.W5Card;
import iwb.model.db.W5PageObject;



public class W5CardResult implements W5MetaResult{
	
	private	int	cardId;
    private int	action;
    private Map<String, Object> scd;    
	private Map<String,String>	requestParams;	
	private	W5Card card;
	private	W5FormResult searchFormResult;
    private W5PageObject	tplObj;
	private Map<String,Object>	extraOutMap;	

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

	public int getCardId() {
		return cardId;
	}

	public void setCardId(int cardId) {
		this.cardId = cardId;
	}

	public W5Card getCard() {
		return card;
	}

	public void setCard(W5Card card) {
		this.card = card;
	}

	public W5CardResult(int cardId) {
		super();
		this.cardId = cardId;
	}

	public W5PageObject getTplObj() {
		return tplObj;
	}

	public void setTplObj(W5PageObject tplObj) {
		this.tplObj = tplObj;
	}

	public Map<String, Object> getExtraOutMap() {
		return extraOutMap;
	}

	public void setExtraOutMap(Map<String, Object> extraOutMap) {
		this.extraOutMap = extraOutMap;
	}
	
	
}
