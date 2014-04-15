package com.gdn.venice.client.app.inventory.view.handler;

import com.gwtplatform.mvp.client.UiHandlers;
import com.smartgwt.client.widgets.Window;

/**
 *
 * @author Roland
 */

public interface PickingListIRUiHandler extends UiHandlers {
	public void onFetchPickerComboBoxData();
	public void onSubmitClicked(String packageIds, String pickerId);
}
