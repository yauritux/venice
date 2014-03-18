package com.gdn.venice.inbound.receivers.impl;

import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gdn.integration.jaxb.Order;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.receivers.OrderReceiver;
import com.gdn.venice.inbound.services.OrderService;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
@Service
@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
public class OrderReceiverImpl implements OrderReceiver {

	@Autowired
	private OrderService orderservice;
	
	private Order order;
	
	public OrderReceiverImpl() {}
	
	@Override
	public void setOrder(Order order){
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "setOrder::order = " + order);
		this.order = order;		
	}
	
	@Override
	public Boolean createOrder() throws VeniceInternalException {	
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::BEGIN");
		
		Boolean result = orderservice.createOrder(order);
		
		CommonUtil.logDebug(this.getClass().getCanonicalName(), "createOrder::result=" + result);
		
		return result;
	}

	@Override
	public Boolean updateOrder() {
		return Boolean.TRUE;
	}
	
}
