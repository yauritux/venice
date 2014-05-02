package com.gdn.venice.constants;

/**
 * 
 * @author yauritux
 *
 */
public enum FinArFundsInActionAppliedConstants {
	
	FIN_AR_FUNDS_IN_ACTION_APPLIED_NONE(Constants.NONE_ID),
	FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_CUSTOMER(Constants.REFUNDED_CUSTOMER_ID),
	FIN_AR_FUNDS_IN_ACTION_APPLIED_ALLOCATED(Constants.ALLOCATED_ID),
	FIN_AR_FUNDS_IN_ACTION_APPLIED_REMOVED(Constants.REMOVED_ID),
	FIN_AR_FUNDS_IN_ACTION_APPLIED_REFUNDED_BANK(Constants.REFUNDED_BANK_ID),
	FIN_AR_FUNDS_IN_ACTION_APPLIED_RECEIVE(Constants.RECEIVE_ID);
	
	private long id;
	
	public long id() {
		return id;
	}
	
	private FinArFundsInActionAppliedConstants(long id) {
		this.id = id;
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
		public static final int NONE_ID = 0;
		public static final int REFUNDED_CUSTOMER_ID = 1;
		public static final int ALLOCATED_ID = 2;
		public static final int REMOVED_ID = 3;
		public static final int REFUNDED_BANK_ID = 4;
		public static final int RECEIVE_ID = 5;
	}
}
