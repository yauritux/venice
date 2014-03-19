package com.gdn.venice.client.app.inventory.presenter;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.view.GRNListView;
import com.gdn.venice.client.app.inventory.view.handler.PickingListUiHandler;
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
 * Presenter for Picking List
 * 
 * @author Roland
 */
public class PickingListPresenter extends Presenter<PickingListPresenter.MyView, PickingListPresenter.MyProxy>
		implements PickingListUiHandler {
	
	GRNListView view;
	
	public static final String pickingListManagementPresenterServlet = "PickingListManagementPresenterServlet";
	
	protected final DispatchAsync dispatcher;

	@ProxyCodeSplit
	@NameToken(NameTokens.pickingListPage)
	public interface MyProxy extends Proxy<PickingListPresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<PickingListUiHandler> {
		public void loadPickingListData(LinkedHashMap<String, String> warehouseMap);
		public void refreshPickingListData();
		public Window getPickingListDetailWindow();
		public int resetTotalQtyPicked();
	}

	@Inject
	public PickingListPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
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
						getView().loadPickingListData(warehouseMap);
				}
		});
	}
	
	@Override
	public void releaseLock(String warehouseId) {	
		RPCRequest request=new RPCRequest();
		request = new RPCRequest();
		request.setActionURL(GWT.getHostPageBaseURL() + "PickingListManagementPresenterServlet?method=releaseLock&type=RPC&username="+MainPagePresenter.signedInUser+"&warehouseId="+warehouseId);
		request.setHttpMethod("POST");
		request.setUseSimpleHttp(true);
		request.setShowPrompt(false);
		RPCManager.sendRequest(request, 
				new RPCCallback () {
					public void execute(RPCResponse response,
							Object rawData, RPCRequest request) {
						String rpcResponse = rawData.toString();
						if (!rpcResponse.startsWith("0")) {
                            SC.warn(rpcResponse);
                        }
				}
		});
	}
	
	@Override
	public void onSaveClicked(HashMap<String, String> itemDataMap, HashMap<String, String> salesDataMap, HashMap<String, String> storageDataMap
			, int totalQtyPicked) {
		RPCRequest request=new RPCRequest();
		
		String itemMap = Util.formXMLfromHashMap(itemDataMap);
		String salesMap = Util.formXMLfromHashMap(salesDataMap);
		String storageMap = Util.formXMLfromHashMap(storageDataMap);
		
		request.setData(itemMap+"#"+salesMap+"#"+storageMap+"#"+totalQtyPicked);
		
		request.setActionURL(GWT.getHostPageBaseURL() + pickingListManagementPresenterServlet + "?method=savePickingListData&type=RPC");
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
							getView().refreshPickingListData();
							getView().getPickingListDetailWindow().destroy();
							getView().resetTotalQtyPicked();
						} else {
							SC.warn(rpcResponse);
						}
					}
		});		
	}
}