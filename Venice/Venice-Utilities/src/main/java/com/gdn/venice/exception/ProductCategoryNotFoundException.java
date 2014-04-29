package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class ProductCategoryNotFoundException extends VeniceInternalException {
	
	private static final long serialVersionUID = -3778446722488344655L;

	public ProductCategoryNotFoundException(String message, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

}
