package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenPromotionQueryException extends VeniceInternalException {

	private static final long serialVersionUID = -5600096759652880463L;

	public VenPromotionQueryException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
