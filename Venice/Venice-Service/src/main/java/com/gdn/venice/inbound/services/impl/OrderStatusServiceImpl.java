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
import com.gdn.venice.exception.VenOrderStatusSynchronizingError;
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
	private EntityManager em;

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public VenOrderStatus synchronizeVenOrderStatusReferences(
			VenOrderStatus venOrderStatus) throws VeniceInternalException {

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenOrderStatusReferences::BEGIN, venOrderStatus = "
						+ venOrderStatus);

		VenOrderStatus synchronizedOrderStatus = venOrderStatus;

		try {
			if (venOrderStatus != null && venOrderStatus.getOrderStatusCode() != null) {
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenOrderStatusReferences::Restricting VenOrderStatus... :" 
				        + venOrderStatus.getOrderStatusCode());

				VenOrderStatus memOrderStatus = venOrderStatusDAO.findByOrderStatusCode(venOrderStatus.getOrderStatusCode());
				CommonUtil.logDebug(this.getClass().getCanonicalName(),
						"synchronizeVenOrderStatusReferences::venOrderStatus found = " + memOrderStatus);
				
				if (memOrderStatus == null) {
					throw CommonUtil.logAndReturnException(
							new OrderStatusNotFoundException(
									"Order status does not exist",
									VeniceExceptionConstants.VEN_EX_000025),
							CommonUtil.getLogger(this.getClass()
									.getCanonicalName()), LoggerLevel.ERROR);
				} else {
					CommonUtil.logDebug(this.getClass().getCanonicalName(), "synchronizeVenOrderStatusReferences::venOrderStatus found");
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
							"synchronizeVenOrderStatusReferences::venOrderStatus ID = " + memOrderStatus.getOrderStatusId());
					/*
					List<VenOrder> orderStatusVenOrders = (memOrderStatus.getVenOrders() != null
							&& (!(memOrderStatus.getVenOrders().isEmpty())) ? new ArrayList<VenOrder>(
							memOrderStatus.getVenOrders()) : new ArrayList<VenOrder>());
					List<VenOrderItem> orderStatusVenOrderItems = (memOrderStatus.getVenOrderItems() != null
							&& (!(memOrderStatus.getVenOrderItems().isEmpty())) ? new ArrayList<VenOrderItem>(
							memOrderStatus.getVenOrderItems()): new ArrayList<VenOrderItem>());

					CommonUtil.logDebug(this.getClass().getCanonicalName(),
									"synchronizeVenOrderStatusReferences::orderStatusVenOrders size = "
											+ (orderStatusVenOrders != null ? orderStatusVenOrders
													.size() : 0));
					CommonUtil.logDebug(this.getClass().getCanonicalName(),
									"synchronizeVenOrderStatusReferences::orderStatusVenOrderItems size = "
											+ (orderStatusVenOrderItems != null ? orderStatusVenOrderItems.size() : 0));
					
					//venOrderStatus.setVenOrders(orderStatusVenOrders);
					synchronizedOrderStatus.setVenOrders(orderStatusVenOrders);
					//venOrderStatus.setVenOrderItems(orderStatusVenOrderItems);
					synchronizedOrderStatus.setVenOrderItems(orderStatusVenOrderItems);
					*/
					synchronizedOrderStatus = memOrderStatus;
				}
			}
		} catch (Exception e) {
			CommonUtil.logError(this.getClass().getCanonicalName(), e);
			e.printStackTrace();
			CommonUtil.logAndReturnException(new VenOrderStatusSynchronizingError("Cannot synchronize VenOrderStatus!"
					, VeniceExceptionConstants.VEN_EX_130008), CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}

		CommonUtil.logDebug(this.getClass().getCanonicalName(),
				"synchronizeVenOrderStatusReferences::returning synchronizedOrderStatus.orderStatusId = " + synchronizedOrderStatus.getOrderStatusId());

		return synchronizedOrderStatus;
	}
}
