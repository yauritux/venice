package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class PaymentProcessorException extends VeniceInternalException {

	private static final long serialVersionUID = -7219195664811708409L;

	public PaymentProcessorException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
