package com.gdn.venice.seattle.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class SeattleOrder {
	private Long seatOrderStatusHistory;
	private Long orderStatusId;	
	private Long orderfulfillmentId;		
	private Long orderStatusTrackingtId;	
	private String wcsOrderId;
	private String wcsOrderItemId;
	private Timestamp updateStatusDate;
	private String statusOrder;		
	private String customerUserName;
	private String customerName;
	private String issueId;	
	private Date etdMax;	
	private Timestamp orderTimestamp;	
	private Timestamp etdOrderComplate;	
	private Timestamp newEtdMax;
	private Long resultStatusId;
	private String resultStatus;
	private String late;
	private Integer lateSecond;	
	private BigDecimal diffEtd;
	private Timestamp statusDueDate;	
	private String lateStatus;
	private Integer LateSecondStatus;	
	private Long resultStatusTrackingId;
	private String resultStatusTracking;	
	private String issueStatusId;
	private boolean closeIssueStatus;
	private boolean closeIssue;
	private Long statusPayment;
	
	public boolean isCloseIssueStatus() {
		return closeIssueStatus;
	}
	public void setCloseIssueStatus(boolean closeIssueStatus) {
		this.closeIssueStatus = closeIssueStatus;
	}
	public boolean isCloseIssue() {
		return closeIssue;
	}
	public void setCloseIssue(boolean closeIssue) {
		this.closeIssue = closeIssue;
	}	
	public Long getStatusPayment() {
		return statusPayment;
	}
	public void setStatusPayment(Long statusPayment) {
		this.statusPayment = statusPayment;
	}
	public Long getOrderStatusTrackingtId() {
		return orderStatusTrackingtId;
	}
	public void setOrderStatusTrackingtId(Long orderStatusTrackingtId) {
		this.orderStatusTrackingtId = orderStatusTrackingtId;
	}	
	public String getIssueStatusId() {
		return issueStatusId;
	}
	public void setIssueStatusId(String issueStatusId) {
		this.issueStatusId = issueStatusId;
	}
	public Timestamp getStatusDueDate() {
		return statusDueDate;
	}
	public void setStatusDueDate(Timestamp statusDueDate) {
		this.statusDueDate = statusDueDate;
	}
	public String getLateStatus() {
		return lateStatus;
	}
	public void setLateStatus(String lateStatus) {
		this.lateStatus = lateStatus;
	}
	public Integer getLateSecondStatus() {
		return LateSecondStatus;
	}
	public void setLateSecondStatus(Integer lateSecondStatus) {
		LateSecondStatus = lateSecondStatus;
	}
	public Long getResultStatusTrackingId() {
		return resultStatusTrackingId;
	}
	public void setResultStatusTrackingId(Long resultStatusTrackingId) {
		this.resultStatusTrackingId = resultStatusTrackingId;
	}
	public String getResultStatusTracking() {
		return resultStatusTracking;
	}
	public void setResultStatusTracking(String resultStatusTracking) {
		this.resultStatusTracking = resultStatusTracking;
	}	
	public BigDecimal getDiffEtd() {
		return diffEtd;
	}
	public void setDiffEtd(BigDecimal diffEtd) {
		this.diffEtd = diffEtd;
	}
	public Timestamp getEtdOrderComplate() {
		return this.etdOrderComplate;
	}
	public void setEtdOrderComplate(Timestamp etdOrderComplate) {
		this.etdOrderComplate = etdOrderComplate;
	}
	public Timestamp getNewEtdMax() {
		return this.newEtdMax;
	}
	public void setNewEtdMax(Timestamp newEtdMax) {
		this.newEtdMax = newEtdMax;
	}
	public Long getResultStatusId() {
		return this.resultStatusId;
	}
	public void setResultStatusId(Long resultStatusId) {
		this.resultStatusId = resultStatusId;
	}
	public String getResultStatus() {
		return this.resultStatus;
	}
	public void setResultStatus(String resultStatus) {
		this.resultStatus = resultStatus;
	}
	public String getLate() {
		return this.late;
	}
	public void setLate(String late) {
		this.late = late;
	}
	public Integer getLateSecond() {
		return this.lateSecond;
	}
	public void setLateSecond(Integer lateSecond) {
		this.lateSecond = lateSecond;
	}
	public String getIssueId() {
		return issueId;
	}
	public void setIssueId(String issueId) {
		this.issueId = issueId;
	}	
	public Long getOrderfulfillmentId() {
		return orderfulfillmentId;
	}
	public void setOrderfulfillmentId(Long orderfulfillmentId) {
		this.orderfulfillmentId = orderfulfillmentId;
	}
	
	public Long getOrderStatusId() {
		return orderStatusId;
	}
	public void setOrderStatusId(Long orderStatusId) {
		this.orderStatusId = orderStatusId;
	}	
	public Timestamp getOrderTimestamp() {
		return orderTimestamp;
	}
	public void setOrderTimestamp(Timestamp orderTimestamp) {
		this.orderTimestamp = orderTimestamp;
	}	
	public Date getEtdMax() {
		return etdMax;
	}
	public void setEtdMax(Date etdMax) {
		this.etdMax = etdMax;
	}
	private Long wcsPaymentTypeId;
	
	public Long getSeatOrderStatusHistory() {
		return seatOrderStatusHistory;
	}
	public void setSeatOrderStatusHistory(Long seatOrderStatusHistory) {
		this.seatOrderStatusHistory = seatOrderStatusHistory;
	}
	
	public String getStatusOrder() {
		return statusOrder;
	}
	public void setStatusOrder(String statusOrder) {
		this.statusOrder = statusOrder;
	}
	
	public String getWcsOrderId() {
		return wcsOrderId;
	}
	public void setWcsOrderId(String wcsOrderId) {
		this.wcsOrderId = wcsOrderId;
	}
	
	public String getCustomerUserName() {
		return customerUserName;
	}
	public void setCustomerUserName(String customerUserName) {
		this.customerUserName = customerUserName;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}	
	public Long getWcsPaymentTypeId() {
		return wcsPaymentTypeId;
	}
	public void setWcsPaymentTypeId(Long wcsPaymentTypeId) {
		this.wcsPaymentTypeId = wcsPaymentTypeId;
	}	
	public String getWcsOrderItemId() {
		return wcsOrderItemId;
	}
	public void setWcsOrderItemId(String wcsOrderItemId) {
		this.wcsOrderItemId = wcsOrderItemId;
	}
	public Timestamp getUpdateStatusDate() {
		return updateStatusDate;
	}
	public void setUpdateStatusDate(Timestamp updateStatusDate) {
		this.updateStatusDate = updateStatusDate;
	}
}
