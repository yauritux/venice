package com.gdn.venice.facade.spring.fraud.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule6DAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.persistence.FrdParameterRule6;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.CommonUtil;

@Service
public class Rule6Impl {
	private static final String CLASS_NAME = Rule6Impl.class.getCanonicalName();
	
	@Autowired
	VenOrderItemDAO venOrderItemDAO;
	
	@Autowired
	FrdParameterRule6DAO frdParameterRule6DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		List<String> countryCodeList = getOrderItemCountries(order);
		
		for (String string : countryCodeList) {
			FrdParameterRule6 rule = frdParameterRule6DAO.findByShippingCountry(string);
			totalRiskPoint += rule.getRiskPoint();
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public boolean isOrderItemAddressForeignCountry(String countryCode){
		CommonUtil.logInfo(CLASS_NAME, "Country code : " + countryCode);
		return !countryCode.equals("ID");
	}
	
	public List<String> getOrderItemCountries(VenOrder order){
		
		List<VenOrderItem> orderItemList = venOrderItemDAO.findWithVenOrderItemAddressVenCountryByVenOrder(order);
		
		List<String> countryCodeList = new ArrayList<String>(orderItemList.size());
		
		try{
			for (VenOrderItem orderItem : orderItemList) {
				String countryCode = orderItem.getVenOrderItemAddresses().get(0).getVenAddress().getVenCountry().getCountryCode();
				
				countryCodeList.add(countryCode);
			}
		}catch (Exception e) {
			CommonUtil.logError(CLASS_NAME, e);
		}
		
		return countryCodeList;
		
	}
	
	
}