package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
	
	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenCity> synchronizeVenCityReferences(
			List<VenCity> cityReferences) {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCityReferences::cityReferences=" + cityReferences);
		
		List<VenCity> synchronizedVenCities = new ArrayList<VenCity>();
		
		if (cityReferences != null) {
			for (VenCity city : cityReferences) {
				//em.detach(city);
				if (city.getCityCode() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenCityReferences::Synchronizing VenCity... :" + city.getCityCode());
					List<VenCity> cityList = venCityDAO.findByCityCode(city.getCityCode());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenCityReferences::cityList size = "
									+ (cityList != null ? cityList.size() : 0));
					if (cityList == null || (cityList.size() == 0)) {						
						if (!em.contains(city)) {
							// city is in detach mode, hence need to call save explicitly
							city = venCityDAO.save(city);
						}						
						//em.detach(city);						
						synchronizedVenCities.add(city);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenCityReferences::successfully added city " + city 
								+ " into synchronizedVenCities collection");
					} else {
						VenCity venCity = cityList.get(0);
						//em.detach(venCity);
						synchronizedVenCities.add(venCity);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "successfully added venCity into synchronizedVenCities collection");
					}
				}			
			} //End Of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizedVenCityReferences::EOM, returning synchronizedVenCities = " + synchronizedVenCities.size());
		return synchronizedVenCities;
	}

}
