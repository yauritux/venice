package com.gdn.venice.facade.spring.fraud.rule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.FrdParameterRule41DAO;
import com.gdn.venice.dao.VenMigsUploadMasterDAO;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.util.CommonUtil;

/**
 * Cek Attempt
 */
@Service("Rule41")
public class Rule41Impl implements Rule{
	private static final String CLASS_NAME = Rule41Impl.class.getCanonicalName();
	
	@Autowired
	FrdParameterRule41DAO frdParameterRule41DAO;
	@Autowired
	VenMigsUploadMasterDAO venMigsUploadMasterDAO;
	
	
	public int getRiskPoint(VenOrder order){
		int totalRiskPoint = 0;
		
		List<VenMigsUploadMaster> migsUploadList = getAttemptPaymentList(order.getWcsOrderId());
		int riskPointAttempt = getRiskPointAttempt();
		int riskPointAttemptCC = getRiskPointAttemptCC();
		
		if(migsUploadList.size() > 0){
			totalRiskPoint = compileRiskPoint(riskPointAttempt, riskPointAttemptCC, migsUploadList);
		}
			
		CommonUtil.logInfo(CLASS_NAME, "Order : " + order.getWcsOrderId() + ", Total Risk Point : " + totalRiskPoint);
		
		return totalRiskPoint;
	}
	
	public int compileRiskPoint(int riskPointAttempt, int riskPointAttemptCC, List<VenMigsUploadMaster> migsUploadList){
		return (riskPointAttempt*migsUploadList.size())+(riskPointAttemptCC*(getTotalUniqueAttemptCC(migsUploadList)-1));
	}
	
	public int getRiskPointAttempt(){
		return frdParameterRule41DAO.findByDescription("attempt").getRiskPoint();
	}
	
	public int getRiskPointAttemptCC(){
		return frdParameterRule41DAO.findByDescription("attempt CC").getRiskPoint();
	} 
	
	public List<VenMigsUploadMaster> getAttemptPaymentList(String wcsOrderId){
		return venMigsUploadMasterDAO.findByMerchantReferenceResposeCodeApproved(wcsOrderId); 
	}
	
	public int getTotalUniqueAttemptCC(List<VenMigsUploadMaster> migsUploadMasterList){
    	Set<String> CCList = new HashSet<String>();
    	for(VenMigsUploadMaster venMigsUploadMaster  :migsUploadMasterList){
    		CCList.add(venMigsUploadMaster.getCardNumber());
    	}
		return CCList.size();
    }
		
}