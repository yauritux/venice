package com.gdn.venice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gdn.venice.persistence.LogAirwayBillRetur;

public interface LogAirwayBillReturDAO extends JpaRepository<LogAirwayBillRetur, Long>{
	
public static final String COUNT_GDN_REF = "SELECT COUNT(ab) FROM LogAirwayBillRetur AS ab WHERE ab.gdnReference = ?1";
	
	@Query(COUNT_GDN_REF)
    public int countByGdnReference(String gdnReference);
}
