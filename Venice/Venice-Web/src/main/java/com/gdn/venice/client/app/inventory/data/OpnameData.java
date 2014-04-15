/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.data;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.OpnamePresenter;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Maria Olivia
 */
public class OpnameData {

    public static RafDataSource getAllItemStorageData(String warehouseCode,
            String stockType, String supplierCode) {
        String fetchUrl = GWT.getHostPageBaseURL() + OpnamePresenter.opnamePresenterServlet
                + "?method=fetchItemStorageData&type=DataSource&warehouseCode=" + warehouseCode + "&stockType=" + stockType + "&supplierCode=" + supplierCode;
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID, "Item Storage ID"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMSKU, "Warehouse SKU ID"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMNAME, "Item Name"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMCATEGORY, "Category"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMUOM, "UoM"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE, "Storage Code"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_SHELFCODE, "Shelf Code"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_QTY, "Qty")
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

    public static RafDataSource getSupplierData(String warehouseCode, String stockType) {
        String fetchUrl = GWT.getHostPageBaseURL() + OpnamePresenter.opnamePresenterServlet
                + "?method=fetchSupplierData&type=DataSource&warehouseCode=" + warehouseCode
                + "&stockType=" + stockType;
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_OPNAME_SUPPLIERCODE, "Supplier Code"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_SUPPLIERNAME, "Supplier Name")
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

    public static RafDataSource getItemStorageDataById(String ids) {
        String fetchUrl = GWT.getHostPageBaseURL() + OpnamePresenter.opnamePresenterServlet
                + "?method=fetchItemStorageDataById&type=DataSource";
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID, "Item Storage ID"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMSKU, "Warehouse SKU ID"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMNAME, "Item Name"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMCATEGORY, "Category"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMUOM, "UoM"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE, "Storage Code"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_SHELFCODE, "Shelf Code"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_QTY, "Qty")
        };
        dataSourceFields[0].setPrimaryKey(true);
        RafDataSource retVal = new RafDataSource(
                "/response/data/*",
                fetchUrl,
                null,
                null,
                null,
                dataSourceFields);
        Map<String, String> param = new HashMap<String, String>();
        param.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID, ids);
        retVal.fetchDataWithParam(param);
        return retVal;
    }

    public static RafDataSource getAllOpnameData(int page, int limit) {
        String fetchUrl = GWT.getHostPageBaseURL() + OpnamePresenter.opnamePresenterServlet
                + "?method=fetchAllOpnameData&type=DataSource&page=" + page + "&limit=" + limit;
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ID, "Opname ID"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_NO, "Stock Opname No"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_CREATEDDATE, "Created Date"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_CREATEDBY, "Prepared By"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_UPDATEDBY, "Last Modified By"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_UPDATEDDATE, "Modified Date"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_STOCKTYPE, "Stock Type"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_SUPPLIERCODE, "Supplier Code"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_WAREHOUSECODE, "Warehouse Code")
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

    public static RafDataSource getOpnameDetail(String opnameId) {
        String fetchUrl = GWT.getHostPageBaseURL() + OpnamePresenter.opnamePresenterServlet
                + "?method=fetchOpnameDetailByOpnameId&type=DataSource&opnameId=" + opnameId;
        String updateUrl = GWT.getHostPageBaseURL() + OpnamePresenter.opnamePresenterServlet
                + "?method=updateOpnameDetail&type=DataSource";
        String addUrl = GWT.getHostPageBaseURL() + OpnamePresenter.opnamePresenterServlet
                + "?method=addOpnameDetail&type=DataSource";
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID, "Item Storage ID"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMSKU, "Warehouse SKU ID"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMNAME, "Item Name"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMCATEGORY, "Category"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMUOM, "UoM"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE, "Storage Code"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_SHELFCODE, "Shelf Code"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_QTY, "Qty"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_NEWQTY, "New Qty"),
            new DataSourceTextField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_NOTE, "Note")
        };
        dataSourceFields[0].setPrimaryKey(true);
        RafDataSource retVal = new RafDataSource(
                "/response/data/*",
                fetchUrl,
                addUrl,
                updateUrl,
                null,
                dataSourceFields);

        return retVal;
    }
}
