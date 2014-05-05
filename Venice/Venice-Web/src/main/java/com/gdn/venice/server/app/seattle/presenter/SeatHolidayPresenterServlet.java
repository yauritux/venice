package com.gdn.venice.server.app.seattle.presenter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.seattle.presenter.commands.AddHolidayDataCommand;
import com.gdn.venice.server.app.seattle.presenter.commands.DeleteHolidayDataCommand;
import com.gdn.venice.server.app.seattle.presenter.commands.FetchHolidayDataCommand;
import com.gdn.venice.server.app.seattle.presenter.commands.UpdateHolidayDataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * Servlet implementation class SeatETDPresenterServlet
 */
public class SeatHolidayPresenterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String notificationText = "";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SeatHolidayPresenterServlet() {
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
			if (method.equals("fetchHolidayData")) {				
				RafDsCommand fetchHolidayData = new FetchHolidayDataCommand(rafDsRequest, userName);
				RafDsResponse rafDsResponse = fetchHolidayData.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (method.equals("deleteHolidayData")) {			
				RafDsCommand fetchHolidayData = new DeleteHolidayDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchHolidayData.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (method.equals("addHolidayData")) {				
				RafDsCommand fetchHolidayData = new AddHolidayDataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchHolidayData.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if (method.equals("updateHolidayData")) {
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(DataNameTokens.HOLIDAY_ID, request.getParameter(DataNameTokens.HOLIDAY_ID));
				rafDsRequest.setParams(params);	
				RafDsCommand fetchHolidayData = new UpdateHolidayDataCommand(rafDsRequest, userName);
				RafDsResponse rafDsResponse = fetchHolidayData.execute();
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
