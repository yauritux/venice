package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class ProductTypeNotFoundException extends VeniceInternalException {

	private static final long serialVersionUID = -4535214830188690164L;

	public ProductTypeNotFoundException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
