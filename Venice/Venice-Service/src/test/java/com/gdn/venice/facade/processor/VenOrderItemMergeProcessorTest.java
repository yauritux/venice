package com.gdn.venice.facade.processor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.facade.spring.LogAirwayBillService;
import com.gdn.venice.facade.spring.PublisherService;
import com.gdn.venice.facade.spring.VenOrderItemStatusHistoryService;
import com.gdn.venice.factory.VenOrderStatusC;
import com.gdn.venice.factory.VenOrderStatusCR;
import com.gdn.venice.factory.VenOrderStatusCX;
import com.gdn.venice.factory.VenOrderStatusD;
import com.gdn.venice.factory.VenOrderStatusES;
import com.gdn.venice.factory.VenOrderStatusFP;
import com.gdn.venice.factory.VenOrderStatusPF;
import com.gdn.venice.factory.VenOrderStatusPP;
import com.gdn.venice.factory.VenOrderStatusPU;
import com.gdn.venice.factory.VenOrderStatusRT;
import com.gdn.venice.factory.VenOrderStatusX;
import com.gdn.venice.persistence.VenOrderItem;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log4jLoggerFactory.class})
public class VenOrderItemMergeProcessorTest {
	// SUT
	private VenOrderItemMergeProcessor processor; 
	@Mock
	private Logger logMock;
	@Mock
	private VenOrderItemDAO venOrderItemDAO;
	@Mock
	private VenOrderItemStatusHistoryService venOrderItemStatusHistoryService;
	@Mock
	private LogAirwayBillService logAirwayBillService;
	@Mock
	private PublisherService publisherService;
	@Mock
	private EntityManager emMock;

	private VenOrderItem orderItemWithStatusFP;
	private VenOrderItem orderItemWithStatusPF;
	private VenOrderItem orderItemWithStatusCR;
	private VenOrderItem orderItemWithStatusPU;
	private VenOrderItem orderItemWithStatusES;
	private VenOrderItem orderItemWithStatusCX;
	private VenOrderItem orderItemWithStatusD;
	private VenOrderItem orderItemWithStatusPP;
	private VenOrderItem orderItemWithStatusRT;
	private VenOrderItem orderItemWithStatusX;
	private VenOrderItem orderItemWithStatusC;
	
	@Before
	public void setup(){
		mockStatic(Log4jLoggerFactory.class);
		PowerMockito.when(Log4jLoggerFactory.getLogger(anyString())).thenReturn(logMock);
		
		processor = new VenOrderItemMergeProcessor(); // SUT
		
		processor.venOrderItemDAO = venOrderItemDAO;
		processor.venOrderItemStatusHistoryService = venOrderItemStatusHistoryService;
		processor.logAirwayBillService = logAirwayBillService;
		processor.publisherService = publisherService;
		processor.em = emMock;
		
		orderItemWithStatusC = new VenOrderItem();
		orderItemWithStatusC.setVenOrderStatus(VenOrderStatusC.createVenOrderStatus());
		
		orderItemWithStatusFP = new VenOrderItem();
		orderItemWithStatusFP.setVenOrderStatus(VenOrderStatusFP.createVenOrderStatus());

		orderItemWithStatusPF = new VenOrderItem();
		orderItemWithStatusPF.setVenOrderStatus(VenOrderStatusPF.createVenOrderStatus());
		
		orderItemWithStatusCR = new VenOrderItem();
		orderItemWithStatusCR.setVenOrderStatus(VenOrderStatusCR.createVenOrderStatus());
		
		orderItemWithStatusPU = new VenOrderItem();
		orderItemWithStatusPU.setVenOrderStatus(VenOrderStatusPU.createVenOrderStatus());

		orderItemWithStatusES = new VenOrderItem();
		orderItemWithStatusES.setVenOrderStatus(VenOrderStatusES.createVenOrderStatus());

		orderItemWithStatusPP = new VenOrderItem();
		orderItemWithStatusPP.setVenOrderStatus(VenOrderStatusPP.createVenOrderStatus());
		
		orderItemWithStatusCX = new VenOrderItem();
		orderItemWithStatusCX.setVenOrderStatus(VenOrderStatusCX.createVenOrderStatus());
		
		orderItemWithStatusRT = new VenOrderItem();
		orderItemWithStatusRT.setVenOrderStatus(VenOrderStatusRT.createVenOrderStatus());
		
		orderItemWithStatusD = new VenOrderItem();
		orderItemWithStatusD.setVenOrderStatus(VenOrderStatusD.createVenOrderStatus());
		
		orderItemWithStatusX = new VenOrderItem();
		orderItemWithStatusX.setVenOrderStatus(VenOrderStatusX.createVenOrderStatus());
		
	}
	
