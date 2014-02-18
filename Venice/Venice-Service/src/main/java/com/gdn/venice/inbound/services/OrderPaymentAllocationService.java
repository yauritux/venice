package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderPaymentAllocationService {

	public List<VenOrderPaymentAllocation> persistOrderPaymentAllocationList(
			List<VenOrderPaymentAllocation> venOrderPaymentAllocationList) throws VeniceInternalException;	
	public Boolean removeOrderPaymentAllocationList(VenOrder venOrder);	
}
