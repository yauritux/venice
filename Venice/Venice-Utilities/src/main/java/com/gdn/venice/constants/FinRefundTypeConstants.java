package com.gdn.venice.constants;

/**
 * 
 * @author yauritux
 *
 */
public enum FinRefundTypeConstants {

	FIN_REFUND_TYPE_CUSTOMER(Constants.CUSTOMER_ID), 
	FIN_REFUND_TYPE_BANK(Constants.BANK_ID);	
	
	private int id;
	
	private FinRefundTypeConstants(int id) {
		this.id = id;
	}
	
	public int id() {
		return id;
	}
	
	 /** 
	  * This static inner class is used to address the problem in Java 5 Annotation 
	  * (i.e. the value for annotation must be a constant expression)
	  * 
	  * @author yauritux
	  * @since 3.1.19
	  *
	  */			
	public static class Constants {
		public static final int CUSTOMER_ID = 0;
		public static final int BANK_ID = 1;
	}
}
