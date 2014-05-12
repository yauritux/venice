package com.gdn.venice.facade.spring.fraud.rule;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import com.gdn.venice.dao.FrdParameterRule47DAO;
import com.gdn.venice.dao.VenCustomerDAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.persistence.VenCustomer;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

/**
 * First time shopper
 */
@Service("Rule47")
public class Rule47Impl implements Rule{
	private static final String CLASS_NAME = Rule47Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule47DAO frdParameterRule47DAO;
	
	@Autowired
	VenCustomerDAO venCustomerDAO;
	
	@Autowired
	VenOrderDAO venOrderDAO;
	
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	public int getRiskPoint(VenOrder order){
		CommonUtil.logInfo(CLASS_NAME, "Calculating Order : " + order.getWcsOrderId());
		
		VenCustomer customer = venCustomerDAO.findOne(order.getVenCustomer().getCustomerId());
		List<VenOrderPaymentAllocation> orderAllocation= venOrderPaymentAllocationDAO.findByVenOrder(order);
		
		int totalSameCC=0;
		int totalDifferenCC=0;
		int totalRiskPoint=0;
		
		 List<VenOrder> venOrders = venOrderDAO.findByVenCustomerEmailWithOrderOutOfAMonth(customer,minusOneMonth(order.getOrderDate()));
		 
		 for(VenOrder venOrder:venOrders){
			 List<VenOrderPaymentAllocation> orderAllocationInAMonth = venOrderPaymentAllocationDAO.findByVenOrder(venOrder);
			 if(venOrder!=order && orderAllocationInAMonth.get(0).getVenOrderPayment().getMaskedCreditCardNumber().equals(orderAllocation.get(0).getVenOrderPayment().getMaskedCreditCardNumber())){
				 totalSameCC=totalSameCC+1;
			 }
		 }

		 totalDifferenCC=venOrders.size()-totalSameCC;
		 
		 if(totalSameCC>=2){
			 totalRiskPoint=totalRiskPoint+getRiskPoint("same CC, order history>2");
		 }else if(totalSameCC<2){
			 totalRiskPoint=totalRiskPoint+getRiskPoint("same CC, order history<2"); 
		 }
		 
		 if(totalDifferenCC>=2){
			 totalRiskPoint=totalRiskPoint+getRiskPoint("different CC, order history>2");	 
		 }else if(totalDifferenCC<2){
			 totalRiskPoint=totalRiskPoint+getRiskPoint("different CC, order history<2");
		 }
		 
		 return totalRiskPoint;
	}
	
	public Date minusOneMonth(Date date){
		return DateUtils.addMonths(date, -1);
	}
	
	public int getRiskPoint(String Description){
		return frdParameterRule47DAO.findByDescription(Description).getValue();
	} 
	
	
}
