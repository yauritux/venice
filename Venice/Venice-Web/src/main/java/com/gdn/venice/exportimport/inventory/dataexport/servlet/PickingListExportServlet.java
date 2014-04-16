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
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.entity.module.outbound.InventoryRequest;
import com.gdn.inventory.exchange.entity.module.outbound.InventoryRequestItem;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackage;
import com.gdn.inventory.exchange.entity.module.outbound.PickingListDetail;
import com.gdn.inventory.wrapper.ResultListWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.exportimport.inventory.dataexport.PickingListPrint;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.app.inventory.service.PutawayManagementService;
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
		PutawayManagementService putawayService = new PutawayManagementService();
		String username = Util.getUserName(request);
		
		String packageIds = request.getParameter("packageIds");		
		
		String[] split = packageIds.split(";");
		List<String> packageIdList = new ArrayList<String>(); 
		
		for(String value:split){
			packageIdList.add(value);
		}
		
		System.out.println("packageIdList size: "+packageIdList.size());
			
		PickingListPrint plPrint = new PickingListPrint();
		List<PickingListPrint> plPrintList = new ArrayList<PickingListPrint>();
		
		for(String packageId : packageIdList){						
			System.out.println("package id: "+packageId);
			PickPackage pickPackage = pickingListService.getSinglePickingListIR(packageId);
			
			if(pickPackage!=null){
        		InventoryRequest inventoryRequest = pickPackage.getInventoryRequest();
        		
        		ResultListWrapper<InventoryRequestItem> irItemWrapper = pickingListService.getIRItemByIRId(Long.toString(inventoryRequest.getId()));
        		for(InventoryRequestItem irItem : irItemWrapper.getContents()){	        		
	        		HashMap<String, String> map = new HashMap<String, String>(); 
					map.put(DataNameTokens.INV_PICKINGLISTIR_INVENTORYREQUESTCODE, inventoryRequest.getIrNumber());
					map.put(DataNameTokens.INV_PICKINGLISTIR_WAREHOUSESKUID, irItem.getItem().getCode());
					map.put(DataNameTokens.INV_PICKINGLISTIR_WAREHOUSESKUNAME, irItem.getItem().getName());
					map.put(DataNameTokens.INV_PICKINGLISTIR_QTY, Long.toString(irItem.getQuantity()));
					
					WarehouseItem whItem = putawayService.getWarehouseItemData(irItem.getItem().getId(), 
							irItem.getInventoryRequest().getFromWarehouse().getId(), 
							irItem.getInventoryRequest().getSupplier().getId(), irItem.getInventoryRequest().getInventoryType());
					
					String shelfCode="";
                    if(whItem!=null){
    					System.out.println("whItem Id: "+whItem.getId());
                    	List<WarehouseItemStorageStock> storageStockList = putawayService.getWarehouseItemStorageList(whItem.getId());    	                    	
                    	for(WarehouseItemStorageStock storageStock : storageStockList){
                    		shelfCode+=storageStock.getStorage().getCode()+" / "+storageStock.getQuantity();
                    		shelfCode+=", ";
                    	}
                    	if(shelfCode.length()>1) shelfCode=shelfCode.substring(0, shelfCode.lastIndexOf(","));
                    	
                    	map.put(DataNameTokens.INV_PICKINGLISTIR_SHELFCODE, shelfCode);
                    }   
                    
//                    dataList.add(map);
        		}
        	}
			
			
			
			
			
			HashMap<String, String> params = new HashMap<String, String>();
	        params.put("username", username);
	        params.put("packageId", packageId);
	        
			RafDsRequest req = new RafDsRequest();
	        req.setParams(params);
	        					
	        ResultWrapper<WarehouseItem> whiWrapper = pickingListService.getWarehouseItem(req);
	        ResultWrapper<PickingListDetail> plWrapper = pickingListService.getPickingListDetail(req);	
	        if(whiWrapper!=null && whiWrapper.isSuccess() && plWrapper!=null && plWrapper.isSuccess()){
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
				headerRow.createCell(startCol+3).setCellValue(new HSSFRichTextString("Tipe Inventory"));
				headerRow.createCell(startCol+4).setCellValue(new HSSFRichTextString("Merchant"));
				headerRow.createCell(startCol+5).setCellValue(new HSSFRichTextString("Qty"));
					   
				//set style for header
				for(int i=startCol; i<=startCol+5; i++){
					HSSFCell cell = headerRow.getCell(i);
					cell.setCellStyle(headerCellstyle);
				}    
										
				//Looping for generating excel rows
				HSSFRow detailRow = null;
				for (int i = 0; i < plPrintList.size(); i++) {
					System.out.println("looping data");					
					PickingListPrint pl = plPrintList.get(i);	
					
					startRow=startRow+1;
					detailRow = sheet.createRow(startRow);
					
					_log.debug("processing row: "+i+" packageId: "+pl.getWarehouseItem().getId() +", warehouseItemCode: "+pl.getWarehouseItem().getCode());
					HSSFCell cell = detailRow.createCell(startCol);cell.setCellValue(new HSSFRichTextString(Integer.toString(i+1)));
					cell = detailRow.createCell(startCol+1);cell.setCellValue(new HSSFRichTextString(pl.getWarehouseItem().getItem().getCode()));
					cell = detailRow.createCell(startCol+2);cell.setCellValue(new HSSFRichTextString(pl.getWarehouseItem().getItem().getName()));
					cell = detailRow.createCell(startCol+3);cell.setCellValue(new HSSFRichTextString(pl.getWarehouseItem().getStockType().name()));	
					cell = detailRow.createCell(startCol+4);cell.setCellValue(new HSSFRichTextString(pl.getWarehouseItem().getSupplier().getName()));
					cell = detailRow.createCell(startCol+5);cell.setCellValue(new HSSFRichTextString(Integer.toString(pl.getWarehouseItem().getStock())));
					
					//set style for list
					for(int l=startCol; l<=startCol+5; l++){
						HSSFCell cell2 = detailRow.getCell(l);
						cell2.setCellStyle(detailCellstyle);
					}	
				}		
				
				//set style for list
				for(int l=startCol; l<=startCol+5; l++){
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
