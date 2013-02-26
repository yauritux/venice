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

import com.gdn.venice.server.app.inventory.command.DeleteCurrencyDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchCurrencyDataCommand;
import com.gdn.venice.server.app.inventory.command.SaveOrUpdateCurrencyDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 *
 * @author Maria Olivia
 */
public class CurrencyManagementPresenterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public CurrencyManagementPresenterServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	System.out.println("masuk post warehouse management servlet");
        String type = request.getParameter("type") == null ? "" : request.getParameter("type");        
        String username = request.getParameter("username");
        if(username == null || username.trim().equals("")){
        	username = "olive";
        }        
        String retVal = "";

        String method = request.getParameter("method");

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username.trim());
        
        if (type.equals(RafDsCommand.DataSource)) {
        	System.out.println("Masuk data source");
            String requestBody = Util.extractRequestBody(request);
            params.put("limit", request.getParameter("limit"));
            params.put("page", request.getParameter("page"));
            
            RafDsRequest rafDsRequest = null;
            try {
                rafDsRequest = RafDsRequest.convertXmltoRafDsRequest(requestBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (method.equals("fetchCurrencyData")) {
            	System.out.println("fetchCurrencyData");
            	rafDsRequest.setParams(params);
            	RafDsCommand rafDsCommand = new FetchCurrencyDataCommand(rafDsRequest);
                RafDsResponse rafDsResponse = rafDsCommand.execute();
                try {
                    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
                    System.out.println(retVal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Masuk RPC");
        	if(method.equals("deleteCurrency")){
	            RafRpcCommand deleteCurrencyCommand = new DeleteCurrencyDataCommand(username, request.getParameter("id"));
	            retVal = deleteCurrencyCommand.execute();
        	} else if(method.equals("saveUpdateCurrency")){
	            String requestBody = Util.extractRequestBody(request);
	            System.out.println(requestBody);
	            RafRpcCommand saveCurrencyCommand = new SaveOrUpdateCurrencyDataCommand(username, requestBody);
	            retVal = saveCurrencyCommand.execute();
	            System.out.println(retVal);
        	}
        }

        response.getOutputStream().println(retVal);
    }
}