package iwb.engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

//import org.graalvm.polyglot.Context;
//import org.graalvm.polyglot.Value;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import iwb.cache.FrameworkCache;
import iwb.cache.FrameworkSetting;
import iwb.cache.LocaleMsgCache;
import iwb.custom.trigger.GlobalFuncTrigger;
import iwb.dao.metadata.MetadataLoader;
import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.exception.IWBException;
import iwb.model.db.Log5GlobalFuncAction;
import iwb.model.db.W5GlobalFuncParam;
import iwb.model.db.W5LookUp;
import iwb.model.db.W5LookUpDetay;
import iwb.model.db.W5Query;
import iwb.model.db.W5QueryField;
import iwb.model.db.W5QueryParam;
import iwb.model.db.W5Table;
import iwb.model.db.W5TableEvent;
import iwb.model.db.W5WsMethod;
import iwb.model.db.W5WsMethodParam;
import iwb.model.result.W5FormResult;
import iwb.model.result.W5GlobalFuncResult;
import iwb.model.result.W5QueryResult;
import iwb.script.NashornScript;
import iwb.util.GenericUtil;
import iwb.util.NashornUtil;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

@Component
public class GlobalScriptEngine {
	private static boolean useNashorn = true;

	@Lazy
	@Autowired
	private PostgreSQL dao;

	@Lazy
	@Autowired
	private MetadataLoader metadataLoader;

	@Lazy
	@Autowired
	private ConversionEngine conversionEngine;

	@Lazy
	@Autowired
	private QueryEngine queryEngine;

	@Lazy
	@Autowired
	private WorkflowEngine workflowEngine;

	@Lazy
	@Autowired
	private AccessControlEngine acEngine;

	@Lazy
	@Autowired
	private CRUDEngine crudEngine;

	@Lazy
	@Autowired
	private RESTEngine restEngine;

	@Lazy
	@Autowired
	private NotificationEngine notyEngine;
	
	public PostgreSQL getDao() {
		return dao;
	}

	public ConversionEngine getConversionEngine() {
		return conversionEngine;
	}

	public QueryEngine getQueryEngine() {
		return queryEngine;
	}

	public CRUDEngine getCrudEngine() {
		return crudEngine;
	}

	public RESTEngine getRestEngine() {
		return restEngine;
	}
	
	
	public NotificationEngine getNotyEngine() {
		return notyEngine;
	}



	private ScriptEngine nashornEngine = null;
	//private Context polyglot = null;

