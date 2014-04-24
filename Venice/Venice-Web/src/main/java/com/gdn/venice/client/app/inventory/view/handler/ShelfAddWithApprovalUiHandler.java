package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashMap;

import com.gwtplatform.mvp.client.UiHandlers;
import com.smartgwt.client.widgets.Window;

/**
 *
 * @author Roland
 */
public interface ShelfAddWithApprovalUiHandler extends UiHandlers{
    public void approveCreateShelfWIPData(String username, HashMap<String, String> data);
    public void rejectCreateShelfWIPData(String username, HashMap<String, String> data);
    public void needCorrectionCreateShelfWIPData(String username, HashMap<String, String> data);
	public void onEditShelfAddClicked(HashMap<String, String> shelfRowMap, HashMap<String, String> storageDataMap, Window shelfDetailWindow);

}
