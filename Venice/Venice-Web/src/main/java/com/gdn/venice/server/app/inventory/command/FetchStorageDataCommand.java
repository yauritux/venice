package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdn.inventory.exchange.entity.Storage;
import com.gdn.inventory.exchange.entity.StorageWIP;
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
public class FetchStorageDataCommand implements RafDsCommand {

    private RafDsRequest request;
    ShelfManagementService shelfService;
    
    public FetchStorageDataCommand(RafDsRequest request) {
        this.request = request;
    }

    @Override
    public RafDsResponse execute() {
    	System.out.println("fetch storage command");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        
        try {
        		shelfService = new ShelfManagementService();
                InventoryPagingWrapper<StorageWIP> storageWrapper = shelfService.getStorageData(request, new Long(request.getParams().get(DataNameTokens.INV_SHELF_ID)));
                if(storageWrapper != null){

                System.out.println("storageWrapper size: "+storageWrapper.getContent().size());
                for(StorageWIP storage : storageWrapper.getContent()){
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(DataNameTokens.INV_STORAGE_ID, storage.getId().toString());
                    map.put(DataNameTokens.INV_STORAGE_CODE, storage.getCode());
                    map.put(DataNameTokens.INV_STORAGE_DESCRIPTION, storage.getDescription());
                    map.put(DataNameTokens.INV_STORAGE_TYPE, storage.getType());
                    
                    dataList.add(map);
                }

                rafDsResponse.setStatus(0);
                rafDsResponse.setStartRow(request.getStartRow());
                rafDsResponse.setTotalRows((int) storageWrapper.getTotalElements());
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
