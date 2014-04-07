package com.gdn.venice.facade.spring.fraud;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.gdn.venice.constants.VenOrderStatusConstants;
import com.gdn.venice.constants.VenPaymentTypeConstants;
import com.gdn.venice.constants.VeniceEnvironment;
import com.gdn.venice.dao.FrdFraudSuspicionCaseDAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.factory.VenOrderStatusC;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPayment;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.persistence.VenOrderStatus;
import com.gdn.venice.persistence.VenPaymentType;
import com.gdn.venice.util.CommonUtil;

@RunWith(MockitoJUnitRunner.class)
public class FraudCalculationServiceImplTest {
	
	FraudCalculationServiceImpl sut;
	@Mock
	VenOrderDAO venOrderDAOMock;
	@Mock
	FrdFraudSuspicionCaseDAO frdFraudSuspicionCaseDAOMock;
	
	VeniceEnvironment veniceEnv;
	
	@Before
	public void setup(){
		sut = new FraudCalculationServiceImpl();
		sut.venOrderDAO = venOrderDAOMock;
		sut.fraudSuspicionCaseDAO = frdFraudSuspicionCaseDAOMock;
		
		veniceEnv = VeniceEnvironment.TESTING;
		CommonUtil.veniceEnv = veniceEnv;
	}
	
	@Test
	public void getOrderPaidByCreditCard_anyStartDate_returnsOrderPaidByCreditCard(){
		String date = "2014-01-01";
		when(venOrderDAOMock.findPaidByCreditCardAndStatusC(anyString())).thenReturn(getOrderListPaidByCreditCard());
		
		List<VenOrder> orderList = sut.getOrderPaidByCreditCard(date);
		
		VenOrder result = orderList.get(0);
		
		assertEquals(VenPaymentTypeConstants.VEN_PAYMENT_TYPE_CC.id(), 
				     result
				     	.getVenOrderPaymentAllocations()
				     		.get(0)
				     			.getVenOrderPayment()
				     				.getVenPaymentType().getPaymentTypeId().longValue());
		
	}
	
	@Test
	public void getOrderPaidByCreditCard_anyStartDate_returnsOrderWithStatusIsC(){
		String date = "2014-01-01";
		when(venOrderDAOMock.findPaidByCreditCardAndStatusC(anyString())).thenReturn(getOrderListPaidByCreditCard());
		
		List<VenOrder> orderList = sut.getOrderPaidByCreditCard(date);
		
		VenOrder result = orderList.get(0);
		
		assertEquals(VenOrderStatusConstants.VEN_ORDER_STATUS_C.code(), 
				     result
				     	.getVenOrderStatus()
				     		.getOrderStatusId().longValue());
		
	}
	
	
	private List<VenOrder> getOrderListPaidByCreditCard(){
		List<VenOrder> orderList = new ArrayList<VenOrder>(2);
		
		VenPaymentType paymentWithCC = new VenPaymentType();
		paymentWithCC.setPaymentTypeId(VenPaymentTypeConstants.VEN_PAYMENT_TYPE_CC.id());
		
		VenOrderStatus statusC = VenOrderStatusC.createVenOrderStatus();
		
		VenOrder order1 = new VenOrder();
		order1.setOrderId(1L);
		order1.setWcsOrderId("1");
		order1.setVenOrderStatus(statusC);
		
		VenOrderPayment payment1 = new VenOrderPayment();
		payment1.setVenPaymentType(paymentWithCC);
		
		VenOrderPaymentAllocation paymentAllocation1 = new VenOrderPaymentAllocation();
		paymentAllocation1.setVenOrderPayment(payment1);
		
		order1.setVenOrderPaymentAllocations(new ArrayList<VenOrderPaymentAllocation>(Arrays.asList(paymentAllocation1)));
		
		orderList.add(order1);
		
		return orderList;
	}
	
	@Test
	public void isMIGSReportUploaded_orderWithMaskedCreditCardNotNullAndNotEmpty_returnsTrue(){
		boolean result = sut.isMIGSReportUploaded(getOrderWithMaskedCreditCardNotNullAndNotEmpty());
		
		assertTrue(result);
	}
	
	private VenOrder getOrderWithMaskedCreditCardNotNullAndNotEmpty(){
		VenOrder order = getOrderListPaidByCreditCard().get(0);
		order.getVenOrderPaymentAllocations().get(0).getVenOrderPayment().setMaskedCreditCardNumber("540912xxxxxxx607");
		
		return order;
	}
	
	@Test
	public void isMIGSReportUploaded_orderWithMaskedCreditCardNull_returnsFalse(){
		boolean result = sut.isMIGSReportUploaded(getOrderWithMaskedCreditCardNull());
		
		assertFalse(result);
	}
	
	private VenOrder getOrderWithMaskedCreditCardNull(){
		VenOrder order = getOrderListPaidByCreditCard().get(0);
		order.getVenOrderPaymentAllocations().get(0).getVenOrderPayment().setMaskedCreditCardNumber(null);
		
		return order;
	}
	
	@Test
	public void isMIGSReportUploaded_orderWithMaskedCreditCardEmpty_returnsFalse(){
		boolean result = sut.isMIGSReportUploaded(getOrderWithMaskedCreditCardEmpty());
		
		assertFalse(result);
	}
	
	private VenOrder getOrderWithMaskedCreditCardEmpty(){
		VenOrder order = getOrderListPaidByCreditCard().get(0);
		order.getVenOrderPaymentAllocations().get(0).getVenOrderPayment().setMaskedCreditCardNumber("");
		
		return order;
	}
	
	@Test
	public void isRiskPointCalculatedBefore_orderRiskPointHasNotCalculated_returnsFalse(){
		when(frdFraudSuspicionCaseDAOMock.countByVenOrderId(anyLong())).thenReturn(0);
		
		boolean result = sut.isRiskPointCalculatedBefore(getOrderWithMaskedCreditCardNotNullAndNotEmpty());
		
		assertFalse(result);
	}
	
	@Test
	public void isRiskPointCalculatedBefore_orderRiskPointCalculated_returnsTrue(){
		when(frdFraudSuspicionCaseDAOMock.countByVenOrderId(anyLong())).thenReturn(1);
		
		boolean result = sut.isRiskPointCalculatedBefore(getOrderWithMaskedCreditCardNotNullAndNotEmpty());
		
		assertTrue(result);
	}
	
	@After
	public void shutdown(){
		sut = null;
		venOrderDAOMock = null;
		frdFraudSuspicionCaseDAOMock = null;
		
		veniceEnv = VeniceEnvironment.PRODUCTION;
		CommonUtil.veniceEnv = veniceEnv;
	}
}
