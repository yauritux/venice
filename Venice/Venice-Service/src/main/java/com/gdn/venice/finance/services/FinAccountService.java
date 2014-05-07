package com.gdn.venice.finance.services;

import com.gdn.venice.exception.VeniceInternalException;


/**
 * 
 * @author yauritux
 *
 */
public interface FinAccountService {

	public long getAccountNumberBank(long paymentReportTypeId)
	    throws VeniceInternalException;
}
