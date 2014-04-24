package com.gdn.venice.facade.processor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.dao.VenOrderItemDAO;
import com.gdn.venice.facade.spring.LogAirwayBillService;
import com.gdn.venice.facade.spring.PublisherService;
import com.gdn.venice.facade.spring.VenOrderItemStatusHistoryService;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.util.VeniceConstants;

@Service("orderItemMergeProcessor")
public class VenOrderItemMergeProcessor extends MergeProcessor{
	protected static Logger _log = Log4jLoggerFactory.getLogger("com.gdn.venice.facade.processor.VenOrderItemMergeProcessor");
	
	@PersistenceContext
    EntityManager em;
	
	@Autowired
	VenOrderItemStatusHistoryService venOrderItemStatusHistoryService;
	
	@Autowired
	LogAirwayBillService logAirwayBillService;

	@Autowired
	PublisherService publisherService;
	
	@Autowired
	VenOrderItemDAO venOrderItemDAO;
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public boolean preMerge(Object obj) {
		try{
			_log.debug("preMerge starts");
			
			VenOrderItem newVenOrderItem = (VenOrderItem) obj;
			
			em.detach(newVenOrderItem);
			
			VenOrderItem existingVenOrderItem = venOrderItemDAO
					.findWithVenOrderStatusByWcsOrderItemId(newVenOrderItem.getWcsOrderItemId());
			
			Long existingOrderItemStatusId = existingVenOrderItem.getVenOrderStatus().getOrderStatusId();
			Long newOrderItemStatusId = newVenOrderItem.getVenOrderStatus().getOrderStatusId();
			
			boolean isAllowedForPublish = isAllowedToPublishUpdateOrderItemStatusMessage(existingOrderItemStatusId, newOrderItemStatusId);
			boolean isAllowedToAddOrderItemStatusHistory = isAllowedToAddOrderItemStatusHistory(existingOrderItemStatusId, newOrderItemStatusId);
			boolean isAllowedToAddDummyLogAirwayBillForNewlyFPOrderItem = isAllowedToAddDummyLogAirwayBillForNewlyFPOrderItem(existingOrderItemStatusId, newOrderItemStatusId);
			
			if(isAllowedForPublish){
				_log.debug("publishUpdateOrderItemStatus");
				publisherService.publishUpdateOrderItemStatus(newVenOrderItem);
			}
			
			if(isAllowedToAddOrderItemStatusHistory){
				_log.debug("saveVenOrderItemStatusHistory");
				venOrderItemStatusHistoryService.saveVenOrderItemStatusHistory(newVenOrderItem);
			}
			
			if(isAllowedToAddDummyLogAirwayBillForNewlyFPOrderItem){
				_log.debug("addDummyLogAirwayBillForNewlyFPOrderItem");
				logAirwayBillService.addDummyLogAirwayBillForNewlyFPOrderItem(existingVenOrderItem);
			}
			
			return true;
		}catch(Exception e){
			_log.error("Exception on preMerge", e);
			
			return false;
		}
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public boolean merge(Object obj) {
		try{
			_log.debug("merge starts");
			
			VenOrderItem newVenOrderItem = (VenOrderItem) obj;
			
			venOrderItemDAO.save(newVenOrderItem);
			
			return true;
		}catch(Exception e){
			_log.error("Exception on merge", e);
			
			return false;
		}
	}

	@Override
	public boolean postMerge(Object obj) {
		return true;
	}
	
	private boolean isAllowedToPublishUpdateOrderItemStatusMessage(long existingOrderItemStatus, long newOrderItemStatus){
		if ((existingOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_PU && newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_ES)
                || (existingOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_ES && newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_PP)
                || (existingOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_ES && newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_CX)
                || (existingOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_PP && newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_CX)
                || (existingOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_ES && newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_RT)
                || (existingOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_ES && newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_D)
                || (existingOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_CX && newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_D)
                || (existingOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_PF && newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_FP)
                || (existingOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_CR && newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_FP)) {
			
			return true;
		}
		
		return false;
	}

	private boolean isAllowedToAddOrderItemStatusHistory(long existingOrderItemStatus, long newOrderItemStatus) {
		if(existingOrderItemStatus == newOrderItemStatus){
			return false;
		}
		
		 if (newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_PU
                 || newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_ES
                 || newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_PP
                 || newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_CX
                 || newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_RT
                 || newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_D
                 || newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_X) {
			 
			 return true;
		 }
		
		return false;
	}
	
	private boolean isAllowedToAddDummyLogAirwayBillForNewlyFPOrderItem(long existingOrderItemStatus, long newOrderItemStatus){
		
		if ((existingOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_C || existingOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_SF) 
				&& newOrderItemStatus == VeniceConstants.VEN_ORDER_STATUS_FP) {
			return true;
		}
		
		return false;
	}
	
}
