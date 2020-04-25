package iwb.report;

import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import iwb.cache.FrameworkSetting;
import iwb.domain.helper.W5ReportCellHelper;
import iwb.util.GenericUtil;

/**
 * This view demonstrates how to send a PDF file with the Spring Framework
 * using the iText PDF library.
 *
 * @author Jean-Pierre Pawlak
 */
public class RptPdfRenderer extends AbstractPdfView {

	private Font HEADER_FONT;
	private Font PARAM_FONT;
	private Font PARAM_BOLD_FONT;
	private Font GROUP_FONT;
	private Font DATA_FONT;
	private Font COLUMN_FONT;
	private static BaseFont bf2=null;	
	private String logoFilePath = "";

	final int[] alignMap=new int[]{Element.ALIGN_CENTER, Element.ALIGN_LEFT, Element.ALIGN_RIGHT};

    protected final Log log = LogFactory.getLog(getClass());

    public RptPdfRenderer(String logoFilePath) {
    	this.logoFilePath = logoFilePath;
	}

	public boolean	isNewPage;
	@SuppressWarnings("unchecked")
	@Override
	protected void buildPdfMetadata(Map model, Document document, HttpServletRequest request) {
		document.addTitle("Rapor");
		document.addCreator("Promis LTD Sti.");

		mva = (List) model.get("report");

		Rectangle rec = PageSize.A4;
		baslik=mva.get(0);
		if(baslik.getCellTip()!=0)rec= rec.rotate();
		document.setPageSize(rec);
		document.setMargins(baslik.getColspan(), baslik.getColspan(), baslik.getColspan(), baslik.getColspan());

	}

	private	List<W5ReportCellHelper> mva = null;
	private	W5ReportCellHelper baslik = null;

	private void yapColumns(PdfPTable table,int columnCount, int[] column_widths,String[] column_labels, PdfPTable header_table, Map<String,Object> scd){
		int[] tmp_column_widths = new int[columnCount];
		for(int i=0;i<columnCount;i++)
			tmp_column_widths[i]=column_widths[i];
		try {
			table.setWidths(tmp_column_widths);
		} catch (DocumentException e) {
			if(FrameworkSetting.debug)e.printStackTrace();
		}

		table.setWidthPercentage(100);
		table.getDefaultCell().setColspan(columnCount);
        if(!GenericUtil.isEmpty(logoFilePath))try {        	        	 	       
            Image x = Image.getInstance(logoFilePath);
            x.scalePercent(50);
            Chunk ck = new Chunk(x, 0, 0);
            Phrase ph = new Phrase();
            ph.add(ck);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            table.addCell(ph);
        } catch (Exception ex) {
			if(FrameworkSetting.debug)ex.printStackTrace();
        }

        table.getDefaultCell().setBorder(Rectangle.BOX);
		table.getDefaultCell().setBorderWidth(0.4f);
		table.getDefaultCell().setPadding(10);
		table.getDefaultCell().setBorderColor(Color.black);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		table.getDefaultCell().setVerticalAlignment( Element.ALIGN_MIDDLE);
		table.addCell(new Phrase(baslik.getDeger(), HEADER_FONT));

		table.addCell(header_table);
		table.getDefaultCell().setColspan(1);
		table.getDefaultCell().setBorderWidth(0.4f);
		table.getDefaultCell().setBorderColor(Color.black);
		table.getDefaultCell().setGrayFill(0.90f);
		table.getDefaultCell().setPadding(2);
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
		table.getDefaultCell().setColspan(1);
		for(int i=0;i<columnCount;i++)
			table.addCell(new Phrase(column_labels[i], COLUMN_FONT));
		
		table.setHeaderRows(3);
		table.getDefaultCell().setBorderWidth(0.2f);
		table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
	}

