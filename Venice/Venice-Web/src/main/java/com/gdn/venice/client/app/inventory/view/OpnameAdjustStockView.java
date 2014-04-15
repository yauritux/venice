/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.OpnameData;
import com.gdn.venice.client.app.inventory.presenter.OpnameAdjustStockPresenter;
import com.gdn.venice.client.app.inventory.view.handler.OpnameAdjustStockUiHandler;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.EditorValueMapFunction;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitEvent;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Maria Olivia
 */
public class OpnameAdjustStockView extends ViewWithUiHandlers<OpnameAdjustStockUiHandler> implements
        OpnameAdjustStockPresenter.MyView {

    RafViewLayout opnameLayout;
    ListGrid opnameGrid, opnameDetailGrid;
    Window adjustOpnameWindow;
    DynamicForm warehouseSelectionForm;
    ComboBoxItem cbWarehouse, cbInventoryType;
    SelectItem cbSupplier;
    ToolStripButton processButton;

    @Inject
    public OpnameAdjustStockView() {
        opnameLayout = new RafViewLayout();

        opnameGrid = new ListGrid();
        opnameGrid.setWidth100();
        opnameGrid.setHeight100();
        opnameGrid.setShowAllRecords(true);
        opnameGrid.setSortField(0);
        opnameGrid.setShowFilterEditor(true);
        opnameGrid.setShowRowNumbers(true);
        opnameGrid.setAutoFetchData(true);
        opnameGrid.setAutoFitData(Autofit.BOTH);
        opnameGrid.setCanSelectText(true);

        opnameDetailGrid = new ListGrid();
        opnameDetailGrid.setWidth100();
        opnameDetailGrid.setHeight100();
        opnameDetailGrid.setShowAllRecords(true);
        opnameDetailGrid.setSortField(0);
        opnameDetailGrid.setShowFilterEditor(false);
        opnameDetailGrid.setShowRowNumbers(true);
        opnameDetailGrid.setCanSelectText(true);
        opnameDetailGrid.setAutoFetchData(true);
        opnameDetailGrid.setCanEdit(true);

        bindCustomUiHandlers();
    }

    private void bindCustomUiHandlers() {
        opnameGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {
            @Override
            public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
                refreshAllOpnameData();
            }
        });

        opnameGrid.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                buildOpnameWindow(opnameGrid.getSelectedRecord()).show();
            }
        });
    }

    private Window buildOpnameWindow(final ListGridRecord record) {
        adjustOpnameWindow = new Window();
        adjustOpnameWindow.setWidth(800);
        adjustOpnameWindow.setHeight(450);
        adjustOpnameWindow.setCanDragResize(true);
        adjustOpnameWindow.setShowMinimizeButton(false);
        adjustOpnameWindow.setShowCloseButton(false);
        adjustOpnameWindow.setIsModal(true);
        adjustOpnameWindow.setShowModalMask(true);
        adjustOpnameWindow.centerInPage();
        adjustOpnameWindow.setTitle("Adjust Stock");

        ToolStrip opnameToolStrip = new ToolStrip();
        opnameToolStrip.setWidth100();

        ToolStripButton submitButton = new ToolStripButton();
        submitButton.setIcon("[SKIN]/icons/process.png");
        submitButton.setTitle("Submit");

        ToolStripButton addRowButton = new ToolStripButton();
        addRowButton.setIcon("[SKIN]/icons/add.png");
        addRowButton.setTitle("Add row");

        VLayout packingDetailLayout = new VLayout();
        packingDetailLayout.setHeight100();
        packingDetailLayout.setWidth100();

        final DynamicForm opnameForm = new DynamicForm();
        opnameForm.setWidth100();
        opnameForm.setPadding(5);

        TextItem opnameNo = new TextItem(DataNameTokens.INV_OPNAME_NO, "Opname No");
        opnameNo.setDisabled(true);
        opnameNo.setValue(record.getAttribute(DataNameTokens.INV_OPNAME_NO));

        final String id = record.getAttribute(DataNameTokens.INV_OPNAME_ID);
        DataSource ds = OpnameData.getOpnameDetail(id);

        opnameDetailGrid.setDataSource(ds);
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);
        opnameDetailGrid.setFields(listGridField);
        opnameDetailGrid.setAutoFitData(Autofit.BOTH);
        opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID).setHidden(true);

        opnameToolStrip.addSeparator();
        opnameToolStrip.setMembers(submitButton, addRowButton);

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onSubmitButton(id);
            }
        });

        final SelectItem skuSelection = new SelectItem();
        skuSelection.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent event) {
                getUiHandlers().onSkuSelected(event.getValue().toString(),
                        record.getAttribute(DataNameTokens.INV_OPNAME_WAREHOUSECODE),
                        record.getAttribute(DataNameTokens.INV_OPNAME_STOCKTYPE),
                        record.getAttribute(DataNameTokens.INV_OPNAME_SUPPLIERCODE));
//                opnameDetailGrid.setValueMap(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE, new LinkedHashMap());
            }
        });

        final SelectItem storageSelection = new SelectItem();
        storageSelection.setAddUnknownValues(false);
