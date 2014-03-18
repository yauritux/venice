package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
	
	@PersistenceContext
	private EntityManager em;

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
