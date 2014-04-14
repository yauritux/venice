package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.module.outbound.OpnameDetail;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.OpnameService;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.util.CommonUtil;
import com.google.gwt.user.client.ui.AttachDetachException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author Maria Olivia
 */
public class SaveOpnameListDataCommand {

    private final String CLASS_NAME = this.getClass().getCanonicalName();
    private String username, warehouseCode, data, stockType, supplierCode;
    private OpnameService opnameService;
    private CellStyle headerCellstyle, detailCellstyle;

    public SaveOpnameListDataCommand(String username, String data, String warehouseCode,
            String stockType, String supplierCode) {
        this.username = username;
        this.data = data;
        this.warehouseCode = warehouseCode;
        this.stockType = stockType;
        this.supplierCode = supplierCode;
    }

    public Workbook execute() {
        ResultWrapper<List<OpnameDetail>> wrapper;
        try {
            opnameService = new OpnameService();
            wrapper = opnameService.saveOpnameList(username, extractItemStorageId(), 
                    warehouseCode, stockType, supplierCode);
            if (wrapper == null || !wrapper.isSuccess() || wrapper.getContent().isEmpty()) {
                return new HSSFWorkbook();
            } else {
                return generateExcel(wrapper.getContent());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new HSSFWorkbook();
        }
    }

    private List<Long> extractItemStorageId() {
        List<Long> idList = new ArrayList<Long>();
        String[] ids = data.split(";");
        for (int i = 0; i < ids.length; i++) {
            idList.add(Long.parseLong(ids[i]));
        }
        return idList;
    }

    private Workbook generateExcel(List<OpnameDetail> opnameDetail) {
        CommonUtil.logDebug(CLASS_NAME, "generateExel, with number of records: " + opnameDetail.size());
        System.out.println("generateExel, with number of records: " + opnameDetail.size());
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Opname List");

        try {
            setStyle(wb);

            int startRow = 2;
            int startCol = 0;

            System.out.println("Set opname number");
            HSSFRow opnameRow = sheet.createRow(0);
            opnameRow.createCell(0).setCellValue(new HSSFRichTextString("Opname No. "));
            opnameRow.createCell(1).setCellValue(new HSSFRichTextString(opnameDetail.get(0).getOpname().getOpnameNo()));

            System.out.println("Create header");
            HSSFRow headerRow = sheet.createRow((short) startRow);
            headerRow.createCell(startCol).setCellValue(new HSSFRichTextString("No"));
            headerRow.createCell(startCol + 1).setCellValue(new HSSFRichTextString("Warehouse SKU ID"));
            headerRow.createCell(startCol + 2).setCellValue(new HSSFRichTextString("Item Name"));
            headerRow.createCell(startCol + 3).setCellValue(new HSSFRichTextString("Category"));
            headerRow.createCell(startCol + 4).setCellValue(new HSSFRichTextString("UoM"));
            headerRow.createCell(startCol + 5).setCellValue(new HSSFRichTextString("Storage Code"));
            headerRow.createCell(startCol + 6).setCellValue(new HSSFRichTextString("Shelf Code"));
            headerRow.createCell(startCol + 7).setCellValue(new HSSFRichTextString("Quantity"));

            //set style for header
            System.out.println("set header Style, number of cells: " + headerRow.getLastCellNum());
            for (int i = startCol; i < headerRow.getLastCellNum(); i++) {
                headerRow.getCell(i).setCellStyle(headerCellstyle);
            }

            //Looping for generating excel rows
            OpnameDetail opname;
            HSSFRow detailRow;
            System.out.println("about to process list");
            for (int i = 0; i < opnameDetail.size(); i++) {
                CommonUtil.logDebug(CLASS_NAME, "processing row: " + i);
                System.out.println("processing row: " + i);
                opname = opnameDetail.get(i);

                startRow = startRow + 1;
                detailRow = sheet.createRow(startRow);
                detailRow.createCell(startCol).setCellValue(new HSSFRichTextString(Integer.toString(i + 1)));
                detailRow.createCell(startCol + 1).setCellValue(new HSSFRichTextString(opname.getItemCode()));
                detailRow.createCell(startCol + 2).setCellValue(new HSSFRichTextString(opname.getItemName()));
                detailRow.createCell(startCol + 3).setCellValue(new HSSFRichTextString(opname.getCategory()));
                detailRow.createCell(startCol + 4).setCellValue(new HSSFRichTextString(opname.getUom()));
                detailRow.createCell(startCol + 5).setCellValue(new HSSFRichTextString(opname.getStorageCode()));
                detailRow.createCell(startCol + 6).setCellValue(new HSSFRichTextString(opname.getShelfCode()));
                detailRow.createCell(startCol + 7).setCellValue(new HSSFRichTextString(opname.getQuantity() + ""));

                //set style for list
                for (int l = startCol; l <= startCol + 5; l++) {
                    detailRow.getCell(l).setCellStyle(detailCellstyle);
                }
            }

            //set style for list
            System.out.println("set Style for list");
            for (int l = startCol; l <= startCol + 7; l++) {
                sheet.autoSizeColumn(l);
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommonUtil.logError(CLASS_NAME, e.getMessage());
        }
        return wb;
    }

    private void setStyle(HSSFWorkbook wb) {
        CommonUtil.logDebug(CLASS_NAME, "setStyle");
        System.out.println("setStyle");

        //style
        headerCellstyle = wb.createCellStyle();
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

        detailCellstyle = wb.createCellStyle();
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
    }
}
