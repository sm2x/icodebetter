package iwb.adapter.ui;

import java.util.Map;

import iwb.domain.helper.W5FormCellHelper;
import iwb.domain.result.W5CardResult;
import iwb.domain.result.W5FormResult;
import iwb.domain.result.W5GlobalFuncResult;
import iwb.domain.result.W5GridResult;
import iwb.domain.result.W5ListViewResult;
import iwb.domain.result.W5PageResult;
import iwb.domain.result.W5QueryResult;
import iwb.domain.result.W5TableRecordInfoResult;

public interface ViewAdapter {
	public	StringBuilder serializeShowForm(W5FormResult formResult);
	public	StringBuilder serializePostForm(W5FormResult formResult);
	public	StringBuilder serializeTemplate(W5PageResult templateResult);
	public	StringBuilder serializeQueryData(W5QueryResult queryResult);
	public	StringBuilder serializeGrid(W5GridResult	gridResult);
	public	StringBuilder serializeCard(W5CardResult	cardResult);
	public	StringBuilder serializeListView(W5ListViewResult	listViewResult);
	public 	StringBuilder serializeGlobalFunc(W5GlobalFuncResult globalFuncResult);
	public	StringBuilder serializeFeeds(Map<String, Object> scd, int platestFeedIndex, int pfeedTip, int proleId, int puserId, int pmoduleId);
	public StringBuilder serializeTableRecordInfo(W5TableRecordInfoResult tableRecordInfoResult);
	public	StringBuilder serializeGetFormSimple(W5FormResult formResult);
	public StringBuilder serializeFormCellStore(W5FormCellHelper rc, int customizationId, String locale);
}
