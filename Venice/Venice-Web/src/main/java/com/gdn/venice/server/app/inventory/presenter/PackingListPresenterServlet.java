/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.presenter;

import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.command.FetchWarehouseDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchWarehouseInProcessDataCommand;
import com.gdn.venice.server.app.inventory.command.SaveOrUpdateWarehouseWIPDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;
import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Maria Olivia
 */
public class PackingListPresenterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public PackingListPresenterServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type") == null ? "" : request.getParameter("type");
        String username = request.getParameter("username");
        if (username == null || username.trim().equals("")) {
            username = "olive";
        }
        String retVal = "";

        String method = request.getParameter("method");

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username.trim());

        if (type.equals(RafDsCommand.DataSource)) {
            String requestBody = Util.extractRequestBody(request);
            params.put("limit", request.getParameter("limit"));
            params.put("page", request.getParameter("page"));

            RafDsRequest rafDsRequest = null;
            try {
                rafDsRequest = RafDsRequest.convertXmltoRafDsRequest(requestBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (method.equals("fetchPackingData")) {
                System.out.println("fetchPackingData");
                rafDsRequest.setParams(params);
                RafDsCommand rafDsCommand = new FetchWarehouseDataCommand(rafDsRequest);
                RafDsResponse rafDsResponse = rafDsCommand.execute();
                try {
                    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
                    System.out.println(retVal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (method.equals("fetchSalesData")) {
                System.out.println("fetchSalesData");
                params.put(DataNameTokens.INV_WAREHOUSE_APPROVALTYPE, ApprovalStatus.APPROVAL_CREATE.toString());
                rafDsRequest.setParams(params);
                RafDsCommand rafDsCommand = new FetchWarehouseInProcessDataCommand(rafDsRequest);
                RafDsResponse rafDsResponse = rafDsCommand.execute();
                try {
                    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (method.equals("fetchWarehouseComboBoxData")) {
                String requestBody = Util.extractRequestBody(request);
                System.out.println("Masuk RPC");
                System.out.println(requestBody);
                RafRpcCommand saveWarehouseCommand = new SaveOrUpdateWarehouseWIPDataCommand(username, requestBody);
                retVal = saveWarehouseCommand.execute();
            }
        }

        response.getOutputStream().println(retVal);
    }
}
