package com.gdn.venice.facade.spring.fraud.rule;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdCustomerWhitelistBlacklistDAO;
import com.gdn.venice.dao.FrdParameterRule48DAO;
import com.gdn.venice.dao.VenMigsUploadMasterDAO;
import com.gdn.venice.persistence.FrdCustomerWhitelistBlacklist;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Cek Attempt
 */
@Service("Rule48")
public class Rule48Impl implements Rule{
	private static final String CLASS_NAME = Rule48Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule48DAO frdParameterRule48DAO;

	@Autowired
	VenMigsUploadMasterDAO venMigsUploadMasterDAO;
	
	@Autowired
	FrdCustomerWhitelistBlacklistDAO frdCustomerWhitelistBlacklistDAO;
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		int totalCard=0;
		
		List<VenMigsUploadMaster> migsUploadList = getAttemptPaymentList(order.getWcsOrderId());
		
		for(VenMigsUploadMaster venMigsUploadMaster:migsUploadList){
			List<FrdCustomerWhitelistBlacklist> frdCustomerWhitelistBlacklist = frdCustomerWhitelistBlacklistDAO.findByCcNumber(venMigsUploadMaster.getCardNumber());
			if(frdCustomerWhitelistBlacklist.size()>0){
				totalCard++;
			}
		}
		
		totalRiskPoint=compileRiskPoint(totalCard);
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int compileRiskPoint(int totalCard){
		return (totalCard*getRiskPoint());
	}

	public int getRiskPoint(){
		return frdParameterRule48DAO.findByDescription("RiskPoint").getValue();
	} 
	
	public List<VenMigsUploadMaster> getAttemptPaymentList(String wcsOrderId){
		return venMigsUploadMasterDAO.findByMerchantReferenceResposeCodeApproved(wcsOrderId); 
	}	
}