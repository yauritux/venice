package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenContactDetailTypeSynchronizingError extends VeniceInternalException {

	private static final long serialVersionUID = 3369427627863364102L;

	public VenContactDetailTypeSynchronizingError(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
