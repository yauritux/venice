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
	public List<VenBank> synchronizeVenBankReferences(
			List<VenBank> bankReferences) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenBankReferences::bankReferences = " + bankReferences);
		//if (bankReferences == null || bankReferences.isEmpty()) return null;
		
		List<VenBank> synchronizedBankReferences = new ArrayList<VenBank>();
		
		if (bankReferences != null) {
			for (VenBank bank : bankReferences) {
				if (bank.getBankCode() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenBankReferences::Restricting VenBank... :" + bank.getBankCode());
					VenBank venBank = venBankDAO.findByBankCode(bank.getBankCode());
					if (venBank == null) {
						throw CommonUtil.logAndReturnException(new BankNotFoundException(
								"Bank does not exist", VeniceExceptionConstants.VEN_EX_200001)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenBankReferences::adding venBank into synchronizedBankReferences");
						synchronizedBankReferences.add(venBank);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenBankReferences::successfully added venBank into synchronizedBankReferences");
					}
				}	
			} //end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenBankReferences::returning synchronizedBankReferences = "
				+ synchronizedBankReferences.size());
		return synchronizedBankReferences;
	}

}
