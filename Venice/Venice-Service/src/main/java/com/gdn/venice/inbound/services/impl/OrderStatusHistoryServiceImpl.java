package com.gdn.venice.inbound.services.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.dao.VenOrderStatusHistoryDAO;
import com.gdn.venice.exception.CannotPersistOrderStatusHistoryException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.services.OrderStatusHistoryService;
import com.gdn.venice.inbound.services.SeatOrderStatusHistoryService;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderStatus;
import com.gdn.venice.persistence.VenOrderStatusHistory;
import com.gdn.venice.persistence.VenOrderStatusHistoryPK;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {

	@Autowired
	private VenOrderStatusHistoryDAO venOrderStatusHistoryDAO;
	
	 @Autowired
	 private SeatOrderStatusHistoryService seatOrderStatusHistoryService;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean createOrderStatusHistory(VenOrder venOrder,
			VenOrderStatus venOrderStatus) throws VeniceInternalException {
		try {
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "createOrderStatusHistory::add order status history");
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "createOrderStatusHistory::wcs order id: "+venOrder.getWcsOrderId());
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "createOrderStatusHistory::order status: "+venOrder.getVenOrderStatus().getOrderStatusCode());
			
			VenOrderStatusHistory venOrderStatusHistory = new VenOrderStatusHistory(); // detach mode
			
			VenOrderStatusHistoryPK venOrderStatusHistoryPK = new VenOrderStatusHistoryPK();
			venOrderStatusHistoryPK.setHistoryTimestamp(new Date(System.currentTimeMillis()));
			venOrderStatusHistoryPK.setOrderId(venOrder.getOrderId());
			
			venOrderStatusHistory.setId(venOrderStatusHistoryPK);
			venOrderStatusHistory.setVenOrder(venOrder);
			venOrderStatusHistory.setStatusChangeReason("Updated by System");
			venOrderStatusHistory.setVenOrderStatus(venOrderStatus);
			
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "createOrderStatusHistory::going to persist venOrderStatusHistory");
			
			venOrderStatusHistory = venOrderStatusHistoryDAO.save(venOrderStatusHistory); // need to explicitly call save to bring the object into an attach mode
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrderStatusHistory::done add order status history");
			if(venOrderStatusHistory != null){
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		} catch (Exception e) {
			throw CommonUtil.logAndReturnException(new CannotPersistOrderStatusHistoryException(
					"An exception occured when creating order status history"
					, VeniceExceptionConstants.VEN_EX_000024)
			  , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}	
	}

}
