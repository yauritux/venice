package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
	
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * Persists a recipient using the session tier
	 * 
	 * @param venRecipient
	 * @return the persisted object
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenRecipient persistRecipient(VenRecipient venRecipient)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistRecipient::BEGIN, venRecipient= " + venRecipient);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistRecipient::venRecipient.recipientId = " + venRecipient.getRecipientId());
		
		if (venRecipient != null && venRecipient.getRecipientId() == null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistRecipient::Persisting VenRecipient... :" + venRecipient.getVenParty().getFullOrLegalName());

			// Persist the party
			VenPartyType venPartyType = new VenPartyType();

			// Set the party type to Recipient
			venPartyType.setPartyTypeId(new Long(VenPartyTypeConstants.VEN_PARTY_TYPE_RECIPIENT.code()));
			venRecipient.getVenParty().setVenPartyType(venPartyType);

			CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistRecipient::venParty.partyId = " + venRecipient.getVenParty().getPartyId());
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistRecipient::venParty.partyType = " + venRecipient.getVenParty().getVenPartyType().getPartyTypeDesc());
			VenParty persistedParty = null;
			if (venRecipient.getVenParty().getPartyId() != null) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistRecipient::recipient's party already persisted, no need to call persist twice");
				persistedParty = venRecipient.getVenParty();
				VenPartyType partyType = new VenPartyType();
				partyType.setPartyTypeId(VenPartyTypeConstants.VEN_PARTY_TYPE_RECIPIENT.code());
				partyType.setPartyTypeDesc(VenPartyTypeConstants.VEN_PARTY_TYPE_RECIPIENT.description());
				persistedParty.setVenPartyType(partyType);
			} else {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistRecipient::recipient's party need to be persisted");
			    persistedParty = partyService.persistParty(venRecipient.getVenParty(), "Recipient");
			}
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistRecipient::persistedParty ID = " + persistedParty.getPartyId());
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistRecipient::persistedParty partyType = " + persistedParty.getVenPartyType().getPartyTypeDesc());
			venRecipient.setVenParty(persistedParty);
			// Synchronize the reference data
			venRecipient = this.synchronizeVenRecipientReferenceData(venRecipient);
			// Persist the object
			
			if (!em.contains(venRecipient)) {
				try {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistRecipient::calling save explicitly");
					venRecipient = venRecipientDAO.save(venRecipient);
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistRecipient::venRecipient successfully persisted");
				} catch (Exception e) {
					CommonUtil.logError(this.getClass().getCanonicalName(), e);
					throw new VeniceInternalException("Cannot persist VenRecipient", e);
				}
			}
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistRecipient::returning venRecipient = " + venRecipient);
		
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
		
		for (VenParty party : venParties) {
			venRecipient.setVenParty(party);
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenRecipientReferenceData::EOM, returning venRecipient = " + venRecipient);
		return venRecipient;		
	}
}
