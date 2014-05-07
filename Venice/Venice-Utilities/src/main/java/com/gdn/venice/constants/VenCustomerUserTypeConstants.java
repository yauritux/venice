package com.gdn.venice.constants;

public enum VenCustomerUserTypeConstants {
	
	USER_TYPE_REGISTERED("Registered shopper"),
	USER_TYPE_UNREGISTERED("Unregistered shopper");
	
	private String value;
	
	private VenCustomerUserTypeConstants(String value){
		this.value = value;
	}
	
	public String value(){
		return this.value;
	}
}
