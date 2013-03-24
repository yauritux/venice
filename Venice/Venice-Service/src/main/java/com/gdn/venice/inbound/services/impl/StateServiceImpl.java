package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.VenStateDAO;
import com.gdn.venice.inbound.services.StateService;
import com.gdn.venice.persistence.VenState;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class StateServiceImpl implements StateService {
	
	@Autowired
	private VenStateDAO venStateDAO;
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenState synchronizeVenState(VenState venState) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenState::BEGIN,venState = " + venState);
		VenState synchState = venState;
		
		if (venState != null && venState.getStateCode() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenState::stateCode=" + venState.getStateCode());
			List<VenState> stateList = venStateDAO.findByStateCode(venState.getStateCode());
			if (stateList == null || stateList.isEmpty()) {
				if (!em.contains(venState)) {
					//venState in detach mode, hence need to explicitly call save
					synchState = venStateDAO.save(venState);
				}
			} else {
				synchState = stateList.get(0);
			}
		}
		
		return synchState;
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenState> synchronizeVenStateReferences(
			List<VenState> stateReferences) {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenStateReferences::BEGIN,stateReferences=" + stateReferences);
		
		List<VenState> synchronizedStateReferences = new ArrayList<VenState>();
		
		if (stateReferences != null) {
			for (VenState state : stateReferences) {
				synchronizedStateReferences.add(synchronizeVenState(state));
			} //end of 'for'
		}	
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenStateReferences::EOM, returning synchronizedStateReferences="
				+ synchronizedStateReferences.size());
		return synchronizedStateReferences;
	}

}
