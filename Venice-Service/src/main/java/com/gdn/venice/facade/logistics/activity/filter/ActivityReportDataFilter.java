package com.gdn.venice.facade.logistics.activity.filter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.gdn.venice.dao.LogAirwayBillDAO;
import com.gdn.venice.dao.LogAirwayBillReturDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.exception.ActivityReportDataFilterException;
import com.gdn.venice.facade.logistics.activity.ActivityReportData;
import com.gdn.venice.logistics.dataimport.DailyReportJNE;
import com.gdn.venice.logistics.integration.AirwayBillEngineConnector;

public abstract class ActivityReportDataFilter {
	
	@Autowired
	LogAirwayBillDAO logAirwayBillDAO;
	
	@Autowired
	LogAirwayBillReturDAO logAirwayBillReturDAO;
	
	@Autowired
	VenOrderItemDAO venOrderItemDAO;
	
	public abstract AirwayBillEngineConnector getAWBConnector();
	
	public abstract Logger getLogger();
	
	public abstract ActivityReportData filter(ActivityReportData activityReportDataToBeFiltered) throws ActivityReportDataFilterException;
	
	public boolean isGdnReferenceFormatCorrect(String gdnReference){
		String[] gdnRefSplit = getSplitGdnReference(gdnReference);
		
		if(gdnRefSplit.length != 4)
			return false;
		
		if(!getOrderOrRMA(gdnReference).equals("R") &&
			!getOrderOrRMA(gdnReference).equals("O"))
			return false;
		
		return true;
	}
	
	public boolean isReturnOrderItem(String gdnReference){
		return (getOrderOrRMA(gdnReference)
							.equals("R")?true:false); 
	} 
	
	public String getOrderOrRMA(String gdnReference) {
		return getSplitGdnReference(gdnReference)[0];
	}
	
	public String getWcsOrderId(String gdnReference) {
		return getSplitGdnReference(gdnReference)[1];
	}
	
	public String getWcsOrderItemId(String gdnReference) {
		return getSplitGdnReference(gdnReference)[2];
	}
	
	public String getSequence(String gdnReference) {
		return getSplitGdnReference(gdnReference)[3];
	}
	
	public String[] getSplitGdnReference(String gdnReference){
		return gdnReference.split("-");
	}
	
	public boolean isOrderItemFound(String wcsOrderItemId){
		int totalOrderItem = venOrderItemDAO.countByWcsOrderItemId(wcsOrderItemId);
		return (totalOrderItem > 0)?true : false;
	}
	
	public boolean isOrderItemServiceBopisOrBigProduct(String wcsOrderItemId){
		int totalOrderItem = venOrderItemDAO.countWhereLogisticServiceIsBopisOrBigProductByWcsOrderItemId(wcsOrderItemId);
		return (totalOrderItem > 0)?true : false;
	}
	
	// use this if you want to add  AWB Engine validation
	public boolean isOrderItemAvailableInAWBEngine(String gdnReference){
		String actualGDNRef = "";
		try {
			if(isReturnOrderItem(gdnReference)){
				actualGDNRef = getAWBConnector().getReturnGDNRef(getWcsOrderItemId(gdnReference));
			}else{
				actualGDNRef = getAWBConnector().getGDNRef(getWcsOrderItemId(gdnReference));
			}
		} catch (Exception e) {
			getLogger().error("Error accessing AWB Engine For Order Item " + getWcsOrderItemId(gdnReference), e);
			e.printStackTrace();
		}
		
		if(actualGDNRef == null || actualGDNRef.equals(""))
			return false;
		
		return true;
	}
	
	// use this if you want to add  LogAirwayBill validation
	public boolean isLogAirwayBillFound(String gdnRef){
		int totalLogAirwayBill = 0;
		
		if(isReturnOrderItem(gdnRef)){
			getLogger().debug("Order Item Return");
			totalLogAirwayBill =  logAirwayBillReturDAO.countByGdnReference(gdnRef);
		}else{
			totalLogAirwayBill =  logAirwayBillDAO.countByGdnReference(gdnRef);
		}
		
		return (totalLogAirwayBill > 0)?true:false;
	}
	
	public boolean isItemOk(String gdnRef, ActivityReportData activityReportData){
		
        getLogger().debug("Filtering GDN Ref : " + gdnRef);
        
        if(!isGdnReferenceFormatCorrect(gdnRef)){
        	gdnRefFormatInvalidHandler(gdnRef, activityReportData);
        	return false;
        }
        
        if(!isOrderItemFound(getWcsOrderItemId(gdnRef))){
        	orderItemNotFoundHandler(gdnRef, activityReportData);
        	return false;
        }
        
        if(isOrderItemServiceBopisOrBigProduct(getWcsOrderItemId(gdnRef))){
        	orderItemIsBPWarningHandler(gdnRef, activityReportData);
        	return false;
        }
        
        return true;
	}
	
	public void orderItemNotFoundHandler(String gdnRef, ActivityReportData activityReportData){
		getLogger().info("Order item id not found in venice: " + gdnRef);
		
		activityReportData.getGdnRefNotFoundList().put(gdnRef, "Order item id not found in venice");
	}
	
	public void orderItemIsBPWarningHandler(String gdnRef, ActivityReportData activityReportData){
		getLogger().info("Order item id is Big Product: " + gdnRef);
		
		activityReportData.getFailedItemList().put(gdnRef, "Order item id is Big Product");
	}
	
	public void gdnRefFormatInvalidHandler(String gdnRef, ActivityReportData activityReportData){
		getLogger().info("GDN Ref Format Invalid: " + gdnRef);
		
		activityReportData.getGdnRefNotFoundList().put(gdnRef, "GDN Ref Format Invalid");
	}
	
}
