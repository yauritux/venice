package com.gdn.venice.client.app.inventory.view.handler;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 *
 * @author Roland
 */

public interface GRNListUiHandler extends UiHandlers {
	public void onFetchAttributeName(String grnItemId, String itemId);
}
