package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenContactDetail;
import com.gdn.venice.persistence.VenParty;

/**
 * 
 * @author yauritux
 *
 */
public interface ContactDetailService {

	public List<VenContactDetail> findByParty(VenParty party);
	
	public List<VenContactDetail> updateContactDetailList(VenParty existingParty
			, List<VenContactDetail> existingVenContactDetailList, List<VenContactDetail> newVenContactDetailList)
			throws VeniceInternalException;
	
	public List<VenContactDetail> persistContactDetails(List<VenContactDetail> venContactDetails) 
			throws VeniceInternalException; 
	
	public VenContactDetail synchronizeVenContactDetailReferenceData(VenContactDetail venContactDetail)
	        throws VeniceInternalException;
	
	public List<VenContactDetail> synchronizeVenContactDetailReferences(List<VenContactDetail> contactDetailReferences);
 }
