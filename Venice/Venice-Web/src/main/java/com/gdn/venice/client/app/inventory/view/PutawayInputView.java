package com.gdn.venice.client.app.inventory.view;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.PutawayData;
import com.gdn.venice.client.app.inventory.presenter.PutawayInputPresenter;
import com.gdn.venice.client.app.inventory.view.handler.PutawayInputUiHandler;
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
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitEvent;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 *
 * @author Roland
 */
public class PutawayInputView extends ViewWithUiHandlers<PutawayInputUiHandler> implements
	PutawayInputPresenter.MyView {

    RafViewLayout layout;
    ListGrid putawayListGrid, grnListGrid;

    VLayout headerLayout;
    ComboBoxItem warehouseComboBox;
    Window putawayDetailWindow;
    DynamicForm itemDetailForm;

    @Inject
    public PutawayInputView() {        
        layout = new RafViewLayout();
        
        putawayListGrid = new ListGrid();
        putawayListGrid.setAutoFetchData(false);
        putawayListGrid.setWidth100();
        putawayListGrid.setHeight100();
        putawayListGrid.setShowAllRecords(true);
        putawayListGrid.setSortField(0);
        putawayListGrid.setSelectionType(SelectionStyle.SINGLE);
        putawayListGrid.setShowFilterEditor(true);
        putawayListGrid.setCanResizeFields(true);
        putawayListGrid.setShowRowNumbers(true);
        putawayListGrid.setSelectionAppearance(SelectionAppearance.ROW_STYLE);
        
        putawayListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshPutawayData();
			}
		}); 
        
        putawayListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = putawayListGrid.getSelectedRecord();

                buildPutawayDetailWindow(record).show();                
            }
        });        
    }
    
	private void buildPutawayListGrid(String warehouseId) {
		DataSource putawayData = PutawayData.getPutawayData(warehouseId, 1, 20);
        LinkedHashMap<String, String> type = new LinkedHashMap<String, String>();
        type.put("GRN", "GRN");
        type.put("PICKING_LIST", "Picking List");
        type.put("PACKING_LIST", "Packing List");
        
        putawayData.getField(DataNameTokens.INV_PUTAWAY_TYPE).setValueMap(type);
        
		ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(putawayData);
        ListGridField finalListGridField[] = {listGridField[1], listGridField[2], listGridField[3]};

        putawayListGrid.setDataSource(putawayData);         
        putawayListGrid.setFields(finalListGridField);
        putawayListGrid.setAutoFitData(Autofit.BOTH);
        
        refreshPutawayData();
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
				buildPutawayListGrid(warehouseComboBox.getValue().toString());
			}
		});
                      
        headerForm.setFields(warehouseComboBox);
        
        headerLayout = new VLayout();
		headerLayout.setWidth100();
		headerLayout.setMargin(10);
		headerLayout.setMembers(headerForm);

        layout.setMembers(headerLayout, putawayListGrid);
    } 
    
    private Window buildPutawayDetailWindow(final ListGridRecord record) {
    	putawayDetailWindow = new Window();
    	putawayDetailWindow.setWidth(925);
    	putawayDetailWindow.setHeight(500);
    	putawayDetailWindow.setTitle("Putaway Detail");
    	putawayDetailWindow.setShowMinimizeButton(false);
    	putawayDetailWindow.setIsModal(true);
    	putawayDetailWindow.setShowModalMask(true);
    	putawayDetailWindow.centerInPage();
        
        IButton saveButton = new IButton("Save");
        HLayout buttonSet = new HLayout(5);
        buttonSet.setAlign(Alignment.LEFT);
        buttonSet.setMembers(saveButton);
                        
        putawayDetailWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {                
            	putawayDetailWindow.destroy();	                        	                
            }
        });          
        
		VLayout headerLayout = new VLayout();
		headerLayout.setWidth(850);
		headerLayout.setHeight(50);
		headerLayout.setMargin(10);
		headerLayout.setMembers(buttonSet);  
		
		grnListGrid = new ListGrid();
        grnListGrid.setAutoFetchData(false);
        grnListGrid.setWidth(850);
        grnListGrid.setHeight100();
        grnListGrid.setShowAllRecords(true);
        grnListGrid.setSortField(0);
        grnListGrid.setSelectionType(SelectionStyle.SINGLE);
        grnListGrid.setShowFilterEditor(false);
        grnListGrid.setCanResizeFields(true);
        grnListGrid.setShowRowNumbers(true);
        grnListGrid.setSelectionAppearance(SelectionAppearance.ROW_STYLE);
        grnListGrid.setSaveLocally(true);
        
        DataSource grnItemData = PutawayData.getPutawayDetailGRNItemData(record.getAttribute(DataNameTokens.INV_PUTAWAY_GRN_ID), 1, 20);
		ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(grnItemData);
		
		ListGridField storageItem = new ListGridField(DataNameTokens.INV_PUTAWAY_GRN_SHELFCODE_INPUT, "Input Storage Code");
		ListGridField qtyItem = new ListGridField(DataNameTokens.INV_PUTAWAY_GRN_QTY_INPUT, "Input Qty");
		storageItem.setCanEdit(true);
		qtyItem.setCanEdit(true);
        ListGridField finalListGridField[] = {listGridField[1], listGridField[2], listGridField[3], listGridField[4], storageItem, qtyItem};

        grnListGrid.setDataSource(grnItemData);         
        grnListGrid.setFields(finalListGridField);

        grnListGrid.setAutoFitData(Autofit.BOTH);
        refreshGrnData();
        
		HLayout detailLayout = new HLayout();
		detailLayout.setWidth(850);
		detailLayout.setMargin(10);
		detailLayout.setMembers(grnListGrid);
		
        VLayout bigLayout = new VLayout();
        bigLayout.setWidth(850);
        bigLayout.setHeight100();
        bigLayout.setMembers(headerLayout, detailLayout);
        putawayDetailWindow.addItem(bigLayout);
        
        saveButton.addClickHandler(new ClickHandler() {
	        @Override
	        public void onClick(ClickEvent event) {   
	      	  final ListGridRecord[] itemRecords = grnListGrid.getRecords();      	  	        	                       	            					
	            final HashMap<String, String> itemDataMap = new HashMap<String, String>();
				HashMap<String, String> itemRowMap = new HashMap<String, String>();
									
				for (int i=0;i<itemRecords.length;i++) {    
					itemRowMap.put(DataNameTokens.INV_PUTAWAY_ID, record.getAttributeAsString(DataNameTokens.INV_PUTAWAY_ID));
					itemRowMap.put(DataNameTokens.INV_PUTAWAY_GRN_ITEMID, itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_ITEMID));
					itemRowMap.put(DataNameTokens.INV_PUTAWAY_GRN_ITEMCODE, itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_ITEMCODE));									
					itemRowMap.put(DataNameTokens.INV_PUTAWAY_GRN_WAREHOUSEITEMID, itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_WAREHOUSEITEMID));
									
					if(itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_SHELFCODE_INPUT)==null){
						SC.say("Please input storage code for item "+itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_ITEMCODE));
						return;
					}else{
						itemRowMap.put(DataNameTokens.INV_PUTAWAY_GRN_SHELFCODE_INPUT, itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_SHELFCODE_INPUT));
					}
					
					if(itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_QTY_INPUT)==null || itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_QTY_INPUT).equals("0")){
						SC.say("Please check quantity for item "+itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_ITEMCODE));
						return;
					}else{  
						itemRowMap.put(DataNameTokens.INV_PUTAWAY_GRN_QTY_INPUT, itemRecords[i].getAttributeAsString(DataNameTokens.INV_PUTAWAY_GRN_QTY_INPUT));						
					}
					
					itemDataMap.put("ITEM"+i, itemRowMap.toString());
				}	
				
	        	SC.ask("Are you sure you want to save this data?", new BooleanCallback() {
	                @Override
	                public void execute(Boolean value) {
	                    if (value != null && value == true) {	
	                    	getUiHandlers().onSaveClicked(itemDataMap, putawayDetailWindow);
	                    }	
	                }    		                  
	        	});                    	                      	      	 
	       }
        });

        return putawayDetailWindow;
    }

    @Override
    public void refreshPutawayData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                putawayListGrid.setData(response.getData());
            }
        };

        putawayListGrid.getDataSource().fetchData(putawayListGrid.getFilterEditorCriteria(), callBack);
    }
    
    @Override
    public void refreshGrnData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                grnListGrid.setData(response.getData());
            }
        };

        grnListGrid.getDataSource().fetchData(grnListGrid.getFilterEditorCriteria(), callBack);
    }
    
    @Override
    public Widget asWidget() {
        return layout;
    }
    
	@Override
	public Window getPutawayDetailWindow() {
		return putawayDetailWindow;
	} 
}