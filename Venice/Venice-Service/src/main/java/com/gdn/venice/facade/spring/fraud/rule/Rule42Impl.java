package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule42DAO;
import com.gdn.venice.dao.VenMigsUploadMasterDAO;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Cek dana fund in MIGS (multiple fund in)
 */
@Service("Rule42")
public class Rule42Impl implements Rule {
	private static final String CLASS_NAME = Rule42Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule42DAO frdParameterRule42DAO;
	@Autowired
	VenMigsUploadMasterDAO venMigsUploadMasterDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		int riskPoint = getRiskPointForMultipleFundIn();
		List<VenMigsUploadMaster> migsList = venMigsUploadMasterDAO.findByMerchantReferenceResposeCodeApproved(order.getWcsOrderId());
		
		if(migsList.size() > 1){
			totalRiskPoint = riskPoint*(migsList.size()-1);
		}
		
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int getRiskPointForMultipleFundIn(){
    	return frdParameterRule42DAO.findByDescription("multiple fund in").getRiskPoint();
    }
		
}