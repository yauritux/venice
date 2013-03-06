package com.gdn.venice.server.app.fraud.rule;

import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.facade.FrdParameterRule43SessionEJBRemote;
import com.gdn.venice.facade.VenMigsUploadMasterSessionEJBRemote;
import com.gdn.venice.facade.VenOrderPaymentAllocationSessionEJBRemote;
import com.gdn.venice.persistence.FrdParameterRule43;
import com.gdn.venice.persistence.VenMigsUploadMaster;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.VeniceConstants;

/**
 * Class for calculate fraud rule 43: Total Payment < 1 jt
 *
 * @author Daniel Hutama Putra
 */

public class Rule43 {
	protected static Logger _log = null;
    
    public Rule43() {
        super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.fraud.rule.Rule43");
    }
    
    public int execute(long orderId) throws Exception{
    	_log.info("Start execute rule 43");
        int fraudPoint=0;  
        int limitPayment;
        List<VenOrderPaymentAllocation> migsUploadList = null;
        
        try{
        limitPayment=	getValue(VeniceConstants.FRD_PARAMETER_RULE_43_PAYMENT);
        migsUploadList=getVenOrderPaymentAllocationList(orderId, limitPayment);
        if(migsUploadList.size()>0){
        	fraudPoint=fraudPoint+getValue(VeniceConstants.FRD_PARAMETER_RULE_43_RISK_POINT);
        }
	     
        }catch(Exception e){
            e.printStackTrace();
        }
        _log.info("Done execute rule 43, fraudPoint is: "+fraudPoint);
        return fraudPoint;
    }
    
    public List<VenOrderPaymentAllocation> getVenOrderPaymentAllocationList(Long orderId, int limitPayment) throws Exception{

        List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = null;

        Locator<Object> locator = null;
    	try{

            locator = new Locator<Object>();
            VenOrderPaymentAllocationSessionEJBRemote sessionHome = (VenOrderPaymentAllocationSessionEJBRemote) locator.lookup(VenOrderPaymentAllocationSessionEJBRemote.class, "VenOrderPaymentAllocationSessionEJBBean");
           
            String query =  "select o from VenOrderPaymentAllocation o where o.venOrder.orderId = "+orderId+" and o.venOrderPayment.amount < "+limitPayment;
            venOrderPaymentAllocationList=sessionHome.queryByRange(query,0,0);

    	}catch(Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (locator != null) {
                    locator.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
		return venOrderPaymentAllocationList;
    }
    
    public int getValue(String parameter) throws Exception{
    	int riskPoint=0;
        Locator<Object> locator = null;
        List<FrdParameterRule43> frdParameterRule43List = null;
    	
    	try{
    		
    		locator = new Locator<Object>();
    		FrdParameterRule43SessionEJBRemote rule43SessionHome = (FrdParameterRule43SessionEJBRemote) locator.lookup(FrdParameterRule43SessionEJBRemote.class, "FrdParameterRule43SessionEJBBean");
            
    		String query = "select o from FrdParameterRule43 o where o.description = '"+parameter+"'";
            frdParameterRule43List=rule43SessionHome.queryByRange(query, 0, 0);
            riskPoint=frdParameterRule43List.get(0).getValue();
            
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
