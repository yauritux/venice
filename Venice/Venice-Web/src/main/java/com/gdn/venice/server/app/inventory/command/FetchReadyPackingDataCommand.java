/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.dto.PackingList;
import com.gdn.inventory.exchange.entity.module.outbound.AWBInfo;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultListWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
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
            ResultListWrapper<PackingList> wrapper = packingService.getReadyPackingData(request);
            if (wrapper != null && wrapper.isSuccess()) {
                //Put result
                for (PackingList packingList : wrapper.getContents()) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(DataNameTokens.INV_PACKING_PICKPACKAGE_ID, packingList.getPickPackage()+"");
                    map.put(DataNameTokens.INV_PACKING_PICKPACKAGE_CODE, packingList.getCode());
                    map.put(DataNameTokens.INV_PACKING_PICKPACKAGE_CONTAINERID, packingList.getContainerId());
                    map.put(DataNameTokens.INV_PACKING_PICKPACKAGE_HANDLING, packingList.getTipePenanganan());
                    map.put(DataNameTokens.INV_PACKING_PICKPACKAGE_PICKER, packingList.getPicker());
                    map.put(DataNameTokens.INV_PACKING_AWB_NUMBER, packingList.getAwbNumber());
                    map.put(DataNameTokens.INV_PACKING_AWB_LOGISTIC, packingList.getLogistic());
                    map.put(DataNameTokens.INV_PACKING_PICKPACKAGE_STATUS, packingList.getStatus().name());
                    map.put(DataNameTokens.INV_PACKING_PICKPACKAGE_CLAIMEDBY, packingList.getClaimer());
                    dataList.add(map);
                }

                //Set DSResponse's properties
                rafDsResponse.setStatus(0);
                rafDsResponse.setStartRow(request.getStartRow());
                rafDsResponse.setTotalRows(wrapper.getContents().size());
                rafDsResponse.setEndRow(request.getStartRow() + dataList.size());
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
