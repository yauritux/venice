package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gdn.venice.persistence.FrdEmailType;

public interface FrdEmailTypeDAO extends JpaRepository<FrdEmailType, Long> {
	public static final String FIND_BY_UPPEREMAILSERVERPATTERN_SQL =
		"SELECT o " +
		"FROM FrdEmailType o " +
		"WHERE UPPER(o.emailServerPattern)= ?1";
	
	public FrdEmailType findByUpperMailServerPattern(String upperCaseEmailServer);
}
