package com.gdn.venice.finance.services.impl;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.FinApprovalStatusConstants;
import com.gdn.venice.dao.FinJournalApprovalGroupDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.finance.services.FinApprovalStatusService;
import com.gdn.venice.finance.services.FinJournalApprovalGroupService;
import com.gdn.venice.persistence.FinJournal;
import com.gdn.venice.persistence.FinJournalApprovalGroup;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class FinJournalApprovalGroupServiceImpl implements FinJournalApprovalGroupService {

	@Autowired
	private FinJournalApprovalGroupDAO finJournalApprovalGroupDAO;
	
	@Autowired
	private FinApprovalStatusService finApprovalStatusService;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public FinJournalApprovalGroup createNew(FinJournal finJournal,
			String journalDescription) throws VeniceInternalException {
		FinJournalApprovalGroup finJournalApprovalGroup = new FinJournalApprovalGroup();
		finJournalApprovalGroup.setFinApprovalStatus(finApprovalStatusService.createNew(
				FinApprovalStatusConstants.FIN_APPROVAL_STATUS_APPROVED));
		finJournalApprovalGroup.setFinJournal(finJournal);
		finJournalApprovalGroup.setJournalGroupDesc(journalDescription);
		finJournalApprovalGroup.setJournalGroupTimestamp(new Timestamp(System.currentTimeMillis()));
		
		return finJournalApprovalGroupDAO.save(finJournalApprovalGroup);
	}
}