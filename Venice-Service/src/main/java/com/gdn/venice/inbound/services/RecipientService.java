package com.gdn.venice.inbound.services;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenRecipient;

/**
 * 
 * @author yauritux
 *
 */
public interface RecipientService {

	public VenRecipient persistRecipient(VenRecipient venRecipient) 
			throws VeniceInternalException;	
	public VenRecipient synchronizeVenRecipientReferenceData(
			VenRecipient venRecipient) throws VeniceInternalException;	
}
