package com.gdn.venice.facade.spring.fraud.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule23DAO;
import com.gdn.venice.dao.VenOrderContactDetailDAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.persistence.FrdParameterRule23;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.util.CommonUtil;

@Service
public class Rule23Impl {
	private static final String CLASS_NAME = Rule23Impl.class.getCanonicalName();
	
	@Autowired
	VenOrderContactDetailDAO venOrderContactDetailDAO;
	@Autowired
	VenOrderDAO venOrderDAO;
	@Autowired
	FrdParameterRule23DAO frdParameterRule23DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		String customerEmail = getCustomerEmail(order);
		String customerName = getCustomerName(order);
		
		String similarity = determineCustomerEmailAndNameSimilarities(customerName, customerEmail);
		
		FrdParameterRule23 rule = frdParameterRule23DAO.findByCode(similarity);
		
		totalRiskPoint = rule.getRiskPoint();
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public String getCustomerEmail(VenOrder order){
		VenOrderContactDetail contact = venOrderContactDetailDAO.findByContactEmailVenOrder(order);
		return contact.getVenContactDetail().getContactDetail();
	}
	
	public String getCustomerName(VenOrder order){
		VenOrder orderWithCustomer = venOrderDAO.findWithVenCustomerByOrder(order);
		return orderWithCustomer.getVenCustomer().getCustomerUserName();
	}
	
	public String determineCustomerEmailAndNameSimilarities(String customerName, String customerEmail){
		String editedCustomerName= customerName.replace(" ", "").replace(".", "").replace(",", "").replace("_", "").replace("-", "").replace("/", "").toUpperCase();
		String editedCustomerEmail =(customerEmail.split("@")[0]+"").replace(".", "").replace(",", "").replace("_", "").replace("-", "").replace("/", "").replace(" ", "").trim().toUpperCase();
		String result = editedCustomerName.equals(editedCustomerEmail)?"full":"";
		
		if(editedCustomerName.replace(editedCustomerEmail, "").equals("") && !result.equals("full")){
			result="full";
		}	
		
		if(!result.equals("full")){
			if( customerName.replace(".", " ").replace("_", " ").replace("-", " ").trim().contains(" ")){
				String[] names = customerName.replace(".", " ").replace("_", " ").replace("-", " ").trim().split(" ");	 
				int i=0;
				for(String iname:names){
					if(editedCustomerEmail.contains(iname.toUpperCase())){
						i++;	
						editedCustomerEmail=editedCustomerEmail.replace(iname.toUpperCase(), "");
					}
				}
				result=i==names.length?"full":i>0?"true":result.equals("true")?"true":"false";
			}else{
				String emails=""+customerEmail.split("@")[0];	
				result=emails.toUpperCase().contains(customerName)?"true":result.equals("true")?"true":"false";							
			}						
		}
		
		return result;
	}
	
	
}