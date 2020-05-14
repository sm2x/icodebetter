package iwb.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocaleMsgCache {
	static	public int	initialCapacity = 16; 
	static	public final String defaultLocale="tr";
	static	public final int defaultCustomizationId=0;
//	static	private Map<String, Map<String, String>> localeMap = new HashMap<String, Map<String, String>>();
	static	private ArrayList<String> publishLocaleKeys = new ArrayList<String>();	
	static	public Map<String, Map<String, Object>> localeMap2 = new HashMap<String, Map<String, Object>>();

	/*
	 * Aşağıdaki parametresiz fonksiyonlar publishLocaleKeys için
	 */
	
	public static void add2publish(String key){
		publishLocaleKeys.add(key);
	}
	/*
	public static Map<String,String> getPublishLocale(String locale){
		Map<String, String> x = new HashMap<String, String>();
		for(Object ss : publishLocaleKeys.toArray()){
			x.put(ss.toString(), get(locale, ss.toString()));
		}
		return x;
	}*/
	
	public static Map<String,String> getPublishLocale2(int customizationId,String locale){
		Map<String, String> x = new HashMap<String, String>();
		for(Object ss : publishLocaleKeys.toArray()){
			x.put(ss.toString(), get2(customizationId,locale, ss.toString()));
		}
		return x;
	}/*
	public static void set(String key, String value){
		set(defaultLocale, key, value);
	}

	public static void set(String locale, String key, String value){
		Map<String, String> x =localeMap.get(locale);
		if(x==null){
			x = new HashMap<String, String>(initialCapacity);
			localeMap.put(locale, x);
		}
		x.put(key, value);
	}*/

	public static void set2(int customizationId, String locale, String key, String value){
		Map<String, Object> x =localeMap2.get(locale);
		if(x==null){
			x = new HashMap<String, Object>(initialCapacity);
			localeMap2.put(locale, x);
			x.put(key, value);
			return;
		} else {
			Object o = x.get(key);
			if(o==null){ // bu key yok
				x.put(key, value);
			} else if(o instanceof HashMap){ // bu key ile ilgili en az 2 tane value var, farkli farkli customizationlara
				((Map<Integer, String>)o).put(customizationId, value);
			} else if(o instanceof String){ // bu key ile ilgili bir tane value var, cust=0 icin
				if(customizationId==0){ //gelen deger de cust 0 icinse
					x.put(key, value);
				} else {
					Map<Integer, String> y = new HashMap<Integer, String>();
					y.put(0, (String)o);
					y.put(customizationId, value);
					x.put(key, y);
				}
			}
		}
	}
/*
	public static String get(String key){
		return	get(defaultLocale, key);
	}

	public static String get(String locale, String key){
		Map<String, String> x =localeMap.get(locale);
		if(x==null){
			return ":MSG-ERROR: no locale for ("+locale+")";
		}
		String s = (String)x.get(key);
		return (s==null) ? key : s;
	}
*/
	public static String get2(int customizationId, String locale, String key){
		Map<String, Object> x =localeMap2.get(locale);
		if(x==null){
			return ":MSG-ERROR: no locale for ("+locale+")";
		}
		Object o = x.get(key);
		String s = null;
		if(o==null)return key;
		else if(o instanceof HashMap){ // bu key ile ilgili en az 2 tane value var, farkli farkli customizationlara
			s = ((Map<Integer, String>)o).get(customizationId);
			if(s==null) s = ((Map<Integer, String>)o).get(0);
			return (s==null) ? key : s;
		} else if(o instanceof String){
			return (String)o;
		}
		return ":MSG-ERROR: weird error for locale("+customizationId+","+locale+","+key+")";
	}
	public static String get2(Map scd, String key){
		int customizationId = (Integer)scd.get("customizationId");
		String locale = (String)scd.get("locale");
		Map<String, Object> x =localeMap2.get(locale);
		if(x==null){
			return ":MSG-ERROR: no locale for ("+locale+")";
		}
		Object o = x.get(key);
		String s = null;
		if(o==null)return key;
		else if(o instanceof HashMap){ // bu key ile ilgili en az 2 tane value var, farkli farkli customizationlara
			s = ((Map<Integer, String>)o).get(customizationId);
			if(s==null) s = ((Map<Integer, String>)o).get(0);
			return (s==null) ? key : s;
		} else if(o instanceof String){
			return (String)o;
		}
		return ":MSG-ERROR: weird error for locale("+customizationId+","+locale+","+key+")";
	}
	/*
	public static StringBuilder filter(String xlocale, String code) {
		StringBuilder tmp = new StringBuilder();
		if(code==null || code.length()==0)return tmp;
		tmp.append(code);
		for(int bas = tmp.indexOf("${"); bas>0; bas=tmp.indexOf("${",bas+2)){
			int bit = tmp.indexOf("}", bas+2);
			tmp.replace(bas, bit+1, get(xlocale, tmp.substring(bas+2, bit))); // getMsgHTML de olabilirdi
		}
		for(int bas = tmp.indexOf("#["); bas>0; bas=tmp.indexOf("#[",bas+2)){
			int bit = tmp.indexOf("]", bas+2);
			tmp.replace(bas, bit+1,PromisCache.getAppSettingStringValue(0, tmp.substring(bas+2, bit))); // getMsgHTML de olabilirdi
		}
		return tmp;
	}
	*/
	
	public static StringBuilder filter2(int customizationId,String xlocale, String code) {
		StringBuilder tmp = new StringBuilder();
		if(code==null || code.length()==0)return tmp;
		tmp.append(code);
		for(int bas = tmp.indexOf("${"); bas>0; bas=tmp.indexOf("${",bas+2)){
			int bit = tmp.indexOf("}", bas+2);
			tmp.replace(bas, bit+1, get2(customizationId, xlocale, tmp.substring(bas+2, bit))); // getMsgHTML de olabilirdi
		}
		for(int bas = tmp.indexOf("#["); bas>0; bas=tmp.indexOf("#[",bas+2)){
			int bit = tmp.indexOf("]", bas+2);
			tmp.replace(bas, bit+1,FrameworkCache.getAppSettingStringValue(0, tmp.substring(bas+2, bit))); // getMsgHTML de olabilirdi
		}
		return tmp;
	}

	public static void addLocaleMsgs2Cache(int customizationId, String locale, Map<String, String> localeMsgs) {
		if(localeMsgs == null) localeMsgs = new HashMap();
		if(localeMsgs.isEmpty()) localeMsgs.put("test", "test");
		for(String key: localeMsgs.keySet())
			set2(customizationId, locale, key, localeMsgs.get(key));
		
	}
}
