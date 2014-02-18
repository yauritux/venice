package com.gdn.venice.exception;

public class FundInFileAlreadyUploadedException extends VeniceInternalException {
	private static final long serialVersionUID = -8002580050879681978L;
	
	public FundInFileAlreadyUploadedException(String message) {
		super(message);
	}
	
	public FundInFileAlreadyUploadedException(String message, Throwable cause) {
		super(message, cause);
	}

}
