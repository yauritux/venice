package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule37DAO;
import com.gdn.venice.dao.VenMigsUploadMasterDAO;
import com.gdn.venice.persistence.FrdParameterRule37;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * MIGS History with same credit card
 */
@Service("Rule37")
public class Rule37Impl implements Rule{
	private static final String CLASS_NAME = Rule37Impl.class.getCanonicalName();
	
	@Autowired
	VenMigsUploadMasterDAO venMigsUploadMasterDAO;
	@Autowired
	FrdParameterRule37DAO frdParameterRule37DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		FrdParameterRule37 rule = frdParameterRule37DAO.findAll().get(0);
		
		List<VenMigsUploadMaster> migsList = venMigsUploadMasterDAO.findByMerchantReferenceCardNumberNotInOrderPaymentResposeCodeNotApproved(order.getWcsOrderId(), order.getOrderId());
		
		if(migsList.size() > 0){
			totalRiskPoint = rule.getRiskPoint() * migsList.size();
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
		
}