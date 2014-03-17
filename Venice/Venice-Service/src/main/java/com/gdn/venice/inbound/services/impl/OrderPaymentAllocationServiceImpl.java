package com.gdn.venice.inbound.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenOrderPaymentAllocationDAO;
import com.gdn.venice.exception.CannotPersistOrderPaymentAllocationException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.OrderPaymentAllocationService;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderPaymentAllocation;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderPaymentAllocationServiceImpl implements OrderPaymentAllocationService {

	@Autowired
	private VenOrderPaymentAllocationDAO venOrderPaymentAllocationDAO;
	
	/**
	 * Persists the payment allocation list to the cache
	 * 
	 * @param venOrderPaymentAllocationList
	 * @return
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<VenOrderPaymentAllocation> persistOrderPaymentAllocationList(
			List<VenOrderPaymentAllocation> venOrderPaymentAllocationList)
			throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistOrderPaymentAllocationList::BEGIN, venOrderPaymentAllocationList = " + venOrderPaymentAllocationList);
		List<VenOrderPaymentAllocation> newVenOrderPaymentAllocationList = new ArrayList<VenOrderPaymentAllocation>();
		if (venOrderPaymentAllocationList != null	&& (!(venOrderPaymentAllocationList.isEmpty()))) {
			/*
			try {
				CommonUtil.logDebug(this.getClass().getCanonicalName()
						, "persistOrderPaymentAllocationList::Persisting VenOrderPaymentAllocation list...:"
				          + venOrderPaymentAllocationList);
				Iterator<VenOrderPaymentAllocation> i = venOrderPaymentAllocationList.iterator();
				while (i.hasNext()) {
					VenOrderPaymentAllocation next = i.next();
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persistOrderPaymentAllocationList::value of paymentAllocation ......: order_id = "
					   + next.getVenOrder().getOrderId() +" and wcs_code_payment = "
					   + next.getVenOrderPayment().getWcsPaymentId());
					// Persist the object 
					newVenOrderPaymentAllocationList.add(venOrderPaymentAllocationDAO.save(next));
				}
			} catch (Exception e) {
				throw CommonUtil.logAndReturnException(new CannotPersistOrderPaymentException(
						"An exception occured when persisting VenOrderPaymentAllocation", VeniceExceptionConstants.VEN_EX_000023)
				, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
			*/
			try {
				newVenOrderPaymentAllocationList = venOrderPaymentAllocationDAO.save(venOrderPaymentAllocationList);
			} catch (Exception e) {
				CommonUtil.logAndReturnException(new CannotPersistOrderPaymentAllocationException(
						"Cannot persist VenOrderPaymentAllocation, " + e, VeniceExceptionConstants.VEN_EX_000031)
				     , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
			}
		}else{
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistOrderPaymentAllocationList::Persisting VenOrderPaymentAllocation list is null");
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistOrderPaymentAllocationList::returning newVenOrderPaymentAllocationList = "
				  + newVenOrderPaymentAllocationList);
		return newVenOrderPaymentAllocationList;	
	}

	/**
	 * Removes all of the payment allocations for an order
	 * 
	 * @param venOrder
	 * @return true if the operation succeeds else false
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean removeOrderPaymentAllocationList(VenOrder venOrder) {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "removeOrderPaymentAllocationList::Remove Order Payment Allocation List ...:order id= "
						+venOrder.getOrderId()+" and wcs Order Id= "+venOrder.getWcsOrderId());
		List<VenOrderPaymentAllocation> venOrderPaymentAllocationList = venOrderPaymentAllocationDAO.findByVenOrder(venOrder);
		
		venOrderPaymentAllocationDAO.deleteInBatch(venOrderPaymentAllocationList);
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "removeOrderPaymentAllocationList::EOM, venOrderPaymentAllocationList have been successfully deleted");
		
		return Boolean.TRUE;		
	}

}
