package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenWcsPaymentType;

/**
 * 
 * @author yauritux
 *
 */
public interface WcsPaymentTypeService {

	public VenWcsPaymentType synchronizeVenWcsPaymentType(VenWcsPaymentType venWcsPaymentType)
	  throws VeniceInternalException;
	public List<VenWcsPaymentType> synchronizeVenWcsPaymentTypeReferences
	   (List<VenWcsPaymentType> wcsPaymentTypeReferences) throws VeniceInternalException;
}
