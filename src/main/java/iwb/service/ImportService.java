package iwb.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iwb.dao.rdbms_impl.PostgreSQL;
import iwb.domain.db.W5ExcelImport;
import iwb.domain.db.W5ExcelImportSheet;
import iwb.domain.db.W5ExcelImportSheetData;
import iwb.engine.GlobalScriptEngine;
import iwb.util.GenericUtil;

@Service
@Transactional
public class ImportService {
	@Lazy
	@Autowired
	private PostgreSQL dao;
	
	@Lazy
	@Autowired
	private GlobalScriptEngine scriptEngine;
	
	public int saveExcelImport(Map<String, Object> scd, String fileName, String systemFileName, LinkedHashMap<String, List<HashMap<String, String>>> parsedData) {
    	W5ExcelImport im = new W5ExcelImport();
    	im.setProjectUuid((String)scd.get("projectId"));
    	im.setDsc(fileName);
    	im.setInsertUserId(GenericUtil.uInt(scd.get("userId")));
    	im.setSystemFileName(systemFileName);
    	dao.saveObject(im);
    	short sheetNo = 1;
    	for(Entry<String, List<HashMap<String, String>>> sheet : parsedData.entrySet())if(sheet.getValue()!=null && sheet.getValue().size()>0){
        	W5ExcelImportSheet ims = new W5ExcelImportSheet();
        	ims.setProjectUuid(im.getProjectUuid());
        	ims.setDsc(sheet.getKey());
        	ims.setTabOrder(sheetNo++);
        	ims.setExcelImportId(im.getExcelImportId());
        	dao.saveObject(ims); 	
        	
        	List<Object> toBeSaved = new ArrayList();

    		for(int i=0; i<sheet.getValue().size(); i++){
	    		W5ExcelImportSheetData imd = new W5ExcelImportSheetData();
	    		imd.setRowNo(i+1);
	    		imd.setExcelImportSheetId(ims.getExcelImportSheetId());
	    		imd.setProjectUuid(im.getProjectUuid());
	    		for(Entry<String, String> entryCols : sheet.getValue().get(i).entrySet()){
	    			imd.setCell(entryCols.getKey(),entryCols.getValue());	
	    		}
	    		toBeSaved.add(imd);
    		}
    		if(toBeSaved.size()>0)for(Object o:toBeSaved) dao.saveObject(o);
    	}

    	return im.getExcelImportId();
		
		
	}


	public void importAppMaker(Map<String, Object> scd, String name, String body, String script) {
		Map m = new HashMap();
		m.put("pbody", body);
		if(name.indexOf('/')>-1) {
			m.put("pname", name.substring(name.indexOf('/')+1));
			String dir  = name.substring(0,name.indexOf('/'));
			if(dir.equals("models")) {
				scriptEngine.executeGlobalFunc(scd, 467, m, (short) 1);
			} else if(dir.equals("pages")) {
				scriptEngine.executeGlobalFunc(scd, 621, m, (short) 1);
			} else if(dir.equals("relations")) {
				scriptEngine.executeGlobalFunc(scd, 5, m, (short) 1);
			} else if(dir.equals("scripts")) {
				m.put("pscript", script);
				scriptEngine.executeGlobalFunc(scd, 269, m, (short) 1);
			}
		} else {
			m.put("pname", name);
			scriptEngine.executeGlobalFunc(scd, 1204, m, (short) 1);
		}
			
		
	}
}
