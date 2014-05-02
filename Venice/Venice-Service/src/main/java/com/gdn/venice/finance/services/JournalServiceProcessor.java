package com.gdn.venice.finance.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.FinJournalApprovalGroup;

/**
 * 
 * @author yauritux
 *
 */
public interface JournalServiceProcessor {

	public void setNextProcessor(JournalServiceProcessor processor);
	
	public void setJournalApprovalGroup(FinJournalApprovalGroup finJournalApprovalGroup);
	
	public void processingJournal(List<? extends Object> finArFundsInReconRecordList) 
	   throws VeniceInternalException;
}
