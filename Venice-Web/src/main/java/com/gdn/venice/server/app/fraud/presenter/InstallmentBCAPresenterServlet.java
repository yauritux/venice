package com.gdn.venice.server.app.fraud.presenter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.venice.server.app.fraud.presenter.commands.FetchCancelInstallmentDataCommand;
import com.gdn.venice.server.app.fraud.presenter.commands.FetchConvertInstallmentDataCommand;
import com.gdn.venice.server.app.fraud.presenter.commands.UpdateCancelInstallmentDataCommand;
import com.gdn.venice.server.app.fraud.presenter.commands.UpdateConvertInstallmentDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * Servlet implementation class InstallmentBCAPresenterServlet
 * 
 * @author Roland
 */
public class InstallmentBCAPresenterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public InstallmentBCAPresenterServlet() {
        super();
       
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String type = request.getParameter("type");
		String retVal =  "";
		
		if (type.equals(RafDsCommand.DataSource)) {
			String requestBody = Util.extractRequestBody(request);
	
			RafDsRequest rafDsRequest = null;
			try {
				rafDsRequest = RafDsRequest.convertXmltoRafDsRequest(requestBody);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String method = request.getParameter("method");
			if (method.equals("fetchConvertInstallmentData")) {				
				try {
					RafDsCommand fetchConvertInstallmentDataCommand = new FetchConvertInstallmentDataCommand(rafDsRequest);
					RafDsResponse rafDsResponse = fetchConvertInstallmentDataCommand.execute();
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(method.equals("updateConvertInstallmentData")){	
				String username = Util.getUserName(request);
				RafDsCommand updateConvertInstallmentDataCommand = new UpdateConvertInstallmentDataCommand(rafDsRequest, username);
				RafDsResponse rafDsResponse = updateConvertInstallmentDataCommand.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(method.equals("fetchCancelInstallmentData")) {
				try {
					RafDsCommand fetchCancelInstallmentDataCommand = new FetchCancelInstallmentDataCommand(rafDsRequest);
					RafDsResponse rafDsResponse = fetchCancelInstallmentDataCommand.execute();
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(method.equals("updateCancelInstallmentData")){	
				String username = Util.getUserName(request);
				RafDsCommand updateCancelInstallmentDataCommand = new UpdateCancelInstallmentDataCommand(rafDsRequest, username);
				RafDsResponse rafDsResponse = updateCancelInstallmentDataCommand.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}		
		} 
		response.getOutputStream().println(retVal);
	}
}
