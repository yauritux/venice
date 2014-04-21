package com.gdn.venice.server.app.general.presenter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import com.djarum.raf.utilities.Locator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.venice.facade.VenOrderSessionEJBRemote;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.VeniceConstants;
import com.gwtplatform.annotation.Out;

/**
 * Servlet implementation class VeniceOrderPaidStatusServlet
 */
public class VeniceOrderPaidStatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VeniceOrderPaidStatusServlet() {
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
		// TODO Auto-generated method stub
	}
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String orderItemId = request.getParameter("orderItemId");
        String orderItemIdArray[] = orderItemId.split(",");
        String json="";
        List <String> orderItemIdList= new ArrayList<String>();
        
        Locator<Object> locator = null;
        VenOrderSessionEJBRemote orderHome;
        ObjectMapper mapper = new ObjectMapper();
        
        try{
            locator = new Locator<Object>();
            orderHome = (VenOrderSessionEJBRemote) locator.lookup(VenOrderSessionEJBRemote.class, "VenOrderSessionEJBBean");
            VenOrder venOrder=null;
            for(int i=0;i<orderItemIdArray.length;i++){
            	String query="select o from VenOrder o where o.venOrderItem.wcsOrderId = '"+orderItemIdArray[i]+"' and o.venOrderStatus.orderStatusId = "+VeniceConstants.VEN_ORDER_STATUS_FP+"";
            	venOrder=orderHome.queryByRange(query,0,1).get(0);
            	if(venOrder!=null){
            		orderItemIdList.add(orderItemIdArray[i]);
            	}
            }
        }catch(JSONException e){
        	e.printStackTrace();
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        json=mapper.writeValueAsString(orderItemIdList);
        response.getOutputStream().print(json);
	}
	
	
}
