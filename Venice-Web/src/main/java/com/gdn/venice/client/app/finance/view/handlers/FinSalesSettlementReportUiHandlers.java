package com.gdn.venice.client.app.finance.view.handlers;

import com.gwtplatform.mvp.client.UiHandlers;
import com.smartgwt.client.data.DataSource;

public interface FinSalesSettlementReportUiHandlers extends UiHandlers {
	DataSource onGetSalesSettlementRecord();
	void onSetComboBoxMerchant() ;
}
