package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistOrderItemException extends VeniceInternalException {
	
	private static final long serialVersionUID = -4023521376333769715L;

	public CannotPersistOrderItemException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
