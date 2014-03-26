package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashMap;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 *
 * @author Roland
 */

public interface PutawayCreateUiHandler extends UiHandlers {

	void onFetchWarehouseComboBoxData();
	void onSubmitClicked(HashMap<String, String> itemDataMap);
}
