/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view.handler;

import java.util.HashMap;

import com.gwtplatform.mvp.client.UiHandlers;
import com.smartgwt.client.widgets.Window;

/**
 *
 * @author Maria Olivia
 */
public interface WarehouseListFilterUiHandler extends UiHandlers {

    public void saveOrUpdateWarehouseData(String username, HashMap<String, String> data, Window window);
}
