package com.gdn.venice.client.ui.data;

import com.gdn.venice.client.app.DataWidgetNameTokens;
import com.gdn.venice.client.app.NameTokens;

public class FraudNavigationPaneSectionData {
	private static NavigationPaneTreeNodeRecord[] records;

	  public static NavigationPaneTreeNodeRecord[] getRecords() {
		if (records == null) {
		  records = getNewRecords();
		}
		return records;
	  }

	  public static NavigationPaneTreeNodeRecord[] getNewRecords() {
		return new NavigationPaneTreeNodeRecord[]{
		  new NavigationPaneTreeNodeRecord("2", "1", "Fraud Module", null, "FR1", DataWidgetNameTokens.FRD_FRAUDMODULETREENODE),
		  new NavigationPaneTreeNodeRecord("3", "2", "Fraud Dashboard", NameTokens.fraudDashboardPage,  "FR2", DataWidgetNameTokens.FRD_FRAUDDASHBOARDTREENODE),
		  new NavigationPaneTreeNodeRecord("4", "2", "Case Management", NameTokens.fraudCaseManagementPage, "FR3", DataWidgetNameTokens.FRD_CASEMANAGEMENTTREENODE),
		  new NavigationPaneTreeNodeRecord("5", "2", "Fraud Case Viewer", NameTokens.fraudCaseViewerPage,  "FR4", DataWidgetNameTokens.FRD_FRAUDCASEVIEWER),		  
		  new NavigationPaneTreeNodeRecord("6", "2", "Upload MIGS", NameTokens.fraudMigsUpload, "FR9", DataWidgetNameTokens.FRD_FRAUDMIGSUPLOAD),
		  new NavigationPaneTreeNodeRecord("7", "2", "Master MIGS", NameTokens.fraudMasterMigs, "FR12", DataWidgetNameTokens.FRD_FRAUDMASTERMIGS),
		  new NavigationPaneTreeNodeRecord("8", "2", "Uncalculated CC Order", NameTokens.fraudUncalculatedCreditCardOrder, "FR10", DataWidgetNameTokens.FRD_FRAUDUNCALCULATEDCREDITCARDORDER),
		  new NavigationPaneTreeNodeRecord("9", "2", "Calculate Fraud", NameTokens.fraudCalculatePage, "FR8", DataWidgetNameTokens.FRD_FRAUDCALCULATEPAGE),
		  new NavigationPaneTreeNodeRecord("10", "2", "Installment BCA", NameTokens.fraudInstallmentBCA, "FR16", DataWidgetNameTokens.FRD_FRAUDINSTALLMENTBCA),
		  new NavigationPaneTreeNodeRecord("11", "2", "Administration", null,  "FR5", DataWidgetNameTokens.FRD_ADMINSITRATIONTREENODE),
		  new NavigationPaneTreeNodeRecord("12", "11", "IP White List & Black List", NameTokens.fraudBlackListMaintenancePage,  "FR6", DataWidgetNameTokens.FRD_BLACKLISTMAINTENANCETREENODE),
		  new NavigationPaneTreeNodeRecord("13", "11", "Customer Black List", NameTokens.fraudCustomerBlackListMaintenancePage, "FR11", DataWidgetNameTokens.FRD_CUSTOMERBLACKLISTMAINTENANCETREENODE),
		  new NavigationPaneTreeNodeRecord("14", "11", "Customer White List", NameTokens.fraudCustomerWhiteListMaintenancePage, "FR13", DataWidgetNameTokens.FRD_CUSTOMERWHITELISTMAINTENANCETREENODE),
		  new NavigationPaneTreeNodeRecord("15", "11", "BIN Credit Limit", NameTokens.fraudBinCreditLimitPage, "FR7", DataWidgetNameTokens.FRD_FRAUDBINCREDITLIMIT),
		  new NavigationPaneTreeNodeRecord("16", "11", "Genuine List", NameTokens.fraudParameterRule31Page,  "FR15", DataWidgetNameTokens.FRD_FRAUDPARAMETERRULE31TREENODE),
		};
	  }
}
