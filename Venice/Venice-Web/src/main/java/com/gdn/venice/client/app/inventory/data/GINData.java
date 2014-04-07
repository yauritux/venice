/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.data;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.GoodIssuedNotePresenter;
import com.gdn.venice.client.app.inventory.presenter.PackingListPresenter;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;

/**
 *
 * @author Maria Olivia
 */
public class GINData {

    public static RafDataSource getAllGINData(String warehouseCode, int page, int limit) {
        String fetchUrl = GWT.getHostPageBaseURL() + GoodIssuedNotePresenter.ginPresenterServlet
                + "?method=fetchGinData&type=DataSource&warehouseCode=" + warehouseCode + "&limit=" + limit + "&page=" + page;
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_GIN_ID, "GIN ID"),
            new DataSourceTextField(DataNameTokens.INV_GIN_NO, "GIN NO"),
            new DataSourceTextField(DataNameTokens.INV_GIN_DATE, "GIN DATE"),
            new DataSourceTextField(DataNameTokens.INV_GIN_LOGISTIC, "LOGISTIC"),
            new DataSourceTextField(DataNameTokens.INV_GIN_NOTE, "GIN NOTE")
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
    
    public static RafDataSource getAwbListData(String ginId) {
        String fetchUrl = GWT.getHostPageBaseURL() + GoodIssuedNotePresenter.ginPresenterServlet
                + "?method=fetchAwbListData&type=DataSource&ginId=" + ginId;
        DataSourceField[] dataSourceFields = {
            new DataSourceTextField(DataNameTokens.INV_GIN_AWB_NO, "AWB NO")
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
