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
	@Transactional(readOnly = false, propagation = Propagation.NOT_SUPPORTED)
	public VenCity synchronizeVenCity(VenCity venCity) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::BEGIN,venCity=" + venCity);
		VenCity synchCity = new VenCity();
		if (venCity != null && venCity.getCityCode() != null) {
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::cityCode = " + venCity.getCityCode());
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::city ID = " + venCity.getCityId());
				if (venCity.getCityId() == null) {
					List<VenCity> cityList = venCityDAO.findByCityCode(venCity.getCityCode());
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::cityList found = "
							+ (cityList != null ? cityList.size() : 0));
					if (cityList == null || cityList.isEmpty()) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenCity::city doesn't exist yet, persisting it");
						if (!em.contains(venCity)) {
							//venCity is in detach mode, hence need to explicitly call save method
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeVenCity::calling venCityDAO.save explicitly, city ID = " + venCity.getCityId());
							synchCity = venCityDAO.save(venCity);
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::city ID = " + venCity.getCityId());
						} else {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::venCity already in attached mode, thus no need to calling save");
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::city ID = " + venCity.getCityId());
							synchCity = venCity;
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::synchCity ID = " + synchCity.getCityId());
						}
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenCity::new VenCity is added successfully into DB");
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::found city in the DB");
						synchCity = cityList.get(0);
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::synchCity ID = " + synchCity.getCityId());
					}
				} else {
					// city ID not null
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenCity::City has been synchronized already, no need  to perform synchronization twice");
					synchCity = venCity;
				}
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName()
						, e);
				CommonUtil.logAndReturnException(new VenCitySynchronizingError("Error in synchronyzing VenCity"
						, VeniceExceptionConstants.VEN_EX_130002), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		} else {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenCity::either venCity is null or venCity.cityCode is null");
			synchCity = venCity;
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCity::returning synchCity = " + (synchCity != null ? synchCity.getCityCode() : synchCity));
		return synchCity;
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.NOT_SUPPORTED)
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