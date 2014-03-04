package com.gdn.venice.constants;

/**
 * 
 * @author yauritux
 *
 */
public enum VenPartyTypeConstants {

	 VEN_PARTY_TYPE_BANK(0, "Bank"),
	 VEN_PARTY_TYPE_MERCHANT(1, "Merchant"),
	 VEN_PARTY_TYPE_LOGISTICS(2, "Logistics Partner"),
	 VEN_PARTY_TYPE_RECIPIENT(3, "Recipient"),
	 VEN_PARTY_TYPE_CUSTOMER(4, "Customer"),
	 VEN_PARTY_TYPE_USER(5, "User");
	 
	 private long code;
	 private String description;
	 
	 private VenPartyTypeConstants(long code, String description) {
		 this.code = code;
		 this.description = description;
	 }
	 
	 public long code() {
		 return code;
	 }
	 
	 public String description() {
		 return description;
	 }
	 
	 @Override
	 public String toString() {
		 return description();
	 }
}
