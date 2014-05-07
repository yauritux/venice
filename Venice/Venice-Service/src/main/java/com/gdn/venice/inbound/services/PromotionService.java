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
	
	public VenPromotion synchronizeVenPromotionReferences(
			VenPromotion venPromotion) throws VeniceInternalException;
	public List<VenPromotion> findByPromotionAndMargin(VenPromotion venPromotion)
			throws VeniceInternalException;
}
