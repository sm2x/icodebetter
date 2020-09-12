package iwb.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.exception.IWBException;
import iwb.model.db.W5BIGraphDashboard;
import iwb.model.db.W5Conversion;
import iwb.model.db.W5ConvertedObject;
import iwb.model.db.W5Detay;
import iwb.model.db.W5Email;
import iwb.model.db.W5Form;
import iwb.model.db.W5FormCell;
import iwb.model.db.W5FormModule;
import iwb.model.db.W5FormSmsMail;
import iwb.model.db.W5FormSmsMailAlarm;
import iwb.model.db.W5Grid;
import iwb.model.db.W5GridColumn;
import iwb.model.db.W5LookUp;
import iwb.model.db.W5LookUpDetay;
import iwb.model.db.W5PageObject;
import iwb.model.db.W5Table;
import iwb.model.db.W5TableChild;
import iwb.model.db.W5TableField;
import iwb.model.db.W5TableParam;
import iwb.model.db.W5WorkflowStep;
import iwb.model.db.W5WsMethod;
import iwb.model.helper.W5FormCellHelper;
import iwb.model.result.M5ListResult;
import iwb.model.result.W5CardResult;
import iwb.model.result.W5FormResult;
import iwb.model.result.W5GridResult;
import iwb.model.result.W5ListViewResult;
import iwb.model.result.W5PageResult;
import iwb.model.result.W5QueryResult;
import iwb.util.DBUtil;
import iwb.util.GenericUtil;
import iwb.util.UserUtil;

@Component
public class UIEngine {
	@Lazy
	@Autowired
	private PostgreSQL dao;

	@Lazy
	@Autowired
	private MetadataLoader metadataLoader;
	
	@Lazy
	@Autowired
	private QueryEngine queryEngine;

	

	@Lazy
	@Autowired
	private CRUDEngine crudEngine;
	
	
	@Lazy
	@Autowired 
	private AccessControlEngine acEngine;
	
	

	@Lazy
	@Autowired
	private GlobalScriptEngine scriptEngine;
	
	
	@Lazy
	@Autowired
	private ConversionEngine conversionEngine;

	
	@Lazy
	@Autowired
	private RESTEngine restEngine;

	public W5FormResult getFormResultByQuery(Map<String, Object> scd, int formId, int queryId,
			Map<String, String> requestParams) {
		W5FormResult formResult = metadataLoader.getFormResult(scd, formId, 1, requestParams);
		// formResult.getForm().get_formCells().clear();
		if (formId != 1622)
			formResult.getForm().get_moduleList().clear(); // TODO: neden
															// yapilmis???
		formResult.setUniqueId(GenericUtil.getNextId("xfi"));

		W5QueryResult queryResult = queryEngine.executeQuery(scd, queryId, requestParams);
		formResult.setFormCellResults(new ArrayList<W5FormCellHelper>(queryResult.getData().size()));

		short tabOrder = 1;
		for (Object[] d : queryResult.getData()) {
			W5FormCellHelper result = GenericUtil.getFormCellResultByQueryRecord(d);
			if (result.getFormCell().getTabOrder() == 0)
				result.getFormCell().setTabOrder(tabOrder);
			result.getFormCell().setFormCellId(-tabOrder);
			tabOrder++;
			formResult.getFormCellResults().add(result);
		}
		if (queryResult.getQuery().get_queryFields().get(7).getPostProcessType() == 10) { // tipi
																							// lookup
																							// ise
																							// o
																							// zaman
																							// modulleri
																							// buraya
																							// koy
			for (W5LookUpDetay d : FrameworkCache
					.getLookUp(scd, queryResult.getQuery().get_queryFields().get(7).getLookupQueryId())
					.get_detayList()) {
				W5FormModule m = new W5FormModule();
				m.setFormModuleId(GenericUtil.uInt(d.getVal()));
				m.setLocaleMsgKey(d.getDsc());
				formResult.getForm().get_moduleList().add(m);
			}
		}
		dao.loadFormCellLookups(scd, formResult.getFormCellResults(), requestParams, null);
		return formResult;
	}
	

	public W5FormResult getFormResult2(Map<String, Object> scd, int formId) {
		W5FormResult formResult = null;
		try {
			Map requestParams = new HashMap();
			formResult = metadataLoader.getFormResult(scd, formId, 2, requestParams);
			formResult.setUniqueId(GenericUtil.getNextId("fi"));
			W5Form f = formResult.getForm();
			if (f.get_moduleList() != null) {
				for (W5FormModule m : f.get_moduleList())switch(m.getModuleType()){
//					case	3://form
					case	4://multi form
						W5FormResult dfr = metadataLoader.getFormResult(scd, m.getObjectId(), 2, requestParams);
						if(dfr!=null) {
							if(formResult.getModuleFormMap()==null)formResult.setModuleFormMap(new HashMap());
							formResult.getModuleFormMap().put(m.getObjectId(), dfr);
						}
					break;

				}
			}
			for(W5FormCell fc:f.get_formCells())if(fc.getActiveFlag()!=0 && fc.getControlType()==97) {
				W5FormResult dfr = metadataLoader.getFormResult(scd, fc.getLookupQueryId(), 2, requestParams);
				if(dfr!=null) {
					if(formResult.getModuleFormMap()==null)formResult.setModuleFormMap(new HashMap());
					formResult.getModuleFormMap().put(-fc.getLookupQueryId(), dfr);
				}

			}
			
			return formResult;
		} catch (Exception e) {
			throw new IWBException("framework", "Form", formId, null, "[40," + formId + "]"
					+ (formResult != null && formResult.getForm() != null ? " " + formResult.getForm().getDsc() : ""),
					e);
		}
	}
	

