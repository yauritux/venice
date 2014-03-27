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
import com.gdn.venice.dao.VenCityDAO;
import com.gdn.venice.exception.VenCitySynchronizingError;
import com.gdn.venice.exception.VeniceInternalException;
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
	public VenCity synchronizeVenCity(VenCity venCity) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::BEGIN,venCity=" + venCity);
		VenCity synchCity = venCity;
		if (venCity != null && venCity.getCityCode() != null) {
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::cityCode = " + venCity.getCityCode());
				List<VenCity> cityList = venCityDAO.findByCityCode(venCity.getCityCode());
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::cityList found = "
						+ (cityList != null ? cityList.size() : 0));
				if (cityList == null || cityList.isEmpty()) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenCity::city doesn't exist yet, persisting it");
					if (!em.contains(venCity)) {
						//venCity is in detach mode, hence need to explicitly call save method
						synchCity = venCityDAO.save(venCity);
					}
				} else {
					synchCity = cityList.get(0);
				}
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName()
						, e);
				CommonUtil.logAndReturnException(new VenCitySynchronizingError("Error in synchronyzing VenCity"
						, VeniceExceptionConstants.VEN_EX_130002), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCity::returning synchCity = " + synchCity);
		return synchCity;
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenCity> synchronizeVenCityReferences(
			List<VenCity> cityReferences) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCityReferences::cityReferences=" + cityReferences);
		
		List<VenCity> synchronizedVenCities = new ArrayList<VenCity>();
		
		if (cityReferences != null) {
			for (VenCity city : cityReferences) {
				synchronizedVenCities.add(synchronizeVenCity(city));
			} //End Of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizedVenCityReferences::EOM, returning synchronizedVenCities = " + synchronizedVenCities.size());
		return synchronizedVenCities;
	}

}