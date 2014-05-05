package com.gdn.venice.server.app.inventory.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.module.inbound.Putaway;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ASNManagementService;
import com.gdn.venice.server.app.inventory.service.GRNManagementService;
import com.gdn.venice.server.app.inventory.service.PutawayManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchPutawayDataCommand implements RafDsCommand {

    private RafDsRequest request;
    GRNManagementService grnService;
    ASNManagementService asnService;
    PutawayManagementService putawayService;
    protected static Logger _log = null;
    
    public FetchPutawayDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchPutawayDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	_log.info("FetchPutawayDataCommand");
    	System.out.println("warehouse id putaway input: "+request.getParams().get(DataNameTokens.INV_WAREHOUSE_ID));
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
        	putawayService = new PutawayManagementService();
        	
        	InventoryPagingWrapper<Putaway> putawayWrapper = putawayService.getPutawayListByWarehouseId(request.getParams().get(DataNameTokens.INV_WAREHOUSE_ID));
        	if(putawayWrapper!=null && putawayWrapper.isSuccess()){       
        		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        		for(Putaway putaway : putawayWrapper.getContent()){
					HashMap<String, String> map = new HashMap<String, String>(); 
					map.put(DataNameTokens.INV_PUTAWAY_ID, String.valueOf(putaway.getId()));
					map.put(DataNameTokens.INV_PUTAWAY_CREATEDDATE, sdf.format(putaway.getCreatedDate()));    			            	
		            map.put(DataNameTokens.INV_PUTAWAY_NUMBER, putaway.getPutawayNumber());
		            map.put(DataNameTokens.INV_PUTAWAY_STATUS, putaway.getPutawayStatus().name());
		            map.put(DataNameTokens.INV_PUTAWAY_TYPE, putaway.getPutawayType().name());
		            map.put(DataNameTokens.INV_PUTAWAY_GRN_ID, String.valueOf(putaway.getGoodReceivedNote().getId()));
	                    
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
