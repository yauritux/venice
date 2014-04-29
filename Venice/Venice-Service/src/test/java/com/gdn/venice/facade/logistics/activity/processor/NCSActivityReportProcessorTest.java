package com.gdn.venice.facade.logistics.activity.processor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
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
public class NCSActivityReportProcessorTest {
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

	private NCSActivityReportProcessor sut;
	
	private NCSActivityReportProcessor sutSpy;
	
	private DailyReportNCS dailyReportNCS;
	
	@Before
	public void setup(){
		mockStatic(Log4jLoggerFactory.class);
		PowerMockito.when(Log4jLoggerFactory.getLogger(anyString())).thenReturn(logMock);
		
		sut = new NCSActivityReportProcessor();
		sutSpy = spy(sut);
		
		sutSpy.filter = filterMock;
		sutSpy.venOrderItemDAO = venOrderItemDAOMock;
		sutSpy.venReturnItemDAO = venReturnItemDAOMock;
		sutSpy.logFileUploadLogDAO = logFileUploadLogDAOMock;
		sutSpy.logAirwayBillDAO = logAirwayBillDAOMock;
		sutSpy.venOrderItemService = venOrderItemServiceMock;
		sutSpy.awbConn = awbConnMock;
		
		dailyReportNCS = new DailyReportNCS();
	}
	
	@Test
	public void processEachOrderItem_orderItemBeforeAirwaybillEngine_executeProcessOrderItemBeforeAWBEngine() throws Exception{
		ActivityReportData activityReportData = new ActivityReportData();
		
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		VenOrderItem orderItemES = new VenOrderItem();
		orderItemES.setVenOrderStatus(VenOrderStatusES.createVenOrderStatus());
		
		AirwayBillTransaction awbTransaction = new AirwayBillTransaction();
		awbTransaction.setKodeLogistik(""+VeniceConstants.VEN_LOGISTICS_PROVIDER_MSG);
		
		when(venOrderItemDAOMock.findWithVenOrderStatusByWcsOrderItemId(anyString()))
			.thenReturn(orderItemES);
		
		when(filterMock.getWcsOrderItemId(anyString())).thenReturn("12345");
		
		when(logAirwayBillDAOMock.countByGdnReference(anyString()))
			.thenReturn(1);
		
		when(awbConnMock.getAirwayBillTransaction(anyString()))
			.thenReturn(awbTransaction);
		
		when(awbConnMock.getAirwayBillTransaction(anyString()))
			.thenReturn(null);
		
		doNothing().when(sutSpy).processOrderItemBeforeAWBEngine(orderItemES, getDailyReportNCSWithCXStatus(), activityReportData, fileUploadLog);
		
		sutSpy.processEachOrderItem(getDailyReportNCSWithCXStatus(), activityReportData, fileUploadLog);
		
		verify(sutSpy, times(1)).processOrderItemBeforeAWBEngine(orderItemES, getDailyReportNCSWithCXStatus(), activityReportData, fileUploadLog);
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
		
		doNothing().when(sutSpy).processOrderItemAfterAWBEngine(orderItemES, getDailyReportNCSWithCXStatus(), activityReportData, fileUploadLog, awbTransaction);
		doReturn(false).when(sutSpy).differentLogisticProvider(getDailyReportNCSWithCXStatus(),activityReportData,null);
		
		sutSpy.processEachOrderItem(getDailyReportNCSWithCXStatus(), activityReportData, fileUploadLog);
		
		verify(sutSpy, times(1)).processOrderItemAfterAWBEngine(orderItemES, getDailyReportNCSWithCXStatus(), activityReportData, fileUploadLog, awbTransaction);
	}
	
