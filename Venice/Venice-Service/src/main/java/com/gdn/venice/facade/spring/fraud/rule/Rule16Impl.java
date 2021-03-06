package com.gdn.venice.facade.spring.fraud.rule;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule16DAO;
import com.gdn.venice.dao.FrdRuleConfigTresholdDAO;
import com.gdn.venice.dao.VenOrderAddressDAO;
import com.gdn.venice.dao.VenOrderContactDetailDAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.persistence.FrdRuleConfigTreshold;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderAddress;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.util.CommonUtil;

/**
 * Same customer with different credit card
 */
@Service("Rule16")
public class Rule16Impl implements Rule {
	private static final String CLASS_NAME = Rule16Impl.class.getCanonicalName();
	private static final SimpleDateFormat SDF_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	FrdRuleConfigTresholdDAO frdRuleConfigTresholdDAO;
	@Autowired
	FrdParameterRule16DAO frdParameterRule16DAO;
	@Autowired
	VenOrderDAO venOrderDAO;
	@Autowired
	VenOrderContactDetailDAO venOrderContactDetailDAO;
	@Autowired
	VenOrderAddressDAO venOrderAddressDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		int noOfOrderHit = 0;
		
		String customerUsernameCurrentOrder = getCustomerUsername(order);
		String customerNameCurrentOrder = getCustomerName(order);
		String customerEmailCurrentOrder = getCustomerEmail(order);
		String customerAddressCurrentOrder = getCustomerAddress(order);
		Timestamp orderTimestamp = order.getOrderDate();
		
		List<VenOrder> otherOrderList = getOrdersToInvestigate(order, getStartRangeDate(orderTimestamp), getEndRangeDate(orderTimestamp));
		
		for (VenOrder otherOrder : otherOrderList) {
			CommonUtil.logInfo(CLASS_NAME, "Comparing with Order : " + otherOrder.getWcsOrderId());
			
			String customerUsernameOtherOrder = getCustomerUsername(otherOrder);
			String customerNameOtherOrder = getCustomerName(otherOrder);
			
			if(customerUsernameCurrentOrder.equalsIgnoreCase(customerUsernameOtherOrder) || 
			   customerNameCurrentOrder.equalsIgnoreCase(customerNameOtherOrder) 
			   && !customerUsernameOtherOrder.trim().isEmpty() && !customerNameOtherOrder.trim().isEmpty()){
				CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + " matched username/customer name  with Order : " + otherOrder.getWcsOrderId());
				noOfOrderHit++;
				continue;
			}
			
			String customerEmailOtherOrder = getCustomerEmail(otherOrder);
			if(customerEmailCurrentOrder.equalsIgnoreCase(customerEmailOtherOrder) && !customerEmailCurrentOrder.trim().isEmpty()){
				CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + " matched email with Order : " + otherOrder.getWcsOrderId());
				noOfOrderHit++;
				continue;
			}
			
			String customerAddressOtherOrder = getCustomerAddress(otherOrder);
			if(customerAddressCurrentOrder.equalsIgnoreCase(customerAddressOtherOrder) && !customerAddressCurrentOrder.trim().isEmpty()){
				CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + " matched address with Order : " + otherOrder.getWcsOrderId());
				noOfOrderHit++;
				continue;
			}
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + " total hit with other order : " + noOfOrderHit);
		
		if(noOfOrderHit > 0){
			int riskPointForEachHit = frdParameterRule16DAO.findByCode("SAME_NAME_EMAIL_ADDRESS").getRiskPoint();
			totalRiskPoint = noOfOrderHit * riskPointForEachHit;
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int getDaySpanFromDBConfig(){
		FrdRuleConfigTreshold config = frdRuleConfigTresholdDAO.findByKeyDaySpanForFraudParameter16();
		return new Integer(config.getValue());
	}
	
	public Date getStartRangeDate(Timestamp timestamp){
		int daySpan = getDaySpanFromDBConfig();
		Date startRangeDate = DateUtils.addDays(timestamp, -daySpan);
		
		return startRangeDate;
	}
	
	public Date getEndRangeDate(Timestamp timestamp){
		
		return timestamp;
	}
	
	public Timestamp getOrderDateTimestamp(VenOrder order){
		return order.getOrderDate();
	}
	
	public List<VenOrder> getOrdersToInvestigate(VenOrder otherThanThisOrder, Date startDate, Date endDate){
		return venOrderDAO.findOtherByStatusCOrderDateRange(otherThanThisOrder, startDate, endDate);
	}
	
	public String getCustomerUsername(VenOrder order){
		
		VenOrder orderWithCustomer = venOrderDAO.findWithVenCustomerByOrder(order.getOrderId());
		
		try{
			return orderWithCustomer.getVenCustomer().getVenParty().getFullOrLegalName();	
		}catch (Exception e) {
			return "";
		}
		
	}
	
	public String getCustomerName(VenOrder order){
		
		VenOrder orderWithCustomer = venOrderDAO.findWithVenCustomerByOrder(order.getOrderId());
		
		try{
			return orderWithCustomer.getVenCustomer().getCustomerUserName();
		}catch (Exception e) {
			return "";
		}
		
	}
	
	public String getCustomerEmail(VenOrder order){
		
		VenOrderContactDetail contact = venOrderContactDetailDAO.findByContactEmailVenOrder(order.getOrderId());
		return (contact == null ? "" : contact.getVenContactDetail().getContactDetail());
		
	}
	
	public String getCustomerAddress(VenOrder order){
		
		VenOrderAddress orderAddress = venOrderAddressDAO.findWithVenAddressByVenOrder(order);
		
		try{
			return (orderAddress.getVenAddress().getStreetAddress1()!=null?orderAddress.getVenAddress().getStreetAddress1().trim():"") + 
			       (orderAddress.getVenAddress().getPostalCode()!=null?orderAddress.getVenAddress().getPostalCode().trim():"");
		}catch (Exception e) {
			return "";
		}
		
	}
	
}