package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gdn.inventory.exchange.entity.module.outbound.OpnameDetail;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.OpnameService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * @author Mria Olivia
 */
public class AddOpnameDetailDataCommand implements RafDsCommand {

    RafDsRequest request;
    String username;
    OpnameService opnameService;

    public AddOpnameDetailDataCommand(String username, RafDsRequest request) {
        this.request = request;
        this.username = username;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
            opnameService = new OpnameService();
            OpnameDetail opnameDetail = new OpnameDetail();
            Map<String, String> data = request.getData().get(0);
            System.out.println("data: " + data);
            opnameDetail.setCategory(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMCATEGORY));
            opnameDetail.setItemCode(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMSKU));
            opnameDetail.setItemName(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMNAME));
            opnameDetail.setNewQuantity(Integer.parseInt(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_NEWQTY)));
            opnameDetail.setNote(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_NOTE));
            opnameDetail.setQuantity(Integer.parseInt(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_QTY)));
            opnameDetail.setShelfCode(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_SHELFCODE));
            opnameDetail.setStorageCode(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_STORAGECODE));
            opnameDetail.setUom(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ITEMUOM));

            ResultWrapper<String> wrapper = opnameService.saveOrUpdateOpnameDetail(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID), opnameDetail, username);
            if (wrapper != null && wrapper.isSuccess()) {
                rafDsResponse.setStatus(0);
            } else {
                rafDsResponse.setStatus(-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            rafDsResponse.setStatus(-1);
        }

        rafDsResponse.setData(dataList);
        return rafDsResponse;
    }
}
