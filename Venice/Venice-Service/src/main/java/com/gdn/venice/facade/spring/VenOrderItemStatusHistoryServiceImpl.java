package com.gdn.venice.facade.spring;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gdn.venice.dao.SeatOrderEtdDAO;
import com.gdn.venice.dao.SeatOrderStatusHistoryDAO;
import com.gdn.venice.dao.VenOrderItemStatusHistoryDAO;
import com.gdn.venice.persistence.SeatOrderEtd;
import com.gdn.venice.persistence.SeatOrderStatusHistory;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderItemStatusHistory;
import com.gdn.venice.persistence.VenOrderItemStatusHistoryPK;

@Service
public class VenOrderItemStatusHistoryServiceImpl implements
		VenOrderItemStatusHistoryService {

	@Autowired
	VenOrderItemStatusHistoryDAO venOrderItemStatusHistoryDAO;
	@Autowired
	SeatOrderStatusHistoryDAO seatOrderStatusHistoryDAO;
	@Autowired
	SeatOrderEtdDAO seatOrderEtdDAO;
	
	@Override
	public void saveVenOrderItemStatusHistory(VenOrderItem venOrderItem) {
		String changeReason = "Updated by System";
		commonSaveVenOrderItemStatusHistory(venOrderItem, changeReason);
		commonSaveSeatOrderStatusHistory(venOrderItem);
	}
	
	@Override
	public void savePartialPartialFulfillmentVenOrderItemStatusHistory(VenOrderItem venOrderItem) {
		String changeReason = "Updated by System (Partial Fulfillment)";
		commonSaveVenOrderItemStatusHistory(venOrderItem, changeReason);
		commonSaveSeatOrderStatusHistory(venOrderItem);
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
	
	private void commonSaveSeatOrderStatusHistory(VenOrderItem venOrderItem){	
		ArrayList<SeatOrderStatusHistory> seatOrderStatusHistoryList = seatOrderStatusHistoryDAO.findByOrderIdAndOrderItemId(venOrderItem.getVenOrder().getOrderId(),venOrderItem.getOrderItemId());	
		SeatOrderStatusHistory seatOrderStatusHistory = null;
		if(seatOrderStatusHistoryList.isEmpty()){
			SeatOrderEtd etdForSeattle = saveOrderETD(venOrderItem);
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
		seatOrderStatusHistoryDAO.save(seatOrderStatusHistory);
	}
	
	private  SeatOrderEtd saveOrderETD(VenOrderItem venOrderItem){
		SeatOrderEtd etdForSeattle = new SeatOrderEtd();        
	    etdForSeattle.setEtdMax(venOrderItem.getMaxEstDate());
	    etdForSeattle.setEtdMin(venOrderItem.getMinEstDate());
	    etdForSeattle.setDiffEtd(new BigDecimal(0));
	    etdForSeattle.setReason("Set From System");
	    etdForSeattle.setOther("Set From System");
	    etdForSeattle.setByUser("System");
	    etdForSeattle.setUpdateEtdDate(new Timestamp(System.currentTimeMillis()));
	    etdForSeattle.setWcsOrderId(venOrderItem.getVenOrder().getWcsOrderId());
	    etdForSeattle = seatOrderEtdDAO.save(etdForSeattle);
		return etdForSeattle;
	}
	

}
