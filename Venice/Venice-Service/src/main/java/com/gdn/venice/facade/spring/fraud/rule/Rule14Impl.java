package com.gdn.venice.facade.spring.fraud.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdEmailTypeDAO;
import com.gdn.venice.dao.FrdParameterRule14DAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.persistence.FrdEmailType;
import com.gdn.venice.persistence.FrdParameterRule14;
import com.gdn.venice.persistence.VenCustomer;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Order from internet addresses that make use of free email services
 *
 */
@Service("Rule14")
public class Rule14Impl implements Rule{
	private static final String CLASS_NAME = Rule14Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule14DAO frdParameterRule14DAO;
	@Autowired
	FrdEmailTypeDAO frdEmailTypeDAO;
	@Autowired
	VenOrderDAO venOrderDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		VenOrder orderWithCustomer = getOrderWithCustomer(order);
		
		String emailType = getEmailType(orderWithCustomer.getVenCustomer());
		
		FrdParameterRule14 rule = frdParameterRule14DAO.findByEmailType(emailType);
		
		totalRiskPoint = rule.getRiskPoint();
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public VenOrder getOrderWithCustomer(VenOrder order){
		return venOrderDAO.findWithVenCustomerByOrder(order);
	}
	
	public String getEmailAddress(VenCustomer customer){
		return customer.getCustomerUserName();
	}
	
	public String getMailServer(VenCustomer customer){
		String emailAddress = getEmailAddress(customer);
		
		if(isEmailAddress(emailAddress)){
			String[] emailSplit = emailAddress.split("@");
			
			CommonUtil.logInfo(CLASS_NAME, "Customer name : " + customer.getVenParty().getFullOrLegalName() + ", Email : " + emailAddress);
			
			return emailSplit[1];
		}
		
		return null;
	}
	
	public boolean isEmailAddress(String customerUsername){
		return customerUsername.contains("@");
	}
	
	public String getEmailType(VenCustomer customer){
		String mailServer = getMailServer(customer);
		String[] tempSplit = mailServer.split("\\.");
		
		mailServer = tempSplit[0];
		
		FrdEmailType frdEmailType = frdEmailTypeDAO.findByUpperMailServerPattern(mailServer.toUpperCase());
		
		if(frdEmailType == null)
			return "corporate";
		else
			return frdEmailType.getEmailType();
	}
	
	
}