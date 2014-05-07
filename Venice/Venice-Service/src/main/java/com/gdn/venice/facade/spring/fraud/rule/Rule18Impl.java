package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule18DAO;
import com.gdn.venice.dao.FrdRuleConfigTresholdDAO;
import com.gdn.venice.dao.VenAddressDAO;
import com.gdn.venice.persistence.FrdParameterRule18;
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Validity of Wording Customer, Shipping, Billing Address
 */
@Service("Rule18")
public class Rule18Impl implements Rule {
	private static final String CLASS_NAME = Rule18Impl.class.getCanonicalName();
	
	@Autowired
	FrdRuleConfigTresholdDAO frdRuleConfigTresholdDAO;
	@Autowired
	FrdParameterRule18DAO frdParameterRule18DAO;
	@Autowired
	VenAddressDAO venAddressDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		String codePattern = getCodePattern();
		
		if(isOneOfTheAddressesDoesntMatchPattern(codePattern, order))
			totalRiskPoint = new Integer(frdRuleConfigTresholdDAO.findByKeyDaySpanForFraudParameter18().getValue());
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public String getCodePattern(){
		List<FrdParameterRule18> ruleList = frdParameterRule18DAO.findAll();
		StringBuilder codeBuilder = new StringBuilder("|");
		
		for (FrdParameterRule18 rule : ruleList) {
			codeBuilder.append("(");
			codeBuilder.append(rule.getCode().toLowerCase());
			codeBuilder.append(")");
			codeBuilder.append("(\\d+)");
		}
		
		return codeBuilder.toString();
	}
	
	public boolean isOneOfTheAddressesDoesntMatchPattern(String codePattern, VenOrder order){
		
		String orderAddress = getOrderAddress(order);
		if(!isValueMatchesPattern(codePattern, orderAddress))
			return true;
		
		List<String> itemAddressList = getItemAddress(order);
		for (String itemAddress : itemAddressList) {
			if(!isValueMatchesPattern(codePattern, itemAddress))
				return true;
		}
		
		List<String> paymentAddressList = getPaymentAddress(order);
		for (String paymentAddress : paymentAddressList) {
			if(!isValueMatchesPattern(codePattern, paymentAddress))
				return true;
		}
		
		return false;
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
	
	public boolean isValueMatchesPattern(String codePattern, String value){
		Pattern pattern = Pattern.compile(codePattern);
		Matcher matcher = pattern.matcher(value);
		
		return matcher.find();
	}
	
	
}