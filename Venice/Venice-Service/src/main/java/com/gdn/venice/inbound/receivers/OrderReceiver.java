package com.gdn.venice.inbound.receivers;

import com.gdn.integration.jaxb.Order;
import com.gdn.venice.exception.VeniceInternalException;


/**
 * 
 * @author yauritux
 *
 */
public interface OrderReceiver {
	
	public void setOrder(Order order);
	public Boolean createOrder() throws VeniceInternalException;
	public Boolean updateOrder();
}
