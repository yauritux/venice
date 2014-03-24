package com.gdn.venice.facade.spring.fraud.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule24DAO;
import com.gdn.venice.persistence.FrdParameterRule24;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 *  Total Order Amount 
 */
@Service
public class Rule24Impl {
	private static final String CLASS_NAME = Rule24Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule24DAO frdParameterRule24DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		FrdParameterRule24 rule = frdParameterRule24DAO.findByOrderAmountRange(order.getAmount());
		
		totalRiskPoint = rule.getRiskPoint();
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
		
}