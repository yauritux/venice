package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistMerchantException extends VeniceInternalException {

	private static final long serialVersionUID = 8293391744410140233L;

	public CannotPersistMerchantException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
