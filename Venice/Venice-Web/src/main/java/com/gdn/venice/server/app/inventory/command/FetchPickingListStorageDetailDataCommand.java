package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.entity.module.outbound.PickingListDetail;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchPickingListStorageDetailDataCommand implements RafDsCommand {

    private RafDsRequest request;
    PickingListManagementService pickingListService;
    protected static Logger _log = null;
    
    public FetchPickingListStorageDetailDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchPickingListStorageDetailDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	_log.info("FetchPickingListStorageDetailDataCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        
        try {
        		pickingListService = new PickingListManagementService();
        		ResultWrapper<PickingListDetail> detailWrapper = pickingListService.getPickingListDetail(request);
                if(detailWrapper.isSuccess()){
                	System.out.println("detailWrapper not null");
                	PickingListDetail detail = detailWrapper.getContent();
                	if(detail.getWhItemStorageStock()!=null){
                		System.out.println("getWhItemStorageStock not null");
                		List<WarehouseItemStorageStock> list = detail.getWhItemStorageStock();
                		for(WarehouseItemStorageStock wiss : list){        	 
                    		HashMap<String, String> map = new HashMap<String, String>();
        	                map.put(DataNameTokens.INV_PICKINGLIST_WAREHOUSESTORAGEID, wiss.getId().toString());
        	                map.put(DataNameTokens.INV_PICKINGLIST_SHELFCODE, wiss.getStorage().getShelf().getCode());
        	                map.put(DataNameTokens.INV_PICKINGLIST_QTY, Integer.toString(wiss.getQuantity()));
        	                map.put(DataNameTokens.INV_PICKINGLIST_QTYPICKED, "");
        	                	                    
        	                dataList.add(map);
                		}
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
