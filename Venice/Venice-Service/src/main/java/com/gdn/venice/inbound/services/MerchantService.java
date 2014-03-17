package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenMerchant;

/**
 * 
 * @author yauritux
 *
 */
public interface MerchantService {

	public List<VenMerchant> findByWcsMerchantId(String wcsMerchantId);
	public VenMerchant synchronizeVenMerchantData(VenMerchant venMerchant)
	       throws VeniceInternalException;
	public List<VenMerchant> synchronizeVenMerchantReferences(
			List<VenMerchant> merchantRefs) throws VeniceInternalException;
	public VenMerchant persist(VenMerchant venMerchant)
			throws VeniceInternalException;	
}
