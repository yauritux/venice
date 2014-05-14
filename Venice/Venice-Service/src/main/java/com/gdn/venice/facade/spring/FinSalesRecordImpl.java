package com.gdn.venice.facade.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gdn.venice.facade.processor.MergeProcessor;
import com.gdn.venice.persistence.FinSalesRecord;

@Service
public class FinSalesRecordImpl implements FinSalesRecordService {
	@Autowired
	@Qualifier("finSalesRecordMergeProcessor")
	MergeProcessor processor;
	
	@Override
	public FinSalesRecord mergeFinSalesRecord(FinSalesRecord finSalesRecord){
		finSalesRecord = (FinSalesRecord) processor.doMerge(finSalesRecord);
		
		return finSalesRecord;
	}
	
}
