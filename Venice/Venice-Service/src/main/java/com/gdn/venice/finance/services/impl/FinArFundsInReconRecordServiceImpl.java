package com.gdn.venice.finance.services.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.FinArFundsInReconRecordDAO;
import com.gdn.venice.exception.CannotPersistFinArFundsInReconRecordException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.finance.services.FinArFundsInReconRecordService;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class FinArFundsInReconRecordServiceImpl implements FinArFundsInReconRecordService {
	
	@Autowired
	private FinArFundsInReconRecordDAO finArFundsInReconRecordDAO;
	
	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public FinArFundsInReconRecord persist(FinArFundsInReconRecord reconRecord)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persist::BEGIN,reconRecord=" + reconRecord);
		FinArFundsInReconRecord persistedReconRecord = reconRecord;
		if (reconRecord != null) {
			try {
				if (!em.contains(reconRecord)) {
					//reconRecord in detach mode, hence call save explicitly
					persistedReconRecord = finArFundsInReconRecordDAO.saveAndFlush(reconRecord);
				}
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), e);
				throw CommonUtil.logAndReturnException(
						new CannotPersistFinArFundsInReconRecordException("Cannot persist FinArFundsInReconRecord!"
								, VeniceExceptionConstants.VEN_EX_800001), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}
		
		return persistedReconRecord;
	}

}
