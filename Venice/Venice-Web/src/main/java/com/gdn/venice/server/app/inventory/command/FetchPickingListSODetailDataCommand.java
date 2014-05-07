package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackageSalesOrder;
import com.gdn.inventory.wrapper.ResultListWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.app.inventory.service.PutawayManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchPickingListSODetailDataCommand implements RafDsCommand {

    private RafDsRequest request;
    PickingListManagementService pickingListService;
    PutawayManagementService putawayService;
    protected static Logger _log = null;
    
    public FetchPickingListSODetailDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchPickingListSODetailDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	System.out.println("FetchPickingListSODetailDataCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
        	pickingListService = new PickingListManagementService();        	
        	putawayService = new PutawayManagementService();
        	
        	ResultListWrapper<PickPackageSalesOrder> pickPackageWrapper = pickingListService.getPickingListSODetail(request.getParams().get("username"), request.getParams().get(DataNameTokens.INV_PICKINGLISTSO_PACKAGEID));
        	
        	if(pickPackageWrapper!=null && pickPackageWrapper.isSuccess()){
        		for(PickPackageSalesOrder ppso : pickPackageWrapper.getContents()){        			    	
	        		HashMap<String, String> map = new HashMap<String, String>(); 
					map.put(DataNameTokens.INV_PICKINGLISTSO_SALESORDERCODE, ppso.getSalesOrder().getSalesOrderNumber());
					map.put(DataNameTokens.INV_PICKINGLISTSO_WAREHOUSESKUID, ppso.getSalesOrder().getAssignedItem().getCode());
					map.put(DataNameTokens.INV_PICKINGLISTSO_WAREHOUSESKUNAME, ppso.getSalesOrder().getAssignedItem().getName());
					map.put(DataNameTokens.INV_PICKINGLISTSO_QTY, Long.toString(ppso.getSalesOrder().getQuantity()));
					
					WarehouseItem whItem = putawayService.getWarehouseItemData(ppso.getSalesOrder().getAssignedItem().getId(), 
							ppso.getSalesOrder().getWarehouse().getId(), 
							ppso.getSalesOrder().getSupplier().getId(), ppso.getSalesOrder().getStockType());
					
					String shelfCode="";
                    if(whItem!=null){
    					System.out.println("whItem Id: "+whItem.getId());
                    	List<WarehouseItemStorageStock> storageStockList = putawayService.getWarehouseItemStorageList(whItem.getId());    	                    	
                    	for(WarehouseItemStorageStock storageStock : storageStockList){
                    		shelfCode+=storageStock.getStorage().getCode()+" / "+storageStock.getQuantity();
                    		shelfCode+=", ";
                    	}
                    	if(shelfCode.length()>1){
                    		shelfCode=shelfCode.substring(0, shelfCode.lastIndexOf(","));
                    	}else{
                    		shelfCode="-";
                    	}
                    	
                    	map.put(DataNameTokens.INV_PICKINGLISTIR_SHELFCODE, shelfCode);
                    }else{
                    	shelfCode="-";
                    	_log.error("Warehouse item not found");
                    }   
                    
                    dataList.add(map);
        		}
        	}
        	
	        rafDsResponse.setStatus(0);
	        rafDsResponse.setTotalRows(dataList.size());
	        rafDsResponse.setStartRow(request.getStartRow());
	        rafDsResponse.setEndRow(request.getStartRow() + dataList.size());
        } catch (Throwable e) {
            e.printStackTrace();
            rafDsResponse.setStatus(-1);
        }

        rafDsResponse.setData(dataList);
        return rafDsResponse;
    }
}
