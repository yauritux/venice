package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.PickingListData;
import com.gdn.venice.client.app.inventory.presenter.PickingListIRPresenter;
import com.gdn.venice.client.app.inventory.view.handler.PickingListIRUiHandler;
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
import com.smartgwt.client.rpc.RPCCallback;
import com.smartgwt.client.rpc.RPCManager;
import com.smartgwt.client.rpc.RPCRequest;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.Encoding;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.UploadItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.CellFormatter;
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
public class PickingListIRView extends ViewWithUiHandlers<PickingListIRUiHandler> implements
        PickingListIRPresenter.MyView {

	VLayout headerLayout;
    RafViewLayout layout, pickerLayout;
    ListGrid packageListGrid, packageDetailListGrid;
    Window pickingListDetailWindow, assignPickerWindow, uploadWindow;
    ToolStrip toolStrip;
    ComboBoxItem pickerComboBox, warehouseComboBox; 
    IButton submitButton;
    LinkedHashMap<String, String> warehouseMap;
    ToolStripButton asignPickerButton, exportButton, uploadButton; 

    @Inject
    public PickingListIRView() {
        toolStrip = new ToolStrip();
        toolStrip.setWidth100();
        toolStrip.setPadding(2);                   
        
    	packageListGrid = new ListGrid();       
    	packageListGrid.setAutoFetchData(false); 
        packageListGrid.setWidth100();
        packageListGrid.setHeight100();
        packageListGrid.setShowAllRecords(true);
        packageListGrid.setSortField(0);
        packageListGrid.setShowFilterEditor(true);
        packageListGrid.setCanResizeFields(true);
        packageListGrid.setShowRowNumbers(true);
        packageListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
        packageListGrid.setSelectionType(SelectionStyle.SIMPLE);
        packageListGrid.setAutoFitData(Autofit.BOTH);
                
        packageListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {
            @Override
            public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
                refreshPickingListIRData();
            }
        });  
        
		packageListGrid.addCellClickHandler(new CellClickHandler() {			
			@Override
			public void onCellClick(CellClickEvent event) {
				if (packageListGrid.getField(event.getColNum()).getName().equals(DataNameTokens.INV_PICKINGLISTIR_DETAIL)) {
	                ListGridRecord record = event.getRecord();
	                buildPickingListDetailWindow(record).show();
				}				
			}
		});
        
    	layout = new RafViewLayout();

        asignPickerButton = new ToolStripButton();
        asignPickerButton.setIcon("[SKIN]/icons/business_user_next.png");
        asignPickerButton.setTooltip("Assign Picker");
        asignPickerButton.setTitle("Assign Picker");
        asignPickerButton.setDisabled(true);
        
        exportButton = new ToolStripButton();
        exportButton.setIcon("[SKIN]/icons/book_down.png");
        exportButton.setTooltip("Export");
        exportButton.setTitle("Export");
        exportButton.setDisabled(true);

        uploadButton = new ToolStripButton();
        uploadButton.setIcon("[SKIN]/icons/book_up.png");
        uploadButton.setTooltip("Upload");
        uploadButton.setTitle("Upload");
        uploadButton.setDisabled(true);

        toolStrip.addButton(asignPickerButton);
        toolStrip.addButton(exportButton);
        toolStrip.addButton(uploadButton);   
                        
        exportButton.addClickHandler(new ClickHandler() {
     			@Override
     			public void onClick(ClickEvent event) {
     				ListGridRecord[] records = packageListGrid.getRecords();     				
     				HashSet<String> set = new HashSet<String>();
     				for (int i = 0; i < records.length; i++) {
     					ListGridRecord selectedRecord = records[i];  
     					if(!selectedRecord.getAttributeAsString(DataNameTokens.INV_PICKINGLISTIR_PICKERNAME).isEmpty()){     						
     						set.add(selectedRecord.getAttributeAsString(DataNameTokens.INV_PICKINGLISTIR_PICKERID));
     					}
     				}
     				
     				String host = GWT.getHostPageBaseURL();
     				if(host.contains("8889")){
     					host = "http://localhost:8090/";
     				}

     				if(host.contains("Venice/")){
     					host = host.substring(0, host.indexOf("Venice/"));
     				}
     				
     				if(set.size()>0){
						for(String pickerId : set){		
		     				com.google.gwt.user.client.Window.open(host + "Venice/PickingListIRExportServlet?warehouseId="+warehouseComboBox.getValue().toString()+"&pickerId=" + pickerId, "_blank", null);
						}    			
     				}else{
     					SC.say("There is no package assigned to export, please assigned a picker to package first");
     				}
     			}
     	});
        
        uploadButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				buildUploadWindow().show();
			}
		});
        
        layout.setMembers(toolStrip, packageListGrid);
    }
    
    /**
	 * Builds the upload window as a modal dialog
	 * @return the window once it is built
	 */
	private Window buildUploadWindow() {
		uploadWindow = new Window();
		uploadWindow.setWidth(350);
		uploadWindow.setHeight(120);
		uploadWindow.setTitle("Upload Picking List");
		uploadWindow.setShowMinimizeButton(false);
		uploadWindow.setIsModal(true);
		uploadWindow.setShowModalMask(true);
		uploadWindow.centerInPage();
		
		uploadWindow.addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClientEvent event) {
				uploadWindow.destroy();
			}
		});

		VLayout uploadLayout = new VLayout();
		uploadLayout.setHeight100();
		uploadLayout.setWidth100();

		final DynamicForm uploadForm = new DynamicForm();
		uploadForm.setPadding(5);
		uploadForm.setEncoding(Encoding.MULTIPART);
		uploadForm.setTarget("upload_frame");

		UploadItem reportFileItem = new UploadItem();
		reportFileItem.setTitle("Report");
		uploadForm.setItems(reportFileItem);

		HLayout uploadButtons = new HLayout(5);

		IButton buttonUpload = new IButton("Upload");
		IButton buttonCancel = new IButton("Cancel");

		buttonUpload.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String host = GWT.getHostPageBaseURL();
				if(host.contains(":8889")){
					host = "http://localhost:8090/";
				}else{
					host = host.substring(0, host.lastIndexOf("/", host.length() - 2) + 1);
				}

				uploadForm.setAction(host + "Venice/PickingListIRImportServlet?username=" + MainPagePresenter.signedInUser);								
				uploadForm.submitForm();
				uploadWindow.destroy();
				refreshPickingListIRData();
			}
		});
		
		buttonCancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				uploadWindow.destroy();
			}
		});
		uploadButtons.setAlign(Alignment.CENTER);
		uploadButtons.setMembers(buttonUpload, buttonCancel);

		uploadLayout.setMembers(uploadForm, uploadButtons);
		uploadWindow.addItem(uploadLayout);
		return uploadWindow;
	}
    
    private Window buildAssignPickerWindow(String warehouseName) {
    	assignPickerWindow = new Window();
    	assignPickerWindow.setWidth(400);
    	assignPickerWindow.setHeight(120);
        assignPickerWindow.setTitle("Assign Picker");
        assignPickerWindow.setShowMinimizeButton(false);
        assignPickerWindow.setIsModal(true);
        assignPickerWindow.setShowModalMask(true);
        assignPickerWindow.centerInPage(); 
        
        IButton cancelButton = new IButton("Cancel");
        submitButton = new IButton("Submit");
		submitButton.setDisabled(true);	
		
		HLayout buttonSet = new HLayout(5);
		
        buttonSet.setAlign(Alignment.CENTER);
        buttonSet.setMembers(submitButton, cancelButton);
		
		DynamicForm pickerForm = new DynamicForm();
		pickerForm.setWidth100();
		pickerForm.setHeight(50);
		pickerForm.setNumCols(2);
		pickerForm.setPadding(5);
		
    	pickerComboBox = new ComboBoxItem();
    	pickerComboBox.setTitle("Picker");
		    	
		RPCRequest requestPicker = new RPCRequest();
		requestPicker.setActionURL(GWT.getHostPageBaseURL() + "PickingListManagementPresenterServlet?method=fetchPickerComboBoxData&type=RPC&warehouseName="+warehouseName+"&page=1&limit=50");
		requestPicker.setHttpMethod("POST");
		requestPicker.setUseSimpleHttp(true);
		requestPicker.setShowPrompt(false);
				
		RPCManager.sendRequest(requestPicker, new RPCCallback () {
					public void execute(RPCResponse response, Object rawData, RPCRequest request) {
						String rpcResponse = rawData.toString();
						String xmlData = rpcResponse;
						
						LinkedHashMap<String, String> pickerMap = Util.formComboBoxMap(Util.formHashMapfromXML(xmlData));
						pickerComboBox.setValueMap(pickerMap);
					}
		});
		        
    	pickerComboBox.addChangedHandler(new ChangedHandler() {			
			@Override
			public void onChanged(ChangedEvent event) {
				submitButton.setDisabled(false);
			}
		});
    	
		pickerForm.setFields(pickerComboBox);
		
		pickerLayout = new RafViewLayout();
		pickerLayout.setMembers(pickerForm, buttonSet);
		
		assignPickerWindow.addItem(pickerLayout);        
        assignPickerWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
            	assignPickerWindow.destroy();
            }
        });
        
        cancelButton.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				assignPickerWindow.destroy();
			}
		});
        
        submitButton.addClickHandler(new ClickHandler() {
	          @Override
	          public void onClick(ClickEvent event) {  
			    final ListGridRecord[] packageRecords = packageListGrid.getSelection();
	        	  if(packageRecords.length<1){
					SC.say("Please select package to be assigned");
					return;
	        	  }else{	        	  
						SC.ask("Are you sure you want to assign this picker?", new BooleanCallback() {
						@Override
						public void execute(Boolean value) {
						    if (value != null && value == true) {	 
						    	StringBuilder sb = new StringBuilder();
								for (int i = 0; i < packageRecords.length; i++) {
									ListGridRecord selectedRecord = packageRecords[i];             					
									sb.append(selectedRecord.getAttributeAsString(DataNameTokens.INV_PICKINGLISTIR_PACKAGEID));
									
									if(i != packageRecords.length -1)
										sb.append(";");
									}	 
									getUiHandlers().onSubmitClicked(sb.toString(), pickerComboBox.getValue().toString());
						        }	                      
						    }	
						});	
	        	  }
	         }
	    });
                
    	return assignPickerWindow;
    }

    private Window buildPickingListDetailWindow(final ListGridRecord record) {
        pickingListDetailWindow = new Window();
        pickingListDetailWindow.setWidth(1000);
        pickingListDetailWindow.setHeight(425);
        pickingListDetailWindow.setTitle("Picking List Detail");
        pickingListDetailWindow.setShowMinimizeButton(false);
        pickingListDetailWindow.setIsModal(true);
        pickingListDetailWindow.setShowModalMask(true);
        pickingListDetailWindow.centerInPage();      

        DataSource packageDetailData = PickingListData.getPickingListIRDetailData(record.getAttributeAsString(DataNameTokens.INV_PICKINGLISTIR_PACKAGEID));
        ListGridField listGridSalesField[] = Util.getListGridFieldsFromDataSource(packageDetailData);

        packageDetailListGrid = new ListGrid();
        packageDetailListGrid.setDataSource(packageDetailData);
        packageDetailListGrid.setFields(listGridSalesField);
        packageDetailListGrid.setWidth100();
        packageDetailListGrid.setHeight100();
        packageDetailListGrid.setPadding(5);
        packageDetailListGrid.setMargin(5);
        packageDetailListGrid.setAutoFetchData(true);
        packageDetailListGrid.setUseAllDataSourceFields(false);
        packageDetailListGrid.setShowFilterEditor(false);
        packageDetailListGrid.setCanResizeFields(true);
        packageDetailListGrid.setShowRowNumbers(true);
        packageDetailListGrid.setShowAllRecords(true);
        packageDetailListGrid.setSortField(0);
        packageDetailListGrid.setSelectionAppearance(SelectionAppearance.ROW_STYLE);
        packageDetailListGrid.setSelectionType(SelectionStyle.SIMPLE);
        packageDetailListGrid.getField(DataNameTokens.INV_PICKINGLISTIR_SHELFCODE).setWidth("40%");
        
        pickingListDetailWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
            	pickingListDetailWindow.destroy();
            }
        });

        VLayout detailLayout = new VLayout();
        detailLayout.setHeight100();
        detailLayout.setWidth100();
        detailLayout.setMembers(packageDetailListGrid);
        pickingListDetailWindow.addItem(detailLayout);

        return pickingListDetailWindow;
    }
	
    @Override
    public void loadPickingListData(LinkedHashMap<String, String> warehouseMap) {     
    	this.warehouseMap = warehouseMap;
    	
    	final DynamicForm warehouseForm = new DynamicForm();
        warehouseForm.setPadding(5);
        warehouseForm.setNumCols(2);

        warehouseComboBox = new ComboBoxItem();
        warehouseComboBox.setTitle("Warehouse");
        warehouseComboBox.setValueMap(warehouseMap);

        warehouseForm.setFields(warehouseComboBox);

        warehouseComboBox.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent event) {
                buildPackageListGrid(warehouseComboBox.getValue().toString());
            }
        });

        headerLayout = new VLayout();
        headerLayout.setWidth100();
        headerLayout.setMargin(10);
        headerLayout.setMembers(warehouseForm);
        
        asignPickerButton.addClickHandler(new ClickHandler() {			
			@Override
			public void onClick(ClickEvent event) {
				ListGridRecord[] selectedRecords = packageListGrid.getSelection(); 				 				
				StringBuilder sb = new StringBuilder();
				
 				for (int i = 0; i < selectedRecords.length; i++) {
 					ListGridRecord selectedRecord = selectedRecords[i];
 					if(!selectedRecord.getAttributeAsString(DataNameTokens.INV_PICKINGLISTIR_PICKERNAME).isEmpty()){
 	 					sb.append(selectedRecord.getAttributeAsString(DataNameTokens.INV_PICKINGLISTIR_PACKAGECODE));
 	 					if(i != selectedRecords.length -1) sb.append(", ");
 					}
 				}
 				
 				if(selectedRecords.length>0){
 	 				if(sb.length()>0){
	 					SC.ask("Package "+sb+" has already been assigned, are you sure you want to reassign the picker?", new BooleanCallback() {
							@Override
							public void execute(Boolean value) {
							    if (value != null && value == true) {	 
							    		buildAssignPickerWindow(warehouseComboBox.getDisplayValue().toString()).show();
							        }	                      
							    }	
							});	
 	 				}else{
 	 					buildAssignPickerWindow(warehouseComboBox.getDisplayValue().toString()).show();
 	 				}
 				}else{
 					SC.say("Please select the package you want to assign");
 				} 			
			}
		});

        layout.setMembers(toolStrip, headerLayout, packageListGrid);        
    }
    
    private void buildPackageListGrid(String warehouseId) {
    	DataSource packageData = PickingListData.getPickingListIRData(warehouseId, 1, 20); 
    	
        Map<String, String> type = new HashMap<String, String>();
        type.put("CONSIGNMENT_COMMISION", "Consignment Commission");
        type.put("TRADING", "Trading");
        type.put("CONSIGNMENT_TRADING", "Consignment Trading");
        packageData.getField(DataNameTokens.INV_PICKINGLISTIR_INVENTORYTYPE).setValueMap(type);
        
        Map<String, String> irType = new HashMap<String, String>();
        irType.put("EXPENSE_ASSET", "Expense Asset");
        irType.put("TRANSFER_WAREHOUSE", "Transfer Warehouse");
        irType.put("CONVERT_SKU_ASSEMBLY", "Convert SKU Assembly");
        irType.put("CONVERT_SKU_DISASSEMBLY", "Convert SKU Disassembly");
        packageData.getField(DataNameTokens.INV_PICKINGLISTIR_IRTYPE).setValueMap(irType);
        
    	packageListGrid.setDataSource(packageData); 
    	
    	ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(packageData);
        ListGridField finalListGridField[] = {listGridField[1], listGridField[2], listGridField[3], listGridField[4], listGridField[6]};

        packageListGrid.setFields(finalListGridField);    	
        packageListGrid.setSortField(DataNameTokens.INV_PICKINGLISTIR_PACKAGEID);
        packageListGrid.getField(DataNameTokens.INV_PICKINGLISTIR_DETAIL).setWidth(50);
        packageListGrid.setAutoFitData(Autofit.BOTH);
        
        packageListGrid.getField(DataNameTokens.INV_PICKINGLISTIR_DETAIL).setCellFormatter(new CellFormatter() {			
			@Override
			public String format(Object value, ListGridRecord record, int rowNum, int colNum) {
				return "<span style='color:blue;text-decoration:underline;cursor:hand;cursor:pointer'>"+value+"</span>";
			}
		});
        
        asignPickerButton.setDisabled(false);
        exportButton.setDisabled(false);
        uploadButton.setDisabled(false);
        
        refreshPickingListIRData();
    }

    @Override
    public void refreshPickingListIRData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                packageListGrid.setData(response.getData());
            }
        };

        packageListGrid.getDataSource().fetchData(packageListGrid.getFilterEditorCriteria(), callBack);
    }

    @Override
    public Widget asWidget() {
        return layout;
    }

    @Override
    public Window getPickingListDetailWindow() {
        return pickingListDetailWindow;
    }
    
    @Override
    public Window getAssignPickerWindow() {
        return assignPickerWindow;
    }    
}