package com.gdn.venice.constants;

public enum FraudRuleConstants {
	FRAUD_RULE_1("Rule 01 - First time shopper"),
	FRAUD_RULE_2("Rule 02 - Larger than normal order"),
	FRAUD_RULE_3("Rule 03 - Order that include several of the same time"),
	FRAUD_RULE_4("Rule 04 - Order made up of big ticket items"),
	FRAUD_RULE_5("Rule 05 - Rush or overnight shipping"),
	FRAUD_RULE_6("Rule 06 - Shipping to an international address"),
	FRAUD_RULE_7("Rule 07 - Transactions with similar account number"),
	FRAUD_RULE_8("Rule 08 - Payment Type"),
	FRAUD_RULE_9("Rule 09 - Shipping to a single address, but transactions placed on multiple cards"),
	
	FRAUD_RULE_10("Rule 10 - Multiple transactions on one card over a very short period of time"),
	FRAUD_RULE_11("Rule 11 - Multiple transactions on one card or a similar card with a single billing address, but multiple shipping addresses"),
	FRAUD_RULE_12("Rule 12 - Multiple cards used from a single IP address"),
	FRAUD_RULE_13("Rule 13 - IP address company"),
	FRAUD_RULE_14("Rule 14 - Order from internet addresses that make use of free email services"),
	FRAUD_RULE_15("Rule 15 - Bin number not registered"),
	FRAUD_RULE_16("Rule 16 - Same customer with different credit card"),
	FRAUD_RULE_17("Rule 17 - City blacklist"),
	FRAUD_RULE_18("Rule 18 - Validity of wording customer, shipping, billing address"),
	FRAUD_RULE_19("Rule 19 - Validity of address"),
	FRAUD_RULE_20("Rule 20 - UMR by 33 province"),
	
	FRAUD_RULE_21("Rule 21 - Order timestamp blacklist"),
	FRAUD_RULE_22("Rule 22 - Customer shopping limit per month"),
	FRAUD_RULE_23("Rule 23 - Customer name vs customer email"),
	FRAUD_RULE_24("Rule 24 - Total order amount "),
	FRAUD_RULE_25("Rule 25 - Company shipping address"),
	FRAUD_RULE_26("Rule 26 - Same customer email in one week"),
	FRAUD_RULE_27("Rule 27 - Same product name & customer email in one week"),
	FRAUD_RULE_28("Rule 28 - Same product category & customer email in one week"),
	FRAUD_RULE_29("Rule 29 - Same customer address in one week"),
	FRAUD_RULE_30("Rule 30 - Phone area code customer"),
	
	FRAUD_RULE_31("Rule 31 - List genuine transaction by BCA"),
	FRAUD_RULE_32("Rule 32 - Collection blacklist"),
	FRAUD_RULE_33("Rule 33 - E-Commerce Indicator (ECI)"),
	FRAUD_RULE_34("Rule 34 - IP geolocation information"),
	FRAUD_RULE_35("Rule 35 - Grey list"),
	FRAUD_RULE_36("Rule 36 - Black List"),
	FRAUD_RULE_37("Rule 37 - MIGS History with same credit card"),
	FRAUD_RULE_38("Rule 38 - MIGS History with different credit card"),	
	FRAUD_RULE_39("Rule 39 - Pasca bayar"),
	FRAUD_RULE_40("Rule 40 - Handphone area vs customer location"),
	
	FRAUD_RULE_41("Rule 41 - Attempt CC"),
	FRAUD_RULE_42("Rule 42 - Cek Dana Fund In MIGS"),
	FRAUD_RULE_43("Rule 43 - Total Payment < 1jt"),
	FRAUD_RULE_44("Rule 44 - Slow moving product category");
	
	private String title;
	
	private FraudRuleConstants(String title){
		this.title = title;
	}
	
	public String title(){
		return this.title;
	}
	
}
