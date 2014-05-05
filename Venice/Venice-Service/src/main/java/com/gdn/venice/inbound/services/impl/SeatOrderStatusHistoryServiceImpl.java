package com.gdn.venice.inbound.services.impl;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.constants.VenOrderStatusConstants;
import com.gdn.venice.dao.SeatOrderEtdDAO;
import com.gdn.venice.dao.SeatOrderStatusHistoryDAO;
import com.gdn.venice.inbound.services.SeatOrderEtdService;
import com.gdn.venice.inbound.services.SeatOrderStatusHistoryService;
import com.gdn.venice.persistence.SeatOrderEtd;
import com.gdn.venice.persistence.SeatOrderStatusHistory;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderStatus;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author Arifin
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class SeatOrderStatusHistoryServiceImpl implements
		SeatOrderStatusHistoryService {
	
	@Autowired
	private SeatOrderStatusHistoryDAO seatOrderStatusHistoryDAO;
	
	@Autowired
	private SeatOrderEtdDAO seatOrderEtdDAO;
	
	@Autowired
	private SeatOrderEtdService seatOrderEtdService;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public Boolean createSeatOrderStatusHistory(VenOrderItem venOrderItem,
			VenOrderStatus venOrderStatus) {
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "createSeatOrderStatusHistory::add order item status history");
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "createSeatOrderStatusHistory::wcs order item id: "+venOrderItem.getWcsOrderItemId());
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "createSeatOrderStatusHistory::order item status: "+venOrderItem.getVenOrderStatus().getOrderStatusCode());
		
		SeatOrderStatusHistory seatOrderStatusHistory = new SeatOrderStatusHistory();		
		ArrayList<SeatOrderStatusHistory> seatOrderStatusHistoryList = seatOrderStatusHistoryDAO.findByOrderIdAndOrderItemId(venOrderItem.getVenOrder().getOrderId(),venOrderItem.getOrderItemId());	
		if(seatOrderStatusHistoryList.isEmpty()){
			
			SeatOrderEtd etdForSeattle = seatOrderEtdService.createSeatOrderEtd(venOrderItem);			
			seatOrderStatusHistory = new SeatOrderStatusHistory();
			seatOrderStatusHistory.setVenOrder(venOrderItem.getVenOrder());
			seatOrderStatusHistory.setVenOrderItem(venOrderItem);
			seatOrderStatusHistory.setVenOrderStatus(venOrderItem.getVenOrderStatus());		
			seatOrderStatusHistory.setSeatOrderEtd(etdForSeattle);
			seatOrderStatusHistory.setUpdateStatusDate(new Timestamp(System.currentTimeMillis()));	
		}else{
			seatOrderStatusHistory = seatOrderStatusHistoryList.get(0);
			seatOrderStatusHistory.setVenOrderStatus(venOrderItem.getVenOrderStatus());		
			seatOrderStatusHistory.setUpdateStatusDate(new Timestamp(System.currentTimeMillis()));	
		}				
				
		seatOrderStatusHistory = seatOrderStatusHistoryDAO.save(seatOrderStatusHistory);
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "done add Seat order item status history");
		
		if(seatOrderStatusHistory != null){
			return Boolean.TRUE;
		}
		
		return Boolean.FALSE;
	}

	@Override
	public Boolean createSeatOrderStatusHistoryVAAndCS(VenOrder venOrder,SeatOrderEtd seatOrderEtd) {
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "createSeatOrderStatusHistoryVAAndCS::add Order status history");
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "createSeatOrderStatusHistory::wcs order id: "+venOrder.getWcsOrderId());	
		SeatOrderStatusHistory seatOrderStatusHistory =null;
	
		if(venOrder.getVenOrderStatus().getOrderStatusId()==VenOrderStatusConstants.VEN_ORDER_STATUS_VA.code()
				|| venOrder.getVenOrderStatus().getOrderStatusId()==VenOrderStatusConstants.VEN_ORDER_STATUS_CS.code()){
			
			SeatOrderEtd orderEtd = seatOrderEtdDAO.save(seatOrderEtd);
			
			seatOrderStatusHistory = new SeatOrderStatusHistory();		
			seatOrderStatusHistory.setVenOrder(venOrder);
			seatOrderStatusHistory.setVenOrderStatus(venOrder.getVenOrderStatus());	
			if(orderEtd!=null){
				seatOrderStatusHistory.setSeatOrderEtd(orderEtd);
			}
			seatOrderStatusHistory.setUpdateStatusDate(new Timestamp(System.currentTimeMillis()));	
			
			seatOrderStatusHistory= seatOrderStatusHistoryDAO.save(seatOrderStatusHistory);
		}
		if(seatOrderStatusHistory != null){
			return Boolean.TRUE;
		}
		
		return Boolean.FALSE;
	}
}
