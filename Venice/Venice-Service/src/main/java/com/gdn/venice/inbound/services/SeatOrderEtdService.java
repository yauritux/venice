package com.gdn.venice.inbound.services;

import com.gdn.venice.persistence.SeatOrderEtd;
import com.gdn.venice.persistence.VenOrderItem;

public interface SeatOrderEtdService {
	public SeatOrderEtd createSeatOrderEtd(VenOrderItem venOrderItem);
}
