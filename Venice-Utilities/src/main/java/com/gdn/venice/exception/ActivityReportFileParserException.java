package com.gdn.venice.exception;

public class ActivityReportFileParserException extends VeniceInternalException{
	
	private static final long serialVersionUID = 1L;
	
	public ActivityReportFileParserException(String message) {
		super(message);
	}
	
	public ActivityReportFileParserException(String message, Throwable cause) {
		super(message, cause);
	}

}
