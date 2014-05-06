/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.OpnameData;
import com.gdn.venice.client.app.inventory.presenter.OpnamePresenter;
import com.gdn.venice.client.app.inventory.view.handler.OpnameUiHandler;
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
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.TitleOrientation;
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
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitEvent;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 *
 * @author Maria Olivia
 */
public class OpnameView extends ViewWithUiHandlers<OpnameUiHandler> implements
        OpnamePresenter.MyView {

    RafViewLayout opnameLayout;
    ListGrid opnameGrid, newOpnameGrid, supplierGrid;
    Window createOpnameWindow;
    DynamicForm warehouseSelectionForm;
    ComboBoxItem cbWarehouse, cbInventoryType;
    SelectItem cbSupplier;
    ToolStrip toolstrip;
    ToolStripButton processButton;
    private LinkedHashMap<String, String> categoryMap, uomMap;

    @Inject
    public OpnameView() {
        opnameLayout = new RafViewLayout();
        toolstrip = new ToolStrip();
        toolstrip.setWidth100();

        processButton = new ToolStripButton();
        processButton.setIcon("[SKIN]/icons/process.png");
        processButton.setTitle("Process");
        toolstrip.addButton(processButton);

        opnameGrid = new ListGrid();
        opnameGrid.setWidth100();
        opnameGrid.setHeight100();
        opnameGrid.setShowAllRecords(true);
        opnameGrid.setSortField(0);
        opnameGrid.setShowFilterEditor(true);
        opnameGrid.setShowRowNumbers(true);
        opnameGrid.setAutoFitData(Autofit.BOTH);
        opnameGrid.setCanSelectText(true);
        opnameGrid.setSelectionType(SelectionStyle.SIMPLE);
        opnameGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);

        newOpnameGrid = new ListGrid();
        newOpnameGrid.setWidth100();
        newOpnameGrid.setHeight100();
        newOpnameGrid.setShowAllRecords(true);
        newOpnameGrid.setSortField(0);
        newOpnameGrid.setShowFilterEditor(false);
        newOpnameGrid.setShowRowNumbers(true);
        newOpnameGrid.setCanSelectText(true);
        newOpnameGrid.setAutoFetchData(true);

        supplierGrid = new ListGrid();
        supplierGrid.setWidth100();
        supplierGrid.setSortField(1);
        supplierGrid.setShowFilterEditor(true);
        supplierGrid.setAutoFetchData(true);
        supplierGrid.setCanSelectText(true);

        bindCustomUiHandlers();
    }

    private void bindCustomUiHandlers() {
        opnameGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {
            @Override
            public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
                refreshAllItemStorageData();
            }
        });

        supplierGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {
            @Override
            public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
                refreshAllSupplierData();
            }
        });

        processButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (cbWarehouse.getValue() == null
                        || cbWarehouse.getValueAsString().isEmpty()) {
                    SC.warn("Please select warehouse first");
                } else {
                    ListGridRecord[] records = opnameGrid.getSelection();
                    if (records.length > 0) {
                        buildCreateOpnameWindow(records).show();
                    } else {
                        SC.warn("Please select at leat one record");
                    }
                }
            }
        });
    }

    private Window buildCreateOpnameWindow(final ListGridRecord[] records) {
        createOpnameWindow = new Window();
        createOpnameWindow.setWidth(600);
        createOpnameWindow.setHeight(450);
        createOpnameWindow.setCanDragResize(true);
        createOpnameWindow.setShowMinimizeButton(false);
        createOpnameWindow.setIsModal(true);
        createOpnameWindow.setShowModalMask(true);
        createOpnameWindow.centerInPage();
        createOpnameWindow.setTitle("Create Stock Opname List");

        ToolStrip opnameToolStrip = new ToolStrip();
        opnameToolStrip.setWidth100();

        ToolStripButton submitButton = new ToolStripButton();
        submitButton.setIcon("[SKIN]/icons/process.png");
        submitButton.setTitle("Submit & Print");

        ToolStripButton closeButton = new ToolStripButton();
        closeButton.setTitle("Close");

        VLayout packingDetailLayout = new VLayout();
        packingDetailLayout.setHeight100();
        packingDetailLayout.setWidth100();

        final DynamicForm opnameForm = new DynamicForm();
        opnameForm.setWidth100();
        opnameForm.setPadding(5);

        TextItem opnameNo = new TextItem(DataNameTokens.INV_OPNAME_NO, "Opname No");
        opnameNo.setDisabled(true);
        opnameNo.setValue("[generated by system]");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < records.length; i++) {
            if (!sb.toString().isEmpty()) {
                sb.append(";");
            }
            sb.append(records[i].getAttribute(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID));
        }
        DataSource ds = OpnameData.getItemStorageDataById(sb.toString());

        newOpnameGrid.setDataSource(ds);
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);
        newOpnameGrid.setFields(listGridField);
        newOpnameGrid.setAutoFitData(Autofit.BOTH);
        newOpnameGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID).setHidden(true);

        opnameToolStrip.addSeparator();
        opnameToolStrip.setMembers(submitButton, closeButton);

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < records.length; i++) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(";");
                    }
                    sb.append(records[i].getAttribute(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID));
                }
                getUiHandlers().onSubmitButton(sb.toString(), cbWarehouse.getValueAsString(), cbInventoryType.getValueAsString(), cbSupplier.getValueAsString());
            }
        });

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createOpnameWindow.destroy();
            }
        });

        createOpnameWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                createOpnameWindow.destroy();
            }
        });
        packingDetailLayout.setMembers(opnameToolStrip, opnameForm, newOpnameGrid);
        createOpnameWindow.addItem(packingDetailLayout);

        return createOpnameWindow;
    }

    @Override
    public void loadComboboxData(LinkedHashMap<String, String> warehouse) {
        cbInventoryType = new ComboBoxItem("inventory", "Inventory Type");
        cbWarehouse = new ComboBoxItem("warehouse", "Select warehouse");
        cbWarehouse.setValueMap(warehouse);
        Iterator<String> itr = warehouse.keySet().iterator();
        if (itr.hasNext()) {
            cbWarehouse.setValue(itr.next());

            LinkedHashMap<String, String> inventoryType = new LinkedHashMap<String, String>();
            inventoryType.put("TRADING", "Trading");
            inventoryType.put("CONSIGNMENT_COMMISION", "Commission Consignment");
            inventoryType.put("CONSIGNMENT_TRADING", "Trading Consignment");
            cbInventoryType.setValueMap(inventoryType);
        }

        cbSupplier = new SelectItem("supplier", "Supplier");
        cbSupplier.setDisplayField(DataNameTokens.INV_OPNAME_SUPPLIERNAME);
        cbSupplier.setValueField(DataNameTokens.INV_OPNAME_SUPPLIERCODE);
        cbSupplier.setPickListWidth(300);

        warehouseSelectionForm = new DynamicForm();
        warehouseSelectionForm.setWidth100();
        warehouseSelectionForm.setPadding(20);
        warehouseSelectionForm.setFields(cbWarehouse, cbInventoryType, cbSupplier);
        warehouseSelectionForm.setTitleOrientation(TitleOrientation.TOP);
        warehouseSelectionForm.setNumCols(1);

        cbWarehouse.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent event) {
                if (cbWarehouse.getValueAsString() != null && !cbWarehouse.getValueAsString().isEmpty()) {
                    if (cbInventoryType.getValueAsString() != null && !cbInventoryType.getValueAsString().isEmpty()) {
                        if (cbInventoryType.getValueAsString().equals("TRADING")) {
                            loadItemStorageData(cbWarehouse.getValueAsString(), cbInventoryType.getValueAsString(), null);
                            cbSupplier.setValue("");
                            cbSupplier.setDisabled(true);
                        } else {
                            loadSupplierData(cbWarehouse.getValueAsString(), cbInventoryType.getValueAsString());
                        }
                    } else {
                        cbSupplier.setValue("");
                        cbSupplier.setDisabled(true);
                    }
                    cbInventoryType.setDisabled(false);
                } else {
                    cbInventoryType.setValue("");
                    cbInventoryType.setDisabled(true);
                }
            }
        });

        cbInventoryType.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent event) {
                if (cbWarehouse.getValueAsString() != null && !cbWarehouse.getValueAsString().isEmpty()) {
                    if (cbInventoryType.getValueAsString().equals("TRADING")) {
                        loadItemStorageData(cbWarehouse.getValueAsString(), cbInventoryType.getValueAsString(), null);
                        cbSupplier.setValue("");
                        cbSupplier.setDisabled(true);
                    } else {
                        loadSupplierData(cbWarehouse.getValueAsString(), cbInventoryType.getValueAsString());
                    }
                }
            }
        });

        cbSupplier.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent event) {
                if (cbSupplier.getValueAsString() != null && !cbSupplier.getValueAsString().isEmpty()) {
                    loadItemStorageData(cbWarehouse.getValueAsString(), cbInventoryType.getValueAsString(), cbSupplier.getValueAsString());
                }
            }
        });

        opnameLayout.setMembers(warehouseSelectionForm, toolstrip);
    }

    public void loadItemStorageData(String warehouseCode, String stockType, String supplierCode) {
        DataSource ds = OpnameData.getAllItemStorageData(warehouseCode, stockType, supplierCode);
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);
        ds.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMCATEGORY).setValueMap(categoryMap);
        ds.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMUOM).setValueMap(uomMap);
        
        opnameGrid.setDataSource(ds);
        opnameGrid.setFields(listGridField);
        opnameGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID).setHidden(true);
        opnameGrid.getField(DataNameTokens.INV_OPNAME_ITEMSTORAGE_QTY).setCanFilter(false);
        refreshAllItemStorageData();
        opnameLayout.setMembers(warehouseSelectionForm, toolstrip, opnameGrid);
    }

    @Override
    public void refreshAllItemStorageData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                opnameGrid.setData(response.getData());
            }
        };

        opnameGrid.getDataSource().fetchData(opnameGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public Widget asWidget() {
        return opnameLayout;
    }

    @Override
    public Window getCreateOpnameWindow() {
        return createOpnameWindow;
    }

    private void loadSupplierData(String warehouseCode, String stockType) {
        cbSupplier.setValue("");
        DataSource ds = OpnameData.getSupplierData(warehouseCode, stockType);
        cbSupplier.setOptionDataSource(ds);
        cbSupplier.setPickListFields(Util.getListGridFieldsFromDataSource(ds));
        cbSupplier.setPickListProperties(supplierGrid);
        cbSupplier.setDisabled(false);
    }

    @Override
    public void refreshAllSupplierData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                supplierGrid.setData(response.getData());
            }
        };

        supplierGrid.getDataSource().fetchData(supplierGrid.getFilterEditorCriteria(), callBack);
    }

    /**
     * @param categoryMap the categoryMap to set
     */
    @Override
    public void setCategoryMap(LinkedHashMap<String, String> categoryMap) {
        this.categoryMap = categoryMap;
    }

    /**
     * @param uomMap the uomMap to set
     */
    @Override
    public void setUomMap(LinkedHashMap<String, String> uomMap) {
        this.uomMap = uomMap;
    }
}