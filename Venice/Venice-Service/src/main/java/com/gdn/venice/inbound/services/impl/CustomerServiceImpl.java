package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VenPartyTypeConstants;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenCustomerDAO;
import com.gdn.venice.exception.CannotPersistCustomerException;
import com.gdn.venice.exception.InvalidOrderException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.CustomerService;
import com.gdn.venice.inbound.services.PartyService;
import com.gdn.venice.persistence.VenCustomer;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPartyType;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class CustomerServiceImpl implements CustomerService {
	
	@Autowired
	private VenCustomerDAO venCustomerDAO;
	
	@Autowired
	private PartyService partyService;

	@Override
	public VenCustomer findByWcsCustomerId(String wcsCustomerId) {
		List<VenCustomer> venCustomers = venCustomerDAO.findByWcsCustomerId(wcsCustomerId);
		return ((venCustomers != null && venCustomers.size() > 0) 
				? venCustomers.get(0) : null);
	}
	
	@Override
	public List<VenCustomer> findByCustomerName(String customerName) {
		return venCustomerDAO.findByCustomerName(customerName);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenCustomer persistCustomer(VenCustomer venCustomer)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistCustomer::BEGIN, venCustomer = " + venCustomer);
		if (venCustomer != null) {
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::Persisting VenCustomer... :" + venCustomer.getCustomerUserName());
				// If the customer already exists then return it, else persist everything
				VenCustomer existingVenCustomer = findByWcsCustomerId(venCustomer.getWcsCustomerId());
				if (existingVenCustomer != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistCustomer::existingVenCustomer NOT NULL --> " + existingVenCustomer);
					venCustomer.setCustomerId(existingVenCustomer.getCustomerId());
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::setting Customer's partyType");
				VenPartyType venPartyType = new VenPartyType();
				// Set the party type to Customer
				//venPartyType.setPartyTypeId(new Long(4));
				venPartyType.setPartyTypeId(VenPartyTypeConstants.VEN_PARTY_TYPE_CUSTOMER.code());
				venPartyType.setPartyTypeDesc("Customer");
				venCustomer.getVenParty().setVenPartyType(venPartyType);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::Customer's partyType successfully set");
				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::setting Customer's party");
				VenParty party = venCustomer.getVenParty();
				List<VenCustomer> venCustomers = new ArrayList<VenCustomer>();
				venCustomers.add(0, venCustomer);
				party.setVenCustomers(venCustomers);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::Customer's party successfully set");
				venCustomer.setVenParty(partyService.persistParty(party, "Customer"));
				// Synchronize the reference data
				//venCustomer = synchronizeVenCustomerReferenceData(venCustomer);

				// Persist the object
				VenCustomer customer = venCustomer;
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::persisting venCustomer");
				//venCustomer.setVenParty(customer.getVenParty());
				venCustomer = venCustomerDAO.save(venCustomer);
				venCustomer.setVenParty(customer.getVenParty());
			} catch (Exception e) {
				e.printStackTrace();
				throw CommonUtil.logAndReturnException(new CannotPersistCustomerException(
						e.getMessage(), VeniceExceptionConstants.VEN_EX_100001)
				  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistCustomer::EOM, venCustomer has been persisted successfully, " + venCustomer);
		return venCustomer;		
	}

	/**
	 * Synchronizes the data for the direct VenCustomer references
	 * 
	 * @param venCustomer
	 * @return the synchronized data object
	 */	
	@Override
	public VenCustomer synchronizeVenCustomerReferenceData(
			VenCustomer venCustomer) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCustomerReferenceData, BEGIN, venCustomer = " + venCustomer);
		List<VenParty> references = new ArrayList<VenParty>();
		references.add(venCustomer.getVenParty());

		// Synchronize the data references
		//references = this.synchronizeReferenceData(references);
		references = partyService.synchronizeVenPartyReferenceData(references);

		// Push the keys back into the record
		/*
		Iterator<Object> referencesIterator = references.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenParty) {
				venCustomer.setVenParty((VenParty) next);
			}
		}
		*/
		
		for (VenParty venParty : references) { // weird, isn't it ? Got what I mean here ? VenCustomer should merely refer to one VenParty (1-1 relation)
			venCustomer.setVenParty(venParty); // thus, why do we need to do the logic in the loop ? 
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCustomerReferenceData::EOM, returning venCustomer " + venCustomer);
		return venCustomer;		
	}
}
