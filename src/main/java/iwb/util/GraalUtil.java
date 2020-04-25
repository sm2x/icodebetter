package iwb.util;

public class GraalUtil {
	/*
	public static Map fromGraalValue2Map(Object reqP) {
		Map<String, Object> rp = new HashMap<String, Object>();
		if(reqP==null)return rp;
		if(reqP instanceof Value) {
			Value jsRequestParams = (Value)reqP;
			if(jsRequestParams.hasArrayElements()) {
				rp.put("rhino", fromGraalValue2List(jsRequestParams));
			} else for (String key : jsRequestParams.getMemberKeys()) {
				Value o = jsRequestParams.getMember(key);
				if (o != null) {
					if(o.hasArrayElements()) {
						rp.put(key, fromGraalValue2List(o));
					} else if(o.hasMembers()) {
						rp.put(key, fromGraalValue2Map(o));
					} else {
						String res = o.toString();
						if (res.endsWith(".0") && GenericUtil.uInt(res.substring(0, res.length() - 2)) > 0)
							res = res.substring(0, res.length() - 2);
						rp.put(key, res);
					}
				}
			}
			return rp;
			
		}
		if(!(reqP instanceof Map)) {
			try {
				reqP = GenericUtil.fromJSONObjectToMap(new JSONObject(reqP.toString()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(reqP instanceof Map) {
			Map jsRequestParams = (Map)reqP;
			for (Object key : jsRequestParams.keySet()) {
				Object o = jsRequestParams.get(key.toString());
				if (o != null) {
					if(o  instanceof List) {
						rp.put(key.toString(), o); //fromGraalValue2List(o)
					} else if(o  instanceof Map) {
						rp.put(key.toString(), o); //fromGraalValue2Map(o)
					} else {
						String res = o.toString();
						if (res.endsWith(".0") && GenericUtil.uInt(res.substring(0, res.length() - 2)) > 0)
							res = res.substring(0, res.length() - 2);
						rp.put(key.toString(), res);
					}
				}
			}
		}
		return rp;
	}

	public static List fromGraalValue2List(Value o) {
		List<Object> ll = new ArrayList();
		if(o==null)return ll;
		if(o.hasArrayElements())for(int qi=0;qi<o.getArraySize();qi++) {
			Value oo = o.getArrayElement(qi);
			if(oo==null) {
				ll.add(oo);
			} else if(oo.hasArrayElements()){
				ll.add(fromGraalValue2List(oo));
			} else if(oo.hasMembers())  {
				ll.add(fromGraalValue2Map(oo));
			} else {
				String res = oo.toString();
				if (res.endsWith(".0") && GenericUtil.uInt(res.substring(0, res.length() - 2)) > 0)
					res = res.substring(0, res.length() - 2);
				ll.add(res);
			}
			
		}
		return ll;
	}
*/
}
