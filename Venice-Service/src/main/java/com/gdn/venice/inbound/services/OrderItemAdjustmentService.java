package com.gdn.venice.inbound.services;

import java.util.List;

import com.gdn.venice.exception.VeniceInternalException;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenOrderItemAdjustment;

/**
 * 
 * @author yauritux
 *
 */
public interface OrderItemAdjustmentService {

	public List<VenOrderItemAdjustment> persistOrderItemAdjustmentList(
			VenOrderItem venOrderItem, List<VenOrderItemAdjustment> venOrderItemAdjustmentList) 
			   throws VeniceInternalException;
	public VenOrderItemAdjustment synchronizeVenOrderItemAdjustmentReferenceData(
			VenOrderItemAdjustment venOrderItemAdjustment) 
			throws VeniceInternalException;	
}
