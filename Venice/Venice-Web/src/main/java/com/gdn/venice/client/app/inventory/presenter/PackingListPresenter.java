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
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.rpc.RPCCallback;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.PromptStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.grid.ListGrid;
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

        public Window buildAttributeWindow(String salesOrderId, int quantity, DataSourceField[] dataSourceFields);

        public Window getAttributeWindow();

        public ListGrid getAttributeGrid();

        public Window getPackingDetailWindow();
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
        ((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
        loadWarehouseData();
        this.dispatcher = dispatcher;
    }

    public void loadWarehouseData() {
        RPCRequest request = new RPCRequest();
        request.setActionURL(GWT.getHostPageBaseURL() + "WarehouseManagementPresenterServlet?method=fetchWarehouseComboBoxData&type=RPC&username=" + MainPagePresenter.signedInUser);
        request.setHttpMethod("POST");
        request.setUseSimpleHttp(true);
        request.setPrompt("Get eligible warehouse data");
        request.setShowPrompt(true);
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

    @Override
    public void onSalesOrderGridClicked(final String salesOrderId, String itemId, final String quantity) {
        RPCRequest request = new RPCRequest();
        request.setActionURL(GWT.getHostPageBaseURL() + packingListPresenterServlet + "?method=fetchAttributeName&type=RPC&itemId=" + itemId);
        request.setHttpMethod("POST");
        request.setUseSimpleHttp(true);
        RPCManager.sendRequest(request,
                new RPCCallback() {
                    @Override
                    public void execute(RPCResponse response,
                            Object rawData, RPCRequest request) {
                        String[] fieldName = rawData.toString().split(";");
                        DataSourceField[] dataSourceFields = new DataSourceField[fieldName.length];
                        for (int i = 0; i < fieldName.length; i++) {
                            dataSourceFields[i] = new DataSourceTextField(fieldName[i].trim(), fieldName[i].trim());
                        }
                        getView().buildAttributeWindow(salesOrderId, Integer.parseInt(quantity), dataSourceFields).show();
                        getView().getAttributeGrid().startEditingNew();

                    }
                });
    }

    @Override
    public void onSaveAttribute(String username, String attributes, String salesOrderId) {
        try {
            RPCRequest request = new RPCRequest();

            request.setData(attributes);

            request.setActionURL(GWT.getHostPageBaseURL() + packingListPresenterServlet
                    + "?method=saveAttribute&type=RPC&username=" + username + "&salesOrderId=" + salesOrderId);
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
                                SC.say("Attributes saved");
                                getView().getAttributeWindow().destroy();
                            } else {
                                SC.warn(rpcResponse);
                            }
                        }
                    });
        } catch (Exception e) {
            SC.warn("Failed saving attribute, please try again later");
        }
    }

    @Override
    public void onSavePacking(String username, String awbInfoId) {
        try {
            RPCRequest request = new RPCRequest();

            request.setActionURL(GWT.getHostPageBaseURL() + packingListPresenterServlet
                    + "?method=savePacking&type=RPC&username=" + username + "&awbInfoId=" + awbInfoId);
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
                                SC.say("Packing completed");
                                getView().getPackingDetailWindow().destroy();
                                getView().refreshAllPackingListData();
                            } else {
                                SC.warn(rpcResponse);
                            }
                        }
                    });
        } catch (Exception e) {
            SC.warn("Failed saving packing data, please try again later");
        }
    }

    @Override
    public void onRejectPacking(String salesOrderId) {
        try {
            RPCRequest request = new RPCRequest();
            request.setActionURL(GWT.getHostPageBaseURL() + packingListPresenterServlet
                    + "?method=rejectPacking&type=RPC&username=" + MainPagePresenter.signedInUser + "&salesOrderId=" + salesOrderId);
            request.setHttpMethod("POST");
            request.setUseSimpleHttp(true);
            request.setWillHandleError(true);
            RPCManager.setPromptStyle(PromptStyle.DIALOG);
            RPCManager.setDefaultPrompt("Submitting request...");
            RPCManager.setShowPrompt(true);

            RPCManager.sendRequest(request,
                    new RPCCallback() {
                        @Override
                        public void execute(RPCResponse response,
                                Object rawData, RPCRequest request) {
                            String rpcResponse = rawData.toString();

                            if (rpcResponse.startsWith("0")) {
                                SC.say("Sales order rejected. AWB cannot be packed until all Sales Order's picked");
                                getView().getPackingDetailWindow().destroy();
                                getView().refreshAllPackingListData();
                            } else {
                                SC.warn(rpcResponse);
                            }
                        }
                    });
        } catch (Exception e) {
            SC.warn("Failed reject packing, please try again later");
        }
    }
}
