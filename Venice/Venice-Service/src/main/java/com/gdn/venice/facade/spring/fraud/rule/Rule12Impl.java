package com.gdn.venice.facade.spring.fraud.rule;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule12DAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.persistence.FrdParameterRule12;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Multiple cards used from a single IP address
 */
@Service("Rule12")
public class Rule12Impl implements Rule{
	private static final String CLASS_NAME = Rule12Impl.class.getCanonicalName();
	private static final SimpleDateFormat SDF_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	@Autowired
	FrdParameterRule12DAO frdParameterRule12DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		List<FrdParameterRule12> ruleList = frdParameterRule12DAO.findAll();
		
		for (FrdParameterRule12 rule : ruleList) {
			totalRiskPoint += getRiskByIPAddress(order, rule);
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int getRiskByIPAddress(VenOrder order, FrdParameterRule12 rule){
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