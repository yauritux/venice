package com.gdn.venice.inbound.services;

/*
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.stub;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.integration.jaxb.Customer;
import com.gdn.integration.jaxb.MasterData;
import com.gdn.integration.jaxb.Order;
import com.gdn.integration.jaxb.OrderItem;
import com.gdn.integration.jaxb.Payment;
import com.gdn.venice.constants.OrderConstants;
import com.gdn.venice.constants.VeniceEnvironment;
import com.gdn.venice.dao.FinArFundsInReconRecordDAO;
import com.gdn.venice.dao.LogLogisticServiceDAO;
import com.gdn.venice.dao.LogLogisticsProviderDAO;
import com.gdn.venice.dao.VenAddressDAO;
import com.gdn.venice.dao.VenAddressTypeDAO;
import com.gdn.venice.dao.VenBankDAO;
import com.gdn.venice.dao.VenCityDAO;
import com.gdn.venice.dao.VenContactDetailDAO;
import com.gdn.venice.dao.VenContactDetailTypeDAO;
import com.gdn.venice.dao.VenCountryDAO;
import com.gdn.venice.dao.VenCustomerDAO;
import com.gdn.venice.dao.VenFraudCheckStatusDAO;
import com.gdn.venice.dao.VenMerchantDAO;
import com.gdn.venice.dao.VenMerchantProductDAO;
import com.gdn.venice.dao.VenOrderAddressDAO;
import com.gdn.venice.dao.VenOrderBlockingSourceDAO;
import com.gdn.venice.dao.VenOrderContactDetailDAO;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.dao.VenOrderItemAddressDAO;
import com.gdn.venice.dao.VenOrderItemAdjustmentDAO;
import com.gdn.venice.dao.VenOrderItemContactDetailDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.dao.VenOrderItemStatusHistoryDAO;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.dao.VenOrderPaymentDAO;
import com.gdn.venice.dao.VenOrderStatusDAO;
import com.gdn.venice.dao.VenOrderStatusHistoryDAO;
import com.gdn.venice.dao.VenPartyAddressDAO;
import com.gdn.venice.dao.VenPartyDAO;
import com.gdn.venice.dao.VenPartyTypeDAO;
import com.gdn.venice.dao.VenPaymentStatusDAO;
import com.gdn.venice.dao.VenPaymentTypeDAO;
import com.gdn.venice.dao.VenProductCategoryDAO;
import com.gdn.venice.dao.VenProductTypeDAO;
import com.gdn.venice.dao.VenPromotionDAO;
import com.gdn.venice.dao.VenRecipientDAO;
import com.gdn.venice.dao.VenStateDAO;
import com.gdn.venice.dao.VenWcsPaymentTypeDAO;
import com.gdn.venice.exception.InvalidOrderAmountException;
import com.gdn.venice.exception.InvalidOrderException;
import com.gdn.venice.exception.InvalidOrderFulfillmentStatusException;
import com.gdn.venice.exception.InvalidOrderStatusException;
import com.gdn.venice.exception.InvalidOrderTimestampException;
import com.gdn.venice.exception.NoOrderReceivedException;
import com.gdn.venice.inbound.services.impl.OrderServiceImpl;
import com.gdn.venice.persistence.VenContactDetail;
import com.gdn.venice.persistence.VenCustomer;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPartyAddress;
import com.gdn.venice.util.CommonUtil;

*//**
 * 
 * @author yauritux
 *
 *//*
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:OrderServiceTest-context.xml"})
@PrepareForTest({OrderServiceImpl.class, Log4jLoggerFactory.class, Logger.class, CommonUtil.class})
*/
public class OrderCreationTest {
/*		
	@Autowired
	private VenAddressDAO venAddressDAO;
	
	@Autowired
	private VenCountryDAO venCountryDAO;
	
	@Autowired
	private VenPaymentStatusDAO venPaymentStatusDAO;
	
	@Autowired
	private VenStateDAO venStateDAO;
	
	@Autowired
	private VenPaymentTypeDAO venPaymentTypeDAO;
	
	@Autowired
	private VenPartyTypeDAO venPartyTypeDAO;
	
	@Autowired
	private VenContactDetailTypeDAO venContactDetailTypeDAO;
	
	@Autowired
	private VenOrderStatusDAO venOrderStatusDAO;
	
	@Autowired
	private VenFraudCheckStatusDAO venFraudCheckStatusDAO;
	
	@Autowired
	private LogLogisticServiceDAO logLogisticServiceDAO;
	
	@Autowired
	private LogLogisticsProviderDAO logLogisticProviderDAO;
	
	@Autowired
	private VenWcsPaymentTypeDAO venWcsPaymentTypeDAO;
	
	@Autowired
	private VenProductCategoryDAO venProductCategoryDAO;
	
	@Autowired
	private VenProductTypeDAO venProductTypeDAO;
	
	@Autowired
	private VenBankDAO venBankDAO;
	
	@Autowired
	private FinArFundsInReconRecordDAO finArFundsInReconRecordDAO;
	
	@Autowired
	private VenOrderAddressDAO venOrderAddressDAO;
	
	@Autowired
	private VenMerchantDAO venMerchantDAO;
	
	@Autowired
	private VenOrderBlockingSourceDAO venOrderBlockingSourceDAO;
	
	@Autowired
	private VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	@Autowired
	private VenPartyDAO venPartyDAO;
	
	@Autowired
	private VenPromotionDAO venPromotionDAO;
	
	@Autowired
	private VenOrderContactDetailDAO venOrderContactDetailDAO;
	
	@Autowired
	private VenContactDetailDAO venContactDetailDAO;	
	
	@Autowired
	private VenCustomerDAO venCustomerDAO;
	
	@Autowired
	private VenAddressTypeDAO venAddressTypeDAO;
	
	@Autowired
	private VenOrderDAO venOrderDAO;
	
	@Autowired	
	private VenOrderItemDAO venOrderItemDAO;
	
	@Autowired
	private VenMerchantProductDAO venMerchantProductDAO;
	
	@Autowired
	private VenOrderItemAddressDAO venOrderItemAddressDAO;
	
	@Autowired
	private VenOrderItemAdjustmentDAO venOrderItemAdjustmentDAO;
	
	@Autowired
	private VenOrderItemContactDetailDAO venOrderItemContactDetailDAO;
	
	@Autowired
	private VenOrderPaymentDAO venOrderPaymentDAO;
	
	@Autowired
	private VenOrderStatusHistoryDAO venOrderStatusHistoryDAO;
	
	@Autowired
	private VenOrderItemStatusHistoryDAO venOrderItemStatusHistoryDAO;
	
	@Autowired
	private VenPartyAddressDAO venPartyAddressDAO;
	
	@Autowired
	private VenRecipientDAO venRecipientDAO;
	
	@Autowired
	private VenCityDAO venCityDAO;	
	
	@Autowired
	@InjectMocks
	OrderService orderService;
	
	@Mock
	private EntityManager em;
	
	@Spy
	private Logger logger = Logger.getLogger("OrderCreationTest");
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private List<VenCustomer> customers;	
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Order order;
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private VenCustomer venCustomer = new VenCustomer();
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private VenOrder venOrder;	
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private VenParty venParty = new VenParty();
		
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private OrderItem item;
	
	@Spy
	Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
	
	@Spy
	private List<OrderItem> items = new ArrayList<OrderItem>();
	
	@Mock
	private List<Payment> payments;
	
	@Mock
	private List<VenContactDetail> venContactDetails;
	
	@Mock
	private List<VenPartyAddress> venPartyAddresses;
	
	@Mock
	private Iterator<Payment> paymentIterator;
	
	@BeforeClass
	public static void initialized() {
		CommonUtil.veniceEnv = VeniceEnvironment.TESTING;
	}
	
	@Before	
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);		

		stub(order.getAmount()).toReturn(new Double("100000"));
		stub(order.isRmaFlag()).toReturn(false); // Not an "Order Returned" 
		stub(order.getCustomer()).toReturn(new Customer());		
		stub(order.getOrderId()).toReturn(new MasterData());
		stub(order.getTimestamp()).toReturn(createOrderTimestamp());
		stub(order.getStatus()).toReturn("C");
		stub(item.getLogisticsInfo().getLogisticsProvider().getParty().getFullOrLegalName()).toReturn("TIKI JNE");
		items.add(item);
		stub(order.getOrderItems()).toReturn(items);
		stub(payments.isEmpty()).toReturn(false);				
		stub(order.getPayments()).toReturn(payments);
		stub(order.getPayments().iterator()).toReturn(paymentIterator);
		
		stub(customers.isEmpty()).toReturn(false);
		stub((Object) customers.get(0)).toReturn(venCustomer);
		
		stub(venContactDetailDAO.findByParty(any(VenParty.class))).toReturn(venContactDetails);
		stub(venCustomerDAO.findByWcsCustomerId(anyString())).toReturn(customers);
		stub(venCustomerDAO.findByCustomerName(anyString())).toReturn(customers);
		stub(venCustomerDAO.save(any(VenCustomer.class))).toReturn(venCustomer);
		stub(venPartyAddressDAO.findByVenParty(any(VenParty.class))).toReturn(venPartyAddresses);
	}
	
	private Answer<Object> createVenPartyAnswer() {
		return new Answer<Object> () {
			
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return venParty;
			}
		};
	}
	
	private Answer<Object> createVenCustomerAnswer() {
		return new Answer<Object> () {
			
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return venCustomer;
			}
		};
	}

	@Test(expected = NoOrderReceivedException.class, timeout = 1000)
	public void createOrder_NoOrderReceived_VENEX000002Caught() throws Exception {
		orderService.createOrder(null);
	}
	
	@Test(expected = InvalidOrderAmountException.class, timeout = 1000)
	public void createOrder_NoOrderAmount_VENEX000003Caught() throws Exception {
		stub(order.getAmount()).toReturn(null);
		orderService.createOrder(order);
	}
	
	//@Test(expected = CustomerNotFoundException.class, timeout = 1000)
	@Ignore
	public void createOrder_NoCustomerRecord_VENEX000004Caught() throws Exception {
		//stub(order.getCustomer()).toReturn(null);
		orderService.createOrder(order);
	}	
	
	@Test(expected = InvalidOrderException.class, timeout = 1000)
	public void createOrder_NoOrderID_VENEX000005Caught() throws Exception {
		stub(order.getOrderId()).toReturn(null);
		orderService.createOrder(order);
	}	
	
	@Test(expected = InvalidOrderException.class, timeout = 1000)
	public void createOrder_OrderItemsNull_VENEX000006Caught() throws Exception {
		stub(order.getOrderItems()).toReturn(null);
		orderService.createOrder(order);
	}
	
	@Test(expected = InvalidOrderException.class, timeout = 1000)
	public void createOrder_OrderItemsEmpty_VENEX000006Caught() throws Exception {
		stub(items.isEmpty()).toReturn(true);
		orderService.createOrder(order);
	}
	
	@Test(expected = InvalidOrderException.class, timeout = 1000)
	public void createOrder_NullPayment_VENEX000007Caught() throws Exception {
		stub(payments.isEmpty()).toReturn(true);
		orderService.createOrder(order);
	}
	
	@Test(expected = InvalidOrderTimestampException.class, timeout = 1000) 
	public void createOrder_NoTimestamp_VENEX000008Caught() throws Exception {
		stub(order.getTimestamp()).toReturn(null);
		orderService.createOrder(order);
	}
	
	@Test(expected = InvalidOrderStatusException.class, timeout = 1000)
	public void createOrder_NoOrderStatus_VENEX000009Caught() throws Exception {
		stub(order.getStatus()).toReturn(null);
		orderService.createOrder(order);
	}
	
	@Test(expected = InvalidOrderStatusException.class, timeout = 1000)
	public void createOrder_InvalidOrderStatus_VENEX000009Caught() throws Exception {
		stub(order.getStatus()).toReturn("F");
		orderService.createOrder(order);
	}
	
	@Test(expected = InvalidOrderFulfillmentStatusException.class, timeout = 1000) 
	public void createOrder_InvalidOrderFulfillmentStatus_VENEX000010Caught() throws Exception {
		stub(order.getFullfillmentStatus()).toReturn(OrderConstants.VEN_FULFILLMENT_STATUS_ONE.code());
		orderService.createOrder(order);
	}
	
	@Test(expected = InvalidOrderException.class, timeout = 1000)
	public void createOrder_InvalidShippingProviderInfo_VENEX000011Caught() throws Exception {
		stub(item.getLogisticsInfo().getLogisticsProvider().getParty().getFullOrLegalName()).toReturn("Select Shipping");
		orderService.createOrder(order);
	}
	
	@After
	public void shutdown() {
		order = null;
		items = null;
		payments = null;
	}
	
	private XMLGregorianCalendar createOrderTimestamp(){
        GregorianCalendar gregory = new GregorianCalendar();
        gregory.setTime(new Date());
        XMLGregorianCalendar calendar = null;
        try {
                calendar = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(
                            gregory);
        } catch (DatatypeConfigurationException e) {
                e.printStackTrace();
        }
        
        return calendar;
	}
	*/	
}