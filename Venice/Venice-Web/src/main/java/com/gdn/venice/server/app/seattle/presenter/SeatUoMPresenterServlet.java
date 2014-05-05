package com.gdn.venice.server.app.seattle.presenter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.seattle.presenter.commands.FetchUoMDataCommand;
import com.gdn.venice.server.app.seattle.presenter.commands.UpdateUoMDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * Servlet implementation class SeatUoMPresenterServlet
 */
public class SeatUoMPresenterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SeatUoMPresenterServlet() {
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
			if (method.equals("fetchUoMData")) {
				RafDsCommand fetchUoMData = new FetchUoMDataCommand(rafDsRequest, userName);
				RafDsResponse rafDsResponse = fetchUoMData.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if(method.equals("updateUoMData")){		
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(DataNameTokens.SEATSTATUSUOM_ID, request.getParameter(DataNameTokens.SEATSTATUSUOM_ID));
				rafDsRequest.setParams(params);	
				RafDsCommand updateUoMData = new UpdateUoMDataCommand(rafDsRequest,userName);
				RafDsResponse rafDsResponse = updateUoMData.execute();
				try {
					if (rafDsResponse.getStatus() == 0){
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
					}else {
						String errorMessage = "Please check again of data is selected to be Updated";						
						retVal = "<response><status>-1</status><data>" + errorMessage + "</data></response>";
					}
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
