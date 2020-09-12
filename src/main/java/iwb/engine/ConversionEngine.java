package iwb.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.exception.IWBException;
import iwb.model.db.W5Conversion;
import iwb.model.db.W5ConversionCol;
import iwb.model.db.W5ConvertedObject;
import iwb.model.db.W5Form;
import iwb.model.db.W5FormCell;
import iwb.model.db.W5Table;
import iwb.model.db.W5TableField;
import iwb.model.db.W5WsMethod;
import iwb.model.db.W5WsMethodParam;
import iwb.model.helper.W5QueuedActionHelper;
import iwb.model.result.W5FormResult;
import iwb.util.GenericUtil;

@Component
public class ConversionEngine {
	@Lazy
	@Autowired
	private PostgreSQL dao;

	@Lazy
	@Autowired
	private MetadataLoader metadataLoader;

	@Lazy
	@Autowired
	private CRUDEngine crudEngine;

	@Lazy
	@Autowired
	private RESTEngine restEngine;

	@Lazy
	@Autowired
	private GlobalScriptEngine scriptEngine;
	
	public boolean extFormConversion(W5FormResult formResult, String prefix, int action, Map<String, Object> scd,
			Map<String, String> requestParams, W5Table t, String ptablePk, boolean cleanConversion) {
		if ((action == 0 || action == 1 || action == 2)
				&& !GenericUtil.isEmpty(formResult.getForm().get_conversionList())) {
			Set<Integer> cnvSet = new HashSet();
			if (!cleanConversion)
				for (W5Conversion cnv : formResult.getForm().get_conversionList())
					if (
					/* (action==1 && cnv.getSynchOnUpdateFlag()!=0) || */ ((cnv.getConversionType() == 3 /* invisible */
							|| cnv.getConversionType() == 0 /* kesin */)
							&& GenericUtil.hasPartInside2(cnv.getActionTypes(), action))) {
						cnvSet.add(cnv.getConversionId());
					}
			String cnvStr = requestParams.get("_cnvStr");
			if (!GenericUtil.isEmpty(cnvStr)) {
				String[] arCnvStr = cnvStr.split(",");
				if (arCnvStr != null && arCnvStr.length > 0)
					for (String acs : arCnvStr) {
						int conversionId = GenericUtil.uInt(acs);
						if (conversionId != 0)
							cnvSet.add(conversionId);
					}
			}

			if (action == 1 || action == 0) { // conversion olan bir form? o
												// zaman sync olan covnerted
												// objeleri bul
				String inStr = "";
				if (!cleanConversion) {
					for (W5Conversion cnv : formResult.getForm().get_conversionList())
						if (GenericUtil.hasPartInside2(cnv.getActionTypes(), action)
								|| cnv.getSynchOnUpdateFlag() != 0) { // synch
																		// varsa
							inStr += "," + cnv.getConversionId();
						}
				} else
					for (Integer s : cnvSet) {
						inStr += "," + s;
					}

				if (inStr.length() > 1) {
					List<W5ConvertedObject> lco = dao.find(
							"from W5ConvertedObject x where x.projectUuid=?0 AND x.conversionId in ("
									+ inStr.substring(1) + ") and x.srcTablePk=?1",
							scd.get("projectId"),
							GenericUtil.uInt(requestParams, t.get_tableParamList().get(0).getDsc() + prefix));
					if (!lco.isEmpty()) {
						Map<Integer, List<W5ConvertedObject>> m = new HashMap();
						formResult.setMapConvertedObject(m);
						for (W5ConvertedObject co : lco) {
							List<W5ConvertedObject> l = m.get(co.getConversionId());
							if (l == null) {
								l = new ArrayList();
								m.put(co.getConversionId(), l);
							}
							l.add(co);
							cnvSet.add(co.getConversionId());
						}
					}
				}
			}

			List<Map<String, String>> previewConversionMapList = null;
			boolean convertedAny = false;
			W5Conversion c = null;
			for (int conversionId : cnvSet)
				try {
					c = null;
					for (W5Conversion cnv : formResult.getForm().get_conversionList())
						if (cnv.getConversionId() == conversionId) {
							c = cnv;
							break;
						}
					if (c == null)
						continue; // bulamadi
					// if(!PromisUtil.hasPartInside2(c.getActionTips(), action)
					// && (action!=1 ||
					// c.getSynchOnUpdateFlag()==0)) continue; // bulamadi veya
					// buldugunun action'i uygun
					// degil
					if (c.getSrcDstTip() == 0) { // Table -> Table
						if (formResult.getMapConvertedObject() != null) {
							List<W5ConvertedObject> lco = formResult.getMapConvertedObject().get(c.getConversionId());
							if (lco != null) { // dikkat daha once convert
												// edilmis object var
								if (c.getMaxNumofConversion() == 0 || c.getMaxNumofConversion() > lco.size()
										|| (!cleanConversion && c.getSynchOnUpdateFlag() != 0)) {
									if (c.getSynchOnUpdateFlag() != 0)
										for (W5ConvertedObject co : lco) { // bu
																			// conversion'da
																			// synch
																			// var
											//
											// formResult.getOutputMessages().add(PromisLocaleMsg.get2(0,(String)scd.get("locale"),"fw_conversion_auto_error")+
											// "
											// ["+PromisLocaleMsg.get2(scd,c.getDsc())+"]");
											int advancedConditionGroupId = 0;

											String prefix2 = "";
											Map<String, String> m = new HashMap();
											m.put("a", "1");
											m.put("_fid", "" + c.getDstFormId());
											W5Table dt = FrameworkCache.getTable(scd, c.getDstTableId());
											m.put(dt.get_tableParamList().get(0).getDsc() + prefix2,
													"" + co.getDstTablePk());
											W5FormResult dstFormResult = metadataLoader.getFormResult(scd, c.getDstFormId(), 1, m);
											Map<String, Object> mq = interprateConversionTemplate(c, dstFormResult,
													GenericUtil.uInt(ptablePk), false, true);
											if (GenericUtil.isEmpty(mq)) {
												formResult.getOutputMessages()
														.add(LocaleMsgCache.get2(0, (String) scd.get("locale"),
																"fw_conversion_sync_error") + "  ["
														+ LocaleMsgCache.get2(scd, c.getDsc()) + "]");
												continue;
											}
											for (String k : mq.keySet())
												if (!k.startsWith("_"))
													m.put(k + prefix2, mq.get(k).toString());

											crudEngine.postForm4Table(dstFormResult, prefix2, new HashSet());
											if (dstFormResult.getErrorMap().isEmpty()) {
												co.setVersionNo(co.getVersionNo() + 1);
												co.setVersionUserId((Integer) scd.get("userId"));
												dao.updateObject(co);

												if (FrameworkSetting.liveSyncRecord)
													formResult.addSyncRecordAll(
															dstFormResult.getListSyncAfterPostHelper());
											} else {
												// cok dogru degil
												// throw new
												// PromisException("security","Form",
												// 0, null,
												// PromisLocaleMsg.get2(0,(String)scd.get("locale"),"fw_conversion_auto_synch_error")+
												// " 2-
												// ["+PromisLocaleMsg.get2(scd,c.getDsc())+"]:
												// " +
												// PromisUtil.fromMapToHtmlString(dstFormResult.getErrorMap()),
												// null);
											}
										}
								} else {
									formResult.getOutputMessages()
											.add(LocaleMsgCache.get2(scd, "fw_conversion_msg_for") + " ["
													+ LocaleMsgCache.get2(scd, c.getDsc()) + "]: "
													+ LocaleMsgCache.get2(scd, "fw_max_number_of_conversion"));
									continue; // daha fazla conversion yok,
												// sinira ulasmis
								}
							}
						}
						if (!GenericUtil.hasPartInside2(c.getActionTypes(), action))
							continue; // bulamadi veya buldugunun action'i uygun
										// degil

						Map<String, String> m = new HashMap();
						m.put("_cnvId", "" + conversionId);
						// m.put("_cnvGroupId",""+advancedConditionGroupId);
						// m.put("_cnvDetailTableIds",""+advancedConditionGroupId);
						m.put("_fid", "" + c.getDstFormId());
						m.put("_cnvTblPk", ptablePk);
						m.put("_cnvDsc", c.getDsc());
						m.put(".w", requestParams.get(".w"));
						if (c.getPreviewFlag() != 0) { // simdi gonderilmeyecek,
														// formda geri donecek
							if (previewConversionMapList == null) {
								previewConversionMapList = new ArrayList();
								formResult.setPreviewConversionMapList(previewConversionMapList);
							}
							previewConversionMapList.add(m);
							continue;
						} else {
							m.put("a", "2");
							W5FormResult dstFormResult = metadataLoader.getFormResult(scd, c.getDstFormId(), 2, requestParams);
							Map mq = interprateConversionTemplate(c, dstFormResult, GenericUtil.uInt(ptablePk),
									false, false);
							if (GenericUtil.isEmpty(mq)) {
								formResult.getOutputMessages()
										.add(LocaleMsgCache.get2(0, (String) scd.get("locale"),
												"fw_conversion_auto_error") + " 1- ["
										+ LocaleMsgCache.get2(scd, c.getDsc()) + "]");
								continue;
							}
							m.putAll(mq);

							if (GenericUtil.isEmpty(c.getRhinoCode()) || c.getRhinoCode().startsWith("!")) {
								W5FormResult newFormResult = crudEngine.postForm4Table(scd, c.getDstFormId(), 2, m,
										prefix);
								if (newFormResult.getErrorMap().isEmpty()) {
									formResult.getOutputMessages()
											.add(LocaleMsgCache.get2(0, (String) scd.get("locale"),
													"fw_conversion_auto_done") + " ["
											+ LocaleMsgCache.get2(scd, c.getDsc()) + "]");
									convertedAny = true;

									if (FrameworkSetting.liveSyncRecord)
										formResult.addSyncRecordAll(newFormResult.getListSyncAfterPostHelper());
									if (!GenericUtil.isEmpty(c.getRhinoCode())) {
										if (!GenericUtil.isEmpty(newFormResult.getOutputFields()))
											for (String s : newFormResult.getOutputFields().keySet())
												if (newFormResult.getOutputFields().get(s) != null)
													m.put("out." + s,
															newFormResult.getOutputFields().get(s).toString());
										scriptEngine.executeScript(scd, requestParams, c.getRhinoCode(), m, "707r"+c.getConversionId());
									}
								} else {
									if (FrameworkSetting.debug)
										System.out.println(newFormResult.getErrorMap());
									if (c.getRowErrorStrategyType() == 1)
										throw new IWBException("security", "Form", 0, null,
												LocaleMsgCache.get2(0, (String) scd.get("locale"),
														"fw_conversion_auto_error") + "  2- ["
												+ LocaleMsgCache.get2(scd, c.getDsc()) + "]", null);
									else {
										formResult.getOutputMessages()
												.add(LocaleMsgCache.get2(0, (String) scd.get("locale"),
														"fw_conversion_auto_error") + "  3- ["
												+ LocaleMsgCache.get2(scd, c.getDsc()) + "]");
										/*
										 * if(previewConversionMapList==null){
										 * //TODO aslinda hata durumunda preivew
										 * acsin mi denilebilir. bir dusun
										 * previewConversionMapList= new
										 * ArrayList(); formResult.
										 * setPreviewConversionMapList(
										 * previewConversionMapList); }
										 * previewConversionMapList.add(m);
										 */
									}
								}
							} else {
								scriptEngine.executeScript(scd, requestParams, c.getRhinoCode(), m, "result");
							}
						}
					} else if (c.getSrcDstTip() == 1) { // Table -> Ws Method
						if (formResult.getMapConvertedObject() != null) {
							List<W5ConvertedObject> lco = formResult.getMapConvertedObject().get(c.getConversionId());
							if (lco != null && c.getMaxNumofConversion() > 0
									&& c.getMaxNumofConversion() <= lco.size()) { // yapilmayacak
								continue;
							}
						}

						W5WsMethod wsm = FrameworkCache.getWsMethod(scd, c.getDstTableId());
						if (wsm.get_params() == null) {//TODO
							wsm.set_params(dao.find(
									"from W5WsMethodParam t where t.wsMethodId=?0 AND t.projectUuid=?1 order by t.tabOrder",
									wsm.getWsMethodId(), (String) scd.get("projectId")));
						}

						Map mq = interprateConversionTemplate4WsMethod(scd, requestParams, c,
								GenericUtil.uInt(ptablePk), wsm);
						if (GenericUtil.isEmpty(mq)) {
							formResult.getOutputMessages()
									.add(LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_conversion_auto_error")
											+ " 1- [" + LocaleMsgCache.get2(scd, c.getDsc()) + "]");
							continue;
						}
						if (GenericUtil.isEmpty(c.getRhinoCode()) || c.getRhinoCode().startsWith("!")) {
							Map result = restEngine.REST(scd, wsm.get_ws().getDsc() + "." + wsm.getDsc(), mq); // TODO:
																									// result
																									// ne
																									// yapayim
							if (result != null) {
								if (result.containsKey("faultcode"))
									throw new IWBException("rhino", "Conversion", c.getConversionId(),
											wsm.get_ws().getDsc() + "." + wsm.getDsc(),
											result.get("faultcode") + ": " + result.get("faultstring"), null);
								else {
									if (formResult.getOutputFields() == null)
										formResult.setOutputFields(new HashMap());
									formResult.getOutputFields().putAll(result);
								}
								mq.putAll(result);
							}
						}
						if (!GenericUtil.isEmpty(c.getRhinoCode())) {
							scriptEngine.executeScript(scd, requestParams, c.getRhinoCode(), mq, "result");
						}
					}
				} catch (IWBException e) {
					if (c.getRowErrorStrategyType() == 1 && cleanConversion) // throw
																			// e;
						throw new IWBException("framework", "Conversion", conversionId, null,
								"[707," + conversionId + "] " + c.getDsc(), e);
					// if(FrameworkSetting.debug)e.printStackTrace();
					formResult.getOutputMessages().add("CONVERSION(" + conversionId + ") EXCEPTION: " + e.getMessage());
				} catch (Exception e) {
					if (c.getRowErrorStrategyType() == 1 && cleanConversion)
						throw new IWBException("framework", "Conversion", conversionId, null,
								"[707," + conversionId + "] " + c.getDsc(), e);
					// throw new IWBException("framework","Conversion",
					// conversionId, null,
					// LocaleMsgCache.get2(0,(String)scd.get("locale"),"fw_conversion_auto_error")+
					// " 3-
					// ["+e.getMessage()+"]", null);
					// if(FrameworkSetting.debug)e.printStackTrace();
					formResult.getOutputMessages().add("CONVERSION(" + conversionId + ") EXCEPTION: " + e.getMessage());
				}
			return convertedAny;
		} else
			return false;
	}
	
