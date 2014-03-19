package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.GRNData;
import com.gdn.venice.client.app.inventory.presenter.GRNCreatePresenter;
import com.gdn.venice.client.app.inventory.view.handler.GRNCreateUiHandler;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.rpc.RPCCallback;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
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
public class GRNCreateView extends ViewWithUiHandlers<GRNCreateUiHandler> implements
	GRNCreatePresenter.MyView {

    RafViewLayout layout;
    ListGrid asnListGrid, itemListGrid, attributeListGrid;
    Window createGRNWindow, attributeWindow;
    Boolean canSave=false;

    ToolStrip toolStrip;
    ToolStripButton addItemButton, removeItemButton;

    @Inject
    public GRNCreateView() {
        toolStrip = new ToolStrip();
        toolStrip.setWidth100();
        toolStrip.setPadding(2);

        layout = new RafViewLayout();

        asnListGrid = new ListGrid();
        asnListGrid.setWidth100();
        asnListGrid.setHeight100();
        asnListGrid.setShowAllRecords(true);
        asnListGrid.setSortField(0);

        asnListGrid.setShowFilterEditor(true);
        asnListGrid.setCanResizeFields(true);
        asnListGrid.setShowRowNumbers(true);

        asnListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = asnListGrid.getSelectedRecord();
                buildCreateGRNWindow(record).show();
            }
        });
        
        asnListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshASNData();
			}
		});
    }

    private Window buildCreateGRNWindow(final ListGridRecord record) {
        createGRNWindow = new Window();
        createGRNWindow.setWidth(700);
        createGRNWindow.setHeight(525);
        createGRNWindow.setTitle("Create GRN");
        createGRNWindow.setShowMinimizeButton(false);
        createGRNWindow.setIsModal(true);
        createGRNWindow.setShowModalMask(true);
        createGRNWindow.centerInPage();
        
        IButton saveButton = new IButton("Save");
        HLayout buttonSet = new HLayout(5);
        buttonSet.setAlign(Alignment.LEFT);
        buttonSet.setMembers(saveButton);
                
        createGRNWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                createGRNWindow.destroy();
            }
        });

        VLayout detailLayout = new VLayout();
        detailLayout.setHeight100();
        detailLayout.setWidth100();

        final DynamicForm asnDetailForm = new DynamicForm();
        asnDetailForm.setPadding(5);
        asnDetailForm.setNumCols(4);

        final String id = record.getAttribute(DataNameTokens.INV_ASN_ID);

        final TextItem asnNumberItem = new TextItem(DataNameTokens.INV_ASN_NUMBER, "ASN No");
        asnNumberItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_NUMBER));
        
        final TextItem reffNumberItem = new TextItem(DataNameTokens.INV_ASN_REFF_NUMBER, "Reff No");
        reffNumberItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_REFF_NUMBER));
        
        final TextItem reffDateItem = new TextItem(DataNameTokens.INV_ASN_REFF_DATE, "Reff Date");
        reffDateItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_REFF_DATE));
        
        final TextItem inventoryTypeItem = new TextItem(DataNameTokens.INV_ASN_INVENTORY_TYPE, "Inventory Type");
        inventoryTypeItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_INVENTORY_TYPE));
        
        final TextItem supplierCodeItem = new TextItem(DataNameTokens.INV_ASN_SUPPLIER_CODE, "Supplier Code");
        supplierCodeItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_SUPPLIER_CODE));
        
        final TextItem supplierNameItem = new TextItem(DataNameTokens.INV_ASN_SUPPLIER_NAME, "Supplier Name");
        supplierNameItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_SUPPLIER_NAME));
        
        final TextItem DestinationItem = new TextItem(DataNameTokens.INV_ASN_DESTINATION, "Destination");
        DestinationItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_DESTINATION));
        
        final TextItem DestinationItemCode = new TextItem(DataNameTokens.INV_ASN_DESTINATIONCODE, "Destination Code");
        DestinationItemCode.setValue(record.getAttribute(DataNameTokens.INV_ASN_DESTINATIONCODE));
        
        asnDetailForm.setDisabled(true);
                
        asnDetailForm.setFields(asnNumberItem, reffDateItem, reffNumberItem, inventoryTypeItem, supplierCodeItem, 
        		DestinationItem, supplierNameItem);   
        
        itemListGrid = buildItemListGrid(id);   
        
        itemListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {
				final ListGridRecord selectedrecord = itemListGrid.getSelectedRecord();
				 
				RPCRequest request = new RPCRequest();
                request.setActionURL(GWT.getHostPageBaseURL() + "GRNManagementPresenterServlet?method=getMaxGRNItemQuantityAllowed&type=RPC&asnItemId="+selectedrecord.getAttributeAsString(DataNameTokens.INV_ASN_ITEM_ID));
                request.setHttpMethod("POST");
                request.setUseSimpleHttp(true);
               
                RPCManager.sendRequest(request, new RPCCallback() {
                    public void execute(RPCResponse response, Object rawData, RPCRequest request) {
                        String rpcResponse = rawData.toString().trim();
                        if (rpcResponse!=null) {	
                    		if(Integer.parseInt(selectedrecord.getAttributeAsString(DataNameTokens.INV_POCFF_QTY))>Integer.parseInt(rpcResponse)){
                    			SC.say("GRN item max allowed is "+rpcResponse);    
                    			canSave=false;               			
                    		}else{
                    			canSave=true;
                    		}
                        }
                    }
                });				
			}
		});
                
        saveButton.addClickHandler(new ClickHandler() {
	          @Override
	          public void onClick(ClickEvent event) {   
//	        	  if(canSave==true){
					HashMap<String, String> grnDataMap = new HashMap<String, String>();
						
					grnDataMap.put(DataNameTokens.INV_ASN_ID, id);
					grnDataMap.put(DataNameTokens.INV_ASN_DESTINATIONCODE, DestinationItemCode.getValueAsString());
					grnDataMap.put(DataNameTokens.INV_ASN_REFF_NUMBER, reffNumberItem.getValueAsString());
					grnDataMap.put(DataNameTokens.INV_ASN_INVENTORY_TYPE, inventoryTypeItem.getValueAsString());
		                
		            HashMap<String, String> grnItemDataMap = new HashMap<String, String>();
					ListGridRecord[] itemRecords = itemListGrid.getRecords();
	
					HashMap<String, String> itemRowMap = new HashMap<String, String>();
										
					for (int i=0;i<itemRecords.length;i++) {
						itemRowMap.put(DataNameTokens.INV_ASN_ITEM_ID, itemRecords[i].getAttributeAsString(DataNameTokens.INV_ASN_ITEM_ID));
						itemRowMap.put(DataNameTokens.INV_POCFF_QTY, itemRecords[i].getAttributeAsString(DataNameTokens.INV_POCFF_QTY));
						grnItemDataMap.put("ITEM"+i, itemRowMap.toString());					
					}
					getUiHandlers().onSaveClicked(grnDataMap, grnItemDataMap, createGRNWindow);
//	        	  }else{
//	        		  SC.say("Please check the GRN item quantity");
//	        	  }
	  		}
	    });
                        
        createGRNWindow.addCloseClickHandler(new CloseClickHandler() {
	      public void onCloseClick(CloseClientEvent event) {
	    	  createGRNWindow.destroy();
	      }
        });
        
		VLayout headerLayout = new VLayout();
		headerLayout.setWidth100();
		headerLayout.setMargin(10);
		headerLayout.setMembers(buttonSet, asnDetailForm);
        
		Label itemLabel = new Label("<b>GRN Item:</b>");
		itemLabel.setHeight(10);	
		
		ToolStrip itemToolStrip = new ToolStrip();
		itemToolStrip.setWidth100();
           	
        detailLayout.setMembers(headerLayout, itemLabel, itemToolStrip, itemListGrid);
        createGRNWindow.addItem(detailLayout);

        return createGRNWindow;
    }
            
    private ListGrid buildItemListGrid(String asnId){  	
    	final DataSource grnItemData = GRNData.getASNItemData(asnId, 1, 20);
    	
    	final ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(grnItemData);
		ListGridField attributeField = new ListGridField("ATTRIBUTEFIELD", "Add Attribute");  
		attributeField.setWidth(35);
		attributeField.setCanFilter(false);
		attributeField.setCanSort(false);
		attributeField.setCanEdit(false);
		attributeField.setWidth(100);
		attributeField.setAlign(Alignment.CENTER);
        ListGridField finalListGridField[] = {listGridField[0], listGridField[1], listGridField[2], listGridField[3], listGridField[4], attributeField};
               		
        itemListGrid = new ListGrid()  {  
			@Override
			protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {
				if (this.getFieldName(colNum).equals("ATTRIBUTEFIELD")) {
					ImgButton attributeImg = new ImgButton();
					attributeImg.setShowDown(false);
					attributeImg.setShowRollOver(false);
					attributeImg.setLayoutAlign(Alignment.CENTER);
					attributeImg.setSrc("[SKIN]/icons/book_add.png");
					attributeImg.setPrompt("Add/Edit Attribute");
					attributeImg.setHeight(16);
					attributeImg.setWidth(16);
					attributeImg.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {		
					        attributeWindow = buildAttributeWindow(record.getAttribute(DataNameTokens.INV_ASN_ITEM_ID));	
					        attributeWindow.show();
						}
					});

					return attributeImg;
				} else {
					return null;
				}
			}
		};
    	
		itemListGrid.setDataSource(grnItemData);
				
    	itemListGrid.setWidth100();
    	itemListGrid.setHeight100();
    	itemListGrid.setShowAllRecords(true);
    	itemListGrid.setSortField(0);
    	itemListGrid.setAutoFetchData(true);
        itemListGrid.setCanEdit(true);
        itemListGrid.setSaveLocally(true);
    	itemListGrid.setCanResizeFields(true);
    	itemListGrid.setShowRowNumbers(true);
    	itemListGrid.setShowFilterEditor(false);
    	itemListGrid.setShowRecordComponents(true);          
    	itemListGrid.setShowRecordComponentsByCell(true); 
				        
		itemListGrid.setFields(finalListGridField);
		itemListGrid.getField(DataNameTokens.INV_ASN_ITEM_ID).setCanEdit(false);
		itemListGrid.getField(DataNameTokens.INV_POCFF_ITEMCODE).setCanEdit(false);
		itemListGrid.getField(DataNameTokens.INV_POCFF_ITEMDESC).setCanEdit(false);
		itemListGrid.getField(DataNameTokens.INV_POCFF_ITEMUNIT).setCanEdit(false);
		itemListGrid.getField(DataNameTokens.INV_ASN_ITEM_ID).setHidden(true);	
              				        
        return itemListGrid;
    }

    @Override
    public void loadASNData(DataSource dataSource) {
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(dataSource);

        asnListGrid.setDataSource(dataSource);    	
        asnListGrid.setAutoFetchData(true);
        asnListGrid.setFields(listGridField);
        asnListGrid.getField(DataNameTokens.INV_ASN_ID).setHidden(true);
        asnListGrid.getField(DataNameTokens.INV_ASN_DESTINATIONCODE).setHidden(true);
        asnListGrid.setAutoFitData(Autofit.BOTH);

        layout.setMembers(toolStrip, asnListGrid);
    }
    
    private Window buildAttributeWindow(String itemId) {
		final Window addEditAttributeWindow = new Window();
		addEditAttributeWindow.setWidth(400);
		addEditAttributeWindow.setHeight(400);
		addEditAttributeWindow.setShowMinimizeButton(false);
		addEditAttributeWindow.setIsModal(true);
		addEditAttributeWindow.setShowModalMask(true);
		addEditAttributeWindow.centerInPage();
		addEditAttributeWindow.setTitle("Add/Edit Attribute");
		
		ToolStripButton addButton = new ToolStripButton();
		addButton.setIcon("[SKIN]/icons/business_users_add.png");
		addButton.setTitle("Add");
		
		ToolStripButton removeButton = new ToolStripButton();
		removeButton.setIcon("[SKIN]/icons/business_users_delete.png");
		removeButton.setTitle("Remove");
		
		ToolStrip attributeToolStrip = new ToolStrip();
		attributeToolStrip.setWidth100();
		attributeToolStrip.addButton(addButton);
		attributeToolStrip.addButton(removeButton);

		final DataSource attributeData = GRNData.getItemAttributeData(itemId);			
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();  
		map.put("imei", "IMEI");
		map.put("serial", "Serial Number");
		map.put("expired", "Expired Date");
		attributeData.getField(DataNameTokens.INV_ITEM_ATTRIBUTE_NAME).setValueMap(map);
		
		attributeListGrid = new ListGrid();
		attributeListGrid.setDataSource(attributeData);
		attributeListGrid.setAutoFetchData(true);
		attributeListGrid.setCanEdit(true);
		attributeListGrid.setShowFilterEditor(true);
		attributeListGrid.setSelectionType(SelectionStyle.SIMPLE);
		attributeListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		attributeListGrid.setShowRowNumbers(true);
		
		ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(attributeData);
		ListGridField finalListGridField[] = {listGridField[0], listGridField[1], listGridField[2]};        
		attributeListGrid.setFields(finalListGridField);
		
		attributeListGrid.getField(DataNameTokens.INV_ITEM_ATTRIBUTE_ID).setHidden(true);
		attributeListGrid.getField(DataNameTokens.INV_ITEM_ATTRIBUTE_NAME).setWidth("30%");
		attributeListGrid.getField(DataNameTokens.INV_ITEM_ATTRIBUTE_VALUE).setWidth("70%");
		attributeListGrid.getField(DataNameTokens.INV_ITEM_ATTRIBUTE_NAME).setCanFilter(false);
		attributeListGrid.getField(DataNameTokens.INV_ITEM_ATTRIBUTE_VALUE).setCanFilter(false);
//		attributeListGrid.groupBy(DataNameTokens.INV_ITEM_ATTRIBUTE_NAME);
		 
		VLayout attributeLayout = new VLayout();
		attributeLayout.setHeight100();
		attributeLayout.setWidth100();
		attributeLayout.setMembers(attributeToolStrip, attributeListGrid);
		addEditAttributeWindow.addItem(attributeLayout);
		
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				attributeListGrid.startEditingNew();
			}
		});
		
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				attributeListGrid.removeSelectedData();
				refreshAttributeData();
			}
		});
		
		addEditAttributeWindow.addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClientEvent event) {
				addEditAttributeWindow.destroy();
			}
		});
				
		attributeListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {		
				attributeListGrid.saveAllEdits();
				refreshAttributeData();
			}
		});
		
		attributeListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshAttributeData();
			}
		});

		return addEditAttributeWindow;
	}

    @Override
    public void refreshASNData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                asnListGrid.setData(response.getData());
            }
        };

        asnListGrid.getDataSource().fetchData(asnListGrid.getFilterEditorCriteria(), callBack);
    }
    
    @Override
    public void refreshAttributeData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
            	attributeListGrid.setData(response.getData());
            }
        };

        attributeListGrid.getDataSource().fetchData(attributeListGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public Widget asWidget() {
        return layout;
    }

	@Override
	public Window getGrnCreateWindow() {
		return createGRNWindow;
	}        
}