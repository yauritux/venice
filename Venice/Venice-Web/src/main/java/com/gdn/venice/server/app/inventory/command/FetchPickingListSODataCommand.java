package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackage;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackageSalesOrder;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultListWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchPickingListSODataCommand implements RafDsCommand {

    RafDsRequest request;
    PickingListManagementService pickingListService;
    protected static Logger _log = null;
    
    public FetchPickingListSODataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchPickingListSODataCommand");
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        System.out.println("FetchPickingListSODataCommand");
        try {
        		pickingListService = new PickingListManagementService();
                InventoryPagingWrapper<PickPackage> pickPackageWrapper = pickingListService.getPickingListSO(request);
                if(pickPackageWrapper!=null && pickPackageWrapper.isSuccess()){	 		        
	                for(PickPackage pp : pickPackageWrapper.getContent()){
	                    HashMap<String, String> map = new HashMap<String, String>();
	                    map.put(DataNameTokens.INV_PICKINGLISTSO_PACKAGEID, Long.toString(pp.getId()));  
	                    map.put(DataNameTokens.INV_PICKINGLISTSO_DETAIL, "Detail");
	                    map.put(DataNameTokens.INV_PICKINGLISTSO_PACKAGECODE, pp.getCode());
	                    
	                    ResultListWrapper<PickPackageSalesOrder> ppso = pickingListService.getPickingListSODetail(request.getParams().get("username"), pp.getId().toString());
	                    map.put(DataNameTokens.INV_PICKINGLISTSO_MERCHANTNAME, ppso!=null?ppso.getContents().get(0).getSalesOrder().getSupplier()!=null?ppso.getContents().get(0).getSalesOrder().getSupplier().getName():"":"");
	                    map.put(DataNameTokens.INV_PICKINGLISTSO_PICKERID, pp.getAssignedPicker()!=null?Long.toString(pp.getAssignedPicker().getId()):"");
	                    map.put(DataNameTokens.INV_PICKINGLISTSO_PICKERNAME, pp.getAssignedPicker()!=null?pp.getAssignedPicker().getName():"");
	                    	                    	                  
	                    dataList.add(map);
	                }
	                
	                rafDsResponse.setStatus(0);
	                rafDsResponse.setStartRow(request.getStartRow());
	                rafDsResponse.setTotalRows(dataList.size());
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
