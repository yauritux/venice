package com.gdn.venice.client.app.inventory.data;

import java.util.HashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.ShelfListFilterPresenter;
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
public class ShelfData {
		
	public static DataSource getShelfData(int page, int limit) {
		DataSourceField[] dataSourceFields = {
				new DataSourceIntegerField(DataNameTokens.INV_SHELF_ID, "Shelf ID"),
				new DataSourceTextField(DataNameTokens.INV_SHELF_CODE, "Shelf Code"),
				new DataSourceTextField(DataNameTokens.INV_SHELF_DESCRIPTION, "Shelf Description"),
				new DataSourceTextField(DataNameTokens.INV_SHELF_ACTIVESTATUS, "Active/Non Active"),
				new DataSourceTextField(DataNameTokens.INV_SHELF_APPROVAL_IN_PROCESS, "Approval In Process")};
		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + ShelfListFilterPresenter.shelfManagementPresenterServlet + "?method=fetchShelfData&type=DataSource&limit="+limit+"&page="+page,
				null,
				null,
				null, 
				dataSourceFields);

		return dataSource;		  
	}
	
	public static DataSource getStorageData(String shelfId, int page, int limit) {
		System.out.println("shelfId: "+shelfId);
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.INV_STORAGE_ID, "Storage Bin ID"),
				new DataSourceTextField(DataNameTokens.INV_STORAGE_CODE, "Storage Bin Code"),
				new DataSourceTextField(DataNameTokens.INV_STORAGE_DESCRIPTION, "Storage Bin Description"),
				new DataSourceTextField(DataNameTokens.INV_STORAGE_TYPE, "Storage Bin Type")};
		dataSourceFields[0].setPrimaryKey(true);

		RafDataSource dataSource = new RafDataSource("/response/data/*",
				GWT.getHostPageBaseURL() + ShelfListFilterPresenter.shelfManagementPresenterServlet + "?method=fetchStorageData&type=DataSource&limit="+limit+"&page="+page,
				null,
				null,
				null, 
				dataSourceFields);
		HashMap<String, String> params = new HashMap<String, String>();
		
		if(shelfId != null) {
			params.put(DataNameTokens.INV_SHELF_ID, shelfId);
			dataSource.getOperationBinding(DSOperationType.FETCH).setDefaultParams(params);
		}

		return dataSource;		 
	}
	
	public static RafDataSource getAllShelfInProcessData(String username, int page, int limit, String process) {
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.INV_SHELF_ID, "Shelf ID"),
				new DataSourceTextField(DataNameTokens.INV_SHELF_CODE, "Shelf Code"),     
				new DataSourceTextField(DataNameTokens.INV_SHELF_DESCRIPTION, "Shelf Description"),
				new DataSourceTextField(DataNameTokens.INV_SHELF_APPROVALSTATUS, "Approval Status"),
				new DataSourceTextField(DataNameTokens.INV_SHELF_ORIGINID, "Original Shelf")
		};

		dataSourceFields[0].setPrimaryKey(true);
		RafDataSource retVal = new RafDataSource(
				"/response/data/*",
				GWT.getHostPageBaseURL() + ShelfListFilterPresenter.shelfManagementPresenterServlet 
				+ "?method=fetchShelfInProcess"+process+"Data&type=DataSource&username="+username+"&limit="+limit+"&page="+page,
				null,
				null,
				null,
				dataSourceFields);

		return retVal;
	}
}
