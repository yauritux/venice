package com.gdn.venice.client.app.fraud.ui.widgets;

import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.client.util.Util;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;

/**
 * Widget for Fraud Case Management 
 * 
 * @author Anto
 */

public class FraudCaseManagementMoreInfoTab extends Tab {
		
	ListGrid orderHistoryListGrid;
	
	public FraudCaseManagementMoreInfoTab(String title,DataSource OrderHistData) {
		super(title);		
		VLayout moreInfoLayout = new VLayout();
		moreInfoLayout.setMargin(5);
		
		orderHistoryListGrid = new ListGrid();
		orderHistoryListGrid.setWidth100();
		orderHistoryListGrid.setHeight100();
		orderHistoryListGrid.setShowAllRecords(true);
		orderHistoryListGrid.setShowRowNumbers(true);
		orderHistoryListGrid.setSortField(0);
		orderHistoryListGrid.setCanHover(true);
		orderHistoryListGrid.setDataSource(OrderHistData);  
		orderHistoryListGrid.setCanResizeFields(true);		
		orderHistoryListGrid.setShowFilterEditor(true);
		orderHistoryListGrid.setFields(Util.getListGridFieldsFromDataSource(OrderHistData));		
		orderHistoryListGrid.setSelectionType(SelectionStyle.SIMPLE);
//		orderHistoryListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_ORDERDATE).setCanFilter(false);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_WCSORDERID).setCanFilter(false);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_ORDERITEMID).setCanFilter(false);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERCONTACTDETAIL_EMAIL);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENCUSTOMER_VENPARTY_FULLORLEGALNAME);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_VENMERCHANTPRODUCT_WCSPRODUCTNAME).setCanFilter(false);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_VENADDRESS_VENCITY_CITYID).setCanFilter(false);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_TOTAL).setCanFilter(false);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_VENMERCHANTPRODUCT_VENPRODUCTCATEGORIES_PRODUCTCATEGORY).setCanFilter(false);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENWCSPAYMENTTYPE_WCSPAYMENTCODE).setCanFilter(false);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_MASKEDCREDITCARDNUMBER).setCanFilter(false);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_VENBANK_BANKSHORTNAME).setCanFilter(false);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDERPAYMENT_THREEDSSECURITYLEVELAUTH).setCanFilter(false);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERITEMS_VENADDRESS_STREETADDRESS1);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERCONTACTDETAIL_MOBILE);
		orderHistoryListGrid.getField(DataNameTokens.VENORDERPAYMENTALLOCATION_VENORDER_VENORDERSTATUS_ORDERSTATUSCODE).setCanFilter(false);
	
			
		moreInfoLayout.setMembers(orderHistoryListGrid);

		setPane(moreInfoLayout);
	}	
	
	
	
}
