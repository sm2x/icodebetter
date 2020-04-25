/*
 * Created on 10.Mar.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package iwb.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import iwb.cache.FrameworkSetting;
import iwb.domain.helper.W5ReportCellHelper;
import iwb.util.GenericUtil;

/**
 * @author Administrator
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class RptExcelRenderer extends AbstractXlsView {
	private	List<W5ReportCellHelper> mva = null;


	
/*
	protected void buildExcelDocument(
			Map model, Workbook wb, HttpServletRequest request, HttpServletResponse response)
			throws NoSuchMessageException {
		final short[] alignMap=new short[]{CellStyle.ALIGN_CENTER, CellStyle.ALIGN_LEFT, CellStyle.ALIGN_RIGHT};
		mva = (List) model.get("report");
		
		// As we use a from scratch document, we create a new sheet.
		List<Sheet> sheetList = new ArrayList<Sheet>();
		Map<Integer, Short> kolonAlignList = new HashMap<Integer,Short>();
		
		// The same for properties data
		Font fp = wb.createFont();
//		fp.setColor((short) 0xc);
		CellStyle csp[] = {wb.createCellStyle(),wb.createCellStyle(),wb.createCellStyle()};
		csp[0].setFont(fp);
		csp[0].setAlignment(CellStyle.ALIGN_CENTER);
		csp[1].setFont(fp);
		csp[1].setAlignment(CellStyle.ALIGN_LEFT);
		csp[2].setFont(fp);
		csp[2].setAlignment(CellStyle.ALIGN_RIGHT);

		// We create a date style
		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setFont(fp);
		dateStyle.setAlignment(CellStyle.ALIGN_CENTER);
		dateStyle.setDataFormat(DataFormat.getBuiltinFormat("mm.dd.yyyy"));
		int carpan = 300;

		int rowid = 1, sheetIndex = 0, kolonSayisi = 0, lastRowId = 0, eksiRow = 0, artiRow = -1;
		Cell cell;	
		boolean baslikVar = false;

		for(W5ReportCellHelper rp : mva){
			if (rp.getColumnId()<1)
				continue;
			//hangi sheet in kullanılacağı belirleniyor
			if(rp.getRowTip()==100){//yeni sayfa
				Sheet sheet = wb.createSheet(rp.getDeger()== null ? "Page"+(sheetList.size()+1) : rp.getDeger());
				sheetList.add(sheet);	
				baslikVar = false;
				kolonSayisi = 0;
				artiRow = -1;
				eksiRow = eksiRow + lastRowId;
				if (sheetList.size()>1){
					for (int i = 0; i<kolonAlignList.size();i++){
						sheetList.get(sheetList.size()-1).setColumnWidth(i, sheetList.get(0).getColumnWidth(i));	
					}								
				}
			}
			if (sheetList.size()==0){
				Sheet sheet = wb.createSheet("Code2");
				sheetList.add(sheet);								
			}
			sheetIndex = sheetList.size()-1;
			
			int RowId = rp.getRowId() + artiRow - eksiRow;
			String tag = rp.getTag();	
			if (tag == null) tag = "";
			String[] tagList = tag.split(";");
			
			//içerik hazırlanıyor
			if(rp.getRowTip()==0){//başlık
				baslikVar = true;
				cell= getCell(sheetList.get(sheetIndex), 0, rp.getColumnId()-1);
				setCellStyle2(wb, cell, CellStyle.ALIGN_LEFT, (short)12, Font.BOLDWEIGHT_BOLD, "", "");
				cell.setCellValue(rp.getDeger());	
				artiRow = artiRow + 1;
			}
			else if(rp.getRowTip()==1){//parametre
				cell= getCell(sheetList.get(sheetIndex), RowId, rp.getColumnId()-1);				
				cell.setCellValue(tag + " : " + rp.getDeger());	
				if (rp.getColspan()==-1){
					carpan = 50;
				}
			}
			else if(rp.getRowTip()==2){//kolon başlığı
				cell= getCell(sheetList.get(sheetIndex), RowId , rp.getColumnId()-1);
				short cwidth = rp.getCellTip()>200 ? 200 : rp.getCellTip();
				sheetList.get(sheetIndex).setColumnWidth(rp.getColumnId()-1, (cwidth * carpan));				
				cell.setCellValue(rp.getDeger());	
				if (kolonSayisi<(rp.getColumnId()-1))
					kolonSayisi = rp.getColumnId()-1;				
		        try{setCellStyle2(wb, cell, (getTagValue(tagList, "A").equals("") ? CellStyle.ALIGN_CENTER : GenericUtil.uShort(getTagValue(tagList, "A"))), (short)10, Font.BOLDWEIGHT_BOLD, getTagValue(tagList, "B"), getTagValue(tagList, "BC"));}catch(Exception e){}	        
		        //başlık kısmının colspan ayarı yapılıyor
				if (baslikVar){ 
					//sheetList.get(sheetIndex).addMergedRegion(new Region(0,(short)0,0,(short)kolonSayisi));
				}
				int algn =1;
				if (rp.getColspan()==0) 
					algn=2;
				else if (rp.getColspan()==2) 
					algn = 3;					
				kolonAlignList.put(rp.getColumnId(), (short)algn);
			}
			else if(rp.getRowTip()==3){//kolon içeriği
				cell= getCell(sheetList.get(sheetIndex), RowId, rp.getColumnId()-1);	
				String dataType = getTagValue(tagList, "T");
				if ((dataType != null)&&(dataType.equals("1"))){
					try{
						cell.setCellValue(Double.parseDouble(rp.getDeger().replace(",", ".")));
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					}catch(Exception e){
						cell.setCellValue(rp.getDeger());
						cell.setCellType(Cell.CELL_TYPE_STRING);
					};
				}
				else if ((dataType != null)&&(dataType.equals("2"))){					
					try{
						cell.setCellType(Cell.CELL_TYPE_FORMULA);
						cell.setCellFormula(rp.getDeger());
					}catch(Exception e){cell.setCellValue(rp.getDeger());};					
				}
				else{
					cell.setCellValue(rp.getDeger());
					cell.setCellType(Cell.CELL_TYPE_STRING);
				}
				
				short cell_tip = rp.getCellTip();  
				short font_weight = Font.BOLDWEIGHT_NORMAL;
				if (cell_tip > 0){
					font_weight = Font.BOLDWEIGHT_BOLD;
				}
				try{setCellStyle2(wb, cell, (getTagValue(tagList, "A").equals("") ? kolonAlignList.get(rp.getColumnId()) : GenericUtil.uShort(getTagValue(tagList, "A"))), (short)10, font_weight, getTagValue(tagList, "B"),getTagValue(tagList, "BC"));}catch(Exception e){}
				
				if (rp.getColspan()>1){
					short toCol = (short)(rp.getColumnId()-1 + rp.getColspan()-1);
					sheetList.get(sheetIndex).addMergedRegion(new Region(RowId,(short)(rp.getColumnId()-1),RowId,toCol));
					cell= getCell(sheetList.get(sheetIndex), RowId, toCol);
					try{setCellStyle2(wb, cell, kolonAlignList.get(rp.getColumnId()), (short)10, font_weight, getTagValue(tagList, "B"),getTagValue(tagList, "BC"));}catch(Exception e){}
				}				
			}
			lastRowId= RowId;			
		}
				
		
	}
	*/
	private String getTagValue(String[] tagList, String key){
		String value = "";
		try
		{
			for(int i = 0; i<tagList.length; i++){
				String[] kv= tagList[i].split(":"); 
				if (kv[0].equals(key)){
					value = kv[1];
					break;
				}
			}
		} catch (Exception e) {
			if (FrameworkSetting.debug)
				e.printStackTrace();
		}	
		return value;
	}
	
	private void setCellStyle2(Workbook wb, Cell cell, short valign, short fontSize, short fontWeight, String border, String backroundColor) {
		Font f = wb.createFont();
		CellStyle style = wb.createCellStyle();
		style.setWrapText(true);
		f.setBoldweight(fontWeight);
		f.setFontHeightInPoints(fontSize);
		style.setFont(f);
		style.setAlignment(valign);
	    if (GenericUtil.uShort(border) != null){
	    	short borderw = GenericUtil.uShort(border);
			style.setBorderBottom(borderw);
			style.setBorderTop(borderw);
			style.setBorderLeft(borderw);
			style.setBorderRight(borderw);			
		};	
		if (GenericUtil.uShort(backroundColor) != null){
			style.setFillForegroundColor(GenericUtil.uShort(backroundColor));		 
			style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		}
        cell.setCellStyle(style);
    }

	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook wb, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		final short[] alignMap=new short[]{CellStyle.ALIGN_CENTER, CellStyle.ALIGN_LEFT, CellStyle.ALIGN_RIGHT};
		mva = (List) model.get("report");
		
		// As we use a from scratch document, we create a new sheet.
		List<Sheet> sheetList = new ArrayList<Sheet>();
		Map<Integer, Short> kolonAlignList = new HashMap<Integer,Short>();
		
		// The same for properties data
		Font fp = wb.createFont();
