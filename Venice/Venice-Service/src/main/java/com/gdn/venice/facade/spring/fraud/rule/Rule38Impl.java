package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule38DAO;
import com.gdn.venice.dao.VenMigsUploadMasterDAO;
import com.gdn.venice.persistence.FrdParameterRule38;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * MIGS History with different credit card
 */
@Service("Rule38")
public class Rule38Impl implements Rule {
	private static final String CLASS_NAME = Rule38Impl.class.getCanonicalName();
	
	@Autowired
	VenMigsUploadMasterDAO venMigsUploadMasterDAO;
	@Autowired
	FrdParameterRule38DAO frdParameterRule38DAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		List<VenMigsUploadMaster> migsMasterList = venMigsUploadMasterDAO.findByMerchantReferenceCardNumberNotInOrderPaymentResposeCodeNotApproved(order.getWcsOrderId(), order.getOrderId());
		
		if(migsMasterList.size() > 0){
			List<FrdParameterRule38> ruleList = frdParameterRule38DAO.findAll();
			
			totalRiskPoint = ruleList.get(0).getRiskPoint() * migsMasterList.size();
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
		
}
