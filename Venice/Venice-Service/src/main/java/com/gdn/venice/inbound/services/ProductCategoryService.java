package com.gdn.venice.inbound.services;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenProductCategory;

/**
 * 
 * @author yauritux
 *
 */
public interface ProductCategoryService {
	
	public VenProductCategory synchronizeVenProductCategory(VenProductCategory venProductCategory)
	    throws VeniceInternalException;
}
