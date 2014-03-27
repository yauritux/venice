package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public class AddItemAttributeDataCommand implements RafDsCommand {

    private RafDsRequest request;
    GRNManagementService grnService;
    ASNManagementService asnService;
    protected static Logger _log = null;
    
    public AddItemAttributeDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.AddItemAttributeDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	_log.info("AddItemAttributeDataCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
        	grnService = new GRNManagementService();
        	asnService = new ASNManagementService();
        	
        	String itemIdParam = null;
        	if(request.getParams().get(DataNameTokens.INV_ASN_ITEM_ID)!=null){
        		itemIdParam = request.getParams().get(DataNameTokens.INV_ASN_ITEM_ID);
        		_log.info("item id from asn item: "+itemIdParam);
        	}
        	
        	ResultWrapper<AdvanceShipNoticeItem> asnItemWrapper = asnService.getSingleASNItemData(itemIdParam);
        	Long itemId = null;
        	
        	if(asnItemWrapper.isSuccess()){
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
            		ResultWrapper<ConsignmentFinalItem> cffItemWrapper = asnService.getCFFItemData(request, asnItem.getReferenceNumber());
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
            		List<WarehouseItem> whItemList = grnService.getWarehouseItemDataList(itemId.toString());
            		if(whItemList!=null){ 
            			dataList=request.getData();
            			for(int i=0;i< dataList.size();i++){
            				Map<String, String> data = dataList.get(i);
            				
            				Iterator<String> iter=data.keySet().iterator();
            				
        					Attribute attribute = new Attribute();
        					WarehouseItem whItem = new WarehouseItem();
        					whItem.setId(whItemList.get(0).getId());
        					attribute.setWarehouseItem(whItem);
        					
            				while(iter.hasNext()){
            					String key=iter.next();
            					if(key.equals(DataNameTokens.INV_ITEM_ATTRIBUTE_NAME)){
            						attribute.setName(data.get(key));
            					}else if(key.equals(DataNameTokens.INV_ITEM_ATTRIBUTE_VALUE)){
            						attribute.setValue(data.get(key));
            					}
            				}
            				
            				grnService.addEditItemAttribute(request.getParams().get("username"), attribute);
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
