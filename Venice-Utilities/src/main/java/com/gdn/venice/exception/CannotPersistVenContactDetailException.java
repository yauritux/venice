package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistVenContactDetailException extends VeniceInternalException {

	private static final long serialVersionUID = 4887378582499541847L;

	public CannotPersistVenContactDetailException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
