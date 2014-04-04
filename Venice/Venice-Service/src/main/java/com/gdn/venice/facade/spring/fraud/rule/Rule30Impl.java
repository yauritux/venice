package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule30DAO;
import com.gdn.venice.dao.FrdRuleConfigTresholdDAO;
import com.gdn.venice.dao.VenAddressDAO;
import com.gdn.venice.dao.VenOrderContactDetailDAO;
import com.gdn.venice.persistence.FrdParameterRule30;
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.util.CommonUtil;

/**
 * Phone Area Code Customer
 */
@Service("Rule30")
public class Rule30Impl implements Rule{
	private static final String CLASS_NAME = Rule30Impl.class.getCanonicalName();
	
	@Autowired
	VenAddressDAO venAddressDAO;
	@Autowired
	FrdRuleConfigTresholdDAO frdRuleConfigTresholdDAO;
	@Autowired
	FrdParameterRule30DAO frdParameterRule30DAO;
	@Autowired
	VenOrderContactDetailDAO venOrderContactDetailDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		int riskPoint = getRiskPoint();
		
		VenAddress address = venAddressDAO.findWithVenCityByOrder(order);
		
		if(address != null){
			String city = sanitizeCityName(address.getVenCity().getCityName());
			FrdParameterRule30 rule = frdParameterRule30DAO.findByUpperNamaKota(city.toUpperCase());
			
			if(rule != null){
				List<VenOrderContactDetail> phoneContactList = venOrderContactDetailDAO.findByContactPhoneVenOrder(order);
				for (VenOrderContactDetail phoneContact : phoneContactList) {
					int phoneContactLength = phoneContact.getVenContactDetail().getContactDetail().trim().length();
					int areaCodeLength = rule.getKodeKota().trim().length();
					
					if(phoneContactLength >= areaCodeLength){
						if(!isAreaCodeMatches(rule, phoneContact)){
							totalRiskPoint = riskPoint;
							break;
						}
					}
					
				}
			}
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public String sanitizeCityName(String cityFullName){
		return cityFullName.toLowerCase()
						.replace("kota","")
						.replace("barat","")
						.replace("pusat","")
						.replace("timur","")
						.replace("selatan","")
						.replace(".","")
						.replace("utara","")
						.replace("kab","")
						.replace("tenggara","").trim().toUpperCase();
	}
	
	public int getRiskPoint(){
		return new Integer(frdRuleConfigTresholdDAO.findByKeyFrdParameterRule30().getValue());
	}
	
	public boolean isAreaCodeMatches(FrdParameterRule30 rule, VenOrderContactDetail phoneContact){
		String phoneNumber = phoneContact.getVenContactDetail().getContactDetail();
		int areaCodeLength = rule.getKodeKota().length();
		
		if(phoneNumber.substring(0, areaCodeLength).equals(rule.getKodeKota())){
			return true;
		}else{
			return false;
		}
	}
		
}