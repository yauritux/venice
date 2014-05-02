package com.gdn.venice.exception;

public class MIGSFileParserException extends VeniceInternalException{
	
	private static final long serialVersionUID = 687914392367486082L;

	public MIGSFileParserException(String message) {
		super(message);
	}
	
	public MIGSFileParserException(String message, Throwable cause) {
		super(message, cause);
	}
}
