package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;


/**
 * 
 * @author yauritux
 *
 * This interface will be used as a contract-based service for VenOrderItem.
 * 
 */
public interface OrderItemService {
	
	public boolean isItemWCSExistInDB(String wcsOrderItemId);
	public List<VenOrderItem> persistOrderItemList(
			VenOrder venOrder, List<VenOrderItem> venOrderItemList) throws VeniceInternalException;	
	public VenOrderItem synchronizeVenOrderItemReferenceData(
			VenOrderItem venOrderItem) throws VeniceInternalException;	
	public List<VenOrderItem> findByVenOrderId(Long orderId) throws VeniceInternalException;
}
