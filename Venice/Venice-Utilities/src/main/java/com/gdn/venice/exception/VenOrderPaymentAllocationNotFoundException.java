package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenOrderPaymentAllocationNotFoundException extends VeniceInternalException {

	private static final long serialVersionUID = -6802674696400199777L;

	public VenOrderPaymentAllocationNotFoundException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
