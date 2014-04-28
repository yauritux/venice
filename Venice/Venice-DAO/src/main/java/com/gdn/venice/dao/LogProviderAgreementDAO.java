package com.gdn.venice.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.LogProviderAgreement;

/**
 * 
 * @author yauritux
 *
 */
public interface LogProviderAgreementDAO extends JpaRepository<LogProviderAgreement, Long> {

	public static final String FIND_BY_LOGISTICS_PROVIDERCODE = 
			"SELECT o FROM LogProviderAgreement o " + 
	        "WHERE o.logLogisticsProvider.logisticsProviderCode = ?1";
	
	public List<LogProviderAgreement> findByLogisticsProviderCode(String logisticsProviderCode);
}
