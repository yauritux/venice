package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchPickingListDataCommand implements RafDsCommand {

    private RafDsRequest request;
    PickingListManagementService pickingListService;
    protected static Logger _log = null;
    
    public FetchPickingListDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchPickingListDataCommand");
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        _log.info("FetchPickingListDataCommand");
        try {
        		pickingListService = new PickingListManagementService();
                InventoryPagingWrapper<WarehouseItem> warehouseItemwrapper = pickingListService.getPickingList(request);
                if(warehouseItemwrapper != null){
	 		        
	                for(WarehouseItem whi : warehouseItemwrapper.getContent()){
	                    HashMap<String, String> map = new HashMap<String, String>();
	                    map.put(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMID, Long.toString(whi.getId()));                    	                    
	                    map.put(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMSKU, whi.getItem().getCode());
	                    map.put(DataNameTokens.INV_PICKINGLIST_ITEMSKUNAME, whi.getItem().getName());
	                    map.put(DataNameTokens.INV_PICKINGLIST_STOCKTYPE, whi.getStockType().name());
	                    map.put(DataNameTokens.INV_PICKINGLIST_MERCHANT, whi.getSupplier().getName());
	                    map.put(DataNameTokens.INV_PICKINGLIST_QTY, whi.getSumSO().toString());
	                    
	                    String qtyPicked = "0";
                    	map.put(DataNameTokens.INV_PICKINGLIST_QTYPICKED, qtyPicked);
	                    	                    
	                    dataList.add(map);
	                }
	
	                rafDsResponse.setStatus(0);
	                rafDsResponse.setStartRow(request.getStartRow());
	                rafDsResponse.setTotalRows((int) warehouseItemwrapper.getTotalElements());
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
