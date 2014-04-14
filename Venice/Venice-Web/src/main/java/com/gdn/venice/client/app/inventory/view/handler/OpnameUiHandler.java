/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view.handler;

import com.gwtplatform.mvp.client.UiHandlers;
import java.util.HashMap;

/**
 *
 * @author Maria Olivia
 */
public interface OpnameUiHandler extends UiHandlers {

    public void onSubmitButton(String data, String warehouseCode, String stockType, String supplierCode);
}
