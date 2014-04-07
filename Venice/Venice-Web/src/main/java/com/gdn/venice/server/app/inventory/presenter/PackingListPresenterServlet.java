/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.presenter;

import com.gdn.venice.server.app.inventory.command.FetchAttributeNameDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchReadyPackingDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchSalesOrderAWBInfoDetailDataCommand;
import com.gdn.venice.server.app.inventory.command.RejectPackingDataCommand;
import com.gdn.venice.server.app.inventory.command.SaveAttributeDataCommand;
import com.gdn.venice.server.app.inventory.command.SavePackingDataCommand;
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
        String username = Util.getUserName(request);;
        if (username == null || username.trim().equals("")) {
            username = "olive";
        }
        String retVal = "";

        String method = request.getParameter("method");

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username.trim());
        RafDsCommand rafDsCommand = null;

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
                params.put("warehouseId", request.getParameter("warehouseId"));
                rafDsRequest.setParams(params);
                rafDsCommand = new FetchReadyPackingDataCommand(rafDsRequest);
            } else if (method.equals("fetchSalesData")) {
                System.out.println("fetchSalesData");
                rafDsCommand = new FetchSalesOrderAWBInfoDetailDataCommand(request.getParameter("awbInfoId"), username);
            }

            if (rafDsCommand != null) {
                RafDsResponse rafDsResponse = rafDsCommand.execute();
                try {
                    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
                    System.out.println(retVal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (method.equals("fetchAttributeName")) {
                RafRpcCommand fetchAttributeNameCommand = new FetchAttributeNameDataCommand(request.getParameter("itemId"), username);
                retVal = fetchAttributeNameCommand.execute();
            } else if (method.equals("saveAttribute")) {
                RafRpcCommand saveAttributeCommand = new SaveAttributeDataCommand(username, request.getParameter("salesOrderId"), Util.extractRequestBody(request));
                retVal = saveAttributeCommand.execute();
            } else if (method.equals("savePacking")) {
                RafRpcCommand savePackingCommand = new SavePackingDataCommand(username, request.getParameter("awbInfoId"));
                retVal = savePackingCommand.execute();
            }else if (method.equals("rejectPacking")) {
                RafRpcCommand rejectPackingCommand = new RejectPackingDataCommand(username, request.getParameter("salesOrderId"));
                retVal = rejectPackingCommand.execute();
            }
        }

        response.getOutputStream().println(retVal);
    }
}
