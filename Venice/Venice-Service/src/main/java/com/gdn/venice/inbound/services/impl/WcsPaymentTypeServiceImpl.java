package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenWcsPaymentTypeDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.exception.WcsPaymentTypeNotFoundException;
import com.gdn.venice.inbound.services.WcsPaymentTypeService;
import com.gdn.venice.persistence.VenWcsPaymentType;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class WcsPaymentTypeServiceImpl implements WcsPaymentTypeService {

	@Autowired
	private VenWcsPaymentTypeDAO venWcsPaymentTypeDAO;
	
	@Override
	public VenWcsPaymentType synchronizeVenWcsPaymentType(VenWcsPaymentType venWcsPaymentType)
	  throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenWcsPaymentType::BEGIN,venWcsPaymentType = " + venWcsPaymentType);
		
		VenWcsPaymentType synchWcsPaymentType = venWcsPaymentType;
		
		if (venWcsPaymentType != null && venWcsPaymentType.getWcsPaymentTypeCode() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenWcsPaymentType::wcsPaymentTypeCode =  " + venWcsPaymentType.getWcsPaymentTypeCode());
			synchWcsPaymentType = venWcsPaymentTypeDAO.findByWcsPaymentTypeCode(venWcsPaymentType.getWcsPaymentTypeCode());
			if (synchWcsPaymentType == null) {
				throw CommonUtil.logAndReturnException(new WcsPaymentTypeNotFoundException(
						"WCS Payment type does not exist!", VeniceExceptionConstants.VEN_EX_400003)
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);				
			}
			return synchWcsPaymentType;
		}
		return synchWcsPaymentType;
	}
	
	@Override
	public List<VenWcsPaymentType> synchronizeVenWcsPaymentTypeReferences(
			List<VenWcsPaymentType> wcsPaymentTypeReferences)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenWcsPaymentTypeReferences::BEGIN,wcsPaymentTypeReferences="
				  + wcsPaymentTypeReferences);
		
		List<VenWcsPaymentType> synchronizedWcsPaymentTypeReferences
		   = new ArrayList<VenWcsPaymentType>();
		
		if (wcsPaymentTypeReferences != null) {
			try {
				for (VenWcsPaymentType wcsPaymentType : wcsPaymentTypeReferences) {
					synchronizedWcsPaymentTypeReferences.add(synchronizeVenWcsPaymentType(wcsPaymentType));
				} // end of 'for'
			} catch (VeniceInternalException e) {
				CommonUtil.logAndReturnException(e, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			} catch (Exception e) {
				throw CommonUtil.logAndReturnException(new WcsPaymentTypeNotFoundException(
						"WCS Payment type does not exist!", VeniceExceptionConstants.VEN_EX_400003)
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);				
			}
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenWcsPaymentTypeReferences::returning synchronizedWcsPaymentTypeReferences = "
				  + synchronizedWcsPaymentTypeReferences.size());
		return synchronizedWcsPaymentTypeReferences;
	}

}