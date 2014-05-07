package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenOrderStatusSynchronizingError extends VeniceInternalException {
	
	private static final long serialVersionUID = 807280944107748374L;

	public VenOrderStatusSynchronizingError(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
