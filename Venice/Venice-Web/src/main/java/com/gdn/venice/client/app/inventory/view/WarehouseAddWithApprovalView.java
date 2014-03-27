/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;
import java.util.Map;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.WarehouseAddWithApprovalPresenter;
import com.gdn.venice.client.app.inventory.view.handler.WarehouseAddWithApprovalUiHandler;
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
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
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

/**
 *
 * @author Maria Olivia
 */
public class WarehouseAddWithApprovalView extends ViewWithUiHandlers<WarehouseAddWithApprovalUiHandler> implements
        WarehouseAddWithApprovalPresenter.MyView {

    RafViewLayout warehouseApprovalAddLayout;
    ListGrid warehouseListGrid;
    Window warehouseDetailWindow;
    /*
     * The toolstrip objects for the header
     */
    ToolStrip warehouseListToolStrip;

    @Inject
    public WarehouseAddWithApprovalView() {
        warehouseListToolStrip = new ToolStrip();
        warehouseListToolStrip.setWidth100();
        warehouseListToolStrip.setPadding(2);

        warehouseApprovalAddLayout = new RafViewLayout();

        warehouseListGrid = new ListGrid();
        warehouseListGrid.setWidth100();
        warehouseListGrid.setHeight100();
        warehouseListGrid.setShowAllRecords(true);
        warehouseListGrid.setSortField(0);

        warehouseListGrid.setShowFilterEditor(true);
        warehouseListGrid.setCanResizeFields(true);
        warehouseListGrid.setShowRowNumbers(true);

        bindCustomUiHandlers();
    }

    protected void bindCustomUiHandlers() {
        warehouseListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = warehouseListGrid.getSelectedRecord();
                buildWarehouseDetailWindow(record).show();
            }
        });

        warehouseListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {
            @Override
            public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
                refreshAllWarehouseData();
            }
        });
    }

    private Window buildWarehouseDetailWindow(final ListGridRecord record) {
        warehouseDetailWindow = new Window();
        warehouseDetailWindow.setWidth(600);
        warehouseDetailWindow.setHeight(450);
        warehouseDetailWindow.setCanDragResize(true);
        warehouseDetailWindow.setTitle("Warehouse Detail");
        warehouseDetailWindow.setShowMinimizeButton(false);
        warehouseDetailWindow.setIsModal(true);
        warehouseDetailWindow.setShowModalMask(true);
        warehouseDetailWindow.centerInPage();

        warehouseDetailWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                warehouseDetailWindow.destroy();
            }
        });

        VLayout warehouseDetailLayout = new VLayout();
        warehouseDetailLayout.setHeight100();
        warehouseDetailLayout.setWidth100();

        final DynamicForm warehouseDetailForm = new DynamicForm();
        warehouseDetailForm.setWidth100();
        warehouseDetailForm.setPadding(5);

        final Long id = Long.parseLong(record.getAttribute(DataNameTokens.INV_WAREHOUSE_ID));

        DataSource ds = new DataSource();
        DataSourceTextField whName = new DataSourceTextField("whName", "Warehouse Name", 255, true);
        DataSourceTextField whDescription = new DataSourceTextField("whDescription", "Warehouse Description", 1000);
        DataSourceTextField whAddress = new DataSourceTextField("whAddress", "Address", 255, true);
        DataSourceTextField whCity = new DataSourceTextField("whCity", "City", 255, true);
        DataSourceIntegerField whZipcode = new DataSourceIntegerField("whZipcode", "Zipcode", 5);
        DataSourceTextField whContactPerson = new DataSourceTextField("whContactPerson", "Contact Person", 255);
        DataSourceIntegerField whContactPhone = new DataSourceIntegerField("whContactPhone", "Contact Phone", 16);
        DataSourceFloatField whSpace = new DataSourceFloatField("whSpace", "Space", 10);
        DataSourceFloatField whAvailSpace = new DataSourceFloatField("whAvailSpace", "Available Space", 10);

        ds.setFields(whName, whDescription, whAddress, whCity, whZipcode, whContactPerson, whContactPhone, whSpace, whAvailSpace);
        warehouseDetailForm.setDataSource(ds);
        warehouseDetailForm.setDisabled(true);

        warehouseDetailForm.setValue("whName", record.getAttribute(DataNameTokens.INV_WAREHOUSE_NAME));
        warehouseDetailForm.setValue("whDescription", record.getAttribute(DataNameTokens.INV_WAREHOUSE_DESCRIPTION));
        warehouseDetailForm.setValue("whAddress", record.getAttribute(DataNameTokens.INV_WAREHOUSE_ADDRESS));
        warehouseDetailForm.setValue("whCity", record.getAttribute(DataNameTokens.INV_WAREHOUSE_CITY));
        warehouseDetailForm.setValue("whZipcode", record.getAttribute(DataNameTokens.INV_WAREHOUSE_ZIPCODE));
        warehouseDetailForm.setValue("whContactPerson", record.getAttribute(DataNameTokens.INV_WAREHOUSE_CONTACT_PERSON));
        warehouseDetailForm.setValue("whContactPhone", record.getAttribute(DataNameTokens.INV_WAREHOUSE_CONTACT_PHONE));
        warehouseDetailForm.setValue("whSpace", record.getAttribute(DataNameTokens.INV_WAREHOUSE_SPACE));
        warehouseDetailForm.setValue("whAvailSpace", record.getAttribute(DataNameTokens.INV_WAREHOUSE_AVAILABLE_SPACE));

        HLayout buttonSet = new HLayout(5);

        IButton closeButton = new IButton("Close");
        final IButton editButton = new IButton("Edit");
        IButton correctionButton = new IButton("Need Correction");
        IButton approveButton = new IButton("Approve");
        IButton rejectButton = new IButton("Reject");

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                warehouseDetailWindow.destroy();
            }
        });

        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (editButton.getTitle().equals("Edit")) {
                    warehouseDetailForm.setDisabled(false);
                    editButton.setTitle("Save");
                } else {
                    if (warehouseDetailForm.validate()) {
                        HashMap<String, String> data = new HashMap<String, String>();
                        data.put(DataNameTokens.INV_WAREHOUSE_ID, id.toString());
                        data.put(DataNameTokens.INV_WAREHOUSE_NAME, warehouseDetailForm.getValue("whName").toString());
                        data.put(DataNameTokens.INV_WAREHOUSE_DESCRIPTION, warehouseDetailForm.getValue("whDescription").toString());
                        data.put(DataNameTokens.INV_WAREHOUSE_ADDRESS, warehouseDetailForm.getValue("whAddress").toString());
                        data.put(DataNameTokens.INV_WAREHOUSE_CITY, warehouseDetailForm.getValue("whCity").toString());
                        data.put(DataNameTokens.INV_WAREHOUSE_ZIPCODE, warehouseDetailForm.getValue("whZipcode").toString());
                        data.put(DataNameTokens.INV_WAREHOUSE_CONTACT_PERSON, warehouseDetailForm.getValue("whContactPerson").toString());
                        data.put(DataNameTokens.INV_WAREHOUSE_CONTACT_PHONE, warehouseDetailForm.getValue("whContactPhone").toString());
                        data.put(DataNameTokens.INV_WAREHOUSE_SPACE, warehouseDetailForm.getValue("whSpace").toString());
                        data.put(DataNameTokens.INV_WAREHOUSE_AVAILABLE_SPACE, warehouseDetailForm.getValue("whAvailSpace").toString());

                        getUiHandlers().updateWarehouseWIPData(MainPagePresenter.signedInUser, data);
                    }
                }
            }
        });

        approveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HashMap<String, String> data = new HashMap<String, String>();
                data.put(DataNameTokens.INV_WAREHOUSE_ID, id.toString());
                data.put(DataNameTokens.INV_WAREHOUSE_APPROVALSTATUS, "APPROVED");

                getUiHandlers().updateWarehouseWIPData(MainPagePresenter.signedInUser, data);
            }
        });

        correctionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HashMap<String, String> data = new HashMap<String, String>();
                data.put(DataNameTokens.INV_WAREHOUSE_ID, id.toString());
                data.put(DataNameTokens.INV_WAREHOUSE_APPROVALSTATUS, "NEED_CORRECTION");

                getUiHandlers().updateWarehouseWIPData(MainPagePresenter.signedInUser, data);
            }
        });

        rejectButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HashMap<String, String> data = new HashMap<String, String>();
                data.put(DataNameTokens.INV_WAREHOUSE_ID, id.toString());
                data.put(DataNameTokens.INV_WAREHOUSE_APPROVALSTATUS, "REJECTED");

                getUiHandlers().updateWarehouseWIPData(MainPagePresenter.signedInUser, data);
            }
        });

        buttonSet.setAlign(Alignment.CENTER);
        buttonSet.addMember(closeButton);
        if (!record.getAttribute(DataNameTokens.INV_WAREHOUSE_APPROVALSTATUS).equals("Need Correction") 
                && MainPagePresenter.getSignedInUserRole().toLowerCase().contains("inv_wh_approver")) {
            buttonSet.addMember(approveButton);
            buttonSet.addMember(correctionButton);
            buttonSet.addMember(rejectButton);
        } else if (record.getAttribute(DataNameTokens.INV_WAREHOUSE_APPROVALSTATUS).equals("Need Correction")) {
            buttonSet.addMember(editButton);
        }

        warehouseDetailLayout.setMembers(warehouseDetailForm, buttonSet);
        warehouseDetailWindow.addItem(warehouseDetailLayout);

        return warehouseDetailWindow;
    }

    @Override
    public void loadApprovalAddWarehouseData(DataSource dataSource) {
        Map<String, String> status = new HashMap<String, String>();
        status.put("CREATED", "New");
        status.put("APPROVED", "Approved");
        status.put("NEED_CORRECTION", "Need Correction");
        status.put("REJECTED", "Rejected");
        dataSource.getField(DataNameTokens.INV_WAREHOUSE_APPROVALSTATUS).setValueMap(status);

        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(dataSource);

        warehouseListGrid.setDataSource(dataSource);
        warehouseListGrid.setAutoFetchData(Boolean.TRUE);
        warehouseListGrid.setFields(listGridField);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_ID).setHidden(Boolean.TRUE);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_CODE).setHidden(Boolean.TRUE);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_ADDRESS).setCanFilter(Boolean.FALSE);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_CITY).setCanFilter(Boolean.FALSE);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_DESCRIPTION).setCanFilter(Boolean.FALSE);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_ZIPCODE).setCanFilter(Boolean.FALSE);
        warehouseListGrid.setAutoFitData(Autofit.BOTH);

        warehouseApprovalAddLayout.setMembers(warehouseListToolStrip, warehouseListGrid);
        MainPagePresenter.getSignedInUserRole();
//        SC.say("Role: " +MainPagePresenter.getSignedInUserRole().toLowerCase());
    }

    @Override
    public void refreshAllWarehouseData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                warehouseListGrid.setData(response.getData());
            }
        };

        warehouseListGrid.getDataSource().fetchData(warehouseListGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public Widget asWidget() {
        return warehouseApprovalAddLayout;
    }

    @Override
    public Window getWarehouseDetailWindow() {
        return warehouseDetailWindow;
    }
}