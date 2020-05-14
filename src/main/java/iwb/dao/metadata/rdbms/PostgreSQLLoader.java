package iwb.dao.metadata.rdbms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
//import org.redisson.api.RMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.rdbms_impl.BaseDAO;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.domain.db.M5List;
import iwb.domain.db.W5Card;
import iwb.domain.db.W5Component;
import iwb.domain.db.W5Conversion;
import iwb.domain.db.W5Customization;
import iwb.domain.db.W5ExternalDb;
import iwb.domain.db.W5Form;
import iwb.domain.db.W5FormCell;
import iwb.domain.db.W5FormCellProperty;
import iwb.domain.db.W5FormModule;
import iwb.domain.db.W5FormSmsMail;
import iwb.domain.db.W5GlobalFunc;
import iwb.domain.db.W5GlobalFuncParam;
import iwb.domain.db.W5Grid;
import iwb.domain.db.W5GridColumn;
import iwb.domain.db.W5JobSchedule;
import iwb.domain.db.W5List;
import iwb.domain.db.W5ListBase;
import iwb.domain.db.W5ListColumn;
import iwb.domain.db.W5LookUp;
import iwb.domain.db.W5LookUpDetay;
import iwb.domain.db.W5Mq;
import iwb.domain.db.W5MqCallback;
import iwb.domain.db.W5ObjectMailSetting;
import iwb.domain.db.W5ObjectToolbarItem;
import iwb.domain.db.W5Page;
import iwb.domain.db.W5PageObject;
import iwb.domain.db.W5Project;
import iwb.domain.db.W5Query;
import iwb.domain.db.W5QueryField;
import iwb.domain.db.W5QueryParam;
import iwb.domain.db.W5Table;
import iwb.domain.db.W5TableChild;
import iwb.domain.db.W5TableEvent;
import iwb.domain.db.W5TableField;
import iwb.domain.db.W5TableFieldCalculated;
import iwb.domain.db.W5TableParam;
import iwb.domain.db.W5VcsCommit;
import iwb.domain.db.W5Workflow;
import iwb.domain.db.W5WorkflowStep;
import iwb.domain.db.W5Ws;
import iwb.domain.db.W5WsMethod;
import iwb.domain.db.W5WsMethodParam;
import iwb.domain.db.W5WsServer;
import iwb.domain.db.W5WsServerMethod;
import iwb.domain.db.W5WsServerMethodParam;
import iwb.domain.helper.W5FormCellHelper;
import iwb.domain.result.M5ListResult;
import iwb.domain.result.W5CardResult;
import iwb.domain.result.W5FormResult;
import iwb.domain.result.W5GlobalFuncResult;
import iwb.domain.result.W5GridResult;
import iwb.domain.result.W5ListViewResult;
import iwb.domain.result.W5PageResult;
import iwb.domain.result.W5QueryResult;
import iwb.enums.FieldDefinitions;
import iwb.exception.IWBException;
import iwb.util.EncryptionUtil;
import iwb.util.GenericUtil;
import iwb.util.UserUtil;

@Repository
public class PostgreSQLLoader extends BaseDAO implements MetadataLoader {
	private static Logger logger = Logger.getLogger(PostgreSQLLoader.class);

	@Lazy
	@Autowired
	private PostgreSQL dao;

//	private RMap<String, RMap> redisGlobalMap = null;

	private boolean loadForm(W5FormResult fr) {
		// f.setForm((W5Form)loadObject(W5Form.class, f.getFormId()));

		// int customizationId = (Integer)f.getScd().get("customizationId");
		String projectId = FrameworkCache.getProjectId(fr.getScd(), "40." + fr.getFormId());

			/*
			 * try { form = (W5Form) (redisGlobalMap.get(projectId + ":form:" +
			 * fr.getFormId()));// rformMap.get(fr.getFormId()); if (form != null &&
			 * form.get_moduleList() == null) { form.set_moduleList(new ArrayList()); } // }
			 * } catch (Exception e) { throw new IWBException("framework", "Redis.Form",
			 * fr.getFormId(), null, "Loading Form from Redis", e); }
			 */

		W5Form form = (W5Form) getMetadataObject("W5Form","formId", fr.getFormId(),
					projectId, "Form"); // ozel bir client icin varsa

		form.set_formCells(find(
				"from W5FormCell t where t.formId=?0 AND t.projectUuid=?1 order by t.tabOrder, t.xOrder, t.dsc",
				fr.getFormId(), projectId));
		if (form.getRenderType() != 0) { // eger baska turlu render
										// edilecekse
			form.set_moduleList(
					find("from W5FormModule t where t.formId=?0 AND t.projectUuid=?1 order by t.tabOrder",
							form.getFormId(), projectId));
		}

		form.set_toolbarItemList(find(
				"from W5ObjectToolbarItem t where t.objectType=?0 AND t.objectId=?1 AND t.projectUuid=?2 order by t.tabOrder",
				(short) 40, fr.getFormId(), projectId));
		form.set_formHintList(find(
				"from W5FormHint h where h.activeFlag=1 AND h.formId=?0 AND h.projectUuid=?1 order by h.tabOrder",
				fr.getFormId(), projectId));
		if (form.get_formHintList().isEmpty())
			form.set_formHintList(null);


		fr.setForm(form);

		Map<Integer, W5FormCell> formCellMap = new HashMap();
		for (W5FormCell fc : form.get_formCells()) {
			formCellMap.put(fc.getFormCellId(), fc);
		}
		List<W5FormCellProperty> lfcp = find(
				"select t from W5FormCellProperty t, W5FormCell f where t.formCellId=f.formCellId AND f.projectUuid=t.projectUuid AND f.formId=?0 AND t.projectUuid=?1",
				form.getFormId(), projectId);
		for (W5FormCellProperty fcp : lfcp) {
			W5FormCell fc = formCellMap.get(fcp.getFormCellId());
			if (fc.get_formCellPropertyList() == null)
				fc.set_formCellPropertyList(new ArrayList());
			fc.get_formCellPropertyList().add(fcp);
		}

		if (form.getObjectType() != 1 && form.getRenderTemplateId() > 0) { //  if not grid(seachForm)
			W5Page page = getPageResult(fr.getScd(), form.getRenderTemplateId()).getPage();
			form.set_renderTemplate(page);
		}
		Map<Short, W5Workflow> mam = null;
		W5Table mt = null;
		switch (form.getObjectType()) {
		case 6: // conversion icin
			W5Conversion c = FrameworkCache.getConversion(projectId, form.getObjectId());
			for (W5FormCell fc : form.get_formCells())
				if (fc.getObjectDetailId() != 0) {
					fc.set_sourceObjectDetail(c.get_conversionColMap().get(fc.getObjectDetailId()));
				}

			break;
		case 2: // table icin ise
			// f.setTable((W5Table)loadObject(W5Table.class,
			// f.getForm().getObjectId()));
			W5Table t = FrameworkCache.getTable(projectId, form.getObjectId());
			// f.getForm().set_sourceTable(t);
			Map<String, W5TableField> fieldMap1 = new HashMap();
			for (W5TableField tf : (List<W5TableField>) t.get_tableFieldList()) {
				fieldMap1.put(tf.getDsc(), tf);
			}
			for (W5FormCell fc : form.get_formCells())
				if (fc.getObjectDetailId() != 0) {
					fc.set_sourceObjectDetail(t.get_tableFieldMap().get(fc.getObjectDetailId()));
				}
			if ((fieldMap1.get("INSERT_USER_ID") != null || fieldMap1.get("insert_user_id") != null)
					&& (fieldMap1.get("VERSION_USER_ID") != null || fieldMap1.get("version_user_id") != null)) {
				form.set_versioningFlag(true);
			}
			if (FrameworkSetting.sms || FrameworkSetting.mail) {
				if (!FrameworkSetting.redisCache)
					form.set_formSmsMailList(find(
							"from W5FormSmsMail t where t.projectUuid=?0 AND t.formId=?1 AND t.activeFlag=1 order by t.smsMailSentType,t.tabOrder",
							projectId, fr.getFormId()));
				if (GenericUtil.isEmpty(form.get_formSmsMailList()))
					form.set_formSmsMailList(null);
				else {
					form.set_formSmsMailMap(new HashMap());
					for (W5FormSmsMail fsm : form.get_formSmsMailList()) {
						form.get_formSmsMailMap().put(fsm.getFormSmsMailId(), fsm);
					}
				}
			}

			form.set_conversionList(listConversion4Form(projectId, fr.getFormId(), form.getObjectId()));

			break;
		case 1: // grid icin ise
			Object[] ooo = (Object[]) find(
					"select t.queryId,(select q.sourceObjectId from W5Query q where q.queryId=t.queryId AND q.projectUuid=t.projectUuid) from W5Grid t where t.projectUuid=?0 AND t.gridId=?1",
					projectId, form.getObjectId()).get(0);
			int queryId = (Integer) ooo[0];
			int sourceObjectId = (Integer) ooo[1];
			if (sourceObjectId > 0)
				mt = FrameworkCache.getTable(projectId, sourceObjectId); // f.getForm().set_sourceTable()
			W5Query query = getQueryResult(fr.getScd(), queryId).getQuery();
			form.set_sourceQuery(query);
			Map<Integer, W5QueryParam> fieldMap2 = new HashMap();
			for (W5QueryParam tf : form.get_sourceQuery().get_queryParams()) {
				fieldMap2.put(tf.getQueryParamId(), tf);
			}
			for (W5FormCell fc : form.get_formCells())
				if (fc.getObjectDetailId() != 0) {
					if (fc.getObjectDetailId() > 0)
						fc.set_sourceObjectDetail(fieldMap2.get(fc.getObjectDetailId())); // queryField'dan
					else if (mt != null) {
						fc.set_sourceObjectDetail(mt.get_tableFieldMap().get(-fc.getObjectDetailId()));
					}
				}
			// onay mekanizmasi icin
			if (mt != null)
				mam = mt.get_approvalMap();

			break;
		case 3:
		case 4: // db func
			W5GlobalFunc dbf = getGlobalFuncResult(fr.getScd(), form.getObjectId()).getGlobalFunc();
			Map<Integer, W5GlobalFuncParam> fieldMap3 = new HashMap();
			for (W5GlobalFuncParam tf : dbf.get_dbFuncParamList()) {
				fieldMap3.put(tf.getGlobalFuncParamId(), tf);
			}
			for (W5FormCell fc : form.get_formCells())
				if (fc.getObjectDetailId() != 0) {
					fc.set_sourceObjectDetail(fieldMap3.get(fc.getObjectDetailId()));
				}
		}


		if (mam != null && !mam.isEmpty()) { // map of ApprovalManagement
			int maxFirstColumnTabOrder = 0;
			for (W5FormCell c : form.get_formCells())
				if (c.getFormModuleId() == 0 && c.getTabOrder() < 1000) {
					maxFirstColumnTabOrder++;
				}
			for (short actionTip : mam.keySet()) {
				W5FormCell approvalCell = new W5FormCell();
				approvalCell.setTabOrder((short) (-actionTip));
				approvalCell.setDsc("_approval_step_ids" + actionTip);
				approvalCell.setControlType((short) 15); // low-combo query
				approvalCell.setLookupQueryId(606); // approval steps
				approvalCell.setLookupIncludedParams("xapproval_id=" + mam.get(actionTip).getWorkflowId());
				approvalCell.setControlWidth((short) 250);
				approvalCell.setLocaleMsgKey("approval_status"); // mam.get(actionTip).getDsc()
				approvalCell.setInitialSourceType((short) 10); // approvalStates
				// approvalCell.setInitialValue(""+mam.get(actionTip).getApprovalId());//workflowId
				approvalCell.setActiveFlag((short) 1);
				form.get_formCells().add(0, /* maxFirstColumnTabOrder, */ approvalCell);
			}
		}
		return true;
	}

