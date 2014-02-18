package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenPaymentStatus;

/**
 * 
 * @author yauritux
 *
 */
public interface PaymentStatusService {

	public List<VenPaymentStatus> synchronizeVenPaymentStatusReferences
	   (List<VenPaymentStatus> paymentStatusReferences) throws VeniceInternalException;
}
