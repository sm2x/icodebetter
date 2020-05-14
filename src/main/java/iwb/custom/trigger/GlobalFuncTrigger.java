package iwb.custom.trigger;

import iwb.cache.LocaleMsgCache;
import iwb.domain.result.W5GlobalFuncResult;

public class GlobalFuncTrigger {
	public static void beforeExec(W5GlobalFuncResult r, String paramSuffix){
		switch(r.getGlobalFuncId()){
		
		}
	}
	
	public static void afterExec(W5GlobalFuncResult r, String paramSuffix){		
		switch (r.getGlobalFunc().getGlobalFuncId()) {
		case -478:
		case 925: // reload locale msg cache
			LocaleMsgCache.set2((Integer) r.getScd().get("customizationId"),
					r.getRequestParams().get("plocale" + paramSuffix),
					r.getRequestParams().get("plocale_msg_key" + paramSuffix),
					r.getRequestParams().get("pdsc" + paramSuffix));
			break;
		}
	}
}
