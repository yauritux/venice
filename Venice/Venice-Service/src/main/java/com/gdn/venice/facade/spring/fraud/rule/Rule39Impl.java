package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule39DAO;
import com.gdn.venice.dao.VenOrderContactDetailDAO;
import com.gdn.venice.persistence.FrdParameterRule39;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.util.CommonUtil;

/**
 * Pasca bayar
 * 
 */
@Service("Rule39")
public class Rule39Impl implements Rule {
	private static final String CLASS_NAME = Rule39Impl.class.getCanonicalName();
	
	@Autowired
	VenOrderContactDetailDAO venOrderContactDetailDAO;
	@Autowired
	FrdParameterRule39DAO frdParameterRule39DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		List<VenOrderContactDetail> phoneList = venOrderContactDetailDAO.findByContactMobileContactPhoneVenOrder(order);
		
		if(phoneList.size() > 0){
			
			for (VenOrderContactDetail phoneContact : phoneList) {
				String phoneCode = getPhoneCode(phoneContact);
				if(!phoneCode.isEmpty()){
					FrdParameterRule39 rule = frdParameterRule39DAO.findByNoHp(phoneCode);
					
					if(rule != null){
						CommonUtil.logInfo(CLASS_NAME, "no hp/telepon match: "+ phoneContact.getVenContactDetail().getContactDetail());
						totalRiskPoint = rule.getRiskPoint();
						break;
					}
				}
				
			}
			
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public String getPhoneCode(VenOrderContactDetail phoneContact){
		String phoneNumber = phoneContact.getVenContactDetail().getContactDetail();
		
		if(phoneNumber.length() > 4){
			return phoneNumber.substring(0, 4);
		}else{
			return null;
		}
	}
		
}