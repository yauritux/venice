package com.gdn.venice.constants;

public enum FinArFundsInReportTimeConstants {
	
	FIN_AR_FUNDS_IN_REPORT_TIME_REAL_TIME(Constants.REALTIME_ID),
	FIN_AR_FUNDS_IN_REPORT_TIME_H1(Constants.H1_ID);
	
	private long id;
	
	private FinArFundsInReportTimeConstants(long id){
		this.id = id;
	}
	
	public long id(){
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
		public static final int REALTIME_ID = 1;
		public static final int H1_ID = 2;
	}

}
