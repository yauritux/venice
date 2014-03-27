package com.gdn.venice.finance.services;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.FinArFundsInReconRecord;

/**
 * 
 * @author yauritux
 *
 * This interface will be used as a contract-based service 
 * for all operations related to FinArFundsInReconRecord
 */
public interface FinArFundsInReconRecordService {

	public FinArFundsInReconRecord persist(FinArFundsInReconRecord reconRecord)
	   throws VeniceInternalException;
}
