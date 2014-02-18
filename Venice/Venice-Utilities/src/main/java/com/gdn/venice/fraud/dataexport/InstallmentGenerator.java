package com.gdn.venice.fraud.dataexport;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.gdn.venice.fraud.dataexport.bean.Installment;
import com.gdn.venice.hssf.DataArgs;
import com.gdn.venice.hssf.DataColumn;
import com.gdn.venice.hssf.DataRow;
import com.gdn.venice.hssf.Export;
import com.gdn.venice.hssf.GCell;
import com.gdn.venice.hssf.GCellStyle;
import com.gdn.venice.hssf.RGBColor;

/**
 * Excel generator for installment report 
 * 
 * @author Roland
 */
public class InstallmentGenerator {
	public void exportInstallmentData(String fileName, ArrayList<Installment> installmentList) {
		
		//Define required variable
		Export e = new Export(false);
		DataArgs data = new DataArgs();
		data.setFileName(fileName);
			
		//Define columns
		data.addColumn(new DataColumn("NO"));
		data.addColumn(new DataColumn("PAYMENT ID"));
		data.addColumn(new DataColumn("ORDER ID"));
		data.addColumn(new DataColumn("ORDER DATE"));
		data.addColumn(new DataColumn("AUTHORIZATION CODE"));
		data.addColumn(new DataColumn("AMOUNT"));
		data.addColumn(new DataColumn("TENOR"));
		data.addColumn(new DataColumn("INSTALLMENT"));
		data.addColumn(new DataColumn("INTEREST"));
		data.addColumn(new DataColumn("TOTAL INSTALLMENT+INTEREST"));
		data.addColumn(new DataColumn("EMAIL CONVERT SENT TO BCA"));
		data.addColumn(new DataColumn("CUSTOMER EMAIL"));
		data.addColumn(new DataColumn("CUSTOMER NAME"));		
				
		//Create cell style
		GCellStyle style = new GCellStyle();		
		style.setBackgroundColor(new RGBColor((byte)255, (byte)255, (byte)255));
		
		//Looping for generating excel rows
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		
		for (int i = 0; i < installmentList.size(); i++) {
			//Get row
			Installment inst = (Installment) installmentList.get(i);
								
			//Create row
			DataRow dr = new DataRow();
			
			Integer row=i+1;
			dr.addCells(new GCell[] {
					new GCell (row.toString(), style),
					new GCell (inst.getWcsPaymentId().toString(), style),
					new GCell (inst.getWcsOrderId().toString(), style),
					new GCell (sdf.format(inst.getOrderDate()), style),
					new GCell (inst.getReferenceId(), style),
					new GCell (inst.getAmount()!=null?formatDouble(inst.getAmount()).toString():"", style),					
					new GCell (inst.getTenor()!=null?inst.getTenor().toString():"", style),
					new GCell (inst.getInstallment()!=null?formatDouble(inst.getInstallment()).toString():"", style),
					new GCell (inst.getInterest()!=null?formatDouble(inst.getInterest()).toString():"", style),
					new GCell (inst.getInteresInstallment()!=null?formatDouble(inst.getInteresInstallment()).toString():"", style),
					new GCell (sdf.format(inst.getInstallmentSentDate()), style),
					new GCell (inst.getCustomerUserName(), style),
					new GCell (inst.getCustomerName(), style)
					});
			data.addRow(dr);
		}
		
		//Write the cells to the sheet first so we can manipulate the rows
		e.writeCellsToSheet(data);

		/*
		 * Create a style that is suitable for the output of the report
		 */
	    CellStyle headerCellstyle = e.getWb().createCellStyle();
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
		
	    //Create the style in the first row
		for(int i = 0; i <= 12; i++){
			HSSFCell cell = e.getSheet().getRow(0).getCell(i);
			cell.setCellStyle(headerCellstyle);
			//Autosize the columns while we are there
			e.getSheet().autoSizeColumn(i);
		}		
		e.writeWorkBookToFile(data);
	}
	
	public void exportCancelInstallmentData(String fileName, ArrayList<Installment> installmentList) {
		
		//Define required variable
		Export e = new Export(false);
		DataArgs data = new DataArgs();
		data.setFileName(fileName);
			
		//Define columns
		data.addColumn(new DataColumn("NO"));
		data.addColumn(new DataColumn("PAYMENT ID"));
		data.addColumn(new DataColumn("ORDER ID"));
		data.addColumn(new DataColumn("ORDER DATE"));
		data.addColumn(new DataColumn("AUTHORIZATION CODE"));
		data.addColumn(new DataColumn("AMOUNT"));
		data.addColumn(new DataColumn("TENOR"));
		data.addColumn(new DataColumn("INSTALLMENT"));
		data.addColumn(new DataColumn("INTEREST"));
		data.addColumn(new DataColumn("TOTAL INSTALLMENT+INTEREST"));
		data.addColumn(new DataColumn("EMAIL CANCEL SENT TO BCA"));
		data.addColumn(new DataColumn("CUSTOMER EMAIL"));
		data.addColumn(new DataColumn("CUSTOMER NAME"));		
				
		//Create cell style
		GCellStyle style = new GCellStyle();		
		style.setBackgroundColor(new RGBColor((byte)255, (byte)255, (byte)255));
		
		//Looping for generating excel rows
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		
		for (int i = 0; i < installmentList.size(); i++) {
			//Get row
			Installment inst = (Installment) installmentList.get(i);
								
			//Create row
			DataRow dr = new DataRow();
			
			Integer row=i+1;
			dr.addCells(new GCell[] {
					new GCell (row.toString(), style),
					new GCell (inst.getWcsPaymentId().toString(), style),
					new GCell (inst.getWcsOrderId().toString(), style),
					new GCell (sdf.format(inst.getOrderDate()), style),
					new GCell (inst.getReferenceId(), style),
					new GCell (inst.getAmount()!=null?formatDouble(inst.getAmount()).toString():"", style),					
					new GCell (inst.getTenor()!=null?inst.getTenor().toString():"", style),
					new GCell (inst.getInstallment()!=null?formatDouble(inst.getInstallment()).toString():"", style),
					new GCell (inst.getInterest()!=null?formatDouble(inst.getInterest()).toString():"", style),
					new GCell (inst.getInteresInstallment()!=null?formatDouble(inst.getInteresInstallment()).toString():"", style),
					new GCell (sdf.format(inst.getInstallmentCancelDate()), style),
					new GCell (inst.getCustomerUserName(), style),
					new GCell (inst.getCustomerName(), style)
					});
			data.addRow(dr);
		}
		
		//Write the cells to the sheet first so we can manipulate the rows
		e.writeCellsToSheet(data);

		/*
		 * Create a style that is suitable for the output of the report
		 */
	    CellStyle headerCellstyle = e.getWb().createCellStyle();
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
		
	    //Create the style in the first row
		for(int i = 0; i <= 12; i++){
			HSSFCell cell = e.getSheet().getRow(0).getCell(i);
			cell.setCellStyle(headerCellstyle);
			//Autosize the columns while we are there
			e.getSheet().autoSizeColumn(i);
		}		
		e.writeWorkBookToFile(data);
	}
	
	private String formatDouble(BigDecimal value){
		NumberFormat nf = new DecimalFormat("#,###,###,###,###");
		return "Rp " + nf.format(value.doubleValue()).replace(',', '.');
	}
}
