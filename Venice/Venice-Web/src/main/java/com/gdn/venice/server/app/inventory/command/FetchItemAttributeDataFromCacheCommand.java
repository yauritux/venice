package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.Attribute;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.GRNManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchItemAttributeDataFromCacheCommand implements RafDsCommand {

    private RafDsRequest request;
    GRNManagementService grnService;
    protected static Logger _log = null;
    
    public FetchItemAttributeDataFromCacheCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchItemAttributeDataFromCacheCommand");
    }

    @Override
    public RafDsResponse execute() {
    	System.out.println("FetchItemAttributeDataFromCacheCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
        	grnService = new GRNManagementService();

        	String itemIdParam = null;
        	if(request.getParams().get(DataNameTokens.INV_ASN_ITEM_ID)!=null){
        		System.out.println("item id from asn item");
        		itemIdParam = request.getParams().get(DataNameTokens.INV_ASN_ITEM_ID);
        	}
        	
        	ResultWrapper<List<Attribute>> attributeWrapper = grnService.getAttributeFromCache(itemIdParam);
        	
        	if(attributeWrapper!=null && attributeWrapper.isSuccess()){  
        		List<Attribute> attributeList = attributeWrapper.getContent();
				System.out.println("attribute found: "+attributeList.size());
				
				String[] fieldName = request.getParams().get("fieldName").split(";");
				int counter = 0;
				HashMap<String, String> map = new HashMap<String, String>();
				for(int i=0;i<attributeList.size();i++){   
	    			for(int j=0;j<fieldName.length;j++){  
						if(fieldName[j].equalsIgnoreCase(attributeList.get(i).getName())){
	        				System.out.println("put fieldName: "+fieldName[j]+", value: "+attributeList.get(i).getValue());
							map.put(fieldName[j], attributeList.get(i).getValue());
							counter++;
							break;
						}
					}
					
	    			if(counter==fieldName.length){
	    	        	System.out.println("add map to list: "+map.toString());
	    				counter = 0;
	    				dataList.add(map);
	    	        	map = new HashMap<String, String>();
	    			}
				}    		           	  
        	}
        	System.out.println("dataList size: "+dataList.size());

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
