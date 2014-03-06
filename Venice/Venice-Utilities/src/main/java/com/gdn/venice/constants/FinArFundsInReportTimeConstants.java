package com.gdn.venice.constants;

public enum FinArFundsInReportTimeConstants {
	
	FIN_AR_FUNDS_IN_REPORT_TIME_REAL_TIME(1),
	FIN_AR_FUNDS_IN_REPORT_TIME_H1(2);
	
	private long id;
	
	private FinArFundsInReportTimeConstants(long id){
		this.id = id;
	}
	
	public long id(){
		return id;
	}

}
