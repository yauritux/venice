package com.gdn.venice.fraud.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.FrdFraudCaseHistory;

/**
 * 
 * @author yauritux
 *
 */
public interface FrdFraudCaseHistoryService {

	public List<FrdFraudCaseHistory> findByFraudSuspicionCaseId(Long fraudCaseId)
	   throws VeniceInternalException;
}
