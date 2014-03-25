package com.gdn.venice.client.app.fraud.view;

import java.util.LinkedHashMap;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.app.fraud.presenter.InstallmentBCAPresenter;
import com.gdn.venice.client.app.fraud.view.handlers.InstallmentBCAUiHandlers;
import com.gdn.venice.client.util.Util;
import com.gdn.venice.client.widgets.RafViewLayout;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.toolbar.ToolStrip;

/**
 * View for installment BCA
 * 
 * @author Roland
 */
public class InstallmentBCAView extends
		ViewWithUiHandlers<InstallmentBCAUiHandlers> implements
		InstallmentBCAPresenter.MyView {

	RafViewLayout installmentBCALayout;
	ListGrid convertInstallmentListGrid = new ListGrid();
	ListGrid cancelInstallmentListGrid = new ListGrid();

	@Inject
	public InstallmentBCAView() {
		installmentBCALayout = new RafViewLayout();
		
		TabSet tabSet = new TabSet();
		tabSet.setTabBarPosition(Side.TOP);
		tabSet.setWidth100();
		tabSet.setHeight100();
		
		tabSet.addTab(buildConvertInstallmentTab());
		tabSet.addTab(buildCancelInstallmentTab());
					
		installmentBCALayout.setMembers(tabSet);
		
		bindCustomUiHandlers();
	}
	
	private Tab buildConvertInstallmentTab() {
		Tab convertInstallmentTab = new Tab("Convert Installment BCA");		
		VLayout convertInstallmentLayout = new VLayout();
		
		ToolStrip convertToolStrip = new ToolStrip();
		convertToolStrip.setWidth100();
				
		convertInstallmentListGrid.setAutoFetchData(false);
		convertInstallmentListGrid.setCanEdit(true);
		convertInstallmentListGrid.setCanResizeFields(true);
		convertInstallmentListGrid.setShowFilterEditor(true);
		convertInstallmentListGrid.setCanSort(true);
		convertInstallmentListGrid.setSelectionType(SelectionStyle.SIMPLE);
		convertInstallmentListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		convertInstallmentListGrid.setEditEvent(ListGridEditEvent.DOUBLECLICK);
		convertInstallmentListGrid.setShowRowNumbers(true);			

		
		convertInstallmentListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {
				convertInstallmentListGrid.saveAllEdits();
				refreshConvertInstallmentData();
				if(event.getDsResponse().getStatus()==0){
					SC.say("Data Edited");
				}				
			}
		});

		convertInstallmentLayout.setMembers(convertToolStrip, convertInstallmentListGrid);
		convertInstallmentTab.setPane(convertInstallmentLayout);	
		
		return convertInstallmentTab;
	}
	
	private Tab buildCancelInstallmentTab() {
		Tab cancelInstallmentTab = new Tab("Cancel Installment BCA");	
		VLayout cancelInstallmentLayout = new VLayout();
		
		ToolStrip cancelToolStrip = new ToolStrip();
		cancelToolStrip.setWidth100();
				
		cancelInstallmentListGrid.setAutoFetchData(true);
//		cancelInstallmentListGrid.setCanEdit(true);
		cancelInstallmentListGrid.setCanResizeFields(true);
		cancelInstallmentListGrid.setShowFilterEditor(false);
		cancelInstallmentListGrid.setCanSort(true);
		cancelInstallmentListGrid.setSelectionType(SelectionStyle.SIMPLE);
		cancelInstallmentListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		cancelInstallmentListGrid.setEditEvent(ListGridEditEvent.DOUBLECLICK);
		cancelInstallmentListGrid.setShowRowNumbers(true);				
		
		cancelInstallmentListGrid.addEditCompleteHandler(new EditCompleteHandler() {			
			@Override
			public void onEditComplete(EditCompleteEvent event) {
				cancelInstallmentListGrid.saveAllEdits();
				refreshCancelInstallmentData();
				if(event.getDsResponse().getStatus()==0){
					SC.say("Data Edited");
				}				
			}
		});

		cancelInstallmentLayout.setMembers(cancelToolStrip, cancelInstallmentListGrid);
		cancelInstallmentTab.setPane(cancelInstallmentLayout);	
		
		return cancelInstallmentTab;
	}

	@Override
	public Widget asWidget() {
		return installmentBCALayout;
	}

	protected void bindCustomUiHandlers() {

	}

	@Override
	public void loadInstallmentBCAData(DataSource convertInstallmentDS, DataSource cancelInstallmentDS) {			
		//populate convert installment listgrid
		LinkedHashMap<String, String> paymentTypeMap = new LinkedHashMap<String, String>();  
		paymentTypeMap.put("3", "MIGS Credit Card");
		paymentTypeMap.put("7", "MIGS Installment");
		convertInstallmentDS.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENWCSPAYMENTTYPE_WCSPAYMENTTYPEID).setValueMap(paymentTypeMap);
		

		LinkedHashMap<String, String> convertMap = new LinkedHashMap<String, String>();  
		convertMap.put("true", "Yes");
		convertMap.put("false", "No");
		convertInstallmentDS.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_INSTALLMENTSENTFLAG).setValueMap(convertMap);
		
		convertInstallmentListGrid.setDataSource(convertInstallmentDS);	
		convertInstallmentListGrid.setFields(Util.getListGridFieldsFromDataSource(convertInstallmentDS));
		convertInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_ORDERPAYMENTID).setCanEdit(false);
		convertInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_WCSORDERID).setCanEdit(false);		
		convertInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_WCSPAYMENTID).setCanEdit(false);
		convertInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_REFERENCEID).setCanEdit(false);	
		convertInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENWCSPAYMENTTYPE_WCSPAYMENTTYPEID).setCanEdit(true);
		convertInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_AMOUNT).setCanEdit(false);
		convertInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_TENOR).setCanEdit(true);
		convertInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENCUSTOMER_CUSTOMERUSERNAME).setCanEdit(false);	
		convertInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENCUSTOMER_VENPARTY_FULLORLEGALNAME).setCanEdit(false);	
		convertInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_INSTALLMENTSENTFLAG).setCanEdit(false);
		Util.formatListGridFieldAsCurrency(convertInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_AMOUNT));
		
		//populate cancel installment listgrid
		LinkedHashMap<String, String> tenorMap = new LinkedHashMap<String, String>();  
		tenorMap.put("6", "6 Bulan");
		tenorMap.put("12", "12 Bulan");
		
		LinkedHashMap<String, String> paymentTypeCancelMap = new LinkedHashMap<String, String>();  
		paymentTypeCancelMap.put("7", "MIGS Installment");
		cancelInstallmentDS.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENWCSPAYMENTTYPE_WCSPAYMENTTYPEID).setValueMap(paymentTypeCancelMap);	
		cancelInstallmentDS.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_TENOR).setValueMap(tenorMap);
		
		LinkedHashMap<String, String> cancelMap = new LinkedHashMap<String, String>();  
		cancelMap.put("true", "Yes");
		cancelMap.put("false", "No");
		cancelInstallmentDS.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_INSTALLMENTCANCELFLAG).setValueMap(cancelMap);
		
		cancelInstallmentListGrid.setDataSource(cancelInstallmentDS);				
		cancelInstallmentListGrid.setFields(Util.getListGridFieldsFromDataSource(cancelInstallmentDS));
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_ORDERPAYMENTID).setCanEdit(false);	
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_ORDERPAYMENTID).setHidden(true);		
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_WCSORDERID).setCanEdit(false);		
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_WCSPAYMENTID).setCanEdit(false);
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_REFERENCEID).setCanEdit(false);	
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENWCSPAYMENTTYPE_WCSPAYMENTTYPEID).setCanEdit(false);
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_AMOUNT).setCanEdit(false);	
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_TENOR).setCanEdit(false);
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENCUSTOMER_CUSTOMERUSERNAME).setCanEdit(false);	
		cancelInstallmentListGrid.getField(DataNameTokens.VENCUSTOMER_VENPARTY_FULLORLEGALNAME).setCanEdit(false);	
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_INSTALLMENTCANCELFLAG).setCanEdit(true);
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_WCSORDERID).setWidth(100);
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_WCSPAYMENTID).setWidth(100);
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_REFERENCEID).setWidth(100);
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENWCSPAYMENTTYPE_WCSPAYMENTTYPEID).setWidth(100);
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_AMOUNT).setWidth(120);
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_TENOR).setWidth(75);
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENCUSTOMER_CUSTOMERUSERNAME).setWidth(150);
		cancelInstallmentListGrid.getField(DataNameTokens.VENCUSTOMER_VENPARTY_FULLORLEGALNAME).setWidth(150);
		cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_INSTALLMENTCANCELFLAG).setWidth(120);
		Util.formatListGridFieldAsCurrency(cancelInstallmentListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_AMOUNT));				
				 				
        bindCustomUiHandlers();
	}
	
	public void refreshConvertInstallmentData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				convertInstallmentListGrid.setData(response.getData());
			}
		};
		
		convertInstallmentListGrid.getDataSource().fetchData(convertInstallmentListGrid.getFilterEditorCriteria(), callBack);
	}
	
	public void refreshCancelInstallmentData() {
		DSCallback callBack = new DSCallback() {			
			@Override
			public void execute(DSResponse response, Object rawData, DSRequest request) {
				cancelInstallmentListGrid.setData(response.getData());
			}
		};
		
		cancelInstallmentListGrid.getDataSource().fetchData(cancelInstallmentListGrid.getFilterEditorCriteria(), callBack);
	}
}
