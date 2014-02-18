package com.gdn.venice.exception;

public class ActivityReportDataFilterException extends VeniceInternalException{
	private static final long serialVersionUID = 1L;
	
	public ActivityReportDataFilterException(String message) {
		super(message);
	}
	
	public ActivityReportDataFilterException(String message, Throwable cause) {
		super(message, cause);
	}
}
