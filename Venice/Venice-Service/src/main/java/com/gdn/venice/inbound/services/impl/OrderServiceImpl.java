package com.gdn.venice.inbound.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.integration.jaxb.Order;
import com.gdn.integration.jaxb.OrderItem;
import com.gdn.integration.jaxb.Payment;
import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VenOrderStatusConstants;
import com.gdn.venice.constants.VenPartyTypeConstants;
import com.gdn.venice.constants.VenWCSPaymentTypeConstants;
import com.gdn.venice.constants.VeniceExceptionConstants;
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
import com.gdn.venice.exception.CSOrderNotApprovedException;
import com.gdn.venice.exception.CannotPersistCustomerException;
import com.gdn.venice.exception.DuplicateWCSOrderIDException;
import com.gdn.venice.exception.InvalidOrderException;
import com.gdn.venice.exception.InvalidOrderItemException;
import com.gdn.venice.exception.InvalidOrderLogisticInfoException;
import com.gdn.venice.exception.OrderNotFoundException;
import com.gdn.venice.exception.VAOrderNotApprovedException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.factory.VeninboundFactory;
import com.gdn.venice.inbound.services.AddressService;
import com.gdn.venice.inbound.services.CustomerService;
import com.gdn.venice.inbound.services.MerchantService;
import com.gdn.venice.inbound.services.OrderAddressService;
import com.gdn.venice.inbound.services.OrderBlockingSourceService;
import com.gdn.venice.inbound.services.OrderContactDetailService;
import com.gdn.venice.inbound.services.OrderItemService;
import com.gdn.venice.inbound.services.OrderPaymentAllocationService;
import com.gdn.venice.inbound.services.OrderPaymentService;
import com.gdn.venice.inbound.services.OrderService;
import com.gdn.venice.inbound.services.OrderStatusHistoryService;
import com.gdn.venice.inbound.services.OrderStatusService;
import com.gdn.venice.inbound.services.PartyService;
import com.gdn.venice.persistence.FinApprovalStatus;
import com.gdn.venice.persistence.FinArFundsInActionApplied;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArReconResult;
import com.gdn.venice.persistence.LogLogisticsProvider;
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenAddressType;
import com.gdn.venice.persistence.VenContactDetail;
import com.gdn.venice.persistence.VenCustomer;
import com.gdn.venice.persistence.VenFraudCheckStatus;
import com.gdn.venice.persistence.VenMerchant;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderAddress;
import com.gdn.venice.persistence.VenOrderBlockingSource;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderPayment;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.persistence.VenOrderPaymentAllocationPK;
import com.gdn.venice.persistence.VenOrderStatus;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPartyAddress;
import com.gdn.venice.persistence.VenPartyAddressPK;
import com.gdn.venice.persistence.VenPartyType;
import com.gdn.venice.persistence.VenProductCategory;
import com.gdn.venice.util.CommonUtil;
import com.gdn.venice.util.OrderUtil;
import com.gdn.venice.util.VeniceConstants;
import com.gdn.venice.validator.factory.OrderCreationValidatorFactory;
import com.rits.cloning.Cloner;

/**
 * 
 * @author yauritux
 * 
 */
