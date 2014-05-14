/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.presenter;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.command.AddOpnameDetailDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchItemCategoryComboBoxDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchItemStorageDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchItemUomComboBoxDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchOpnameDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchOpnameDetailDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchStorageByItemDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchSupplierDataCommand;
import com.gdn.venice.server.app.inventory.command.SaveOpnameAdjustmentDataCommand;
import com.gdn.venice.server.app.inventory.command.SaveOpnameListDataCommand;
import com.gdn.venice.server.app.inventory.command.UpdateOpnameDetailDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Maria Olivia
 */
public class OpnamePresenterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public OpnamePresenterServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type") == null ? "" : request.getParameter("type");
        String username = request.getParameter("username");
        if (username == null || username.trim().equals("")) {
            username = "olive";
        }
        String retVal = "";

        String method = request.getParameter("method");

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username.trim());
        RafDsCommand rafDsCommand = null;

        if (type.equals(RafDsCommand.DataSource)) {
            System.out.println("DataSource");
            String requestBody = Util.extractRequestBody(request);
            params.put("limit", request.getParameter("limit"));
            params.put("page", request.getParameter("page"));

            RafDsRequest rafDsRequest = null;
            try {
                rafDsRequest = RafDsRequest.convertXmltoRafDsRequest(requestBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (method.equals("fetchItemStorageData")) {
                params.put("warehouseCode", request.getParameter("warehouseCode"));
                params.put("stockType", request.getParameter("stockType"));
                params.put("supplierCode", request.getParameter("supplierCode"));
                rafDsRequest.setParams(params);
                rafDsCommand = new FetchItemStorageDataCommand(rafDsRequest);
            } else if (method.equals("fetchSupplierData")) {
                rafDsCommand = new FetchSupplierDataCommand(request.getParameter("warehouseCode"),
                        request.getParameter("stockType"));
            } else if (method.equals("fetchItemStorageDataById")) {
                rafDsCommand = new FetchItemStorageDataCommand(request.getParameter(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID));
            } else if (method.equals("fetchAllOpnameData")) {
                rafDsRequest.setParams(params);
                rafDsCommand = new FetchOpnameDataCommand(rafDsRequest);
            } else if (method.equals("fetchOpnameDetailByOpnameId")) {
                rafDsCommand = new FetchOpnameDetailDataCommand(request.getParameter("opnameId"));
            } else if (method.equals("updateOpnameDetail")) {
                try {
                    rafDsRequest.setData(RafDsRequest.getModifiedValuesData(requestBody));
                } catch (Exception ex) {
                    Logger.getLogger(OpnamePresenterServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
                rafDsCommand = new UpdateOpnameDetailDataCommand(username, rafDsRequest);
            } else if (method.equals("addOpnameDetail")) {
                try {
                    rafDsRequest.setData(RafDsRequest.getModifiedValuesData(requestBody));
                } catch (Exception ex) {
                    Logger.getLogger(OpnamePresenterServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
                rafDsCommand = new AddOpnameDetailDataCommand(username, rafDsRequest);
            } else if (method.equals("getStorageByItem")) {
                String itemCode = rafDsRequest.getCriteria().getSimpleCriteria().get(0).getValue(),
                        stockType = request.getParameter("stockType"),
                        supplierCode = request.getParameter("supplierCode"),
                        warehouseCode = request.getParameter("warehouseCode");
                rafDsCommand = new FetchStorageByItemDataCommand(warehouseCode, stockType, supplierCode, itemCode);
            }

            if (rafDsCommand != null) {
                RafDsResponse rafDsResponse = rafDsCommand.execute();
                try {
                    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
                    System.out.println("retval: " + retVal);
                    response.getOutputStream().println(retVal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (method.equals("saveOpnameList")) {
                String data = request.getParameter("data"),
                        stockType = request.getParameter("stockType"),
                        supplierCode = request.getParameter("supplierCode"),
                        warehouseCode = request.getParameter("warehouseCode");

                SaveOpnameListDataCommand saveOpnameCommand = new SaveOpnameListDataCommand(username,
                        data, warehouseCode, stockType, supplierCode);

                response.setContentType("application/vnd.ms-excel");
                String filename = "StockOpname/" + request.getParameter("warehouseCode")
                        + "_" + new SimpleDateFormat("yyyyMMdd").format(new Date());
                response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".xls");
                saveOpnameCommand.execute().write(response.getOutputStream());
            } else if (method.equals("saveOpnameAdjustment")) {
                RafRpcCommand getStorageByItemCommand = new SaveOpnameAdjustmentDataCommand(request.getParameter("opnameId"), username);
                response.getOutputStream().println(getStorageByItemCommand.execute());
            } else if (method.equals("fetchCategoryComboBoxData")) {
                RafRpcCommand fetchCategoryComboBoxDataCommand = new FetchItemCategoryComboBoxDataCommand();
                String val = fetchCategoryComboBoxDataCommand.execute();
                System.out.println(val);
                response.getOutputStream().println(val);
            } else if (method.equals("fetchUomComboBoxData")) {
                RafRpcCommand fetchUomComboBoxDataCommand = new FetchItemUomComboBoxDataCommand();
                response.getOutputStream().println(fetchUomComboBoxDataCommand.execute());
            }
        }
    }
}
