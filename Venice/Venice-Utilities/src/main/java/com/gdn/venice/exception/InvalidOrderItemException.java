package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class InvalidOrderItemException extends InvalidOrderException {

	public InvalidOrderItemException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public InvalidOrderItemException(String message, VeniceExceptionConstants errorCode
			, Throwable cause) {
		super(message, cause, errorCode);
	}
	
	private static final long serialVersionUID = 736569445215351357L;	
	
}
