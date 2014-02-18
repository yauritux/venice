package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class PaymentStatusNotFoundException extends VeniceInternalException {

	private static final long serialVersionUID = 7317775954415952786L;

	public PaymentStatusNotFoundException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
