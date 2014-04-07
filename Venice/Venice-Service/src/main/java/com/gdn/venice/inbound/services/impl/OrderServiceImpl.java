package com.gdn.venice.inbound.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenOrderDAO;
import com.gdn.venice.exception.CSOrderNotApprovedException;
import com.gdn.venice.exception.CannotPersistCustomerException;
import com.gdn.venice.exception.DuplicateWCSOrderIDException;
import com.gdn.venice.exception.InvalidOrderException;
import com.gdn.venice.exception.InvalidOrderItemException;
import com.gdn.venice.exception.MerchantPartySynchFailedException;
import com.gdn.venice.exception.OrderNotFoundException;
import com.gdn.venice.exception.PaymentProcessorException;
import com.gdn.venice.exception.VAOrderNotApprovedException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.factory.VeninboundFactory;
import com.gdn.venice.finance.services.FinArFundsInReconRecordService;
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
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenContactDetail;
import com.gdn.venice.persistence.VenCustomer;
import com.gdn.venice.persistence.VenMerchant;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderAddress;
import com.gdn.venice.persistence.VenOrderBlockingSource;
import com.gdn.venice.persistence.VenOrderContactDetail;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderStatus;
import com.gdn.venice.persistence.VenParty;
import com.gdn.venice.persistence.VenPartyType;
import com.gdn.venice.util.CommonUtil;
import com.gdn.venice.util.OrderUtil;
import com.gdn.venice.util.VeniceConstants;
import com.gdn.venice.validator.factory.OrderCreationValidatorFactory;

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
	
	@Autowired
	VenOrderDAO venOrderDAO;
	
	@Autowired
	private AddressService addressService;
	
	@Autowired
	private FinArFundsInReconRecordService finArFundsInReconRecordService;
	
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
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public Boolean createOrder(Order order) throws VeniceInternalException {
		OrderUtil.checkOrder(order, VeninboundFactory.getOrderValidator(new OrderCreationValidatorFactory()));
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "OrderServiceImpl::createOrder::all basic validation passed");
				
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
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::processing merchant");
		
		Boolean merchantProcess = merchantService.processMerchant(merchantProduct, orderItems);
		
		if (!merchantProcess) {
			CommonUtil.logAndReturnException(new MerchantPartySynchFailedException("Cannot synchronize VenMerchant and VenParty!"
					, VeniceExceptionConstants.VEN_EX_130006), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::VenMerchant and VenParty have been successfully synchronized");

		// If the order is RMA do nothing with payments because there are none
		try {
			if (!venOrder.getRmaFlag()) {
				/*
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "createOrder::rma flag false, remove existing payment");
				 */
				// Remove any existing order payment allocations that were allocated at VA stage
				//orderPaymentAllocationService.removeOrderPaymentAllocationList(venOrder);
				/*
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "createOrder::done remove existing payment");
				 */

				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "createOrder::processing payment");
				orderPaymentService.processPayment(order, venOrder);
			}
		} catch (Exception e) {
			CommonUtil.logError(this.getClass().getCanonicalName(), e);
			e.printStackTrace();
			CommonUtil.logAndReturnException(new PaymentProcessorException("Cannot process payment!"
					, VeniceExceptionConstants.VEN_EX_400004), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}

		return Boolean.TRUE;
	}
	
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public Boolean createOrderVAPayment(Order order) throws VeniceInternalException {
    	return Boolean.TRUE;
    }	
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrder persistOrder(Boolean vaPaymentExists, Boolean csPaymentExists, VenOrder venOrder) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistOrder::vaPaymentExists: "+vaPaymentExists);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistOrder::csPaymentExists: "+csPaymentExists);
		if (venOrder != null) {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::Persisting VenOrder... :" + venOrder.getWcsOrderId());				
				
				// backup the order items before persisting as it will be detached				
				//List<VenOrderItem> venOrderItemList = venOrder.getVenOrderItems();
				List<VenOrderItem> venOrderItemList = new ArrayList<VenOrderItem>(venOrder.getVenOrderItems());
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::venOrderItem merchantProduct WCS Product SKU = " 
				        + venOrderItemList.get(0).getVenMerchantProduct().getWcsProductSku());
				
				// Detach the order items prior to persisting the order.
				venOrder.setVenOrderItems(null);
				
				// Detach the order payment allocations
				// Note that these will be allocated at 100% of the order price later when processing payments
				venOrder.setVenOrderPaymentAllocations(null);

				// Detach the transaction fees list
				venOrder.setVenTransactionFees(null);
				// Detach the customer first then persist and re-attach
				VenCustomer customer = venOrder.getVenCustomer();
				//em.detach(customer);
				venOrder.setVenCustomer(null);

				if(venOrder.getVenOrderBlockingSource().getBlockingSourceId()== null 
						&& venOrder.getVenOrderBlockingSource().getBlockingSourceDesc()==null) {
					venOrder.setVenOrderBlockingSource(null);
				}
									
				// Persist the customer
				
				try {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::trying to persist venCustomer; WCS Customer ID = " + customer.getWcsCustomerId()
							+ ", Customer User Name = " + customer.getCustomerUserName());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::venCustomer's Party = " + customer.getVenParty());
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::venCustomer's Party Type = " 
					        + (customer.getVenParty() != null ? customer.getVenParty().getVenPartyType() : null));
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::venCustomer's Party Legal Name = " 
							+ (customer.getVenParty() != null ? customer.getVenParty().getFullOrLegalName() : null));
					//VenCustomer persistedCustomer = customerService.persistCustomer(customer);
					customer = customerService.persistCustomer(customer);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::venCustomer has been successfully persisted");					
					//venOrder.setVenCustomer(persistedCustomer);
					venOrder.setVenCustomer(customer);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::customer has just been reattached to venOrder");
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
				
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrder::successfully persisted orderAddress");				
				
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
					//venOrder = venOrderDAO.save(venOrder); no need to explicitly call save, since it would be handling automatically by the JPA transaction
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
				orderStatusHistoryService.createOrderStatusHistory(venOrder, venOrder.getVenOrderStatus()); //here, venOrder has already in attached mode
				
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
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "venOrder is attached ? " + (em.contains(venOrder)));					
					List<VenOrderItem> venOrderItemListPersisted 
					   = orderItemService.persistOrderItemList(venOrder, venOrderItemList); //venorder = attached, venOrderItemList = detached
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
					orderContactDetailService.persistVenOrderContactDetails(venOrderContactDetailList);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrder::venOrderContactDetails successfully persisted");
				}			
		} // end if venOrder != null
		
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
				"synchronizeVenOrderReferenceData::BEGIN, venOrder=" + venOrder);
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
				List<VenOrder> orderStatusVenOrders = orderStatus.getVenOrders();
				orderStatusVenOrders.add(venOrder);
				orderStatus.setVenOrders(orderStatusVenOrders);
				venOrder.setVenOrderStatus(orderStatus);
			}			
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderReferenceData::EOF, returning venOrder = " + venOrder);
		return venOrder;
	}	
		
	@Override
	public VenOrder retrieveExistingOrder(String wcsOrderId) {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "OrderServiceImpl::retrieveExistingOrder::BEGIN");
		VenOrder venOrder = venOrderDAO.findByWcsOrderId(wcsOrderId);
		if (venOrder != null) return venOrder;
		
		return null;
	}	
	
	@Override
	public boolean isOrderExist(String wcsOrderId) {
		if (retrieveExistingOrder(wcsOrderId) != null) return true;
		
		return false;
	}
	
	@Override
	public VenOrder synchronizeVenOrder(VenOrder venOrder) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrder::BEGIN,venOrder = " + venOrder);
		VenOrder synchOrder = venOrder;
		if (venOrder != null && venOrder.getWcsOrderId() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrder::wcsOrderId = " + venOrder.getWcsOrderId());
			synchOrder = venOrderDAO.findByWcsOrderId(venOrder.getWcsOrderId());
			if (synchOrder == null) {
				throw CommonUtil.logAndReturnException(new OrderNotFoundException(
						"VenOrder does not exist!"
						, VeniceExceptionConstants.VEN_EX_000020)
				  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);				
			}
			return synchOrder;
		}
		return synchOrder;
	}
	
	@Override
	public List<VenOrder> synchronizeVenOrderReferences(List<VenOrder> orderReferences) 
	   throws VeniceInternalException {

		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderReferences::BEGIN, orderReferences = " + orderReferences);
		
		List<VenOrder> synchronizedOrderReferences = new ArrayList<VenOrder>();
		
		try {
			for (VenOrder order : orderReferences) {
				synchronizedOrderReferences.add(synchronizeVenOrder(order));
			} //end of 'for'
		} catch (VeniceInternalException e) {
			CommonUtil.logAndReturnException(e, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		} catch (Exception e) {
			throw CommonUtil.logAndReturnException(new OrderNotFoundException(
					"VenOrder does not exist!"
					, VeniceExceptionConstants.VEN_EX_000020)
			  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);			
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderReferences::returning synchronizedOrderReferences = " 
				  + synchronizedOrderReferences.size());
		
		return synchronizedOrderReferences;
	}

}