package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule21;

public interface FrdParameterRule21DAO extends JpaRepository<FrdParameterRule21, Long> {
	
	public static final String FIND_BY_TIMERANGE =
		"SELECT o " +
		"FROM FrdParameterRule21 o " +
		"WHERE ?1 BETWEEN o.minTime AND o.maxTime";
	
	@Query(FIND_BY_TIMERANGE)
	public FrdParameterRule21 findByTimeRange(String orderTime);
}
