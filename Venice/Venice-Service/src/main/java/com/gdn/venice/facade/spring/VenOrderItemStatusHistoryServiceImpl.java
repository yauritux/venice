package com.gdn.venice.facade.spring;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.VenOrderItemStatusHistoryDAO;
import com.gdn.venice.inbound.services.SeatOrderStatusHistoryService;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderItemStatusHistory;
import com.gdn.venice.persistence.VenOrderItemStatusHistoryPK;

@Service
public class VenOrderItemStatusHistoryServiceImpl implements
		VenOrderItemStatusHistoryService {

	@Autowired
	VenOrderItemStatusHistoryDAO venOrderItemStatusHistoryDAO;
	
	@Autowired
	SeatOrderStatusHistoryService seatOrderStatusHistoryService;
	
	@Override
	public void saveVenOrderItemStatusHistory(VenOrderItem venOrderItem) {
		String changeReason = "Updated by System";
		commonSaveVenOrderItemStatusHistory(venOrderItem, changeReason);
		seatOrderStatusHistoryService.createSeatOrderStatusHistory(venOrderItem,venOrderItem.getVenOrderStatus());
	}
	
	@Override
	public void savePartialPartialFulfillmentVenOrderItemStatusHistory(VenOrderItem venOrderItem) {
		String changeReason = "Updated by System (Partial Fulfillment)";
		commonSaveVenOrderItemStatusHistory(venOrderItem, changeReason);
		seatOrderStatusHistoryService.createSeatOrderStatusHistory(venOrderItem,venOrderItem.getVenOrderStatus());
	}
	
	private void commonSaveVenOrderItemStatusHistory(VenOrderItem venOrderItem, String changeReason) {
		VenOrderItemStatusHistoryPK venOrderItemStatusHistoryPK = new VenOrderItemStatusHistoryPK();
        venOrderItemStatusHistoryPK.setOrderItemId(venOrderItem.getOrderItemId());
        venOrderItemStatusHistoryPK.setHistoryTimestamp(new Timestamp(System.currentTimeMillis()));

        VenOrderItemStatusHistory orderItemStatusHistory = new VenOrderItemStatusHistory();
        orderItemStatusHistory.setId(venOrderItemStatusHistoryPK);
        orderItemStatusHistory.setStatusChangeReason(changeReason);
        orderItemStatusHistory.setVenOrderStatus(venOrderItem.getVenOrderStatus());

        venOrderItemStatusHistoryDAO.save(orderItemStatusHistory);
	}	
}
