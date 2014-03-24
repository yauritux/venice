package com.gdn.venice.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule22;

public interface FrdParameterRule22DAO extends JpaRepository<FrdParameterRule22, Long> {
	public static final String FIND_BY_AMOUNTRANGE = 
		"SELECT o " +
		"FROM FrdParameterRule22 o " +
		"WHERE (?1 BETWEEN o.minValue AND o.maxValue AND o.maxValue > 0) " +
		"OR (?1 > o.minValue AND o.maxValue = 0)";
	
	@Query(FIND_BY_AMOUNTRANGE)
	public FrdParameterRule22 findByAmountRange(BigDecimal amount);
	
}
