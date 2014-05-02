package com.gdn.venice.finance.services;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.FinJournal;
import com.gdn.venice.persistence.FinJournalApprovalGroup;

/**
 * 
 * @author yauritux
 *
 */
public interface FinJournalApprovalGroupService {

	public FinJournalApprovalGroup createNew(FinJournal finJournal, String journalDescription)
	   throws VeniceInternalException;
}
