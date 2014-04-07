/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.module.outbound.AWBInfo;
import com.gdn.inventory.exchange.entity.module.outbound.GoodIssuedNote;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.GINService;
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
public class FetchGinDataCommand implements RafDsCommand {

    private RafDsRequest request;
    GINService ginService;

    public FetchGinDataCommand(RafDsRequest request) {
        this.request = request;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

        try {
            ginService = new GINService();
            InventoryPagingWrapper<GoodIssuedNote> wrapper = ginService.getGinData(request);
            if (wrapper != null) {
                if (wrapper.isSuccess()) {
                    //Put result
                    for (GoodIssuedNote gin : wrapper.getContent()) {
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(DataNameTokens.INV_GIN_ID, gin.getId().toString());
                        map.put(DataNameTokens.INV_GIN_NO, gin.getGinNumber());
                        map.put(DataNameTokens.INV_GIN_DATE, gin.getCreatedDate().toString());
                        map.put(DataNameTokens.INV_GIN_LOGISTIC, gin.getLogistic());
                        map.put(DataNameTokens.INV_GIN_NOTE, gin.getSpecialNote());
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
