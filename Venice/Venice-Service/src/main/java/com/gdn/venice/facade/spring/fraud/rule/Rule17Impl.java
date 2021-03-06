package com.gdn.venice.facade.spring.fraud.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule17DAO;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * City blacklist
 */
@Service("Rule17")
public class Rule17Impl implements Rule{
	private static final String CLASS_NAME = Rule17Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule17DAO frdParameterRule17DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		Integer riskPoint = frdParameterRule17DAO.sumBlacklistedCityRiskPointByVenOrder(order);
		
		totalRiskPoint = riskPoint != null ? riskPoint : 0;
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	
	
	
}