package com.gdn.venice.facade.spring;


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.dao.LogActivityReconRecordDAO;
import com.gdn.venice.persistence.LogActivityReconRecord;
import com.gdn.venice.persistence.LogAirwayBill;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log4jLoggerFactory.class})
public class LogAirwayBillServiceImplTest {
	
	@Mock
	private Logger logMock;
	@Mock
	private LogActivityReconRecordDAO logActivityReconRecordDAOMock;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private LogAirwayBill logAirwayBillMock;
	
	private LogAirwayBillServiceImpl sut;
	private LogAirwayBillServiceImpl spySut;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
		
		mockStatic(Log4jLoggerFactory.class);
		PowerMockito.when(Log4jLoggerFactory.getLogger(anyString())).thenReturn(logMock);
		
		sut = new LogAirwayBillServiceImpl();
		spySut = spy(sut);
		
		spySut.logActivityReconRecordDAO = logActivityReconRecordDAOMock;
	}
	
	@Test
	public void reconcileActualPickupDate_pickUpDateFromMTASameWithLogistic_returnsTrue(){
		Date actualPickupDateFromMTA = new Date();
		Date actualPickupDateFromLogistics = actualPickupDateFromMTA;
		
		boolean result = spySut.reconcileActualPickupDate(1L,actualPickupDateFromLogistics, actualPickupDateFromMTA);
		
		assertTrue(result);
	}
	
	@Test
	public void reconcileActualPickupDate_pickUpDateFromMTADifferentWithLogistic_returnsFalse(){
		Date actualPickupDateFromMTA = new Date();
		Date actualPickupDateFromLogistics = addDays(actualPickupDateFromMTA, 1);
		
		boolean result = spySut.reconcileActualPickupDate(1L,actualPickupDateFromLogistics, actualPickupDateFromMTA);
		
		assertFalse(result);
	}
	
	@Test
	public void reconcileActualPickupDate_pickUpDateFromMTADifferentWithLogistic_logActivityReconRecordSaved(){
		Date actualPickupDateFromMTA = new Date();
		Date actualPickupDateFromLogistics = addDays(actualPickupDateFromMTA, 1);
		
		spySut.reconcileActualPickupDate(1L,actualPickupDateFromLogistics, actualPickupDateFromMTA);
		
		verify(logActivityReconRecordDAOMock, times(1)).save(any(LogActivityReconRecord.class));	
	}
	
	@Test
	public void reconcileActualPickupDate_pickUpDateFromMTANull_returnsFalse(){
		Date actualPickupDateFromMTA = null;
		Date actualPickupDateFromLogistics = new Date();
		
		boolean result = spySut.reconcileActualPickupDate(1L,actualPickupDateFromLogistics, actualPickupDateFromMTA);
		
		assertFalse(result);
	}
	
	@Test
	public void reconcileActualPickupDate_pickUpDateFromLogisticNull_returnsFalse(){
		Date actualPickupDateFromMTA = new Date();
		Date actualPickupDateFromLogistics = null;
		
		boolean result = spySut.reconcileActualPickupDate(1L,actualPickupDateFromLogistics, actualPickupDateFromMTA);
		
		assertFalse(result);
	}
	
	@Test
	public void reconcileService_serviceFromMTASameWithLogistic_returnsTrue(){
		String serviceFromMTA = "JNE";
		String serviceFromLogistic = serviceFromMTA;
		String providerCodeFromLogistic = serviceFromMTA;
		
		boolean result = spySut.reconcileService(1L, providerCodeFromLogistic, serviceFromLogistic, serviceFromMTA);
		
		assertTrue(result);	
	}
	
	@Test
	public void reconcileService_serviceFromMTADifferentWithLogistic_returnsFalse(){
		String serviceFromMTA = "JNE";
		String serviceFromLogistic = "REGULER";
		String providerCodeFromLogistic = "NCS";
		
		boolean result = spySut.reconcileService(1L, providerCodeFromLogistic, serviceFromLogistic, serviceFromMTA);
		
		assertFalse(result);	
	}
	
	@Test
	public void reconcileService_serviceFromMTADifferentWithLogistic_logActivityReconRecordSaved(){
		String serviceFromMTA = "JNE";
		String serviceFromLogistic = "REGULER";
		String providerCodeFromLogistic = "NCS";
		
		spySut.reconcileService(1L, providerCodeFromLogistic, serviceFromLogistic, serviceFromMTA);
		
		verify(logActivityReconRecordDAOMock, times(1)).save(any(LogActivityReconRecord.class));		
	}
	
	@Test
	public void reconcileRecipient_recipientFromMTASameWithLogistic_returnsTrue(){
		String recipientFromMTA = "Recipient";
		String recipientFromLogistic = recipientFromMTA;
		
		boolean result = spySut.reconcileRecipient(1L, recipientFromLogistic, recipientFromMTA);
		
		assertTrue(result);	
	}
	
	@Test
	public void reconcileRecipient_recipientFromMTADifferentWithLogistic_returnsFalse(){
		String recipientFromMTA = "Recipient";
		String recipientFromLogistic = "Recipient 1";
		
		boolean result = spySut.reconcileRecipient(1L, recipientFromLogistic, recipientFromMTA);
		
		assertFalse(result);	
	}
	
	@Test
	public void reconcileRecipient_recipientFromMTADifferentWithLogistic_logActivityReconRecordSaved(){
		String recipientFromMTA = "Recipient";
		String recipientFromLogistic = "Recipient 1";
		
		spySut.reconcileRecipient(1L, recipientFromLogistic, recipientFromMTA);
		
		verify(logActivityReconRecordDAOMock, times(1)).save(any(LogActivityReconRecord.class));
	}
	
	@Test
	public void reconcileAirwayBill_pickUpDateFromMTADifferentWithLogistic_activityResultStatusOK(){
		LogAirwayBill providerAirwayBillSpy = spy(new LogAirwayBill());
		
		doReturn("").when(spySut).getLogisticsProviderCode(providerAirwayBillSpy);
		doReturn("").when(spySut).getLogisticsProviderCode(logAirwayBillMock);
		
		when(logAirwayBillMock.getVenOrderItem().getVenRecipient().getVenParty().getFullOrLegalName()).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return "";
			}
		});
		
		doReturn(false).when(spySut).reconcileActualPickupDate(anyLong(), any(Date.class), any(Date.class));
		doReturn(true).when(spySut).reconcileService(anyLong(), anyString(), anyString(), anyString());
		doReturn(true).when(spySut).reconcileRecipient(anyLong(), anyString(), anyString());
		
		LogAirwayBill result = spySut.reconcileAirwayBill(providerAirwayBillSpy, logAirwayBillMock, true);
		
		assertEquals("OK", result.getActivityResultStatus());
	}
	
	@Test
	public void reconcileAirwayBill_serviceFromMTADifferentWithLogistic_activityResultStatusProblemExist(){
		LogAirwayBill providerAirwayBillSpy = spy(new LogAirwayBill());
		
		doReturn("").when(spySut).getLogisticsProviderCode(providerAirwayBillSpy);
		doReturn("").when(spySut).getLogisticsProviderCode(logAirwayBillMock);
		
		when(logAirwayBillMock.getVenOrderItem().getVenRecipient().getVenParty().getFullOrLegalName()).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return "";
			}
		});
		
		doReturn(true).when(spySut).reconcileActualPickupDate(anyLong(), any(Date.class), any(Date.class));
		doReturn(false).when(spySut).reconcileService(anyLong(), anyString(), anyString(), anyString());
		doReturn(true).when(spySut).reconcileRecipient(anyLong(), anyString(), anyString());
		
		LogAirwayBill result = spySut.reconcileAirwayBill(providerAirwayBillSpy, logAirwayBillMock, true);
		
		assertEquals("Problem Exists", result.getActivityResultStatus());
	}
	
	@Test
	public void reconcileAirwayBill_recipientFromMTADifferentWithLogistic_activityResultStatusProblemExist(){
		LogAirwayBill providerAirwayBillSpy = spy(new LogAirwayBill());
		
		doReturn("").when(spySut).getLogisticsProviderCode(providerAirwayBillSpy);
		doReturn("").when(spySut).getLogisticsProviderCode(logAirwayBillMock);
		
		when(logAirwayBillMock.getVenOrderItem().getVenRecipient().getVenParty().getFullOrLegalName()).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return "";
			}
		});
		
		doReturn(true).when(spySut).reconcileActualPickupDate(anyLong(), any(Date.class), any(Date.class));
		doReturn(true).when(spySut).reconcileService(anyLong(), anyString(), anyString(), anyString());
		doReturn(false).when(spySut).reconcileRecipient(anyLong(), anyString(), anyString());
		
		LogAirwayBill result = spySut.reconcileAirwayBill(providerAirwayBillSpy, logAirwayBillMock, true);
		
		assertEquals("Problem Exists", result.getActivityResultStatus());
	}
	
	@Test
	public void getActivityReconRecord_mCxIsTrue_allActivityReconRecordByAirwayBillFetched(){
		spySut.getActivityReconRecord(1L, true);
		
		verify(logActivityReconRecordDAOMock, times(1)).findByLogAirwayBillAndReconIsNotSettlementMismatch(any(LogAirwayBill.class));
	}
	
	@Test
	public void getActivityReconRecord_mCxIsFalse_activityReconRecordNotSettlementMismatchByAirwayBillFetched(){
		spySut.getActivityReconRecord(1L, false);
		
		verify(logActivityReconRecordDAOMock, times(1)).findByLogAirwayBill(any(LogAirwayBill.class));
	}
	
	
	@After
	public void shutdown(){
		logMock = null;
		logActivityReconRecordDAOMock = null;
		logAirwayBillMock = null;
		sut = null;
		spySut = null;
	}
	
	public static Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }
	
}
