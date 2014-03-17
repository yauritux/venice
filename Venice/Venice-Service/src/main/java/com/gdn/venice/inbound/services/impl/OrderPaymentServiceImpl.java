package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.integration.jaxb.Payment;
import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VenCSPaymentStatusIDConstants;
import com.gdn.venice.constants.VenVAPaymentStatusIDConstants;
import com.gdn.venice.constants.VenWCSPaymentTypeConstants;
import com.gdn.venice.dao.VenOrderPaymentDAO;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.AddressService;
import com.gdn.venice.inbound.services.BankService;
import com.gdn.venice.inbound.services.OrderPaymentAllocationService;
import com.gdn.venice.inbound.services.OrderPaymentService;
import com.gdn.venice.inbound.services.OrderService;
import com.gdn.venice.inbound.services.PaymentStatusService;
import com.gdn.venice.inbound.services.PaymentTypeService;
import com.gdn.venice.inbound.services.WcsPaymentTypeService;
import com.gdn.venice.persistence.VenAddress;
import com.gdn.venice.persistence.VenBank;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPayment;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.persistence.VenPaymentStatus;
import com.gdn.venice.persistence.VenPaymentType;
import com.gdn.venice.persistence.VenWcsPaymentType;
import com.gdn.venice.util.CommonUtil;

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
	private OrderPaymentAllocationService orderPaymentAllocationService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private PaymentTypeService paymentTypeService;
	
	@Autowired
	private PaymentStatusService paymentStatusService;
	
	@Autowired
	private WcsPaymentTypeService wcsPaymentTypeService;

	@Override
	public List<VenOrderPayment> findByWcsPaymentId(String wcsPaymentId) {
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
		if (!isPaymentExist(payment)) return false;
		
		VenOrderPayment venOrderPayment = getVenOrderPayment(payment);
		
		if (venOrderPayment == null) return false;
		
		if (isVirtualAccountPayment(payment.getPaymentType())) {
			//VA Payment
			if (venOrderPayment.getVenPaymentStatus().getPaymentStatusId()
					!= VenVAPaymentStatusIDConstants.VEN_VA_PAYMENT_STATUS_ID_APPROVED.code()) {
				//VA Payment is not approved yet, thus throw an exception
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "An order with an unapproved VA payment has been received");
				return false;
			}
		} else if (isCSPayment(payment.getPaymentType())) {
			//CS Payment
			if (venOrderPayment.getVenPaymentStatus().getPaymentStatusId()
					!= VenCSPaymentStatusIDConstants.VEN_CS_PAYMENT_STATUS_ID_APPROVED.code()) {
				//CS Payment is not approved yet, thus throw and exception
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "An order with an unapproved CS payment has been received");
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
					
					//List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = payment.getVenOrderPaymentAllocations();
					List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = new ArrayList<VenOrderPaymentAllocation>();
					if (payment.getVenOrderPaymentAllocations() != null) {
						for (VenOrderPaymentAllocation venOrderPaymentAllocation : payment.getVenOrderPaymentAllocations()) {
							venOrderPaymentAllocationList.add(venOrderPaymentAllocation);
						}
					}
					
					payment.setVenOrderPaymentAllocations(null);
					
					// Synchronize the references
					payment = synchronizeVenOrderPaymentReferenceData(payment);
					
					// Persist the billing address
					payment.setVenAddress(addressService.persistAddress(payment.getVenAddress()));
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderPaymentList::successfully persisted billing address");
					/*
					 * Check to see if the payment is already in the cache and 
					 * if it is then assume it is a VA payment and should not be 
					 * changed because it was APPROVED by Venice
					 */
					
					List<VenOrderPayment> paymentList = venOrderPaymentDAO.findByWcsPaymentId(payment.getWcsPaymentId());
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderPaymentList::paymentList found : " + paymentList);
					
					if ((paymentList == null) || (paymentList.isEmpty())) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderPaymentList::payment not found, persisting it");
						// Persist the object
						//VenOrderPayment venOrderPaymentPersisted = venOrderPaymentDAO.save(payment);
						//newVenOrderPaymentList.add(venOrderPaymentPersisted);
						newVenOrderPaymentList.add(payment);
						// Persist the allocations
						//List<VenOrderPaymentAllocation> venOrderPaymentAllocationPersisted 
						  // = orderPaymentAllocationService.persistOrderPaymentAllocationList(venOrderPaymentAllocationList);
						//payment.setVenOrderPaymentAllocations(venOrderPaymentAllocationPersisted);
						payment.setVenOrderPaymentAllocations(venOrderPaymentAllocationList);
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderPaymentList::persist the allocations");
						// Persist the allocations
						//List<VenOrderPaymentAllocation> venOrderPaymentAllocationPersisted 
						   //= orderPaymentAllocationService.persistOrderPaymentAllocationList(venOrderPaymentAllocationList);
						//payment.setVenOrderPaymentAllocations(venOrderPaymentAllocationPersisted);
						payment.setVenOrderPaymentAllocations(venOrderPaymentAllocationList);
						// Just put it back into the new list
						newVenOrderPaymentList.add(payment);						
					}
				} //end of 'for'
				/*
				Iterator<VenOrderPayment> i = venOrderPaymentList.iterator();
				while (i.hasNext()) {
					VenOrderPayment next = i.next();
					
					// Detach the allocations before persisting
					List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = (List<VenOrderPaymentAllocation>)next.getVenOrderPaymentAllocations();
					
					next.setVenOrderPaymentAllocations(null);

					// Synchronize the references
					next = synchronizeVenOrderPaymentReferenceData(next);

					// Persist the billing address
					next.setVenAddress(addressService.persistAddress(next.getVenAddress()));
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderPaymentList::venaddress has successfully persisted");

					
					//Check to see if the payment is already in the cache and
					//if it is then assume it is a VA payment and should not be
					//changed because it was APPROVED by Venice
					List<VenOrderPayment> paymentList = venOrderPaymentDAO.findByWcsPaymentId(next.getWcsPaymentId());
					
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderPaymentList::paymentList found : " + paymentList.size());

					if (paymentList.isEmpty()) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderPaymentList::Payment not found so persisting it...");
						// Persist the object
						VenOrderPayment venOrderPaymentPersisted = venOrderPaymentDAO.save(next);
						newVenOrderPaymentList.add(venOrderPaymentPersisted);
						// Persist the allocations
						List<VenOrderPaymentAllocation> venOrderPaymentAllocationPersisted 
						   = orderPaymentAllocationService.persistOrderPaymentAllocationList(venOrderPaymentAllocationList);
						next.setVenOrderPaymentAllocations(venOrderPaymentAllocationPersisted);
					} else {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "persistOrderPaymentList::persist the allocations");
						// Persist the allocations
						List<VenOrderPaymentAllocation> venOrderPaymentAllocationPersisted 
						   = orderPaymentAllocationService.persistOrderPaymentAllocationList(venOrderPaymentAllocationList);
						next.setVenOrderPaymentAllocations(venOrderPaymentAllocationPersisted);
						// Just put it back into the new list
						newVenOrderPaymentList.add(next);
					}
				} //end of 'while'
				*/
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
	public VenOrderPayment synchronizeVenOrderPaymentReferenceData(
			VenOrderPayment venOrderPayment) throws VeniceInternalException {
		
		if (venOrderPayment.getVenBank() != null) {
			List<VenBank> bankReferences = new ArrayList<VenBank>();
			bankReferences.add(venOrderPayment.getVenBank());
			// Synchronize VenBank references
			bankReferences = bankService.synchronizeVenBankReferences(bankReferences);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenOrderPaymentReferenceData::bankReferences is synchronized : "
					+ bankReferences);
			for (VenBank bank : bankReferences) { // do we need to do this inside a loop ? weird isn't it ? should be refactored later
				venOrderPayment.setVenBank(bank);
			}			
		}
		
		if (venOrderPayment.getVenPaymentStatus() != null) {
			List<VenPaymentStatus> paymentStatusReferences = new ArrayList<VenPaymentStatus>();
			paymentStatusReferences.add(venOrderPayment.getVenPaymentStatus());
			// Synchronize VenPaymentStatus references
			paymentStatusReferences = paymentStatusService.synchronizeVenPaymentStatusReferences(paymentStatusReferences);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenOrderPaymentReferenceData::paymentStatusReferences is synchronized : "
					+ paymentStatusReferences);
			for (VenPaymentStatus paymentStatus : paymentStatusReferences) {
				venOrderPayment.setVenPaymentStatus(paymentStatus);
			}			
		} 
		
		if (venOrderPayment.getVenPaymentType() != null) {
			List<VenPaymentType> paymentTypeReferences = new ArrayList<VenPaymentType>();
			paymentTypeReferences.add(venOrderPayment.getVenPaymentType());
			// Synchronize VenPaymentType references
			paymentTypeReferences = paymentTypeService.synchronizeVenPaymentTypeReferences(paymentTypeReferences);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenOrderPaymentReferenceData::paymentTypeReferences is synchronized : "
					+ paymentTypeReferences);
			for (VenPaymentType paymentType : paymentTypeReferences) {
				venOrderPayment.setVenPaymentType(paymentType);
			}			
		}
		
		if (venOrderPayment.getVenWcsPaymentType() != null) {
			List<VenWcsPaymentType> wcsPaymentTypeReferences = new ArrayList<VenWcsPaymentType>();
			wcsPaymentTypeReferences.add(venOrderPayment.getVenWcsPaymentType());
			// Synchronize VenWcsPaymentType references
			wcsPaymentTypeReferences = wcsPaymentTypeService.synchronizeVenWcsPaymentTypeReferences(wcsPaymentTypeReferences);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenOrderPaymentReferenceData::wcsPaymentTypeReferences is synchronized : "
							+ wcsPaymentTypeReferences);
			for (VenWcsPaymentType wcsPaymentType : wcsPaymentTypeReferences) {
				venOrderPayment.setVenWcsPaymentType(wcsPaymentType);
			}			
		}
		
		if (venOrderPayment.getOldVenOrder() != null) {
			List<VenOrder> oldOrderReferences = new ArrayList<VenOrder>();
			oldOrderReferences.add(venOrderPayment.getOldVenOrder());
			oldOrderReferences = orderService.synchronizeVenOrderReferences(oldOrderReferences);
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "synchronizeVenOrderPaymentReferenceData::oldOrderReferences is synchronized : "
					+ oldOrderReferences);
			for (VenOrder order : oldOrderReferences) {
				venOrderPayment.setOldVenOrder(order);
			}			
		}
		/*
		List<Object> references = new ArrayList<Object>();
		references.add(venOrderPayment.getVenBank());
		references.add(venOrderPayment.getVenPaymentStatus());
		references.add(venOrderPayment.getVenPaymentType());
		references.add(venOrderPayment.getVenAddress());
		references.add(venOrderPayment.getVenWcsPaymentType());
		references.add(venOrderPayment.getOldVenOrder());
		*/

		if (venOrderPayment.getVenAddress() != null) {
			VenAddress venAddress = addressService.synchronizeVenAddressReferenceData(venOrderPayment.getVenAddress());
			venOrderPayment.setVenAddress(venAddress);
		}
		
		//references = this.synchronizeReferenceData(references);

		// Push the keys back into the record
		/*
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
		*/
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderPaymentReferenceData::EOM, returning venOrderPayment = " + venOrderPayment);
		
		return venOrderPayment;		
	}
	
	@Override
	public List<VenOrderPayment> synchronizeVenOrderPaymentReferences(List<VenOrderPayment> orderPaymentReferences) 
	  throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderPaymentReferences::BEGIN,orderPaymentReferences=" + orderPaymentReferences);
		//if (orderPaymentReferences == null || orderPaymentReferences.isEmpty()) return null;
		
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
