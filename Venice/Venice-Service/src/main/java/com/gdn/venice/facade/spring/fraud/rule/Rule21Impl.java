package com.gdn.venice.facade.spring.fraud.rule;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule21DAO;
import com.gdn.venice.persistence.FrdParameterRule21;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

@Service
public class Rule21Impl {
	private static final String CLASS_NAME = Rule21Impl.class.getCanonicalName();
	private static final SimpleDateFormat SDF_TIME = new SimpleDateFormat("HH:mm:ss");
	
	@Autowired
	FrdParameterRule21DAO frdParameterRule21DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		String orderTime = SDF_TIME.format(order.getOrderDate().getTime());
		
		FrdParameterRule21 rule = frdParameterRule21DAO.findByTimeRange(orderTime);
		
		totalRiskPoint = rule.getRiskPoint();
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	
	
	
}