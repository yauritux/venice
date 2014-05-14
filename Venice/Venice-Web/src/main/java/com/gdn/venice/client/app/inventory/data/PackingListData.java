/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.data;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.PackingListPresenter;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;

/**
 *
 * @author Maria Olivia
 */
public class PackingListData {
    public static RafDataSource getAllPackingData(String warehouseId, int page, int limit) {
        String fetchUrl = GWT.getHostPageBaseURL() + PackingListPresenter.packingListPresenterServlet
                + "?method=fetchPackingData&type=DataSource&warehouseId=" + warehouseId + "&limit=" + limit + "&page=" + page;
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_PACKING_PICKPACKAGE_ID, "Packing ID"),
            new DataSourceTextField(DataNameTokens.INV_PACKING_PICKPACKAGE_CODE, "Code"),
            new DataSourceTextField(DataNameTokens.INV_PACKING_PICKPACKAGE_CONTAINERID, "Container ID"),
            new DataSourceTextField(DataNameTokens.INV_PACKING_PICKPACKAGE_HANDLING, "Handling"),
            new DataSourceTextField(DataNameTokens.INV_PACKING_PICKPACKAGE_PICKER, "Picker"),
            new DataSourceTextField(DataNameTokens.INV_PACKING_AWB_NUMBER, "Airway Bill Number"),
            new DataSourceTextField(DataNameTokens.INV_PACKING_AWB_LOGISTIC, "Logistic"),
            new DataSourceTextField(DataNameTokens.INV_PACKING_PICKPACKAGE_STATUS, "Status"),
            new DataSourceTextField(DataNameTokens.INV_PACKING_PICKPACKAGE_CLAIMEDBY, "Claimed By")
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

    public static RafDataSource getAllSalesData(String pickPackageId, String username) {
        String fetchUrl = GWT.getHostPageBaseURL() + PackingListPresenter.packingListPresenterServlet
                + "?method=fetchSalesData&type=DataSource&pickPackageId=" + pickPackageId + "&username=" + username;
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_SO_ID, "SO ID"),
            new DataSourceTextField(DataNameTokens.INV_SO_ORDERID, "Order ID"),
            new DataSourceTextField(DataNameTokens.INV_SO_ORDERITEMID, "Order Item ID"),
            new DataSourceTextField(DataNameTokens.INV_SO_MERCHANTSKU, "Warehouse SKU ID"),
            new DataSourceTextField(DataNameTokens.INV_SO_ITEMDESC, "Item Description"),
            new DataSourceTextField(DataNameTokens.INV_SO_QUANTITY, "Qty"),
            new DataSourceTextField(DataNameTokens.INV_SO_ITEMUOM, "UoM"),
            new DataSourceTextField(DataNameTokens.INV_SO_ITEMPHOTO, "Item Photo"),
            new DataSourceTextField(DataNameTokens.INV_SO_ITEMHASATTRIBUTE, "Has Attribute"),
            new DataSourceTextField(DataNameTokens.INV_SO_ATTRIBUTE, "Attributes"),
            new DataSourceTextField(DataNameTokens.INV_AWB_CLAIMEDBY, "Claimed By"),
            new DataSourceTextField(DataNameTokens.INV_SO_ITEMID, "Item ID")
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
}
