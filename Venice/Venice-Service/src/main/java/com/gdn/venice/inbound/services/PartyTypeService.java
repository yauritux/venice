package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenPartyType;

/**
 * 
 * @author yauritux
 *
 */
public interface PartyTypeService {
	
	public List<VenPartyType> synchronizeVenPartyTypeReferenceData(
			List<VenPartyType> venPartyTypes) throws VeniceInternalException;
}
