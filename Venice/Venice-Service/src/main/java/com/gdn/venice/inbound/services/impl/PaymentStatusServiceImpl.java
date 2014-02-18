package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenPaymentStatusDAO;
import com.gdn.venice.exception.PaymentStatusNotFoundException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.PaymentStatusService;
import com.gdn.venice.persistence.VenPaymentStatus;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class PaymentStatusServiceImpl implements PaymentStatusService {

	@Autowired
	private VenPaymentStatusDAO venPaymentStatusDAO;
	
	@Override
	public List<VenPaymentStatus> synchronizeVenPaymentStatusReferences(
			List<VenPaymentStatus> paymentStatusReferences)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPaymentStatusReferences::BEGIN,paymentStatusReferences="
				   + paymentStatusReferences);
		//if (paymentStatusReferences == null || paymentStatusReferences.isEmpty()) return null;
		
		List<VenPaymentStatus> synchronizedPaymentStatusReferences 
		   = new ArrayList<VenPaymentStatus>();
		
		if (paymentStatusReferences != null) {
			for (VenPaymentStatus paymentStatus : paymentStatusReferences) {
				if (paymentStatus.getPaymentStatusCode() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenPaymentStatusReferences::Restricting VenPaymentStatus... :" 
									+ paymentStatus.getPaymentStatusCode());
					List<VenPaymentStatus> paymentStatusList = venPaymentStatusDAO.findByPaymentStatusCode(paymentStatus.getPaymentStatusCode());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenPaymentStatusReferences::paymentStatusList = " + paymentStatusList);
					if (paymentStatusList == null || (paymentStatusList.isEmpty())) {
						throw CommonUtil.logAndReturnException(new PaymentStatusNotFoundException(
								"Payment status does not exist", VeniceExceptionConstants.VEN_EX_400001)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenPaymentStatusReferences::adding paymentstatus into synchronizedPaymentStatusReferences");
						VenPaymentStatus venPaymentStatus = paymentStatusList.get(0);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenPaymentStatusReferences::paymentStatus=" + venPaymentStatus);
						synchronizedPaymentStatusReferences.add(venPaymentStatus);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenPaymentStatusReferences::successfully added venPaymentStatus into synchronizedPaymentStatusReferences");
					}
				}

			} // end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPaymentStatusReferences::returning synchronizedPaymentStatusReferences = "
				   + synchronizedPaymentStatusReferences.size());
		return synchronizedPaymentStatusReferences;
	}

}
