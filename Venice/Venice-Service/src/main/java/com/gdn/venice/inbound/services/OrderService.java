package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.integration.jaxb.Order;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderStatus;

/**
 * 
 * @author yauritux
 *
 * This interface will be used as a contract-based service for VenOrder.
 */
public interface OrderService {
	
	public Boolean createOrder(Order order) throws VeniceInternalException;
	public VenOrder retrieveExistingOrder(String wcsOrderId);
	public boolean isOrderExist(String wcsOrderId);
	public VenOrder persistOrder(
			Boolean vaPaymentExists, Boolean csPaymentExists
			, VenOrder venOrder) throws VeniceInternalException;	
	public VenOrder synchronizeVenOrderReferenceData(VenOrder venOrder)
			throws VeniceInternalException;
	public List<VenOrder> synchronizeVenOrderReferences(List<VenOrder> orderReferences) 
	        throws VeniceInternalException;
}
