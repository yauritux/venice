package com.gdn.venice.facade.spring.fraud.rule;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule26272829DAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.persistence.FrdParameterRule26272829;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Same  Customer Email in one week
 */
@Service("Rule26")
public class Rule26Impl implements Rule{
	private static final String CLASS_NAME = Rule26Impl.class.getCanonicalName();
	private static final SimpleDateFormat SDF_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	FrdParameterRule26272829DAO frdParameterRule26272829DAO;
	@Autowired
	VenOrderDAO venOrderDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		VenOrder orderWithCustomer = venOrderDAO.findWithVenCustomerByOrder(order.getOrderId());
		Date startDate = getStartRangeDate(order.getOrderDate());
		Date endDate = getEndRangeDate(order.getOrderDate());
		
		FrdParameterRule26272829 rule = frdParameterRule26272829DAO.findByRule26OrderFromSameCustomerInOrderDateRange(orderWithCustomer.getVenCustomer().getCustomerId(), startDate, endDate, order.getOrderId());
		
		if (rule != null) {
			totalRiskPoint = rule.getRiskPoint();
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public Date getStartRangeDate(Timestamp timestamp){
		int weekSpan = -1;
		Date startRangeDate = DateUtils.addWeeks(timestamp, weekSpan);
		return startRangeDate;
	}
	
	public Date getEndRangeDate(Timestamp timestamp){
		
		return timestamp;
	}
		
}