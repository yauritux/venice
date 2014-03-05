package com.gdn.venice.server.app.inventory.presenter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.command.FetchASNDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchASNItemDataCommand;
import com.gdn.venice.server.app.inventory.command.FetchStorageDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * 
 * @author Roland
 */
public class ASNManagementPresenterServlet extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	public ASNManagementPresenterServlet(){
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

			if (request.getParameter(DataNameTokens.INV_ASN_ID)!=null) {
				params.put(DataNameTokens.INV_ASN_ID, request.getParameter(DataNameTokens.INV_ASN_ID));				
			}
			
			rafDsRequest.setParams(params);
			
			String method = request.getParameter("method");
			
			if(method.equals("fetchASNData")){				
				System.out.println("fetchASNData");
				RafDsCommand fetchASNDataCommand = new FetchASNDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchASNDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}else if(method.equals("fetchASNItemData")){
				System.out.println("fetchASNItemData");
				RafDsCommand fetchASNItemDataCommand = new FetchASNItemDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchASNItemDataCommand.execute();
				try{
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		response.getOutputStream().println(retVal);
	}
}
