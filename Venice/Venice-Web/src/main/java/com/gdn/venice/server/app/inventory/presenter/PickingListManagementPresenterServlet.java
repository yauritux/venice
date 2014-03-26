package com.gdn.venice.server.app.inventory.presenter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.venice.server.app.inventory.command.FetchPickingListDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchPickingListItemDetailDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchPickingListSalesOrderDetailDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchPickingListStorageDetailDataCommand;
import com.gdn.venice.server.app.inventory.command.ReleaseLockDataCommand;
import com.gdn.venice.server.app.inventory.command.SavePickingListDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * 
 * @author Roland
 */
public class PickingListManagementPresenterServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	public PickingListManagementPresenterServlet(){
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
        
		String method = request.getParameter("method");
		String requestBody = Util.extractRequestBody(request);

		if (type.equals(RafDsCommand.DataSource)){
	        params.put("limit", request.getParameter("limit"));
	        params.put("page", request.getParameter("page"));
	        
			RafDsRequest rafDsRequest = null;
			try{
				rafDsRequest = RafDsRequest.convertXmltoRafDsRequest(requestBody);
			}catch(Exception e){
				e.printStackTrace();
			}

			if(request.getParameter("warehouseId")!=null){
				params.put("warehouseId", request.getParameter("warehouseId"));
			}
			if(request.getParameter("warehouseItemId")!=null){
				params.put("warehouseItemId", request.getParameter("warehouseItemId"));
			}

			rafDsRequest.setParams(params);
			
			if(method.equals("fetchPickingListData")){	
				RafDsCommand fetchPickingListDataCommand = new FetchPickingListDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchPickingListDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(method.equals("fetchPickingListItemDetailData")){
				RafDsCommand fetchPickingListItemDetailDataCommand = new FetchPickingListItemDetailDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchPickingListItemDetailDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(method.equals("fetchPickingListSalesOrderDetailData")){	
				RafDsCommand fetchPickingListSalesOrderDetailDataCommand = new FetchPickingListSalesOrderDetailDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchPickingListSalesOrderDetailDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(method.equals("fetchPickingListStorageDetailData")){	
				RafDsCommand fetchPickingListStorageDetailDataCommand = new FetchPickingListStorageDetailDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchPickingListStorageDetailDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}else if (type.equals(RafRpcCommand.RPC)) {
			if(method.equals("releaseLock")){					
				RafRpcCommand releaseLockDataCommand = new ReleaseLockDataCommand(username, request.getParameter("warehouseId"));
				retVal = releaseLockDataCommand.execute();
			}else if(method.equals("savePickingListData")){					
				RafRpcCommand SavePickingListDataCommand = new SavePickingListDataCommand(username, requestBody);
				retVal = SavePickingListDataCommand.execute();
			}
		}
		
		response.getOutputStream().println(retVal);
	}
}
