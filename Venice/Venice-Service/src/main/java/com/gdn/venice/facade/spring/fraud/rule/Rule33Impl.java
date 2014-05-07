
package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.constants.VenOrderPaymentConstants;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * ECI 5 & 7
 */
@Service("Rule33")
public class Rule33Impl implements Rule {
	private static final String CLASS_NAME = Rule33Impl.class.getCanonicalName();
	
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		List<VenOrderPaymentAllocation> orderPaymentList = venOrderPaymentAllocationDAO.findByVenOrderPaymentTypeCC(order);
		
		totalRiskPoint = getRiskPoint(orderPaymentList);
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int getRiskPoint(List<VenOrderPaymentAllocation> orderPaymentList){
		if (orderPaymentList.size() > 0) {
			String threeDsSecurityLevel = orderPaymentList.get(0).getVenOrderPayment().getThreeDsSecurityLevelAuth();
			if (threeDsSecurityLevel != null) {
				return getRiskPoint(threeDsSecurityLevel);
			}
		}
		
		return 0;
	}
	
	public int getRiskPoint(String eCommerceIndicator){
		if (eCommerceIndicator.equals(VenOrderPaymentConstants.E_COMMERCE_INDICATOR_5.value())) {
			return -100;
		}else if (eCommerceIndicator.equals(VenOrderPaymentConstants.E_COMMERCE_INDICATOR_7.value())) {
			return 500;
		}else{
			return 0;
		}
	}
		
}
