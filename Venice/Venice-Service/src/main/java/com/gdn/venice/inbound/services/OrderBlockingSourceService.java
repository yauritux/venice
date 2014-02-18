package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrderBlockingSource;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderBlockingSourceService {
	
	public List<VenOrderBlockingSource> synchronizeVenOrderBlockingSourceReferences
	    (List<VenOrderBlockingSource> orderBlockingSourceReferences)
	    throws VeniceInternalException;
}
