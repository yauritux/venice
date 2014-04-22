package com.gdn.venice.client.app.inventory.presenter;

import java.util.LinkedHashMap;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.view.handler.PickingListSOUiHandler;
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
import com.smartgwt.client.widgets.Window;

/**
 * Presenter for Picking List SO
 * 
 * @author Roland
 */
public class PickingListSOPresenter extends Presenter<PickingListSOPresenter.MyView, PickingListSOPresenter.MyProxy>
		implements PickingListSOUiHandler {
	
	public static final String pickingListManagementPresenterServlet = "PickingListManagementPresenterServlet";
	
	protected final DispatchAsync dispatcher;

	@ProxyCodeSplit
	@NameToken(NameTokens.pickingListSOPage)
	public interface MyProxy extends Proxy<PickingListSOPresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<PickingListSOUiHandler> {
		public void loadPickingListData(LinkedHashMap<String, String> warehouseMap);
		public void refreshPickingListSOData();
		public Window getPickingListDetailWindow();
		public Window getAssignPickerWindow();
	}

	@Inject
	public PickingListSOPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
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
		RPCRequest requestWarehouse = new RPCRequest();
		requestWarehouse.setActionURL(GWT.getHostPageBaseURL() + "WarehouseManagementPresenterServlet?method=fetchWarehouseComboBoxData&type=RPC&username="+MainPagePresenter.signedInUser);
		requestWarehouse.setHttpMethod("POST");
		requestWarehouse.setUseSimpleHttp(true);
		requestWarehouse.setShowPrompt(false);
		
		RPCManager.sendRequest(requestWarehouse, new RPCCallback () {
					public void execute(RPCResponse response, Object rawData, RPCRequest request) {
						String rpcResponseWarehouse = rawData.toString();
						String xmlDataWarehouse = rpcResponseWarehouse;
						final LinkedHashMap<String, String> warehouseMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlDataWarehouse));
						getView().loadPickingListData(warehouseMap);
					}
		});
	}
	
	@Override
	public void onSubmitClicked(String packageIds, String pickerId) {
		RPCRequest request=new RPCRequest();			
		request.setData(packageIds);
		
		request.setActionURL(GWT.getHostPageBaseURL() + pickingListManagementPresenterServlet + "?method=submitPickerData&type=RPC&pickerId="+pickerId);
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
							getView().refreshPickingListSOData();
							getView().getAssignPickerWindow().destroy();
						} else {
							SC.warn(rpcResponse);
						}
					}
		});		
	}
}