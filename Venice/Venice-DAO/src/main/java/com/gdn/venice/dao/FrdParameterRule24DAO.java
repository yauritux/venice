package com.gdn.venice.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule24;

public interface FrdParameterRule24DAO extends JpaRepository<FrdParameterRule24, Long> {
	public static final String FIND_BY_ORDERAMOUNTRANGE =
		"SELECT o " +
		"FROM FrdParameterRule24 o " +
		"WHERE (?1 BETWEEN o.minValue AND o.maxValue AND o.maxValue > 0) OR " +
		"(?1 > o.minValue AND o.maxValue = 0)";
	
	@Query(FIND_BY_ORDERAMOUNTRANGE)
	public FrdParameterRule24 findByOrderAmountRange(BigDecimal orderAmount); 
}
