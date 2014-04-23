package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashMap;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 *
 * @author Roland
 */
public interface ShelfNonActiveWithApprovalUiHandler extends UiHandlers{
    public void approveNonActiveShelfData(String username, HashMap<String, String> data); 
    public void rejectNonActiveShelfData(String username, HashMap<String, String> data);
}
