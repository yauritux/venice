package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrderStatus;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderStatusService {
	
	public List<VenOrderStatus> synchronizeVenOrderStatusReferences
	   (List<VenOrderStatus> orderStatusReferences)
	   throws VeniceInternalException;
}
