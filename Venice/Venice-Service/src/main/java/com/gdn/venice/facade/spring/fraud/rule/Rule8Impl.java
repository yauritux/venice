package com.gdn.venice.facade.spring.fraud.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule8DAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.persistence.FrdParameterRule8;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

@Service
public class Rule8Impl {
	private static final String CLASS_NAME = Rule8Impl.class.getCanonicalName();
	
	@Autowired
	VenOrderDAO venOrderDAO;
	
	@Autowired
	FrdParameterRule8DAO frdParameterRule8DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		VenOrder orderWithPaymentType = venOrderDAO.findWithWcsPaymentTypeByOrder(order);
		
		String wcsPaymentType = orderWithPaymentType.getVenOrderPaymentAllocations().get(0).getVenOrderPayment().getVenWcsPaymentType().getWcsPaymentTypeCode();
		
		FrdParameterRule8 rule = frdParameterRule8DAO.findByPaymentType(wcsPaymentType);
		
		if(rule != null){
			totalRiskPoint = rule.getRiskPoint();
			CommonUtil.logInfo(CLASS_NAME, "Payment Type Matched : " + wcsPaymentType);
		}
			
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	
	
}