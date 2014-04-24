package com.gdn.venice.client.ui.data;

import com.gdn.venice.client.app.DataWidgetNameTokens;
import com.gdn.venice.client.app.NameTokens;

public class SeattleNavigationPaneSectionData {
	private static NavigationPaneTreeNodeRecord[] records;

	  public static NavigationPaneTreeNodeRecord[] getRecords() {
		if (records == null) {
		  records = getNewRecords();
		}
		return records;
	  }

	  public static NavigationPaneTreeNodeRecord[] getNewRecords() {
		return new NavigationPaneTreeNodeRecord[]{
		  new NavigationPaneTreeNodeRecord("2", "1", "Seattle", null, "SM1", DataWidgetNameTokens.SEATTLE_GENERALMODULE_TREENODE),
		  new NavigationPaneTreeNodeRecord("3", "2", "SLA Fulfillment", NameTokens.seattleSLAFulfillmentViewer, "SM2", DataWidgetNameTokens.SEATTLE_SLAFULFILLMENT_TREENODE),
		  new NavigationPaneTreeNodeRecord("4", "2", "SLA Status", NameTokens.seattleSLAStatusViewer, "SM3", DataWidgetNameTokens.SEATTLE_SLASTATUS_TREENODE),
		  new NavigationPaneTreeNodeRecord("5", "2", "ETD", NameTokens.seattleETDViewer, "SM4", DataWidgetNameTokens.SEATTLE_ETD_TREENODE),
		  new NavigationPaneTreeNodeRecord("6", "2", "UoM", NameTokens.seattleUoMViewer, "SM5", DataWidgetNameTokens.SEATTLE_UOM_TREENODE)
		};
	  }
}
