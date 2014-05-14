package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.integration.jaxb.Order;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrder;

/**
 * 
 * @author yauritux
 *
 * This interface will be used as a contract-based service for VenOrder.
 */
public interface OrderService {
	
	public Boolean createOrder(Order order) throws VeniceInternalException;
	public Boolean createOrderVAPayment(Order order) throws VeniceInternalException;
	public VenOrder retrieveExistingOrder(String wcsOrderId);
	public boolean isOrderExist(String wcsOrderId);
	public VenOrder persistOrder(
			Boolean vaPaymentExists, Boolean csPaymentExists
			, VenOrder venOrder, Order order) throws VeniceInternalException;	
	public VenOrder synchronizeVenOrderReferenceData(VenOrder venOrder)
			throws VeniceInternalException;
	public VenOrder synchronizeVenOrder(VenOrder venOrder) throws VeniceInternalException;
	public List<VenOrder> synchronizeVenOrderReferences(List<VenOrder> orderReferences) 
	        throws VeniceInternalException;
	public VenOrder findByWcsOrderId(String wcsOrderId) throws VeniceInternalException;
}
