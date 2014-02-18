package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VAOrderNotApprovedException extends VeniceInternalException {

	private static final long serialVersionUID = -5664508494123584646L;

	public VAOrderNotApprovedException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}	
}
