package com.gdn.venice.client.app.inventory.view.handler;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 *
 * @author Roland
 */

public interface PickingListIRUiHandler extends UiHandlers {
	public void onSubmitClicked(String packageIds, String pickerId);
	public void onFetchWarehouseComboBoxData();
}
