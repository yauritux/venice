package com.gdn.venice.facade.spring.fraud.rule;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdBlacklistReasonDAO;
import com.gdn.venice.dao.FrdParameterRule35DAO;
import com.gdn.venice.dao.FrdRuleConfigTresholdDAO;
import com.gdn.venice.dao.VenOrderContactDetailDAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.persistence.FrdBlacklistReason;
import com.gdn.venice.persistence.FrdParameterRule35;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

/**
 * Grey list
 */
@Service("Rule35")
public class Rule35Impl implements Rule{
	private static final String CLASS_NAME = Rule35Impl.class.getCanonicalName();
	
	private static final String BLACKLIST_REASON_CUSTOMER_NAME = "Customer name grey list"; 
	private static final String BLACKLIST_REASON_CUSTOMER_EMAIL = "Customer email grey list"; 
	private static final String BLACKLIST_REASON_CUSTOMER_CC_NUMBER = "Customer cc number grey list"; 
	
	@Autowired
	FrdParameterRule35DAO frdParameterRule35DAO;
	@Autowired
	FrdRuleConfigTresholdDAO frdRuleConfigTresholdDAO;
	@Autowired
	VenOrderDAO venOrderDAO;
	@Autowired
	VenOrderContactDetailDAO venOrderContactDetailDAO;
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	@Autowired
	FrdBlacklistReasonDAO frdBlacklistReasonDAO;
	
	public int getRiskPoint(VenOrder order){
		ArrayList<String> greyListReason=new ArrayList<String>(3);
		int totalRiskPoint = 0;
		
		VenOrder orderWithCustomer = venOrderDAO.findWithVenCustomerByOrder(order.getOrderId());
		
		if(isCustomerNameInGreyList(orderWithCustomer)){
			greyListReason.add(BLACKLIST_REASON_CUSTOMER_NAME);
			
			CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + " " + BLACKLIST_REASON_CUSTOMER_NAME);
		}
		
		if(isCustomerEmailInGreyList(orderWithCustomer)){
			greyListReason.add(BLACKLIST_REASON_CUSTOMER_EMAIL);
			
			CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + " " + BLACKLIST_REASON_CUSTOMER_EMAIL);
		}
		
		if(isCreditCardNoInGreyList(orderWithCustomer)){
			greyListReason.add(BLACKLIST_REASON_CUSTOMER_CC_NUMBER);
			
			CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + " " + BLACKLIST_REASON_CUSTOMER_CC_NUMBER);
		}
		
		saveBlackListReason(order, greyListReason);
		
		totalRiskPoint = getRiskPoint(greyListReason.size());
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int getRiskPoint(int greyList){
		if(greyList > 0){
			return new Integer(frdRuleConfigTresholdDAO.findByKeyFrdParameterRule35().getValue());
		}else{
			return 0;
		}
	}
	
	public void saveBlackListReason(VenOrder order, ArrayList<String> greyListReason){
		for(int i=0;i<greyListReason.size();i++){
			
			FrdBlacklistReason reason = new FrdBlacklistReason();
			reason.setOrderId(order.getOrderId());
			reason.setWcsOrderId(order.getWcsOrderId());
			reason.setBlacklistReason(greyListReason.get(i));
			
			frdBlacklistReasonDAO.save(reason);
		}
	}
	
	public boolean isCustomerNameInGreyList(VenOrder order){
		return getCustomerNameGreyList(order).size() > 0;
	}
	
	public boolean isCustomerEmailInGreyList(VenOrder order){
		return getCustomerEmailGreyList(getCustomerEmail(order)).size() > 0;
	}
	
	public boolean isCreditCardNoInGreyList(VenOrder order){
		return getCreditCardGreyList(getCreditCardNo(order)).size() > 0;
	}
	
	public List<FrdParameterRule35> getCustomerNameGreyList(VenOrder order){
		return frdParameterRule35DAO.findByUpperCaseCustomerName(order.getVenCustomer().getVenParty().getFullOrLegalName().toUpperCase());
	}
	
	public VenOrderContactDetail getCustomerEmail(VenOrder order){
		return venOrderContactDetailDAO.findByContactEmailVenOrder(order.getOrderId());
	}
	
	public List<FrdParameterRule35> getCustomerEmailGreyList(VenOrderContactDetail orderContactDetail){
		String customerEmail = orderContactDetail.getVenContactDetail().getContactDetail();
		return frdParameterRule35DAO.findByUpperCaseCustomerEmail(customerEmail.toUpperCase());
	}
	
	public VenOrderPaymentAllocation getCreditCardNo(VenOrder order){
		List<VenOrderPaymentAllocation> orderPaymentAllocationList = venOrderPaymentAllocationDAO.findByVenOrderPaymentTypeCC(order);
		return orderPaymentAllocationList.get(0);
	}
	
	public List<FrdParameterRule35> getCreditCardGreyList(VenOrderPaymentAllocation orderPaymentAllocation){
		String maskedCreditCardNo = orderPaymentAllocation.getVenOrderPayment().getMaskedCreditCardNumber();
		return frdParameterRule35DAO.findByCcNumber(maskedCreditCardNo);
	}
		
}