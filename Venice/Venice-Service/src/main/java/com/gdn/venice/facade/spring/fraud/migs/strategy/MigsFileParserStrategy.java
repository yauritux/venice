package com.gdn.venice.facade.spring.fraud.migs.strategy;


import java.util.ArrayList;

import com.gdn.venice.fraud.dataimport.MigsReport;
import com.gdn.venice.hssf.ExcelToPojo;
import com.gdn.venice.hssf.PojoInterface;

public class MigsFileParserStrategy implements FileParserStrategy{

	ExcelToPojo ex;
	
	@Override
	public void parse(String fileNameAndFullPath) {
		ex = new ExcelToPojo(System.getenv("VENICE_HOME") + "/files/template/MIGSReport.xml", fileNameAndFullPath, 5, 1, 23, MigsReport.class);
	}

	@Override
	public String getErrorMessage() {
		return ex.getErrorMessage();
	}

	@Override
	public ArrayList<PojoInterface> getParseResult() {
		return ex.getPojoResult();
	}

	@Override
	public boolean isError() {
		if(getErrorMessage().equalsIgnoreCase(""))
			return false;
		else
			return true;
	}

}
