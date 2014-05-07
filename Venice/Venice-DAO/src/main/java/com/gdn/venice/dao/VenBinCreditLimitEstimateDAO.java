package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.VenBinCreditLimitEstimate;

public interface VenBinCreditLimitEstimateDAO extends JpaRepository<VenBinCreditLimitEstimate, Long> {

	public static final String FIND_BY_ACTIVE_BINNUMBER_SQL =
		"SELECT o " +
		"FROM VenBinCreditLimitEstimate o " +
		"WHERE o.isActive = TRUE AND " +
		"o.binNumber = ?1 ";
	
	@Query(FIND_BY_ACTIVE_BINNUMBER_SQL)
	public VenBinCreditLimitEstimate findByActiveAndBinNumber(String binNumber);
	
}
