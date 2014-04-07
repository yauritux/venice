package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrderContactDetail;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderContactDetailService {

	public VenOrderContactDetail persist(VenOrderContactDetail venOrderContactDetail) 
			throws VeniceInternalException;
	public List<VenOrderContactDetail> persistVenOrderContactDetails(
			List<VenOrderContactDetail> orderContactDetails) throws VeniceInternalException;
}
