package com.gdn.venice.fraud.dataexport.bean;

import java.math.BigDecimal;
import java.util.Date;

public class Installment {
	private Long orderPaymentId;
	private String wcsOrderId;
	private String wcsPaymentId;
	private Date orderDate;
	private String referenceId;
	private BigDecimal Amount;
	private Integer tenor;
	private BigDecimal installment;
	private BigDecimal interest;
	private BigDecimal interesInstallment;
	private Date installmentSentDate;
	private Date installmentCancelDate;
	private Boolean installmentSentFlag;
	private Boolean installmentCancelFlag;
	private String customerUserName;
	private String customerName;
	private Long wcsPaymentTypeId;
	
	public String getWcsOrderId() {
		return wcsOrderId;
	}
	public void setWcsOrderId(String wcsOrderId) {
		this.wcsOrderId = wcsOrderId;
	}
	public String getWcsPaymentId() {
		return wcsPaymentId;
	}
	public void setWcsPaymentId(String wcsPaymentId) {
		this.wcsPaymentId = wcsPaymentId;
	}
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	public String getReferenceId() {
		return referenceId;
	}
	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}
	public BigDecimal getAmount() {
		return Amount;
	}
	public void setAmount(BigDecimal amount) {
		Amount = amount;
	}
	public Integer getTenor() {
		return tenor;
	}
	public void setTenor(Integer tenor) {
		this.tenor = tenor;
	}
	public BigDecimal getInstallment() {
		return installment;
	}
	public void setInstallment(BigDecimal installment) {
		this.installment = installment;
	}
	public BigDecimal getInterest() {
		return interest;
	}
	public void setInterest(BigDecimal interest) {
		this.interest = interest;
	}
	public BigDecimal getInteresInstallment() {
		return interesInstallment;
	}
	public void setInteresInstallment(BigDecimal interesInstallment) {
		this.interesInstallment = interesInstallment;
	}
	public Date getInstallmentSentDate() {
		return installmentSentDate;
	}
	public void setInstallmentSentDate(Date installmentSentDate) {
		this.installmentSentDate = installmentSentDate;
	}
	public Date getInstallmentCancelDate() {
		return installmentCancelDate;
	}
	public void setInstallmentCancelDate(Date installmentCancelDate) {
		this.installmentCancelDate = installmentCancelDate;
	}
	public Boolean getInstallmentSentFlag() {
		return installmentSentFlag;
	}
	public void setInstallmentSentFlag(Boolean installmentSentFlag) {
		this.installmentSentFlag = installmentSentFlag;
	}
	public Boolean getInstallmentCancelFlag() {
		return installmentCancelFlag;
	}
	public void setInstallmentCancelFlag(Boolean installmentCancelFlag) {
		this.installmentCancelFlag = installmentCancelFlag;
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
	public Long getOrderPaymentId() {
		return orderPaymentId;
	}
	public void setOrderPaymentId(Long orderPaymentId) {
		this.orderPaymentId = orderPaymentId;
	}
	public Long getWcsPaymentTypeId() {
		return wcsPaymentTypeId;
	}
	public void setWcsPaymentTypeId(Long wcsPaymentTypeId) {
		this.wcsPaymentTypeId = wcsPaymentTypeId;
	}
}
