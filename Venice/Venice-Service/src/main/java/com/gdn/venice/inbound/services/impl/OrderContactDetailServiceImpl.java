package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.VenOrderContactDetailDAO;
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
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrderContactDetail persist(VenOrderContactDetail venOrderContactDetail) 
	   throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persist::BEGIN, venOrderContactDetail = " + venOrderContactDetail);
		
		if (em.contains(venOrderContactDetail)) {
			em.detach(venOrderContactDetail);
		}
		return venOrderContactDetail;
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenOrderContactDetail> persistVenOrderContactDetails(
			List<VenOrderContactDetail> orderContactDetails)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistVenOrderContactDetails::BEGIN, orderContactDetails = " + orderContactDetails);
		
		List<VenOrderContactDetail> persistedOrderContactDetails = new ArrayList<VenOrderContactDetail>();
		
		if (orderContactDetails != null && (!(orderContactDetails.isEmpty()))) {
			/*
			try {
				persistedOrderContactDetails = venOrderContactDetailDAO.save(orderContactDetails);
			} catch (Exception e) {
				CommonUtil.logAndReturnException(new CannotPersistOrderContactDetailException(
						"Cannot persist VenOrderContactDetail, " + e, VeniceExceptionConstants.VEN_EX_000029)
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
			*/
			for (VenOrderContactDetail ocd : orderContactDetails) {
				persistedOrderContactDetails.add(persist(ocd));
			}

			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistVenOrderContactDetails::EOM, returning persistedOrderContactDetails = "
							+ persistedOrderContactDetails.size());
		}
		
		return persistedOrderContactDetails;
	}
}
