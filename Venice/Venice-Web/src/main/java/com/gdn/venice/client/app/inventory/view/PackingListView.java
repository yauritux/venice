/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.PackingListData;
import com.gdn.venice.client.app.inventory.presenter.PackingListPresenter;
import com.gdn.venice.client.app.inventory.view.handler.PackingListUiHandler;
import com.gdn.venice.client.data.RafDataSource;
import com.gdn.venice.client.presenter.MainPagePresenter;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.util.EventHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.events.KeyPressEvent;
import com.smartgwt.client.widgets.events.KeyPressHandler;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 *
 * @author Maria Olivia
 */
public class PackingListView extends ViewWithUiHandlers<PackingListUiHandler> implements
        PackingListPresenter.MyView {

    private int records;
    RafViewLayout packingListLayout;
    ListGrid packingListGrid, salesOrderGrid, attributeGrid;
    Window packingDetailWindow, attributeWindow;
    DynamicForm warehouseSelectionForm;
    ComboBoxItem cbWarehouse;
    ToolStripButton toolstrip;

    @Inject
    public PackingListView() {
        packingListLayout = new RafViewLayout();
        toolstrip = new ToolStripButton();

        packingListGrid = new ListGrid();
        packingListGrid.setWidth100();
        packingListGrid.setHeight100();
        packingListGrid.setShowAllRecords(true);
        packingListGrid.setSortField(0);
        packingListGrid.setShowFilterEditor(true);
        packingListGrid.setShowRowNumbers(true);
        packingListGrid.setAutoFetchData(Boolean.TRUE);
        packingListGrid.setAutoFitData(Autofit.BOTH);

        salesOrderGrid = new ListGrid();
        salesOrderGrid.setWidth100();
        salesOrderGrid.setHeight100();
        salesOrderGrid.setShowAllRecords(true);
        salesOrderGrid.setSortField(0);
        salesOrderGrid.setShowFilterEditor(true);
        salesOrderGrid.setShowRowNumbers(true);
        salesOrderGrid.setAutoFetchData(Boolean.TRUE);

        attributeGrid = new ListGrid();
        attributeGrid.setWidth100();
        attributeGrid.setHeight100();
        attributeGrid.setShowAllRecords(true);
        attributeGrid.setSortField(0);
        attributeGrid.setShowFilterEditor(false);
        attributeGrid.setShowRowNumbers(true);
        attributeGrid.setAutoFetchData(Boolean.TRUE);

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
                if (record.getAttribute(DataNameTokens.INV_SO_ITEMHASATTRIBUTE).equals(Boolean.toString(true))) {
                    getUiHandlers().onSalesOrderGridClicked(record.getAttribute(DataNameTokens.INV_SO_ID),
                            record.getAttribute(DataNameTokens.INV_SO_ITEMID), record.getAttribute(DataNameTokens.INV_SO_QUANTITY));
                } else {
                    SC.say("The record selected have no attribute");
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

        packingInfoForm.setFields(packingNo, awbNo, puDate, logistic, claimedBy);
        packingInfoForm.setDisabled(true);

        String username = MainPagePresenter.signedInUser == null
                || MainPagePresenter.signedInUser.trim().isEmpty()
                ? "olive" : MainPagePresenter.signedInUser;
        DataSource ds = PackingListData.getAllSalesData(awbId, username);
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);

        salesOrderGrid.setDataSource(ds);
        salesOrderGrid.setFields(listGridField);
        salesOrderGrid.getField(DataNameTokens.INV_SO_ID).setHidden(Boolean.TRUE);
        salesOrderGrid.getField(DataNameTokens.INV_SO_ITEMHASATTRIBUTE).setHidden(Boolean.TRUE);
        salesOrderGrid.getField(DataNameTokens.INV_SO_ATTRIBUTE).setHidden(Boolean.TRUE);
        salesOrderGrid.getField(DataNameTokens.INV_AWB_CLAIMEDBY).setHidden(Boolean.TRUE);
        salesOrderGrid.getField(DataNameTokens.INV_SO_ITEMID).setHidden(Boolean.TRUE);
        salesOrderGrid.setAutoFitData(Autofit.BOTH);

//        String claimer = salesOrderGrid.getRecord(0).getAttribute(DataNameTokens.INV_AWB_CLAIMEDBY);
//        claimedBy.setValue(claimer);

        ToolStrip packingToolStrip = new ToolStrip();
        packingToolStrip.setWidth100();
//        if (username.trim().equals(claimer)) {
        packingToolStrip.addButton(submitButton);
        packingToolStrip.addSeparator();
        packingToolStrip.addButton(printAwbButton);
        packingToolStrip.addSeparator();
        packingToolStrip.addButton(printLblButton);
        packingToolStrip.addSeparator();
//        }
        packingToolStrip.addButton(closeButton);

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                packingDetailWindow.destroy();
            }
        });

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onSavePacking(MainPagePresenter.signedInUser, awbId);
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

    @Override
    public Window buildAttributeWindow(final String salesOrderId, final int quantity, final DataSourceField[] dataSourceFields) {
        records = 0;
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
        final IButton saveButton = new IButton("Save");
        saveButton.setDisabled(true);

        buttonSet.setAlign(Alignment.CENTER);
        buttonSet.setMembers(closeButton, saveButton);

        dataSourceFields[0].setPrimaryKey(true);
        RafDataSource ds = new RafDataSource(
                "/response/data/*",
                null,
                null,
                null,
                null,
                dataSourceFields);

        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);

        attributeGrid.setDataSource(ds);
        attributeGrid.setFields(listGridField);

        attributeGrid.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (records < quantity) {
                    records++;
                }

                if (EventHandler.getKey().equalsIgnoreCase("Enter")
                        && records < quantity) {
                    attributeGrid.startEditingNew();
                } else {
                    saveButton.setDisabled(false);
                }
            }
        });


        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                attributeWindow.destroy();
            }
        });

        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                StringBuilder sb = new StringBuilder();
                Set<String> attr = new HashSet<String>();
                String attributeValue;
                for (int r = 0; r < quantity; r++) {
                    for (int c = 0; c < dataSourceFields.length; c++) {
                        if (attributeGrid.getEditValueAsString(r, dataSourceFields[c].getName()) == null
                                || attributeGrid.getEditValueAsString(r, dataSourceFields[c].getName()).isEmpty()) {
                            SC.warn("All attributes must be filled");
                            return;
                        } else {
                            attributeValue = dataSourceFields[c].getName() + ":" + attributeGrid.getEditValueAsString(r, dataSourceFields[c].getName());
                            if (attr.contains(attributeValue)) {
                                SC.warn(attributeValue + ", ERROR: cannot be inputed more than once");
                                return;
                            } else {
                                attr.add(attributeValue);
                            }
                        }
                    }
                }

                for (String string : attr) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(";");
                    }
                    sb.append(string);
                }

                getUiHandlers().onSaveAttribute(MainPagePresenter.signedInUser,
                        sb.toString(), salesOrderId);
            }
        });

        attributeLayout.setMembers(attributeGrid, buttonSet);
        attributeWindow.addItem(attributeLayout);

        return attributeWindow;
    }

    @Override
    public void loadAllWarehouseData(LinkedHashMap<String, String> warehouse) {
        cbWarehouse = new ComboBoxItem("warehouse", "Select warehouse");
        cbWarehouse.setValueMap(warehouse);

        warehouseSelectionForm = new DynamicForm();
        warehouseSelectionForm.setWidth100();
        warehouseSelectionForm.setPadding(20);
        warehouseSelectionForm.setFields(cbWarehouse);
        warehouseSelectionForm.setTitleOrientation(TitleOrientation.TOP);

        cbWarehouse.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent event) {
                loadPackingData(cbWarehouse.getValue().toString());
            }
        });

        packingListLayout.setMembers(toolstrip, warehouseSelectionForm);
    }

    public void loadPackingData(String warehouseId) {
        DataSource ds = PackingListData.getAllPackingData(warehouseId, 1, 20);
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);

        packingListGrid.setDataSource(ds);
        packingListGrid.setFields(listGridField);
        packingListGrid.getField(DataNameTokens.INV_AWB_ID).setHidden(Boolean.TRUE);

        refreshAllPackingListData();
        packingListLayout.setMembers(toolstrip, warehouseSelectionForm, packingListGrid);
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

    @Override
    public Window getAttributeWindow() {
        return attributeWindow;
    }

    @Override
    public Window getPackingDetailWindow() {
        return packingDetailWindow;
    }

    @Override
    public ListGrid getAttributeGrid() {
        return attributeGrid;
    }
}