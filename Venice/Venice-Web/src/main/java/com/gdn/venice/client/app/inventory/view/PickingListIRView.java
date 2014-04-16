package com.gdn.venice.client.app.inventory.view;

import java.util.LinkedHashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.PickingListData;
import com.gdn.venice.client.app.inventory.presenter.PickingListIRPresenter;
import com.gdn.venice.client.app.inventory.view.handler.PickingListIRUiHandler;
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
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Autofit;
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

    RafViewLayout layout, pickerLayout;
    ListGrid packageListGrid, packageDetailListGrid;
    Window pickingListDetailWindow, assignPickerWindow;
    ToolStrip toolStrip;
    ComboBoxItem pickerComboBox; 
    IButton submitButton;
    LinkedHashMap<String, String> pickerMap;

    @Inject
    public PickingListIRView() {
        toolStrip = new ToolStrip();
        toolStrip.setWidth100();
        toolStrip.setPadding(2);
        
        DataSource packageData = PickingListData.getPickingListIRData(1, 20);        
        
    	packageListGrid = new ListGrid();
        packageListGrid.setDataSource(packageData); 
    	packageListGrid.setAutoFetchData(true);
        packageListGrid.setFields(Util.getListGridFieldsFromDataSource(packageData));
        packageListGrid.setSortField(DataNameTokens.INV_PICKINGLISTIR_PACKAGEID);

        packageListGrid.getField(DataNameTokens.INV_PICKINGLISTIR_PACKAGEID).setHidden(true);
        packageListGrid.getField(DataNameTokens.INV_PICKINGLISTIR_PICKERID).setHidden(true);
        packageListGrid.getField(DataNameTokens.INV_PICKINGLISTIR_DETAIL).setWidth(50);
        
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
        
        packageListGrid.getField(DataNameTokens.INV_PICKINGLISTIR_DETAIL).setCellFormatter(new CellFormatter() {			
			@Override
			public String format(Object value, ListGridRecord record, int rowNum, int colNum) {
				return "<span style='color:blue;text-decoration:underline;cursor:hand;cursor:pointer'>"+value+"</span>";
			}
		});

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

        ToolStripButton asignPickerButton = new ToolStripButton();
        asignPickerButton.setIcon("[SKIN]/icons/business_user_next.png");
        asignPickerButton.setTooltip("Assign Picker");
        asignPickerButton.setTitle("Assign Picker");
        
        ToolStripButton exportButton = new ToolStripButton();
        exportButton.setIcon("[SKIN]/icons/book_down.png");
        exportButton.setTooltip("Export");
        exportButton.setTitle("Export");
        
        ToolStripButton uploadButton = new ToolStripButton();
        uploadButton.setIcon("[SKIN]/icons/book_up.png");
        uploadButton.setTooltip("Upload");
        uploadButton.setTitle("Upload");

        toolStrip.addButton(asignPickerButton);
        toolStrip.addButton(exportButton);
        toolStrip.addButton(uploadButton);   
        
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
 				
 				if(sb.length()>0){
 					SC.ask("Package "+sb+" has already been assigned, are you sure you want to reassign the picker?", new BooleanCallback() {
						@Override
						public void execute(Boolean value) {
						    if (value != null && value == true) {	 
						    		buildAssignPickerWindow().show();
						        }	                      
						    }	
						});	
 				}
			}
		});
        
        exportButton.addClickHandler(new ClickHandler() {
     			@Override
     			public void onClick(ClickEvent event) {
     				ListGridRecord[] selectedRecords = packageListGrid.getSelection();     				
     				StringBuilder sbSelectedRecords = new StringBuilder();
     				
     				for (int i = 0; i < selectedRecords.length; i++) {
     					ListGridRecord selectedRecord = selectedRecords[i];     					
     					sbSelectedRecords.append(selectedRecord.getAttributeAsString(DataNameTokens.INV_PICKINGLISTIR_PACKAGEID));
     					
     					if(i != selectedRecords.length -1)
     						sbSelectedRecords.append(";");
     				}
     				
     				String host = GWT.getHostPageBaseURL();

     				if(host.contains("8889")){
     					host = "http://localhost:8090/";
     				}

     				if(host.contains("Venice/")){
     					host = host.substring(0, host.indexOf("Venice/"));
     				}
     												
     				com.google.gwt.user.client.Window.open(host + "Venice/PickingListIRExportServlet?packageIds=" + sbSelectedRecords.toString(), "_blank", null);
     							
     			}
     		});
        
        layout.setMembers(toolStrip, packageListGrid);
    }
    
    private Window buildAssignPickerWindow() {
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
    	pickerComboBox.setValueMap(pickerMap);
        
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
    public void loadPickingListData(LinkedHashMap<String, String> pickerMap) {
    	this.pickerMap = pickerMap;        
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