package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.VenPartyAddressDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.PartyAddressService;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPartyAddress;
import com.gdn.venice.persistence.VenPartyAddressPK;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class PartyAddressServiceImpl implements PartyAddressService {
	
	@Autowired
	private VenPartyAddressDAO venPartyAddressDAO;
	
	@Override
	public List<VenPartyAddress> findByVenParty(VenParty party) {
		return venPartyAddressDAO.findByVenParty(party);
	}

	/**
	 * Persists a list of party addresses using the session tier.
	 * 
	 * @param venPartyAddressList
	 * @return the persisted object
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenPartyAddress> persistPartyAddresses(
			List<VenPartyAddress> venPartyAddressList)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistPartyAddresses::BEGIN, venPartyAddressList = " + venPartyAddressList);
		List<VenPartyAddress> newVenPartyAddressList = new ArrayList<VenPartyAddress>();
		if (venPartyAddressList != null && !venPartyAddressList.isEmpty()) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistPartyAddresses::Persisting VenPartyAddress list...:" + venPartyAddressList.size());
			Iterator<VenPartyAddress> i = venPartyAddressList.iterator();
			while (i.hasNext()) {
				VenPartyAddress next = i.next();

				// Set up the primary key object
				VenPartyAddressPK id = new VenPartyAddressPK();
				id.setAddressId(next.getVenAddress().getAddressId());
				id.setPartyId(next.getVenParty().getPartyId());
				id.setAddressTypeId(next.getVenAddressType().getAddressTypeId());
				next.setId(id);
				// Persist the object
				newVenPartyAddressList.add(venPartyAddressDAO.save(next));
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistPartyAddresses::EOM, returning newVenPartyAddressList = " + newVenPartyAddressList.size());
		return newVenPartyAddressList;
	}

}
