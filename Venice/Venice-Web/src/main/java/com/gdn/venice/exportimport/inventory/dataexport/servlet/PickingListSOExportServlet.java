package com.gdn.venice.exportimport.inventory.dataexport.servlet;

import java.io.IOException;
import java.io.OutputStream;
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
import com.gdn.inventory.exchange.entity.request.PickPackagePrintRequest;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;

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
		
		PickingListManagementService pickingListService = new PickingListManagementService();
		List<PickPackagePrintRequest> plPrintList = pickingListService.getPickPackageSOPrint(request.getParameter("pickerId")
				, request.getParameter("warehouseId"));
						
		System.out.println("plPrintList size: "+plPrintList.size()+", start print report to excel");
		OutputStream out = null;

		try {
			String shortname="PickingListSO" + System.currentTimeMillis() + ".xls";								
						
			if(plPrintList!=null && plPrintList.size()>0){
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
				headerRow.createCell(startCol+13).setCellValue(new HSSFRichTextString("Container ID"));
									   
				for(int i=startCol; i<=startCol+13; i++){
					HSSFCell cell = headerRow.getCell(i);
					cell.setCellStyle(headerCellstyle);
				}    
										
				HSSFRow detailRow = null;
				for (int i = 0; i < plPrintList.size(); i++) {
					System.out.println("looping data");					
					PickPackagePrintRequest pl = plPrintList.get(i);	
					
					startRow=startRow+1;
					detailRow = sheet.createRow(startRow);
					
					System.out.println("processing row: "+i+" packageId: "+pl.getPickPackage().getCode());
					HSSFCell cell = detailRow.createCell(startCol);cell.setCellValue(new HSSFRichTextString(pl.getPickPackage().getCode()));
					cell = detailRow.createCell(startCol+1);cell.setCellValue(new HSSFRichTextString(pl.getSalesOrder().getSupplier()!=null?pl.getSalesOrder().getSupplier().getCode():""));
					cell = detailRow.createCell(startCol+2);cell.setCellValue(new HSSFRichTextString(pl.getSalesOrder().getSupplier()!=null?pl.getSalesOrder().getSupplier().getName():""));	
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
					cell = detailRow.createCell(startCol+13);cell.setCellValue(new HSSFRichTextString(""));
					
					for(int l=startCol; l<=startCol+13; l++){
						HSSFCell cell2 = detailRow.getCell(l);
						cell2.setCellStyle(detailCellstyle);
					}	
				}		
				
				for(int l=startCol; l<=startCol+13; l++){
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
