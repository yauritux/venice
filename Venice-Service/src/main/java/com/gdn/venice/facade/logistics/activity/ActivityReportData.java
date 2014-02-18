package com.gdn.venice.facade.logistics.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.logistics.dataexport.FailedStatusUpdate;
import com.gdn.venice.persistence.LogActivityReportUpload;

public class ActivityReportData {
	
	private ArrayList<PojoInterface> orderItemList = new ArrayList<PojoInterface>();
	private HashMap<String, String> gdnRefNotFoundList = new HashMap<String, String>();
    private HashMap<String, String> failedItemList = new HashMap<String, String>();
    private List<FailedStatusUpdate> failedStatusUpdateList = new ArrayList<FailedStatusUpdate>();
    private LogActivityReportUpload activityReportUpload = new LogActivityReportUpload();
    
	public ArrayList<PojoInterface> getOrderItemList() {
		return orderItemList;
	}
	public void setOrderItemList(ArrayList<PojoInterface> itemList) {
		this.orderItemList = itemList;
	}
	public HashMap<String, String> getGdnRefNotFoundList() {
		return gdnRefNotFoundList;
	}
	public void setGdnRefNotFoundList(HashMap<String, String> gdnRefNotFoundList) {
		this.gdnRefNotFoundList = gdnRefNotFoundList;
	}
	public HashMap<String, String> getFailedItemList() {
		return failedItemList;
	}
	public void setFailedItemList(HashMap<String, String> failedItemList) {
		this.failedItemList = failedItemList;
	}
	public LogActivityReportUpload getActivityReportUpload() {
		return activityReportUpload;
	}
	public void setActivityReportUpload(LogActivityReportUpload activityReportUpload) {
		this.activityReportUpload = activityReportUpload;
	}
	public List<FailedStatusUpdate> getFailedStatusUpdateList() {
		return failedStatusUpdateList;
	}
	public void setFailedStatusUpdateList(
			List<FailedStatusUpdate> failedStatusUpdateList) {
		this.failedStatusUpdateList = failedStatusUpdateList;
	}
    
}
