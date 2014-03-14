package com.gdn.venice.client.app.inventory.data;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.PickingListPresenter;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceDateField;
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
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_STOCKTYPE, "Type"),
				new DataSourceTextField(DataNameTokens.INV_PICKINGLIST_MERCHANT, "Supplier"),
				new DataSourceDateField(DataNameTokens.INV_PICKINGLIST_QTY, "Qty"),
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
}
