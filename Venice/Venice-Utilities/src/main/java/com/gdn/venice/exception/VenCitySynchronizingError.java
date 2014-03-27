package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenCitySynchronizingError extends VeniceInternalException {

	private static final long serialVersionUID = -8391190718636571559L;

	public VenCitySynchronizingError(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
