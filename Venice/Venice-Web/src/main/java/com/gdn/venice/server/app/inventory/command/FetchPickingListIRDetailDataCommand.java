package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.entity.module.outbound.InventoryRequest;
import com.gdn.inventory.exchange.entity.module.outbound.InventoryRequestItem;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackage;
import com.gdn.inventory.wrapper.ResultListWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
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
public class FetchPickingListIRDetailDataCommand implements RafDsCommand {

    private RafDsRequest request;
    PickingListManagementService pickingListService;
    PutawayManagementService putawayService;
    protected static Logger _log = null;
    
    public FetchPickingListIRDetailDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchPickingListIRDetailDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	System.out.println("FetchPickingListIRDetailDataCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
        	pickingListService = new PickingListManagementService();        	
        	putawayService = new PutawayManagementService();
        	
        	ResultWrapper<PickPackage> pickPackageWrapper = pickingListService.getSinglePickingListIR(request.getParams().get("username"), request.getParams().get(DataNameTokens.INV_PICKINGLISTIR_PACKAGEID));
        	if(pickPackageWrapper!=null && pickPackageWrapper.isSuccess()){
        		InventoryRequest inventoryRequest = pickPackageWrapper.getContent().getInventoryRequest();
        		
        		ResultListWrapper<InventoryRequestItem> irItemWrapper = pickingListService.getIRItemByIRId(Long.toString(inventoryRequest.getId()));
        		for(InventoryRequestItem irItem : irItemWrapper.getContents()){	        		

					
					WarehouseItem whItem = putawayService.getWarehouseItemData(irItem.getItem().getId(), 
							irItem.getInventoryRequest().getFromWarehouse().getId(), 
							irItem.getInventoryRequest().getSupplier().getId(), irItem.getInventoryRequest().getInventoryType());
					
                    if(whItem!=null){
    					System.out.println("whItem Id: "+whItem.getId());
                    	List<WarehouseItemStorageStock> storageStockList = putawayService.getWarehouseItemStorageList(whItem.getId());
                    	if(storageStockList.size()>0){
	                    	for(WarehouseItemStorageStock storageStock : storageStockList){
	        	        		HashMap<String, String> map = new HashMap<String, String>(); 
	        					map.put(DataNameTokens.INV_PICKINGLISTIR_INVENTORYREQUESTCODE, inventoryRequest.getIrNumber());
	        					map.put(DataNameTokens.INV_PICKINGLISTIR_WAREHOUSESKUID, irItem.getItem().getCode());
	        					map.put(DataNameTokens.INV_PICKINGLISTIR_WAREHOUSESKUNAME, irItem.getItem().getName());
	        					map.put(DataNameTokens.INV_PICKINGLISTIR_QTY, Long.toString(irItem.getQuantity()));        					                 		
	                        	map.put(DataNameTokens.INV_PICKINGLISTIR_STORAGECODE, storageStock.getStorage().getCode()!=null?storageStock.getStorage().getCode():"");
	                        	map.put(DataNameTokens.INV_PICKINGLISTIR_QTYSTORAGE, String.valueOf(storageStock.getQuantity())!=null?String.valueOf(storageStock.getQuantity()):"0");
	                            
	                        	dataList.add(map);
	                    	}        
                    	}else{
                    		HashMap<String, String> map = new HashMap<String, String>(); 
        					map.put(DataNameTokens.INV_PICKINGLISTIR_INVENTORYREQUESTCODE, inventoryRequest.getIrNumber());
        					map.put(DataNameTokens.INV_PICKINGLISTIR_WAREHOUSESKUID, irItem.getItem().getCode());
        					map.put(DataNameTokens.INV_PICKINGLISTIR_WAREHOUSESKUNAME, irItem.getItem().getName());
        					map.put(DataNameTokens.INV_PICKINGLISTIR_QTY, Long.toString(irItem.getQuantity()));        					                 		
                        	map.put(DataNameTokens.INV_PICKINGLISTIR_STORAGECODE, "");
                        	map.put(DataNameTokens.INV_PICKINGLISTIR_QTYSTORAGE, "0");
                            
                        	dataList.add(map);
                    	}
                    }else{
                    	_log.error("Warehouse item not found");
                    }   
                    
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
