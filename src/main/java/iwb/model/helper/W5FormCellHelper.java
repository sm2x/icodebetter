package iwb.model.helper;

import java.util.List;
import java.util.Map;

import iwb.model.db.W5FormCell;
import iwb.model.result.W5QueryResult;

public class W5FormCellHelper {
	
	private W5FormCell formCell;
	private String	value;
	private String	dsc;
	private String	hiddenValue;
	private	W5QueryResult	lookupQueryResult;
	private	List lookupListValues;
	private	Map<String, Object> extraValuesMap;
	private short localeMsgFlag;

	public String getDsc() {
		return dsc;
	}
	public void setDsc(String dsc) {
		this.dsc = dsc;
	}
	
	public W5FormCell getFormCell() {
		return formCell;
	}
	public void setFormCell(W5FormCell formCell) {
		this.formCell = formCell;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public W5QueryResult getLookupQueryResult() {
		return lookupQueryResult;
	}
	public void setLookupQueryResult(W5QueryResult lookupQueryResult) {
		this.lookupQueryResult = lookupQueryResult;
	}
	public W5FormCellHelper(W5FormCell formCell) {
		this.formCell = formCell;
	}
	public List getLookupListValues() {
		return lookupListValues;
	}
	public void setLookupListValues(List lookupListValues) {
		this.lookupListValues = lookupListValues;
	}
	public short getLocaleMsgFlag() {
		return localeMsgFlag;
	}
	public void setLocaleMsgFlag(short localeMsgFlag) {
		this.localeMsgFlag = localeMsgFlag;
	}
	public String getHiddenValue() {
		return hiddenValue;
	}
	public void setHiddenValue(String hiddenValue) {
		this.hiddenValue = hiddenValue;
	}
	public Map<String, Object> getExtraValuesMap() {
		return extraValuesMap;
	}
	public void setExtraValuesMap(Map<String, Object> extraValuesMap) {
		this.extraValuesMap = extraValuesMap;
	}
	public W5FormCellHelper(W5FormCell formCell, String value) {
		this.formCell = formCell;
		this.value = value;
	}


}
