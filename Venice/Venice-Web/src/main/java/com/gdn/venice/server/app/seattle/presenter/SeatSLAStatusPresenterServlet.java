package com.gdn.venice.server.app.seattle.presenter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.kpi.presenter.commands.FetchKpiSetupPartySlaDataCommandComboBoxKpi;
import com.gdn.venice.server.app.seattle.presenter.commands.FetchSLAStatusDataCommand;
import com.gdn.venice.server.app.seattle.presenter.commands.FetchUoMCommandComboBox;
import com.gdn.venice.server.app.seattle.presenter.commands.UpdateSLAFulfillmenDataCommand;
import com.gdn.venice.server.app.seattle.presenter.commands.UpdateSLAStatusDataCommand;
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
			if (method.equals("fetchSLAStatusData")) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("userRole", request.getParameter("userRole"));
				rafDsRequest.setParams(params);	
				RafDsCommand fetchSLAStatusData = new FetchSLAStatusDataCommand(rafDsRequest, userName);
				RafDsResponse rafDsResponse = fetchSLAStatusData.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if(method.equals("updateSLAStatusData")){		
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_ID, request.getParameter(DataNameTokens.SEATSLASTATUSPERCENTAGE_ID));
				rafDsRequest.setParams(params);	
				RafDsCommand updateSLAStatusData = new UpdateSLAStatusDataCommand(rafDsRequest,userName);
				RafDsResponse rafDsResponse = updateSLAStatusData.execute();
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
			if(method.equals("fetchUoMCommandComboBox")){										
				RafRpcCommand fetchUoMCommandComboBox = new FetchUoMCommandComboBox();
				retVal = fetchUoMCommandComboBox.execute();				
			}
		}
		response.getOutputStream().println(retVal);
	}

}
