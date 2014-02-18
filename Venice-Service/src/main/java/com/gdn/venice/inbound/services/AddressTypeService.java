package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.persistence.VenAddressType;

/**
 * 
 * @author yauritux
 *
 */
public interface AddressTypeService {
	
	public List<VenAddressType> synchronizeVenAddressTypeReferences
	    (List<VenAddressType> addressTypeReferences);
}
