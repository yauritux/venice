package com.gdn.venice.exportimport.finance.dataexport;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.facade.FinSalesRecordSessionEJBRemote;
import com.gdn.venice.facade.VenReturItemSessionEJBRemote;
import com.gdn.venice.persistence.FinSalesRecord;
import com.gdn.venice.persistence.VenReturItem;
import com.gdn.venice.util.VeniceConstants;

public class SalesSettlementRecordExport {

    protected static Logger _log = null;
    HSSFWorkbook wb = null;
    Locator<Object> locator;

    /**
     * Basic constructor
     *
     * @throws Exception
     */
    public SalesSettlementRecordExport(HSSFWorkbook wb) throws Exception {
        super();
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.finance.dataexport.SalesSettlementRecordExport");
        locator = new Locator<Object>();
        this.wb = wb;
    }      
    private List<FinSalesRecord> getSalesRecordList(List<String> salesRecordIdList) throws Exception {

        FinSalesRecordSessionEJBRemote salesRecordHome = (FinSalesRecordSessionEJBRemote) locator
                .lookup(FinSalesRecordSessionEJBRemote.class, "FinSalesRecordSessionEJBBean");

        StringBuilder commaDelimitedId = new StringBuilder();

        for (int i = 0; i < salesRecordIdList.size(); i++) {
            commaDelimitedId.append(salesRecordIdList.get(i));

            if (i != salesRecordIdList.size() - 1) {
                commaDelimitedId.append(",");
            }
        }

        String query = "select o from FinSalesRecord o where o.salesRecordId in ( " + commaDelimitedId.toString() + " )";
        List<FinSalesRecord> finSalesRecordList = salesRecordHome.queryByRange(query, 0, 0);

        return finSalesRecordList;
    }
    
      private int[] getFieldsWithNumberFormatIndex(String[] headerTitles, String[] fieldsWithNumberFormat) {
        int[] fieldsWithNumberFormatIndex = new int[fieldsWithNumberFormat.length];

        for (int i = 0; i < fieldsWithNumberFormat.length; i++) {

            for (int j = 0; j < headerTitles.length; j++) {
                if (headerTitles[j].equals(fieldsWithNumberFormat[i])) {
                    fieldsWithNumberFormatIndex[i] = j;
                    break;
                }
            }
        }

        return fieldsWithNumberFormatIndex;
    }

