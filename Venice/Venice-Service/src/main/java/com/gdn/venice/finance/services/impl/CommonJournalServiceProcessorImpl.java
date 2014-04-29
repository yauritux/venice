package com.gdn.venice.finance.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import com.gdn.venice.constants.FinArFundsInActionAppliedConstants;
import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.dao.FinArFundsInAllocatePaymentDAO;
import com.gdn.venice.dao.FinArFundsInReconRecordDAO;
import com.gdn.venice.exception.NoParameterSuppliedException;
import com.gdn.venice.exception.NoRecordFoundException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.finance.services.JournalServiceProcessor;
import com.gdn.venice.persistence.FinArFundsInAllocatePayment;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinJournalApprovalGroup;
import com.gdn.venice.util.CommonUtil;

/**
 * This class should be the first service's class (first entry gate) 
 * in the JournalServiceProcessor chain (CoR) 
 * before another class in the same particular chain going to be executed.
 * Since JournalServiceProcessor is using CoR, therefore priority should be 
 * considered carefully.
 * 
 * @author yauritux
 *
 */
@Service
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CommonJournalServiceProcessorImpl implements JournalServiceProcessor {

	@Autowired
	private FinArFundsInReconRecordDAO finArFundsInReconRecordDAO;
	
	@Autowired
	private FinArFundsInAllocatePaymentDAO finArFundsInAllocatePaymentDAO;
	
	private JournalServiceProcessor nextProcessor;
	
	private FinJournalApprovalGroup finJournalApprovalGroup;
	
	@Override
	public void setNextProcessor(JournalServiceProcessor nextProcessor) {
		this.nextProcessor = nextProcessor;
	}
	
	@Override
	public void setJournalApprovalGroup(FinJournalApprovalGroup finJournalApprovalGroup) {
		this.finJournalApprovalGroup = finJournalApprovalGroup;
	}
	
	@Override
	public void processingJournal(List<? extends Object> finArFundsInReconRecordIdList) 
	   throws VeniceInternalException {
		if (finArFundsInReconRecordIdList == null 
				|| finArFundsInReconRecordIdList.isEmpty()) {
			throw CommonUtil.logAndReturnException(new NoParameterSuppliedException(
					"Empty list passed to postCashReceiveJournalTransaction.The reconciliation record list must contain entries.")
			   , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}
		
		// Read all the relevant funds in records from the database
		List<FinArFundsInReconRecord> reconRecordList = new ArrayList<FinArFundsInReconRecord>();
		for (Object reconciliationRecordID : finArFundsInReconRecordIdList) {
			FinArFundsInReconRecord finArFundsInReconRecord = null;
			try {
				finArFundsInReconRecord = finArFundsInReconRecordDAO.findByReconRecordIdActionAppliedNotRemoved((Long) reconciliationRecordID);
			} catch (Exception e) {
				throw CommonUtil.logAndReturnException(new NoRecordFoundException(
						"cannot find reconciliation record. perhaps due to cannot parse reconciliationRecordIDList into appropriate type.", e)
				    , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
			
			if (finArFundsInReconRecord != null) {
				reconRecordList.add(finArFundsInReconRecord);
			} else {
				throw CommonUtil.logAndReturnException(new NoRecordFoundException(
						"Parameter supplied contains no valid keys. The reconciliation record list must contain valid keys for existing reconciliation records.")
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}
		
		//proceed with journal
		for (FinArFundsInReconRecord reconRecord : reconRecordList) {
			List<FinArFundsInAllocatePayment> idRecordDestination = finArFundsInAllocatePaymentDAO.findByIdReconRecordDest(
					reconRecord.getReconciliationRecordId());
			if (!idRecordDestination.isEmpty() && (reconRecord.getFinArFundsInActionApplied()
					.getActionAppliedId() == FinArFundsInActionAppliedConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_NONE.id())) {
				continue; // no need to create journal, proceed to next record
			}
			
			nextProcessor.processingJournal(reconRecordList);
			nextProcessor.setJournalApprovalGroup(finJournalApprovalGroup);
		}
	}
}