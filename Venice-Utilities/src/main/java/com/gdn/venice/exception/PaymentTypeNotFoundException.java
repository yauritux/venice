package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class PaymentTypeNotFoundException extends VeniceInternalException {

	private static final long serialVersionUID = 6109161574860528042L;

	public PaymentTypeNotFoundException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
