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
import com.gdn.venice.dao.VenOrderStatusDAO;
import com.gdn.venice.exception.OrderStatusNotFoundException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.OrderStatusService;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderStatus;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderStatusServiceImpl implements OrderStatusService {

	@Autowired
	private VenOrderStatusDAO venOrderStatusDAO;
	
	@PersistenceContext
	EntityManager em;
	
	@Override
	public List<VenOrderStatus> synchronizeVenOrderStatusReferences(
			List<VenOrderStatus> orderStatusReferences) throws VeniceInternalException {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderStatusReferences::BEGIN, orderStatusReferences=" + orderStatusReferences);
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderStatusReferences::BEGIN, orderStatusReferences size =" 
		        + (orderStatusReferences != null ? orderStatusReferences.size() : 0));		
		
		List<VenOrderStatus> synchronizedOrderStatus = new ArrayList<VenOrderStatus>();

		try {
			if (orderStatusReferences != null) {
				for (VenOrderStatus orderStatus : orderStatusReferences) {
					em.detach(orderStatus);
					if (orderStatus.getOrderStatusCode() != null) {
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenOrderStatusReferences::Restricting VenOrderStatus... :" 
										+ orderStatus.getOrderStatusCode());

						VenOrderStatus venOrderStatus = venOrderStatusDAO.findByOrderStatusCode(orderStatus.getOrderStatusCode());
						CommonUtil.logDebug(this.getClass().getCanonicalName()
								, "synchronizeVenOrderStatusReferences::venOrderStatus found = " + venOrderStatus);
						if (venOrderStatus == null) {
							throw CommonUtil.logAndReturnException(new OrderStatusNotFoundException("Order status does not exist", 
									VeniceExceptionConstants.VEN_EX_000025)
							, CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
						} else {
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeVenOrderStatusReferences::venOrderStatus ID = " + venOrderStatus.getOrderStatusId());
							List<VenOrder> orderStatusVenOrders = (venOrderStatus.getVenOrders() != null 
									&& (!(venOrderStatus.getVenOrders().isEmpty())) 
									? new ArrayList<VenOrder>(venOrderStatus.getVenOrders()) : new ArrayList<VenOrder>());							
							List<VenOrderItem> orderStatusVenOrderItems = (venOrderStatus.getVenOrderItems() != null 
									&& (!(venOrderStatus.getVenOrderItems().isEmpty())) 
									? new ArrayList<VenOrderItem>(venOrderStatus.getVenOrderItems()) : new ArrayList<VenOrderItem>());														

							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeVenOrderStatusReferences::going to detach venOrderStatus");
							em.detach(venOrderStatus);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeVenOrderStatusReferences::venOrderStatus is detached");
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeVenOrderStatusReferences::orderStatusVenOrders size = "
											+ (orderStatusVenOrders != null ? orderStatusVenOrders.size() : 0));
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeVenOrderStatusReferences::orderStatusVenOrderItems size = "
											+ (orderStatusVenOrderItems != null ? orderStatusVenOrderItems.size() : 0));							
							venOrderStatus.setVenOrders(orderStatusVenOrders);
							venOrderStatus.setVenOrderItems(orderStatusVenOrderItems);							
							
							synchronizedOrderStatus.add(venOrderStatus);
							CommonUtil.logDebug(this.getClass().getCanonicalName()
									, "synchronizeVenOrderStatusReferences::successfully added venOrderStatus into synchronizedOrderStatus");
						}
					}			
				} // end of 'for'
			}
		} catch (Exception e) {
			CommonUtil.logError(this.getClass().getCanonicalName()
					, e);
		}
		
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "synchronizeVenOrderStatusReferences::returning synchronizedOrderStatus = " + synchronizedOrderStatus.size());
		
		return synchronizedOrderStatus;
	}
}
