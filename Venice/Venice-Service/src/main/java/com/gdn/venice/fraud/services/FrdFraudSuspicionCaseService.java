package com.gdn.venice.fraud.services;

import com.gdn.venice.persistence.FrdFraudSuspicionCase;

/**
 * 
 * @author yauritux
 *
 */
public interface FrdFraudSuspicionCaseService {
	
	public FrdFraudSuspicionCase findByPK(Long id);
}