	private List<W5Conversion> listConversion4Form(String projectId, int formId, int tableId) {
		if (FrameworkSetting.redisCache) {
			return FrameworkCache.listConversion4Form(projectId, formId, tableId);
		} else {
			List<W5Conversion> lc = find(
					"from W5Conversion t where t.projectUuid=?0 AND ((t.srcDstTip=0 AND t.srcFormId=?1) OR (t.srcDstTip=1 AND t.srcTableId=?2)) AND t.activeFlag=1 order by t.tabOrder",
					projectId, formId, tableId);
			for (W5Conversion cnv : lc) {
				cnv.set_conversionColList(
						find("from W5ConversionCol t where t.projectUuid=?0 AND t.conversionId=?1 order by t.tabOrder",
								projectId, cnv.getConversionId()));
				FrameworkCache.addConversion(projectId, cnv);
			}
			return lc;

		}
	}

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#getFormResult(java.util.Map, int, int, java.util.Map)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getFormResult(java.util.Map, int, int, java.util.Map)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getFormResult(java.util.Map, int, int, java.util.Map)
	 */
	@Override
	public W5FormResult getFormResult(Map<String, Object> scd, int formId, int action,
			Map<String, String> requestParams) {
		W5FormResult formResult = new W5FormResult(formId);
		formResult.setScd(scd);
		formResult.setErrorMap(new HashMap());
		formResult.setAction(action);
		formResult.setRequestParams(requestParams);
		formResult.setOutputFields(new HashMap());
		formResult.setPkFields(new HashMap());
		formResult.setOutputMessages(new ArrayList());
		formResult.setExtraFormCells(new ArrayList());
		W5Form f = FrameworkCache.getForm(scd, formId);
		if (f == null) {
			loadForm(formResult);
			f = formResult.getForm();
			FrameworkCache.addForm(scd, f);
		}
		formResult.setForm(f);

		if (formResult.getForm().get_moduleList() != null) { // eger baska turlu
																// render
																// edilecekse
			for (W5FormModule m : formResult.getForm().get_moduleList()) { // form
				switch (m.getModuleType()) {
				case 5: // grid
					if (formResult.getModuleGridMap() == null)
						formResult.setModuleGridMap(new HashMap());
					formResult.getModuleGridMap().put(m.getObjectId(),
							getGridResult(scd, m.getObjectId(), requestParams, true));
					break;
				case 6: // query4formcell
					W5QueryResult queryResult4FormCell = dao.executeQuery(scd, m.getObjectId(), requestParams);
					if (formResult.getFormCellResults() == null)
						formResult.setFormCellResults(
								new ArrayList<W5FormCellHelper>(queryResult4FormCell.getData().size()));
					for (Object[] d : queryResult4FormCell.getData()) {
						W5FormCellHelper result = GenericUtil.getFormCellResultByQueryRecord(d);
						if (m.getTabOrder() != 0)
							result.getFormCell()
									.setTabOrder((short) (result.getFormCell().getTabOrder() + m.getTabOrder()));
						formResult.getFormCellResults().add(result);
					}
					break;
				case 10:// mobile list
					if (formResult.getModuleListMap() == null)
						formResult.setModuleListMap(new HashMap());
					formResult.getModuleListMap().put(m.getObjectId(),
							getMListResult(scd, m.getObjectId(), requestParams, true));

				}
			}
		}

		return formResult;
	}

	private void loadQuery(W5QueryResult qr, String projectId) {
		W5Query query = null;
		if (FrameworkSetting.redisCache) {
			/*
			 * try { // RMap<Integer, W5Query> rqueryMap = //
			 * FrameworkCache.getRedissonClient().getMap(String.format(
			 * "icb-cache2:%s:query", // projectId)); // if (rqueryMap != null) {//
			 * rgridMap.getName() query = (W5Query) (redisGlobalMap.get(projectId +
			 * ":query:" + qr.getQueryId())); ;// rqueryMap.get(qr.getQueryId()); // } }
			 * catch (Exception e) { throw new IWBException("framework", "Redis.Query",
			 * qr.getQueryId(), null, "Loading Query from Redis", e); }
			 */
		} else {
			query = (W5Query) getMetadataObject("W5Query","queryId",
					qr.getQueryId(), projectId, "Query");

			query.set_queryFields(find(
					"from W5QueryField t where t.queryId=?0 AND t.tabOrder>0 AND t.postProcessType!=99 AND t.projectUuid=?1 order by t.tabOrder",
					qr.getQueryId(), projectId));
			query.set_queryParams(
					find("from W5QueryParam t where t.queryId=?0 AND t.projectUuid=?1 order by t.tabOrder",
							qr.getQueryId(), projectId));

			for (W5QueryField f : query.get_queryFields())
				if (f.getPostProcessType() == 31 && (f.getFieldType() == 3 || f.getFieldType() == 4)) {
					if (query.get_aggQueryFields() == null)
						query.set_aggQueryFields(new ArrayList());
					query.get_aggQueryFields().add(f);
				}

		}
		qr.setQuery(query);

		if (query.getShowParentRecordFlag() != 0)
			for (W5QueryField field : query.get_queryFields()) {
				if (field.getDsc().equals("table_id"))
					query.set_tableIdTabOrder(field.getTabOrder());
				if (field.getDsc().equals("table_pk"))
					query.set_tablePkTabOrder(field.getTabOrder());

			}
	}

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#getQueryResult(java.util.Map, int)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getQueryResult(java.util.Map, int)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getQueryResult(java.util.Map, int)
	 */
	@Override
	public W5QueryResult getQueryResult(Map<String, Object> scd, int queryId) {
		if (scd != null && scd.get("customizationId") != null && (Integer) scd.get("customizationId") > 0
				&& GenericUtil.uInt(scd.get("rbac")) != 0)
			switch (queryId) { // tenant user and role conversion
			case 43:
				queryId = 4511;
				break; // lookup_user1
			case 554:
				queryId = 4512;
				break; // lookup_role1
			}
		W5QueryResult queryResult = new W5QueryResult(queryId);
		queryResult.setScd(scd);
		String projectId = FrameworkCache.getProjectId(scd, "8." + queryId);
		queryResult.setQuery(FrameworkCache.getQuery(projectId, queryId));
		if (queryResult.getQuery() == null) {
			loadQuery(queryResult, projectId);
			FrameworkCache.addQuery(projectId, queryResult.getQuery());
		}

		switch (queryResult.getQuery().getQuerySourceType()) {
		case 0:
		case 15:
			if (queryResult.getQuery().getSourceObjectId() != 0) {
				queryResult.setMainTable(FrameworkCache.getTable(projectId, queryResult.getQuery().getSourceObjectId()));
			}
		}
		return queryResult;
	}

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#getPageResult(java.util.Map, int)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getPageResult(java.util.Map, int)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getPageResult(java.util.Map, int)
	 */
	@Override
	public W5PageResult getPageResult(Map<String, Object> scd, int pageId) {

		W5PageResult pr = new W5PageResult(pageId);
		pr.setScd(scd);

		pr.setPage(FrameworkCache.getPage(scd, pageId));
		if (pr.getPage() == null) {
			loadPage(pr);
			FrameworkCache.addPage(scd, pr.getPage());
		}

		return pr;
	}

	private void loadPage(W5PageResult pr) {

		String projectId = FrameworkCache.getProjectId(pr.getScd(), "63." + pr.getPageId());
			/*
			 * try { page = (W5Page) (redisGlobalMap.get(projectId + ":page:" +
			 * pr.getTemplateId()));//
			 * FrameworkCache.getRedissonClient().getMap(String.format("icb-cache4:%s:page",
			 * // projectId)); if (page != null && page.get_pageObjectList() == null) {
			 * page.set_pageObjectList(new ArrayList()); } } catch (Exception e) { throw new
			 * IWBException("framework", "Redis.Page", pr.getTemplateId(), null,
			 * "Loading Page from Redis", e); }
			 */

		W5Page page = (W5Page) getMetadataObject("W5Page","pageId",
					pr.getPageId(), projectId, "Page"); 
		
		page.set_pageObjectList(find(
				"from W5PageObject t where t.activeFlag=1 AND t.pageId=?0 AND t.projectUuid=?1 order by t.tabOrder",
				pr.getPageId(), projectId));

		for (W5PageObject to : page.get_pageObjectList())
			if (to.getSrcQueryFieldId() != null && to.getDstQueryParamId() != null) {
				List p = new ArrayList();
				p.add(projectId);
				p.add(to.getSrcQueryFieldId());
				p.add(projectId);
				p.add(to.getDstQueryParamId());
				String sql = "select (select f1.dsc from iwb.w5_query_field f1 where f1.project_uuid=? AND f1.query_field_id=?) f_dsc, "
						+ "(select p1.dsc from iwb.w5_query_param p1 where p1.project_uuid=? AND p1.query_param_id=?) p_dsc";
				if (to.getDstStaticQueryParamId() != null && !GenericUtil.isEmpty(to.getDstStaticQueryParamVal())) {
					p.add(projectId);
					p.add(to.getDstStaticQueryParamId());
					sql += ",(select p1.dsc from iwb.w5_query_param p1 where p1.project_uuid=? AND p1.query_param_id=?) ps_dsc";
				}
				List l = executeSQLQuery2Map(sql, p);
				if (!GenericUtil.isEmpty(l)) {
					Map m = (Map) l.get(0);
					to.set_srcQueryFieldName((String) m.get("f_dsc"));
					to.set_dstQueryParamName((String) m.get("p_dsc"));
					if (m.containsKey("ps_dsc"))
						to.set_dstStaticQueryParamName((String) m.get("ps_dsc"));
				}
			}

		pr.setPage(page);

	}

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#getCardResult(java.util.Map, int, java.util.Map, boolean)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getCardResult(java.util.Map, int, java.util.Map, boolean)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getCardResult(java.util.Map, int, java.util.Map, boolean)
	 */
	@Override
	public W5CardResult getCardResult(Map<String, Object> scd, int cardId, Map<String, String> requestParams,
			boolean noSearchForm) {
		W5CardResult cr = new W5CardResult(cardId);
		String projectId = FrameworkCache.getProjectId(scd, "930." + cardId);
		cr.setRequestParams(requestParams);
		cr.setScd(scd);

		cr.setCard(FrameworkCache.getCard(projectId, cardId));
		if (cr.getCard() == null) {
			loadCard(cr);
			FrameworkCache.addCard(projectId, cr.getCard());
		}
		// search Form
		if (!noSearchForm && cr.getCard().get_searchFormId() != 0) {
			W5FormResult searchForm = getFormResult(scd, cr.getCard().get_searchFormId(), 2, requestParams);
			dao.initializeForm(searchForm, false);
			dao.loadFormCellLookups(cr.getScd(), searchForm.getFormCellResults(), cr.getRequestParams(), null);
			cr.setSearchFormResult(searchForm);
		}
		return cr;
	}

	private Integer findFormId4Object(int objectType, int objectId, String projectId) {
		List<Integer> l = find("select t.formId from W5Form t where t.objectType=?0 and t.objectId=?1 and t.projectUuid=?2",
				(short)objectType, objectId, projectId);
		return l.isEmpty() ? null : l.get(0);
	}
	