	public W5GlobalFuncResult executeGlobalFunc(Map<String, Object> scd, int globalFuncId,
			Map<String, String> parameterMap, short accessSourceType) {

		W5GlobalFuncResult r = metadataLoader.getGlobalFuncResult(scd, globalFuncId);
		if (!GenericUtil.isEmpty(r.getGlobalFunc().getAccessSourceTypes())
				&& !GenericUtil.hasPartInside2(r.getGlobalFunc().getAccessSourceTypes(), accessSourceType))
			throw new IWBException("security", "GlobalFunc", globalFuncId, null, "Access Source Type Control", null);

		dao.checkTenant(scd);
		r.setErrorMap(new HashMap());
		r.setRequestParams(parameterMap);
		GlobalFuncTrigger.beforeExec(r, "");

		Log5GlobalFuncAction action = new Log5GlobalFuncAction(r);
		String error = null;

		boolean hasOutParam = false;
		Object requestJson = r.getRequestParams().get("_json");
		String script = r.getGlobalFunc().getCode();
		if (script.length() > 0 && script.charAt(0) == '!')
			script = script.substring(1);
		List<Object> params = new ArrayList();

		switch (r.getGlobalFunc().getLkpCodeType()) {
		case 1:// NashornJS
			if (nashornEngine == null)
				nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
			Object nobj = FrameworkCache.getGraalFunc(scd, "20." + globalFuncId);
			String fncName = null;
			if (nobj == null)
				try {
					fncName = "fnc_" + (r.getScd() != null && r.getScd().get("projectId") != null
							? r.getScd().get("projectId").toString().replace('-', '_')
							: "xxx") + "_" + r.getGlobalFuncId();
					StringBuilder sb = new StringBuilder();
					StringBuilder sbPost1 = new StringBuilder(), sbPost2 = new StringBuilder();
					sb.append("function ").append(fncName).append("($, _scd, _request");
					if (!GenericUtil.isEmpty(r.getGlobalFunc().get_dbFuncParamList())) {
						for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
							if (p1.getOutFlag() == 0) {
								sb.append(",").append(p1.getDsc());
							} else {
								hasOutParam = true;
								sbPost1.append(p1.getDsc()).append("=null, ");
								sbPost2.append(p1.getDsc()).append(":").append(p1.getDsc()).append(", ");
							}
					}
					if (requestJson != null && requestJson instanceof JSONObject) {
						sb.append(",json");
						r.getRequestParams().remove("_json");
					} else
						requestJson = null;

					sb.append("){\n");
					if (hasOutParam) {
						sbPost1.setLength(sbPost1.length() - 2);
						sbPost2.setLength(sbPost2.length() - 2);
						sb.append("var ").append(sbPost1).append(";\n").append(script).append("\nreturn {")
								.append(sbPost2).append("}");

					} else
						sb.append(script);
					sb.append("\n}");
					script = sb.toString();
					nashornEngine.eval(script);
					FrameworkCache.addGraalFunc(scd, "20." + globalFuncId, fncName);
				} catch (Exception ge) {
					String msg = getErrorLineNumberAsMsg(ge);
					dao.logGlobalFuncAction(action, r, error);
					throw new IWBException("rhino", "NashornGlobalFunc.Compile", r.getGlobalFuncId(), script,
							"[20," + r.getGlobalFuncId() + "] " + r.getGlobalFunc().getDsc() + msg, ge);
				}
			else if (nobj instanceof String) {
				fncName = nobj.toString();
			}
			params.add(new NashornScript(r.getScd(), r.getRequestParams(), this));
			params.add(scd);
			params.add(parameterMap);

			if (requestJson != null && requestJson instanceof JSONObject) {
				try {
					params.add(GenericUtil.fromJSONObjectToMap((JSONObject) requestJson));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (!GenericUtil.isEmpty(r.getGlobalFunc().get_dbFuncParamList())) {
				for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
					if (p1.getOutFlag() == 0) {
						Object o = GenericUtil.prepareParam(p1, r.getScd(), r.getRequestParams(), (short) -1, null,
								(short) 0, p1.getSourceType() == 1 ? p1.getDsc() : null, null, r.getErrorMap());
						if (o == null)
							params.add(null);
						else if ((o instanceof Integer) || (o instanceof Double) || (o instanceof BigDecimal)
								|| (o instanceof Boolean))
							params.add(o);
						else if ((o instanceof Date))
							params.add(GenericUtil.uFormatDate((Date) o));
						else
							params.add(o);
					} else
						hasOutParam = true;
			}

			try {
				Object funcResult = ((Invocable) nashornEngine).invokeFunction(fncName, params.toArray(new Object[0]));

				if (hasOutParam) {
					// JSONObject jo=new JSONObject();
					Map<String, Object> res = new HashMap<String, Object>();
					if (funcResult != null && funcResult instanceof ScriptObjectMirror) {
						Map resultMap = NashornUtil.fromScriptObject2Map((ScriptObjectMirror) funcResult);
						for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
							if (p1.getOutFlag() != 0 && resultMap.containsKey(p1.getDsc())) {
								Object em = resultMap.get(p1.getDsc());
								if (em != null) {
									String v = em.toString();
									if (p1.getParamType() == 4 && v.endsWith(".0"))
										v = v.substring(0, v.length() - 2);
									res.put(p1.getDsc(), GenericUtil.getObjectByTip(v, p1.getParamType()));
								}
							}
					}
					r.setResultMap(res);
				}
				r.setSuccess(true);
			} catch (Exception ge) {
				String msg = getErrorLineNumberAsMsg(ge);
				dao.logGlobalFuncAction(action, r, error);
				if(!script.startsWith("function "))script="function "+fncName+"($, _scd, _request /* and other params*/) {\n"+script+"\n}";
				throw new IWBException("rhino", "NashornGlobalFunc.Run", r.getGlobalFuncId(), script,
						"[20," + r.getGlobalFuncId() + "] " + r.getGlobalFunc().getDsc() + msg, ge);
			}

			break;
		case 13:// Python
			throw new IWBException("rhino", "Graal Python", 0, script, "Python Not Implemented yet", null);
//			break;
		case 14:// Java Groovy
			throw new IWBException("rhino", "Java Groovy", 0, script, "Java Groovy Not Implemented yet", null);
//			break;

		case 12:// R
//			throw new IWBException("rhino", "Graal R", 0, script,"R Not Implemented yet", null);
//			break;
		case 11:// GraalJS
	/*		if (polyglot == null)
				try {// "js"
					polyglot = Context.newBuilder().allowAllAccess(true).allowIO(true).build();

					String rPre = FrameworkCache.getAppSettingStringValue(scd, "r_prerequisites");
					if (!GenericUtil.isEmpty(rPre)) {
						polyglot.eval("R", rPre);
					}
				} catch (Exception ee) {
					throw new IWBException("rhino", "GraalVM polyglot", 0, null, "GraalVM creation error", ee);
				}
			Value func = (Value) FrameworkCache.getGraalFunc(scd, "20." + globalFuncId);
			String lang = "js";
			if (func == null)
				try {
					StringBuilder sb = new StringBuilder();
					StringBuilder sbPost1 = new StringBuilder(), sbPost2 = new StringBuilder();
					switch (r.getGlobalFunc().getLkpCodeType()) {
					case 11:// JS
						sb.append("(function($, _scd, _request");
						if (!GenericUtil.isEmpty(r.getGlobalFunc().get_dbFuncParamList())) {
							for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
								if (p1.getOutFlag() == 0) {
									sb.append(",").append(p1.getDsc());
								} else {
									hasOutParam = true;
									sbPost1.append(p1.getDsc()).append("=null, ");
									sbPost2.append(p1.getDsc()).append(":").append(p1.getDsc()).append(", ");
								}
						}
						if (requestJson != null && requestJson instanceof JSONObject) {
							sb.append(",json");
							r.getRequestParams().remove("_json");
						} else
							requestJson = null;

						sb.append("){\n");
						if (hasOutParam) {
							sbPost1.setLength(sbPost1.length() - 2);
							sbPost2.setLength(sbPost2.length() - 2);
							sb.append("var ").append(sbPost1).append(";\n").append(script).append("\nreturn {")
									.append(sbPost2).append("}");

						} else
							sb.append(script);
						sb.append("\n})");
						break;
					case 12:// R
						lang = "R";
						sb.append("function(x, scd, request");
						if (!GenericUtil.isEmpty(r.getGlobalFunc().get_dbFuncParamList())) {
							for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
								if (p1.getOutFlag() == 0) {
									sb.append(",").append(p1.getDsc());
								} else {
									hasOutParam = true;
									sbPost1.append(p1.getDsc()).append(" <- ").append(p1.getParamTip() > 2 ? "0" : "''")
											.append(" \n");
									sbPost2.append("\"").append(p1.getDsc()).append("\" = ").append(p1.getDsc())
											.append(", ");
								}
						}
						if (requestJson != null && requestJson instanceof JSONObject) {
							sb.append(",json");
							r.getRequestParams().remove("_json");
						} else
							requestJson = null;

						sb.append("){\n");
						if (hasOutParam) {
							sbPost2.setLength(sbPost2.length() - 2);
							sb.append(sbPost1).append(script).append("\nreturn (list(").append(sbPost2).append("))");

						} else
							sb.append(script);
						sb.append("\n}");
					}
					script = sb.toString();
					func = polyglot.eval(lang, script);
					FrameworkCache.addGraalFunc(scd, "20." + globalFuncId, func);
				} catch (Exception ge) {
					ge.printStackTrace();
					dao.logGlobalFuncAction(action, r, error);
					throw new IWBException("rhino", "GraalGlobalFunc.Compile." + lang, r.getGlobalFuncId(), script,
							"[20," + r.getGlobalFuncId() + "] " + r.getGlobalFunc().getDsc(), ge);
				}

			params.add(new GraalScript(r.getScd(), r.getRequestParams(), this));
			params.add(scd);
			params.add(parameterMap);

			if (requestJson != null && requestJson instanceof JSONObject) {
				params.add(requestJson);
			} else if (!GenericUtil.isEmpty(r.getGlobalFunc().get_dbFuncParamList())) {
				for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
					if (p1.getOutFlag() == 0) {
						Object o = GenericUtil.prepareParam(p1, r.getScd(), r.getRequestParams(), (short) -1, null,
								(short) 0, p1.getSourceTip() == 1 ? p1.getDsc() : null, null, r.getErrorMap());
						if (o == null)
							params.add(null);
						else if ((o instanceof Integer) || (o instanceof Double) || (o instanceof BigDecimal)
								|| (o instanceof Boolean))
							params.add(o);
						else if ((o instanceof Date))
							params.add(GenericUtil.uFormatDate((Date) o));
						else
							params.add(o);
					} else
						hasOutParam = true;
			}
			try {
				Object funcResult = func.execute(params.toArray(new Object[0]));

				if (hasOutParam) {
					// JSONObject jo=new JSONObject();
					Map<String, String> res = new HashMap<String, String>();
					if (funcResult != null && funcResult instanceof Value) {
						Map resultMap = ScriptUtil.fromGraalValue2Map((Value) funcResult);

						r.setResultMap(resultMap);
					} else
						r.setResultMap(res);
				}
				r.setSuccess(true);
			} catch (Exception ge) {
				dao.logGlobalFuncAction(action, r, error);
				throw new IWBException("rhino", "GraalGlobalFunc.Run." + lang, r.getGlobalFuncId(), script,
						"[20," + r.getGlobalFuncId() + "] " + r.getGlobalFunc().getDsc(), ge);
			}
*/
			break;
		case 99: // DB
			dao.executeDbFunc(r, "");

		}

		if (r.getErrorMap().isEmpty()) { // sorun yok
			// post sms
			if (!GenericUtil.isEmpty(r.getResultMap()))for(String k:r.getResultMap().keySet())
				parameterMap.put(k,r.getResultMap().get(k)!=null?r.getResultMap().get(k).toString():null);

		}
		GlobalFuncTrigger.afterExec(r, "");

		/*
		 * switch (globalFuncId) { case -478: // reload locale msg cache for (Object[] m
		 * : (List<Object[]>) dao.executeSQLQuery(
		 * "select locale, locale_msg_key, dsc from iwb.w5_locale_msg where locale_msg_key=? AND customization_id=?"
		 * , parameterMap.get("plocale_msg_key"), scd.get("customizationId"))) {
		 * LocaleMsgCache.set2((Integer) scd.get("customizationId"), (String) m[0],
		 * (String) m[1], (String) m[2]); } }
		 */

		return r;
	}

	public W5GlobalFuncResult postEditGridGlobalFunc(Map<String, Object> scd, int globalFuncId, int dirtyCount,
			Map<String, String> requestParams, String paramSuffix) {

		W5GlobalFuncResult globalFuncResult = metadataLoader.getGlobalFuncResult(scd, globalFuncId);
		if (!GenericUtil.isEmpty(globalFuncResult.getGlobalFunc().getAccessSourceTypes())
				&& !GenericUtil.hasPartInside2(globalFuncResult.getGlobalFunc().getAccessSourceTypes(), 1))
			throw new IWBException("security", "GlobalFunc", globalFuncId, null, "Access Restrict Type Control", null);


		globalFuncResult.setErrorMap(new HashMap());
		globalFuncResult.setRequestParams(requestParams);

		for (int id = 1; id <= dirtyCount; id++) {
			if (globalFuncResult.getGlobalFunc().getLkpCodeType() == 99) {
				GlobalFuncTrigger.beforeExec(globalFuncResult, paramSuffix + id);
				dao.executeDbFunc(globalFuncResult, paramSuffix + id);
				GlobalFuncTrigger.afterExec(globalFuncResult, paramSuffix + id);

			} else {
				Map<String, String> newRequestParams = new HashMap();
				newRequestParams.putAll(requestParams);
				for (String key : requestParams.keySet())
					if (key.endsWith(paramSuffix + id)) {
						newRequestParams.put(key.substring(0, key.length() - (paramSuffix + id).length()),
								requestParams.get(key));
					}
				W5GlobalFuncResult res2 = executeGlobalFunc(scd, globalFuncId, newRequestParams, (short) 1);
				globalFuncResult.setSuccess(res2.isSuccess());
				globalFuncResult.setErrorMap(res2.getErrorMap());
			}

			if (!globalFuncResult.getErrorMap().isEmpty() || !globalFuncResult.isSuccess()) {
				throw new IWBException("validation", "GlobalFunc", -globalFuncId,
						GenericUtil.fromMapToHtmlString(globalFuncResult.getErrorMap()), "Detail Grid Validation",
						null);
			}
		}

		return globalFuncResult;
	}

	public void executeTableEvent(W5TableEvent ta, W5FormResult formResult, String action, Map<String, Object> scd,
			Map<String, String> requestParams, W5Table t, String ptablePk) {
		String msg = null;
		if (useNashorn) {
			if (nashornEngine == null)
				nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
			Object nobj = FrameworkCache.getGraalFunc(scd, "1209." + ta.getTableTriggerId());

			String scrName = null, script = ta.getTriggerCode();
			if (nobj == null)
				try {
					scrName = "te_" + (scd != null && scd.get("projectId") != null
							? scd.get("projectId").toString().replace('-', '_')
							: "xxx") + "_1209_" + ta.getTableTriggerId();
					StringBuilder sb = new StringBuilder();
					sb.append("function ").append(scrName).append("($, _scd, _request, triggerAction, ")
							.append(t.get_tableFieldList().get(0).getDsc());
					sb.append("){\n");
					sb.append(ta.getTriggerCode());
					if (ta.getTriggerCode().indexOf("result") > -1)
						sb.append("\nreturn result;");
					sb.append("\n}");
					script = sb.toString();
					nashornEngine.eval(script);
					FrameworkCache.addGraalFunc(scd, "1209." + ta.getTableTriggerId(), scrName);
				} catch (Exception ge) {
					String msgx = getErrorLineNumberAsMsg(ge);
//				dao.logGlobalFuncAction(action, r, error);
					throw new IWBException("rhino", "NashornTableEvent.Compile", ta.getTableTriggerId(), script,
							"[1209," + ta.getTableTriggerId() + "] " + ta.getDsc() + msgx, ge);
				}
			else if (nobj instanceof String) {
				scrName = nobj.toString();
			}

			try {
				Object funcResult = ((Invocable) nashornEngine).invokeFunction(scrName,
						new NashornScript(scd, requestParams, this), scd, requestParams, action,
						GenericUtil.isEmpty(ptablePk) ? null : ptablePk);
				if (funcResult != null && funcResult instanceof Boolean && (Boolean) funcResult == false)
					funcResult = null;

				if (funcResult != null) {
					msg = funcResult instanceof Boolean ? LocaleMsgCache.get2(scd, ta.getDsc()) : funcResult.toString();
				}

			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				String msgx = getErrorLineNumberAsMsg(ge);
				if(!script.startsWith("function "))script="function "+scrName+"($, _scd, _request, triggerAction /* and other params*/) {\n"+script+"\n}";
				throw new IWBException("rhino", "NashornTableEvent.Run", ta.getTableTriggerId(), script,
						"[1209," + ta.getTableTriggerId() + "] " + ta.getDsc() + msgx, ge);
			}

		} else {
/*			if (polyglot == null)
				polyglot = Context.create();// newBuilder("js").allowHostAccess(true).build();

			Value func = (Value) FrameworkCache.getGraalFunc(scd, "1209." + ta.getTableTriggerId());

			String scrName = null, script = null;
			if (func == null)
				try {
					scrName = "te_" + (scd != null && scd.get("projectId") != null
							? scd.get("projectId").toString().replace('-', '_')
							: "xxx") + "_1209_" + ta.getTableTriggerId();
					StringBuilder sb = new StringBuilder();
					sb.append("function ").append(scrName).append("($, _scd, _request, triggerAction, ")
							.append(t.get_tableFieldList().get(0).getDsc());
					sb.append("){\n");
					sb.append(ta.getTriggerCode());
					if (ta.getTriggerCode().indexOf("result") > -1)
						sb.append("\nreturn result;");
					sb.append("\n}");
					script = sb.toString();
					func = polyglot.eval("js", script);
					FrameworkCache.addGraalFunc(scd, "1209." + ta.getTableTriggerId(), func);
				} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
					throw new IWBException("rhino", "GraalTableEvent.Compile", ta.getTableTriggerId(), script,
							"[1209," + ta.getTableTriggerId() + "] " + ta.getDsc(), ge);
				}

			try {
				Value funcResult = func.execute(new GraalScript(scd, requestParams, this), scd, requestParams, action,
						GenericUtil.isEmpty(ptablePk) ? null : ptablePk);

				if (funcResult != null && funcResult.isBoolean() && funcResult.asBoolean() == false)
					funcResult = null;

				if (funcResult != null) {
					msg = funcResult.toString();
				}

			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				throw new IWBException("rhino", "GraalTableEvent.Run", ta.getTableTriggerId(), script,
						"[1209," + ta.getTableTriggerId() + "] " + ta.getDsc(), ge);
			} */
		}
		if (msg != null) {
//			short resultAction = ta.getLkpResultAction();
//			if (scope.has("resultAction", scope))resultAction = (short) GenericUtil.uInt(scope.get("resultAction", scope).toString());
			switch (ta.getLkpResultAction()) {
			case 1: // readonly
				formResult.setViewMode(true);
			case 0: // continue
				formResult.getOutputMessages().add(msg);
				break;
			case 2: // confirm & continue
				if (!requestParams.containsKey("_confirmId_" + ta.getTableTriggerId()))
					throw new IWBException("confirm", "ConfirmId", ta.getTableTriggerId(), null, msg, null);
				break;
			case 3: // stop with message
				throw new IWBException("security", "TableTrigger", ta.getTableTriggerId(), null, msg, null);
			}
		}
	}

	public Object executeScript(Map<String, Object> scd, Map<String, String> requestParams, String script, Map obj,
			String key) {
		if (GenericUtil.isEmpty(script))
			return null;

		String fncName = "scr_"
				+ (scd != null && scd.get("projectId") != null ? scd.get("projectId").toString().replace('-', '_')
						: "xxx")
				+ "_" + key;
		if (useNashorn) {
			if (nashornEngine == null)
				nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
			Object nobj = FrameworkCache.getGraalFunc(scd, key);

			if (nobj == null)
				try {
					StringBuilder sb = new StringBuilder();
					sb.append("function ").append(fncName).append("($, _scd, _request, _obj");
					sb.append("){var result=null;\n");
					sb.append(script);
					sb.append("\nreturn result}");
					script = sb.toString();
					nashornEngine.eval(script);
					FrameworkCache.addGraalFunc(scd, key, fncName);
				} catch (Exception ge) {
					String msg = getErrorLineNumberAsMsg(ge);
//				dao.logGlobalFuncAction(action, r, error);
					throw new IWBException("rhino", "NashornScript.Compile", 0, script, key + msg, ge);
				}
			else if (nobj instanceof String) {
				fncName = nobj.toString();
			}

			try {
				Object funcResult = ((Invocable) nashornEngine).invokeFunction(fncName,
						new NashornScript(scd, requestParams, this), scd, requestParams, obj);

				return funcResult;

			} catch (Exception ge) {
				String msg = getErrorLineNumberAsMsg(ge);
//				dao.logGlobalFuncAction(action, r, error);
				throw new IWBException("rhino", "NashornScript.Run", 0, script, fncName + msg, ge);
			}

		} else { // graal
/*			if (polyglot == null)
				polyglot = Context.create();// newBuilder("js").allowHostAccess(true).build();
			Value func = (Value) FrameworkCache.getGraalFunc(scd, key);
			if (func == null)
				try {
					StringBuilder sb = new StringBuilder();
					sb.append("(function($, _scd, _request, _obj");
					sb.append("){\n");
					sb.append(script);
					sb.append("\nreturn result})");
					script = sb.toString();
					func = polyglot.eval("js", script);
					FrameworkCache.addGraalFunc(scd, key, func);
				} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
					throw new IWBException("rhino", "GraalScript.Compile", 0, script, key, ge);
				}

			try {
				Object funcResult = func.execute(new GraalScript(scd, requestParams, this), scd, requestParams, obj);

				return funcResult;

			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				throw new IWBException("rhino", "GraalScript.Run", 0, script, key, ge);
			}*/
			return null;

		}
	}

	public void executeQueryAsScript(W5QueryResult qr, String code) {

		// Context cx = Context.enter();
		W5Query q = qr.getQuery();
		boolean dynamicQuery = !GenericUtil.isEmpty(code);
		String script = GenericUtil.uStrNvl(code, q.getSqlFrom());
		List<Object> params = new ArrayList();

		if (useNashorn) { // Nashorn
			if (nashornEngine == null)
				nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
			Object nobj = dynamicQuery ? null : FrameworkCache.getGraalFunc(qr.getScd(), "8." + qr.getQueryId());
			String qryName = null;
			if (nobj == null)
				try {
					qryName = "qry_" + qr.getScd().get("projectId").toString().replace('-', '_') + "_"
							+ qr.getQueryId();
					StringBuilder sb = new StringBuilder();
					sb.append("function ").append(qryName).append("($, _scd, _request");
					if (!GenericUtil.isEmpty(qr.getQuery().get_queryParams())) {
						for (W5QueryParam p1 : qr.getQuery().get_queryParams())
							sb.append(",").append(p1.getDsc());
					}

					sb.append("){\n");
					sb.append(script);
					sb.append("\nreturn result}");
					script = sb.toString();
					nashornEngine.eval(script);
					if (!dynamicQuery)
						FrameworkCache.addGraalFunc(qr.getScd(), "8." + qr.getQueryId(), qryName);
				} catch (Exception ge) {
					String msg = getErrorLineNumberAsMsg(ge);
//				dao.logGlobalFuncAction(action, r, error);
					throw new IWBException("rhino", "NashornQuery.Compile", qr.getQueryId(), script,
							"[8," + qr.getQueryId() + "] " + qr.getQuery().getDsc() + msg, ge);
				}
			else if (nobj instanceof String) {
				qryName = nobj.toString();
			}

			params.add(new NashornScript(qr.getScd(), qr.getRequestParams(), this));
			params.add(qr.getScd());
			params.add(qr.getRequestParams());

			for (W5QueryParam p1 : q.get_queryParams()) {
				String s = qr.getRequestParams().get(p1.getDsc());
				Object o = GenericUtil.isEmpty(s) ? null : GenericUtil.getObjectByTip(s, p1.getParamType());
				if (o == null) {
					if (p1.getNotNullFlag() != 0)
						qr.getErrorMap().put(p1.getDsc(),
								LocaleMsgCache.get2(qr.getScd(), "validation_error_not_null"));
					params.add(null);
				} else if ((o instanceof Integer) || (o instanceof Double) || (o instanceof BigDecimal)
						|| (o instanceof Boolean))
					params.add(o);
				else if ((o instanceof Date))
					params.add(GenericUtil.uFormatDateTime((Date) o));
				else
					params.add(o);
			}

			try {
				Object funcResult = ((Invocable) nashornEngine).invokeFunction(qryName, params.toArray(new Object[0]));

				if (funcResult instanceof ScriptObjectMirror) {
					ScriptObjectMirror result = (ScriptObjectMirror) funcResult;
					if (!result.isArray()) { // result and extraOutMap:TODO
						if (result.containsKey("extraOutMap"))
							qr.setExtraOutMap(
									NashornUtil.fromScriptObject2Map((ScriptObjectMirror) result.get("extraOutMap")));

						if (result.containsKey("data"))
							result = (ScriptObjectMirror) result.get("data");
						else if (result.containsKey("result"))
							result = (ScriptObjectMirror) result.get("result");
						else
							throw new IWBException("rhino", "NashornQuery.Typo", qr.getQueryId(), script,
									"[8," + qr.getQueryId() + "] " + qr.getQuery().getDsc() + " Missing data/result",
									null);
					}

//					int qi = result.size();
					qr.setFetchRowCount(result.size());

					int maxTabOrder = 0;
					qr.setNewQueryFields(new ArrayList());
					for (W5QueryField qf : q.get_queryFields()) {
						if (qf.getTabOrder() > maxTabOrder)
							maxTabOrder = qf.getTabOrder();
						qr.getNewQueryFields().add(qf);
					}
					for (W5QueryField qf : q.get_queryFields())
						if ((qf.getPostProcessType() == 16 || qf.getPostProcessType() == 17)
								&& qf.getLookupQueryId() != 0) {
							W5QueryResult queryFieldLookupQueryResult = metadataLoader.getQueryResult(qr.getScd(),
									qf.getLookupQueryId());
							if (queryFieldLookupQueryResult != null && queryFieldLookupQueryResult.getQuery() != null) {
								W5QueryField field = new W5QueryField();
								field.setDsc(qf.getDsc() + "_qw_");
								field.setMainTableFieldId(qf.getMainTableFieldId());
								maxTabOrder++;
								field.setTabOrder((short) maxTabOrder);
								qr.getNewQueryFields().add(field);
								if (qf.getPostProcessType() == 16
										&& queryFieldLookupQueryResult.getQuery().get_queryFields().size() > 2)
									for (int qi = 2; qi < queryFieldLookupQueryResult.getQuery().get_queryFields()
											.size(); qi++) {
										W5QueryField qf2 = queryFieldLookupQueryResult.getQuery().get_queryFields()
												.get(qi);
										field = new W5QueryField();
										field.setDsc(qf.getDsc() + "__" + qf2.getDsc());
										field.setMainTableFieldId(qf2.getMainTableFieldId());
										maxTabOrder++;
										field.setTabOrder((short) maxTabOrder);
										qr.getNewQueryFields().add(field);
									}
							}
						}
					qr.setData(new ArrayList(result.size()));
					int qi = 0;
					for (Object noo : result.values()) {
						ScriptObjectMirror no = (ScriptObjectMirror) noo;
						Object[] o = new Object[maxTabOrder];
						qr.getData().add(o);
						for (W5QueryField qf : qr.getNewQueryFields())
							if (no.containsKey(qf.getDsc())) {
								Object o2 = no.get(qf.getDsc());
								if (o2 != null) {
									switch (qf.getFieldType()) {
									case 4: // integer
										o[qf.getTabOrder() - 1] = GenericUtil.uInt(o2);
										break;
									case 8: // json
										if (o2 instanceof ScriptObjectMirror) {
											ScriptObjectMirror no2 = (ScriptObjectMirror) o2;
											if (no2.isArray()) {
												o[qf.getTabOrder() - 1] = GenericUtil.fromListToJsonString2Recursive(
														NashornUtil.fromScriptObject2List(no2));
											} else {
												o[qf.getTabOrder() - 1] = GenericUtil.fromMapToJsonString2Recursive(
														NashornUtil.fromScriptObject2Map(no2));
											}

										} else if (o2 instanceof Map)
											o[qf.getTabOrder() - 1] = GenericUtil
													.fromMapToJsonString2Recursive((Map) o2);
										else if (o2 instanceof List)
											o[qf.getTabOrder() - 1] = GenericUtil
													.fromListToJsonString2Recursive((List) o2);
										else
											o[qf.getTabOrder() - 1] = "'" + GenericUtil.stringToJS(o2.toString()) + "'";

										break;
									default:
										o[qf.getTabOrder() - 1] = o2;
									}
								}
							}
						qi++;
					}
					qi++;
				}

			} catch (Exception ge) {
				String msg = getErrorLineNumberAsMsg(ge);
//				dao.logGlobalFuncAction(action, r, error);
				if(!script.startsWith("function "))script="function "+qryName+"($, _scd, _request /* and other params*/) {\n"+script+"\nreturn result}";
				throw new IWBException("rhino", "NashornQuery.Run", qr.getQueryId(), script,
						"[8," + qr.getQueryId() + "] " + qr.getQuery().getDsc() + msg, ge);
			}
		} else {// graal
	/*		if (polyglot == null)
				polyglot = Context.create();// newBuilder("js").allowHostAccess(true).build();
			Value func = (Value) FrameworkCache.getGraalFunc(qr.getScd(), "8." + qr.getQueryId());
			if (func == null)
				try {
					StringBuilder sb = new StringBuilder();
					sb.append("(function($, _scd, _request");
					if (!GenericUtil.isEmpty(qr.getQuery().get_queryParams())) {
						for (W5QueryParam p1 : qr.getQuery().get_queryParams())
							sb.append(",").append(p1.getDsc());
					}

					sb.append("){\n");
					sb.append(script);
					sb.append("\nreturn result})");
					script = sb.toString();
					func = polyglot.eval("js", script);
					FrameworkCache.addGraalFunc(qr.getScd(), "8." + qr.getQueryId(), func);
				} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
					throw new IWBException("rhino", "GraalQuery.Compile", qr.getQueryId(), script,
							"[8," + qr.getQueryId() + "] " + qr.getQuery().getDsc(), ge);
				}

			params.add(new GraalScript(qr.getScd(), qr.getRequestParams(), this));
			params.add(qr.getScd());
			params.add(qr.getRequestParams());

			for (W5QueryParam p1 : q.get_queryParams()) {
				String s = qr.getRequestParams().get(p1.getDsc());
				Object o = GenericUtil.isEmpty(s) ? null : GenericUtil.getObjectByTip(s, p1.getParamTip());
				if (o == null) {
					if (p1.getNotNullFlag() != 0)
						qr.getErrorMap().put(p1.getDsc(),
								LocaleMsgCache.get2(qr.getScd(), "validation_error_not_null"));
					params.add(null);
				} else if ((o instanceof Integer) || (o instanceof Double) || (o instanceof BigDecimal)
						|| (o instanceof Boolean))
					params.add(o);
				else if ((o instanceof Date))
					params.add(GenericUtil.uFormatDateTime((Date) o));
				else
					params.add(o);
			}

			try {
//				Object funcResult = ((Invocable) nashornEngine).invokeFunction(qryName, params.toArray(new Object[0]));

				Object funcResult = func.execute(params.toArray(new Object[0]));

				if (funcResult != null && funcResult instanceof Value) {
					Value result = (Value) funcResult;
					if (!result.hasArrayElements()) { // result and extraOutMap:TODO
						if (result.hasMember("extraOutMap"))
							qr.setExtraOutMap(ScriptUtil.fromGraalValue2Map((Value) result.getMember("extraOutMap")));

						if (result.hasMember("data"))
							result = (Value) result.getMember("data");
						else if (result.hasMember("result"))
							result = (Value) result.getMember("result");
						else
							throw new IWBException("rhino", "GraalQuery.Typo", qr.getQueryId(), script,
									"[8," + qr.getQueryId() + "] " + qr.getQuery().getDsc() + " Missing data/result",
									null);
					}

//					int qi = result.size();
					qr.setFetchRowCount((int) result.getArraySize());

					int maxTabOrder = 0;
					qr.setNewQueryFields(new ArrayList());
					for (W5QueryField qf : q.get_queryFields()) {
						if (qf.getTabOrder() > maxTabOrder)
							maxTabOrder = qf.getTabOrder();
						qr.getNewQueryFields().add(qf);
					}
					for (W5QueryField qf : q.get_queryFields())
						if ((qf.getPostProcessTip() == 16 || qf.getPostProcessTip() == 17)
								&& qf.getLookupQueryId() != 0) {
							W5QueryResult queryFieldLookupQueryResult = metaDataDao.getQueryResult(qr.getScd(),
									qf.getLookupQueryId());
							if (queryFieldLookupQueryResult != null && queryFieldLookupQueryResult.getQuery() != null) {
								W5QueryField field = new W5QueryField();
								field.setDsc(qf.getDsc() + "_qw_");
								field.setMainTableFieldId(qf.getMainTableFieldId());
								maxTabOrder++;
								field.setTabOrder((short) maxTabOrder);
								qr.getNewQueryFields().add(field);
								if (qf.getPostProcessTip() == 16
										&& queryFieldLookupQueryResult.getQuery().get_queryFields().size() > 2)
									for (int qi = 2; qi < queryFieldLookupQueryResult.getQuery().get_queryFields()
											.size(); qi++) {
										W5QueryField qf2 = queryFieldLookupQueryResult.getQuery().get_queryFields()
												.get(qi);
										field = new W5QueryField();
										field.setDsc(qf.getDsc() + "__" + qf2.getDsc());
										field.setMainTableFieldId(qf2.getMainTableFieldId());
										maxTabOrder++;
										field.setTabOrder((short) maxTabOrder);
										qr.getNewQueryFields().add(field);
									}
							}
						}
					qr.setData(new ArrayList((int) result.getArraySize()));

					for (int qi = 0; qi < result.getArraySize(); qi++) {

						Value no = result.getArrayElement(qi);
						Object[] o = new Object[maxTabOrder];
						qr.getData().add(o);
						for (W5QueryField qf : qr.getNewQueryFields())
							if (no.hasMember(qf.getDsc())) {
								Object o2 = no.getMember(qf.getDsc());
								if (o2 != null) {
									switch (qf.getFieldTip()) {
									case 4: // integer
										o[qf.getTabOrder() - 1] = GenericUtil.uInt(o2);
										break;
									case 8: // json
										if (o2 instanceof Value) {
											Value no2 = (Value) o2;
											if (no2.hasArrayElements()) {
												o[qf.getTabOrder() - 1] = GenericUtil.fromListToJsonString2Recursive(
														ScriptUtil.fromGraalValue2List(no2));
											} else {
												o[qf.getTabOrder() - 1] = GenericUtil.fromMapToJsonString2Recursive(
														ScriptUtil.fromGraalValue2Map(no2));
											}

										} else if (o2 instanceof Map)
											o[qf.getTabOrder() - 1] = GenericUtil
													.fromMapToJsonString2Recursive((Map) o2);
										else if (o2 instanceof List)
											o[qf.getTabOrder() - 1] = GenericUtil
													.fromListToJsonString2Recursive((List) o2);
										else
											o[qf.getTabOrder() - 1] = "'" + GenericUtil.stringToJS(o2.toString()) + "'";

										break;
									default:
										o[qf.getTabOrder() - 1] = o2;
									}
								}
							}
					}
				}

			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				throw new IWBException("rhino", "GraalQuery.Run", qr.getQueryId(), script,
						"[8," + qr.getQueryId() + "] " + qr.getQuery().getDsc(), ge);
			}
*/
		}
	}

	public Map executeQuery4StatWS(W5QueryResult queryResult) {

		W5WsMethod wsm = FrameworkCache.getWsMethod(queryResult.getScd(), queryResult.getQuery().getSourceObjectId());
		Map<String, Object> scd = queryResult.getScd();

		W5WsMethodParam parentParam = null;
		for (W5WsMethodParam px : wsm.get_params())
			if (px.getOutFlag() != 0 && px.getParamType() == 10) {
				parentParam = px;
				break;
			}
		Map<String, String> m2 = new HashMap();
		Map<String, String> requestParams = queryResult.getRequestParams();

		int statType = GenericUtil.uInt(requestParams, "_stat"); // 0:count,
																	// 1:sum,
																	// 2.avg
		String funcFields = requestParams.get("_ffids"); // statFunctionFields
		if (statType > 0 && GenericUtil.isEmpty(funcFields))
			throw new IWBException("framework", "Query", queryResult.getQueryId(), null,
					LocaleMsgCache.get2(0, (String) scd.get("locale"), "fw_grid_stat_func_fields"), null);

		int queryFieldId = GenericUtil.uInt(requestParams, "_qfid");
		int stackFieldId = GenericUtil.uInt(requestParams, "_sfid");
		if (stackFieldId > 0 && stackFieldId == queryFieldId)
			stackFieldId = 0;

		for (W5QueryParam qp : queryResult.getQuery().get_queryParams())
			if (!GenericUtil.isEmpty(requestParams.get(qp.getDsc()))) {
				m2.put(qp.getExpressionDsc(), requestParams.get(qp.getDsc()));
			}
		StringBuilder rc = new StringBuilder();
		rc.append("function _x_(x){\nreturn {").append(queryResult.getQuery().getSqlSelect())
				.append("\n}}\nvar result=[], q=$.REST('").append(wsm.get_ws().getDsc() + "." + wsm.getDsc())
				.append("',").append(GenericUtil.fromMapToJsonString2(m2))
				.append(");\nif(q && q.get('success')){q=q.get('").append(parentParam.getDsc())
				.append("');for(var i=0;i<q.size();i++)result.push(_x_(q.get(i)));}");
		executeQueryAsScript(queryResult, rc.toString());
		Map result = new HashMap();
		result.put("success", true);
		if (queryResult.getErrorMap().isEmpty()) {
			List<Map> data = new ArrayList();
			Map<String, Map> mdata = new HashMap();
			W5QueryField statQF = null, funcQF = null;
			W5LookUp statLU = null;
			for (W5QueryField qf : queryResult.getQuery().get_queryFields())
				if (qf.getQueryFieldId() == queryFieldId) {
					statQF = qf;
					if (qf.getPostProcessType() == 10)
						statLU = FrameworkCache.getLookUp(scd, qf.getLookupQueryId());
					break;
				}
			if (statType > 0) {
				int funcFieldId = GenericUtil.uInt(funcFields.split(",")[0]);
				for (W5QueryField qf : queryResult.getQuery().get_queryFields())
					if (qf.getQueryFieldId() == funcFieldId) {
						funcQF = qf;
						break;
					}
			}
			for (Object[] o : queryResult.getData()) {
				Object okey = o[statQF.getTabOrder() - 1];
				String key = "_";
				if (okey != null)
					key = okey.toString();
				Map mr = mdata.get(key);
				if (mr == null) {
					mr = new HashMap();
					mr.put("id", key);
					if (statLU != null) {
						W5LookUpDetay ld = statLU.get_detayMap().get(key.toString());
						mr.put("dsc", LocaleMsgCache.get2(scd, ld.getDsc()));
					} else
						mr.put("dsc", key);
					mr.put("xres", BigDecimal.ZERO);
					mr.put("xcnt", 0);
					mdata.put(key, mr);
					data.add(mr);
				}
				switch (statType) {
				case 0: // count
				case 2: // avg
					int cnt = (Integer) mr.get("xcnt");
					cnt++;
					mr.put("xcnt", cnt);
					if (statType == 0)
						break;
				case 1: // sum
					BigDecimal res = (BigDecimal) mr.get("xres");
					res = res.add(GenericUtil.uBigDecimal2(o[funcQF.getTabOrder() - 1]));
					mr.put("xres", res);
					break;
				}
			}
			for (Map mr : data)
				switch (statType) {
				case 0: // count
					mr.put("xres", mr.get("xcnt"));
					break;
				case 2: // avg
					int cnt = (Integer) mr.get("xcnt");
					if (cnt > 0)
						mr.put("xres", ((BigDecimal) mr.get("xres")).divide(new BigDecimal(cnt)));
					break;
				}

			result.put("data", data);
		}

		return result;
	}

	private static String getErrorLineNumberAsMsg(Exception ge) {
		String msg = ge.getMessage();
		if (msg != null) {
			for (Throwable prt = ge; prt != null; prt = prt.getCause()) {
				String xmsg = prt.getMessage();
				if (xmsg != null && xmsg.contains("at line number")) {
					int xi = xmsg.indexOf("at line number");
					xmsg = xmsg.substring(xi + "at line number".length()).trim();
					xi = xmsg.indexOf(" ");
					if (xi > -1)
						xmsg = xmsg.substring(0, xi);
					return " #{" + xmsg + "}#";
				}
			}

		}
		for (StackTraceElement ste : ge.getStackTrace()) {
			if (ste.getClassName().startsWith("jdk.nashorn.internal.scripts.")) {
				return " #{" + ste.getLineNumber()+"}#";
			}
		}
		return "";
	}

	public W5GlobalFuncResult executeGlobalFunc4Debug(Map<String, Object> scd, int globalFuncId,
			Map<String, String> parameterMap) {
		W5GlobalFuncResult r = globalFuncId == -1 ? new W5GlobalFuncResult(-1)
				: metadataLoader.getGlobalFuncResult(scd, globalFuncId);
		r.setScd(scd);
		dao.checkTenant(scd);
		r.setErrorMap(new HashMap());
		r.setRequestParams(parameterMap);
		String script = parameterMap.get("_rhino_script_code");

		if (useNashorn) {
			if (nashornEngine == null)
				nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
			String fncName = null;
			boolean hasOutParam = false;
			List<Object> params = new ArrayList();
			try {
				fncName = "dfnc_" + (r.getScd() != null && r.getScd().get("projectId") != null
						? r.getScd().get("projectId").toString().replace('-', '_')
						: "xxx") + "_" + Math.abs(globalFuncId);
				StringBuilder sb = new StringBuilder();
				StringBuilder sbPost1 = new StringBuilder(), sbPost2 = new StringBuilder();
				sb.append("function ").append(fncName).append("($, _scd, _request");
				if (r.getGlobalFunc() != null && !GenericUtil.isEmpty(r.getGlobalFunc().get_dbFuncParamList())) {
					for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
						if (p1.getOutFlag() == 0) {
							sb.append(",").append(p1.getDsc());
						} else {
							hasOutParam = true;
							sbPost1.append(p1.getDsc()).append("=null, ");
							sbPost2.append(p1.getDsc()).append(":").append(p1.getDsc()).append(", ");
						}
				}

				sb.append("){\n");
				if (hasOutParam) {
					sbPost1.setLength(sbPost1.length() - 2);
					sbPost2.setLength(sbPost2.length() - 2);
					sb.append("var ").append(sbPost1).append(";\n").append(script).append("\nreturn {").append(sbPost2)
							.append("}");

				} else
					sb.append(script);
				sb.append("\n}");
				script = sb.toString();
				nashornEngine.eval(script);
			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				String msg = getErrorLineNumberAsMsg(ge);
				throw new IWBException("rhino", "NashornGlobalFuncDebug.Compile", r.getGlobalFuncId(), script,
						"[20," + r.getGlobalFuncId() + "] "
								+ (r.getGlobalFunc() != null ? r.getGlobalFunc().getDsc() : "") + msg,
						ge);
			}
			NashornScript se = new NashornScript(r.getScd(), r.getRequestParams(), this);

			params.add(se);
			params.add(scd);
			params.add(parameterMap);

			if (r.getGlobalFunc() != null && !GenericUtil.isEmpty(r.getGlobalFunc().get_dbFuncParamList())) {
				for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
					if (p1.getOutFlag() == 0) {
						Object o = GenericUtil.prepareParam(p1, r.getScd(), r.getRequestParams(), (short) -1, null,
								(short) 0, p1.getSourceType() == 1 ? p1.getDsc() : null, null, r.getErrorMap());
						if (o == null)
							params.add(null);
						else if ((o instanceof Integer) || (o instanceof Double) || (o instanceof BigDecimal)
								|| (o instanceof Boolean))
							params.add(o);
						else if ((o instanceof Date))
							params.add(GenericUtil.uFormatDate((Date) o));
						else
							params.add(o);
					} else
						hasOutParam = true;
			}

			try {
				if (FrameworkSetting.debug)
					se.console("start: " + (r.getGlobalFunc() != null ? r.getGlobalFunc().getDsc() : "new"), "DEBUG",
							"info");
				long startTm = System.currentTimeMillis();

				Object funcResult = ((Invocable) nashornEngine).invokeFunction(fncName, params.toArray(new Object[0]));
				r.setProcessTime((int) (System.currentTimeMillis() - startTm));
				if (FrameworkSetting.debug)
					se.console("end: " + (r.getGlobalFunc() != null ? r.getGlobalFunc().getDsc() : "new") + " in "
							+ r.getProcessTime() + "ms", "DEBUG", "info");

				if (hasOutParam) {
					// JSONObject jo=new JSONObject();
					Map<String, Object> res = new HashMap<String, Object>();
					if (funcResult != null && funcResult instanceof ScriptObjectMirror) {
						Map resultMap = NashornUtil.fromScriptObject2Map((ScriptObjectMirror) funcResult);
						r.setResultMap(resultMap);
						if (false)
							for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
								if (p1.getOutFlag() != 0 && resultMap.containsKey(p1.getDsc())) {
									Object em = resultMap.get(p1.getDsc());
									if (em != null) {
										String v = em.toString();
										if (p1.getParamType() == 4 && v.endsWith(".0"))
											v = v.substring(0, v.length() - 2);
										res.put(p1.getDsc(), v);
									}
								}
					} else
						r.setResultMap(res);
				}
			} catch (Exception ge) {// ge.printStackTrace();
//				dao.logGlobalFuncAction(action, r, error);
				String msg = getErrorLineNumberAsMsg(ge);

				if (r.getGlobalFunc() != null)
					throw new IWBException("rhino", "NashornGlobalFuncDebug.Run", r.getGlobalFuncId(), script,
							"[20," + r.getGlobalFuncId() + "] " + r.getGlobalFunc().getDsc() + msg, ge);
				else
					throw new IWBException("rhino", "NashornDebug.Run", 0, script, "Nashorn Exception" + msg, ge);
			}

		} else { // Graal
/*			if (polyglot == null)
				polyglot = Context.create();// newBuilder("js").allowHostAccess(true).build();

			boolean hasOutParam = false;
			List<Object> params = new ArrayList();
			Value func = null;
			try {
				StringBuilder sb = new StringBuilder();
				StringBuilder sbPost1 = new StringBuilder(), sbPost2 = new StringBuilder();
				sb.append("(function($, _scd, _request");
				if (r.getGlobalFunc() != null && !GenericUtil.isEmpty(r.getGlobalFunc().get_dbFuncParamList())) {
					for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
						if (p1.getOutFlag() == 0) {
							sb.append(",").append(p1.getDsc());
						} else {
							hasOutParam = true;
							sbPost1.append(p1.getDsc()).append("=null, ");
							sbPost2.append(p1.getDsc()).append(":").append(p1.getDsc()).append(", ");
						}
				}

				sb.append("){\n");
				if (hasOutParam) {
					sbPost1.setLength(sbPost1.length() - 2);
					sbPost2.setLength(sbPost2.length() - 2);
					sb.append("var ").append(sbPost1).append(";\n").append(script).append("\nreturn {").append(sbPost2)
							.append("}");

				} else
					sb.append(script);
				sb.append("\n})");
				script = sb.toString();
				func = polyglot.eval("js", script);
			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				throw new IWBException("rhino", "GraalGlobalFuncDebug.Compile", r.getGlobalFuncId(), script, "[20,"
						+ r.getGlobalFuncId() + "] " + (r.getGlobalFunc() != null ? r.getGlobalFunc().getDsc() : ""),
						ge);
			}
			GraalScript se = new GraalScript(r.getScd(), r.getRequestParams(), this);

			params.add(se);
			params.add(scd);
			params.add(parameterMap);

			if (r.getGlobalFunc() != null && !GenericUtil.isEmpty(r.getGlobalFunc().get_dbFuncParamList())) {
				for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
					if (p1.getOutFlag() == 0) {
						Object o = GenericUtil.prepareParam(p1, r.getScd(), r.getRequestParams(), (short) -1, null,
								(short) 0, p1.getSourceTip() == 1 ? p1.getDsc() : null, null, r.getErrorMap());
						if (o == null)
							params.add(null);
						else if ((o instanceof Integer) || (o instanceof Double) || (o instanceof BigDecimal)
								|| (o instanceof Boolean))
							params.add(o);
						else if ((o instanceof Date))
							params.add(GenericUtil.uFormatDate((Date) o));
						else
							params.add(o);
					} else
						hasOutParam = true;
			}

			try {
				if (FrameworkSetting.debug)
					se.console("start: " + (r.getGlobalFunc() != null ? r.getGlobalFunc().getDsc() : "new"), "DEBUG",
							"info");
				long startTm = System.currentTimeMillis();

				Object funcResult = func.execute(params.toArray(new Object[0]));

				r.setProcessTime((int) (System.currentTimeMillis() - startTm));
				if (FrameworkSetting.debug)
					se.console("end: " + (r.getGlobalFunc() != null ? r.getGlobalFunc().getDsc() : "new") + " in "
							+ r.getProcessTime() + "ms", "DEBUG", "info");

				if (hasOutParam) {
					// JSONObject jo=new JSONObject();
					Map<String, String> res = new HashMap<String, String>();
					if (funcResult != null && funcResult instanceof Value) {
						Map resultMap = ScriptUtil.fromGraalValue2Map((Value) funcResult);
						r.setResultMap(resultMap);
						if (false)
							for (W5GlobalFuncParam p1 : r.getGlobalFunc().get_dbFuncParamList())
								if (p1.getOutFlag() != 0 && resultMap.containsKey(p1.getDsc())) {
									Object em = resultMap.get(p1.getDsc());
									if (em != null) {
										String v = em.toString();
										if (p1.getParamTip() == 4 && v.endsWith(".0"))
											v = v.substring(0, v.length() - 2);
										res.put(p1.getDsc(), v);
									}
								}
					} else
						r.setResultMap(res);
				}
			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				if (r.getGlobalFunc() != null)
					throw new IWBException("rhino", "GraalGlobalFuncDebug.Run", r.getGlobalFuncId(), script,
							"[20," + r.getGlobalFuncId() + "] " + r.getGlobalFunc().getDsc(), ge);
				else
					throw new IWBException("rhino", "GraalDebug.Run", 0, script, "Graal Exception", ge);
			}
*/
		}
		r.setSuccess(true);
		return r;
	}

	public Map executeQueryAsScript4Debug(W5QueryResult qr, String script) {

		// Context cx = Context.enter();
		W5Query q = qr.getQuery();
		dao.checkTenant(qr.getScd());

		Map m = new HashMap();
		m.put("success", true);
		List<Object> params = new ArrayList();

		if (useNashorn) {
			if (nashornEngine == null)
				nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
			String qryName = "dqry_" + qr.getScd().get("projectId").toString().replace('-', '_') + "_"
					+ Math.abs(qr.getQueryId());
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("function ").append(qryName).append("($, _scd, _request");
				if (!GenericUtil.isEmpty(qr.getQuery().get_queryParams())) {
					for (W5QueryParam p1 : qr.getQuery().get_queryParams())
						sb.append(",").append(p1.getDsc());
				}

				sb.append("){\n");
				sb.append(script);
				sb.append("\nreturn result}");
				script = sb.toString();
				nashornEngine.eval(script);
			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				String msg = getErrorLineNumberAsMsg(ge);
				throw new IWBException("rhino", "DebugNashornQuery.Compile", qr.getQueryId(), script,
						"[8," + qr.getQueryId() + "] " + (qr.getQuery() != null ? qr.getQuery().getDsc() : "") + msg, ge);
			}

			NashornScript se = new NashornScript(qr.getScd(), qr.getRequestParams(), this);
			params.add(se);
			params.add(qr.getScd());
			params.add(qr.getRequestParams());

			for (W5QueryParam p1 : q.get_queryParams()) {
				String s = qr.getRequestParams().get(p1.getDsc());
				Object o = GenericUtil.isEmpty(s) ? null : GenericUtil.getObjectByTip(s, p1.getParamType());
				if (o == null) {
					if (p1.getNotNullFlag() != 0)
						qr.getErrorMap().put(p1.getDsc(),
								LocaleMsgCache.get2(qr.getScd(), "validation_error_not_null"));
					params.add(null);
				} else if ((o instanceof Integer) || (o instanceof Double) || (o instanceof BigDecimal)
						|| (o instanceof Boolean))
					params.add(o);
				else if ((o instanceof Date))
					params.add(GenericUtil.uFormatDateTime((Date) o));
				else
					params.add(o);
			}

			try {
				if (FrameworkSetting.debug)
					se.console("start: " + (q != null ? q.getDsc() : "new"), "DEBUG", "info");
				long startTm = System.currentTimeMillis();

				Object funcResult = ((Invocable) nashornEngine).invokeFunction(qryName, params.toArray(new Object[0]));
				qr.setProcessTime((int) (System.currentTimeMillis() - startTm));
				if (FrameworkSetting.debug)
					se.console("end: " + (q != null ? q.getDsc() : "new") + " in " + qr.getProcessTime() + "ms",
							"DEBUG", "info");

				if (funcResult != null && funcResult instanceof ScriptObjectMirror) {
					ScriptObjectMirror result = (ScriptObjectMirror) funcResult;
					if (!result.isArray()) { // result and extraOutMap:TODO
						if (result.containsKey("extraOutMap"))
							qr.setExtraOutMap(
									NashornUtil.fromScriptObject2Map((ScriptObjectMirror) result.get("extraOutMap")));

						if (result.containsKey("data"))
							result = (ScriptObjectMirror) result.get("data");
						else if (result.containsKey("result"))
							result = (ScriptObjectMirror) result.get("result");
						else
							throw new IWBException("rhino", "DebugNashornQuery.Typo", qr.getQueryId(), script,
									"[8," + qr.getQueryId() + "] "
											+ (qr.getQuery() != null ? qr.getQuery().getDsc() : "")
											+ " Missing data/result",
									null);
					}

//					int qi = result.size();
					qr.setFetchRowCount(result.size());

					int maxTabOrder = 0;
					qr.setNewQueryFields(new ArrayList());
					for (W5QueryField qf : q.get_queryFields()) {
						if (qf.getTabOrder() > maxTabOrder)
							maxTabOrder = qf.getTabOrder();
						qr.getNewQueryFields().add(qf);
					}

					List<Map> data = new ArrayList(result.size());
					int qi = 0;
					for (Object noo : result.values()) {
						ScriptObjectMirror no = (ScriptObjectMirror) noo;
						Object[] o = new Object[maxTabOrder];
						Map d = new HashMap();
						data.add(d);
						for (W5QueryField qf : qr.getNewQueryFields())
							if (no.containsKey(qf.getDsc())) {
								// o[qf.getTabOrder()-1] =
								// no.get(qf.getDsc(), scope);
								d.put(qf.getDsc(), no.get(qf.getDsc()));
							}
						qi++;
					}
					m.put("data", data);
					m.put("fetchTime", System.currentTimeMillis() - startTm);
					Map m2 = new HashMap();
					m2.put("startRow", 0);
					m2.put("fetchCount", result.size());
					m2.put("totalCount", result.size());
					m.put("browseInfo", m2);

					List fields = new ArrayList();
					for (W5QueryField qf : q.get_queryFields()) {
						Map d = new HashMap();
						d.put("name", qf.getDsc());
						switch (qf.getFieldType()) {
						case 3:
							d.put("type", "int");
							break;
						case 4:
							d.put("type", "float");
							break;
						case 2:
							d.put("type", "date");
							break;
						}
						fields.add(d);
					}
					m.put("fields", fields);

				}

			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				String msg = getErrorLineNumberAsMsg(ge);
				throw new IWBException("rhino", "NashornQueryDebug.Run", qr.getQueryId(), script,
						"[8," + qr.getQueryId() + "] " + (qr.getQuery() != null ? qr.getQuery().getDsc() : "") + msg, ge);
			}
		} else { // graal
/*			if (polyglot == null)
				polyglot = Context.create();// newBuilder("js").allowHostAccess(true).build();

			Value func = null;
			try {
				StringBuilder sb = new StringBuilder();
				sb.append("(function($, _scd, _request");
				if (!GenericUtil.isEmpty(qr.getQuery().get_queryParams())) {
					for (W5QueryParam p1 : qr.getQuery().get_queryParams())
						sb.append(",").append(p1.getDsc());
				}

				sb.append("){\n");
				sb.append(script);
				sb.append("\nreturn result})");
				script = sb.toString();
				func = polyglot.eval("js", script);
			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				throw new IWBException("rhino", "GraalQueryDebug.Compile", qr.getQueryId(), script,
						"[8," + qr.getQueryId() + "] " + (qr.getQuery() != null ? qr.getQuery().getDsc() : ""), ge);
			}

			GraalScript se = new GraalScript(qr.getScd(), qr.getRequestParams(), this);
			params.add(se);
			params.add(qr.getScd());
			params.add(qr.getRequestParams());

			for (W5QueryParam p1 : q.get_queryParams()) {
				String s = qr.getRequestParams().get(p1.getDsc());
				Object o = GenericUtil.isEmpty(s) ? null : GenericUtil.getObjectByTip(s, p1.getParamTip());
				if (o == null) {
					if (p1.getNotNullFlag() != 0)
						qr.getErrorMap().put(p1.getDsc(),
								LocaleMsgCache.get2(qr.getScd(), "validation_error_not_null"));
					params.add(null);
				} else if ((o instanceof Integer) || (o instanceof Double) || (o instanceof BigDecimal)
						|| (o instanceof Boolean))
					params.add(o);
				else if ((o instanceof Date))
					params.add(GenericUtil.uFormatDateTime((Date) o));
				else
					params.add(o);
			}

			try {
				if (FrameworkSetting.debug)
					se.console("start: " + (q != null ? q.getDsc() : "new"), "DEBUG", "info");
				long startTm = System.currentTimeMillis();

				Object funcResult = func.execute(params.toArray(new Object[0]));

				qr.setProcessTime((int) (System.currentTimeMillis() - startTm));
				if (FrameworkSetting.debug)
					se.console("end: " + (q != null ? q.getDsc() : "new") + " in " + qr.getProcessTime() + "ms",
							"DEBUG", "info");

				if (funcResult != null && funcResult instanceof Value) {
					Value result = (Value) funcResult;
					if (!result.hasArrayElements()) { // result and extraOutMap:TODO
						if (result.hasMember("extraOutMap"))
							qr.setExtraOutMap(ScriptUtil.fromGraalValue2Map((Value) result.getMember("extraOutMap")));

						if (result.hasMember("data"))
							result = (Value) result.getMember("data");
						else if (result.hasMember("result"))
							result = (Value) result.getMember("result");
						else
							throw new IWBException("rhino", "GraalQueryDebug.Typo", qr.getQueryId(), script,
									"[8," + qr.getQueryId() + "] "
											+ (qr.getQuery() != null ? qr.getQuery().getDsc() : "")
											+ " Missing data/result",
									null);
					}

//					int qi = result.size();
					qr.setFetchRowCount((int) result.getArraySize());

					int maxTabOrder = 0;
					qr.setNewQueryFields(new ArrayList());
					for (W5QueryField qf : q.get_queryFields()) {
						if (qf.getTabOrder() > maxTabOrder)
							maxTabOrder = qf.getTabOrder();
						qr.getNewQueryFields().add(qf);
					}

					List<Map> data = new ArrayList((int) result.getArraySize());
					for (int qi = 0; qi < result.getArraySize(); qi++) {
						Value no = (Value) result.getArrayElement(qi);
						Object[] o = new Object[maxTabOrder];
						Map d = new HashMap();
						data.add(d);
						for (W5QueryField qf : qr.getNewQueryFields())
							if (no.hasMember(qf.getDsc())) {
								// o[qf.getTabOrder()-1] =
								// no.get(qf.getDsc(), scope);
								d.put(qf.getDsc(), no.getMember(qf.getDsc()));
							}
						qi++;
					}
					m.put("data", data);
					m.put("fetchTime", System.currentTimeMillis() - startTm);
					Map m2 = new HashMap();
					m2.put("startRow", 0);
					m2.put("fetchCount", result.getArraySize());
					m2.put("totalCount", result.getArraySize());
					m.put("browseInfo", m2);

					List fields = new ArrayList();
					for (W5QueryField qf : q.get_queryFields()) {
						Map d = new HashMap();
						d.put("name", qf.getDsc());
						switch (qf.getFieldTip()) {
						case 3:
							d.put("type", "int");
							break;
						case 4:
							d.put("type", "float");
							break;
						case 2:
							d.put("type", "date");
							break;
						}
						fields.add(d);
					}
					m.put("fields", fields);

				}

			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				throw new IWBException("rhino", "GraalQueryDebug.Run", qr.getQueryId(), script,
						"[8," + qr.getQueryId() + "] " + (qr.getQuery() != null ? qr.getQuery().getDsc() : ""), ge);
			}*/
		}
		return m;
	}

	private static ScriptEngine snashornEngine = null;
//	private static Context spolyglot = null;

	public static String executePrepareParam(String script, Map<String, Object> scd, Map<String, String> requestParams,
			PostgreSQL dao) {

		String key = GenericUtil.getMd5Hash(script);

		if (useNashorn) {
			if (snashornEngine == null)
				snashornEngine = new ScriptEngineManager().getEngineByName("nashorn");

			Object nobj = FrameworkCache.getGraalFunc(scd, key);
			String fncName = "ep_" + key.replace('-', '_');
			if (nobj == null)
				try {
					StringBuilder sb = new StringBuilder();
					sb.append("function ").append(fncName).append("(_scd, _request, dao");
					sb.append("){\n");
					sb.append(script);
					sb.append("\nreturn result}");
					script = sb.toString();
					snashornEngine.eval(script);
					FrameworkCache.addGraalFunc(scd, key, fncName);
				} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
					throw new IWBException("rhino", "NashornPrepareParam.Compile", 0, script, key, ge);
				}
			else if (nobj instanceof String) {
				fncName = nobj.toString();
			}

			try {
				Object funcResult = ((Invocable) snashornEngine).invokeFunction(fncName, scd, requestParams, dao);

				return funcResult != null ? funcResult.toString() : null;

			} catch (Exception ge) {
//				dao.logGlobalFuncAction(action, r, error);
				throw new IWBException("rhino", "NashornPrepareParam.Run", 0, script, fncName, ge);
			}
		} else {
/*			if (spolyglot == null)
				spolyglot = Context.create();// newBuilder("js").allowHostAccess(true).build();

			Value func = (Value) FrameworkCache.getGraalFunc(scd, key);
			if (func == null)
				try {
					StringBuilder sb = new StringBuilder();
					sb.append("(function(_scd, _request, dao){\n");
					sb.append(script);
					sb.append("\nreturn result})");
					script = sb.toString();
					func = spolyglot.eval("js", script);
					FrameworkCache.addGraalFunc(scd, key, func);
				} catch (Exception ge) {
					// dao.logGlobalFuncAction(action, r, error);
					throw new IWBException("rhino", "GraalPrepareParam.Compile", 0, script, "Error Compile", ge);
				}

			try {
				Object funcResult = func.execute(scd, requestParams, dao);

				if (funcResult != null && funcResult instanceof Value) {
					Value result = (Value) funcResult;
					return result.toString();
				}
			} catch (Exception ge) {
				// dao.logGlobalFuncAction(action, r, error);
				throw new IWBException("rhino", "GraalPrepareParam.Run", 0, script, "Error Run", ge);
			}*/
		}
		return null;
	}

}
