package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistOrderContactDetailException extends VeniceInternalException {

	private static final long serialVersionUID = -6197138566130613614L;

	public CannotPersistOrderContactDetailException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
