package com.gdn.venice.facade.logistics.activity.filter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;

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
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.exception.ActivityReportDataFilterException;
import com.gdn.venice.facade.logistics.activity.ActivityReportData;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.logistics.dataimport.DailyReportJNE;
import com.gdn.venice.logistics.integration.AirwayBillEngineConnector;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Log4jLoggerFactory.class)
public class JNEActivityReportDataFilterTest {
	@Mock
	private Logger logMock;
	@Mock
	private AirwayBillEngineConnector awbConnMock;
	@Mock
	private VenOrderItemDAO venOrderItemDAOMock;
	
	private JNEActivityReportDataFilter sut;
	
	private static String GDN_REF = "O-1234-12345-2";
	private static String GDN_REF_RETURN = "R-1234-12345-1";
	
	@Before
	public void setup(){
		mockStatic(Log4jLoggerFactory.class);
		PowerMockito.when(Log4jLoggerFactory.getLogger(anyString())).thenReturn(logMock);

		sut = new JNEActivityReportDataFilter();
		sut.awbConn = awbConnMock;
		sut.venOrderItemDAO = venOrderItemDAOMock;
	}
	
	@Test
	public void isGdnReferenceFormatCorrect_passInCorrectGdnRefFormat_returnsTrue(){
		boolean isFormatCorrect = sut.isGdnReferenceFormatCorrect(GDN_REF);
		
		assertTrue(isFormatCorrect);
	}
	
	@Test
	public void isGdnReferenceFormatCorrect_passInDoubleGdnRefFormat_returnsFalse(){
		boolean isFormatCorrect = sut.isGdnReferenceFormatCorrect("O-1378612-4293114-1/O-1378612-4293114-2");
		
		assertFalse(isFormatCorrect);
	}
	
	@Test
	public void isGdnReferenceFormatCorrect_passInIncorrectOrderFormat_returnsFalse(){
		boolean isFormatCorrect = sut.isGdnReferenceFormatCorrect("o-1234-12345-1");
		
		assertFalse(isFormatCorrect);
	}
	
	@Test
	public void isGdnReferenceFormatCorrect_passInIncorrectGdnRefFormat_returnsFalse(){
		boolean isFormatCorrect = sut.isGdnReferenceFormatCorrect("O-1234-12345");
		
		assertFalse(isFormatCorrect);
	}
	
	@Test
	public void getIsReturnOrderItem_passInGdnRefReturn_returnsTrue(){
		boolean isReturnOrderItem = sut.isReturnOrderItem(GDN_REF_RETURN);
		
		assertTrue(isReturnOrderItem);
	}
	
	@Test
	public void getOrderOrRMA_passInGdnRef_returnsCorrectOrderOrRMA(){
		String orderOrRMA = sut.getOrderOrRMA(GDN_REF);
		
		assertEquals("O", orderOrRMA);
	}
	
	@Test
	public void getWcsOrderId_passInGdnRef_returnsCorrectWcsOrderId(){
		String wcsOrderId = sut.getWcsOrderId(GDN_REF);
		
		assertEquals("1234", wcsOrderId);
	}
	
	@Test
	public void getWcsOrderItemId_passInGdnRef_returnsCorrectWcsOrderItemId(){
		String wcsOrderItemId = sut.getWcsOrderItemId(GDN_REF);
		
		assertEquals("12345", wcsOrderItemId);
	}
	
	@Test
	public void getSequence_passInGdnRef_returnsCorrectSequence(){
		String sequence = sut.getSequence(GDN_REF);
		
		assertEquals("2", sequence);
	}
	
	@Test
	public void filter_orderItemNotFound_orderItemRemovedFromList() throws ActivityReportDataFilterException{
		DailyReportJNE jneItem = new DailyReportJNE();
		jneItem.setGdnRefNumber(GDN_REF);
		
		ArrayList<PojoInterface> orderItemList = new ArrayList<PojoInterface>();
		orderItemList.add(jneItem);
		
		ActivityReportData data = new ActivityReportData();
		data.setOrderItemList(orderItemList);
		
		when(venOrderItemDAOMock.countByWcsOrderItemId("12345")).thenReturn(0);
		
		data = sut.filter(data);
		
		assertEquals(0, data.getOrderItemList().size());
	}
	
