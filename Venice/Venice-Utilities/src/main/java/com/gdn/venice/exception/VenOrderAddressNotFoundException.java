package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenOrderAddressNotFoundException extends VeniceInternalException {

	private static final long serialVersionUID = -6280934908416302410L;

	public VenOrderAddressNotFoundException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
