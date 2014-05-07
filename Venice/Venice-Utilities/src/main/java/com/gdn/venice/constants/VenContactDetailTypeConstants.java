package com.gdn.venice.constants;

public enum VenContactDetailTypeConstants {
	
	VEN_CONTACT_DETAIL_ID_PHONE(0),
	VEN_CONTACT_DETAIL_ID_MOBILE(1),
	VEN_CONTACT_DETAIL_ID_EMAIL(3);
	
	private long id;
	
	private VenContactDetailTypeConstants(long id) {
		this.id = id;
	}
	
	public long id(){
		return this.id;
	}
}
