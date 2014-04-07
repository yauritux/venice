package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.PickingListData;
import com.gdn.venice.client.app.inventory.presenter.PickingListPresenter;
import com.gdn.venice.client.app.inventory.view.handler.PickingListUiHandler;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.core.client.GWT;
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
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
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

/**
 *
 * @author Roland
 */
public class PickingListView extends ViewWithUiHandlers<PickingListUiHandler> implements
        PickingListPresenter.MyView {

    RafViewLayout layout;
    ListGrid warehouseItemListGrid, salesDetailListGrid, storageDetailListGrid;
    Window pickingListDetailWindow, attributeWindow;
    ToolStrip toolStrip;
    VLayout headerLayout;
    ComboBoxItem warehouseItemComboBox;
    DynamicForm itemDetailForm;
    int totalQtyPicked = 0;

    @Inject
    public PickingListView() {
        toolStrip = new ToolStrip();
        toolStrip.setWidth100();
        toolStrip.setPadding(2);

        ToolStripButton printButton = new ToolStripButton();
        printButton.setIcon("[SKIN]/icons/printer.png");
        printButton.setTooltip("Print");
        printButton.setTitle("Print");

        toolStrip.addButton(printButton);

        layout = new RafViewLayout();

        warehouseItemListGrid = new ListGrid();
        warehouseItemListGrid.setAutoFetchData(false);
        warehouseItemListGrid.setWidth100();
        warehouseItemListGrid.setHeight100();
        warehouseItemListGrid.setShowAllRecords(true);
        warehouseItemListGrid.setSortField(0);
        warehouseItemListGrid.setSelectionType(SelectionStyle.SINGLE);
        warehouseItemListGrid.setShowFilterEditor(true);
        warehouseItemListGrid.setCanResizeFields(true);
        warehouseItemListGrid.setShowRowNumbers(true);
        warehouseItemListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);

        warehouseItemListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = warehouseItemListGrid.getSelectedRecord();

                buildPickingListDetailWindow(record).show();
            }
        });

        warehouseItemListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {
            @Override
            public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
                refreshPickingListData();
            }
        });
        
        printButton.addClickHandler(new ClickHandler() {
     			@Override
     			public void onClick(ClickEvent event) {
     				ListGridRecord[] selectedRecords = warehouseItemListGrid.getSelection();
     				
     				StringBuilder sbSelectedRecords = new StringBuilder();
     				
     				for (int i = 0; i < selectedRecords.length; i++) {
     					ListGridRecord selectedRecord = selectedRecords[i];
     					
     					sbSelectedRecords.append(selectedRecord.getAttributeAsString(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMID));
     					
     					if(i != selectedRecords.length -1)
     						sbSelectedRecords.append(";");
     				}
     				
     				String host = GWT.getHostPageBaseURL();

     				if(host.contains("8889")){
     					host = "http://localhost:8090/";
     				}

     				if(host.contains("Venice/")){
     					host = host.substring(0, host.indexOf("Venice/"));
     				}
     												
     				com.google.gwt.user.client.Window.open(host + "Venice/PickingListExportServlet?warehouseItemIds=" + sbSelectedRecords.toString(), "_blank", null);
     							
     			}
     		});
    }

    private void buildWarehouseItemListGrid(String warehouseId) {
        DataSource warehouseItemData = PickingListData.getPickingListData(warehouseId, 1, 20);
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(warehouseItemData);
        ListGridField finalListGridField[] = {listGridField[1], listGridField[2], listGridField[3], listGridField[4], listGridField[5], listGridField[6]};

        warehouseItemListGrid.setDataSource(warehouseItemData);
        warehouseItemListGrid.setFields(finalListGridField);

        warehouseItemListGrid.setAutoFitData(Autofit.BOTH);
        refreshPickingListData();
    }

    private Window buildPickingListDetailWindow(final ListGridRecord record) {
        pickingListDetailWindow = new Window();
        pickingListDetailWindow.setWidth(1100);
        pickingListDetailWindow.setHeight(425);
        pickingListDetailWindow.setTitle("Picking List Detail");
        pickingListDetailWindow.setShowMinimizeButton(false);
        pickingListDetailWindow.setIsModal(true);
        pickingListDetailWindow.setShowModalMask(true);
        pickingListDetailWindow.centerInPage();

        IButton submitButton = new IButton("Submit");
        HLayout buttonSet = new HLayout(5);
        buttonSet.setAlign(Alignment.LEFT);
        buttonSet.setMembers(submitButton);

        pickingListDetailWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                SC.ask("Data not submitted yet, are you sure you want to close?", new BooleanCallback() {
                    @Override
                    public void execute(Boolean value) {
                        if (value != null && value == true) {
                            getUiHandlers().releaseLock(warehouseItemComboBox.getValue().toString());
                            resetTotalQtyPicked();
                            pickingListDetailWindow.destroy();
                        } else {
                            return;
                        }
                    }
                });
            }
        });

        VLayout headerLayout = new VLayout();
        headerLayout.setWidth100();
        headerLayout.setHeight(50);
        headerLayout.setMargin(10);
        headerLayout.setMembers(buttonSet);

        //set item data
        final DataSource itemDetailData = PickingListData.getPickingListItemDetailData(record.getAttribute(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMID));

        DataSourceField[] dataSourceFields = itemDetailData.getFields();
        FormItem[] formItems = new FormItem[dataSourceFields.length];

        for (int i = 0; i < dataSourceFields.length; i++) {
            formItems[i] = new StaticTextItem(dataSourceFields[i].getName());
        }

        itemDetailForm = new DynamicForm();
        itemDetailForm.setWidth(300);
        itemDetailForm.setHeight(300);
        itemDetailForm.setDataSource(itemDetailData);
        itemDetailForm.setUseAllDataSourceFields(false);
        itemDetailForm.setNumCols(2);
        itemDetailForm.setFields(formItems[0], formItems[1], formItems[2], formItems[3], formItems[4], formItems[5], formItems[6], formItems[7], formItems[8]);
        itemDetailForm.fetchData();

        //set sales order data
        DataSource salesDetailData = PickingListData.getSalesOrderListData(record.getAttribute(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMID));
        ListGridField listGridSalesField[] = Util.getListGridFieldsFromDataSource(salesDetailData);
        ListGridField finalListGridSalesField[] = {listGridSalesField[1], listGridSalesField[2]};

        salesDetailListGrid = new ListGrid();
        salesDetailListGrid.setDataSource(salesDetailData);
        salesDetailListGrid.setFields(finalListGridSalesField);
        salesDetailListGrid.setSelectionProperty("isSelected");
        salesDetailListGrid.setWidth(325);
        salesDetailListGrid.setHeight(300);
        salesDetailListGrid.setPadding(5);
        salesDetailListGrid.setMargin(5);
        salesDetailListGrid.setAutoFetchData(true);
        salesDetailListGrid.setUseAllDataSourceFields(false);
        salesDetailListGrid.setShowFilterEditor(false);
        salesDetailListGrid.setCanResizeFields(true);
        salesDetailListGrid.setShowRowNumbers(true);
        salesDetailListGrid.setShowAllRecords(true);
        salesDetailListGrid.setSortField(0);
        salesDetailListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
        salesDetailListGrid.setSelectionType(SelectionStyle.SIMPLE);
        salesDetailListGrid.getField(DataNameTokens.INV_PICKINGLIST_SALESORDERQTY).setWidth("40%");


        //set storage data
        DataSource storageDetailData = PickingListData.getStorageListData(record.getAttribute(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMID));
        ListGridField listGridStorageField[] = Util.getListGridFieldsFromDataSource(storageDetailData);
        ListGridField finalListGridStorageField[] = {listGridStorageField[1], listGridStorageField[2], listGridStorageField[3]};

        storageDetailListGrid = new ListGrid();
        storageDetailListGrid.setSaveLocally(true);
        storageDetailListGrid.setDataSource(storageDetailData);
        storageDetailListGrid.setFields(finalListGridStorageField);
        storageDetailListGrid.setWidth(325);
        storageDetailListGrid.setHeight(300);
        storageDetailListGrid.setPadding(5);
        storageDetailListGrid.setMargin(5);
        storageDetailListGrid.setAutoFetchData(true);
        storageDetailListGrid.setUseAllDataSourceFields(false);
        storageDetailListGrid.setShowFilterEditor(false);
        storageDetailListGrid.setCanResizeFields(true);
        storageDetailListGrid.setShowRowNumbers(true);
        storageDetailListGrid.setShowAllRecords(true);
        storageDetailListGrid.setSortField(0);
        storageDetailListGrid.setSelectionAppearance(SelectionAppearance.ROW_STYLE);
        storageDetailListGrid.setSelectionType(SelectionStyle.SIMPLE);
        storageDetailListGrid.getField(DataNameTokens.INV_PICKINGLIST_QTY).setWidth("30%");
        storageDetailListGrid.getField(DataNameTokens.INV_PICKINGLIST_QTYPICKED).setWidth("30%");
        storageDetailListGrid.getField(DataNameTokens.INV_PICKINGLIST_QTYPICKED).setCanEdit(true);

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ListGridRecord itemRecords = warehouseItemListGrid.getSelectedRecord();
                HashMap<String, String> itemDataMap = new HashMap<String, String>();
                HashMap<String, String> itemRowMap = new HashMap<String, String>();

                itemRowMap.put(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMID, itemRecords.getAttributeAsString(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMID));
                itemRowMap.put(DataNameTokens.INV_PICKINGLIST_ITEMID, itemDetailForm.getField(DataNameTokens.INV_PICKINGLIST_ITEMID).getValue().toString());
                itemRowMap.put(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMSKU, itemDetailForm.getValueAsString(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMSKU));
                itemRowMap.put(DataNameTokens.INV_PICKINGLIST_ITEMSKUNAME, itemDetailForm.getValueAsString(DataNameTokens.INV_PICKINGLIST_ITEMSKUNAME));
                itemRowMap.put(DataNameTokens.INV_PICKINGLIST_STOCKTYPE, itemDetailForm.getValueAsString(DataNameTokens.INV_PICKINGLIST_STOCKTYPE));
                itemRowMap.put(DataNameTokens.INV_PICKINGLIST_MERCHANT, itemDetailForm.getValueAsString(DataNameTokens.INV_PICKINGLIST_MERCHANT));
                itemRowMap.put(DataNameTokens.INV_PICKINGLIST_DIMENSION, itemDetailForm.getValueAsString(DataNameTokens.INV_PICKINGLIST_DIMENSION));
                itemRowMap.put(DataNameTokens.INV_PICKINGLIST_WEIGHT, itemDetailForm.getValueAsString(DataNameTokens.INV_PICKINGLIST_WEIGHT));
                itemRowMap.put(DataNameTokens.INV_PICKINGLIST_UOM, itemDetailForm.getValueAsString(DataNameTokens.INV_PICKINGLIST_UOM));
                itemRowMap.put(DataNameTokens.INV_PICKINGLIST_ATTRIBUTE, itemDetailForm.getValueAsString(DataNameTokens.INV_PICKINGLIST_ATTRIBUTE));
                itemDataMap.put("ITEM", itemRowMap.toString());

                ListGridRecord[] salesRecords = salesDetailListGrid.getSelection();
                HashMap<String, String> salesDataMap = new HashMap<String, String>();
                HashMap<String, String> salesRowMap = new HashMap<String, String>();

                for (int i = 0; i < salesRecords.length; i++) {
                    salesRowMap.put(DataNameTokens.INV_PICKINGLIST_SALESORDERID, salesRecords[i].getAttributeAsString(DataNameTokens.INV_PICKINGLIST_SALESORDERID));
                    salesRowMap.put(DataNameTokens.INV_PICKINGLIST_SALESORDERNUMBER, salesRecords[i].getAttributeAsString(DataNameTokens.INV_PICKINGLIST_SALESORDERNUMBER));
                    salesRowMap.put(DataNameTokens.INV_PICKINGLIST_SALESORDERQTY, salesRecords[i].getAttributeAsString(DataNameTokens.INV_PICKINGLIST_SALESORDERQTY));
                    salesRowMap.put(DataNameTokens.INV_PICKINGLIST_SALESORDERTIPEPENANGANAN, salesRecords[i].getAttributeAsString(DataNameTokens.INV_PICKINGLIST_SALESORDERTIPEPENANGANAN));
                    salesDataMap.put("SALES" + i, salesRowMap.toString());
                }

                ListGridRecord[] storageRecords = storageDetailListGrid.getRecords();
                HashMap<String, String> storageDataMap = new HashMap<String, String>();
                HashMap<String, String> storageRowMap = new HashMap<String, String>();

                for (int i = 0; i < storageRecords.length; i++) {
                    storageRowMap.put(DataNameTokens.INV_PICKINGLIST_WAREHOUSESTORAGEID, storageRecords[i].getAttributeAsString(DataNameTokens.INV_PICKINGLIST_WAREHOUSESTORAGEID));
                    storageRowMap.put(DataNameTokens.INV_PICKINGLIST_SHELFCODE, storageRecords[i].getAttributeAsString(DataNameTokens.INV_PICKINGLIST_SHELFCODE));
                    storageRowMap.put(DataNameTokens.INV_PICKINGLIST_QTY, storageRecords[i].getAttributeAsString(DataNameTokens.INV_PICKINGLIST_QTY));
                    storageRowMap.put(DataNameTokens.INV_PICKINGLIST_QTYPICKED, storageRecords[i].getAttributeAsString(DataNameTokens.INV_PICKINGLIST_QTYPICKED));

                    int qtyPick = 0;
                    try {
                        qtyPick = Integer.parseInt(storageRecords[i].getAttributeAsString(DataNameTokens.INV_PICKINGLIST_QTYPICKED));
                    } catch (Exception e) {
                        qtyPick = 0;
                    }
                    totalQtyPicked += qtyPick;
                    storageDataMap.put("STORAGE" + i, storageRowMap.toString());
                }
                getUiHandlers().onSaveClicked(itemDataMap, salesDataMap, storageDataMap, totalQtyPicked);
            }
        });

        Label itemLabel = new Label("<b>Item Detail:</b>");
        itemLabel.setHeight(10);

        Label salesLabel = new Label("<b>Sales Order Detail:</b>");
        salesLabel.setHeight(10);

        Label storageLabel = new Label("<b>Shelf List:</b>");
        storageLabel.setHeight(10);

        VLayout itemLayout = new VLayout();
        itemLayout.setWidth100();
        itemLayout.setMargin(5);
        itemLayout.setMembers(itemLabel, itemDetailForm);

        VLayout salesLayout = new VLayout();
        salesLayout.setWidth100();
        salesLayout.setMargin(5);
        salesLayout.setMembers(salesLabel, salesDetailListGrid);

        VLayout storageLayout = new VLayout();
        storageLayout.setWidth100();
        storageLayout.setMargin(5);
        storageLayout.setMembers(storageLabel, storageDetailListGrid);

        HLayout detailLayout = new HLayout();
        detailLayout.setWidth100();
        detailLayout.setMargin(10);
        detailLayout.setMembers(itemLayout, salesLayout, storageLayout);

        VLayout bigLayout = new VLayout();
        bigLayout.setHeight100();
        bigLayout.setWidth100();
        bigLayout.setMembers(headerLayout, detailLayout);
        pickingListDetailWindow.addItem(bigLayout);

        return pickingListDetailWindow;
    }

    @Override
    public void loadPickingListData(LinkedHashMap<String, String> warehouseMap) {
        final DynamicForm warehouseForm = new DynamicForm();
        warehouseForm.setPadding(5);
        warehouseForm.setNumCols(2);

        warehouseItemComboBox = new ComboBoxItem();
        warehouseItemComboBox.setTitle("Warehouse");
        warehouseItemComboBox.setValueMap(warehouseMap);

        warehouseForm.setFields(warehouseItemComboBox);

        warehouseItemComboBox.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent event) {
                buildWarehouseItemListGrid(warehouseItemComboBox.getValue().toString());
            }
        });

        headerLayout = new VLayout();
        headerLayout.setWidth100();
        headerLayout.setMargin(10);
        headerLayout.setMembers(warehouseForm);

        layout.setMembers(toolStrip, headerLayout, warehouseItemListGrid);
    }

    @Override
    public void refreshPickingListData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                warehouseItemListGrid.setData(response.getData());
            }
        };

        warehouseItemListGrid.getDataSource().fetchData(warehouseItemListGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public Widget asWidget() {
        return layout;
    }

    @Override
    public Window getPickingListDetailWindow() {
        return pickingListDetailWindow;
    }

    @Override
    public int resetTotalQtyPicked() {
        return totalQtyPicked = 0;
    }
}