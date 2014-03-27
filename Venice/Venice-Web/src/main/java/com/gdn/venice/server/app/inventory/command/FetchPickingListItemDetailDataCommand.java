package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.WarehouseItem;
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
public class FetchPickingListItemDetailDataCommand implements RafDsCommand {

    private RafDsRequest request;
    PickingListManagementService pickingListService;
    protected static Logger _log = null;
    
    public FetchPickingListItemDetailDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchPickingListItemDetailDataCommand");
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        _log.info("FetchPickingListItemDetailDataCommand");
        try {
        		pickingListService = new PickingListManagementService();
        		ResultWrapper<PickingListDetail> detailWrapper = pickingListService.getPickingListDetail(request);
                if(detailWrapper.isSuccess()){
                	PickingListDetail detail = detailWrapper.getContent();
	                HashMap<String, String> map = new HashMap<String, String>();
	                map.put(DataNameTokens.INV_PICKINGLIST_ITEMID, detail.getItem().getId().toString());
	                map.put(DataNameTokens.INV_PICKINGLIST_WAREHOUSEITEMSKU, detail.getItem().getCode());
	                map.put(DataNameTokens.INV_PICKINGLIST_ITEMSKUNAME, detail.getItem().getName());
	                
	                ResultWrapper<WarehouseItem> warehouseItemWrapper = pickingListService.getWarehouseItem(request);
	                
	                String stockType="", merchant="";
	                if(warehouseItemWrapper.isSuccess()){
	                	stockType = warehouseItemWrapper.getContent().getStockType().name();
	                	merchant = warehouseItemWrapper.getContent().getSupplier().getName();
	                }
	                map.put(DataNameTokens.INV_PICKINGLIST_STOCKTYPE, stockType);
	                map.put(DataNameTokens.INV_PICKINGLIST_MERCHANT, merchant);
	                String width = Float.toString(detail.getItem().getWidth());
	                String height = Float.toString(detail.getItem().getHeight());
	                String length = Float.toString(detail.getItem().getLength());
	                map.put(DataNameTokens.INV_PICKINGLIST_DIMENSION, width+"x"+height+"x"+length);
	                map.put(DataNameTokens.INV_PICKINGLIST_WEIGHT, Float.toString(detail.getItem().getWeight()));
	                map.put(DataNameTokens.INV_PICKINGLIST_UOM, detail.getItem().getItemUnit());
	                map.put(DataNameTokens.INV_PICKINGLIST_ATTRIBUTE, Boolean.valueOf(detail.getItem().isHasAttribute()).toString());
	                	                    
	                dataList.add(map);
	
	                rafDsResponse.setStatus(0);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            rafDsResponse.setStatus(-1);
        }

        rafDsResponse.setData(dataList);
        return rafDsResponse;
    }
}
