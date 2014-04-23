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
 * @author Maria Olivia
 */
public class UpdateOpnameDetailDataCommand implements RafDsCommand {

    RafDsRequest request;
    String username;
    OpnameService opnameService;

    public UpdateOpnameDetailDataCommand(String username, RafDsRequest request) {
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
            System.out.println("data: " + request.getData());
            opnameDetail.setId(Long.parseLong(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_ID)));
            int newQty = data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_NEWQTY) == null? 0:Integer.parseInt(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_NEWQTY));
            opnameDetail.setNewQuantity(newQty);
            opnameDetail.setNote(data.get(DataNameTokens.INV_OPNAME_ITEMSTORAGE_NOTE));

            ResultWrapper<String> wrapper = opnameService.saveOrUpdateOpnameDetail(null, opnameDetail, username);
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
