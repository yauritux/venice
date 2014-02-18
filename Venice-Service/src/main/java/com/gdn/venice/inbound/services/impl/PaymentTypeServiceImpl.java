package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenPaymentTypeDAO;
import com.gdn.venice.exception.InvalidOrderException;
import com.gdn.venice.exception.PaymentTypeNotFoundException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.PaymentTypeService;
import com.gdn.venice.persistence.VenPaymentType;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class PaymentTypeServiceImpl implements PaymentTypeService {

	@Autowired
	private VenPaymentTypeDAO venPaymentTypeDAO;
	
	@Override
	public List<VenPaymentType> synchronizeVenPaymentTypeReferences(
			List<VenPaymentType> paymentTypeReferences)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPaymentTypeReferences::BEGIN,paymentTypeReferences="
				   + paymentTypeReferences);
		//if (paymentTypeReferences == null || paymentTypeReferences.isEmpty()) return null;
		
		List<VenPaymentType> synchronizedPaymentTypeReferences 
		   = new ArrayList<VenPaymentType>();
		
		if (paymentTypeReferences != null) {
			for (VenPaymentType paymentType : paymentTypeReferences) {
				if (paymentType.getPaymentTypeCode() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenPaymentTypeReferences::Restricting VenPaymentType... :" 
									+ paymentType.getPaymentTypeCode());
					List<VenPaymentType> paymentTypeList = venPaymentTypeDAO.findByPaymentTypeCode(paymentType.getPaymentTypeCode());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenPaymentTypeReferences::paymentTypeList = "
									+ paymentTypeList);
					if (paymentTypeList == null || (paymentTypeList.isEmpty())) {
						throw CommonUtil.logAndReturnException(new PaymentTypeNotFoundException(
								"Payment type does not exist", VeniceExceptionConstants.VEN_EX_400002)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenPaymentTypeReferences::paymentTypeList not empty");
						VenPaymentType venPaymentType = paymentTypeList.get(0);
						synchronizedPaymentTypeReferences.add(venPaymentType);
					}
				}			
			} // end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPaymentTypeReferences::returning synchronizedPaymentTypeReferences = "
				   + synchronizedPaymentTypeReferences.size());
		return synchronizedPaymentTypeReferences;
	}

}
