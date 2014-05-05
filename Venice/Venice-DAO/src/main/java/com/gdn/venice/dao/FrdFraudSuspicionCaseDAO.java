package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdFraudSuspicionCase;

public interface FrdFraudSuspicionCaseDAO extends JpaRepository<FrdFraudSuspicionCase, Long> {
	public static final String COUNT_BY_VENORDERID = 
		"SELECT COUNT(o) " +
		"FROM FrdFraudSuspicionCase o " +
		"WHERE o.venOrder.orderId = ?1 ";
	
	@Query(COUNT_BY_VENORDERID)
	public int countByVenOrderId(Long orderId);
	
	public FrdFraudSuspicionCase findByFraudSuspicionCaseId(Long fraudSuspicionCaseId);
	
}
