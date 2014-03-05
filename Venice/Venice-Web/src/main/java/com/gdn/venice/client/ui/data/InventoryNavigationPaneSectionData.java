package com.gdn.venice.client.ui.data;

import com.gdn.venice.client.app.DataWidgetNameTokens;
import com.gdn.venice.client.app.NameTokens;

/**
 * This class contains the menu data definition for the inventory stack section
 *
 * @author Maria Olivia
 */
public class InventoryNavigationPaneSectionData {
	private static NavigationPaneTreeNodeRecord[] records;

	  public static NavigationPaneTreeNodeRecord[] getRecords() {
		if (records == null) {
		  records = getNewRecords();
		}
		return records;
	  }

	  public static NavigationPaneTreeNodeRecord[] getNewRecords() {
		return new NavigationPaneTreeNodeRecord[]{
		  new NavigationPaneTreeNodeRecord("2", "1", "Master Warehouse", null, "IM1", DataWidgetNameTokens.INV_MASTERWAREHOUSETREENODE),
		  new NavigationPaneTreeNodeRecord("3", "2", "Warehouse List & Filter", NameTokens.warehouseListFilterPage, "IM2", DataWidgetNameTokens.INV_WAREHOUSELISTFILTERTREENODE),
		  new NavigationPaneTreeNodeRecord("4", "2", "Add with Approval", NameTokens.warehouseAddWithApprovalPage, "IM3", DataWidgetNameTokens.INV_ADDWITHAPPROVALTREENODE),
		  new NavigationPaneTreeNodeRecord("5", "2", "Edit with Approval", NameTokens.warehouseEditWithApprovalPage, "IM4", DataWidgetNameTokens.INV_EDITWITHAPPROVALTREENODE),
		  new NavigationPaneTreeNodeRecord("6", "2", "Non-Active with Approval", NameTokens.warehouseNonActiveWithApprovalPage, "IM5", DataWidgetNameTokens.INV_NONACTIVEWITHAPPROVALTREENODE),		  
		  new NavigationPaneTreeNodeRecord("7", "1", "Shelf Management", null, "IM6", DataWidgetNameTokens.INV_SHELFMANAGEMENTTREENODE),
		  new NavigationPaneTreeNodeRecord("8", "7", "Shelf List & Filter", NameTokens.shelfListFilterPage, "IM7", DataWidgetNameTokens.INV_SHELFLISTFILTERTREENODE),
		  new NavigationPaneTreeNodeRecord("9", "7", "Add Approval", NameTokens.shelfAddApprovalPage, "IM8", DataWidgetNameTokens.INV_SHELFADDAPPROVALTREENODE),
		  new NavigationPaneTreeNodeRecord("10", "7", "Edit Approval", NameTokens.shelfEditApprovalPage, "IM9", DataWidgetNameTokens.INV_SHELFEDITAPPROVALTREENODE),
		  new NavigationPaneTreeNodeRecord("11", "7", "Non-Active Approval", NameTokens.shelfNonActiveApprovalPage, "IM10", DataWidgetNameTokens.INV_SHELFNONACTIVEAPPROVALTREENODE),
		  new NavigationPaneTreeNodeRecord("12", "1", "Master Currency", NameTokens.currencyManagementPage, "IM11", DataWidgetNameTokens.INV_CURRENCYMANAGEMENTTREENODE),
		  new NavigationPaneTreeNodeRecord("13", "1", "Advance Ship Notice", null, "IM12", DataWidgetNameTokens.INV_ASNTREENODE),
		  new NavigationPaneTreeNodeRecord("14", "13", "Advance Ship Notice List", NameTokens.asnListPage, "IM13", DataWidgetNameTokens.INV_ASNLISTTREENODE)
		};
	  }
}
