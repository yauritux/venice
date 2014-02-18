package com.gdn.venice.facade.logistics.activity;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.gdn.venice.constants.LogisticProviderConstants;

@RunWith(MockitoJUnitRunner.class)
public class LogisticProviderActivityReportTemplateTest {
	// SUT
	private  LogisticProviderActivityReportTemplate sut;
	
	@Before
	public void setup(){
		sut = new LogisticProviderActivityReportTemplate(); 
	}
	
	@Test
	public void getTemplateName_passingJNE_returnsCorrectFileName(){
		String result = sut.getTemplateName(LogisticProviderConstants.JNE);
		assertEquals("DailyReportJNE.xml", result);
	}
	
	@Test
	public void getTemplateName_passingNCS_returnsCorrectFileName(){
		String result = sut.getTemplateName(LogisticProviderConstants.NCS);
		assertEquals("DailyReportNCS.xml", result);
	}
	
	@Test
	public void getTemplateName_passingRPX_returnsCorrectFileName(){
		String result = sut.getTemplateName(LogisticProviderConstants.RPX);
		assertEquals("DailyReportRPX.xml", result);
	}
	
	@Test
	public void getTemplateName_passingMSG_returnsCorrectFileName(){
		String result = sut.getTemplateName(LogisticProviderConstants.MSG);
		assertEquals("DailyReportMSG.xml", result);
	}
	
}
