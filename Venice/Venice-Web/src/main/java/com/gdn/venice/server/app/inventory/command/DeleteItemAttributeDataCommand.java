package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.Attribute;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.GRNManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class DeleteItemAttributeDataCommand implements RafDsCommand {

    private RafDsRequest request;
    GRNManagementService grnService;
    protected static Logger _log = null;
    
    public DeleteItemAttributeDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.DeleteItemAttributeDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	_log.info("DeleteItemAttributeDataCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
        	grnService = new GRNManagementService();
        	
			dataList=request.getData();
			for(int i=0;i< dataList.size();i++){
				Map<String, String> data = dataList.get(i);				
				Iterator<String> iter=data.keySet().iterator();
				
				Attribute attribute = new Attribute();
				
				while(iter.hasNext()){
					String key=iter.next();
					if(key.equals(DataNameTokens.INV_ITEM_ATTRIBUTE_ID)){
						attribute.setId(new Long(data.get(key)));
					}
				}
				
				grnService.deleteItemAttribute(request.getParams().get("username"), attribute);
			}            			            		
	        		
	        rafDsResponse.setStatus(0);
        } catch (Throwable e) {
            e.printStackTrace();
            rafDsResponse.setStatus(-1);
        }

        rafDsResponse.setData(dataList);
        return rafDsResponse;
    }
}
