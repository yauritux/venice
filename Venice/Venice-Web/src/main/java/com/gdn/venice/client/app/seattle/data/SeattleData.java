package com.gdn.venice.client.app.seattle.data;

import java.util.HashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceDateField;
import com.smartgwt.client.data.fields.DataSourceDateTimeField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DSOperationType;

public class SeattleData {
							
	public static RafDataSource getSLAFulfillment(String userRole) {
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_ID, "ID"),
				new DataSourceTextField(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATORDERSTATUS_ORDERSTATUSDESC, "Order Status"), 
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
				GWT.getHostPageBaseURL() + "SeaSLAFulfillmentPresenterServlet?method=updateSLAFulfillmenData&type=DataSource",
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
	
	public static RafDataSource getSLAStatus(String userRole) {
		DataSourceField[] dataSourceFields = {				
				new DataSourceTextField(DataNameTokens.SEATSLASTATUSPERCENTAGE_ID, "ID"),
				new DataSourceTextField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATORDERSTATUS_ORDERSTATUSDESC, "Order Status"), 
				new DataSourceTextField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATRESULTSTATUSTRACKING_DESC, "Status"),
				new DataSourceTextField(DataNameTokens.SEATSLASTATUSPERCENTAGE_MIN, "Min"),
				new DataSourceTextField(DataNameTokens.SEATSLASTATUSPERCENTAGE_MAX, "Max"), 
				new DataSourceTextField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SLA, "Sla"), 
				new DataSourceTextField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATSTATUSUOM_DESC, "UoM Desc"), 
				new DataSourceTextField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATSTATUSUOM_ID, "UoMId"), 
				new DataSourceTextField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_BYUSER, "By User"),
				new DataSourceDateTimeField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_UPDATEDATE, "Update Date"),
				new DataSourceTextField(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATORDERSTATUS_PIC, "PIC")				
		};
		dataSourceFields[0].setPrimaryKey(true);
		
		RafDataSource dataSource = new RafDataSource(
				"/response/data/*",
				GWT.getHostPageBaseURL() + "SeatSLAStatusPresenterServlet?method=fetchSLAStatusData&type=DataSource",
				null,
				GWT.getHostPageBaseURL() + "SeatSLAStatusPresenterServlet?method=updateSLAStatusData&type=DataSource",
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
	
	public static RafDataSource getUoMData() {
		DataSourceField[] dataSourceFields = {								
				new DataSourceTextField(DataNameTokens.SEATSTATUSUOM_ID, "ID"),
				new DataSourceTextField(DataNameTokens.SEATSTATUSUOM_STATUSUOMDESC, "Desc"), 
				new DataSourceTextField(DataNameTokens.SEATSTATUSUOM_STATUSUOMFROM, "From"),
				new DataSourceTextField(DataNameTokens.SEATSTATUSUOM_STATUSUOMEND, "End"),
				new DataSourceTextField(DataNameTokens.SEATSTATUSUOM_BYUSER, "By User"), 		
				new DataSourceDateTimeField(DataNameTokens.SEATSTATUSUOM_UPDATEDATE, "Update Date")
							
		};
		dataSourceFields[0].setPrimaryKey(true);
		
		RafDataSource dataSource = new RafDataSource(
				"/response/data/*",
				GWT.getHostPageBaseURL() + "SeatUoMPresenterServlet?method=fetchUoMData&type=DataSource",
				null,
				GWT.getHostPageBaseURL() + "SeatUoMPresenterServlet?method=updateUoMData&type=DataSource",
				null,
				dataSourceFields); 
				
		return dataSource;
				
	}
	
	public static RafDataSource getHolidayData() {
		DataSourceField[] dataSourceFields = {								
				new DataSourceTextField(DataNameTokens.HOLIDAY_ID, "ID"),
				new DataSourceDateField(DataNameTokens.HOLIDAY_DATE, "Date"),
				new DataSourceTextField(DataNameTokens.HOLIDAY_DESKRIPSI, "Deskripsi")
		};
		dataSourceFields[0].setPrimaryKey(true);
		
		RafDataSource dataSource = new RafDataSource(
				"/response/data/*",
				GWT.getHostPageBaseURL() + "SeatHolidayPresenterServlet?method=fetchHolidayData&type=DataSource",
				GWT.getHostPageBaseURL() + "SeatHolidayPresenterServlet?method=addHolidayData&type=DataSource",
				GWT.getHostPageBaseURL() + "SeatHolidayPresenterServlet?method=updateHolidayData&type=DataSource",
				GWT.getHostPageBaseURL() + "SeatHolidayPresenterServlet?method=deleteHolidayData&type=DataSource",
				dataSourceFields); 
				
		return dataSource;
				
	}
	
	public static RafDataSource getSKUData() {
		DataSourceField[] dataSourceFields = {								
				new DataSourceTextField(DataNameTokens.VENMERCHANTPRODUCT_PRODUCTID, "Product Id"),
				new DataSourceTextField(DataNameTokens.VENMERCHANTPRODUCT_WCSPRODUCTSKU, "SKU"),
				new DataSourceTextField(DataNameTokens.VENMERCHANTPRODUCT_WCSPRODUCTNAME, "Product Name"),
				new DataSourceDateField(DataNameTokens.SEAT_ORDER_ETD_NEW, "New Tgl Pengiriman"),
				new DataSourceDateField(DataNameTokens.SEAT_ORDER_ETD_START, "Start Date"),
				new DataSourceDateField(DataNameTokens.SEAT_ORDER_ETD_END, "End date")
		};
		dataSourceFields[0].setPrimaryKey(true);
		
		RafDataSource dataSource = new RafDataSource(
				"/response/data/*",
				GWT.getHostPageBaseURL() + "SeatETDPresenterServlet?method=fetchSkuData&type=DataSource",
				null,
				GWT.getHostPageBaseURL() + "SeatETDPresenterServlet?method=updateSkuData&type=DataSource",
				null,
				dataSourceFields); 
				
		return dataSource;
				
	}
}