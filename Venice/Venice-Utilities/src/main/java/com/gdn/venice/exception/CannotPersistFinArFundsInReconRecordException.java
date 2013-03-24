package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistFinArFundsInReconRecordException extends VeniceInternalException {

	private static final long serialVersionUID = 9094998044682679827L;
	
	public CannotPersistFinArFundsInReconRecordException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
