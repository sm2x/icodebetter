package iwb.dao.metadata;


import java.util.List;
import java.util.Map;

import iwb.domain.db.W5LookUpDetay;
import iwb.domain.db.W5ObjectMailSetting;
import iwb.domain.db.W5Project;
import iwb.domain.db.W5TableFieldCalculated;
import iwb.domain.result.M5ListResult;
import iwb.domain.result.W5CardResult;
import iwb.domain.result.W5FormResult;
import iwb.domain.result.W5GlobalFuncResult;
import iwb.domain.result.W5GridResult;
import iwb.domain.result.W5ListViewResult;
import iwb.domain.result.W5PageResult;
import iwb.domain.result.W5QueryResult;

public interface MetadataLoader {

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#getFormResult(java.util.Map, int, int, java.util.Map)
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#getFormResult(java.util.Map, int, int, java.util.Map)
	 */
	W5FormResult getFormResult(Map<String, Object> scd, int formId, int action, Map<String, String> requestParams);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#getQueryResult(java.util.Map, int)
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#getQueryResult(java.util.Map, int)
	 */
	W5QueryResult getQueryResult(Map<String, Object> scd, int queryId);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#getPageResult(java.util.Map, int)
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#getPageResult(java.util.Map, int)
	 */
	W5PageResult getPageResult(Map<String, Object> scd, int pageId);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#getCardResult(java.util.Map, int, java.util.Map, boolean)
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#getCardResult(java.util.Map, int, java.util.Map, boolean)
	 */
	W5CardResult getCardResult(Map<String, Object> scd, int cardId, Map<String, String> requestParams,
			boolean noSearchForm);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#getGridResult(java.util.Map, int, java.util.Map, boolean)
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#getGridResult(java.util.Map, int, java.util.Map, boolean)
	 */
	W5GridResult getGridResult(Map<String, Object> scd, int gridId, Map<String, String> requestParams,
			boolean noSearchForm);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#reloadProjectsCache(int)
	 */
	List<W5Project> reloadProjectsCache(int cid);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#setApplicationSettingsValues()
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#setApplicationSettingsValues()
	 */
	void setApplicationSettingsValues();

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#reloadLookUpCache(java.lang.String)
	 */
	void reloadLookUpCache(String projectId);


	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadMobileCache()
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#reloadMobileCache()
	 */
	void reloadMobileCache();

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadTablesCache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#reloadTablesCache(java.lang.String)
	 */
	void reloadTablesCache(String projectId);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadWorkflowCache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#reloadWorkflowCache(java.lang.String)
	 */
	void reloadWorkflowCache(String projectId);


	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadDeveloperEntityKeys()
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#reloadDeveloperEntityKeys()
	 */
	void reloadDeveloperEntityKeys();

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadFrameworkCaches(int)
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#reloadFrameworkCaches(int)
	 */
	void reloadFrameworkCaches(int customizationId);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadProjectCaches(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#reloadProjectCaches(java.lang.String)
	 */
	void reloadProjectCaches(String projectId);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadWsServersCache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#reloadWsServersCache(java.lang.String)
	 */
	void reloadWsServersCache(String projectId);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#reloadWsClientsCache(java.lang.String)
	 */
	void reloadWsClientsCache(String projectId);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#getGlobalFuncResult(java.util.Map, int)
	 */
	W5GlobalFuncResult getGlobalFuncResult(Map<String, Object> scd, int globalFuncId);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#getMListResult(java.util.Map, int, java.util.Map, boolean)
	 */
	M5ListResult getMListResult(Map<String, Object> scd, int listId, Map<String, String> requestParams,
			boolean noSearchForm);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#getListViewResult(java.util.Map, int, java.util.Map, boolean)
	 */
	W5ListViewResult getListViewResult(Map<String, Object> scd, int listViewId, Map<String, String> requestParams,
			boolean noSearchForm);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#addProject2Cache(java.lang.String)
	 */
	void addProject2Cache(String projectId);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#getMetadataObject(java.lang.String, java.lang.String, int, java.lang.Object, java.lang.String)
	 */
	Object getMetadataObject(String objectName, String pkFieldName, int objectId, Object projectId, String onErrorMsg);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#loadProject(java.lang.String)
	 */
	W5Project loadProject(String newProjectId);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#findObjectMailSetting(java.util.Map, int)
	 */
	W5ObjectMailSetting findObjectMailSetting(Map<String, Object> scd, int mailSettingId);

	

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#findLookUpDetay(int, java.lang.String)
	 */
	List<W5LookUpDetay> findLookUpDetay(int lookupQueryId, String projectUuid);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#findTableCalcFieldByName(java.lang.String, int, java.lang.String)
	 */
	List<W5TableFieldCalculated> findTableCalcFieldByName(String projectUuid, int tableId, String fieldName);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#findTableCalcFields(java.lang.String, int)
	 */
	List<W5TableFieldCalculated> findTableCalcFields(String projectUuid, int tableId);

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.rdbms.MetadataLoader#findFirstCRUDForm4Table(int, java.lang.String)
	 */
	int findFirstCRUDForm4Table(int tableId, String projectUuid);
	
	
	public boolean changeActiveProject(Map<String, Object> scd, String projectUuid);
	
	public int getGlobalNextval(String id);
	
	public Map<String, Object> getProjectMetadata(String projectId);


}