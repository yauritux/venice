package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class InvalidOrderAmountException extends InvalidOrderException {

	public InvalidOrderAmountException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public InvalidOrderAmountException(String message, Throwable cause, VeniceExceptionConstants errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}
	
	private static final long serialVersionUID = -3127271129943881529L;

}
