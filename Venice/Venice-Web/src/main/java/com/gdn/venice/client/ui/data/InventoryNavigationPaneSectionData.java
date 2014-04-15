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
                    new NavigationPaneTreeNodeRecord("3", "2", "Warehouse List", NameTokens.warehouseListFilterPage, "IM2", DataWidgetNameTokens.INV_WAREHOUSELISTFILTERTREENODE),
                    new NavigationPaneTreeNodeRecord("4", "2", "Add Approval", NameTokens.warehouseAddWithApprovalPage, "IM3", DataWidgetNameTokens.INV_ADDWITHAPPROVALTREENODE),
                    new NavigationPaneTreeNodeRecord("5", "2", "Edit Approval", NameTokens.warehouseEditWithApprovalPage, "IM4", DataWidgetNameTokens.INV_EDITWITHAPPROVALTREENODE),
                    new NavigationPaneTreeNodeRecord("6", "2", "Non-Active Approval", NameTokens.warehouseNonActiveWithApprovalPage, "IM5", DataWidgetNameTokens.INV_NONACTIVEWITHAPPROVALTREENODE),
                    new NavigationPaneTreeNodeRecord("7", "1", "Master Shelf", null, "IM6", DataWidgetNameTokens.INV_MASTERSHELFTREENODE),
                    new NavigationPaneTreeNodeRecord("8", "7", "Shelf List", NameTokens.shelfListFilterPage, "IM7", DataWidgetNameTokens.INV_SHELFLISTFILTERTREENODE),
                    new NavigationPaneTreeNodeRecord("9", "7", "Add Approval", NameTokens.shelfAddApprovalPage, "IM8", DataWidgetNameTokens.INV_SHELFADDAPPROVALTREENODE),
                    new NavigationPaneTreeNodeRecord("10", "7", "Edit Approval", NameTokens.shelfEditApprovalPage, "IM9", DataWidgetNameTokens.INV_SHELFEDITAPPROVALTREENODE),
                    new NavigationPaneTreeNodeRecord("11", "7", "Non-Active Approval", NameTokens.shelfNonActiveApprovalPage, "IM10", DataWidgetNameTokens.INV_SHELFNONACTIVEAPPROVALTREENODE),
                    new NavigationPaneTreeNodeRecord("12", "1", "Master Currency", NameTokens.currencyManagementPage, "IM11", DataWidgetNameTokens.INV_CURRENCYMANAGEMENTTREENODE),
                    new NavigationPaneTreeNodeRecord("13", "1", "Advance Ship Notice", NameTokens.asnListPage, "IM12", DataWidgetNameTokens.INV_ASNLISTTREENODE),
                    new NavigationPaneTreeNodeRecord("14", "1", "Good Received Note", null, "IM13", DataWidgetNameTokens.INV_GRNTREENODE),
                    new NavigationPaneTreeNodeRecord("15", "14", "Create Good Received Note", NameTokens.grnCreatePage, "IM14", DataWidgetNameTokens.INV_GRNCREATETREENODE),
                    new NavigationPaneTreeNodeRecord("16", "14", "Good Received Note List", NameTokens.grnListPage, "IM15", DataWidgetNameTokens.INV_GRNLISTTREENODE),
                    new NavigationPaneTreeNodeRecord("17", "1", "Picking List", null, "IM16", DataWidgetNameTokens.INV_PICKINGLISTTREENODE),
                    new NavigationPaneTreeNodeRecord("18", "17", "Picking List IR", NameTokens.pickingListIRPage, "IM17", DataWidgetNameTokens.INV_PICKINGLISTIRTREENODE),
                    new NavigationPaneTreeNodeRecord("19", "17", "Picking List SO", NameTokens.pickingListSOPage, "IM18", DataWidgetNameTokens.INV_PICKINGLISTSOTREENODE),
                    new NavigationPaneTreeNodeRecord("20", "1", "Putaway", null, "IM19", DataWidgetNameTokens.INV_PUTAWAYTREENODE),
                    new NavigationPaneTreeNodeRecord("21", "20", "Create Putaway", NameTokens.putawayCreatePage, "IM20", DataWidgetNameTokens.INV_CREATEPUTAWAYTREENODE),
                    new NavigationPaneTreeNodeRecord("22", "20", "Input Putaway Location", NameTokens.putawayInputPage, "IM21", DataWidgetNameTokens.INV_INPUTPUTAWAYLOCATIONTREENODE),
                    new NavigationPaneTreeNodeRecord("23", "1", "Packing List", NameTokens.packingListPage, "IM22", DataWidgetNameTokens.INV_PACKINGLISTTREENODE),
                    new NavigationPaneTreeNodeRecord("24", "1", "Good Issued Note", NameTokens.ginPage, "IM23", DataWidgetNameTokens.INV_GINTREENODE),
                    new NavigationPaneTreeNodeRecord("25", "1", "Stock Opname", null, "IM24", DataWidgetNameTokens.INV_OPNAMETREENODE),
                    new NavigationPaneTreeNodeRecord("26", "25", "Create Stock Opname List", NameTokens.opnamePage, "IM25", DataWidgetNameTokens.INV_CREATEOPNAMETREENODE),
                    new NavigationPaneTreeNodeRecord("27", "25", "Adjust Stock", NameTokens.ginPage, "IM26", DataWidgetNameTokens.INV_OPNAMEADJUSTMENTTREENODE),                    
                    new NavigationPaneTreeNodeRecord("28", "1", "Master Picker", NameTokens.pickerManagementPage, "IM27", DataWidgetNameTokens.INV_PICKERMANAGEMENTTREENODE)
                };
    }
}