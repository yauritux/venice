package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class MerchantPartySynchFailedException extends VeniceInternalException {

	private static final long serialVersionUID = -7426566634333300784L;

	public MerchantPartySynchFailedException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
