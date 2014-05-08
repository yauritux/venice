package com.gdn.venice.client.app.seattle.presenter;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.seattle.data.SeattleData;
import com.gdn.venice.client.app.seattle.view.hendlers.SeatSLAFulfillmentUiHandlers;
import com.gdn.venice.client.presenter.MainPagePresenter;
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
 * Presenter for SeaSLAFulfillmentReportPresenter
 * 
 * @author Arifin
 */
public class SeatSLAFulfillmentReportPresenter extends Presenter<SeatSLAFulfillmentReportPresenter.MyView, SeatSLAFulfillmentReportPresenter.MyProxy>implements SeatSLAFulfillmentUiHandlers {
	private final DispatchAsync dispatcher;
	public final static String seaSLAFulfillmentPresenterServlet = "SeaSLAFulfillmentPresenterServlet";

	/**
	 * {@link SeatSLAFulfillmentReportPresenter}'s proxy.
	 */
	@ProxyCodeSplit
	@NameToken(NameTokens.seattleSLAFulfillmentViewer)
	public interface MyProxy extends Proxy<SeatSLAFulfillmentReportPresenter>, Place {
	}

	/**
	 * {@link SeatSLAFulfillmentReportPresenter}'s view.
	 */
	public interface MyView extends View, HasUiHandlers<SeatSLAFulfillmentUiHandlers> {
		void loadData(DataSource dataSource);
		
	}

	@Inject
	public SeatSLAFulfillmentReportPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
		onGetUserRoleData();
		this.dispatcher = dispatcher;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea,
				this);
	}
	
	@Override
	public void onGetUserRoleData() {	
		RPCRequest request=new RPCRequest();				
		request.setActionURL(GWT.getHostPageBaseURL() + "ToDoListPresenterServlet?method=getUserRole&type=RPC");
		request.setHttpMethod("POST");
		request.setUseSimpleHttp(true);
		
		RPCManager.sendRequest(request, new RPCCallback () {
					public void execute(RPCResponse response, Object rawData, RPCRequest request) {
						String userRole = rawData.toString().trim();
						getView().loadData(SeattleData.getSLAFulfillment(userRole));
					}
    	});  
	}


	


}