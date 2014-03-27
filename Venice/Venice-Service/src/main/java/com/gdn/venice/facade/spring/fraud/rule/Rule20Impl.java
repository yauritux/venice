package com.gdn.venice.facade.spring.fraud.rule;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule20DAO;
import com.gdn.venice.dao.VenOrderAddressDAO;
import com.gdn.venice.persistence.FrdParameterRule20;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderAddress;
import com.gdn.venice.util.CommonUtil;

/**
 * Class for calculate fraud rule 20: UMR by 33 Province
 */
@Service("Rule20")
public class Rule20Impl implements Rule{
private static final String CLASS_NAME = Rule20Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule20DAO frdParameterRule20DAO;
	@Autowired
	VenOrderAddressDAO venOrderAddressDAO;

	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		VenOrderAddress orderAddress = venOrderAddressDAO.findWithVenAddressVenStateByVenOrder(order);
		
		String province = getProvince(orderAddress);
		
		FrdParameterRule20 rule = frdParameterRule20DAO.findByUpperCaseProvince(province);
		
		totalRiskPoint = calculateRiskPoint(order, rule);
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int calculateRiskPoint(VenOrder order, FrdParameterRule20 rule){
		if(rule == null)
			return 0;

		return calculateRiskPoint(order.getAmount(), rule.getUmr());
	}
	
	public int calculateRiskPoint(BigDecimal orderAmount, BigDecimal umr){
		return (int) Math.round((new Double(orderAmount.toString())*10)/(new Double(umr.toString())*4));
	}
	
	public String getProvince(VenOrderAddress orderAddress){
		return orderAddress.getVenAddress().getVenState().getStateName();
	}
}
