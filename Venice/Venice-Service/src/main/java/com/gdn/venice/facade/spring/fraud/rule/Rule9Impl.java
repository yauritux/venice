package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule9DAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.persistence.FrdParameterRule9;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

@Service
public class Rule9Impl {
	private static final String CLASS_NAME = Rule9Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule9DAO frdParameterRule9DAO;
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		String shippingAddressCode = getShippingCode();
		String paymentCode = getPaymentCode(order);
		
		String ruleCode = shippingAddressCode + paymentCode;
		
		FrdParameterRule9 rule = frdParameterRule9DAO.findByCode(ruleCode); 
		
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
			return "N";
		else
			return "1";
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