	public W5FormResult postBulkConversion(Map<String, Object> scd, int conversionId, int dirtyCount,
			Map<String, String> requestParams, String prefix) {
		W5Conversion cnv = null;
		try {
			String projectId = (String) scd.get("projectId");
			if (/* customizationId > 0 && */scd != null && (Integer) scd.get("roleId") == 0)
				projectId = FrameworkCache.getProjectId(scd, "707." + conversionId);
			cnv = FrameworkCache.getConversion(scd, conversionId);
			if(!FrameworkSetting.redisCache && cnv == null){
				cnv = (W5Conversion) metadataLoader.getMetadataObject(
					"W5Conversion","conversionId", conversionId, projectId, null);
			}
			if (cnv == null || GenericUtil.isEmpty(cnv.getActionTypes()) || cnv.getActiveFlag() == 0)
				throw new IWBException("validation", "Conversion", conversionId, null,
						"Conversion Control Error (" + conversionId + ")", null);
			Set<String> checkedParentRecords = new HashSet();

			if (GenericUtil.hasPartInside2(cnv.getActionTypes(), 0)) { // manual
																		// conversion
				int srcFormId = cnv.getSrcFormId();
				W5FormResult formResult = metadataLoader.getFormResult(scd, srcFormId, 2, requestParams);
				W5Table t = FrameworkCache.getTable(scd, formResult.getForm().getObjectId()); // formResult.getForm().get_sourceTable();
				int convertedCount = 0, errorConversionCount = 0;
				for (int id = 1; id <= dirtyCount; id++) {
					Map<String, String> m = new HashMap();
					m.put("_cnvStr", conversionId + "");
					m.put(t.get_tableParamList().get(0).getDsc(), requestParams.get("srcTablePk" + prefix + id));
					m.put(".w", requestParams.get(".w"));
					String pid = prefix + id;
					for (String k : requestParams.keySet())
						if (!k.startsWith("srcTablePk") && k.endsWith(pid)) { // veli,
																				// eger
																				// request'ten
																				// gelen
																				// extra
																				// parametreler
																				// varsa,
																				// onlari
																				// da
																				// yeni
							// map'e koy
							m.put(k.substring(0, k.length() - pid.length()), requestParams.get(k));
						}
					boolean b = extFormConversion(formResult, prefix, 0, scd, m, t,
							requestParams.get("srcTablePk" + prefix + id), true);
					if (!b || !formResult.getErrorMap().isEmpty()) {
						if (cnv.getRowErrorStrategyType() == 1) {
							String msg = LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_conversion_auto_error")
									+ "  2- [" + LocaleMsgCache.get2(scd, cnv.getDsc()) + "]";
							if (!GenericUtil.isEmpty(formResult.getOutputMessages()))
								for (String s : formResult.getOutputMessages()) {
									msg += "<br> - " + s;
								}
							throw new IWBException("security", "Form", 0, null, msg, null);
						} else {
							//
							// formResult.getOutputMessages().add(PromisLocaleMsg.get2(0,(String)scd.get("locale"),"fw_conversion_auto_error")+
							// " 3-
							// ["+PromisLocaleMsg.get2(scd,cnv.getDsc())+"]");
							errorConversionCount++;
						}
						formResult.getErrorMap().clear();
					} else
						convertedCount++;
				}
				if (!GenericUtil.isEmpty(formResult.getOutputMessages()))
					formResult.getOutputMessages().add("-");
				formResult.getOutputMessages().add(convertedCount + " "
						+ LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_records_converted"));
				if (errorConversionCount > 0)
					formResult.getOutputMessages().add(errorConversionCount + " "
							+ LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_records_not_converted"));
				formResult.setQueuedActionList(new ArrayList<W5QueuedActionHelper>());
				return formResult;
			} else if (GenericUtil.hasPartInside2(cnv.getActionTypes(), 3)) { // bulk
																				// conversion

				List<W5QueuedActionHelper> queuedGlobalFuncList = new ArrayList<W5QueuedActionHelper>();

				int dstFormId = cnv.getDstFormId();
				W5FormResult formResult = metadataLoader.getFormResult(scd, dstFormId, 2, requestParams);
				W5Table t = FrameworkCache.getTable(scd, formResult.getForm().getObjectId()); // formResult.getForm().get_sourceTable();
				if (t.getAccessViewTip() == 0 && !FrameworkCache.roleAccessControl(scd, 0)) {
					throw new IWBException("security", "Module", 0, null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_module_control"), null);
				}
				if (t.getAccessViewUserFields() == null && !GenericUtil.accessControl(scd, t.getAccessViewTip(),
						t.getAccessViewRoles(), t.getAccessViewUsers())) {
					throw new IWBException("security", "Form", dstFormId, null,
							LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_security_table_control_view"),
							null);
				}
				Map originalRequestParams = formResult.getRequestParams();
				for (int id = 1; id <= dirtyCount; id++) {
					// formResult.setAction(PromisUtil.uInt(requestParams.get("a"+prefix+id)));
					int srcTablePk = GenericUtil.uInt(requestParams, "srcTablePk" + prefix + id);
					Map mq = interprateConversionTemplate(cnv, formResult, srcTablePk, false, false);
					if (!GenericUtil.isEmpty(mq)) {
						mq.put("_cnvId", "" + conversionId);
						mq.put("_cnvTblPk", "" + srcTablePk);
						formResult.setRequestParams(mq);
						queuedGlobalFuncList.addAll(crudEngine.postForm4Table(formResult, prefix, checkedParentRecords));
						formResult.setRequestParams(originalRequestParams);
						if (!formResult.getErrorMap().isEmpty()) {
							throw new IWBException("validation", "Form", dstFormId, null,
									"Detail Conversion Validation Error("
											+ LocaleMsgCache.get2((Integer) scd.get("customizationId"),
													(String) scd.get("locale"), formResult.getForm().getLocaleMsgKey())
											+ "): " + GenericUtil.fromMapToJsonString(formResult.getErrorMap()),
									null);
						}

					} else
						throw new IWBException("validation", "Conversion", conversionId, null,
								"Detay Conversion Hatası", null);
				}

				formResult.setQueuedActionList(queuedGlobalFuncList);
				if (formResult.getOutputMessages() != null && formResult.getOutputMessages().isEmpty())
					formResult.getOutputMessages().add("Total " + dirtyCount + " records processed.");
				return formResult;
			} else
				throw new IWBException("validation", "Conversion", conversionId, null,
						"Conversion Control Error2 (" + conversionId + ")", null);
		} catch (Exception e) {
			throw new IWBException("framework", "Bulk.Conversion", conversionId, "",
					"[707," + conversionId + "]" + (cnv != null ? " " + cnv.getDsc() : ""), e);
		}
	}
	
	public W5FormResult postBulkConversionMulti(Map<String, Object> scd, int conversionCount,
			Map<String, String> parameterMap) {
		W5FormResult result = null;
		for (int i = 1; i <= conversionCount; i++) {
			int cnvId = GenericUtil.uInt(parameterMap, "_cnvId" + i);
			int dirtyCount = GenericUtil.uInt(parameterMap, "_cnt" + i);

			result = postBulkConversion(scd, cnvId, dirtyCount, parameterMap, i + ".");
		}
		return result;
	}
	

	public Map interprateConversionTemplate(W5Conversion c, W5FormResult dstFormResult, int conversionTablePk,
			boolean checkCondition, boolean onlyForSynch) {
		Map<String, Object> scd = dstFormResult.getScd();
		int customizationId = (Integer) scd.get("customizationId");
		Map<String, String> requestParams = new HashMap();
		requestParams.putAll(dstFormResult.getRequestParams());
		requestParams.put("_conversion_table_pk", "" + conversionTablePk);
		W5FormResult srcFormResult = metadataLoader.getFormResult(scd, c.getSrcFormId(), 2, requestParams);
		W5Form sf = srcFormResult.getForm();
		W5Form df = dstFormResult.getForm();
		int conversionTableId = sf.getObjectId();
		if (GenericUtil.isEmpty(sf.get_conversionList()))
			return null;
		if (c.getDstFormId() != df.getFormId())
			return null; // boyle bir forma donus yok
		if (c.get_conversionColList() == null)
			for (W5Conversion co : sf.get_conversionList())
				if (co.getConversionId() == c.getConversionId()) {
					c.set_conversionColList(co.get_conversionColList());
					break;
				}
		if (c.get_conversionColList() == null)
			return null;
		Map<String, Object> m = new HashMap<String, Object>();
		if (checkCondition && !GenericUtil.isEmpty(c.getConditionSqlCode())) { // TODO
			boolean b = dao.conditionRecordExistsCheck(scd, dstFormResult.getRequestParams(), sf.getObjectId(),
					conversionTablePk, c.getConditionSqlCode());
			if (!b)
				return null;
		}
		// TODO: source kayda yetki kontrolu olacak

		for (W5ConversionCol cCol : c.get_conversionColList())
			if (!onlyForSynch || cCol.getSynchFlag() != 0) {
				if (!GenericUtil.isEmpty(cCol.getConversionCode())) {
					String cc = cCol.getConversionCode();
					switch (cCol.getFieldConversionTip()) {
					case 2: // SQL
						Map<String, Object> sqlm = dao.runSQLQuery2Map(cc, scd, requestParams, null);
						cc = null;
						if (!GenericUtil.isEmpty(sqlm)) {
							Object oo = null;
							if (sqlm.containsKey("result")) {
								oo = sqlm.get("result");
							} else
								oo = sqlm.values().toArray()[0];
							if (oo != null)
								cc = oo.toString();
						}
						// cc = GenericUtil.isEmpty(sqlm) ? null :
						// sqlm.get("result").toString();
						break;
					case 6: // Rhino
						Object result = scriptEngine.executeScript(dstFormResult.getScd(),
								dstFormResult.getRequestParams(), cc, null, "708c"+cCol.getConversionColId());
						if (result != null)
							cc = result.toString();
						break;
					case 1: // not implemented
					default:
						if (cc.contains("${")) {
							StringBuilder tmp1 = new StringBuilder(cc);
							dao.interprateTemplate(scd, dstFormResult.getRequestParams(), conversionTableId,
									conversionTablePk, tmp1, true, 0, cCol.getFieldConversionTip());
							cc = tmp1.toString();
						}
					}
					for (W5FormCell fc : df.get_formCells())
						if (cCol.getFormCellId() == fc.getFormCellId()) { // buna
																			// donusecek
							if (fc.get_sourceObjectDetail() == null)
								break;
							short fieldTip = ((W5TableField) fc.get_sourceObjectDetail()).getFieldType();
							Object ob = GenericUtil.getObjectByTip(cc, fieldTip);
							if (ob != null)
								m.put(fc.getDsc(), fieldTip == 2 /* date? */ ? cc : ob.toString());
							break;
						}
				}
			}
		m.put("_cnv", c);
		m.put("_cnv_name", c.getDsc());
		m.put("_cnv_src_tbl_id", "" + c.getSrcTableId());
		m.put("_cnv_src_tbl_pk", "" + conversionTablePk);
		m.put("_cnv_src_frm_id", "" + sf.getFormId());
		m.put("_cnv_record", dao.getSummaryText4Record(scd, c.getSrcTableId(), conversionTablePk));
		return m;
	}
	

	public Map interprateConversionTemplate4WsMethod(Map<String, Object> scd, Map<String, String> requestParams,
			W5Conversion c, int conversionTablePk, W5WsMethod wsm) {
		requestParams.put("_conversion_table_pk", "" + conversionTablePk);
		int conversionTableId = c.getSrcTableId();
		Map<String, Object> m = new HashMap<String, Object>();

		for (W5ConversionCol cCol : c.get_conversionColList()) {
			if (!GenericUtil.isEmpty(cCol.getConversionCode())) {
				String cc = cCol.getConversionCode();
				switch (cCol.getFieldConversionTip()) {
				case 2: // SQL
					Map<String, Object> sqlm = dao.runSQLQuery2Map(cc, scd, requestParams, null);
					cc = GenericUtil.isEmpty(sqlm) ? null : sqlm.get("result").toString();
				case 6: // Rhino
					Object result = scriptEngine.executeScript(scd, requestParams, cc, null, "708c"+cCol.getConversionColId());
					if (result != null)
						cc = result.toString();
					break;
				case 1: // not implemented
				default:
					if (cc.contains("${")) {
						StringBuilder tmp1 = new StringBuilder(cc);
						dao.interprateTemplate(scd, requestParams, conversionTableId, conversionTablePk, tmp1, true, 0,
								cCol.getFieldConversionTip());
						cc = tmp1.toString();
					}
				}
				for (W5WsMethodParam wsmp : wsm.get_params())
					if (cCol.getFormCellId() == wsmp.getWsMethodParamId()) { // buna
																				// donusecek
						Object ob = GenericUtil.getObjectByTip(cc, wsmp.getParamType());
						if (ob != null)
							m.put(wsmp.getDsc(), wsmp.getParamType() == 2 /* date? */ ? cc : ob.toString());
						break;
					}
			}
		}
		return m;
	}
}
