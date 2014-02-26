/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;
import java.util.Map;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.presenter.WarehouseEditWithApprovalPresenter;
import com.gdn.venice.client.app.inventory.view.handler.WarehouseEditWithApprovalUiHandler;
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

/**
 *
 * @author Maria Olivia
 */
public class WarehouseEditWithApprovalView extends ViewWithUiHandlers<WarehouseEditWithApprovalUiHandler> implements
	WarehouseEditWithApprovalPresenter.MyView{

	RafViewLayout warehouseApprovalAddLayout;
	ListGrid warehouseListGrid;
	Window warehouseDetailWindow;
	/*
	 * The toolstrip objects for the header
	 */
	ToolStrip warehouseListToolStrip;

	@Inject
	public WarehouseEditWithApprovalView() {
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
		warehouseDetailWindow.setHeight(300);
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

		DynamicForm warehouseDetailForm = new DynamicForm();
		warehouseDetailForm.setPadding(5);

		final Long id = Long.parseLong(record.getAttribute(DataNameTokens.INV_WAREHOUSE_ID));

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

		HLayout buttonSet = new HLayout(5);

		IButton closeButton = new IButton("Close");
		IButton editButton = new IButton("Edit");
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
				HashMap<String, String> data = new HashMap<String, String>();
				data.put(DataNameTokens.INV_WAREHOUSE_ID, id.toString());
				data.put(DataNameTokens.INV_WAREHOUSE_NAME, whName.getValueAsString());
				data.put(DataNameTokens.INV_WAREHOUSE_DESCRIPTION, whDescription.getValueAsString());
				data.put(DataNameTokens.INV_WAREHOUSE_ADDRESS, whAddress.getValueAsString());
				data.put(DataNameTokens.INV_WAREHOUSE_CITY, whCity.getValueAsString());
				data.put(DataNameTokens.INV_WAREHOUSE_ZIPCODE, whZipcode.getValueAsString());

				getUiHandlers().updateWarehouseWIPData(MainPagePresenter.signedInUser, data);
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

		//        if (InventoryUtil.isApprover(MainPagePresenter.signedInUser)) {
		buttonSet.setMembers(closeButton, editButton,
				approveButton, correctionButton, rejectButton);
		//        } else {
		//            buttonSet.setMembers(closeButton, editButton);
		//        }

		warehouseDetailLayout.setMembers(warehouseDetailForm, buttonSet);
		warehouseDetailWindow.addItem(warehouseDetailLayout);

		return warehouseDetailWindow;
	}

	@Override
	public void loadApprovalEditWarehouseData(DataSource dataSource) {
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