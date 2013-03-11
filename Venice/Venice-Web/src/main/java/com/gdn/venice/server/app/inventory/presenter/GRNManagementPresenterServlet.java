package com.gdn.venice.server.app.inventory.presenter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.command.FetchGRNDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchGRNItemDataCommand;
import com.gdn.venice.server.app.inventory.command.GetMaxGRNItemQuantityAllowedDataCommand;
import com.gdn.venice.server.app.inventory.command.SaveGrnDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchItemAttributeDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * 
 * @author Roland
 */
public class GRNManagementPresenterServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	public GRNManagementPresenterServlet(){
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

			if (request.getParameter(DataNameTokens.INV_GRN_ID)!=null) {
				params.put(DataNameTokens.INV_GRN_ID, request.getParameter(DataNameTokens.INV_GRN_ID));				
			}
			
			if (request.getParameter(DataNameTokens.INV_ASN_ITEM_ID)!=null) {
				params.put(DataNameTokens.INV_ASN_ITEM_ID, request.getParameter(DataNameTokens.INV_ASN_ITEM_ID));				
			}
			
			rafDsRequest.setParams(params);
			
			String method = request.getParameter("method");
			
			if(method.equals("fetchGRNData")){			
				RafDsCommand fetchGRNDataCommand = new FetchGRNDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchGRNDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(method.equals("fetchGRNItemData")){
				RafDsCommand fetchGRNItemDataCommand = new FetchGRNItemDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchGRNItemDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(method.equals("fetchItemAttributeData")){
				System.out.println("fetchItemAttributeData di servlet");
				RafDsCommand fetchItemAttributeDataCommand = new FetchItemAttributeDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchItemAttributeDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}else if (type.equals(RafRpcCommand.RPC)) {
			String method = request.getParameter("method");	
			String requestBody = Util.extractRequestBody(request);	
			if(method.equals("saveGrnData")){					
				RafRpcCommand saveGrnDataCommand = new SaveGrnDataCommand(username, requestBody);
				retVal = saveGrnDataCommand.execute();
			}else if(method.equals("getMaxGRNItemQuantityAllowed")){
				String asnItemId = request.getParameter("asnItemId");
				RafRpcCommand getMaxGRNItemQuantityAllowedDataCommand = new GetMaxGRNItemQuantityAllowedDataCommand(username, asnItemId);
				retVal = getMaxGRNItemQuantityAllowedDataCommand.execute();
			}
		}
		
		response.getOutputStream().println(retVal);
	}
}
