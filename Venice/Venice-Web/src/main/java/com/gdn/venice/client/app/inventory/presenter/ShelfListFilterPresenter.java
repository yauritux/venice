package com.gdn.venice.client.app.inventory.presenter;

import java.util.HashMap;

import com.gdn.venice.client.app.DataMessageTokens;
import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.data.ShelfData;
import com.gdn.venice.client.app.inventory.view.ShelfListFilterView;
import com.gdn.venice.client.app.inventory.view.handler.ShelfListFilterUiHandler;
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
import com.smartgwt.client.types.PromptStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Window;

/**
 * Presenter for Shelf List Filter
 * 
 * @author Roland
 */
public class ShelfListFilterPresenter extends Presenter<ShelfListFilterPresenter.MyView, ShelfListFilterPresenter.MyProxy>
		implements ShelfListFilterUiHandler {
	
	ShelfListFilterView providerView;
	
	public static final String shelfManagementPresenterServlet = "ShelfManagementPresenterServlet";
	
	protected final DispatchAsync dispatcher;

	@ProxyCodeSplit
	@NameToken(NameTokens.shelfListFilterPage)
	public interface MyProxy extends Proxy<ShelfListFilterPresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<ShelfListFilterUiHandler> {
		public void loadShelfData(DataSource dataSource);
		public void refreshShelfData();
	}

	@Inject
	public ShelfListFilterPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		
		((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
		getView().loadShelfData(ShelfData.getShelfData(1, 20));
		this.dispatcher = dispatcher;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
	}

	@Override
	public void onSaveShelfClicked(HashMap<String, String> shelfDataMap, HashMap<String, String> storageDataMap, final Window window) {
		RPCRequest request=new RPCRequest();
		
		String shelfMap = Util.formXMLfromHashMap(shelfDataMap);
		String storageMap = Util.formXMLfromHashMap(storageDataMap);
		
		request.setData(shelfMap+"#"+storageMap);
		
		request.setActionURL(GWT.getHostPageBaseURL() + shelfManagementPresenterServlet + "?method=saveShelfData&type=RPC");
		request.setHttpMethod("POST");
		request.setUseSimpleHttp(true);
		request.setWillHandleError(true);
		RPCManager.setPromptStyle(PromptStyle.DIALOG);
		RPCManager.setDefaultPrompt("Saving records...");
		RPCManager.setShowPrompt(true);
		
		RPCManager.sendRequest(request, 
				new RPCCallback () {
					public void execute(RPCResponse response,
							Object rawData, RPCRequest request) {
						String rpcResponse = rawData.toString();
						
						if (rpcResponse.startsWith("0")) {
                            SC.say("Shelf added/updated and need approval for changes to take place");
                            window.destroy();
							getView().refreshShelfData();
						} else {
							/*
							 * Use the 2nd positional split on ":" as the error message
							 */
							String[] split = rpcResponse.split(":");
							if(split.length>1){
								SC.warn(split[1]);
							}else {
								SC.warn(rpcResponse);
							}
						}
					}
		});		
	}
	
	@Override
	public void onNonActiveShelfClicked(HashMap<String, String> shelfDataMap, final Window window) {
		RPCRequest request=new RPCRequest();
		
		String shelfMap = Util.formXMLfromHashMap(shelfDataMap);
		
		request.setData(shelfMap);
		
		request.setActionURL(GWT.getHostPageBaseURL() + shelfManagementPresenterServlet + "?method=saveUpdateStatusShelf&type=RPC");
		request.setHttpMethod("POST");
		request.setUseSimpleHttp(true);
		request.setWillHandleError(true);
		RPCManager.setPromptStyle(PromptStyle.DIALOG);
		RPCManager.setDefaultPrompt("Saving records...");
		RPCManager.setShowPrompt(true);
		
		RPCManager.sendRequest(request, 
				new RPCCallback () {
					public void execute(RPCResponse response, Object rawData, RPCRequest request) {
						String rpcResponse = rawData.toString();
						
						if (rpcResponse.startsWith("0")) {
                            SC.say("Shelf added/updated and need approval for changes to take place");
                            window.destroy();
							getView().refreshShelfData();
						} else {
							/*
							 * Use the 2nd positional split on ":" as the error message
							 */
							String[] split = rpcResponse.split(":");
							if(split.length>1){
								SC.warn(split[1]);
							}else{
								SC.warn(DataMessageTokens.GENERAL_ERROR_MESSAGE);
							}
						}
					}
		});		
	}
}