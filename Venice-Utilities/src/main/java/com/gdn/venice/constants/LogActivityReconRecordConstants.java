package com.gdn.venice.constants;

public enum LogActivityReconRecordConstants {
	COMMENT_PICKUP_DATE_MISMATCH{
		public String toString(){
			return "Venice identified a pickup date mismatch";
		}
	},
	COMMENT_SERVICE_MISMATCH{
		public String toString(){
			return "Venice identified a service mismatch";
		}
	},
	COMMENT_RECIPIENT_MISMATCH{
		public String toString(){
			return "Venice identified a recipient mismatch";
		}
	};
}
