package com.gdn.venice.facade.logistics.activity.processor;

import com.gdn.venice.constants.LogisticProviderConstants;

public class ActivityReportProcessorFactory {
	
	public static ActivityReportProcessor getActivityReportProcessor(LogisticProviderConstants logisticProvider){
		switch (logisticProvider) {
		case JNE:
			return new JNEActivityReportProcessor();

		default:
			return null;
		}
	}
	
}
