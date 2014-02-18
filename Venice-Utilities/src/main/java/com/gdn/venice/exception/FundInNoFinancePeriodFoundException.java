package com.gdn.venice.exception;

public class FundInNoFinancePeriodFoundException extends VeniceInternalException {

	private static final long serialVersionUID = -9218131888305013247L;

	public FundInNoFinancePeriodFoundException(String message) {
		super(message);
	}

	public FundInNoFinancePeriodFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