	private void yapHeader(PdfPTable table)throws DocumentException, NoSuchMessageException{
		if(baslik.getTag()==null || baslik.getTag().length()==0)return; //2^20,30,20,30|...
		String	tags = baslik.getTag();//.split("|");
		String[]	paramTags = tags.split(";");
		int	paramColumnNo	= 2*GenericUtil.uInt(paramTags[0]);
		if(paramColumnNo==0)return;
		PdfPTable param_table = new PdfPTable(paramColumnNo);
		int headerwidths_param[] = new int[paramColumnNo];
		String[]	paramColumnWidths = paramTags[1].split(",");
		for(int i=0;i<paramColumnNo;i++)headerwidths_param[i]=GenericUtil.uInt(paramColumnWidths[i]);
		param_table.setWidths(headerwidths_param);
		param_table.setWidthPercentage(100);
		param_table.getDefaultCell().setBorderWidth(0);
		param_table.getDefaultCell().setPadding(1);
		param_table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
		param_table.getDefaultCell().setVerticalAlignment( Element.ALIGN_MIDDLE);
		param_table.getDefaultCell().setGrayFill(1f);
		for(W5ReportCellHelper sp:mva)if(sp.getRowTip()==1){
			param_table.getDefaultCell().setColspan(1);
			param_table.addCell(new Phrase(sp.getTag(), PARAM_FONT));
			if(sp.getCellTip()>1)param_table.getDefaultCell().setColspan(sp.getCellTip());
			String str=": ";if(sp.getDeger()!=null)str+=sp.getDeger();
			param_table.addCell(new Phrase(str, PARAM_BOLD_FONT));
		}else if(sp.getRowTip()>1)break;
		table.addCell(param_table);
	}

