package com.gdn.venice.finance.services;

import com.gdn.venice.exception.VeniceInternalException;

/**
 * 
 * @author yauritux
 *
 */
public interface FinJournalService<T> {
	
	public T createNew(T obj) throws VeniceInternalException;
}
