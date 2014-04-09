package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenPromotionSynchronizingError extends VeniceInternalException {

	private static final long serialVersionUID = 583322469379953618L;

	public VenPromotionSynchronizingError(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
