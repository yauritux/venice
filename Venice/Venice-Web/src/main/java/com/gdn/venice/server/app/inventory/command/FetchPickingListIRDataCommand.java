package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackage;
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
public class FetchPickingListIRDataCommand implements RafDsCommand {

    private RafDsRequest request;
    PickingListManagementService pickingListService;
    protected static Logger _log = null;
    
    public FetchPickingListIRDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchPickingListIRDataCommand");
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        System.out.println("FetchPickingListIRDataCommand");
        try {
        		pickingListService = new PickingListManagementService();
                InventoryPagingWrapper<PickPackage> pickPackageWrapper = pickingListService.getPickingListIR(request.getParams().get("username"), request);
                if(pickPackageWrapper!=null && pickPackageWrapper.isSuccess()){	 		        
	                for(PickPackage pp : pickPackageWrapper.getContent()){
	                    HashMap<String, String> map = new HashMap<String, String>();
	                    map.put(DataNameTokens.INV_PICKINGLISTIR_PACKAGEID, Long.toString(pp.getId()));  
	                    map.put(DataNameTokens.INV_PICKINGLISTIR_DETAIL, "Detail");
	                    map.put(DataNameTokens.INV_PICKINGLISTIR_PACKAGECODE, pp.getCode());
	                    map.put(DataNameTokens.INV_PICKINGLISTIR_INVENTORYTYPE, pp.getInventoryRequest().getInventoryType().name());
	                    map.put(DataNameTokens.INV_PICKINGLISTIR_IRTYPE, pp.getInventoryRequest().getType().name());
	                    map.put(DataNameTokens.INV_PICKINGLISTIR_PICKERID, pp.getAssignedPicker()!=null?Long.toString(pp.getAssignedPicker().getId()):"");
	                    map.put(DataNameTokens.INV_PICKINGLISTIR_PICKERNAME, pp.getAssignedPicker()!=null?pp.getAssignedPicker().getName():"");
	                    	                    	                  
	                    dataList.add(map);
	                }
	                
	                rafDsResponse.setStatus(0);
	                rafDsResponse.setStartRow(request.getStartRow());
	                rafDsResponse.setTotalRows((int) pickPackageWrapper.getTotalElements());
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
