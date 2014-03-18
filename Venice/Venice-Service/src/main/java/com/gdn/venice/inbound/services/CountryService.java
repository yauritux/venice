package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.persistence.VenCountry;

/**
 * 
 * @author yauritux
 *
 */
public interface CountryService {
	
	public VenCountry synchronizeVenCountry(VenCountry venCountry);
	public List<VenCountry> synchronizeVenCountryReferences(List<VenCountry> countryReferences);
}
