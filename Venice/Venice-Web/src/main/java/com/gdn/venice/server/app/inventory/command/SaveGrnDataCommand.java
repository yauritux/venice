package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.AdvanceShipNotice;
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.Warehouse;
import com.gdn.inventory.exchange.entity.module.inbound.GoodReceivedNote;
import com.gdn.inventory.exchange.entity.module.inbound.GoodReceivedNoteItem;
import com.gdn.inventory.exchange.type.ASNReferenceType;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.GRNManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.util.InventoryUtil;

public class SaveGrnDataCommand implements RafRpcCommand {

	HashMap<String, String> grnMap;
	HashMap<String, String> itemMap;
	String username, url;
	GRNManagementService grnService;
	protected static Logger _log = null;

	public SaveGrnDataCommand(String username, String data) {
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.SaveGrnDataCommand");
		
		String[] split = data.split("#");
		_log.debug("split 0: "+split[0]);
		_log.debug("split 1: "+split[1]);
		
		grnMap = Util.formHashMapfromXML(split[0]);
		itemMap = Util.formHashMapfromXML(split[1]);
		
		this.username = username;
	}

	@Override
	public String execute() {
		GoodReceivedNote grn;
		List<GoodReceivedNoteItem> itemList = new ArrayList<GoodReceivedNoteItem>();
		ResultWrapper<GoodReceivedNote> grnWrapper;
		try {
			grnService = new GRNManagementService();
			_log.info("SaveGrnDataCommand");
			grn = new GoodReceivedNote();
			
			AdvanceShipNotice asn = new AdvanceShipNotice();
			asn.setId(new Long(grnMap.get(DataNameTokens.INV_ASN_ID)));
			asn.setReferenceNumber(grnMap.get(DataNameTokens.INV_ASN_REFF_NUMBER));			
			asn.setReferenceType(ASNReferenceType.valueOf(grnMap.get(DataNameTokens.INV_ASN_REFF_TYPE)));
			
			_log.debug("asn reff number: "+asn.getReferenceNumber());
			_log.debug("asn reff type: "+asn.getReferenceType());
			
			Warehouse destination = new Warehouse();
			destination.setName(grnMap.get(DataNameTokens.INV_ASN_DESTINATION));
			
			grn.setAdvanceShipNotice(asn);					
			grn.setReceivedWarehouse(destination);			
							
			for(Map.Entry<String, String> entry : itemMap.entrySet()){
				String value = entry.getValue();
				
				GoodReceivedNoteItem grnItem = new GoodReceivedNoteItem();
				HashMap<String, String> map = InventoryUtil.convertToHashMap(value);
				for(Map.Entry<String, String> e : map.entrySet()){
					String k = e.getKey();
					String v = e.getValue();
					_log.debug("item key: "+k);
					_log.debug("item value: "+v);
					
					if(k.equals(DataNameTokens.INV_ASN_ITEM_ID)){				
						AdvanceShipNoticeItem item = new AdvanceShipNoticeItem();
						item.setId(new Long(v));						
						grnItem.setAdvanceShipNoticeItem(item);
					}
					if(k.equals(DataNameTokens.INV_POCFF_QTY)){
						grnItem.setQuantity(Integer.parseInt(v));
					}
				}	

				itemList.add(grnItem);
			}
			
			_log.debug("item size: "+itemList.size());			
			grnWrapper = grnService.saveGrn(username, grn, itemList);
			
			if(grnWrapper != null){
				if(!grnWrapper.isSuccess()){
					return grnWrapper.getError();
				}
			} else {
				return "Failed saving grn, error connection";
			}
		} catch (Exception e) {
			return "Failed saving grn, try again later. If error persist please contact administrator";
		}
		
		return "0";
	}
}
