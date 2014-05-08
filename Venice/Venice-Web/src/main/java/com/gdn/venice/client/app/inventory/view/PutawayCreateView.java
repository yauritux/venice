package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.PutawayData;
import com.gdn.venice.client.app.inventory.presenter.PutawayCreatePresenter;
import com.gdn.venice.client.app.inventory.view.handler.PutawayCreateUiHandler;
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
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitEvent;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

/**
 *
 * @author Roland
 */
public class PutawayCreateView extends ViewWithUiHandlers<PutawayCreateUiHandler> implements
	PutawayCreatePresenter.MyView {

    RafViewLayout layout;
    ListGrid grnListGrid;

    ToolStrip toolStrip;
    ToolStripButton submitButton;
    VLayout headerLayout;
    ComboBoxItem warehouseComboBox, putawayTypeComboBox;
    DynamicForm headerForm;

    @Inject
    public PutawayCreateView() {
        toolStrip = new ToolStrip();
        toolStrip.setWidth100();
        toolStrip.setPadding(2);
        
        submitButton = new ToolStripButton();
		submitButton.setTooltip("Submit");
		submitButton.setTitle("Submit");
		submitButton.setDisabled(true);
		
        toolStrip.addButton(submitButton);

        layout = new RafViewLayout();
        
        grnListGrid = new ListGrid();
        grnListGrid.setAutoFetchData(false);
        grnListGrid.setWidth100();
        grnListGrid.setHeight100();
        grnListGrid.setShowAllRecords(true);
        grnListGrid.setSortField(0);
        grnListGrid.setSelectionType(SelectionStyle.SIMPLE);
        grnListGrid.setShowFilterEditor(true);
        grnListGrid.setCanResizeFields(true);
        grnListGrid.setShowRowNumbers(true);
        grnListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
        
        grnListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshPutawayData();
			}
		}); 
        
        submitButton.addClickHandler(new ClickHandler() {
	          @Override
	          public void onClick(ClickEvent event) {   
	        	  final ListGridRecord[] itemRecords = grnListGrid.getSelection();
	        	  
	        	  if(itemRecords.length<1){
					SC.say("Please select GRN");
					return;
	        	  }else{	        	  
		        	  SC.ask("Are you sure you want to submit this data?", new BooleanCallback() {
	                      @Override
	                      public void execute(Boolean value) {
	                          if (value != null && value == true) {	                        	            					
	          		            HashMap<String, String> itemDataMap = new HashMap<String, String>();
	          					HashMap<String, String> itemRowMap = new HashMap<String, String>();
	          										
	          					for (int i=0;i<itemRecords.length;i++) {
	          						itemRowMap.put(DataNameTokens.INV_PUTAWAY_GRN_ITEMID, itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_ITEMID));
	          						itemRowMap.put(DataNameTokens.INV_PUTAWAY_GRN_GRNNUMBER, itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_GRNNUMBER));
	          						itemRowMap.put(DataNameTokens.INV_PUTAWAY_GRN_ITEMCODE, itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_ITEMCODE));
	          						itemRowMap.put(DataNameTokens.INV_PUTAWAY_GRN_SHELFCODE, itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_SHELFCODE));
	          						itemRowMap.put(DataNameTokens.INV_PUTAWAY_GRN_QTY, itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_QTY));
	          						itemRowMap.put(DataNameTokens.INV_PUTAWAY_GRN_TYPE, putawayTypeComboBox.getValue().toString());
	          						itemDataMap.put("ITEM"+i, itemRowMap.toString());					
	          					}	          					
	          					getUiHandlers().onSubmitClicked(itemDataMap);
	                          }	                      
	                      }	
		        	  });	
	        	 }
	         }
	    });
    }
    
	private void buildGrnListGrid(String warehouseId, String type) {
		if(type.equals("GRN")){
			DataSource grnItemData = PutawayData.getGRNItemData(warehouseId, 1, 20);
			ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(grnItemData);
	        ListGridField finalListGridField[] = {listGridField[1], listGridField[2], listGridField[3], listGridField[4]};
	
	        grnListGrid.setDataSource(grnItemData);         
	        grnListGrid.setFields(finalListGridField);
	
	        grnListGrid.setAutoFitData(Autofit.BOTH);
	        refreshPutawayData();
		}
	}
                    
    @Override
    public void loadPutawayData(LinkedHashMap<String, String> warehouseMap) {    	        
        final DynamicForm headerForm = new DynamicForm();
        headerForm.setPadding(5);
        headerForm.setNumCols(2);
        		
        warehouseComboBox = new ComboBoxItem();
        warehouseComboBox.setTitle("Warehouse");
        warehouseComboBox.setValueMap(warehouseMap);
        
        warehouseComboBox.addChangedHandler(new ChangedHandler() {			
			@Override
			public void onChanged(ChangedEvent event) {
				if(putawayTypeComboBox.getValue()!=null){
					buildGrnListGrid(warehouseComboBox.getValue().toString(), putawayTypeComboBox.getValue().toString());
				}
				putawayTypeComboBox.setDisabled(false);
			}
		});
        
        LinkedHashMap<String, String> type = new LinkedHashMap<String, String>();
        type.put("GRN", "GRN");
        type.put("PICKING_LIST", "Picking List");
        type.put("PACKING_LIST", "Packing List");
        
        putawayTypeComboBox = new ComboBoxItem();
        putawayTypeComboBox.setTitle("Putaway Type");
        putawayTypeComboBox.setValueMap(type);
        putawayTypeComboBox.setDisabled(true);
        
        headerForm.setFields(warehouseComboBox, putawayTypeComboBox);
        
        putawayTypeComboBox.addChangedHandler(new ChangedHandler() {			
			@Override
			public void onChanged(ChangedEvent event) {
				buildGrnListGrid(warehouseComboBox.getValue().toString(), putawayTypeComboBox.getValue().toString());
				submitButton.setDisabled(false);
			}
		});
        
        headerLayout = new VLayout();
		headerLayout.setWidth100();
		headerLayout.setMargin(10);
		headerLayout.setMembers(headerForm);

        layout.setMembers(toolStrip, headerLayout, grnListGrid);
    }      

    @Override
    public void refreshPutawayData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                grnListGrid.setData(response.getData());
            }
        };

        grnListGrid.getDataSource().fetchData(grnListGrid.getFilterEditorCriteria(), callBack);
    }
    
    @Override
    public void printPutawayData(){
			ListGridRecord[] selectedRecords = grnListGrid.getSelection();
				
			StringBuilder sbSelectedRecords = new StringBuilder();
			
			for (int i = 0; i < selectedRecords.length; i++) {
				ListGridRecord selectedRecord = selectedRecords[i];
				
				sbSelectedRecords.append(selectedRecord.getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_ITEMID));
				
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
											
			com.google.gwt.user.client.Window.open(host + "Venice/PutawayExportServlet?grnItemIds="+sbSelectedRecords.toString()
					+"&putawayType="+putawayTypeComboBox.getValue().toString(), "_blank", null);     	
    }
    
    @Override
    public Widget asWidget() {
        return layout;
    }
}