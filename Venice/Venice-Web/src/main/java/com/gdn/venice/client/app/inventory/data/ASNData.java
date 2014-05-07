package com.gdn.venice.client.app.inventory.data;

import java.util.HashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.ASNListPresenter;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.DSOperationType;

/**
 * @author Roland
 */
public class ASNData {
		
	public static DataSource getASNData(int page, int limit) {
		DataSourceField[] dataSourceFields = {
				new DataSourceIntegerField(DataNameTokens.INV_ASN_ID, "ASN ID"),
				new DataSourceTextField(DataNameTokens.INV_ASN_NUMBER, "ASN No."),
				new DataSourceTextField(DataNameTokens.INV_ASN_REFF_NUMBER, "Reff No."),
				new DataSourceTextField(DataNameTokens.INV_ASN_CREATED_DATE, "Created Date"),
				new DataSourceTextField(DataNameTokens.INV_ASN_EST_DATE, "Estimation Date"),
				new DataSourceTextField(DataNameTokens.INV_ASN_REFF_DATE, "Reff Date"),
				new DataSourceTextField(DataNameTokens.INV_ASN_INVENTORY_TYPE, "Inventory Type"),
				new DataSourceTextField(DataNameTokens.INV_ASN_SUPPLIER_CODE, "Supplier Code"),
				new DataSourceTextField(DataNameTokens.INV_ASN_SUPPLIER_NAME, "Supplier Name"),
				new DataSourceTextField(DataNameTokens.INV_ASN_DESTINATION, "Destination"),
				new DataSourceTextField(DataNameTokens.INV_ASN_STATUS, "Status"),
				new DataSourceTextField(DataNameTokens.INV_ASN_SPECIAL_NOTES, "Special Notes")};
		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + ASNListPresenter.asnManagementPresenterServlet + "?method=fetchASNData&type=DataSource&limit="+limit+"&page="+page,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		  
	}
	
	public static DataSource getASNItemData(String asnId, int page, int limit) {
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.INV_ASN_ITEM_ID, "ASN Item ID"),
				new DataSourceTextField(DataNameTokens.INV_POCFF_ITEMCODE, "Warehouse SKU ID"),
				new DataSourceTextField(DataNameTokens.INV_POCFF_ITEMDESC, "Item Desc"),
				new DataSourceTextField(DataNameTokens.INV_POCFF_QTY, "Qty"),
				new DataSourceTextField(DataNameTokens.INV_POCFF_ITEMUNIT, "UoM"),
				new DataSourceTextField(DataNameTokens.INV_POCFF_ITEMLENGTH, "Length"),
				new DataSourceTextField(DataNameTokens.INV_POCFF_ITEMWIDTH, "Width"),
				new DataSourceTextField(DataNameTokens.INV_POCFF_ITEMHEIGHT, "Height"),
				new DataSourceTextField(DataNameTokens.INV_POCFF_VOLUME, "Volume"),
				new DataSourceTextField(DataNameTokens.INV_POCFF_ITEMWEIGHT, "Weight"),
				new DataSourceTextField(DataNameTokens.INV_POCFF_QTYGRN, "Qty GRN")};

		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + ASNListPresenter.asnManagementPresenterServlet + "?method=fetchASNItemData&type=DataSource&limit="+limit+"&page="+page,
				null,
				null,
				null, 
				dataSourceFields);
		HashMap<String, String> params = new HashMap<String, String>();
		
		if(asnId != null) {
			params.put(DataNameTokens.INV_ASN_ID, asnId);
			dataSource.getOperationBinding(DSOperationType.FETCH).setDefaultParams(params);
		}

		return dataSource;		 
	}		
}