	private void commonPreMergeTest(VenOrderItem existingOrderItem, VenOrderItem newOrderItem){
		when(venOrderItemDAO
				.findWithVenOrderStatusByWcsOrderItemId(newOrderItem.getWcsOrderItemId()))
					.thenReturn(existingOrderItem);
		
		doNothing().when(emMock).detach(newOrderItem);
		
		processor.preMerge(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoES_DOESNOTPublishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusES;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService, never()).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoES_DOESNOTAddOrderItemStatusHistory(){
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusES;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService, never()).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoES_DOESNOTAddDummyLogAirwayBill(){
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusES;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService, never()).addDummyLogAirwayBillForNewlyFPOrderItem(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromCtoFP_DOESNOTPublishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusC;
		VenOrderItem newOrderItem = orderItemWithStatusFP;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService, never()).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromCtoFP_DOESNOTAddOrderItemStatusHistory(){
		VenOrderItem existingOrderItem = orderItemWithStatusC;
		VenOrderItem newOrderItem = orderItemWithStatusFP;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService, never()).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromCtoFP_addDummyLogAirwayBill(){
		VenOrderItem existingOrderItem = orderItemWithStatusC;
		VenOrderItem newOrderItem = orderItemWithStatusFP;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService).addDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromPUtoES_publishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusPU;
		VenOrderItem newOrderItem = orderItemWithStatusES;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromPUtoES_addOrderItemStatusHistory(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusPU;
		VenOrderItem newOrderItem = orderItemWithStatusES;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromPUtoES_DOESNOTAddDummyLogAirwayBill(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusPU;
		VenOrderItem newOrderItem = orderItemWithStatusES;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService, never()).addDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoPP_publishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusPP;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoPP_addOrderItemStatusHistory(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusPP;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoPP_DOESNOTAddDummyLogAirwayBill(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusPP;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService, never()).addDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoCX_publishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusCX;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoCX_addOrderItemStatusHistory(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusCX;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoCX_DOESNOTAddDummyLogAirwayBill(){
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusCX;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService, never()).addDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromPPtoCX_publishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusPP;
		VenOrderItem newOrderItem = orderItemWithStatusCX;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromPPtoCX_addOrderItemStatusHistory(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusPP;
		VenOrderItem newOrderItem = orderItemWithStatusCX;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromPPtoCX_DOESNOTAddDummyLogAirwayBill(){
		VenOrderItem existingOrderItem = orderItemWithStatusPP;
		VenOrderItem newOrderItem = orderItemWithStatusCX;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService, never()).addDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoRT_publishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusRT;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoRT_addOrderItemStatusHistory(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusRT;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoRT_DOESNOTAddDummyLogAirwayBill(){
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusRT;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService, never()).addDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoD_publishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusD;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoD_addOrderItemStatusHistory(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusD;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromEStoD_DOESNOTAddDummyLogAirwayBill(){
		VenOrderItem existingOrderItem = orderItemWithStatusES;
		VenOrderItem newOrderItem = orderItemWithStatusD;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService, never()).addDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromCXtoD_publishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusCX;
		VenOrderItem newOrderItem = orderItemWithStatusD;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromCXtoD_addOrderItemStatusHistory(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusCX;
		VenOrderItem newOrderItem = orderItemWithStatusD;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromCXtoD_DOESNOTAddDummyLogAirwayBill(){
		VenOrderItem existingOrderItem = orderItemWithStatusCX;
		VenOrderItem newOrderItem = orderItemWithStatusD;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService, never()).addDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromPFtoFP_publishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusPF;
		VenOrderItem newOrderItem = orderItemWithStatusFP;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromPFtoFP_DOESNOTAddOrderItemStatusHistory(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusPF;
		VenOrderItem newOrderItem = orderItemWithStatusFP;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService, never()).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromPFtoFP_DOESNOTAddDummyLogAirwayBill(){
		VenOrderItem existingOrderItem = orderItemWithStatusPF;
		VenOrderItem newOrderItem = orderItemWithStatusFP;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService, never()).addDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromCRtoFP_publishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusCR;
		VenOrderItem newOrderItem = orderItemWithStatusCR;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromCRtoFP_DOESNOTAddOrderItemStatusHistory(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusCR;
		VenOrderItem newOrderItem = orderItemWithStatusCR;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService, never()).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromCRtoFP_DOESNOTAddDummyLogAirwayBill(){
		VenOrderItem existingOrderItem = orderItemWithStatusCR;
		VenOrderItem newOrderItem = orderItemWithStatusCR;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService, never()).addDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromFPtoX_DOESNOTPublishUpdateOrderItemStatusMessage(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusFP;
		VenOrderItem newOrderItem = orderItemWithStatusX;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(publisherService, never()).publishUpdateOrderItemStatus(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromFPtoX_addOrderItemStatusHistory(){
		
		VenOrderItem existingOrderItem = orderItemWithStatusFP;
		VenOrderItem newOrderItem = orderItemWithStatusX;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(venOrderItemStatusHistoryService).saveVenOrderItemStatusHistory(newOrderItem);
	}
	
	@Test
	public void preMerge_orderItemStatusChangeFromFPtoX_DOESNOTAddDummyLogAirwayBill(){
		VenOrderItem existingOrderItem = orderItemWithStatusFP;
		VenOrderItem newOrderItem = orderItemWithStatusX;
		
		commonPreMergeTest(existingOrderItem, newOrderItem);
		
		verify(logAirwayBillService, never()).addDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItem);
	}
	
}
