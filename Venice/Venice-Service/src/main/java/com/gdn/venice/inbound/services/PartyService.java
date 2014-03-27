package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenParty;

/**
 * 
 * @author yauritux
 *
 */
public interface PartyService {

	public List<VenParty> findByLegalName(String legalName);
	public VenParty persistParty(VenParty venParty, String type) 
			throws VeniceInternalException;
	public VenParty retrieveExistingParty(String name, boolean findByCustomer)
	        throws VeniceInternalException;	
	public VenParty synchronizeVenPartyReferenceData(
			VenParty venParty) throws VeniceInternalException;
	public List<VenParty> synchronizeVenPartyReferenceData(
			List<VenParty> venParties) throws VeniceInternalException;
}
