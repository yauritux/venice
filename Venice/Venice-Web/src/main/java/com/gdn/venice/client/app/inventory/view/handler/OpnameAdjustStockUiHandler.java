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
public interface OpnameAdjustStockUiHandler extends UiHandlers {

    public void onSubmitButton(String opnameId);

    public void onSkuSelected(String itemSKU, String warehouseCode, String stockType, String supplierCode);
}
