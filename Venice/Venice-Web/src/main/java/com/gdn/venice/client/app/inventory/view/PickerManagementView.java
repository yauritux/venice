/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.PickerManagementPresenter;
import com.gdn.venice.client.app.inventory.view.handler.PickerManagementUiHandler;
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
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
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
import java.util.Map;

/**
 *
 * @author Maria Olivia
 */
public class PickerManagementView extends ViewWithUiHandlers<PickerManagementUiHandler> implements
        PickerManagementPresenter.MyView {

    RafViewLayout pickerLayout;
    ListGrid pickerListGrid;
    Window pickerDetailWindow;
    LinkedHashMap<String, String> warehouseData;
    private static final int NEW_PICKER = 0;
    private static final int EXISTING_PICKER = 1;
    /*
     * The toolstrip objects for the header
     */
    ToolStrip pickerToolStrip;
    ToolStripButton addButton;

    @Inject
    public PickerManagementView() {
        pickerToolStrip = new ToolStrip();
        pickerToolStrip.setWidth100();
        pickerToolStrip.setPadding(2);

        addButton = new ToolStripButton();
        addButton.setIcon("[SKIN]/icons/add.png");
        addButton.setTooltip("Add New Picker");
        addButton.setTitle("Add Picker");

        pickerToolStrip.addButton(addButton);

        pickerLayout = new RafViewLayout();

        pickerListGrid = new ListGrid();
        pickerListGrid.setWidth100();
        pickerListGrid.setHeight100();
        pickerListGrid.setShowAllRecords(true);
        pickerListGrid.setSortField(0);
        pickerListGrid.setShowFilterEditor(true);
        pickerListGrid.setCanResizeFields(true);
        pickerListGrid.setShowRowNumbers(true);
        pickerListGrid.setCanSelectText(true);

        bindCustomUiHandlers();
    }

    private void bindCustomUiHandlers() {
        pickerListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = pickerListGrid.getSelectedRecord();
                buildPickerDetailWindow(record, EXISTING_PICKER).show();
            }
        });

        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                buildPickerDetailWindow(null, NEW_PICKER).show();
            }
        });

        pickerListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {
            @Override
            public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
                refreshAllPickerData();
            }
        });
    }

    private Window buildPickerDetailWindow(final ListGridRecord record, final int operation) {
        pickerDetailWindow = new Window();
        pickerDetailWindow.setWidth(360);
        pickerDetailWindow.setHeight(170);
        pickerDetailWindow.setShowMinimizeButton(false);
        pickerDetailWindow.setIsModal(true);
        pickerDetailWindow.setShowModalMask(true);
        pickerDetailWindow.centerInPage();

        pickerDetailWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                pickerDetailWindow.destroy();
            }
        });

        VLayout pickerDetailLayout = new VLayout();
        pickerDetailLayout.setHeight100();
        pickerDetailLayout.setWidth100();

        final DynamicForm pickerDetailForm = new DynamicForm();
        pickerDetailForm.setPadding(5);

        final TextItem pickerCode = new TextItem(DataNameTokens.INV_PICKER_CODE, "Picker Code");
        pickerCode.setDisabled(true);
        final TextItem pickerName = new TextItem(DataNameTokens.INV_PICKER_NAME, "Picker Name");
        pickerName.setRequired(Boolean.TRUE);
        pickerName.setRequiredMessage("Required field");

        final ComboBoxItem pickerWarehouse = new ComboBoxItem(DataNameTokens.INV_PICKER_WAREHOUSECODE, "Warehouse");
        pickerWarehouse.setRequired(Boolean.TRUE);
        pickerWarehouse.setRequiredMessage("Required field");
        pickerWarehouse.setValueMap(warehouseData);
        pickerWarehouse.setAddUnknownValues(false);
        pickerDetailForm.setFields(pickerCode, pickerName, pickerWarehouse);

        final HLayout buttonSet = new HLayout(5);
        final IButton closeButton = new IButton("Close");
        final IButton editButton = new IButton();

        if (operation == NEW_PICKER) {
            pickerCode.setValue("[generated after submit]");
            pickerName.setDisabled(false);
            pickerDetailWindow.setTitle("Add New Picker");
            editButton.setTitle("Save");
            buttonSet.setMembers(editButton, closeButton);
        } else {
            pickerDetailWindow.setTitle("Picker Detail");
            pickerDetailForm.setDisabled(true);
            pickerCode.setValue(record.getAttribute(DataNameTokens.INV_PICKER_CODE));
            pickerName.setValue(record.getAttribute(DataNameTokens.INV_PICKER_NAME));
            pickerName.setDisabled(true);
            pickerWarehouse.setValue(record.getAttribute(DataNameTokens.INV_PICKER_WAREHOUSECODE));
            editButton.setTitle("Edit");
            IButton nonActiveButton = new IButton("Non Active");

            nonActiveButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    getUiHandlers().nonActivePicker(record.getAttribute(DataNameTokens.INV_PICKER_ID));
                }
            });

            buttonSet.setMembers(editButton, nonActiveButton, closeButton);
        }

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                pickerDetailWindow.destroy();
            }
        });

        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (editButton.getTitle().equals("Edit")) {
                    pickerDetailForm.setDisabled(false);
                    editButton.setTitle("Save");
                    buttonSet.setMembers(editButton, closeButton);
                } else {
                    if (pickerDetailForm.validate()) {
                        HashMap<String, String> data = new HashMap<String, String>();
                        if (operation == EXISTING_PICKER) {
                            data.put(DataNameTokens.INV_PICKER_ID, record.getAttribute(DataNameTokens.INV_PICKER_ID));
                            data.put(DataNameTokens.INV_PICKER_CODE, record.getAttribute(DataNameTokens.INV_PICKER_CODE));
                        }
                        data.put(DataNameTokens.INV_PICKER_NAME, pickerName.getValueAsString());
                        data.put(DataNameTokens.INV_PICKER_WAREHOUSECODE, pickerWarehouse.getValueAsString());
                        getUiHandlers().saveOrUpdatePickerData(data);
                    }
                }
            }
        });

        buttonSet.setAlign(Alignment.CENTER);

        pickerDetailLayout.setMembers(pickerDetailForm, buttonSet);
        pickerDetailWindow.addItem(pickerDetailLayout);

        return pickerDetailWindow;
    }

    @Override
    public void loadAllPickerData(DataSource dataSource) {
        Map<String, String> activeMap = new HashMap<String, String>();
        activeMap.put("true", "Active");
        activeMap.put("false", "Non Active");
        dataSource.getField(DataNameTokens.INV_PICKER_STATUS).setValueMap(activeMap);

        pickerListGrid.setDataSource(dataSource);
        pickerListGrid.setAutoFetchData(true);
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(dataSource);
        pickerListGrid.setFields(listGridField);
        pickerListGrid.setDataSource(dataSource);
        pickerListGrid.getField(DataNameTokens.INV_PICKER_ID).setHidden(true);
        pickerListGrid.getField(DataNameTokens.INV_PICKER_WAREHOUSECODE).setHidden(true);
        pickerListGrid.setAutoFitData(Autofit.BOTH);

        pickerLayout.setMembers(pickerToolStrip, pickerListGrid);
    }

    @Override
    public void refreshAllPickerData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                pickerListGrid.setData(response.getData());
            }
        };

        pickerListGrid.getDataSource().fetchData(pickerListGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public Widget asWidget() {
        return pickerLayout;
    }

    @Override
    public Window getDetailWindow() {
        return pickerDetailWindow;
    }

    @Override
    public void setPickerWarehouseData(LinkedHashMap<String, String> data) {
        warehouseData = data;
    }
}