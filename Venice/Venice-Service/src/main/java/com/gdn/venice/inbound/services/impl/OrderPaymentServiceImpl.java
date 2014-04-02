package com.gdn.venice.inbound.services.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.integration.jaxb.Order;
import com.gdn.integration.jaxb.Payment;
import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VenCSPaymentStatusIDConstants;
import com.gdn.venice.constants.VenVAPaymentStatusIDConstants;
import com.gdn.venice.constants.VenWCSPaymentTypeConstants;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenOrderPaymentDAO;
import com.gdn.venice.exception.CannotPersistFinArFundsInReconRecordException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.finance.services.FinArFundsInReconRecordService;
import com.gdn.venice.inbound.services.AddressService;
import com.gdn.venice.inbound.services.BankService;
import com.gdn.venice.inbound.services.OrderPaymentAllocationService;
import com.gdn.venice.inbound.services.OrderPaymentService;
import com.gdn.venice.inbound.services.OrderService;
import com.gdn.venice.inbound.services.PaymentStatusService;
import com.gdn.venice.inbound.services.PaymentTypeService;
import com.gdn.venice.inbound.services.WcsPaymentTypeService;
import com.gdn.venice.persistence.FinApprovalStatus;
import com.gdn.venice.persistence.FinArFundsInActionApplied;
import com.gdn.venice.persistence.FinArFundsInReconRecord;
import com.gdn.venice.persistence.FinArReconResult;
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenBank;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPayment;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.persistence.VenOrderPaymentAllocationPK;
import com.gdn.venice.persistence.VenPaymentStatus;
import com.gdn.venice.persistence.VenPaymentType;
import com.gdn.venice.persistence.VenWcsPaymentType;
import com.gdn.venice.util.CommonUtil;
import com.gdn.venice.util.OrderUtil;
import com.gdn.venice.util.VeniceConstants;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderPaymentServiceImpl implements OrderPaymentService {
	
	@Autowired
	private VenOrderPaymentDAO venOrderPaymentDAO;
	
	@Autowired
	private AddressService addressService;
	
	@Autowired
	private BankService bankService;
	
	@Autowired
	private FinArFundsInReconRecordService finArFundsInReconRecordService;
	
	@Autowired
	private OrderPaymentAllocationService orderPaymentAllocationService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private PaymentTypeService paymentTypeService;
	
	@Autowired
	private PaymentStatusService paymentStatusService;
	
	@Autowired
	private WcsPaymentTypeService wcsPaymentTypeService;
	
	@PersistenceContext
	private EntityManager em;
	
	private Mapper mapper = DozerBeanMapperSingletonWrapper.getInstance();		

	@Override
	public List<VenOrderPayment> findByWcsPaymentId(String wcsPaymentId) {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "findByWcsPaymentId::wcsPaymentId=" + wcsPaymentId);
		return venOrderPaymentDAO.findByWcsPaymentId(wcsPaymentId);	
	}
	
	@Override
	public VenOrderPayment getVenOrderPayment(Payment payment) {
		if (payment == null) return null;
		
		List<VenOrderPayment> orderPayments = findByWcsPaymentId(payment.getPaymentId().getCode());
		
		return ((orderPayments != null && orderPayments.size() > 0)
				? orderPayments.get(0) : null);
	}
	
	@Override
	public boolean isPaymentExist(Payment payment) {

		return (getVenOrderPayment(payment) != null ? true : false);
	}
	
	@Override
	public boolean isPaymentApproved(Payment payment) {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "isPaymentApproved::payment = " + payment);
		
		if (!isPaymentExist(payment)) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "isPaymentApproved::payment not exist!");
			return false;
		}
		
		VenOrderPayment venOrderPayment = getVenOrderPayment(payment);
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "isPaymentApproved::venOrderPayment = " + venOrderPayment);
		
		if (venOrderPayment == null) return false;
		
		if (isVirtualAccountPayment(payment.getPaymentType())) {
			//VA Payment
			if (venOrderPayment.getVenPaymentStatus().getPaymentStatusId()
					!= VenVAPaymentStatusIDConstants.VEN_VA_PAYMENT_STATUS_ID_APPROVED.code()) {
				//VA Payment is not approved yet, thus throw an exception
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "isPaymentApproved::An order with an unapproved VA payment has been received");
				return false;
			}
		} else if (isCSPayment(payment.getPaymentType())) {
			//CS Payment
			if (venOrderPayment.getVenPaymentStatus().getPaymentStatusId()
					!= VenCSPaymentStatusIDConstants.VEN_CS_PAYMENT_STATUS_ID_APPROVED.code()) {
				//CS Payment is not approved yet, thus throw and exception
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "isPaymentApproved::An order with an unapproved CS payment has been received");
				return false;
			}
		}
		return true;
	}	

	@Override
	public boolean isVirtualAccountPayment(String paymentType) {
		if (paymentType == null) return false;
		return paymentType.trim().equalsIgnoreCase(
				VenWCSPaymentTypeConstants.VEN_WCS_PAYMENT_TYPE_VirtualAccount.desc());	
	}

	@Override
	public boolean isCSPayment(String paymentType) {
		if (paymentType == null) return false;
		return paymentType.trim().equalsIgnoreCase(
				VenWCSPaymentTypeConstants.VEN_WCS_PAYMENT_TYPE_CSPayment.desc());
	}
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public boolean processPayment(Order order, VenOrder venOrder) throws VeniceInternalException {
		// An array list of order payment allocations
		List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = new ArrayList<VenOrderPaymentAllocation>();
		List<VenOrderPayment> venOrderPaymentList = new ArrayList<VenOrderPayment>();

		//Allocate the payments to the order.
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "processPayment::Allocate the payments to the order");				
		if (order.getPayments() != null && (!(order.getPayments().isEmpty()))){
			CommonUtil.logDebug(this.getClass().getCanonicalName(),
					"processPayment::order.getPayments() is not null and not empty");
			//Iterator<?> paymentIterator = order.getPayments().iterator();
			//while (paymentIterator.hasNext()) {
			for (Payment payment : order.getPayments()) {
				//Payment next = (Payment) paymentIterator.next();
				//Ignore partial fulfillment payments ... looks like a work around in WCS ... no need for this in Venice
				//if (!next.getPaymentType().equals(VenWCSPaymentTypeConstants.VEN_WCS_PAYMENT_TYPE_PartialFulfillment.desc())) {
				if (!payment.getPaymentType().equals(VenWCSPaymentTypeConstants.VEN_WCS_PAYMENT_TYPE_PartialFulfillment.desc())) {
					VenOrderPayment venOrderPayment = new VenOrderPayment();

					//If the payment already exists then just fish it
					//from the DB. This is the case for VA payments as
					//they are received before the confirmed order.
					//List<VenOrderPayment> venOrderPaymentList2 = orderPaymentService.findByWcsPaymentId(next.getPaymentId().getCode());
					List<VenOrderPayment> venOrderPaymentList2 = findByWcsPaymentId(payment.getPaymentId().getCode());
					
					if (venOrderPaymentList2 != null && (!(venOrderPaymentList2.isEmpty()))) {
						venOrderPayment = venOrderPaymentList2.get(0);
					}
					// Map the payment with dozer
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "processPayment::Mapping the VenOrderPayment object...");
					//mapper.map(next, venOrderPayment);
					mapper.map(payment, venOrderPayment);

					// Set the payment type based on the WCS payment type
					// VenPaymentType venPaymentType = new VenPaymentType();
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "processPayment::mapping payment type");
					
					//venOrderPayment = OrderUtil.getVenOrderPaymentByWCSPaymentType(venOrderPayment, next);
					venOrderPayment = OrderUtil.getVenOrderPaymentByWCSPaymentType(venOrderPayment, payment);
					venOrderPaymentList.add(venOrderPayment);
				}
			}					
			
			List<VenOrderPayment> updatedVenOrderPayments = new ArrayList<VenOrderPayment>();
			
			//check whether payment's address equal with Customer's address
			for (VenOrderPayment orderPayment : venOrderPaymentList) {
				if (orderPayment.getVenAddress().equals(venOrder.getVenCustomer().getVenParty().getVenPartyAddresses().get(0).getVenAddress())) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "processPayment::payment's address equal with Customer's address, assign customer's address to payment's address");
					orderPayment.setVenAddress(venOrder.getVenCustomer().getVenParty().getVenPartyAddresses().get(0).getVenAddress());		
				}
				updatedVenOrderPayments.add(orderPayment);
			}
			
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "processPayment::persisting payment");
			//venOrderPaymentList = persistOrderPaymentList(venOrderPaymentList);
			venOrderPaymentList = persistOrderPaymentList(updatedVenOrderPayments);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "processPayment::venOrderPaymentList members = " + (venOrderPaymentList != null ? venOrderPaymentList.size() : 0));

			//paymentIterator = venOrderPaymentList.iterator();
			BigDecimal paymentBalance = venOrder.getAmount();
			int p=0;
			//while (paymentIterator.hasNext()) {
			for (VenOrderPayment venOrderPayment : venOrderPaymentList) {
				//VenOrderPayment next = (VenOrderPayment) paymentIterator.next();

				//Only include the allocations for non-VA payments
				//because VA payments are already in the DB
				CommonUtil.logDebug(this.getClass().getCanonicalName(), "processPayment::allocate payment,payment type = "
						+ venOrderPayment.getVenPaymentType().getPaymentTypeCode());
				
				//semua Payment di allocate, untuk payment VA dan non-VA.
		
				//if (!next.getVenPaymentType().getPaymentTypeCode().equals(VEN_PAYMENT_TYPE_VA)) {
					// Build the allocation list manually based on the payment
					VenOrderPaymentAllocation allocation = new VenOrderPaymentAllocation();
					allocation.setVenOrder(venOrder);
					//BigDecimal paymentAmount = next.getAmount();
					BigDecimal paymentAmount = venOrderPayment.getAmount();
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "processPayment::Order Amount = "+paymentBalance);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "processPayment::paymentBalance.compareTo(new BigDecimal(0)):  "
					+paymentBalance.compareTo(new BigDecimal(0)) );
					
					// If the balance is greater than zero
					if (paymentBalance.compareTo(new BigDecimal(0)) >= 0) {
					
						//If the payment amount is greater than the
						//balance then allocate the balance amount else
						//allocate the payment amount.
						if (paymentBalance.compareTo(paymentAmount) < 0) {
							allocation.setAllocationAmount(paymentBalance);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "processPayment::Order Allocation Amount is paymentBalance = "+paymentBalance);
						} else {
							allocation.setAllocationAmount(paymentAmount);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "processPayment::Order Allocation Amount is paymentAmount = "+paymentAmount);
						}
						
						paymentBalance = paymentBalance.subtract(paymentAmount);
						//allocation.setVenOrderPayment(next);
						allocation.setVenOrderPayment(venOrderPayment);

						// Need a primary key object...
						VenOrderPaymentAllocationPK venOrderPaymentAllocationPK = new VenOrderPaymentAllocationPK();
						//venOrderPaymentAllocationPK.setOrderPaymentId(next.getOrderPaymentId());
						venOrderPaymentAllocationPK.setOrderPaymentId(venOrderPayment.getOrderPaymentId());
						venOrderPaymentAllocationPK.setOrderId(venOrder.getOrderId());
						allocation.setId(venOrderPaymentAllocationPK);

						venOrderPaymentAllocationList.add(allocation);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "processPayment::venOrder Payment Allocation List size from looping ke-"
						   + p +" = "+venOrderPaymentAllocationList.size());
						p++;
					}
				//}
			}
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "processPayment::persist payment allocation");
			
			venOrderPaymentAllocationList = orderPaymentAllocationService.persistOrderPaymentAllocationList(venOrderPaymentAllocationList);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "processPayment::venOrderPaymentAllocationList members = " + (venOrderPaymentAllocationList != null ? venOrderPaymentAllocationList.size() : 0));
			venOrder.setVenOrderPaymentAllocations(venOrderPaymentAllocationList);
			
			//Here we need to create a dummy reconciliation records
			//for the non-VA payments so that they appear in the 
			//reconciliation screen as unreconciled.
			//Later these records will be updated when the funds in
			//reports are processed 					
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "processPayment::create reconciliation record");
			
			/*
			List<VenOrderPayment> orderPayments = new ArrayList<VenOrderPayment>();
			
			for (VenOrderPayment venOrderPayment : venOrderPaymentList) {
				orderPayments.add(venOrderPayment);
			}
			*/
			
			try {
				for (VenOrderPayment payment : venOrderPaymentList) {
				//for (VenOrderPayment payment : orderPayments) {
					//Only insert reconciliation records for non-VA payments here
					//because the VA records will have been inserted when a VA payment is received.
					if (payment.getVenPaymentType().getPaymentTypeId() != VeniceConstants.VEN_PAYMENT_TYPE_ID_VA 
							&& payment.getVenPaymentType().getPaymentTypeId() != VeniceConstants.VEN_PAYMENT_TYPE_ID_CS) {
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

						reconRecord.setVenOrderPayment(payment);
						reconRecord.setWcsOrderId(venOrder.getWcsOrderId());
						reconRecord.setOrderDate(venOrder.getOrderDate());
						reconRecord.setPaymentAmount(payment.getAmount());
						reconRecord.setNomorReff(payment.getReferenceId()!=null?payment.getReferenceId():"");

						// balance per payment amount - handling fee = payment amount, jadi bukan amount order total keseluruhan
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "processPayment::payment Amount  = " + payment.getAmount());
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "processPayment::HandlingFee = " + payment.getHandlingFee());
						BigDecimal remaining = payment.getAmount().subtract(payment.getHandlingFee()); 
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "processPayment::setRemainingBalanceAmount = " + remaining);

						reconRecord.setRemainingBalanceAmount(remaining);
						reconRecord.setUserLogonName("System");	

						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "processPayment::persisting reconRecord");
						reconRecord = finArFundsInReconRecordService.persist(reconRecord);
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "processPayment::reconRecord has been successfully persisted,reconRecord = " + reconRecord);
					}
				} //end of for 'venOrderPaymentList'
			} catch (Exception e) {
				CommonUtil.logError(this.getClass().getCanonicalName(), e);
				e.printStackTrace();
				CommonUtil.logAndReturnException(new CannotPersistFinArFundsInReconRecordException("Cannot persist FinArFundsInReconRecord"
						, VeniceExceptionConstants.VEN_EX_800001), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}			

		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "processPayment::EOM");
		return true;
	}
	
	/**
	 * Persists a list of order payments using the session tier.
	 * 
	 * @param orderPayments
	 * @return the persisted object
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenOrderPayment> persistOrderPaymentList(List<VenOrderPayment> venOrderPaymentList) throws VeniceInternalException {
		List<VenOrderPayment> newVenOrderPaymentList = new ArrayList<VenOrderPayment>();
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistOrderPaymentList::BEGIN,venOrderPaymentList=" + venOrderPaymentList);
		
		if (venOrderPaymentList != null && (!(venOrderPaymentList.isEmpty()))) {
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrderPaymentList::Persisting VenOrderPayment list...:"+ venOrderPaymentList.size());
				
				for (VenOrderPayment payment : venOrderPaymentList) {
					
					//backup initial payment allocations before it's detached
					List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = null;
					if (payment.getVenOrderPaymentAllocations() != null) {
					   venOrderPaymentAllocationList = new ArrayList<VenOrderPaymentAllocation>(payment.getVenOrderPaymentAllocations());
					} else {
						venOrderPaymentAllocationList = new ArrayList<VenOrderPaymentAllocation>();
					}
					
					//detach initial payment allocations
					payment.setVenOrderPaymentAllocations(null);
					
					// Synchronize the references
				    payment = synchronizeVenOrderPaymentReferenceData(payment);
					
					// Persist the billing address
				    VenAddress persistedPaymentAddress = addressService.persistAddress(payment.getVenAddress());
					payment.setVenAddress(persistedPaymentAddress);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderPaymentList::successfully persisted billing address");
					/*
					 * Check to see if the payment is already in the cache and 
					 * if it is then assume it is a VA payment and should not be 
					 * changed because it was APPROVED by Venice
					 */
					List<VenOrderPayment> paymentList = findByWcsPaymentId(payment.getWcsPaymentId());
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderPaymentList::Total paymentList found : " + paymentList);
					
					if ((paymentList == null) || (paymentList.isEmpty())) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderPaymentList::payment not found, persisting it");
						// Persist the object
						VenOrderPayment venOrderPaymentPersisted = payment;
						if (!em.contains(payment)) {
							//payment in detach mode, hence need to explicitly call save as shown below
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "persistOrderPaymentList::calling venOrderPaymentDAO save explicitly");
							venOrderPaymentPersisted = venOrderPaymentDAO.save(payment);
						}
						newVenOrderPaymentList.add(venOrderPaymentPersisted);
						// Persist the allocations
						List<VenOrderPaymentAllocation> venOrderPaymentAllocationPersisted 
						   = orderPaymentAllocationService.persistOrderPaymentAllocationList(venOrderPaymentAllocationList);
						payment.setVenOrderPaymentAllocations(venOrderPaymentAllocationPersisted);
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderPaymentList::payment found, update/merge the payment allocations");
						// Persist the allocations
						//List<VenOrderPaymentAllocation> venOrderPaymentAllocationPersisted 
						   //= orderPaymentAllocationService.persistOrderPaymentAllocationList(venOrderPaymentAllocationList);
						//payment.setVenOrderPaymentAllocations(venOrderPaymentAllocationPersisted);
						payment.setVenOrderPaymentAllocations(venOrderPaymentAllocationList);
						// Just put it back into the new list
						newVenOrderPaymentList.add(payment);						
					}
				} //end of 'for'
			} catch (Exception e) {
				throw CommonUtil.logAndReturnException(new VeniceInternalException(
						"An exception occured when persisting VenOrderItem", e)
						, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
			return newVenOrderPaymentList;
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistOrderPaymentList::EOM, returning venOrderPaymentList = " + venOrderPaymentList);
		return venOrderPaymentList;
	}
	
	/**
	 * Synchronizes the data for the direct VenOrderPayment references
	 * 
	 * @param venOrderPayment
	 * @return the synchronized data object
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrderPayment synchronizeVenOrderPaymentReferenceData(
			VenOrderPayment venOrderPayment) throws VeniceInternalException {
		
		if (venOrderPayment.getVenBank() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenOrderPaymentReferenceData::synchronizing venBank: ");			
			VenBank synchBank = bankService.synchronizeVenBank(venOrderPayment.getVenBank());
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenOrderPaymentReferenceData::venBank is synchronized : " + synchBank);
			venOrderPayment.setVenBank(synchBank);
		}
		
		if (venOrderPayment.getVenPaymentStatus() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderPaymentReferenceData::synchronizing venPaymentStatus");
			VenPaymentStatus synchPaymentStatus = paymentStatusService.synchronizeVenPaymentStatus(venOrderPayment.getVenPaymentStatus());
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderPaymentReferenceData::venPaymentStatus is synchronized");
			venOrderPayment.setVenPaymentStatus(synchPaymentStatus);
		} 
		
		if (venOrderPayment.getVenPaymentType() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderPaymentReferenceData::synchronizing venPaymentType");
			VenPaymentType synchPaymentType = paymentTypeService.synchronizeVenPaymentType(venOrderPayment.getVenPaymentType());
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderPaymentReferenceData::venPaymentType is synchronized");
			venOrderPayment.setVenPaymentType(synchPaymentType);
		}
		
		if (venOrderPayment.getVenWcsPaymentType() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderPaymentReferenceData::synchronizing venWcsPaymentType");
			VenWcsPaymentType synchWcsPaymentType = wcsPaymentTypeService.synchronizeVenWcsPaymentType(venOrderPayment.getVenWcsPaymentType());
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderPaymentReferenceData::venWcsPaymentType is synchronized");
			venOrderPayment.setVenWcsPaymentType(synchWcsPaymentType);			
		}
		
		if (venOrderPayment.getOldVenOrder() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderPaymentReferenceData::synchronizing Old Order");
			VenOrder synchOldOrder = orderService.synchronizeVenOrder(venOrderPayment.getOldVenOrder());
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderPaymentReferenceData::Old Order is synchronized");
			venOrderPayment.setOldVenOrder(synchOldOrder);
		}

		if (venOrderPayment.getVenAddress() != null) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderPaymentReferenceData::synchronizing VenAddress");			
			VenAddress synchAddress = addressService.synchronizeVenAddressReferenceData(venOrderPayment.getVenAddress());
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderPaymentReferenceData::VenAddress is synchronized");			
			venOrderPayment.setVenAddress(synchAddress);
		}
				
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderPaymentReferenceData::EOM, returning venOrderPayment = " + venOrderPayment);
		
		return venOrderPayment;		
	}
	
	@Override
	public List<VenOrderPayment> synchronizeVenOrderPaymentReferences(List<VenOrderPayment> orderPaymentReferences) 
	  throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderPaymentReferences::BEGIN,orderPaymentReferences=" + orderPaymentReferences);
		
		List<VenOrderPayment> synchronizedOrderPaymentReferences = new ArrayList<VenOrderPayment>();
		
		if (orderPaymentReferences != null) {
			for (VenOrderPayment orderPayment : orderPaymentReferences) {
				if (orderPayment.getWcsPaymentId() != null) {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenOrderPaymentReferences::Synchronizing VenOrderPayment... :" 
									+ orderPayment.getWcsPaymentId());
					VenOrderPayment venOrderPayment = synchronizeVenOrderPaymentReferenceData(orderPayment);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenOrderPaymentReferences::adding venOrderPayment into synchronizedOrderPaymentReferences");
					synchronizedOrderPaymentReferences.add(venOrderPayment);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "synchronizeVenOrderPaymentReferences::successfully added venOrderPayment into synchronizedOrderPaymentReferences");
				}			
			} // end of 'for'
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderPaymentReferences::returning synchronizedOrderPaymentReferences = "
				   + synchronizedOrderPaymentReferences.size());
		return synchronizedOrderPaymentReferences;
	}
}
