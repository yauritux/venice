package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule25DAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.persistence.FrdParameterRule25;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.CommonUtil;

/**
 *  Company Shipping Address
 */
@Service("Rule25")
public class Rule25Impl implements Rule{
	private static final String CLASS_NAME = Rule25Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule25DAO frdParameterRule25DAO;
	@Autowired
	VenOrderItemDAO venOrderItemDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		List<FrdParameterRule25> ruleList = frdParameterRule25DAO.findAll();
		
		for (FrdParameterRule25 rule : ruleList) {
			List<VenOrderItem> orderItemList = venOrderItemDAO.findByVenOrderAddressStreet(order, rule.getCode().toUpperCase());
			
			if(orderItemList.size() > 0){
				totalRiskPoint = 1;
				break;
			}
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
		
}