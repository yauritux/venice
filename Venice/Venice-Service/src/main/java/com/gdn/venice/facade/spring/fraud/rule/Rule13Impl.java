package com.gdn.venice.facade.spring.fraud.rule;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule13DAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.persistence.FrdParameterRule13;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

@Service
public class Rule13Impl {
	private static final String CLASS_NAME = Rule13Impl.class.getCanonicalName();
	private static final SimpleDateFormat SDF_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	@Autowired
	FrdParameterRule13DAO frdParameterRule13DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		List<FrdParameterRule13> ruleList = frdParameterRule13DAO.findAll();
		
		for (FrdParameterRule13 rule : ruleList) {
			totalRiskPoint += getRiskByIPAddress(order, rule);
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int getRiskByIPAddress(VenOrder order, FrdParameterRule13 rule){
		int riskPoint = 0;
		int daySpan = rule.getTimespan();
		int minCCNumber = rule.getMinCcNumber();
		
		Timestamp orderDate = order.getOrderDate();
		
		Date backedDate = DateUtils.addDays(orderDate, -daySpan);
		String backedDateStr = SDF_TIMESTAMP.format(backedDate);
		String currentPaymentDateStr = SDF_TIMESTAMP.format(orderDate);
		
		List<Integer> totalCreditCard = venOrderPaymentAllocationDAO
								.countMaskedCreditCardByIpAddressOrderDateRange(order.getIpAddress(), backedDateStr, currentPaymentDateStr);
		
		if(totalCreditCard.size() > minCCNumber){
			riskPoint += rule.getRiskPoint();
			CommonUtil.logInfo(CLASS_NAME, "IP address found : " + order.getIpAddress() + ", Start Date : " + backedDateStr + ", End Date : " + currentPaymentDateStr);
		}
		
		return riskPoint;
	}
	
	
	
}