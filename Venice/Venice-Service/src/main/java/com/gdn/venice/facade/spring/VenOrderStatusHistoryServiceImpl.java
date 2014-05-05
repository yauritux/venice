package com.gdn.venice.facade.spring;

import java.sql.Timestamp;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.constants.VenOrderStatusConstants;
import com.gdn.venice.dao.SeatOrderEtdDAO;
import com.gdn.venice.dao.SeatOrderStatusHistoryDAO;
import com.gdn.venice.dao.VenOrderStatusHistoryDAO;
import com.gdn.venice.persistence.SeatOrderEtd;
import com.gdn.venice.persistence.SeatOrderStatusHistory;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderStatusHistory;
import com.gdn.venice.persistence.VenOrderStatusHistoryPK;

@Service
public class VenOrderStatusHistoryServiceImpl implements VenOrderStatusHistoryService {
	@Autowired
	VenOrderStatusHistoryDAO venOrderStatusHistoryDAO;
	@Autowired
	SeatOrderStatusHistoryDAO seatOrderStatusHistoryDAO;
	@Autowired
	SeatOrderEtdDAO seatOrderEtdDAO;
	
	@Override
	public void saveVenOrderStatusHistory(VenOrder venOrder) {
		String changeReason = "Updated by System";
		commonSaveVenOrderStatusHistory(venOrder, changeReason);	
	}
	
	@Override
	public void savePartialPartialFulfillmentVenOrderStatusHistory(VenOrder venOrder) {
		String changeReason = "Updated by System (Partial Fulfillment)";
		commonSaveVenOrderStatusHistory(venOrder, changeReason);
	} 
	
	private void commonSaveVenOrderStatusHistory(VenOrder venOrder, String changeReason){
		VenOrderStatusHistoryPK venOrderStatusHistoryPK = new VenOrderStatusHistoryPK();
		venOrderStatusHistoryPK.setOrderId(new Long(venOrder.getOrderId()));
		venOrderStatusHistoryPK.setHistoryTimestamp(new Timestamp(System.currentTimeMillis()));
		
		VenOrderStatusHistory orderStatusHistory = new VenOrderStatusHistory();
		orderStatusHistory.setId(venOrderStatusHistoryPK);
		orderStatusHistory.setStatusChangeReason(changeReason);
		orderStatusHistory.setVenOrderStatus(venOrder.getVenOrderStatus());
		
		venOrderStatusHistoryDAO.save(orderStatusHistory);
	}
	
	private void commonSaveSeatOrderStatusHistory(VenOrder venOrder){	
		if(venOrder.getVenOrderStatus().getOrderStatusId()==VenOrderStatusConstants.VEN_ORDER_STATUS_VA.code()
				|| venOrder.getVenOrderStatus().getOrderStatusId()==VenOrderStatusConstants.VEN_ORDER_STATUS_CS.code()){
			
			ArrayList<SeatOrderEtd> seatOrderEtd = seatOrderEtdDAO.findByWcsOrderId(venOrder.getWcsOrderId());				
			SeatOrderStatusHistory seatOrderStatusHistory = new SeatOrderStatusHistory();		
			seatOrderStatusHistory.setVenOrder(venOrder);
			seatOrderStatusHistory.setVenOrderStatus(venOrder.getVenOrderStatus());	
			if(seatOrderEtd!=null && seatOrderEtd.isEmpty()){
				seatOrderStatusHistory.setSeatOrderEtd(seatOrderEtd.get(0));
			}
			seatOrderStatusHistory.setUpdateStatusDate(new Timestamp(System.currentTimeMillis()));	
			
			seatOrderStatusHistoryDAO.save(seatOrderStatusHistory);
		}
		
		
	}
}
