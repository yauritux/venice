package com.gdn.venice.client.app.finance.presenter;

import java.util.LinkedHashMap;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.finance.data.FinanceData;
import com.gdn.venice.client.app.finance.view.handlers.FinSalesSettlementReportUiHandlers;
import com.gdn.venice.client.presenter.MainPagePresenter;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.client.DispatchAsync;
import com.gwtplatform.mvp.client.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.rpc.RPCCallback;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.rpc.RPCResponse;

/**
 * Presenter for SattlementReport Setup
 * <p>
 * <b>author:</b> <a href="mailto:christian.suwuh@pwsindonesia.com">Christian Suwuh</a>
 * <p>
 * <b>version:</b> 1.0
 * <p>
 * <b>since:</b> 2011
 */
public class FinSalesSettlementReportPresenter extends
		Presenter<FinSalesSettlementReportPresenter.MyView, FinSalesSettlementReportPresenter.MyProxy>
		implements FinSalesSettlementReportUiHandlers {

	public final static String finSalesSettlementReportPresenterServlet = "FinSalesSettlementReportPresenterServlet";
	private final DispatchAsync dispatcher;
	
	/**
	 * {@link FinSalesSettlementReportPresenter}'s view.
	 */
	public interface MyView extends View,
			HasUiHandlers<FinSalesSettlementReportUiHandlers> {
		void loadDataSalesSettlementRecord(LinkedHashMap<String, String> merchantParam);
	}
	
	/**
	 * {@link FinSalesSettlementReportPresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(NameTokens.financeSalesSettlementReport)
	public interface MyProxy extends Proxy<FinSalesSettlementReportPresenter>, Place {
		
	}
	
	/**
	 * Links the presenter to the view, proxy, event bus and dispatcher
	 * @param eventBus
	 * @param view
	 * @param proxy
	 * @param dispatcher
	 */
	@Inject
	public FinSalesSettlementReportPresenter(EventBus eventBus, MyView view, MyProxy proxy,
			DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());		
		onSetComboBoxMerchant();
		this.dispatcher = dispatcher;
	}
	
	/* (non-Javadoc)
	 * @see com.gwtplatform.mvp.client.Presenter#revealInParent()
	 */
	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);	
	}
	
	@Override
	public DataSource onGetSalesSettlementRecord() {
		DataSource dataSources = FinanceData.getGetSalesSettlementRecord();
		return dataSources;
	}
	
	@Override
	public void onSetComboBoxMerchant() {
		//Request Fraud Case Status Combo
		RPCRequest requestStatus = new RPCRequest();
		requestStatus.setActionURL(GWT.getHostPageBaseURL() + "FinSalesSettlementReportPresenterServlet?method=fetchMerchantComboBoxData&type=RPC");
		requestStatus.setHttpMethod("POST");
		requestStatus.setUseSimpleHttp(true);
		requestStatus.setShowPrompt(false);
		RPCManager.sendRequest(requestStatus, 
				new RPCCallback () {
					public void execute(RPCResponse response,
							Object rawData, RPCRequest request) {
						String rpcResponseStatus = rawData.toString();
						String xmlDataStatus = rpcResponseStatus;
						getView().loadDataSalesSettlementRecord(Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataStatus)));							
				}
		});
	}
}