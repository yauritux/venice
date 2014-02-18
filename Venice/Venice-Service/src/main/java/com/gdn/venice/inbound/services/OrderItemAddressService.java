package com.gdn.venice.inbound.services;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrderItemAddress;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderItemAddressService {

	public VenOrderItemAddress persist(VenOrderItemAddress venOrderItemAddress)
	    throws VeniceInternalException;
}
