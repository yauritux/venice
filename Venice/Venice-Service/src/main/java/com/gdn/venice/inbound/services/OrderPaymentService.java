package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.integration.jaxb.Payment;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrderPayment;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderPaymentService {
	
	public List<VenOrderPayment> findByWcsPaymentId(String wcsPaymentId);
	public VenOrderPayment getVenOrderPayment(Payment payment);
	public boolean isPaymentExist(Payment payment);
	public boolean isPaymentApproved(Payment payment);
	public boolean isVirtualAccountPayment(String paymentType);
	public boolean isCSPayment(String paymentType);
	public List<VenOrderPayment> persistOrderPaymentList(List<VenOrderPayment> venOrderPaymentList) 
			throws VeniceInternalException;
	public VenOrderPayment synchronizeVenOrderPaymentReferenceData(
			VenOrderPayment venOrderPayment) throws VeniceInternalException;
	public List<VenOrderPayment> synchronizeVenOrderPaymentReferences(
			List<VenOrderPayment> orderPaymentReferences) throws VeniceInternalException;
}
