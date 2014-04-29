package com.gdn.venice.constants;

public enum VenWCSPaymentTypeConstants {	

	 VEN_WCS_PAYMENT_TYPE_KlikBCA(0, "KlikBCA"),
	 VEN_WCS_PAYMENT_TYPE_MandiriKlikpay(1, "DebitMandiri"),
	 VEN_WCS_PAYMENT_TYPE_VirtualAccount(2, "VirtualAccount"),
	 VEN_WCS_PAYMENT_TYPE_MIGSCreditCard(3, "MIGSCreditCard"),
	 VEN_WCS_PAYMENT_TYPE_KlikPAYFullPayment(4, "KlikPAYFullPayment"),
	 VEN_WCS_PAYMENT_TYPE_KlikPAYZeroPercentInstallment(5, "KlikPAYZeroPercentInstallment"),
	 VEN_WCS_PAYMENT_TYPE_KlikPAYXPercentInstallment(6, "KlikPAYXPercentInstallment"),
	 VEN_WCS_PAYMENT_TYPE_MIGSBCAInstallment(7, "MIGSBCAInst"),
	 VEN_WCS_PAYMENT_TYPE_KlikPAYInstallment(8, "KlikPayInst"),
	 VEN_WCS_PAYMENT_TYPE_CIMBClicks(9, "CIMBClicks"),
	 VEN_WCS_PAYMENT_TYPE_XLTunai(10, "XLTunai"),
	 VEN_WCS_PAYMENT_TYPE_MandiriInstallment(11, "MandiriInstallment"),
	 VEN_WCS_PAYMENT_TYPE_BIIngkisan(12, "BIIngkisan"),
	 VEN_WCS_PAYMENT_TYPE_BRI(13, "BRI"),
	 VEN_WCS_PAYMENT_TYPE_MandiriDebit(14, "MandiriDebit"),
	 VEN_WCS_PAYMENT_TYPE_CSPayment(15, "CSPayment"),
	 
	 // 2 constants below are replacing KlikPAYFullPayment
	 VEN_WCS_PAYMENT_TYPE_KlikPAYKartuKredit(16, "KlikPAYKartuKredit"),
	 VEN_WCS_PAYMENT_TYPE_KlikPAYKlikBCA(17, "KlikPAYKlikBCA"),
	 
	 VEN_WCS_PAYMENT_TYPE_TelkomselPoin(18, "TelkomselPoin"),
	 VEN_WCS_PAYMENT_TYPE_BCACreditCard(19, "BCACreditCard"),	
	 VEN_WCS_PAYMENT_TYPE_ANZ(20, "ANZ"),
	 VEN_WCS_PAYMENT_TYPE_CIMBCreditCard(21, "CIMBCreditCard"),
	 VEN_WCS_PAYMENT_TYPE_DanamonCreditCard(22, "DanamonCreditCard"),
	 VEN_WCS_PAYMENT_TYPE_VISACreditCard(23,"VISACreditCard"),
	//this is only for wcs
	 VEN_WCS_PAYMENT_TYPE_PartialFulfillment(19, "PartialFulfillment");
	 
	 private long id;
	 private String desc;
	 
	 private VenWCSPaymentTypeConstants(long id, String desc) {
		 this.id = id;
		 this.desc = desc;
	 }
	 
	 public long id() {
		 return id;
	 }
	 
	 public String desc() {
		 return desc;
	 }
	
}
