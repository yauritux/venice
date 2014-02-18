package com.gdn.venice.constants;

public enum LogActionAppliedConstants {
	
	 VENICE_DATA_APPLIED(0),
	 PROVIDER_DATA_APPLIED(1),
	 MANUAL_DATA_APPLIED(2),
	 IGNORED(3);
	 
	 private long id;
	 
	 public long id() {
		 return id;
	 }
	 
	 private LogActionAppliedConstants(long id) {
		 this.id = id;
	 }
}
