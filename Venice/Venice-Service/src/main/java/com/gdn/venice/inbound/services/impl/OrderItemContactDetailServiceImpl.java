package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrderItemContactDetail persist(
			VenOrderItemContactDetail venOrderItemContactDetail)
			throws VeniceInternalException {
		
		VenOrderItemContactDetail persistedOrderItemContactDetail = null;
		
		/*
		if (!em.contains(venOrderItemContactDetail)) {
			// venOrderItemContactDetail is in detach mode, hence should call save explicitly to make it attached
			try {
				persistedOrderItemContactDetail = venOrderItemContactDetailDAO.save(venOrderItemContactDetail);
			} catch (Exception e) {
				CommonUtil.logAndReturnException(new CannotPersistOrderItemContactDetailException(
						"Cannot persist VenOrderItemContactDetail, " + e
						, VeniceExceptionConstants.VEN_EX_000028)
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		} else {
			persistedOrderItemContactDetail = venOrderItemContactDetail;
		}
		*/
		if (em.contains(venOrderItemContactDetail)) {
			em.detach(venOrderItemContactDetail);
		}
		persistedOrderItemContactDetail = venOrderItemContactDetail;
		
		return persistedOrderItemContactDetail;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenOrderItemContactDetail> persist(
		List<VenOrderItemContactDetail> venOrderItemContactDetails)
			throws VeniceInternalException {
		List<VenOrderItemContactDetail> persistedOrderItemContactDetails = new ArrayList<VenOrderItemContactDetail>();
				
		/*
		try {
			persistedOrderItemContactDetails = venOrderItemContactDetailDAO.save(venOrderItemContactDetails);
		} catch (Exception e) {
			CommonUtil.logAndReturnException(new CannotPersistOrderItemContactDetailException
					("Cannot persist VenOrderItemContactDetail, " + e
					, VeniceExceptionConstants.VEN_EX_000028)
			     , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}
		*/
		for (VenOrderItemContactDetail itemContactDetail : venOrderItemContactDetails) {
			persistedOrderItemContactDetails.add(persist(itemContactDetail));
		}
		
		return persistedOrderItemContactDetails;
	}

}
