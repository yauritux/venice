package com.gdn.venice.inbound.services.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.SeatOrderEtdDAO;
import com.gdn.venice.inbound.services.SeatOrderEtdService;
import com.gdn.venice.persistence.SeatOrderEtd;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.CommonUtil;
/**
 * 
 * @author Arifin
 *
 */
@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRED)
public class SeatOrderEtdServiceImpl implements SeatOrderEtdService {

	@Autowired
	private SeatOrderEtdDAO seatOrderEtdDAO;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public SeatOrderEtd createSeatOrderEtd(VenOrderItem venOrderItem) {
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "createSeatOrderEtd::add order item status history");
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "createSeatOrderEtd::wcs order item id: "+venOrderItem.getWcsOrderItemId());
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "createSeatOrderEtd::order item status: "+venOrderItem.getVenOrderStatus().getOrderStatusCode());
			
			List<SeatOrderEtd>  etdForSeattleList = seatOrderEtdDAO.findBySku(venOrderItem.getVenMerchantProduct().getWcsProductSku());
			
			SeatOrderEtd etdForSeattle = new SeatOrderEtd();
			 etdForSeattle.setDiffEtd(new BigDecimal(0));
			if(etdForSeattleList.size()>0){
				if(etdForSeattleList.get(0).getEtdNew()!=null){
					Date newDate = getTimeafterAddDay(etdForSeattleList.get(0).getEtdNew(),venOrderItem.getLogisticsEtd()!=null?venOrderItem.getLogisticsEtd().intValue():0);
					if(newDate.compareTo(venOrderItem.getMaxEstDate())==new Long(1)){							
						etdForSeattle.setDiffEtd(new BigDecimal((Math.abs(newDate.getTime()-venOrderItem.getMaxEstDate().getTime())) / (24 * 60 * 60 * 1000)));											
					}
					etdForSeattle.setEtdNew(etdForSeattleList.get(0).getEtdNew());
					etdForSeattle.setEndDate(etdForSeattleList.get(0).getEndDate());
					etdForSeattle.setStartDate(etdForSeattleList.get(0).getStartDate());				
				}				
			}			      
		    etdForSeattle.setEtdMax(venOrderItem.getMaxEstDate());
		    etdForSeattle.setEtdMin(venOrderItem.getMinEstDate());
		    
		    etdForSeattle.setReason("Set From System");
		    etdForSeattle.setOther("Set From System");
		    etdForSeattle.setByUser("System");
		    etdForSeattle.setLogisticsEtd(venOrderItem.getLogisticsEtd());
		    etdForSeattle.setSku(venOrderItem.getVenMerchantProduct().getWcsProductSku());
		    etdForSeattle.setUpdateEtdDate(new Timestamp(System.currentTimeMillis()));
		    etdForSeattle.setWcsOrderId(venOrderItem.getVenOrder().getWcsOrderId());
		    etdForSeattle = seatOrderEtdDAO.save(etdForSeattle);
		
			CommonUtil.logDebug(this.getClass().getCanonicalName(), "done add Seat order item status history");
		
			
			return etdForSeattle;
	}
	
	 private Date getTimeafterAddDay(Date dateTime, int addDay){
		    Calendar cal = Calendar.getInstance();
		    cal.setTime(dateTime);
		    cal.add(Calendar.DATE, addDay);		 
		    return new Date(cal.getTime().getTime());
	 }
}
