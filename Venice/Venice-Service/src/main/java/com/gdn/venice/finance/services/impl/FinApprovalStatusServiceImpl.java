package com.gdn.venice.finance.services.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.gdn.venice.constants.FinApprovalStatusConstants;
import com.gdn.venice.finance.services.FinApprovalStatusService;
import com.gdn.venice.persistence.FinApprovalStatus;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class FinApprovalStatusServiceImpl implements FinApprovalStatusService {

	@Override
	public FinApprovalStatus createNew(
			FinApprovalStatusConstants finApprovalStatusConstants) {
		FinApprovalStatus finApprovalStatus = new FinApprovalStatus();
		finApprovalStatus.setApprovalStatusId(finApprovalStatusConstants.id());
		return finApprovalStatus;
	}

	
}
