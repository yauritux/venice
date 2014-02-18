package com.gdn.venice.constants;

public enum LogReconActivityRecordResultConstants {
	
	 GDN_REFERENCE_DOES_NOT_EXIST(0),
	 PICKUP_DATE_MISMATCH(1),
	 SETTLEMENT_CODE_MISMATCH(2),
	 SERVICE_MISMATCH(3),
	 RECIPIENT_MISMATCH(4),
	 WEIGHT_MISMATCH(5);
	 
	 private long id;
	 
	 public long id() {
		 return id;
	 }
	 
	 private LogReconActivityRecordResultConstants(long id) {
		 this.id = id;
	 }
	 
}