    public HSSFWorkbook exportExcel(List<String> params, HSSFSheet sheet)
            throws ServletException {

        int startRow = 0;
        int startCol = 0;

        CellStyle headerCellstyle = getHeaderStyle();
        CellStyle style = getStyle();
        CellStyle styleAlighRight = getStyleAlighRight();
        CellStyle style2 = getStyle2();
        CellStyle style2AlighRight = getStyle2AlighRight();


        String[] headerTitles = {
            "Trxn",
            "Order Id",
            "Order Item ID",
            "Keterangan",
            "Qty",
            "Harga Satuan",
            "Jumlah",
            "Komisi",
            "Biaya Transaksi",
            "PPH 23",
            "Total"
            
        };

        String[] headersWithNumberFormat = {
        		"Qty",
                "Harga Satuan",
                "Jumlah",
                "Komisi",
                "Biaya Transaksi",
                "PPH 23",
                "Total"
        };

        int[] fieldWithNumberFormat = getFieldsWithNumberFormatIndex(headerTitles, headersWithNumberFormat);
     
        try {

            List<FinSalesRecord> finSalesRecordList = getSalesRecordList(params);
        	VenReturItemSessionEJBRemote venReturItemSessionHome = (VenReturItemSessionEJBRemote) locator.lookup(VenReturItemSessionEJBRemote.class, "VenReturItemSessionEJBBean");
    		
            // Create the column headings
            HSSFRow headerRow = sheet.createRow((short) startRow);
            for (int i = 0; i < headerTitles.length; i++) {
                headerRow.createCell(startCol + i).setCellValue(new HSSFRichTextString(headerTitles[i]));
            }

            for (int i = startCol; i < headerTitles.length + startCol; i++) {
                HSSFCell cell = headerRow.getCell(i);
                cell.setCellStyle(headerCellstyle);
                // Autosize the columns while we are there
                sheet.autoSizeColumn(i);
            }

            if (!finSalesRecordList.isEmpty()) {

                _log.debug("Start WriteExcel ");
                String sqlQuery=null;
                String desc=null;
                for (int i = 0; i < finSalesRecordList.size(); i++) {

                    HSSFRow row = sheet.createRow(++startRow);

                    FinSalesRecord salesRecord = finSalesRecordList.get(i);
                    desc=null;
                    BigDecimal returItemNumber = new BigDecimal(1);
                    sqlQuery = "select o from VenReturItem o where o.wcsReturItemId ='"+salesRecord.getVenOrderItem().getWcsOrderItemId()+"' and o.venReturStatus.orderStatusId="+VeniceConstants.VEN_ORDER_STATUS_RF;
					List<VenReturItem> returItem = venReturItemSessionHome.queryByRange(sqlQuery, 0, 0);					
					if(!returItem.isEmpty() && returItem.size()>0 ){				
						desc=" ( Retur - Refund )";
						returItemNumber=new BigDecimal(-1);
					}
                    //ID
                    HSSFCell nameCell = row.createCell(startCol);
                    nameCell.setCellValue(new HSSFRichTextString((startRow+"")));
                    //Order ID
                    nameCell = row.createCell(startCol + 1);
                    nameCell.setCellValue(new HSSFRichTextString((salesRecord.getVenOrderItem() != null && salesRecord.getVenOrderItem().getVenOrder() != null) ? salesRecord.getVenOrderItem().getVenOrder().getWcsOrderId() : ""));
                     //Order Item ID
                    nameCell = row.createCell(startCol + 2);
                    nameCell.setCellValue(new HSSFRichTextString((salesRecord.getVenOrderItem() != null) ? salesRecord.getVenOrderItem().getWcsOrderItemId() : ""));
                    //Item Desc
                    nameCell = row.createCell(startCol + 3);
                    nameCell.setCellValue(new HSSFRichTextString((salesRecord.getVenOrderItem() != null && salesRecord.getVenOrderItem().getVenMerchantProduct() != null) ? salesRecord.getVenOrderItem().getVenMerchantProduct().getWcsProductName() +(desc!=null?desc:""): ""));
                    //Quantity
                    nameCell = row.createCell(startCol + 4);
                    nameCell.setCellValue(new HSSFRichTextString((salesRecord.getVenOrderItem() != null && salesRecord.getVenOrderItem().getQuantity() != null) ? (salesRecord.getVenOrderItem().getQuantity() * returItemNumber.doubleValue())+"" : ""));
                    //Price Per Unit
                    nameCell = row.createCell(startCol + 5);
                    nameCell.setCellValue(new HSSFRichTextString((salesRecord.getVenOrderItem() != null && salesRecord.getVenOrderItem().getPrice() != null) ? salesRecord.getVenOrderItem().getPrice().multiply(returItemNumber)+"" : ""));
        			 //Jumlah
                    nameCell = row.createCell(startCol + 6);
                    nameCell.setCellValue(new HSSFRichTextString((salesRecord.getVenOrderItem() != null && salesRecord.getVenOrderItem().getTotal() != null) ? salesRecord.getVenOrderItem().getTotal().multiply(returItemNumber)+"" : ""));
                    //Commission
                    nameCell = row.createCell(startCol + 7);
                    nameCell.setCellValue(new HSSFRichTextString((salesRecord.getGdnCommissionAmount() != null) ? salesRecord.getGdnCommissionAmount().multiply(returItemNumber)+"" : ""));
                    //Transaction Fee
                    nameCell = row.createCell(startCol + 8);
                    nameCell.setCellValue(new HSSFRichTextString((salesRecord.getGdnTransactionFeeAmount() != null) ? salesRecord.getGdnTransactionFeeAmount().multiply(returItemNumber)+"" : ""));
                    //PPH23
                    nameCell = row.createCell(startCol + 9);
                    nameCell.setCellValue(new HSSFRichTextString((salesRecord.getPph23Amount() != null ) ? salesRecord.getPph23Amount().multiply(returItemNumber)+"" : ""));
                   
            		BigDecimal sum = salesRecord.getVenOrderItem().getTotal().subtract(salesRecord.getGdnCommissionAmount()!=null?salesRecord.getGdnCommissionAmount():new BigDecimal(0)).subtract(salesRecord.getGdnTransactionFeeAmount()!=null?salesRecord.getGdnTransactionFeeAmount():new BigDecimal(0)) ;	
                    //Total
                    nameCell = row.createCell(startCol + 10);
                    nameCell.setCellValue(new HSSFRichTextString((sum.multiply(returItemNumber)+"")));
                               
                      for (int j = startCol; j < 11 + startCol; j++) {
                        HSSFCell cells = row.getCell(j);
                        if (startRow % 2 == 0) {
                            cells.setCellStyle(style);

                            if (ArrayUtils.contains(fieldWithNumberFormat, j)) {
                                cells.setCellStyle(styleAlighRight);
                            }

                        } else {
                            cells.setCellStyle(style2);

                            if (ArrayUtils.contains(fieldWithNumberFormat, j)) {
                                cells.setCellStyle(style2AlighRight);
                            }
                        }
                        sheet.autoSizeColumn(i);
                    }
                }                
            }
        } catch (Exception e) {
            throw new ServletException("Exception in Excel Sample Servlet", e);
        }

        return wb;
    }

