/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.presenter;


import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.view.handler.PackingListUiHandler;
import com.gdn.venice.client.app.logistic.presenter.DeliveryStatusTrackingPresenter;
import com.gdn.venice.client.presenter.MainPagePresenter;
import com.gdn.venice.client.util.Util;
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
import java.util.LinkedHashMap;

/**
 *
 * @author Maria Olivia
 */
public class PackingListPresenter extends Presenter<PackingListPresenter.MyView, PackingListPresenter.MyProxy>
        implements PackingListUiHandler {

    @SuppressWarnings("unused")
    private final DispatchAsync dispatcher;
    public final static String packingListPresenterServlet = "PackingListPresenterServlet";

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
    }

    /**
     * {@link DeliveryStatusTrackingPresenter}'s proxy.
     */
    @ProxyCodeSplit
    @NameToken(NameTokens.packingListPage)
    public interface MyProxy extends Proxy<PackingListPresenter>, Place {
    }

    /**
     * {@link PackingListPresenter}'s view.
     */
    public interface MyView extends View,
            HasUiHandlers<PackingListUiHandler> {

        public void loadAllWarehouseData(LinkedHashMap<String, String> warehouse);

        public void refreshAllPackingListData();
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
    public PackingListPresenter(EventBus eventBus, MyView view,
            MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
//        loadWarehouseData();
        this.dispatcher = dispatcher;
    }

    public void loadWarehouseData() {
        RPCRequest request = new RPCRequest();

        request.setActionURL(GWT.getHostPageBaseURL() + packingListPresenterServlet + "?method=fetchWarehouseComboBoxData&type=RPC");
        request.setHttpMethod("POST");
        request.setUseSimpleHttp(true);

        RPCManager.sendRequest(request,
                new RPCCallback() {
                    @Override
                    public void execute(RPCResponse response,
                            Object rawData, RPCRequest request) {
                        String rpcResponse = rawData.toString();
                        getView().loadAllWarehouseData(Util.formComboBoxMap(Util.formHashMapfromXML(rpcResponse)));
                    }
                });
    }
}
