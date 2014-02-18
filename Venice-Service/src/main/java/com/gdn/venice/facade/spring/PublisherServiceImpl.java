package com.gdn.venice.facade.spring;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.venice.dao.LogAirwayBillDAO;
import com.gdn.venice.integration.outbound.Publisher;
import com.gdn.venice.persistence.LogAirwayBill;
import com.gdn.venice.persistence.VenOrderItem;

@Service
public class PublisherServiceImpl implements PublisherService {
	
	@PersistenceContext
    EntityManager em;
	
	@Autowired
	LogAirwayBillDAO logAirwayBillDAO;
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void publishUpdateOrderItemStatus(VenOrderItem newVenOrderItem){
		LogAirwayBill logAirwayBill = logAirwayBillDAO.findByOrderItemId(newVenOrderItem.getOrderItemId()).get(0);
		
		em.detach(newVenOrderItem);
		em.detach(logAirwayBill);
		
		Publisher publisher = new Publisher();
        publisher.publishUpdateOrderItemStatus(newVenOrderItem, logAirwayBill);
	}
}
