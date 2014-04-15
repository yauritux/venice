/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashMap;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 *
 * @author Maria Olivia
 */
public interface PickerManagementUiHandler extends UiHandlers {

    public void saveOrUpdatePickerData(HashMap<String, String> data);
    
    public void nonActivePicker(String id);
}
