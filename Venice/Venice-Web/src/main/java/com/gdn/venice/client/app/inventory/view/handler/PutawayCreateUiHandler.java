package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashSet;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 *
 * @author Roland
 */

public interface PutawayCreateUiHandler extends UiHandlers {

	void onFetchWarehouseComboBoxData();
	void onSubmitClicked(HashSet<String> grnNumberSet, String putawayType);
}
