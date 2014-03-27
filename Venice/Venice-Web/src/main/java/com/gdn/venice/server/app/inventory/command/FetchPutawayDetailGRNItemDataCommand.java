package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentApprovalItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentFinalItem;
import com.gdn.inventory.exchange.entity.module.inbound.GoodReceivedNoteItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrderItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseRequisitionItem;
import com.gdn.inventory.exchange.type.ASNReferenceType;
import com.gdn.inventory.exchange.type.ConsignmentType;
import com.gdn.inventory.exchange.type.StockType;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ASNManagementService;
import com.gdn.venice.server.app.inventory.service.GRNManagementService;
import com.gdn.venice.server.app.inventory.service.PutawayManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchPutawayDetailGRNItemDataCommand implements RafDsCommand {

    private RafDsRequest request;
    GRNManagementService grnService;
    ASNManagementService asnService;
    PutawayManagementService putawayService;
    protected static Logger _log = null;
    
    public FetchPutawayDetailGRNItemDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchPutawayDetailGRNItemDataCommand");
    }

    @Override
    public RafDsResponse execute() {
    	_log.info("FetchPutawayDetailGRNItemDataCommand");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        try {
        	putawayService = new PutawayManagementService();
        	
        	InventoryPagingWrapper<GoodReceivedNoteItem> grnItemWrapper = null;
        	if(request.getParams().get(DataNameTokens.INV_WAREHOUSE_ID)!=null){
        		System.out.println("fetch grn item for add putaway");
        		grnItemWrapper = putawayService.getGRNItemDataListByWarehouseId(request.getParams().get(DataNameTokens.INV_WAREHOUSE_ID));
        	}else if(request.getParams().get(DataNameTokens.INV_PUTAWAY_GRN_ID)!=null){
        		System.out.println("fetch grn item for input putaway location");
        		grnItemWrapper = putawayService.getGRNItemDataListByGrnId(request.getParams().get(DataNameTokens.INV_PUTAWAY_GRN_ID));
        	}
        	if(grnItemWrapper.isSuccess()){                 	
	    		asnService = new ASNManagementService(); 
            	for(GoodReceivedNoteItem grnItem : grnItemWrapper.getContent()){  
            		
    				HashMap<String, String> map = new HashMap<String, String>(); 
    				map.put(DataNameTokens.INV_PUTAWAY_GRN_ITEMID, String.valueOf(grnItem.getId()));
    				map.put(DataNameTokens.INV_PUTAWAY_GRN_GRNNUMBER, String.valueOf(grnItem.getGoodReceivedNote().getGrnNumber()));
    				
	            	AdvanceShipNoticeItem asnItem = grnItem.getAdvanceShipNoticeItem();
	            	if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.PURCHASE_ORDER.name())){
                		_log.info("reff type: purchase order");                		                		
                		ResultWrapper<PurchaseOrderItem> poItemWrapper = asnService.getPOItemData(request, asnItem.getReferenceNumber().toString());
                    	if(poItemWrapper.isSuccess()){
                    		PurchaseRequisitionItem prItem = poItemWrapper.getContent().getPurchaseRequisitionItem();
                    		_log.debug("PO item found, code:"+prItem.getItem().getCode());    
                    		                   				             
    	                    map.put(DataNameTokens.INV_PUTAWAY_GRN_ITEMCODE, prItem.getItem().getCode());
    	                    map.put(DataNameTokens.INV_PUTAWAY_GRN_ITEMDESC, prItem.getItem().getDescription());   
    	                    
    	                    System.out.println("item id "+prItem.getItem().getId());
    	                    System.out.println("destination: "+asnItem.getAdvanceShipNotice().getDestinationWarehouse().getId());
    	                    System.out.println("supplier: "+poItemWrapper.getContent().getPurchaseOrder().getSupplier().getId());
    	                    WarehouseItem whItem = putawayService.getWarehouseItemData(prItem.getItem().getId(), 
    	                    		asnItem.getAdvanceShipNotice().getDestinationWarehouse().getId(), 
    	                    		poItemWrapper.getContent().getPurchaseOrder().getSupplier().getId(), StockType.TRADING);
    	                    
    	                    String shelfCode="";
    	                    int qty=0;
    	                    if(whItem!=null){
    	                    	List<WarehouseItemStorageStock> storageStockList = putawayService.getWarehouseItemStorageList(whItem.getId());    	                    	
    	                    	for(WarehouseItemStorageStock storageStock : storageStockList){
    	                    		shelfCode+=storageStock.getStorage().getCode()+" / "+storageStock.getStorage().getShelf().getCode()+",";
    	                    		qty+=storageStock.getQuantity();
    	                    	}
    	                    	if(shelfCode.length()>1) shelfCode=shelfCode.substring(0, shelfCode.lastIndexOf(","));
    	                    }
    	                    map.put(DataNameTokens.INV_PUTAWAY_GRN_WAREHOUSEITEMID, whItem.getId().toString());
    	                    map.put(DataNameTokens.INV_PUTAWAY_GRN_SHELFCODE, shelfCode);
    	                    map.put(DataNameTokens.INV_PUTAWAY_GRN_QTY, String.valueOf(qty));
   	                    
    	                    dataList.add(map);
                    	}else{
                    		_log.error("PO item not found");
                    	}   
                	}else if(asnItem.getAdvanceShipNotice().getReferenceType().name().equals(ASNReferenceType.CONSIGNMENT_FINAL.name())){
                		_log.info("reff type: consignment");
                		ResultWrapper<ConsignmentFinalItem> cffItemWrapper = asnService.getCFFItemData(request, asnItem.getReferenceNumber());
                    	if(cffItemWrapper.isSuccess()){
                    		ConsignmentApprovalItem cafItem = cffItemWrapper.getContent().getConsignmentApprovalItem();
                    		_log.debug("CFF item found, code:"+cafItem.getItem().getCode());    
                    		                   				             
    	                    map.put(DataNameTokens.INV_PUTAWAY_GRN_ITEMCODE, cafItem.getItem().getCode());
    	                    map.put(DataNameTokens.INV_PUTAWAY_GRN_ITEMDESC, cafItem.getItem().getDescription());
    	                        
    	                    StockType st = null;
    	                    if(cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getConsignmentType().name().equals(ConsignmentType.COMMISSION.name())){
    	                    	st = StockType.CONSIGMENT_COMMISION;
    	                    }else if(cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getConsignmentType().name().equals(ConsignmentType.TRADING.name())){
    	                    	st = StockType.CONSIGNMENT_TRADING;
    	                    }
    	                    
    	                    System.out.println("item id "+cafItem.getItem().getId());
    	                    System.out.println("destination: "+asnItem.getAdvanceShipNotice().getDestinationWarehouse().getId());
    	                    System.out.println("supplier: "+cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getSupplier().getId());
    	                    WarehouseItem whItem = putawayService.getWarehouseItemData(cafItem.getItem().getId(), 
    	                    		asnItem.getAdvanceShipNotice().getDestinationWarehouse().getId(), 
    	                    		cffItemWrapper.getContent().getConsignmentFinalForm().getConsignmentApprovalForm().getSupplier().getId(), st);
    	                    
    	                    String shelfCode="";
    	                    int qty=0;
    	                    if(whItem!=null){
    	                    	List<WarehouseItemStorageStock> storageStockList = putawayService.getWarehouseItemStorageList(whItem.getId());    	                    	
    	                    	for(WarehouseItemStorageStock storageStock : storageStockList){
    	                    		shelfCode+=storageStock.getStorage().getCode()+" / "+storageStock.getStorage().getShelf().getCode()+",";
    	                    		qty+=storageStock.getQuantity();
    	                    	}
    	                    	if(shelfCode.length()>1) shelfCode=shelfCode.substring(0, shelfCode.lastIndexOf(","));
    	                    }
    	                    map.put(DataNameTokens.INV_PUTAWAY_GRN_WAREHOUSEITEMID, whItem.getId().toString());
    	                    map.put(DataNameTokens.INV_PUTAWAY_GRN_SHELFCODE, shelfCode);
    	                    map.put(DataNameTokens.INV_PUTAWAY_GRN_QTY, String.valueOf(qty));
    	                        	                    
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