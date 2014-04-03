package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenMerchant;
import com.gdn.venice.persistence.VenOrderItem;

/**
 * 
 * @author yauritux
 *
 */
public interface MerchantService {

	public Boolean processMerchant(List<String> merchantProducts, List<VenOrderItem> orderItems);
	public List<VenMerchant> findByWcsMerchantId(String wcsMerchantId);
	public VenMerchant synchronizeVenMerchantData(VenMerchant venMerchant)
	        throws VeniceInternalException;
	public List<VenMerchant> synchronizeVenMerchantReferences(
			List<VenMerchant> merchantRefs) throws VeniceInternalException;
	public VenMerchant persist(VenMerchant venMerchant)
			throws VeniceInternalException;	
}
