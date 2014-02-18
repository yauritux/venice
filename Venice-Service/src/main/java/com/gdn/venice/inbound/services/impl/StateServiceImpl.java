package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public List<VenState> synchronizeVenStateReferences(
			List<VenState> stateReferences) {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenStateReferences::BEGIN,stateReferences=" + stateReferences);
		//if (stateReferences == null || stateReferences.size() == 0) return null;
		
		List<VenState> synchronizedStateReferences = new ArrayList<VenState>();
		
		if (stateReferences != null) {
			for (VenState state : stateReferences) {
				if (state.getStateCode() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenStateReferences::Synchronizing VenState... :" 
									+ state.getStateCode());
					List<VenState> stateList = venStateDAO.findByStateCode(state.getStateCode());
					if (stateList == null || stateList.isEmpty()) {
						VenState venState = venStateDAO.save(state);
						synchronizedStateReferences.add(venState);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenStateReferences::successfully added venState into synchronizedStateReferences");
					} else {
						VenState venState = stateList.get(0);
						synchronizedStateReferences.add(venState);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeReferenceData::successfully added venState into synchronizedStateReferences");
					}
				}		
			} //end of 'for'
		}	
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenStateReferences::EOM, returning synchronizedStateReferences="
				+ synchronizedStateReferences.size());
		return synchronizedStateReferences;
	}

}
