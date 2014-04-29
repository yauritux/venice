package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenPaymentStatusSynchronizingError extends VeniceInternalException {

	private static final long serialVersionUID = -8766125568182003855L;

	public VenPaymentStatusSynchronizingError(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
