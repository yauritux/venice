/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.presenter;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.data.OpnameData;
import com.gdn.venice.client.app.inventory.view.handler.OpnameAdjustStockUiHandler;
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
import java.util.LinkedHashMap;

/**
 *
 * @author Maria Olivia
 */
public class OpnameAdjustStockPresenter extends Presenter<OpnameAdjustStockPresenter.MyView, OpnameAdjustStockPresenter.MyProxy>
        implements OpnameAdjustStockUiHandler {

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
    @NameToken(NameTokens.opnameAdjustStockPage)
    public interface MyProxy extends Proxy<OpnameAdjustStockPresenter>, Place {
    }

    /**
     * {@link PackingListPresenter}'s view.
     */
    public interface MyView extends View,
            HasUiHandlers<OpnameAdjustStockUiHandler> {

        public void loadOpnameData(DataSource ds);

        public void refreshAllOpnameData();

        public Window getAdjustOpnameWindow();

        public void setStorageData(LinkedHashMap<String, String> storageData);
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
    public OpnameAdjustStockPresenter(EventBus eventBus, MyView view,
            MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
        ((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
        getView().loadOpnameData(OpnameData.getAllOpnameData(1, 20));
        this.dispatcher = dispatcher;
    }

    @Override
    public void onSubmitButton(String opnameId) {
        try {
            RPCRequest request = new RPCRequest();

            request.setActionURL(GWT.getHostPageBaseURL() + opnamePresenterServlet
                    + "?method=saveOpnameAdjustment&type=RPC&username=" + MainPagePresenter.signedInUser
                    + "&opnameId=" + opnameId);
            request.setHttpMethod("POST");
            request.setUseSimpleHttp(true);
            request.setWillHandleError(true);
            RPCManager.setPromptStyle(PromptStyle.DIALOG);
            RPCManager.setDefaultPrompt("Sumbit opname...");
            RPCManager.setShowPrompt(true);

            RPCManager.sendRequest(request,
                    new RPCCallback() {
                        @Override
                        public void execute(RPCResponse response,
                                Object rawData, RPCRequest request) {
                            String rpcResponse = rawData.toString();
                            if (rpcResponse.startsWith("0")) {
                                SC.say("Opname saved");
                                getView().getAdjustOpnameWindow().destroy();
                            } else {
                                SC.warn(rpcResponse);
                            }
                        }
                    });
        } catch (Exception e) {
            SC.warn("Failed saving opname, please try again later");
        }
    }

    @Override
    public void onSkuSelected(String itemSKU) {
        RPCRequest request = new RPCRequest();
        request.setActionURL(GWT.getHostPageBaseURL() + opnamePresenterServlet
                + "?method=getStorageByItem&type=RPC" + "&itemSKU=" + itemSKU);
        request.setHttpMethod("POST");
        request.setUseSimpleHttp(true);
        RPCManager.sendRequest(request,
                new RPCCallback() {
                    @Override
                    public void execute(RPCResponse response,
                            Object rawData, RPCRequest request) {
                        String rpcResponse = rawData.toString();
                        getView().setStorageData(Util.formComboBoxMap(Util.formHashMapfromXML(rpcResponse)));
                    }
                });
    }
}
