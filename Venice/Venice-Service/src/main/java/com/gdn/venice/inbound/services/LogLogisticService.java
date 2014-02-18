package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;

/**
 * 
 * @author yauritux
 *
 */
public interface LogLogisticService {
	
	public List<com.gdn.venice.persistence.LogLogisticService> synchronizeLogLogisticServiceReferences(
			List<com.gdn.venice.persistence.LogLogisticService> logLogisticServiceRefs) throws VeniceInternalException;
}
