package com.gdn.venice.server.app.fraud.rule;


import java.util.List;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Locator;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.facade.FrdParameterRule44SessionEJBRemote;
import com.gdn.venice.facade.VenMerchantProductSessionEJBRemote;
import com.gdn.venice.facade.VenOrderItemSessionEJBRemote;
import com.gdn.venice.persistence.FrdParameterRule44;
import com.gdn.venice.persistence.VenMerchantProduct;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.VeniceConstants;

/**
 * Class for calculate fraud rule 44: Slow Moving Category
 *
 * @author Daniel Hutama Putra
 */

public class Rule44 {
	protected static Logger _log = null;
    
    public Rule44() {
        super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.fraud.rule.Rule44");
    }
    
    public int execute(long orderId) throws Exception{
    	_log.info("Start execute");
    	int fraudPoint=0;
    	
        List<VenOrderItem> venOrderItemList = null;
        venOrderItemList=getVenOrderItemList(orderId);
        
    	try{
    		
    		if(venOrderItemList.size()>0){
    	    	String productIdList="";
    	        List<VenMerchantProduct> venMerchantProductList=null;    	    
	    		for(int i=0; i<venOrderItemList.size();i++){	    			
	    			if(i==0){
	    				productIdList=venOrderItemList.get(i).getVenMerchantProduct().getProductId().toString();
	    			}	  
	    			else
	    			productIdList=productIdList+","+venOrderItemList.get(i).getVenMerchantProduct().getProductId();
	    		}		    	
	    		venMerchantProductList=getVenMerchantProductList(productIdList);
	    		
	    		if(venMerchantProductList.size()==venOrderItemList.size()){
		    			fraudPoint=getRiskPoint(VeniceConstants.FRD_PARAMETER_RULE_44_RISK_POINT);
		    	}
    		}
    	}
    	catch (Exception e){
    		e.printStackTrace();
    	}
    	
        _log.info("Done execute rule 44, fraudPoint is: "+fraudPoint);
        return fraudPoint;
    }
    
    
    
    public List<VenOrderItem> getVenOrderItemList(Long orderId) throws Exception{

        Locator<Object> locator = null;
        List<VenOrderItem> venOrderItemList = null;
        
    	try{
    		locator = new Locator<Object>();
    		VenOrderItemSessionEJBRemote venOrderItemSessionHome = (VenOrderItemSessionEJBRemote) locator.lookup(VenOrderItemSessionEJBRemote.class, "VenOrderItemSessionEJBBean");            
   		
    		String query="select o from VenOrderItem o where o.venOrder.orderId = "+orderId;
    		venOrderItemList=venOrderItemSessionHome.queryByRange(query, 0, 0);
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return venOrderItemList;
    }

    
    
    public List<VenMerchantProduct> getVenMerchantProductList(String productIdList) throws Exception{

        Locator<Object> locator = null;
        List<VenMerchantProduct> venMerchantProductList = null;
        
    	try{
    		locator = new Locator<Object>();
            VenMerchantProductSessionEJBRemote venMerchantProductSessionHome = (VenMerchantProductSessionEJBRemote) locator.lookup(VenMerchantProductSessionEJBRemote.class, "VenMerchantProductSessionEJBBean");
   
            String query="select o from VenMerchantProduct o join fetch o.venProductCategories oi where o.productId in("+productIdList+") and (oi.productCategory not like ('"+VeniceConstants.VEN_PRODUCT_CATEGORY_PRODUCT_CATEGORY_ELECTRONIC+"%') " +
    				"and oi.productCategory not like ('"+VeniceConstants.VEN_PRODUCT_CATEGORY_PRODUCT_CATEGORY_HANDPHONE+"%')) and oi.level = "+VeniceConstants.VEN_PRODUCT_CATEGORY_LEVEL_ONE;
    		venMerchantProductList=venMerchantProductSessionHome.queryByRange(query, 0, 0);
    		
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return venMerchantProductList;
    } 
    
    public Integer getRiskPoint(String parameter) throws Exception{
    	int riskPoint=0;
        Locator<Object> locator = null;
        List<FrdParameterRule44> frdParameterRule44List = null;
    	
    	try{
    		
    		locator = new Locator<Object>();
    		FrdParameterRule44SessionEJBRemote rule44SessionHome = (FrdParameterRule44SessionEJBRemote) locator.lookup(FrdParameterRule44SessionEJBRemote.class, "FrdParameterRule44SessionEJBBean");
            
    		String query = "select o from FrdParameterRule44 o where o.description = '"+parameter+"'";
            frdParameterRule44List=rule44SessionHome.queryByRange(query, 0, 0);
            riskPoint=frdParameterRule44List.get(0).getValue();
            
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
