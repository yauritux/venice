package com.gdn.venice.constants;

/**
 * 
 * @author yauritux
 *
 */
public enum VenLogisticsProviderConstants {

	 VEN_LOGISTICS_PROVIDER_RPX(Constants.RPX_ID),
	 VEN_LOGISTICS_PROVIDER_NCS(Constants.NCS_ID),
	 VEN_LOGISTICS_PROVIDER_JNE(Constants.JNE_ID),
	 VEN_LOGISTICS_PROVIDER_GOJEK(Constants.GOJEK_ID),
	 VEN_LOGISTICS_PROVIDER_MSG(Constants.MSG_ID),
	 VEN_LOGISTICS_PROVIDER_BOPIS(Constants.BOPIS_ID),
	 VEN_LOGISTICS_PROVIDER_BIGPRODUCT(Constants.BIGPRODUCT_ID);
	 
	 private long id;
	 
	 private VenLogisticsProviderConstants(long id) {
		 this.id = id;
	 }
	 
	 public long id() {
		 return id;
	 }
	 
	 public static class Constants {
		 
		 public static final int RPX_ID = 0;
		 public static final int NCS_ID = 1;
		 public static final int JNE_ID = 2;
		 public static final int GOJEK_ID = 3;
		 public static final int MSG_ID = 4;
		 public static final int BOPIS_ID = 10;
		 public static final int BIGPRODUCT_ID = 11;
	 }
}
