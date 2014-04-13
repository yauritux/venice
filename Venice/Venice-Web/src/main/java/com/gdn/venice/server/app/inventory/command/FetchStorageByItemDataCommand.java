package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
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
 * @author Roland
 */
public class FetchStorageByItemDataCommand implements RafRpcCommand {

    String itemSku;
    OpnameService opnameService;

    public FetchStorageByItemDataCommand(String itemSku) {
        this.itemSku = itemSku;
    }

    /*
     * Edited by Maria Olivia 20140320
     */
    public String execute() {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            opnameService = new OpnameService();
            ResultWrapper<List<WarehouseItemStorageStock>> wrapper = opnameService.getStorageByItemData(itemSku);
            if (wrapper != null) {
                if (wrapper.isSuccess()) {
                    for (WarehouseItemStorageStock itemStock : wrapper.getContent()) {
                        map.put(itemStock.getStorage().getCode(), itemStock.getStorage().getCode());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Util.formXMLfromHashMap(map);
    }
}
