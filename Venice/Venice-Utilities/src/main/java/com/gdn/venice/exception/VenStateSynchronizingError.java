package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class VenStateSynchronizingError extends VeniceInternalException {

	private static final long serialVersionUID = 1216568209746267289L;

	public VenStateSynchronizingError(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
