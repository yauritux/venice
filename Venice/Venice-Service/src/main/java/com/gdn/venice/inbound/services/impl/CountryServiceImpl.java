package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.VenCountryDAO;
import com.gdn.venice.inbound.services.CountryService;
import com.gdn.venice.persistence.VenCountry;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class CountryServiceImpl implements CountryService {
	
	@Autowired
	private VenCountryDAO venCountryDAO;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenCountry synchronizeVenCountry(VenCountry venCountry) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCountry::BEGIN,venCountry = " + venCountry);
		VenCountry synchCountry = venCountry;
		if (venCountry != null && venCountry.getCountryCode() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCountry::countryCode=  " + venCountry.getCountryCode());
			List<VenCountry> countryList = venCountryDAO.findByCountryCode(venCountry.getCountryCode());
			if (countryList == null || countryList.isEmpty()) {
				if (!em.contains(venCountry)) {
					//venCountry in detach mode, hence need to explicitly call save 
					synchCountry = venCountryDAO.saveAndFlush(venCountry);
				} 
			} else {
				synchCountry = countryList.get(0);
			}
		}
		return synchCountry;
	}
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenCountry> synchronizeVenCountryReferences(
			List<VenCountry> countryReferences) {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCountryReferences::BEGIN,countryReferences="
				  + countryReferences);
		
		List<VenCountry> synchronizedVenCountries = new ArrayList<VenCountry>();
		
		if (countryReferences != null) {
			for (VenCountry country : countryReferences) {
				synchronizedVenCountries.add(synchronizeVenCountry(country));
			} //end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCountryReferences::returning synchronizedVenCountries = "
				+ synchronizedVenCountries.size());
		return synchronizedVenCountries;
	}

}
