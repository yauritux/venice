package com.gdn.venice.facade.spring.fraud.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule2DAO;
import com.gdn.venice.persistence.FrdParameterRule2;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

@Service
public class Rule2Impl {
	private static final String CLASS_NAME = Rule2Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule2DAO frdParameterRule2DAO;
	
	public int getRiskPoint(VenOrder order){
		CommonUtil.logInfo(CLASS_NAME, "Calculating Order : " + order.getWcsOrderId());
		
		FrdParameterRule2 frdRule = frdParameterRule2DAO.findByOrderAmount(order.getAmount());
		
		int riskPoint = frdRule.getRiskPoint();
		
		CommonUtil.logInfo(CLASS_NAME, "Order Amount : " + order.getAmount());
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + riskPoint);
		
		return riskPoint;
	}
	
}
