package com.gdn.venice.server.app.fraud.rule;

import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.facade.FrdParameterRule42SessionEJBRemote;
import com.gdn.venice.facade.VenMigsUploadMasterSessionEJBRemote;
import com.gdn.venice.persistence.FrdParameterRule42;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.util.VeniceConstants;

/**
 * Class for calculate fraud rule 41: Cek dana fund in MIGS (multiple fund in)
 *
 * @author Daniel Hutama Putra
 */

public class Rule42 {
	protected static Logger _log = null;
    
    public Rule42() {
        super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.fraud.rule.Rule41");
    }
    
    public int execute(String wcsOrderId) throws Exception{
    	_log.info("Start execute rule 42");
        int fraudPoint=0;  
        List<VenMigsUploadMaster> migsUploadList = null;
        try{ 
            
        migsUploadList=getVenMigsUploadMasterList(wcsOrderId);
        
        if(migsUploadList.size()>1){
        	fraudPoint=getRiskPoint(VeniceConstants.FRD_PARAMETER_RULE_42_MULTIPLE_FUND_IN)*(migsUploadList.size()-1);
        }
	     
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception();
        }
        _log.info("Done execute rule 42, fraudPoint is: "+fraudPoint);
        return fraudPoint;
    }
    
    public List<VenMigsUploadMaster> getVenMigsUploadMasterList(String wcsOrderId) throws Exception{

        List<VenMigsUploadMaster> migsUploadList = null;

        Locator<Object> locator = null;
    	try{

            locator = new Locator<Object>();
            VenMigsUploadMasterSessionEJBRemote sessionHome = (VenMigsUploadMasterSessionEJBRemote) locator.lookup(VenMigsUploadMasterSessionEJBRemote.class, "VenMigsUploadMasterSessionEJBBean");
           
            String query =  "select o from VenMigsUploadMaster o where (o.merchantTransactionReference like '"+ wcsOrderId +"-%' or o.merchantTransactionReference = " +
    		"'"+wcsOrderId+"') and o.responseCode = '"+VeniceConstants.VEN_MIGS_MASTER_UPLOAD_RESPONSE_CODE_APPROVE+"'";
            migsUploadList=sessionHome.queryByRange(query,0,0);

    	}catch(Exception e){
            e.printStackTrace();
            throw new Exception();
        } finally {
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
        Locator<Object> locator = null;
        List<FrdParameterRule42> frdParameterRule42List = null;
    	
    	try{
    		
    		locator = new Locator<Object>();
    		FrdParameterRule42SessionEJBRemote rule42SessionHome = (FrdParameterRule42SessionEJBRemote) locator.lookup(FrdParameterRule42SessionEJBRemote.class, "FrdParameterRule42SessionEJBBean");
            
    		String query = "select o from FrdParameterRule42 o where o.description = '"+parameter+"'";
            frdParameterRule42List=rule42SessionHome.queryByRange(query, 0, 0);
            riskPoint=frdParameterRule42List.get(0).getRiskPoint();
            
    	} finally {
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
    
}
