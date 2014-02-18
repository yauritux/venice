package com.gdn.venice.server.app.fraud.rule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.facade.FrdParameterRule41SessionEJBRemote;
import com.gdn.venice.facade.VenMigsUploadMasterSessionEJBRemote;
import com.gdn.venice.persistence.FrdParameterRule41;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.util.VeniceConstants;

/**
 * Class for calculate fraud rule 41: Cek Attemp
 *
 * @author Daniel Hutama Putra
 */

public class Rule41 {
	protected static Logger _log = null;
    
    public Rule41() {
        super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.fraud.rule.Rule41");
    }
    
    public int execute(String wcsOrderId) throws Exception{
    	_log.info("Start execute rule 41");
        int fraudPoint=0;  
        int attemptRiskPoint = 0;
        int attemptCCRiskPoint = 0;
        List<VenMigsUploadMaster> migsUploadList = null;
        
        try{
            migsUploadList=getAttemptPaymentList(wcsOrderId);
        	
            attemptRiskPoint=getRiskPoint(VeniceConstants.FRD_PARAMETER_RULE_41_ATTEMPT);
            attemptCCRiskPoint=getRiskPoint(VeniceConstants.FRD_PARAMETER_RULE_41_ATTEMPT_CC);
            
            if(migsUploadList.size()>0){
            	fraudPoint=(attemptRiskPoint*migsUploadList.size())+(attemptCCRiskPoint*(getAttemptCC(migsUploadList).size()-1));
            }
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception();
        } 
        _log.info("Done execute rule 41, fraudPoint is: "+fraudPoint);
        return fraudPoint;
    }
    
    public List<VenMigsUploadMaster> getAttemptPaymentList(String wcsOrderId) throws Exception{

        List<VenMigsUploadMaster> migsUploadList = null;
        Locator<Object> locator = null;
        
        try{
        	locator = new Locator<Object>();
        	VenMigsUploadMasterSessionEJBRemote sessionHome = (VenMigsUploadMasterSessionEJBRemote) locator.lookup(VenMigsUploadMasterSessionEJBRemote.class, "VenMigsUploadMasterSessionEJBBean");
             
        	String query = "select o from VenMigsUploadMaster o where (o.merchantTransactionReference like '"+ wcsOrderId +"-%' or o.merchantTransactionReference =" +
    		"'"+wcsOrderId+"') and o.responseCode <> '"+VeniceConstants.VEN_MIGS_MASTER_UPLOAD_RESPONSE_CODE_APPROVE+"'";
            migsUploadList=sessionHome.queryByRange(query,0,0);
            
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception();
        }finally {
            try {
                if (locator != null) {
                    locator.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return migsUploadList;
    }
    
    public int getRiskPoint(String parameter) throws Exception{
    	int riskPoint=0;
        List<FrdParameterRule41> frdParameterRule41List = null;
        Locator<Object> locator = null;
    	
    	try{
    		locator = new Locator<Object>();
        	FrdParameterRule41SessionEJBRemote rule41SessionHome =  (FrdParameterRule41SessionEJBRemote) locator.lookup(FrdParameterRule41SessionEJBRemote.class, "FrdParameterRule41SessionEJBBean");
        
            String query = "select o from FrdParameterRule41 o";
            frdParameterRule41List=rule41SessionHome.queryByRange(query, 0, 0);
            
            for(FrdParameterRule41 frdParameterRule41: frdParameterRule41List){
            	if(frdParameterRule41.getDescription().trim().equals(parameter)){
            		riskPoint = frdParameterRule41.getRiskPoint();
            	}
            }
    	}catch(Exception e){
            e.printStackTrace();
            throw new Exception();
        }finally {
            try {
                if (locator != null) {
                    locator.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
		return riskPoint;
    }
    
    public Set<String> getAttemptCC(List<VenMigsUploadMaster> migsUploadMasterList){
    	Set<String> CCList = new HashSet<String>();
    	for(VenMigsUploadMaster venMigsUploadMaster  :migsUploadMasterList){
    		CCList.add(venMigsUploadMaster.getCardNumber());
    	}
		return CCList;
    }
    
}
