package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistOrderItemAdjustmentsException extends VeniceInternalException {

	private static final long serialVersionUID = -6347991122205643516L;

	public CannotPersistOrderItemAdjustmentsException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
