package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdFraudCaseHistory;

/**
 * 
 * @author yauritux
 *
 */
public interface FrdFraudCaseHistoryDAO extends JpaRepository<FrdFraudCaseHistory, Long> {

	public static final String FIND_BY_FRAUD_SUSPICION_CASE_ID = 
			"SELECT o FROM FrdFraudCaseHistory o join fetch o.frdFraudSuspicionCase WHERE o.frdFraudSuspicionCase.fraudSuspicionCaseId = ?1";
	
	@Query(FIND_BY_FRAUD_SUSPICION_CASE_ID)
	public List<FrdFraudCaseHistory> findByFraudSuspicionCaseId(Long fraudSuspicionCaseId);
}
