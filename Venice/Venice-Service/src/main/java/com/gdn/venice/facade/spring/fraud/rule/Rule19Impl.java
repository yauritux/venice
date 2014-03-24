package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule19DAO;
import com.gdn.venice.dao.VenAddressDAO;
import com.gdn.venice.persistence.FrdParameterRule19;
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Validity of Address
 */
@Service
public class Rule19Impl {
	private static final String CLASS_NAME = Rule19Impl.class.getCanonicalName();
	
	@Autowired
	VenAddressDAO venAddressDAO;
	@Autowired
	FrdParameterRule19DAO frdParameterRule19DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		String orderAddress = getOrderAddress(order);
		List<String> itemAddressList = getItemAddress(order);
		List<String> paymentAddressList = getPaymentAddress(order);
		
		String similarityResult = determineAddressesSimilarity(orderAddress, itemAddressList, paymentAddressList);
		
		FrdParameterRule19 rule = frdParameterRule19DAO.findByCode(similarityResult);
		totalRiskPoint = rule.getRiskPoint();
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public String getOrderAddress(VenOrder order){
		VenAddress venAddress = venAddressDAO.findOrderAddressByOrder(order);
		return (venAddress.getStreetAddress1()!=null?venAddress.getStreetAddress1().toLowerCase():"").replace(".", "").replace(" ", "");
	}
	
	public List<String> getItemAddress(VenOrder order){
		return venAddressDAO.findGroupedItemAddressByOrder(order);
	}
	
	public List<String> getPaymentAddress(VenOrder order){
		return venAddressDAO.findGroupedPaymentAddressByOrder(order);
	}
	
	public String determineAddressesSimilarity(String orderAddress, List<String> itemAddressList, List<String> paymentAddressList){
		String similarityResult = "";
		if(itemAddressList.size()==1 && paymentAddressList.size()==1){					
			similarityResult = orderAddress.equalsIgnoreCase(itemAddressList.get(0)) && orderAddress.equalsIgnoreCase(paymentAddressList.get(0))?"full":orderAddress.equalsIgnoreCase(itemAddressList.get(0)) || orderAddress.equalsIgnoreCase(paymentAddressList.get(0)) || (itemAddressList.get(0)).equalsIgnoreCase(paymentAddressList.get(0))?"true":"false";				
		}else{
			for(int i=0;i<itemAddressList.size();i++){
				for(int j=0;j<paymentAddressList.size();j++){
					similarityResult = orderAddress.equalsIgnoreCase(itemAddressList.get(i)) || orderAddress.equalsIgnoreCase(paymentAddressList.get(j)) || itemAddressList.get(i).equalsIgnoreCase(paymentAddressList.get(j))?"true":"false";							
					if(similarityResult.equals("true")){
						break;
					}
				}	
				if(similarityResult.equals("true")){
					break;
				}
			}
		}
		
		return similarityResult;
	}
	
	
}