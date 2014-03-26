/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view.handler;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 *
 * @author Maria Olivia
 */
public interface PackingListUiHandler extends UiHandlers {

    public void onSalesOrderGridClicked(String salesOrderId, String itemId, String quantity);

    public void onSaveAttribute(String username, String attribute, String soId);

    public void onSavePacking(String username, String awbInfoId);
}
