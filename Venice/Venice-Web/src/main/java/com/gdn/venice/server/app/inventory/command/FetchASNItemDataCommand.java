package com.gdn.venice.server.app.inventory.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdn.inventory.exchange.entity.AdvanceShipNotice;
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.ConsignmentFinalForm;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentApprovalForm;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentApprovalItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrder;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseRequisition;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseRequisitionItem;
import com.gdn.inventory.exchange.type.ASNReferenceType;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultListWrapper;
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
    
    public FetchASNItemDataCommand(RafDsRequest request) {
        this.request = request;
    }

    @Override
    public RafDsResponse execute() {
    	System.out.println("fetch asn item command");
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        System.out.println("asn id: "+request.getParams().get(DataNameTokens.INV_ASN_ID));
        try {
        	System.out.println("get asn data command");
        		asnService = new ASNManagementService();        		
        		ResultWrapper<AdvanceShipNotice> asnWrapper = asnService.getASNData(request, request.getParams().get(DataNameTokens.INV_ASN_ID));
                if(asnWrapper != null){     
                	System.out.println("asn data found");
                	
                	String asnItemQuantity = "";
                	String asnItemQtyGrn = "";
//                	InventoryPagingWrapper<AdvanceShipNoticeItem> asnItemWrapper = asnService.getASNItemData(request, request.getParams().get(DataNameTokens.INV_ASN_ID));           	
//					if(asnItemWrapper != null){
//					  for(AdvanceShipNoticeItem item : asnItemWrapper.getContent()){
//					      HashMap<String, String> map = new HashMap<String, String>();
//					      asnItemQuantity = Integer.toString(item.getQuantity());
//					  }
//					}
                	
                	AdvanceShipNotice asn = asnWrapper.getContent();
                	if(asn.getReferenceType().name().equals(ASNReferenceType.PURCHASE_ORDER.name())){
                		System.out.println("purchase order");
                		InventoryPagingWrapper<PurchaseOrder> poWrapper = asnService.getPOData(request, asn.getReferenceNumber());
                    	if(poWrapper!=null){
                    		System.out.println("po found");
                    		PurchaseRequisition pr = poWrapper.getContent().get(0).getPurchaseRequisition();
                    		ResultListWrapper<PurchaseRequisitionItem> prItemWrapper = asnService.getPRItemData(request, pr.getId());
                    		if(prItemWrapper!=null){
                    			System.out.println("pr item found");
                    			for(PurchaseRequisitionItem item : prItemWrapper.getContents()){   
                    				System.out.println("item code: "+item.getItem().getCode());
                    				HashMap<String, String> map = new HashMap<String, String>();                    				             
            	                    map.put(DataNameTokens.INV_POCFF_ITEMCODE, item.getItem().getCode());
            	                    map.put(DataNameTokens.INV_POCFF_ITEMDESC, item.getItem().getDescription());
            	                    map.put(DataNameTokens.INV_POCFF_QTY, asnItemQuantity);
            	                    map.put(DataNameTokens.INV_POCFF_ITEMUNIT, item.getItem().getItemUnit());
            	                    map.put(DataNameTokens.INV_POCFF_ITEMLENGTH, String.valueOf(item.getItem().getLength()));
            	                    map.put(DataNameTokens.INV_POCFF_ITEMWIDTH, String.valueOf(item.getItem().getWidth()));
            	                    map.put(DataNameTokens.INV_POCFF_ITEMHEIGHT, String.valueOf(item.getItem().getHeight()));
            	                    map.put(DataNameTokens.INV_POCFF_VOLUME, String.valueOf(item.getItem().getLength()*item.getItem().getWidth()
            	                    		*item.getItem().getHeight()));
            	                    map.put(DataNameTokens.INV_POCFF_ITEMWEIGHT, String.valueOf(item.getItem().getWeight()));
            	                    map.put(DataNameTokens.INV_POCFF_QTYGRN, String.valueOf(asnItemQtyGrn));
            	                    
            	                    dataList.add(map);
            	                    System.out.println("dataList size: "+dataList.size());
                    			}                    			
                    		}else{
                    			System.out.println("PR item not found");
                    		}
                    	}else{
                    		System.out.println("PO not found");
                    	}   
                	}else if(asn.getReferenceType().name().equals(ASNReferenceType.CONSIGNMENT_FINAL.name())){
                		System.out.println("consignment");
                		InventoryPagingWrapper<ConsignmentFinalForm> cffWrapper = asnService.getCFFData(request, asn.getReferenceNumber());
                    	if(cffWrapper!=null){
                    		System.out.println("cff found");
                    		ConsignmentApprovalForm caf = cffWrapper.getContent().get(0).getConsignmentApprovalForm();
                    		ResultListWrapper<ConsignmentApprovalItem> cafItemWrapper = asnService.getCAFItemData(request, caf.getId());
                    		if(cafItemWrapper!=null){
                    			System.out.println("caf item found");
                    			for(ConsignmentApprovalItem item : cafItemWrapper.getContents()){                    				                    				
                    				HashMap<String, String> map = new HashMap<String, String>();                    				             
            	                    map.put(DataNameTokens.INV_POCFF_ITEMCODE, item.getItem().getCode());
            	                    map.put(DataNameTokens.INV_POCFF_ITEMDESC, item.getItem().getDescription());
            	                    map.put(DataNameTokens.INV_POCFF_QTY, asnItemQuantity);
            	                    map.put(DataNameTokens.INV_POCFF_ITEMUNIT, item.getItem().getItemUnit());
            	                    map.put(DataNameTokens.INV_POCFF_ITEMLENGTH, String.valueOf(item.getItem().getLength()));
            	                    map.put(DataNameTokens.INV_POCFF_ITEMWIDTH, String.valueOf(item.getItem().getWidth()));
            	                    map.put(DataNameTokens.INV_POCFF_ITEMHEIGHT, String.valueOf(item.getItem().getHeight()));
            	                    map.put(DataNameTokens.INV_POCFF_VOLUME, String.valueOf(item.getItem().getLength()*item.getItem().getWidth()
            	                    		*item.getItem().getHeight()));
            	                    map.put(DataNameTokens.INV_POCFF_ITEMWEIGHT, String.valueOf(item.getItem().getWeight()));
            	                    map.put(DataNameTokens.INV_POCFF_QTYGRN, String.valueOf(asnItemQtyGrn));
            	                    
            	                    dataList.add(map);
                    			}                    			
                    		}else{
                    			System.out.println("CAF item not found");
                    		}
                    	}else{
                    		System.out.println("CFF not found");
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
