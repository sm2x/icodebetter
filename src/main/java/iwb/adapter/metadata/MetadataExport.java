package iwb.adapter.metadata;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import iwb.cache.FrameworkSetting;
import iwb.model.db.W5Project;
import iwb.util.GenericUtil;

public class MetadataExport {
	
	private JSONObject java2json(Object o) {
		JSONObject j = new JSONObject();
		Class c = o.getClass();
		for(Method m:c.getMethods()) if(m.getName().length()>3 && m.getName().startsWith("get") && !m.getName().startsWith("getClass") && !m.getName().startsWith("get_") && !m.getName().equals("getProjectUuid")
				 && !m.getName().equals("getCustomizationId") && m.getParameterCount()==0)try{
			String mname = m.getName();
			mname = mname.substring(3,4).toLowerCase(FrameworkSetting.appLocale) + mname.substring(4);
			Object oo = m.invoke(o);
			if(oo==null)continue;
			if(m.getReturnType().isPrimitive()) {
				if(m.getReturnType() == Integer.TYPE) {
					if((Integer)oo == 0)continue;
				} else	if(m.getReturnType() == Short.TYPE) {
					if((Short)oo == 0)continue;
				} 
			}
			j.put(mname, oo);			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return j;
	}
	
	public String toJson(Map<String, Object> m) {
		StringBuilder s = new StringBuilder();
		s.append("{");
		String projectId = null;
		
		boolean b = false;
		for(String k:m.keySet()) {
			Object o = m.get(k);
			if(o==null)continue;
			String ss = null;
			if(o instanceof List) {
				List l = (List)o;
				if(!l.isEmpty()) {
					JSONArray ar = new JSONArray();
					for(Object oo:l)ar.put(java2json(oo));
					ss = ar.toString();
					
				}
			} else if(o instanceof Map){
				ss = GenericUtil.fromMapToJsonString2((Map)o);
			} else {
				ss = java2json(o).toString();
				if(o instanceof W5Project) {
					projectId = ((W5Project)o).getProjectUuid();
				}
			}
			if(ss == null || ss.length()==0)continue;
			if(b)s.append(",\n"); else b=true;
			s.append("\"").append(k).append("\":").append(ss);
		}
		if(projectId!=null)
			s.append(",\n\"projectId\":\"").append(projectId).append("\"");
		s.append("\n}");
		return s.toString();
	}
}
