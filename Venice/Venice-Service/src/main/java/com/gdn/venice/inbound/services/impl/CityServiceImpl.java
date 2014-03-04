package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.VenCityDAO;
import com.gdn.venice.inbound.services.CityService;
import com.gdn.venice.persistence.VenCity;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class CityServiceImpl implements CityService {
	
	@Autowired
	private VenCityDAO venCityDAO;

	@Override
	public List<VenCity> synchronizeVenCityReferences(
			List<VenCity> cityReferences) {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCityReferences::cityReferences=" + cityReferences);
		
		List<VenCity> synchronizedVenCities = new ArrayList<VenCity>();
		
		if (cityReferences != null) {
			for (VenCity city : cityReferences) {
				if (city.getCityCode() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenCityReferences::Synchronizing VenCity... :" + city.getCityCode());
					/*
					List<VenCity> cityList = venCityDAO.findByCityCode(city.getCityCode());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenCityReferences::cityList size = "
									+ cityList.size());
					if (cityList == null || (cityList.size() == 0)) {
						VenCity venCity = venCityDAO.save(city);
						synchronizedVenCities.add(venCity);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenCityReferences::successfully added venCity " + venCity 
								+ " into synchronizedVenCities collection");
					} else {
						VenCity venCity = cityList.get(0);
						synchronizedVenCities.add(venCity);
					*/
					VenCity venCity = venCityDAO.save(city);
					synchronizedVenCities.add(venCity);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "successfully added venCity into synchronizedVenCities collection");
					//}
				}			
			} //End Of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizedVenCityReferences::EOM, returning synchronizedVenCities = " + synchronizedVenCities.size());
		return synchronizedVenCities;
	}

}
