package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentApprovalItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentFinalItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrderItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseRequisitionItem;
import com.gdn.inventory.exchange.type.ASNReferenceType;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ASNManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchASNItemDataCommand implements RafDsCommand {

    private RafDsRequest request;
    ASNManagementService asnService;
    protected static Logger _log = null;
    
    public FetchASNItemDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchASNItemDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	_log.info("FetchASNItemDataCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
    		asnService = new ASNManagementService();        		
    		InventoryPagingWrapper<AdvanceShipNoticeItem> asnWrapper = asnService.getASNItemData(request, request.getParams().get(DataNameTokens.INV_ASN_ID));
            if(asnWrapper.isSuccess()){                 	
            	for(AdvanceShipNoticeItem asnItem: asnWrapper.getContent()){
            		if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.PURCHASE_ORDER.name())){
            			_log.info("reff type: purchase order");                		                		
                		ResultWrapper<PurchaseOrderItem> poItemWrapper = asnService.getPOItemData(request, asnItem.getReferenceNumber().toString());
                    	if(poItemWrapper.isSuccess()){
                    		PurchaseRequisitionItem item = poItemWrapper.getContent().getPurchaseRequisitionItem();
                    		_log.debug("PO item found, code:"+item.getItem().getCode());    
                    		
            				HashMap<String, String> map = new HashMap<String, String>();
            				map.put(DataNameTokens.INV_ASN_ITEM_ID, asnItem.getId().toString());
    	                    map.put(DataNameTokens.INV_POCFF_ITEMCODE, item.getItem().getCode());
    	                    map.put(DataNameTokens.INV_POCFF_ITEMDESC, item.getItem().getDescription());
    	                    map.put(DataNameTokens.INV_POCFF_QTY, Integer.toString(poItemWrapper.getContent().getQuantity()));
    	                    map.put(DataNameTokens.INV_POCFF_ITEMUNIT, item.getItem().getItemUnit());
    	                    map.put(DataNameTokens.INV_POCFF_ITEMLENGTH, String.valueOf(item.getItem().getLength()));
    	                    map.put(DataNameTokens.INV_POCFF_ITEMWIDTH, String.valueOf(item.getItem().getWidth()));
    	                    map.put(DataNameTokens.INV_POCFF_ITEMHEIGHT, String.valueOf(item.getItem().getHeight()));
    	                    map.put(DataNameTokens.INV_POCFF_VOLUME, String.valueOf(item.getItem().getLength()*item.getItem().getWidth()*item.getItem().getHeight()));
    	                    map.put(DataNameTokens.INV_POCFF_ITEMWEIGHT, String.valueOf(item.getItem().getWeight()));
    	                    map.put(DataNameTokens.INV_POCFF_QTYGRN, Integer.toString(asnItem.getGrnQuantity())); 
    	                    map.put(DataNameTokens.INV_POCFF_ITEMID, item.getItem().getId().toString());
    	                    
    	                    dataList.add(map);
                    	}else{
                    		_log.error("PO item not found");
                    	}   
                	}else if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.CONSIGNMENT_FINAL.name())){
                		_log.info("reff type: consignment");
                		ResultWrapper<ConsignmentFinalItem> cffItemWrapper = asnService.getCFFItemData(request, asnItem.getReferenceNumber());
                    	if(cffItemWrapper.isSuccess()){
                    		ConsignmentApprovalItem item = cffItemWrapper.getContent().getConsignmentApprovalItem();
                    		_log.debug("CFF item found, code:"+item.getItem().getCode());    
                    		
            				HashMap<String, String> map = new HashMap<String, String>();
            				map.put(DataNameTokens.INV_ASN_ITEM_ID, asnItem.getId().toString());
    	                    map.put(DataNameTokens.INV_POCFF_ITEMCODE, item.getItem().getCode());
    	                    map.put(DataNameTokens.INV_POCFF_ITEMDESC, item.getItem().getDescription());
    	                    map.put(DataNameTokens.INV_POCFF_QTY, Integer.toString(cffItemWrapper.getContent().getQuantity()));
    	                    map.put(DataNameTokens.INV_POCFF_ITEMUNIT, item.getItem().getItemUnit());
    	                    map.put(DataNameTokens.INV_POCFF_ITEMLENGTH, String.valueOf(item.getItem().getLength()));
    	                    map.put(DataNameTokens.INV_POCFF_ITEMWIDTH, String.valueOf(item.getItem().getWidth()));
    	                    map.put(DataNameTokens.INV_POCFF_ITEMHEIGHT, String.valueOf(item.getItem().getHeight()));
    	                    map.put(DataNameTokens.INV_POCFF_VOLUME, String.valueOf(item.getItem().getLength()*item.getItem().getWidth()*item.getItem().getHeight()));
    	                    map.put(DataNameTokens.INV_POCFF_ITEMWEIGHT, String.valueOf(item.getItem().getWeight()));
    	                    map.put(DataNameTokens.INV_POCFF_QTYGRN, Integer.toString(asnItem.getGrnQuantity()));
    	                    map.put(DataNameTokens.INV_POCFF_ITEMID, item.getItem().getId().toString());
    	                    
    	                    dataList.add(map);
                    	}else{
                    		_log.error("CFF item not found");
                    	} 
                	}
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
