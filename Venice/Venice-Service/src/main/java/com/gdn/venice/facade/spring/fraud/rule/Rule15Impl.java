package com.gdn.venice.facade.spring.fraud.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule15DAO;
import com.gdn.venice.dao.VenBinCreditLimitEstimateDAO;
import com.gdn.venice.persistence.FrdParameterRule15;
import com.gdn.venice.persistence.VenBinCreditLimitEstimate;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Bin number not registered
 */
@Service("Rule15")
public class Rule15Impl implements Rule{
	private static final String CLASS_NAME = Rule15Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule15DAO frdParameterRule15DAO;
	@Autowired
	VenBinCreditLimitEstimateDAO venBinCreditLimitEstimateDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		String maskedCreditCard = getMaskedCreditCard(order);
		String binNumber = getBinNumber(maskedCreditCard);
		
		if(isBinNumberNotRegistered(binNumber)){
			FrdParameterRule15 rule = frdParameterRule15DAO.findByCode("BIN-NR");
			totalRiskPoint = rule.getRiskPoint();
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public String getMaskedCreditCard(VenOrder order){
		return order.getVenOrderPaymentAllocations().get(0).getVenOrderPayment().getMaskedCreditCardNumber();
	}
	
	public String getBinNumber(String maskedCreditCardNumber){
		return maskedCreditCardNumber.substring(0, 6);
	}
	
	public boolean isBinNumberNotRegistered(String binNumber){
		VenBinCreditLimitEstimate registeredBIN = venBinCreditLimitEstimateDAO.findByActiveAndBinNumber(binNumber);
		return registeredBIN == null;
	}
}