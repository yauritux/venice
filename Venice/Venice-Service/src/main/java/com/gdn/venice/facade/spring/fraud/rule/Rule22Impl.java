package com.gdn.venice.facade.spring.fraud.rule;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule22DAO;
import com.gdn.venice.dao.VenCustomerDAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.persistence.FrdParameterRule22;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Customer Shopping Limit per Month
 */

@Service("Rule22")
public class Rule22Impl implements Rule {
	private static final String CLASS_NAME = Rule22Impl.class.getCanonicalName();
	private static final SimpleDateFormat SDF_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	FrdParameterRule22DAO frdParameterRule22DAO;
	@Autowired
	VenOrderDAO venOrderDAO;
	@Autowired
	VenCustomerDAO venCustomerDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		BigDecimal totalAmount = getTotalOrderAmountInAMonth(order);
		
		FrdParameterRule22 rule = frdParameterRule22DAO.findByAmountRange(totalAmount);
		totalRiskPoint = rule.getRiskPoint();
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public BigDecimal getTotalOrderAmountInAMonth(VenOrder order){
		Date startDate = minusOneMonth(order.getOrderDate());
		Date endDate = order.getOrderDate();
		
		VenOrder orderWithCustomer = venOrderDAO.findWithVenCustomerByOrder(order);
		
		BigDecimal totalAmount = venOrderDAO.getAmountSumByOrderOrCustomerOrderDateRange(orderWithCustomer, orderWithCustomer.getVenCustomer(), startDate, endDate);
		
		return totalAmount;
	}
	
	public String convertToString(Date date){
		return SDF_TIMESTAMP.format(date.getTime());
	}
	
	public Date minusOneMonth(Date date){
		return DateUtils.addMonths(date, -1);
	}
	
	
}