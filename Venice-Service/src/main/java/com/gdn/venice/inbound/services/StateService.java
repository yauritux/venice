package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.persistence.VenState;

/**
 * 
 * @author yauritux
 *
 */
public interface StateService {
	
	public List<VenState> synchronizeVenStateReferences(
			List<VenState> stateReferences);
}
