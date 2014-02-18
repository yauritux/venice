package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class CannotPersistOrderItemContactDetailException extends VeniceInternalException {

	private static final long serialVersionUID = 9012534327094440964L;

	public CannotPersistOrderItemContactDetailException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
