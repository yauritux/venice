package com.gdn.venice.fraud.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.FrdFraudCaseHistoryDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.fraud.services.FrdFraudCaseHistoryService;
import com.gdn.venice.persistence.FrdFraudCaseHistory;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class FrdFraudCaseHistoryServiceImpl implements FrdFraudCaseHistoryService {

	@Autowired
	private FrdFraudCaseHistoryDAO frdFraudCaseHistoryDAO;
	
	@Override
	public List<FrdFraudCaseHistory> findByFraudSuspicionCaseId(Long fraudCaseId)
			throws VeniceInternalException {
		List<FrdFraudCaseHistory> frdFraudCaseHistoryLst = new ArrayList<FrdFraudCaseHistory>();
		try {
			frdFraudCaseHistoryLst = frdFraudCaseHistoryDAO.findByFraudSuspicionCaseId(fraudCaseId);
		} catch (Exception e) {
			CommonUtil.logError(this.getClass().getCanonicalName(), e);
		}
		
		return frdFraudCaseHistoryLst;
	}

}
