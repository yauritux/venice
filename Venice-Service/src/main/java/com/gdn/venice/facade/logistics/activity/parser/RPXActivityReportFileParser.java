package com.gdn.venice.facade.logistics.activity.parser;

import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.constants.LogisticProviderConstants;
import com.gdn.venice.exception.ActivityReportFileParserException;
import com.gdn.venice.facade.logistics.activity.ActivityReportData;
import com.gdn.venice.hssf.ExcelToPojo;
import com.gdn.venice.logistics.dataimport.DailyReportNCS;
import com.gdn.venice.logistics.dataimport.DailyReportRPX;
import com.gdn.venice.logistics.dataimport.LogisticsConstants;

public class RPXActivityReportFileParser extends ActivityReportFileParser{

	protected static Logger _log = Log4jLoggerFactory.getLogger("com.gdn.venice.facade.logistics.activity.parser.RPXActivityReportFileParser");
	
	public ActivityReportData parse(String excelFileNameAndFullPath)throws ActivityReportFileParserException {
		ExcelToPojo excelToPojo = null;
		ActivityReportData data = new ActivityReportData();
        
        try {
        	
            excelToPojo = new ExcelToPojo(DailyReportNCS.class, getLogisticProviderActivityReportTemplateFileNameAndFullPath(LogisticProviderConstants.NCS), excelFileNameAndFullPath, 0, 0);
            excelToPojo = excelToPojo.getPojo();
            
        } catch (Exception e) {
            String errMsg = LogisticsConstants.EXCEPTION_TEXT_FILE_PARSE + e.getMessage() + ". Processing row number " + (excelToPojo != null && excelToPojo.getErrorRowNumber() != null ? excelToPojo.getErrorRowNumber() : "1" + "\n");
            _log.error(errMsg + ", updating file upload log to FAIL", e);
            
            throw new ActivityReportFileParserException(errMsg, e);
        }
        
        data.setOrderItemList(excelToPojo.getPojoResult());
        
        if (data.getOrderItemList().isEmpty()) {
            throw new ActivityReportFileParserException("Parse result is empty");
        } else {
            _log.debug("Parsed result size: " + data.getOrderItemList().size());
        }
	        
		return data;
	}

}
