package com.gdn.venice.facade.spring.fraud.migs.strategy;

import java.util.ArrayList;

import com.gdn.venice.hssf.PojoInterface;



public interface FileParserStrategy {
	public void parse(String fileNameAndFullPath);
	
	public ArrayList<PojoInterface> getParseResult();
	
	public String getErrorMessage();
	
	public boolean isError();
	
}