    private CellStyle getHeaderStyle() {
        CellStyle headerCellstyle = wb.createCellStyle();
        headerCellstyle.setBorderBottom(CellStyle.BORDER_THIN);
        headerCellstyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        headerCellstyle.setBorderLeft(CellStyle.BORDER_THIN);
        headerCellstyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        headerCellstyle.setBorderRight(CellStyle.BORDER_THIN);
        headerCellstyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        headerCellstyle.setBorderTop(CellStyle.BORDER_THIN);
        headerCellstyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headerCellstyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        headerCellstyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerCellstyle.setAlignment(CellStyle.ALIGN_CENTER);

        return headerCellstyle;
    }

    private CellStyle getStyle() {
        CellStyle style = wb.createCellStyle();
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);

        return style;
    }

    private CellStyle getStyleAlighRight() {
        CellStyle styleAlighRight = wb.createCellStyle();
        styleAlighRight.setBorderBottom(CellStyle.BORDER_THIN);
        styleAlighRight.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        styleAlighRight.setBorderLeft(CellStyle.BORDER_THIN);
        styleAlighRight.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        styleAlighRight.setBorderRight(CellStyle.BORDER_THIN);
        styleAlighRight.setRightBorderColor(IndexedColors.BLACK.getIndex());
        styleAlighRight.setBorderTop(CellStyle.BORDER_THIN);
        styleAlighRight.setTopBorderColor(IndexedColors.BLACK.getIndex());
        styleAlighRight.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        styleAlighRight.setFillPattern(CellStyle.SOLID_FOREGROUND);
        styleAlighRight.setAlignment(CellStyle.ALIGN_RIGHT);

        return styleAlighRight;
    }

    private CellStyle getStyle2() {
        CellStyle style2 = wb.createCellStyle();
        style2.setBorderBottom(CellStyle.BORDER_THIN);
        style2.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style2.setBorderLeft(CellStyle.BORDER_THIN);
        style2.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style2.setBorderRight(CellStyle.BORDER_THIN);
        style2.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style2.setBorderTop(CellStyle.BORDER_THIN);
        style2.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style2.setFillForegroundColor(IndexedColors.GREY_40_PERCENT
                .getIndex());

        return style2;
    }

    private CellStyle getStyle2AlighRight() {
        CellStyle style2AlighRight = wb.createCellStyle();
        style2AlighRight.setBorderBottom(CellStyle.BORDER_THIN);
        style2AlighRight.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style2AlighRight.setBorderLeft(CellStyle.BORDER_THIN);
        style2AlighRight.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style2AlighRight.setBorderRight(CellStyle.BORDER_THIN);
        style2AlighRight.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style2AlighRight.setBorderTop(CellStyle.BORDER_THIN);
        style2AlighRight.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style2AlighRight.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style2AlighRight.setAlignment(CellStyle.ALIGN_RIGHT);

        return style2AlighRight;
    }
}