@Service
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
	
	@PersistenceContext
	private EntityManager em;
	
	/*
	@Autowired
	OrderReceiver orderReceiver;
	@Autowired
	Command createOrderCmd;
	*/
	@Autowired
	VenAddressDAO venAddressDAO;
	@Autowired
	VenCountryDAO venCountryDAO;
	@Autowired
	VenPaymentStatusDAO venPaymentStatusDAO;
	@Autowired
	VenStateDAO venStateDAO;
	@Autowired	
	VenPaymentTypeDAO venPaymentTypeDAO;
	@Autowired
	VenPartyTypeDAO venPartyTypeDAO;
	@Autowired
	VenContactDetailTypeDAO venContactDetailTypeDAO;
	@Autowired
	VenOrderStatusDAO venOrderStatusDAO;
	@Autowired
	VenFraudCheckStatusDAO venFraudCheckStatusDAO;
	@Autowired
	LogLogisticServiceDAO logLogisticServiceDAO;
	@Autowired
	LogLogisticsProviderDAO logLogisticProviderDAO;
	@Autowired
	VenWcsPaymentTypeDAO venWcsPaymentTypeDAO;
	@Autowired
	VenProductCategoryDAO venProductCategoryDAO;
	@Autowired
	VenProductTypeDAO venProductTypeDAO;
	@Autowired
	VenBankDAO venBankDAO;
	@Autowired
	FinArFundsInReconRecordDAO finArFundsInReconRecordDAO;
	@Autowired
	VenOrderAddressDAO venOrderAddressDAO;
	@Autowired
	VenMerchantDAO venMerchantDAO;
	@Autowired
	VenOrderBlockingSourceDAO venOrderBlockingSourceDAO;
	@Autowired
	VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	@Autowired
	VenPartyDAO venPartyDAO;
	@Autowired
	VenPromotionDAO venPromotionDAO;
	@Autowired
	VenOrderContactDetailDAO venOrderContactDetailDAO;
	@Autowired
	VenContactDetailDAO venContactDetailDAO;	
	@Autowired
	VenCustomerDAO venCustomerDAO;
	@Autowired
	VenAddressTypeDAO venAddressTypeDAO;
	@Autowired
	VenOrderDAO venOrderDAO;
	@Autowired	
	VenOrderItemDAO venOrderItemDAO;
	@Autowired
	VenMerchantProductDAO venMerchantProductDAO;
	@Autowired
	VenOrderItemAddressDAO venOrderItemAddressDAO;
	@Autowired
	VenOrderItemAdjustmentDAO venOrderItemAdjustmentDAO;
	@Autowired
	VenOrderItemContactDetailDAO venOrderItemContactDetailDAO;
	@Autowired
	VenOrderPaymentDAO venOrderPaymentDAO;
	@Autowired
	VenOrderStatusHistoryDAO venOrderStatusHistoryDAO;
	@Autowired
	VenOrderItemStatusHistoryDAO venOrderItemStatusHistoryDAO;
	@Autowired
	VenPartyAddressDAO venPartyAddressDAO;
	@Autowired
	VenRecipientDAO venRecipientDAO;
	@Autowired
	VenCityDAO venCityDAO;
	
	@Autowired
	private AddressService addressService;
	
	@Autowired
	private OrderAddressService orderAddressService;
	
	@Autowired
	private OrderBlockingSourceService orderBlockingSourceService;
	
	@Autowired
	private OrderContactDetailService orderContactDetailService;
	
    @Autowired
    private OrderItemService orderItemService;	
    
    @Autowired 
    private OrderPaymentService orderPaymentService;
    
    @Autowired
    private OrderPaymentAllocationService orderPaymentAllocationService;
    
    @Autowired
    private OrderStatusService orderStatusService;
    
    @Autowired
    private OrderStatusHistoryService orderStatusHistoryService;
    
    @Autowired
    private MerchantService merchantService;
    
    @Autowired
    private PartyService partyService;
    
    @Autowired
    private CustomerService customerService;
	
	private Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();	
	private Cloner cloner = new Cloner();
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public Boolean createOrder(Order order) throws VeniceInternalException {
		OrderUtil.checkOrder(order, VeninboundFactory.getOrderValidator(new OrderCreationValidatorFactory()));
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "OrderServiceImpl::createOrder::all basic validation passed");
				
		//OrderReceiver orderReceiver = new OrderReceiverImpl(order);
		//Command createOrderCmd = new CreateOrderCommand(orderReceiver);
		//createOrderCmd.execute();
		
		/*
		 * Check that none of the order items already exist and remove any party
		 * record from merchant to prevent data problems from WCS
		 */
		List <String> merchantProduct =  new ArrayList<String>();
	
		for (OrderItem item : order.getOrderItems()) {
			
			if (orderItemService.isItemWCSExistInDB(item.getItemId().getCode())) {
				throw CommonUtil.logAndReturnException(new InvalidOrderItemException(
						"createOrder:message received with an order item that already exists in the database:" 
			                       + item.getItemId().getCode()
						, VeniceExceptionConstants.VEN_EX_000012)
				, CommonUtil.getLogger(this.getClass().getCanonicalName())
				, LoggerLevel.ERROR);
			}
			
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "merchant party = " + item.getProduct().getMerchant().getParty());
			// Remove party from merchant
			if (item.getProduct().getMerchant().getParty() != null) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "createOrder::merchant party is not NULL, going to remove it...");
				String merchantSKU = item.getProduct().getMerchant().getMerchantId().getCode()
						+ "&" 
						+(item.getProduct().getMerchant().getParty().getFullOrLegalName()!=null
						   ? item.getProduct().getMerchant().getParty().getFullOrLegalName():"");
				merchantProduct.add(merchantSKU);
				item.getProduct().getMerchant().setParty(null);
			}
		}				
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "createOrder::checking payment type");
		
		boolean vaPaymentExists = false;
		boolean csPaymentExists = false;
		
		Payment payment = (order.getPayments() != null && order.getPayments().size() > 0)
				? order.getPayments().get(0) : null;
				
		try {
			//if (orderPaymentService.isPaymentExist(payment)) {
			if (orderPaymentService.isPaymentApproved(payment)) {
				if (orderPaymentService.isVirtualAccountPayment(payment.getPaymentType())) {
					vaPaymentExists = true;
				} else if (orderPaymentService.isCSPayment(payment.getPaymentType())) {
					csPaymentExists = true;
				} else { //neither VA nor CS payment type, validate whether order id has already existed or not
					if (this.isOrderExist(order.getOrderId().getCode())) {
						throw CommonUtil.logAndReturnException(
								new InvalidOrderException(
										"createOrder:An order with this WCS orderId already exists: "
										           + order.getOrderId().getCode(), VeniceExceptionConstants.VEN_EX_000017)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
					}					
				}
			} else {
				if (orderPaymentService.isVirtualAccountPayment(payment.getPaymentType())) {
					throw CommonUtil.logAndReturnException(
							new VAOrderNotApprovedException(
									"createOrder::VA Order has not been approved yet"
									, VeniceExceptionConstants.VEN_EX_000014)
							, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				} else if (orderPaymentService.isCSPayment(payment.getPaymentType())) {
					throw CommonUtil.logAndReturnException(
							new CSOrderNotApprovedException(
									"createOrder::CS Order has not been approved yet"
									, VeniceExceptionConstants.VEN_EX_000016)
							, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				}
			}
		} catch (VeniceInternalException vie) {
			throw vie;
		}
		
		/*
		 * This is really important. We need to get the order from the DB if
		 * it is VA because it will exist and we must update it with all of
		 * the details.
		 * 
		 * There MUST be NO changes to the VA payment data because it MUST
		 * be what was sent by Venice to WCS originally. Therefore if the
		 * payment information is included then we must not update it.
		 */
		
		VenOrder venOrder = new VenOrder();
		// If there is a VA payment then get the order from the cache
		if (vaPaymentExists || csPaymentExists) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "va/cs payment exist, retrieve existing order");
			venOrder = retrieveExistingOrder(order.getOrderId().getCode());
			// If there is no existing VA status order then throw an exception
			if (venOrder == null) {
				String errMsg = "message received for an order with VA payments where there is no existing VA status order:" 
			                   + order.getOrderId().getCode();
				throw CommonUtil.logAndReturnException(new InvalidOrderException(errMsg, VeniceExceptionConstants.VEN_EX_000013)
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}

			//If the status of the existing order is not VA then it is a duplicate.
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "checking order status");
			if (venOrder.getVenOrderStatus().getOrderStatusId() != VenOrderStatusConstants.VEN_ORDER_STATUS_VA.code() 
					&& venOrder.getVenOrderStatus().getOrderStatusId() != VenOrderStatusConstants.VEN_ORDER_STATUS_CS.code()) {
				String errMsg = "message received with  the status of the existing order is not VA/CS (duplicate wcs order id):" + venOrder.getWcsOrderId();
				throw CommonUtil.logAndReturnException(new DuplicateWCSOrderIDException(errMsg, VeniceExceptionConstants.VEN_EX_000019)
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		} else { //neither VA nor CS payment type
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "checking wcs order for non-VA/CS payment");
			if (isOrderExist(order.getOrderId().getCode())) {
				String errMsg = "message received where an order with WCS orderId already exists:" + venOrder.getWcsOrderId();
				throw CommonUtil.logAndReturnException(new InvalidOrderException(errMsg, VeniceExceptionConstants.VEN_EX_000017)
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "set status to C");
		//Set the status of the order explicitly to C (workaround for OpenJPA 2.0 problem)
		VenOrderStatus venOrderStatusC = new VenOrderStatus();
		venOrderStatusC.setOrderStatusId(VeniceConstants.VEN_ORDER_STATUS_C);
		venOrderStatusC.setOrderStatusCode("C");
		venOrder.setVenOrderStatus(venOrderStatusC);

		//Map the jaxb Order object to a JPA VenOrder object. This will be
		//ok for both persist and merge because the PK is not touched and
		//everything must be added anyway (VA payment will only have an
		//orderId and timestamp).
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "createOrder::Mapping the order object to the venOrder object...");
		mapper.map(order, venOrder);
					
		//Party for merchant
		
		List<VenOrderItem> orderItems = venOrder.getVenOrderItems();
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "createOrder::orderItems member = " + orderItems.size());
		
		// Set the defaults for all of the boolean values in venOrder
		if (venOrder.getBlockedFlag() == null) venOrder.setBlockedFlag(false);
		if (venOrder.getRmaFlag() == null) venOrder.setRmaFlag(false);
		// Default the finance reconcile flag to false.
		venOrder.setFinanceReconcileFlag(false);

		// If the order amount is missing then set it to default to 0
		if (venOrder.getAmount() == null) venOrder.setAmount(new BigDecimal(0));
		
		//This method call will persist the order if there has been no VA payment else it will merge
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::persisting order");
		
		venOrder = persistOrder(vaPaymentExists, csPaymentExists, venOrder);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::Persisted VenOrder = " + venOrder);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::done persist order");
		
		Pattern pattern = Pattern.compile("&");
		for(String party : merchantProduct){				
			String[] temp = pattern.split(party, 0);
				
			CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "createOrder::show venParty in orderItem :  "+party);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "createOrder::string merchant :  "+temp[0]+" and "+temp[1]);
				
			if((temp[1] != null) && (!temp[1].trim().equals(""))){
				CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "createOrder::temp[1] not empty");
				CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "createOrder::orderItems = " + orderItems);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "createOrder::orderItems size = " + (orderItems != null ? orderItems.size() : 0));
				for(int h =0; h < orderItems.size(); h++){
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "createOrder::h=" + h + ",orderItems=" + orderItems.get(h));
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "createOrder::venMerchantProduct = " + orderItems.get(h).getVenMerchantProduct());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "createOrder::venMerchant = " 
					+ orderItems.get(h).getVenMerchantProduct().getVenMerchant());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "createOrder::wcsMerchantId = " 
							+ orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId());
					if(orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId().equals(temp[0].trim())){
						//List<VenMerchant> venMerchantList = venMerchantDAO.findByWcsMerchantId(temp[0]);
						List<VenMerchant> venMerchantList = merchantService.findByWcsMerchantId(temp[0]);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "createOrder::venMerchantList found = " + venMerchantList);
						if (venMerchantList != null && venMerchantList.size() > 0) {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "createOrder::venMerchantList size = " + venMerchantList.size());
							if (venMerchantList.get(0).getVenParty() == null) {
								//List<VenParty> venPartyList = venPartyDAO.findByLegalName(temp[1].trim());
								List<VenParty> venPartyList = partyService.findByLegalName(temp[1]);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "createOrder::venPartyList found = " + venPartyList);
								    if (venPartyList == null || venPartyList.size() == 0) { 
								    	CommonUtil.logDebug(this.getClass().getCanonicalName()
								    			, "createOrder::venPartyList is empty, creating new one");
										VenParty venPartyitem = new VenParty();
										VenPartyType venPartyType = new VenPartyType();
										// set party type id = 1 adalah merchant
										//venPartyType.setPartyTypeId(new Long(1));
										venPartyType.setPartyTypeId(VenPartyTypeConstants.VEN_PARTY_TYPE_MERCHANT.code());
										venPartyitem.setVenPartyType(venPartyType);
										venPartyitem.setFullOrLegalName(temp[1]);	
										CommonUtil.logDebug(this.getClass().getCanonicalName()
												, "createOrder::persist venParty :  "+venPartyitem.getFullOrLegalName());
										venPartyitem = venPartyDAO.save(venPartyitem);
										venMerchantList.get(0).setVenParty(venPartyitem);
										//venMerchantHome.mergeVenMerchant(venMechantList.get(0));
										//venMerchantDAO.save(venMerchantListCloned.get(0));
										VenMerchant venMerchant = venMerchantList.get(0);
										merchantService.persist(venMerchant);
										CommonUtil.logDebug(this.getClass().getCanonicalName()
												, "createOrder::added  new party for venmerchant (Merchant Id :"
										          + orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId() +" )"
										);			
									}else{
										venMerchantList.get(0).setVenParty(venPartyList.get(0));
										VenMerchant venMerchant = venMerchantList.get(0);
										merchantService.persist(venMerchant);
										CommonUtil.logDebug(this.getClass().getCanonicalName()
												, "createOrder::add  party for venmerchant (Merchant Id :"
										          + orderItems.get(h).getVenMerchantProduct().getVenMerchant().getWcsMerchantId() +" )"
										);						
									}
								}
							}
					}
					
				}
				}else {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "createOrder::party is null for venmerchant (Merchant Id :"+ temp[0] +" )");
				}
					
			} //EOF for

		// If the order is RMA do nothing with payments because there are none
		if (!venOrder.getRmaFlag()) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "createOrder::rma flag false, remove existing payment");
			// Remove any existing order payment allocations that were allocated at VA stage
			orderPaymentAllocationService.removeOrderPaymentAllocationList(venOrder);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "createOrder::done remove existing payment");
			// An array list of order payment allocations
			List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = new ArrayList<VenOrderPaymentAllocation>();
			List<VenOrderPayment> venOrderPaymentList = new ArrayList<VenOrderPayment>();

			//Allocate the payments to the order.
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::Allocate the payments to the order");				
			if (order.getPayments() != null && order.getPayments().size() > 0) {
				Iterator<?> paymentIterator = order.getPayments().iterator();
				while (paymentIterator.hasNext()) {
					Payment next = (Payment) paymentIterator.next();
					//Ignore partial fulfillment payments ... looks like a work around in WCS ... no need for this in Venice
					if (!next.getPaymentType().equals(VenWCSPaymentTypeConstants.VEN_WCS_PAYMENT_TYPE_PartialFulfillment.desc())) {
						VenOrderPayment venOrderPayment = new VenOrderPayment();
	
						//If the payment already exists then just fish it
						//from the DB. This is the case for VA payments as
						//they are received before the confirmed order.
						List<VenOrderPayment> venOrderPaymentList2 = orderPaymentService.findByWcsPaymentId(next.getPaymentId().getCode());
						
						if (venOrderPaymentList2 != null && (venOrderPaymentList2.size() > 0)) {
							venOrderPayment = venOrderPaymentList2.get(0);
						}
						// Map the payment with dozer
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "createOrder::Mapping the VenOrderPayment object...");
						mapper.map(next, venOrderPayment);

						// Set the payment type based on the WCS payment type
						// VenPaymentType venPaymentType = new VenPaymentType();
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::mapping payment type");
						
						venOrderPayment = OrderUtil.getVenOrderPaymentByWCSPaymentType(venOrderPayment, next);
						venOrderPaymentList.add(venOrderPayment);
					}
				}					
				
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::persist payment");
				venOrderPaymentList = orderPaymentService.persistOrderPaymentList(venOrderPaymentList);

				paymentIterator = venOrderPaymentList.iterator();
				BigDecimal paymentBalance = venOrder.getAmount();
				int p=0;
				while (paymentIterator.hasNext()) {
					VenOrderPayment next = (VenOrderPayment) paymentIterator.next();

					//Only include the allocations for non-VA payments
					//because VA payments are already in the DB
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::allocate payment");
					
					//semua Payment di allocate, untuk payment VA dan non-VA.
			
					//if (!next.getVenPaymentType().getPaymentTypeCode().equals(VEN_PAYMENT_TYPE_VA)) {
						// Build the allocation list manually based on the payment
						VenOrderPaymentAllocation allocation = new VenOrderPaymentAllocation();
						allocation.setVenOrder(venOrder);
						BigDecimal paymentAmount = next.getAmount();
						
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "createOrder::Order Amount = "+paymentBalance);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "createOrder::paymentBalance.compareTo(new BigDecimal(0)):  "
						+paymentBalance.compareTo(new BigDecimal(0)) );
						
						// If the balance is greater than zero
						if (paymentBalance.compareTo(new BigDecimal(0)) >= 0) {
						
							//If the payment amount is greater than the
							//balance then allocate the balance amount else
							//allocate the payment amount.
							if (paymentBalance.compareTo(paymentAmount) < 0) {
								allocation.setAllocationAmount(paymentBalance);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "createOrder::Order Allocation Amount is paymentBalance = "+paymentBalance);
							} else {
								allocation.setAllocationAmount(paymentAmount);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "createOrder::Order Allocation Amount is paymentAmount = "+paymentAmount);
							}
							
							paymentBalance = paymentBalance.subtract(paymentAmount);
							allocation.setVenOrderPayment(next);

							// Need a primary key object...
							VenOrderPaymentAllocationPK venOrderPaymentAllocationPK = new VenOrderPaymentAllocationPK();
							venOrderPaymentAllocationPK.setOrderPaymentId(next.getOrderPaymentId());
							venOrderPaymentAllocationPK.setOrderId(venOrder.getOrderId());
							allocation.setId(venOrderPaymentAllocationPK);

							venOrderPaymentAllocationList.add(allocation);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "createOrder::venOrder Payment Allocation List size from looping ke-"
							   + p +" = "+venOrderPaymentAllocationList.size());
							p++;
						}
					//}
				}
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "createOrder::persist payment allocation");
				
				venOrderPaymentAllocationList = orderPaymentAllocationService.persistOrderPaymentAllocationList(venOrderPaymentAllocationList);
				venOrder.setVenOrderPaymentAllocations(venOrderPaymentAllocationList);
				
				//Here we need to create a dummy reconciliation records
				//for the non-VA payments so that they appear in the 
				//reconciliation screen as unreconciled.
				//Later these records will be updated when the funds in
				//reports are processed 					
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::create reconciliation record");
				for (VenOrderPayment orderPayment : venOrderPaymentList) {
					//Only insert reconciliation records for non-VA payments here
					//because the VA records will have been inserted when a VA payment is received.
					if (orderPayment.getVenPaymentType().getPaymentTypeId() != VeniceConstants.VEN_PAYMENT_TYPE_ID_VA 
							&& orderPayment.getVenPaymentType().getPaymentTypeId() != VeniceConstants.VEN_PAYMENT_TYPE_ID_CS) {
						FinArFundsInReconRecord reconRecord = new FinArFundsInReconRecord();

						FinArReconResult result = new FinArReconResult();
						result.setReconResultId(VeniceConstants.FIN_AR_RECON_RESULT_NONE);
						reconRecord.setFinArReconResult(result);
						
						FinArFundsInActionApplied actionApplied = new FinArFundsInActionApplied();
						actionApplied.setActionAppliedId(VeniceConstants.FIN_AR_FUNDS_IN_ACTION_APPLIED_NONE);
						reconRecord.setFinArFundsInActionApplied(actionApplied);

						FinApprovalStatus approvalStatus = new FinApprovalStatus();
						approvalStatus.setApprovalStatusId(VeniceConstants.FIN_APPROVAL_STATUS_NEW);
						reconRecord.setFinApprovalStatus(approvalStatus);
						
						reconRecord.setVenOrderPayment(orderPayment);
						reconRecord.setWcsOrderId(venOrder.getWcsOrderId());
						reconRecord.setOrderDate(venOrder.getOrderDate());
						reconRecord.setPaymentAmount(orderPayment.getAmount());
						reconRecord.setNomorReff(payment.getReferenceId()!=null?payment.getReferenceId():"");

						// balance per payment amount - handling fee = payment amount, jadi bukan amount order total keseluruhan
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "createOrder::payment Amount  = "+payment.getAmount());
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "createOrder::HandlingFee = "+payment.getHandlingFee());
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "createOrder::setRemainingBalanceAmount = " + orderPayment.getAmount().subtract(orderPayment.getHandlingFee()));
						
						reconRecord.setRemainingBalanceAmount(orderPayment.getAmount());
						reconRecord.setUserLogonName("System");
						finArFundsInReconRecordDAO.save(reconRecord);
						//reconRecordHome.persistFinArFundsInReconRecord(reconRecord);
					}
				}
			}			
		}

		
		//LOG.debug("\n done create order!");
		//Long endTime = System.currentTimeMillis();
		//Long duration = endTime - startTime;
		//LOG.debug("createOrder: persisted new venOrder.orderId:"
				//+ venOrder.getOrderId() + " status:"
				//+ venOrder.getVenOrderStatus().getOrderStatusCode()
				//+ " in:" + duration + "ms");				
		
		return Boolean.TRUE;
	}
	
	/**
	 * Synchronizes the data for the direct VenOrderPayment references
	 * 
	 * @param venOrderPayment
	 * @return the synchronized data object
	 */
	/*
	@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
	private VenOrderPayment synchronizeVenOrderPaymentReferenceData(
			VenOrderPayment venOrderPayment) throws VeniceInternalException {

		List<Object> references = new ArrayList<Object>();
		references.add(venOrderPayment.getVenBank());
		references.add(venOrderPayment.getVenPaymentStatus());
		references.add(venOrderPayment.getVenPaymentType());
		references.add(venOrderPayment.getVenAddress());
		references.add(venOrderPayment.getVenWcsPaymentType());
		references.add(venOrderPayment.getOldVenOrder());

		// Synchronize the data references
		references = this.synchronizeReferenceData(references);

		// Push the keys back into the record
		Iterator<Object> referencesIterator = references.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenBank) {
				venOrderPayment.setVenBank((VenBank) next);
			} else if (next instanceof VenPaymentStatus) {
				venOrderPayment.setVenPaymentStatus((VenPaymentStatus) next);
			} else if (next instanceof VenPaymentType) {
				venOrderPayment.setVenPaymentType((VenPaymentType) next);
			} else if (next instanceof VenAddress) {
				venOrderPayment.setVenAddress((VenAddress) next);
			} else if (next instanceof VenWcsPaymentType) {
				venOrderPayment.setVenWcsPaymentType((VenWcsPaymentType) next);
			} else if (next instanceof VenOrder) {
				venOrderPayment.setOldVenOrder((VenOrder) next);
			}
		}
		return venOrderPayment;
	}
	*/	
	
	/**
	 * Persists a list of order payments using the session tier.
	 * 
	 * @param orderPayments
	 * @return the persisted object
	 */
	/*
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private List<VenOrderPayment> persistOrderPaymentList(List<VenOrderPayment> venOrderPaymentList) throws VeniceInternalException {
		List<VenOrderPayment> newVenOrderPaymentList = new ArrayList<VenOrderPayment>();
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::persistOrderPaymentList::BEGIN,venOrderPaymentList=" + venOrderPaymentList);
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::persistOrderPaymentList::venOrderPaymentList size = " + venOrderPaymentList.size());
		if (venOrderPaymentList != null && (venOrderPaymentList.size() > 0)) {
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::persistOrderPaymentList::Persisting VenOrderPayment list...:"+ venOrderPaymentList.size());
				Iterator<VenOrderPayment> i = venOrderPaymentList.iterator();
				while (i.hasNext()) {
					VenOrderPayment next = i.next();
					
					// Detach the allocations before persisting
					List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = (List<VenOrderPaymentAllocation>)next.getVenOrderPaymentAllocations();
					//List<VenOrderPaymentAllocation> venOrderPaymentAllocationListCloned = cloner.deepClone(venOrderPaymentAllocationList);
					//em.detach(venOrderPaymentAllocationList);
					
					next.setVenOrderPaymentAllocations(null);

					// Synchronize the references
					next = this.synchronizeVenOrderPaymentReferenceData(next);

					// Persist the billing address
					next.setVenAddress(this.persistAddress(next.getVenAddress()));
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::persistOrderPaymentList::venaddress has successfully persisted");

					//Check to see if the payment is already in the cache and
					//if it is then assume it is a VA payment and should not be
					//changed because it was APPROVED by Venice
					
					//List<VenOrderPayment> paymentList = paymentHome.queryByRange("select o from VenOrderPayment o where o.wcsPaymentId = '" + next.getWcsPaymentId() + "'", 0, 1);
					List<VenOrderPayment> paymentList = venOrderPaymentDAO.findByWcsPaymentId(next.getWcsPaymentId());
					//em.detach(paymentList);
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::persistOrderPaymentList::paymentList found : " + paymentList.size());

					if (paymentList.isEmpty()) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderServiceImpl::persistOrderPaymentList::Payment not found so persisting it...");
						// Persist the object
						//newVenOrderPaymentList.add((VenOrderPayment) paymentHome.persistVenOrderPayment(next));
						VenOrderPayment venOrderPaymentPersisted = venOrderPaymentDAO.save(next);
						//em.detach(venOrderPaymentPersisted);
						//newVenOrderPaymentList.add(venOrderPaymentDAO.save(next));
						newVenOrderPaymentList.add(venOrderPaymentPersisted);
						// Persist the allocations
						//List<VenOrderPaymentAllocation> venOrderPaymentAllocationPersisted = this.persistOrderPaymentAllocationList(venOrderPaymentAllocationListCloned);
						List<VenOrderPaymentAllocation> venOrderPaymentAllocationPersisted = this.persistOrderPaymentAllocationList(venOrderPaymentAllocationList);
						//List<VenOrderPaymentAllocation> venOrderPaymentAllocationPersistedCloned = cloner.deepClone(venOrderPaymentAllocationPersisted);
						//em.detach(venOrderPaymentAllocationPersisted);
						//next.setVenOrderPaymentAllocations(this.persistOrderPaymentAllocationList(venOrderPaymentAllocationList));
						//next.setVenOrderPaymentAllocations(venOrderPaymentAllocationPersistedCloned);
						next.setVenOrderPaymentAllocations(venOrderPaymentAllocationPersisted);
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderServiceImpl::persistOrderPaymentList::persist the allocations");
						// Persist the allocations
						List<VenOrderPaymentAllocation> venOrderPaymentAllocationPersisted = this.persistOrderPaymentAllocationList(venOrderPaymentAllocationList);
						//List<VenOrderPaymentAllocation> venOrderPaymentAllocationPersistedCloned = cloner.deepClone(venOrderPaymentAllocationPersisted);
						//em.detach(venOrderPaymentAllocationPersisted);
						next.setVenOrderPaymentAllocations(venOrderPaymentAllocationPersisted);
						// Just put it back into the new list
						newVenOrderPaymentList.add(next);
					}
				}
			} catch (Exception e) {
				String errMsg = "An exception occured when persisting VenOrderItem:";
				throw CommonUtil.logAndReturnException(new VeniceInternalException(errMsg, e)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
			List<VenOrderPayment> newVenOrderPaymentListCloned = cloner.deepClone(newVenOrderPaymentList);
			return newVenOrderPaymentListCloned;
		}
		return venOrderPaymentList;
	}
	*/
	
	/**
	 * Persists the payment allocation list to the cache
	 * 
	 * @param venOrderPaymentAllocationList
	 * @return
	 */
	/*
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private List<VenOrderPaymentAllocation> persistOrderPaymentAllocationList(
			List<VenOrderPaymentAllocation> venOrderPaymentAllocationList) throws VeniceInternalException {
		List<VenOrderPaymentAllocation> newVenOrderPaymentAllocationList = new ArrayList<VenOrderPaymentAllocation>();
		//em.detach(newVenOrderPaymentAllocationList);
		if (venOrderPaymentAllocationList != null	&& venOrderPaymentAllocationList.size() > 0) {
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::persistOrderPaymentAllocationList::Persisting VenOrderPaymentAllocation list...:"
				+ venOrderPaymentAllocationList.size());
				Iterator<VenOrderPaymentAllocation> i = venOrderPaymentAllocationList.iterator();
				while (i.hasNext()) {
					VenOrderPaymentAllocation next = i.next();
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::persistOrderPaymentAllocationList::value of paymentAllocation ......: order_id = "
					   + next.getVenOrder().getOrderId() +" and wcs_code_payment = "
					   + next.getVenOrderPayment().getWcsPaymentId());
					// Persist the object
					newVenOrderPaymentAllocationList.add(venOrderPaymentAllocationDAO.save(next));
				}
			} catch (Exception e) {
				String errMsg = "An exception occured when persisting VenOrderPaymentAllocation:";
				e.printStackTrace();
				throw CommonUtil.logAndReturnException(new CannotPersistOrderPaymentException(errMsg, VeniceExceptionConstants.VEN_EX_000023)
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}else{
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "OrderServiceImpl::persistOrderPaymentAllocationList::Persisting VenOrderPaymentAllocation list is null");
		}
		return newVenOrderPaymentAllocationList;
	}	
	*/	
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrder persistOrder(Boolean vaPaymentExists, Boolean csPaymentExists, VenOrder venOrder) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistOrder::vaPaymentExists: "+vaPaymentExists);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistOrder::csPaymentExists: "+csPaymentExists);
		if (venOrder != null) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::Persisting VenOrder... :" + venOrder.getWcsOrderId());				
				
				// Save the order items before persisting as it will be detached
				List<VenOrderItem> venOrderItemList = venOrder.getVenOrderItems();
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::venOrderItem merchantProduct orderItems="
						+ venOrderItemList.get(0).getVenMerchantProduct().getVenOrderItems());

				// Detach the order items prior to persisting the order.
				venOrder.setVenOrderItems(null);
				
				// Detach the order payment allocations
				// Note that these will be allocated at 100% of the order price later when processing payments
				venOrder.setVenOrderPaymentAllocations(null);

				// Detach the transaction fees list
				venOrder.setVenTransactionFees(null);
				// Detach the customer first then persist and re-attach
				VenCustomer customer = venOrder.getVenCustomer();
				venOrder.setVenCustomer(null);

				if(venOrder.getVenOrderBlockingSource().getBlockingSourceId()== null 
						&& venOrder.getVenOrderBlockingSource().getBlockingSourceDesc()==null)
					venOrder.setVenOrderBlockingSource(null);
									
				// Persist the customer
				
				try {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::trying to persist venCustomer");
					VenCustomer persistedCustomer = customerService.persistCustomer(customer);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::venCustomer persisted");					
					venOrder.setVenCustomer(persistedCustomer);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::customer has been set to venOrder");
				} catch (Exception e) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "cannot persist customer!! Error = " + e);
					throw CommonUtil.logAndReturnException(
							new CannotPersistCustomerException("An exception occured when trying to persist Customer"
									, VeniceExceptionConstants.VEN_EX_100001)
							, CommonUtil.getLogger(this.getClass().getCanonicalName())
							, LoggerLevel.ERROR);
				}
				
				VenAddress orderAddress = new VenAddress();
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::trying to persistAddress");
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::customer : " + venOrder.getVenCustomer());
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::party : " + venOrder.getVenCustomer().getVenParty());
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::venPartyAddresses = " + venOrder.getVenCustomer().getVenParty().getVenPartyAddresses());
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::venAddress = " + venOrder.getVenCustomer().getVenParty().getVenPartyAddresses().get(0)
						.getVenAddress());
				orderAddress = addressService.persistAddress(venOrder.getVenCustomer()
						.getVenParty().getVenPartyAddresses().get(0).getVenAddress());
				
				// Synchronize the reference data
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::synchronizing order reference data");
				try {
					venOrder = synchronizeVenOrderReferenceData(venOrder);
				} catch (VeniceInternalException e) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::Exception = " + e);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::Message = " + e.getMessage());
				}
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::order reference data is synchronized now");
				
				// If a VA payment exists then merge else persist the order
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::going to persist the VenOrder (" + venOrder + ")");
				if (vaPaymentExists || csPaymentExists) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::either vaPayment or csPayment is exist, do merging operation");
					venOrder = venOrderDAO.save(venOrder);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::venOrder merged");
				} else {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::both vaPayment and csPayment not exist, do persisting operation");
					venOrder = venOrderDAO.save(venOrder);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::venOrder persisted");
				}
				//add order status history
				orderStatusHistoryService.createOrderStatusHistory(venOrder, venOrder.getVenOrderStatus());
				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::creating orderStatusHistory");
				
				//Persist the order items regardless of if it is VA or not
				//because if there has been a VA payment then the items will
				//not be in the cache anyway.
				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::venOrder.wcsOrderId: "+venOrder.getWcsOrderId());
				
				try {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::trying to persistOrderItemList");
					List<VenOrderItem> venOrderItemListPersisted = orderItemService.persistOrderItemList(venOrder, venOrderItemList);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::trying to assign venOrderItemListPersisted into venOrder");
					venOrder.setVenOrderItems(venOrderItemListPersisted);
				    CommonUtil.logDebug(this.getClass().getCanonicalName()
				    		, "persistOrder::successfully assigned venOrderItemListPersisted into venOrder");
				} catch (Exception e) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::an exception occured when trying assign venOrderItems into venOrder: "
							+ e);
				}
				
				//Tally Order with customer address and contact details
				//defined in the ref tables VenOrderAddress and VenOrderContactDetail
				
				if(orderAddress!=null){
					VenOrderAddress venOrderAddress = new VenOrderAddress();
					venOrderAddress.setVenOrder(venOrder);
					venOrderAddress.setVenAddress(orderAddress);
						
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::Persisting VenOrderAddress");
					// persist VenOrderAddress
					orderAddressService.persist(venOrderAddress);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::venOrderAddress is persisted");
				}else{
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::customer address is null");
				}
				
				List<VenOrderContactDetail> venOrderContactDetailList = new ArrayList<VenOrderContactDetail>();
				
				List<VenContactDetail> venContactDetailList = venOrder.getVenCustomer().getVenParty().getVenContactDetails();
				if(venContactDetailList != null){				
					for (VenContactDetail venContactDetail:venContactDetailList){
						VenOrderContactDetail venOrderContactDetail = new VenOrderContactDetail();
						venOrderContactDetail.setVenOrder(venOrder);
						venOrderContactDetail.setVenContactDetail(venContactDetail);
						
						venOrderContactDetailList.add(venOrderContactDetail);
					}
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::Total VenOrderContactDetail to be persisted => " 
					+ venOrderContactDetailList.size());
					// persist VenOrderContactDetail
					//venOrderContactDetailDAO.save(venOrderContactDetailList);
					orderContactDetailService.persistVenOrderContactDetails(venOrderContactDetailList);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::venOrderContactDetails successfully persisted");
				}			
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistOrder::EOF, returning venOrder = " + venOrder);
		return venOrder;
	}	
	
	/**
	 * Synchronizes the reference data for the direct VenOrder references
	 * 
	 * @param venOrder
	 * @return the synchronized data object
	 */
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public VenOrder synchronizeVenOrderReferenceData(VenOrder venOrder) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenOrderReferenceData::START, venOrder=" + venOrder);
		/*
		List<Object> references = new ArrayList<Object>();
		references.add(venOrder.getVenOrderBlockingSource());
		references.add(venOrder.getVenOrderStatus());
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::synchronizeVenOrderReferenceData::references size = " + references.size());
		*/
		if (venOrder.getVenOrderBlockingSource() != null) {
			List<VenOrderBlockingSource> orderBlockingSourceReferences = new ArrayList<VenOrderBlockingSource>();
			orderBlockingSourceReferences.add(venOrder.getVenOrderBlockingSource());
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenOrderReferenceData::going to synchronize VenOrderBlockingSource");
			// Synchronize the data references
			orderBlockingSourceReferences = orderBlockingSourceService
					.synchronizeVenOrderBlockingSourceReferences(orderBlockingSourceReferences);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenOrderRefereneData::orderBlockingSourceReferences has been synchronized : " 
			          + orderBlockingSourceReferences);			
			for (VenOrderBlockingSource orderBlockingSource : orderBlockingSourceReferences) { //do we need to do this inside loop ???
				venOrder.setVenOrderBlockingSource(orderBlockingSource);
			}			
		}
		
		if (venOrder.getVenOrderStatus() != null) {
			List<VenOrderStatus> orderStatusReferences = new ArrayList<VenOrderStatus>();
			orderStatusReferences.add(venOrder.getVenOrderStatus());

			orderStatusReferences = orderStatusService.synchronizeVenOrderStatusReferences(orderStatusReferences);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenOrderReferenceData::orderStatusReferences has been synchronized : " 
			          + orderStatusReferences);
			for (VenOrderStatus orderStatus : orderStatusReferences) { // weird isn't it ?
				venOrder.setVenOrderStatus(orderStatus);
			}			
		}
		/*
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderReferenceData::calling synchronizedReferenceData");
		List<Object> synchronizedReferences = synchronizeReferenceData(references);
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::synchronizeVenOrderReferenceData::synchronizeReferenceData successfully processed");

		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::synchronizeVenOrderReferenceData::synchronizedReferences size = " 
				+ synchronizedReferences.size());
		// Push the keys back into the order record
		Iterator<Object> referencesIterator = synchronizedReferences.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenOrderBlockingSource) {
				venOrder.setVenOrderBlockingSource((VenOrderBlockingSource) next);
			} else if (next instanceof VenOrderStatus) {
				venOrder.setVenOrderStatus((VenOrderStatus) next);
			}
		}
		*/		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderReferenceData::EOF, returning venOrder = " + venOrder);
		return venOrder;
	}	
	
	/*
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	private VenCustomer persistCustomer(VenCustomer venCustomer) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistCustomer::BEGIN, venCustomer = " + venCustomer);
		if (venCustomer != null) {
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::Persisting VenCustomer... :" + venCustomer.getCustomerUserName());
				// If the customer already exists then return it, else persist everything
				//List<VenCustomer> venCustomerList = venCustomerDAO.findByWcsCustomerId(venCustomer.getWcsCustomerId());
				//if (venCustomerList != null && (venCustomerList.size() > 0)) {
				//	venCustomer.setCustomerId(venCustomerList.get(0).getCustomerId());
				//}
				VenCustomer existingVenCustomer = customerService.findByWcsCustomerId(venCustomer.getWcsCustomerId());
				if (existingVenCustomer != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistCustomer::existingVenCustomer NOT NULL --> " + existingVenCustomer);
					venCustomer.setCustomerId(existingVenCustomer.getCustomerId());
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::setting Customer's partyType");
				VenPartyType venPartyType = new VenPartyType();
				// Set the party type to Customer
				venPartyType.setPartyTypeId(new Long(4));
				venPartyType.setPartyTypeDesc("Customer");
				venCustomer.getVenParty().setVenPartyType(venPartyType);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::Customer's partyType successfully set");
				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::setting Customer's party");
				VenParty party = venCustomer.getVenParty();
				List<VenCustomer> venCustomers = new ArrayList<VenCustomer>();
				venCustomers.add(0, venCustomer);
				party.setVenCustomers(venCustomers);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::Customer's party successfully set");
				venCustomer.setVenParty(this.persistParty(party, "Customer"));
				// Synchronize the reference data
				venCustomer = this.synchronizeVenCustomerReferenceData(venCustomer);

				// Persist the object
				VenCustomer customer = venCustomer;
				//venCustomer = customerHome.mergeVenCustomer(venCustomer);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistCustomer::persisting venCustomer");
				venCustomer.setVenParty(customer.getVenParty());
				venCustomer = venCustomerDAO.save(venCustomer);
				//venCustomer.setVenParty(customer.getVenParty());
			} catch (Exception e) {
				//e.printStackTrace();
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, e.getMessage());
				throw CommonUtil.logAndReturnException(new InvalidOrderException(
						"An exception occured when persisting VenCustomer", VeniceExceptionConstants.VEN_EX_100001)
				  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistCustomer::EOM, venCustomer has been persisted successfully, " + venCustomer);
		return venCustomer;
	}
	*/	
	
	/**
	 * Synchronizes the data for the direct VenCustomer references
	 * 
	 * @param venCustomer
	 * @return the synchronized data object
	 */
	/*
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	private VenCustomer synchronizeVenCustomerReferenceData(
			VenCustomer venCustomer) throws VeniceInternalException {

		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenCustomerReferenceData, BEGIN, venCustomer = " + venCustomer);
		List<Object> references = new ArrayList<Object>();
		references.add(venCustomer.getVenParty());

		// Synchronize the data references
		references = this.synchronizeReferenceData(references);

		// Push the keys back into the record
		Iterator<Object> referencesIterator = references.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenParty) {
				venCustomer.setVenParty((VenParty) next);
			}
		}
		return venCustomer;
	}	
	*/
	
	/**
	 * Persists a list of party addresses using the session tier.
	 * 
	 * @param venPartyAddressList
	 * @return the persisted object
	 */
	/*
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	List<VenPartyAddress> persistPartyAddresses(List<VenPartyAddress> venPartyAddressList) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistPartyAddresses::BEGIN, venPartyAddressList = " + venPartyAddressList);
		List<VenPartyAddress> newVenPartyAddressList = new ArrayList<VenPartyAddress>();
		//if (venPartyAddressList != null && !venPartyAddressList.isEmpty()) {
		if (venPartyAddressList != null && venPartyAddressList.size() > 0) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistPartyAddresses::Persisting VenPartyAddress list...:" + venPartyAddressList.size());
				Iterator<VenPartyAddress> i = venPartyAddressList.iterator();
				while (i.hasNext()) {
					VenPartyAddress next = i.next();

					// Set up the primary key object
					VenPartyAddressPK id = new VenPartyAddressPK();
					id.setAddressId(next.getVenAddress().getAddressId());
					id.setPartyId(next.getVenParty().getPartyId());
					id.setAddressTypeId(next.getVenAddressType().getAddressTypeId());
					next.setId(id);
					// Persist the object
					newVenPartyAddressList.add(venPartyAddressDAO.save(next));
				}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistPartyAddresses::EOM, returning newVenPartyAddressList = " + newVenPartyAddressList);
		return newVenPartyAddressList;
	}	
	*/
	
	/*
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	VenParty persistParty(VenParty venParty, String type) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistParty::BEGIN, venParty " + venParty + ", type " + type);
		if (venParty != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistParty::venParty is not null (i.e " + venParty + ")");
			//try {
				VenParty existingParty;
				if(type.equals("Customer")){
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistParty::Persisting VenParty... :" 
					+ venParty.getVenCustomers().get(0).getCustomerUserName());
	
					// Get any existing party based on customer username
					existingParty = this.retrieveExistingParty(venParty.getVenCustomers().get(0).getCustomerUserName());
				} else {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistParty::Persisting VenParty... :" + venParty.getFullOrLegalName());

					// Get any existing party based on full or legal name 
					existingParty = this.retrieveExistingParty(venParty.getFullOrLegalName());
				}
			
				//If the party already exists then the existing party 
				//addresses and contacts may have changed so we
				//need to synchronize them
				 							
				if (existingParty != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistParty::existing Party not null" );
					
					//Get the existing addresses
					List<VenAddress> existingAddressList = new ArrayList<VenAddress>();
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistParty::existingParty.getVenPartyAddresses() = " 
							+ existingParty.getVenPartyAddresses());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistParty::existingParty.getVenPartyAddresses().size() = "
							+ existingParty.getVenPartyAddresses().size());
					for(VenPartyAddress venPartyAddress:existingParty.getVenPartyAddresses()){
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::ven Party Address... :"+venPartyAddress.getVenAddress().getAddressId());
						existingAddressList.add(venPartyAddress.getVenAddress());
					}
					
					//Get the new addresses
					List<VenAddress> newAddressList = new ArrayList<VenAddress>();
					if(venParty.getVenPartyAddresses()!=null){
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::venParty.getVenPartyAddresses() not null");
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::venParty.getVenPartyAddresses().size() = " 
								+ venParty.getVenPartyAddresses().size());
						for(VenPartyAddress venPartyAddress:venParty.getVenPartyAddresses()){
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "persistParty::New ven Party Address... :"
							+ venPartyAddress.getVenAddress().getStreetAddress1());
							newAddressList.add(venPartyAddress.getVenAddress());
						}
					}

					//If any new addresses are provided then check that 
					//the existing addresses match the new addresses
					//else add the new addresses for the party 
					
					if(!newAddressList.isEmpty()){
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::New ven Party Address is not empty and Update Address List");
						List<VenAddress> updatedAddressList = this.updateAddressList(existingAddressList, newAddressList);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::updatedAddressList size => " + updatedAddressList.size());
						List<VenAddress> tempAddressList = new ArrayList<VenAddress>();
						
						tempAddressList.addAll(updatedAddressList);
						
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::Remove old VenAddress");
						//Remove all the existing addresses
						updatedAddressList.removeAll(existingAddressList);
						
						//Setup the new VenPartyAddress records
						List<VenPartyAddress> venPartyAddressList = new ArrayList<VenPartyAddress>();
						for(VenAddress updatedAddress:updatedAddressList){
							VenPartyAddress venPartyAddress = new VenPartyAddress();							
							VenAddressType venAddressType = new VenAddressType();
							venAddressType.setAddressTypeId(VeniceConstants.VEN_ADDRESS_TYPE_DEFAULT);
														
							venPartyAddress.setVenAddress(updatedAddress);
							venPartyAddress.setVenAddressType(venAddressType);
							venPartyAddress.setVenParty(existingParty);
							existingParty.getVenPartyAddresses().add(venPartyAddress);
							
							venPartyAddressList.add(venPartyAddress);							
						}
						
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::persist Party Addresses ");
						//Persist the new VenPartyAddress records
						venPartyAddressList = this.persistPartyAddresses(venPartyAddressList);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::venPartyAddressList=" + venPartyAddressList);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::size=" + venPartyAddressList.size());
						
						if(updatedAddressList.size() == 0){
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "persistParty::updatedAddressList.size == 0");
							for(VenAddress updatedAddress:tempAddressList){					
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "persistParty::updatedAddress = " + updatedAddress);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "persistParty::total venparty addresses : " + updatedAddress.getVenPartyAddresses().size() );
								venPartyAddressList.addAll(updatedAddress.getVenPartyAddresses());	
								for(VenPartyAddress venPartyAddress:venPartyAddressList){
									CommonUtil.logDebug(this.getClass().getCanonicalName()
											, "persistParty::VenPartyAddress => " + venPartyAddress.getVenAddress().getAddressId());
								}
							}
						}
						
						//copy existing address list to new list so it can be added new address list
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::copy address list to new list");
//						List<VenPartyAddress> venPartyAddressList2=new ArrayList<VenPartyAddress>(existingParty.getVenPartyAddresses()).subList(0,venPartyAddressList.size());
						
						//Add all the new party address records	
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::add All VenParty Addresses");
						existingParty.setVenPartyAddresses(venPartyAddressList);
						for(VenPartyAddress venPartyAddress:venPartyAddressList){
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "persistParty::VenPartyAddress => " + venPartyAddress.getVenAddress().getAddressId());
						}
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistParty::done add All VenParty Addresses");
					}
					
					//If any new contact details are provided then check
					//that the new contact details match the existing 
					//contact details else add the new contact details
					//to the party and then merge.
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistParty::Get old and new party ven contact Detail  ");
					//Get the existing contact details
					List<VenContactDetail> existingContactDetailList = existingParty.getVenContactDetails();
					
					if(venParty.getVenContactDetails()!=null){
							//Get the new addresses
							List<VenContactDetail> newContactDetailList = venParty.getVenContactDetails();
							
							if(!newContactDetailList.isEmpty()){
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "persistParty::updatedContact Detail List from existingContactDetailList to newContactDetailList");
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "persistParty::start updating contact detail");
								//if the contact detail of existing party is null we can not get the party id using existingContactDetailList, so send the existing party
								List<VenContactDetail> updatedContactDetailList = this.updateContactDetailList(existingParty, existingContactDetailList, newContactDetailList);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "persistParty::done updating contact detail!!!");
		
								existingParty.setVenContactDetails(updatedContactDetailList);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "persistParty::assigned updatedContactDetailList into existingParty");
							}
					}
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistParty::existingParty not null block condition, returning existingParty " + existingParty);
					return existingParty;
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistParty::persisting addresses");
				// Persist addresses
				List<VenAddress> addressList = new ArrayList<VenAddress>();
				Iterator<VenPartyAddress> i = venParty.getVenPartyAddresses().iterator();
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistParty::i = " + i);
				while (i.hasNext()) {
					addressList.add(i.next().getVenAddress());
				}
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistParty::persisting Address List");
				addressList = this.persistAddressList(addressList);
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistParty::addressList persisted with total member : " + addressList.size());

				// Assign the address keys back to the n-n object
				i = venParty.getVenPartyAddresses().iterator();
				int index = 0;
				while (i.hasNext()) {
					VenPartyAddress next = i.next();
					next.setVenAddress(addressList.get(index));
					VenAddressType addressType = new VenAddressType();
					addressType.setAddressTypeId(VeniceConstants.VEN_ADDRESS_TYPE_DEFAULT);
					List<Object> references = new ArrayList<Object>();
					references.add(addressType);
					references = this.synchronizeReferenceData(references);
					index++;
				}

				// Detach the party addresses object before persisting party
				List<VenPartyAddress> venPartyAddressList = venParty.getVenPartyAddresses();
				venParty.setVenPartyAddresses(null);

				// Detach the list of contact details before persisting party
				List<VenContactDetail> venContactDetailList = venParty.getVenContactDetails();
				venParty.setVenContactDetails(null);
				
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistParty::synchronize VenParty Reference Data");
				// Synchronize the reference data
				venParty = this.synchronizeVenPartyReferenceData(venParty);
				
				// Persist the object
				//VenPartySessionEJBLocal partyHome = (VenPartySessionEJBLocal) this._genericLocator.lookupLocal(VenPartySessionEJBLocal.class, "VenPartySessionEJBBeanLocal");

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistParty::persisting VenParty ");
				// Merge the party object
				//venParty = (VenParty) partyHome.persistVenParty(venParty);
				venParty = venPartyDAO.save(venParty);

				VenAddressType venAddressType = new VenAddressType();
				venAddressType.setAddressTypeId(VeniceConstants.VEN_ADDRESS_TYPE_DEFAULT);

				// Set the party relationship for each VenPartyAddress
				i = venPartyAddressList.iterator();
				while (i.hasNext()) {
					VenPartyAddress next = i.next();
					next.setVenParty(venParty);
					next.setVenAddressType(venAddressType);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistParty::persisting Party Addresses ");
				// Persist the party addresses
				venParty.setVenPartyAddresses(this.persistPartyAddresses(venPartyAddressList));
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistParty::Venpartyaddress size = >" + venParty.getVenPartyAddresses().size());
				// Set the party relationship for each contact detail
				Iterator<VenContactDetail> contactsIterator = venContactDetailList.iterator();
				while (contactsIterator.hasNext()) {
					contactsIterator.next().setVenParty(venParty);
				}

				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistParty::persisting Contact Details");
				// Persist the contact details
				venParty.setVenContactDetails(this.persistContactDetails(venContactDetailList));
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistParty::VenContactDetails size = >" + venParty.getVenContactDetails());
			
			//} catch (Exception e) {
				//String errMsg = "An exception occured when persisting VenParty:";
				//LOG.error(errMsg + e.getMessage());
				//throw new EJBException(errMsg);
			//}
			
		} //end of block if venParty != null
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistParty::EOM, returning venParty " + venParty);
		return venParty;
	}
	*/
	
	/**
	 * Synchronizes the data for the direct VenParty references
	 * 
	 * @param venParty
	 * @return the synchronized data object
	 */
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	private VenParty synchronizeVenPartyReferenceData(VenParty venParty) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPartyReferenceData::BEGIN, venParty = " + venParty);
		List<Object> references = new ArrayList<Object>();
		if (venParty.getVenParty() != null) {
			references.add(venParty.getVenParty());
		}
		references.add(venParty.getVenPartyType());

		// Synchronize the data references
		references = this.synchronizeReferenceData(references);

		// Push the keys back into the record
		Iterator<Object> referencesIterator = references.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenParty) {
				venParty.setVenParty((VenParty) next);
			} else if (next instanceof VenPartyType) {
				venParty.setVenPartyType((VenPartyType) next);
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenPartyReferenceData::EOM, returning venParty " + venParty);
		return venParty;
	}	
	
	/**
	 * updateContactDetailList - compares the existing contact detail list with the 
	 * new contact detail list, writes any new contact details to the database 
	 * and returns the updated contact detail list.
	 * @param existingVenContactDetailList
	 * @param newVenContactDetailList
	 * @return the updated contact detail list
	 */
	/*
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	List<VenContactDetail> updateContactDetailList(VenParty existingParty
			, List<VenContactDetail> existingVenContactDetailList, List<VenContactDetail> newVenContactDetailList)
			throws VeniceInternalException {
		List<VenContactDetail> updatedVenContactDetailList = new ArrayList<VenContactDetail>();
		List<VenContactDetail> persistVenContactDetailList = new ArrayList<VenContactDetail>();
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "OrderServiceImpl::updateContactDetailList::START");
		
		//Iterate the list of existing contacts to determine if 
		//the new contacts exist already
		 
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::updateContactDetailList::newVenContactDetailList member = " 
				+ newVenContactDetailList.size());
		for(VenContactDetail newVenContactDetail:newVenContactDetailList){
			Boolean bFound = false;
			if(existingVenContactDetailList.size() > 0){
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::updateContactDetailList::existingVenContactDetailList not empty");
				for(VenContactDetail existingVenContactDetail:existingVenContactDetailList){
				
					//If the contact detail and type are not null and they are equal to each other (new and existing) 
					//then the contact is existing and is added to the return list only.
					 
					//If it is a new contact it is added to the persist list
					 
					if((existingVenContactDetail.getContactDetail() != null && newVenContactDetail.getContactDetail() != null) 
							&& existingVenContactDetail.getContactDetail().trim().equalsIgnoreCase(newVenContactDetail.getContactDetail().trim())
							&& ((existingVenContactDetail.getVenContactDetailType() != null	&& newVenContactDetail.getVenContactDetailType() != null)) 
							&& existingVenContactDetail.getVenContactDetailType().getContactDetailTypeDesc().equals(newVenContactDetail.getVenContactDetailType().getContactDetailTypeDesc())){
						
						//The contact detail is assumed to be equal (note that the equals() 
						//operation can't be used because it is implemented by 
						//JPA on the primary key. Add it to the list
						 
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderServiceImpl::updateContactDetailList::contact detail equal with existing, added to updated list");
						updatedVenContactDetailList.add(existingVenContactDetail);
						
						bFound = true;
						//Break from the inner loop as the contact is found
						break;
					}
				}
			}else{
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::updateContactDetailList::existingVenContactDetailList is empty");
			}
			
			//The contact detail is not found in the existing
			//contact list therefore it is a new contact detail 
			//and it needs to be persisted. The existing party
			//record also needs to be set otherwise it
			//will fail as the new contact record has a
			//detached party
			 
			if(!bFound){
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::updateContactDetailList::contact detail not equal with existing, persist it");
				newVenContactDetail.setVenParty(existingParty);
				if(!persistVenContactDetailList.contains(newVenContactDetail)){
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::updateContactDetailList::added the new contact detail to list");
					persistVenContactDetailList.add(newVenContactDetail);
				}
			}
		}	
		
		
		//Persist any contact details that are new
		 
		if(!persistVenContactDetailList.isEmpty()){
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "OrderServiceImpl::updateContactDetailList::new contact detail list not empty, start persist new contact detail");
			persistVenContactDetailList = this.persistContactDetails(persistVenContactDetailList);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "OrderServiceImpl::updateContactDetailList::done persist contact detail");
			//Add the persisted contact details to the new list
			updatedVenContactDetailList.addAll(persistVenContactDetailList);
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::updateContactDetailList::returning updated contact detail list ("
				+ updatedVenContactDetailList.size() + " members)");
		return updatedVenContactDetailList;
	}	
	*/
	
	/**
	 * Synchronizes the data for the direct VenContactDetail references
	 * 
	 * @param venContactDetail
	 * @return the synchronized data object
	 */
	/*
	private VenContactDetail synchronizeVenContactDetailReferenceData(VenContactDetail venContactDetail) 
			throws VeniceInternalException {
		List<Object> references = new ArrayList<Object>();
		references.add(venContactDetail.getVenContactDetailType());
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::synchronizeVenContactDetailReferenceData::start sync contact detail method");
		// Synchronize the data references
		references = this.synchronizeReferenceData(references);

		// Push the keys back into the record
		Iterator<Object> referencesIterator = references.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenContactDetailType) {
				venContactDetail.setVenContactDetailType((VenContactDetailType) next);
			}
		}
		return venContactDetail;
	}
	*/	
	
	/**
	 * Persists a list of contact details using the session tier.
	 * 
	 * @param venContactDetails
	 * @return the persisted object
	 */
	/*
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	List<VenContactDetail> persistContactDetails(List<VenContactDetail> venContactDetailList) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistContactDetails::start method persist contact detail");
		List<VenContactDetail> newVenContactDetailList = new ArrayList<VenContactDetail>();
		if (venContactDetailList != null && !venContactDetailList.isEmpty()) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistContactDetails::Persisting VenContactDetail list...:" + venContactDetailList.size());
				Iterator<VenContactDetail> i = venContactDetailList.iterator();
				while (i.hasNext()) {
					VenContactDetail next = i.next();
					// Synchronize the references
					this.synchronizeVenContactDetailReferenceData(next);
					// Persist the object
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::persistContactDetails::start persisting contact detail");
					//newVenContactDetailList.add((VenContactDetail) detailHome.persistVenContactDetail(next));
					newVenContactDetailList.add(venContactDetailDAO.save(next));
				}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistContactDetails::EOM, returning newVenContactDetailList = " + newVenContactDetailList);
		return newVenContactDetailList;
	}
	*/
	
	/**
	 * Persists an address using the session tier
	 * 
	 * @param venAddress
	 * @return the persisted object
	 * @throws InvalidOrderException 
	 */
	/*
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	VenAddress persistAddress(VenAddress venAddress) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistAddress::BEGIN, venAddress="
				+ venAddress);
		if (venAddress != null) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::persistAddress::Persisting VenAddress... :" + venAddress.getStreetAddress1());
				// Synchronize the reference data
				venAddress = this.synchronizeVenAddressReferenceData(venAddress);
				// Persist the object

				if (venAddress.getAddressId() == null) {
					if(venAddress.getStreetAddress1()==null && venAddress.getKecamatan()==null && venAddress.getKelurahan()==null && venAddress.getVenCity()==null &&
							venAddress.getVenState()==null && venAddress.getPostalCode()==null && venAddress.getVenCountry()==null){
						CommonUtil.logInfo(CommonUtil.getLogger(this.getClass().getCanonicalName())
								, "persistAddress::Address is null, no need to persist address");
					}else{
						//detach city, state, dan country karena bisa null dari WCS	
						CommonUtil.logInfo(CommonUtil.getLogger(this.getClass().getCanonicalName())
								, "persistAddress::Address is not null, detach city, state, and country");
						VenCity city = null;
						VenState state = null;
						VenCountry country = null;
						
						if(venAddress.getVenCity()!=null){
							if(venAddress.getVenCity().getCityCode()!=null){
								city = venAddress.getVenCity();
							}							
							venAddress.setVenCity(null);
						}
						
						if(venAddress.getVenState()!=null){
							if(venAddress.getVenState().getStateCode()!=null){
								state = venAddress.getVenState();
							}							
							venAddress.setVenState(null);
						}
						
						if(venAddress.getVenCountry()!=null){
							if(venAddress.getVenCountry().getCountryCode()!=null){
								country = venAddress.getVenCountry();
							}							
							venAddress.setVenCountry(null);
						}			
						
						//venAddress = (VenAddress) addressHome.persistVenAddress(venAddress);
						venAddress = venAddressDAO.save(venAddress);
						
						//attach lagi setelah persist
						venAddress.setVenCity(city);
						venAddress.setVenState(state);
						venAddress.setVenCountry(country);
						
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistAddress::persist address");
						//venAddress = (VenAddress) addressHome.mergeVenAddress(venAddress);
						venAddress = venAddressDAO.save(venAddress);
					}
				} else {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistAddress::merge address");
					//venAddress = (VenAddress) addressHome.mergeVenAddress(venAddress);
					venAddress = venAddressDAO.save(venAddress);
				}

		}
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistAddress::returning venAddress " + venAddress);
		return venAddress;
	}	
	
	/**
	 * This is a private method specifically to handle the synchronization
	 * problems with reference data using the master data management strategy
	 * implemented in the interfaces. Whenever any inbound message is received
	 * this method is called to check the cache for the dependent data.
	 * 
	 * SLAVE: If the reference does not exist and one of the other applications
	 * is the master then the data will be inserted.
	 * 
	 * MASTER: if the reference data does not exist and Venice is the master
	 * then an exception shall be thrown.
	 * 
	 * If the data already exists (either as MASTER or SLAVE) as a part of this
	 * process then the keys used will be handed back to the caller to use in
	 * persisting the master record.
	 * 
	 * Note: that this method hits most of the order related database structure
	 * recursively upon any of the modifying operations (see above).
	 * 
	 * @return a list of synchronized objects
	 */
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	private List<Object> synchronizeReferenceData(List<Object> references) throws VeniceInternalException {
		List<Object> retVal = new ArrayList<Object>();
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeReferenceData::references size = " + references.size());
		Iterator<Object> i = references.iterator();
		while (i.hasNext()) {
			Object next = i.next();
			if (next != null) {
				/*
				if (next instanceof VenAddress) {
					this.synchronizeVenAddressReferenceData((VenAddress) next);
				}
				*/
				
				// Banks need to be restricted to Venice values
				/*
				if (next instanceof VenOrder) {
					if (((VenOrder) next).getWcsOrderId() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderServiceImpl::synchronizeReferenceData::Restricting VenOrder... :" + ((VenOrder) next).getWcsOrderId());
						VenOrder venOrder = venOrderDAO.findByWcsOrderId(((VenOrder) next).getWcsOrderId());
						if (venOrder == null) {
							throw CommonUtil.logAndReturnException(new InvalidOrderException(
									"OrderServiceImpl::synchronizeReferenceData::Order does not exist"
									, VeniceExceptionConstants.VEN_EX_000020)
							  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						} else {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::adding venOrder into retVal");
							retVal.add(venOrder);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::successfully added venOrder into retVal");
						}
					}
				}
				*/
				
				// Order payments need to be synchronized SPECIAL CASE
				// MANY-MANY)
				/*
				if (next instanceof VenOrderPayment) {
					if (((VenOrderPayment) next).getWcsPaymentId() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderServiceImpl::synchronizeReferenceData::Synchronizing VenOrderPayment... :" 
						+ ((VenOrderPayment) next).getWcsPaymentId());
						VenOrderPayment orderPayment = this.synchronizeVenOrderPaymentReferenceData((VenOrderPayment) next);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderServiceImpl::synchronizeReferenceData::adding orderPayment into retVal");
						retVal.add(orderPayment);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderServiceImpl::synchronizeReferenceData::successfully added orderPayment into retVal");
					}
				}
				*/

				// Parties need to be synchronized
				/*
				if (next instanceof VenParty) {
					if (((VenParty) next).getFullOrLegalName() != null) {
						try {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), 
									"synchronizeReferenceData::Synchronizing VenParty reference data... ");
							next = this.synchronizeVenPartyReferenceData((VenParty) next);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeReferenceData::adding next (venParty) into retVal");
							retVal.add(next);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeReferenceData::successfully added next (venParty) into retVal");
						} catch (Exception e) {
							//e.printStackTrace();
							throw CommonUtil.logAndReturnException(
									new VeniceInternalException("An exception occured synchronizing VenParty reference data")
							  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						}
					}
				}
				*/
				
				// Banks need to be restricted to Venice values
				/*
				if (next instanceof VenBank) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::synchronizeReferenceData::next instanceof VenBank");
					if (((VenBank) next).getBankCode() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting VenBank... :" + ((VenBank) next).getBankCode());
						VenBank bank = venBankDAO.findByBankCode(((VenBank) next).getBankCode());
						if (bank == null) {
							throw CommonUtil.logAndReturnException(new InvalidOrderException(
									"Bank does not exist", VeniceExceptionConstants.VEN_EX_200001)
							, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						} else {
							// clone to prevent data unexpectedly changed in DB
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::adding bank into retVal");
							retVal.add(bank);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::successfully added bank into retVal");
						}
					}
				}
				*/
				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeReferenceData::customers need to be synchronized");
				// Customers need to be synchronized
				if (next instanceof VenCustomer) {
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeReferenceData::next instanceof VenCustomer");
					if (((VenCustomer) next).getWcsCustomerId() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Synchronizing VenCustomer... :" + ((VenCustomer) next).getWcsCustomerId());
						// Synchronize the reference data
						next = customerService.synchronizeVenCustomerReferenceData((VenCustomer) next);
						// Synchronize the object
						VenCustomer customer = venCustomerDAO.save((VenCustomer) next);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeReferenceData::customer successfully saved");
						retVal.add(customer);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeReferenceData::successfully added customer into retVal");
					}
				}
				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeReferenceData::LogisticsProvider should be restricted to venice values");
				// Logistics provider must be restricted to Venice values
				if (next instanceof LogLogisticsProvider) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "next instanceof LogLogisticsProvider");
					if (((LogLogisticsProvider) next).getLogisticsProviderCode() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting LogLogisticsProvider... :" 
								+ ((LogLogisticsProvider) next).getLogisticsProviderCode());
						LogLogisticsProvider logisticsProvider = logLogisticProviderDAO.findByLogisticsProviderCode(((LogLogisticsProvider) next).getLogisticsProviderCode());
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeReferenceData::logisticProviders is detached");
						if (logisticsProvider == null) {
							throw CommonUtil.logAndReturnException(new InvalidOrderLogisticInfoException(
									"Logistics provider does not exist", VeniceExceptionConstants.VEN_EX_000011)
							, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						} else {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeReferenceData::adding logisticsProvider into retVal");
							retVal.add(logisticsProvider);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeReferenceData::logisticsProvider added into retVal");
						}
					}
				}
				// Merchant products need to be synchronized
				/*
				if (next instanceof VenMerchantProduct) {
					if (((VenMerchantProduct) next).getWcsProductSku() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Synchronizing VenMerchantProduct... :" + ((VenMerchantProduct) next)	.getWcsProductSku());
						next = this.synchronizeVenMerchantProductReferenceData((VenMerchantProduct) next);
						List<VenMerchantProduct> merchantProductList = venMerchantProductDAO.findByWcsProductSku(((VenMerchantProduct) next).getWcsProductSku());
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeReferenceData::merchantProductList size = " 
										+ merchantProductList.size());
						if (merchantProductList == null || (merchantProductList.size() == 0)) {
							VenMerchantProduct merchantProduct = venMerchantProductDAO.save((VenMerchantProduct) next);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeReferenceData::adding merchantProduct into retVal");
							retVal.add(merchantProduct);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeReferenceData::successfully added merchantProduct into retVal");
						} else {
							VenMerchantProduct merchantProduct = merchantProductList.get(0);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeReferenceData::successfully added merchantProduct into retVal");
						}
					}
				}
				*/
				// Logistics service must be restricted to Venice values
				/*
				if (next instanceof LogLogisticService) {
					if (((LogLogisticService) next).getServiceCode() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting LogLogisticService... :" + ((LogLogisticService) next).getServiceCode());
						LogLogisticService logisticService = logLogisticServiceDAO.findByServiceCode(((LogLogisticService) next).getServiceCode());
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderserviceImpl::synchronizeReferenceData::logisticService detached");
						if (logisticService == null) {
							throw CommonUtil.logAndReturnException(new InvalidOrderException(
									"Logistics service does not exist", VeniceExceptionConstants.VEN_EX_000011)
							, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						} else {
							retVal.add(logisticService);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "ORderServiceImpl::synchronizeReferenceData::successfully added logisticService into retVal");
						}
					}
				}
				*/
				// Party addresses need to be synchronized
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::synchronizeReferenceData::Party addresses need to be synchronized");
				if (next instanceof VenPartyAddress) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::synchronizeReferenceData::next instanceof VenPartyAddress");
					if (((VenPartyAddress) next).getVenAddress() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Synchronizing VenPartyAddress... :" 
								+ ((VenPartyAddress) next).getVenAddress().getStreetAddress1());
						// Synchronize the reference data
						next = this.synchronizeVenPartyAddressReferenceData((VenPartyAddress) next);
						// Synchronize the object

						VenPartyAddress partyAddress = (VenPartyAddress) venPartyAddressDAO.save((VenPartyAddress) next);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderServiceImpl::synchronizeReferenceData::partyAddress detached");
						retVal.add(partyAddress);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "ORderServiceImpl::synchronizeReferenceData::successfully added partyAddress into retVal");
					}
				}
				// Order status values need to be restricted to Venice values
				/*
				if (next instanceof VenOrderStatus) {
					if (((VenOrderStatus) next).getOrderStatusCode() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting VenOrderStatus... :" + ((VenOrderStatus) next).getOrderStatusCode());
						
						VenOrderStatus venOrderStatus = venOrderStatusDAO.findByOrderStatusCode(((VenOrderStatus) next).getOrderStatusCode());
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderServiceImpl::synchronizeReferenceData::venOrderStatus detached");
						if (venOrderStatus == null) {
							throw CommonUtil.logAndReturnException(new InvalidOrderException("Order status does not exist", 
									 VeniceExceptionConstants.VEN_EX_000025)
							  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						} else {
							retVal.add(venOrderStatus);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "ORderServiceImpl::synchronizeReferenceData::successfully added venOrderStatus into retVal");
						}
					}
				}
				*/
				// Fraud check status needs to be restricted to Venice values
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::synchronizeReferenceData::Fraud check status need to be restricted to Venice values");
				if (next instanceof VenFraudCheckStatus) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::synchronizeReferenceData::next instanceof VenFraudCheckStatus");
					if (((VenFraudCheckStatus) next).getFraudCheckStatusDesc() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting VenFraudCheckStatus... :" + ((VenFraudCheckStatus) next).getFraudCheckStatusDesc());
						List<VenFraudCheckStatus> fraudCheckStatusList = venFraudCheckStatusDAO.findByFraudCheckStatusDesc(((VenFraudCheckStatus) next).getFraudCheckStatusDesc());
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderServiceImpl::synchronizeReferenceData::fraudCheckStatusList size = " 
										+ fraudCheckStatusList.size());
						if (fraudCheckStatusList == null || (fraudCheckStatusList.size() == 0)) {
							throw CommonUtil.logAndReturnException(new InvalidOrderException(
									"Fraud check status value does not exist", VeniceExceptionConstants.VEN_EX_300001)
							, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						} else {
							VenFraudCheckStatus fraudCheckStatus = fraudCheckStatusList.get(0);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::fraudCheckStatus detached");
							retVal.add(fraudCheckStatus);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::successfully added fraudCheckStatus into retVal");
						}
					}
				}
				// Contact details need to be synchronized
				/*
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::synchronizeReferenceData::Contact details need to be synchronized");
				if (next instanceof VenContactDetail) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::synchronizeReferenceData::next instanceof VenContactDetail");
					if (((VenContactDetail) next).getVenContactDetailType() != null) {
						try {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "Synchronizing VenContactDetail... :" + ((VenContactDetail) next).getVenContactDetailType());
							// Synchronize the reference data
							next = this.synchronizeVenContactDetailReferenceData((VenContactDetail) next);
							// Synchronize the object							
							VenContactDetail contactDetail = venContactDetailDAO.save((VenContactDetail) next);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::contactDetail detached");
							//VenContactDetail contactDetailClone = cloner.deepClone(contactDetail);
							//retVal.add(contactDetailClone);
							retVal.add(contactDetail);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::successfully added contactDetail into retVal");
						} catch (Exception e) {
							CommonUtil.logAndReturnException(new VeniceInternalException("cannot persisting VenContactDetail", e)
							   , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						}						
					}
				}
				*/
				// Contact detail types need to be restricted to Venice values
				/*
				if (next instanceof VenContactDetailType) {
					if (((VenContactDetailType) next).getContactDetailTypeDesc() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting VenContactDetailType... :" + ((VenContactDetailType) next).getContactDetailTypeDesc());
							List<VenContactDetailType> contactDetailTypeList = venContactDetailTypeDAO.findByContactDetailTypeDesc(((VenContactDetailType) next).getContactDetailTypeDesc());
							if (contactDetailTypeList == null || (contactDetailTypeList.size() == 0)) {
								throw CommonUtil.logAndReturnException(new InvalidOrderException(
										"Contact detail type does not exist", VeniceExceptionConstants.VEN_EX_999999)
								  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
							} else {
								VenContactDetailType contactDetailType = contactDetailTypeList.get(0);
								retVal.add(contactDetailType);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::successfully added contactDetailType into retVal");
							}
					}
				}
				*/
				// Blocking sources need to be restricted to Venice values
				/*
				if (next instanceof VenOrderBlockingSource) {
					if (((VenOrderBlockingSource) next).getBlockingSourceDesc() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting VenOrderBlockingSource... :" + ((VenOrderBlockingSource) next).getBlockingSourceDesc());
							
						VenOrderBlockingSource venOrderBlockingSource = venOrderBlockingSourceDAO.findByBlockingSourceDesc(((VenOrderBlockingSource) next).getBlockingSourceDesc());
						if (venOrderBlockingSource == null) {
							throw CommonUtil.logAndReturnException(new InvalidOrderException(
									"Order blocking source does not exist", VeniceExceptionConstants.VEN_EX_999999)
							  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						} else {
							retVal.add(venOrderBlockingSource);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::successfully added venOrderBlockingSource into retVal");
						}
					}
				}
				*/
				// Party types need to be restricted to Venice values
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::synchronizeReferenceData::Party types need to be restricted to venice values");
				if (next instanceof VenPartyType) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::synchronizeReferenceData::next instanceof VenPartyType");
					if (((VenPartyType) next).getPartyTypeDesc() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting VenPartyType... :" + ((VenPartyType) next).getPartyTypeId());
						VenPartyType partyType = venPartyTypeDAO.findOne(((VenPartyType) next).getPartyTypeId());
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "partyType detached");
						if (partyType == null) {
							throw CommonUtil.logAndReturnException(new InvalidOrderException(
									"Party type does not exist", VeniceExceptionConstants.VEN_EX_999999)
							, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						} else {
							//retVal.add(partyTypeList.get(0));
							//VenPartyType partyTypeClone = cloner.deepClone(partyType);
							//retVal.add(partyTypeClone);
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "OrderServiceImpl::synchronizeReferenceData::adding partyType into retVal");
							retVal.add(partyType);
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "OrderServiceImpl::synchronizeReferenceData::successfully added partyType into retVal");
						}
					}
				}
				// Payment status must be restricted to Venice values
				/*
				if (next instanceof VenPaymentStatus) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::synchronizeReferenceData::next instanceof VenPaymentStatus");
					if (((VenPaymentStatus) next).getPaymentStatusCode() != null) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting VenPaymentStatus... :" + ((VenPaymentStatus) next).getPaymentStatusCode());
							List<VenPaymentStatus> paymentStatusList = venPaymentStatusDAO.findByPaymentStatusCode(((VenPaymentStatus) next).getPaymentStatusCode());
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "OrderServiceImpl::synchronizeReferenceData::paymentStatusList size = " + paymentStatusList.size());
							if (paymentStatusList == null || (paymentStatusList.size() == 0)) {
								throw CommonUtil.logAndReturnException(new InvalidOrderException(
										"Payment status does not exist", VeniceExceptionConstants.VEN_EX_999999)
								  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
							} else {
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "OrderServiceImpl::synchronizeReferenceData::adding paymentstatus into retVal");
								VenPaymentStatus paymentStatus = paymentStatusList.get(0);
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "OrderServiceImpl::synchronizeReferenceData::paymentStatus=" + paymentStatus);
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "OrderServiceImpl::synchronizeReferenceData::paymentStatus detached");
								retVal.add(paymentStatus);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::successfully added paymentStatus into retVal");
							}
					}
				}
				*/
				// Payment type must be restricted to Venice values
				/*
				if (next instanceof VenPaymentType) {
					if (((VenPaymentType) next).getPaymentTypeCode() != null) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting VenPaymentType... :" + ((VenPaymentType) next).getPaymentTypeCode());
							List<VenPaymentType> paymentTypeList = venPaymentTypeDAO.findByPaymentTypeCode(((VenPaymentType) next).getPaymentTypeCode());
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "OrderServiceImpl::synchronizeReferenceData::paymentTypeList size = "
									+ paymentTypeList.size());
							if (paymentTypeList == null || (paymentTypeList.size() == 0)) {
								throw CommonUtil.logAndReturnException(new InvalidOrderException(
										"Payment type does not exist", VeniceExceptionConstants.VEN_EX_999999)
								  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
							} else {
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::paymentTypeList not empty");
								VenPaymentType paymentType = paymentTypeList.get(0);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::paymentType detached");
							}
					}
				}
				*/
				// WCS Payment type must be restricted
				/*
				if (next instanceof VenWcsPaymentType) {
					if (((VenWcsPaymentType) next).getWcsPaymentTypeCode() != null) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting VenWcsPaymentType... :" + ((VenWcsPaymentType) next).getWcsPaymentTypeCode());
							VenWcsPaymentType wcsPaymentType = venWcsPaymentTypeDAO.findByWcsPaymentTypeCode(((VenWcsPaymentType) next).getWcsPaymentTypeCode()); 
							em.detach(wcsPaymentType);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::wcsPaymentType = " + wcsPaymentType);
							if (wcsPaymentType == null) {
								throw CommonUtil.logAndReturnException(new InvalidOrderException(
										"WCS Payment type does not exist", VeniceExceptionConstants.VEN_EX_999999)
								  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
							} else {
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::adding wcsPaymentType "
										+ wcsPaymentType + " into retVal");
								retVal.add(wcsPaymentType);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::wcsPaymentType added into retVal");
							}
					}
				}
				*/

				// Product categories need to be synchronized
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::synchronizeReferenceData::Product categories need to be synchronized");
				if (next instanceof VenProductCategory) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::synchronizeReferenceData::next instanceof VenProductCategory");
					if (((VenProductCategory) next).getProductCategory() != null) {
						try {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "Synchronizing VenProductCategory... :" + ((VenProductCategory) next).getProductCategory());
							
							List<VenProductCategory> productCategoryList = venProductCategoryDAO.findByProductCategory(((VenProductCategory) next).getProductCategory());
						    CommonUtil.logDebug(this.getClass().getCanonicalName()
						    		, "OrderServiceImpl::synchronizeReferenceData::productCategoryList size = "
						    		+ productCategoryList.size());
							if (productCategoryList == null || (productCategoryList.size() == 0)) {
								//VenProductCategory productCategory = (VenProductCategory) productCategoryHome.persistVenProductCategory((VenProductCategory) next);
								VenProductCategory productCategory = venProductCategoryDAO.save((VenProductCategory) next);
								em.detach(productCategory);
								CommonUtil.logDebug(this.getClass().getCanonicalName(), "OrderServiceImpl::synchronizeReferenceData::productCategory detached");
								//VenProductCategory productCategoryClone = cloner.deepClone(productCategory);
								//retVal.add(productCategoryClone);
								retVal.add(productCategory);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::added productCategory into retVal");
							} else {
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::productCategoryList size = "
										+ productCategoryList.size());
								VenProductCategory productCategory = productCategoryList.get(0);
								em.detach(productCategory);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::productCategory detached");
								//retVal.add(productCategoryList.get(0));
								retVal.add(productCategory);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::added productCategory into retVal");
							}
							
						} catch (Exception e) {
							throw CommonUtil.logAndReturnException(new VeniceInternalException("cannot persisting product category", e)
							   , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						}						
					}
				}
				// Promotions need to be synchronized
				/*
				if (next instanceof VenPromotion) {
					if (((VenPromotion) next).getPromotionCode() != null) {
						try {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "Synchronizing VenPromotion... :" + ((VenPromotion) next).getPromotionCode());
							VenPromotion promotion = new VenPromotion();
							
							List<VenPromotion> promotionExactList = venPromotionDAO.findByPromotionAndMargin(((VenPromotion) next).getPromotionCode()
									, ((VenPromotion) next).getPromotionName(), ((VenPromotion) next).getGdnMargin()
									, ((VenPromotion) next).getMerchantMargin(), ((VenPromotion) next).getOthersMargin());
							
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::promotionExactList size: "+promotionExactList.size());
														
							if(promotionExactList.size()>0) {
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::exact promo found");
								promotion=promotionExactList.get(0);								
								if(promotion.getVenPromotionType()==null || promotion.getVenPromotionType().getPromotionType()==null){	
									if(promotion.getPromotionName().toLowerCase().contains("free shipping")){
										VenPromotionType type = new VenPromotionType();
										type.setPromotionType(VeniceConstants.VEN_PROMOTION_TYPE_FREESHIPPING);
										promotion.setVenPromotionType(type);
										//promotion=promotionHome.mergeVenPromotion(promotion);
										promotion = venPromotionDAO.save(promotion);
									}																				
								}								
							}else {								
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::exact promo not found, cek uploaded promo");
								List<VenPromotion> promotionUploadedList = venPromotionDAO.findByPromotionAndMargin(
										((VenPromotion) next).getPromotionCode(), null, null, null, null);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::promotionUploadedList size = "
										+ promotionUploadedList.size());
								if(promotionUploadedList.size()>0){
									CommonUtil.logDebug(this.getClass().getCanonicalName(), "uploaded promo found, set the promo name and margins and then merge");
									promotion=promotionUploadedList.get(0);
									CommonUtil.logDebug(this.getClass().getCanonicalName()
											, "OrderServiceImpl::synchronizeReferenceData::promotion is detached");
									promotion.setPromotionName(((VenPromotion) next).getPromotionName());
									promotion.setGdnMargin(((VenPromotion) next).getGdnMargin());
									promotion.setMerchantMargin(((VenPromotion) next).getMerchantMargin());
									promotion.setOthersMargin(((VenPromotion) next).getOthersMargin());	
									promotion = venPromotionDAO.save(promotion);
									CommonUtil.logDebug(this.getClass().getCanonicalName()
											, "OrderServiceImpl::synchronizeReferenceData::successfully saved promotion into database");
								}else{
									CommonUtil.logDebug(this.getClass().getCanonicalName(), "no exact matching promo code, no uploaded promo, persist promo from inbound");
									promotion = venPromotionDAO.save((VenPromotion) next);
									
									CommonUtil.logDebug(this.getClass().getCanonicalName()
											, "OrderServiceImpl::synchronizeReferenceData::promotion is detached");
									//check the promo code for free shipping
									if(promotion.getVenPromotionType()==null || promotion.getVenPromotionType().getPromotionType()==null){											
										if(promotion.getPromotionName().toLowerCase().contains("free shipping")){
											VenPromotionType type = new VenPromotionType();
											type.setPromotionType(VeniceConstants.VEN_PROMOTION_TYPE_FREESHIPPING);
											promotion.setVenPromotionType(type);
											promotion = venPromotionDAO.save(promotion);
											CommonUtil.logDebug(this.getClass().getCanonicalName()
													, "OrderServiceImpl::synchronizeReferenceData::successfully saved promotion");
										}
									}
								}															
							}
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "ORderServiceImpl::synchronizeReferenceData::adding promotion into retVal");
							retVal.add(promotion);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::successfully added promotion into retVal");
						} catch (Exception e) {
							throw CommonUtil.logAndReturnException(
									new VeniceInternalException("An unknown exception occured inside synchronizeReferenceData method")
								, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						}
					}
				}
				*/
				// Cities need to be synchronized
				/*
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::synchronizeReferenceData::Cities need to be synchronized");
				if (next instanceof VenCity) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::synchronizeReferenceData::next instanceof VenCity");
					if (((VenCity) next).getCityCode() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Synchronizing VenCity... :" + ((VenCity) next).getCityCode());
						//try {
							//VenCitySessionEJBLocal cityHome = (VenCitySessionEJBLocal) this._genericLocator.lookupLocal(VenCitySessionEJBLocal.class, "VenCitySessionEJBBeanLocal");
							//List<VenCity> cityList = cityHome.queryByRange("select o from VenCity o where o.cityCode ='" + ((VenCity) next).getCityCode() + "'", 0, 1);
							List<VenCity> cityList = venCityDAO.findByCityCode(((VenCity) next).getCityCode());
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "ORderServiceImpl::synchronizeReferenceData::cityList size = "
									+ cityList.size());
							if (cityList == null || (cityList.size() == 0)) {
								//VenCity city = (VenCity) cityHome.persistVenCity((VenCity) next);
								VenCity city = (VenCity) venCityDAO.save((VenCity) next);
								retVal.add(city);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::successfully added city " + city 
										+ " into retVal");
							} else {
								VenCity city = cityList.get(0);
								retVal.add(city);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "successfully added city into retVal");
							}
						} catch (Exception e) {
							LOG.error("An exception occured when looking up VenCitySessionEJBBean:" + e.getMessage());
							e.printStackTrace();
							throw new EJBException("An exception occured when looking up VenCitySessionEJBBean:");
						}
					}
				}
				*/
				// States need to be synchronized
				/*
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "ORderServiceImpl::synchronizeReferenceData::States need to be synchronized");
				if (next instanceof VenState) {
					if (((VenState) next).getStateCode() != null) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "Synchronizing VenState... :" + ((VenState) next).getStateCode());
							List<VenState> stateList = venStateDAO.findByStateCode(((VenState) next).getStateCode());
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::stateList size = "
									+ stateList.size());
							if (stateList == null || stateList.isEmpty()) {
								//VenState state = (VenState) stateHome.persistVenState((VenState) next);
								VenState state = venStateDAO.save((VenState) next);
								retVal.add(state);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::successfully added state into retVal");
							} else {
								VenState state = stateList.get(0);
								em.detach(state);
								retVal.add(state);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "ORderServiceImpl::synchronizeReferenceData::successfully added state into retVal");
							}
					}
				}
				*/
				// Countries need to be synchronized
				/*
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "OrderServiceImpl::synchronizeReferenceData::Countries need to be synchronized");
				if (next instanceof VenCountry) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::synchronizeReferenceData::next instanceof VenCountry");
					if (((VenCountry) next).getCountryCode() != null) {
							CommonUtil.logDebug(this.getClass().getCanonicalName(), "Synchronizing VenCountry... :" + ((VenCountry) next).getCountryCode());
							List<VenCountry> countryList = venCountryDAO.findByCountryCode(((VenCountry) next).getCountryCode());
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::countryList size = "
									+ countryList.size());
							if (countryList == null || countryList.isEmpty()) {
								//VenCountry country = (VenCountry) countryHome.persistVenCountry((VenCountry) next);
								VenCountry country = venCountryDAO.save((VenCountry) next);
								retVal.add(country);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::successfully added country into retVal");
							} else {
								VenCountry country = countryList.get(0);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "ORderServiceImpl::synchronizeReferenceData::country is detached");
								retVal.add(country);
								CommonUtil.logDebug(this.getClass().getCanonicalName()
										, "OrderServiceImpl::synchronizeReferenceData::successfully added country into retVal");
							}
					}
				}
				*/
				// Address types need to be restricted
				/*
				if (next instanceof VenAddressType) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "OrderServiceImpl::synchronizeReferenceData::next instanceof VenAddressType");
					if (((VenAddressType) next).getAddressTypeId() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName(), "Restricting VenAddressType... :" + ((VenAddressType) next).getAddressTypeId());
						VenAddressType venAddressType = venAddressTypeDAO.findOne(((VenAddressType) next).getAddressTypeId());
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "OrderServiceImpl::synchronizeReferenceData::venAddressType detached");
						//if (addressTypeList == null || addressTypeList.isEmpty()) {
						if (venAddressType == null) {
							VenAddressType addressType = venAddressTypeDAO.save((VenAddressType) next);
							retVal.add(addressType);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "OrderServiceImpl::synchronizeReferenceData::successfully added addressType into retVal");
						} else {
							retVal.add(venAddressType);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "ORderserviceImpl::synchronizeReferenceData::successfully added venAddressType into retVal");
						}
					}
				}
				*/							
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeReferenceData::returning retVal with size = " + retVal.size());
		return retVal;
	}	
	
	/**
	 * Synchronizes the data for the direct VenAddress references
	 * 
	 * @param venAddress
	 * @return the synchronized data object
	 */
	/*
	private VenAddress synchronizeVenAddressReferenceData(VenAddress venAddress) throws VeniceInternalException {
		List<Object> references = new ArrayList<Object>();
		references.add(venAddress.getVenCity());
		references.add(venAddress.getVenCountry());
		references.add(venAddress.getVenState());

		// Synchronize the data references
		references = this.synchronizeReferenceData(references);

		// Push the keys back into the record
		Iterator<Object> referencesIterator = references.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenCity) {
				venAddress.setVenCity((VenCity) next);
			} else if (next instanceof VenCountry) {
				venAddress.setVenCountry((VenCountry) next);
			} else if (next instanceof VenState) {
				venAddress.setVenState((VenState) next);
			}
		}
		return venAddress;
	}
	*/	
	
	/**
	 * Synchronizes the data for the direct VenPartyAddress references
	 * 
	 * @param venPartyAddress
	 * @return
	 */
	private VenPartyAddress synchronizeVenPartyAddressReferenceData(VenPartyAddress venPartyAddress)
	 throws VeniceInternalException {
		List<Object> references = new ArrayList<Object>();
		references.add(venPartyAddress.getVenAddress());
		references.add(venPartyAddress.getVenAddressType());

		// Synchronize the data references
		references = this.synchronizeReferenceData(references);

		// Push the keys back into the record
		Iterator<Object> referencesIterator = references.iterator();
		while (referencesIterator.hasNext()) {
			Object next = referencesIterator.next();
			if (next instanceof VenAddress) {
				venPartyAddress.setVenAddress((VenAddress) next);
				VenPartyAddressPK pk = new VenPartyAddressPK();
				pk.setAddressId(((VenAddress) next).getAddressId());
				venPartyAddress.setId(pk);
			} else if (next instanceof VenPartyType) {
				venPartyAddress.setVenAddressType((VenAddressType) next);
			}
		}
		return venPartyAddress;
	}	
	
	/**
	 * Persists a list of addresses.
	 * 
	 * @param venAddressList
	 * @return
	 * @throws InvalidOrderException 
	 */
	/*
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	List<VenAddress> persistAddressList(List<VenAddress> venAddressList) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistAddressList::BEGIN, venAddressList = " + venAddressList);
		List<VenAddress> newVenAddressList = new ArrayList<VenAddress>();
		Iterator<VenAddress> i = venAddressList.iterator();
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistAddressList::venAddressList="
				+ venAddressList);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistAddressList::venAddressList size="
				+ venAddressList.size());
		while (i.hasNext()) {
			VenAddress newAddress = this.persistAddress(i.next());
			//VenAddress newAddressCloned = cloner.deepClone(newAddress);
			//newVenAddressList.add(newAddressCloned);
			newVenAddressList.add(newAddress);
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistAddressList::returning newVenAddressList="
				+ newVenAddressList);
		return newVenAddressList;
	}
	*/	
	
	/**
	 * updateAddressList - compares the existing address list with the new address list,
	 * writes any new addresses to the database and returns the updated address list.
	 * 
	 * @param existingVenAddressList
	 * @param newVenAddressList
	 * @return the updated address list
	 * @throws InvalidOrderException 
	 */
	/*
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	List<VenAddress> updateAddressList(List<VenAddress> existingVenAddressList, List<VenAddress> newVenAddressList) throws VeniceInternalException{
		List<VenAddress> updatedVenAddressList = new ArrayList<VenAddress>();
		List<VenAddress> persistVenAddressList = new ArrayList<VenAddress>();
		VenAddress tempAddress = new VenAddress();
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "updateAddressList::existingVenAddressList = "
				+ existingVenAddressList);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "updateAddressList::existingVenAddressList size = "
				+ existingVenAddressList.size());		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "updateAddressList::newVenAddressList size = "
				+ newVenAddressList.size());
		Boolean isAddressEqual=false;
		for(VenAddress newVenAddress:newVenAddressList){
			for(VenAddress existingVenAddress:existingVenAddressList){	
				
				//VenAddress existingVenAddressCloned = cloner.deepClone(existingVenAddress);
				
				if(((existingVenAddress.getKecamatan() == null && newVenAddress.getKecamatan() == null) || (existingVenAddress.getKecamatan()==null?"":existingVenAddress.getKecamatan().trim()).equalsIgnoreCase(newVenAddress.getKecamatan()==null?"":newVenAddress.getKecamatan().trim()))
						&& ((existingVenAddress.getKelurahan() == null && newVenAddress.getKelurahan() == null) || (existingVenAddress.getKelurahan()==null?"":existingVenAddress.getKelurahan().trim()).equalsIgnoreCase(newVenAddress.getKelurahan()==null?"":newVenAddress.getKelurahan().trim()))
						&& ((existingVenAddress.getPostalCode() == null && newVenAddress.getPostalCode() == null) || (existingVenAddress.getPostalCode()==null?"":existingVenAddress.getPostalCode().trim()).equalsIgnoreCase(newVenAddress.getPostalCode()==null?"":newVenAddress.getPostalCode().trim()))
						&& ((existingVenAddress.getStreetAddress1() == null && newVenAddress.getStreetAddress1() == null) || (existingVenAddress.getStreetAddress1()==null?"":existingVenAddress.getStreetAddress1().trim()).equalsIgnoreCase(newVenAddress.getStreetAddress1()==null?"":newVenAddress.getStreetAddress1().trim()))
						&& ((existingVenAddress.getStreetAddress2() == null && newVenAddress.getStreetAddress2() == null) || (existingVenAddress.getStreetAddress2()==null?"":existingVenAddress.getStreetAddress2().trim()).equalsIgnoreCase(newVenAddress.getStreetAddress2()==null?"":newVenAddress.getStreetAddress2().trim()))
						&& ((existingVenAddress.getVenCity() == null && newVenAddress.getVenCity() == null) || ((existingVenAddress.getVenCity()!=null?existingVenAddress.getVenCity().getCityCode():null)==null?"":existingVenAddress.getVenCity().getCityCode().trim()).equalsIgnoreCase((newVenAddress.getVenCity()!=null?newVenAddress.getVenCity().getCityCode():null)==null?"":newVenAddress.getVenCity().getCityCode().trim()))
						&& ((existingVenAddress.getVenCountry() == null && newVenAddress.getVenCountry() == null) || (existingVenAddress.getVenCountry().getCountryCode()==null?"":existingVenAddress.getVenCountry().getCountryCode().trim()).equalsIgnoreCase(newVenAddress.getVenCountry().getCountryCode()==null?"":newVenAddress.getVenCountry().getCountryCode().trim()))
						&& ((existingVenAddress.getVenState() == null && newVenAddress.getVenState() == null) || ((existingVenAddress.getVenState()!=null?existingVenAddress.getVenState().getStateCode():null)==null?"":existingVenAddress.getVenState().getStateCode().trim()).equalsIgnoreCase((newVenAddress.getVenState()!=null?newVenAddress.getVenState().getStateCode():null)==null?"":newVenAddress.getVenState().getStateCode().trim()))){
					
					//The address is assumed to be equal, not that the equals() 
					//operation can't be used because it is implemented by 
					//JPA on the primary key. Add it to the list
					 
					isAddressEqual=true;
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "updateAddressList::party address equal with existing.");
					//updatedVenAddressList.add(existingVenAddressCloned);	
					updatedVenAddressList.add(existingVenAddress);
					break;
				}else{
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "updateAddressList::party address NOT equal with existing.");
					isAddressEqual=false;
					//tempAddress=existingVenAddressCloned;
					tempAddress = existingVenAddress;
				}
			}
			if(isAddressEqual==false){
				
				//The address is a new address so it needs to be persisted
				 
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "updateAddressList::party address is new address.");
				newVenAddress.setVenPartyAddresses(tempAddress.getVenPartyAddresses());
				//VenAddress newVenAddressCloned = cloner.deepClone(newVenAddress);
				//persistVenAddressList.add(newVenAddressCloned);
				persistVenAddressList.add(newVenAddress);
			}
		}	
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "updateAddressList::trying to update addressList");
		
		//Persist any addresses that are new
		
		//if(!persistVenAddressList.isEmpty()){
		if (persistVenAddressList != null && persistVenAddressList.size() > 0) {
			persistVenAddressList = this.persistAddressList(persistVenAddressList);
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "updatedAddressList::persistVenAddressList="
					+ persistVenAddressList);
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "updatedAddressList::persistVenAddressList size="
					+ persistVenAddressList.size());
			
			//Add the persisted addresses to the new list
			//List<VenAddress> persistVenAddressListCloned = cloner.deepClone(persistVenAddressList);
			//updatedVenAddressList.addAll(persistVenAddressListCloned);
			updatedVenAddressList.addAll(persistVenAddressList);
		}
		//List<VenAddress> updatedVenAddressListCloned = cloner.deepClone(updatedVenAddressList);
		//return updatedVenAddressListCloned;
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "updatedAddressList::updatedVenAddressList="
				+ updatedVenAddressList);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "updatedAddressList::updatedVenAddressList size="
				+ updatedVenAddressList.size());
		return updatedVenAddressList;
	}	
	*/	
	
	@Override
	public VenOrder retrieveExistingOrder(String wcsOrderId) {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::retrieveExistingOrder::BEGIN");
		VenOrder venOrder = venOrderDAO.findByWcsOrderId(wcsOrderId);
		if (venOrder != null) return venOrder;
		
		return null;
	}	
	
	/**
	 * Retreives an existing party from the cache along with 
	 * contact and address details
	 * 
	 * @param fullOrLegalName
	 * @return the party if it exists else null
	 */
	/*
	private VenParty retrieveExistingParty(String custUserName) {
		String escapeChar = "";

		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::retrieveExistingParty::custUserName = " + custUserName);
		List<VenCustomer> customerList = venCustomerDAO.findByCustomerName(JPQLStringEscapeUtility.escapeJPQLStringData(custUserName, escapeChar));
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::retrieveExistingParty::customerList size = " + customerList.size());
		if (customerList != null && (customerList.size() > 0)) {
			VenParty party = customerList.get(0).getVenParty();
			
			//Fetch the list of contact details for the party
			List<VenContactDetail> venContactDetailList = venContactDetailDAO.findByParty(party);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "OrderServiceImpl::retrieveExistingParty::Total existing vencontactdetail => " + venContactDetailList.size());
			party.setVenContactDetails(venContactDetailList);

			//Fetch the list of party addresses for the party
			//List<VenPartyAddress> venPartyAddressList = partyAddressHome.queryByRange("select o from VenPartyAddress o where o.venParty.partyId = " + party.getPartyId(), 0, 0);
			List<VenPartyAddress> venPartyAddressList = venPartyAddressDAO.findByVenParty(party);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "OrderServiceImpl::retrieveExistingParty::Total existing VenPartyAddress => " + venPartyAddressList.size());
			//em.detach(venPartyAddressList);
			party.setVenPartyAddresses(venPartyAddressList);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "OrderServiceImpl::retrieveExistingParty::successfully set venPartyAddressList into party");

			return party;
		} else {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "OrderServiceImpl::retrieveExistingParty::Party cannot be found, returning null");
			return null;
		}
	}
	*/	
	
	@Override
	public boolean isOrderExist(String wcsOrderId) {
		if (retrieveExistingOrder(wcsOrderId) != null) return true;
		
		return false;
	}
	
	/*
	private boolean isItemWCSExistInDB(String wcsOrderItemId) {
		VenOrderItem venOrderItem = venOrderItemDAO.findByWcsOrderItemId(wcsOrderItemId);
		if (venOrderItem != null) return true;
		return false;
	}	
	*/
	
	@Override
	public List<VenOrder> synchronizeVenOrderReferences(List<VenOrder> orderReferences) 
	   throws VeniceInternalException {

		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderReferences::BEGIN, orderReferences = " + orderReferences);
		
		List<VenOrder> synchronizedOrderReferences = new ArrayList<VenOrder>();
		
		for (VenOrder order : orderReferences) {
			if (order.getWcsOrderId() != null) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "synchronizeVenOrderReferences::Restricting VenOrder... :" + order.getWcsOrderId());
				VenOrder venOrder = venOrderDAO.findByWcsOrderId(order.getWcsOrderId());
				if (venOrder == null) {
					throw CommonUtil.logAndReturnException(new OrderNotFoundException(
							"synchronizeVenOrderReferences::Order does not exist"
							, VeniceExceptionConstants.VEN_EX_000020)
					  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
				} else {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenOrderReferences::adding venOrder into synchronizedOrderReferences");
					synchronizedOrderReferences.add(venOrder);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenOrderReferences::successfully added venOrder into synchronizedOrderReferences");
				}
			}			
		} //end of 'for'
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderReferences::returning synchronizedOrderReferences = " 
				  + synchronizedOrderReferences.size());
		
		return synchronizedOrderReferences;
	}

}