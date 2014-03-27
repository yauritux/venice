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
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenMerchant synchronizeVenMerchantData(VenMerchant venMerchant)
	   throws VeniceInternalException {
		VenMerchant merchant = venMerchant;
		if (venMerchant != null) {
			List<VenMerchant> merchantList = findByWcsMerchantId(venMerchant.getWcsMerchantId());
			if (merchantList != null && (!merchantList.isEmpty())) {
				merchant = merchantList.get(0);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantData::found venMerchant WCS Merchant ID = "  + merchant.getWcsMerchantId());	
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantData::found venMerchant Merchant ID = "  + merchant.getMerchantId());					
			} else {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantData::venMerchant is not listed in the DB, saving it");
				merchant = persist(venMerchant);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantData::successfully persisted venMerchant");
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenMerchantData::new venMerchant ID = " + merchant.getMerchantId());				
			}
		} 
		return merchant;
	}

	@Override
	public List<VenMerchant> synchronizeVenMerchantReferences(
			List<VenMerchant> merchantRefs) throws VeniceInternalException {

		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantReferences::BEGIN");
		
		List<VenMerchant> synchronizedMerchantReferences = new ArrayList<VenMerchant>();
		
		for (VenMerchant merchant : merchantRefs) {	
			VenMerchant synchMerchant = synchronizeVenMerchantData(merchant);
			synchronizedMerchantReferences.add(synchMerchant);
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenMerchantReferences::EOF, returning synchronizedMerchantReferences = "
				+ synchronizedMerchantReferences.size());
		return synchronizedMerchantReferences;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenMerchant persist(VenMerchant venMerchant)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persist::BEGIN, venMerchant = " + venMerchant);
		
		VenMerchant persistedVenMerchant = null;
		
		if (venMerchant != null) {
			if (!em.contains(venMerchant)) {
				// venMerchant is in detach mode, hence should call save explicitly as shown below
				try {
					persistedVenMerchant = venMerchantDAO.save(venMerchant);
				} catch (Exception e) {
					CommonUtil.logAndReturnException(new CannotPersistMerchantException(
							"Cannot persist VenMerchant," + e, VeniceExceptionConstants.VEN_EX_120001)
					, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				}
			}
		}
		
		persistedVenMerchant  = venMerchant;
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persist::EOM, returning persistedVenMerchant = " + persistedVenMerchant);
		return persistedVenMerchant;
	}

}
