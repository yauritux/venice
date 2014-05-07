package com.gdn.venice.inbound.services;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenProductType;

/**
 * 
 * @author yauritux
 *
 */
public interface ProductTypeService {
	
	public VenProductType synchronizeVenProductType(VenProductType venProductType) 
			throws VeniceInternalException;	
}
