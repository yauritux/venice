package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.ShelfManagementService;
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

    String warehouseCode, stockType, supplierCode, itemCode;
    ShelfManagementService shelfManagementService;

    public FetchStorageByItemDataCommand(String warehouseCode,
            String stockType, String supplierCode, String itemCode) {
        this.itemCode = itemCode;
        this.warehouseCode = warehouseCode;
        this.stockType = stockType;
        this.supplierCode = supplierCode;
    }

    /*
     * Edited by Maria Olivia 20140320
     */
    public String execute() {
        HashMap<String, String> map = new HashMap<String, String>();
        try {
            shelfManagementService = new ShelfManagementService();
            ResultWrapper<List<WarehouseItemStorageStock>> wrapper = shelfManagementService.getStorageByItemData(warehouseCode,
                    stockType, supplierCode, itemCode);
            if (wrapper != null) {
                if (wrapper.isSuccess()) {
                    for (WarehouseItemStorageStock itemStock : wrapper.getContent()) {
                        map.put(itemStock.getQuantity() + "", itemStock.getStorage().getCode());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Util.formXMLfromHashMap(map);
    }
}
