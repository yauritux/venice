package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.Item;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentApprovalItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentFinalItem;
import com.gdn.inventory.exchange.entity.module.inbound.GoodReceivedNoteItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrderItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseRequisitionItem;
import com.gdn.inventory.exchange.entity.module.inbound.Putaway;
import com.gdn.inventory.exchange.entity.module.inbound.PutawayItem;
import com.gdn.inventory.exchange.type.ASNReferenceType;
import com.gdn.inventory.exchange.type.PutawayType;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ASNManagementService;
import com.gdn.venice.server.app.inventory.service.GRNManagementService;
import com.gdn.venice.server.app.inventory.service.PutawayManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.util.InventoryUtil;

public class SavePutawayDataCommand implements RafRpcCommand {

	HashMap<String, String> itemMap;
	String username, url;
	PutawayManagementService putawayService;
	GRNManagementService grnService;
	ASNManagementService asnService;
	protected static Logger _log = null;

	public SavePutawayDataCommand(String username, String data) {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.SavePutawayDataCommand");

		itemMap = Util.formHashMapfromXML(data);
		System.out.println("itemMap di constructor: "+itemMap.toString());
		this.username = username;
	}

	@Override
	public String execute() {
		System.out.println("SavePutawayDataCommand");
		List<PutawayItem> itemList = new ArrayList<PutawayItem>();
		ResultWrapper<Putaway> putawayWrapper;
		try {
			putawayService = new PutawayManagementService();	
			grnService = new GRNManagementService();
			asnService = new ASNManagementService();
			ResultWrapper<GoodReceivedNoteItem> grni = null;
								
			String grnItemId="";
			System.out.println("itemMap.entrySet() size: "+itemMap.entrySet().size());
			for(Map.Entry<String, String> entry : itemMap.entrySet()){	
				PutawayType pt = null;
				Putaway putaway = new Putaway();			
				putaway.setCreatedBy(username);				
				PutawayItem putawayItem = new PutawayItem();
				
				String val = entry.getValue();
				HashMap<String, String> map = InventoryUtil.convertToHashMap(val);
				for(Map.Entry<String, String> e : map.entrySet()){
					String key = e.getKey();
					String value = e.getValue();
					System.out.println("item key: "+key);
					System.out.println("item value: "+value);
					
					if(key.equals(DataNameTokens.INV_PUTAWAY_GRN_ITEMID)){	
						putawayItem.setId(new Long(value));
																		
						putawayItem.setPutaway(putaway);
						grnItemId = value;
					}
					if(key.equals(DataNameTokens.INV_PUTAWAY_GRN_TYPE)){
						if(value.equals("GRN")){
							pt = PutawayType.GRN;
						}else if(value.equals("PICKING_LIST")){
							pt = PutawayType.PICKING_LIST;
						}else if(value.equals("PACKING_LIST")){
							pt = PutawayType.PACKING_LIST;
						}
						
						putaway.setPutawayType(pt);	
					}
				}

				grni = grnService.findItemByGRNItemId(grnItemId);
				putaway.setGoodReceivedNote(grni.getContent().getGoodReceivedNote());
				
				AdvanceShipNoticeItem asnItem = grni.getContent().getAdvanceShipNoticeItem();

				Long itemId=null;
				RafDsRequest request = new RafDsRequest();
				HashMap<String, String> params = new HashMap<String, String>();
		        params.put("username", username);
		        request.setParams(params);
				if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.PURCHASE_ORDER.name())){
            		System.out.println("reff type: purchase order");                		                		
            		ResultWrapper<PurchaseOrderItem> poItemWrapper = asnService.getPOItemData(request, asnItem.getReferenceNumber().toString());
                	if(poItemWrapper!=null && poItemWrapper.isSuccess()){
                		PurchaseRequisitionItem prItem = poItemWrapper.getContent().getPurchaseRequisitionItem();
                		System.out.println("PO item found, id:"+prItem.getItem().getId());                    		                   				             
                		itemId = prItem.getItem().getId();      	                    	                    
                	}else{
                		System.out.println("PO item not found");
                	}   
            	}else if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.CONSIGNMENT_FINAL.name())){
            		System.out.println("reff type: consignment");
            		ResultWrapper<ConsignmentFinalItem> cffItemWrapper = asnService.getCFFItemData(request, asnItem.getReferenceNumber());
                	if(cffItemWrapper!=null && cffItemWrapper.isSuccess()){
                		ConsignmentApprovalItem cafItem = cffItemWrapper.getContent().getConsignmentApprovalItem();
                		System.out.println("CFF item found, id:"+cafItem.getItem().getId());                    		                   				             
                		itemId = cafItem.getItem().getId();                                   	                        	             
                	}else{
                		System.out.println("CFF item not found");
                	} 
            	}    
				            	
				Item item = new Item();
				item = putawayService.findItemById(username, itemId);
				putawayItem.setItem(item);
				itemList.add(putawayItem);				
			}
									
			System.out.println("item size: "+itemList.size());			
			putawayWrapper = putawayService.savePutaway(username, itemList);
			if(putawayWrapper==null || !putawayWrapper.isSuccess()){
				return putawayWrapper.getError();
			}
		} catch (Exception e) {
			return "Failed saving putaway.";
		}
		
		return "0";
	}
}
