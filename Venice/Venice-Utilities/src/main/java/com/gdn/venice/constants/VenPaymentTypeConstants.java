package com.gdn.venice.constants;

/**
 * 
 * @author yauritux
 *
 */
public enum VenPaymentTypeConstants {
	
	 VEN_PAYMENT_TYPE_CC(Constants.CC_ID, "CC"),
	 VEN_PAYMENT_TYPE_IB(Constants.IB_ID, "IB"),
	 VEN_PAYMENT_TYPE_VA(Constants.VA_ID, "VA"),
	 VEN_PAYMENT_TYPE_CS(Constants.CS_ID, "CS");
	 
	 private long id;
	 private String desc;
	 
	 private VenPaymentTypeConstants(long id, String desc) {
		 this.id = id;
		 this.desc = desc;
	 }
	 
	 public long id() {
		 return id;
	 }
	 
	 public String desc() {
		 return desc;
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
		 public static final int CC_ID = 0;
		 public static final int IB_ID = 1;
		 public static final int VA_ID = 2;
		 public static final int CS_ID = 3;		 
	 }
}