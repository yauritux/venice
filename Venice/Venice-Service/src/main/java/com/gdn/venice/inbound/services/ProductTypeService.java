package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenProductType;

/**
 * 
 * @author yauritux
 *
 */
public interface ProductTypeService {
	
	public List<VenProductType> synchronizeVenProductTypeReferences(
			List<VenProductType> productTypeRefs) throws VeniceInternalException;
}
