/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.entity.module.outbound.Opname;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.OpnameService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Maria Olivia
 */
public class FetchOpnameDataCommand implements RafDsCommand {

    private RafDsRequest request;
    OpnameService opnameService;

    public FetchOpnameDataCommand(RafDsRequest request) {
        this.request = request;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        try {
            opnameService = new OpnameService();
            ResultWrapper<List<Opname>> wrapper = opnameService.getOpnameData(request);
            if (wrapper != null) {
                if (wrapper.isSuccess()) {
                    //Put result
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    for (Opname opname : wrapper.getContent()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(DataNameTokens.INV_OPNAME_ID, opname.getId() + "");
                        map.put(DataNameTokens.INV_OPNAME_NO, opname.getOpnameNo());
                        map.put(DataNameTokens.INV_OPNAME_CREATEDBY, opname.getCreatedBy());
                        map.put(DataNameTokens.INV_OPNAME_CREATEDDATE, sdf.format(opname.getCreatedDate()));
                        map.put(DataNameTokens.INV_OPNAME_STOCKTYPE, opname.getStockType().name());
                        map.put(DataNameTokens.INV_OPNAME_SUPPLIERCODE, opname.getSupplierCode());
                        map.put(DataNameTokens.INV_OPNAME_UPDATEDBY, opname.getUpdatedBy());
                        map.put(DataNameTokens.INV_OPNAME_UPDATEDDATE, sdf.format(opname.getUpdatedDate()));
                        map.put(DataNameTokens.INV_OPNAME_WAREHOUSECODE, opname.getWarehouseCode());
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
