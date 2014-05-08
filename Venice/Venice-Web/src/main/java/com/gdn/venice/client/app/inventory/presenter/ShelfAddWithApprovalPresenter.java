package com.gdn.venice.client.app.inventory.presenter;

import java.util.HashMap;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.data.ShelfData;
import com.gdn.venice.client.app.inventory.view.handler.ShelfAddWithApprovalUiHandler;
import com.gdn.venice.client.app.logistic.presenter.DeliveryStatusTrackingPresenter;
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
 *
 * @author Roland
 */
public class ShelfAddWithApprovalPresenter extends Presenter<ShelfAddWithApprovalPresenter.MyView, ShelfAddWithApprovalPresenter.MyProxy>
        implements ShelfAddWithApprovalUiHandler {

    @SuppressWarnings("unused")
    private final DispatchAsync dispatcher;
    public final static String shelfManagementPresenterServlet = "ShelfManagementPresenterServlet";

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
    }

    /**
     * {@link DeliveryStatusTrackingPresenter}'s proxy.
     */
    @ProxyCodeSplit
    @NameToken(NameTokens.shelfAddApprovalPage)
    public interface MyProxy extends Proxy<ShelfAddWithApprovalPresenter>, Place {
    }

    /**
     * {@link ShelfListFilterPresenter}'s view.
     */
    public interface MyView extends View, HasUiHandlers<ShelfAddWithApprovalUiHandler> {

        public void loadApprovalAddShelfData(DataSource dataSource);
        public void refreshAllShelfData();
        public Window getShelfDetailWindow();
    }

    /**
     * Links the presenter to the view, proxy, event bus and dispatcher
     *
     * @param eventBus
     * @param view
     * @param proxy
     * @param dispatcher
     */
    @Inject
    public ShelfAddWithApprovalPresenter(EventBus eventBus, MyView view, MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
        ((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
        getView().loadApprovalAddShelfData(ShelfData.getAllShelfInProcessData(MainPagePresenter.signedInUser, 1, 20, "Create"));
        this.dispatcher = dispatcher;
    }
        
    @Override
    public void approveCreateShelfWIPData(String username, HashMap<String, String> data) {
        try {
            RPCRequest request = new RPCRequest();
            request.setData(Util.formXMLfromHashMap(data));
            request.setActionURL(GWT.getHostPageBaseURL() + shelfManagementPresenterServlet
                    + "?method=approveCreateShelfWIP&type=RPC&username=" + username);
            request.setHttpMethod("POST");
            request.setUseSimpleHttp(true);
            request.setWillHandleError(true);
            RPCManager.setPromptStyle(PromptStyle.DIALOG);
            RPCManager.setDefaultPrompt("Saving records...");
            RPCManager.setShowPrompt(true);

            RPCManager.sendRequest(request, new RPCCallback() {
                        @Override
                        public void execute(RPCResponse response, Object rawData, RPCRequest request) {
                            String rpcResponse = rawData.toString();

                            if (rpcResponse.startsWith("0")) {
                                SC.say("Shelf create approved");
                                getView().getShelfDetailWindow().destroy();
                                getView().refreshAllShelfData();
                            } else {
                                SC.warn(rpcResponse);
                            }
                        }
                    });
        } catch (Exception e) {
            SC.warn("Failed approve shelf, please try again later");
        }
    }
    
    @Override
    public void rejectCreateShelfWIPData(String username, HashMap<String, String> data) {
        try {
            RPCRequest request = new RPCRequest();
            request.setData(Util.formXMLfromHashMap(data));
            request.setActionURL(GWT.getHostPageBaseURL() + shelfManagementPresenterServlet
                    + "?method=rejectCreateShelfWIP&type=RPC&username=" + username);
            request.setHttpMethod("POST");
            request.setUseSimpleHttp(true);
            request.setWillHandleError(true);
            RPCManager.setPromptStyle(PromptStyle.DIALOG);
            RPCManager.setDefaultPrompt("Saving records...");
            RPCManager.setShowPrompt(true);

            RPCManager.sendRequest(request, new RPCCallback() {
                        @Override
                        public void execute(RPCResponse response, Object rawData, RPCRequest request) {
                            String rpcResponse = rawData.toString();

                            if (rpcResponse.startsWith("0")) {
                                SC.say("Shelf create rejected");
                                getView().getShelfDetailWindow().destroy();
                                getView().refreshAllShelfData();
                            } else {
                                SC.warn(rpcResponse);
                            }
                        }
                    });
        } catch (Exception e) {
            SC.warn("Failed reject shelf, please try again later");
        }
    }
    
    @Override
    public void needCorrectionCreateShelfWIPData(String username, HashMap<String, String> data) {
        try {
            RPCRequest request = new RPCRequest();
            request.setData(Util.formXMLfromHashMap(data));
            request.setActionURL(GWT.getHostPageBaseURL() + shelfManagementPresenterServlet
                    + "?method=needCorrectionCreateShelfWIP&type=RPC&username=" + username);
            request.setHttpMethod("POST");
            request.setUseSimpleHttp(true);
            request.setWillHandleError(true);
            RPCManager.setPromptStyle(PromptStyle.DIALOG);
            RPCManager.setDefaultPrompt("Saving records...");
            RPCManager.setShowPrompt(true);

            RPCManager.sendRequest(request, new RPCCallback() {
                        @Override
                        public void execute(RPCResponse response, Object rawData, RPCRequest request) {
                            String rpcResponse = rawData.toString();

                            if (rpcResponse.startsWith("0")) {
                                SC.say("Shelf need correction created");
                                getView().getShelfDetailWindow().destroy();
                                getView().refreshAllShelfData();
                            } else {
                                SC.warn(rpcResponse);
                            }
                        }
                    });
        } catch (Exception e) {
            SC.warn("Failed saving shelf, please try again later");
        }
    }
    
    @Override
	public void onEditShelfAddClicked(HashMap<String, String> shelfDataMap, HashMap<String, String> storageDataMap, final Window window) {
		RPCRequest request=new RPCRequest();
		
		String shelfMap = Util.formXMLfromHashMap(shelfDataMap);
		String storageMap = Util.formXMLfromHashMap(storageDataMap);
		
		request.setData(shelfMap+"#"+storageMap);
		
		request.setActionURL(GWT.getHostPageBaseURL() + shelfManagementPresenterServlet + "?method=editShelfAddWIPData&type=RPC");
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
                            SC.say("Shelf approval edited");
                            window.destroy();
							getView().refreshAllShelfData();
						} else {
							SC.warn("Edit shelf failed");
						}
					}
		});		
	}
}
