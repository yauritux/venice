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
	public List<VenWcsPaymentType> synchronizeVenWcsPaymentTypeReferences(
			List<VenWcsPaymentType> wcsPaymentTypeReferences)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenWcsPaymentTypeReferences::BEGIN,wcsPaymentTypeReferences="
				  + wcsPaymentTypeReferences);
		//if (wcsPaymentTypeReferences == null || wcsPaymentTypeReferences.isEmpty()) return null;
		
		List<VenWcsPaymentType> synchronizedWcsPaymentTypeReferences
		   = new ArrayList<VenWcsPaymentType>();
		
		if (wcsPaymentTypeReferences != null) {
			for (VenWcsPaymentType wcsPaymentType : wcsPaymentTypeReferences) {
				if (wcsPaymentType.getWcsPaymentTypeCode() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenWcsPaymentTypeReferences::Restricting VenWcsPaymentType... :" 
									+ wcsPaymentType.getWcsPaymentTypeCode());
					VenWcsPaymentType venWcsPaymentType = venWcsPaymentTypeDAO.findByWcsPaymentTypeCode(wcsPaymentType.getWcsPaymentTypeCode()); 
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenWcsPaymentTypeReferences::venWcsPaymentType = " + venWcsPaymentType);
					if (venWcsPaymentType == null) {
						throw CommonUtil.logAndReturnException(new WcsPaymentTypeNotFoundException(
								"WCS Payment type does not exist", VeniceExceptionConstants.VEN_EX_400003)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenWcsPaymentTypeReferences::adding venWcsPaymentType "
										+ venWcsPaymentType + " into synchronizedWcsPaymentTypeReferences");
						synchronizedWcsPaymentTypeReferences.add(venWcsPaymentType);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenWcsPaymentTypeReferences::venWcsPaymentType added into synchronizedWcsPaymentTypeReferences");
					}
				}			
			} // end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenWcsPaymentTypeReferences::returning synchronizedWcsPaymentTypeReferences = "
				  + synchronizedWcsPaymentTypeReferences.size());
		return synchronizedWcsPaymentTypeReferences;
	}

}
