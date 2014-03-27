package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistPartyAddressException extends VeniceInternalException {

	private static final long serialVersionUID = 8947280481847061157L;

	public CannotPersistPartyAddressException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
