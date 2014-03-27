package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenContactDetailType;

/**
 * 
 * @author yauritux
 *
 */
public interface ContactDetailTypeService {
	
	public VenContactDetailType synchronizeVenContactDetailType(VenContactDetailType venContactDetailType) 
	   throws VeniceInternalException;
	public List<VenContactDetailType> synchronizeVenContactDetailTypeReferences(
			List<VenContactDetailType> contactDetailTypes) throws VeniceInternalException;
}
