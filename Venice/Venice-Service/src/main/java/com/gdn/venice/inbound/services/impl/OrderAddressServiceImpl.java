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
import com.gdn.venice.dao.VenOrderAddressDAO;
import com.gdn.venice.exception.CannotPersistOrderAddressException;
import com.gdn.venice.exception.VenOrderAddressNotFoundException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.OrderAddressService;
import com.gdn.venice.persistence.VenOrderAddress;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderAddressServiceImpl implements OrderAddressService {

	@Autowired
	private VenOrderAddressDAO venOrderAddressDAO;
	
	@PersistenceContext
	EntityManager em;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrderAddress persist(VenOrderAddress venOrderAddress)
			throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persist::BEGIN, venOrderAddress = " + venOrderAddress);
		
		VenOrderAddress persistedOrderAddress = null;
		
		if (venOrderAddress != null) {					
			if (!em.contains(venOrderAddress)) {
				//venOrderAddress is in detach mode, hence need to call save explicitly
				try {
					persistedOrderAddress = venOrderAddressDAO.save(venOrderAddress);
				} catch (Exception e) {
					CommonUtil.logAndReturnException(new CannotPersistOrderAddressException(
							"Cannot persist VenOrderAddress," + e, VeniceExceptionConstants.VEN_EX_000030)
					, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				}			
			}			
		} 
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persist::EOM, returning persistedOrderAddress = " + persistedOrderAddress);
		
		return persistedOrderAddress;
	}
	
	@Autowired
	public List<VenOrderAddress> findByVenOrderWcsOrderId(String wcsOrderId) {
		List<VenOrderAddress> orderAddresses = new ArrayList<VenOrderAddress>();
		
		try {
			orderAddresses = venOrderAddressDAO.findByVenOrderWcsOrderId(wcsOrderId);
		} catch (Exception e) {
			CommonUtil.logError(this.getClass().getCanonicalName(), e);
			CommonUtil.logAndReturnException(new VenOrderAddressNotFoundException(
					"Cannot found VenOrderAddress for wcsOrderId=" + wcsOrderId, 
					VeniceExceptionConstants.VEN_EX_000220), CommonUtil.getLogger(this.getClass().getCanonicalName()), 
					LoggerLevel.ERROR);
		}
		
		return orderAddresses;
	}

}
