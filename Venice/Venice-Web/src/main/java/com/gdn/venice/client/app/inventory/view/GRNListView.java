package com.gdn.venice.client.app.inventory.view;

import java.util.LinkedHashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.inventory.data.GRNData;
import com.gdn.venice.client.app.inventory.presenter.GRNListPresenter;
import com.gdn.venice.client.app.inventory.view.handler.GRNListUiHandler;
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
import com.smartgwt.client.widgets.Canvas;
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
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitEvent;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;

/**
 *
 * @author Roland
 */
public class GRNListView extends ViewWithUiHandlers<GRNListUiHandler> implements
	GRNListPresenter.MyView {

    RafViewLayout layout;
    ListGrid grnListGrid, itemListGrid, attributeListGrid;
    Window viewGRNWindow, attributeWindow;

    ToolStrip toolStrip;

    @Inject
    public GRNListView() {
        toolStrip = new ToolStrip();
        toolStrip.setWidth100();
        toolStrip.setPadding(2);

        layout = new RafViewLayout();

        grnListGrid = new ListGrid();
        grnListGrid.setWidth100();
        grnListGrid.setHeight100();
        grnListGrid.setShowAllRecords(true);
        grnListGrid.setSortField(0);

        grnListGrid.setShowFilterEditor(true);
        grnListGrid.setCanResizeFields(true);
        grnListGrid.setShowRowNumbers(true);

        grnListGrid.addCellClickHandler(new CellClickHandler() {
            @Override
            public void onCellClick(CellClickEvent event) {
                ListGridRecord record = grnListGrid.getSelectedRecord();
                buildViewGRNWindow(record).show();
            }
        });
        
        grnListGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshGRNData();
			}
		});
    }

    private Window buildViewGRNWindow(final ListGridRecord record) {
        viewGRNWindow = new Window();
        viewGRNWindow.setWidth(700);
        viewGRNWindow.setHeight(525);
        viewGRNWindow.setTitle("GRN Detail");
        viewGRNWindow.setShowMinimizeButton(false);
        viewGRNWindow.setIsModal(true);
        viewGRNWindow.setShowModalMask(true);
        viewGRNWindow.centerInPage();

        viewGRNWindow.addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClientEvent event) {
                viewGRNWindow.destroy();
            }
        });

        VLayout detailLayout = new VLayout();
        detailLayout.setHeight100();
        detailLayout.setWidth100();

        final DynamicForm asnDetailForm = new DynamicForm();
        asnDetailForm.setPadding(5);
        asnDetailForm.setNumCols(4);

        final String id = record.getAttribute(DataNameTokens.INV_GRN_ID);
        
        final TextItem grnNumberItem = new TextItem(DataNameTokens.INV_GRN_NUMBER, "GRN No");
        grnNumberItem.setValue(record.getAttribute(DataNameTokens.INV_GRN_NUMBER));

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
        
        
        asnDetailForm.setFields(grnNumberItem, reffDateItem, asnNumberItem, reffNumberItem, inventoryTypeItem, supplierCodeItem, 
        		DestinationItem, supplierNameItem);
                
        itemListGrid = buildItemListGrid(id);
        itemListGrid.setCanEdit(false);
        
        viewGRNWindow.addCloseClickHandler(new CloseClickHandler() {
	      public void onCloseClick(CloseClientEvent event) {
	    	  viewGRNWindow.destroy();
	      }
        });
                    
        HLayout buttonSet = new HLayout(5);
        buttonSet.setAlign(Alignment.LEFT);
        
		VLayout headerLayout = new VLayout();
		headerLayout.setWidth100();
		headerLayout.setMargin(10);
		headerLayout.setMembers(buttonSet, asnDetailForm);
        
		Label itemLabel = new Label("<b>GRN Item:</b>");
		itemLabel.setHeight(10);	
           		
        detailLayout.setMembers(headerLayout, itemLabel, itemListGrid);
        viewGRNWindow.addItem(detailLayout);

        return viewGRNWindow;
    }
            
    private ListGrid buildItemListGrid(String grnId){
    	DataSource grnItemData = GRNData.getGRNItemData(grnId, 1, 20);
    	
    	final ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(grnItemData);
		ListGridField attributeField = new ListGridField("ATTRIBUTEFIELD", "View Attribute");  
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
					attributeImg.setSrc("[SKIN]/icons/book_search.png");
					attributeImg.setPrompt("View Attribute");
					attributeImg.setHeight(16);
					attributeImg.setWidth(16);
					attributeImg.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {		
					        attributeWindow = buildAttributeWindow(record.getAttribute(DataNameTokens.INV_GRN_ITEM_ID));	
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
		itemListGrid.setFields(finalListGridField);

    	itemListGrid.setWidth100();
    	itemListGrid.setHeight100();
    	itemListGrid.setShowAllRecords(true);
    	itemListGrid.setSortField(0);
    	itemListGrid.setShowFilterEditor(true);
    	itemListGrid.setCanResizeFields(true);
    	itemListGrid.setShowRowNumbers(true);
    	itemListGrid.setAutoFetchData(true);
        itemListGrid.setCanEdit(true);
    	itemListGrid.setShowRecordComponents(true);          
    	itemListGrid.setShowRecordComponentsByCell(true); 
		        
        itemListGrid.getField(DataNameTokens.INV_GRN_ITEM_ID).setHidden(true);  
		        
        return itemListGrid;
    }
    
    private Window buildAttributeWindow(String itemId) {
		final Window addEditAttributeWindow = new Window();
		addEditAttributeWindow.setWidth(400);
		addEditAttributeWindow.setHeight(400);
		addEditAttributeWindow.setShowMinimizeButton(false);
		addEditAttributeWindow.setIsModal(true);
		addEditAttributeWindow.setShowModalMask(true);
		addEditAttributeWindow.centerInPage();
		addEditAttributeWindow.setTitle("View Attribute");

		ToolStrip attributeToolStrip = new ToolStrip();
		attributeToolStrip.setWidth100();

		final DataSource attributeData = GRNData.getItemAttributeViewData(itemId);
		
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();  
		map.put("imei", "IMEI");
		map.put("serial", "Serial Number");
		map.put("expired", "Expired Date");
		attributeData.getField(DataNameTokens.INV_ITEM_ATTRIBUTE_NAME).setValueMap(map);
		
		attributeListGrid = new ListGrid();
		attributeListGrid.setDataSource(attributeData);
		attributeListGrid.setAutoFetchData(true);
		attributeListGrid.setCanEdit(false);
		attributeListGrid.setShowFilterEditor(false);
		attributeListGrid.setSelectionType(SelectionStyle.SIMPLE);
		attributeListGrid.setSelectionAppearance(SelectionAppearance.ROW_STYLE);
		attributeListGrid.setShowRowNumbers(true);
		attributeListGrid.setSaveLocally(true);
		
		ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(attributeData);
		ListGridField finalListGridField[] = {listGridField[0], listGridField[1], listGridField[2]};        
		attributeListGrid.setFields(finalListGridField);
		
		attributeListGrid.getField(DataNameTokens.INV_ITEM_ATTRIBUTE_ID).setHidden(true);
		attributeListGrid.getField(DataNameTokens.INV_ITEM_ATTRIBUTE_NAME).setWidth("30%");
		attributeListGrid.getField(DataNameTokens.INV_ITEM_ATTRIBUTE_VALUE).setWidth("70%");
		attributeListGrid.groupBy(DataNameTokens.INV_ITEM_ATTRIBUTE_NAME);
				 
		VLayout attributeLayout = new VLayout();
		attributeLayout.setHeight100();
		attributeLayout.setWidth100();
		attributeLayout.setMembers(attributeToolStrip, attributeListGrid);
		addEditAttributeWindow.addItem(attributeLayout);
		
		addEditAttributeWindow.addCloseClickHandler(new CloseClickHandler() {
			public void onCloseClick(CloseClientEvent event) {
				addEditAttributeWindow.destroy();
			}
		});

		return addEditAttributeWindow;
	}

    @Override
    public void loadGRNData(DataSource dataSource) {
        ListGridField listGridField[] = Util.getListGridFieldsFromDataSource(dataSource);

        grnListGrid.setDataSource(dataSource);    	
        grnListGrid.setAutoFetchData(true);
        grnListGrid.setFields(listGridField);
        grnListGrid.getField(DataNameTokens.INV_GRN_ID).setHidden(true);
        grnListGrid.setAutoFitData(Autofit.BOTH);

        layout.setMembers(toolStrip, grnListGrid);
    }

    @Override
    public void refreshGRNData() {
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
}