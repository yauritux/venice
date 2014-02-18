package com.gdn.venice.inbound.services;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderStatus;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderStatusHistoryService {
	
	public Boolean createOrderStatusHistory(VenOrder venOrder, VenOrderStatus venOrderStatus) 
			  throws VeniceInternalException;	
}
