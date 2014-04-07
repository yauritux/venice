package com.gdn.venice.facade;

import javax.ejb.Remote;

import com.gdn.venice.persistence.VenOrder;

@Remote
public interface FraudCalculationSessionEJBRemote {
	public boolean calculateFraudRules(VenOrder venOrder);
	public String getDuplicateOrderReport();
}
