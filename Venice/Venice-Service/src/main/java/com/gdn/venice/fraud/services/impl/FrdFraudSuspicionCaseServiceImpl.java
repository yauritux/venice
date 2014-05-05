package com.gdn.venice.fraud.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.FrdFraudSuspicionCaseDAO;
import com.gdn.venice.fraud.services.FrdFraudSuspicionCaseService;
import com.gdn.venice.persistence.FrdFraudSuspicionCase;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class FrdFraudSuspicionCaseServiceImpl implements FrdFraudSuspicionCaseService {
	
	@Autowired
	private FrdFraudSuspicionCaseDAO frdFraudSuspicionCaseDAO;

	@Override
	public FrdFraudSuspicionCase findByPK(Long id) {
		return frdFraudSuspicionCaseDAO.findByFraudSuspicionCaseId(id);
	}

}
