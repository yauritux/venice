package com.gdn.venice.facade.spring.fraud.rule;

import com.gdn.venice.persistence.VenOrder;

public interface Rule {
	public int getRiskPoint(VenOrder order);
}
