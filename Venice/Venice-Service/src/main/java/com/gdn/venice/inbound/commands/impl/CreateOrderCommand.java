package com.gdn.venice.inbound.commands.impl;

import com.gdn.venice.constants.LoggerLevel;
import com.gdn.venice.constants.VeniceExceptionConstants;
import com.gdn.venice.exception.OrderCreationFailedException;
import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.inbound.commands.Command;
import com.gdn.venice.inbound.receivers.OrderReceiver;
import com.gdn.venice.util.CommonUtil;

/**
 * 
 * @author yauritux
 *
 */
public class CreateOrderCommand implements Command {
	
	private OrderReceiver orderReceiver;
	
	public CreateOrderCommand(OrderReceiver receiver) {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "Constructor::Creating CreateOrderCommand instance, orderReceiver = " + receiver);
		orderReceiver = receiver;
	}
	
	@Override
	public void execute() throws VeniceInternalException {
		CommonUtil.logDebug(this.getClass().getCanonicalName()
				, "execute::::calling orderReceiver.createOrder()");
		try {
			orderReceiver.createOrder();
			CommonUtil.logDebug(this.getClass().getCanonicalName()
					, "orderReceiver.createOrder() successfully completed");
		} catch (VeniceInternalException vie) {
			CommonUtil.logError(this.getClass().getCanonicalName(), vie);
			throw CommonUtil.logAndReturnException(new OrderCreationFailedException(
					"Cannot create Order!", VeniceExceptionConstants.VEN_EX_000111)
			        , CommonUtil.getLogger(this.getClass().getCanonicalName()), LoggerLevel.ERROR);
		}
	}
}
