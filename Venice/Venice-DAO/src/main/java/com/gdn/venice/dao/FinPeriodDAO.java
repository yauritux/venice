package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FinPeriod;

public interface FinPeriodDAO extends JpaRepository<FinPeriod, Long> {
	public static final String FIND_CURRENT_PERIOD = "SELECT o " +
													 "FROM FinPeriod o " +
													 "WHERE CURRENT_DATE BETWEEN o.fromDatetime " +
													 "AND o.toDatetime";
	@Query(FIND_CURRENT_PERIOD)
	public FinPeriod findCurrentPeriod();
}
