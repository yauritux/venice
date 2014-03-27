package com.gdn.venice.inbound.receivers.impl;

import com.gdn.integration.jaxb.Order;
import com.gdn.venice.inbound.receivers.OrderReceiver;

/**
 * 
 * @author yauritux
 *
 */
public class OrderVAReceiverImpl implements OrderReceiver {
	
	private Order order;
	
	@Override
	public void setOrder(Order order) {
		this.order = order;
	}
	
	public OrderVAReceiverImpl() {}

	@Override
	public Boolean createOrder() {
		return Boolean.TRUE;
	}

	@Override
	public Boolean updateOrder() {
		return Boolean.TRUE;
	}

}
