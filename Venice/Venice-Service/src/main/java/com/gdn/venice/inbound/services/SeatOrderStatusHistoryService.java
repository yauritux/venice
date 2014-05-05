package com.gdn.venice.inbound.services;

import com.gdn.venice.persistence.SeatOrderEtd;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderStatus;

public interface SeatOrderStatusHistoryService {
	public Boolean createSeatOrderStatusHistory(VenOrderItem venOrderItem, VenOrderStatus venOrderStatus);
	public Boolean createSeatOrderStatusHistoryVAAndCS(VenOrder venOrder,SeatOrderEtd seatOrderEtd);

}
