package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentApprovalItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentFinalItem;
import com.gdn.inventory.exchange.entity.module.inbound.GoodReceivedNoteItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrderItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseRequisitionItem;
import com.gdn.inventory.exchange.type.ASNReferenceType;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ASNManagementService;
import com.gdn.venice.server.app.inventory.service.GRNManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchGRNItemDataCommand implements RafDsCommand {

    private RafDsRequest request;
    GRNManagementService grnService;
    ASNManagementService asnService;
    protected static Logger _log = null;
    
    public FetchGRNItemDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchGRNItemDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	_log.info("FetchGRNItemDataCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
        	grnService = new GRNManagementService();
        	InventoryPagingWrapper<GoodReceivedNoteItem> grnItemWrapper = grnService.getGRNItemDataList(request, request.getParams().get(DataNameTokens.INV_GRN_ID));
        	if(grnItemWrapper != null){     
            	
	    		asnService = new ASNManagementService(); 
            	for(GoodReceivedNoteItem grnItem : grnItemWrapper.getContent()){  
            		
    				HashMap<String, String> map = new HashMap<String, String>(); 
    				map.put(DataNameTokens.INV_GRN_ITEM_ID, String.valueOf(grnItem.getId()));
    				
	            	AdvanceShipNoticeItem asnItem = grnItem.getAdvanceShipNoticeItem();
	            	if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.PURCHASE_ORDER.name())){
                		_log.info("reff type: purchase order");                		                		
                		ResultWrapper<PurchaseOrderItem> poItemWrapper = asnService.getPOItemData(request, asnItem.getReferenceNumber().toString());
                    	if(poItemWrapper!=null){
                    		PurchaseRequisitionItem item = poItemWrapper.getContent().getPurchaseRequisitionItem();
                    		_log.debug("PO item found, code:"+item.getItem().getCode());    
                    		                   				             
    	                    map.put(DataNameTokens.INV_POCFF_ITEMCODE, item.getItem().getCode());
    	                    map.put(DataNameTokens.INV_POCFF_ITEMDESC, item.getItem().getDescription());
    	                    map.put(DataNameTokens.INV_POCFF_QTY, Integer.toString(poItemWrapper.getContent().getQuantity()));
    	                    map.put(DataNameTokens.INV_POCFF_ITEMUNIT, item.getItem().getItemUnit());
    	                        	                    
    	                    dataList.add(map);
                    	}else{
                    		_log.error("PO item not found");
                    	}   
                	}else if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.CONSIGNMENT_FINAL.name())){
                		_log.info("reff type: consignment");
                		ResultWrapper<ConsignmentFinalItem> cffItemWrapper = asnService.getCFFItemData(request, asnItem.getReferenceNumber().toString());
                    	if(cffItemWrapper!=null){
                    		ConsignmentApprovalItem item = cffItemWrapper.getContent().getConsignmentApprovalItem();
                    		_log.debug("CFF item found, code:"+item.getItem().getCode());    
                    		                   				             
    	                    map.put(DataNameTokens.INV_POCFF_ITEMCODE, item.getItem().getCode());
    	                    map.put(DataNameTokens.INV_POCFF_ITEMDESC, item.getItem().getDescription());
    	                    map.put(DataNameTokens.INV_POCFF_QTY, Integer.toString(cffItemWrapper.getContent().getQuantity()));
    	                    map.put(DataNameTokens.INV_POCFF_ITEMUNIT, item.getItem().getItemUnit());
    	                        	                    
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
