package com.gdn.venice.exception;

import com.gdn.venice.constants.VeniceExceptionConstants;

/**
 * 
 * @author yauritux
 *
 */
public class OrderStatusNotFoundException extends VeniceInternalException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3492714278634619005L;
	
	public OrderStatusNotFoundException(String message
			, VeniceExceptionConstants errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
}
