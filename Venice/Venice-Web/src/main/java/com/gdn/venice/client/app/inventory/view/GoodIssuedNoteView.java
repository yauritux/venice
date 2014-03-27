/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.GINData;
import com.gdn.venice.client.app.inventory.data.PackingListData;
import com.gdn.venice.client.app.inventory.presenter.GoodIssuedNotePresenter;
import com.gdn.venice.client.app.inventory.view.handler.GoodIssuedNoteUiHandler;
import com.gdn.venice.client.data.RafDataSource;
import com.gdn.venice.client.presenter.MainPagePresenter;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.util.EventHandler;
import com.smartgwt.client.util.SC;
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
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 *
 * @author Maria Olivia
 */
public class GoodIssuedNoteView extends ViewWithUiHandlers<GoodIssuedNoteUiHandler> implements
        GoodIssuedNotePresenter.MyView {

    private static final int OPS_DETAIL = 0,
            OPS_ADD = 1;
    private int recordNumber;
    RafViewLayout packingListLayout;
    ListGrid ginGrid, awbGinGrid;
    Window ginDetailWindow;
    DynamicForm warehouseSelectionForm;
    ComboBoxItem cbWarehouse;
    ToolStrip toolstrip;
    ToolStripButton addButton;
    private LinkedHashMap<String, String> logisticMap;

    @Inject
    public GoodIssuedNoteView() {
        packingListLayout = new RafViewLayout();
        toolstrip = new ToolStrip();
        toolstrip.setWidth100();

        addButton = new ToolStripButton();
        addButton.setIcon("[SKIN]/icons/add.png");
        addButton.setTitle("Add");
        toolstrip.addButton(addButton);

        ginGrid = new ListGrid();
        ginGrid.setWidth100();
        ginGrid.setHeight100();
        ginGrid.setShowAllRecords(true);
        ginGrid.setSortField(0);
        ginGrid.setShowFilterEditor(true);
        ginGrid.setShowRowNumbers(true);
        ginGrid.setAutoFetchData(Boolean.TRUE);
        ginGrid.setAutoFitData(Autofit.BOTH);

        bindCustomUiHandlers();
    }

    private void bindCustomUiHandlers() {
        ginGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = ginGrid.getSelectedRecord();
                buildGinDetailWindow(record, OPS_DETAIL).show();
            }
        });

        ginGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {
            @Override
            public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
                refreshAllGinListData();
            }
        });

        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (cbWarehouse.getValue() == null
                        || cbWarehouse.getValueAsString().isEmpty()) {
                    SC.warn("Please select warehouse first");
                } else {
                    buildGinDetailWindow(null, OPS_ADD).show();
                }
            }
        });
    }

    private Window buildGinDetailWindow(final ListGridRecord record, final int operation) {
        awbGinGrid = new ListGrid();
        awbGinGrid.setWidth100();
        awbGinGrid.setHeight100();
        awbGinGrid.setShowAllRecords(true);
        awbGinGrid.setSortField(0);
        awbGinGrid.setShowFilterEditor(false);
        awbGinGrid.setShowRowNumbers(true);

        setRecordNumber(0);
        ginDetailWindow = new Window();
        ginDetailWindow.setWidth(600);
        ginDetailWindow.setHeight(450);
        ginDetailWindow.setCanDragResize(true);
        ginDetailWindow.setShowMinimizeButton(false);
        ginDetailWindow.setIsModal(true);
        ginDetailWindow.setShowModalMask(true);
        ginDetailWindow.centerInPage();

        ToolStrip packingToolStrip = new ToolStrip();
        packingToolStrip.setWidth100();

        ToolStripButton submitButton = new ToolStripButton();
        submitButton.setIcon("[SKIN]/icons/process.png");
        submitButton.setTitle("Submit");

        ToolStripButton printGinButton = new ToolStripButton();
        printGinButton.setIcon("[SKIN]/icons/printer.png");
        printGinButton.setTitle("Print");

        ToolStripButton closeButton = new ToolStripButton();
        closeButton.setTitle("Close");

        VLayout packingDetailLayout = new VLayout();
        packingDetailLayout.setHeight100();
        packingDetailLayout.setWidth100();

        final DynamicForm packingInfoForm = new DynamicForm();
        packingInfoForm.setWidth100();
        packingInfoForm.setPadding(5);

        TextItem ginNo = new TextItem(DataNameTokens.INV_GIN_NO, "GIN No");
        ginNo.setDisabled(true);
        TextItem ginDate = new TextItem(DataNameTokens.INV_GIN_DATE, "GIN Date");
        ginDate.setDisabled(true);
        final TextItem whCode = new TextItem(DataNameTokens.INV_GIN_WAREHOUSECODE, "Warehouse Code");
        whCode.setValue(cbWarehouse.getValueAsString());
        whCode.setDisabled(true);
        TextItem whName = new TextItem(DataNameTokens.INV_GIN_WAREHOUSENAME, "Warehouse Name");
        whName.setValue(cbWarehouse.getDisplayValue());
        whName.setDisabled(true);
        final TextItem spcNote = new TextItem(DataNameTokens.INV_GIN_NOTE, "Special Note");
        final ComboBoxItem logistic = new ComboBoxItem(DataNameTokens.INV_GIN_LOGISTIC, "Logistic Name");

        packingInfoForm.setFields(ginNo, ginDate, whCode, whName, spcNote, logistic);

        String username = MainPagePresenter.signedInUser == null
                || MainPagePresenter.signedInUser.trim().isEmpty()
                ? "olive" : MainPagePresenter.signedInUser;

        if (operation == OPS_DETAIL) {
            ginDetailWindow.setTitle("GIN Detail");
            ginNo.setValue(record.getAttribute(DataNameTokens.INV_GIN_NO));
            ginDate.setValue(record.getAttribute(DataNameTokens.INV_GIN_DATE));
            spcNote.setValue(record.getAttribute(DataNameTokens.INV_GIN_NOTE));
            spcNote.setDisabled(true);
            logistic.setValue(record.getAttribute(DataNameTokens.INV_GIN_LOGISTIC));
            logistic.setDisabled(true);
            packingToolStrip.addButton(printGinButton);
            DataSource ds = GINData.getAwbListData(record.getAttribute(DataNameTokens.INV_GIN_ID));
            ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);

            awbGinGrid.setDataSource(ds);
            awbGinGrid.setFields(listGridField);
            refreshAwbGinListData();
        } else {
            ginDetailWindow.setTitle("Add New GIN");
            ginNo.setValue("[generated by system]");
            ginDate.setValue(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT).format(new Date()));
            spcNote.setDisabled(false);
            logistic.setValueMap(logisticMap);
            logistic.setDisabled(false);
            packingToolStrip.addButton(submitButton);
            DataSourceField[] dsfNew = {new DataSourceTextField(DataNameTokens.INV_GIN_AWB_NO, "AWB NO")};
            RafDataSource dsNew = new RafDataSource(
                    "/response/data/*",
                    null,
                    null,
                    null,
                    null,
                    dsfNew);

            ListGridField lgsNew[] = Util.getListGridFieldsFromDataSource(dsNew);

            awbGinGrid.setDataSource(dsNew);
            awbGinGrid.setFields(lgsNew);
        }
        awbGinGrid.setAutoFitData(Autofit.BOTH);

        awbGinGrid.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (operation == OPS_ADD) {
                    if (logistic.getValue() == null
                            || logistic.getValueAsString().isEmpty()) {
                        SC.warn("Please select Logistic first");
                    } else {
                        if (EventHandler.getKey().equalsIgnoreCase("Enter")) {
                            if (recordNumber > 0) {
                                if (awbGinGrid.getEditValueAsString(recordNumber - 1, DataNameTokens.INV_GIN_AWB_NO) != null
                                        && !awbGinGrid.getEditValueAsString(recordNumber - 1, DataNameTokens.INV_GIN_AWB_NO).isEmpty()) {
                                    getUiHandlers().onEditAwbNumberCompleted(awbGinGrid.getEditValueAsString(recordNumber - 1, DataNameTokens.INV_GIN_AWB_NO),
                                            logistic.getDisplayValue(), cbWarehouse.getValueAsString(), recordNumber);
                                } else {
                                    awbGinGrid.removeData(awbGinGrid.getEditedRecord(recordNumber));
                                    SC.warn("Please fill AWB Number");
                                }
                            } else {
                                recordNumber++;
                                awbGinGrid.startEditingNew();
                            }
                        }
                    }
                }
            }
        });

        packingToolStrip.addSeparator();
        packingToolStrip.addButton(closeButton);

        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HashMap<String, String> data = new HashMap<String, String>();
                data.put(DataNameTokens.INV_GIN_WAREHOUSECODE, whCode.getValueAsString());
                data.put(DataNameTokens.INV_GIN_LOGISTIC, logistic.getDisplayValue());
                data.put(DataNameTokens.INV_GIN_NOTE, spcNote.getValueAsString());

                StringBuilder sb = new StringBuilder();
                for (int r = 0; r < recordNumber; r++) {
                    if (awbGinGrid.getEditValueAsString(r, DataNameTokens.INV_GIN_AWB_NO) != null
                            && !awbGinGrid.getEditValueAsString(r, DataNameTokens.INV_GIN_AWB_NO).isEmpty()) {
                        if(!sb.toString().isEmpty()){
                            sb.append(";");
                        }
                        sb.append(awbGinGrid.getEditValueAsString(r, DataNameTokens.INV_GIN_AWB_NO));
                    }
                }
                if(!sb.toString().isEmpty()){
                    data.put(DataNameTokens.INV_GIN_AWB_NO, sb.toString());
                    getUiHandlers().onSaveGin(data);
                } else {
                    SC.say("Should contain at least 1 valid awb number");
                }
            }
        });

        printGinButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            }
        });

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ginDetailWindow.destroy();
            }
        });

        ginDetailWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                awbGinGrid.clear();
                ginDetailWindow.destroy();
            }
        });
        packingDetailLayout.setMembers(packingToolStrip, packingInfoForm, awbGinGrid);
        ginDetailWindow.addItem(packingDetailLayout);

        return ginDetailWindow;
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
                loadGinData(cbWarehouse.getValueAsString());
            }
        });

        packingListLayout.setMembers(toolstrip, warehouseSelectionForm);
    }

    public void loadGinData(String warehouseCode) {
        DataSource ds = GINData.getAllGINData(warehouseCode, 1, 20);
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);

        ginGrid.setDataSource(ds);
        ginGrid.setFields(listGridField);
        ginGrid.getField(DataNameTokens.INV_GIN_ID).setHidden(Boolean.TRUE);
        ginGrid.getField(DataNameTokens.INV_GIN_NOTE).setHidden(Boolean.TRUE);

        refreshAllGinListData();
        packingListLayout.setMembers(toolstrip, warehouseSelectionForm, ginGrid);
    }

    @Override
    public void refreshAllGinListData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                ginGrid.setData(response.getData());
            }
        };

        ginGrid.getDataSource().fetchData(ginGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public void refreshAwbGinListData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                awbGinGrid.setData(response.getData());
            }
        };

        awbGinGrid.getDataSource().fetchData(awbGinGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public Widget asWidget() {
        return packingListLayout;
    }

    @Override
    public Window getGinDetailWindow() {
        return ginDetailWindow;
    }

    @Override
    public void setLogisticMap(LinkedHashMap<String, String> logisticMap) {
        this.logisticMap = logisticMap;
    }

    @Override
    public ListGrid getAwbGinGrid() {
        return awbGinGrid;
    }

    @Override
    public void setRecordNumber(int recordNumber) {
        this.recordNumber = recordNumber;
    }
}