package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;
import java.util.Map;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.ShelfData;
import com.gdn.venice.client.app.inventory.presenter.ShelfNonActiveWithApprovalPresenter;
import com.gdn.venice.client.app.inventory.view.handler.ShelfNonActiveWithApprovalUiHandler;
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
import com.smartgwt.client.widgets.Label;
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
 * @author Roland
 */
public class ShelfNonActiveWithApprovalView extends ViewWithUiHandlers<ShelfNonActiveWithApprovalUiHandler> implements
        ShelfNonActiveWithApprovalPresenter.MyView {

    RafViewLayout shelfApprovalAddLayout;
    ListGrid shelfListGrid, storageListGrid;
    Window shelfDetailWindow;

    ToolStrip shelfListToolStrip;

    @Inject
    public ShelfNonActiveWithApprovalView() {
        shelfListToolStrip = new ToolStrip();
        shelfListToolStrip.setWidth100();
        shelfListToolStrip.setPadding(2);

        shelfApprovalAddLayout = new RafViewLayout();

        shelfListGrid = new ListGrid();
        shelfListGrid.setWidth100();
        shelfListGrid.setHeight100();
        shelfListGrid.setShowAllRecords(true);
        shelfListGrid.setSortField(0);

        shelfListGrid.setShowFilterEditor(true);
        shelfListGrid.setCanResizeFields(true);
        shelfListGrid.setShowRowNumbers(true);

        bindCustomUiHandlers();
    }

    protected void bindCustomUiHandlers() {
        shelfListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = shelfListGrid.getSelectedRecord();
                buildShelfDetailWindow(record).show();
            }
        });
        
        shelfListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshAllShelfData();
			}
		});
    }

    private Window buildShelfDetailWindow(final ListGridRecord record) {
        shelfDetailWindow = new Window();
        shelfDetailWindow.setWidth(600);
		shelfDetailWindow.setHeight(375);
        shelfDetailWindow.setTitle("Shelf Detail");
        shelfDetailWindow.setShowMinimizeButton(false);
        shelfDetailWindow.setIsModal(true);
        shelfDetailWindow.setShowModalMask(true);
        shelfDetailWindow.centerInPage();

        shelfDetailWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                shelfDetailWindow.destroy();
            }
        });

        VLayout shelfDetailLayout = new VLayout();
        shelfDetailLayout.setHeight100();
        shelfDetailLayout.setWidth100();

        DynamicForm shelfDetailForm = new DynamicForm();
        shelfDetailForm.setPadding(5);

        final String id = record.getAttribute(DataNameTokens.INV_SHELF_ID);
        
        final String originId = record.getAttribute(DataNameTokens.INV_SHELF_ORIGINID);

        final TextItem whCode = new TextItem(DataNameTokens.INV_SHELF_CODE, "Shelf Code");
        whCode.setValue(record.getAttribute(DataNameTokens.INV_SHELF_CODE));
        whCode.setDisabled(Boolean.TRUE);

        final TextAreaItem whDescription = new TextAreaItem(DataNameTokens.INV_SHELF_DESCRIPTION, "Shelf Description");
        whDescription.setValue(record.getAttribute(DataNameTokens.INV_SHELF_DESCRIPTION));
       
        shelfDetailForm.setFields(whCode, whDescription);
        
		Label storageLabel = new Label("<b>Storage Bin:</b>");
		storageLabel.setHeight(10);
        
        storageListGrid = buildStorageListGrid(originId);
        storageListGrid.setCanEdit(false);

        HLayout buttonSet = new HLayout(5);

        IButton approveButton = new IButton("Approve");
        IButton rejectButton = new IButton("Reject");

        approveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HashMap<String, String> data = new HashMap<String, String>();
                data.put(DataNameTokens.INV_SHELF_ID, id.toString());
                data.put(DataNameTokens.INV_SHELF_ORIGINID, originId);
                data.put(DataNameTokens.INV_SHELF_APPROVALSTATUS, "APPROVED");

                getUiHandlers().approveNonActiveShelfData(MainPagePresenter.signedInUser, data);
            }
        });

        rejectButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                HashMap<String, String> data = new HashMap<String, String>();
                data.put(DataNameTokens.INV_SHELF_ID, id.toString());
                data.put(DataNameTokens.INV_SHELF_ORIGINID, originId);
                data.put(DataNameTokens.INV_SHELF_APPROVALSTATUS, "REJECTED");

                getUiHandlers().rejectNonActiveShelfData(MainPagePresenter.signedInUser, data);
            }
        });

        buttonSet.setAlign(Alignment.CENTER);
        if (MainPagePresenter.getSignedInUserRole().toLowerCase().contains("inv_wh_approver")) {
            buttonSet.setMembers(approveButton, rejectButton);
        }

        shelfDetailLayout.setMembers(shelfDetailForm, buttonSet, storageLabel, storageListGrid);
        shelfDetailWindow.addItem(shelfDetailLayout);

        return shelfDetailWindow;
    }
    
    private ListGrid buildStorageListGrid(String shelfId){
    	storageListGrid = new ListGrid();
    	storageListGrid.setWidth100();
    	storageListGrid.setHeight100();
    	storageListGrid.setShowAllRecords(true);
    	storageListGrid.setSortField(0);

    	storageListGrid.setShowFilterEditor(true);
    	storageListGrid.setCanResizeFields(true);
    	storageListGrid.setShowRowNumbers(true);
    	storageListGrid.setShowFilterEditor(true);
    	    	
    	if(shelfId==null){
    		ListGridField codeField = new ListGridField(DataNameTokens.INV_STORAGE_CODE, "Code");  
            ListGridField descField = new ListGridField(DataNameTokens.INV_STORAGE_DESCRIPTION, "Description");  
            ListGridField typeField = new ListGridField(DataNameTokens.INV_STORAGE_TYPE, "Type");  
                        
            storageListGrid.setFields(codeField, descField, typeField);
            
            Map<String, String> storageType = new HashMap<String, String>();
            storageType.put("bin", "Bin");
            storageType.put("carton", "Carton");
            storageType.put("pallet", "Pallet");
            storageListGrid.getField(DataNameTokens.INV_STORAGE_TYPE).setValueMap(storageType);
    	}else{
        	DataSource storageData = ShelfData.getStorageData(shelfId, 1, 100);
    		storageListGrid.setDataSource(storageData);
    		storageListGrid.setFields(Util.getListGridFieldsFromDataSource(storageData));
    		
            Map<String, String> storageType = new HashMap<String, String>();
            storageType.put("bin", "Bin");
            storageType.put("carton", "Carton");
            storageType.put("pallet", "Pallet");
            storageData.getField(DataNameTokens.INV_STORAGE_TYPE).setValueMap(storageType);
            
        	storageListGrid.setAutoFetchData(true);
            storageListGrid.getField(DataNameTokens.INV_STORAGE_ID).setHidden(true);
            storageListGrid.getField(DataNameTokens.INV_STORAGE_CODE).setCanFilter(false);
            storageListGrid.getField(DataNameTokens.INV_STORAGE_DESCRIPTION).setCanFilter(false);
            storageListGrid.getField(DataNameTokens.INV_STORAGE_TYPE).setCanFilter(false);            
    	}
		        
        return storageListGrid;
    }

    @Override
    public void loadApprovalNonActiveShelfData(DataSource dataSource) {
        Map<String, String> status = new HashMap<String, String>();
        status.put("CREATED", "New");
        status.put("APPROVED", "Approved");
        status.put("NEED_CORRECTION", "Need Correction");
        status.put("REJECTED", "Rejected");
        dataSource.getField(DataNameTokens.INV_SHELF_APPROVALSTATUS).setValueMap(status);

        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(dataSource);

        shelfListGrid.setDataSource(dataSource);
        shelfListGrid.setAutoFetchData(Boolean.TRUE);
        shelfListGrid.setFields(listGridField);
        shelfListGrid.getField(DataNameTokens.INV_SHELF_ID).setHidden(true);
        shelfListGrid.getField(DataNameTokens.INV_SHELF_ORIGINID).setCanFilter(false);
        shelfListGrid.getField(DataNameTokens.INV_SHELF_CODE).setCanFilter(true);
        shelfListGrid.getField(DataNameTokens.INV_SHELF_DESCRIPTION).setCanFilter(true);
        shelfListGrid.setAutoFitData(Autofit.BOTH);

        shelfApprovalAddLayout.setMembers(shelfListToolStrip, shelfListGrid);
    }

    @Override
    public void refreshAllShelfData() {
    	DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                shelfListGrid.setData(response.getData());
            }
        };

        shelfListGrid.getDataSource().fetchData(shelfListGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public Widget asWidget() {
        return shelfApprovalAddLayout;
    }

    @Override
    public Window getShelfDetailWindow() {
        return shelfDetailWindow;
    }
}