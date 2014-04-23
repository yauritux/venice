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
import com.gdn.inventory.exchange.entity.module.outbound.PickPackage;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackageSalesOrder;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultListWrapper;
import com.gdn.venice.exportimport.inventory.dataexport.PickingListPrint;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.app.inventory.service.PutawayManagementService;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.util.Util;

/**
 * Servlet implementation class PickingListSOExportServlet.
 * 
 * @author Roland
 * 
 */
public class PickingListSOExportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected static Logger _log = null;

	public PickingListSOExportServlet() {
		super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory.getLog4JLogger("com.gdn.venice.exportimport.inventory.dataexport.servlet.PickingListSOExportServlet");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		service(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		service(request, response);
	}
	
	protected void service(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		System.out.println("PickingListSOExportServlet");
		
		PickingListPrint plPrint = new PickingListPrint();
		List<PickingListPrint> plPrintList = new ArrayList<PickingListPrint>();
		
		PickingListManagementService pickingListService = new PickingListManagementService();
		PutawayManagementService putawayService = new PutawayManagementService();
		
		RafDsRequest rafDsRequest = new RafDsRequest();
		HashMap<String, String> params = new HashMap<String, String>();
        params.put("page", "1");
		params.put("limit", "100");
        rafDsRequest.setParams(params);       

        InventoryPagingWrapper<PickPackage> packageWrapper = pickingListService.getPackageSOByPicker(Util.getUserName(request), request.getParameter("pickerName"), request.getParameter("warehouseId"), rafDsRequest);
		if(packageWrapper!=null && packageWrapper.isSuccess()){							
			for(PickPackage pickPackage : packageWrapper.getContent()){					
				ResultListWrapper<PickPackageSalesOrder> pickPackageWrapper = pickingListService.getPickingListSODetail(Util.getUserName(request), pickPackage.getId().toString());
	        	if(pickPackageWrapper!=null && pickPackageWrapper.isSuccess()){
	        		for(PickPackageSalesOrder ppso : pickPackageWrapper.getContents()){ 
	        			plPrint.setSalesOrder(ppso.getSalesOrder());
	        			plPrint.setPickPackage(ppso.getPickPackage());
	        			plPrint.setWarehouseSkuId(ppso.getSalesOrder().getAssignedItem().getCode());
	        			plPrint.setItemName(ppso.getSalesOrder().getAssignedItem().getName());
	        									            			
						WarehouseItem whItem = putawayService.getWarehouseItemData(ppso.getSalesOrder().getAssignedItem().getId(), 
								ppso.getSalesOrder().getWarehouse().getId(), 
								ppso.getSalesOrder().getSupplier().getId(), ppso.getSalesOrder().getStockType());
						        			
                        if(whItem!=null){
        					System.out.println("whItem Id: "+whItem.getId());
                        	List<WarehouseItemStorageStock> storageStockList = putawayService.getWarehouseItemStorageList(whItem.getId());
                        	int counter = 0;
                        	for(WarehouseItemStorageStock storageStock : storageStockList){
                        		if(counter==0){
                        			System.out.println("add first stock");
                        			plPrint.setQty(Integer.toString(ppso.getSalesOrder().getQuantity()));
                            		plPrint.setShelfCode(storageStock.getStorage().getShelf().getCode());
                            		plPrint.setStorageCode(storageStock.getStorage().getCode());
                        			plPrint.setQtyStorage(Integer.toString(storageStock.getQuantity()));
                            		plPrintList.add(plPrint);
                        			counter+=1;
                        		}else{
                        			System.out.println("add other stock");
                        			PickingListPrint plPrintTemp = new PickingListPrint();
                        			plPrintTemp.setSalesOrder(plPrint.getSalesOrder());
                        			plPrintTemp.setPickPackage(plPrint.getPickPackage());
                        			plPrintTemp.setWarehouseSkuId(plPrint.getWarehouseSkuId());
                        			plPrintTemp.setItemName(plPrint.getItemName());
                        			plPrintTemp.setShelfCode(storageStock.getStorage().getShelf().getCode());
                        			plPrintTemp.setStorageCode(storageStock.getStorage().getCode());
                        			plPrintTemp.setQtyStorage(Integer.toString(storageStock.getQuantity()));
                        			plPrintList.add(plPrintTemp);
                        		}
                        	}                    	
                        }                       	            		
	        		}
	        	}
			}				
		}	
		System.out.println("plPrintList size: "+plPrintList.size()+", start print report to excel");
		OutputStream out = null;

		try {
			String shortname="PickingListSO" + System.currentTimeMillis() + ".xls";								
						
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
				 
				int startRow = 2;
				int startCol=0;
				
				HSSFRow titleRow = sheet.createRow((short) 0);
				titleRow.createCell(startCol).setCellValue(new HSSFRichTextString("Picking List"));
				
				HSSFRow headerRow = sheet.createRow((short) startRow);
				headerRow.createCell(startCol).setCellValue(new HSSFRichTextString("Package ID"));
				headerRow.createCell(startCol+1).setCellValue(new HSSFRichTextString("Merchant Code"));
				headerRow.createCell(startCol+2).setCellValue(new HSSFRichTextString("Merchant Store"));
				headerRow.createCell(startCol+3).setCellValue(new HSSFRichTextString("Picker Name"));
				headerRow.createCell(startCol+4).setCellValue(new HSSFRichTextString("Keterangan"));
				headerRow.createCell(startCol+5).setCellValue(new HSSFRichTextString("Sales Order ID"));
				headerRow.createCell(startCol+6).setCellValue(new HSSFRichTextString("Warehouse SKU ID"));
				headerRow.createCell(startCol+7).setCellValue(new HSSFRichTextString("Item Name"));
				headerRow.createCell(startCol+8).setCellValue(new HSSFRichTextString("Qty"));
				headerRow.createCell(startCol+9).setCellValue(new HSSFRichTextString("Shelf Code"));
				headerRow.createCell(startCol+10).setCellValue(new HSSFRichTextString("Storage Code"));
				headerRow.createCell(startCol+11).setCellValue(new HSSFRichTextString("Qty Storage"));
				headerRow.createCell(startCol+12).setCellValue(new HSSFRichTextString("Qty Picked"));
									   
				//set style for header
				for(int i=startCol; i<=startCol+12; i++){
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
					
					System.out.println("processing row: "+i+" packageId: "+pl.getPickPackage().getCode());
					HSSFCell cell = detailRow.createCell(startCol);cell.setCellValue(new HSSFRichTextString(pl.getPickPackage().getCode()));
					cell = detailRow.createCell(startCol+1);cell.setCellValue(new HSSFRichTextString(pl.getSalesOrder().getSupplier().getCode()));
					cell = detailRow.createCell(startCol+2);cell.setCellValue(new HSSFRichTextString(pl.getSalesOrder().getSupplier().getName()));	
					cell = detailRow.createCell(startCol+3);cell.setCellValue(new HSSFRichTextString(pl.getPickPackage().getAssignedPicker().getName()));
					cell = detailRow.createCell(startCol+4);cell.setCellValue(new HSSFRichTextString(pl.getSalesOrder().getStockType().name()));
					cell = detailRow.createCell(startCol+5);cell.setCellValue(new HSSFRichTextString(pl.getSalesOrder().getSalesOrderNumber()));
					cell = detailRow.createCell(startCol+6);cell.setCellValue(new HSSFRichTextString(pl.getWarehouseSkuId()));
					cell = detailRow.createCell(startCol+7);cell.setCellValue(new HSSFRichTextString(pl.getItemName()));
					cell = detailRow.createCell(startCol+8);cell.setCellValue(new HSSFRichTextString(pl.getQty()));
					cell = detailRow.createCell(startCol+9);cell.setCellValue(new HSSFRichTextString(pl.getShelfCode()));
					cell = detailRow.createCell(startCol+10);cell.setCellValue(new HSSFRichTextString(pl.getStorageCode()));
					cell = detailRow.createCell(startCol+11);cell.setCellValue(new HSSFRichTextString(pl.getQtyStorage()));
					cell = detailRow.createCell(startCol+12);cell.setCellValue(new HSSFRichTextString(""));
					
					//set style for list
					for(int l=startCol; l<=startCol+12; l++){
						HSSFCell cell2 = detailRow.getCell(l);
						cell2.setCellStyle(detailCellstyle);
					}	
				}		
				
				//set style for list
				for(int l=startCol; l<=startCol+12; l++){
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