	public W5FormResult getFormResult(Map<String, Object> scd, int formId, int action,
			Map<String, String> requestParams) {
		
		if(formId==0) {
			W5Table t = FrameworkCache.getTable(scd, GenericUtil.uInt(requestParams.get("_tb_id")));
			if (t == null) {
				throw new IWBException("framework", "Table", GenericUtil.uInt(requestParams.get("_tb_id")), null,
						LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_such_table"), null);
			}
			formId = t.getDefaultUpdateFormId();
			if (formId == 0) {
				formId = metadataLoader.findFirstCRUDForm4Table(t.getTableId(), t.getProjectUuid());
				if (formId == 0) {
					throw new IWBException("framework", "Table", t.getTableId(), null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_no_form_for_table"), null);
				}
			}
		//formId = GenericUtil.uInt(requestParams, "_fid");
		}


		W5FormResult formResult = null;
		try {
			formResult = metadataLoader.getFormResult(scd, formId, action, requestParams);
			dao.checkTenant(formResult.getScd());
			formResult.setUniqueId(GenericUtil.getNextId("fi"));
			if(GenericUtil.uInt(requestParams, "_viewMode")!=0)
				formResult.setViewMode(true);
			W5Form f = formResult.getForm();
			
			String projectId = FrameworkCache.getProjectId(scd, "40." + formId);
			W5Table t = null;
			switch (f.getObjectType()) {
			case 2: // table
				t = FrameworkCache.getTable(projectId, f.getObjectId()); // form.get_sourceTable();
				if(action<=1 && GenericUtil.uInt(requestParams.get(t.get_tableParamList().get(0).getDsc()))==0 && 
						(GenericUtil.uInt(requestParams.get("_tb_pk")) != 0 || GenericUtil.uInt(requestParams.get("_id")) != 0)) { // _tb_id, and _tb_pk can also be used
					int oo = GenericUtil.uInt(requestParams.get("_tb_pk"));if(oo==0)oo= GenericUtil.uInt(requestParams.get("_id"));
					requestParams.put(t.get_tableParamList().get(0).getDsc(), ""+oo);
					action = 1;
					requestParams.put("a", "1");
				}
				
				switch (t.getAccessViewTip()) {
				case 1:
					if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
							t.getAccessViewRoles(), t.getAccessViewUsers())) {
						throw new IWBException("security", "Form", formId, null, LocaleMsgCache.get2(0,
								(String) scd.get("locale"), "fw_security_table_control_view"), null);
					}
				}

				acEngine.accessControl4FormTable(formResult, null);
				if (f.get_moduleList() != null) {
					for (W5FormModule m : f.get_moduleList()) if(m.getModuleViewType() == 0 || m.getModuleViewType() == action)
						switch(m.getModuleType()){
						case	3://form
						case	4://multi form
							int newAction = m.getModuleType() == 3 ? 2 : GenericUtil.uInt(requestParams.get("a" + m.getTabOrder()));
							if (newAction == 0)
								newAction = action;
							if (formResult.getModuleFormMap() == null)
								formResult.setModuleFormMap(new HashMap());
							W5FormResult dfr = getFormResult(scd, m.getObjectId(), newAction, new HashMap());
							formResult.getModuleFormMap().put(m.getObjectId(), dfr);
							if(action==1 && m.getModuleType() == 3 && !GenericUtil.isEmpty(t.get_tableChildList())) {//TODO: load detail records for form
								for(W5TableChild tc:t.get_tableChildList()) if(tc.getRelatedTableId() == dfr.getForm().getObjectId()){
									W5Table dt = FrameworkCache.getTable(scd, tc.getRelatedTableId());
									StringBuilder sql = new StringBuilder();
									String pkFieldName = dt.get_tableFieldList().get(0).getDsc();
									sql.append("select x.").append(pkFieldName);
									for(W5FormCell fc2:dfr.getForm().get_formCells()) if(fc2.getActiveFlag()!=0 && fc2.getControlType()>0 && fc2.getControlType()<90 && fc2.get_sourceObjectDetail()!=null){
										W5TableField tf2 = (W5TableField)fc2.get_sourceObjectDetail();
										if(tf2.getTabOrder()!=1)sql.append(", x.").append(tf2.getDsc());
										if(fc2.getControlType()==71) {//file
											if(FrameworkCache.getTable(scd, FrameworkSetting.customFileTableId)==null)
												sql.append(", (select a.original_file_name from iwb.w5_file_attachment a where a.project_uuid='").append(scd.get("projectId")).append("' AND a.file_attachment_id=x.").append(tf2.getDsc()).append(") ").append(tf2.getDsc()).append("_qw_");
											else 
												sql.append(", (select a.dsc from x_file a where a.file_id=x.").append(tf2.getDsc()).append(") ").append(tf2.getDsc()).append("_qw_");
											
										}
									}
									sql.append(" from ").append(dt.getDsc()).append(" x where x.");
									sql.append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc()).append("=?");
									if(tc.getRelatedStaticTableFieldId()!=0 && tc.getRelatedStaticTableFieldVal()!=0)
										sql.append(" AND ").append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal());
									List params = new ArrayList();
									params.add(GenericUtil.uInt(requestParams.get(t.get_tableParamList().get(0).getDsc())));
									if(dt.get_tableParamList().size()>0)for(W5TableParam tp2:dt.get_tableParamList())if(tp2.getSourceType()==2) {
										sql.append(" AND x.").append(tp2.getExpressionDsc()).append("=?");
										params.add(scd.get(tp2.getDsc()));
									}
									sql.append(" order by 1");

									List<Map> list = dao.executeSQLQuery2Map(sql.toString(), params);
									if(!GenericUtil.isEmpty(list)) {
										for(Map m2:list) {//converting PK to specialField 
											m2.put("_id_"+pkFieldName, m2.get(pkFieldName));
											m2.put("_id_", m2.get(pkFieldName));
											m2.remove(pkFieldName);
										}
										if(dfr.getOutputFields()==null)dfr.setOutputFields(new HashMap());
										dfr.getOutputFields().put("list", list);
									}

								}
									
							}

						break;

					}
					for (W5FormCell fc : f.get_formCells()) if(fc.getControlType()==97){//detail-form-multi

							if (formResult.getModuleFormMap() == null)
								formResult.setModuleFormMap(new HashMap());
							W5FormResult dfr = getFormResult(scd, fc.getLookupQueryId(), 2, new HashMap());
							formResult.getModuleFormMap().put(-fc.getLookupQueryId(), dfr);
							if(action==1 && !GenericUtil.isEmpty(t.get_tableChildList())) {//TODO: load detail records for form
								for(W5TableChild tc:t.get_tableChildList()) if(tc.getRelatedTableId() == dfr.getForm().getObjectId()){
									W5Table dt = FrameworkCache.getTable(scd, tc.getRelatedTableId());
									StringBuilder sql = new StringBuilder();
									String pkFieldName = dt.get_tableFieldList().get(0).getDsc();
									Map paramMap = new HashMap();
									sql.append("select x.").append(pkFieldName);
									for(W5FormCell fc2:dfr.getForm().get_formCells()) if(fc2.getActiveFlag()!=0 && fc2.getControlType()>0 && fc2.getControlType()<90 && fc2.get_sourceObjectDetail()!=null){
										W5TableField tf2 = (W5TableField)fc2.get_sourceObjectDetail();
										if(tf2.getTabOrder()!=1)sql.append(", x.").append(tf2.getDsc());
										if(fc2.getControlType()==71) {//file
											if(FrameworkCache.getTable(scd, FrameworkSetting.customFileTableId)==null)
												sql.append(", (select a.original_file_name from iwb.w5_file_attachment a where a.project_uuid='").append(scd.get("projectId")).append("' AND a.file_attachment_id=x.").append(tf2.getDsc()).append(") ").append(tf2.getDsc()).append("_qw_");
											else 
												sql.append(", (select a.dsc from x_file a where a.file_id=x.").append(tf2.getDsc()).append(") ").append(tf2.getDsc()).append("_qw_");
											
										}
									}
									sql.append(" from ").append(dt.getDsc()).append(" x where x.");
									sql.append(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc()).append("=?");
									if(tc.getRelatedStaticTableFieldId()!=0 && tc.getRelatedStaticTableFieldVal()!=0) {
										paramMap.put(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc(), tc.getRelatedStaticTableFieldVal());
										sql.append(" AND ").append(dt.get_tableFieldMap().get(tc.getRelatedStaticTableFieldId()).getDsc()).append("=").append(tc.getRelatedStaticTableFieldVal());
									}			
									List params = new ArrayList();
									int param1 = GenericUtil.uInt(requestParams.get(t.get_tableParamList().get(0).getDsc())); 
									params.add(param1);
									paramMap.put(dt.get_tableFieldMap().get(tc.getRelatedTableFieldId()).getDsc(), param1);
									if(dt.get_tableParamList().size()>0)for(W5TableParam tp2:dt.get_tableParamList())if(tp2.getSourceType()==2) {
										sql.append(" AND x.").append(tp2.getExpressionDsc()).append("=?");
										params.add(scd.get(tp2.getDsc()));
									}
									sql.append(" order by 1");

									List<Map> list = dao.executeSQLQuery2Map(sql.toString(), params);
									if(!GenericUtil.isEmpty(list)) {
										for(Map m2:list) {//converting PK to specialField 
											m2.put("_id_"+pkFieldName, m2.get(pkFieldName));
											m2.put("_id_", m2.get(pkFieldName));
											m2.remove(pkFieldName);
										}
										if(dfr.getOutputFields()==null)dfr.setOutputFields(new HashMap());
										dfr.getOutputFields().put("list", list);

									}
									dfr.getOutputFields().put("paramMap", paramMap);

								}
									
							}

						break;

					}
				}
				break;
			case 5: // formByQuery:
				formResult
						.setQueryResult4FormCell(queryEngine.executeQuery(scd, f.getObjectId(), requestParams));
				formResult.setFormCellResults(new ArrayList());
				for (Object[] d : formResult.getQueryResult4FormCell().getData()) {
					W5FormCellHelper result = GenericUtil.getFormCellResultByQueryRecord(d);
					// result.getFormCell().setTabOrder(tabOrder++);
					formResult.getFormCellResults().add(result);
				}
				break;
			}

