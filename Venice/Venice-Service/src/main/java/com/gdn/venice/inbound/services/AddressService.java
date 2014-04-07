package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenAddress;

/**
 * 
 * @author yauritux
 *
 */
public interface AddressService {
	
	/*
	public List<VenAddress> updateAddressList(List<VenAddress> existingVenAddressList
			, List<VenAddress> newVenAddressList) throws VeniceInternalException;
	*/
	public List<VenAddress> persistAddressList(List<VenAddress> venAddressList) 
			throws VeniceInternalException;	
	public VenAddress persistAddress(VenAddress venAddress) throws VeniceInternalException;	
	public VenAddress synchronizeVenAddressReferenceData(VenAddress venAddress) 
			throws VeniceInternalException;
}
