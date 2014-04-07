package com.gdn.venice.constants;

public enum VenOrderPaymentConstants {

	E_COMMERCE_INDICATOR_5("05"),
	E_COMMERCE_INDICATOR_7("07");
	
	private String value;
	
	private VenOrderPaymentConstants(String code){
		value = code;
	}
	
	public String value(){
		return value;
	}
}
