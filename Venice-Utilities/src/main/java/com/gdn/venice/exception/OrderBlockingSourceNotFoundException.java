package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class OrderBlockingSourceNotFoundException extends VeniceInternalException {

	private static final long serialVersionUID = 7075983838030761201L;

	public OrderBlockingSourceNotFoundException(
			String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
