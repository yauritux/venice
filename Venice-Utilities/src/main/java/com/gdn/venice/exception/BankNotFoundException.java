package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class BankNotFoundException extends VeniceInternalException {

	private static final long serialVersionUID = 2884250194314196752L;
	
	public BankNotFoundException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

}
