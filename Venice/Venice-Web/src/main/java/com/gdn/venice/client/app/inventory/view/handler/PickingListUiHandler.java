package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashMap;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 *
 * @author Roland
 */

public interface PickingListUiHandler extends UiHandlers {

	void onFetchWarehouseComboBoxData();
	void releaseLock(String warehouseId);
	void onSaveClicked(HashMap<String, String> itemDataMap, HashMap<String, String> salesDataMap, HashMap<String, String> storageDataMap
			, int totalQtyPicked);
}