	private void loadCard(W5CardResult cr) {
		String projectId = FrameworkCache.getProjectId(cr.getScd(), "930." + cr.getCardId());
		W5Card card = null;

		if (FrameworkSetting.redisCache) {
			/*
			 * try { card = (W5Card) (redisGlobalMap.get(projectId + ":card:" +
			 * cr.getDataViewId()));// rcardMap.get(cr.getDataViewId()); } catch (Exception
			 * e) { throw new IWBException("framework", "Redis.Card", cr.getDataViewId(),
			 * null, "Loading Card from Redis", e); }
			 */
		} else {
			card = (W5Card) getMetadataObject("W5Card","cardId",
					cr.getCardId(), projectId, "Card"); // ozel bir client
															// icin varsa

			card.set_toolbarItemList(find(
					"from W5ObjectToolbarItem t where t.objectType=8 AND t.objectId=?0 AND t.projectUuid=?1 order by t.tabOrder",
					card.getCardId(), projectId));
			card.set_menuItemList(find(
					"from W5ObjectMenuItem t where t.objectType=8 AND t.objectId=?0 AND t.projectUuid=?1 order by t.tabOrder",
					card.getCardId(), projectId));
			Integer searchFormId = findFormId4Object(8, cr.getCardId(), projectId);
			if (searchFormId != null)
				card.set_searchFormId(searchFormId);
		}
		cr.setCard(card);

		W5Query query = getQueryResult(cr.getScd(), card.getQueryId()).getQuery();
		if (!GenericUtil.isEmpty(card.getOrderQueryFieldIds())) {
			card.set_orderQueryFieldNames(new ArrayList());
			for (W5QueryField qf : query.get_queryFields())
				if (GenericUtil.hasPartInside2(card.getOrderQueryFieldIds(), qf.getQueryFieldId())) {
					card.get_orderQueryFieldNames().add(qf.getDsc());
				}
		}

		card.set_query(query);

		card.set_crudTable(FrameworkCache.getTable(projectId, query.getSourceObjectId()));

		Map<Integer, W5QueryField> fieldMap = new HashMap<Integer, W5QueryField>();
		Map<String, W5QueryField> fieldMapDsc = new HashMap<String, W5QueryField>();
		for (W5QueryField field : query.get_queryFields()) {
			fieldMap.put(field.getQueryFieldId(), field);
			fieldMapDsc.put(field.getDsc(), field);
		}

		card.set_queryFieldMap(fieldMap);

		card.set_queryFieldMapDsc(fieldMapDsc);
		card.set_pkQueryField(fieldMap.get(card.getPkQueryFieldId()));

		if (card.getDefaultCrudFormId() != 0) {
			W5Form defaultCrudForm = (W5Form) getMetadataObject(
					"W5Form","formId", card.getDefaultCrudFormId(), projectId,
					"Form"); // ozel bir
								// client
								// icin
								// varsa

			if (defaultCrudForm != null) {
				// defaultCrudForm.set_sourceTable(PromisCache.getTable(customizationId,
				// defaultCrudForm.getObjectId()));
				W5Table t = FrameworkCache.getTable(projectId, defaultCrudForm.getObjectId()); // PromisCache.getTable(f.getScd(),
																								// f.getForm().getObjectId())
				card.set_defaultCrudForm(defaultCrudForm);

				card.set_crudFormSmsMailList(find(
						"from W5FormSmsMail t where t.activeFlag=1 AND t.actionTypes like '%0%' AND t.formId=?0 AND t.projectUuid=?1 order by t.tabOrder",
						card.getDefaultCrudFormId(), projectId));
				card.set_crudFormConversionList(find(
						"from W5Conversion t where t.activeFlag=1 AND t.actionTypes like '%0%' AND t.srcFormId=?0 AND t.projectUuid=?1 order by t.tabOrder",
						card.getDefaultCrudFormId(), projectId));

				organizeListPostProcessQueryFields(cr.getScd(), t, card);

			}
		}
	}

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#getGridResult(java.util.Map, int, java.util.Map, boolean)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getGridResult(java.util.Map, int, java.util.Map, boolean)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getGridResult(java.util.Map, int, java.util.Map, boolean)
	 */
	@Override
	public W5GridResult getGridResult(Map<String, Object> scd, int gridId, Map<String, String> requestParams,
			boolean noSearchForm) {
		W5Grid g = null;
		try {
			String projectId = FrameworkCache.getProjectId(scd, "5." + gridId);
			W5GridResult gridResult = new W5GridResult(gridId);
			gridResult.setRequestParams(requestParams);
			gridResult.setScd(scd);
			g = FrameworkCache.getGrid(projectId, gridId);
			if (g != null) {
				gridResult.setGrid(g);
			} else {
				loadGrid(gridResult);
				g = gridResult.getGrid();
				FrameworkCache.addGrid(projectId, g);
			}

			// search Form
			if (!noSearchForm && g.get_searchFormId() != 0)
				try {
					W5FormResult searchForm = getFormResult(scd, g.get_searchFormId(), 2, requestParams);
					dao.initializeForm(searchForm, false);
					dao.loadFormCellLookups(scd, searchForm.getFormCellResults(), requestParams, null);
					gridResult.setSearchFormResult(searchForm);
				} catch (Exception e) {
					throw new IWBException("framework", "Load.SearchForm", g.get_searchFormId(), null,
							"[40," + g.get_searchFormId() + "]", e);
				}

			gridResult.setFormCellResultMap(new HashMap());

			for (W5GridColumn column : g.get_gridColumnList())
				if (column.get_formCell() != null) {
					W5FormCellHelper cellResult = new W5FormCellHelper(column.get_formCell());
					gridResult.getFormCellResultMap().put(column.get_formCell().getFormCellId(), cellResult);
				}

			if (!gridResult.getFormCellResultMap().isEmpty())
				dao.loadFormCellLookups(gridResult.getScd(), new ArrayList(gridResult.getFormCellResultMap().values()),
						gridResult.getRequestParams(), null);

			if (!GenericUtil.isEmpty(gridResult.getGrid().get_toolbarItemList()))
				for (W5ObjectToolbarItem ti : gridResult.getGrid().get_toolbarItemList()) {
					if ((ti.getControlType() == 7 || ti.getControlType() == 15) && ti.getLookupQueryId() > 0) {

						W5QueryResult lookupQueryResult = getQueryResult(scd, ti.getLookupQueryId());
						lookupQueryResult.setErrorMap(new HashMap());
						lookupQueryResult.setRequestParams(requestParams);
						lookupQueryResult.setOrderBy(lookupQueryResult.getQuery().getSqlOrderby());
						lookupQueryResult.prepareQuery(null);
						if (lookupQueryResult.getErrorMap().isEmpty()) {
							dao.runQuery(lookupQueryResult);
							if (gridResult.getExtraOutMap() == null)
								gridResult.setExtraOutMap(new HashMap());
							gridResult.getExtraOutMap().put("_tlb_" + ti.getToolbarItemId(), lookupQueryResult);
						}

					}
				}

			return gridResult;
		} catch (Exception e) {
			throw new IWBException("framework", "Load.Grid", gridId, null,
					"[5," + gridId + "]" + (g != null ? " " + g.getDsc() : ""), e);
		}
	}

	private void loadGrid(W5GridResult gr) {
		String projectId = FrameworkCache.getProjectId(gr.getScd(), "5." + gr.getGridId());

		W5Grid grid = null;
		if (FrameworkSetting.redisCache) {
			/*
			 * try { grid = (W5Grid) (redisGlobalMap.get(projectId + ":grid:" +
			 * gr.getGridId()));//
			 * FrameworkCache.getRedissonClient().getMap(String.format("icb-cache2:%s:grid",
			 * // projectId)); if(grid.getColumnRenderTip() == 1 &&
			 * grid.get_gridModuleList() == null) grid.set_gridModuleList(new ArrayList());
			 * 
			 * } catch (Exception e) { throw new IWBException("framework", "Redis.Grid",
			 * gr.getGridId(), null, "Loading Grid from Redis", e); }
			 */
		} else {
			grid = (W5Grid) getMetadataObject("W5Grid","gridId", gr.getGridId(),
					projectId, "Grid"); // ozel bir client icin varsa

			grid.set_gridColumnList(
					find("from W5GridColumn t where t.projectUuid=?0 AND t.gridId=?1 order by t.tabOrder", projectId,
							gr.getGridId()));
			grid.set_toolbarItemList(find(
					"from W5ObjectToolbarItem t where t.objectType=?0 AND t.objectId=?1 AND t.projectUuid=?2 order by t.tabOrder",
					(short) 5, gr.getGridId(), projectId));
			grid.set_menuItemList(find(
					"from W5ObjectMenuItem t where t.objectType=?0 AND t.objectId=?1 AND t.projectUuid=?2 order by t.tabOrder",
					(short) 5, gr.getGridId(), projectId));

			Integer searchFormId = findFormId4Object(
					1, gr.getGridId(), projectId);
			if (searchFormId != null)
				grid.set_searchFormId(searchFormId);

		}

		gr.setGrid(grid);
		switch (grid.getRowColorFxType()) {
		case 1:
			if (grid.getRowColorFxQueryFieldId() != 0) { // lookup eslesme
				grid.set_listCustomGridColumnRenderer(find(
						"from W5CustomGridColumnRenderer t where t.projectUuid=?0 AND t.gridId=?1 AND t.queryFieldId=?2",
						projectId, gr.getGridId(), grid.getRowColorFxQueryFieldId()));
			}
			break;
		case 2:
		case 3:
			if (grid.getRowColorFxQueryFieldId() != 0 || grid.getRowColorFxType() == 3) { // kosul
				grid.set_listCustomGridColumnCondition(find(
						"from W5CustomGridColumnCondition t where t.projectUuid=?0 AND t.gridId=?1 AND t.queryFieldId=?2 order by t.tabOrder",
						projectId, gr.getGridId(), grid.getRowColorFxQueryFieldId()));
			}
			break;
		}

		W5Query query = getQueryResult(gr.getScd(), grid.getQueryId()).getQuery();

		grid.set_query(query);

		grid.set_viewTable(FrameworkCache.getTable(projectId, query.getSourceObjectId()));

		Map<Integer, W5QueryField> fieldMap = new HashMap<Integer, W5QueryField>();
		Map<String, W5QueryField> fieldMapDsc = new HashMap<String, W5QueryField>();
		for (W5QueryField field : query.get_queryFields()) {
			fieldMap.put(field.getQueryFieldId(), field);
			fieldMapDsc.put(field.getDsc(), field);
		}
		if (grid.get_viewTable() != null) { // extended fields
			int qi = 0;
			for (qi = 0; qi < grid.get_viewTable().get_tableFieldList().size(); qi++) {
				W5TableField tf = grid.get_viewTable().get_tableFieldList().get(qi);
			}
		}
		grid.set_queryFieldMap(fieldMap);

		grid.set_queryFieldMapDsc(fieldMapDsc);
		grid.set_autoExpandField(fieldMap.get(grid.getAutoExpandFieldId()));
		grid.set_pkQueryField(fieldMap.get(grid.getPkQueryFieldId()));
		if (grid.get_pkQueryField() == null) {
			if (FrameworkSetting.debug)
				throw new IWBException("framework", "Grid", grid.getGridId(), null, "Grid PK Missing", null);
			grid.set_pkQueryField(query.get_queryFields().get(0));
		}
		grid.set_groupingField(fieldMap.get(grid.getGroupingFieldId()));
		grid.set_fxRowField(fieldMap.get(grid.getRowColorFxQueryFieldId()));

		int formCellCounter = 1;
		for (W5GridColumn column : grid.get_gridColumnList()) {
			column.set_queryField(fieldMap.get(column.getQueryFieldId()));
			if (column.getFormCellId() > 0) { // form_cell
				W5FormCell cell = (W5FormCell) getMetadataObject(
						"W5FormCell","formCellId", column.getFormCellId(),
						projectId, null);
				if (cell != null) {
					column.set_formCell(cell);
				}
			} else if (column.getFormCellId() < 0) { // control
				W5FormCell cell = new W5FormCell(-formCellCounter++);
				cell.setControlType((short) -column.getFormCellId());
				cell.setDsc(column.get_queryField().getDsc());
				cell.setFormCellId(column.getQueryFieldId());
				column.set_formCell(cell);
			}
		}

		if (grid.get_toolbarItemList() != null)
			for (W5ObjectToolbarItem c : grid.get_toolbarItemList())
				switch (c.getControlType()) { // TODO:toolbar icine bisey
											// konulacaksa
				case 10:
				case 7:
				case 15:
				case 9:
					break;
				case 14:
				case 8:
				case 6:
					break;
				}
		// if(grid.getSelectionModeTip()==4)

		if (grid.getDefaultCrudFormId() != 0) {
			W5Form defaultCrudForm = getFormResult(gr.getScd(), grid.getDefaultCrudFormId(), 2, gr.getRequestParams())
					.getForm();
			grid.set_defaultCrudForm(defaultCrudForm);

			if (defaultCrudForm != null && defaultCrudForm.getObjectType() == 2) {
				// defaultCrudForm.set_sourceTable(PromisCache.getTable(customizationId,
				// defaultCrudForm.getObjectId()));
				W5Table t = FrameworkCache.getTable(projectId, defaultCrudForm.getObjectId()); // PromisCache.getTable(f.getScd(),
																								// f.getForm().getObjectId())
				grid.set_crudTable(t);

				List<W5FormSmsMail> xcrudFormSmsList = defaultCrudForm.get_formSmsMailList();
				if (xcrudFormSmsList != null) {
					List<W5FormSmsMail> crudFormSmsList = new ArrayList();
					for (W5FormSmsMail x : xcrudFormSmsList)
						if (GenericUtil.hasPartInside2(x.getActionTypes(), 0)) {
							crudFormSmsList.add(x);
						}
					grid.set_crudFormSmsMailList(crudFormSmsList);
				}

				List<W5Conversion> xcrudFormConversionList = defaultCrudForm.get_conversionList();
				if (xcrudFormConversionList != null) {
					List<W5Conversion> crudFormConversionList = new ArrayList();
					for (W5Conversion x : xcrudFormConversionList)
						if (GenericUtil.hasPartInside2(x.getActionTypes(), 0)) {
							crudFormConversionList.add(x);
						}
					grid.set_crudFormConversionList(crudFormConversionList);
				}

				if (GenericUtil.isEmpty(grid.get_crudFormSmsMailList()))
					grid.set_crudFormSmsMailList(null);

				// Gridle ilgili onay mekanizması ataması
				organizeListPostProcessQueryFields(gr.getScd(), t, grid);
			}
		}
	}

