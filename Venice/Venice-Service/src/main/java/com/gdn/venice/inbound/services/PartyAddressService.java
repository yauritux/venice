package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPartyAddress;

/**
 * 
 * @author yauritux
 *
 */
public interface PartyAddressService {
	
	public List<VenPartyAddress> findByVenParty(VenParty party);
	
	public List<VenPartyAddress> updatePartyAddressList(List<VenPartyAddress> existingPartyAddresses
			, List<VenPartyAddress> newPartyAddresses) throws VeniceInternalException;
	
	public List<VenPartyAddress> persistPartyAddresses(
			List<VenPartyAddress> partyAddresses) throws VeniceInternalException;
}
