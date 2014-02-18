package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistOrderPaymentAllocationException extends VeniceInternalException {

	private static final long serialVersionUID = -3075453294918226902L;

	public CannotPersistOrderPaymentAllocationException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