	private void organizeListPostProcessQueryFields(Map<String, Object> scd, W5Table t, W5ListBase l) {
		// Gridle ilgili onay mekanizması ataması
		List<W5Workflow> a = find("from W5Workflow t where t.activeFlag=1 AND t.tableId = ?0 AND t.projectUuid = ?1",
				t.getTableId(), t.getProjectUuid());
		if (!a.isEmpty()) {
			l.set_workflow(a.get(0));
		}

		// extra islemler
		if (FrameworkSetting.workflow && !a.isEmpty()) { // table
															// Record
															// Approvals
			if (l.get_postProcessQueryFields() == null)
				l.set_postProcessQueryFields(new ArrayList());
			W5QueryField f = new W5QueryField();
			f.setDsc(FieldDefinitions.queryFieldName_Workflow);
			f.setFieldType((short) 5); // comment
			f.setTabOrder((short) 22); // aslinda width
			f.setPostProcessType((short) 49); // approvalPostProcess
			l.get_postProcessQueryFields().add(f);
			W5QueryField f2 = new W5QueryField();
			f2.setDsc(FieldDefinitions.queryFieldName_ArVersionNo);
			f2.setFieldType((short) 4); // comment
			f2.setTabOrder((short) 22); // aslinda width
			l.get_postProcessQueryFields().add(f2);
		}

		if (FrameworkCache.getAppSettingIntValue(scd, "file_attachment_flag") != 0 && t.getFileAttachmentFlag() != 0) {
			if (l.get_postProcessQueryFields() == null)
				l.set_postProcessQueryFields(new ArrayList());
			W5QueryField f = new W5QueryField();
			f.setDsc(FieldDefinitions.queryFieldName_FileAttachment);
			f.setFieldType((short) 2); // file attachment
			f.setTabOrder((short) 35); // aslinda width
			l.get_postProcessQueryFields().add(f);
		}
		if (FrameworkCache.getAppSettingIntValue(scd, "make_comment_flag") != 0 && t.getMakeCommentFlag() != 0) { // table
																													// Comment
			if (l.get_postProcessQueryFields() == null)
				l.set_postProcessQueryFields(new ArrayList());
			W5QueryField f = new W5QueryField();
			f.setDsc(FieldDefinitions.queryFieldName_Comment);
			f.setFieldType((short) 3); // comment
			f.setTabOrder((short) 35); // aslinda width
			// if(PromisSetting.commentSummary)f.set
			l.get_postProcessQueryFields().add(f);
		}
		if (FrameworkSetting.vcs && t.getVcsFlag() != 0) {
			if (l.get_postProcessQueryFields() == null)
				l.set_postProcessQueryFields(new ArrayList());
			W5QueryField f = new W5QueryField();
			f.setDsc(FieldDefinitions.queryFieldName_Vcs);
			f.setFieldType((short) 9); // vcs
			f.setTabOrder((short) 35); // aslinda width
			l.get_postProcessQueryFields().add(f);
		}
	}


