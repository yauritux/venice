package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.FinArFundsInReport;

public interface FinArFundsInReportDAO extends JpaRepository<FinArFundsInReport, Long>{
	
	public static final String COUNT_BY_REPORTDESC = "SELECT COUNT(o) " +
	                                                 "FROM FinArFundsInReport o " +
	                                                 "WHERE o.reportDesc = ?1";
	
	@Query(COUNT_BY_REPORTDESC)
	public int countByReportDesc(String reportDesc);
}