//        opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE).setEditorValueMapFunction(new EditorValueMapFunction() {
//            @Override
//            public Map getEditorValueMap(Map values, ListGridField field, ListGrid grid) {
//                return storageData;
//            }
//        });

        opnameDetailGrid.addEditCompleteHandler(new EditCompleteHandler() {
            @Override
            public void onEditComplete(EditCompleteEvent event) {
                opnameDetailGrid.saveAllEdits();
                refreshAllOpnameDetailData();
                if (event.getDsResponse().getStatus() == 0) {
                    SC.say("Data Added/Edited");
                }
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMCATEGORY).setCanEdit(false);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMNAME).setCanEdit(false);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMSKU).setCanEdit(false);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMUOM).setCanEdit(false);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_QTY).setCanEdit(false);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_SHELFCODE).setCanEdit(false);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE).setCanEdit(false);
            }
        });

        addRowButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMCATEGORY).setCanEdit(false);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMNAME).setCanEdit(false);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMUOM).setCanEdit(false);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_QTY).setCanEdit(false);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_SHELFCODE).setCanEdit(false);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMSKU).setCanEdit(true);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE).setCanEdit(true);
                Map<String, String> data = new HashMap<String, String>();
                data.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID, id);
                LinkedHashMap<String, String> availableSKU = new LinkedHashMap<String, String>();
                for (int i = 0; i < opnameDetailGrid.getRecords().length; i++) {
                    availableSKU.put(opnameDetailGrid.getRecords()[i].getAttribute(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMSKU),
                            opnameDetailGrid.getRecords()[i].getAttribute(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMSKU));
                }
                skuSelection.setValueMap(availableSKU);
                storageSelection.setValueMap(new LinkedHashMap());
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMSKU).setEditorType(skuSelection);
                opnameDetailGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE).setEditorType(storageSelection);
                opnameDetailGrid.startEditingNew(data);
            }
        });

        adjustOpnameWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                adjustOpnameWindow.destroy();
            }
        });
        packingDetailLayout.setMembers(opnameToolStrip, opnameForm, opnameDetailGrid);
        adjustOpnameWindow.addItem(packingDetailLayout);

        return adjustOpnameWindow;
    }

    @Override
    public void loadOpnameData(DataSource ds) {
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);
        opnameGrid.setDataSource(ds);
        opnameGrid.setFields(listGridField);
        opnameGrid.getField(DataNameTokens.INV_OPNAME_ID).setHidden(true);
        opnameGrid.getField(DataNameTokens.INV_OPNAME_SUPPLIERCODE).setHidden(true);
        opnameGrid.getField(DataNameTokens.INV_OPNAME_WAREHOUSECODE).setHidden(true);
        opnameGrid.getField(DataNameTokens.INV_OPNAME_STOCKTYPE).setHidden(true);
        opnameLayout.setMembers(warehouseSelectionForm, opnameGrid);
    }

    @Override
    public void refreshAllOpnameData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                opnameGrid.setData(response.getData());
            }
        };

        opnameGrid.getDataSource().fetchData(opnameGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public void refreshAllOpnameDetailData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                opnameDetailGrid.setData(response.getData());
            }
        };

        opnameDetailGrid.getDataSource().fetchData(opnameDetailGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public Widget asWidget() {
        return opnameLayout;
    }

    @Override
    public Window getAdjustOpnameWindow() {
        return adjustOpnameWindow;
    }

    @Override
    public ListGrid getOpnameDetailGrid(){
        return opnameDetailGrid;
    }
}