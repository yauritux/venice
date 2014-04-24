package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashMap;

import com.gwtplatform.mvp.client.UiHandlers;
import com.smartgwt.client.widgets.Window;

/**
 *
 * @author Roland
 */
public interface ShelfEditWithApprovalUiHandler extends UiHandlers{
    public void approveEditShelfData(String username, HashMap<String, String> data);
    public void needCorrectionEditShelfData(String username, HashMap<String, String> data);
    public void rejectEditShelfData(String username, HashMap<String, String> data);
	public void onEditShelfEditClicked(HashMap<String, String> shelfDataMap, HashMap<String, String> storageDataMap, Window window);   
}
