package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenCustomer;

/**
 * 
 * @author yauritux
 *
 */
public interface CustomerService {
	
	public VenCustomer findByWcsCustomerId(String wcsCustomerId);
	public List<VenCustomer> findByCustomerName(String customerName);
	public VenCustomer persistCustomer(VenCustomer venCustomer) 
			throws VeniceInternalException;
	public VenCustomer synchronizeVenCustomerReferenceData(
			VenCustomer venCustomer) throws VeniceInternalException;
}
