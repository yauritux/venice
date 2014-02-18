package com.gdn.venice.inbound.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenOrderItemContactDetailDAO;
import com.gdn.venice.exception.CannotPersistOrderItemContactDetailException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.OrderItemContactDetailService;
import com.gdn.venice.persistence.VenOrderItemContactDetail;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderItemContactDetailServiceImpl implements OrderItemContactDetailService {

	@Autowired
	private VenOrderItemContactDetailDAO venOrderItemContactDetailDAO;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrderItemContactDetail persist(
			VenOrderItemContactDetail venOrderItemContactDetail)
			throws VeniceInternalException {
		
		VenOrderItemContactDetail persistedOrderItemContactDetail = null;
		
		try {
			persistedOrderItemContactDetail = venOrderItemContactDetailDAO.save(venOrderItemContactDetail);
		} catch (Exception e) {
			CommonUtil.logAndReturnException(new CannotPersistOrderItemContactDetailException(
					"Cannot persist VenOrderItemContactDetail, " + e
					, VeniceExceptionConstants.VEN_EX_000028)
			     , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}
		
		return persistedOrderItemContactDetail;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenOrderItemContactDetail> persist(
		List<VenOrderItemContactDetail> venOrderItemContactDetails)
			throws VeniceInternalException {
		List<VenOrderItemContactDetail> persistedOrderItemContactDetails = null;
		
		try {
			persistedOrderItemContactDetails = venOrderItemContactDetailDAO.save(venOrderItemContactDetails);
		} catch (Exception e) {
			CommonUtil.logAndReturnException(new CannotPersistOrderItemContactDetailException
					("Cannot persist VenOrderItemContactDetail, " + e
					, VeniceExceptionConstants.VEN_EX_000028)
			     , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}
		
		return persistedOrderItemContactDetails;
	}

}
