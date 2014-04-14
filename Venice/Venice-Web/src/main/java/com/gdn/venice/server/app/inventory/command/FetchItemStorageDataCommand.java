/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.OpnameService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Maria Olivia
 */
public class FetchItemStorageDataCommand implements RafDsCommand {

    private RafDsRequest request;
    private String ids;
    OpnameService opnameService;

    public FetchItemStorageDataCommand(RafDsRequest request) {
        this.request = request;
    }

    public FetchItemStorageDataCommand(String ids) {
        this.ids = ids;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        try {
            opnameService = new OpnameService();
            ResultWrapper<List<WarehouseItemStorageStock>> wrapper;
            if (ids == null || ids.trim().isEmpty()) {
                wrapper = opnameService.getStorageItemData(request);
            } else {
                String[] id = ids.split(";");
                List<Long> idList = new ArrayList<Long>();
                for (int i = 0; i < id.length; i++) {
                    idList.add(Long.parseLong(id[i]));
                }
                wrapper = opnameService.getStorageItemData(idList);
            }
            if (wrapper != null) {
                if (wrapper.isSuccess()) {
                    //Put result
                    for (WarehouseItemStorageStock itemStorage : wrapper.getContent()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID, itemStorage.getId() + "");
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMCATEGORY, itemStorage.getWarehouseItem().getItem().getCategory());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMNAME, itemStorage.getWarehouseItem().getItem().getName());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMSKU, itemStorage.getWarehouseItem().getItem().getCode());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMUOM, itemStorage.getWarehouseItem().getItem().getItemUnit());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_QTY, itemStorage.getQuantity() + "");
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_SHELFCODE, itemStorage.getStorage().getShelf().getCode());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE, itemStorage.getStorage().getCode());
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
