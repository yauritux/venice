package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenOrderStatusDAO;
import com.gdn.venice.exception.CannotPersistOrderStatusHistoryException;
import com.gdn.venice.exception.OrderStatusNotFoundException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.OrderStatusService;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderStatus;
import com.gdn.venice.persistence.VenOrderStatusHistory;
import com.gdn.venice.persistence.VenOrderStatusHistoryPK;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderStatusServiceImpl implements OrderStatusService {

	@Autowired
	private VenOrderStatusDAO venOrderStatusDAO;	
	
	@Override
	public List<VenOrderStatus> synchronizeVenOrderStatusReferences(
			List<VenOrderStatus> orderStatusReferences) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderStatusReferences::BEGIN, orderStatusReferences=" + orderStatusReferences);
		
		List<VenOrderStatus> synchronizedOrderStatus = new ArrayList<VenOrderStatus>();
		
		if (orderStatusReferences != null) {
			for (VenOrderStatus orderStatus : orderStatusReferences) {
				if (orderStatus.getOrderStatusCode() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenOrderStatusReferences::Restricting VenOrderStatus... :" 
									+ orderStatus.getOrderStatusCode());

					/*
					VenOrderStatus venOrderStatus = venOrderStatusDAO.findByOrderStatusCode(orderStatus.getOrderStatusCode());
					if (venOrderStatus == null) {
						throw CommonUtil.logAndReturnException(new OrderStatusNotFoundException("Order status does not exist", 
								VeniceExceptionConstants.VEN_EX_000025)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
					} else {
						synchronizedOrderStatus.add(venOrderStatus);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenOrderStatusReferences::successfully added venOrderStatus into synchronizedOrderStatus");
					}
					*/
					VenOrderStatus venOrderStatus = venOrderStatusDAO.save(orderStatus);
					synchronizedOrderStatus.add(venOrderStatus);
				}			
			} // end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderStatusReferences::returning synchronizedOrderStatus = " + synchronizedOrderStatus.size());
		
		return synchronizedOrderStatus;
	}
}
