package com.gdn.venice.facade.spring;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.gdn.venice.facade.processor.MergeProcessor;
import com.gdn.venice.persistence.VenOrderItem;

@Service
public class VenOrderItemServiceImpl implements VenOrderItemService{
	@Resource(name="orderItemMergeProcessor")
	MergeProcessor processor;
	
	@Override
	public VenOrderItem mergeVenOrderItem(VenOrderItem venOrderItem) {
		
		venOrderItem = (VenOrderItem) processor.doMerge(venOrderItem);
		
		return venOrderItem;
	}

}
