package com.gdn.venice.facade.logistics.activity.processor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.constants.VenOrderStatusConstants;
import com.gdn.venice.dao.LogAirwayBillDAO;
import com.gdn.venice.dao.LogFileUploadLogDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.dao.VenReturnItemDAO;
import com.gdn.venice.exception.ActivityReportFileParserException;
import com.gdn.venice.facade.logistics.activity.ActivityReportData;
import com.gdn.venice.facade.logistics.activity.filter.ActivityReportDataFilter;
import com.gdn.venice.facade.spring.VenOrderItemService;
import com.gdn.venice.factory.VenOrderStatusES;
import com.gdn.venice.logistics.dataimport.DailyReportJNE;
import com.gdn.venice.logistics.integration.AirwayBillEngineConnector;
import com.gdn.venice.logistics.integration.bean.AirwayBillTransaction;
import com.gdn.venice.persistence.LogAirwayBill;
import com.gdn.venice.persistence.LogApprovalStatus;
import com.gdn.venice.persistence.LogFileUploadLog;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.VeniceConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log4jLoggerFactory.class})
public class JNEActivityReportProcessorTest {
	
	@Mock
	private Logger logMock;
	@Mock
	private ActivityReportDataFilter filterMock;
	@Mock
	private VenOrderItemDAO venOrderItemDAOMock;
	@Mock
	private VenReturnItemDAO venReturnItemDAOMock;
	@Mock
	LogAirwayBillDAO logAirwayBillDAOMock;
	@Mock
	private LogFileUploadLogDAO logFileUploadLogDAOMock;
	@Mock
	private VenOrderItemService venOrderItemServiceMock;
	@Mock
	private AirwayBillEngineConnector awbConnMock;

	private JNEActivityReportProcessor sut;
	
	private JNEActivityReportProcessor sutSpy;
	
	private DailyReportJNE dailyReportJNE;
	
	@Before
	public void setup(){
		mockStatic(Log4jLoggerFactory.class);
		PowerMockito.when(Log4jLoggerFactory.getLogger(anyString())).thenReturn(logMock);
		
		sut = new JNEActivityReportProcessor();
		sutSpy = spy(sut);
		
		sutSpy.filter = filterMock;
		sutSpy.venOrderItemDAO = venOrderItemDAOMock;
		sutSpy.venReturnItemDAO = venReturnItemDAOMock;
		sutSpy.logFileUploadLogDAO = logFileUploadLogDAOMock;
		sutSpy.logAirwayBillDAO = logAirwayBillDAOMock;
		sutSpy.venOrderItemService = venOrderItemServiceMock;
		sutSpy.awbConn = awbConnMock;
		
		dailyReportJNE = new DailyReportJNE();
	}
	
	@Test
	public void isEligibleForStatusChange_existingOrderItemStatusPU_returnsTrue() throws ActivityReportFileParserException{
		boolean result = sutSpy.isEligibleForStatusChange(VeniceConstants.VEN_ORDER_STATUS_PU);
		assertTrue(result);
	}
	
	@Test
	public void isEligibleForStatusChange_existingOrderItemStatusPP_returnsTrue() throws ActivityReportFileParserException{
		boolean result = sutSpy.isEligibleForStatusChange(VeniceConstants.VEN_ORDER_STATUS_PP);
		assertTrue(result);
	}
	
	@Test
	public void isEligibleForStatusChange_existingOrderItemStatusES_returnsTrue() throws ActivityReportFileParserException{
		boolean result = sutSpy.isEligibleForStatusChange(VeniceConstants.VEN_ORDER_STATUS_ES);
		assertTrue(result);
	}
	
	@Test
	public void isEligibleForStatusChange_existingOrderItemStatusCX_returnsTrue() throws ActivityReportFileParserException{
		boolean result = sutSpy.isEligibleForStatusChange(VeniceConstants.VEN_ORDER_STATUS_CX);
		assertTrue(result);
	}
	
	@Test
	public void isEligibleForStatusChange_existingOrderItemStatusD_returnsTrue() throws ActivityReportFileParserException{
		boolean result = sutSpy.isEligibleForStatusChange(VeniceConstants.VEN_ORDER_STATUS_D);
		assertTrue(result);
	}
	
	@Test
	public void isEligibleForStatusChange_existingOrderItemStatusFP_returnsFalse() throws ActivityReportFileParserException{
		boolean result = sutSpy.isEligibleForStatusChange(VeniceConstants.VEN_ORDER_STATUS_FP);
		assertFalse(result);
	}
	
