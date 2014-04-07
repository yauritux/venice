package com.gdn.venice.facade;

import javax.ejb.Local;

import com.gdn.venice.persistence.VenOrder;

@Local
public interface FraudCalculationSessionEJBLocal {
	public boolean calculateFraudRules(VenOrder venOrder);
	public String getDuplicateOrderReport();
}
