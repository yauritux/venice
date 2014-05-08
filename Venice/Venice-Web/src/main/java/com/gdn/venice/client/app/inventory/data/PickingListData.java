package com.gdn.venice.client.app.inventory.data;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.PickingListIRPresenter;
import com.gdn.venice.client.app.inventory.presenter.PickingListSOPresenter;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;

/**
 * @author Roland
 */
public class PickingListData {	
	public static DataSource getPickingListIRData(String warehouseId, int page, int limit) {
		DataSourceField[] dataSourceFields = {
				new DataSourceIntegerField(DataNameTokens.INV_PICKINGLISTIR_PACKAGEID, "Package ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_DETAIL, "Detail"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_PACKAGECODE, "Package Code"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_INVENTORYTYPE, "Inventory Type"),				
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_IRTYPE, "IR Type"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_PICKERID, "Picker ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_PICKERNAME, "Picker Name")};
		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PickingListIRPresenter.pickingListManagementPresenterServlet + 
				"?method=fetchPickingListIRData&type=DataSource&warehouseId="+warehouseId+"&limit="+limit+"&page="+page,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		  
	}
	
	public static DataSource getPickingListIRDetailData(String packageId) {
		DataSourceField[] dataSourceFields = {				
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_INVENTORYREQUESTCODE, "Inventory Request ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_WAREHOUSESKUID, "Warehouse SKU ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_WAREHOUSESKUNAME, "Item Name"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_QTY, "Qty"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_STORAGECODE, "Storage Code"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTIR_QTYSTORAGE, "Qty Storage")};
		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PickingListIRPresenter.pickingListManagementPresenterServlet + 
				"?method=fetchPickingListIRDetailData&type=DataSource&packageId="+packageId,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		  
	}
	
	public static DataSource getPickingListSOData(String warehouseId, int page, int limit) {
		DataSourceField[] dataSourceFields = {
				new DataSourceIntegerField(DataNameTokens.INV_PICKINGLISTSO_PACKAGEID, "Package ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTSO_DETAIL, "Detail"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTSO_PACKAGECODE, "Package Code"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTSO_MERCHANTNAME, "Merchant Store"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTSO_PICKERID, "Picker ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTSO_PICKERNAME, "Picker Name")};
		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PickingListSOPresenter.pickingListManagementPresenterServlet + 
				"?method=fetchPickingListSOData&type=DataSource&warehouseId="+warehouseId+"&limit="+limit+"&page="+page,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		  
	}
	
	public static DataSource getPickingListSODetailData(String packageId) {
		DataSourceField[] dataSourceFields = {				
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTSO_SALESORDERCODE, "Sales Order ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTSO_WAREHOUSESKUID, "Warehouse SKU ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTSO_WAREHOUSESKUNAME, "Item Name"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTSO_QTY, "Qty"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTSO_STORAGECODE, "Storage Code"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLISTSO_QTYSTORAGE, "Qty Storage")};
		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PickingListSOPresenter.pickingListManagementPresenterServlet + 
				"?method=fetchPickingListSODetailData&type=DataSource&packageId="+packageId,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		  
	}
}
