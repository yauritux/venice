/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.presenter;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.NameTokens;
import com.gdn.venice.client.app.inventory.view.handler.GoodIssuedNoteUiHandler;
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
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.grid.ListGrid;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author Maria Olivia
 */
public class GoodIssuedNotePresenter extends Presenter<GoodIssuedNotePresenter.MyView, GoodIssuedNotePresenter.MyProxy>
        implements GoodIssuedNoteUiHandler {

    @SuppressWarnings("unused")
    private final DispatchAsync dispatcher;
    public final static String ginPresenterServlet = "GINPresenterServlet";

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetContextArea, this);
    }

    /**
     * {@link DeliveryStatusTrackingPresenter}'s proxy.
     */
    @ProxyCodeSplit
    @NameToken(NameTokens.ginPage)
    public interface MyProxy extends Proxy<GoodIssuedNotePresenter>, Place {
    }

    /**
     * {@link PackingListPresenter}'s view.
     */
    public interface MyView extends View,
            HasUiHandlers<GoodIssuedNoteUiHandler> {

        public void loadAllWarehouseData(LinkedHashMap<String, String> warehouse);

        public void refreshAllGinListData();
        
        public void refreshAwbGinListData();

        public void setLogisticMap(LinkedHashMap<String, String> logisticMap);

        public Window getGinDetailWindow();

        public ListGrid getAwbGinGrid();

        public void setRecordNumber(int recordNumber);
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
    public GoodIssuedNotePresenter(EventBus eventBus, MyView view,
            MyProxy proxy, DispatchAsync dispatcher) {
        super(eventBus, view, proxy);
        getView().setUiHandlers(this);
        ((RafViewLayout) getView().asWidget()).setViewPageName(getProxy().getNameToken());
        loadWarehouseData();
        loadLogisticComboData();
        this.dispatcher = dispatcher;
    }

    public void loadWarehouseData() {
        RPCRequest request = new RPCRequest();
        request.setActionURL(GWT.getHostPageBaseURL() + "WarehouseManagementPresenterServlet"
                + "?method=fetchWarehouseComboBoxData&type=RPC"
                + "&username=" + MainPagePresenter.signedInUser + "&isCode=true");
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
                        getView().loadAllWarehouseData(Util.formComboBoxMap(Util.formHashMapfromXML(rpcResponse)));
                    }
                });
    }

    public void loadLogisticComboData() {
        RPCRequest request = new RPCRequest();
        request.setActionURL(GWT.getHostPageBaseURL() + "DeliveryStatusTrackingPresenterServlet?"
                + "method=fetchLogisticProviderComboBoxData&type=RPC");
        request.setHttpMethod("POST");
        request.setUseSimpleHttp(true);

        RPCManager.setPromptStyle(PromptStyle.DIALOG);
        RPCManager.setDefaultPrompt("Get logistic list...");
        RPCManager.setShowPrompt(true);
        RPCManager.sendRequest(request,
                new RPCCallback() {
                    @Override
                    public void execute(RPCResponse response,
                            Object rawData, RPCRequest request) {
                        String rpcResponse = rawData.toString();
                        getView().setLogisticMap(Util.formComboBoxMap(Util.formHashMapfromXML(rpcResponse)));
                    }
                });
    }

    @Override
    public void onSaveGin(HashMap<String, String> data) {
        try {
            RPCRequest request = new RPCRequest();

            request.setData(Util.formXMLfromHashMap(data));

            request.setActionURL(GWT.getHostPageBaseURL() + ginPresenterServlet
                    + "?method=saveGIN&type=RPC&username=" + MainPagePresenter.signedInUser);
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
                                SC.say("GIN saved");
                                getView().getGinDetailWindow().destroy();
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
    public void onEditAwbNumberCompleted(String awbNumber, String logistic, String warehouseCode, final int recordNumber) {
        try {
            RPCRequest request = new RPCRequest();
            request.setActionURL(GWT.getHostPageBaseURL() + ginPresenterServlet
                    + "?method=checkAirwayBillNumber&type=RPC&awbNumber=" + awbNumber
                    + "&logistic=" + logistic + "&warehouseCode=" + warehouseCode);
            request.setHttpMethod("POST");
            request.setUseSimpleHttp(true);
            request.setWillHandleError(true);
            RPCManager.setPromptStyle(PromptStyle.DIALOG);
            RPCManager.setDefaultPrompt("Checking awb number...");
            RPCManager.setShowPrompt(true);

            RPCManager.sendRequest(request,
                    new RPCCallback() {
                        @Override
                        public void execute(RPCResponse response,
                                Object rawData, RPCRequest request) {
                            String rpcResponse = rawData.toString();
                            if (rpcResponse.startsWith("0")) {
                                getView().getAwbGinGrid().startEditingNew();
                                getView().setRecordNumber(recordNumber+1);
                            } else {
                                getView().getAwbGinGrid().clearEditValue(recordNumber, DataNameTokens.INV_GIN_AWB_NO);
                                SC.warn(rpcResponse);
                            }
                        }
                    });
        } catch (Exception e) {
            SC.warn("Failed checking awb, please try again later");
        }
    }
}
