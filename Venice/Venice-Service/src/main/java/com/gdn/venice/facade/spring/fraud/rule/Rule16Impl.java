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
		
		List<VenOrder> otherOrderList = venOrderDAO.findOtherByStatusCOrderDateRange(order, getStartRangeDate(orderTimestamp), getEndRangeDate(orderTimestamp));
		
		for (VenOrder otherOrder : otherOrderList) {
			
			String customerUsernameOtherOrder = getCustomerName(otherOrder);
			String customerNameOtherOrder = getCustomerName(otherOrder);
			
			if(customerUsernameCurrentOrder.equalsIgnoreCase(customerUsernameOtherOrder) || 
			   customerNameCurrentOrder.equalsIgnoreCase(customerNameOtherOrder)){
				CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + " matched username/customer name  with Order : " + otherOrder.getWcsOrderId());
				noOfOrderHit++;
				continue;
			}
			
			String customerEmailOtherOrder = getCustomerEmail(otherOrder);
			if(customerEmailCurrentOrder.equalsIgnoreCase(customerEmailOtherOrder)){
				CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + " matched email with Order : " + otherOrder.getWcsOrderId());
				noOfOrderHit++;
				continue;
			}
			
			String customerAddressOtherOrder = getCustomerAddress(otherOrder);
			if(customerAddressCurrentOrder.equalsIgnoreCase(customerAddressOtherOrder)){
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
	
	public String getStartRangeDate(Timestamp timestamp){
		int daySpan = getDaySpanFromDBConfig();
		Date startRangeDate = DateUtils.addDays(timestamp, -daySpan);
		
		return SDF_TIMESTAMP.format(startRangeDate);
	}
	
	public String getEndRangeDate(Timestamp timestamp){
		
		return SDF_TIMESTAMP.format(timestamp.getTime());
	}
	
	public Timestamp getOrderDateTimestamp(VenOrder order){
		return order.getOrderDate();
	}
	
	public List<VenOrder> getOrdersToInvestigate(VenOrder otherThanThisOrder, String startDate, String endDate){
		return venOrderDAO.findOtherByStatusCOrderDateRange(otherThanThisOrder, startDate, endDate);
	}
	
	public String getCustomerUsername(VenOrder order){
		
		VenOrder orderWithCustomer = venOrderDAO.findWithVenCustomerByOrder(order);
		return orderWithCustomer.getVenCustomer().getVenParty().getFullOrLegalName();
		
	}
	
	public String getCustomerName(VenOrder order){
		
		VenOrder orderWithCustomer = venOrderDAO.findWithVenCustomerByOrder(order);
		return orderWithCustomer.getVenCustomer().getCustomerUserName();
		
	}
	
	public String getCustomerEmail(VenOrder order){
		
		VenOrderContactDetail contact = venOrderContactDetailDAO.findByContactEmailVenOrder(order);
		return contact.getVenContactDetail().getContactDetail();
		
	}
	
	public String getCustomerAddress(VenOrder order){
		
		VenOrderAddress orderAddress = venOrderAddressDAO.findWithVenAddressByVenOrder(order);
		return (orderAddress.getVenAddress().getStreetAddress1()!=null?orderAddress.getVenAddress().getStreetAddress1().trim():"") + 
		       (orderAddress.getVenAddress().getPostalCode()!=null?orderAddress.getVenAddress().getPostalCode().trim():"");
		
	}
	
}