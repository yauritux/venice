package com.gdn.venice.facade.logistics.activity.parser;

import com.gdn.venice.constants.LogisticProviderConstants;
import com.gdn.venice.exception.ActivityReportFileParserException;
import com.gdn.venice.facade.logistics.activity.ActivityReportData;
import com.gdn.venice.facade.logistics.activity.LogisticProviderActivityReportTemplate;
import com.gdn.venice.logistics.dataimport.LogisticsConstants;

public abstract class ActivityReportFileParser {
	public abstract ActivityReportData parse(String fileNameAndFullPath) throws ActivityReportFileParserException;
	
	public String getLogisticProviderActivityReportTemplateFileNameAndFullPath(LogisticProviderConstants logisticProviderConstants){
		LogisticProviderActivityReportTemplate templateFileNameProvider = new LogisticProviderActivityReportTemplate();
		String templateFileName = templateFileNameProvider.getTemplateName(logisticProviderConstants);
		
		return System.getenv("VENICE_HOME") + LogisticsConstants.TEMPLATE_FOLDER + templateFileName;
	}
}
