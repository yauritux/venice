package com.gdn.venice.inbound.services.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenOrderItemAddressDAO;
import com.gdn.venice.exception.CannotPersistOrderItemAddressException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.OrderItemAddressService;
import com.gdn.venice.persistence.VenOrderItemAddress;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderItemAddressServiceImpl implements OrderItemAddressService {

	@Autowired
	private VenOrderItemAddressDAO venOrderItemAddressDAO;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrderItemAddress persist(VenOrderItemAddress venOrderItemAddress) 
	  throws VeniceInternalException {
		VenOrderItemAddress persistedOrderItemAddress = venOrderItemAddress;
		if (venOrderItemAddress != null && venOrderItemAddress.getOrderItemAddressId() == null) {
			if (!em.contains(venOrderItemAddress)) {
				// venOrderItemAddress is not in attach mode, hence should be attached by calling save explicitly
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persist::calling venOrderItemAddressDAO.save explicitly");
				try {
					persistedOrderItemAddress = venOrderItemAddressDAO.save(venOrderItemAddress);
				} catch (Exception e) {
					CommonUtil.logError(this.getClass().getCanonicalName(), e);
					throw CommonUtil.logAndReturnException(new CannotPersistOrderItemAddressException(
							"Cannot persist VenOrderItemAddress " + e, VeniceExceptionConstants.VEN_EX_000027)
					, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				}
			}
		}
	
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persist::returning persistedOrderItemAddress = " + persistedOrderItemAddress);
		return persistedOrderItemAddress;
	}

}
