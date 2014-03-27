package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class OrderCreationFailedException extends VeniceInternalException {

	private static final long serialVersionUID = 4069191181014069611L;

	public OrderCreationFailedException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
