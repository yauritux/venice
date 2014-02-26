/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;
import java.util.Map;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.WarehouseListFilterPresenter;
import com.gdn.venice.client.app.inventory.view.handler.WarehouseListFilterUiHandler;
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
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextAreaItem;
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

/**
 *
 * @author Maria Olivia
 */
public class WarehouseListFilterView extends ViewWithUiHandlers<WarehouseListFilterUiHandler> implements
        WarehouseListFilterPresenter.MyView {

    RafViewLayout warehouseListFilterLayout;
    ListGrid warehouseListGrid;
    Window warehouseDetailWindow, addWarehouseWindow;
    /*
     * The toolstrip objects for the header
     */
    ToolStrip warehouseListToolStrip;
    ToolStripButton addButton;

    @Inject
    public WarehouseListFilterView() {
        warehouseListToolStrip = new ToolStrip();
        warehouseListToolStrip.setWidth100();
        warehouseListToolStrip.setPadding(2);

        addButton = new ToolStripButton();
        addButton.setIcon("[SKIN]/icons/add.png");
        addButton.setTooltip("Add New Warehouse");
        addButton.setTitle("Add Warehouse");

        warehouseListToolStrip.addButton(addButton);

        warehouseListFilterLayout = new RafViewLayout();

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

    private void bindCustomUiHandlers() {
        warehouseListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = warehouseListGrid.getSelectedRecord();
                buildWarehouseDetailWindow(record).show();
            }
        });

        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                buildAddWarehouseWindow().show();
            }
        });
        
        warehouseListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {
			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				// TODO Auto-generated method stub
				refreshAllWarehouseData();
			}
		});
    }

    private Window buildWarehouseDetailWindow(final ListGridRecord record) {
        warehouseDetailWindow = new Window();
        warehouseDetailWindow.setWidth(360);
        warehouseDetailWindow.setHeight(170);
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
        warehouseDetailForm.setPadding(5);

        final String id = record.getAttribute(DataNameTokens.INV_WAREHOUSE_ID);

        String errMsg = "Required field";
        final TextItem whCode = new TextItem(DataNameTokens.INV_WAREHOUSE_CODE, "Warehouse Code");
        whCode.setValue(record.getAttribute(DataNameTokens.INV_WAREHOUSE_CODE));
        whCode.setDisabled(Boolean.TRUE);
        final TextItem whName = new TextItem(DataNameTokens.INV_WAREHOUSE_NAME, "Warehouse Name");
        whName.setValue(record.getAttribute(DataNameTokens.INV_WAREHOUSE_NAME));
        whName.setRequired(Boolean.TRUE);
        whName.setRequiredMessage(errMsg);
        final TextAreaItem whDescription = new TextAreaItem(DataNameTokens.INV_WAREHOUSE_DESCRIPTION, "Warehouse Description");
        whDescription.setValue(record.getAttribute(DataNameTokens.INV_WAREHOUSE_DESCRIPTION));
        final TextItem whAddress = new TextItem(DataNameTokens.INV_WAREHOUSE_ADDRESS, "Address");
        whAddress.setValue(record.getAttribute(DataNameTokens.INV_WAREHOUSE_ADDRESS));
        whAddress.setRequired(Boolean.TRUE);
        whAddress.setRequiredMessage(errMsg);
        final TextItem whCity = new TextItem(DataNameTokens.INV_WAREHOUSE_CITY, "City");
        whCity.setValue(record.getAttribute(DataNameTokens.INV_WAREHOUSE_CITY));
        whCity.setRequired(Boolean.TRUE);
        whCity.setRequiredMessage(errMsg);
        final TextItem whZipcode = new TextItem(DataNameTokens.INV_WAREHOUSE_ZIPCODE, "Zipcode");
        whZipcode.setValue(record.getAttribute(DataNameTokens.INV_WAREHOUSE_ZIPCODE));
        whZipcode.setRequired(Boolean.TRUE);
        whZipcode.setRequiredMessage(errMsg);

        warehouseDetailForm.setFields(whName, whDescription, whAddress, whCity, whZipcode);
        warehouseDetailForm.setDisabled(true);
        
        HLayout buttonSet = new HLayout(5);
        IButton closeButton = new IButton("Close");
        final IButton editButton = new IButton("Edit");
        IButton nonActiveButton = new IButton("Non-Active");

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                warehouseDetailWindow.destroy();
            }
        });

        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	if(editButton.getTitle().equals("Edit")){
            		warehouseDetailForm.setDisabled(false);
            		editButton.setTitle("Save");
            	} else {
            		if(warehouseDetailForm.validate()){
    	                HashMap<String, String> data = new HashMap<String, String>();
    	                data.put(DataNameTokens.INV_WAREHOUSE_ACTIVESTATUS, record.getAttribute(DataNameTokens.INV_WAREHOUSE_ACTIVESTATUS));
    	                data.put(DataNameTokens.INV_WAREHOUSE_NAME, whName.getValueAsString());
    	                data.put(DataNameTokens.INV_WAREHOUSE_DESCRIPTION, whDescription.getValueAsString());
    	                data.put(DataNameTokens.INV_WAREHOUSE_ADDRESS, whAddress.getValueAsString());
    	                data.put(DataNameTokens.INV_WAREHOUSE_CITY, whCity.getValueAsString());
    	                data.put(DataNameTokens.INV_WAREHOUSE_ZIPCODE, whZipcode.getValueAsString());
    	                data.put(DataNameTokens.INV_WAREHOUSE_ORIGINALID, id);
    	                getUiHandlers().saveOrUpdateWarehouseData(MainPagePresenter.signedInUser, data, warehouseDetailWindow);
            		}
            	}
            }
        });

        nonActiveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	HashMap<String, String> data = new HashMap<String, String>();
            	data.put(DataNameTokens.INV_WAREHOUSE_ACTIVESTATUS, "Non Active");
                data.put(DataNameTokens.INV_WAREHOUSE_NAME, whName.getValueAsString());
                data.put(DataNameTokens.INV_WAREHOUSE_DESCRIPTION, whDescription.getValueAsString());
                data.put(DataNameTokens.INV_WAREHOUSE_ADDRESS, whAddress.getValueAsString());
                data.put(DataNameTokens.INV_WAREHOUSE_CITY, whCity.getValueAsString());
                data.put(DataNameTokens.INV_WAREHOUSE_ZIPCODE, whZipcode.getValueAsString());
                data.put(DataNameTokens.INV_WAREHOUSE_ORIGINALID, id);
                getUiHandlers().saveOrUpdateWarehouseData(MainPagePresenter.signedInUser, data, warehouseDetailWindow);
            }
        });

        buttonSet.setAlign(Alignment.CENTER);
        System.out.println(record.getAttributeAsString(DataNameTokens.INV_WAREHOUSE_ACTIVESTATUS));
        System.out.println(record.getAttributeAsString(DataNameTokens.INV_WAREHOUSE_APPROVAL_IN_PROCESS));
        
        if (record.getAttributeAsString(DataNameTokens.INV_WAREHOUSE_ACTIVESTATUS).equals("Active")
                && record.getAttributeAsString(DataNameTokens.INV_WAREHOUSE_APPROVAL_IN_PROCESS).equals("false")) {
            buttonSet.setMembers(closeButton, editButton, nonActiveButton);
        } else {
            buttonSet.setMembers(closeButton);
        }

        warehouseDetailLayout.setMembers(warehouseDetailForm, buttonSet);
        warehouseDetailWindow.addItem(warehouseDetailLayout);

        return warehouseDetailWindow;
    }

    private Window buildAddWarehouseWindow() {
    	addWarehouseWindow = new Window();
    	addWarehouseWindow.setWidth(500);
    	addWarehouseWindow.setHeight(300);
    	addWarehouseWindow.setTitle("Add Warehouse");
    	addWarehouseWindow.setShowMinimizeButton(false);
    	addWarehouseWindow.setIsModal(true);
    	addWarehouseWindow.setShowModalMask(true);
    	addWarehouseWindow.centerInPage();

    	addWarehouseWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
            	addWarehouseWindow.destroy();
            }
        });

        VLayout addWarehouseLayout = new VLayout();
        addWarehouseLayout.setHeight100();
        addWarehouseLayout.setWidth100();

        final DynamicForm addWarehouseForm = new DynamicForm();
        addWarehouseForm.setPadding(5);

        String errMsg = "Required field";
        final TextItem whName = new TextItem("whName", "Warehouse Name");
        whName.setRequired(Boolean.TRUE);
        whName.setRequiredMessage(errMsg);
        final TextAreaItem whDescription = new TextAreaItem("whDescription", "Warehouse Description");
        final TextItem whAddress = new TextItem("whAddress", "Address");
        whAddress.setRequired(Boolean.TRUE);
        whAddress.setRequiredMessage(errMsg);
        final TextItem whCity = new TextItem("whCity", "City");
        whCity.setRequired(Boolean.TRUE);
        whCity.setRequiredMessage(errMsg);
        final TextItem whZipcode = new TextItem("whZipcode", "Zipcode");
        whZipcode.setRequired(Boolean.TRUE);
        whZipcode.setRequiredMessage(errMsg);

        addWarehouseForm.setFields(whName, whDescription, whAddress, whCity, whZipcode);

        HLayout buttonSet = new HLayout(5);

        IButton closeButton = new IButton("Cancel");
        IButton saveButton = new IButton("Save");

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	addWarehouseWindow.destroy();
            }
        });

        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	if(addWarehouseForm.validate()){
	                HashMap<String, String> data = new HashMap<String, String>();
	                data.put(DataNameTokens.INV_WAREHOUSE_NAME, whName.getValueAsString());
	                data.put(DataNameTokens.INV_WAREHOUSE_DESCRIPTION, whDescription.getValueAsString());
	                data.put(DataNameTokens.INV_WAREHOUSE_ADDRESS, whAddress.getValueAsString());
	                data.put(DataNameTokens.INV_WAREHOUSE_CITY, whCity.getValueAsString());
	                data.put(DataNameTokens.INV_WAREHOUSE_ZIPCODE, whZipcode.getValueAsString());
	                getUiHandlers().saveOrUpdateWarehouseData(MainPagePresenter.signedInUser, data, addWarehouseWindow);
            	}
            }
        });

        buttonSet.setAlign(Alignment.CENTER);
        buttonSet.setMembers(closeButton, saveButton);

        addWarehouseLayout.setMembers(addWarehouseForm, buttonSet);
        addWarehouseWindow.addItem(addWarehouseLayout);

        return addWarehouseWindow;
    }

    @Override
    public void loadAllWarehouseData(DataSource dataSource) {
        Map<String, String> status = new HashMap<String, String>();
        status.put("active", "Active");
        status.put("nonActive", "Non Active");
        dataSource.getField(DataNameTokens.INV_WAREHOUSE_ACTIVESTATUS).setValueMap(status);

        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(dataSource);

        warehouseListGrid.setDataSource(dataSource);
        warehouseListGrid.setAutoFetchData(Boolean.TRUE);
        warehouseListGrid.setFields(listGridField);
        warehouseListGrid.setDataSource(dataSource);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_ID).setHidden(Boolean.TRUE);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_ADDRESS).setCanFilter(Boolean.FALSE);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_CITY).setCanFilter(Boolean.FALSE);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_DESCRIPTION).setCanFilter(Boolean.FALSE);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_ZIPCODE).setCanFilter(Boolean.FALSE);
        warehouseListGrid.getField(DataNameTokens.INV_WAREHOUSE_APPROVAL_IN_PROCESS).setHidden(Boolean.TRUE);
        warehouseListGrid.setAutoFitData(Autofit.BOTH);

        warehouseListFilterLayout.setMembers(warehouseListToolStrip, warehouseListGrid);
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
        return warehouseListFilterLayout;
    }
}