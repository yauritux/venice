package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistOrderAddressException extends VeniceInternalException {
	
	private static final long serialVersionUID = -8544455056024625955L;

	public CannotPersistOrderAddressException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