			if (f.getObjectType() != 2 && (f.getObjectType()!=11 || action == 0))
				action = 2; // eger table degilse sadece initializeForm olabilir

			if (f.getObjectType() != 5
					&& action == 9 /* edit (if not insert) */) {
				action = dao.checkIfRecordsExists(scd, requestParams, t) ? 1 : 2;
			}

			/* tableTrigger before Show start */
			if (f.getObjectType() == 2 && action != 3)
				crudEngine.extFormTableEvent(formResult, new String[] { "_", "su", "si", "_", "_", "si" }[action], scd,
						requestParams, t, null, null);
			/* end of tableTrigger */

			if (f.getObjectType() != 5)
				switch (action) {
				case 5: // copy
				case 1: // edit
					if (f.getObjectType() == 2) {
						if (action == 1 && !GenericUtil.isEmpty(f.get_conversionList())) { // conversion form, then sync
							// covnerted objects find
							String inStr = "";
							for (W5Conversion cnv : f.get_conversionList())
								if (GenericUtil.hasPartInside2(cnv.getActionTypes(), action)
										|| cnv.getSynchOnUpdateFlag() != 0) { // synch
																				// varsa
									inStr += "," + cnv.getConversionId();
								}
							if (inStr.length() > 1) {
								List<W5ConvertedObject> lco = dao.find(
										"from W5ConvertedObject x where x.projectUuid=?0 AND x.conversionId in ("
												+ inStr.substring(1) + ") and x.srcTablePk=?1",
										projectId,
										GenericUtil.uInt(requestParams, t.get_tableParamList().get(0).getDsc()));
								if (!lco.isEmpty()) {
									Map<Integer, List<W5ConvertedObject>> m = new HashMap();
									formResult.setMapConvertedObject(m);
									List<W5ConvertedObject> orphanCol = new ArrayList();
									for (W5ConvertedObject co : lco) {
										int dstTableId = 0;
										for (W5Conversion cnv : f.get_conversionList())
											if (cnv.getConversionId() == co.getConversionId()) {
												dstTableId = cnv.getDstTableId();
												break;
											}
										co.set_relatedRecord(dao.findRecordParentRecords(scd, dstTableId,
												co.getDstTablePk(), 10, false));
										if (GenericUtil.isEmpty(co.get_relatedRecord())) {
											orphanCol.add(co);
										} else {
											List<W5ConvertedObject> l = m.get(co.getConversionId());
											if (l == null) {
												l = new ArrayList();
												m.put(co.getConversionId(), l);
											}
											l.add(co);
										}
									}
									if (!orphanCol.isEmpty()) {
										dao.removeAllObjects(orphanCol);
									}
								}
							}
						}
						dao.loadFormTable(formResult);
					} else if(f.getObjectType() == 11){ //ws method
						if (f.getSourceWsMethodId() != 0) {
							W5WsMethod wsm = FrameworkCache.getWsMethod(scd, f.getSourceWsMethodId());
							if(wsm!=null){
								
								Map r = restEngine.REST(scd, FrameworkCache.getWsClientById(scd, wsm.getWsId()).getDsc()+"."+wsm.getDsc(), requestParams);
								if(r!=null){
									List lfcr = new ArrayList();
									formResult.setFormCellResults(lfcr);
									for(W5FormCell cell:f.get_formCells())if(cell.getActiveFlag()!=0){
										W5FormCellHelper result = new W5FormCellHelper(cell);
										Object o = r.get(cell.getDsc());
										if(o!=null) {
											if(cell.getControlType()==5) {
												if(o.toString()=="true" || o.toString()=="on" || o.toString()=="yes" || GenericUtil.uInt(o)!=0)
													result.setValue("1");
												else result.setValue("0");
											} else
												result.setValue(o.toString());
										}
										lfcr.add(result);
									}
		
								}
							}
						}
					}

					// eralp istedi, amaci freefieldlarda initial query deger
					// verebilsin
					for (W5FormCellHelper fcx : formResult.getFormCellResults())
						if (fcx.getFormCell().getObjectDetailId() == 0) { // bir
																			// tane
																			// freefield
																			// bulabilirse
																			// gir
							int initialQueryId = GenericUtil.uInt(requestParams, "_iqid");
							if (initialQueryId != 0) {
								Map<String, Object> m = queryEngine.executeQuery2Map(scd, initialQueryId, requestParams);
								if (m != null) {
									for (W5FormCellHelper fc : formResult.getFormCellResults())
										if (fc.getFormCell().getObjectDetailId() == 0) {
											Object s = m.get(
													fc.getFormCell().getDsc().toLowerCase(FrameworkSetting.appLocale));
											if (s != null)
												fc.setValue(s.toString());
										}
								}
							}
							for (W5FormCellHelper fcx2 : formResult.getFormCellResults())
								if (fcx2.getFormCell().getObjectDetailId() == 0)
									switch (fcx2.getFormCell().getInitialSourceType()) {
									case 0: // yok-sabit
										fcx2.setValue(fcx2.getFormCell().getInitialValue());
										break;
									case 1: // request
										fcx2.setValue(formResult.getRequestParams()
												.get(fcx2.getFormCell().getInitialValue()));
										break;
									case 2:
										Object o = formResult.getScd().get(fcx2.getFormCell().getInitialValue());
										fcx2.setValue(o == null ? null : o.toString());
										break;
									case 3: // app_setting
										fcx2.setValue(FrameworkCache.getAppSettingStringValue(formResult.getScd(),
												fcx2.getFormCell().getInitialValue()));
										break;
									case 4: // SQL
										Object[] oz = DBUtil.filterExt4SQL(fcx2.getFormCell().getInitialValue(),
												formResult.getScd(), formResult.getRequestParams(), null);
										if (oz[1] != null && ((List) oz[1]).size() > 0) {
											Map<String, Object> m = dao.runSQLQuery2Map(oz[0].toString(), (List) oz[1],
													null);
											if (m != null && m.size() > 0)
												fcx2.setValue(m.values().iterator().next().toString());
										} else {
											List l = dao.executeSQLQuery(oz[0].toString());
											if (l != null && l.size() > 0 && l.get(0) != null)
												fcx2.setValue(l.get(0).toString());
										}
										break;
									}

							break;
						}
					for (W5FormCellHelper fcx : formResult.getFormCellResults())
						if (fcx.getFormCell().getFormCellId() == 6060) { // mail password
							fcx.setValue("************");
						}

					if (action == 1)
						break;
				case 2: // insert
					if (action == 2) {
						dao.initializeForm(formResult, false);
						Map mq = null;
						boolean convb = false;
						if (requestParams.containsKey("_cnvId") && requestParams.containsKey("_cnvTblPk")) { // conversion
							int conversionId = GenericUtil.uInt(requestParams, "_cnvId");
							int conversionTablePk = GenericUtil.uInt(requestParams, "_cnvTblPk");
							if (conversionId != 0 && conversionTablePk != 0) {
								String prjId = FrameworkCache.getProjectId(scd, "707." + conversionId);
								W5Conversion c = (W5Conversion) metadataLoader.getMetadataObject(
										"W5Conversion","conversionId", conversionId,
										prjId, "Conversion");
								if (c == null || c.getDstFormId() != formResult.getFormId()) {
									throw new IWBException("framework", "Conversion", conversionId, null,
											LocaleMsgCache.get2(0, (String) scd.get("locale"), "wrong_conversion"),
											null);
								}
								if (c.getMaxNumofConversion() > 0) {
									List l = dao.find(
											"select 1 from W5ConvertedObject c where c.conversionId=?0  AND c.projectUuid=?1 AND c.srcTablePk=?2",
											conversionId, scd.get("projectId"), conversionTablePk);
									if (l.size() >= c.getMaxNumofConversion()) {
										throw new IWBException("framework", "Conversion", conversionId, null,
												LocaleMsgCache.get2(0, (String) scd.get("locale"),
														"max_number_of_conversion_reached") + " (" + l.size() + ")",
												null);
									}
								}
								W5Table srcTable = FrameworkCache.getTable(scd, c.getSrcTableId());
								if (!GenericUtil.isEmpty(srcTable.get_approvalMap())) { // burda
																						// bir
																						// approval
																						// olabilir,
																						// kontrol
																						// etmek
																						// lazim
									List l = dao.find(
											"select 1 from W5WorkflowRecord c where c.projectUuid=?0  AND c.finishedFlag=0 AND c.tableId=?1 AND c.tablePk=?2",
											scd.get("projectId"), c.getSrcTableId(), conversionTablePk);
									if (!l.isEmpty()) {
										throw new IWBException("framework", "Conversion", conversionId, null,
												LocaleMsgCache.get2(0, (String) scd.get("locale"),
														"record_must_be_approved_before_conversion"),
												null);
									}
								}
								mq = conversionEngine.interprateConversionTemplate(c, formResult, conversionTablePk, true, false);
								if (mq != null) {
									convb = true;
									formResult.getOutputMessages()
											.add(LocaleMsgCache.get2(scd, "fw_converted_from")
													+ " : " + mq
															.get("_cnv_record")
													+ " --> " + LocaleMsgCache.get2(scd,
															mq.get("_cnv_name").toString() + " --> ?"));
								}
							}
						} else if ((formId == 650 /* || formId==631 */) && requestParams.containsKey("_fsmId")) { // formId=650/631
																													// ise
																													// buna
																													// gore
																													// mail/sms
																													// hazirlanacak
							int fsmFrmId = GenericUtil.uInt(requestParams, "_fsmFrmId");
							W5FormResult fsmformResult = getFormResult(formResult.getScd(), fsmFrmId, (short) 2,
									requestParams);
							int fsmId = GenericUtil.uInt(requestParams, "_fsmId");
							W5FormSmsMail fsm = fsmformResult.getForm().get_formSmsMailMap().get(fsmId);
							int fsmTableId = GenericUtil.uInt(requestParams, "_tableId");
							int fsmTablePk = GenericUtil.uInt(requestParams, "_tablePk");
							W5Email email = /*
											 * formId==631 ?
											 * dao.interprateSmsTemplate(fsm,
											 * formResult.getScd(),requestParams
											 * ,fsmTableId,fsmTablePk) :
											 */
							dao.interprateMailTemplate(fsm, formResult.getScd(), requestParams, fsmTableId, fsmTablePk);
							if (formId == 650 && !GenericUtil.isEmpty(scd.get("mailSettingId"))) {
								Map<String, Object> mapMailSign = dao.runSQLQuery2Map(
										"select x.EMAIL_SIGNATURE s from iwb.w5_object_mail_setting x where x.MAIL_SETTING_ID=${scd.mailSettingId} AND x.customization_id in (0,${scd.customizationId})",
										scd, requestParams, null);
								if (!GenericUtil.isEmpty(mapMailSign) && !GenericUtil.isEmpty(mapMailSign.get("s"))) {
									if (email == null || GenericUtil.isEmpty(email.getMailBody()))
										mq.put("pmail_body",
												FrameworkSetting.mailSeperator + (String) mapMailSign.get("s"));
									else
										mq.put("pmail_body", email.getMailBody() + FrameworkSetting.mailSeperator
												+ (String) mapMailSign.get("s"));
								}
							}
							formResult.getOutputMessages()
									.add(LocaleMsgCache.get2(scd, "fw_mail_converted_from")
											+ " : " + dao.getSummaryText4Record(scd, fsmTableId, fsmTablePk) + " --> "
											+ fsm.getDsc());
						} else if (requestParams.containsKey("_iqid")) {
							mq = queryEngine.executeQuery2Map(scd, GenericUtil.uInt(requestParams, "_iqid"), requestParams);
						}
						if (mq != null) {
							for (W5FormCellHelper fc : formResult.getFormCellResults()) {
								Object s = mq.get(fc.getFormCell().getDsc().toLowerCase(FrameworkSetting.appLocale));
								if (s != null) {
									fc.setValue(s.toString());
									// conversion esnasinda, request'ten gelen
									// degerleri simule ediyor, request'e
									// manuel koyarak
									if (convb && (fc.getFormCell().getControlType() == 0
											|| (fc.getFormCell().getNrdType() == 2
													&& fc.getFormCell().getInitialSourceType() == 1
													&& fc.getFormCell().getInitialValue() != null && fc.getFormCell()
															.getInitialValue().equals(fc.getFormCell().getDsc()))))
										formResult.getRequestParams().put(fc.getFormCell().getDsc(), s.toString());
								}
							}
						}
					} else { // copy
						if (f.getObjectType() == 2)
							for (W5FormCellHelper fcr : formResult.getFormCellResults())
								if (fcr.getFormCell().get_sourceObjectDetail() != null)
									switch (((W5TableField) fcr.getFormCell().get_sourceObjectDetail())
											.getCopySourceTip()) {
									case 1: // request
										fcr.setValue(
												dao.getInitialFormCellValue(scd, fcr.getFormCell(), requestParams));
										break;
									case 6: // object_source:demek ki sorulacak
											// degisecek mi diye?
										break;
									case 7: // object_source(readonly)
										fcr.setHiddenValue("1");
									}
						Map mq = null;
						if (requestParams.containsKey("_iqid")) {
							mq = queryEngine.executeQuery2Map(scd, GenericUtil.uInt(requestParams, "_iqid"), requestParams);
						}
						if (mq != null) {
							for (W5FormCellHelper fc : formResult.getFormCellResults())
								if (fc.getValue() == null || fc.getValue().length() == 0) {
									Object s = mq
											.get(fc.getFormCell().getDsc().toLowerCase(FrameworkSetting.appLocale));
									if (s != null)
										fc.setValue(s.toString());
								}
						}
						action = 2;
						formResult.setAction(2);
					}

					break;
				default:
					throw new IWBException("framework", "Form", formId, null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "wrong_use_of_action") + " (" + action
									+ ")",
							null);
				}

			// form cell lookup load
			dao.loadFormCellLookups(formResult.getScd(), formResult.getFormCellResults(), formResult.getRequestParams(),
					FrameworkSetting.liveSyncRecord && f.getObjectType() == 2
							? formResult.getUniqueId() : null);

			for (W5FormCellHelper cr : formResult.getFormCellResults())
				if (cr.getFormCell().getControlType() == 99) { // grid 
					W5Grid g = (W5Grid) cr.getFormCell().get_sourceObjectDetail();
					W5GridResult gr = new W5GridResult(g.getGridId());
					gr.setRequestParams(formResult.getRequestParams());
					gr.setScd(formResult.getScd());
					gr.setFormCellResultMap(new HashMap());

					for (W5GridColumn column : g.get_gridColumnList())
						if (column.get_formCell() != null) {
							gr.getFormCellResultMap().put(column.get_formCell().getFormCellId(),
									new W5FormCellHelper(column.get_formCell()));
						}

					gr.setGrid(g);
					if (formResult.getModuleGridMap() == null)
						formResult.setModuleGridMap(new HashMap());
					formResult.getModuleGridMap().put(g.getGridId(), gr);

					if (!gr.getFormCellResultMap().isEmpty())
						dao.loadFormCellLookups(gr.getScd(), new ArrayList(gr.getFormCellResultMap().values()),
								gr.getRequestParams(), null);
				}

			if (GenericUtil.uInt(formResult.getRequestParams().get("viewMode")) != 0)
				formResult.setViewMode(true);

			W5WorkflowStep workflowStep = formResult.getApprovalStep();
			/*if (f.getObjectTip() == 2 && action == 1
					&& FrameworkCache.getTable(scd,
							f.getObjectId()) != null
					&& formResult.getApprovalRecord() != null) {
				W5Workflow workflow = FrameworkCache.getWorkflow(projectId,
						formResult.getApprovalRecord().getApprovalId());
				if (workflow != null) {
					workflowStep = formResult.getApprovalStep();//workflow.get_approvalStepMap().get(formResult.getApprovalRecord().getApprovalStepId()).getNewInstance();
					if (workflowStep != null) {
						boolean canCancel = GenericUtil.hasPartInside2(workflow.getAfterFinUpdateUserIds(),
								scd.get("userId")) && formResult.getApprovalRecord().getApprovalActionTip() == 5
								&& formResult.getApprovalRecord().getApprovalStepId() == 998 ? true : false;
						if (workflowStep.getApprovalStepId() != 901 && workflowStep.getUpdatableFields() == null
								&& !canCancel)
							formResult.setViewMode(true);
						formResult.setApprovalStep(workflowStep);
					}
				}
			}*/
			// normal/readonly/disabled show control/ozel kodlama
			int updatableFieldsCount = 0;
			for (W5FormCellHelper cr : formResult.getFormCellResults())
				if (cr.getFormCell().getControlType() != 0 && cr.getFormCell().getControlType() != 13
						&& cr.getFormCell().getControlType() != 100) { // yok ve
																		// hidden
																		// ve
																		// buttondan
																		// baska
					W5TableField tf = f.getObjectType() == 2
							&& cr.getFormCell().get_sourceObjectDetail() instanceof W5TableField
									? (W5TableField) cr.getFormCell().get_sourceObjectDetail() : null;
					if (formResult.isViewMode() || cr.getHiddenValue() != null
							|| (action == 1 && cr.getFormCell()
									.getControlType() == 31 /* ozel kodlama */
							&& GenericUtil.uInt(cr.getFormCell().getLookupIncludedValues()) == 1
							&& !GenericUtil.hasPartInside(cr.getFormCell().getLookupIncludedParams(),
									"" + formResult.getScd().get("userId")))
							|| cr.getFormCell().getNrdType() != 0 // readonly/disabled
							|| (workflowStep != null
									&& cr.getFormCell()
											.get_sourceObjectDetail() != null
									&& !GenericUtil.hasPartInside(workflowStep.getUpdatableFields(),
											"" + ((W5TableField) cr.getFormCell().get_sourceObjectDetail())
													.getTableFieldId())) // approvalStepUpdatable
																			// Table
																			// Fields
							|| (f.getObjectType() == 2 && action == 1 && tf != null
									&& (tf.getCanUpdateFlag() == 0 || (tf.getAccessUpdateTip() != 0
											&& !GenericUtil.accessControl(scd, tf.getAccessUpdateTip(),
													tf.getAccessUpdateRoles(), tf.getAccessUpdateUsers())
											&& (GenericUtil.isEmpty(tf.getAccessUpdateUserFields())
													|| dao.accessUserFieldControl(t, tf.getAccessUpdateUserFields(),
															scd, requestParams, null)))))) { // ction=edit'te
																								// edti
																								// hakki
																								// yok

						String value = cr.getValue();
						cr.setHiddenValue(value == null || value.length() == 0 ? "_" : GenericUtil.stringToJS(value));
						switch (cr.getFormCell().getControlType()) {
						case 5: // checkbox
							break;
						case 9:
						case 16:
						case 60: // remote Lookups
							// cr.setHiddenValue(null);
							break;
						case 2: // date
							cr.setValue(GenericUtil.uDateStr(value));
							break;
						case 18: // timestamp
							cr.setValue(value);
							break;
						case 10:
						case 61: // autoselect,
									// superboxselect-combo-query-advanced
							if (cr.getLookupQueryResult() != null && cr.getLookupQueryResult().getData() != null
									&& cr.getLookupQueryResult().getData().size() != 0) {
								cr.setValue((String) cr.getLookupQueryResult().getData().get(0)[0]);
								// cr.setHiddenValue(value);
							}
							break;
						case 6: // combo static
							// cr.setHiddenValue(cr.getValue());
							if (cr.getLookupListValues() != null)
								for (W5Detay d : (List<W5Detay>) cr.getLookupListValues()) {
									if (d.getVal().equals(cr.getValue())) {
										cr.setValue(LocaleMsgCache.get2((Integer) scd.get("customizationId"),
												(String) scd.get("locale"), d.getDsc()));
										break;
									}
								}
							break;
						case 7: // combo query
						case 23: // treecombo query
							// cr.setHiddenValue(cr.getValue());
							if (cr.getLookupQueryResult() != null && cr.getLookupQueryResult().getData() != null)
								for (Object[] d : (List<Object[]>) cr.getLookupQueryResult().getData()) {
									if (d[1] != null && d[1].toString().equals(cr.getValue())) {
										cr.setValue(d[0].toString());
										break;
									}
								}
							break;
						case 59: // superboxselect query
						case 15: // lov combo query
							String xval = "";
							if (cr.getLookupQueryResult() != null && cr.getValue() != null) {
								Vector<String> vals = new Vector<String>();
								for (String str : cr.getValue().split(",")) {
									vals.add(str);
								}
								for (Object[] d : (List<Object[]>) cr.getLookupQueryResult().getData()) {
									if (vals.contains(d[1].toString())) {
										xval += cr.getLookupQueryResult().getQuery().get_queryFields().get(0)
												.getPostProcessType() == 2
														? LocaleMsgCache.get2((Integer) scd.get("customizationId"),
																(String) scd.get("locale"), d[0].toString()) + ", "
														: d[0].toString() + ", ";
									}
								}
							}
							cr.setValue(xval.length() > 2 ? xval.substring(0, xval.length() - 2) : xval);
							break;

						case 58: // superboxselect static
						case 8: // lov combo static
							if (cr.getValue() != null) {
								String xval2 = "";
								String[] arr = cr.getValue().split(",");
								for (int sindex = 0; sindex < arr.length; sindex++) {
									int no = GenericUtil.getIndexNo(arr[sindex], cr.getLookupListValues());
									W5LookUpDetay ld = (W5LookUpDetay) cr.getLookupListValues().get(no);
									xval2 += LocaleMsgCache.get2((Integer) scd.get("customizationId"),
											(String) scd.get("locale"), ld.getDsc()) + " , ";
								}
								cr.setValue(xval2.substring(0, xval2.length() - 2));
							}
							break;
						}

					} else
						updatableFieldsCount++;
				} else if (cr.getFormCell().getControlType() == 100) { // Buton
																		// ise
																		// butonun
																		// extra
																		// kodunda
																		// local
																		// mesajları
																		// cevirereceğiz
																		// inşallah
					cr.getFormCell().setExtraDefinition(GenericUtil.filterExt(cr.getFormCell().getExtraDefinition(),
							formResult.getScd(), formResult.getRequestParams(), null).toString());
				} else
					cr.setHiddenValue(null);

			if (f.getObjectType() == 2) { // table ise
				if (updatableFieldsCount == 0)
					formResult.setViewMode(true);
				if (action == 1) { // eidt mode'da ise
					if (FrameworkSetting.alarm /* && !formResult.isViewMode() */
							&& !GenericUtil.isEmpty(f.get_formSmsMailList())) { // readonly
																									// degil
																									// ise
						boolean alarm = false;
						for (W5FormSmsMail i : f.get_formSmsMailList())
							if (i.getAlarmFlag() != 0) {
								alarm = true;
								break;
							}
						if (alarm) {
							formResult.setFormAlarmList((List<W5FormSmsMailAlarm>) dao.find(
									"from W5FormSmsMailAlarm a where a.projectUuid=?0  AND a.insertUserId=?1 AND a.tableId=?2 AND a.tablePk=?3 ",
									projectId, scd.get("userId"), f.getObjectId(),
									GenericUtil.uInt(requestParams, t.get_tableParamList().get(0).getDsc())));
						}
					}
				}
			}

			return formResult;
		} catch (Exception e) {
			throw new IWBException("framework", "Form", formId, null, "[40," + formId + "]"
					+ (formResult != null && formResult.getForm() != null ? " " + formResult.getForm().getDsc() : ""),
					e);
		}
	}
	public W5GridResult getGridResult(Map<String, Object> scd, int gridId, Map<String, String> requestParams) {
		return metadataLoader.getGridResult(scd, gridId, requestParams, false);
	}
	
	public W5PageResult getPageResult(Map<String, Object> scd, int pageId, Map<String, String> requestParams) {
		W5PageResult pr = null;
		try {
			boolean developer = scd.get("roleId") != null && (Integer) scd.get("roleId") == 0;
			boolean debugAndDeveloper = FrameworkSetting.debug && developer;
			pr = metadataLoader.getPageResult(scd, pageId);
			pr.setRequestParams(requestParams);
			pr.setPageObjectList(new ArrayList<Object>());
			List<W5PageObject> templateObjectListExt = new ArrayList<W5PageObject>(
					pr.getPage().get_pageObjectList().size() + 5);
			templateObjectListExt.addAll(pr.getPage().get_pageObjectList());

			requestParams.put("_dont_throw", "1");

			for (int i = 1; requestParams.containsKey("_gid" + i) || requestParams.containsKey("_fid" + i)
					|| requestParams.containsKey("_dvid" + i) || requestParams.containsKey("_lvid" + i)
					|| requestParams.containsKey("_gdid" + i) || requestParams.containsKey("_mlid" + i)
					|| requestParams.containsKey("_qid" + i) || requestParams.containsKey("_ggid" + i); i++) { // extra
																												// olarak
																												// _gid1=12&_gid=2
																												// gibi
																												// seyler
																												// soylenebilir
				int objectId = GenericUtil.uInt(requestParams.get("_gid" + i)); // grid
				short objectType = -1;
				if (objectId == 0) {
					objectId = GenericUtil.uInt(requestParams.get("_fid" + i)); // form
					objectType = -3;
				}
				if (objectId == 0) {
					objectId = GenericUtil.uInt(requestParams.get("_dvid" + i)); // card
					objectType = -2;
				}
				if (objectId == 0) {
					objectId = GenericUtil.uInt(requestParams.get("_lvid" + i)); // list view
					objectType = -7;
				}
				if (objectId == 0) {
					objectId = GenericUtil.uInt(requestParams.get("_qid" + i)); // query Result
					objectType = -4;
				}
				if (objectId == 0) {
					objectId = GenericUtil.uInt(requestParams.get("_mlid" + i)); // mobile list view
					objectType = -11;
				}
				if (objectId == 0) {
					objectId = GenericUtil.uInt(requestParams.get("_gdid" + i)); // graph dashboard
					objectType = -9;
				}
				if (objectId == 0) {
					objectId = GenericUtil.uInt(requestParams.get("_ggid" + i)); // gauge view
					objectType = -22;
				}
				W5PageObject o = new W5PageObject();
				o.setObjectType(objectType);
				o.setObjectId(objectId);
				templateObjectListExt.add(o);
			}
			
			if(pr.getPage().getPageType()==0 && scd!=null && GenericUtil.uInt(scd.get("dashboardPageId"))!=0) {//html
				W5PageObject o = new W5PageObject();
				o.setObjectType((short)-31);
				o.setObjectId(GenericUtil.uInt(scd.get("dashboardPageId")));
				templateObjectListExt.add(o);
			}

			int objectCount = 0;
			for (W5PageObject o : templateObjectListExt) {
				boolean accessControl = debugAndDeveloper ? true
						: GenericUtil.accessControl(scd, o.getAccessViewTip(), o.getAccessViewRoles(),
								o.getAccessViewUsers());
				Object obz = null;
				W5Table mainTable = null;
				switch (Math.abs(o.getObjectType())) {
				case 1: // grid
					W5GridResult gridResult = metadataLoader.getGridResult(scd, o.getObjectId(), requestParams,
							pageId == 298 /* || objectCount!=0 */);
					if (o.getObjectType() < 0) {
						if (GenericUtil.uInt(requestParams, "_gid" + gridResult.getGridId() + "_a") != 0)
							gridResult.setAction(
									GenericUtil.uInt(requestParams, "_gid" + gridResult.getGridId() + "_a"));
						gridResult.setGridId(-gridResult.getGridId());
					}
					mainTable = gridResult.getGrid() != null && gridResult.getGrid().get_query() != null
							? FrameworkCache.getTable(scd, gridResult.getGrid().get_query().getSourceObjectId())
							: null;
					if (!debugAndDeveloper && mainTable != null
							&& ((mainTable.getAccessViewUserFields() == null
									&& !GenericUtil.accessControl(scd, mainTable.getAccessViewTip(),
											mainTable.getAccessViewRoles(), mainTable.getAccessViewUsers()))))
						obz = gridResult.getGrid().getDsc();
					else {
						if (GenericUtil.uInt(requestParams, "_viewMode") != 0)
							gridResult.setViewReadOnlyMode(true);
						else if (GenericUtil.uInt(requestParams, "_viewMode" + o.getObjectId()) != 0)
							gridResult.setViewReadOnlyMode(true);
						obz = accessControl ? gridResult : gridResult.getGrid().getDsc();
					}
					if (obz instanceof W5GridResult) {
						if(gridResult.getExtraOutMap()==null)gridResult.setExtraOutMap(new HashMap());
						Map m = gridResult.getExtraOutMap();
						gridResult.setTplObj(o);
						gridResult.setExtraOutMap(m);
						m.put("tplId", o.getPageId());
						m.put("tplObjId", o.getPageObjectId());
					}
					break;
				case 2: // card view
					W5CardResult cardResult = metadataLoader.getCardResult(scd, o.getObjectId(), requestParams,
							objectCount != 0);
					if (o.getObjectType() < 0)
						cardResult.setCardId(-cardResult.getCardId());
					mainTable = cardResult.getCard() != null && cardResult.getCard().get_query() != null
							? FrameworkCache.getTable(scd, cardResult.getCard().get_query().getSourceObjectId())
							: null;
					if (!debugAndDeveloper && mainTable != null
							&& ((mainTable.getAccessViewUserFields() == null
									&& !GenericUtil.accessControl(scd, mainTable.getAccessViewTip(),
											mainTable.getAccessViewRoles(), mainTable.getAccessViewUsers()))))
						obz = cardResult.getCard().getDsc();
					else {
						obz = accessControl ? cardResult : cardResult.getCard().getDsc();
					}
					if (obz instanceof W5CardResult) {
						Map m = new HashMap();
						cardResult.setTplObj(o);
						cardResult.setExtraOutMap(m);
						m.put("tplId", o.getPageId());
						m.put("tplObjId", o.getPageObjectId());
					}
					break;
				case 7: // list view
					W5ListViewResult listViewResult = metadataLoader.getListViewResult(scd, o.getObjectId(), requestParams,
							objectCount != 0);
					if (o.getObjectType() < 0)
						listViewResult.setListId(-listViewResult.getListId());
					mainTable = listViewResult.getListView() != null
							&& listViewResult.getListView().get_query() != null
									? FrameworkCache.getTable(scd,
											listViewResult.getListView().get_query().getSourceObjectId())
									: null;
					if (!debugAndDeveloper && mainTable != null
							&& (( mainTable.getAccessViewUserFields() == null
									&& !GenericUtil.accessControl(scd, mainTable.getAccessViewTip(),
											mainTable.getAccessViewRoles(), mainTable.getAccessViewUsers()))))
						obz = listViewResult.getListView().getDsc();
					else {
						obz = accessControl ? listViewResult : listViewResult.getListView().getDsc();
					}
					break;
				case 3: // form
					W5FormResult formResult = getFormResult(scd, o.getObjectId(),
							requestParams.get("a") != null ? GenericUtil.uInt(requestParams, "a") : 2,
							requestParams);
					if (o.getObjectType() < 0)
						formResult.setFormId(-formResult.getFormId());
					formResult.setObjectType(o.getObjectType()); // render
																// icin
																// gerekecek
					/*
					 * if(PromisSetting.moduleAccessControl!=0 &&
					 * formResult.getForm()!=null &&
					 * formResult.getForm().get_sourceTable()!=null &&
					 * !PromisCache.roleAccessControl(scd,
					 * formResult.getForm().get_sourceTable().getModuleId())
					 * ) obz = formResult.getForm().getDsc(); else
					 */
					obz = accessControl ? formResult : formResult.getForm().getDsc();
					break;
				case 4: // query
				case	10:
					if(FrameworkSetting.metadata && pr.getPage().getPageType()==0 && o.getObjectId()==2822) { //prepare menu from cache
						obz = FrameworkCache.getQueryResult4Menu(scd);
					} else {
						Map paramMap = new HashMap();
						paramMap.putAll(requestParams);
						if (!GenericUtil.isEmpty(o.getPostJsCode())) {
							String[] ar1 = o.getPostJsCode().split("&");
							for (int it4 = 0; it4 < ar1.length; it4++) {
								String[] ar2 = ar1[it4].split("=");
								if (ar2.length == 2 && ar2[0] != null && ar2[1] != null)
									paramMap.put(ar2[0], ar2[1]);
							}
						}
						obz = queryEngine.executeQuery(scd, o.getObjectId(), paramMap);
					}
					break;
				case 8:// component
					obz = FrameworkCache.getComponent(scd, o.getObjectId());//metaDataDao.loadComponent(scd, o.getObjectId(), new HashMap());
					break;
				case 15: // Graph Query
//				case 10: // Badge
				case 22: // Gauge
					obz = metadataLoader.getQueryResult(scd, o.getObjectId());//queryEngine.executeQuery(scd, o.getObjectId(), new HashMap());
					break;
				case 5: // dbFunc
					obz = scriptEngine.executeGlobalFunc(scd, o.getObjectId(), requestParams, (short) 1);
					break;
				case 11: // Mobile List
					obz = metadataLoader.getMListResult(scd, o.getObjectId(), requestParams, false);
					break;
				case 31: // Page
//					obz = pr.getPage().getTemplateTip()==0? getPageResult(scd, o.getObjectId(), new HashMap()):metaDataDao.getPageResult(scd, o.getObjectId());
					obz = getPageResult(scd, o.getObjectId(), new HashMap());
					break;
				case 9: // graph dashboard
					W5BIGraphDashboard obz2 = (W5BIGraphDashboard) metadataLoader.getMetadataObject(
							"W5BIGraphDashboard","graphDashboardId",
							o.getObjectId(), scd.get("projectId"), null);
					if (accessControl) {
						obz = obz2;
					} else {
						obz = "graph" + o.getObjectId();
					}
					break;
				}
				if (pr.getPage().getPageType() <= 2 && objectCount == 0) { // throw exception for the first object
					if (obz instanceof String)
						throw new IWBException("security", "Module", o.getObjectId(), null,
								"Role Access Control(Page Object)", null);

				}
				if (obz != null)
					pr.getPageObjectList().add(obz);
				objectCount++;
			}
			
			if ((Integer) scd.get("customizationId") == 1 && GenericUtil.uInt(scd.get("mainTemplateId")) == pageId) {
				List<Object> params = new ArrayList();
				params.add(scd.get("projectId"));
				dao.executeUpdateSQLQuery(
						"update iwb.w5_project p set preview_count=preview_count+1 where p.project_uuid=?", params);
			}
			return pr;
		} catch (Exception e) {
			throw new IWBException("framework", "Load.Page", pageId, null,
					"[63," + pageId + "]" + (pr != null && pr.getPage() != null ? " " + pr.getPage().getDsc() : ""), e);
		}
	}
	

	public W5FormCellHelper reloadFormCell(Map<String, Object> scd, int fcId, String webPageId, String tabId) {
		String projectId = FrameworkCache.getProjectId(scd, null);

		int userId = (Integer) scd.get("userId");
		W5FormCell c = (W5FormCell) metadataLoader.getMetadataObject(
				"W5FormCell","formCellId", fcId, projectId, null);
		if (c == null)
			return null;
		W5FormCellHelper rc = new W5FormCellHelper(c);
		String includedValues = c.getLookupIncludedValues();
		Map<String, String> requestParams = null;
		switch (c.getControlType()) {
		case 58: // superboxselect
		case 8: // lovcombo static
		case 6: // eger static combobox ise listeyi load et
			W5LookUp lookUp = FrameworkCache.getLookUp(scd, c.getLookupQueryId());
			rc.setLocaleMsgFlag((short) 1);
			requestParams = UserUtil.getTableGridFormCellReqParams(projectId, -c.getLookupQueryId(), userId,
					(String) scd.get("sessionId"), webPageId, tabId, -fcId);
			List<W5LookUpDetay> oldList = metadataLoader.findLookUpDetay( c.getLookupQueryId(), lookUp.getProjectUuid());

			List<W5LookUpDetay> newList = null;
			if (includedValues != null && includedValues.length() > 0) {
				// List<W5LookUpDetay> oldList = lookUp.get_detayList();
				boolean notInFlag = false;
				if (includedValues.charAt(0) == '!') {
					notInFlag = true;
					includedValues = includedValues.substring(1);
				}
				String[] ar1 = includedValues.split(",");
				newList = new ArrayList<W5LookUpDetay>(oldList.size());
				for (W5LookUpDetay p : oldList)
					if ((rc.getValue() != null && p.getVal().equals(rc.getValue())) || p.getActiveFlag() != 0) {
						boolean in = false;
						for (int it4 = 0; it4 < ar1.length; it4++)
							if (ar1[it4].equals(p.getVal())) {
								in = true;
								break;
							}
						if (in ^ notInFlag)
							newList.add(p);
					}
			} else if (requestParams != null && requestParams.get("_lsc" + c.getFormCellId()) != null) {
				String[] lsc = requestParams.get("_lsc" + c.getFormCellId()).split(",");
				newList = new ArrayList<W5LookUpDetay>();
				for (String q : lsc) {
					newList.add(lookUp.get_detayMap().get(q));
				}
			} else {
				newList = new ArrayList<W5LookUpDetay>(oldList.size());
				for (W5LookUpDetay p : oldList)
					if ((rc.getValue() != null && p.getVal().equals(rc.getValue())) || p.getActiveFlag() != 0)
						newList.add(p);
				// newList = lookUp.get_detayList();
			}
			List<W5LookUpDetay> newList2 = new ArrayList<W5LookUpDetay>(newList.size());
			for (W5LookUpDetay ld : newList) {
				newList2.add(ld);
			}
			rc.setLookupListValues(newList2);
			break;
		case 7:
		case 15:
		case 59: // dynamic query, lovcombo, superbox
		case 23:
		case 24:
		case 55: // tree combo and treepanel
			Map paramMap = new HashMap();
			requestParams = UserUtil.getTableGridFormCellReqParams((String) scd.get("projectId"), c.getLookupQueryId(),
					userId, (String) scd.get("sessionId"), webPageId, tabId, -fcId);
			String includedParams = GenericUtil.filterExt(c.getLookupIncludedParams(), scd, requestParams, null)
					.toString();
			if (includedParams != null && includedParams.length() > 2) {
				String[] ar1 = includedParams.split("&");
				for (int it4 = 0; it4 < ar1.length; it4++) {
					String[] ar2 = ar1[it4].split("=");
					if (ar2.length == 2 && ar2[0] != null && ar2[1] != null)
						paramMap.put(ar2[0], ar2[1]);
				}
			}

			dao.checkTenant(scd);
			W5QueryResult lookupQueryResult = metadataLoader.getQueryResult(scd, c.getLookupQueryId());
			lookupQueryResult.setErrorMap(new HashMap());
			lookupQueryResult.setRequestParams(requestParams);
			lookupQueryResult.setOrderBy(lookupQueryResult.getQuery().getSqlOrderby());
			if (rc.getValue() != null && rc.getValue().length() > 0
					&& GenericUtil.hasPartInside("7,10,61", Short.toString(c.getControlType())))
				paramMap.put("pmust_load_id", rc.getValue());
			switch (lookupQueryResult.getQuery().getQueryType()) {
			case 12:
				lookupQueryResult.prepareTreeQuery(paramMap);
				break; // lookup tree query
			default:
				lookupQueryResult.prepareQuery(paramMap);
			}
			rc.setLookupQueryResult(lookupQueryResult);

			if (lookupQueryResult.getErrorMap().isEmpty()) {
				dao.runQuery(lookupQueryResult);
				if (tabId != null && lookupQueryResult.getQuery().getSourceObjectId() != 0) {
					Set<Integer> keys = UserUtil.getTableGridFormCellCachedKeys((String) scd.get("projectId"),
							lookupQueryResult.getQuery().getSourceObjectId(), userId, (String) scd.get("sessionId"),
							requestParams.get(".w"), tabId, -c.getFormCellId(), requestParams, true);
					for (Object[] o : lookupQueryResult.getData())
						keys.add(GenericUtil.uInt(o[1]));
				}
			}

			break;
		}
		return rc;
	}
	
	public M5ListResult getMListResult(Map<String, Object> scd, int listId, Map<String, String> requestParams) {
		return metadataLoader.getMListResult(scd, listId, requestParams, false);
	}

}
