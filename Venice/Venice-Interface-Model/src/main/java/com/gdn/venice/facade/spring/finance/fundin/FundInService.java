package com.gdn.venice.facade.spring.finance.fundin;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.gdn.venice.exception.FundInFileAlreadyUploadedException;
import com.gdn.venice.exception.FundInNoFinancePeriodFoundException;

public interface FundInService {
	
	public String process(String fileNameAndFullPath, String uploadUserName) throws NoSuchAlgorithmException, IOException, FundInFileAlreadyUploadedException, FundInNoFinancePeriodFoundException;
	
}
