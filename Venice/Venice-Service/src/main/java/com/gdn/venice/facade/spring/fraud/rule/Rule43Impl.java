package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule42DAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

/**
 * Total Payment < 1 jt
 */
@Service("Rule43")
public class Rule43Impl implements Rule {
	private static final String CLASS_NAME = Rule43Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule42DAO frdParameterRule42DAO;
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		int paymentLimit = getPaymentValue();
		int riskPoint = getRiskPointValue();
		
		List<VenOrderPaymentAllocation> migsUploadList = venOrderPaymentAllocationDAO.findByVenOrderOrderPaymentLessThanLimit(order, paymentLimit);
		
		if(migsUploadList.size() > 0)
			totalRiskPoint = riskPoint;
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int getRiskPointValue(){
		return frdParameterRule42DAO.findByDescription("Risk Point").getRiskPoint();
	}
	
	public int getPaymentValue(){
		return frdParameterRule42DAO.findByDescription("Payment").getRiskPoint();
	}
		
}