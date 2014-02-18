package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistOrderItemAddressException extends VeniceInternalException {
	
	private static final long serialVersionUID = -7429109518434958L;

	public CannotPersistOrderItemAddressException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
