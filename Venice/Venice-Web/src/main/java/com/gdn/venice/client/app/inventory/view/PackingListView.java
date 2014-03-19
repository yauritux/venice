/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.PackingListData;
import com.gdn.venice.client.app.inventory.presenter.PackingListPresenter;
import com.gdn.venice.client.app.inventory.view.handler.PackingListUiHandler;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitEvent;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import java.util.LinkedHashMap;

/**
 *
 * @author Maria Olivia
 */
public class PackingListView extends ViewWithUiHandlers<PackingListUiHandler> implements
        PackingListPresenter.MyView {

    RafViewLayout packingListLayout;
    ListGrid packingListGrid, salesOrderGrid;
    Window packingDetailWindow, attributeWindow;
    DynamicForm warehouseSelectionForm;
    ComboBoxItem cbWarehouse;

    @Inject
    public PackingListView() {
        cbWarehouse = new ComboBoxItem();
        
        warehouseSelectionForm = new DynamicForm();
        warehouseSelectionForm.setWidth100();
        warehouseSelectionForm.setPadding(5);
        warehouseSelectionForm.setFields(cbWarehouse);

        packingListLayout = new RafViewLayout();

        packingListGrid = new ListGrid();
        packingListGrid.setWidth100();
        packingListGrid.setHeight100();
        packingListGrid.setShowAllRecords(true);
        packingListGrid.setSortField(0);
        packingListGrid.setShowFilterEditor(true);
        packingListGrid.setShowRowNumbers(true);

        salesOrderGrid = new ListGrid();
        salesOrderGrid.setWidth100();
        salesOrderGrid.setHeight100();
        salesOrderGrid.setShowAllRecords(true);
        salesOrderGrid.setSortField(0);
        salesOrderGrid.setShowFilterEditor(true);
        salesOrderGrid.setShowRowNumbers(true);

        bindCustomUiHandlers();
    }

    private void bindCustomUiHandlers() {
        packingListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = packingListGrid.getSelectedRecord();
                buildPackingDetailWindow(record).show();
            }
        });

        cbWarehouse.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent event) {
                loadPackingData(cbWarehouse.getValue().toString());
            }
        });

        packingListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {
            @Override
            public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
                refreshAllPackingListData();
            }
        });

        salesOrderGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = salesOrderGrid.getSelectedRecord();
                if (record.getAttributeAsBoolean(DataNameTokens.INV_SO_ITEMHASATTRIBUTE)) {
                    buildAttributeWindow(record.getAttribute(DataNameTokens.INV_SO_ID),
                            record.getAttributeAsInt(DataNameTokens.INV_SO_QUANTITY)).show();
                }
            }
        });
    }

    private Window buildPackingDetailWindow(final ListGridRecord record) {
        packingDetailWindow = new Window();
        packingDetailWindow.setWidth(600);
        packingDetailWindow.setHeight(450);
        packingDetailWindow.setCanDragResize(true);
        packingDetailWindow.setTitle("Packing List");
        packingDetailWindow.setShowMinimizeButton(false);
        packingDetailWindow.setIsModal(true);
        packingDetailWindow.setShowModalMask(true);
        packingDetailWindow.centerInPage();

        packingDetailWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                packingDetailWindow.destroy();
            }
        });

        ToolStripButton submitButton = new ToolStripButton();
        submitButton.setIcon("[SKIN]/icons/process.png");
        submitButton.setTitle("Submit");

        ToolStripButton printAwbButton = new ToolStripButton();
        printAwbButton.setIcon("[SKIN]/icons/printer.png");
        printAwbButton.setTitle("Print AWB");

        ToolStripButton printLblButton = new ToolStripButton();
        printLblButton.setIcon("[SKIN]/icons/printer.png");
        printLblButton.setTitle("Print Label");

        ToolStripButton closeButton = new ToolStripButton();
        closeButton.setTitle("Close");

        ToolStrip packingToolStrip = new ToolStrip();
        packingToolStrip.addButton(submitButton);
        packingToolStrip.addSeparator();
        packingToolStrip.addButton(printAwbButton);
        packingToolStrip.addSeparator();
        packingToolStrip.addButton(printLblButton);
        packingToolStrip.addSeparator();
        packingToolStrip.addButton(closeButton);

        VLayout packingDetailLayout = new VLayout();
        packingDetailLayout.setHeight100();
        packingDetailLayout.setWidth100();

        final String awbId = record.getAttribute(DataNameTokens.INV_AWB_ID);

        final DynamicForm packingInfoForm = new DynamicForm();
        packingInfoForm.setWidth100();
        packingInfoForm.setPadding(5);

        TextItem packingNo = new TextItem("packingNo", "Packing List No");
        packingNo.setValue("[generated by system]");

        TextItem awbNo = new TextItem(DataNameTokens.INV_AWB_NO, "AWB No");
        awbNo.setValue(record.getAttribute(DataNameTokens.INV_AWB_NO));

        TextItem puDate = new TextItem(DataNameTokens.INV_AWB_PUDATE, "PU Date");
        puDate.setValue(record.getAttribute(DataNameTokens.INV_AWB_PUDATE));

        TextItem logistic = new TextItem(DataNameTokens.INV_AWB_LOGNAME, "Logistic Name");
        logistic.setValue(record.getAttribute(DataNameTokens.INV_AWB_LOGNAME));

        TextItem claimedBy = new TextItem(DataNameTokens.INV_AWB_CLAIMEDBY, "Claimed By");
        claimedBy.setValue(record.getAttribute(DataNameTokens.INV_AWB_CLAIMEDBY));

        packingInfoForm.setFields(packingNo, awbNo, puDate, logistic, claimedBy);
        packingInfoForm.setDisabled(true);

        DataSource ds = PackingListData.getAllSalesData(awbId, 1, 20);
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);

        salesOrderGrid.setDataSource(ds);
        salesOrderGrid.setAutoFetchData(Boolean.TRUE);
        salesOrderGrid.setFields(listGridField);
        salesOrderGrid.getField(DataNameTokens.INV_AWB_ID).setHidden(Boolean.TRUE);
        salesOrderGrid.setAutoFitData(Autofit.BOTH);

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                packingDetailWindow.destroy();
            }
        });

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            }
        });

        printAwbButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            }
        });

        packingDetailLayout.setMembers(packingToolStrip, packingInfoForm, salesOrderGrid);
        packingDetailWindow.addItem(packingDetailLayout);

        return packingDetailWindow;
    }

    private Window buildAttributeWindow(String salesOrderId, int quantity) {
        attributeWindow = new Window();
        attributeWindow.setWidth(600);
        attributeWindow.setHeight(400);
        attributeWindow.setCanDragResize(true);
        attributeWindow.setTitle("Insert Attribute Data");
        attributeWindow.setShowMinimizeButton(false);
        attributeWindow.setIsModal(true);
        attributeWindow.setShowModalMask(true);
        attributeWindow.centerInPage();

        attributeWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                attributeWindow.destroy();
            }
        });

        VLayout attributeLayout = new VLayout();
        attributeLayout.setHeight100();
        attributeLayout.setWidth100();

        HLayout buttonSet = new HLayout(5);

        IButton closeButton = new IButton("Cancel");
        IButton saveButton = new IButton("Save");

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                attributeWindow.destroy();
            }
        });

        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            }
        });

        buttonSet.setAlign(Alignment.CENTER);
        buttonSet.setMembers(closeButton, saveButton);

        attributeLayout.setMembers(buttonSet);
        attributeWindow.addItem(attributeLayout);

        return attributeWindow;
    }

    @Override
    public void loadAllWarehouseData(LinkedHashMap<String, String> warehouse) {
        cbWarehouse = new ComboBoxItem("warehouse", "Select warehouse");
        cbWarehouse.setValueMap(warehouse);
        packingListLayout.setMembers(warehouseSelectionForm);
    }

    public void loadPackingData(String warehouseId) {
        DataSource ds = PackingListData.getAllPackingData(warehouseId, 1, 20);
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);

        packingListGrid.setDataSource(ds);
        packingListGrid.setAutoFetchData(Boolean.TRUE);
        packingListGrid.setFields(listGridField);
        packingListGrid.getField(DataNameTokens.INV_AWB_ID).setHidden(Boolean.TRUE);
        packingListGrid.setAutoFitData(Autofit.BOTH);

        packingListLayout.setMembers(warehouseSelectionForm, packingListGrid);
    }

    @Override
    public void refreshAllPackingListData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                packingListGrid.setData(response.getData());
            }
        };

        packingListGrid.getDataSource().fetchData(packingListGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public Widget asWidget() {
        return packingListLayout;
    }
}