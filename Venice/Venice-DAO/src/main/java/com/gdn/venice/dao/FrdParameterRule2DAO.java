package com.gdn.venice.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule2;

public interface FrdParameterRule2DAO extends JpaRepository<FrdParameterRule2, Long> {
	
	public static final String FIND_BY_ORDERAMOUNT = 
		"SELECT o " +
		"FROM FrdParameterRule2 o " +
		"WHERE " +
		" ?1 " +
		"BETWEEN o.minValue AND o.maxValue ";
	
	@Query(FIND_BY_ORDERAMOUNT)
	public FrdParameterRule2 findByOrderAmount(BigDecimal orderAmount);
	
}
