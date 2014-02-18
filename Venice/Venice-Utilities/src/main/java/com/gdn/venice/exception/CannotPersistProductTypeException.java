package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistProductTypeException extends VeniceInternalException {

	private static final long serialVersionUID = 1645204783333289099L;

	public CannotPersistProductTypeException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
