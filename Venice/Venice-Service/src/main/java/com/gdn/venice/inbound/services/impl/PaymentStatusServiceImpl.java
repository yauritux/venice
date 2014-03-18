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
	public VenPaymentStatus synchronizeVenPaymentStatus(VenPaymentStatus venPaymentStatus)
	  throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPaymentStatus::BEGIN, venPaymentStatus = " + venPaymentStatus);
		VenPaymentStatus synchPaymentStatus = venPaymentStatus;
		if (venPaymentStatus != null && venPaymentStatus.getPaymentStatusCode() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenPaymentStatus::venPaymentStatus code = " + venPaymentStatus.getPaymentStatusCode());
			List<VenPaymentStatus> paymentStatusList = venPaymentStatusDAO.findByPaymentStatusCode(venPaymentStatus.getPaymentStatusCode());
			if (paymentStatusList == null || paymentStatusList.isEmpty()) {
				throw CommonUtil.logAndReturnException(new PaymentStatusNotFoundException(
						"Payment Status does not exist!", VeniceExceptionConstants.VEN_EX_400001)
				   , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			} else {
				synchPaymentStatus = paymentStatusList.get(0);
			}
			return synchPaymentStatus;
		}
		return synchPaymentStatus;
	}
	
	@Override
	public List<VenPaymentStatus> synchronizeVenPaymentStatusReferences(
			List<VenPaymentStatus> paymentStatusReferences)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPaymentStatusReferences::BEGIN,paymentStatusReferences="
				   + paymentStatusReferences);
		
		List<VenPaymentStatus> synchronizedPaymentStatusReferences 
		   = new ArrayList<VenPaymentStatus>();
		
		if (paymentStatusReferences != null) {
			try {
				for (VenPaymentStatus paymentStatus : paymentStatusReferences) {
					synchronizedPaymentStatusReferences.add(synchronizeVenPaymentStatus(paymentStatus));
				} // end of 'for'
			} catch (VeniceInternalException e) {
				CommonUtil.logAndReturnException(e, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			} catch (Exception e) {
				throw CommonUtil.logAndReturnException(new PaymentStatusNotFoundException(
						"Payment Status does not exist!", VeniceExceptionConstants.VEN_EX_400001)
				   , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);				
			}
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPaymentStatusReferences::returning synchronizedPaymentStatusReferences = "
				   + synchronizedPaymentStatusReferences.size());
		return synchronizedPaymentStatusReferences;
	}

}
