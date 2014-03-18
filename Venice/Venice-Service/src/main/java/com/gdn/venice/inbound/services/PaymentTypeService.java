package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenPaymentType;

/**
 * 
 * @author yauritux
 *
 */
public interface PaymentTypeService {

	public VenPaymentType synchronizeVenPaymentType(VenPaymentType venPaymentType)
	  throws VeniceInternalException;
	public List<VenPaymentType> synchronizeVenPaymentTypeReferences
	   (List<VenPaymentType> paymentTypeReferences) throws VeniceInternalException;
}
