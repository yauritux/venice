/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.presenter;

import com.gdn.venice.server.app.inventory.command.CheckAwbNumberCommand;
import com.gdn.venice.server.app.inventory.command.FetchAwbGinListDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchGinDataCommand;
import com.gdn.venice.server.app.inventory.command.SaveGINDataCommand;
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
public class GINPresenterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public GINPresenterServlet() {
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

            if (method.equals("fetchGinData")) {
                params.put("warehouseCode", request.getParameter("warehouseCode"));
                rafDsRequest.setParams(params);
                rafDsCommand = new FetchGinDataCommand(rafDsRequest);
            } else if(method.equals("fetchAwbListData")){
                rafDsCommand = new FetchAwbGinListDataCommand(request.getParameter("ginId"));
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
            if (method.equals("checkAirwayBillNumber")) {
                RafRpcCommand cekAwbCommand = new CheckAwbNumberCommand(request.getParameter("awbNumber"),
                        request.getParameter("logistic"), request.getParameter("warehouseCode"));
                retVal = cekAwbCommand.execute();
            } else if (method.equals("saveGIN")) {
                RafRpcCommand cekAwbCommand = new SaveGINDataCommand(username, Util.extractRequestBody(request));
                retVal = cekAwbCommand.execute();
            }
        }

        response.getOutputStream().println(retVal);
    }
}
