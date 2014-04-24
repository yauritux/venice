package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashMap;

import com.gwtplatform.mvp.client.UiHandlers;
import com.smartgwt.client.widgets.Window;

/**
 *
 * @author Roland
 */

public interface ShelfListFilterUiHandler extends UiHandlers {
	public void onSaveShelfClicked(HashMap<String, String> shelfDataMap, HashMap<String, String> storageDataMap, Window window);
	public void onNonActiveShelfClicked(HashMap<String, String> shelfDataMap, Window window);
	public void onEditShelfClicked(HashMap<String, String> shelfDataMap, HashMap<String, String> storageDataMap, Window window);
}
