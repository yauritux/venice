package com.gdn.venice.inbound.services;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenContactDetailType;

/**
 * 
 * @author yauritux
 *
 */
public interface ContactDetailTypeService {
		
	public VenContactDetailType synchronizeVenContactDetailType(
			VenContactDetailType contactDetailType) throws VeniceInternalException;			
}
