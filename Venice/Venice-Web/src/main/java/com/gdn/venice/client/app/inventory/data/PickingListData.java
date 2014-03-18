package com.gdn.venice.client.app.inventory.data;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.PickingListPresenter;
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
		
	public static DataSource getPickingListData(String warehouseId, int page, int limit) {
		DataSourceField[] dataSourceFields = {
				new DataSourceIntegerField(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMID, "Warehouse Item ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMSKU, "Warehouse Item SKU"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_ITEMSKUNAME, "Item SKU Name"),				
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_STOCKTYPE, "Type"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_MERCHANT, "Supplier"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_QTY, "Qty"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_QTYPICKED, "Qty Picked")};
		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PickingListPresenter.pickingListManagementPresenterServlet + 
				"?method=fetchPickingListData&type=DataSource&warehouseId="+warehouseId+"&limit="+limit+"&page="+page,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		  
	}
	
	public static DataSource getPickingListItemDetailData(String warehouseItemId) {
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMID, "Warehouse Item ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMSKU, "Warehouse Item SKU"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_ITEMSKUNAME, "Item SKU Name"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_STOCKTYPE, "Type"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_MERCHANT, "Merchant"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_DIMENSION, "W x H x L"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_WEIGHT, "Weight"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_UOM, "UoM"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_ATTRIBUTE, "Reff Attribute")};

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PickingListPresenter.pickingListManagementPresenterServlet + "?method=fetchPickingListItemDetailData&type=DataSource&warehouseItemId="+warehouseItemId,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		 
	}
	
	public static DataSource getSalesOrderListData(String warehouseItemId) {
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_SALESORDERID, "Sales Order ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_SALESORDERNUMBER, "Sales Order Number"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_SALESORDERQTY, "Sales Order Qty")};

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PickingListPresenter.pickingListManagementPresenterServlet + "?method=fetchPickingListSalesOrderDetailData&type=DataSource&warehouseItemId="+warehouseItemId,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		 
	}
	
	public static DataSource getStorageListData(String warehouseItemId) {
		System.out.println("warehouseItemId: "+warehouseItemId);
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_WAREHOUSESTORAGEID, "Warehouse Storage ID"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_SHELFCODE, "Shelf Code"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_QTY, "Qty"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_QTYPICKED, "Qty Picked")};

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PickingListPresenter.pickingListManagementPresenterServlet + "?method=fetchPickingListStorageDetailData&type=DataSource&warehouseItemId="+warehouseItemId,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		 
	}
}
