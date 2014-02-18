package com.gdn.venice.facade.logistics.activity;

import com.gdn.venice.constants.LogisticProviderConstants;


public class LogisticProviderActivityReportTemplate {
	public String getTemplateName(LogisticProviderConstants logisticProvider){
		
		switch (logisticProvider) {
			case JNE:
				return "DailyReportJNE.xml";
			case NCS:
				return "DailyReportNCS.xml";
			case RPX:
				return "DailyReportRPX.xml";	
			case MSG:
				return "DailyReportMSG.xml";
		}
		
		return null;
		
	}
}
