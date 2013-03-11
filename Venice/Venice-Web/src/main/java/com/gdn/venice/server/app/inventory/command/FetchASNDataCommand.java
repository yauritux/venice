package com.gdn.venice.server.app.inventory.command;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.AdvanceShipNotice;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentFinalForm;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrder;
import com.gdn.inventory.exchange.type.ASNReferenceType;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.app.inventory.service.ASNManagementService;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 *
 * @author Roland
 */
public class FetchASNDataCommand implements RafDsCommand {

    private RafDsRequest request;
    ASNManagementService asnService;
    protected static Logger _log = null;
    
    public FetchASNDataCommand(RafDsRequest request) {
        this.request = request;
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.command.FetchASNDataCommand");
    }

    @Override
    public RafDsResponse execute() {
        RafDsResponse rafDsResponse = new RafDsResponse();
        List<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();
        _log.info("FetchASNDataCommand");
        try {
        		asnService = new ASNManagementService();
                InventoryPagingWrapper<AdvanceShipNotice> asnWrapper = asnService.getASNDataList(request);
                if(asnWrapper != null){
	                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	 		        
	                for(AdvanceShipNotice asn : asnWrapper.getContent()){
	                    HashMap<String, String> map = new HashMap<String, String>();
	                    map.put(DataNameTokens.INV_ASN_ID, asn.getId().toString());
	                    map.put(DataNameTokens.INV_ASN_NUMBER, asn.getAsnNumber());
	                    map.put(DataNameTokens.INV_ASN_REFF_NUMBER, asn.getReferenceNumber());
	                    
	                    _log.debug("reff type: "+asn.getReferenceType().name());
	                    String supplierCode="", supplierName="";
	                    if(asn.getReferenceType().name().equals(ASNReferenceType.PURCHASE_ORDER.name())){
	                    	InventoryPagingWrapper<PurchaseOrder> poWrapper = asnService.getPOData(request, asn.getReferenceNumber().toString());
	                    	if(poWrapper!=null){
	                    		PurchaseOrder po = poWrapper.getContent().get(0);
	                    		supplierCode = po.getSupplier().getCode();
	                    		supplierName = po.getSupplier().getName();
	                    	}else{
	                    		_log.error("PO not found");
	                    	}                    	
	                    }else if(asn.getReferenceType().name().equals(ASNReferenceType.CONSIGNMENT_FINAL.name())){
	                    	InventoryPagingWrapper<ConsignmentFinalForm> cffWrapper = asnService.getCFFData(request, asn.getReferenceNumber());
	                    	if(cffWrapper!=null){
	                    		ConsignmentFinalForm cff = cffWrapper.getContent().get(0);
	                    		supplierCode = cff.getConsignmentApprovalForm().getSupplier().getCode();
	                    		supplierName = cff.getConsignmentApprovalForm().getSupplier().getName();
	                    	}else{
	                    		_log.error("CFF not found");
	                    	}
	                   }
	                    
		                map.put(DataNameTokens.INV_ASN_CREATED_DATE, sdf.format(asn.getCreatedDate()));
		                map.put(DataNameTokens.INV_ASN_EST_DATE, sdf.format(asn.getEstimatedDate()));
		                map.put(DataNameTokens.INV_ASN_REFF_DATE, sdf.format(asn.getReferenceDate()));
		                map.put(DataNameTokens.INV_ASN_INVENTORY_TYPE, asn.getReferenceType().toString());
		                map.put(DataNameTokens.INV_ASN_SUPPLIER_CODE, supplierCode);
		                map.put(DataNameTokens.INV_ASN_SUPPLIER_NAME, supplierName);
		                map.put(DataNameTokens.INV_ASN_DESTINATION, asn.getDestinationWarehouse().getName());
		                map.put(DataNameTokens.INV_ASN_STATUS, asn.getCurrentStatus().toString());
		                map.put(DataNameTokens.INV_ASN_SPECIAL_NOTES, asn.getSpecialNotes());
	                    
	                    dataList.add(map);
	                }
	
	                rafDsResponse.setStatus(0);
	                rafDsResponse.setStartRow(request.getStartRow());
	                rafDsResponse.setTotalRows((int) asnWrapper.getTotalElements());
	                rafDsResponse.setEndRow(request.getStartRow() + dataList.size());
            }
        } catch (Throwable e) {
            e.printStackTrace();
            rafDsResponse.setStatus(-1);
        }

        rafDsResponse.setData(dataList);
        return rafDsResponse;
    }
}