//		fp.setColor((short) 0xc);
		CellStyle csp[] = {wb.createCellStyle(),wb.createCellStyle(),wb.createCellStyle()};
		csp[0].setFont(fp);
		csp[0].setAlignment(CellStyle.ALIGN_CENTER);
		csp[1].setFont(fp);
		csp[1].setAlignment(CellStyle.ALIGN_LEFT);
		csp[2].setFont(fp);
		csp[2].setAlignment(CellStyle.ALIGN_RIGHT);

		// We create a date style
		CellStyle dateStyle = wb.createCellStyle();
		dateStyle.setFont(fp);
		dateStyle.setAlignment(CellStyle.ALIGN_CENTER);
//		dateStyle.setDataFormat(DataFormat.getBuiltinFormat("mm.dd.yyyy"));
		int carpan = 300;

		int rowid = 1, sheetIndex = 0, kolonSayisi = 0, lastRowId = 0, eksiRow = 0, artiRow = -1;
		Cell cell;	
		boolean baslikVar = false;

		for(W5ReportCellHelper rp : mva){
			if (rp.getColumnId()<1)
				continue;
			//hangi sheet in kullanılacağı belirleniyor
			if(rp.getRowTip()==100){//yeni sayfa
				Sheet sheet = wb.createSheet(rp.getDeger()== null ? "Page"+(sheetList.size()+1) : rp.getDeger());
				sheetList.add(sheet);	
				baslikVar = false;
				kolonSayisi = 0;
				artiRow = -1;
				eksiRow = eksiRow + lastRowId;
				if (sheetList.size()>1){
					for (int i = 0; i<kolonAlignList.size();i++){
						sheetList.get(sheetList.size()-1).setColumnWidth(i, sheetList.get(0).getColumnWidth(i));	
					}								
				}
			}
			if (sheetList.size()==0){
				Sheet sheet = wb.createSheet("Code2");
				sheetList.add(sheet);								
			}
			sheetIndex = sheetList.size()-1;
			
			int RowId = rp.getRowId() + artiRow - eksiRow;
			String tag = rp.getTag();	
			if (tag == null) tag = "";
			String[] tagList = tag.split(";");
			
			//içerik hazırlanıyor
			if(rp.getRowTip()==0){//başlık
				baslikVar = true;
				cell= getCell(sheetList.get(sheetIndex), 0, rp.getColumnId()-1);
				setCellStyle2(wb, cell, CellStyle.ALIGN_LEFT, (short)12, Font.BOLDWEIGHT_BOLD, "", "");
				cell.setCellValue(rp.getDeger());	
				artiRow = artiRow + 1;
			}
			else if(rp.getRowTip()==1){//parametre
				cell= getCell(sheetList.get(sheetIndex), RowId, rp.getColumnId()-1);				
				cell.setCellValue(tag + " : " + rp.getDeger());	
				if (rp.getColspan()==-1){
					carpan = 50;
				}
			}
			else if(rp.getRowTip()==2){//kolon başlığı
				cell= getCell(sheetList.get(sheetIndex), RowId , rp.getColumnId()-1);
				short cwidth = rp.getCellTip()>200 ? 200 : rp.getCellTip();
				sheetList.get(sheetIndex).setColumnWidth(rp.getColumnId()-1, (cwidth * carpan));				
				cell.setCellValue(rp.getDeger());	
				if (kolonSayisi<(rp.getColumnId()-1))
					kolonSayisi = rp.getColumnId()-1;				
		        try{setCellStyle2(wb, cell, (getTagValue(tagList, "A").equals("") ? CellStyle.ALIGN_CENTER : GenericUtil.uShort(getTagValue(tagList, "A"))), (short)10, Font.BOLDWEIGHT_BOLD, getTagValue(tagList, "B"), getTagValue(tagList, "BC"));}catch(Exception e){}	        
		        //başlık kısmının colspan ayarı yapılıyor
				if (baslikVar){ 
					//sheetList.get(sheetIndex).addMergedRegion(new Region(0,(short)0,0,(short)kolonSayisi));
				}
				int algn =1;
				if (rp.getColspan()==0) 
					algn=2;
				else if (rp.getColspan()==2) 
					algn = 3;					
				kolonAlignList.put(rp.getColumnId(), (short)algn);
			}
			else if(rp.getRowTip()==3){//kolon içeriği
				cell= getCell(sheetList.get(sheetIndex), RowId, rp.getColumnId()-1);	
				String dataType = getTagValue(tagList, "T");
				if ((dataType != null)&&(dataType.equals("1"))){
					try{
						cell.setCellValue(Double.parseDouble(rp.getDeger().replace(",", ".")));
						cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					}catch(Exception e){
						cell.setCellValue(rp.getDeger());
						cell.setCellType(Cell.CELL_TYPE_STRING);
					};
				}
				else if ((dataType != null)&&(dataType.equals("2"))){					
					try{
						cell.setCellType(Cell.CELL_TYPE_FORMULA);
						cell.setCellFormula(rp.getDeger());
					}catch(Exception e){cell.setCellValue(rp.getDeger());};					
				}
				else{
					cell.setCellValue(rp.getDeger());
					cell.setCellType(Cell.CELL_TYPE_STRING);
				}
				
				short cell_tip = rp.getCellTip();  
				short font_weight = Font.BOLDWEIGHT_NORMAL;
				if (cell_tip > 0){
					font_weight = Font.BOLDWEIGHT_BOLD;
				}
				try{setCellStyle2(wb, cell, (getTagValue(tagList, "A").equals("") ? kolonAlignList.get(rp.getColumnId()) : GenericUtil.uShort(getTagValue(tagList, "A"))), (short)10, font_weight, getTagValue(tagList, "B"),getTagValue(tagList, "BC"));}catch(Exception e){}
				
				if (rp.getColspan()>1){
					short toCol = (short)(rp.getColumnId()-1 + rp.getColspan()-1);
					sheetList.get(sheetIndex).addMergedRegion(new CellRangeAddress(RowId,(short)(rp.getColumnId()-1),RowId,toCol));
					cell= getCell(sheetList.get(sheetIndex), RowId, toCol);
					try{setCellStyle2(wb, cell, kolonAlignList.get(rp.getColumnId()), (short)10, font_weight, getTagValue(tagList, "B"),getTagValue(tagList, "BC"));}catch(Exception e){}
				}				
			}
			lastRowId= RowId;			
		}
						
	}

	private Cell getCell(Sheet sheet, int rownum, int cellnum) {
		Row row = sheet.getRow(rownum);
		if(row==null)row = sheet.createRow(rownum);
		Cell cell = row.getCell(cellnum);
		if(cell==null) cell = row.createCell(cellnum);
		return cell;

	}

}
