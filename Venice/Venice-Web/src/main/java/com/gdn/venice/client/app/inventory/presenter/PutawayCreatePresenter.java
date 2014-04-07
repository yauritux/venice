package com.gdn.venice.client.app.inventory.presenter;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.view.PutawayCreateView;
import com.gdn.venice.client.app.inventory.view.handler.PutawayCreateUiHandler;
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
import com.smartgwt.client.rpc.RPCCallback;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.PromptStyle;
import com.smartgwt.client.util.SC;

/**
 * Presenter for create putaway
 * 
 * @author Roland
 */
public class PutawayCreatePresenter extends Presenter<PutawayCreatePresenter.MyView, PutawayCreatePresenter.MyProxy>
		implements PutawayCreateUiHandler {
	
	PutawayCreateView view;
	
	public static final String putawayManagementPresenterServlet = "PutawayManagementPresenterServlet";
	
	protected final DispatchAsync dispatcher;

	@ProxyCodeSplit
	@NameToken(NameTokens.putawayCreatePage)
	public interface MyProxy extends Proxy<PutawayCreatePresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<PutawayCreateUiHandler> {
		public void loadPutawayData(LinkedHashMap<String, String> warehouseMap);
		public void refreshPutawayData();
	}

	@Inject
	public PutawayCreatePresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		
		((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
		onFetchWarehouseComboBoxData();
		this.dispatcher = dispatcher;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
	}
	
	@Override
	public void onFetchWarehouseComboBoxData() {	
		RPCRequest request=new RPCRequest();
		request = new RPCRequest();
		request.setActionURL(GWT.getHostPageBaseURL() + "WarehouseManagementPresenterServlet?method=fetchWarehouseComboBoxData&type=RPC&username="+MainPagePresenter.signedInUser);
		request.setHttpMethod("POST");
		request.setUseSimpleHttp(true);
		request.setShowPrompt(false);
		RPCManager.sendRequest(request, 
				new RPCCallback () {
					public void execute(RPCResponse response,
							Object rawData, RPCRequest request) {
						String rpcResponse = rawData.toString();
						String xmlData = rpcResponse;
						final LinkedHashMap<String, String> warehouseMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlData));
						getView().loadPutawayData(warehouseMap);
				}
		});
	}
	
	@Override
	public void onSubmitClicked(HashMap<String, String> itemDataMap) {
		RPCRequest request=new RPCRequest();		
		String itemMap = Util.formXMLfromHashMap(itemDataMap);		
		request.setData(itemMap);
		
		request.setActionURL(GWT.getHostPageBaseURL() + putawayManagementPresenterServlet + "?method=submitPutawayData&type=RPC");
		request.setHttpMethod("POST");
		request.setUseSimpleHttp(true);
		request.setWillHandleError(true);
		RPCManager.setPromptStyle(PromptStyle.DIALOG);
		RPCManager.setDefaultPrompt("Saving records...");
		RPCManager.setShowPrompt(true);
		
		RPCManager.sendRequest(request, new RPCCallback () {
					public void execute(RPCResponse response, Object rawData, RPCRequest request) {
						String rpcResponse = rawData.toString();
						
						if (rpcResponse.startsWith("0")) {
                            SC.say("Data submitted");
							getView().refreshPutawayData();
						} else {
							SC.warn(rpcResponse);
						}
					}
		});		
	}
}