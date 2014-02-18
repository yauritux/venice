package com.gdn.venice.client.app.finance.view;

import java.util.LinkedHashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.finance.presenter.FinSalesSettlementReportPresenter;
import com.gdn.venice.client.app.finance.view.handlers.FinSalesSettlementReportUiHandlers;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SummaryFunctionType;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitEvent;
import com.smartgwt.client.widgets.grid.events.FilterEditorSubmitHandler;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;

/**
 * The view class for the COA Setup screen
 * 
 * <p>
 * <b>author:</b> <a href="mailto:christian.suwuh@pwsindonesia.com">Christian Suwuh</a>
 * <p>
 * <b>version:</b> 1.0
 * <p>
 * <b>since:</b> 2011
 * 
 */
public class FinSalesSettlementReportView extends ViewWithUiHandlers<FinSalesSettlementReportUiHandlers> implements 
	FinSalesSettlementReportPresenter.MyView {
	
	private RafViewLayout salesSattlementReportLayout;
	ListGrid settlementRecordGrid;
	ToolStripButton exportButton = null;

	LinkedHashMap<String, String> merchantParam =null;
	/*
	 * Build the view and inject it
	 */
	@Inject
	public FinSalesSettlementReportView() {
		salesSattlementReportLayout = new RafViewLayout();	 
	}
	public void loadDataSalesSettlementRecord(LinkedHashMap<String, String> merchantParam){
		//Toolstrip atas
		ToolStrip toolStrip = new ToolStrip();
		toolStrip.setWidth100();
		DataSource dataSource = getUiHandlers().onGetSalesSettlementRecord();
		
		exportButton = new ToolStripButton();
		exportButton.setIcon("[SKIN]/icons/notes_accept.png");
		exportButton.setTooltip("Export to Excel");
		exportButton.setTitle("Export");
		exportButton.disable();
		
		toolStrip.addButton(exportButton);
		
		dataSource.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENMERCHANTPRODUCT_VENMERCHANT_WCSMERCHANTID).setValueMap(merchantParam);
		
		settlementRecordGrid = new ListGrid();
		settlementRecordGrid.setAutoFetchData(false);
		settlementRecordGrid.setShowFilterEditor(true);
		settlementRecordGrid.setCanSort(true);
		settlementRecordGrid.setShowRowNumbers(true);
		settlementRecordGrid.setWidth100();
		settlementRecordGrid.setShowRecordComponents(true);          
		settlementRecordGrid.setShowRecordComponentsByCell(true);  
		settlementRecordGrid.setSelectionType(SelectionStyle.SIMPLE);
		settlementRecordGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		
		settlementRecordGrid.setDataSource(dataSource);
		settlementRecordGrid.setFields(Util.getListGridFieldsFromDataSource(dataSource));	 
		
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENMERCHANTPRODUCT_VENMERCHANT_WCSMERCHANTID).setWidth(150);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENMERCHANTPRODUCT_VENMERCHANT_VENPARTY_FULLORLEGALNAME).setWidth(200);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_CXF_DATE).setWidth(100);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENORDER_WCSORDERID).setWidth(75);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_WCSORDERITEMID).setWidth(75);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_VENMERCHANTPRODUCT_WCSPRODUCTNAME).setWidth(200);		
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_QUANTITY).setWidth(75);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_PRICE).setWidth(100);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_JUMLAH).setWidth(100);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_GDNCOMMISIONAMOUNT).setWidth(100);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_GDNTRANSACTIONFEEAMOUNT).setWidth(100);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_PPH23_AMOUNT).setWidth(75);		
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_TOTAL).setWidth(100);
		
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_SALESRECORDID).setHidden(true);
		
		Util.formatListGridFieldAsCurrency(settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_PRICE));
		Util.formatListGridFieldAsCurrency(settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_JUMLAH));
		Util.formatListGridFieldAsCurrency(settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_GDNCOMMISIONAMOUNT));
		Util.formatListGridFieldAsCurrency(settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_GDNTRANSACTIONFEEAMOUNT));
		Util.formatListGridFieldAsCurrency(settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_PPH23_AMOUNT));
		Util.formatListGridFieldAsCurrency(settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_TOTAL));
		
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_QUANTITY).setSummaryFunction(SummaryFunctionType.SUM);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_PRICE).setSummaryFunction(SummaryFunctionType.SUM);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_JUMLAH).setSummaryFunction(SummaryFunctionType.SUM);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_GDNCOMMISIONAMOUNT).setSummaryFunction(SummaryFunctionType.SUM);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_GDNTRANSACTIONFEEAMOUNT).setSummaryFunction(SummaryFunctionType.SUM);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_PPH23_AMOUNT).setSummaryFunction(SummaryFunctionType.SUM);
		settlementRecordGrid.getField(DataNameTokens.FINSALESRECORD_VENORDERITEM_TOTAL).setSummaryFunction(SummaryFunctionType.SUM);
		
		settlementRecordGrid.setShowGridSummary(true);
		
		

    	         
	   salesSattlementReportLayout.setMembers(toolStrip,settlementRecordGrid);
		bindCustomUiHandlers();
	}	
	
	protected void bindCustomUiHandlers() {		
		settlementRecordGrid.addFilterEditorSubmitHandler(new FilterEditorSubmitHandler() {			
			@Override
			public void onFilterEditorSubmit(FilterEditorSubmitEvent event) {
				refreshsettlementRecordViewerViewData();
				}
		});		
		
		// Grid record selection handler
		settlementRecordGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
			@Override
			public void onSelectionChanged(SelectionEvent event) {
				// Get selected record
				ListGridRecord[] selectedRecords = settlementRecordGrid.getSelection();
				
				// when no record is selected, top buttons are disabled
				if(selectedRecords.length == 0){
					exportButton.disable();
				// enable top buttons after a record is selected	
				}else{
					exportButton.enable();
				}
				
			}
		});
		
		// export click handler
		exportButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ListGridRecord[] selectedRecords = settlementRecordGrid.getSelection();
				
				StringBuilder sbSelectedRecords = new StringBuilder();
				
				for (int i = 0; i < selectedRecords.length; i++) {
					ListGridRecord selectedRecord = selectedRecords[i];
					
					sbSelectedRecords.append(selectedRecord.getAttributeAsString(DataNameTokens.FINSALESRECORD_SALESRECORDID));
					
					if(i != selectedRecords.length -1)
						sbSelectedRecords.append(";");
				}
				
				String host = GWT.getHostPageBaseURL();

				//If in debug mode then change the host URL to the servlet in the server side
				if(host.contains("8889")){
					host = "http://localhost:8090/";
				}
				
				/* 
				 * Somehow when the app is deployed in Geronimo the getHostPageBaseURL call
				 * adds the context root of "Venice/" as it is the web application.
				 * This does not happen in development mode as it is running in the root
				 * of the Jetty servlet container.
				 * 
				 * Consequently the context root needs to be removed because the servlet 
				 * being called has its own context root in a different web application.
				 */
				if(host.contains("Venice/")){
					host = host.substring(0, host.indexOf("Venice/"));
				}
												
				Window.open(host + "Venice/SalesSettlementReportLauncherServlet?salesRecordIds=" + sbSelectedRecords.toString(), "_blank", null);
							
			}
		});
	}
	
	public void refreshsettlementRecordViewerViewData() {
		DSCallback callBack = new DSCallback() {
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				settlementRecordGrid.setData(response.getData());}
		};		
		settlementRecordGrid.getDataSource().fetchData(settlementRecordGrid.getFilterEditorCriteria(), callBack);
		
	}
	
	@Override
	public Widget asWidget() {
		return salesSattlementReportLayout;
	}	

}