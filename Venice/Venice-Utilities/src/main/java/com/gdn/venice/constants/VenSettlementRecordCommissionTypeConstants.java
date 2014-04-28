package com.gdn.venice.constants;

/**
 * 
 * @author yauritux
 *
 */
public enum VenSettlementRecordCommissionTypeConstants {

	 VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_COMMISSION("CM"),
	 VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_REBATE("RB"),
	 VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_TRADING("TD"),
	 VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_CONSIGNMENT("KS"),
	 VEN_SETTLEMENT_RECORD_COMMISSIONTYPE_MERCHANTPARTNER("MP");
	 
	 private String label;
	 
	 private VenSettlementRecordCommissionTypeConstants(String label) {
		 this.label = label;
	 }
	 
	 public String label() {
		 return label;
	 }
}
