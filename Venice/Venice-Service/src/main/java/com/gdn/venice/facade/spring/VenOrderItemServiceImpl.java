package com.gdn.venice.facade.spring;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gdn.venice.facade.processor.MergeProcessor;
import com.gdn.venice.persistence.VenOrderItem;

@Service
public class VenOrderItemServiceImpl implements VenOrderItemService{
	@Autowired
	@Qualifier("orderItemMergeProcessor")
	MergeProcessor processor;
	
	@Override
	public VenOrderItem mergeVenOrderItem(VenOrderItem venOrderItem) {
		
		venOrderItem = (VenOrderItem) processor.doMerge(venOrderItem);
		
		return venOrderItem;
	}

}
