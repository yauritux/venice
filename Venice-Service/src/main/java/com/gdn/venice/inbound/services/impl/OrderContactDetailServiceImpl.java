package com.gdn.venice.inbound.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenOrderContactDetailDAO;
import com.gdn.venice.exception.CannotPersistOrderContactDetailException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.OrderContactDetailService;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderContactDetailServiceImpl implements OrderContactDetailService {

	@Autowired
	private VenOrderContactDetailDAO venOrderContactDetailDAO;
	
	@Override
	public List<VenOrderContactDetail> persistVenOrderContactDetails(
			List<VenOrderContactDetail> orderContactDetails)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistVenOrderContactDetails::BEGIN, orderContactDetails = " + orderContactDetails);
		
		List<VenOrderContactDetail> persistedOrderContactDetails = null;
		
		if (orderContactDetails != null && (!(orderContactDetails.isEmpty()))) {
			try {
				persistedOrderContactDetails = venOrderContactDetailDAO.save(orderContactDetails);
			} catch (Exception e) {
				CommonUtil.logAndReturnException(new CannotPersistOrderContactDetailException(
						"Cannot persist VenOrderContactDetail, " + e, VeniceExceptionConstants.VEN_EX_000029)
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistVenOrderContactDetails::EOM, returning persistedOrderContactDetails = "
							+ persistedOrderContactDetails.size());
		}
		
		return persistedOrderContactDetails;
	}
}
