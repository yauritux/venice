/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.presenter;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.view.handler.OpnameUiHandler;
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
import com.smartgwt.client.rpc.RPCCallback;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.PromptStyle;
import com.smartgwt.client.widgets.Window;
import java.util.LinkedHashMap;

/**
 *
 * @author Maria Olivia
 */
public class OpnamePresenter extends Presenter<OpnamePresenter.MyView, OpnamePresenter.MyProxy>
        implements OpnameUiHandler {

    @SuppressWarnings("unused")
    private final DispatchAsync dispatcher;
    public final static String opnamePresenterServlet = "OpnamePresenterServlet";

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
    }

    /**
     * {@link DeliveryStatusTrackingPresenter}'s proxy.
     */
    @ProxyCodeSplit
    @NameToken(NameTokens.opnamePage)
    public interface MyProxy extends Proxy<OpnamePresenter>, Place {
    }

    /**
     * {@link PackingListPresenter}'s view.
     */
    public interface MyView extends View,
            HasUiHandlers<OpnameUiHandler> {

        public void loadComboboxData(LinkedHashMap<String, String> warehouse);

        public void refreshAllItemStorageData();

        public void refreshAllSupplierData();

        public Window getCreateOpnameWindow();
        
        public void setCategoryMap(LinkedHashMap<String, String> map);
        
        public void setUomMap(LinkedHashMap<String, String> map);
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
    public OpnamePresenter(EventBus eventBus, MyView view,
            MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
        ((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
        loadWarehouseData();
        loadCategoryData();
        loadUomData();
        this.dispatcher = dispatcher;
    }

    private void loadWarehouseData() {
        RPCRequest request = new RPCRequest();
        request.setActionURL(GWT.getHostPageBaseURL() + "WarehouseManagementPresenterServlet"
                + "?method=fetchWarehouseComboBoxData&type=RPC"
                + "&username=" + MainPagePresenter.signedInUser
                + "&isCode=true");
        request.setHttpMethod("POST");
        request.setUseSimpleHttp(true);
        RPCManager.setPromptStyle(PromptStyle.DIALOG);
        RPCManager.setDefaultPrompt("Get eligible warehouse...");
        RPCManager.setShowPrompt(true);
        RPCManager.sendRequest(request,
                new RPCCallback() {
                    @Override
                    public void execute(RPCResponse response,
                            Object rawData, RPCRequest request) {
                        String rpcResponse = rawData.toString();
                        getView().loadComboboxData(Util.formComboBoxMap(Util.formHashMapfromXML(rpcResponse)));
                    }
                });
    }

    @Override
    public void onSubmitButton(String data, String warehouseCode, String stockType, String supplierCode) {
        String host = GWT.getHostPageBaseURL();
        if (host.contains("8889")) {
            host = "http://localhost:8090/Venice/";
        }

        getView().getCreateOpnameWindow().destroy();
        com.google.gwt.user.client.Window.open(host + opnamePresenterServlet
                + "?method=saveOpnameList&type=RPC&username=" + MainPagePresenter.signedInUser
                + "&warehouseCode=" + warehouseCode + "&data=" + data
                + "&stockType=" + stockType + "&supplierCode=" + supplierCode, "_blank", null);
    }

    private void loadCategoryData() {
        RPCRequest request = new RPCRequest();
        request.setActionURL(GWT.getHostPageBaseURL() + opnamePresenterServlet
                + "?method=fetchCategoryComboBoxData&type=RPC");
        request.setHttpMethod("POST");
        request.setUseSimpleHttp(true);
        RPCManager.sendRequest(request,
                new RPCCallback() {
                    @Override
                    public void execute(RPCResponse response,
                            Object rawData, RPCRequest request) {
                        String rpcResponse = rawData.toString();
                        getView().setCategoryMap(Util.formComboBoxMap(Util.formHashMapfromXML(rpcResponse)));
                    }
                });
    }

    private void loadUomData() {
        RPCRequest request = new RPCRequest();
        request.setActionURL(GWT.getHostPageBaseURL() + opnamePresenterServlet
                + "?method=fetchUomComboBoxData&type=RPC");
        request.setHttpMethod("POST");
        RPCManager.sendRequest(request,
                new RPCCallback() {
                    @Override
                    public void execute(RPCResponse response,
                            Object rawData, RPCRequest request) {
                        String rpcResponse = rawData.toString();
                        getView().setUomMap(Util.formComboBoxMap(Util.formHashMapfromXML(rpcResponse)));
                    }
                });
    }
}
