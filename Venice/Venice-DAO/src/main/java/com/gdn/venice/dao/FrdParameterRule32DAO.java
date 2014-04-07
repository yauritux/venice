package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule32;

public interface FrdParameterRule32DAO extends JpaRepository<FrdParameterRule32, Long> {
	public static final String FIND_BY_UPPERCASEPRODUCTTYPE_SQL = 
		"SELECT o " +
		"FROM FrdParameterRule32 o " +
		"WHERE UPPER(o.productType)= ?1";
	
	@Query(FIND_BY_UPPERCASEPRODUCTTYPE_SQL)
	public FrdParameterRule32 findByUpperCaseProductType(String productType);
}
