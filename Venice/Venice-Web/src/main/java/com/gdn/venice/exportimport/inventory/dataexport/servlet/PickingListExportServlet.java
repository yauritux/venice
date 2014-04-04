package com.gdn.venice.exportimport.inventory.dataexport.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.module.outbound.PickingListDetail;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.exportimport.inventory.dataexport.PickingListPrint;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.util.Util;

/**
 * Servlet implementation class PickingListExportServlet.
 * 
 * @author Roland
 * 
 */
public class PickingListExportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected static Logger _log = null;

	public PickingListExportServlet() {
		super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory.getLog4JLogger("com.gdn.venice.exportimport.inventory.dataexport.servlet.PickingListExportServlet");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		service(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		service(request, response);
	}
	
	protected void service(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		System.out.println("PickingListExportServlet");
		
		PickingListManagementService pickingListService = new PickingListManagementService();
		String username = Util.getUserName(request);
		
		String warehouseItemIds = request.getParameter("warehouseItemIds");		
		
		String[] split = warehouseItemIds.split(";");
		List<String> warehouseItemIdList = new ArrayList<String>(); 
		
		for(String value:split){
			warehouseItemIdList.add(value);
		}
		
		System.out.println("warehouseItemIdList size: "+warehouseItemIdList.size());
			
		PickingListPrint plPrint = new PickingListPrint();
		List<PickingListPrint> plPrintList = new ArrayList<PickingListPrint>();
		
		for(String warehouseItemId : warehouseItemIdList){						
			System.out.println("warehouse item id: "+warehouseItemId);
			HashMap<String, String> params = new HashMap<String, String>();
	        params.put("username", username);
	        params.put("warehouseItemId", warehouseItemId);
	        
			RafDsRequest req = new RafDsRequest();
	        req.setParams(params);
	        					
	        ResultWrapper<WarehouseItem> whiWrapper = pickingListService.getWarehouseItem(req);
	        ResultWrapper<PickingListDetail> plWrapper = pickingListService.getPickingListDetail(req);	
	        if(whiWrapper!=null && plWrapper!=null){
	        	System.out.println("set warehouse item");
	        	plPrint.setWarehouseItem(whiWrapper.getContent());

	        	System.out.println("set storage");
	        	plPrint.setWhItemStorageStock(plWrapper.getContent().getWhItemStorageStock());
	        	
				plPrintList.add(plPrint);
	        }
		}				
			
		System.out.println("plPrintList size: "+plPrintList.size()+", start print report to excel");
		OutputStream out = null;

		try {
			String shortname="PickingList" + System.currentTimeMillis() + ".xls";								
						
			if(plPrintList!=null && plPrintList.size()>0){
				response.setContentType("application/vnd.ms-excel");
				response.setHeader("Content-Disposition",  "attachment; filename="+shortname);

				HSSFWorkbook wb = new HSSFWorkbook(); 
				HSSFSheet sheet = wb.createSheet("Picking List");				
				 
				//style
				CellStyle headerCellstyle = wb.createCellStyle();
				headerCellstyle.setBorderBottom(CellStyle.BORDER_THIN);
				headerCellstyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
				headerCellstyle.setBorderLeft(CellStyle.BORDER_THIN);
				headerCellstyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
				headerCellstyle.setBorderRight(CellStyle.BORDER_THIN);
				headerCellstyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
				headerCellstyle.setBorderTop(CellStyle.BORDER_THIN);
				headerCellstyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
				headerCellstyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
				headerCellstyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
				headerCellstyle.setAlignment(CellStyle.ALIGN_CENTER);	 
					 
				CellStyle detailCellstyle = wb.createCellStyle();
				detailCellstyle.setBorderBottom(CellStyle.BORDER_THIN);
				detailCellstyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
				detailCellstyle.setBorderLeft(CellStyle.BORDER_THIN);
				detailCellstyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
				detailCellstyle.setBorderRight(CellStyle.BORDER_THIN);
				detailCellstyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
				detailCellstyle.setBorderTop(CellStyle.BORDER_THIN);
				detailCellstyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
				detailCellstyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				detailCellstyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
				detailCellstyle.setAlignment(CellStyle.ALIGN_CENTER);	 
				 
				int startRow = 0;
				int startCol=0;
				
				HSSFRow headerRow = sheet.createRow((short) startRow);
				headerRow.createCell(startCol).setCellValue(new HSSFRichTextString("No"));
				headerRow.createCell(startCol+1).setCellValue(new HSSFRichTextString("Warehouse SKU ID"));
				headerRow.createCell(startCol+2).setCellValue(new HSSFRichTextString("Item SKU Name"));
				headerRow.createCell(startCol+3).setCellValue(new HSSFRichTextString("Qty"));
				headerRow.createCell(startCol+4).setCellValue(new HSSFRichTextString("UoM"));
					   
				//set style for header
				for(int i=startCol; i<=startCol+4; i++){
					HSSFCell cell = headerRow.getCell(i);
					cell.setCellStyle(headerCellstyle);
				}    
										
				//Looping for generating excel rows
				HSSFRow row = null;
				for (int i = 0; i < plPrintList.size(); i++) {
					System.out.println("looping data");					
					PickingListPrint pl = plPrintList.get(i);	
					
					startRow=startRow+1;
					row = sheet.createRow(startRow);
					
					_log.debug("processing row: "+i+" warehouseItemId: "+pl.getWarehouseItem().getId() +", warehouseItemCode: "+pl.getWarehouseItem().getCode());
					HSSFCell cell = row.createCell(startCol);cell.setCellValue(new HSSFRichTextString(Integer.toString(i+1)));
					cell = row.createCell(startCol+1);cell.setCellValue(new HSSFRichTextString(pl.getWarehouseItem().getItem().getCode()));
					cell = row.createCell(startCol+2);cell.setCellValue(new HSSFRichTextString(pl.getWarehouseItem().getItem().getName()));
					cell = row.createCell(startCol+3);cell.setCellValue(new HSSFRichTextString(pl.getWarehouseItem().getSumSO().toString()));	
					cell = row.createCell(startCol+4);cell.setCellValue(new HSSFRichTextString(pl.getWarehouseItem().getItem().getItemUnit()));
					
					//set style for list
					for(int l=startCol; l<=startCol+4; l++){
						HSSFCell cell2 = row.getCell(l);
						cell2.setCellStyle(detailCellstyle);
					}	
				}		
				
				//set style for list
				for(int l=startCol; l<=startCol+4; l++){
					sheet.autoSizeColumn(l);
				}	
				
				System.out.println("write to stream");	
				out = response.getOutputStream();			
				wb.write(out);					
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {		    
			try {				
				out.flush();
				out.close();				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
