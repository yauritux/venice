package com.gdn.venice.facade.spring.fraud.rule;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule7DAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.persistence.FrdParameterRule7;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

@Service
public class Rule7Impl {
	private static final String CLASS_NAME = Rule7Impl.class.getCanonicalName();
	private static final SimpleDateFormat SDF_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	@Autowired
	FrdParameterRule7DAO frdParameterRule7DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		List<FrdParameterRule7> ruleList = frdParameterRule7DAO.findAll();
		
		for (FrdParameterRule7 rule : ruleList) {
			totalRiskPoint += getMatchedOrderPayment(order, rule);
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int getMatchedOrderPayment(VenOrder order, FrdParameterRule7 rule){
		int riskPoint = 0;
		int daySpan = rule.getDaySpan();
		Timestamp paymentTimestamp = order.getVenOrderPaymentAllocations().get(0).getVenOrderPayment().getPaymentTimestamp();
		String maskedCreditCard = order.getVenOrderPaymentAllocations().get(0).getVenOrderPayment().getMaskedCreditCardNumber();
		
		Date backedDate = DateUtils.addDays(paymentTimestamp, -daySpan);
		
		String backedDateStr = SDF_TIMESTAMP.format(backedDate);
		String currentPaymentDateStr = SDF_TIMESTAMP.format(paymentTimestamp);
		
		int totalPayment = venOrderPaymentAllocationDAO
								.countByPaymentTimeRangeCreditCardNotSameOrder(order, maskedCreditCard, backedDateStr, currentPaymentDateStr);
		
		if(totalPayment > 0){
			riskPoint += rule.getRiskPoint();
			CommonUtil.logInfo(CLASS_NAME, "CC number found : " + maskedCreditCard + ", Start Date : " + backedDateStr + ", End Date : " + currentPaymentDateStr);
		}
		
		return riskPoint;
	}
	
	
	
}