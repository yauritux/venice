package com.gdn.venice.server.app.inventory.command;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentFinalItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrderItem;
import com.gdn.inventory.exchange.type.ASNReferenceType;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.app.inventory.service.ASNManagementService;
import com.gdn.venice.server.app.inventory.service.GRNManagementService;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.data.RafDsRequest;

public class GetMaxGRNItemQuantityAllowedDataCommand implements RafRpcCommand {

	String url, asnItemId, username;
	GRNManagementService grnService;
	ASNManagementService asnService;
	protected static Logger _log = null;

	public GetMaxGRNItemQuantityAllowedDataCommand(String username, String asnItemId) {		
		this.asnItemId = asnItemId;
		this.username = username;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.GetMaxGRNItemQuantityAllowedDataCommand");
	}

	@Override
	public String execute() {
		Integer allowed = null;
		try {
			grnService = new GRNManagementService();
			asnService = new ASNManagementService();
			_log.info("GetMaxGRNItemQuantityAllowedDataCommand");	
			Integer created = grnService.getASNItemQuantityAlreadyCreated(asnItemId)!=null?grnService.getASNItemQuantityAlreadyCreated(asnItemId):0;									
			_log.info("qty already created: "+created);
									
			HashMap<String, String> params = new HashMap<String, String>();
	        params.put("username", username);			
			RafDsRequest request = new RafDsRequest();
			request.setParams(params);
			
			ResultWrapper<AdvanceShipNoticeItem> asnItem = grnService.findItemByASNItemId(asnItemId);
			Integer oriQty = null;
			if(asnItem.getContent().getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.PURCHASE_ORDER.name())){
				ResultWrapper<PurchaseOrderItem> poItemWrapper = asnService.getPOItemData(request, asnItem.getContent().getReferenceNumber().toString());
				oriQty = poItemWrapper.getContent().getQuantity();
			}else if(asnItem.getContent().getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.CONSIGNMENT_FINAL.name())){
				_log.debug("type: "+asnItem.getContent().getAdvanceShipNotice().getReferenceType().name());
				_log.debug("number: "+asnItem.getContent().getReferenceNumber());
				ResultWrapper<ConsignmentFinalItem> cffItemWrapper = asnService.getCFFItemData(request, asnItem.getContent().getReferenceNumber().toString());
				oriQty = cffItemWrapper.getContent().getQuantity();
			}
			_log.info("qty original: "+oriQty);
			
			allowed = oriQty-created;
			_log.info("quantity allowed: "+allowed);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return allowed.toString();		
	}	
}
