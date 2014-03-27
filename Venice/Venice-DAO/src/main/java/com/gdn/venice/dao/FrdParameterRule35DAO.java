package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdParameterRule35;

public interface FrdParameterRule35DAO extends JpaRepository<FrdParameterRule35, Long> {
	public static final String FIND_BY_UPPERCASECUSTOMERNAME =
		"SELECT o " +
		"FROM FrdParameterRule35 o " +
		"WHERE upper(o.customerName) = ?1";
	
	public static final String FIND_BY_UPPERCASECUSTOMEREMAIL = 
		"SELECT o " +
		"FROM FrdParameterRule35 o " +
		"WHERE upper(o.email) = ?1";
	
	@Query(FIND_BY_UPPERCASECUSTOMERNAME)
	public List<FrdParameterRule35> findByUpperCaseCustomerName(String uppercaseCustomerName);
	
	@Query(FIND_BY_UPPERCASECUSTOMEREMAIL)
	public List<FrdParameterRule35> findByUpperCaseCustomerEmail(String uppercaseCustomerEmail);
	
	public List<FrdParameterRule35> findByCcNumber(String ccNumber);
}
