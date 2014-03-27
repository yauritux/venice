package com.gdn.venice.facade.spring.fraud.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.constants.VenCustomerUserTypeConstants;
import com.gdn.venice.dao.FrdParameterRule1DAO;
import com.gdn.venice.dao.VenCustomerDAO;
import com.gdn.venice.persistence.FrdParameterRule1;
import com.gdn.venice.persistence.VenCustomer;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * First time shopper
 */
@Service("Rule1")
public class Rule1Impl implements Rule{
	private static final String CLASS_NAME = Rule1Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule1DAO frdParameterRule1DAO;
	
	@Autowired
	VenCustomerDAO venCustomerDAO;
	
	public int getRiskPoint(VenOrder order){
		CommonUtil.logInfo(CLASS_NAME, "Calculating Order : " + order.getWcsOrderId());
		
		VenCustomer customer = venCustomerDAO.findOne(order.getVenCustomer().getCustomerId());
		String customerType = getCustomerType(customer).value();
		FrdParameterRule1 frdRule = frdParameterRule1DAO.findByCustomerType(customerType);
		
		int riskPoint = frdRule.getRiskPoint();
		
		CommonUtil.logInfo(CLASS_NAME, "Customer Type : " + customerType);
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + riskPoint);
		
		return riskPoint;
	}
	
	public boolean isCustomerRegisteredUser(VenCustomer customer){
		if (customer.getUserType() != null && customer.getUserType().equals("R")) {
			return true;
		}else{
			return false;
		}
	}
	
	public VenCustomerUserTypeConstants getCustomerType(VenCustomer customer){
		
		if(isCustomerRegisteredUser(customer)){
			return VenCustomerUserTypeConstants.USER_TYPE_REGISTERED;
		}else{
			return VenCustomerUserTypeConstants.USER_TYPE_UNREGISTERED;
		}
	}
}
