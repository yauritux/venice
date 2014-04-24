package com.gdn.venice.server.app.inventory.presenter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.command.ApproveCreateShelfWIPDataCommand;
import com.gdn.venice.server.app.inventory.command.ApproveEditShelfDataCommand;
import com.gdn.venice.server.app.inventory.command.ApproveNonActiveShelfDataCommand;
import com.gdn.venice.server.app.inventory.command.EditShelfAddWIPDataCommand;
import com.gdn.venice.server.app.inventory.command.EditShelfDataCommand;
import com.gdn.venice.server.app.inventory.command.EditShelfEditWIPDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchShelfDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchShelfInProcessDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchStorageDataCommand;
import com.gdn.venice.server.app.inventory.command.NeedCorrectionCreateShelfWIPDataCommand;
import com.gdn.venice.server.app.inventory.command.NeedCorrectionEditShelfDataCommand;
import com.gdn.venice.server.app.inventory.command.NonActiveShelfDataCommand;
import com.gdn.venice.server.app.inventory.command.RejectCreateShelfWIPDataCommand;
import com.gdn.venice.server.app.inventory.command.RejectEditShelfDataCommand;
import com.gdn.venice.server.app.inventory.command.RejectNonActiveShelfDataCommand;
import com.gdn.venice.server.app.inventory.command.SaveShelfWIPDataCommand;
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
				RafDsCommand fetchShelfDataCommand = new FetchShelfDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchShelfDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(method.equals("fetchStorageData")){
				RafDsCommand fetchStorageDataCommand = new FetchStorageDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchStorageDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if (method.equals("fetchShelfInProcessCreateData")) {
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

			String requestBody = Util.extractRequestBody(request);		
			if(method.equals("saveShelfData")){
				RafRpcCommand saveShelfWIPDataCommand = new SaveShelfWIPDataCommand(username, requestBody);
				retVal = saveShelfWIPDataCommand.execute();
			}else if(method.equals("approveCreateShelfWIP")){	
				RafRpcCommand approveCreateShelfWIPDataCommand = new ApproveCreateShelfWIPDataCommand(username, requestBody);
				retVal = approveCreateShelfWIPDataCommand.execute();
			}else if(method.equals("rejectCreateShelfWIP")){	
				RafRpcCommand rejectCreateShelfWIPDataCommand = new RejectCreateShelfWIPDataCommand(username, requestBody);
				retVal = rejectCreateShelfWIPDataCommand.execute();
			}else if(method.equals("needCorrectionCreateShelfWIP")){
				RafRpcCommand needCorrectionCreateShelfWIPDataCommand = new NeedCorrectionCreateShelfWIPDataCommand(username, requestBody);
				retVal = needCorrectionCreateShelfWIPDataCommand.execute();
			}else if(method.equals("nonActiveShelf")){		
				RafRpcCommand nonActiveShelfDataCommand = new NonActiveShelfDataCommand(username, requestBody);
				retVal = nonActiveShelfDataCommand.execute();
			}else if(method.equals("approveNonActiveShelf")){	
				RafRpcCommand approveNonActiveShelfDataCommand = new ApproveNonActiveShelfDataCommand(username, requestBody);
				retVal = approveNonActiveShelfDataCommand.execute();
			}else if(method.equals("rejectNonActiveShelf")){	
				RafRpcCommand rejectNonActiveShelfDataCommand = new RejectNonActiveShelfDataCommand(username, requestBody);
				retVal = rejectNonActiveShelfDataCommand.execute();
			}else if(method.equals("editShelfData")){
				RafRpcCommand editShelfDataCommand = new EditShelfDataCommand(username, requestBody);
				retVal = editShelfDataCommand.execute();
			}else if(method.equals("approveEditShelf")){	
				RafRpcCommand approveEditShelfDataCommand = new ApproveEditShelfDataCommand(username, requestBody);
				retVal = approveEditShelfDataCommand.execute();
			}else if(method.equals("rejectEditShelf")){	
				RafRpcCommand rejectEditShelfDataCommand = new RejectEditShelfDataCommand(username, requestBody);
				retVal = rejectEditShelfDataCommand.execute();
			}else if(method.equals("needCorrectionEditShelf")){
				RafRpcCommand needCorrectionEditShelfDataCommand = new NeedCorrectionEditShelfDataCommand(username, requestBody);
				retVal = needCorrectionEditShelfDataCommand.execute();
			}else if(method.equals("editShelfAddWIPData")){
				RafRpcCommand editShelfWIPAddDataCommand = new EditShelfAddWIPDataCommand(username, requestBody);
				retVal = editShelfWIPAddDataCommand.execute();
			}else if(method.equals("editShelfEditWIPData")){
				RafRpcCommand editShelfWIPEditDataCommand = new EditShelfEditWIPDataCommand(username, requestBody);
				retVal = editShelfWIPEditDataCommand.execute();
			}
		}
		
		response.getOutputStream().println(retVal);
	}
}
