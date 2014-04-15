/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.data;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.PickerManagementPresenter;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;

/**
 *
 * @author Maria Olivia
 */
public class PickerData {

    public static RafDataSource getAllPickerData(int page, int limit) {
        String fetchUrl = GWT.getHostPageBaseURL() + PickerManagementPresenter.pickerManagementPresenterServlet
                + "?method=fetchPickerData&type=DataSource&limit=" + limit + "&page=" + page;
        System.out.println(fetchUrl);
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_PICKER_ID, "Picker ID"),
            new DataSourceTextField(DataNameTokens.INV_PICKER_CODE, "Picker Code"),
            new DataSourceTextField(DataNameTokens.INV_PICKER_NAME, "Picker Name"),
            new DataSourceTextField(DataNameTokens.INV_PICKER_WAREHOUSECODE, "Warehouse Code"),
            new DataSourceTextField(DataNameTokens.INV_PICKER_WAREHOUSENAME, "Warehouse"),
            new DataSourceTextField(DataNameTokens.INV_PICKER_STATUS, "Status")
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
