package com.gdn.venice.inbound.services.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenMerchantDAO;
import com.gdn.venice.exception.CannotPersistMerchantException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.MerchantService;
import com.gdn.venice.persistence.VenMerchant;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class MerchantServiceImpl implements MerchantService {
	
	@Autowired
	private VenMerchantDAO venMerchantDAO;
	
	@PersistenceContext
	private EntityManager em;

	@Override
	public List<VenMerchant> findByWcsMerchantId(String wcsMerchantId) {
		return venMerchantDAO.findByWcsMerchantId(wcsMerchantId);
	}

	@Override
	public List<VenMerchant> synchronizeVenMerchantReferences(
			List<VenMerchant> merchantRefs) throws VeniceInternalException {
		// TODO Auto-generated method stub
		return null; // homework, discuss with the team whether this should be implemented or not
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenMerchant persist(VenMerchant venMerchant)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persist::BEGIN, venMerchant = " + venMerchant);
		
		VenMerchant persistedVenMerchant = null;
		
		if (venMerchant != null) {
			/*
			if (!em.contains(venMerchant)) {
				// venMerchant is in detach mode, hence should call save explicitly as shown below
				try {
					persistedVenMerchant = venMerchantDAO.save(venMerchant);
				} catch (Exception e) {
					CommonUtil.logAndReturnException(new CannotPersistMerchantException(
							"Cannot persist VenMerchant," + e, VeniceExceptionConstants.VEN_EX_120001)
					, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				}
			} else {
				persistedVenMerchant = venMerchant;
			}
			*/
			if (em.contains(venMerchant)) {
				em.detach(venMerchant);
			}
			persistedVenMerchant = venMerchant;
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persist::EOM, returning persistedVenMerchant = " + persistedVenMerchant);
		return persistedVenMerchant;
	}

}