	@Test
	public void differentLogisticProvider_sameGdnReffDifferentLogisticProvider_executeDifferentLogisticProvider() throws Exception{
		ActivityReportData activityReportData = new ActivityReportData();
		
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		VenOrderItem orderItemES = new VenOrderItem();
		orderItemES.setVenOrderStatus(VenOrderStatusES.createVenOrderStatus());
		
		AirwayBillTransaction awbTransaction = new AirwayBillTransaction();
		awbTransaction.setKodeLogistik(""+VeniceConstants.VEN_LOGISTICS_PROVIDER_MSG);
		
		when(venOrderItemDAOMock.findWithVenOrderStatusByWcsOrderItemId(anyString()))
			.thenReturn(orderItemES);
		
		when(filterMock.getWcsOrderItemId(anyString())).thenReturn("12345");
		
		when(logAirwayBillDAOMock.countByGdnReference(anyString()))
			.thenReturn(1);
		
		when(awbConnMock.getAirwayBillTransaction(anyString()))
			.thenReturn(awbTransaction);

		sutSpy.differentLogisticProvider(getDailyReportNCSWithCXStatus(),activityReportData,""+VeniceConstants.VEN_LOGISTICS_PROVIDER_MSG);
		
		assertEquals(1, activityReportData.getFailedProviderForGdnReff().size());
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
		
		boolean result = sutSpy.composeLogAirwayBill(logAirwayBill, getDailyReportNCSWithCXStatus(), activityReportData, fileUploadLog);
		
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
		
		DailyReportNCS dailyReportNCS = getDailyReportNCSWithCXStatus();
		dailyReportNCS.setPuDate("");
		
		boolean result = sutSpy.composeLogAirwayBill(logAirwayBill, dailyReportNCS, activityReportData, fileUploadLog);
		
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
		
		DailyReportNCS dailyReportNCS = getDailyReportNCSWithDStatus();
		dailyReportNCS.setReceived("invalid date");
		
		boolean result = sutSpy.composeLogAirwayBill(getLogAirwayBill(), dailyReportNCS, activityReportData, fileUploadLog);
		
		assertEquals(1, activityReportData.getFailedItemList().size());
		assertFalse(result);
	}
	
	@Test
	public void composeLogAirwayBill_newOrderItemStatusIsD_receivedRecipientRelationIsNotEmptyInLogAirwayBill(){
		ActivityReportData activityReportData = new ActivityReportData();
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		
		DailyReportNCS dailyReportNCS = getDailyReportNCSWithDStatus();
		
		LogAirwayBill logAirwayBill = getLogAirwayBill();
		
		sutSpy.composeLogAirwayBill(logAirwayBill, dailyReportNCS, activityReportData, fileUploadLog);
		
		assertNotNull(logAirwayBill.getReceived());
		assertFalse(logAirwayBill.getRecipient().isEmpty());
		assertFalse(logAirwayBill.getRelation().isEmpty());
		
	}
	
	@Test
	public void composeLogAirwayBillAfterAWBEngine_newOrderItemStatusIsD_receivedRecipientRelationIsNotEmptyInAirwayBillTransaction(){
		ActivityReportData activityReportData = new ActivityReportData();
		LogFileUploadLog fileUploadLog = new LogFileUploadLog();
		
		DailyReportNCS dailyReportNCS = getDailyReportNCSWithDStatus();
		
		AirwayBillTransaction airwayBillTransaction = new AirwayBillTransaction();
		airwayBillTransaction.setWeight((double)5);
		
		sutSpy.composeLogAirwayBillAfterAWBEngine(getLogAirwayBill(), dailyReportNCS, activityReportData, fileUploadLog, airwayBillTransaction);
		
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
	
	private DailyReportNCS getDailyReportNCSWithCXStatus(){
		dailyReportNCS.setStatus("");
		dailyReportNCS.setTrNo("");
		dailyReportNCS.setReceived("");
		dailyReportNCS.setRecipient("");
		dailyReportNCS.setPuDate("01-JAN-2014");
		dailyReportNCS.setRefNo("O-1234-12345-1");
		return dailyReportNCS;
	}
	
	private DailyReportNCS getDailyReportNCSWithDStatus(){
		// 3 data
		dailyReportNCS.setStatus("OK");
		dailyReportNCS.setReceived("01-JAN-2014");
		dailyReportNCS.setRecipient("Test");
		
		dailyReportNCS.setTrNo("");
		dailyReportNCS.setConsignee("");
		dailyReportNCS.setPuDate("01-JAN-2014");
		dailyReportNCS.setRefNo("O-1234-12345-1");
		return dailyReportNCS;
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
		dailyReportNCS = null;
	}
}
