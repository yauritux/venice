/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.data;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.CurrencyManagementPresenter;
import com.gdn.venice.client.data.RafDataSource;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;

/**
 *
 * @author Maria Olivia
 */
public class CurrencyData {

	public static RafDataSource getAllCurrencyData(String username, int page, int limit) {
		System.out.println(username+" "+page+" "+limit);
		String fetchUrl = GWT.getHostPageBaseURL() + CurrencyManagementPresenter.currencyManagementPresenterServlet 
				+ "?method=fetchCurrencyData&type=DataSource&username="+username+"&limit="+limit+"&page="+page;
		System.out.println(fetchUrl);
		DataSourceField[] dataSourceFields = {
				new DataSourceTextField(DataNameTokens.INV_CURRENCY_ID, "Currency ID"),
				new DataSourceTextField(DataNameTokens.INV_CURRENCY_CURRENCY, "Currency"),
				new DataSourceTextField(DataNameTokens.INV_CURRENCY_RATE, "Value")
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
