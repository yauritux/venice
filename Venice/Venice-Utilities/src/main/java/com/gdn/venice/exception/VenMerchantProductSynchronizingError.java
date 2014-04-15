package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenMerchantProductSynchronizingError extends VeniceInternalException {

	private static final long serialVersionUID = -9222005314884860688L;

	public VenMerchantProductSynchronizingError(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
