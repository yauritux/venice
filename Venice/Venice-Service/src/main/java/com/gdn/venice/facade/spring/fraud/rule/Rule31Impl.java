
package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule31DAO;
import com.gdn.venice.persistence.FrdParameterRule31;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * List Genuine Transaction by BCA
 */
@Service("Rule31")
public class Rule31Impl implements Rule{
	private static final String CLASS_NAME = Rule31Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule31DAO frdParameterRule31DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		List<FrdParameterRule31> ruleList = frdParameterRule31DAO.findByGenuineList(order.getOrderId());
		
		if(ruleList.size() > 0)
			totalRiskPoint = 1;
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
		
}