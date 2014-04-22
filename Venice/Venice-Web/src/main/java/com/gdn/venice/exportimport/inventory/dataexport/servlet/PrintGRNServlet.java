/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.exportimport.inventory.dataexport.servlet;

import com.gdn.inventory.exchange.beans.GoodReceivedNote;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.GRNManagementService;
import com.gdn.venice.util.CommonUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author Maria Olivia
 */
public class PrintGRNServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String CLASS_NAME = PrintGRNServlet.class.getCanonicalName();
    GRNManagementService grnService;

    public PrintGRNServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        grnService = new GRNManagementService();
        ResultWrapper<com.gdn.inventory.exchange.beans.GoodReceivedNote> wrapper = grnService.getGrnForPrint(request.getParameter("grnId"));
        if (wrapper != null && wrapper.isSuccess() && wrapper.getContent() != null) {
            response.setContentType("application/vnd.ms-excel");
            GoodReceivedNote grn = wrapper.getContent();

            String filename = grn.getGrnNumber() + "_" + new SimpleDateFormat("yyyyMMdd").format(grn.getCreatedDate());
            response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xls");
            Map<String, Object> beans = new HashMap<String, Object>();
            beans.put("grn", grn);
            XLSTransformer transformer = new XLSTransformer();
            FileInputStream is = new FileInputStream("/home/software/venice_home/files/template/inventory/grn-template.xls");
            try {
                Workbook wb = transformer.transformXLS(is, beans);
                wb.write(response.getOutputStream());
            } catch (ParsePropertyException ex) {
                CommonUtil.logError(CLASS_NAME, ex.getMessage());
            } catch (InvalidFormatException ex) {
                CommonUtil.logError(CLASS_NAME, ex.getMessage());
            }
        }
    }
}
