package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule10;

public interface FrdParameterRule10DAO extends JpaRepository<FrdParameterRule10, Long> {
	public static final String FIND_BY_PERCENTAGEUSAGE = 
		"SELECT o " +
		"FROM FrdParameterRule10 o " +
		"WHERE ?1 BETWEEN o.minPercentageUsage AND o.maxPercentageUsage";
	
	@Query(FIND_BY_PERCENTAGEUSAGE)
	public FrdParameterRule10 findByPercentageUsage(int percentage);
}
