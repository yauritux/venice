package com.gdn.venice.inbound.services;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrderAddress;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderAddressService {
	
	public VenOrderAddress persist(VenOrderAddress venOrderAddress) 
	   throws VeniceInternalException;
	
	public List<VenOrderAddress> findByVenOrderWcsOrderId(String wcsOrderId);
}
