package com.gdn.venice.server.app.inventory.presenter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.command.FetchShelfDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchStorageDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchShelfInProcessDataCommand;
import com.gdn.venice.server.app.inventory.command.SaveOrUpdateShelfWIPDataCommand;
import com.gdn.venice.server.app.inventory.command.SaveOrUpdateStatusShelfWIPDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * 
 * @author Roland
 */
public class ShelfManagementPresenterServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	public ShelfManagementPresenterServlet(){
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String type = request.getParameter("type");
		String retVal =  "";
		
		String username = Util.getUserName(request);
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username.trim());
		
		if (type.equals(RafDsCommand.DataSource)){
			String requestBody = Util.extractRequestBody(request);
	        params.put("limit", request.getParameter("limit"));
	        params.put("page", request.getParameter("page"));
			
			RafDsRequest rafDsRequest = null;
			try{
				rafDsRequest = RafDsRequest.convertXmltoRafDsRequest(requestBody);
			}catch(Exception e){
				e.printStackTrace();
			}

			if (request.getParameter(DataNameTokens.INV_SHELF_ID)!=null) {
				params.put(DataNameTokens.INV_SHELF_ID, request.getParameter(DataNameTokens.INV_SHELF_ID));				
			}
			
			rafDsRequest.setParams(params);
			
			String method = request.getParameter("method");
			
			if(method.equals("fetchShelfData")){				
				System.out.println("fetchShelfData");
				RafDsCommand fetchShelfDataCommand = new FetchShelfDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchShelfDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(method.equals("fetchStorageData")){
				System.out.println("fetchStorageData");
				RafDsCommand fetchStorageDataCommand = new FetchStorageDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchStorageDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if (method.equals("fetchShelfInProcessCreateData")) {
            	System.out.println("fetchShelfInProcessCreateData");
            	params.put(DataNameTokens.INV_SHELF_APPROVALTYPE, ApprovalStatus.APPROVAL_CREATE.toString());
            	rafDsRequest.setParams(params);
                RafDsCommand rafDsCommand = new FetchShelfInProcessDataCommand(rafDsRequest);
                RafDsResponse rafDsResponse = rafDsCommand.execute();
                try {
                    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (method.equals("fetchShelfInProcessEditData")) {
            	System.out.println("fetchShelfInProcessEditData");
            	params.put(DataNameTokens.INV_SHELF_APPROVALTYPE, ApprovalStatus.APPROVAL_UPDATE.toString());
            	rafDsRequest.setParams(params);
                RafDsCommand rafDsCommand = new FetchShelfInProcessDataCommand(rafDsRequest);
                RafDsResponse rafDsResponse = rafDsCommand.execute();
                try {
                    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (method.equals("fetchShelfInProcessNonActiveData")) {
            	System.out.println("fetchShelfInProcessNonActiveData");
            	params.put(DataNameTokens.INV_SHELF_APPROVALTYPE, ApprovalStatus.APPROVAL_NON_ACTIVE.toString());
            	rafDsRequest.setParams(params);
                RafDsCommand rafDsCommand = new FetchShelfInProcessDataCommand(rafDsRequest);
                RafDsResponse rafDsResponse = rafDsCommand.execute();
                try {
                    retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
		}else if (type.equals(RafRpcCommand.RPC)) {
			String method = request.getParameter("method");	
			if(method.equals("saveShelfData") || method.equals("saveUpdateShelfWIP")){		
				System.out.println("saveShelfData");
				String requestBody = Util.extractRequestBody(request);		
				System.out.println("requestBody: "+requestBody);
				RafRpcCommand saveOrUpdateShelfWIPDataCommand = new SaveOrUpdateShelfWIPDataCommand(username, requestBody);
				retVal = saveOrUpdateShelfWIPDataCommand.execute();
			} else if(method.equals("saveUpdateStatusShelf") || method.equals("saveUpdateStatusShelf")){		
				System.out.println("updateStatusShelfData");
				String requestBody = Util.extractRequestBody(request);		
				System.out.println("requestBody: "+requestBody);
				RafRpcCommand saveOrUpdateStatusShelfWIPDataCommand = new SaveOrUpdateStatusShelfWIPDataCommand(username, requestBody);
				retVal = saveOrUpdateStatusShelfWIPDataCommand.execute();
			} 
		}
		
		response.getOutputStream().println(retVal);
	}
}
