package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenBank;

/**
 * 
 * @author yauritux
 *
 */
public interface BankService {

	public List<VenBank> synchronizeVenBankReferences(List<VenBank> bankReferences) 
	   throws VeniceInternalException;
}
