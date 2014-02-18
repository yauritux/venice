package com.gdn.venice.inbound.services.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.VenOrderItemStatusHistoryDAO;
import com.gdn.venice.inbound.services.OrderItemStatusHistoryService;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderItemStatusHistory;
import com.gdn.venice.persistence.VenOrderItemStatusHistoryPK;
import com.gdn.venice.persistence.VenOrderStatus;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderItemStatusHistoryServiceImpl implements OrderItemStatusHistoryService {

	@Autowired
	private VenOrderItemStatusHistoryDAO venOrderItemStatusHistoryDAO;
	
	/**
	 * Create a history record for the new status for an order item 
	 * received as an updateOrderItemStatus message
	 * @param venOrderItem
	 * @param orderStatusId
	 * @return
	 */	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean createOrderItemStatusHistory(VenOrderItem venOrderItem,
			VenOrderStatus venOrderStatus) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrderItemStatusHistory::add order item status history");
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "createOrderItemStatusHistory::wcs order item id: "+venOrderItem.getWcsOrderItemId());
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "createOrderItemStatusHistory::order item status: "+venOrderItem.getVenOrderStatus().getOrderStatusCode());
		
		VenOrderItemStatusHistory venOrderItemStatusHistory = new VenOrderItemStatusHistory();
		
		VenOrderItemStatusHistoryPK venOrderItemStatusHistoryPK = new VenOrderItemStatusHistoryPK();
		venOrderItemStatusHistoryPK.setHistoryTimestamp(new Date(System.currentTimeMillis()));
		venOrderItemStatusHistoryPK.setOrderItemId(venOrderItem.getOrderItemId());
		
		venOrderItemStatusHistory.setId(venOrderItemStatusHistoryPK);
		venOrderItemStatusHistory.setVenOrderItem(venOrderItem);
		venOrderItemStatusHistory.setStatusChangeReason("Updated by System");
		venOrderItemStatusHistory.setVenOrderStatus(venOrderStatus);
		
		venOrderItemStatusHistory = venOrderItemStatusHistoryDAO.save(venOrderItemStatusHistory);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "done add order item status history");
		
		if(venOrderItemStatusHistory != null){
			return Boolean.TRUE;
		}
		
		return Boolean.FALSE;
	}

}
