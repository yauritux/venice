package com.gdn.venice.inbound.services;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrderItemContactDetail;
import java.util.List;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderItemContactDetailService {
	
	public VenOrderItemContactDetail persist(
			VenOrderItemContactDetail venOrderItemContactDetail) throws VeniceInternalException;
	public List<VenOrderItemContactDetail> persist(
			List<VenOrderItemContactDetail> venOrderItemContactDetails) throws VeniceInternalException;
}