	@Test
	public void filter_orderItemNotFound_gdnRefNotFoundWillNotEmpty() throws ActivityReportDataFilterException{
		DailyReportJNE jneItem = new DailyReportJNE();
		jneItem.setGdnRefNumber(GDN_REF);
		
		ArrayList<PojoInterface> orderItemList = new ArrayList<PojoInterface>();
		orderItemList.add(jneItem);
		
		ActivityReportData data = new ActivityReportData();
		data.setOrderItemList(orderItemList);
		
		when(venOrderItemDAOMock.countByWcsOrderItemId("12345")).thenReturn(0);
		
		data = sut.filter(data);
		
		assertEquals(1, data.getGdnRefNotFoundList().size());
	}
	
	@Test
	public void filter_orderItemFound_orderItemNotRemovedFromList() throws ActivityReportDataFilterException{
		DailyReportJNE jneItem = new DailyReportJNE();
		jneItem.setGdnRefNumber(GDN_REF);
		
		ArrayList<PojoInterface> orderItemList = new ArrayList<PojoInterface>();
		orderItemList.add(jneItem);
		
		ActivityReportData data = new ActivityReportData();
		data.setOrderItemList(orderItemList);
		
		when(venOrderItemDAOMock.countByWcsOrderItemId("12345")).thenReturn(1);
		
		data = sut.filter(data);
		
		assertEquals(1, data.getOrderItemList().size());
	}
	
	@Test
	public void filter_orderItemFound_gdnRefNotFoundWillEmpty() throws ActivityReportDataFilterException{
		DailyReportJNE jneItem = new DailyReportJNE();
		jneItem.setGdnRefNumber(GDN_REF);
		
		ArrayList<PojoInterface> orderItemList = new ArrayList<PojoInterface>();
		orderItemList.add(jneItem);
		
		ActivityReportData data = new ActivityReportData();
		data.setOrderItemList(orderItemList);
		
		when(venOrderItemDAOMock.countByWcsOrderItemId("12345")).thenReturn(1);
		
		data = sut.filter(data);
		
		assertEquals(0, data.getGdnRefNotFoundList().size());
	}
	
	@Test
	public void filter_orderItemIsBopisOrBigProduct_failedItemListWillNotEmpty() throws ActivityReportDataFilterException{
		DailyReportJNE jneItem = new DailyReportJNE();
		jneItem.setGdnRefNumber(GDN_REF);
		
		ArrayList<PojoInterface> orderItemList = new ArrayList<PojoInterface>();
		orderItemList.add(jneItem);
		
		ActivityReportData data = new ActivityReportData();
		data.setOrderItemList(orderItemList);
		
		when(venOrderItemDAOMock.countByWcsOrderItemId("12345")).thenReturn(1);
		when(venOrderItemDAOMock.countWhereLogisticServiceIsBopisOrBigProductByWcsOrderItemId("12345")).thenReturn(1);
		
		data = sut.filter(data);
		
		assertEquals(1, data.getFailedItemList().size());
	}
	
	@Test
	public void filter_gdnRefFormatInvalid_gdnRefNotFoundWillNotEmpty() throws ActivityReportDataFilterException{
		DailyReportJNE jneItem = new DailyReportJNE();
		jneItem.setGdnRefNumber("O-1378612-4293114-1/O-1378612-4293114-2");
		
		ArrayList<PojoInterface> orderItemList = new ArrayList<PojoInterface>();
		orderItemList.add(jneItem);
		
		ActivityReportData data = new ActivityReportData();
		data.setOrderItemList(orderItemList);
		
		data = sut.filter(data);
		
		assertEquals(1, data.getGdnRefNotFoundList().size());
	}
	
	@After
	public void shutdown(){
		logMock = null;
		awbConnMock = null;
		venOrderItemDAOMock = null;
		sut = null;
	}

}
