package com.gdn.venice.server.app.seattle.presenter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.finance.presenter.commands.FetchBankAccountComboBoxDataCommand;
import com.gdn.venice.server.app.finance.presenter.commands.FetchJournalRelatedLogisticsInvoiceDataCommand;
import com.gdn.venice.server.app.finance.presenter.commands.FetchLogisticsPaymentDataCommand;
import com.gdn.venice.server.app.finance.presenter.commands.FetchLogisticsPaymentProcessingDataCommand;
import com.gdn.venice.server.app.finance.presenter.commands.FetchManualJournalTransactionPaymentData;
import com.gdn.venice.server.app.finance.presenter.commands.FetchMerchantPaymentDataCommand;
import com.gdn.venice.server.app.finance.presenter.commands.FetchMerchantPaymentProcessingDataCommand;
import com.gdn.venice.server.app.finance.presenter.commands.FetchPaymentDataCommand;
import com.gdn.venice.server.app.finance.presenter.commands.FetchRefundPaymentDataCommand;
import com.gdn.venice.server.app.finance.presenter.commands.FetchRefundPaymentProcessingDataCommand;
import com.gdn.venice.server.app.finance.presenter.commands.FinishPaymentCommand;
import com.gdn.venice.server.app.finance.presenter.commands.MakePaymentCommand;
import com.gdn.venice.server.app.finance.presenter.commands.PaymentsProcessCommand;
import com.gdn.venice.server.app.finance.presenter.commands.UpdateRefundPaymentProcessingDataCommand;
import com.gdn.venice.server.app.seattle.presenter.commands.FetchSLAFulfillmenDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * Servlet implementation class SeatSLAStatusPresenterServlet
 */
public class SeatSLAStatusPresenterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SeatSLAStatusPresenterServlet() {
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
		String retVal =  "";
		
		String userName = Util.getUserName(request);
		
		if (type.equals(RafDsCommand.DataSource)) {
                        response.setContentType("application/xml");
			String requestBody = Util.extractRequestBody(request);
	
			RafDsRequest rafDsRequest = null;
			try {
				rafDsRequest = RafDsRequest.convertXmltoRafDsRequest(requestBody);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String method = request.getParameter("method");
			if (method.equals("fetchSLAFulfillmenData")) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("userRole", request.getParameter("userRole"));
				rafDsRequest.setParams(params);	
				RafDsCommand fetchSLAFulfillmenData = new FetchSLAFulfillmenDataCommand(rafDsRequest, userName);
				RafDsResponse rafDsResponse = fetchSLAFulfillmenData.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
		} else if (type.equals(RafRpcCommand.RPC)) {
			String method = request.getParameter("method");			
			String requestBody = Util.extractRequestBody(request);	
		}
		response.getOutputStream().println(retVal);
	}

}