	private	void statDuz(Document document, Map<String,Object> scd)throws DocumentException, NoSuchMessageException {
		PdfPTable table = null, header_table = null;
		int renderState=0;//0:not started, 1:settin headerparams, 2:setting columns, 3:rendering
		int columnCount = 0;
		int	headerColumnCount	= 0;
		int[] column_widths = new int[1000];
		int[] column_aligns = new int[1000];
		String[] column_labels = new String[1000];
		int[] header_column_widths = null;
		boolean even = false;
		float grayFill;
		int currentColumn = 1;
		int currentRow = -1;
		
		for(W5ReportCellHelper sonuc:mva)switch(sonuc.getRowTip()) {
		case 1: // header set, params
			if(renderState!=1){
				if(baslik.getTag()==null || baslik.getTag().length()==0)break; //2^20,30,20,30|...
				String tags = baslik.getTag();//.split("|");
				String[] paramTags = tags.split(";");
				headerColumnCount = 2*GenericUtil.uInt(paramTags[0]);
				if(headerColumnCount==0)break;
				header_column_widths = new int[headerColumnCount];
				header_table = new PdfPTable(headerColumnCount);
				String[] paramColumnWidths = paramTags[1].split(",");
				for(int i=0;i<headerColumnCount;i++)header_column_widths[i]=GenericUtil.uInt(paramColumnWidths[i]);
				header_table.setWidths(header_column_widths);
				header_table.setWidthPercentage(100);
				header_table.getDefaultCell().setBorderWidth(0);
				header_table.getDefaultCell().setPadding(1);
				header_table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
				header_table.getDefaultCell().setVerticalAlignment( Element.ALIGN_MIDDLE);
				header_table.getDefaultCell().setGrayFill(1f);
				renderState=1;
				
			}
            header_table.getDefaultCell().setColspan(1);			
			header_table.addCell(new Phrase(sonuc.getTag(), PARAM_FONT));
			if(sonuc.getCellTip()>1)header_table.getDefaultCell().setColspan(sonuc.getCellTip());
			String str=": ";if(sonuc.getDeger()!=null)str+=sonuc.getDeger();
			header_table.addCell(new Phrase(str, PARAM_BOLD_FONT));
                        /*Image jpg ;
                        try{
                            jpg = Image.getInstance("http://promis.com.tr/images/promis_logo_oil&gas.jpg");
                            header_table.addCell(jpg);
                        }
                        catch(Exception ex){
                            ex.printStackTrace();
                        }*/

			break;
		case 2: // columns set
			if(renderState!=2){
				columnCount=0;
				renderState=2;
				if(table!=null){
					while(currentColumn<= columnCount){
						table.addCell(new Phrase(" ", DATA_FONT));
						currentColumn++;
					}
					document.add(table);
					table=null;
				}
			}
			column_widths[columnCount] = sonuc.getCellTip();
			column_labels[columnCount] = sonuc.getDeger();
			column_aligns[++columnCount] = alignMap[sonuc.getColspan()];
			break;
		case 100: // new page
			if(table!=null){
				while(currentColumn<= columnCount){
					table.addCell(new Phrase(" ", DATA_FONT));
					currentColumn++;
				}
				document.add(table);
				table=null;
				document.newPage();
				
				Rectangle rec = PageSize.A4;
				if(sonuc.getCellTip()!=0)rec= rec.rotate();
				document.setPageSize(rec);
				document.setMargins(sonuc.getColspan(), sonuc.getColspan(), sonuc.getColspan(), sonuc.getColspan());				
			}			
			renderState=0;
			break;
		case 3: //data
			if(renderState!=3){
				table = new PdfPTable(columnCount);
				yapColumns(table, columnCount, column_widths,column_labels, header_table, scd);
				renderState=3;
			}
			int sonucRow = sonuc.getRowId();
			if(currentRow==-1 || currentRow< sonucRow){
				if(currentRow!=-1 && currentRow< sonucRow){ // ekle sonuna kadar olanlari
					while(currentColumn<= columnCount){
						table.getDefaultCell().setColspan(1);
						table.addCell(new Phrase(" ", DATA_FONT));
						currentColumn++;
					}
				}
				
				switch(sonuc.getCellTip()){
				case	5:
					table.getDefaultCell().setBackgroundColor(Color.CYAN);
				case	0:
					if (even) {
						grayFill = 0.95f;
						even = false;
					} else {
						grayFill = 1.00f;
						even = true;
					}
					table.getDefaultCell().setGrayFill(grayFill);
					break; // sradan
				case	1:
					table.getDefaultCell().setGrayFill(0.80f);
					break; // group header
				case	2:
					table.getDefaultCell().setGrayFill(0.80f);
					break; // group footer
				case	3:break; // transperent border
				case	4:
					table.getDefaultCell().setColspan(columnCount);
					table.getDefaultCell().setFixedHeight(Float.parseFloat(sonuc.getDeger()));
					table.getDefaultCell().setGrayFill(1f);
//					table.getDefaultCell().setBorderColor(Color.white);
					table.addCell(new Phrase(" ", DATA_FONT));
//					table.getDefaultCell().setBorderColor(Color.black);
					table.getDefaultCell().setFixedHeight(0.0f);
					table.getDefaultCell().setColspan(1);
					currentColumn = 1;
					continue;// bos bir satir eklemek. yuksekligi deger kadar
				}
					
				currentRow = sonucRow;
				currentColumn = 1;

			}
			
			
			str = " ";
			if(sonuc.getDeger()!=null && sonuc.getDeger().length()>0) str=sonuc.getDeger();
			
			while(currentColumn< sonuc.getColumnId()){
				table.getDefaultCell().setColspan(1);
				table.addCell(new Phrase(" ", DATA_FONT));
				currentColumn++;
			}
			
			table.getDefaultCell().setHorizontalAlignment(column_aligns[currentColumn]);
			table.getDefaultCell().setColspan(sonuc.getColspan());
			
			if(sonuc.getCellTip()==5){
				StringReader HTMLreader = new StringReader(str);
				try {
	
					Object[] xx = HTMLWorker.parseToList(HTMLreader,null).toArray();
					//Phrase p = new Phrase();
					for(int i=0 ; i<xx.length; i++){
						table.addCell(new Phrase(((Paragraph)xx[i]).getContent(), DATA_FONT));
					}
					//table.addCell(new Phrase(str, sonuc.getCellTip()==0 ? DATA_FONT : GROUP_FONT));*/
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
			
			else{
				table.addCell(new Phrase(str, sonuc.getCellTip()==0 ? DATA_FONT : GROUP_FONT));
			}
			
			currentColumn+=sonuc.getColspan();
			if(currentColumn>columnCount){ // satir sonuna geldi asagi inmek lazim
				currentRow=-1;
			}
		}
		
		if(table!=null){
			while(currentColumn<= columnCount){
				table.addCell(new Phrase(" ", DATA_FONT));
				currentColumn++;
			}
			document.add(table);
		}
	}
	@Override
	protected void buildPdfDocument(Map model, Document document,	PdfWriter writer,
			HttpServletRequest request, HttpServletResponse response)
			throws DocumentException, NoSuchMessageException {


		int yaziBoyut = 8;
		@SuppressWarnings("unchecked")
		Map<String,Object> scd = (Map<String, Object>) model.get("scd");
		try{
			if(bf2==null)bf2 = BaseFont.createFont( BaseFont.HELVETICA, "windows-1254", true );
			HEADER_FONT = new Font(bf2, yaziBoyut+5, Font.BOLD, Color.black );
			PARAM_FONT = new Font(bf2, yaziBoyut+2, Font.NORMAL, Color.black );
			PARAM_BOLD_FONT = new Font(bf2, yaziBoyut+2, Font.BOLD, Color.black );
			GROUP_FONT = new Font(bf2, yaziBoyut+3, Font.BOLD, Color.black );
			DATA_FONT = new Font(bf2, yaziBoyut, Font.NORMAL, Color.black );
			COLUMN_FONT = new Font(bf2, yaziBoyut, Font.ITALIC, Color.black );
		} catch(Exception e){ log.error("buildPdfDocument: "+e.toString() + " =+= " + e.getLocalizedMessage());return;}

		// We search the data to insert.


			MyPageEvents eventsDuz = new MyPageEvents();
			writer.setPageEvent(eventsDuz);
			eventsDuz.onOpenDocument(writer, document);

			statDuz(document,scd);

	}


	//~ Inner Classes ----------------------------------------------------------

	private static class MyPageEvents extends PdfPageEventHelper {

	    protected final Log log = LogFactory.getLog(getClass());

		// This is the contentbyte object of the writer
		PdfContentByte cb;

		// we will put the final number of pages in a template
		PdfTemplate template;
		int endpage=0;
		// we override the onOpenDocument method
		@Override
		public void onOpenDocument(PdfWriter writer, Document document) {
			cb = writer.getDirectContent();
			template = cb.createTemplate(50, 50);
		}

		// we override the onEndPage method
		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			int pageN = writer.getPageNumber();
			String text = "Sayfa : " + pageN + " / ";
//			messageSourceAccessor.getMessage("page", "page", loc) + " " + pageN + " " + messageSourceAccessor.getMessage("on", "on", loc) + " ";
			float  len = bf2.getWidthPoint( text, 8 );
			cb.beginText();
			cb.setFontAndSize(bf2, 8);

		//	float bas = (document.getPageSize().width()-50)/2;
			float bas = (document.getPageSize().getWidth()-50)/2;
			cb.setTextMatrix(bas, 16); //MARGIN
			cb.showText(text);
			cb.endText();

			cb.addTemplate(template, bas + len, 16);
			cb.beginText();
			cb.setFontAndSize(bf2, 8);

			cb.endText();
			endpage=1;
		}

		// we override the onCloseDocument method
		@Override
		public void onCloseDocument(PdfWriter writer, Document document) {

			template.beginText();
			template.setFontAndSize(bf2, 8);
			template.showText(String.valueOf( writer.getPageNumber() - 1 ));
			template.endText();
		}

	    @Override
		public void onStartPage(PdfWriter writer,Document document) {
	    }

	}


}
