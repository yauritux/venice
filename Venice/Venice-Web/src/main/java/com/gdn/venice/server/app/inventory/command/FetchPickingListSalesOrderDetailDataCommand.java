package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.module.outbound.PickingListDetail;
import com.gdn.inventory.exchange.entity.module.outbound.SalesOrder;
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
public class FetchPickingListSalesOrderDetailDataCommand implements RafDsCommand {

    private RafDsRequest request;
    PickingListManagementService pickingListService;
    protected static Logger _log = null;
    
    public FetchPickingListSalesOrderDetailDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchPickingListSalesOrderDetailDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	_log.info("FetchPickingListSalesOrderDetailDataCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        
        try {
        		pickingListService = new PickingListManagementService();
        		ResultWrapper<PickingListDetail> detailWrapper = pickingListService.getPickingListDetail(request);
                if(detailWrapper!=null && detailWrapper.isSuccess()){
                	PickingListDetail detail = detailWrapper.getContent();
                	if(detail.getSalesOrder()!=null){
                		List<SalesOrder> list = detail.getSalesOrder();
                		for(SalesOrder so : list){        	 
                    		HashMap<String, String> map = new HashMap<String, String>();
        	                map.put(DataNameTokens.INV_PICKINGLIST_SALESORDERID, so.getId().toString());
        	                map.put(DataNameTokens.INV_PICKINGLIST_SALESORDERNUMBER, so.getSalesOrderNumber());
        	                map.put(DataNameTokens.INV_PICKINGLIST_SALESORDERQTY, Integer.toString(so.getQuantity()));
        	                map.put(DataNameTokens.INV_PICKINGLIST_SALESORDERTIPEPENANGANAN, so.getTipePenanganan());
        	                
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
