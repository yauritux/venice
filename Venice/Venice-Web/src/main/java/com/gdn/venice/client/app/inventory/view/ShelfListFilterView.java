package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;
import java.util.Map;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.ShelfData;
import com.gdn.venice.client.app.inventory.presenter.ShelfListFilterPresenter;
import com.gdn.venice.client.app.inventory.view.handler.ShelfListFilterUiHandler;
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
import com.smartgwt.client.types.SelectionAppearance;
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
public class ShelfListFilterView extends ViewWithUiHandlers<ShelfListFilterUiHandler> implements
        ShelfListFilterPresenter.MyView {

    RafViewLayout shelfListFilterLayout;
    ListGrid shelfListGrid, storageListGrid;
    Window shelfDetailWindow, addShelfWindow;

    ToolStrip shelfListToolStrip;
    ToolStripButton addButton, addStorageButton, removeStorageButton;

    @Inject
    public ShelfListFilterView() {
        shelfListToolStrip = new ToolStrip();
        shelfListToolStrip.setWidth100();
        shelfListToolStrip.setPadding(2);

        addButton = new ToolStripButton();
        addButton.setIcon("[SKIN]/icons/add.png");
        addButton.setTooltip("Add New Shelf");
        addButton.setTitle("Add Shelf");

        shelfListToolStrip.addButton(addButton);

        shelfListFilterLayout = new RafViewLayout();

        shelfListGrid = new ListGrid();
        shelfListGrid.setWidth100();
        shelfListGrid.setHeight100();
        shelfListGrid.setShowAllRecords(true);
        shelfListGrid.setSortField(0);

        shelfListGrid.setShowFilterEditor(true);
        shelfListGrid.setCanResizeFields(true);
        shelfListGrid.setShowRowNumbers(true);

        shelfListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = shelfListGrid.getSelectedRecord();
                buildShelfDetailWindow(record).show();
            }
        });

        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                buildAddShelfWindow().show();
            }
        });
        
        shelfListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshShelfData();
			}
		});
    }

    private Window buildShelfDetailWindow(final ListGridRecord record) {
        shelfDetailWindow = new Window();
        shelfDetailWindow.setWidth(500);
        shelfDetailWindow.setHeight(500);
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

        final TextItem shelfCodeItem = new TextItem(DataNameTokens.INV_SHELF_CODE, "Shelf Code");
        shelfCodeItem.setValue(record.getAttribute(DataNameTokens.INV_SHELF_CODE));
        shelfCodeItem.setDisabled(true);
        
        final TextAreaItem shelfDescItem = new TextAreaItem(DataNameTokens.INV_SHELF_DESCRIPTION, "Shelf Description");
        shelfDescItem.setValue(record.getAttribute(DataNameTokens.INV_SHELF_DESCRIPTION));
        shelfDescItem.setRequired(true);
        
        shelfDetailForm.setFields(shelfCodeItem, shelfDescItem);
        shelfDetailForm.setDisabled(true);
        
        final IButton editButton = new IButton("Edit");
        IButton nonActiveButton = new IButton("Non-Active");
        
        storageListGrid = buildStorageListGrid(id);
        storageListGrid.setCanEdit(false);
        
        shelfDetailWindow.addCloseClickHandler(new CloseClickHandler() {
	      public void onCloseClick(CloseClientEvent event) {
	    	  shelfDetailWindow.destroy();
	      }
        });

        editButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	if(editButton.getTitle().equals("Edit")){
            		shelfDetailForm.setDisabled(false);
            		addStorageButton.setDisabled(false);
            		removeStorageButton.setDisabled(false);
            		storageListGrid.setCanEdit(true);
            		editButton.setTitle("Save");
            	} else {
            		if(shelfDetailForm.validate()){
            			HashMap<String, String> shelfDataMap = new HashMap<String, String>();
    	                HashMap<String, String> shelfRowMap = new HashMap<String, String>();    	 
    	                
    	                shelfRowMap.put(DataNameTokens.INV_SHELF_ORIGINID, id);
    	                shelfRowMap.put(DataNameTokens.INV_SHELF_CODE, shelfCodeItem.getValueAsString());
    	                shelfRowMap.put(DataNameTokens.INV_SHELF_DESCRIPTION, shelfDescItem.getValueAsString());    	             
    	                shelfRowMap.put(DataNameTokens.INV_SHELF_ACTIVESTATUS, record.getAttribute(DataNameTokens.INV_SHELF_ACTIVESTATUS));
    	                shelfDataMap.put("SHELF"+0, Util.formXMLfromHashMap(shelfRowMap));
    	                
    	                //get storage listgrid values
    	                HashMap<String, String> storageDataMap = new HashMap<String, String>();
    					ListGridRecord[] storageRecords = storageListGrid.getSelection();

    					HashMap<String, String> storageMap = new HashMap<String, String>();
    					HashMap<String, String> storageRowMap = new HashMap<String, String>();
    										
    					for (int i=0;i<storageRecords.length;i++) {
    						storageRowMap.put(DataNameTokens.INV_STORAGE_ID, storageRecords[i].getAttributeAsString(DataNameTokens.INV_STORAGE_ID));
    						storageRowMap.put(DataNameTokens.INV_STORAGE_CODE, storageRecords[i].getAttributeAsString(DataNameTokens.INV_STORAGE_CODE));
    						storageRowMap.put(DataNameTokens.INV_STORAGE_DESCRIPTION, storageRecords[i].getAttributeAsString(DataNameTokens.INV_STORAGE_DESCRIPTION));
    						storageRowMap.put(DataNameTokens.INV_STORAGE_TYPE, storageRecords[i].getAttributeAsString(DataNameTokens.INV_STORAGE_TYPE));
    						storageMap.put("STORAGE" + i, Util.formXMLfromHashMap(storageRowMap));
    					}
    					storageDataMap.put("STORAGE", Util.formXMLfromHashMap(storageMap));
        	            getUiHandlers().onSaveShelfClicked(shelfDataMap, storageDataMap, shelfDetailWindow);
            		}
            	}
            }
        });

        nonActiveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	HashMap<String, String> shelfDataMap = new HashMap<String, String>();
            	shelfDataMap.put(DataNameTokens.INV_SHELF_ORIGINID, id);
            	shelfDataMap.put(DataNameTokens.INV_SHELF_CODE, shelfCodeItem.getValueAsString());
            	shelfDataMap.put(DataNameTokens.INV_SHELF_DESCRIPTION, shelfDescItem.getValueAsString());
            	shelfDataMap.put(DataNameTokens.INV_SHELF_ACTIVESTATUS, "Non Active");
            	shelfDataMap.put(DataNameTokens.INV_SHELF_APPROVALSTATUS, "CREATED");
            	shelfDataMap.put(DataNameTokens.INV_SHELF_APPROVALTYPE, "APPROVAL_NON_ACTIVE");
                getUiHandlers().onNonActiveShelfClicked(shelfDataMap, shelfDetailWindow);
            }
        });

        HLayout buttonSet = new HLayout(5);
        buttonSet.setAlign(Alignment.CENTER);
        
        if (record.getAttributeAsString(DataNameTokens.INV_SHELF_ACTIVESTATUS).equals("Active")
                && record.getAttributeAsString(DataNameTokens.INV_SHELF_APPROVAL_IN_PROCESS).equals("false")) {
            buttonSet.setMembers(editButton, nonActiveButton);
        }
        
		VLayout shelfHeaderLayout = new VLayout();
		shelfHeaderLayout.setWidth100();
		shelfHeaderLayout.setMargin(10);
		shelfHeaderLayout.setMembers(buttonSet, shelfDetailForm);
        
		Label storageLabel = new Label("<b>Storage Bin:</b>");
		storageLabel.setHeight(10);	
			    
		addStorageButton = new ToolStripButton();
	    addStorageButton.setIcon("[SKIN]/icons/business_users_add.png");  
	    addStorageButton.setTooltip("Add Storage Bin");
	    addStorageButton.setTitle("Add Storage Bin");
	    addStorageButton.setDisabled(true);
	    
	    removeStorageButton = new ToolStripButton();
	    removeStorageButton.setIcon("[SKIN]/icons/business_users_delete.png");  
	    removeStorageButton.setTooltip("Remove Storage Bin");
	    removeStorageButton.setTitle("Remove Storage Bin");
	    removeStorageButton.setDisabled(true);
              
		ToolStrip storageToolStrip = new ToolStrip();
		storageToolStrip.setWidth100();
		
		storageToolStrip.addButton(addStorageButton);
		storageToolStrip.addButton(removeStorageButton);

		addStorageButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				storageListGrid.startEditingNew();
			}
		});
		
		removeStorageButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				storageListGrid.removeSelectedData();
			}
		});
        		
        shelfDetailLayout.setMembers(shelfHeaderLayout, storageLabel, storageToolStrip, storageListGrid);
        shelfDetailWindow.addItem(shelfDetailLayout);

        return shelfDetailWindow;
    }
    
    private Window buildAddShelfWindow() {
    	addShelfWindow = new Window();
    	addShelfWindow.setWidth(500);
    	addShelfWindow.setHeight(500);
    	addShelfWindow.setTitle("Add Shelf");
    	addShelfWindow.setShowMinimizeButton(false);
    	addShelfWindow.setIsModal(true);
    	addShelfWindow.setShowModalMask(true);
    	addShelfWindow.centerInPage();

		addShelfWindow.addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClientEvent event) {
				addShelfWindow.destroy();
			}
		});

		VLayout addShelfLayout = new VLayout();
		addShelfLayout.setHeight100();
		addShelfLayout.setWidth100();

        final DynamicForm addShelfForm = new DynamicForm();
        addShelfForm.setPadding(5);
        
		final TextItem shelfCodeItem = new TextItem(DataNameTokens.INV_SHELF_CODE, "Shelf Code");
		shelfCodeItem.setDisabled(true);

		final TextAreaItem shelfDescItem = new TextAreaItem(DataNameTokens.INV_SHELF_DESCRIPTION, "Shelf Description");
		shelfDescItem.setRequired(true);

		addShelfForm.setFields(shelfCodeItem, shelfDescItem);
		
        HLayout buttonSet = new HLayout(5);
        buttonSet.setAlign(Alignment.CENTER);
                
        IButton cancelButton= new IButton("Cancel");
        IButton saveButton = new IButton("Save");
        
        buttonSet.setAlign(Alignment.LEFT);
        buttonSet.setMembers(saveButton, cancelButton);  
        
        storageListGrid = buildStorageListGrid(null);
        
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addShelfWindow.destroy();
			}
		});
  	         
        saveButton.addClickHandler(new ClickHandler() {
	          @Override
	          public void onClick(ClickEvent event) {
	          	if(addShelfForm.validate()){	          		
					HashMap<String, String> shelfRowMap = new HashMap<String, String>();
						
					//get shelf form values
					shelfRowMap.put(DataNameTokens.INV_SHELF_DESCRIPTION, shelfDescItem.getValueAsString());
		                
		            //get storage listgrid values
		            HashMap<String, String> storageDataMap = new HashMap<String, String>();
					ListGridRecord[] storageRecords = storageListGrid.getRecords();

					HashMap<String, String> storageRowMap = new HashMap<String, String>();
										
					for (int i=0;i<storageRecords.length;i++) {
						storageRowMap.put(DataNameTokens.INV_STORAGE_DESCRIPTION, storageRecords[i].getAttributeAsString(DataNameTokens.INV_STORAGE_DESCRIPTION));
						storageRowMap.put(DataNameTokens.INV_STORAGE_TYPE, storageRecords[i].getAttributeAsString(DataNameTokens.INV_STORAGE_TYPE));
						storageDataMap.put("STORAGE"+i, storageRowMap.toString());
					}
					getUiHandlers().onSaveShelfClicked(shelfRowMap, storageDataMap, addShelfWindow);
          		}
          	}
        });
        
        VLayout shelfHeaderLayout = new VLayout();
		shelfHeaderLayout.setWidth100();
		shelfHeaderLayout.setMargin(10);
		shelfHeaderLayout.setMembers(buttonSet, addShelfForm);
        
		Label storageLabel = new Label("<b>Storage Bin:</b>");
		storageLabel.setHeight(10);	
			    
		addStorageButton = new ToolStripButton();
	    addStorageButton.setIcon("[SKIN]/icons/business_users_add.png");  
	    addStorageButton.setTooltip("Add Storage Bin");
	    addStorageButton.setTitle("Add Storage Bin");
	    
	    removeStorageButton = new ToolStripButton();
	    removeStorageButton.setIcon("[SKIN]/icons/business_users_delete.png");  
	    removeStorageButton.setTooltip("Remove Storage Bin");
	    removeStorageButton.setTitle("Remove Storage Bin");
              
		ToolStrip storageToolStrip = new ToolStrip();
		storageToolStrip.setWidth100();
		
		storageToolStrip.addButton(addStorageButton);
		storageToolStrip.addButton(removeStorageButton);

		addStorageButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				storageListGrid.startEditingNew();
			}
		});
		
		removeStorageButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				storageListGrid.removeSelectedData();
			}
		});
        		
		addShelfLayout.setMembers(shelfHeaderLayout, storageLabel, storageToolStrip, storageListGrid);
		addShelfWindow.addItem(addShelfLayout);

        return addShelfWindow;
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
    	storageListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
    	    	
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
        	DataSource storageData = ShelfData.getStorageData(shelfId, 1, 20);
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
    public void loadShelfData(DataSource dataSource) {
        Map<String, String> status = new HashMap<String, String>();
        status.put("active", "Active");
        status.put("nonActive", "Non Active");
        dataSource.getField(DataNameTokens.INV_SHELF_ACTIVESTATUS).setValueMap(status);

        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(dataSource);

        shelfListGrid.setDataSource(dataSource);
        shelfListGrid.setAutoFetchData(true);
        shelfListGrid.setFields(listGridField);
        shelfListGrid.setDataSource(dataSource);
        shelfListGrid.getField(DataNameTokens.INV_SHELF_ID).setHidden(true);
        shelfListGrid.getField(DataNameTokens.INV_SHELF_CODE).setCanFilter(false);
        shelfListGrid.getField(DataNameTokens.INV_SHELF_DESCRIPTION).setCanFilter(false);
        shelfListGrid.getField(DataNameTokens.INV_SHELF_APPROVAL_IN_PROCESS).setHidden(true);
        shelfListGrid.setAutoFitData(Autofit.BOTH);

        shelfListFilterLayout.setMembers(shelfListToolStrip, shelfListGrid);
    }

    @Override
    public void refreshShelfData() {
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
        return shelfListFilterLayout;
    }
}