package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CustomerNotFoundException extends InvalidOrderException {

	public CustomerNotFoundException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public CustomerNotFoundException(String message, Throwable cause, VeniceExceptionConstants errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}
	
	private static final long serialVersionUID = -5895412311867019406L;

}
