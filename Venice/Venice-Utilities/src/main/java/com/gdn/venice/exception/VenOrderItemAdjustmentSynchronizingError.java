package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenOrderItemAdjustmentSynchronizingError extends VeniceInternalException {

	private static final long serialVersionUID = 1473072771482371306L;

	public VenOrderItemAdjustmentSynchronizingError(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
