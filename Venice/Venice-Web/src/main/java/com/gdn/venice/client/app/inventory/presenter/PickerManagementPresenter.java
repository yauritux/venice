/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.presenter;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.data.PickerData;
import com.gdn.venice.client.app.inventory.view.handler.PickerManagementUiHandler;
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
public class PickerManagementPresenter extends Presenter<PickerManagementPresenter.MyView, PickerManagementPresenter.MyProxy>
        implements PickerManagementUiHandler {

    @SuppressWarnings("unused")
    private final DispatchAsync dispatcher;
    public final static String pickerManagementPresenterServlet = "PickerManagementPresenterServlet";

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
    }

    /**
     * {@link DeliveryStatusTrackingPresenter}'s proxy.
     */
    @ProxyCodeSplit
    @NameToken(NameTokens.pickerManagementPage)
    public interface MyProxy extends Proxy<PickerManagementPresenter>, Place {
    }

    /**
     * {@link PickerManagementPresenter}'s view.
     */
    public interface MyView extends View,
            HasUiHandlers<PickerManagementUiHandler> {

        public void loadAllPickerData(DataSource dataSource);

        public void refreshAllPickerData();

        public Window getDetailWindow();

        public void setPickerWarehouseData(LinkedHashMap<String, String> data);
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
    public PickerManagementPresenter(EventBus eventBus, MyView view,
            MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
        ((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
        getView().loadAllPickerData(PickerData.getAllPickerData(1, 100));
        loadWarehouseComboboxData();
        this.dispatcher = dispatcher;
    }

    @Override
    public void saveOrUpdatePickerData(HashMap<String, String> data) {
        try {
            RPCRequest request = new RPCRequest();

            request.setData(Util.formXMLfromHashMap(data));

            request.setActionURL(GWT.getHostPageBaseURL() + pickerManagementPresenterServlet
                    + "?method=saveUpdatePicker&type=RPC&username=" + MainPagePresenter.signedInUser);
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
                                SC.say("Picker added/updated");
                                getView().getDetailWindow().destroy();
                                getView().refreshAllPickerData();
                            } else {
                                SC.warn(rpcResponse);
                            }

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            SC.warn("Failed saving Picker, please try again later");
        }
    }

    @Override
    public void nonActivePicker(String id) {
        try {
            RPCRequest request = new RPCRequest();

            request.setActionURL(GWT.getHostPageBaseURL() + pickerManagementPresenterServlet
                    + "?method=nonActivePicker&type=RPC&username=" + MainPagePresenter.signedInUser + "&id=" + id);
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
                                SC.say("Picker added/updated");
                                getView().getDetailWindow().destroy();
                                getView().refreshAllPickerData();
                            } else {
                                SC.warn(rpcResponse);
                            }

                        }
                    });
        } catch (Exception e) {
            SC.warn("Failed deleting Picker, please try again later");
        }
    }

    private void loadWarehouseComboboxData() {
        RPCRequest request = new RPCRequest();
        request.setActionURL(GWT.getHostPageBaseURL() + "WarehouseManagementPresenterServlet"
                + "?method=fetchAllWarehouseComboBoxData&type=RPC");
        request.setHttpMethod("POST");
        request.setUseSimpleHttp(true);
        RPCManager.sendRequest(request,
                new RPCCallback() {
                    @Override
                    public void execute(RPCResponse response,
                            Object rawData, RPCRequest request) {
                        String rpcResponse = rawData.toString();
                        getView().setPickerWarehouseData(Util.formComboBoxMap(Util.formHashMapfromXML(rpcResponse)));
                    }
                });
    }
}
