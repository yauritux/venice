package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashMap;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 *
 * @author Roland
 */
public interface ShelfAddWithApprovalUiHandler extends UiHandlers{
    public void updateShelfWIPData(String username, HashMap<String, String> data);   
}
