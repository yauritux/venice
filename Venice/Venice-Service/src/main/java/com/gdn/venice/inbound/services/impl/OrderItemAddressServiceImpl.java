package com.gdn.venice.inbound.services.impl;

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
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrderItemAddress persist(VenOrderItemAddress venOrderItemAddress) 
	  throws VeniceInternalException {
		VenOrderItemAddress persistedOrderItemAddress = null;
		try {
		   persistedOrderItemAddress = venOrderItemAddressDAO.save(venOrderItemAddress);
		} catch (Exception e) {
			CommonUtil.logAndReturnException(new CannotPersistOrderItemAddressException(
					"Cannot persist VenOrderItemAddress " + e, VeniceExceptionConstants.VEN_EX_000027)
			    , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}
		
		return persistedOrderItemAddress;
	}

}
