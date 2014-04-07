package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashMap;

import com.gwtplatform.mvp.client.UiHandlers;
import com.smartgwt.client.widgets.Window;

/**
 *
 * @author Roland
 */

public interface GRNCreateUiHandler extends UiHandlers {

	void onSaveClicked(HashMap<String, String> grnDataMap, HashMap<String, String> itemDataMap, Window window);
	void onSaveAttribute(String username, String attributes, String asnItemId);
	void onFetchAttributeName(String asnItemId, String itemId, String quantity);
}
