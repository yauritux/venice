package com.gdn.venice.client.app.seattle.data;

import java.util.HashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceDateTimeField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DSOperationType;

public class SeattleData {
							
	public static RafDataSource getSLAFulfillment(String userRole) {
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_ID, "ID"),
				new DataSourceTextField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATORDERSTATUS_VENORDERSTATUS_CODE, "Order Status"), 
				new DataSourceTextField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATRESULTSTATUSTRACKING_DESC, "Status"),
				new DataSourceTextField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MIN, "Min"),
				new DataSourceTextField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MAX, "Max"), 
				new DataSourceTextField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_BYUSER, "By User"),
				new DataSourceDateTimeField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_UPDATEDATE, "Update Date"),
				new DataSourceTextField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATORDERSTATUS_PIC, "PIC")				
		};
		dataSourceFields[0].setPrimaryKey(true);
		
		RafDataSource dataSource = new RafDataSource(
				"/response/data/*",
				GWT.getHostPageBaseURL() + "SeaSLAFulfillmentPresenterServlet?method=fetchSLAFulfillmenData&type=DataSource",
				null,
				null,
				null,
				dataSourceFields); 
		
		 HashMap<String, String> params = new HashMap<String, String>();
	        if (userRole != null) {
	            params.put("userRole", userRole);
	        } else {
	            params.put("userRole", null);
	        }

	        dataSource.getOperationBinding(DSOperationType.FETCH).setDefaultParams(params);
		
		return dataSource;
				
	}
}