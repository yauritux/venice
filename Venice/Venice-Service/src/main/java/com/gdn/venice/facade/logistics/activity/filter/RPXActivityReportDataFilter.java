package com.gdn.venice.facade.logistics.activity.filter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.dao.LogAirwayBillDAO;
import com.gdn.venice.dao.LogAirwayBillReturDAO;
import com.gdn.venice.exception.ActivityReportDataFilterException;
import com.gdn.venice.facade.logistics.activity.ActivityReportData;
import com.gdn.venice.logistics.dataimport.DailyReportNCS;
import com.gdn.venice.logistics.integration.AirwayBillEngineClientConnector;
import com.gdn.venice.logistics.integration.AirwayBillEngineConnector;

@Service("RPXActivityReportDataFilter")
public class RPXActivityReportDataFilter extends ActivityReportDataFilter{
	protected static Logger _log = Log4jLoggerFactory.getLogger("com.gdn.venice.facade.logistics.activity.filter.RPXActivityReportDataFilter");
	
	@Autowired
	LogAirwayBillDAO logAirwayBillDAO;
	@Autowired
	LogAirwayBillReturDAO logAirwayBillReturDAO;
	
	AirwayBillEngineConnector awbConn;
	
	@Override
	public AirwayBillEngineConnector getAWBConnector() {
		return awbConn;
	}

	@Override
	public Logger getLogger() {
		return _log;
	}
	
	@Override
	public ActivityReportData filter(ActivityReportData activityReportDataToBeFiltered)	throws ActivityReportDataFilterException {
		
		if(awbConn == null)
			awbConn = new AirwayBillEngineClientConnector();
		
		ActivityReportData activityReportFilterResult = new ActivityReportData();
		
		int totalItemBeforeFilter = activityReportDataToBeFiltered.getOrderItemList().size();
		
		_log.debug("Total item before filter " + totalItemBeforeFilter);
		
		DailyReportNCS dailyReportOrderItem = null;
		
		for(int i = 0; i < totalItemBeforeFilter; i++){
			
			dailyReportOrderItem = (DailyReportNCS) activityReportDataToBeFiltered.getOrderItemList().get(i);
			
			if(isItemOk(dailyReportOrderItem.getRefNo(), activityReportFilterResult)){
				activityReportFilterResult.getOrderItemList().add(dailyReportOrderItem);
			}
		}
		
		activityReportDataToBeFiltered.getOrderItemList().clear();
		
		_log.debug("Total item after filter " + activityReportFilterResult.getOrderItemList().size());
		
		return activityReportFilterResult;
	}

}
