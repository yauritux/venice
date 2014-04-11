package com.gdn.venice.client.app.inventory.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.GRNData;
import com.gdn.venice.client.app.inventory.presenter.GRNCreatePresenter;
import com.gdn.venice.client.app.inventory.view.handler.GRNCreateUiHandler;
import com.gdn.venice.client.data.RafDataSource;
import com.gdn.venice.client.presenter.MainPagePresenter;
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
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.rpc.RPCCallback;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.DSOperationType;
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
import com.smartgwt.client.widgets.events.HoverEvent;
import com.smartgwt.client.widgets.events.HoverHandler;
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
import com.smartgwt.client.widgets.grid.events.RowOverEvent;
import com.smartgwt.client.widgets.grid.events.RowOverHandler;
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
    private int records;

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
        asnNumberItem.setDisabled(true);
        
        final TextItem reffNumberItem = new TextItem(DataNameTokens.INV_ASN_REFF_NUMBER, "Reff No");
        reffNumberItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_REFF_NUMBER));
        reffNumberItem.setDisabled(true);
        
        final TextItem reffDateItem = new TextItem(DataNameTokens.INV_ASN_REFF_DATE, "Reff Date");
        reffDateItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_REFF_DATE));
        reffDateItem.setDisabled(true);
        
        final TextItem inventoryTypeItem = new TextItem(DataNameTokens.INV_ASN_INVENTORY_TYPE, "Inventory Type");
        inventoryTypeItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_INVENTORY_TYPE));
        inventoryTypeItem.setDisabled(true);
        
        final TextItem supplierCodeItem = new TextItem(DataNameTokens.INV_ASN_SUPPLIER_CODE, "Supplier Code");
        supplierCodeItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_SUPPLIER_CODE));
        supplierCodeItem.setDisabled(true);
        
        final TextItem supplierNameItem = new TextItem(DataNameTokens.INV_ASN_SUPPLIER_NAME, "Supplier Name");
        supplierNameItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_SUPPLIER_NAME));
        supplierNameItem.setDisabled(true);
        
        final TextItem destinationItem = new TextItem(DataNameTokens.INV_ASN_DESTINATION, "Destination");
        destinationItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_DESTINATION));
        destinationItem.setDisabled(true);
        
        final TextItem destinationItemCode = new TextItem(DataNameTokens.INV_ASN_DESTINATIONCODE, "Destination Code");
        destinationItemCode.setValue(record.getAttribute(DataNameTokens.INV_ASN_DESTINATIONCODE));
        destinationItemCode.setDisabled(true);
        
        final TextItem doNumberItem = new TextItem(DataNameTokens.INV_DO_NUMBER, "DO Number");
        doNumberItem.setDisabled(false);
                
        asnDetailForm.setFields(asnNumberItem, reffDateItem, reffNumberItem, inventoryTypeItem, supplierCodeItem, 
        		destinationItem, supplierNameItem, doNumberItem);   
        
        itemListGrid = buildItemListGrid(id);   
                        
        saveButton.addClickHandler(new ClickHandler() {
	          @Override
	          public void onClick(ClickEvent event) {   
					HashMap<String, String> grnDataMap = new HashMap<String, String>();
						
					grnDataMap.put(DataNameTokens.INV_ASN_ID, id);
					grnDataMap.put(DataNameTokens.INV_ASN_DESTINATIONCODE, destinationItemCode.getValueAsString());
					grnDataMap.put(DataNameTokens.INV_ASN_REFF_NUMBER, reffNumberItem.getValueAsString());
					grnDataMap.put(DataNameTokens.INV_ASN_INVENTORY_TYPE, inventoryTypeItem.getValueAsString());
					grnDataMap.put(DataNameTokens.INV_DO_NUMBER, doNumberItem.getValueAsString());
		                
		            HashMap<String, String> grnItemDataMap = new HashMap<String, String>();
					ListGridRecord[] itemRecords = itemListGrid.getRecords();
	
					HashMap<String, String> itemRowMap = new HashMap<String, String>();
										
					for (int i=0;i<itemRecords.length;i++) {
						itemRowMap.put(DataNameTokens.INV_ASN_ITEM_ID, itemRecords[i].getAttributeAsString(DataNameTokens.INV_ASN_ITEM_ID));
						itemRowMap.put(DataNameTokens.INV_POCFF_QTY, itemRecords[i].getAttributeAsString(DataNameTokens.INV_POCFF_QTY));
						grnItemDataMap.put("ITEM"+i, itemRowMap.toString());					
					}
					getUiHandlers().onSaveClicked(grnDataMap, grnItemDataMap, createGRNWindow);
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
					        RPCRequest request = new RPCRequest();
					        request.setActionURL(GWT.getHostPageBaseURL() + "GRNManagementPresenterServlet?method=fetchAttributeName&type=RPC&itemId=" + record.getAttribute(DataNameTokens.INV_POCFF_ITEMID));
					        request.setHttpMethod("POST");
					        request.setUseSimpleHttp(true);
					        RPCManager.sendRequest(request, new RPCCallback() {
					                    @Override
					                    public void execute(RPCResponse response, Object rawData, RPCRequest request) {
					                        String[] fieldName = rawData.toString().split(";");
					                        DataSourceField[] dataSourceFields = new DataSourceField[fieldName.length];
					                        for (int i = 0; i < fieldName.length; i++) {
					                            dataSourceFields[i] = new DataSourceTextField(fieldName[i].trim(), fieldName[i].trim());
					                        }
					                        buildAttributeWindow(record.getAttribute(DataNameTokens.INV_ASN_ITEM_ID), Integer.parseInt(record.getAttribute(DataNameTokens.INV_ASN_ITEM_QTY)), dataSourceFields, rawData.toString()).show();
					                    }
					                });
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
    
    @Override
    public Window buildAttributeWindow(final String asnItemId, final int quantity, final DataSourceField[] dataSourceFields, String fieldName) {
        records = 0;
        attributeWindow = new Window();
        attributeWindow.setWidth(600);
        attributeWindow.setHeight(400);
        attributeWindow.setTitle("Add Attribute Data");
        attributeWindow.setShowMinimizeButton(false);
        attributeWindow.setIsModal(true);
        attributeWindow.setShowModalMask(true);
        attributeWindow.centerInPage();
        
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

        VLayout attributeLayout = new VLayout();
        attributeLayout.setHeight100();
        attributeLayout.setWidth100();

        HLayout buttonSet = new HLayout(5);

        IButton cancelButton = new IButton("Cancel");
        final IButton saveButton = new IButton("Save");
        saveButton.setDisabled(true);

        buttonSet.setAlign(Alignment.CENTER);
        buttonSet.setMembers(saveButton, cancelButton);

        dataSourceFields[0].setPrimaryKey(true);
        RafDataSource ds = new RafDataSource(
                "/response/data/*",
                GWT.getHostPageBaseURL() + GRNCreatePresenter.grnManagementPresenterServlet + "?method=fetchItemAttributeDataFromCache&type=DataSource&fieldName="+fieldName,
                null,
                null,
                null,
                dataSourceFields);
                
        HashMap<String, String> params = new HashMap<String, String>();
		
		if(asnItemId != null) {
			params.put(DataNameTokens.INV_ASN_ITEM_ID, asnItemId);
		}

		ds.getOperationBinding(DSOperationType.FETCH).setDefaultParams(params);
              
        attributeListGrid = new ListGrid();
        attributeListGrid.setWidth100();
        attributeListGrid.setHeight100();
        attributeListGrid.setShowAllRecords(true);
        attributeListGrid.setSaveLocally(true);
        attributeListGrid.setSortField(0);
        attributeListGrid.setShowFilterEditor(false);
        attributeListGrid.setCanEdit(true);
        attributeListGrid.setShowRowNumbers(true);
        attributeListGrid.setAutoFetchData(false); 
        
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(ds);
        attributeListGrid.setDataSource(ds);
        attributeListGrid.setFields(listGridField);
		refreshAttributeData();
                  
        attributeWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                attributeWindow.destroy();
            }
        });
        
        attributeListGrid.addRowOverHandler(new RowOverHandler() {			
			@Override
			public void onRowOver(RowOverEvent event) {
				records = attributeListGrid.getTotalRows();
				if(records>0){
					saveButton.setDisabled(false);
				}
			}
		});
        
        attributeListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {
				attributeListGrid.saveAllEdits();
				records = attributeListGrid.getTotalRows();
				if(records>0){
					saveButton.setDisabled(false);
				}
			}
		});
        
		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {    
				records = attributeListGrid.getTotalRows();
                if (records < quantity) {
                    records++;
                    attributeListGrid.startEditingNew();
                }else{
                	SC.say("Cannot add attribute more than item quantity");
                	return;
                }
			}
		});

		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				records = attributeListGrid.getTotalRows();
				attributeListGrid.removeSelectedData();
				records--;
				
				if(records==0){
					saveButton.setDisabled(true);
				}
			}
		});

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                attributeWindow.destroy();
            }
        });

        saveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                StringBuilder sb = new StringBuilder();
                List<String> attr = new ArrayList<String>();
                int counter = 1;
                for (int r = 0; r < quantity; r++) {
                    for (int c = 0; c < dataSourceFields.length; c++) {
                        attr.add(dataSourceFields[c].getName() + ":" + attributeListGrid.getEditedRecord(r).getAttributeAsString(dataSourceFields[c].getName()) + ":" + counter);
                        counter++;
                    }
                }

                for (String string : attr) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(";");
                    }
                    sb.append(string);
                }

                getUiHandlers().onSaveAttribute(MainPagePresenter.signedInUser,sb.toString(), asnItemId);
            }
        });

        attributeLayout.setMembers(attributeToolStrip, attributeListGrid, buttonSet);
        attributeWindow.addItem(attributeLayout);

        return attributeWindow;
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
	
    @Override
    public Window getAttributeWindow() {
        return attributeWindow;
    }

    @Override
    public ListGrid getAttributeGrid() {
        return attributeListGrid;
    }
}