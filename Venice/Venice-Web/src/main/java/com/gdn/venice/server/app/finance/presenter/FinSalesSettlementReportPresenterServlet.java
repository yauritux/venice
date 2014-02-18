package com.gdn.venice.server.app.finance.presenter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.venice.server.app.finance.presenter.commands.FetchMerchantComboBoxDataCommand;
import com.gdn.venice.server.app.finance.presenter.commands.FetchSalesSettlementRecordDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * Servlet implementation class FinSalesSettlementReportPresenterServlet
 */
public class FinSalesSettlementReportPresenterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FinSalesSettlementReportPresenterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
			String type = request.getParameter("type");
			String retVal = "";
			String userName = Util.getUserName(request);

			if (type.equals(RafDsCommand.DataSource)) {
				String requestBody = Util.extractRequestBody(request);

				RafDsRequest rafDsRequest = null;
				try {
					rafDsRequest = RafDsRequest
							.convertXmltoRafDsRequest(requestBody);
				} catch (Exception e) {
					e.printStackTrace();
				}

				String method = request.getParameter("method");
				if (method.equals("fetchSalesSettlementRecordData")) {					
					RafDsCommand fetchSalesSettlementRecordDataCommand = new FetchSalesSettlementRecordDataCommand(rafDsRequest);
					RafDsResponse rafDsResponse = fetchSalesSettlementRecordDataCommand.execute();
					try {
						retVal = RafDsResponse
								.convertRafDsResponsetoXml(rafDsResponse);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} 
			}else if (type.equals(RafRpcCommand.RPC)) {
				String method = request.getParameter("method");			
				String requestBody = Util.extractRequestBody(request);
				if(method.equals("fetchMerchantComboBoxData")){			
					/*
					 * Get the combo box data for Merchant
					 */
					RafRpcCommand fetchMerchantComboBoxData = new FetchMerchantComboBoxDataCommand();
					retVal = fetchMerchantComboBoxData.execute();				
				} 
			}
			response.getOutputStream().println(retVal);
	}

}
