package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashMap;

import com.gwtplatform.mvp.client.UiHandlers;
import com.smartgwt.client.widgets.Window;

/**
 *
 * @author Roland
 */

public interface PutawayInputUiHandler extends UiHandlers {
	void onSaveClicked(HashMap<String, String> itemDataMap, Window putawayDetailWindow);
	void onFetchWarehouseComboBoxData();
}
