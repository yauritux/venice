package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule34DAO;
import com.gdn.venice.dao.VenOrderAddressDAO;
import com.gdn.venice.dao.VenOrderItemAddressDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.persistence.FrdParameterRule34;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderAddress;
import com.gdn.venice.persistence.VenOrderItemAddress;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

/**
 * IP geolocation information
 */
@Service("Rule34")
public class Rule34Impl implements Rule{
	private static final String CLASS_NAME = Rule34Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule34DAO frdParameterRule34DAO;
	@Autowired
	VenOrderAddressDAO venOrderAddressDAO;
	@Autowired
	VenOrderItemAddressDAO venOrderItemAddressDAO;
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		FrdParameterRule34 rule = frdParameterRule34DAO.findByIpAddress(order.getIpAddress());
		
		if(rule != null){
			String cityBasedOnIpAddress = rule.getCityName().toUpperCase();
			totalRiskPoint = getRiskPoint(cityBasedOnIpAddress, order);
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int getRiskPoint(String cityBasedOnIpAddress,VenOrder order){
		int matchCounter = 0;
		
		String orderCity = getOrderCity(order);
		String orderItemCity = getOrderItemCity(order);
		String orderPaymentCity = getOrderPaymentCity(order);
		
		CommonUtil.logInfo(CLASS_NAME, "City Based On Ip Address : " + cityBasedOnIpAddress + 
				                       ", Order City : " + orderCity  + 
				                       ", Order Item City : " + orderItemCity  + 
				                       ", Order Payment City : " + orderPaymentCity);
		
		if(orderCity != null && orderCity.contains(cityBasedOnIpAddress))
			matchCounter++;
		
		if(orderItemCity != null && orderItemCity.contains(cityBasedOnIpAddress))
			matchCounter++;
		
		if(orderPaymentCity != null && orderPaymentCity.contains(cityBasedOnIpAddress))
			matchCounter++;
		
		return determineRiskPointBasedOnNoOfMatches(matchCounter);
	}
	
	public int determineRiskPointBasedOnNoOfMatches(int noOfMatches){
		if(noOfMatches==0){
			return 10;
		}else if(noOfMatches>0 && noOfMatches <3){
			return 5;
		}else{
			return 0;
		}
	}
	
	public String getOrderCity(VenOrder order){
		VenOrderAddress orderAddress = venOrderAddressDAO.findWithVenAddressVenCityByVenOrder(order);
		return getOrderCity(orderAddress);
	}
	
	public String getOrderCity(VenOrderAddress orderAddress){
		if(orderAddress.getVenAddress().getVenCity() != null)
			return orderAddress.getVenAddress().getVenCity().getCityName().toUpperCase();
		else
			return "";
	}
	
	public String getOrderItemCity(VenOrder order){
		List<VenOrderItemAddress> orderItemAddressList = venOrderItemAddressDAO.findWithVenAddressVenCityByVenOrder(order);
		return getOrderItemCity(orderItemAddressList.get(0));
	}
	
	public String getOrderItemCity(VenOrderItemAddress orderItemAddress){
		if(orderItemAddress.getVenAddress().getVenCity() != null)
			return orderItemAddress.getVenAddress().getVenCity().getCityName().toUpperCase();
		else
			return "";
	}
	
	public String getOrderPaymentCity(VenOrder order){
		List<VenOrderPaymentAllocation> orderPaymentAllocationList = venOrderPaymentAllocationDAO.findWithVenAddressVenCityByVenOrder(order);
		return getOrderPaymentCity(orderPaymentAllocationList.get(0));
	}
	
	public String getOrderPaymentCity(VenOrderPaymentAllocation orderPaymentAllocation){
		if(orderPaymentAllocation.getVenOrderPayment().getVenAddress().getVenCity() != null)
			return orderPaymentAllocation.getVenOrderPayment().getVenAddress().getVenCity().getCityName().toUpperCase();
		else
			return "";
	}
		
}