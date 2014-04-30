package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;
import java.util.Map;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.ShelfData;
import com.gdn.venice.client.app.inventory.presenter.ShelfEditWithApprovalPresenter;
import com.gdn.venice.client.app.inventory.view.handler.ShelfEditWithApprovalUiHandler;
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
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

/**
 *
 * @author Roland
 */
public class ShelfEditWithApprovalView extends ViewWithUiHandlers<ShelfEditWithApprovalUiHandler> implements
	ShelfEditWithApprovalPresenter.MyView{

	RafViewLayout shelfApprovalAddLayout;
	ListGrid shelfListGrid, storageListGrid;
	Window shelfDetailWindow;

	ToolStrip shelfListToolStrip;
	ToolStripButton addStorageButton;

	@Inject
	public ShelfEditWithApprovalView() {
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

		final DynamicForm shelfDetailForm = new DynamicForm();
		shelfDetailForm.setPadding(5);

		final String id = record.getAttribute(DataNameTokens.INV_SHELF_ID);

		final TextItem shelfCode = new TextItem(DataNameTokens.INV_SHELF_CODE, "Shelf Code");
		shelfCode.setValue(record.getAttribute(DataNameTokens.INV_SHELF_CODE));
		shelfCode.setDisabled(true);
		
		final TextAreaItem shelfDescItem = new TextAreaItem(DataNameTokens.INV_SHELF_DESCRIPTION, "Shelf Description");
		shelfDescItem.setValue(record.getAttribute(DataNameTokens.INV_SHELF_DESCRIPTION));
		shelfDescItem.setDisabled(true);
			
		shelfDetailForm.setFields(shelfCode, shelfDescItem);
		
		Label storageLabel = new Label("<b>Storage Bin:</b>");
		storageLabel.setHeight(10);
		
        storageListGrid = buildStorageListGrid(id);
        storageListGrid.setCanEdit(false);

		HLayout buttonSet = new HLayout(5);

		final IButton editButton = new IButton("Edit");
		IButton correctionButton = new IButton("Need Correction");
		correctionButton.setAutoFit(true);
		IButton approveButton = new IButton("Approve");
		IButton rejectButton = new IButton("Reject");

		editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	if(editButton.getTitle().equals("Edit")){
            		shelfDetailForm.setDisabled(false);
            		storageListGrid.setCanEdit(true);
            		addStorageButton.setDisabled(false);
            		shelfDescItem.setDisabled(false);
            		editButton.setTitle("Save");
            	} else {
            		if(shelfDetailForm.validate()){
    	                HashMap<String, String> shelfRowMap = new HashMap<String, String>();    	 
    	                
    	                shelfRowMap.put(DataNameTokens.INV_SHELF_ID, id);
    	                shelfRowMap.put(DataNameTokens.INV_SHELF_DESCRIPTION, shelfDescItem.getValueAsString());   
    	                
    	            	ListGridRecord[] storageRecords = storageListGrid.getRecords();
    	                HashMap<String, String> storageDataMap = new HashMap<String, String>();
    					HashMap<String, String> storageRowMap = new HashMap<String, String>();
    										
    					for (int i=0;i<storageRecords.length;i++) {
    						storageRowMap.put(DataNameTokens.INV_STORAGE_ID, storageRecords[i].getAttributeAsString(DataNameTokens.INV_STORAGE_ID));
    						storageRowMap.put(DataNameTokens.INV_STORAGE_CODE, storageRecords[i].getAttributeAsString(DataNameTokens.INV_STORAGE_CODE));
    						storageRowMap.put(DataNameTokens.INV_STORAGE_DESCRIPTION, storageRecords[i].getAttributeAsString(DataNameTokens.INV_STORAGE_DESCRIPTION));
    						storageRowMap.put(DataNameTokens.INV_STORAGE_TYPE, storageRecords[i].getAttributeAsString(DataNameTokens.INV_STORAGE_TYPE));
    						storageDataMap.put("STORAGE" + i, storageRowMap.toString());
    					}
        	            getUiHandlers().onEditShelfEditClicked(shelfRowMap, storageDataMap, shelfDetailWindow);
            		}
            	}
            }
        });

		approveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				HashMap<String, String> data = new HashMap<String, String>();
				data.put(DataNameTokens.INV_SHELF_ID, id.toString());
				data.put(DataNameTokens.INV_SHELF_APPROVALSTATUS, "APPROVED");

				getUiHandlers().approveEditShelfData(MainPagePresenter.signedInUser, data);
			}
		});

		correctionButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				HashMap<String, String> data = new HashMap<String, String>();
				data.put(DataNameTokens.INV_SHELF_ID, id.toString());
				data.put(DataNameTokens.INV_SHELF_APPROVALSTATUS, "NEED_CORRECTION");

				getUiHandlers().needCorrectionEditShelfData(MainPagePresenter.signedInUser, data);
			}
		});

		rejectButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				HashMap<String, String> data = new HashMap<String, String>();
				data.put(DataNameTokens.INV_SHELF_ID, id.toString());
				data.put(DataNameTokens.INV_SHELF_APPROVALSTATUS, "REJECTED");

				getUiHandlers().rejectEditShelfData(MainPagePresenter.signedInUser, data);
			}
		});

		buttonSet.setAlign(Alignment.CENTER);
        if(record.getAttribute(DataNameTokens.INV_SHELF_APPROVALSTATUS).equals("Need Correction")){
        	buttonSet.addMember(editButton);
        }else{            
            buttonSet.addMember(approveButton);
            buttonSet.addMember(correctionButton);
            buttonSet.addMember(rejectButton);
        }
        
        addStorageButton = new ToolStripButton();
	    addStorageButton.setIcon("[SKIN]/icons/business_users_add.png");  
	    addStorageButton.setTooltip("Add Storage Bin");
	    addStorageButton.setTitle("Add Storage Bin");
	    addStorageButton.setDisabled(true);
              
		ToolStrip storageToolStrip = new ToolStrip();
		storageToolStrip.setWidth100();
		
		storageToolStrip.addButton(addStorageButton);

		addStorageButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				storageListGrid.startEditingNew();
			}
		});
		
		shelfDetailLayout.setMembers(shelfDetailForm, buttonSet, storageLabel, storageToolStrip, storageListGrid);
		shelfDetailWindow.addItem(shelfDetailLayout);

		return shelfDetailWindow;
	}
	
    private ListGrid buildStorageListGrid(String shelfId){
    	storageListGrid = new ListGrid();
    	storageListGrid.setWidth100();
    	storageListGrid.setHeight100();
    	storageListGrid.setShowAllRecords(true);
    	storageListGrid.setSortField(0);
    	storageListGrid.setSaveLocally(true);

    	storageListGrid.setShowFilterEditor(true);
    	storageListGrid.setCanResizeFields(true);
    	storageListGrid.setShowRowNumbers(true);
    	storageListGrid.setShowFilterEditor(true);
    	    	
    	if(shelfId==null){
    		ListGridField codeField = new ListGridField(DataNameTokens.INV_STORAGE_CODE, "Code"); 
    		codeField.setCanEdit(false);
            ListGridField descField = new ListGridField(DataNameTokens.INV_STORAGE_DESCRIPTION, "Description");  
            ListGridField typeField = new ListGridField(DataNameTokens.INV_STORAGE_TYPE, "Type");  
                        
            storageListGrid.setFields(codeField, descField, typeField);
            
            Map<String, String> storageType = new HashMap<String, String>();
            storageType.put("bin", "Bin");
            storageType.put("carton", "Carton");
            storageType.put("pallet", "Pallet");
            storageListGrid.getField(DataNameTokens.INV_STORAGE_TYPE).setValueMap(storageType);
    	}else{
        	DataSource storageData = ShelfData.getStorageInProcessData(shelfId, 1, 50);
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
            storageListGrid.getField(DataNameTokens.INV_STORAGE_CODE).setCanEdit(false);
            storageListGrid.getField(DataNameTokens.INV_STORAGE_DESCRIPTION).setCanFilter(false);
            storageListGrid.getField(DataNameTokens.INV_STORAGE_TYPE).setCanFilter(false);            
    	}
		        
        return storageListGrid;
    }

	@Override
	public void loadApprovalEditShelfData(DataSource dataSource) {
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
		shelfListGrid.getField(DataNameTokens.INV_SHELF_ID).setHidden(Boolean.TRUE);
		shelfListGrid.getField(DataNameTokens.INV_SHELF_CODE).setHidden(Boolean.TRUE);
		shelfListGrid.getField(DataNameTokens.INV_SHELF_DESCRIPTION).setCanFilter(Boolean.FALSE);
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