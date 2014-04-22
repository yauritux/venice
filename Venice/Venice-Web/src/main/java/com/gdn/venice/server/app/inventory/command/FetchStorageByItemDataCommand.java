package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ShelfManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Maria Olivia
 */
public class FetchStorageByItemDataCommand implements RafDsCommand {

    String warehouseCode, stockType, supplierCode, itemCode;
    ShelfManagementService shelfManagementService;

    public FetchStorageByItemDataCommand(String warehouseCode,
            String stockType, String supplierCode, String itemCode) {
        this.itemCode = itemCode;
        this.warehouseCode = warehouseCode;
        this.stockType = stockType;
        this.supplierCode = supplierCode;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        try {
            shelfManagementService = new ShelfManagementService();
            ResultWrapper<List<WarehouseItemStorageStock>> wrapper = shelfManagementService
                    .getStorageByItemData(warehouseCode, stockType, supplierCode, itemCode);
            if (wrapper != null) {
                if (wrapper.isSuccess()) {
                    //Put result
                    for (WarehouseItemStorageStock itemStock : wrapper.getContent()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_SHELFCODE, itemStock.getStorage().getShelf().getCode() + " / " + itemStock.getQuantity());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE, itemStock.getStorage().getCode());
                        dataList.add(map);
                    }

                    //Set DSResponse's properties
                    rafDsResponse.setStatus(0);
                    rafDsResponse.setStartRow(0);
                    rafDsResponse.setTotalRows(wrapper.getContent().size());
                    rafDsResponse.setEndRow(dataList.size());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            rafDsResponse.setStatus(-1);
        }

        //Set data and return
        rafDsResponse.setData(dataList);
        return rafDsResponse;
    }
}