	private void reloadJobsCache(String projectId) {
		// Job Schedule
		try {
			Map<Integer, W5JobSchedule> mjobs = new HashMap();
			for (W5JobSchedule j : (List<W5JobSchedule>) find(
					"from W5JobSchedule x where x.activeFlag=1 and x.actionStartTip=1 AND x.projectUuid=?0",
					projectId)) {
				List<Object> res = executeSQLQuery(
						"select r.user_role_id from iwb.w5_user u, iwb.w5_user_role r where u.project_uuid=r.project_uuid and u.user_id=r.user_id and u.user_id=? and r.role_id=? and r.project_uuid=?",
						j.getExecuteUserId(), j.getExecuteRoleId(), projectId);
				if (res != null)
					j.set_userRoleId(GenericUtil.uInt(res.get(0)));
				mjobs.put(j.getJobScheduleId(), j);
			}

			FrameworkCache.wJobs.put(projectId, mjobs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void reloadExternalDbsCache(String projectId) {
		FrameworkCache.addExternalDbs2Cache(projectId, (List<W5ExternalDb>) find(
				"from W5ExternalDb x where x.activeFlag=1 and x.projectUuid=?0", projectId));
	}


	private void reloadMqsCache(String projectId) {
		// Job Schedule
		try {
			Map<Integer, W5Mq> myMq = new HashMap();
			for (W5Mq j : (List<W5Mq>) find("from W5Mq x where x.activeFlag=1 and x.projectUuid=?0", projectId)) {
				myMq.put(j.getMqId(), j);
			}

			FrameworkCache.wMqs.put(projectId, myMq);

			if (!myMq.isEmpty()) {
				for (W5MqCallback j : (List<W5MqCallback>) find(
						"from W5MqCallback x where x.activeFlag=1 and x.projectUuid=?0", projectId)) {
					W5Mq mq = myMq.get(j.getMqId());
					if (mq != null) {
						if (mq.get_callbacks() == null)
							mq.set_callbacks(new ArrayList());
						mq.get_callbacks().add(j);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void reloadLocaleMsgsCache2(int cid) {
		String whereSql = cid != -1 ? (" where x.customization_id=" + cid) : "";
		List<Object[]> l = (List<Object[]>) executeSQLQuery(
				"select x.customization_id, x.locale, x.locale_msg_key, x.dsc from iwb.w5_locale_msg x " + whereSql
						+ " order by customization_id, locale, locale_msg_key, dsc");
		if (l != null)
			for (Object[] m : l) {
				LocaleMsgCache.set2((int) m[0], (String) m[1], (String) m[2], (String) m[3]);
			}

		// Publish Locale Msgs
		if (cid == -1 || cid == 0) {
			List<String> publishSet = executeSQLQuery(
					"select x.locale_msg_key from iwb.w5_locale_msg x where  x.customization_id=0 AND x.publish_flag = 1 AND x.locale='en' order by locale, locale_msg_key, dsc");
			if (publishSet != null)
				for (String m : publishSet)
					LocaleMsgCache.add2publish(m);
		}
	}


	private void reloadErrorMessagesCache(String projectId) { // TODO
//		FrameworkCache.wExceptions.clear();
		FrameworkCache.wExceptions.put(projectId, dao.find("from W5Exception t where t.projectUuid=?0 order by t.tabOrder", projectId));
		
		/*
		 * List l = executeSQLQuery(
		 * "select exc_code, locale_msg_key from iwb.w5_exception_filter order by exc_id"
		 * ); if(l!=null)for(Object[] m : (List<Object[]>)l){
		 * FrameworkCache.wExceptions.put((String)m[0], (String)m[1]); }
		 */
	}


	private void reloadApplicationSettingsCache(int cid) {
		if (cid == -1)
			FrameworkCache.appSettings.clear();
		else
			FrameworkCache.appSettings.remove(cid);
		List<Object[]> lm = (List<Object[]>) executeSQLQuery(
				"select customization_id, dsc, val from iwb.w5_app_setting where ?=-1 OR customization_id=?", cid, cid);
		if (lm != null)
			for (Object[] m : lm) {
				int customizationId = (int) m[0];
				Map<String, String> subMap = FrameworkCache.appSettings.get(customizationId);
				if (subMap == null) {
					subMap = new HashMap<String, String>();
					FrameworkCache.appSettings.put(customizationId, subMap);
				}
				subMap.put((String) m[1], (String) m[2]);
			}
		int default_customization_id = Integer
				.parseInt(FrameworkCache.appSettings.get(0).get("default_customization_id"));
		FrameworkCache.publishAppSettings.clear();
		List<Object> lm2 = (List<Object>) executeSQLQuery(
				"select dsc from iwb.w5_app_setting where customization_id=? AND publish_flag!=0",
				default_customization_id);
		if (lm2 != null)
			for (Object m : lm2) {
				FrameworkCache.publishAppSettings.add((String) m);
			}
	}


	/* (non-Javadoc)
	 * @see MetadataLoader#reloadProjectsCache(int)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadProjectsCache(int)
	 */
	@Override
	public List<W5Project> reloadProjectsCache(int cid) {
		// if(cid==-1)FrameworkCache.wProjects.clear();

		List<W5Project> lp = cid == -1 ? (List<W5Project>) find("from W5Project t")
				: (List<W5Project>) find("from W5Project t where t.customizationId=?0", cid);
		if (lp != null)
			for (W5Project p : lp) {
				List ll = executeSQLQuery(
						"select min(t.user_tip) from iwb.w5_user_tip t where t.user_tip!=122 AND t.active_flag=1 AND t.project_uuid=?::text",
						p.getProjectUuid());
				if (!GenericUtil.isEmpty(ll))
					p.set_defaultRoleGroupId(GenericUtil.uInt(ll.get(0)));
				FrameworkCache.addProject(p);
				FrameworkSetting.projectSystemStatus.put(p.getProjectUuid(), 0);

				/*
				 * if(FrameworkSetting.tsdbFlag && p.getTsdbFlag()!=0 &&
				 * !GenericUtil.isEmpty(p.getTsdbUrl()))try{
				 * p.set_tsdb(InfluxDBFactory.connect(p.getTsdbUrl(), p.getTsdbUserName(),
				 * p.getTsdbPassWord())); }catch(Exception e){ p.setTsdbFlag((short)0);
				 * if(FrameworkSetting.debug)e.printStackTrace();
				 * 
				 * }
				 */
			}
		return lp;
	}

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#setApplicationSettingsValues()
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#setApplicationSettingsValues()
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#setApplicationSettingsValues()
	 */
	@Override
	public void setApplicationSettingsValues() {
		FrameworkSetting.debug = FrameworkCache.getAppSettingIntValue(0, "debug") != 0;


		FrameworkSetting.mq = FrameworkCache.getAppSettingIntValue(0, "mq_flag") != 0;
		// FrameworkSetting.preloadWEngine =
		// FrameworkCache.getAppSettingIntValue(0, "preload_engine");
		FrameworkSetting.chat = FrameworkCache.getAppSettingIntValue(0, "chat_flag") != 0;
		// FrameworkSetting.allowMultiLogin =
		// FrameworkCache.getAppSettingIntValue(0,
		// "allow_multi_login_flag")!=0;
		// FrameworkSetting.profilePicture =
		// FrameworkCache.getAppSettingIntValue(0,
		// "profile_picture_flag")!=0;
		FrameworkSetting.alarm = FrameworkCache.getAppSettingIntValue(0, "alarm_flag") != 0;
		FrameworkSetting.sms = FrameworkCache.getAppSettingIntValue(0, "sms_flag") != 0;
		FrameworkSetting.mail = FrameworkCache.getAppSettingIntValue(0, "mail_flag") != 0;

		FrameworkSetting.vcs = FrameworkCache.getAppSettingIntValue(0, "vcs_flag") != 0;
		FrameworkSetting.vcsServer = FrameworkCache.getAppSettingIntValue(0, "vcs_server_flag") != 0;
		FrameworkSetting.vcsServerClient = FrameworkCache.getAppSettingIntValue(0, "vcs_server_client_flag") != 0;

		// if(FrameworkSetting.preloadWEngine!=0)FrameworkCache.clearPreloadCache();
		// //TODO

		FrameworkSetting.advancedSelectShowEmptyText = FrameworkCache.getAppSettingIntValue(0,
				"advanced_select_show_empty_text") != 0;
		FrameworkSetting.simpleSelectShowEmptyText = FrameworkCache.getAppSettingIntValue(0,
				"simple_select_show_empty_text") != 0;
		FrameworkSetting.cacheTimeoutRecord = FrameworkCache.getAppSettingIntValue(0, "cache_timeout_record") * 1000;
		FrameworkSetting.crudLogSchema = FrameworkCache.getAppSettingStringValue(0, "log_crud_schema",
				FrameworkSetting.crudLogSchema);

		FrameworkSetting.asyncTimeout = FrameworkCache.getAppSettingIntValue(0, "async_timeout", 100);
		//
		// if(MVAUtil.appSettings.get("file_local_path")!=null)MVAUtil.localPath=MVAUtil.appSettings.get("file_local_path");

		FrameworkSetting.onlineUsersAwayMinute = 1000 * 60
				* FrameworkCache.getAppSettingIntValue(0, "online_users_away_minute", 3);
		FrameworkSetting.onlineUsersLimitMinute = 1000 * 60
				* FrameworkCache.getAppSettingIntValue(0, "online_users_limit_minute", 10);
		FrameworkSetting.onlineUsersLimitMobileMinute = 1000 * 60
				* FrameworkCache.getAppSettingIntValue(0, "online_users_limit_mobile_minute", 7 * 24 * 60); // 7
																											// gun
		FrameworkSetting.tableChildrenMaxRecordNumber = FrameworkCache.getAppSettingIntValue(0,
				"table_children_max_record_number", 100);

		FrameworkSetting.mailPassEncrypt = FrameworkCache.getAppSettingIntValue(0, "encrypt_mail_pass") != 0;

		FrameworkSetting.mobilePush = FrameworkCache.getAppSettingIntValue(0, "mobile_push_flag") != 0;
		FrameworkSetting.mobilePushProduction = FrameworkCache.getAppSettingIntValue(0,
				"mobile_push_production_flag") != 0;

		FrameworkSetting.workflow = FrameworkCache.getAppSettingIntValue(0, "approval_flag") != 0;
		FrameworkSetting.liveSyncRecord = FrameworkCache.getAppSettingIntValue(0, "live_sync_record") != 0;

		FrameworkSetting.lookupEditFormFlag = FrameworkCache.getAppSettingIntValue(0, "lookup_edit_form_flag") != 0;
		// PromisSetting.replaceSqlSelectX =
		// PromisCache.getAppSettingIntValue(0,
		// "replace_sql_select_x")!=0;;
	}


	/* (non-Javadoc)
	 * @see MetadataLoader#reloadLookUpCache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadLookUpCache(java.lang.String)
	 */
	@Override
	public void reloadLookUpCache(String projectId) {
		
		List<W5LookUp> lookUps = (List<W5LookUp>) find("from W5LookUp t where t.projectUuid=?0 order by  t.lookUpId",
				projectId);
		List<W5LookUpDetay> lookUpDetays = (List<W5LookUpDetay>) find(
				"from W5LookUpDetay t where t.projectUuid=?0 order by t.lookUpId, t.tabOrder", projectId);
		
		FrameworkCache.addLookUps2Cache(projectId, lookUps, lookUpDetays);
	}


	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadMobileCache()
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadMobileCache()
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadMobileCache()
	 */
	@Override
	public void reloadMobileCache() { // TODO
		if (true)
			return;
		UserUtil.clearDevices();
		List<Object[]> l = (List<Object[]>) executeSQLQuery(
				"select r.mobile_device_id, r.user_id, r.customization_id, r.device_tip, r.last_login_dttm  from iwb.w5_mobile_device r where r.active_flag=1");
		if (l != null)
			for (Object[] o : l) {
				// UserUtil.addDevice(o[0].toString(), GenericUtil.uInt(o[1]),
				// GenericUtil.uInt(o[2]),
				// (short)GenericUtil.uInt(o[3]), ((Date)o[4]).getTime());
			}
	}

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadTablesCache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadTablesCache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadTablesCache(java.lang.String)
	 */
	@Override
	public void reloadTablesCache(String projectId) {
		List<W5Table> tables = (List<W5Table>) find("from W5Table t where t.projectUuid=?0 order by t.tableId", projectId);
		List<W5TableField> tableFields = (List<W5TableField>) find(
				"from W5TableField t where t.projectUuid=?0 AND t.tableFieldId>0 AND t.tabOrder>0 order by t.tableId, t.tabOrder",
				projectId);
		List<W5TableParam> tableParams = (List<W5TableParam>) find(
				"from W5TableParam t where t.projectUuid=?0 order by t.tableId, t.tabOrder", projectId);
		List<W5TableEvent> tableEvents = find(
				"from W5TableEvent r where r.projectUuid=?0 AND r.activeFlag=1 order by r.tableId, r.tabOrder, r.tableTriggerId",
				projectId);
		List<W5TableChild> tableChilds = (List<W5TableChild>) find(
				"from W5TableChild t where t.projectUuid=?0 order by t.tableId", projectId);

		FrameworkCache.addTables2Cache(projectId, tables, tableFields, tableParams, tableEvents, null, tableChilds);

	}

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadWorkflowCache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadWorkflowCache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadWorkflowCache(java.lang.String)
	 */
	@Override
	public void reloadWorkflowCache(String projectId) {
		if (!FrameworkSetting.workflow)
			return;
		// Approval ın bağlantılı olduğu w5table ların approval maplari
		// temizleniyor
		FrameworkCache.clearProjectWorkflows(projectId);

		// approval cache yenileniyor ve ilgili w5table lara ekleniyor
		List<W5Workflow> al = (List<W5Workflow>) find(
				"from W5Workflow t where t.activeFlag!=0 AND t.projectUuid=?0 order by t.tableId", projectId);
		for (W5Workflow ta : al) {
			FrameworkCache.addWorkflow(projectId, ta);
			W5Table t = FrameworkCache.getTable(projectId, ta.getTableId());
			if (t == null)
				continue;
			if (t.get_approvalMap() == null) {
				t.set_approvalMap(new HashMap<Short, W5Workflow>());
			}
			t.get_approvalMap().put(ta.getActionTip(), ta);
			ta.set_approvalStepList((List<W5WorkflowStep>) find(
					"from W5WorkflowStep t where t.projectUuid=?0 and t.workflowId=?1 order by approvalStepId",
					projectId, ta.getWorkflowId()));

			if (ta.getApprovalRequestTip() != 1) {// automatic degilse
				W5WorkflowStep approvedStep = new W5WorkflowStep();
				approvedStep.setApprovalStepId(901);
				approvedStep.setDsc(ta.getApprovalRequestMsg());// setDsc("onay_talep_edilecek");
				ta.get_approvalStepList().add(approvedStep);
			}

			W5WorkflowStep approvedStep = new W5WorkflowStep();
			approvedStep.setApprovalStepId(998);
			approvedStep.setDsc(ta.getApprovedMsg());// setDsc("onaylandi");
			ta.get_approvalStepList().add(approvedStep);

			if (true || ta.getOnRejectTip() == 1) {// make status rejected
				W5WorkflowStep rejectedStep = new W5WorkflowStep();
				rejectedStep.setApprovalStepId(999);
				rejectedStep.setDsc(ta.getRejectedMsg());// setDsc("reddedildi");
				ta.get_approvalStepList().add(rejectedStep);
			}
			ta.set_approvalStepMap(new HashMap());
			for (W5WorkflowStep step : ta.get_approvalStepList()) {
				if (step.getAccessViewTip() != 0) {
					t.set_hasApprovalViewControlFlag((short) 1);
				}
				ta.get_approvalStepMap().put(step.getApprovalStepId(), step);
			}
//			getCurrentSession().flush();
		}
	}

	

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadDeveloperEntityKeys()
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadDeveloperEntityKeys()
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadDeveloperEntityKeys()
	 */
	@Override
	public void reloadDeveloperEntityKeys() {
		Set<String> m = new HashSet();
		m.add("20.1"); // login form
		for (Object[] x : (List<Object[]>) executeSQLQuery(
				"select x.table_id, x.dsc, (select tp.expression_dsc from iwb.w5_table_param tp where tp.table_id=x.table_id AND x.project_uuid=tp.project_uuid AND tp.tab_order=1) tp_dsc from iwb.w5_table x where x.project_uuid='"+FrameworkSetting.devUuid+"' AND x.vcs_flag=1 AND x.table_id in (4,5,8,9,10,13,14,15,16,20,40,41,42,63,64,230,231,254,707,930,936,1345,3351,4658,1376)")) {
			List<Object> lo = executeSQLQuery("select t." + x[2] + " from " + x[1]
					+ " t where t.project_uuid='"+FrameworkSetting.devUuid+"'");
			if (lo != null)
				for (Object o : lo) {
					m.add(x[0] + "." + o);
				}
		}
		FrameworkCache.setDevEntityKeys(m);
	}

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadFrameworkCaches(int)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadFrameworkCaches(int)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadFrameworkCaches(int)
	 */
	@Override
	public void reloadFrameworkCaches(int customizationId) {
		// if (FrameworkSetting.redisCache && redisGlobalMap == null)redisGlobalMap =

		// Application Settings
		List<W5Project> lp = reloadProjectsCache(customizationId);

		// Locale Msgs
		reloadLocaleMsgsCache2(customizationId);

		// Application Settings
		reloadApplicationSettingsCache(customizationId);

		List<W5Customization> customizationList;
		if (customizationId == -1) {
			customizationList = (List<W5Customization>) find("from W5Customization t order by t.customizationId");
			reloadMobileCache();
			setApplicationSettingsValues();
			FrameworkCache.cachedOnlineQueryFields = (List<W5QueryField>) find(
					"from W5QueryField f where f.queryId=142 AND f.projectUuid='"+FrameworkSetting.devUuid+"' order by f.tabOrder");
			reloadDeveloperEntityKeys();
		} else
			customizationList = (List<W5Customization>) find("from W5Customization t where t.customizationId=?0",
					customizationId);

		for (W5Customization c : customizationList)
			FrameworkCache.wCustomizationMap.put(c.getCustomizationId(), c);

		dao.reloadUsersCache(customizationId);

		for (W5Project p : lp) {
			FrameworkCache.clearPreloadCache(p.getProjectUuid());
			reloadProjectCaches(p.getProjectUuid());
		}

		List<Object[]> ll = executeSQLQuery(
				"select x.related_project_uuid, string_agg(x.user_id::text,',') from iwb.w5_user_related_project x group by x.related_project_uuid");
		if (ll != null)
			for (Object[] oo : ll)
				UserUtil.addProjectUsers(oo[0].toString(), oo[1].toString());
		ll = executeSQLQuery(
				"select x.project_uuid, string_agg(x.user_id::text,',') from iwb.w5_user x group by x.project_uuid");
		if (ll != null)
			for (Object[] oo : ll)
				UserUtil.addProjectUsers(oo[0].toString(), oo[1].toString());
	}

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadProjectCaches(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadProjectCaches(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadProjectCaches(java.lang.String)
	 */
	@Override
	public void reloadProjectCaches(String projectId) {
		try {
			FrameworkSetting.projectSystemStatus.put(projectId, 2); // suspended
			logger.info("Caching project = " + projectId);

			//reloadRolesCache(projectId);

			// Job Schedule
			// if(cid==-1 || cid==0)reloadJobsCache();

			// Error Messages

			// Publish LookUps
			// if(cid==-1 || cid==0)reloadPublishLookUpsCache();

			// condition SQLs

			// int defaultCustomizationId = PromisCache.getAppSettingIntValue(0,
			// "default_customization_id");

			// Conversions
			// reloadConversionsCache(cid);

			// Table Params
			// if(cid==-1 ||
			// cid==0)reloadTableParamListChildListParentListCache();

			reloadLookUpCache(projectId);
			reloadTablesCache(projectId);
			if (FrameworkSetting.workflow)
				reloadWorkflowCache(projectId);
			// reloadTableAccessConditionSQLs(projectId);
			reloadWsServersCache(projectId);
			reloadWsClientsCache(projectId);
			reloadComponentCache(projectId);
			reloadJobsCache(projectId);
			if (FrameworkSetting.externalDb)
				reloadExternalDbsCache(projectId);
			if (FrameworkSetting.mq)
				reloadMqsCache(projectId);
			// FrameworkCache.getRedissonClient().getMap("icb-cache5");
			reloadErrorMessagesCache(projectId);
			W5Project po = FrameworkCache.getProject(projectId);
			if(po.getUiWebFrontendTip()==8) {//google AppMaker React
				reloadCacheLevel2(projectId);
			}


			FrameworkSetting.projectSystemStatus.put(projectId, 0); // working
			FrameworkCache.clearReloadCache(projectId);
			// if(FrameworkSetting.feedLoadAtStartupDepth>0 &&
			// cid==-1)reloadFeed(customizationId);

		} catch (Exception e) {
			logger.error("Error for project = " + projectId);
			e.printStackTrace();
		}

		logger.info("Cache Loaded.");
	}

	private void reloadCacheLevel2(String projectId) {
	
		List menuItems = dao.find("from W5ObjectMenuItem t where t.projectUuid=?0 order by t.objectType, t.objectId, t.tabOrder", projectId);
		List toolbarItems = dao.find("from W5ObjectToolbarItem t where t.projectUuid=?0 order by t.objectType, t.objectId, t.tabOrder", projectId);

		FrameworkCache.addFuncs2Cache(projectId, dao.find("from W5GlobalFunc t where t.projectUuid=?0 order by t.globalFuncId", projectId), 
				dao.find("from W5GlobalFuncParam t where t.projectUuid=?0 order by t.globalFuncId, t.tabOrder", projectId));
		
		FrameworkCache.addQueries2Cache(projectId, dao.find("from W5Query t where t.projectUuid=?0 or (t.queryId=2822 AND t.projectUuid=?1) order by t.queryId", projectId, FrameworkSetting.devUuid), 
				dao.find("from W5QueryField t where t.projectUuid=?0 or (t.queryId=2822 AND t.projectUuid=?1) order by t.queryId, t.tabOrder", projectId, FrameworkSetting.devUuid), 
				dao.find("from W5QueryParam t where t.projectUuid=?0 or (t.queryId=2822 AND t.projectUuid=?1) order by t.queryId, t.tabOrder", projectId, FrameworkSetting.devUuid));

		List formCells = dao.find("from W5FormCell t where t.activeFlag=1 and t.projectUuid=?0 order by t.formId, t.tabOrder", projectId);
		FrameworkCache.addForms2Cache(projectId, dao.find("from W5Form t where t.projectUuid=?0 order by t.formId", projectId), 
				formCells, 
				dao.find("from W5FormModule t where t.projectUuid=?0 order by t.formId, t.tabOrder", projectId), 
				dao.find("from W5FormCellProperty t where t.projectUuid=?0 order by t.formCellId, t.formCellPropertyId", projectId), 
				dao.find("from W5FormSmsMail t where t.activeFlag=1 and t.projectUuid=?0 order by t.formId, t.tabOrder", projectId), 
				dao.find("from W5FormHint t where t.activeFlag=1 and t.projectUuid=?0 order by t.formId, t.tabOrder", projectId), 
				toolbarItems);

		
		FrameworkCache.addGrids2Cache(projectId, dao.find("from W5Grid t where t.projectUuid=?0 order by t.gridId", projectId), 
				dao.find("from W5GridColumn t where t.projectUuid=?0 order by t.gridId, t.tabOrder", projectId), 
				dao.find("from W5CustomGridColumnCondition t where t.projectUuid=?0 order by t.gridId, t.tabOrder", projectId), 
				dao.find("from W5CustomGridColumnRenderer t where t.projectUuid=?0 order by t.gridId, t.customGridColumnRendererId", projectId), toolbarItems, menuItems, formCells);


		FrameworkCache.addMobileLists2Cache(projectId, dao.find("from M5List t where t.projectUuid=?0 order by t.listId", projectId));
		
		FrameworkCache.addCards2Cache(projectId, dao.find("from W5Card t where t.projectUuid=?0 order by t.cardId", projectId), toolbarItems, menuItems);
		

		FrameworkCache.addConversions2Cache(projectId, dao.find("from W5Conversion t where t.activeFlag=1 and t.projectUuid=?0 order by t.conversionId", projectId), 
				dao.find("from W5ConversionCol t where t.projectUuid=?0 order by t.conversionId, t.tabOrder", projectId));
		
		FrameworkCache.addPages2Cache(projectId, dao.find("from W5Page t where t.projectUuid=?0 order by t.pageId", projectId), 
				dao.find("from W5PageObject t where t.activeFlag=1 AND t.projectUuid=?0 order by t.pageId, t.tabOrder", projectId));

		
	}

	private void reloadComponentCache(String projectId) {
		Map<Integer, W5Component> wComponentMap = new HashMap<Integer, W5Component>();
		List<W5Component> l = find("from W5Component t where t.projectUuid=?0", projectId);
		for (W5Component c : l) {
			wComponentMap.put(c.getComponentId(), c);
		}
		FrameworkCache.setComponentMap(projectId, wComponentMap);
	}

	/* (non-Javadoc)
	 * @see iwb.dao.metadata.postgresql.MetadataLoader#reloadWsServersCache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadWsServersCache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadWsServersCache(java.lang.String)
	 */
	@Override
	public void reloadWsServersCache(String projectId) {
		List<W5WsServer> lt = (List<W5WsServer>) find("from W5WsServer t where t.projectUuid=?0", projectId);
		Map<String, W5WsServer> wssMap = new HashMap<String, W5WsServer>(lt.size() * 14 / 10);
		W5Project po = FrameworkCache.getProject(projectId);
		FrameworkCache.setWsServersMap(projectId, wssMap);
		for (W5WsServer o : lt) {
			wssMap.put(o.getWsUrl(), o);
			o.set_methods((List<W5WsServerMethod>) find(
					"from W5WsServerMethod t where t.projectUuid=?0 AND t.wsServerId=?1 order by t.tabOrder", projectId,
					o.getWsServerId()));
			o.get_methods().add(0, new W5WsServerMethod("login", (short) 4, 3));
			// o.get_methods().add(1,new W5WsServerMethod("logout", (short)4,
			// 5));
			for (W5WsServerMethod wsm : o.get_methods())
				if (wsm.getObjectType() == 19) { // QueryResult
					wsm.set_params((List<W5WsServerMethodParam>) find(
							"from W5WsServerMethodParam t where t.projectUuid=?0 AND t.wsServerMethodId=?1 order by t.tabOrder",
							projectId, wsm.getWsServerMethodId()));
					if (wsm.get_params().isEmpty())
						wsm.set_params(null);
					else if (po.getAuthenticationFuncId() != 0) {
						W5WsServerMethodParam tokenKey = new W5WsServerMethodParam(-998, "tokenKey", (short) 1);
						tokenKey.setOutFlag((short) 0);
						tokenKey.setNotNullFlag((short) 1);
						wsm.get_params().add(0, tokenKey);
					}
				}
		}
	}


	/* (non-Javadoc)
	 * @see MetadataLoader#reloadWsClientsCache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#reloadWsClientsCache(java.lang.String)
	 */
	@Override
	public void reloadWsClientsCache(String projectId) {
		// if(true)return;
		/*
		 * if (FrameworkSetting.redisCache) try { RMap<Integer, W5Ws> rwsMap =
		 * FrameworkCache.getRedissonClient() .getMap(String.format("icb-cache2:%s:ws",
		 * projectId)); if (rwsMap != null) {// rgridMap.getName() for (Integer key :
		 * rwsMap.keySet()) { W5Ws ws = rwsMap.get(key); wsMap.put(ws.getDsc(), ws); if
		 * (ws.get_methods() == null) ws.set_methods(new ArrayList()); else for
		 * (W5WsMethod wsm : ws.get_methods()) FrameworkCache.addWsMethod(projectId,
		 * wsm); } FrameworkCache.setWsClientsMap(projectId, wsMap); } } catch
		 * (Exception e) { throw new IWBException("framework", "Redis.Ws", 0, null,
		 * "Loading Ws from Redis", e); }
		 */
		List<W5Ws> wss = find("from W5Ws x where x.projectUuid=?0", projectId);
		List<W5WsMethod> wsMethods = find("from W5WsMethod x where x.activeFlag=1 AND x.projectUuid=?0 order by x.wsId, x.wsMethodId",
				projectId);
		List<W5WsMethodParam> wsMethodParams = find("from W5WsMethodParam t where t.projectUuid=?0 order by t.wsMethodId, t.parentWsMethodParamId, t.tabOrder",
				projectId);
		
		FrameworkCache.addWss2Cache(projectId, wss, wsMethods, wsMethodParams);

	}

	private void loadGlobalFunc(W5GlobalFuncResult gfr) {
		String projectId = FrameworkCache.getProjectId(gfr.getScd(), "20." + gfr.getGlobalFuncId());

		/*
		 * try { func = (W5GlobalFunc) (redisGlobalMap.get(projectId + ":func:" +
		 * gfr.getGlobalFuncId()));// rfuncMap.get(gfr.getGlobalFuncId()); } catch
		 * (Exception e) { throw new IWBException("framework", "Redis.GlobalFunc",
		 * gfr.getGlobalFuncId(), null, "Loading GlobalFunc from Redis", e); }
		 */
		W5GlobalFunc func = ((W5GlobalFunc) getMetadataObject("W5GlobalFunc","globalFuncId",
				gfr.getGlobalFuncId(), projectId, "GlobalFunc"));
		func.set_dbFuncParamList(
				find("from W5GlobalFuncParam t where t.projectUuid=?0 AND t.globalFuncId=?1 order by t.tabOrder",
						projectId, gfr.getGlobalFuncId()));

		gfr.setGlobalFunc(func);
	}


	/* (non-Javadoc)
	 * @see MetadataLoader#getGlobalFuncResult(java.util.Map, int)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getGlobalFuncResult(java.util.Map, int)
	 */
	@Override
	public W5GlobalFuncResult getGlobalFuncResult(Map<String, Object> scd, int globalFuncId) {
		String projectId = FrameworkCache.getProjectId(scd,
				globalFuncId < -1 ? ("40." + (-globalFuncId)) : ("20." + globalFuncId));
		if (globalFuncId < -1) {
			globalFuncId = (Integer) find(
					"select t.objectId from W5Form t where t.objectType in (3,4) AND t.projectUuid=?0 AND t.formId=?1",
					projectId, -globalFuncId).get(0);
		}

		W5GlobalFuncResult gfr = new W5GlobalFuncResult(globalFuncId);
		gfr.setScd(scd);
		gfr.setGlobalFunc(FrameworkCache.getGlobalFunc(projectId, globalFuncId));
		if (gfr.getGlobalFunc() == null) {
			loadGlobalFunc(gfr);
			FrameworkCache.addGlobalFunc(projectId, gfr.getGlobalFunc());
		}

		return gfr;
	}

	private void loadMList(M5ListResult mlr) {
		String projectId = FrameworkCache.getProjectId(mlr.getScd(), "1345." + mlr.getListId());

		M5List ml = null;
		if (FrameworkSetting.redisCache) {
			/*
			 * try { RMap<Integer, M5List> rmlistMap = FrameworkCache.getRedissonClient()
			 * .getMap(String.format("icb-cache2:%s:mlist", projectId)); if (rmlistMap !=
			 * null) {// rgridMap.getName() ml = rmlistMap.get(mlr.getListId()); } } catch
			 * (Exception e) { throw new IWBException("framework", "Redis.MobileList",
			 * mlr.getListId(), null, "Loading MobileList from Redis", e); }
			 */
		} else {
			ml = (M5List) getMetadataObject("M5List","listId", mlr.getListId(),
					projectId, "MobileList"); // ozel bir client icin varsa

			Integer searchFormId = findFormId4Object(
					10, mlr.getListId(), projectId);
			if (searchFormId != null)
				ml.set_searchFormId(searchFormId);
			ml.set_detailMLists(find("from M5List l where l.projectUuid=?0 AND l.parentListId=?1 order by l.listId",
					projectId, mlr.getListId()));
			for (M5List dl : ml.get_detailMLists()) {
				dl.set_toolbarItemList(find(
						"from W5ObjectToolbarItem t where t.objectType=1345 AND t.objectId=?0 AND t.projectUuid=?1 order by t.tabOrder",
						dl.getListId(), projectId));

				dl.set_menuItemList(find(
						"from W5ObjectMenuItem t where t.objectType=1345 AND t.objectId=?0 AND t.projectUuid=?1 order by t.tabOrder",
						dl.getListId(), projectId));

				W5Query q2 = getQueryResult(mlr.getScd(), dl.getQueryId()).getQuery();
				dl.set_query(q2);
			}
			if (!GenericUtil.isEmpty(ml.getOrderQueryFieldIds()))
				ml.set_orderQueryFieldNames(find(
						"select qf.dsc from W5QueryField qf where qf.queryId=?0 and qf.projectUuid=?1 AND qf.queryFieldId in ("
								+ ml.getOrderQueryFieldIds() + ") order by qf.tabOrder",
						ml.getQueryId(), projectId));
			ml.set_toolbarItemList(find(
					"from W5ObjectToolbarItem t where t.objectType=1345 AND t.objectId=?0 AND t.projectUuid=?1 order by t.tabOrder",
					mlr.getListId(), projectId));

			ml.set_menuItemList(find(
					"from W5ObjectMenuItem t where t.objectType=1345 AND t.objectId=?0 AND t.projectUuid=?1 order by t.tabOrder",
					mlr.getListId(), projectId));
		}
		W5Query q = getQueryResult(mlr.getScd(), ml.getQueryId()).getQuery();
		ml.set_query(q);

		if (q.getSourceObjectId() != 0)
			ml.set_mainTable(FrameworkCache.getTable(projectId, q.getSourceObjectId()));
		mlr.setList(ml);
	}


	/* (non-Javadoc)
	 * @see MetadataLoader#getMListResult(java.util.Map, int, java.util.Map, boolean)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getMListResult(java.util.Map, int, java.util.Map, boolean)
	 */
	@Override
	public M5ListResult getMListResult(Map<String, Object> scd, int listId, Map<String, String> requestParams,
			boolean noSearchForm) {
		String projectId = FrameworkCache.getProjectId(scd, "1345." + listId);
		M5ListResult mlr = new M5ListResult(listId);
		mlr.setRequestParams(requestParams);
		mlr.setScd(scd);
		M5List ml = FrameworkCache.getMListView(projectId, listId);
		if (ml != null) {
			mlr.setList(ml);
		} else {
			loadMList(mlr);
			ml = mlr.getList();
			FrameworkCache.addMListView(scd, ml);
		}

		// search Form
		if (!noSearchForm && ml.get_searchFormId() != 0) {
			W5FormResult searchForm = getFormResult(scd, ml.get_searchFormId(), 10, requestParams);
			dao.initializeForm(searchForm, false);
			dao.loadFormCellLookups(mlr.getScd(), searchForm.getFormCellResults(), mlr.getRequestParams(), null);
			mlr.setSearchFormResult(searchForm);
		}
		return mlr;
	}


	/* (non-Javadoc)
	 * @see MetadataLoader#getListViewResult(java.util.Map, int, java.util.Map, boolean)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getListViewResult(java.util.Map, int, java.util.Map, boolean)
	 */
	@Override
	public W5ListViewResult getListViewResult(Map<String, Object> scd, int listViewId,
			Map<String, String> requestParams, boolean noSearchForm) {

		W5ListViewResult listViewResult = new W5ListViewResult(listViewId);
		String projectId = FrameworkCache.getProjectId(scd, "936." + listViewId);
		listViewResult.setRequestParams(requestParams);
		listViewResult.setScd(scd);

		W5List d = FrameworkCache.getListView(projectId, listViewId);
		if (d != null) {
			listViewResult.setListView(d);
		} else {
			loadListView(listViewResult);
			d = listViewResult.getListView();
			FrameworkCache.addListView(projectId, d);
		}
		// search Form
		if (!noSearchForm && d.get_searchFormId() != 0) {
			W5FormResult searchForm = getFormResult(scd, d.get_searchFormId(), 72, requestParams);
			dao.initializeForm(searchForm, false);
			dao.loadFormCellLookups(listViewResult.getScd(), searchForm.getFormCellResults(),
					listViewResult.getRequestParams(), null);
			listViewResult.setSearchFormResult(searchForm);
		}
		return listViewResult;
	}

	private void loadListView(W5ListViewResult lr) {
		String projectId = FrameworkCache.getProjectId(lr.getScd(), "936." + lr.getListId());
		W5List d = (W5List) getMetadataObject("W5List","listId", lr.getListId(),
				projectId, "List"); // ozel bir client icin varsa
		lr.setListView(d);

		W5Query query = getQueryResult(lr.getScd(), d.getQueryId()).getQuery();

		if (query == null) {
			query = new W5Query();
			List<W5QueryField> queryFields = find(
					"from W5QueryField t where t.queryId=?0 and t.tabOrder>0 AND t.projectUuid=?1 order by t.tabOrder",
					d.getQueryId(), projectId);
			d.set_query(query);
			query.set_queryFields(queryFields); // dataReader icin gerekli
			query.setSourceObjectId(
					(Integer) find("select t.sourceObjectId from W5Query t where t.queryId=?0 and t.projectUuid=?1",
							d.getQueryId(), projectId).get(0));
		} else
			d.set_query(query);

		d.set_mainTable(FrameworkCache.getTable(projectId, query.getSourceObjectId()));
		d.set_listColumnList(find("from W5ListColumn t where t.projectUuid=?0 AND t.listId=?1 order by t.tabOrder",
				projectId, lr.getListId()));

		Map<Integer, W5QueryField> fieldMap = new HashMap<Integer, W5QueryField>();
		Map<String, W5QueryField> fieldMapDsc = new HashMap<String, W5QueryField>();
		for (W5QueryField field : query.get_queryFields()) {
			fieldMap.put(field.getQueryFieldId(), field);
			fieldMapDsc.put(field.getDsc(), field);
		}

		d.set_queryFieldMap(fieldMap);

		d.set_queryFieldMapDsc(fieldMapDsc);
		d.set_pkQueryField(fieldMap.get(d.getPkQueryFieldId()));
		int totalWidth = 0;
		for (W5ListColumn lc : d.get_listColumnList()) {
			lc.set_queryField(fieldMap.get(lc.getQueryFieldId()));
			totalWidth += lc.getWidth();
		}
		d.set_totalWidth(totalWidth);

		List<Integer> formIdz = find(
				"select t.formId from W5Form t where t.projectUuid=?0 AND t.objectType=7 and t.objectId=?1", projectId,
				lr.getListId());
		if (formIdz.size() > 0 && formIdz.get(0) != null)
			d.set_searchFormId(formIdz.get(0));

		d.set_toolbarItemList(find(
				"from W5ObjectToolbarItem t where t.objectType=8 AND t.objectId=?0 AND t.projectUuid=?1 order by t.tabOrder",
				lr.getListId(), projectId));
		for (W5ObjectToolbarItem c : d.get_toolbarItemList())
			switch (c.getControlType()) { // TODO:toolbar icine bisey konulacaksa
			case 10:
			case 7:
			case 15:
			case 9:
				break;
			case 14:
			case 8:
			case 6:
				break;
			}
	}



	/* (non-Javadoc)
	 * @see MetadataLoader#addProject2Cache(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#addProject2Cache(java.lang.String)
	 */
	@Override
	public void addProject2Cache(String projectId) {
		List l = find("from W5Project t where t.projectUuid=?0", projectId);
		if (l.isEmpty())
			throw new IWBException("framework", "Not Valid Project", 0, projectId, "Not Valid Project", null);
		W5Project p = (W5Project) l.get(0);
		List ll = executeSQLQuery(
				"select min(t.user_tip) from iwb.w5_user_tip t where t.user_tip!=122 AND t.active_flag=1 AND t.project_uuid=?",
				projectId);
		if (!GenericUtil.isEmpty(ll))
			p.set_defaultRoleGroupId(GenericUtil.uInt(ll.get(0)));
		FrameworkCache.addProject(p);
	}
	
	/* (non-Javadoc)
	 * @see MetadataLoader#getMetadataObject(java.lang.String, java.lang.String, int, java.lang.Object, java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#getMetadataObject(java.lang.String, java.lang.String, int, java.lang.Object, java.lang.String)
	 */
	@Override
	public Object getMetadataObject(String objectName, String pkFieldName, int objectId, Object projectId, String onErrorMsg) {
		StringBuilder hql = new StringBuilder();
		hql.append("from ").append(objectName).append(" t where t.").append(pkFieldName).append("=?0 AND t.projectUuid=?1");
		List list = find(hql.toString(), objectId, projectId);
		if (list.size() == 0) {
			if (onErrorMsg == null)
				return null;
			throw new IWBException("framework", onErrorMsg, objectId, null, "Wrong " + onErrorMsg + " ID: " + objectId, null);
		} else
			return list.get(0);
	}
	
	/* (non-Javadoc)
	 * @see MetadataLoader#loadProject(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#loadProject(java.lang.String)
	 */
	@Override
	public W5Project loadProject(String newProjectId) {
		List<W5Project> l = find("from W5Project t where t.projectUuid=?0", newProjectId);
		return l.isEmpty() ? null : l.get(0);
	}
	
	/* (non-Javadoc)
	 * @see MetadataLoader#findObjectMailSetting(java.util.Map, int)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#findObjectMailSetting(java.util.Map, int)
	 */
	@Override
	public W5ObjectMailSetting findObjectMailSetting(Map<String, Object> scd, int mailSettingId) {
		int ms = mailSettingId != 0 ? mailSettingId
				: GenericUtil.uInt(scd.get("mailSettingId"));
		if (ms == 0)
			ms = 1;
		int cusId = ms != 1 ? (Integer) scd.get("customizationId") : 0;
		if(mailSettingId==0) mailSettingId = GenericUtil.uInt(scd.get("mailSettingId"));
		List<W5ObjectMailSetting> oms = find(
				"from W5ObjectMailSetting w where w.customizationId=?0 order by w.mailSettingId",
				cusId);
		W5ObjectMailSetting r = null; 
		if(!oms.isEmpty()) {
			if(mailSettingId!=0)for(W5ObjectMailSetting o:oms)if(o.getMailSettingId()==mailSettingId) {
				r = o;
				break;
			}
			if(r==null)r = oms.get(0);
			r.setOutboxServerPassWord(EncryptionUtil.decryptAES(r.getOutboxServerPassWord()));
			return r;
		}
		
		if(ms != 1) {
			throw new IWBException("framework", "MailSetting", mailSettingId, null, "Wrong MailSetting ID: " + mailSettingId, null);
		}
		oms = find("from W5ObjectMailSetting w where w.mailSettingId=1 AND w.customizationId=0");
		if(oms.isEmpty()) {
			throw new IWBException("framework", "MailSetting", 1, null, "Wrong MailSetting ID: " + 1, null);
		}
		
		r = oms.get(0);
		r.setOutboxServerPassWord(EncryptionUtil.decryptAES(r.getOutboxServerPassWord()));
		return r;
	}
	
	
	
	/* (non-Javadoc)
	 * @see MetadataLoader#findLookUpDetay(int, java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#findLookUpDetay(int, java.lang.String)
	 */
	@Override
	public List<W5LookUpDetay> findLookUpDetay(int lookupQueryId, String projectUuid){
		return (List<W5LookUpDetay>) dao.find(
				"from W5LookUpDetay t where t.projectUuid=?0  AND t.lookUpId=?1 order by t.tabOrder", projectUuid,
				lookupQueryId);
	}


	

	/* (non-Javadoc)
	 * @see MetadataLoader#findTableCalcFieldByName(java.lang.String, int, java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#findTableCalcFieldByName(java.lang.String, int, java.lang.String)
	 */
	@Override
	public List<W5TableFieldCalculated> findTableCalcFieldByName(String projectUuid, int tableId, String fieldName){
		return find("from W5TableFieldCalculated t where t.projectUuid=?0 AND t.tableId=?1 AND t.dsc=?2",
				projectUuid, tableId, fieldName);
	}

	/* (non-Javadoc)
	 * @see MetadataLoader#findTableCalcFields(java.lang.String, int)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#findTableCalcFields(java.lang.String, int)
	 */
	@Override
	public List<W5TableFieldCalculated> findTableCalcFields(String projectUuid, int tableId){
		return find("from W5TableFieldCalculated t where t.projectUuid=?0 AND t.tableId=?1 order by t.tabOrder",
				projectUuid, tableId);
	}

	/* (non-Javadoc)
	 * @see MetadataLoader#findFirstCRUDForm4Table(int, java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see MetadataLoader#findFirstCRUDForm4Table(int, java.lang.String)
	 */
	@Override
	public int findFirstCRUDForm4Table(int tableId, String projectUuid) {
		List<Integer> ll = find(
			"select min(f.formId) from W5Form f where f.projectIuid=?0 AND f.objectType=2 AND f.objectId=?1",
			projectUuid, tableId);
		return ll.isEmpty() ? 0 : ll.get(0);
	}
	
	public boolean changeActiveProject(Map<String, Object> scd, String projectUuid) {
		List<Object> params = new ArrayList();
		params.add(projectUuid);
		params.add(scd.get(scd.containsKey("ocustomizationId") ? "ocustomizationId" : "customizationId"));
		params.add(scd.get("userId"));
		List list = dao.executeSQLQuery2Map(
				"select x.customization_id,(select 1 from iwb.w5_query q where q.query_id=session_query_id AND x.project_uuid=q.project_uuid) rbac from iwb.w5_project x where x.project_uuid=? AND (x.customization_id=? OR exists(select 1 from iwb.w5_user_related_project ur where ur.user_id=? AND x.project_uuid=ur.related_project_uuid))",
				params);
		if (GenericUtil.isEmpty(list))
			return false;
		Map p = (Map) list.get(0);
		int newCustomizationId = GenericUtil.uInt(p.get("customization_id"));

		if (newCustomizationId != (Integer) scd.get("customizationId")) { //
			if (!scd.containsKey("ocustomizationId"))
				scd.put("ocustomizationId", (Integer) scd.get("customizationId"));
			scd.put("customizationId", newCustomizationId);
		}
		W5Project po = FrameworkCache.getProject(projectUuid);
		scd.put("projectId", projectUuid);
		scd.put("rbac", newCustomizationId > 0 ? GenericUtil.uInt(p.get("rbac")) : 1);
		scd.put("_renderer2", GenericUtil.getRenderer(po.getUiWebFrontendTip()));
		return true;
	}

	public int getGlobalNextval(String id) {
		List l = executeSQLQuery("select nextval(?::text)", id);

		return GenericUtil.uInt(l.get(0));
	}
	
	public Map<String, Object> getProjectMetadata(String projectId){
		List<W5Project> lpo = dao.find("from W5Project t where t.projectUuid=?0", projectId);
		if(lpo.isEmpty()) 
			throw new IWBException("framework", "Project", 0, null,
					"Project not Found: " + projectId, null);
		W5Project po = (W5Project)lpo.get(0);
		Map<String, Object> m = new HashMap();
		m.put("project", po);
		m.put("roleGroups", dao.find("from W5RoleGroup t where t.projectUuid=?0 order by t.roleGroupId", projectId));
		
		
		List<Object[]> appSettingList = (List<Object[]>) executeSQLQuery("select dsc, val from iwb.w5_app_setting where customization_id=?", po.getCustomizationId());
		if(appSettingList!=null) {
			Map mm = new HashMap();
			for(Object[] oo:appSettingList)
				mm.put(oo[0], oo[1]);
			m.put("appSettings", mm);
		}

		if(po.getLocaleMsgKeyFlag()!=0) for(String locale:po.getLocales().split(",")){
			List<Object[]> localeMsgs = (List<Object[]>) executeSQLQuery("select locale_msg_key, dsc from iwb.w5_locale_msg where customization_id=? AND locale=?", po.getCustomizationId(), locale);
			if(localeMsgs!=null) {
				Map mm = new HashMap();
				for(Object[] oo:localeMsgs)
					mm.put(oo[0], oo[1]);
				m.put("localeMsgs_"+locale, mm);
			}
		}
		
		if(projectId.equals(FrameworkSetting.devUuid)) { //just tables, funcs and sequences
			StringBuilder ddl = new StringBuilder();
			for(Object o:dao.executeSQLQuery("select * from iwb.generate_create_table_statement('"+po.getRdbmsSchema()+"',null) ")) {
				ddl.append(o).append("\n");
			}
			List lm = dao.find("select max(t.vcsCommitId) from W5VcsCommit t where t.projectUuid=?0", projectId);
			W5VcsCommit commit = new W5VcsCommit();
			commit.setVcsCommitId((Integer)lm.get(0));
			commit.setExtraSql(ddl.toString());
			commit.setComment("initial DDL");
			List vcsCommits = new ArrayList();
			vcsCommits.add(commit);
			m.put("vcsCommits", vcsCommits);
		} else {
			m.put("vcsCommits", dao.find("from W5VcsCommit t where t.projectUuid=?0 AND t.vcsCommitId>0 AND length(t.extraSql)>2 order by t.vcsCommitId", projectId));
			m.put("vcsCommits2", dao.find("from W5VcsCommit t where t.projectUuid=?0 AND t.vcsCommitId<0 AND t.runLocalFlag>0 AND length(t.extraSql)>2 order by -t.vcsCommitId", projectId));
		}

		
		m.put("lookUps", dao.find("from W5LookUp t where t.projectUuid=?0 order by t.lookUpId", projectId));
		m.put("lookUpDetays", dao.find("from W5LookUpDetay t where t.projectUuid=?0 order by t.lookUpId, t.tabOrder", projectId));
		
		m.put("tables", dao.find("from W5Table t where t.projectUuid=?0 order by t.tableId", projectId));
		m.put("tableFields", dao.find("from W5TableField t where t.projectUuid=?0 order by t.tableId, t.tabOrder", projectId));
		m.put("tableParams", dao.find("from W5TableParam t where t.projectUuid=?0 order by t.tableId, t.tabOrder", projectId));
		m.put("tableEvents", dao.find("from W5TableEvent t where t.activeFlag=1 and t.projectUuid=?0 order by t.tableId, t.tabOrder", projectId));
		m.put("tableFieldCalculateds", dao.find("from W5TableFieldCalculated t where t.projectUuid=?0 order by t.tableId, t.tabOrder", projectId));
		m.put("tableChilds", dao.find("from W5TableChild t where t.projectUuid=?0 order by t.tableId, t.tableChildId", projectId));
		
		m.put("wss", dao.find("from W5Ws t where t.activeFlag=1 and t.projectUuid=?0 order by t.wsId", projectId));
		m.put("wsMethods", dao.find("from W5WsMethod t where t.activeFlag=1 and t.projectUuid=?0 order by t.wsId", projectId));
		m.put("wsMethodParams", dao.find("from W5WsMethodParam t where t.projectUuid=?0 order by t.wsMethodId, t.parentWsMethodParamId, t.tabOrder", projectId));
		
		m.put("funcs", dao.find("from W5GlobalFunc t where t.projectUuid=?0 order by t.globalFuncId", projectId));
		m.put("funcParams", dao.find("from W5GlobalFuncParam t where t.projectUuid=?0 order by t.globalFuncId, t.tabOrder", projectId));

		m.put("queries", dao.find("from W5Query t where t.projectUuid=?0 or (t.queryId=2822 AND t.projectUuid=?1) order by t.queryId", projectId, FrameworkSetting.devUuid));
		m.put("queryFields", dao.find("from W5QueryField t where t.tabOrder>0 AND t.projectUuid=?0 or (t.queryId=2822 AND t.projectUuid=?1) order by t.queryId, t.tabOrder", projectId, FrameworkSetting.devUuid));
		m.put("queryParams", dao.find("from W5QueryParam t where t.projectUuid=?0 or (t.queryId=2822 AND t.projectUuid=?1) order by t.queryId, t.tabOrder", projectId, FrameworkSetting.devUuid));
		
		m.put("forms", dao.find("from W5Form t where t.projectUuid=?0 order by t.formId", projectId));
		m.put("formCells", dao.find("from W5FormCell t where t.activeFlag=1 and t.projectUuid=?0 order by t.formId, t.tabOrder, t.xOrder", projectId));
		m.put("formModules", dao.find("from W5FormModule t where t.projectUuid=?0 order by t.formId, t.tabOrder", projectId));
		m.put("formCellProperties", dao.find("from W5FormCellProperty t where t.projectUuid=?0 order by t.formCellId, t.formCellPropertyId", projectId));
		m.put("formSmsMails", dao.find("from W5FormSmsMail t where t.activeFlag=1 and t.projectUuid=?0 order by t.formId, t.tabOrder", projectId));
		m.put("formHints", dao.find("from W5FormHint t where t.projectUuid=?0 order by t.formId, t.tabOrder", projectId));
		
		m.put("grids", dao.find("from W5Grid t where t.projectUuid=?0 order by t.gridId", projectId));
		m.put("gridColumns", dao.find("from W5GridColumn t where t.projectUuid=?0 order by t.gridId, t.tabOrder", projectId));
		m.put("gridColumnCustomConditions", dao.find("from W5CustomGridColumnCondition t where t.projectUuid=?0 order by t.gridId, t.tabOrder", projectId));
		m.put("gridColumnCustomRenderers", dao.find("from W5CustomGridColumnRenderer t where t.projectUuid=?0 order by t.gridId, t.customGridColumnRendererId", projectId));

		m.put("mobileLists", dao.find("from M5List t where t.projectUuid=?0 order by t.listId", projectId));
		
		m.put("cards", dao.find("from W5Card t where t.projectUuid=?0 order by t.cardId", projectId));
		
		m.put("menuItems", dao.find("from W5ObjectMenuItem t where t.projectUuid=?0 order by t.objectType, t.objectId, t.tabOrder", projectId));
		m.put("toolbarItems", dao.find("from W5ObjectToolbarItem t where t.projectUuid=?0 order by t.objectType, t.objectId, t.tabOrder", projectId));

		m.put("workflows", dao.find("from W5Workflow t where t.activeFlag=1 and t.projectUuid=?0 order by t.workflowId", projectId));
		m.put("workflowSteps", dao.find("from W5WorkflowStep t where t.projectUuid=?0 order by t.workflowId, t.approvalStepId", projectId));

		m.put("conversions", dao.find("from W5Conversion t where t.activeFlag=1 and t.projectUuid=?0 order by t.conversionId", projectId));
		m.put("conversionCols", dao.find("from W5ConversionCol t where t.projectUuid=?0 order by t.conversionId, t.tabOrder", projectId));
		
		m.put("pages", dao.find("from W5Page t where t.projectUuid=?0 order by t.pageId", projectId));
		m.put("pageObjects", dao.find("from W5PageObject t where t.activeFlag=1 AND t.projectUuid=?0 order by t.pageId, t.tabOrder", projectId));

		m.put("menus", dao.find("from W5Menu t where t.projectUuid=?0 order by t.roleGroupId, t.parentMenuId, t.tabOrder", projectId));
		m.put("mmenus", dao.find("from M5Menu t where t.projectUuid=?0 order by t.roleGroupId, t.parentMenuId, t.tabOrder", projectId));

		m.put("externalDbs", dao.find("from W5ExternalDb t where t.activeFlag=1 and t.projectUuid=?0", projectId));
		m.put("exceptions", dao.find("from W5Exception t where t.projectUuid=?0 order by t.tabOrder", projectId));
		

		return m;

	}

}
