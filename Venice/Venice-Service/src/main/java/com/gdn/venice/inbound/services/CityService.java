package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.persistence.VenCity;

/**
 * 
 * @author yauritux
 *
 */
public interface CityService {
	
	public List<VenCity> synchronizeVenCityReferences(List<VenCity> cityReferences);
}
