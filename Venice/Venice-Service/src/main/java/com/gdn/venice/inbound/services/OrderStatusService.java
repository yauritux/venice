package com.gdn.venice.inbound.services;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrderStatus;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderStatusService {
	
	public VenOrderStatus synchronizeVenOrderStatusReferences(VenOrderStatus venOrderStatus)
	   throws VeniceInternalException;
}
