package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FrdRuleConfigTreshold;

public interface FrdRuleConfigTresholdDAO extends JpaRepository<FrdRuleConfigTreshold, Long> {
	public static final String FIND_BY_KEY_DAY_SPAN_FOR_FRAUD_PARAMETER10_SQL =
		"SELECT o " +
		"FROM FrdRuleConfigTreshold o " +
		"WHERE o.key = 'DAY_SPAN_FOR_FRAUD_PARAMETER10'";
	
	public static final String FIND_BY_KEY_DAY_SPAN_FOR_FRAUD_PARAMETER16_SQL = 
		"SELECT o " +
		"FROM FrdRuleConfigTreshold o " +
		"WHERE o.key = 'DAY_SPAN_FOR_FRAUD_PARAMETER16'";
	
	public static final String FIND_BY_KEY_DAY_SPAN_FOR_FRAUD_PARAMETER18_SQL = 
		"SELECT o " +
		"FROM FrdRuleConfigTreshold o " +
		"WHERE o.key = 'DAY_SPAN_FOR_FRAUD_PARAMETER18'";
	
	@Query(FIND_BY_KEY_DAY_SPAN_FOR_FRAUD_PARAMETER10_SQL)
	public FrdRuleConfigTreshold findByKeyDaySpanForFraudParameter10();
	
	@Query(FIND_BY_KEY_DAY_SPAN_FOR_FRAUD_PARAMETER16_SQL)
	public FrdRuleConfigTreshold findByKeyDaySpanForFraudParameter16();
	
	@Query(FIND_BY_KEY_DAY_SPAN_FOR_FRAUD_PARAMETER18_SQL)
	public FrdRuleConfigTreshold findByKeyDaySpanForFraudParameter18();
}