	@Test
	public void isEligibleForStatusChange_existingOrderItemStatusPF_returnsFalse() throws ActivityReportFileParserException{
		boolean result = sutSpy.isEligibleForStatusChange(VeniceConstants.VEN_ORDER_STATUS_PF);
		assertFalse(result);
	}
	
	@Test
	public void updateOrderItemStatusBeforeAirwayBillAutomation_PUtoCX_mergeOrderItemStatusInvoked2Times(){
		VenOrderItem dummyItem = new VenOrderItem();
		sutSpy.updateOrderItemStatusBeforeAirwayBillAutomation(dummyItem, VenOrderStatusConstants.VEN_ORDER_STATUS_PU, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
		verify(venOrderItemServiceMock, times(2)).mergeVenOrderItem(dummyItem);
	}
	
	@Test
	public void updateOrderItemStatusBeforeAirwayBillAutomation_PUtoD_mergeOrderItemStatusInvoked0Time(){
		VenOrderItem dummyItem = new VenOrderItem();
		sutSpy.updateOrderItemStatusBeforeAirwayBillAutomation(dummyItem, VenOrderStatusConstants.VEN_ORDER_STATUS_PU, VenOrderStatusConstants.VEN_ORDER_STATUS_D);
		verify(venOrderItemServiceMock, never()).mergeVenOrderItem(dummyItem);
	}
	
	@Test
	public void updateOrderItemStatusBeforeAirwayBillAutomation_EStoCX_mergeOrderItemStatusInvoked1Time(){
		VenOrderItem dummyItem = new VenOrderItem();
		sutSpy.updateOrderItemStatusBeforeAirwayBillAutomation(dummyItem, VenOrderStatusConstants.VEN_ORDER_STATUS_ES, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
		verify(venOrderItemServiceMock, times(1)).mergeVenOrderItem(dummyItem);
	}
	
	@Test
	public void updateOrderItemStatusBeforeAirwayBillAutomation_EStoD_mergeOrderItemStatusInvoked2Times(){
		VenOrderItem dummyItem = new VenOrderItem();
		sutSpy.updateOrderItemStatusBeforeAirwayBillAutomation(dummyItem, VenOrderStatusConstants.VEN_ORDER_STATUS_ES, VenOrderStatusConstants.VEN_ORDER_STATUS_D);
		verify(venOrderItemServiceMock, times(2)).mergeVenOrderItem(dummyItem);
	}
	
	@Test
	public void updateOrderItemStatusBeforeAirwayBillAutomation_CXtoD_mergeOrderItemStatusInvoked1Time(){
		VenOrderItem dummyItem = new VenOrderItem();
		sutSpy.updateOrderItemStatusBeforeAirwayBillAutomation(dummyItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX, VenOrderStatusConstants.VEN_ORDER_STATUS_D);
		verify(venOrderItemServiceMock, times(1)).mergeVenOrderItem(dummyItem);
	}
	
	@Test
	public void updateOrderItemStatusBeforeAirwayBillAutomation_DtoD_mergeOrderItemStatusInvoked0Time(){
		VenOrderItem dummyItem = new VenOrderItem();
		sutSpy.updateOrderItemStatusBeforeAirwayBillAutomation(dummyItem, VenOrderStatusConstants.VEN_ORDER_STATUS_D, VenOrderStatusConstants.VEN_ORDER_STATUS_D);
		verify(venOrderItemServiceMock, never()).mergeVenOrderItem(dummyItem);
	}
	
	@Test
	public void updateOrderItemStatus_orderItemStatusCX_orderItemSalesBatchStatusIsReady(){
		VenOrderItem dummyItem = new VenOrderItem();
		sutSpy.updateOrderItemStatus(dummyItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
		
		assertEquals(VeniceConstants.FIN_SALES_BATCH_STATUS_READY, dummyItem.getSalesBatchStatus());
	}
	
	@Test
	public void updateOrderItemStatus_orderItemStatusCX_orderItemCXDateIsNotNull(){
		VenOrderItem dummyItem = new VenOrderItem();
		sutSpy.updateOrderItemStatus(dummyItem, VenOrderStatusConstants.VEN_ORDER_STATUS_CX);
		
		assertNotNull(dummyItem.getCxDate());
	}
	
	@Test
	public void processEachOrderItem_orderItemBeforeAirwaybillEngine_executeProcessOrderItemBeforeAWBEngine() throws Exception{
		ActivityReportData activityReportData = new ActivityReportData();
		
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		VenOrderItem orderItemES = new VenOrderItem();
		orderItemES.setVenOrderStatus(VenOrderStatusES.createVenOrderStatus());
		
		when(venOrderItemDAOMock.findWithVenOrderStatusByWcsOrderItemId(anyString()))
			.thenReturn(orderItemES);
		
		when(filterMock.getWcsOrderItemId(anyString())).thenReturn("12345");
		
		when(logAirwayBillDAOMock.countByGdnReference(anyString()))
			.thenReturn(1);
		
		when(awbConnMock.getAirwayBillTransaction(anyString()))
			.thenReturn(null);
		
		doNothing().when(sutSpy).processOrderItemBeforeAWBEngine(orderItemES, getDailyReportJNEWithCXStatus(), activityReportData, fileUploadLog);
		
		sutSpy.processEachOrderItem(getDailyReportJNEWithCXStatus(), activityReportData, fileUploadLog);
		
		verify(sutSpy, times(1)).processOrderItemBeforeAWBEngine(orderItemES, getDailyReportJNEWithCXStatus(), activityReportData, fileUploadLog);
	}
	
	@Test
	public void processEachOrderItem_orderItemAfterAirwaybillEngine_executeProcessOrderItemBeforeAWBEngine() throws Exception{
		ActivityReportData activityReportData = new ActivityReportData();
		
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		
		VenOrderItem orderItemES = new VenOrderItem();
		orderItemES.setVenOrderStatus(VenOrderStatusES.createVenOrderStatus());
		
		AirwayBillTransaction awbTransaction = new AirwayBillTransaction();
		awbTransaction.setStatus("ES");
		
		when(venOrderItemDAOMock.findWithVenOrderStatusByWcsOrderItemId(anyString()))
			.thenReturn(orderItemES);
		
		when(filterMock.getWcsOrderItemId(anyString())).thenReturn("12345");
		
		when(logAirwayBillDAOMock.countByGdnReference(anyString()))
			.thenReturn(0);
		
		when(awbConnMock.getAirwayBillTransaction(anyString()))
			.thenReturn(awbTransaction);
		
		doNothing().when(sutSpy).processOrderItemAfterAWBEngine(orderItemES, getDailyReportJNEWithCXStatus(), activityReportData, fileUploadLog, awbTransaction);
		
		sutSpy.processEachOrderItem(getDailyReportJNEWithCXStatus(), activityReportData, fileUploadLog);
		
		verify(sutSpy, times(1)).processOrderItemAfterAWBEngine(orderItemES, getDailyReportJNEWithCXStatus(), activityReportData, fileUploadLog, awbTransaction);
	}
	
	@Test
	public void composeLogAirwayBill_orderItemNewStatusIsCX_updatedSuccessfully(){
		ActivityReportData activityReportData = new ActivityReportData();
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		
		VenOrderItem orderItemES = new VenOrderItem();
		orderItemES.setVenOrderStatus(VenOrderStatusES.createVenOrderStatus());
		
		LogApprovalStatus approvalStatus2 = new LogApprovalStatus();
		approvalStatus2.setApprovalStatusId(VeniceConstants.VEN_LOGISTICS_APPROVAL_STATUS_APPROVED);
		
		LogAirwayBill logAirwayBill = new LogAirwayBill();
		logAirwayBill.setLogApprovalStatus2(approvalStatus2);
		
		
		AirwayBillTransaction awbTransaction = new AirwayBillTransaction();
		awbTransaction.setStatus("ES");
		
		boolean result = sutSpy.composeLogAirwayBill(logAirwayBill, getDailyReportJNEWithCXStatus(), activityReportData, fileUploadLog);
		
		assertTrue(result);
	}
	
	@Test
	public void composeLogAirwayBill_invalidcNoteDate_updateFail(){
		ActivityReportData activityReportData = new ActivityReportData();
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		
		VenOrderItem orderItemES = new VenOrderItem();
		orderItemES.setVenOrderStatus(VenOrderStatusES.createVenOrderStatus());
		
		LogApprovalStatus approvalStatus2 = new LogApprovalStatus();
		approvalStatus2.setApprovalStatusId(VeniceConstants.VEN_LOGISTICS_APPROVAL_STATUS_APPROVED);
		
		LogAirwayBill logAirwayBill = new LogAirwayBill();
		logAirwayBill.setLogApprovalStatus2(approvalStatus2);
		
		AirwayBillTransaction awbTransaction = new AirwayBillTransaction();
		awbTransaction.setStatus("ES");
		
		DailyReportJNE dailyReportJNE = getDailyReportJNEWithCXStatus();
		dailyReportJNE.setcNoteDate("");
		
		boolean result = sutSpy.composeLogAirwayBill(logAirwayBill, dailyReportJNE, activityReportData, fileUploadLog);
		
		assertEquals(1, activityReportData.getFailedItemList().size());
		assertFalse(result);
	}
	
	@Test
	public void composeLogAirwayBill_orderItemNewStatusIsCXWithInvalidReceivedDate_updateFail(){
		ActivityReportData activityReportData = new ActivityReportData();
		LogFileUploadLog fileUploadLog = new LogFileUploadLog(); 
		
		VenOrderItem orderItemES = new VenOrderItem();
		orderItemES.setVenOrderStatus(VenOrderStatusES.createVenOrderStatus());
		
		AirwayBillTransaction awbTransaction = new AirwayBillTransaction();
		awbTransaction.setStatus("CX");
		
		DailyReportJNE dailyReportJNE = getDailyReportJNEWithDStatus();
		dailyReportJNE.setReceivedDate("invalid date");
		
		boolean result = sutSpy.composeLogAirwayBill(getLogAirwayBill(), dailyReportJNE, activityReportData, fileUploadLog);
		
		assertEquals(1, activityReportData.getFailedItemList().size());
		assertFalse(result);
	}
	
	@Test
	public void composeLogAirwayBill_newOrderItemStatusIsD_receivedRecipientRelationIsNotEmptyInLogAirwayBill(){
		ActivityReportData activityReportData = new ActivityReportData();
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		
		DailyReportJNE dailyReportJNE = getDailyReportJNEWithDStatus();
		
		LogAirwayBill logAirwayBill = getLogAirwayBill();
		
		sutSpy.composeLogAirwayBill(logAirwayBill, dailyReportJNE, activityReportData, fileUploadLog);
		
		assertNotNull(logAirwayBill.getReceived());
		assertFalse(logAirwayBill.getRecipient().isEmpty());
		assertFalse(logAirwayBill.getRelation().isEmpty());
		
	}
	
	@Test
	public void composeLogAirwayBillAfterAWBEngine_newOrderItemStatusIsD_receivedRecipientRelationIsNotEmptyInAirwayBillTransaction(){
		ActivityReportData activityReportData = new ActivityReportData();
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		
		DailyReportJNE dailyReportJNE = getDailyReportJNEWithDStatus();
		
		AirwayBillTransaction airwayBillTransaction = new AirwayBillTransaction();
		airwayBillTransaction.setWeight((double)5);
		
		sutSpy.composeLogAirwayBillAfterAWBEngine(getLogAirwayBill(), dailyReportJNE, activityReportData, fileUploadLog, airwayBillTransaction);
		
		assertNotNull(airwayBillTransaction.getReceived());
		assertFalse(airwayBillTransaction.getRecipient().isEmpty());
		assertFalse(airwayBillTransaction.getRelation().isEmpty());
		
	}
	
	private LogAirwayBill getLogAirwayBill(){
		LogApprovalStatus approvalStatus2 = new LogApprovalStatus();
		approvalStatus2.setApprovalStatusId(VeniceConstants.VEN_LOGISTICS_APPROVAL_STATUS_APPROVED);
		
		LogAirwayBill logAirwayBill = new LogAirwayBill();
		logAirwayBill.setLogApprovalStatus2(approvalStatus2);
		
		return logAirwayBill;
	}
	
	private DailyReportJNE getDailyReportJNEWithCXStatus(){
		dailyReportJNE.setStatus("");
		dailyReportJNE.setNumber("");
		dailyReportJNE.setReceivedDate("");
		dailyReportJNE.setRecipient("");
		dailyReportJNE.setcNoteDate("01-JAN-2014");
		dailyReportJNE.setGdnRefNumber("O-1234-12345-1");
		return dailyReportJNE;
	}
	
	private DailyReportJNE getDailyReportJNEWithDStatus(){
		// 3 data
		dailyReportJNE.setStatus("DL");
		dailyReportJNE.setReceivedDate("01-JAN-2014");
		dailyReportJNE.setRecipient("Test");
		
		dailyReportJNE.setNumber("");
		dailyReportJNE.setReceiver("");
		dailyReportJNE.setcNoteDate("01-JAN-2014");
		dailyReportJNE.setGdnRefNumber("O-1234-12345-1");
		return dailyReportJNE;
	}
	
	@After
	public void shutdown(){
		logMock = null;
		filterMock = null;
		venOrderItemDAOMock = null;
		venReturnItemDAOMock = null;
		logAirwayBillDAOMock = null;
		logFileUploadLogDAOMock = null;
		venOrderItemServiceMock = null;
		awbConnMock = null;
		sut = null;
		sutSpy = null;
		dailyReportJNE = null;
	}
	
	
}
