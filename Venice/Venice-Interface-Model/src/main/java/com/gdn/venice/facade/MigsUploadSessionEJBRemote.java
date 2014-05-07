package com.gdn.venice.facade;

import javax.ejb.Remote;

import com.gdn.venice.exception.MIGSFileParserException;

@Remote
public interface MigsUploadSessionEJBRemote {

	public String process(String fileNameAndFullPath) throws MIGSFileParserException;

}
