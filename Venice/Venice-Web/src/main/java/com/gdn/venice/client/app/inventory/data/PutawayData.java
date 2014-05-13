package com.gdn.venice.client.app.inventory.data;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.PutawayCreatePresenter;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;

/**
 * @author Roland
 */
public class PutawayData {
	
	public static DataSource getGRNItemData(String warehouseId, int page, int limit) {
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_ITEMID, "GRN Item ID"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_GRNNUMBER, "Reff No"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_ITEMCODE, "Warehouse SKU ID"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_ITEMDESC, "Item Desc"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_STORAGECODE, "Storage Code / Qty")};

		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PutawayCreatePresenter.putawayManagementPresenterServlet + "?method=fetchPutawayGRNItemData&type=DataSource&limit="+limit+"&page="+page+"&warehouseId="+warehouseId,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		 
	}
	
	public static DataSource getPutawayData(String warehouseId, int page, int limit) {
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_ID, "Putaway ID"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_NUMBER, "Putaway Number"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_CREATEDDATE, "Created Date"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_TYPE, "Putaway Type"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_STATUS, "Putaway Status"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_ID, "GRN ID")};

		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PutawayCreatePresenter.putawayManagementPresenterServlet + "?method=fetchPutawayData&type=DataSource&limit="+limit+"&page="+page+"&warehouseId="+warehouseId,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		 
	}	
	
	public static DataSource getPutawayDetailGRNItemData(String grnId, int page, int limit) {
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_ITEMID, "GRN Item ID"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_ITEMCODE, "Warehouse SKU ID"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_ITEMDESC, "Item Desc"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_STORAGECODE, "Storage Code"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_QTY, "Qty"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_WAREHOUSEITEMID, "Warehouse Item ID")};

		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PutawayCreatePresenter.putawayManagementPresenterServlet + "?method=fetchPutawayDetailGRNItemData&type=DataSource&limit="+limit+"&page="+page+"&grnId="+grnId,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		 
	}
}
