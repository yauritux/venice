package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenCountrySynchronizingError extends VeniceInternalException {

	private static final long serialVersionUID = 9067032077365147040L;

	public VenCountrySynchronizingError(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
