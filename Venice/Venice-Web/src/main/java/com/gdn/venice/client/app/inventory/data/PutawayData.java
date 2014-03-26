package com.gdn.venice.client.app.inventory.data;

import java.util.HashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.PutawayCreatePresenter;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DSOperationType;

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
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_SHELFCODE, "Storage Code / Shelf Code"),
				new DataSourceTextField(DataNameTokens.INV_PUTAWAY_GRN_QTY, "Qty")};

		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + PutawayCreatePresenter.putawayManagementPresenterServlet + "?method=fetchPutawayGRNItemData&type=DataSource&limit="+limit+"&page="+page,
				null,
				null,
				null, 
				dataSourceFields);
		HashMap<String, String> params = new HashMap<String, String>();
		
		if(warehouseId != null) {
			params.put(DataNameTokens.INV_WAREHOUSE_ID, warehouseId);
			dataSource.getOperationBinding(DSOperationType.FETCH).setDefaultParams(params);
		}

		return dataSource;		 
	}	
}
