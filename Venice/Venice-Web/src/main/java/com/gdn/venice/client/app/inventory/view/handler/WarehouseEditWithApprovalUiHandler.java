/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.client.app.inventory.view.handler;

import com.gwtplatform.mvp.client.UiHandlers;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.RestDataSource;
import java.util.HashMap;

/**
 *
 * @author Maria Olivia
 */
public interface WarehouseEditWithApprovalUiHandler extends UiHandlers{
    public void updateWarehouseWIPData(String username, HashMap<String, String> data);   
}
