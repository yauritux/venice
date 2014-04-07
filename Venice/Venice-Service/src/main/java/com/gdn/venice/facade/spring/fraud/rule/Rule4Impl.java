package com.gdn.venice.facade.spring.fraud.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule4DAO;
import com.gdn.venice.persistence.FrdParameterRule4;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Order made up of big ticket items
 */
@Service("Rule4")
public class Rule4Impl implements Rule{
	private static final String CLASS_NAME = Rule4Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule4DAO frdParameterRule4DAO;
	
	public int getRiskPoint(VenOrder order){
		CommonUtil.logInfo(CLASS_NAME, "Calculating Order : " + order.getWcsOrderId());
		
		FrdParameterRule4 rule = frdParameterRule4DAO.findByOrderAmountBetweenMinAndMaxValue(order.getAmount());
		
		int riskPoint = getRiskPoint(rule);
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + riskPoint);
		
		return riskPoint;
	}
	
	public int getRiskPoint(FrdParameterRule4 rule){
		if(rule == null) return 0;
		return rule.getRiskPoint();
	}
	
	
	
}