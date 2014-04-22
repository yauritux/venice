package com.gdn.venice.server.app.inventory.presenter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.venice.server.app.inventory.command.FetchPickerComboBoxDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchPickingListIRDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchPickingListIRDetailDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchPickingListSODataCommand;
import com.gdn.venice.server.app.inventory.command.FetchPickingListSODetailDataCommand;
import com.gdn.venice.server.app.inventory.command.SubmitPickerDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 *
 * @author Roland
 */
public class PickingListManagementPresenterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public PickingListManagementPresenterServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type");
        String retVal = "";
        String username = Util.getUserName(request);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username.trim());

        String method = request.getParameter("method");
        String requestBody = Util.extractRequestBody(request);

        if (type.equals(RafDsCommand.DataSource)) {
            params.put("limit", request.getParameter("limit"));
            params.put("page", request.getParameter("page"));

            RafDsRequest rafDsRequest = null;
            try {
                rafDsRequest = RafDsRequest.convertXmltoRafDsRequest(requestBody);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (request.getParameter("warehouseId") != null) {
                params.put("warehouseId", request.getParameter("warehouseId"));
            }
            if (request.getParameter("warehouseItemId") != null) {
                params.put("warehouseItemId", request.getParameter("warehouseItemId"));
            }
            if (request.getParameter("packageId") != null) {
                params.put("packageId", request.getParameter("packageId"));
            }
            rafDsRequest.setParams(params);
            
            if (method.equals("fetchPickingListIRData")) {
                RafDsCommand fetchPickingListIRDataCommand = new FetchPickingListIRDataCommand(rafDsRequest);
                RafDsResponse rafDsResponse = fetchPickingListIRDataCommand.execute();
                try {
                    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(method.equals("fetchPickingListIRDetailData")){
				RafDsCommand fetchPickingListIRDetailDataCommand = new FetchPickingListIRDetailDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchPickingListIRDetailDataCommand.execute();
				try {
				    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
				    e.printStackTrace();
				}
            }else if (method.equals("fetchPickingListSOData")) {
                RafDsCommand fetchPickingListSODataCommand = new FetchPickingListSODataCommand(rafDsRequest);
                RafDsResponse rafDsResponse = fetchPickingListSODataCommand.execute();
                try {
                    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(method.equals("fetchPickingListSODetailData")){
				RafDsCommand fetchPickingListSODetailDataCommand = new FetchPickingListSODetailDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchPickingListSODetailDataCommand.execute();
				try {
				    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
				    e.printStackTrace();
				}
            }
        } else if (type.equals(RafRpcCommand.RPC)) {            
            if (method.equals("fetchPickerComboBoxData")) {
            	RafDsRequest rafDsRequest = new RafDsRequest();
                
                if (request.getParameter("warehouseName") != null) {
                    params.put("warehouseName", request.getParameter("warehouseName"));
                }
                params.put("limit", request.getParameter("limit"));
                params.put("page", request.getParameter("page"));
                rafDsRequest.setParams(params);
                
                RafRpcCommand fetchPickerComboBoxDataCommand = new FetchPickerComboBoxDataCommand(rafDsRequest);
                retVal = fetchPickerComboBoxDataCommand.execute();
            } else if (method.equals("submitPickerData")) {
                RafRpcCommand submitPickerDataCommand = new SubmitPickerDataCommand(requestBody, request.getParameter("pickerId"));
                retVal = submitPickerDataCommand.execute();
            } 
        }

        response.getOutputStream().println(retVal);
    }
}
