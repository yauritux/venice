package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.Attribute;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentApprovalItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentFinalItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrderItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseRequisitionItem;
import com.gdn.inventory.exchange.type.ASNReferenceType;
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
public class FetchItemAttributeDataCommand implements RafDsCommand {

    private RafDsRequest request;
    GRNManagementService grnService;
    ASNManagementService asnService;
    protected static Logger _log = null;
    
    public FetchItemAttributeDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchItemAttributeDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	_log.info("FetchItemAttributeDataCommand");
    	System.out.println("FetchItemAttributeDataCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
        	grnService = new GRNManagementService();
        	asnService = new ASNManagementService();
        	
        	ResultWrapper<AdvanceShipNoticeItem> asnItemWrapper = asnService.getSingleASNItemData(request.getParams().get(DataNameTokens.INV_ASN_ITEM_ID));
        	Long itemId = null;
        	
        	if(asnItemWrapper!=null){
        		System.out.println("asnItemWrapper found");
        		AdvanceShipNoticeItem asnItem = asnItemWrapper.getContent();       		
		  
            	if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.PURCHASE_ORDER.name())){
            		_log.info("reff type: purchase order");                		                		
            		ResultWrapper<PurchaseOrderItem> poItemWrapper = asnService.getPOItemData(request, asnItem.getReferenceNumber().toString());
                	if(poItemWrapper!=null){
                		PurchaseRequisitionItem item = poItemWrapper.getContent().getPurchaseRequisitionItem();
                		_log.debug("PO item found, id:"+item.getItem().getId());    
                		itemId=item.getItem().getId();
                	}else{
                		_log.error("PO item not found");
                	}   
            	}else if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.CONSIGNMENT_FINAL.name())){
            		_log.info("reff type: consignment");
            		ResultWrapper<ConsignmentFinalItem> cffItemWrapper = asnService.getCFFItemData(request, asnItem.getReferenceNumber().toString());
                	if(cffItemWrapper!=null){
                		ConsignmentApprovalItem item = cffItemWrapper.getContent().getConsignmentApprovalItem();
                		_log.debug("CFF item found, id:"+item.getItem().getId());    
                		itemId=item.getItem().getId();
                	}else{
                		_log.error("CFF item not found");
                	} 
            	}
            	
            	if(itemId!=null){
            		_log.debug("item found, id:"+itemId); 
            		System.out.println("item found, id:"+itemId);
            		List<WarehouseItem> whItemList = grnService.getWarehouseItemDataList(itemId.toString());
            		for(WarehouseItem whItem : whItemList){
            			List<Attribute> attList = grnService.getAttributeDataList(whItem.getId().toString());
            			for(Attribute att : attList){
            				HashMap<String, String> map = new HashMap<String, String>();
            				_log.debug("attribute found, id:"+att.getId()); 
            				System.out.println("attribute found, id:"+att.getId());
            				map.put(DataNameTokens.INV_ITEM_ATTRIBUTE_ID, att.getId().toString());
            				
            				if(att.getName().equals(DataNameTokens.INV_ITEM_ATTRIBUTE_IMEI)){
            					map.put(DataNameTokens.INV_ITEM_ATTRIBUTE_IMEI, att.getValue());
            				}else if(att.getName().equals(DataNameTokens.INV_ITEM_ATTRIBUTE_SERIALNUMBER)){
            					map.put(DataNameTokens.INV_ITEM_ATTRIBUTE_SERIALNUMBER, att.getValue());
            				}else if(att.getName().equals(DataNameTokens.INV_ITEM_ATTRIBUTE_EXPIREDDATE)){
            					map.put(DataNameTokens.INV_ITEM_ATTRIBUTE_EXPIREDDATE, att.getValue());
            				}
    	                    
    	                    dataList.add(map);
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
