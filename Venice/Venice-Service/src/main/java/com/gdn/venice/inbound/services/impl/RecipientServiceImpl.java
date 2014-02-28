package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.VenPartyTypeConstants;
import com.gdn.venice.dao.VenRecipientDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.PartyService;
import com.gdn.venice.inbound.services.RecipientService;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPartyType;
import com.gdn.venice.persistence.VenRecipient;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class RecipientServiceImpl implements RecipientService {

	@Autowired
	private VenRecipientDAO venRecipientDAO;
	
	@Autowired
	private PartyService partyService;
	
	/**
	 * Persists a recipient using the session tier
	 * 
	 * @param venRecipient
	 * @return the persisted object
	 */	
	@Override
	public VenRecipient persistRecipient(VenRecipient venRecipient)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistRecipient::BEGIN, venRecipient= " + venRecipient);
		
		if (venRecipient != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistRecipient::Persisting VenRecipient... :" + venRecipient.getVenParty().getFullOrLegalName());

			// Persist the party
			VenPartyType venPartyType = new VenPartyType();

			// Set the party type to Recipient
			venPartyType.setPartyTypeId(new Long(VenPartyTypeConstants.VEN_PARTY_TYPE_RECIPIENT.code()));
			venRecipient.getVenParty().setVenPartyType(venPartyType);

			//venRecipient.setVenParty(partyService.persistParty(venRecipient.getVenParty(), "Recipient"));
			VenParty venParty = partyService.persistParty(venRecipient.getVenParty()
					, VenPartyTypeConstants.VEN_PARTY_TYPE_RECIPIENT.description());
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistRecipient::successfully persisted VenParty");
			venRecipient.setVenParty(venParty);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistRecipient::venParty is assigned to venRecipient");
			// Synchronize the reference data
			venRecipient = this.synchronizeVenRecipientReferenceData(venRecipient);
			// Persist the object
			venRecipient = venRecipientDAO.save(venRecipient);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "successfully persisted venRecipient");
		}
		return venRecipient;
	}
	
	/**
	 * Synchronizes the data for the direct VenRecipient references
	 * 
	 * @param venRecipient
	 * @return the synchronized data object
	 */	
	@Override
	public VenRecipient synchronizeVenRecipientReferenceData(VenRecipient venRecipient) throws VeniceInternalException {

		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenRecipientReferenceData::BEGIN, venRecipient = " + venRecipient);
		
		List<VenParty> venParties = new ArrayList<VenParty>();
		venParties.add(venRecipient.getVenParty());

		// Synchronize the data references
		venParties = partyService.synchronizeVenPartyReferenceData(venParties);

		// Push the keys back into the record
		/*
		Iterator<Object> referencesIterator = references.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenParty) {
				venRecipient.setVenParty((VenParty) next);
			}
		}
		*/
		
		for (VenParty party : venParties) {
			venRecipient.setVenParty(party);
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenRecipientReferenceData::EOM, returning venRecipient = " + venRecipient);
		return venRecipient;		
	}
}
