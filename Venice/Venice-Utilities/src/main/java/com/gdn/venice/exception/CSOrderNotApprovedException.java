package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CSOrderNotApprovedException extends VeniceInternalException {
	
	private static final long serialVersionUID = -8537343031614644800L;

	public CSOrderNotApprovedException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}	
}
