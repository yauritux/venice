package com.gdn.venice.server.app.inventory.command;

import java.util.HashMap;
import java.util.List;

import com.gdn.inventory.exchange.entity.Picker;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;

/**
 * Fetch Command for picker combo box
 *
 * @author Roland
 */
public class FetchPickerComboBoxDataCommand implements RafRpcCommand {
    PickingListManagementService pickingService;

    public FetchPickerComboBoxDataCommand() {
    }

    public String execute() {
    	System.out.println("FetchPickerComboBoxDataCommand");
        HashMap<String, String> map = new HashMap<String, String>();
        try {
        	pickingService = new PickingListManagementService();
            ResultWrapper<List<Picker>> pickerWrapper = pickingService.getPickerData();
            if (pickerWrapper != null && pickerWrapper.isSuccess()) {
                for (Picker p : pickerWrapper.getContent()) {
                	map.put("data" + p.getId().toString(), p.getName());                        
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Util.formXMLfromHashMap(map);
    }
}
