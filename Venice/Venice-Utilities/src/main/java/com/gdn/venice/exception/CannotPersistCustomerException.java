package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistCustomerException extends VeniceInternalException {

	private static final long serialVersionUID = 7645669479591942026L;

	public CannotPersistCustomerException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
