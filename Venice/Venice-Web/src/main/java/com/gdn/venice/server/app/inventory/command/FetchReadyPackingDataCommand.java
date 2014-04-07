/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.module.outbound.AWBInfo;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.PackingListService;
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
public class FetchReadyPackingDataCommand implements RafDsCommand {

    private RafDsRequest request;
    PackingListService packingService;

    public FetchReadyPackingDataCommand(RafDsRequest request) {
        this.request = request;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        try {
            packingService = new PackingListService();
            InventoryPagingWrapper<AWBInfo> wrapper = packingService.getReadyPackingData(request);
            if (wrapper != null) {
                if (wrapper.isSuccess()) {
                    //Put result
                    for (AWBInfo awbInfo : wrapper.getContent()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(DataNameTokens.INV_AWB_ID, awbInfo.getId().toString());
                        map.put(DataNameTokens.INV_AWB_NO, awbInfo.getAirwayBillNumber());
                        map.put(DataNameTokens.INV_AWB_PUDATE, awbInfo.getPuDate().toString());
                        map.put(DataNameTokens.INV_AWB_LOGNAME, awbInfo.getLogisticCode());
                        map.put(DataNameTokens.INV_AWB_STATUS, awbInfo.getStatus().getValue());
                        map.put(DataNameTokens.INV_AWB_OFFLINE, awbInfo.isOrderOffline() + "");
                        dataList.add(map);
                    }

                    //Set DSResponse's properties
                    rafDsResponse.setStatus(0);
                    rafDsResponse.setStartRow(request.getStartRow());
                    rafDsResponse.setTotalRows(Integer.parseInt(wrapper.getTotalElements() + ""));
                    rafDsResponse.setEndRow(request.getStartRow() + dataList.size());
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
