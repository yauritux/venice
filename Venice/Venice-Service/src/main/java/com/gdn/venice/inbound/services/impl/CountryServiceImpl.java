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
	public List<VenCountry> synchronizeVenCountryReferences(
			List<VenCountry> countryReferences) {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCountryReferences::BEGIN,countryReferences="
				  + countryReferences);
		
		List<VenCountry> synchronizedVenCountries = new ArrayList<VenCountry>();
		
		if (countryReferences != null) {
			for (VenCountry country : countryReferences) {
				em.detach(country);
				if (country.getCountryCode() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), 
							"synchronizeVenCountryReferences::Synchronizing VenCountry... :" + country.getCountryCode());
					List<VenCountry> countryList = venCountryDAO.findByCountryCode(country.getCountryCode());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenCountryReferences::countryList size = "
									+ (countryList != null ? countryList.size() : 0));
					if (countryList == null || countryList.size() == 0) {
						VenCountry venCountry = venCountryDAO.save(country);
						synchronizedVenCountries.add(venCountry);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenCountryReferences::successfully added venCountry into synchronizedVenCountries");
					} else {
						VenCountry venCountry = countryList.get(0);
						synchronizedVenCountries.add(venCountry);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenCountryReferences::successfully added venCountry into synchronizedVenCountries");
					}
				}		
			} //end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCountryReferences::returning synchronizedVenCountries = "
				+ synchronizedVenCountries.size());
		return synchronizedVenCountries;
	}

}
