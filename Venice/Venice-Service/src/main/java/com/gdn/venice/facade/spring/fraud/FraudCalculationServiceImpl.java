package com.gdn.venice.facade.spring.fraud;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdFraudSuspicionCaseDAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

@Service
public class FraudCalculationServiceImpl implements FraudCalculationService {
	private static final String CLASS_NAME = FraudCalculationServiceImpl.class.getCanonicalName();
	
	@Autowired
	VenOrderDAO venOrderDAO;
	
	@Autowired
	FrdFraudSuspicionCaseDAO fraudSuspicionCaseDAO;
	
	public List<VenOrder> getOrderPaidByCreditCard(String startDate){
		List<VenOrder> orderList = venOrderDAO.findPaidByCreditCardAndStatusC(startDate);
		CommonUtil.logInfo(CLASS_NAME, "Total order with status C and paid by Credit Card = " + orderList.size());
		return orderList;
	}
	
	public boolean isMIGSReportUploaded(VenOrder venOrder){
		String maskedCreditCardNumber = venOrder.getVenOrderPaymentAllocations().get(0).getVenOrderPayment().getMaskedCreditCardNumber();
		return (maskedCreditCardNumber != null && !maskedCreditCardNumber.trim().equalsIgnoreCase(""));
	}
	
	public boolean isRiskPointCalculatedBefore(VenOrder venOrder){
		int totalOrder = fraudSuspicionCaseDAO.countByVenOrderId(venOrder.getOrderId());
		return totalOrder > 0;		
	}
		
}
