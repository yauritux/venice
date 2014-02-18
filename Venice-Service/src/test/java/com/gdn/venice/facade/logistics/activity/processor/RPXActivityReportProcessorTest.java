package com.gdn.venice.facade.logistics.activity.processor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
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
import com.gdn.venice.dao.LogAirwayBillDAO;
import com.gdn.venice.dao.LogFileUploadLogDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.dao.VenReturnItemDAO;
import com.gdn.venice.facade.logistics.activity.ActivityReportData;
import com.gdn.venice.facade.logistics.activity.filter.ActivityReportDataFilter;
import com.gdn.venice.facade.spring.VenOrderItemService;
import com.gdn.venice.factory.VenOrderStatusES;
import com.gdn.venice.logistics.dataimport.DailyReportNCS;
import com.gdn.venice.logistics.integration.AirwayBillEngineConnector;
import com.gdn.venice.logistics.integration.bean.AirwayBillTransaction;
import com.gdn.venice.persistence.LogAirwayBill;
import com.gdn.venice.persistence.LogApprovalStatus;
import com.gdn.venice.persistence.LogFileUploadLog;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.VeniceConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log4jLoggerFactory.class})
public class RPXActivityReportProcessorTest {
	@Mock
	private Logger logMock;
	@Mock
	private ActivityReportDataFilter filterMock;
	@Mock
	private VenOrderItemDAO venOrderItemDAOMock;
	@Mock
	private VenReturnItemDAO venReturnItemDAOMock;
	@Mock
	private LogAirwayBillDAO logAirwayBillDAOMock;
	@Mock
	private LogFileUploadLogDAO logFileUploadLogDAOMock;
	@Mock
	private VenOrderItemService venOrderItemServiceMock;
	@Mock
	private AirwayBillEngineConnector awbConnMock;

	private RPXActivityReportProcessor sut;
	
	private RPXActivityReportProcessor sutSpy;
	
	private DailyReportNCS dailyReportRPX;
	
	@Before
	public void setup(){
		mockStatic(Log4jLoggerFactory.class);
		PowerMockito.when(Log4jLoggerFactory.getLogger(anyString())).thenReturn(logMock);
		
		sut = new RPXActivityReportProcessor();
		sutSpy = spy(sut);
		
		sutSpy.filter = filterMock;
		sutSpy.venOrderItemDAO = venOrderItemDAOMock;
		sutSpy.venReturnItemDAO = venReturnItemDAOMock;
		sutSpy.logFileUploadLogDAO = logFileUploadLogDAOMock;
		sutSpy.logAirwayBillDAO = logAirwayBillDAOMock;
		sutSpy.venOrderItemService = venOrderItemServiceMock;
		sutSpy.awbConn = awbConnMock;
		
		dailyReportRPX = new DailyReportNCS();
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
		
		doNothing().when(sutSpy).processOrderItemBeforeAWBEngine(orderItemES, getDailyReportRPXWithCXStatus(), activityReportData, fileUploadLog);
		
		sutSpy.processEachOrderItem(getDailyReportRPXWithCXStatus(), activityReportData, fileUploadLog);
		
		verify(sutSpy, times(1)).processOrderItemBeforeAWBEngine(orderItemES, getDailyReportRPXWithCXStatus(), activityReportData, fileUploadLog);
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
		
		doNothing().when(sutSpy).processOrderItemAfterAWBEngine(orderItemES, getDailyReportRPXWithCXStatus(), activityReportData, fileUploadLog, awbTransaction);
		
		sutSpy.processEachOrderItem(getDailyReportRPXWithCXStatus(), activityReportData, fileUploadLog);
		
		verify(sutSpy, times(1)).processOrderItemAfterAWBEngine(orderItemES, getDailyReportRPXWithCXStatus(), activityReportData, fileUploadLog, awbTransaction);
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
		
		boolean result = sutSpy.composeLogAirwayBill(logAirwayBill, getDailyReportRPXWithCXStatus(), activityReportData, fileUploadLog);
		
		assertTrue(result);
	}
	
	@Test
	public void composeLogAirwayBill_invalidPUDate_updateFail(){
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
		
		DailyReportNCS dailyReportRPX = getDailyReportRPXWithCXStatus();
		dailyReportRPX.setPuDate("");
		
		boolean result = sutSpy.composeLogAirwayBill(logAirwayBill, dailyReportRPX, activityReportData, fileUploadLog);
		
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
		
		DailyReportNCS dailyReportRPX = getDailyReportRPXWithDStatus();
		dailyReportRPX.setReceived("invalid date");
		
		boolean result = sutSpy.composeLogAirwayBill(getLogAirwayBill(), dailyReportRPX, activityReportData, fileUploadLog);
		
		assertEquals(1, activityReportData.getFailedItemList().size());
		assertFalse(result);
	}
	
	@Test
	public void composeLogAirwayBill_newOrderItemStatusIsD_receivedRecipientRelationIsNotEmptyInLogAirwayBill(){
		ActivityReportData activityReportData = new ActivityReportData();
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		
		DailyReportNCS dailyReportRPX = getDailyReportRPXWithDStatus();
		
		LogAirwayBill logAirwayBill = getLogAirwayBill();
		
		sutSpy.composeLogAirwayBill(logAirwayBill, dailyReportRPX, activityReportData, fileUploadLog);
		
		assertNotNull(logAirwayBill.getReceived());
		assertFalse(logAirwayBill.getRecipient().isEmpty());
		assertFalse(logAirwayBill.getRelation().isEmpty());
		
	}
	
	@Test
	public void composeLogAirwayBillAfterAWBEngine_newOrderItemStatusIsD_receivedRecipientRelationIsNotEmptyInAirwayBillTransaction(){
		ActivityReportData activityReportData = new ActivityReportData();
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		
		DailyReportNCS dailyReportRPX = getDailyReportRPXWithDStatus();
		
		AirwayBillTransaction airwayBillTransaction = new AirwayBillTransaction();
		airwayBillTransaction.setWeight((double)5);
		
		sutSpy.composeLogAirwayBillAfterAWBEngine(getLogAirwayBill(), dailyReportRPX, activityReportData, fileUploadLog, airwayBillTransaction);
		
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
	
	private DailyReportNCS getDailyReportRPXWithCXStatus(){
		dailyReportRPX.setStatus("");
		dailyReportRPX.setTrNo("");
		dailyReportRPX.setReceived("");
		dailyReportRPX.setRecipient("");
		dailyReportRPX.setPuDate("01-JAN-2014");
		dailyReportRPX.setRefNo("O-1234-12345-1");
		return dailyReportRPX;
	}
	
	private DailyReportNCS getDailyReportRPXWithDStatus(){
		// 3 data
		dailyReportRPX.setStatus("OK");
		dailyReportRPX.setReceived("01-JAN-2014");
		dailyReportRPX.setRecipient("Test");
		
		dailyReportRPX.setTrNo("");
		dailyReportRPX.setConsignee("");
		dailyReportRPX.setPuDate("01-JAN-2014");
		dailyReportRPX.setRefNo("O-1234-12345-1");
		return dailyReportRPX;
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
		dailyReportRPX = null;
	}
}
