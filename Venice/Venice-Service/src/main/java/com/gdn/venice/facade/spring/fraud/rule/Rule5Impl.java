
package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule5DAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.persistence.FrdParameterRule5;
import com.gdn.venice.persistence.LogLogisticsProvider;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.CommonUtil;

/**
 * Rush or overnight shipping
 *
 */
@Service("Rule5")
public class Rule5Impl implements Rule{
	private static final String CLASS_NAME = Rule5Impl.class.getCanonicalName();
	
	@Autowired
	VenOrderItemDAO orderItemDAO;
	@Autowired
	FrdParameterRule5DAO frdParameterRule5DAO;
	
	public int getRiskPoint(VenOrder order){
		CommonUtil.logInfo(CLASS_NAME, "Calculating Order : " + order.getWcsOrderId());
		
		List<VenOrderItem> orderItemList = orderItemDAO.findWithLogisticProviderByVenOrder(order);
		
		LogLogisticsProvider provider = getLogisticsProvider(orderItemList.get(0));
		
		FrdParameterRule5 rule = frdParameterRule5DAO.findByShippingType(provider.getLogisticsProviderCode());
		
		int riskPoint = getRiskPoint(rule);
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + riskPoint);
		
		return riskPoint;
		
	}
	
	public LogLogisticsProvider getLogisticsProvider(VenOrderItem orderItem){
		return orderItem.getLogLogisticService().getLogLogisticsProvider();
	}
	
	public int getRiskPoint(FrdParameterRule5 rule){
		if(rule == null) return 0;
		return rule.getRiskPoint();
	}
	
}