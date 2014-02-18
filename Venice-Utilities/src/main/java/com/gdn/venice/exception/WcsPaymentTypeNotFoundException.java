package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class WcsPaymentTypeNotFoundException extends VeniceInternalException {

	private static final long serialVersionUID = -1471194745788564908L;

	public WcsPaymentTypeNotFoundException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
