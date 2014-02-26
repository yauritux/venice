package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdn.inventory.exchange.entity.Shelf;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ShelfManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchShelfDataCommand implements RafDsCommand {

    private RafDsRequest request;
    ShelfManagementService shelfService;
    
    public FetchShelfDataCommand(RafDsRequest request) {
        this.request = request;
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        System.out.println("fetch shelf command");
        try {
        		shelfService = new ShelfManagementService();
                InventoryPagingWrapper<Shelf> shelfWrapper = shelfService.getShelfData(request);
                if(shelfWrapper != null){

                System.out.println(shelfWrapper.getContent().size());
                for(Shelf shelf : shelfWrapper.getContent()){
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(DataNameTokens.INV_SHELF_ID, shelf.getId().toString());
                    map.put(DataNameTokens.INV_SHELF_CODE, shelf.getCode());
                    map.put(DataNameTokens.INV_SHELF_DESCRIPTION, shelf.getDescription());
                    map.put(DataNameTokens.INV_SHELF_ACTIVESTATUS, shelf.isActive() ? "Active" : "Non Active");
                    map.put(DataNameTokens.INV_SHELF_APPROVAL_IN_PROCESS, shelf.isApprovalInProcess() ? "true" : "false");
                    
                    dataList.add(map);
                }

                rafDsResponse.setStatus(0);
                rafDsResponse.setStartRow(request.getStartRow());
                rafDsResponse.setTotalRows((int) shelfWrapper.getTotalElements());
                rafDsResponse.setEndRow(request.getStartRow() + dataList.size());
            }
        } catch (Throwable e) {
            e.printStackTrace();
            rafDsResponse.setStatus(-1);
        }

        rafDsResponse.setData(dataList);
        return rafDsResponse;
    }
}
