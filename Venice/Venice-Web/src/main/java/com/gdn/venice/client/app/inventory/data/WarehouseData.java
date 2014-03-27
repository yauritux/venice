/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.data;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.WarehouseListFilterPresenter;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;

/**
 *
 * @author Maria Olivia
 */
public class WarehouseData {

    public static RafDataSource getAllWarehouseData(String username, int page, int limit) {
        System.out.println(username + " " + page + " " + limit);
        String fetchUrl = GWT.getHostPageBaseURL() + WarehouseListFilterPresenter.warehouseManagementPresenterServlet
                + "?method=fetchWarehouseData&type=DataSource&username=" + username + "&limit=" + limit + "&page=" + page;
        System.out.println(fetchUrl);
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_ID, "Warehouse ID"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_CODE, "Warehouse Code"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_NAME, "Warehouse Name"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_DESCRIPTION, "Warehouse Description"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_ADDRESS, "Warehouse Address"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_CITY, "Warehouse City"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_ZIPCODE, "Warehouse Zipcode"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_CONTACT_PERSON, "Contact Person"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_CONTACT_PHONE, "Contact Phone"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_SPACE, "Space"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_AVAILABLE_SPACE, "Available Space"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_ACTIVESTATUS, "Active/Non Active"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_APPROVAL_IN_PROCESS, "Approval In Process")
        };
        dataSourceFields[0].setPrimaryKey(true);
        RafDataSource retVal = new RafDataSource(
                "/response/data/*",
                fetchUrl,
                null,
                null,
                null,
                dataSourceFields);

        return retVal;
    }

    public static RafDataSource getAllWarehouseInProcessData(String username, int page, int limit, String process) {
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_ID, "Warehouse ID"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_CODE, "Warehouse Code"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_NAME, "Warehouse Name"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_DESCRIPTION, "Warehouse Description"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_ADDRESS, "Warehouse Address"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_CITY, "Warehouse City"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_ZIPCODE, "Warehouse Zipcode"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_CONTACT_PERSON, "Contact Person"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_CONTACT_PHONE, "Contact Phone"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_SPACE, "Space"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_AVAILABLE_SPACE, "Available Space"),
            new DataSourceTextField(DataNameTokens.INV_WAREHOUSE_APPROVALSTATUS, "Approval Status")
        };

        dataSourceFields[0].setPrimaryKey(true);
        RafDataSource retVal = new RafDataSource(
                "/response/data/*",
                GWT.getHostPageBaseURL() + WarehouseListFilterPresenter.warehouseManagementPresenterServlet
                + "?method=fetchWarehouseInProcess" + process + "Data&type=DataSource&username=" + username + "&limit=" + limit + "&page=" + page,
                null,
                null,
                null,
                dataSourceFields);

        return retVal;
    }
}
