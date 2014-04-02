package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenCountryDAO;
import com.gdn.venice.exception.VenCountrySynchronizingError;
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
		VenCountry synchCountry = new VenCountry();
		if (venCountry != null && venCountry.getCountryCode() != null) {
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCountry::countryCode=  " + venCountry.getCountryCode());
				List<VenCountry> countryList = venCountryDAO.findByCountryCode(venCountry.getCountryCode());
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCountry::countryList found = "
						+ (countryList != null ? countryList.size() : 0));			
				if (countryList == null || countryList.isEmpty()) {
					if (!em.contains(venCountry)) {
						//venCountry in detach mode, hence need to explicitly call save 
						synchCountry = venCountryDAO.save(venCountry);
					} else {
						synchCountry = venCountry;
					}
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenCountry::new venCountry is added successfully into DB");
				} else {
					synchCountry = countryList.get(0);
				}
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName()
						, e);
				CommonUtil.logAndReturnException(new VenCountrySynchronizingError("Error in synchronizing VenCountry"
						, VeniceExceptionConstants.VEN_EX_130003), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);				
			}
		} else {
			synchCountry = venCountry;
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCountry::returning synchCountry = " + (synchCountry != null ? synchCountry.getCountryCode() : synchCountry));
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
