package com.gdn.venice.finance.services;

import com.gdn.venice.constants.FinApprovalStatusConstants;
import com.gdn.venice.persistence.FinApprovalStatus;

/**
 * 
 * @author yauritux
 *
 */
public interface FinApprovalStatusService {

	public FinApprovalStatus createNew(FinApprovalStatusConstants finApprovalStatusConstants);
}
