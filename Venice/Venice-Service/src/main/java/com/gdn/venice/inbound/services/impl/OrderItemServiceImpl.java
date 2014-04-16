package com.gdn.venice.inbound.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenContactDetailDAO;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.exception.CannotPersistOrderItemException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.AddressService;
import com.gdn.venice.inbound.services.ContactDetailService;
import com.gdn.venice.inbound.services.MerchantProductService;
import com.gdn.venice.inbound.services.OrderItemAddressService;
import com.gdn.venice.inbound.services.OrderItemAdjustmentService;
import com.gdn.venice.inbound.services.OrderItemContactDetailService;
import com.gdn.venice.inbound.services.OrderItemService;
import com.gdn.venice.inbound.services.OrderItemStatusHistoryService;
import com.gdn.venice.inbound.services.OrderStatusHistoryService;
import com.gdn.venice.inbound.services.OrderStatusService;
import com.gdn.venice.inbound.services.RecipientService;
import com.gdn.venice.persistence.LogLogisticService;
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenContactDetail;
import com.gdn.venice.persistence.VenMerchantProduct;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderItemAddress;
import com.gdn.venice.persistence.VenOrderItemAdjustment;
import com.gdn.venice.persistence.VenOrderItemContactDetail;
import com.gdn.venice.persistence.VenOrderStatus;
import com.gdn.venice.persistence.VenRecipient;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderItemServiceImpl implements OrderItemService {
	
	@Autowired	
	private VenOrderItemDAO venOrderItemDAO;	
	
	@Autowired
	private AddressService addressService;
	
	@Autowired
	private com.gdn.venice.inbound.services.LogLogisticService logLogisticService;
	
	@Autowired
	private MerchantProductService merchantProductService;
	
	@Autowired
	private OrderItemAddressService orderItemAddressService;
	
	@Autowired
	private OrderItemContactDetailService orderItemContactDetailService;
	
	@Autowired
	private OrderStatusService orderStatusService;
	
	@Autowired
	private OrderStatusHistoryService orderStatusHistoryService;
	
	@Autowired
	private OrderItemAdjustmentService orderItemAdjustmentService;
	
	@Autowired
	private OrderItemStatusHistoryService orderItemStatusHistoryService;
	
	@Autowired
	private RecipientService recipientService;
	
	@Autowired
	private ContactDetailService contactDetailService;
	
	@Autowired
	private VenContactDetailDAO venContactDetailDAO;
	
	@PersistenceContext
	private EntityManager em;

	/**
	 * This method is used to check whether the OrderItem has already existed or not 
	 * in the database (search by it's wcsOrderItemId).
	 * It will be returning "true" if the particular OrderItem is exist 
	 * otherwise it will return "false".
	 * 
	 * @param wcsOrderItemId 
	 * @return boolean
	 */
	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
	public boolean isItemWCSExistInDB(String wcsOrderItemId) {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "isItemWCSExistInDB::BEGIN");
		VenOrderItem venOrderItem = venOrderItemDAO.findByWcsOrderItemId(wcsOrderItemId);
		if (venOrderItem != null) return true;
		return false;
	}

	/**
	 * Persists a list of order items using the session tier.
	 * 
	 * @param venOrderLineList
	 * @return the persisted object
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenOrderItem> persistOrderItemList(VenOrder venOrder,
			List<VenOrderItem> venOrderItemList) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistOrderItemList::BEGIN, venOrder=" + venOrder + ", venOrderItemList=" + venOrderItemList);
		List<VenOrderItem> newVenOrderItemList = new ArrayList<VenOrderItem>();
		if (venOrderItemList != null && (!(venOrderItemList.isEmpty()))) {
			try {
				for (VenOrderItem venOrderItem : venOrderItemList) {
					VenOrderItem synchOrderItem = synchronizeVenOrderItemReferenceData(venOrderItem);
					
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistOrderItemlist::venOrder = " + venOrder);
					// Attach the order
					synchOrderItem.setVenOrder(venOrder);
					
					// Detach the marginPromo before persisting
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistOrderItemList::venOrderItem.venOrderItemAdjustments = "
							+ venOrderItem.getVenOrderItemAdjustments() + ",  members=" + (venOrderItem.getVenOrderItemAdjustments() != null
							? venOrderItem.getVenOrderItemAdjustments().size() : 0));
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistOrderItemList::synchOrderItem.venOrderItemAdjustments = " 
							+ synchOrderItem.getVenOrderItemAdjustments() + ", members=" + (synchOrderItem.getVenOrderItemAdjustments() != null
							? synchOrderItem.getVenOrderItemAdjustments().size() : 0));
					List<VenOrderItemAdjustment> venOrderItemAdjustments = new ArrayList<VenOrderItemAdjustment>(synchOrderItem.getVenOrderItemAdjustments());
					synchOrderItem.setVenOrderItemAdjustments(null);
					
					// Detach the pickup instructions before persisting
					//CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistOrderItemList::logMerchantPickupInstructions = "
							//+ synchOrderItem.getLogMerchantPickupInstructions());
					//List<LogMerchantPickupInstruction> logMerchantPickupInstructions = new ArrayList<LogMerchantPickupInstruction>(synchOrderItem.getLogMerchantPickupInstructions());
					synchOrderItem.setLogMerchantPickupInstructions(null);
					
					// Persist the shipping Address
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::going to persist shipping address");
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::shipping address = " + venOrderItem.getVenAddress().getStreetAddress1());
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::address ID = " + venOrderItem.getVenAddress().getAddressId());
					VenAddress persistedAddress = addressService.persistAddress(venOrderItem.getVenAddress());
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::persistedAddress ID = " + persistedAddress.getAddressId());
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistOrderItemList::persistedAddress=" + persistedAddress
							+ ",hashCode = " + persistedAddress.hashCode());
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistOrderItemList::synchOrderItem.venAddress=" + synchOrderItem.getVenAddress()
							+ ",hashCode = " + synchOrderItem.getVenAddress().hashCode());
					synchOrderItem.setVenAddress(persistedAddress);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::orderItem venAddress has been successfully persisted");						
					

					// Persist the recipient	
					//VenRecipient persistedRecipient = null;
					if (venOrder.getVenCustomer().getVenParty().equals(synchOrderItem.getVenRecipient().getVenParty())) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderItemList::Customer is Recipient");
						/*
						persistedRecipient = synchOrderItem.getVenRecipient();
						persistedRecipient.setVenParty(venOrder.getVenCustomer().getVenParty());
						*/
						synchOrderItem.getVenRecipient().setVenParty(venOrder.getVenCustomer().getVenParty());
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderItemList::Recipient is different with Customer");
					}
					
				    VenRecipient persistedRecipient = recipientService.persistRecipient(synchOrderItem.getVenRecipient());
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::persistedRecipient ID = " + persistedRecipient.getRecipientId());					
					synchOrderItem.setVenRecipient(persistedRecipient);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::successfully persist the recipient in main processing loop");					
					
					// Adjust the shipping weight because it comes across as the
					// product shipping weight
					synchOrderItem.setShippingWeight(new BigDecimal(synchOrderItem.getShippingWeight().doubleValue() * synchOrderItem.getQuantity()));
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::successfully set shipping weight in main processing loop");
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::check venMerchantProduct (SKU = "
							+ synchOrderItem.getVenMerchantProduct().getWcsProductSku() + ") before persisting into DB");

					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::is synchOrderItem attached ? " + (em.contains(synchOrderItem)));
					
					// Persist the object						
					VenOrderItem persistedOrderItem = null;
					if (!em.contains(synchOrderItem)) {
						//orderItem is in detach mode, hence should call save explicitly as shown below
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderItemList::calling save on VenOrderItem explicitly");
						//synchOrderItem = venOrderItemDAO.save(synchOrderItem);
						persistedOrderItem = venOrderItemDAO.save(synchOrderItem);
					}					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::successfully persisted orderItem into DB");
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::is synchOrderItem attached ? " + (em.contains(synchOrderItem)));					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::is persistedOrderItem attached ? " + (em.contains(persistedOrderItem)));					
					
					/*
					 * Tally Order Item with recipient address and contact details
					 * defined in the ref tables VenOrderItemAddress and VenOrderItemContactDetail
					 */
										
					List<VenOrderItemContactDetail> venOrderItemContactDetailList = new ArrayList<VenOrderItemContactDetail>();
					
					VenOrderItemAddress venOrderItemAddress = new VenOrderItemAddress();
					
					//venOrderItemAddress.setVenOrderItem(synchOrderItem);
					venOrderItemAddress.setVenOrderItem(persistedOrderItem);
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "persistOrderItemList::setting orderItemAddress = "  + synchOrderItem.getVenAddress());
					venOrderItemAddress.setVenAddress(synchOrderItem.getVenAddress());

					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::persisting  VenOrderItemAddress = " + venOrderItemAddress);
					// persist VenOrderItemAddress
					venOrderItemAddress = orderItemAddressService.persist(venOrderItemAddress);
					
					List<VenContactDetail> venContactDetailList = synchOrderItem.getVenRecipient().getVenParty().getVenContactDetails();
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::venContactDetailList = " + venContactDetailList);
					
					for (VenContactDetail venContactDetail : venContactDetailList){
						//List<VenContactDetail> contactDetailList = venContactDetailDAO.findByContactDetail(venContactDetail.getContactDetail());
						
						//venContactDetail = syncContactDetail(venContactDetail);
						venContactDetail = contactDetailService.persistContactDetail(venContactDetail, persistedOrderItem.getVenRecipient().getVenParty());
						
						VenOrderItemContactDetail venOrderItemContactDetail = new VenOrderItemContactDetail();
						//venOrderItemContactDetail.setVenOrderItem(synchOrderItem);
						venOrderItemContactDetail.setVenOrderItem(persistedOrderItem);
						venOrderItemContactDetail.setVenContactDetail(venContactDetail);
						
						venOrderItemContactDetailList.add(venOrderItemContactDetail);
						
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderItemList::venContactDetail = " + venContactDetail.getContactDetailId());
						
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderItemList::venContactDetail = " + venContactDetail.getContactDetail());
						
					}
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::Total VenOrderItemContactDetail to be persisted => " 
					        + venOrderItemContactDetailList.size());
					orderItemContactDetailService.persist(venOrderItemContactDetailList);
					
					//add order item history
					orderItemStatusHistoryService.createOrderItemStatusHistory(synchOrderItem, venOrder.getVenOrderStatus());
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::successfully created OrderItemStatusHistory");

					// Persist the marginPromo
					//synchOrderItem.setVenOrderItemAdjustments(orderItemAdjustmentService.persistOrderItemAdjustmentList(synchOrderItem, venOrderItemAdjustments));
					synchOrderItem.setVenOrderItemAdjustments(
							orderItemAdjustmentService.persistOrderItemAdjustmentList(persistedOrderItem, venOrderItemAdjustments));
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::adding orderItem into newVenOrderItemList");
					//newVenOrderItemList.add(synchOrderItem);
					newVenOrderItemList.add(persistedOrderItem);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::orderItem added into newVenOrderItemList");					
					
				}
				
				//Main processing loop
				/*
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrderItemList::Main processing loop");
			
				i = venOrderItemList.iterator();
				while (i.hasNext()) {
					VenOrderItem orderItem = i.next();

					// Attach the order
					orderItem.setVenOrder(venOrder);

					// Detach the marginPromo before persisting
					List<VenOrderItemAdjustment> venOrderItemAdjustmentList = (List<VenOrderItemAdjustment>)orderItem.getVenOrderItemAdjustments();
					orderItem.setVenOrderItemAdjustments(null);

					// Detach the pickup instructions before persisting
					orderItem.setLogMerchantPickupInstructions(null);

					// Persist the shipping address
					//VenAddress persistedAddress = addressService.persistAddress(orderItem.getVenAddress()); replace with following line as work around solution related to possible non-thread safe access session
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::street address = " + (venOrder.getVenCustomer().getVenParty().getVenPartyAddresses()
									.get(0).getVenAddress().getStreetAddress1()));
					VenAddress persistedAddress = venOrder.getVenCustomer().getVenParty().getVenPartyAddresses().get(0).getVenAddress();
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::persistedAddress ID = " + persistedAddress.getAddressId());
					orderItem.setVenAddress(persistedAddress);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::orderItem venAddress has been successfully persisted");

					// Persist the recipient	
					VenRecipient persistedRecipient = null;
					//if (venOrder.getVenCustomer().getVenParty().getFullOrLegalName().equalsIgnoreCase(orderItem.getVenRecipient().getVenParty().getFullOrLegalName())) {
					if (venOrder.getVenCustomer().getVenParty().equals(orderItem.getVenRecipient().getVenParty())) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderItemList::Recipient equals with Customer");
						persistedRecipient = orderItem.getVenRecipient();
						persistedRecipient.setVenParty(venOrder.getVenCustomer().getVenParty());
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderItemList::Recipient is different with Customer");
					}
					
				    persistedRecipient = recipientService.persistRecipient(orderItem.getVenRecipient());
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::persistedRecipient ID = " + persistedRecipient.getRecipientId());					
					orderItem.setVenRecipient(persistedRecipient);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::successfully persist the recipient in main processing loop");					
					
					// Adjust the shipping weight because it comes across as the
					// product shipping weight
					orderItem.setShippingWeight(new BigDecimal(orderItem.getShippingWeight().doubleValue() * orderItem.getQuantity()));
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::successfully set shipping weight in main processing loop");
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::check venMerchantProduct (SKU = "
							+ orderItem.getVenMerchantProduct().getWcsProductSku() + ") before persisting into DB");

					// Persist the object						
					if (!em.contains(orderItem)) {
						//orderItem is in detach mode, hence should call save explicitly as shown below
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderItemList::calling save on VenOrderItem explicitly");
						orderItem = venOrderItemDAO.save(orderItem);
					}					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::successfully persisted orderItem into DB");						
					
					
					//Tally Order Item with recipient address and contact details
					//defined in the ref tables VenOrderItemAddress and VenOrderItemContactDetail
					 
										
					List<VenOrderItemContactDetail> venOrderItemContactDetailList = new ArrayList<VenOrderItemContactDetail>();
					
					VenOrderItemAddress venOrderItemAddress = new VenOrderItemAddress();
					
					venOrderItemAddress.setVenOrderItem(orderItem);
					venOrderItemAddress.setVenAddress(orderItem.getVenAddress());

					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::persisting  VenOrderItemAddress" );
					// persist VenOrderItemAddress
					orderItemAddressService.persist(venOrderItemAddress);
					
					List<VenContactDetail> venContactDetailList = orderItem.getVenRecipient().getVenParty().getVenContactDetails();
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::venContactDetailList = " + venContactDetailList);
					
					for (VenContactDetail venContactDetail : venContactDetailList){
						VenOrderItemContactDetail venOrderItemContactDetail = new VenOrderItemContactDetail();
						venOrderItemContactDetail.setVenOrderItem(orderItem);
						venOrderItemContactDetail.setVenContactDetail(venContactDetail);
						
						venOrderItemContactDetailList.add(venOrderItemContactDetail);
					}
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::Total VenOrderItemContactDetail to be persisted => " 
					        + venOrderItemContactDetailList.size());
					orderItemContactDetailService.persist(venOrderItemContactDetailList);
					
					//add order item history
					orderItemStatusHistoryService.createOrderItemStatusHistory(orderItem, venOrder.getVenOrderStatus());

					// Persist the marginPromo
					orderItem.setVenOrderItemAdjustments(orderItemAdjustmentService.persistOrderItemAdjustmentList(orderItem, venOrderItemAdjustmentList));
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::adding orderItem into newVenOrderItemList");
					newVenOrderItemList.add(orderItem);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderItemList::orderItem added into newVenOrderItemList");					
				}//end of while
				*/ 
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), e);
				throw CommonUtil.logAndReturnException(new CannotPersistOrderItemException(
						"An exception occured when persisting VenOrderItem"
						, VeniceExceptionConstants.VEN_EX_000021)
				  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistOrderItemList::EOM, returning newVenOrderItemList");
		return newVenOrderItemList;
	}

	/**
	 * Synchronizes the reference data for the direct VenOrderItem references
	 * 
	 * @param venOrderItem
	 * @return the synchronized data object
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrderItem synchronizeVenOrderItemReferenceData(
			VenOrderItem venOrderItem) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderItemReferenceData::BEGIN, venOrderItem=" + venOrderItem);
		
		//if (venOrderItem.getLogLogisticService() != null) {
			List<LogLogisticService> logLogisticServiceRefs = new ArrayList<LogLogisticService>();
			logLogisticServiceRefs.add(venOrderItem.getLogLogisticService());
			logLogisticServiceRefs = logLogisticService.synchronizeLogLogisticServiceReferences(logLogisticServiceRefs);
			for (LogLogisticService logisticService : logLogisticServiceRefs) {
				venOrderItem.setLogLogisticService(logisticService);
			}
		//}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderItemReferenceData::synchronizing VenMerchantProduct = "
				+ venOrderItem.getVenMerchantProduct());
		//if (venOrderItem.getVenMerchantProduct() != null) {
		    VenMerchantProduct venMerchantProduct = venOrderItem.getVenMerchantProduct();
		    VenMerchantProduct synchVenMerchantProduct = merchantProductService.synchronizeVenMerchantProductData(venMerchantProduct);
		    venOrderItem.setVenMerchantProduct(synchVenMerchantProduct);
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderItemReferenceData::VenMerchantProduct has been successfully synchronized");
		    
		    
		//}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderItemReferenceData::VenOrderItem venOrderStatus = " + venOrderItem.getVenOrderStatus());
		//if (venOrderItem.getVenOrderStatus() != null) {
						
		    VenOrderStatus venOrderStatus = venOrderItem.getVenOrderStatus();
		    VenOrderStatus synchOrderStatus = orderStatusService.synchronizeVenOrderStatusReferences(venOrderStatus);
		    venOrderItem.setVenOrderStatus(synchOrderStatus);
		    CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenorderItemReferenceData::synchronized venOrderStatus.statusId = " 
		    		+ venOrderItem.getVenOrderStatus().getOrderStatusId());		    
		    CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenorderItemReferenceData::synchronized venOrderStatus.statusCode = " 
		    		+ venOrderItem.getVenOrderStatus().getOrderStatusCode());
		//}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderItemReferenceData::EOM, returning venOrderItem = " + venOrderItem
				+ ", merchantProduct = " + venOrderItem.getVenMerchantProduct());
		return venOrderItem;
	}
}
