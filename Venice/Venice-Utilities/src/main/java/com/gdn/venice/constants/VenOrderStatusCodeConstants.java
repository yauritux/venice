package com.gdn.venice.constants;

public enum VenOrderStatusCodeConstants {
	
	 VEN_ORDER_STATUS_SF("SF"),
	 VEN_ORDER_STATUS_FC("FC"),
	 VEN_ORDER_STATUS_FP("FP");
	 
	 private String statusCode;
	 
	 private VenOrderStatusCodeConstants(String statusCode){
		 this.statusCode = statusCode;
	 }
	 
	 public String code(){
		 return this.statusCode; 
	 }
	
}
