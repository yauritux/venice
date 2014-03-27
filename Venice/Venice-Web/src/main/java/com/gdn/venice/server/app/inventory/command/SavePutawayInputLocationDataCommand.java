package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.module.inbound.Putaway;
import com.gdn.inventory.exchange.entity.module.inbound.PutawayDetail;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.PutawayManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.util.InventoryUtil;

public class SavePutawayInputLocationDataCommand implements RafRpcCommand {

	HashMap<String, String> itemMap;
	String username, url;
	PutawayManagementService putawayService;
	protected static Logger _log = null;

	public SavePutawayInputLocationDataCommand(String username, String data) {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.SavePutawayInputLocationDataCommand");

		itemMap = Util.formHashMapfromXML(data);
		System.out.println("itemMap di constructor: "+itemMap.toString());
		this.username = username;
	}

	@Override
	public String execute() {
		System.out.println("SavePutawayInputLocationDataCommand");
		List<PutawayDetail> putawayDetailList = new ArrayList<PutawayDetail>();
		ResultWrapper<PutawayDetail> putawayDetailWrapper;
		try {
			putawayService = new PutawayManagementService();	
			Putaway putaway = new Putaway();
								
			for(Map.Entry<String, String> entry : itemMap.entrySet()){
				String val = entry.getValue();
				
				PutawayDetail putawayDetail = new PutawayDetail();
				HashMap<String, String> map = InventoryUtil.convertToHashMap(val);
				for(Map.Entry<String, String> e : map.entrySet()){
					String key = e.getKey();
					String value = e.getValue();
					System.out.println("item key: "+key);
					System.out.println("item value: "+value);
					
					if(key.equals(DataNameTokens.INV_PUTAWAY_GRN_WAREHOUSEITEMID)){
						putawayDetail.setWarehouseItemId(value);
					}
					if(key.equals(DataNameTokens.INV_PUTAWAY_ID)){
						putaway.setId(Long.parseLong(value));						
						putawayDetail.setPutaway(putaway);
					}
					if(key.equals(DataNameTokens.INV_PUTAWAY_GRN_ITEMID)){	
						putawayDetail.setId(new Long(value));
					}
					if(key.equals(DataNameTokens.INV_PUTAWAY_GRN_ITEMCODE)){	
						putawayDetail.setItemCode(value);
					}
					if(key.equals(DataNameTokens.INV_PUTAWAY_GRN_SHELFCODE_INPUT)){
						putawayDetail.setStorageCode(value);
					}					
					if(key.equals(DataNameTokens.INV_PUTAWAY_GRN_QTY_INPUT)){
						putawayDetail.setQty(Integer.parseInt(value));
					}					
				}				
				putawayDetailList.add(putawayDetail);
			}
						
			System.out.println("putaway detail size: "+putawayDetailList.size());			
			putawayDetailWrapper = putawayService.savePutawayInputLocation(username, putawayDetailList);
			if(!putawayDetailWrapper.isSuccess()){
				return putawayDetailWrapper.getError();
			}
		} catch (Exception e) {
			return "Failed saving putaway, try again later. If error persist please contact administrator";
		}
		
		return "0";
	}
}
