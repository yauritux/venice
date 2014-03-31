package com.gdn.venice.inbound.services.impl;

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
	
	@PersistenceContext
	private EntityManager em;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public VenOrderPaymentAllocation persist(VenOrderPaymentAllocation venOrderPaymentAllocation) throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "persist::BEGIN,venOrderPaymentAllocation=" + venOrderPaymentAllocation);
		VenOrderPaymentAllocation persistedOrderPaymentAllocation = venOrderPaymentAllocation;
		if (venOrderPaymentAllocation != null) {
			if (!em.contains(venOrderPaymentAllocation)) {
				//venOrderPaymentAllocation in detach mode, need to explicitly call save
				try {
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persist::calling venOrderPaymentAllocationDAO save explicitly");
					persistedOrderPaymentAllocation = venOrderPaymentAllocationDAO.save(venOrderPaymentAllocation);
					CommonUtil.logDebug(this.getClass().getCanonicalName()
							, "persist::successfully persisted venOrderPaymentAllocation");
				} catch (Exception e) {
					CommonUtil.logAndReturnException(new CannotPersistOrderPaymentAllocationException(
							"Cannot persist VenOrderPaymentAllocation, " + e, VeniceExceptionConstants.VEN_EX_000031)
					     , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);					
				}
			}
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persist::returning persistedOrderPaymentAllocation = " + persistedOrderPaymentAllocation);
		return persistedOrderPaymentAllocation;
	}
	
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
			for (VenOrderPaymentAllocation orderPaymentAllocation : venOrderPaymentAllocationList) {
				newVenOrderPaymentAllocationList.add(persist(orderPaymentAllocation));
			}
		}else{
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "persistOrderPaymentAllocationList::Persisting VenOrderPaymentAllocation list is null or empty");
		}
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "persistOrderPaymentAllocationList::returning newVenOrderPaymentAllocationList members = "
				  + (newVenOrderPaymentAllocationList != null ? newVenOrderPaymentAllocationList.size() : 0));
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
