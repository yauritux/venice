package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule3;

public interface FrdParameterRule3DAO extends JpaRepository<FrdParameterRule3, Long> {
	public static final String FIND_BY_UPPERCASEDCATEGORY_AND_QUANTITYMORETHANMINQUANTITY = 
		"SELECT o " +
		"FROM FrdParameterRule3 o " +
		"WHERE UPPER(o.category) = ?1 " +
		"AND ?2 > o.minQty";
	
	@Query(FIND_BY_UPPERCASEDCATEGORY_AND_QUANTITYMORETHANMINQUANTITY)
	public FrdParameterRule3 findByUpperCasedCategoryAndQuantityMoreThanMinQTY(String upperCasedCategory, int quantity);
}
