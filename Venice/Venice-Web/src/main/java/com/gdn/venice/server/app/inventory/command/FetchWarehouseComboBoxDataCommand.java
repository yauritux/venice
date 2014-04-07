package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.WarehouseUser;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.WarehouseManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;
import java.util.HashMap;
import java.util.List;

/**
 * Fetch Command for warehouse combo box
 *
 * @author Roland
 */
public class FetchWarehouseComboBoxDataCommand implements RafRpcCommand {

    String username;
    boolean isCode;
    WarehouseManagementService warehouseService;

    public FetchWarehouseComboBoxDataCommand(String username, boolean isCode) {
        this.username = username;
        this.isCode = isCode;
    }

    /*
     * Edited by Maria Olivia 20140320
     */
    public String execute() {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            warehouseService = new WarehouseManagementService();
            ResultWrapper<List<WarehouseUser>> whuWrapper = warehouseService.getWarehouseUserData(username);
            if (whuWrapper != null) {
                if (whuWrapper.isSuccess()) {
                    for (WarehouseUser wu : whuWrapper.getContent()) {
                        if (isCode) {
                            map.put("data" + wu.getWarehouse().getCode(), wu.getWarehouse().getName());
                        } else {
                            map.put("data" + wu.getWarehouse().getId().toString(), wu.getWarehouse().getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Util.formXMLfromHashMap(map);
    }
}
