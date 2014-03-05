package com.gdn.venice.client.app.inventory.view;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.ASNData;
import com.gdn.venice.client.app.inventory.presenter.ASNListPresenter;
import com.gdn.venice.client.app.inventory.view.handler.ASNListUiHandler;
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
import com.smartgwt.client.types.SummaryFunctionType;
import com.smartgwt.client.util.SC;
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
public class ASNListView extends ViewWithUiHandlers<ASNListUiHandler> implements
	ASNListPresenter.MyView {

    RafViewLayout layout;
    ListGrid asnListGrid, itemListGrid;
    Window asnDetailWindow;

    ToolStrip toolStrip;

    @Inject
    public ASNListView() {
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
                buildASNDetailWindow(record).show();
            }
        });
        
        asnListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshASNData();
			}
		});
    }

    private Window buildASNDetailWindow(final ListGridRecord record) {
        asnDetailWindow = new Window();
        asnDetailWindow.setWidth(700);
        asnDetailWindow.setHeight(525);
        asnDetailWindow.setTitle("ASN Detail");
        asnDetailWindow.setShowMinimizeButton(false);
        asnDetailWindow.setIsModal(true);
        asnDetailWindow.setShowModalMask(true);
        asnDetailWindow.centerInPage();

        asnDetailWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                asnDetailWindow.destroy();
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
        
        final TextItem estDateItem = new TextItem(DataNameTokens.INV_ASN_EST_DATE, "Estimation Date");
        estDateItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_EST_DATE));
        
        final TextItem inventoryTypeItem = new TextItem(DataNameTokens.INV_ASN_INVENTORY_TYPE, "Inventory Type");
        inventoryTypeItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_INVENTORY_TYPE));
        
        final TextItem supplierCodeItem = new TextItem(DataNameTokens.INV_ASN_SUPPLIER_CODE, "Supplier Code");
        supplierCodeItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_SUPPLIER_CODE));
        
        final TextItem supplierNameItem = new TextItem(DataNameTokens.INV_ASN_SUPPLIER_NAME, "Supplier Name");
        supplierNameItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_SUPPLIER_NAME));
        
        final TextItem DestinationItem = new TextItem(DataNameTokens.INV_ASN_DESTINATION, "Destination");
        DestinationItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_DESTINATION));
        
        final TextAreaItem notesItem = new TextAreaItem(DataNameTokens.INV_ASN_SPECIAL_NOTES, "Special Notes");
        notesItem.setValue(record.getAttribute(DataNameTokens.INV_ASN_SPECIAL_NOTES));
        
        
        asnDetailForm.setFields(asnNumberItem, reffDateItem, reffNumberItem, estDateItem, inventoryTypeItem, supplierCodeItem, 
        		DestinationItem, supplierNameItem, notesItem);
        
        asnDetailForm.setDisabled(true);
        
        final IButton closeButton = new IButton("Close");
        
        itemListGrid = buildItemListGrid(id);
        itemListGrid.setCanEdit(false);
        
        asnDetailWindow.addCloseClickHandler(new CloseClickHandler() {
	      public void onCloseClick(CloseClientEvent event) {
	    	  asnDetailWindow.destroy();
	      }
        });
        
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	asnDetailWindow.destroy();
            }
        });
        
        HLayout buttonSet = new HLayout(5);
        buttonSet.setAlign(Alignment.LEFT);        
        buttonSet.setMembers(closeButton);
        
		VLayout headerLayout = new VLayout();
		headerLayout.setWidth100();
		headerLayout.setMargin(10);
		headerLayout.setMembers(buttonSet, asnDetailForm);
        
		Label itemLabel = new Label("<b>ASN Item:</b>");
		itemLabel.setHeight(10);	
           		
        detailLayout.setMembers(headerLayout, itemLabel, itemListGrid);
        asnDetailWindow.addItem(detailLayout);

        return asnDetailWindow;
    }
            
    private ListGrid buildItemListGrid(String asnId){
    	itemListGrid = new ListGrid();
    	itemListGrid.setWidth100();
    	itemListGrid.setHeight100();
    	itemListGrid.setShowAllRecords(true);
    	itemListGrid.setSortField(0);

    	itemListGrid.setShowFilterEditor(true);
    	itemListGrid.setCanResizeFields(true);
    	itemListGrid.setShowRowNumbers(true);
    	itemListGrid.setShowFilterEditor(true);

    	DataSource asnItemData = ASNData.getASNItemData(asnId, 1, 20);
		itemListGrid.setDataSource(asnItemData);
		itemListGrid.setFields(Util.getListGridFieldsFromDataSource(asnItemData));
		        
    	itemListGrid.setAutoFetchData(true);
        itemListGrid.getField(DataNameTokens.INV_ASN_ITEM_ID).setHidden(true);  
        itemListGrid.getField(DataNameTokens.INV_POCFF_VOLUME).setSummaryFunction(SummaryFunctionType.SUM);
        itemListGrid.getField(DataNameTokens.INV_POCFF_ITEMWEIGHT).setSummaryFunction(SummaryFunctionType.SUM);
        itemListGrid.setShowGroupSummary(true);
        itemListGrid.setShowGridSummary(true);
		        
        return itemListGrid;
    }

    @Override
    public void loadASNData(DataSource dataSource) {
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(dataSource);

        asnListGrid.setDataSource(dataSource);    	
        asnListGrid.setAutoFetchData(true);
        asnListGrid.setFields(listGridField);
        asnListGrid.setDataSource(dataSource);
        asnListGrid.getField(DataNameTokens.INV_ASN_ID).setHidden(true);
        asnListGrid.getField(DataNameTokens.INV_ASN_SPECIAL_NOTES).setHidden(true);
        asnListGrid.setAutoFitData(Autofit.BOTH);

        layout.setMembers(toolStrip, asnListGrid);
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
    public Widget asWidget() {
        return layout;
    }
}