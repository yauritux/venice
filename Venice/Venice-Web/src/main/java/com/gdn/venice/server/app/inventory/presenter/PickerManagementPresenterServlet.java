/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.presenter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.venice.server.app.inventory.command.NonActivePickerDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchPickerDataCommand;
import com.gdn.venice.server.app.inventory.command.SaveOrUpdatePickerDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 *
 * @author Maria Olivia
 */
public class PickerManagementPresenterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public PickerManagementPresenterServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type") == null ? "" : request.getParameter("type");
        String username = Util.getUserName(request);
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

            if (method.equals("fetchPickerData")) {
                System.out.println("fetchPickerData");
                rafDsRequest.setParams(params);
                RafDsCommand rafDsCommand = new FetchPickerDataCommand(rafDsRequest);
                RafDsResponse rafDsResponse = rafDsCommand.execute();
                try {
                    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
                    System.out.println(retVal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (method.equals("nonActivePicker")) {
                RafRpcCommand nonActivePickerCommand = new NonActivePickerDataCommand(username, request.getParameter("id"));
                retVal = nonActivePickerCommand.execute();
            } else if (method.equals("saveUpdatePicker")) {
                String requestBody = Util.extractRequestBody(request);
                System.out.println(requestBody);
                RafRpcCommand savePickerCommand = new SaveOrUpdatePickerDataCommand(username, requestBody);
                retVal = savePickerCommand.execute();
            }
        }

        response.getOutputStream().println(retVal);
    }
}