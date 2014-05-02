package com.gdn.venice.facade;

import javax.ejb.Local;

import com.gdn.venice.exception.MIGSFileParserException;

@Local
public interface MigsUploadSessionEJBLocal {

	public String process(String fileNameAndFullPath) throws MIGSFileParserException;

}
