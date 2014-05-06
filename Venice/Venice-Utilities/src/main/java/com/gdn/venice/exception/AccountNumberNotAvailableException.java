package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class AccountNumberNotAvailableException extends VeniceInternalException {

	private static final long serialVersionUID = 183741389812903654L;

	public AccountNumberNotAvailableException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
