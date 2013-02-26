/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.presenter;

import java.util.HashMap;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.data.WarehouseData;
import com.gdn.venice.client.app.inventory.view.handler.WarehouseListFilterUiHandler;
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
 * @author Maria Olivia
 */
public class WarehouseListFilterPresenter extends Presenter<WarehouseListFilterPresenter.MyView, WarehouseListFilterPresenter.MyProxy>
        implements WarehouseListFilterUiHandler {

    @SuppressWarnings("unused")
    private final DispatchAsync dispatcher;
    public final static String warehouseManagementPresenterServlet = "WarehouseManagementPresenterServlet";

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
    }

    /**
     * {@link DeliveryStatusTrackingPresenter}'s proxy.
     */
    @ProxyCodeSplit
    @NameToken(NameTokens.warehouseListFilterPage)
    public interface MyProxy extends Proxy<WarehouseListFilterPresenter>, Place {
    }

    /**
     * {@link WarehouseListFilterPresenter}'s view.
     */
    public interface MyView extends View,
            HasUiHandlers<WarehouseListFilterUiHandler> {

        public void loadAllWarehouseData(DataSource dataSource);

        public void refreshAllWarehouseData();
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
    public WarehouseListFilterPresenter(EventBus eventBus, MyView view,
            MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
        ((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
        getView().loadAllWarehouseData(WarehouseData.getAllWarehouseData(MainPagePresenter.signedInUser, 1, 20));
        this.dispatcher = dispatcher;
    }

    @Override
    public void saveOrUpdateWarehouseData(String username, HashMap<String, String> data, final Window window) {
        try {
            RPCRequest request = new RPCRequest();

            request.setData(Util.formXMLfromHashMap(data));

            request.setActionURL(GWT.getHostPageBaseURL() + warehouseManagementPresenterServlet
                    + "?method=saveUpdateWarehouseWIP&type=RPC&username=" + username);
            request.setHttpMethod("POST");
            request.setUseSimpleHttp(true);
            request.setWillHandleError(true);
            RPCManager.setPromptStyle(PromptStyle.DIALOG);
            RPCManager.setDefaultPrompt("Saving records...");
            RPCManager.setShowPrompt(true);

            RPCManager.sendRequest(request,
                    new RPCCallback() {
                        @Override
                        public void execute(RPCResponse response,
                                Object rawData, RPCRequest request) {
                            String rpcResponse = rawData.toString();

                            if (rpcResponse.startsWith("0")) {
                                SC.say("Warehouse added/updated and need approval for changes to take place");
                                window.destroy();
                                getView().refreshAllWarehouseData();
                            } else {
                                SC.warn(rpcResponse);
                            }

                        }
                    });
        } catch (Exception e) {
            SC.warn("Failed saving warehouse, please try again later");
        }
    }
}
