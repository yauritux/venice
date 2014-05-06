package com.gdn.venice.constants;

/**
 * 
 * @author yauritux
 *
 */
public enum VenPromotionTypeConstants {
	
	VEN_PROMOTION_TYPE_VOUCHER(Constants.VOUCHER_ID),
	VEN_PROMOTION_TYPE_VOUCHERCS(Constants.VOUCHERCS_ID),
    VEN_PROMOTION_TYPE_NONVOUCHER(Constants.NONVOUCHER_ID),
	VEN_PROMOTION_TYPE_FREESHIPPING(Constants.FREESHIPPING_ID);
	
	private long id;
	
	private VenPromotionTypeConstants(long id) {
		this.id = id;
	}
	
	public long id() {
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
		public static final int VOUCHER_ID = 1;
		public static final int VOUCHERCS_ID = 2;
		public static final int NONVOUCHER_ID = 3;
		public static final int FREESHIPPING_ID = 4;
	}
}
