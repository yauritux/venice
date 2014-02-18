package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class LogLogisticServiceNotFoundException extends VeniceInternalException {

	private static final long serialVersionUID = -1786900906062611646L;

	public LogLogisticServiceNotFoundException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
