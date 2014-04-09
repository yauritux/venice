package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule40DAO;
import com.gdn.venice.dao.VenOrderAddressDAO;
import com.gdn.venice.dao.VenOrderContactDetailDAO;
import com.gdn.venice.persistence.FrdParameterRule40;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderAddress;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.util.CommonUtil;

/**
 * Handphone area vs customer location
 */
@Service("Rule40")
public class Rule40Impl implements Rule {
	private static final String CLASS_NAME = Rule40Impl.class.getCanonicalName();
	
	@Autowired
	VenOrderAddressDAO venOrderAddressDAO;
	@Autowired
	VenOrderContactDetailDAO venOrderContactDetailDAO;
	@Autowired
	FrdParameterRule40DAO frdParameterRule40DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 5;
		
		VenOrderAddress orderAddress = venOrderAddressDAO.findWithVenAddressVenCityByVenOrder(order);
		List<VenOrderContactDetail> mobilePhoneContactList = venOrderContactDetailDAO.findByContactMobileVenOrder(order);
		
		String orderCity = getOrderCity(orderAddress);
		
		for (VenOrderContactDetail mobilePhoneContact : mobilePhoneContactList) {
			String mobilePhoneNo = getMobilePhoneNo(mobilePhoneContact);
			String mobilePhoneNoPrefix = getMobilePhonePrefix(mobilePhoneNo);
			
			FrdParameterRule40 rule = frdParameterRule40DAO.findByNoHp(mobilePhoneNoPrefix);
			
			if(rule != null && orderCity.contains(rule.getCityName().toUpperCase())){
				totalRiskPoint = 0;
				break;
			}
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public String getOrderCity(VenOrderAddress orderAddress){
		if(orderAddress.getVenAddress().getVenCity() != null)
			return orderAddress.getVenAddress().getVenCity().getCityName().toUpperCase();
		else
			return "";
	}
	
	public String getMobilePhoneNo(VenOrderContactDetail orderContactDetail){
		try{
			return orderContactDetail.getVenContactDetail().getContactDetail();
		} catch (Exception e) {
			return "";
		}
	}
	
	public String getMobilePhonePrefix(String mobilePhoneNo){
		return mobilePhoneNo.substring(0, 7);
	}
		
}
