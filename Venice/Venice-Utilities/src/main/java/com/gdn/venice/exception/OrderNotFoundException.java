package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class OrderNotFoundException extends VeniceInternalException {

	private static final long serialVersionUID = 976693994266479089L;

	public OrderNotFoundException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
