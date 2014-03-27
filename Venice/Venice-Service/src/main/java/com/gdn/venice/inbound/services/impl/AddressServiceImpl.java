package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.VenAddressDAO;
import com.gdn.venice.exception.InvalidOrderException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.AddressService;
import com.gdn.venice.inbound.services.CityService;
import com.gdn.venice.inbound.services.CountryService;
import com.gdn.venice.inbound.services.StateService;
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenCity;
import com.gdn.venice.persistence.VenCountry;
import com.gdn.venice.persistence.VenState;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 * 
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class AddressServiceImpl implements AddressService {
	
	@Autowired
	private VenAddressDAO venAddressDAO;
	
	@Autowired
	private CityService cityService;
	
	@Autowired
	private CountryService countryService;
	
	@Autowired
	private StateService stateService;
	
	@PersistenceContext
	private EntityManager em;
	
	/**
	 * updateAddressList - compares the existing address list with the new address list,
	 * writes any new addresses to the database and returns the updated address list.
	 * 
	 * @param existingVenAddressList
	 * @param newVenAddressList
	 * @return the updated address list
	 * @throws InvalidOrderException 
	 */	
	/*
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenAddress> updateAddressList(
			List<VenAddress> existingVenAddressList,
			List<VenAddress> newVenAddressList) throws VeniceInternalException {
		List<VenAddress> updatedVenAddressList = new ArrayList<VenAddress>();
		List<VenAddress> persistVenAddressList = new ArrayList<VenAddress>();
		VenAddress tempAddress = new VenAddress();
		
		Boolean isAddressEqual=false;
		for(VenAddress newVenAddress : newVenAddressList){
			for(VenAddress existingVenAddress : existingVenAddressList){	
				
				if(((existingVenAddress.getKecamatan() == null && newVenAddress.getKecamatan() == null) || (existingVenAddress.getKecamatan()==null?"":existingVenAddress.getKecamatan().trim()).equalsIgnoreCase(newVenAddress.getKecamatan()==null?"":newVenAddress.getKecamatan().trim()))
						&& ((existingVenAddress.getKelurahan() == null && newVenAddress.getKelurahan() == null) || (existingVenAddress.getKelurahan()==null?"":existingVenAddress.getKelurahan().trim()).equalsIgnoreCase(newVenAddress.getKelurahan()==null?"":newVenAddress.getKelurahan().trim()))
						&& ((existingVenAddress.getPostalCode() == null && newVenAddress.getPostalCode() == null) || (existingVenAddress.getPostalCode()==null?"":existingVenAddress.getPostalCode().trim()).equalsIgnoreCase(newVenAddress.getPostalCode()==null?"":newVenAddress.getPostalCode().trim()))
						&& ((existingVenAddress.getStreetAddress1() == null && newVenAddress.getStreetAddress1() == null) || (existingVenAddress.getStreetAddress1()==null?"":existingVenAddress.getStreetAddress1().trim()).equalsIgnoreCase(newVenAddress.getStreetAddress1()==null?"":newVenAddress.getStreetAddress1().trim()))
						&& ((existingVenAddress.getStreetAddress2() == null && newVenAddress.getStreetAddress2() == null) || (existingVenAddress.getStreetAddress2()==null?"":existingVenAddress.getStreetAddress2().trim()).equalsIgnoreCase(newVenAddress.getStreetAddress2()==null?"":newVenAddress.getStreetAddress2().trim()))
						&& ((existingVenAddress.getVenCity() == null && newVenAddress.getVenCity() == null) || ((existingVenAddress.getVenCity()!=null?existingVenAddress.getVenCity().getCityCode():null)==null?"":existingVenAddress.getVenCity().getCityCode().trim()).equalsIgnoreCase((newVenAddress.getVenCity()!=null?newVenAddress.getVenCity().getCityCode():null)==null?"":newVenAddress.getVenCity().getCityCode().trim()))
						&& ((existingVenAddress.getVenCountry() == null && newVenAddress.getVenCountry() == null) || (existingVenAddress.getVenCountry().getCountryCode()==null?"":existingVenAddress.getVenCountry().getCountryCode().trim()).equalsIgnoreCase(newVenAddress.getVenCountry().getCountryCode()==null?"":newVenAddress.getVenCountry().getCountryCode().trim()))
						&& ((existingVenAddress.getVenState() == null && newVenAddress.getVenState() == null) || ((existingVenAddress.getVenState()!=null?existingVenAddress.getVenState().getStateCode():null)==null?"":existingVenAddress.getVenState().getStateCode().trim()).equalsIgnoreCase((newVenAddress.getVenState()!=null?newVenAddress.getVenState().getStateCode():null)==null?"":newVenAddress.getVenState().getStateCode().trim()))){
					//The address is assumed to be equal, not that the equals() 
					//operation can't be used because it is implemented by 
					//JPA on the primary key. Add it to the list
					isAddressEqual=true;
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "updateAddressList::party address equal with existing.");
					updatedVenAddressList.add(existingVenAddress);
					break;
				}else{
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "updateAddressList::party address NOT equal with existing.");
					isAddressEqual=false;
					tempAddress = existingVenAddress;
				}
			}
			if(isAddressEqual==false){
				//The address is a new address so it needs to be persisted
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "updateAddressList::party address is new address.");
				newVenAddress.setVenPartyAddresses(tempAddress.getVenPartyAddresses());
				persistVenAddressList.add(newVenAddress);
			}
		}	
				
		//Persist any addresses that are new		
		if(!persistVenAddressList.isEmpty()){
			persistVenAddressList = persistAddressList(persistVenAddressList);
			
			//Add the persisted addresses to the new list
			updatedVenAddressList.addAll(persistVenAddressList);
		}
		for (VenAddress updatedAddress : updatedVenAddressList) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "updateAddressList::updatedAddress Street Address 1 = " + updatedAddress.getStreetAddress1());
		}
		return updatedVenAddressList;
	}	
	*/

	/**
	 * Persists a list of addresses.
	 * 
	 * @param venAddressList
	 * @return
	 * @throws InvalidOrderException 
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenAddress> persistAddressList(List<VenAddress> venAddressList)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistAddressList::BEGIN, venAddressList = " + venAddressList);
		List<VenAddress> newVenAddressList = new ArrayList<VenAddress>();
		Iterator<VenAddress> i = venAddressList.iterator();
		while (i.hasNext()) {
			VenAddress newAddress = this.persistAddress(i.next());
			newVenAddressList.add(newAddress);
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistAddressList::EOM, returning newVenAddressList = " + newVenAddressList);
		return newVenAddressList;
	}

	/**
	 * Persists an address using the session tier
	 * 
	 * @param venAddress
	 * @return the persisted object
	 * @throws InvalidOrderException 
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenAddress persistAddress(VenAddress venAddress)
			throws VeniceInternalException {
		if (venAddress != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistAddress::Persisting VenAddress... :" + venAddress.getStreetAddress1());
			// Synchronize the reference data
			venAddress = synchronizeVenAddressReferenceData(venAddress);
			
			// Persist the object
			if (venAddress.getAddressId() == null) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistAddress::venAddress is NEW");				
				if(venAddress.getStreetAddress1()==null && venAddress.getKecamatan()==null && venAddress.getKelurahan()==null && venAddress.getVenCity()==null &&
						venAddress.getVenState()==null && venAddress.getPostalCode()==null && venAddress.getVenCountry()==null){
					CommonUtil.logInfo(CommonUtil.getLogger(this.getClass().getCanonicalName())
							, "persistAddress::Address is null, no need to persist address");
				}else{
					//detach city, state, and country since it can be null from WCS	
					CommonUtil.logInfo(CommonUtil.getLogger(this.getClass().getCanonicalName())
							, "persistAddress::Address is not null, detach city, state, and country");
					VenCity city = null;
					VenState state = null;
					VenCountry country = null;

					if(venAddress.getVenCity()!=null){
						if(venAddress.getVenCity().getCityCode()!=null){
							city = venAddress.getVenCity();
							em.detach(city);
						}							
						venAddress.setVenCity(null);
					}

					if(venAddress.getVenState()!=null){
						if(venAddress.getVenState().getStateCode()!=null){
							state = venAddress.getVenState();
							em.detach(state);
						}							
						venAddress.setVenState(null);
					}

					if(venAddress.getVenCountry()!=null){
						if(venAddress.getVenCountry().getCountryCode()!=null){
							country = venAddress.getVenCountry();
							em.detach(country);
						}							
						venAddress.setVenCountry(null);
					}			
					
					if (!em.contains(venAddress)) {
						// venAddress is in detach mode, hence we need to call save explicitly
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistAddress::attaching venAddress");
						venAddress = venAddressDAO.save(venAddress);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistAddress::venAddress is attached");						
					}					
					
					//reattach after persisted
					venAddress.setVenCity(city);
					venAddress.setVenState(state);
					venAddress.setVenCountry(country);
				}
			} else {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistAddress::updating/renew the data in venAddress");				
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistAddress::merge address");
				if (!em.contains(venAddress)) {
					// venAddress is in detach mode, hence we need to call save explicitly
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistAddress::attaching venAddress");					
					venAddress = venAddressDAO.save(venAddress);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistAddress::venAddress is attached");					
				}				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistAddress::successfully merged venAddress");				
			}

		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistAddress::EOM, returning venAddress " + venAddress);
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistAddress::EOM, returning venAddress street address-1=" + venAddress.getStreetAddress1());		
		return venAddress;
	}

	/**
	 * Synchronizes the data for the direct VenAddress references
	 * 
	 * @param venAddress
	 * @return the synchronized data object
	 */	
	@Override
	public VenAddress synchronizeVenAddressReferenceData(VenAddress venAddress)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenAddressReferenceData::BEGIN, venAddress = " + venAddress);		
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenAddressReferenceData::calling synchronize methods for city, country, and state");
		
		VenCity venCity = venAddress.getVenCity();
		em.detach(venCity);
		//VenCity synchCity = cityService.synchronizeVenCity(venAddress.getVenCity());
		VenCity synchCity = cityService.synchronizeVenCity(venCity);		
		venAddress.setVenCity(synchCity);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenAddressReferenceData::city has been synchronized, result = " + synchCity);
		
		VenCountry venCountry = venAddress.getVenCountry();
		em.detach(venCountry);
		VenCountry synchCountry = countryService.synchronizeVenCountry(venCountry);
		venAddress.setVenCountry(synchCountry);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenAddressReferenceData::country has been synchronized, result = " + synchCountry);		
		
		VenState venState = venAddress.getVenState();
		em.detach(venState);
		VenState synchState = stateService.synchronizeVenState(venState);
		venAddress.setVenState(synchState);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenAddressReferenceData::state has been synchronized, result = " + synchState);		
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeReferenceData::EOM, returning venAddress = " + venAddress);
		
		return venAddress;
	}

}
