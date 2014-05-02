package com.gdn.venice.constants;

/**
 * 
 * @author yauritux
 *
 */
public enum FinApprovalStatusConstants {
	
	FIN_APPROVAL_STATUS_NEW(Constants.NEW_ID),
	FIN_APPROVAL_STATUS_SUBMITTED(Constants.SUBMITTED_ID),
	FIN_APPROVAL_STATUS_APPROVED(Constants.APPROVED_ID),
	FIN_APPROVAL_STATUS_REJECTED(Constants.REJECTED_ID);
	
	private long id;
	
	public Long id() {
		return id;
	}
	
	private FinApprovalStatusConstants(long id) {
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
		public static final int NEW_ID = 0;
		public static final int SUBMITTED_ID = 1;
		public static final int APPROVED_ID = 2;
		public static final int REJECTED_ID = 3;
	}

}
