/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.Supplier;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.OpnameService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Maria Olivia
 */
public class FetchSupplierDataCommand implements RafDsCommand {

    private String warehouseCode, stockType;
    OpnameService opnameService;

    public FetchSupplierDataCommand(String warehouseCode, String stockType) {
        this.warehouseCode = warehouseCode;
        this.stockType = stockType;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        try {
            opnameService = new OpnameService();
            ResultWrapper<List<Supplier>> wrapper = opnameService.getSupplierData(warehouseCode, stockType);
            if (wrapper != null) {
                if (wrapper.isSuccess()) {
                    //Put result
                    for (Supplier supplier : wrapper.getContent()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(DataNameTokens.INV_OPNAME_SUPPLIERCODE, supplier.getCode());
                        map.put(DataNameTokens.INV_OPNAME_SUPPLIERNAME, supplier.getName());
                        dataList.add(map);
                    }

                    //Set DSResponse's properties
                    rafDsResponse.setStatus(0);
                    rafDsResponse.setStartRow(0);
                    rafDsResponse.setTotalRows(wrapper.getContent().size());
                    rafDsResponse.setEndRow(0 + dataList.size());
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
