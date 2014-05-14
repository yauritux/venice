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

	public VenOrderPaymentAllocation persist(VenOrderPaymentAllocation venOrderPaymentAllocation) 
	  throws VeniceInternalException;
	public List<VenOrderPaymentAllocation> persistOrderPaymentAllocationList(
			List<VenOrderPaymentAllocation> venOrderPaymentAllocationList) throws VeniceInternalException;	
	public Boolean removeOrderPaymentAllocationList(VenOrder venOrder);	
	public List<VenOrderPaymentAllocation> findByFraudSuspicionCaseId(Long fraudSuspicionCaseId)
			  throws VeniceInternalException;
	public List<VenOrderPaymentAllocation> findByVenOrderId(Long orderId) 
	          throws VeniceInternalException;
}
