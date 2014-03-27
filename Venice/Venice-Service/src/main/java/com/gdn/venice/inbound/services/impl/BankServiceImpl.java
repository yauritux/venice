package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenBankDAO;
import com.gdn.venice.exception.BankNotFoundException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.BankService;
import com.gdn.venice.persistence.VenBank;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class BankServiceImpl implements BankService {

	@Autowired
	private VenBankDAO venBankDAO;
	
	@Override
	public VenBank synchronizeVenBank(VenBank venBank) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenBank::BEGIN, venBank = " + venBank);
		VenBank synchBank = null;
		if (venBank != null && venBank.getBankCode() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenBank::venBank code = " + venBank.getBankCode());			
			synchBank = venBankDAO.findByBankCode(venBank.getBankCode());
			if (synchBank == null) {
				throw CommonUtil.logAndReturnException(new BankNotFoundException(
						"Bank does not exist!", VeniceExceptionConstants.VEN_EX_200001)
				   , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
			return synchBank;
		} 
		return synchBank;
	}
	
	@Override
	public List<VenBank> synchronizeVenBankReferences(
			List<VenBank> bankReferences) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenBankReferences::bankReferences = " + bankReferences);
		
		List<VenBank> synchronizedBankReferences = new ArrayList<VenBank>();
		
		if (bankReferences != null) {
			try {
				for (VenBank bank : bankReferences) {
					synchronizedBankReferences.add(synchronizeVenBank(bank));
				} //end of 'for'
			} catch (VeniceInternalException e) {
				throw CommonUtil.logAndReturnException(e
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);				
			} catch (Exception e) {
				throw CommonUtil.logAndReturnException(new BankNotFoundException(
						"Bank does not exist", VeniceExceptionConstants.VEN_EX_200001)
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);				
			}
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenBankReferences::returning synchronizedBankReferences = "
				+ synchronizedBankReferences.size());
		return synchronizedBankReferences;
	}

}
