package iwb.util;

import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import iwb.cache.FrameworkSetting;
import iwb.exception.IWBException;
import jdk.nashorn.api.scripting.ScriptObjectMirror;


public class NashornUtil {

	public static List fromScriptObject2List(Object reqL) {
		List ll = new ArrayList();
		if(reqL==null)return ll;
		ScriptObjectMirror jsRequestParams = (ScriptObjectMirror)reqL;
		for (Object oo : jsRequestParams.values()) {
			if (oo == null)
				ll.add(null);
			else if (oo instanceof ScriptObjectMirror)
				if (((ScriptObjectMirror) oo).isArray()) {
					ll.add(fromScriptObject2List((ScriptObjectMirror) oo));
				} else
					ll.add(fromScriptObject2Map((ScriptObjectMirror) oo));
			else
				ll.add(oo);
		}
		return ll;
	}

	public static Map fromScriptObject2Map(Object reqP) {
		Map<String, Object> rp = new HashMap<String, Object>();
		if(reqP==null)return rp;
		if(reqP instanceof ScriptObjectMirror) {
			ScriptObjectMirror jsRequestParams = (ScriptObjectMirror)reqP;
			if (jsRequestParams.isArray()) {
				rp.put("rhino", fromScriptObject2List(jsRequestParams));
				return rp;
	
			}
	
			for (String key : jsRequestParams.keySet()) {
				Object o = jsRequestParams.get(key);
				if (o != null) {
					if(o instanceof ScriptObjectMirror){
						if(((ScriptObjectMirror)o).isArray())
							rp.put(key, fromScriptObject2List(o));
						else
							rp.put(key, fromScriptObject2Map(o));
						
					} else {
						String res = o.toString();
						if (res.endsWith(".0") && GenericUtil.uInt(res.substring(0, res.length() - 2)) > 0)res = res.substring(0, res.length() - 2);
						rp.put(key, res);
					}
				}
			}
		}
		else 		if(reqP instanceof Map)return (Map)reqP;

		return rp;
	}
	

	public static List fromScriptObject2List2(Object reqL) {
		List ll = new ArrayList();
		if(reqL==null)return ll;
		if(reqL instanceof List)return (List)reqL;
		ScriptObjectMirror jsRequestParams = (ScriptObjectMirror)reqL;
		for (Object oo : jsRequestParams.values()) {
			if (oo == null)
				ll.add(null);
			else if (oo instanceof ScriptObjectMirror)
				if (((ScriptObjectMirror) oo).isArray()) {
					ll.add(fromScriptObject2List((ScriptObjectMirror) oo));
				} else
					ll.add(fromScriptObject2Map((ScriptObjectMirror) oo));
			else {
				if(oo instanceof Double || oo instanceof Float) {
					ll.add(oo);
				} else if(oo instanceof Integer || oo instanceof Short) {
					ll.add(oo);
				} else {
					String res = oo.toString();
//					if (res.endsWith(".0") && GenericUtil.uInt(res.substring(0, res.length() - 2)) > 0)res = res.substring(0, res.length() - 2);
					ll.add(res);
				}
			}
		}
		return ll;
	}

	public static Map fromScriptObject2Map2(Object reqP) {
		Map<String, Object> rp = new HashMap<String, Object>();
		if(reqP==null)return rp;
		if(reqP instanceof Map)return (Map)reqP;
		ScriptObjectMirror jsRequestParams = (ScriptObjectMirror)reqP;
		if (jsRequestParams.isArray()) {
			rp.put("rhino", fromScriptObject2List2(jsRequestParams));
			return rp;

		}

		for (String key : jsRequestParams.keySet()) {
			Object o = jsRequestParams.get(key);
			if (o != null) {
				if(o instanceof ScriptObjectMirror){
					if(((ScriptObjectMirror)o).isArray())
						rp.put(key, fromScriptObject2List2(o));
					else
						rp.put(key, fromScriptObject2Map2(o));
					
				} else {
					if(o instanceof Double || o instanceof Float) {
						rp.put(key, o);
					} else if(o instanceof Integer || o instanceof Short) {
						rp.put(key, o);
					} else {
						String res = o.toString();
//						if (res.endsWith(".0") && GenericUtil.uInt(res.substring(0, res.length() - 2)) > 0)res = res.substring(0, res.length() - 2);
						rp.put(key, res);
					}
				}
			}
		}
		return rp;
	}
	
	private static ScriptEngine babelEngine = null;
	private static SimpleBindings babelBindings = null;
	
	public static synchronized String babelTranspileJSX(String jsx) {
		if(FrameworkSetting.transpile==0)return jsx;
		if(GenericUtil.isEmpty(jsx))return jsx;
		if(babelEngine==null) try{
			ClassLoader classLoader = NashornUtil.class.getClassLoader();
			URL resource = classLoader.getResource("static/react/js/babel6/babel.min.js");
			FileReader babelScript = new FileReader(resource.getFile());
			babelEngine = new ScriptEngineManager().getEngineByMimeType("text/javascript");
			babelBindings = new SimpleBindings();
			babelEngine.eval(babelScript, babelBindings);
		} catch (Exception e) {
			if(FrameworkSetting.debug)e.printStackTrace();
			throw new IWBException("rhino", "Babel Enginne init", 0, null,
					"Error while loading babelScript", e);
		}
		Object o;
		try {
			o = babelEngine.eval("Babel.transform(\""+GenericUtil.stringToJS2(jsx)+"\", {presets: [\"react\"] }).code", babelBindings);
		} catch (ScriptException e) {
			if(FrameworkSetting.debug)e.printStackTrace();
			throw new IWBException("rhino", "BabelTranspile", 0, jsx,
					"Error while transpiling JSX", e);
		}
		if(o==null)return "";
		return o.toString();
	}

}
