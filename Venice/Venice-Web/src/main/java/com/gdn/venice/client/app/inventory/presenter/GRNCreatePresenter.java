package com.gdn.venice.client.app.inventory.presenter;

import java.util.HashMap;

import com.gdn.venice.client.app.DataMessageTokens;
import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.data.GRNData;
import com.gdn.venice.client.app.inventory.view.GRNListView;
import com.gdn.venice.client.app.inventory.view.handler.GRNCreateUiHandler;
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
 * Presenter for GRN Create
 * 
 * @author Roland
 */
public class GRNCreatePresenter extends Presenter<GRNCreatePresenter.MyView, GRNCreatePresenter.MyProxy>
		implements GRNCreateUiHandler {
	
	GRNListView view;
	
	public static final String grnManagementPresenterServlet = "GRNManagementPresenterServlet";
	
	protected final DispatchAsync dispatcher;

	@ProxyCodeSplit
	@NameToken(NameTokens.grnCreatePage)
	public interface MyProxy extends Proxy<GRNCreatePresenter>, Place {
	}

	public interface MyView extends View, HasUiHandlers<GRNCreateUiHandler> {
		public void loadASNData(DataSource dataSource);
		public void refreshASNData();
		public Window getGrnCreateWindow();
	}

	@Inject
	public GRNCreatePresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
		
		((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
		getView().loadASNData(GRNData.getASNData(1, 20));
		this.dispatcher = dispatcher;
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
	}
	
	@Override
	public void onSaveClicked(HashMap<String, String> grnDataMap, HashMap<String, String> itemDataMap, final Window window) {
		RPCRequest request=new RPCRequest();
		
		String grnMap = Util.formXMLfromHashMap(grnDataMap);
		String itemMap = Util.formXMLfromHashMap(itemDataMap);
		
		request.setData(grnMap+"#"+itemMap);
		
		request.setActionURL(GWT.getHostPageBaseURL() + grnManagementPresenterServlet + "?method=saveGrnData&type=RPC");
		request.setHttpMethod("POST");
		request.setUseSimpleHttp(true);
		request.setWillHandleError(true);
		RPCManager.setPromptStyle(PromptStyle.DIALOG);
		RPCManager.setDefaultPrompt("Saving records...");
		RPCManager.setShowPrompt(true);
		
		RPCManager.sendRequest(request, new RPCCallback () {
					public void execute(RPCResponse response,
							Object rawData, RPCRequest request) {
						String rpcResponse = rawData.toString();
						
						if (rpcResponse.startsWith("0")) {
                            SC.say("GRN created");
                            getView().getGrnCreateWindow().destroy();
							getView().refreshASNData();
						} else {
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