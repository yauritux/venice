package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.WarehouseUser;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.OpnameService;
import com.gdn.venice.server.app.inventory.service.WarehouseManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;
import java.util.HashMap;
import java.util.List;

/**
 * Fetch Command for warehouse combo box
 *
 * @author Maria Oliva
 */
public class FetchItemUomComboBoxDataCommand implements RafRpcCommand {

    OpnameService opnameService;

    public String execute() {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            opnameService = new OpnameService();
            ResultWrapper<List<String>> wrapper = opnameService.getUoM();
            if (wrapper != null && wrapper.isSuccess()) {
                for (String category : wrapper.getContent()) {
                    map.put("data" + category, category);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Util.formXMLfromHashMap(map);
    }
}
