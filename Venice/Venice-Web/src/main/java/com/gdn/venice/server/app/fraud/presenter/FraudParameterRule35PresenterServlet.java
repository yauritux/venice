package com.gdn.venice.server.app.fraud.presenter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gdn.venice.server.app.fraud.presenter.commands.AddFraudParameterRule35DataCommand;
import com.gdn.venice.server.app.fraud.presenter.commands.DeleteFraudParameterRule35DataCommand;
import com.gdn.venice.server.app.fraud.presenter.commands.FetchFraudParameterRule35DataCommand;
import com.gdn.venice.server.app.fraud.presenter.commands.UpdateFraudParameterRule35DataCommand;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

/**
 * Servlet implementation class FraudParameterRule35PresenterServlet
 * 
 * @author
 */
public class FraudParameterRule35PresenterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FraudParameterRule35PresenterServlet() {
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
			if (method.equals("fetchFraudParameterRule35Data")) {			
				RafDsCommand fetchFraudParameterRule35DataCommand = new FetchFraudParameterRule35DataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = fetchFraudParameterRule35DataCommand.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(method.equals("addFraudParameterRule35Data")){
				RafDsCommand addFraudParameterRule35DataCommand = new AddFraudParameterRule35DataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = addFraudParameterRule35DataCommand.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(method.equals("updateFraudParameterRule35Data")){			
				RafDsCommand updateFraudParameterRule35DataCommand = new UpdateFraudParameterRule35DataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = updateFraudParameterRule35DataCommand.execute();
				try {
					retVal = RafDsResponse.convertRafDsResponsetoXml(rafDsResponse);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}else if(method.equals("deleteFraudParameterRule35Data")){			
				RafDsCommand removeFraudParameterRule35DataCommand = new DeleteFraudParameterRule35DataCommand(rafDsRequest);
				RafDsResponse rafDsResponse = removeFraudParameterRule35DataCommand.execute();
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
