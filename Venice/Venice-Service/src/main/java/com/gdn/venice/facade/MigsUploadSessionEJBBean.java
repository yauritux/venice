package com.gdn.venice.facade;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import com.gdn.venice.exception.MIGSFileParserException;
import com.gdn.venice.facade.spring.fraud.migs.MigsUploadProcessorService;

@Interceptors(SpringBeanAutowiringInterceptor.class)
@Stateless(mappedName = "MigsUploadSessionEJBBean")
public class MigsUploadSessionEJBBean implements 
		MigsUploadSessionEJBLocal,
		MigsUploadSessionEJBRemote {

	@Autowired
	MigsUploadProcessorService migsUploadProcessorService;
	
	@Override
	public String process(String fileNameAndFullPath) throws MIGSFileParserException{
		return migsUploadProcessorService.process(fileNameAndFullPath);
	}
}
