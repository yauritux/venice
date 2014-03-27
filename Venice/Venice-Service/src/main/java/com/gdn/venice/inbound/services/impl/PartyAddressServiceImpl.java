package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenPartyAddressDAO;
import com.gdn.venice.exception.CannotPersistPartyAddressException;
import com.gdn.venice.exception.InvalidOrderException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.AddressService;
import com.gdn.venice.inbound.services.PartyAddressService;
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenAddressType;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPartyAddress;
import com.gdn.venice.persistence.VenPartyAddressPK;
import com.gdn.venice.util.CommonUtil;
import com.gdn.venice.util.VeniceConstants;

/**
 * 
 * @author yauritux
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class PartyAddressServiceImpl implements PartyAddressService {
	
	@Autowired
	private VenPartyAddressDAO venPartyAddressDAO;
	
	@Autowired
	private AddressService addressService;
	
	@Override
	public List<VenPartyAddress> findByVenParty(VenParty party) {
		return venPartyAddressDAO.findByVenParty(party);
	}
	
	/**
	 * updatePartyAddressList - compares the existing party address list with the new party address list,
	 * writes any new addresses to the database and returns the updated address list.
	 * 
	 * @param existingVenAddressList
	 * @param newVenAddressList
	 * @return the updated address list
	 * @throws InvalidOrderException 
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenPartyAddress> updatePartyAddressList(
			List<VenPartyAddress> existingPartyAddresses
			, List<VenPartyAddress> newPartyAddresses) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "updatePartyAddressList::BEGIN");
		
		List<VenPartyAddress> updatedPartyAddressList = new ArrayList<VenPartyAddress>();
		List<VenPartyAddress> persistedPartyAddressList = new ArrayList<VenPartyAddress>();
		
		//VenPartyAddress tempPartyAddress = new VenPartyAddress();		
		
		boolean isAddressEqual = false;
		
		for (VenPartyAddress newVenPartyAddress : newPartyAddresses) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "VenParty = " + newVenPartyAddress.getVenParty() + ", street = " + newVenPartyAddress.getVenAddress().getStreetAddress1());
			for (VenPartyAddress existingVenPartyAddress : existingPartyAddresses) {
				if (existingVenPartyAddress.getVenAddress().equals(newVenPartyAddress.getVenAddress())) {
					isAddressEqual=true; 
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "updatePartyAddressList::party address equal with existing.");
					updatedPartyAddressList.add(existingVenPartyAddress);
					break;				
				} else {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "updatePartyAddressList::party address NOT equal with existing.");
					isAddressEqual=false; // continue to compare with next existing address
					//tempPartyAddress = existingVenPartyAddress;					
				}
			} //end of existingVenPartyAddresses loop
			
			if(isAddressEqual==false){
				//The address is a new address so it needs to be persisted
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "updatePartyAddressList::party address is new address.");
				persistedPartyAddressList.add(newVenPartyAddress);
			}			
		}
		
		//Persist any addresses that are new		
		if(!persistedPartyAddressList.isEmpty()){
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "updatePartyAddressList::persistedPartyAddressList not empty, means new address need to be persisted");
			persistedPartyAddressList = persistPartyAddresses(persistedPartyAddressList);
			
			//Add the persisted addresses to the new list
			updatedPartyAddressList.addAll(persistedPartyAddressList);
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "updatePartyAddressList::returning updatedPartyAddressList = " + updatedPartyAddressList.size());
		
		return updatedPartyAddressList;		
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
			//Iterator<VenPartyAddress> i = venPartyAddressList.iterator();
			//while (i.hasNext()) {
			try {
				for (VenPartyAddress venPartyAddress : venPartyAddressList) {

					//ensure that VenAddress is saved before going to persist the party address				
					VenAddress address = addressService.persistAddress(venPartyAddress.getVenAddress());
					if (address != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistPartyAddresses::address is not NULL, assign to venPartyAddress");
						venPartyAddress.setVenAddress(address);
					}
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistPartyAddresses::venParty address ID = " + 
					        (venPartyAddress.getVenAddress() != null ? venPartyAddress.getVenAddress().getAddressId() : 0));

					//VenPartyAddress next = i.next();

					// Set up the primary key object
					VenPartyAddressPK id = new VenPartyAddressPK();
					/*
					id.setAddressId(next.getVenAddress().getAddressId());
					id.setPartyId(next.getVenParty().getPartyId());
					id.setAddressTypeId(next.getVenAddressType().getAddressTypeId());
					next.setId(id);
					 */
					id.setAddressId(venPartyAddress.getVenAddress().getAddressId());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistPartyAddresses::party ID = " + venPartyAddress.getVenParty().getPartyId());
					id.setPartyId(venPartyAddress.getVenParty().getPartyId());
					
					VenAddressType venAddressType = new VenAddressType();
					venAddressType.setAddressTypeId(VeniceConstants.VEN_ADDRESS_TYPE_DEFAULT);
					venPartyAddress.setVenAddressType(venAddressType);
					
					id.setAddressTypeId(venPartyAddress.getVenAddressType().getAddressTypeId());
					venPartyAddress.setId(id);
					// Persist the object
					//newVenPartyAddressList.add(venPartyAddressDAO.save(next));
					//newVenPartyAddressList.add(next);
					newVenPartyAddressList.add(venPartyAddress);
				}
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), e);
				e.printStackTrace();
				CommonUtil.logAndReturnException(new CannotPersistPartyAddressException("Cannot persist VenPartyAddress"
						, VeniceExceptionConstants.VEN_EX_000032), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistPartyAddresses::EOM, returning newVenPartyAddressList = " + newVenPartyAddressList.size());
		return newVenPartyAddressList;
	}

}
