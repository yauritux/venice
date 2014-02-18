package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenPromotion;

/**
 * 
 * @author yauritux
 *
 */
public interface PromotionService {
	
	public List<VenPromotion> synchronizeVenPromotionReferences(
			List<VenPromotion> promotionReferences) throws VeniceInternalException;
}
