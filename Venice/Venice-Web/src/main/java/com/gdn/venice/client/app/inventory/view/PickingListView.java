package com.gdn.venice.client.app.inventory.view;

import java.util.LinkedHashMap;

import com.gdn.venice.client.app.inventory.data.PickingListData;
import com.gdn.venice.client.app.inventory.presenter.PickingListPresenter;
import com.gdn.venice.client.app.inventory.view.handler.PickingListUiHandler;
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
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

/**
 *
 * @author Roland
 */
public class PickingListView extends ViewWithUiHandlers<PickingListUiHandler> implements
	PickingListPresenter.MyView {

    RafViewLayout layout;
    ListGrid warehouseItemListGrid;
    Window pickingListDetailWindow, attributeWindow;

    ToolStrip toolStrip;
    VLayout headerLayout;
    ComboBoxItem warehouseItemComboBox;

    @Inject
    public PickingListView() {
        toolStrip = new ToolStrip();
        toolStrip.setWidth100();
        toolStrip.setPadding(2);

		ToolStripButton printButton = new ToolStripButton();
		printButton.setIcon("[SKIN]/icons/printer.png");
		printButton.setTooltip("Print");
		printButton.setTitle("Print");
        
        toolStrip.addButton(printButton);

        layout = new RafViewLayout();
        
        warehouseItemListGrid = new ListGrid();
        warehouseItemListGrid.setAutoFetchData(false);
        warehouseItemListGrid.setWidth100();
        warehouseItemListGrid.setHeight100();
        warehouseItemListGrid.setShowAllRecords(true);
        warehouseItemListGrid.setSortField(0);

        warehouseItemListGrid.setShowFilterEditor(true);
        warehouseItemListGrid.setCanResizeFields(true);
        warehouseItemListGrid.setShowRowNumbers(true);
        warehouseItemListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);

        warehouseItemListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = warehouseItemListGrid.getSelectedRecord();
                buildPickingListDetailWindow(record).show();
            }
        });
        
        warehouseItemListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshPickingListData();
			}
		});     
    }
    
	private void buildWarehouseItemListGrid(String warehouseId) {
		DataSource warehouseItemData = PickingListData.getPickingListData(warehouseId, 1, 20);
		ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(warehouseItemData);
        ListGridField finalListGridField[] = {listGridField[1], listGridField[2], listGridField[3], listGridField[4], listGridField[5]};

        warehouseItemListGrid.setDataSource(warehouseItemData);         
        warehouseItemListGrid.setFields(finalListGridField);

        warehouseItemListGrid.setAutoFitData(Autofit.BOTH);
        refreshPickingListData();
	}

    private Window buildPickingListDetailWindow(final ListGridRecord record) {
        pickingListDetailWindow = new Window();
        pickingListDetailWindow.setWidth(700);
        pickingListDetailWindow.setHeight(525);
        pickingListDetailWindow.setTitle("Picking List Detail");
        pickingListDetailWindow.setShowMinimizeButton(false);
        pickingListDetailWindow.setIsModal(true);
        pickingListDetailWindow.setShowModalMask(true);
        pickingListDetailWindow.centerInPage();
        
        IButton submitButton = new IButton("Submit");
        HLayout buttonSet = new HLayout(5);
        buttonSet.setAlign(Alignment.LEFT);
        buttonSet.setMembers(submitButton);
                
        pickingListDetailWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                pickingListDetailWindow.destroy();
            }
        });

        VLayout detailLayout = new VLayout();
        detailLayout.setHeight100();
        detailLayout.setWidth100();

        final DynamicForm asnDetailForm = new DynamicForm();
        asnDetailForm.setPadding(5);
        asnDetailForm.setNumCols(4);

//        final String id = record.getAttribute(DataNameTokens.INV_ASN_ID);                
//        asnDetailForm.setFields(asnNumberItem, reffDateItem, reffNumberItem, inventoryTypeItem, supplierCodeItem, 
//        		DestinationItem, supplierNameItem);                   
                
        submitButton.addClickHandler(new ClickHandler() {
	          @Override
	          public void onClick(ClickEvent event) {   
//				getUiHandlers().onSaveClicked(grnDataMap, grnItemDataMap, pickingListDetailWindow);	        	  
	  		}
	    });
                        
        pickingListDetailWindow.addCloseClickHandler(new CloseClickHandler() {
	      public void onCloseClick(CloseClientEvent event) {
	    	  pickingListDetailWindow.destroy();
	      }
        });
        
		VLayout headerLayout = new VLayout();
		headerLayout.setWidth100();
		headerLayout.setMargin(10);
		headerLayout.setMembers(buttonSet, asnDetailForm);
        
		Label itemLabel = new Label("<b>Item Detail:</b>");
		itemLabel.setHeight(10);	
		
		ToolStrip itemToolStrip = new ToolStrip();
		itemToolStrip.setWidth100();
           	
        detailLayout.setMembers(headerLayout, itemLabel, itemToolStrip);
        pickingListDetailWindow.addItem(detailLayout);

        return pickingListDetailWindow;
    }
                
    @Override
    public void loadPickingListData(LinkedHashMap<String, String> warehouseMap) {    	        
        final DynamicForm warehouseForm = new DynamicForm();
        warehouseForm.setPadding(5);
        warehouseForm.setNumCols(2);
        		
        warehouseItemComboBox = new ComboBoxItem();
        warehouseItemComboBox.setTitle("Warehouse");
        warehouseItemComboBox.setValueMap(warehouseMap);
        
        warehouseForm.setFields(warehouseItemComboBox);
        
        warehouseItemComboBox.addChangedHandler(new ChangedHandler() {			
			@Override
			public void onChanged(ChangedEvent event) {
				buildWarehouseItemListGrid(warehouseItemComboBox.getValue().toString());
			}
		});
        
        headerLayout = new VLayout();
		headerLayout.setWidth100();
		headerLayout.setMargin(10);
		headerLayout.setMembers(warehouseForm);

        layout.setMembers(toolStrip, headerLayout, warehouseItemListGrid);
    }      

    @Override
    public void refreshPickingListData() {
        DSCallback callBack = new DSCallback() {
            @Override
            public void execute(DSResponse response, Object rawData, DSRequest request) {
                warehouseItemListGrid.setData(response.getData());
            }
        };

        warehouseItemListGrid.getDataSource().fetchData(warehouseItemListGrid.getFilterEditorCriteria(), callBack);
    }
    
    @Override
    public Widget asWidget() {
        return layout;
    }

	@Override
	public Window getPickingListDetailWindow() {
		return pickingListDetailWindow;
	}        
}