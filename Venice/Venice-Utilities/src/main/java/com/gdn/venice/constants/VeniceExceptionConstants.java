package com.gdn.venice.constants;

/**
 * 
 * @author yauritux
 *
 */
public enum VeniceExceptionConstants {

	VEN_EX_000001("Invalid Order"),
	VEN_EX_000111("Cannot create Order"),
	VEN_EX_000002("No Order Received"),
	VEN_EX_000003("No Order Amount"),
	VEN_EX_000004("No Customer Record"),
	VEN_EX_000005("No Order ID"),
	VEN_EX_000006("No Order Items"),
	VEN_EX_000007("No Payment Information"),
	VEN_EX_000008("No Timestamp Information"),
	VEN_EX_000009("No Status Information"),
	VEN_EX_000010("Fullfilment status not 1"),
	VEN_EX_000011("Invalid Order Logistic Information"),
	VEN_EX_000012("WCS Order Item already exist in Database"),
	VEN_EX_000013("Order with VA Payment Does not exist in Database"),
	VEN_EX_000014("Order with VA Payment is not approved yet"),
	VEN_EX_000015("Order with CS Payment Does not exist in Database"),
	VEN_EX_000016("Order with CS Payment is not approved yet"),
	VEN_EX_000017("WCS Order already exist in Database"),
	VEN_EX_000019("Duplicate WCS Order ID"),
	VEN_EX_000020("Order does not exist"),
	VEN_EX_000120("Order Item does not exist"),
	VEN_EX_000021("Error occured while persisting Order Item"),
	VEN_EX_000022("Error occured while persisting Order Item Adjustment"),
	VEN_EX_000023("Error occured while persisting Order Payment"),
	VEN_EX_000024("Error occured while persisting Order Status History"),
	VEN_EX_010001("Error occured while persisting Ven Product Type"),
	VEN_EX_000025("Order Status does not exist"),
	VEN_EX_000125("VenOrderPaymentAllocation does not exists"),
	VEN_EX_000026("Order Blocking Source does not exist"),
	VEN_EX_000027("Error occured while persisting VenOrderItemAddress"),
	VEN_EX_000028("Error occured while persisting VenOrderItemContactsDetail"),
	VEN_EX_000029("Error occured while persisting VenOrderContactDetail"),
	VEN_EX_000030("Error occured while persisting VenOrderAddress"),
	VEN_EX_000031("Error occured while persisting VenOrderPaymentAllocation"),
	VEN_EX_000032("Error occured while persisting VenPartyAddress"),
	VEN_EX_100001("Error occured while persisting Customer"),
	VEN_EX_110001("Error occured while persisting Contact Detail"),
	VEN_EX_120001("Error occured while persisting VenMerchant"),
	VEN_EX_120002("Error occured while persisting VenProductType"),
	VEN_EX_120003("Error occured while persisting VenOrderItemAdjustment"),
	VEN_EX_130002("Error occured while synchronizing VenCity"),
	VEN_EX_130003("Error occured while synchronizing VenCountry"),
	VEN_EX_130004("Error occured while synchronizing VenState"),
	VEN_EX_130005("Error occured while synchronizing VenContactDetailType"),
	VEN_EX_130006("Error occured while synchronizing VenMerchant with VenParty"),
	VEN_EX_130007("Error occured while synchronizing VenMerchantProduct"),
	VEN_EX_130008("Error occured while synchronizing VenOrderStatus"),
	VEN_EX_130009("Error occured while synchronizing VenPromotion"),
	VEN_EX_130010("Error occured while synchronizing VenOrderItemAdjustment"),
	VEN_EX_130011("Error occured while synchronizing VenPaymentStatus"),
	VEN_EX_140001("Error occured while performing query on VenPromotion"),
	VEN_EX_200001("Bank does not exist"),
	VEN_EX_300001("Fraud Check Value does not exist"),
	VEN_EX_300011("Fraud Case History does not exist"),
	VEN_EX_300002("FrdParameterRule31 does not exist"),
	VEN_EX_400001("Payment Status does not exist"),
	VEN_EX_400002("Payment Type does not exist"),
	VEN_EX_400003("WCS Payment Type does not exist"),
	VEN_EX_400004("Payment processing failed"),
	VEN_EX_400005("Account number N/A for the Payment."),
	VEN_EX_500001("LogLogisticService does not exist"),
	VEN_EX_600001("Product Category does not exist"),
	VEN_EX_800001("Error occured while persisting FinArFundsInReconRecord"),
	VEN_EX_999999("Unknown Exception");
	
	private String message;
	
	public String getMessage() {
		return message;
	}
	
	private VeniceExceptionConstants(String message) {
		this.message = message;
	}
}
