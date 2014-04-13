/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.module.outbound.OpnameDetail;
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
public class FetchOpnameDetailDataCommand implements RafDsCommand {

    private String opnameId;
    OpnameService opnameService;

    public FetchOpnameDetailDataCommand(String opnameId) {
        this.opnameId = opnameId;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        try {
            opnameService = new OpnameService();
            ResultWrapper<List<OpnameDetail>> wrapper = opnameService.getOpnameDetailData(opnameId);
            if (wrapper != null) {
                if (wrapper.isSuccess()) {
                    //Put result
                    for (OpnameDetail opnameDetail : wrapper.getContent()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID, opnameDetail.getId() + "");
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMCATEGORY, opnameDetail.getCategory());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMNAME, opnameDetail.getItemName());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMSKU, opnameDetail.getItemCode());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMUOM, opnameDetail.getUom());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_QTY, opnameDetail.getQuantity() + "");
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_SHELFCODE, opnameDetail.getShelfCode());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE, opnameDetail.getStorageCode());
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_NEWQTY, "");
                        map.put(DataNameTokens.INV_OPNAME_ITEMSTORAGE_NOTE, "");
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
