package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.Attribute;
import com.gdn.inventory.exchange.entity.Item;
import com.gdn.inventory.exchange.entity.Supplier;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentFinalItem;
import com.gdn.inventory.exchange.entity.module.inbound.GoodReceivedNoteItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrderItem;
import com.gdn.inventory.exchange.type.ASNReferenceType;
import com.gdn.inventory.exchange.type.ConsignmentType;
import com.gdn.inventory.exchange.type.StockType;
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
    String grnItemId;
    
    public FetchItemAttributeDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchItemAttributeDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	System.out.println("FetchItemAttributeDataCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
        	grnService = new GRNManagementService();
        	asnService = new ASNManagementService();

        	WarehouseItem whi = new WarehouseItem();
        	
        	String itemIdParam = null;
        	if(request.getParams().get(DataNameTokens.INV_GRN_ITEM_ID)!=null){    
        		System.out.println("item id from grn item");
        		ResultWrapper<GoodReceivedNoteItem> grnItemWrapper = grnService.findItemByGRNItemId(request.getParams().get(DataNameTokens.INV_GRN_ITEM_ID));
        		
        		if(grnItemWrapper!=null && grnItemWrapper.isSuccess()){
        			itemIdParam = grnItemWrapper.getContent().getAdvanceShipNoticeItem().getId().toString();
                    whi.setWarehouse(grnItemWrapper.getContent().getGoodReceivedNote().getReceivedWarehouse());
        		}
        	}
        	
        	ResultWrapper<AdvanceShipNoticeItem> asnItemWrapper = asnService.getSingleASNItemData(itemIdParam);
        	String itemId = request.getParams().get(DataNameTokens.INV_POCFF_ITEMID);
        	System.out.println("itemId: "+itemId);
        	if(asnItemWrapper!=null && asnItemWrapper.isSuccess()){
        		AdvanceShipNoticeItem asnItem = asnItemWrapper.getContent();       		
		  
        		Item item = new Item();        
            	item=grnService.findItemByItemId(itemId);
            	System.out.println("item found");
            	
            	whi.setItem(item);
            	
            	Supplier supplier = new Supplier();
            	if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.PURCHASE_ORDER.name())){
            		System.out.println("reff type: purchase order");                		                		
            		ResultWrapper<PurchaseOrderItem> poItemWrapper = asnService.getPOItemData(request, asnItem.getReferenceNumber().toString());
                	if(poItemWrapper!=null && poItemWrapper.isSuccess()){
                		supplier = poItemWrapper.getContent().getPurchaseOrder().getSupplier();
                	}else{
                		System.out.println("Supplier PO not found");
                	}   

                    whi.setStockType(StockType.TRADING);
            	}else if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.CONSIGNMENT_FINAL.name())){
            		System.out.println("reff type: consignment");
            		ResultWrapper<ConsignmentFinalItem> cffItemWrapper = asnService.getCFFItemData(request, asnItem.getReferenceNumber());
                	if(cffItemWrapper!=null && cffItemWrapper.isSuccess()){
                		supplier = cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getSupplier();   
                	}else{
                		System.out.println("Supplier CFF not found");
                	} 
                	
               		if(cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getConsignmentType().name().equals(ConsignmentType.COMMISSION.name())){
                       	whi.setStockType(StockType.CONSIGNMENT_COMMISION);
               		}else if(cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getConsignmentType().name().equals(ConsignmentType.TRADING.name())){
                       	whi.setStockType(StockType.CONSIGNMENT_TRADING);
               		}
            	}            	                

                whi.setSupplier(supplier);
                
        		WarehouseItem whItem = grnService.findWarehouseItem(itemId, whi.getWarehouse().getId().toString(), whi.getSupplier().getId().toString(), whi.getStockType());        		
        		
        		if(whItem!=null){
        			System.out.println("warehouseItem found");
        			List<Attribute> attributeList = grnService.getAttributeDataListByWarehouseItem(whItem.getId().toString());
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
