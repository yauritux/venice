package com.gdn.venice.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdParameterRule4;

public interface FrdParameterRule4DAO extends JpaRepository<FrdParameterRule4, Long> {
	
	public static final String FIND_BY_ORDERAMOUNT_BETWEENMINANDMAXVALUE = 
		"SELECT o " +
		"FROM FrdParameterRule4 o " +
		"WHERE ?1 BETWEEN o.minValue AND o.maxValue";
	
	public FrdParameterRule4 findByOrderAmountBetweenMinAndMaxValue(BigDecimal orderAmount);
	
}
