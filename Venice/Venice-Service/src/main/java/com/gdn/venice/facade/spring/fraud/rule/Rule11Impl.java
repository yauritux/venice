package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule11DAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.persistence.FrdParameterRule9;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

/**
 * Multiple transactions on one card or a similar card with a single billing address, but multiple shipping addresses
 * 
 */
@Service("Rule11")
public class Rule11Impl implements Rule{
	private static final String CLASS_NAME = Rule11Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule11DAO frdParameterRule11DAO;
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		String shippingAddressCode = getShippingCode();
		String paymentCode = getPaymentCode(order);
		
		String ruleCode = shippingAddressCode + paymentCode;
		
		FrdParameterRule9 rule = frdParameterRule11DAO.findByCode(ruleCode); 	
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Code Match : " + ruleCode);
		
		totalRiskPoint = rule.getRiskPoint();
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public String getShippingCode(){
		if(isMultipleShippingAddress())
			return "N";
		else
			return "1";
	}
	
	public String getPaymentCode(VenOrder order){
		if(isMultiplePayment(order))
			return "D";
		else
			return "S";
	}
	
	public boolean isMultipleShippingAddress(){
		return false;
	}
	
	public boolean isMultiplePayment(VenOrder order){
		List<VenOrderPaymentAllocation> paymentAllocationList = venOrderPaymentAllocationDAO.findByVenOrder(order);
		
		if(paymentAllocationList.size() > 0)
			return true;
		else
			return false;
	}
	
}