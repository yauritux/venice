package com.gdn.venice.dto;

import java.util.ArrayList;
import java.util.List;

import com.gdn.venice.hssf.PojoInterface;

public class FundInData {
	private ArrayList<PojoInterface> fundInList = new ArrayList<PojoInterface>();
	private List<String> processedFundInList = new ArrayList<String>();
	private List<String> refundFundInList = new ArrayList<String>();
	private List<String> voidFundInList = new ArrayList<String>();
	
	public ArrayList<PojoInterface> getFundInList() {
		return fundInList;
	}
	
	public void setFundInList(ArrayList<PojoInterface> fundInList) {
		this.fundInList = fundInList;
	}
	
	public List<String> getRefundFundInList() {
		return refundFundInList;
	}
	
	public void setRefundFundInList(List<String> refundFundInList) {
		this.refundFundInList = refundFundInList;
	}
	
	public List<String> getVoidFundInList() {
		return voidFundInList;
	}
	
	public void setVoidFundInList(List<String> voidFundInList) {
		this.voidFundInList = voidFundInList;
	}

	public List<String> getProcessedFundInList() {
		return processedFundInList;
	}

	public void setProcessedFundInList(List<String> processedFundInList) {
		this.processedFundInList = processedFundInList;
	}
}
