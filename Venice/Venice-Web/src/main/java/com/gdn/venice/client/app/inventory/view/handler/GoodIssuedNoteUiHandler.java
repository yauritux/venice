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
public interface GoodIssuedNoteUiHandler extends UiHandlers {

    public void onSaveGin(HashMap<String, String> data);
    
    public void onEditAwbNumberCompleted(String awbNumber, String logistic, String warehouseCode, int recordNumber);
}
