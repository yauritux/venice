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
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentApprovalItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentFinalItem;
import com.gdn.inventory.exchange.entity.module.inbound.GoodReceivedNoteItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrderItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseRequisitionItem;
import com.gdn.inventory.exchange.entity.module.inbound.Putaway;
import com.gdn.inventory.exchange.type.ASNReferenceType;
import com.gdn.inventory.exchange.type.ConsignmentType;
import com.gdn.inventory.exchange.type.StockType;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.exportimport.inventory.dataexport.PutawayPrint;
import com.gdn.venice.server.app.inventory.service.ASNManagementService;
import com.gdn.venice.server.app.inventory.service.GRNManagementService;
import com.gdn.venice.server.app.inventory.service.PutawayManagementService;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.util.Util;

/**
 * Servlet implementation class PutawayExportServlet.
 * 
 * @author Roland
 * 
 */
public class PutawayExportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected static Logger _log = null;
	
    GRNManagementService grnService;
    ASNManagementService asnService;
    PutawayManagementService putawayService;

	public PutawayExportServlet() {
		super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory.getLog4JLogger("com.gdn.venice.exportimport.inventory.dataexport.servlet.PutawayExportServlet");
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		service(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		service(request, response);
	}
	
	protected void service(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
		System.out.println("PutawayExportServlet");
		
		String username = Util.getUserName(request);
		
		String putawayType = request.getParameter("putawayType");
		String grnItemIds = request.getParameter("grnItemIds");	
		String putawayDate = "";
		
		String[] split = grnItemIds.split(";");
		List<String> grnItemIdList = new ArrayList<String>(); 
		
		for(String value:split){
			grnItemIdList.add(value);
		}
		
		System.out.println("grnItemIdList size: "+grnItemIdList.size());
			
		PutawayPrint putawayPrint = new PutawayPrint();
		List<PutawayPrint> putawayPrintList = new ArrayList<PutawayPrint>();
		
		putawayService = new PutawayManagementService();
		for(String grnItemId : grnItemIdList){
			ResultWrapper<GoodReceivedNoteItem> grnItemWrapper = putawayService.getGRNItemDataListById(grnItemId);	
			if(grnItemWrapper.isSuccess()){     	        	
	    		asnService = new ASNManagementService(); 
	        	GoodReceivedNoteItem grnItem = grnItemWrapper.getContent();
	        	
	        	List<Putaway> putawayList = putawayService.getPutawayByGrnId(Long.toString(grnItem.getGoodReceivedNote().getId()));
	        	putawayDate = putawayList.get(0).getCreatedDate().toString();	        	
	        	
	        	RafDsRequest rafDsRequest = new RafDsRequest();
	            HashMap<String, String> params = new HashMap<String, String>();
	            params.put("username", username); 
				
            	AdvanceShipNoticeItem asnItem = grnItem.getAdvanceShipNoticeItem();
            	if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.PURCHASE_ORDER.name())){
            		System.out.println("reff type: purchase order");                		                		
            		ResultWrapper<PurchaseOrderItem> poItemWrapper = asnService.getPOItemData(rafDsRequest, asnItem.getReferenceNumber().toString());
                	if(poItemWrapper!=null && poItemWrapper.isSuccess()){
                		PurchaseRequisitionItem prItem = poItemWrapper.getContent().getPurchaseRequisitionItem();
                		System.out.println("PO item found, code:"+prItem.getItem().getCode());                    		                   			 
	                    
	                    System.out.println("item id "+prItem.getItem().getId());
	                    System.out.println("destination: "+asnItem.getAdvanceShipNotice().getDestinationWarehouse().getId());
	                    System.out.println("supplier: "+poItemWrapper.getContent().getPurchaseOrder().getSupplier()!=null?poItemWrapper.getContent().getPurchaseOrder().getSupplier().getId():"");
	                    WarehouseItem whItem = putawayService.getWarehouseItemData(prItem.getItem().getId(), 
	                    		asnItem.getAdvanceShipNotice().getDestinationWarehouse().getId(), 
	                    		poItemWrapper.getContent().getPurchaseOrder().getSupplier()!=null?poItemWrapper.getContent().getPurchaseOrder().getSupplier().getId():new Long(0), StockType.TRADING);
	                    
	                    if(whItem!=null){
	                    	List<WarehouseItemStorageStock> storageStockList = putawayService.getWarehouseItemStorageList(whItem.getId());
	                    	if(storageStockList.size()>0){
		                    	for(WarehouseItemStorageStock storageStock : storageStockList){
		                    		putawayPrint.setReffNo(String.valueOf(grnItem.getGoodReceivedNote().getGrnNumber()));
		            	        	putawayPrint.setWarehouseCode(grnItem.getGoodReceivedNote().getReceivedWarehouse().getCode());
		                    		putawayPrint.setWarehouseSkuId(prItem.getItem().getCode());
		                    		putawayPrint.setItemName(prItem.getItem().getDescription()); 
		    	                    putawayPrint.setStorageCode(storageStock.getStorage().getCode()!=null?storageStock.getStorage().getCode():"-");
		    	                    putawayPrint.setQty(String.valueOf(storageStock.getQuantity())!=null?String.valueOf(storageStock.getQuantity()):"0");
		    	                    putawayPrintList.add(putawayPrint);
		                    	}
	                    	}else{
	                    		putawayPrint.setReffNo(String.valueOf(grnItem.getGoodReceivedNote().getGrnNumber()));
	            	        	putawayPrint.setWarehouseCode(grnItem.getGoodReceivedNote().getReceivedWarehouse().getCode());
	                    		putawayPrint.setWarehouseSkuId(prItem.getItem().getCode());
	                    		putawayPrint.setItemName(prItem.getItem().getDescription()); 
	    	                    putawayPrint.setStorageCode("-");
	    	                    putawayPrint.setQty(String.valueOf("0"));
	    	                    putawayPrintList.add(putawayPrint);
	                    	}
	                    }else{
	                		System.out.println("Warehouse item not found");
	                	}
	                    	                    
                	}else{
                		System.out.println("PO item not found");
                	}   
            	}else if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.CONSIGNMENT_FINAL.name())){
            		System.out.println("reff type: consignment");
            		ResultWrapper<ConsignmentFinalItem> cffItemWrapper = asnService.getCFFItemData(rafDsRequest, asnItem.getReferenceNumber());
                	if(cffItemWrapper!=null && cffItemWrapper.isSuccess()){
                		ConsignmentApprovalItem cafItem = cffItemWrapper.getContent().getConsignmentApprovalItem();
                		System.out.println("CFF item found, code:"+cafItem.getItem().getCode());    
	                        
	                    StockType st = null;
	                    if(cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getConsignmentType().name().equals(ConsignmentType.COMMISSION.name())){
	                    	st = StockType.CONSIGNMENT_COMMISION;
	                    }else if(cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getConsignmentType().name().equals(ConsignmentType.TRADING.name())){
	                    	st = StockType.CONSIGNMENT_TRADING;
	                    }
	                    
	                    System.out.println("item id "+cafItem.getItem().getId());
	                    System.out.println("destination: "+asnItem.getAdvanceShipNotice().getDestinationWarehouse().getId());
	                    System.out.println("supplier: "+cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getSupplier()!=null?cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getSupplier().getId():"");
	                    WarehouseItem whItem = putawayService.getWarehouseItemData(cafItem.getItem().getId(), 
	                    		asnItem.getAdvanceShipNotice().getDestinationWarehouse().getId(), 
	                    		cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getSupplier()!=null?cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getSupplier().getId():new Long(0), st);
	                    
	                    if(whItem!=null){
	                    	List<WarehouseItemStorageStock> storageStockList = putawayService.getWarehouseItemStorageList(whItem.getId());   
	                    	if(storageStockList.size()>0){
		                    	for(WarehouseItemStorageStock storageStock : storageStockList){	   	                    		
		                    		putawayPrint.setReffNo(String.valueOf(grnItem.getGoodReceivedNote().getGrnNumber()));
		            	        	putawayPrint.setWarehouseCode(grnItem.getGoodReceivedNote().getReceivedWarehouse().getCode());
		                    		putawayPrint.setWarehouseSkuId(cafItem.getItem().getCode());
		                    		putawayPrint.setItemName(cafItem.getItem().getDescription());	                    		
		    	                    putawayPrint.setStorageCode(storageStock.getStorage().getCode()!=null?storageStock.getStorage().getCode():"-");
		    	                    putawayPrint.setQty(String.valueOf(storageStock.getQuantity())!=null?String.valueOf(storageStock.getQuantity()):"0");	    	                        	                   
		    	                    putawayPrintList.add(putawayPrint);
		                    	}
	                    	}else{
	                    		putawayPrint.setReffNo(String.valueOf(grnItem.getGoodReceivedNote().getGrnNumber()));
	            	        	putawayPrint.setWarehouseCode(grnItem.getGoodReceivedNote().getReceivedWarehouse().getCode());
	                    		putawayPrint.setWarehouseSkuId(cafItem.getItem().getCode());
	                    		putawayPrint.setItemName(cafItem.getItem().getDescription());	                    		
	    	                    putawayPrint.setStorageCode("-");
	    	                    putawayPrint.setQty("0");	    	                        	                   
	    	                    putawayPrintList.add(putawayPrint);
	                    	}
	                    }else{
	                		System.out.println("Warehouse item not found");
	                	}
                	}else{
                		System.out.println("CFF item not found");
                	} 
            	}	   
	    	}
		}			
			
		System.out.println("plPrintList size: "+putawayPrintList.size()+", start print report to excel");
		OutputStream out = null;

		try {
			String shortname="Putaway" + System.currentTimeMillis() + ".xls";								
						
			if(putawayPrintList!=null && putawayPrintList.size()>0){
				response.setContentType("application/vnd.ms-excel");
				response.setHeader("Content-Disposition",  "attachment; filename="+shortname);

				HSSFWorkbook wb = new HSSFWorkbook(); 
				HSSFSheet sheet = wb.createSheet("Picking List");				
				 
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
				
				HSSFRow titleRow = sheet.createRow((short) startRow);
				titleRow.createCell(startCol).setCellValue(new HSSFRichTextString("PT Global Digital Niaga"));				
				titleRow.createCell(startCol+5).setCellValue(new HSSFRichTextString("warehouse Code"));
				
				titleRow = sheet.createRow(startRow+1);
				titleRow.createCell(startCol).setCellValue(new HSSFRichTextString("PUTAWAY"));
				titleRow.createCell(startCol+5).setCellValue(new HSSFRichTextString(putawayPrintList.get(0).getWarehouseCode()));	
				
				titleRow = sheet.createRow(startRow+2);
				titleRow.createCell(startCol).setCellValue(new HSSFRichTextString("Putaway Type"));
				titleRow.createCell(startCol+1).setCellValue(new HSSFRichTextString(putawayType));				
				
				startRow = 5;
				HSSFRow headerRow = sheet.createRow((short) startRow);
				headerRow.createCell(startCol).setCellValue(new HSSFRichTextString("No"));
				headerRow.createCell(startCol+1).setCellValue(new HSSFRichTextString("Putaway No"));
				headerRow.createCell(startCol+2).setCellValue(new HSSFRichTextString("reff No"));
				headerRow.createCell(startCol+3).setCellValue(new HSSFRichTextString("Warehouse SKU ID"));
				headerRow.createCell(startCol+4).setCellValue(new HSSFRichTextString("Item SKU Name"));
				headerRow.createCell(startCol+5).setCellValue(new HSSFRichTextString("Storage/Shelf Code"));
				headerRow.createCell(startCol+6).setCellValue(new HSSFRichTextString("Qty"));
					   
				for(int i=startCol; i<=startCol+6; i++){
					HSSFCell cell = headerRow.getCell(i);
					cell.setCellStyle(headerCellstyle);
				}    
										
				HSSFRow detailRow = null;
				for (int i = 0; i < putawayPrintList.size(); i++) {
					System.out.println("looping data");					
					PutawayPrint pl = putawayPrintList.get(i);	
					
					startRow=startRow+1;
					detailRow = sheet.createRow(startRow);
					
					_log.debug("processing row: "+i+" warehouseItemId: "+pl.getPutawayNo() +", sku id: "+pl.getWarehouseSkuId());
					HSSFCell cell = detailRow.createCell(startCol);cell.setCellValue(new HSSFRichTextString(Integer.toString(i+1)));
					cell = detailRow.createCell(startCol+1);cell.setCellValue(new HSSFRichTextString(pl.getPutawayNo()));
					cell = detailRow.createCell(startCol+2);cell.setCellValue(new HSSFRichTextString(pl.getReffNo()));
					cell = detailRow.createCell(startCol+3);cell.setCellValue(new HSSFRichTextString(pl.getWarehouseSkuId()));	
					cell = detailRow.createCell(startCol+4);cell.setCellValue(new HSSFRichTextString(pl.getItemName()));
					cell = detailRow.createCell(startCol+5);cell.setCellValue(new HSSFRichTextString(pl.getStorageCode()));
					cell = detailRow.createCell(startCol+6);cell.setCellValue(new HSSFRichTextString(pl.getQty()));
					
					for(int l=startCol; l<=startCol+6; l++){
						HSSFCell cell2 = detailRow.getCell(l);
						cell2.setCellStyle(detailCellstyle);
					}	
				}	
				
				HSSFRow footerRow = sheet.createRow((short) startRow);
				footerRow.createCell(startCol+1).setCellValue(new HSSFRichTextString("Date"));
				footerRow.createCell(startCol+2).setCellValue(new HSSFRichTextString("Name"));
				footerRow.createCell(startCol+3).setCellValue(new HSSFRichTextString("Signature"));
				
				for(int l=startCol+1; l<=startCol+3; l++){
					HSSFCell cell2 = footerRow.getCell(l);
					cell2.setCellStyle(headerCellstyle);
				}
				
				footerRow = sheet.createRow((short) startRow+1);
				footerRow.createCell(startCol).setCellValue(new HSSFRichTextString("Prepared By"));
				footerRow.createCell(startCol+1).setCellValue(new HSSFRichTextString(putawayDate));
				footerRow.createCell(startCol+2).setCellValue(new HSSFRichTextString(username));
				
				for(int l=startCol; l<=startCol+2; l++){
					HSSFCell cell2 = footerRow.getCell(l);
					cell2.setCellStyle(detailCellstyle);
				}
				
				for(int l=startCol; l<=startCol+6; l++){
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
