package com.gdn.venice.inbound.services;

import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderStatus;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderItemStatusHistoryService {

	public Boolean createOrderItemStatusHistory(VenOrderItem venOrderItem, VenOrderStatus venOrderStatus);
}
